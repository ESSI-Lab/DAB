package eu.essi_lab.downloader.hiscentral;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.toscana.HISCentralToscanaConnector;
import eu.essi_lab.accessor.hiscentral.toscana.HISCentralToscanaIdentifierMangler;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.wml._2.WML2QualityCategory;

public class HISCentralToscanaDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_TOSCANA_DOWNLOAD_ERROR = "HISCENTRAL_TOSCANA_DOWNLOAD_ERROR";

    private static final BigDecimal NO_DATA_VALUE = new BigDecimal("-9999.0");

    private Downloader downloader = new Downloader();

    private HISCentralToscanaConnector connector = new HISCentralToscanaConnector();

    @Override
    public boolean canConnect() throws GSException {

	try {
	    return HttpConnectionUtils.checkConnectivity(online.getLinkage());
	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.connector.setSourceURL(resource.getSource().getEndpoint());
    }

    @Override
    public boolean canDownload() {

	return (online.getLinkage().contains("www.sir.toscana.it") && online.getProtocol() != null
		&& online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_TOSCANA_NS_URI));

    }

    @Override
    public boolean canSubset(String dimensionName) {
	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);

    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());
	String name = online.getName();
	// we expect a CUAHSI Hydro Server online resource name, as encoded by the WOFIdentifierMangler
	if (name != null) {
	    HISCentralToscanaIdentifierMangler mangler = new HISCentralToscanaIdentifierMangler();
	    mangler.setMangling(name);
	    String site = mangler.getPlatformIdentifier();
	    String variable = mangler.getParameterIdentifier();
	    // String method = mangler.getMethodIdentifier();
	    // String quality = mangler.getQualityIdentifier();
	    String source = mangler.getSourceIdentifier();
	    if (site.contains(":")) {
		String siteNetwork = site.split(":")[0];
		String siteCode = site.split(":")[1];

		// String variableCode = variable.split(":")[1];
		// SitesResponseDocument siteInfo = getConnector().getSiteInfo(siteNetwork, siteCode);
		//
		// SiteInfo mySite = siteInfo.getSitesInfo().get(0);

		String url = online.getLinkage();

		GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

		Double lat = bbox.getNorth();
		Double lon = bbox.getEast();

		descriptor.setEPSG4326SpatialDimensions(lat, lon);
		descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
		descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);

		// because some HIS services like hydroportal.cuahsi.org have truncated lat-lon values (in the order of
		// the .01 part)
		// in the actual data (while more precise data in the site info) TODO: think if it is acceptable
		descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
		descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
		descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
		descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);

		TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();

		if (extent != null) {

		    String startDate = extent.getBeginPosition();
		    String endDate = extent.getEndPosition();
		    if (extent.isEndPositionIndeterminate()) {
			endDate = ISO8601DateTimeUtils.getISO8601DateTime();
		    }

		    Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate);
		    Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endDate);

		    if (optionalBegin.isPresent() && optionalEnd.isPresent()) {
			Date begin = optionalBegin.get();
			Date end = optionalEnd.get();
			descriptor.setTemporalDimension(begin, end);
			DataDimension temporalDimension = descriptor.getTemporalDimension();
			Long oneDayInMilliseconds = 1000 * 60 * 60 * 24l;
			temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
			temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
		    }
		}

		VerticalExtent verticalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification()
			.getVerticalExtent();

		if (verticalExtent != null) {
		    double elevationDouble = verticalExtent.getMinimumValue();
		    descriptor.setVerticalDimension(elevationDouble, elevationDouble);
		    // String datum = mySite.getVerticalDatum();
		    // if (datum != null && !datum.isEmpty()) {
		    // ContinueDimension verticalDimension =
		    // descriptor.getOtherDimensions().get(0).getContinueDimension();
		    // verticalDimension.setDatum(new Datum(datum));
		}

		ret.add(descriptor);
	    }
	}
	return ret;

    }

    private Long getUpdatedResolution(Number resolution, Number i) {
	if (resolution instanceof Long && i instanceof Long) {
	    return (Long) resolution * (Long) i;
	}
	return Math.round(resolution.doubleValue() * i.doubleValue());
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {

	try {

	    String name = online.getName();

	    // we expect a HISCentralToscana online resource name in the form as encoded by
	    // HISCentralToscanaIdentifierMangler
	    if (name != null) {
		HISCentralToscanaIdentifierMangler mangler = new HISCentralToscanaIdentifierMangler();
		mangler.setMangling(name);
		String site = mangler.getPlatformIdentifier();
		String variable = mangler.getParameterIdentifier();
		// String methodId = mangler.getMethodIdentifier();
		// String qualityControlLevelCode = mangler.getQualityIdentifier();
		String sourceId = mangler.getSourceIdentifier();
		if (site.contains(":")) {
		    String networkName = site.split(":")[0];
		    String siteCode = site.split(":")[1];

		    // String variableCode = variable.split(":")[1];

		    DataDimension dimension = targetDescriptor.getTemporalDimension();
		    Date begin = null;
		    Date end = null;
		    // if (dimension != null && dimension.getContinueDimension().getUom() != null
		    // && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		    // ContinueDimension continueDimension = dimension.getContinueDimension();
		    //
		    // DataDescriptor remoteDescriptor = null;
		    //
		    // Number lower = continueDimension.getLower();
		    // LimitType lowerType = continueDimension.getLowerType();
		    //
		    // Number upper = continueDimension.getUpper();
		    // LimitType upperType = continueDimension.getUpperType();
		    //
		    // if (lowerType == null || !lowerType.equals(LimitType.ABSOLUTE) || //
		    // upperType == null || !upperType.equals(LimitType.ABSOLUTE)) {
		    // // the remote descriptor is retrieved only in case an absolute value
		    // // has not been provided
		    // remoteDescriptor = getRemoteDescriptors().get(0);
		    // }
		    //
		    // lower = getActualLimit(lower, lowerType, remoteDescriptor);
		    // begin = new Date(lower.longValue());
		    //
		    // upper = getActualLimit(upper, upperType, remoteDescriptor);
		    // end = new Date(upper.longValue());
		    // }

		    TimeSeriesResponseType jtst = getJaxbTimeSeriesTemplate();
		    jtst.getTimeSeries().get(0).getVariable().setNoDataValue(NO_DATA_VALUE.doubleValue());
		    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(jtst, getClass().getSimpleName(), ".wml");

		    // DataDimension dimension = descriptor.getTemporalDimension();
		    // Date begin = null;
		    // Date end = null;
		    String startString = null;
		    String endString = null;
		    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
			ContinueDimension sizedDimension = dimension.getContinueDimension();
			begin = new Date(sizedDimension.getLower().longValue());
			end = new Date(sizedDimension.getUpper().longValue());
			startString = ISO8601DateTimeUtils.getISO8601Date(begin);
			endString = ISO8601DateTimeUtils.getISO8601Date(end);
		    }

		    if (startString == null || endString == null) {
			startString = ISO8601DateTimeUtils.getISO8601Date(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L));
			endString = ISO8601DateTimeUtils.getISO8601Date(new Date());
		    }

		    Optional<String> dataResponse = downloader.downloadOptionalString(online.getLinkage());

		    if (dataResponse.isPresent()) {

			DateFormat iso8601OutputFormat = null;
			DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

			JSONObject jsonObj = new JSONObject(dataResponse.get());
			JSONObject propertiesData = jsonObj.optJSONObject("properties");
			if (propertiesData != null) {
			    // unitName = getString(propertiesData, "UnitaMisura");
			    JSONArray dataArray = propertiesData.optJSONArray("SerieDati");
			    int dataLength = dataArray.length();

			    for (Object arr : dataArray) {
				JSONObject data = (JSONObject) arr;

				// String date = data.optString("Data");
				// date = date.contains(" ") ? date.replace(" ", "T") : date;
				String valueString = data.optString("Valore");
				String tipoString = data.optString("TipoValore", "");
				WML2QualityCategory wml2Quality = null;
				switch (tipoString) {
				case "@":
				    wml2Quality = WML2QualityCategory.MISSING;
				    break;
				case "N":
				    wml2Quality = WML2QualityCategory.UNCHECKED;
				    break;
				case "V":
				    wml2Quality = WML2QualityCategory.GOOD;
				    break;
				default:
				    break;
				}
				ValueSingleVariable v = new ValueSingleVariable();

				// XMLNodeReader reader = new XMLNodeReader(n);
				// String variableValue = reader.evaluateString("*:" + variableName);
				if (valueString != null) {
				    // String date = reader.evaluateString("*:DataHora");
				    String date = data.optString("Data");
				    if (iso8601OutputFormat == null) {
					iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				    }
				    Date parsed = iso8601OutputFormat.parse(date);
				    BigDecimal dataValue = null;
				    if (!valueString.isEmpty()) {
					dataValue = new BigDecimal(valueString);
					v.setValue(dataValue);
				    } else {
					v.setValue(NO_DATA_VALUE);
				    }
				    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
				    c.setTime(parsed);
				    XMLGregorianCalendar date2 = xmlFactory.newXMLGregorianCalendar(c);
				    v.setDateTimeUTC(date2);
				    if (wml2Quality != null) {
					v.setQualityControlLevelCode(wml2Quality.getUri());
				    } else {
					v.setQualityControlLevelCode(tipoString);
				    }
				    addValue(tsrt, v);
				}
			    }
			}

			return tsrt.getDataFile();
		    }

		    // TimeSeriesResponseDocument values;
		    // if (begin != null) {
		    // values = getConnector().getValues(networkName, siteCode, variableCode, methodId,
		    // qualityControlLevelCode, sourceId,
		    // begin, end);
		    // } else {
		    // values = getConnector().getValues(networkName, siteCode, variableCode, methodId,
		    // qualityControlLevelCode, sourceId);
		    // }
		    //
		    // try {
		    // return IOStreamUtils.tempFilefromStream(values.getReader().asStream(), "CUAHSI-downloader",
		    // ".xml");
		    //
		    // } catch (Exception e) {
		    //
		    // GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", name, e);
		    //
		    // throw GSException.createException(//
		    // getClass(), //
		    // e.getMessage(), //
		    // null, //
		    // ErrorInfo.ERRORTYPE_INTERNAL, //
		    // ErrorInfo.SEVERITY_ERROR, //
		    // HISCENTRAL_TOSCANA_READER_AS_STREAM_ERROR, //
		    // e);
		    // }
		}
	    }
	} catch (Exception e) {

	}

	throw GSException.createException(//
		getClass(), //
		"Error occurred, unable to download data", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		HISCENTRAL_TOSCANA_DOWNLOAD_ERROR);

    }

    private Number getActualLimit(Number limit, LimitType limitType, DataDescriptor remoteDescriptor) {
	switch (limitType) {
	case MINIMUM:
	    return remoteDescriptor.getTemporalDimension().getContinueDimension().getLower();
	case MAXIMUM:
	    return remoteDescriptor.getTemporalDimension().getContinueDimension().getUpper();
	case ABSOLUTE:
	default:
	    return limit;
	}
    }

}
