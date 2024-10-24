package eu.essi_lab.downloader.hiscentral;

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

import java.io.File;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.bolzano.HISCentralBolzanoConnector;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
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
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Roberto
 */

/**
 * GET DATA REQUEST EXMPLE:
 * https://omnia-develop.osmer.fvg.it/api/ws/data?measure_id=17968&from=202201010000&to=202202020000
 */

public class HISCentralBolzanoDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_BOLZANO_DOWNLOAD_ERROR = "HISCENTRAL_BOLZANO_DOWNLOAD_ERROR";

    private HISCentralBolzanoConnector connector;
    private Downloader downloader;

    /**
     * 
     */
    public HISCentralBolzanoDownloader() {

	connector = new HISCentralBolzanoConnector();
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

	    String startString = null;
	    String endString = null;

	    DataDimension dimension = targetDescriptor.getTemporalDimension();

	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {

		ContinueDimension sizedDimension = dimension.getContinueDimension();

		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());

		startString = ISO8601DateTimeUtils.getISO8601DateTime(begin);
		endString = ISO8601DateTimeUtils.getISO8601DateTime(end);
	    }

	    if (startString == null || endString == null) {

		startString = ISO8601DateTimeUtils.getISO8601Date(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L));
		endString = ISO8601DateTimeUtils.getISO8601Date(new Date());
	    }

	    startString = convertDate(startString);
	    endString = convertDate(endString);
	    String linkage = online.getLinkage() + "&date_from=" + startString + "&date_to=" + endString;

	    JSONArray jsonArray = getData(linkage);

	    TimeSeriesResponseType tsrt = getTimeSeriesTemplate();
	    DateFormat iso8601OutputFormat = null;
	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    if (jsonArray != null && jsonArray.length() > 1) {

		for (Object arr : jsonArray) {

		    JSONObject data = (JSONObject) arr;

		    String valueString = data.optString("VALUE");

		    ValueSingleVariable variable = new ValueSingleVariable();

		    if (valueString != null && !valueString.isEmpty()) {

			//
			// value
			//

			BigDecimal dataValue = new BigDecimal(valueString);
			variable.setValue(dataValue);

			//
			// date
			//

			String date = data.optString("DATE");
			date = date.contains("CET") ? date.split("CET")[0] : date;
			date = date.contains("CEST") ? date.split("CEST")[0] : date;

			if (iso8601OutputFormat == null) {
			    iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			    iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
			}

			// ZoneId cetAllYear = ZoneId.of("Europe/Rome");
			// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			// ZonedDateTime dateTime = LocalDateTime.parse(date, formatter).atZone(cetAllYear);
			// Date parsed = Date.from(dateTime.toInstant());

			Date parsed = iso8601OutputFormat.parse(date);

			GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			gregCal.setTime(parsed);

			XMLGregorianCalendar xmlGregCal = xmlFactory.newXMLGregorianCalendar(gregCal);
			variable.setDateTimeUTC(xmlGregCal);

			//
			//
			//

			addValue(tsrt, variable);
		    }
		}
	    }

	    JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");

	    tmpFile.deleteOnExit();
	    JAXBWML.getInstance().marshal(response, tmpFile);

	    return tmpFile;

	} catch (Exception e) {

	    ex = e;
	}

	throw GSException.createException(//
		getClass(), //
		ex.getMessage(), //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		HISCENTRAL_BOLZANO_DOWNLOAD_ERROR);

    }

    private JSONArray getData(String linkage) throws GSException {
	GSLoggerFactory.getLogger(getClass()).info("Getting Data from Bolzano service");

	try {

	    Optional<String> res = downloader.downloadOptionalString(linkage);
	    if (res.isPresent()) {
		JSONArray obj = new JSONArray(res.get());
		return obj;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + linkage);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + linkage + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_BOLZANO_DOWNLOAD_ERROR);
	}

	return null;
    }

    /**
     * CONVERT DATE FROM ISO TO OMNIA SERVICE PARAMETER (YYYYMMDD)
     **/
    private static String convertDate(String date) {
	// TODO Auto-generated method stub
	// 2022-09-12T09:36:00Z -> 20220912
	// 2022-09-19T09:36:00Z -> 20220919
	String result;
	String[] splittedDate = date.split("T");
	result = splittedDate[0].replaceAll("-", "");// .replaceAll(":", "").replace("T", "");
	return result;
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
		online.getLinkage().contains(HISCentralBolzanoConnector.BASE_URL) && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_BOLZANO_NS_URI));
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

    public static void main(String[] args) throws Exception {
	String date1 = "2016-02-29T13:10:00";
	System.out.println(date1);
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	LocalDateTime cetDate = LocalDateTime.parse(date1, formatter);
	System.out.println(cetDate);
	LocalDateTime utcdate = cetToUtc(cetDate);
	System.out.println(utcdate);
	// LocalDateTime utcDate = cetDate.withZoneSameLocal(ZoneOffset.UTC).toLocalDateTime();
	// System.out.println(utcDate);

	LocalDateTime date = LocalDateTime.now(ZoneId.of("CET"));
	System.out.println(date);

	LocalDateTime utcdate2 = cetToUtc(date);
	System.out.println(utcdate2);

	LocalDateTime cetdate = utcToCet(utcdate2);
	System.out.println(cetdate);

	ZoneId cetAllYear = ZoneId.of("Europe/Rome");
	DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	ZonedDateTime dateTime = LocalDateTime.parse(date1, formatter2).atZone(cetAllYear);

	// DateFormat iso8601OutputFormat = date.contains(" ")
	// ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN)
	// : new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
	//
	// iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("CET"));

	Date parsed2 = Date.from(dateTime.toInstant());

	GregorianCalendar gregCal2 = new GregorianCalendar(TimeZone.getTimeZone("Europe/Rome"));
	gregCal2.setTime(parsed2);

	XMLGregorianCalendar xmlGregCal2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCal2);

	System.out.println(parsed2);
	System.out.println(xmlGregCal2.getHour());

	DateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));

	// ZoneId cetAllYear = ZoneId.of("Europe/Rome");
	// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	// ZonedDateTime dateTime = LocalDateTime.parse(date, formatter).atZone(cetAllYear);
	// Date parsed = Date.from(dateTime.toInstant());

	Date parsed = iso8601OutputFormat.parse(date1);

	GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	gregCal.setTime(parsed);

	XMLGregorianCalendar xmlGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCal);
	System.out.println(parsed);
	System.out.println(xmlGregCal.getHour());

	// LocalDateTime date1 = LocalDateTime.now(ZoneId.of("CET"));
	// ldate.atStartOfDay(ZoneId.of("CET"))

    }

    public static LocalDateTime cetToUtc(LocalDateTime timeInCet) {
	ZonedDateTime cetTimeZoned = ZonedDateTime.of(timeInCet, ZoneId.of("CET"));
	return cetTimeZoned.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static LocalDateTime utcToCet(LocalDateTime timeInUtc) {
	ZonedDateTime utcTimeZoned = ZonedDateTime.of(timeInUtc, ZoneId.of("UTC"));
	return utcTimeZoned.withZoneSameInstant(ZoneId.of("CET")).toLocalDateTime();
    }
}
