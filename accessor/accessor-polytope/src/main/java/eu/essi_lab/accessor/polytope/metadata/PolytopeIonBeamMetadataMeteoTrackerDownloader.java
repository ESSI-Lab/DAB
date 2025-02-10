package eu.essi_lab.accessor.polytope.metadata;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.cuahsi.waterml._1.ObjectFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
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

public class PolytopeIonBeamMetadataMeteoTrackerDownloader extends DataDownloader {

    private Downloader downloader;

    public PolytopeIonBeamMetadataMeteoTrackerDownloader() {
	downloader = new Downloader();
    }

    @Override
    public boolean canDownload() {
	return (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.IONBEAM_TRACKER));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TRAJECTORY);
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setCRS(CRS.EPSG_4326());

	Iterator<BoundingPolygon> iterator = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification()
		.getBoundingPolygons();
	Double south = null;
	Double north = null;
	Double east = null;
	Double west = null;
	Double top = null;
	Double bottom = null;
	if (iterator != null) {
	    while (iterator.hasNext()) {
		BoundingPolygon polygon = iterator.next();
		List<List<Double>> multiPoints = polygon.getMultiPoints();
		for (List<Double> m : multiPoints) {
		    Double lat = m.get(0);
		    Double lon = m.get(1);
		    if (south == null || lat < south) {
			south = lat;
		    }
		    if (north == null || lat > north) {
			north = lat;
		    }
		    if (west == null || lon < west) {
			west = lon;
		    }
		    if (east == null || lon > east) {
			east = lon;
		    }
		    Double alt = m.get(2);
		    if (bottom == null || alt < bottom) {
			bottom = alt;
		    }
		    if (top == null || alt > top) {
			top = alt;
		    }
		}
	    }
	}

	descriptor.setEPSG4326SpatialDimensions(north, east, south, west);
	descriptor.getFirstSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getFirstSpatialDimension().getContinueDimension().setUpperTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setLowerTolerance(0.01);
	descriptor.getSecondSpatialDimension().getContinueDimension().setUpperTolerance(0.01);

	descriptor.setVerticalDimension(bottom, top);

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

	    Optional<Date> optStart = ISO8601DateTimeUtils.parseISO8601ToDate(startString);
	    Optional<Date> optEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endString);

	    // Optional<String> dataResponse = downloader.downloadOptionalString(online.getLinkage());

	    String identifier = online.getName();
	    String[] splittedId = identifier.split(":");
	    String varId = (splittedId.length > 2) ? splittedId[splittedId.length - 1] : splittedId[1];
	    String stationId = splittedId[0];
	    PolytopeIonBeamMetadataMeteoTrackerVariable var = PolytopeIonBeamMetadataMeteoTrackerVariable.decode(varId);
	    String units = var.getUnit();
	    String toMatch = var.getKey();
	    Iterable<CSVRecord> ret = null;
	    File tempFile;
	    //ret = getData(online.getLinkage());
	    ret = getCSVData(online.getLinkage(), startString, endString, stationId);

	    /**
	     * METEOTRACKER USE CASE
	     */

	    if (ret != null) {

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

		boolean isTemperature = false;

		if (units.contains("°C") || units.contains("ºC") || units.contains("K")) {
		    units = units.replace("°C", "K").replace("ºC", "K");
		    isTemperature = true;
		}

		for (CSVRecord obj : ret) {

		    // CASTING

		    String date = obj.get("datetime");
		    String varValue = obj.get(toMatch);
		    Optional<Date> valueDate = simpleDateTransform(date);
		    // Date parsed = convertToDate(date, time, minutes);

		    String lat = obj.get("lat");
		    String lon = obj.get("lon");
		    String altitude = obj.get("altitude");
		    BigDecimal value = new BigDecimal(varValue);

		    // String time = obj.optString("time");
		    // Date initialDateTime = ISO8601DateTimeUtils.parseISO8601(time);
		    // // BigDecimal timeOffset = new BigDecimal(timeOffsetString);
		    // Date observationDateTime = initialDateTime;//
		    // PolytopeMeteoTrackerMapper.updateDateTime(initialDateTime,
		    // // timeOffset);

		    if (value != null) {
			// BigDecimal dataValue = new BigDecimal(value);
			if (isTemperature && value.compareTo(new BigDecimal("100")) < 0) {
			    // from Celsius to Kelvin
			    BigDecimal kelvin = new BigDecimal("273.15");
			    value = value.add(kelvin);
			}
			value = value.setScale(2, BigDecimal.ROUND_FLOOR);
			int valueInteger = value.multiply(new BigDecimal(100)).intValue();

//			if (combined != null) {
//			    Optional<Date> parsed = transformDate(combined);

			    if (date != null && valueDate.isPresent()) {

				lats.add(Double.valueOf(lat));
				lons.add(Double.valueOf(lon));
				alts.add(Double.valueOf(altitude));
				times.add(valueDate.get().getTime());
				temperatures.add(valueInteger);
			    }

			//}
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
		return tempFile;
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
		"POLYTOPE ERROR");
    }


    private Optional<Date> simpleDateTransform(String date) throws ParseException {

	try {
	    // Define a SimpleDateFormat with the combined pattern
	    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");

	    // Parse the combined string into a Date object
	    Date d = inputFormat.parse(date);
	    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	    return Optional.of(d);

	} catch (RuntimeException e) {
	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Unparsable Date: {}", date, e);
	}
	return Optional.empty();
    }

    private Iterable<CSVRecord> getCSVData(String linkage, String startTime, String endTime, String stationId) throws Exception {

	Iterable<CSVRecord> out = null;

	if (PolytopeIonBeamMetadataConnector.BEARER_TOKEN == null) {
	    PolytopeIonBeamMetadataConnector.BEARER_TOKEN = PolytopeIonBeamMetadataConnector.getBearerToken();
	}

	// Create the new parameters for the request
	// ?format=csv&start_time=2025-01-31T00%3A00%3A00Z&end_time=2025-01-31T23%3A59%3A59Z&station_id=246ede0dd7e9d4c8
	String updatedParameters = "format=csv&start_time=" + startTime + "&end_time=" + endTime + "&station_id=" + stationId;
	String updatedUrl = linkage.split("\\?")[0] + "?" + updatedParameters;

	GSLoggerFactory.getLogger(getClass()).info("Getting " + updatedUrl);

	Downloader downloader = new Downloader();
	downloader.setRetryPolicy(20, TimeUnit.SECONDS, 2);

	HttpResponse<InputStream> stationResponse = downloader.downloadResponse(//
		updatedUrl.trim(), //
		HttpHeaderUtils.build("Authorization", "Bearer " + PolytopeIonBeamMetadataConnector.BEARER_TOKEN));

	InputStream stream = stationResponse.body();

	GSLoggerFactory.getLogger(getClass()).info("Got " + updatedUrl);

	if (stream != null) {

	    ClonableInputStream clone = new ClonableInputStream(stream);
	    String response = IOStreamUtils.asUTF8String(clone.clone());
	    try {
		// delimiter seems to be ; by default
		Reader in = new StringReader(response);
		String d = ";";
		char delimiter = d.charAt(0);
		 out = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);

	    } catch (Exception e) {
		// TODO: handle exception
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
		Reader reader = new StringReader(response);
		try {
		    out = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);
		} catch (IOException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		    return null;
		}

	    }

	    stream.close();
	}

	return out;
    }

    
    private List<JSONObject> getData(String linkage) throws Exception {

	ArrayList<JSONObject> out = Lists.newArrayList();

	if (PolytopeIonBeamMetadataConnector.BEARER_TOKEN == null) {
	    PolytopeIonBeamMetadataConnector.BEARER_TOKEN = PolytopeIonBeamMetadataConnector.getBearerToken();
	}

	GSLoggerFactory.getLogger(getClass()).info("Getting " + linkage);

	Downloader downloader = new Downloader();
	downloader.setRetryPolicy(20, TimeUnit.SECONDS, 2);

	HttpResponse<InputStream> stationResponse = downloader.downloadResponse(//
		linkage.trim(), //
		HttpHeaderUtils.build("Authorization", "Bearer " + PolytopeIonBeamMetadataConnector.BEARER_TOKEN));

	InputStream stream = stationResponse.body();

	GSLoggerFactory.getLogger(getClass()).info("Got " + linkage);

	if (stream != null) {

	    ClonableInputStream clone = new ClonableInputStream(stream);

	    // String res = IOStreamUtils.asUTF8String(clone.clone());
	    //
	    // if (res.toLowerCase().contains("invalid token provided")) {
	    // return getData(linkage);
	    // }

	    JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

	    for (int i = 0; i < array.length(); i++) {

		JSONObject object = array.getJSONObject(i);
		out.add(object);
	    }
	    stream.close();
	}

	return out;
    }

    private Optional<Date> transformDate(String date) throws ParseException {

	try {
	    // Define a SimpleDateFormat with the combined pattern
	    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSXXX");

	    // Parse the combined string into a Date object
	    Date d = inputFormat.parse(date);
	    inputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return Optional.of(d);

	} catch (RuntimeException e) {
	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Unparsable Date: {}", date, e);
	}
	return Optional.empty();
    }

    private boolean isValid(Date startDate, Date endDate, Date date) {
	return (!date.before(startDate)) && (!date.after(endDate));
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
    public boolean canSubset(String dimensionName) {
	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

}
