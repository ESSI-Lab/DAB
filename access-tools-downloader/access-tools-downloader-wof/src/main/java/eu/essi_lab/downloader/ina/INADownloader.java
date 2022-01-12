package eu.essi_lab.downloader.ina;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.ina.INAConnector;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesINAResponseDocument;
import eu.essi_lab.accessor.wof.utils.WOFIdentifierMangler;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class INADownloader extends DataDownloader {

    private INAConnector connector;

    private static final String INA_READER_AS_STREAM_ERROR = "INA_READER_AS_STREAM_ERROR";
    private static final String INA_DOWNLOAD_ERROR = "INA_DOWNLOAD_ERROR";

    @Override
    public boolean canConnect() throws GSException {
	SitesResponseDocument sites = connector.getSites();
	return !sites.getSites().isEmpty();
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	String linkage = online.getLinkage();
	this.connector = new INAConnector();
	this.connector.setSourceURL(linkage);
    }

    protected INAConnector getConnector() {
	return connector;
    }

    protected void setConnector(INAConnector connector) {
	this.connector = connector;
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());
	String name = online.getName();
	// we expect a INA Server online resource name, as encoded by the
	// WOFIdentifierMangler
	if (name != null) {
	    WOFIdentifierMangler mangler = new WOFIdentifierMangler();
	    mangler.setMangling(name);
	    String site = mangler.getPlatformIdentifier();
	    String variable = mangler.getParameterIdentifier();
	    if (site.contains(":") && variable.contains(":")) {
		String siteNetwork = site.split(":")[0];
		String siteCode = site.split(":")[1];

		String variableCode = variable.split(":")[1];
		SitesResponseDocument siteInfo = getConnector().getSiteInfo(siteCode);
		if (siteInfo == null) {
		    GSLoggerFactory.getLogger(getClass()).error("XINA no site info: {}",siteCode);
		}
		List<SiteInfo> sitesInfo = siteInfo.getSitesInfo();

		if (sitesInfo == null) {
		    try {
			GSLoggerFactory.getLogger(getClass()).error("XINA no sites info " + siteInfo.getReader().asString());
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}

		if (sitesInfo.isEmpty()) {
		    try {
			GSLoggerFactory.getLogger(getClass()).error("XINA Empty sites info " + siteInfo.getReader().asString());
		    } catch (UnsupportedEncodingException | TransformerException e) {
			e.printStackTrace();
		    }
		}
		SiteInfo mySite = sitesInfo.get(0);

		Double lat = Double.parseDouble(mySite.getLatitude());
		Double lon = Double.parseDouble(mySite.getLongitude());

		descriptor.setEPSG4326SpatialDimensions(lat, lon);

		String methodId = mangler.getMethodIdentifier();
		String qualityControlLevelCode = mangler.getQualityIdentifier();
		String sourceId = mangler.getSourceIdentifier();
		TimeSeries timeSeries = mySite.getSeries(variableCode, methodId, qualityControlLevelCode, sourceId);
		Date begin = timeSeries.getBeginTimePositionDate();
		Date end = timeSeries.getEndTimePositionDate();

		descriptor.setTemporalDimension(begin, end);
		DataDimension temporalDimension = descriptor.getTemporalDimension();

		boolean regularTimeDimension = timeSeries.isTimeScaleRegular();
		Number resolution = timeSeries.getTimeScaleTimeSupport();
		if (regularTimeDimension && resolution != null && Math.abs(resolution.doubleValue()) > 0.00000000000001) {
		    String unitCode = timeSeries.getTimeScaleUnitCode();
		    switch (unitCode) {
		    case "100": // second
			resolution = getUpdatedResolution(resolution, 1000l);
			break;
		    case "101": // millisecond
			// nothing to do
			resolution = getUpdatedResolution(resolution, 1l);
			break;
		    case "102": // minute
			resolution = getUpdatedResolution(resolution, 1000 * 60l);
			break;
		    case "103": // hour
			resolution = getUpdatedResolution(resolution, 1000 * 60 * 60l);
			break;
		    case "104": // day
			resolution = getUpdatedResolution(resolution, 1000 * 60 * 60 * 24l);
			break;
		    case "105": // week
			resolution = getUpdatedResolution(resolution, 1000 * 60 * 60 * 24 * 7l);
			break;
		    case "106": // month
			resolution = getUpdatedResolution(resolution, 1000 * 60 * 60 * 24 * 30l);
			break;
		    case "107": // year
			resolution = getUpdatedResolution(resolution, 1000 * 60 * 60 * 24 * 365l);
			break;
		    case "108": // leap year
			resolution = getUpdatedResolution(resolution, 1000 * 60 * 60 * 24 * 366l);
			break;
		    case "109": // Gregorian year
			resolution = getUpdatedResolution(resolution, 1000 * 60 * 60 * 24 * 365.2425);
			break;
		    default:
			break;
		    }

		    temporalDimension.getContinueDimension().setResolution(resolution);		    // temporalDimension.getContinueDimension().setSize(valueCount);

		    // Long valueCount = timeSeries.getValueCount();
		    // // a check also on value count is needed to assure that the time series has a
		    // regular resolution,
		    // // there are cases (e.g. USU4 of little bear river that the resolution is
		    // specified as regular,
		    // but
		    // // the specified temporal domain isn't completely filled with values
		    // if (valueCount != null) {
		    // long expectedResolution = (end.getTime() - begin.getTime()) / (valueCount -
		    // 1);
		    // if (expectedResolution == resolution.longValue()) {
		    // temporalDimension.getContinueDimension().setResolution(resolution);
		    // temporalDimension.getContinueDimension().setSize(valueCount);
		    // }
		    // }

		}

		String elevation = mySite.getElevationMetres();
		if (elevation != null && !elevation.isEmpty()) {
		    double elevationDouble = Double.parseDouble(elevation);
		    descriptor.setVerticalDimension(elevationDouble, elevationDouble);
		    String datum = mySite.getVerticalDatum();
		    if (datum != null && !datum.isEmpty()) {
			ContinueDimension verticalDimension = descriptor.getOtherDimensions().get(0).getContinueDimension();
			verticalDimension.setDatum(new Datum(datum));
		    }
		}

		ret.add(descriptor);
	    }
	}
	return ret;

    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	String name = online.getName();
	// we expect a INA Server online resource name in the form as encoded by
	// WOFIdentifierMangler
	if (name != null) {
	    WOFIdentifierMangler mangler = new WOFIdentifierMangler();
	    mangler.setMangling(name);
	    String site = mangler.getPlatformIdentifier();
	    String variable = mangler.getParameterIdentifier();
	    if (site.contains(":") && variable.contains(":")) {
		String networkName = site.split(":")[0];
		String siteCode = site.split(":")[1];

		String variableCode = variable.split(":")[1];

		DataDimension dimension = descriptor.getTemporalDimension();
		Date begin = null;
		Date end = null;
		if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		    ContinueDimension sizedDimension = dimension.getContinueDimension();
		    begin = new Date(sizedDimension.getLower().longValue());
		    end = new Date(sizedDimension.getUpper().longValue());
		}
		String methodId = mangler.getMethodIdentifier();
		String qualityControlLevelCode = mangler.getQualityIdentifier();
		String sourceId = mangler.getSourceIdentifier();

		TimeSeriesINAResponseDocument values = null;
		if (begin != null) {
		    values = getConnector().getValues(networkName, siteCode, variableCode, methodId, qualityControlLevelCode, sourceId,
			    begin, end);
		}
		// It seems that date is mandatory
		else {
		    Date endDate = new Date();
		    Date beginDate = new Date(endDate.getTime() - 1 * 24 * 60 * 60 * 1000);
		    values = getConnector().getValues(networkName, siteCode, variableCode, methodId, qualityControlLevelCode, sourceId,
			    beginDate, endDate);
		}

		try {
		    if (values != null) {

			XMLDocumentReader reader = values.getReader();
			XMLDocumentWriter writer = new XMLDocumentWriter(reader);
			writer.rename("//*:siteInfo", "sourceInfo");
			writer.rename("//*:unitAbreviation", "unitAbbreviation");
			writer.rename("//*:siteProperty/@title", "name");
			writer.rename("(//*:unit)[last()]", "units");
			String[] pairs = new String[2];
			pairs[0] = "xsi:type";
			pairs[1] = "SiteInfoType";
			writer.addAttributes("//*:sourceInfo", pairs);
			InputStream input = reader.asStream();

			ClonableInputStream cloneInputStream = new ClonableInputStream(input);
			// file to check if download is ok
			// File tmpFile = File.createTempFile("TEST-INA-downloader", ".wml");
			// FileUtils.copyInputStreamToFile(cloneInputStream.clone(), tmpFile);

			return IOStreamUtils.tempFilefromStream(cloneInputStream.clone(), "INA-downloader", ".wml");
		    }

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", name, e);

		    throw GSException.createException(//
			    getClass(), //
			    e.getMessage(), //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    INA_READER_AS_STREAM_ERROR, //
			    e);
		}
	    }
	}

	throw GSException.createException(//
		getClass(), //
		"Error occurred, unable to download data", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		INA_DOWNLOAD_ERROR);

    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocols.CUAHSI_WATER_ONE_FLOW_1_1.getCommonURN()));
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    private Long getUpdatedResolution(Number resolution, Number i) {
	if (resolution instanceof Long && i instanceof Long) {
	    return (Long) resolution * (Long) i;
	}
	return Math.round(resolution.doubleValue() * i.doubleValue());
    }

}
