package eu.essi_lab.downloader.hiscentral;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.valdaosta.HISCentralValdaostaConnector;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
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
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Roberto
 */

/**
 * GET DATA REQUEST EXMPLE:
 * https://cf-api.regione.vda.it/ws2/data_frame_time_series/1/1661212800/1661256000
 */

public class HISCentralValdaostaDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_VALDAOSTA_DOWNLOAD_ERROR = "HISCENTRAL_VALDAOSTA_DOWNLOAD_ERROR";

    private HISCentralValdaostaConnector connector;
    private Downloader downloader;

    /**
     * 
     */
    public HISCentralValdaostaDownloader() {

	connector = new HISCentralValdaostaConnector();
	downloader = new Downloader();
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	//
	// spatial extent
	//
	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

	Double lat = bbox.getNorth();
	Double lon = bbox.getEast();

	descriptor.setEPSG4326SpatialDimensions(lat, lon);
	descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
	descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);
	descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);

	//
	// temp extent
	//
	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();

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

	ret.add(descriptor);

	return ret;
    }

    @Override
    public File download(DataDescriptor targetDescriptor) throws GSException {

	Exception ex = null;

	try {

	    Date begin = null;
	    Date end = null;

	    ObjectFactory factory = new ObjectFactory();

	    Long startTime = null;
	    Long endTime = null;

	    DataDimension dimension = targetDescriptor.getTemporalDimension();

	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {

		ContinueDimension sizedDimension = dimension.getContinueDimension();

		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());

	    }

	    if (begin == null || end == null) {

		begin = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
		end = new Date();
	    }

	    startTime = convertDate(begin);
	    endTime = convertDate(end);
	    String link = online.getLinkage().contains("data_time_series/") ? online.getLinkage().split("data_time_series/")[0]
		    : online.getLinkage();
	    String id = online.getName().split("_")[1];
	    String linkage = link + "data_frame_time_series/" + id + "/" + startTime + "/" + endTime;

	    JSONObject jsonObj = getData(linkage);

	    if (jsonObj != null) {

		JSONArray valuesData = jsonObj.optJSONArray("data");

		TimeSeriesResponseType tsrt = getTimeSeriesTemplate();

		DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

		DateFormat iso8601OutputFormat = null;

		for (Object arr : valuesData) {

		    JSONObject data = (JSONObject) arr;

		    Double value = data.optDouble("measure");// data.optString("value");

		    ValueSingleVariable variable = new ValueSingleVariable();

		    BigDecimal missingValue = new BigDecimal("-9999.0");

		    BigDecimal dataValue = null;
		    if (value == null || value.isNaN()) {
			dataValue = missingValue;
		    } else {
			dataValue = new BigDecimal(value);
		    }

		    //
		    // value
		    //

		    variable.setValue(dataValue);

		    //
		    // date
		    //

		    String date = data.optString("temporal-extent");// data.optString("datetime");
		    String d = date.contains("/") ? date.split("\\/")[0] : date;
		    if (iso8601OutputFormat == null) {
			iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ITALIAN);
			iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		    }

		    Date parsed = iso8601OutputFormat.parse(d);

		    GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    gregCal.setTime(parsed);

		    XMLGregorianCalendar xmlGregCal = xmlFactory.newXMLGregorianCalendar(gregCal);
		    variable.setDateTimeUTC(xmlGregCal);

		    //
		    //
		    //

		    addValue(tsrt, variable);

		}

		JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
		File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");

		tmpFile.deleteOnExit();
		JAXBWML.getInstance().marshal(response, tmpFile);

		return tmpFile;
	    }

	} catch (Exception e) {

	    ex = e;
	}

	throw GSException.createException(//
		getClass(), //
		ex.getMessage(), //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		HISCENTRAL_VALDAOSTA_DOWNLOAD_ERROR);

    }

    private JSONObject getData(String linkage) throws GSException {
	GSLoggerFactory.getLogger(getClass()).info("Getting BEARER TOKEN from Valle d'Aosta Datascape service");

	String result = null;
	String token = null;

	try {

	    if (HISCentralValdaostaConnector.BEARER_TOKEN == null) {
		HISCentralValdaostaConnector.BEARER_TOKEN = HISCentralValdaostaConnector.getBearerToken(connector.TOKEN_URL);
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Getting " + linkage);

	    int timeout = 120;
	    int responseTimeout = 200;
	    InputStream stream = null;

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, timeout);
	    downloader.setResponseTimeout(TimeUnit.SECONDS, responseTimeout);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    linkage.trim(), //
		    HttpHeaderUtils.build("Authorization", "Bearer " + HISCentralValdaostaConnector.BEARER_TOKEN));

	    stream = getStationResponse.body();

	    GSLoggerFactory.getLogger(getClass()).info("Got " + linkage);

	    int responseCode = getStationResponse.statusCode();
	    if (responseCode >= 400) {
		// repeat again
		HISCentralValdaostaConnector.BEARER_TOKEN = HISCentralValdaostaConnector.getBearerToken(connector.getSourceURL());

		getStationResponse = downloader.downloadResponse(//
			linkage.trim(), //
			HttpHeaderUtils.build("Authorization", "Bearer " + HISCentralValdaostaConnector.BEARER_TOKEN));

		stream = getStationResponse.body();

		GSLoggerFactory.getLogger(getClass()).info("Got " + linkage);
	    }

	    if (stream != null) {
		JSONObject obj = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
		if (obj != null) {
		    JSONObject dataSeries = obj.optJSONObject("data-series");
		    return dataSeries;
		}
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + linkage);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + linkage + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_VALDAOSTA_DOWNLOAD_ERROR);
	}

	return null;
    }

    /**
     * CONVERT DATE FROM ISO TO VALLE D'OSTA SERVICE PARAMETER (epoch time)
     **/
    private Long convertDate(Date date) {
	// TODO Auto-generated method stub
	// 2023-03-02T16:37:25Z -> 1677771445479
	//
	Long result = null;
	result = date.getTime();
	return result / 1000;
    }

    @Override
    public boolean canSubset(String dimensionName) {

	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    @Override
    public boolean canDownload() {

	return (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getLinkage().contains(HISCentralValdaostaConnector.BASE_URL) && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_VALDAOSTA_NS_URI));
    }

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

    public static void main(String[] args) {
	Date d = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
	Long l = d.getTime();
	System.out.println(l / 1000);
    }
}
