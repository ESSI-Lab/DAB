package eu.essi_lab.downloader.usgs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.usgswatersrv.USGSClient;
import eu.essi_lab.accessor.usgswatersrv.USGSIdentifierMangler;
import eu.essi_lab.accessor.usgswatersrv.USGSMetadata;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSCounty;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Fabrizio
 */
public class USGSDataDownloader extends DataDownloader {

    private static final String USGS_DATADOWNLOADER_ERROR = "USGS_DATADOWNLOADER_ERROR";

    @Override
    public boolean canConnect() throws GSException {
	HashMap<String, String> codes = USGSCounty.getInstance().getProperties("01007");
	String code = codes.get("county_nm");
	return code != null && !code.isEmpty();
    }

    @Override
    public boolean canDownload() {
	String linkage = this.online.getLinkage();
	if (linkage != null && !linkage.isEmpty()) {
	    switch (linkage) {
	    case USGSClient.DEFAULT_IV_URL:
	    case USGSClient.DEFAULT_DV_URL:
		return true;
	    default:
		break;
	    }
	}
	return false;
    }

    @Override
    public boolean canSubset(String dimensionName) {
	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);

    }

    @Override
    protected void reduceDimension(DataDimension dataDimension) {
	if (dataDimension.getType().equals(DimensionType.TIME)) {
	    Number lower = dataDimension.getContinueDimension().getLower();
	    dataDimension.getContinueDimension().setUpper(lower.longValue());
	    dataDimension.getContinueDimension().setSize(1l);
	} else {
	    super.reduceDimension(dataDimension);
	}
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    USGSClient client = new USGSClient();
	    String name = this.online.getName();
	    List<DataDescriptor> ret = new ArrayList<>();
	    if (name != null && !name.isEmpty()) {
		String siteCode = null;
		String timeSeriesCode = null;
		String parameterCode = null;
		String statisticalCode = null;
		if (name.contains("@")) { // old format, to be deprecated
		    String[] split = name.split("@");
		    String s = split[0].replace("USGS:", "");
		    if (s.contains(";")) {
			String[] split2 = s.split(";");
			parameterCode = split2[0];
			statisticalCode = split2[1];
		    } else {
			timeSeriesCode = s;
		    }
		    siteCode = split[1].replace("USGS:", "");
		} else {
		    USGSIdentifierMangler mangler = new USGSIdentifierMangler();
		    mangler.setMangling(name);
		    siteCode = mangler.getSite();
		    timeSeriesCode = mangler.getTimeSeries();
		    parameterCode = mangler.getParameterIdentifier();
		    statisticalCode = mangler.getStatisticalCode();
		}

		GSLoggerFactory.getLogger(getClass()).info("Get series info {} {} {} {}", siteCode, timeSeriesCode, parameterCode,
			statisticalCode);

		USGSMetadata metadata = client.getSeriesInfoByTimeSeriesCode(siteCode, timeSeriesCode, parameterCode, statisticalCode);

		Double latitude = metadata.getLatitude();
		Double longitude = metadata.getLongitude();

		String coordinateDatum = metadata.get("dec_coord_datum_cd"); // Decimal Latitude-longitude datum:
		switch (coordinateDatum) {
		case "NAD27":
		    coordinateDatum = "EPSG:4267";
		    break;
		case "NAD83":
		    coordinateDatum = "EPSG:4269";
		    break;
		case "OLDAK":
		    // couldn't find a correspondent EPSG
		    break;
		case "OLDGUAM":
		    coordinateDatum = "EPSG:4675";
		    break;
		case "OLDHI":
		    coordinateDatum = "EPSG:4135";
		    break;
		case "OLDPR":
		    coordinateDatum = "EPSG:4139";
		    break;
		case "OLDSAMOA":
		    coordinateDatum = "EPSG:4169";
		    break;
		case "PUERTORICO":
		    coordinateDatum = "EPSG:4139";
		    break;
		case "WGS72":
		    coordinateDatum = "EPSG:4322";
		    break;
		case "WGS84":
		    coordinateDatum = "EPSG:4326";
		default:
		    break;
		}

		if (latitude != null && longitude != null && !coordinateDatum.equals("EPSG:4326")) {
		    try {
			CoordinateReferenceSystem sourceCRS = org.geotools.referencing.CRS.decode(coordinateDatum);
			CoordinateReferenceSystem targetCRS = org.geotools.referencing.CRS.decode(CRS.EPSG_4326().getIdentifier());
			MathTransform transform = org.geotools.referencing.CRS.findMathTransform(sourceCRS, targetCRS, false);
			Envelope sourceGeometry = new Envelope(longitude, longitude, latitude, latitude);
			Envelope targetGeometry = JTS.transform(sourceGeometry, transform);
			latitude = targetGeometry.getMinY();
			longitude = targetGeometry.getMinX();
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}

		DataDescriptor descriptor = new DataDescriptor();

		descriptor.setDataType(DataType.TIME_SERIES);

		descriptor.setDataFormat(DataFormat.WATERML_1_1()); // it supports also 2.0.. but we haven't yet the
								    // validator

		descriptor.setCRS(CRS.EPSG_4326());
		// descriptor.setEPSG4326SpatialDimensions(latitude, longitude);

		// only needed for the timezone info
		String site = client.getExpandedSeriesInfoByTimeSeriesCode(siteCode, timeSeriesCode, parameterCode, statisticalCode);

		if (site != null) {
		    USGSMetadata siteMetadata = new USGSMetadata(site);
		    String timeZoneCode = siteMetadata.getTimeZoneCode();
		    TimeZone timeZone = TimeZone.getTimeZone(timeZoneCode);

		    String begin = metadata.getBeginDate(); // Begin date: 1978-10-01
		    String end = metadata.getEndDate(); // End date: 1990-01-31

		    metadata.getCountNumber();

		    if (begin != null && end != null) {

			Date beginDate = getDate(begin, timeZone);
			Date endDate = getDate(end, timeZone);

			if (beginDate != null && endDate != null) {
			    descriptor.setTemporalDimension(beginDate, endDate);
			    DataDimension temporalDimension = descriptor.getTemporalDimension();
			    Long oneDayInMilliseconds = 1000 * 60 * 60 * 24l;
			    temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
			    temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);

			    String count = metadata.getCountNumber();
			    if (count != null && !count.equals("")) {
				try {
				    Long countLong = Long.parseLong(count);
				    temporalDimension.getContinueDimension().setSize(countLong);
				} catch (Exception e) {
				}
			    }
			}

		    }
		}

		ret.add(descriptor);

	    }
	    return ret;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).info("USGS Data Downloader unexpected error");
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to downalod file", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    USGS_DATADOWNLOADER_ERROR, //
		    e);
	}
    }

    private Date getDate(String str, TimeZone timeZone) {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	sdf.setTimeZone(timeZone);
	try {
	    return sdf.parse(str);
	} catch (ParseException e) {
	}
	sdf = new SimpleDateFormat("yyyy");
	sdf.setTimeZone(timeZone);
	try {
	    return sdf.parse(str);
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	String linkage = this.online.getLinkage();

	USGSClient client;

	if (linkage.contains("nwis/iv")) {
	    client = new USGSClient(USGSClient.DEFAULT_SITE_URL, linkage, null);
	} else {
	    client = new USGSClient(USGSClient.DEFAULT_SITE_URL, null, linkage);
	}

	TimeZone timezone = TimeZone.getTimeZone("GMT");

	String name = this.online.getName();
	if (name != null && !name.isEmpty()) {
	    String siteCode = null;
	    String timeSeriesCode = null;
	    String parameterCode = null;
	    String statisticalCode = null;
	    if (name.contains("@")) { // old format, to be deprecated
		String[] split = name.split("@");
		String s = split[0].replace("USGS:", "");
		if (s.contains(";")) {
		    String[] split2 = s.split(";");
		    parameterCode = split2[0];
		    statisticalCode = split2[1];
		} else {
		    timeSeriesCode = s;
		}
		siteCode = split[1].replace("USGS:", "");
	    } else {
		USGSIdentifierMangler mangler = new USGSIdentifierMangler();
		mangler.setMangling(name);
		siteCode = mangler.getSite();
		timeSeriesCode = mangler.getTimeSeries();
		parameterCode = mangler.getParameterIdentifier();
		statisticalCode = mangler.getStatisticalCode();
	    }

	    String site = client.getExpandedSeriesInfoByTimeSeriesCode(siteCode, timeSeriesCode, parameterCode, statisticalCode);

	    if (site != null) {
		USGSMetadata siteMetadata = new USGSMetadata(site);
		String timeZoneCode = siteMetadata.getTimeZoneCode();
		if (timeZoneCode != null && !timeZoneCode.equals("")) {
		    timezone = TimeZone.getTimeZone(timeZoneCode);
		}
	    }

	    DataDimension dimension = descriptor.getTemporalDimension();
	    Date begin = null;
	    Date end = null;

	    if (dimension != null && dimension instanceof ContinueDimension) {
		Unit uom = ((ContinueDimension) dimension).getUom();

		if (uom != null) {
		    if (uom.equals(Unit.SECOND) || uom.equals(Unit.MILLI_SECOND)) {
			long factor = 1;
			if (uom.equals(Unit.SECOND)) {
			    factor = 1000;
			}
			ContinueDimension continueDimension = (ContinueDimension) dimension;
			begin = new Date(continueDimension.getLower().longValue() * factor);
			end = new Date(continueDimension.getUpper().longValue() * factor);
		    }

		}
	    }

	    InputStream stream;

	    if (linkage.contains("nwis/iv")) {
		stream = client.getInstantantaneousValues(timeSeriesCode, begin, end, timezone, parameterCode);

		// WaterML format returns all the time series from a site (there is a bug in usgs service)
		// (WaterML 2.0 doesn't have this problem)
		// however, since for the moment we use WaterML, in the following line we remove the unwanted
		// series.
		try {
		    TimeSeriesResponseType timeSeriesResponse = JAXBWML.getInstance().parseTimeSeries(stream);
		    stream.close();
		    
		    // filter series by method id
		    int methodId = Integer.parseInt(timeSeriesCode);
		    JAXBWML.getInstance().filterSeriesByMethodId(timeSeriesResponse, methodId);
		    // augment
		    WMLDataDownloader.augmentSiteInfo((SiteInfoType) timeSeriesResponse.getTimeSeries().get(0).getSourceInfo(), resource);
		    
		    
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    JAXBWML.getInstance().marshal(timeSeriesResponse, baos);
		    byte[] bytes = baos.toByteArray();
		    baos.close();
		    stream = new ByteArrayInputStream(bytes);

		} catch (Exception e) {
		    // TODO: handle exception
		}

	    } else {
		stream = client.getDailyValues(siteCode, parameterCode, statisticalCode, begin, end, timezone);
	    }

	    try {

		//

		return IOStreamUtils.tempFilefromStream(stream, "usgsdatadownloader", null);
	    } catch (IOException e) {

		throw GSException.createException(//
			getClass(), //
			"Unable to downalod file from linkage: " + linkage + " and name: " + name + ". Ex.message: " + e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			USGS_DATADOWNLOADER_ERROR, //
			e);
	    }

	}

	throw GSException.createException(//
		getClass(), //
		"Unable to downalod file from linkage: " + linkage + " and name: " + name, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		USGS_DATADOWNLOADER_ERROR);
    }
}
