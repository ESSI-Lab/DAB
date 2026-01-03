package eu.essi_lab.accessor.trigger;

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
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.trigger.TRIGGERConnector.TRIGGER_VARIABLES;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
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
import eu.essi_lab.netcdf.timeseries.NetCDFVariable;
import eu.essi_lab.netcdf.trajectory.H13SingleTrajectoryWriter;
import eu.essi_lab.netcdf.trajectory.SimpleTrajectory;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class TRIGGERDownloader extends WMLDataDownloader {

    private static final String TRIGGER_DOWNLOADER_ERROR = "TRIGGER_DOWNLOADER_ERROR";
    private Downloader downloader;

    public TRIGGERDownloader() {
	downloader = new Downloader();
    }

    @Override
    public boolean canDownload() {
	return (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.TRIGGER));
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

	if (bbox != null) {
	    descriptor.setDataType(DataType.TRAJECTORY);
	    descriptor.setDataFormat(DataFormat.NETCDF());
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

	// Iterator<BoundingPolygon> iterator =
	// resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification()
	// .getBoundingPolygons();
	// Double south = null;
	// Double north = null;
	// Double east = null;
	// Double west = null;
	// Double top = null;
	// Double bottom = null;
	// if (iterator != null) {
	// while (iterator.hasNext()) {
	// BoundingPolygon polygon = iterator.next();
	// List<List<Double>> multiPoints = polygon.getMultiPoints();
	// for (List<Double> m : multiPoints) {
	// Double lat = m.get(0);
	// Double lon = m.get(1);
	// if (south == null || lat < south) {
	// south = lat;
	// }
	// if (north == null || lat > north) {
	// north = lat;
	// }
	// if (west == null || lon < west) {
	// west = lon;
	// }
	// if (east == null || lon > east) {
	// east = lon;
	// }
	// Double alt = m.get(2);
	// if (bottom == null || alt < bottom) {
	// bottom = alt;
	// }
	// if (top == null || alt > top) {
	// top = alt;
	// }
	// }
	// }
	// }
	//
	// descriptor.setEPSG4326SpatialDimensions(north, east, south, west);
	// descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	// descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	// descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	// descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	//
	// if(bottom != null && top != null) {
	// descriptor.setVerticalDimension(bottom, top);
	// }

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

	    List<JSONObject> ret = new ArrayList<>();
	    ret = getData(online.getLinkage());
	    String identifier = online.getName();
	    String[] splittedId = identifier.split(":");
	    String varId = (splittedId.length > 2) ? splittedId[splittedId.length - 1] : splittedId[1];
	    // String varId = splittedId[1];

	    // Optional<String> dataResponse = downloader.downloadString(online.getLinkage());

	    // if (dataResponse.isPresent()) {
	    //
	    // Iterable<CSVRecord> records = new ArrayList<CSVRecord>();
	    //
	    // try {
	    // // delimiter seems to be ; by default
	    // Reader in = new StringReader(dataResponse.get());
	    // String d = ";";
	    // char delimiter = d.charAt(0);
	    // records = CSVFormat.RFC4180.withDelimiter(delimiter).withFirstRecordAsHeader().parse(in);
	    //
	    // } catch (Exception e) {
	    // GSLoggerFactory.getLogger(this.getClass()).error(e.getMessage());
	    // Reader reader = new StringReader(dataResponse.get());
	    // records = null;
	    // try {
	    // records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);
	    // } catch (IOException e1) {
	    // // TODO Auto-generated catch block
	    // e1.printStackTrace();
	    // }
	    // }

	    Optional<Date> optStart = ISO8601DateTimeUtils.parseISO8601ToDate(startString);
	    Optional<Date> optEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endString);

	    GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();

	    File tempFile;
	    boolean isTemperature = false;
	    TRIGGER_VARIABLES var = TRIGGER_VARIABLES.decode(varId);
	    String units = var.getUnits();
	    if (units.contains("°C")) {
		units = units.replace("°C", "K");
		isTemperature = true;
	    }

	    Set<Long> timeSet = new HashSet<>();

	    if (bbox == null) {
		TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

		DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

		for (JSONObject obj : ret) {

		    BigDecimal value = obj.optBigDecimal(varId.toLowerCase(), null);// data.optString("value");

		    ValueSingleVariable variable = new ValueSingleVariable();

		    if (value != null) {

			//
			// value
			//

			// BigDecimal dataValue = new BigDecimal(value);
			if (isTemperature) {
			    // from Celsius to Kelvin
			    BigDecimal kelvin = new BigDecimal("273.15");
			    value = value.add(kelvin);
			}
			variable.setValue(value);

			//
			// date
			//
			int year = obj.optInt("year");
			int month = obj.optInt("month");
			int day = obj.optInt("day");
			int hour = obj.optInt("hour");
			int minute = obj.optInt("minute");
			int second = obj.optInt("second");
			Integer microsecond = (online.getLinkage().contains("ecg/?") || online.getLinkage().contains("ppg/?"))
				? obj.optInt("microsecond")
				: null;

			Date parsed = convertToDate(year, month, day, hour, minute, second, microsecond);

			if (timeSet.contains(parsed.getTime())) {
			    continue;
			} else {
			    timeSet.add(parsed.getTime());
			}

			// Date parsed =
			// Date.from(dateTime.atZone(ZoneId.of(TimeZone.getTimeZone("GMT").getID())).toInstant());

			if (parsed != null && optStart.isPresent() && optEnd.isPresent()) {
			    if (!isValid(optStart.get(), optEnd.get(), parsed)) {
				continue;
			    }
			    // Date parsed = iso8601OutputFormat.parse(date);

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

		tempFile = tsrt.getDataFile();

	    } else {

		/**
		 * TODO: CHECK IF POSSIBLE TO SEE GPS TRACE
		 */
		String deviceId = online.getLinkage().split("deviceId=")[1];
		String linkage = TRIGGERConnector.BASE_URL + "gps/?" + "deviceId=" + deviceId;

		List<JSONObject> gpsResult = getData(linkage);
		Set<Long> gpsTimes = new HashSet<>();

		Map<Long, TRIGGERTimePosition> posMap = new HashMap<Long, TRIGGERTimePosition>();
		for (JSONObject jO : gpsResult) {
		    int year = jO.optInt("year");
		    int month = jO.optInt("month");
		    int day = jO.optInt("day");
		    int hour = jO.optInt("hour");
		    int minute = jO.optInt("minute");
		    int second = jO.optInt("second");

		    Date parsed = convertToDate(year, month, day, hour, minute, second, null);
		    if (gpsTimes.contains(parsed.getTime())) {
			continue;
		    } else {
			gpsTimes.add(parsed.getTime());
		    }
		    Double lon = jO.optDouble("longitude");
		    Double lat = jO.optDouble("latitude");
		    LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second);
		    TRIGGERTimePosition tp = new TRIGGERTimePosition(lon, lat, dateTime);
		    posMap.put(parsed.getTime(), tp);

		}

		tempFile = File.createTempFile(getClass().getSimpleName(), ".nc");
		tempFile.deleteOnExit();
		H13SingleTrajectoryWriter writer = new H13SingleTrajectoryWriter(tempFile.getAbsolutePath());

		SimpleTrajectory trajectory = new SimpleTrajectory();
		trajectory.setIdentifier(online.getIdentifier());
		trajectory.setName(online.getName());
		trajectory.setDescription(online.getDescription());

		List<Double> lats = new ArrayList<>();
		List<Double> lons = new ArrayList<>();
		List<Double> alts = new ArrayList<>();
		List<Long> times = new ArrayList<>();
		List<Integer> temperatures = new ArrayList<>();

		for (JSONObject obj : ret) {

		    // CASTING

		    int year = obj.optInt("year");
		    int month = obj.optInt("month");
		    int day = obj.optInt("day");
		    int hour = obj.optInt("hour");
		    int minute = obj.optInt("minute");
		    int second = obj.optInt("second");

		    Date parsed = convertToDate(year, month, day, hour, minute, second, null);

		    TRIGGERTimePosition timePosition = posMap.get(parsed.getTime());
		    if (timePosition != null) {
			Double lat = timePosition.getLatitude();
			Double lon = timePosition.getLongitude();

			// String time = obj.optString("time");
			// Date initialDateTime = ISO8601DateTimeUtils.parseISO8601(time);
			// // BigDecimal timeOffset = new BigDecimal(timeOffsetString);
			// Date observationDateTime = initialDateTime;//
			// PolytopeMeteoTrackerMapper.updateDateTime(initialDateTime,
			// // timeOffset);
			BigDecimal value = obj.optBigDecimal(varId, null);

			if (value != null) {

			    if (isTemperature) {
				// from Celsius to Kelvin
				BigDecimal kelvin = new BigDecimal("273.15");
				value = value.add(kelvin);
			    }

			    // Integer microsecond = (online.getLinkage().contains("ecg/?") ||
			    // online.getLinkage().contains("ppg/?"))
			    // ? obj.optInt("microsecond")
			    // : null;
			    //
			    // Date parsed = convertToDate(year, month, day, hour, minute, second, microsecond);
			    //
			    // if(timeSet.contains(parsed.getTime())){
			    // continue;
			    // }else {
			    // timeSet.add(parsed.getTime());
			    // }

			    value = value.setScale(2, BigDecimal.ROUND_FLOOR);
			    int valueInteger = value.multiply(new BigDecimal(100)).intValue();

			    if (parsed != null && optStart.isPresent() && optEnd.isPresent()) {
				if (!isValid(optStart.get(), optEnd.get(), parsed)) {
				    continue;
				}

				lats.add(lat.doubleValue());
				lons.add(lon.doubleValue());
				alts.add(null);
				times.add(parsed.getTime());
				temperatures.add(valueInteger);

			    }
			}
		    }
		}

		NetCDFVariable<Long> timeVariable = new NetCDFVariable<>("time", times, "milliseconds since 1970-01-01 00:00:00",
			ucar.ma2.DataType.LONG);
		NetCDFVariable<Double> latVariable = new NetCDFVariable<>("lat", lats, "degrees_north", ucar.ma2.DataType.DOUBLE);
		NetCDFVariable<Double> lonVariable = new NetCDFVariable<>("lon", lons, "degrees_east", ucar.ma2.DataType.DOUBLE);
		NetCDFVariable<Double> altVariable = new NetCDFVariable<>("z", alts, "m", ucar.ma2.DataType.DOUBLE);
		NetCDFVariable<Integer> temperatureVariable = new NetCDFVariable<>(var.getLabel(), temperatures, units,
			ucar.ma2.DataType.INT);
		temperatureVariable.addAttribute("long_name", var.getLabel());
		temperatureVariable.addAttribute("scale_factor", .01);
		NetCDFVariable[] variables = new NetCDFVariable[] { temperatureVariable };
		writer.write(trajectory, timeVariable, latVariable, lonVariable, altVariable, variables);

		FeatureDataset dataset = FeatureDatasetFactoryManager.open(FeatureType.TRAJECTORY, tempFile.getAbsolutePath(), null, null);
		List<ucar.nc2.Attribute> attributes = dataset.getDataVariables().get(0).getAttributes();
		for (ucar.nc2.Attribute a : attributes) {
		    GSLoggerFactory.getLogger(getClass()).info("Attribute: " + a.getShortName() + " " + a.toString());
		}
		dataset.close();

	    }
	    return tempFile;

	} catch (Exception e) {

	    ex = e;
	}

	throw GSException.createException(//
		getClass(), //
		ex.getMessage(), //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		"TRIGGER ERROR");
    }

    private List<JSONObject> getData(String linkage) throws Exception {

	ArrayList<JSONObject> out = Lists.newArrayList();

	if (TRIGGERConnector.TRIGGER_TOKEN == null) {
	    TRIGGERConnector.TRIGGER_TOKEN = TRIGGERConnector.getBearerToken();
	}

	// HttpGet get = new HttpGet(linkage.trim());
	// get.addHeader("token", TRIGGERConnector.TRIGGER_TOKEN);

	InputStream stream = null;

	HttpResponse<InputStream> triggerResponse = null;

	try {
	    triggerResponse = new Downloader().downloadResponse(//
		    linkage.trim(), //
		    HttpHeaderUtils.build("token", TRIGGERConnector.TRIGGER_TOKEN));

	    int statusCode = triggerResponse.statusCode();

	    if (statusCode > 400 || statusCode == 201) {
		// token expired - refresh token
		TRIGGERConnector.TRIGGER_TOKEN = TRIGGERConnector.getBearerToken();

		triggerResponse = new Downloader().downloadResponse(//
			linkage.trim(), HttpHeaderUtils.build("token", TRIGGERConnector.TRIGGER_TOKEN));
	    }

	    stream = triggerResponse.body();
	    GSLoggerFactory.getLogger(TRIGGERConnector.class).info("Got " + linkage);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(TRIGGERConnector.class).error("Unable to retrieve " + linkage);
	    throw GSException.createException(//
		    TRIGGERConnector.class, //
		    "Unable to retrieve " + linkage + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    TRIGGER_DOWNLOADER_ERROR);
	}

	if (stream != null) {

	    ClonableInputStream clone = new ClonableInputStream(stream);

	    String res = IOStreamUtils.asUTF8String(clone.clone());

	    if (res.toLowerCase().contains("invalid token provided")) {
		return getData(linkage);
	    }

	    JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

	    for (int i = 0; i < array.length(); i++) {

		JSONObject object = array.getJSONObject(i);
		out.add(object);
	    }
	    stream.close();
	}

	return out;
    }

    public Date convertToDate(int year, int month, int day, int hour, int minute, int second, Integer microseconds) {
	Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	calendar.set(year, month - 1, day, hour, minute, second);
	if (microseconds != null) {
	    // Convert microseconds to milliseconds (1 microsecond = 1000 milliseconds)
	    long milliseconds = microseconds / 1000L;
	    // Add the milliseconds to the existing time
	    calendar.add(Calendar.MILLISECOND, (int) milliseconds);
	}
	return calendar.getTime();
    }

    private boolean isValid(Date startDate, Date endDate, Date date) {
	return (!date.before(startDate)) && (!date.after(endDate));
    }

    @Override
    public boolean canConnect() throws GSException {
	try {
	    return !TRIGGERConnector.getResultList(online.getLinkage()).isEmpty();
	} catch (Exception e) {
	    e.printStackTrace();
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

    public static void main(String[] args) {

    }
}
