package eu.essi_lab.accessor.polytope.metadata;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.csv.CSVRecord;
import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class PolytopeIonBeamMetadataDownloader extends WMLDataDownloader {

    private Downloader downloader;

    private static final String POLYTOPE_IONBEAM_GETDATA_ERROR = "POLYTOPE_IONBEAM_GETDATA_ERROR";

    public PolytopeIonBeamMetadataDownloader() {
	downloader = new Downloader();
    }

    @Override
    public boolean canDownload() {
	return (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.POLYTOPE_IONBEAM));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());
	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

	if (bbox != null) {
	    Double lat = bbox.getNorth();
	    Double lon = bbox.getEast();

	    descriptor.setEPSG4326SpatialDimensions(lat, lon);
	    descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
	    descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);
	    descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	    descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	    descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	    descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	}

	Iterator<BoundingPolygon> iterator = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification()
		.getBoundingPolygons();
	if (iterator != null) {
	    while (iterator.hasNext()) {
		BoundingPolygon polygon = iterator.next();
		List<List<Double>> multiPoints = polygon.getMultiPoints();
		for (List<Double> m : multiPoints) {
		    Double lat = m.get(0);
		    Double lon = m.get(1);
		    descriptor.setEPSG4326SpatialDimensions(lat, lon);
		    descriptor.getFirstSpatialDimension().getContinueDimension().setSize(1l);
		    descriptor.getSecondSpatialDimension().getContinueDimension().setSize(1l);
		    descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
		    descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
		    descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
		    descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
		}
	    }
	}

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
    public File download(DataDescriptor descriptor) throws GSException {
	Exception ex = null;
	List<CSVRecord> ret = null;

	try {

	    Date begin = null;
	    Date end = null;

	    ObjectFactory factory = new ObjectFactory();

	    String startString = null;
	    String endString = null;

	    DataDimension dimension = descriptor.getTemporalDimension();

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

	    Optional<Date> optStart = ISO8601DateTimeUtils.parseISO8601ToDate(startString);
	    Optional<Date> optEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endString);

	    // Optional<String> dataResponse = downloader.downloadOptionalString(online.getLinkage());

	    String name = online.getName();
	    String[] splittedId = name.split(":");
	    String varId = (splittedId.length > 2) ? splittedId[splittedId.length - 1] : splittedId[1];
	    String stationId = (splittedId.length > 2) ? splittedId[1] : splittedId[0];
	    String platformName = splittedId[0];
	    String units = "";
	    String toMatch = "";
	    if (platformName.toLowerCase().contains("acronet")) {
		PolytopeIonBeamMetadataAcronetVariable acronetVar = PolytopeIonBeamMetadataAcronetVariable.decode(varId);
		units = acronetVar.getUnit();
		toMatch = acronetVar.getKey();
	    } else if (platformName.toLowerCase().contains("smart")) {
		PolytopeIonBeamMetadataSmartKitVariable smartVar = PolytopeIonBeamMetadataSmartKitVariable.decode(varId);
		units = smartVar.getUnit();
		toMatch = smartVar.getKey();
	    }

	    File tempFile;

	    // List<Date[]> dates = new ArrayList<Date[]>();
	    // if (optStart.isPresent() && optEnd.isPresent()) {
	    // dates = checkDates(optStart.get(), optEnd.get());
	    // }

	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");
	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    boolean isTemperature = false;

	    if (units.contains("°C") || units.contains("ºC") || units.contains("K") || units.contains("Kelvin")) {
		units = units.replace("°C", "K").replace("ºC", "K").replace("Kelvin", "K");
		isTemperature = true;
	    }

	    // for (Date[] d : dates) {

	    // startString = convertISODateToIonBeamDate(ISO8601DateTimeUtils.getISO8601DateTime(d[0]));
	    // endString = convertISODateToIonBeamDate(ISO8601DateTimeUtils.getISO8601DateTime(d[1]));
	    // ret = getData(online.getLinkage(), startString, endString);

	    ret = PolytopeIonBeamMetadataConnector.getCSVData(online.getLinkage(), startString, endString, stationId, platformName);

	    if (ret == null) {
		// multiple requests needed
		Iterable<CSVRecord> out = null;
		ret = new ArrayList<CSVRecord>();
		List<Date[]> dates = checkDates(startString, endString);
		for (Date[] d : dates) {
		    out = PolytopeIonBeamMetadataConnector.getCSVData(online.getLinkage(), ISO8601DateTimeUtils.getISO8601DateTime(d[0]),
			    ISO8601DateTimeUtils.getISO8601DateTime(d[1]), stationId, platformName);
		    for (CSVRecord r : out) {
			ret.add(r);
		    }
		}
	    }

	    int count = 0;
	    if (ret != null) {

		for (CSVRecord obj : ret) {

		    String date = obj.get("datetime");
		    String varValue = obj.get(toMatch);
		    Optional<Date> valueDate = simpleDateTransform(date);
		    if (date != null && optStart.isPresent() && optEnd.isPresent() && valueDate.isPresent() && varValue != null
			    && !varValue.isEmpty()) {
			if (!isValid(optStart.get(), optEnd.get(), valueDate.get()))
			    continue;

			// String chunkDate = obj.optString("chunk_date");
			// String chunkTime = obj.optString("chunk_time");
			// Combine date and time into a single string
			// String combined = chunkDate + chunkTime; // "202501211700"

			BigDecimal value = new BigDecimal(varValue);
			// BigDecimal value = obj.optBigDecimal(toMatch, null);// data.optBigDecimal(1, null);//
			// obj.optBigDecimal(varId.toLowerCase(),
			// null);// data.optString("value");
			ValueSingleVariable variable = new ValueSingleVariable();

			if (value != null && value.doubleValue() != -9998.0) {

			    //
			    // value
			    //

			    // BigDecimal dataValue = new BigDecimal(value);
			    if (isTemperature && value.compareTo(new BigDecimal("100")) < 0) {
				// from Celsius to Kelvin
				BigDecimal kelvin = new BigDecimal("273.15");
				value = value.add(kelvin);
			    }
			    value = value.setScale(2, BigDecimal.ROUND_FLOOR);
			    // int valueInteger = value.multiply(new BigDecimal(100)).intValue();

			    variable.setValue(value);

			    // if (combined != null) {

			    // Optional<Date> parsed = transformDate(combined);

			    // Date parsed =
			    // Date.from(dateTime.atZone(ZoneId.of(TimeZone.getTimeZone("GMT").getID())).toInstant());

			    // if (parsed != null && parsed.isPresent()) {

			    // Date parsed = iso8601OutputFormat.parse(date);

			    GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			    gregCal.setTime(valueDate.get());

			    XMLGregorianCalendar xmlGregCal = xmlFactory.newXMLGregorianCalendar(gregCal);
			    variable.setDateTimeUTC(xmlGregCal);

			    //
			    //
			    //

			    addValue(tsrt, variable);
			    count++;
			}
		    }
		}

	    }

	    return tsrt.getDataFile();

	} catch (Exception e) {

	    ex = e;
	}

	throw GSException.createException(//
		getClass(), //
		ex.getMessage(), //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		POLYTOPE_IONBEAM_GETDATA_ERROR);

    }

    private Optional<Date> transformDate(String date) throws ParseException {

	try {
	    // Define a SimpleDateFormat with the combined pattern
	    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmm");

	    // Parse the combined string into a Date object
	    Date d = inputFormat.parse(date);
	    inputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return Optional.of(d);

	} catch (RuntimeException e) {
	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Unparsable Date: {}", date, e);
	}
	return Optional.empty();
    }

    private Optional<Date> simpleDateTransform(String date) throws ParseException {
	SimpleDateFormat inputFormat = null;
	try {
	    // Define a SimpleDateFormat with the combined pattern
	    inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");

	    // Parse the combined string into a Date object
	    Date d = inputFormat.parse(date);
	    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	    return Optional.of(d);

	} catch (RuntimeException e) {
	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Unparsable Date: {}", date, e);
	    inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");
	    Date d = inputFormat.parse(date);
	    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	return Optional.empty();
    }

    private String convertISODateToIonBeamDate(String date) {
	String[] splitted = date.split("T");
	String acronetDate = splitted[0].replace("-", "");
	return acronetDate;
    }

    private List<Date[]> checkDates(String startDate, String endDate) {
	List<Date[]> dateRanges = new ArrayList<>();
	Optional<Date> optStartDate = ISO8601DateTimeUtils.parseISO8601ToDate(startDate);
	Optional<Date> optEndDate = ISO8601DateTimeUtils.parseISO8601ToDate(endDate);
	Date newStartDate = optStartDate.get();
	Date newEndDate = optEndDate.get();
	// Calculate the difference in days between the two dates
	long diffInMillies = Math.abs(newEndDate.getTime() - newStartDate.getTime());
	long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

	// If the difference is 10 days or less, return the original range
	if (diffInDays > 32) {
	    // take last 30 days
	    newStartDate = new Date(newEndDate.getTime() - TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
	    diffInDays = 32;
	}

	// Otherwise, split the dates into ranges of at most 10 days
	Date currentStartDate = newStartDate;
	boolean maxRequestsReached = false;
	int count = 0;
	while (diffInDays > 8 && !maxRequestsReached) {
	    Date currentEndDate = new Date(currentStartDate.getTime() + TimeUnit.MILLISECONDS.convert(8, TimeUnit.DAYS));
	    dateRanges.add(new Date[] { currentStartDate, currentEndDate });

	    currentStartDate = new Date(currentEndDate.getTime());
	    diffInDays -= 8;
	    count++;
	    if (count > 3) {
		maxRequestsReached = true;
		newEndDate = new Date(currentStartDate.getTime() + TimeUnit.MILLISECONDS.convert(8, TimeUnit.DAYS));
	    }
	}

	// Add the final range
	dateRanges.add(new Date[] { currentStartDate, newEndDate });

	return dateRanges;
    }

    private List<JSONObject> getData(String linkage, String startTime, String endTime) throws Exception {

	ArrayList<JSONObject> out = Lists.newArrayList();

	if (PolytopeIonBeamMetadataConnector.BEARER_TOKEN == null) {
	    PolytopeIonBeamMetadataConnector.BEARER_TOKEN = PolytopeIonBeamMetadataConnector.getBearerToken();
	}

	// Create the new date parameter
	String updatedDate = "date=" + startTime + "/to/" + endTime + "/by/1";
	String updatedUrl = linkage.replaceFirst("date=[^&]+", updatedDate);

	GSLoggerFactory.getLogger(getClass()).info("Getting " + updatedUrl);

	HashMap<String, String> headers = new HashMap<>();
	headers.put("Authorization", "Bearer " + PolytopeIonBeamMetadataConnector.BEARER_TOKEN);

	Optional<String> response = downloader.downloadOptionalString(updatedUrl.trim(), HttpHeaderUtils.build(headers));
	//
	// HttpResponse<InputStream> stationResponse = downloader.downloadResponse(//
	// updatedUrl.trim(), //
	// HttpHeaderUtils.build("Authorization", "Bearer " + PolytopeIonBeamMetadataConnector.BEARER_TOKEN));
	//
	// InputStream stream = stationResponse.body();

	GSLoggerFactory.getLogger(getClass()).info("Got " + updatedUrl);

	if (response.isPresent()) {

	    // String res = IOStreamUtils.asUTF8String(clone.clone());
	    //
	    // if (res.toLowerCase().contains("invalid token provided")) {
	    // return getData(linkage);
	    // }

	    String responseString = response.get();
	    JSONArray array = new JSONArray(responseString);

	    for (int i = 0; i < array.length(); i++) {

		JSONObject object = array.getJSONObject(i);
		out.add(object);
	    }

	}

	return out;
    }

    // private List<CSVRecord> getCSVData(String linkage, String startTime, String endTime, String stationId, String
    // platform)
    // throws Exception {
    //
    // List<CSVRecord> ret = new ArrayList<CSVRecord>();
    //
    // Iterable<CSVRecord> out = null;
    //
    // if (PolytopeIonBeamMetadataConnector.BEARER_TOKEN == null) {
    // PolytopeIonBeamMetadataConnector.BEARER_TOKEN = PolytopeIonBeamMetadataConnector.getBearerToken();
    // }
    //
    // // Create the new parameters for the request
    // // ?format=csv&start_time=2025-01-31T00%3A00%3A00Z&end_time=2025-01-31T23%3A59%3A59Z&station_id=246ede0dd7e9d4c8
    // String updatedParameters = "format=csv&platform=" + platform + "&start_time=" + startTime + "&end_time=" +
    // endTime + "&station_id="
    // + stationId;
    // String updatedUrl = linkage.split("\\?")[0] + "?" + updatedParameters;
    //
    // GSLoggerFactory.getLogger(getClass()).info("Getting " + updatedUrl);
    //
    // HashMap<String, String> headers = new HashMap<>();
    // headers.put("Authorization", "Bearer " + PolytopeIonBeamMetadataConnector.BEARER_TOKEN);
    //
    // Optional<String> response = downloader.downloadOptionalString(updatedUrl.trim(), HttpHeaderUtils.build(headers));
    //
    // if (response.isPresent()) {
    //
    // String responseString = response.get();
    // //*no data use-case
    // if(responseString.toLowerCase().contains("no data found")) {
    // return ret;
    // }
    // //*multiple requests needed
    // if(responseString.toLowerCase().contains("200 data granules")) {
    // return null;
    // }
    // try {
    // // delimiter seems to be ; by default
    // Reader in = new StringReader(responseString);
    // out = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
    // for(CSVRecord r: out) {
    // ret.add(r);
    // }
    //
    // } catch (Exception e) {
    // //multiple requests needeed
    // GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
    // //try
    //// List<Date[]> dates = checkDates(startTime, endTime);
    //// for (Date[] d : dates) {
    //// out = getCSVData(linkage, ISO8601DateTimeUtils.getISO8601DateTime(d[0]),
    // ISO8601DateTimeUtils.getISO8601DateTime(d[1]), stationId, platform);
    //// for(CSVRecord r: out) {
    //// ret.add(r);
    //// }
    //// }
    //// return ret;
    // }
    //
    // }
    //
    // return ret;
    // }

    private boolean isValid(Date startDate, Date endDate, Date date) {

	return (!date.before(startDate)) && (!date.after(endDate));
    }

    @Override
    public boolean canSubset(String dimensionName) {
	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
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

    public static void main(String[] args) throws Exception {
	String linkage = "http://ionbeam-ichange.ecmwf-ichange.f.ewcloud.host/api/v1/retrieve?class=rd&date=20250117/to/20250122/by/1&expver=xxxx&stream=lwda&aggregation_type=by_time&platform=acronet&station_id=7ce702412e21a86e";
	System.out.println("Linkage URL: " + linkage);
	// Define new dates in ISO format
	LocalDate startDate = LocalDate.parse("2025-02-01"); // Replace with your desired date
	LocalDate endDate = LocalDate.parse("2025-02-05"); // Replace with your desired date

	// Format dates back to yyyyMMdd
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	String formattedStartDate = startDate.format(formatter);
	String formattedEndDate = endDate.format(formatter);

	// Create the new date parameter
	String updatedDate = "date=" + formattedStartDate + "/to/" + formattedEndDate + "/by/1";

	String updatedUrl = linkage.replaceFirst("date=[^&]+", updatedDate);
	System.out.println("Updated URL: " + updatedUrl);

	String d = "2025-02-03 12:00:00+00:00";
	// "2025-01-31 05:28:29.011000+00:00"
	// Define a SimpleDateFormat with the combined pattern
	SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");

	// Parse the combined string into a Date object
	Date d1;
	try {
	    d1 = inputFormat.parse(d);
	    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	} catch (Exception e) {
	    inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssXXX");
	    d1 = inputFormat.parse(d);
	    System.out.println(d1.getTime());
	}

    }

}
