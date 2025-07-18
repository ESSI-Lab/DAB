package eu.essi_lab.accessor.waf.trigger;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.downloader.Downloader;
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

public class TRIGGERWafDownloader extends WMLDataDownloader {

    private Downloader downloader;
    private String user;
    private String psw;

    public TRIGGERWafDownloader() {
	this.downloader = new Downloader();
    }

    @Override
    public boolean canDownload() {
	return online.getProtocol().equals(TRIGGERWafConnector.WAF_TRIGGER_PROTOCOL);
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

	    JSONObject ret = new JSONObject();
	    String linkage = online.getLinkage();
	    String name = online.getName();
	    String[] splittedId = name.split(":");

	    String variableName = splittedId[0];
	    String variableUnits = splittedId[1];
	    String interpolation = splittedId[2];

	    Optional<Date> startDate = ISO8601DateTimeUtils.parseISO8601ToDate(startString);
	    Optional<Date> endDate = ISO8601DateTimeUtils.parseISO8601ToDate(endString);

	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    File tempFile;
	    boolean isTemperature = false;

	    // if (units.contains("°C") || units.contains("ºC") || units.contains("K")) {
	    // units = units.replace("°C", "K").replace("ºC", "K");
	    // isTemperature = true;
	    // }

	    if (user == null) {
		user = ConfigurationWrapper.getCredentialsSetting().getTriggerWAFUser().orElse(null);
	    }
	    if (psw == null) {
		psw = ConfigurationWrapper.getCredentialsSetting().getTriggerWAFPassword().orElse(null);
	    }

	    String jsonResp = downloader.downloadOptionalString(linkage, user, psw).orElse(null);
	    if (jsonResp != null && !jsonResp.isEmpty()) {
		ret = new JSONObject(jsonResp);
		Boolean isCams = null;
		isCams = linkage.contains("cams") ? true : false;
		String nameId = null;
		JSONArray data = null;
		String percentiles = null;
		if (isCams) {
		    String[] parts = linkage.split("/");
		    String fileName = parts[parts.length - 1];
		    String[] splittedCams = fileName.split("cams_eu_");
		    nameId = splittedCams[1].split("_web")[0];
		    percentiles = nameId + "-percentiles";
		    JSONObject percentilesObj = ret.optJSONObject(percentiles);
		    if (percentilesObj != null && !percentilesObj.isEmpty()) {
			data = percentilesObj.optJSONArray(interpolation);
		    }
		} else {
		    data = ret.optJSONArray(interpolation);
		}

		if (data == null) {
		    throw GSException.createException(//

			    getClass(), //
			    ex.getMessage(), //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    "TRIGGER WAF DOWNLOAD ERROR");
		}

		Map<String, BigDecimal> result = getDateValue(data, isCams);

		for (Map.Entry<String, BigDecimal> entry : result.entrySet()) {

		    BigDecimal value = entry.getValue();// data.optBigDecimal(1, null);//
							// obj.optBigDecimal(varId.toLowerCase(),
							// null);// data.optString("value");
		    ValueSingleVariable variable = new ValueSingleVariable();

		    if (value != null && value.doubleValue() != -9999.0) {

			//
			// value
			//

			// value = value.setScale(2, BigDecimal.ROUND_FLOOR);
			// int valueInteger = value.multiply(new BigDecimal(100)).intValue();

			variable.setValue(value);

			String date = entry.getKey();

			if (date != null) {

			    Optional<Date> parsed = ISO8601DateTimeUtils.parseISO8601ToDate(date);

			    if (parsed != null && parsed.isPresent()) {

				// Date parsed = iso8601OutputFormat.parse(date);

				GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
				gregCal.setTime(parsed.get());

				XMLGregorianCalendar xmlGregCal = xmlFactory.newXMLGregorianCalendar(gregCal);
				variable.setDateTimeUTC(xmlGregCal);

				//
				//
				//

				addValue(tsrt, variable);

			    }
			}
		    }

		}

	    }

	    return tsrt.getDataFile();

	} catch (

	Exception e) {

	    ex = e;
	}

	throw GSException.createException(//

		getClass(), //
		ex.getMessage(), //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		"TRIGGER WAF DOWNLOAD ERROR");
    }

    private Map<String, BigDecimal> getDateValue(JSONArray data, Boolean isCams) {
	Map<String, BigDecimal> result = new LinkedHashMap<>();
	// Get today's date at 00:00 UTC
	Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	calendar.set(Calendar.HOUR_OF_DAY, 0);
	calendar.set(Calendar.MINUTE, 0);
	calendar.set(Calendar.SECOND, 0);
	calendar.set(Calendar.MILLISECOND, 0);

	SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

	if (isCams) {
	    // First 90 values - Hourly data
	    for (int i = 0; i < 96; i++) {
		calendar.add(Calendar.HOUR_OF_DAY, 1); // Add 1 hour
		result.put(isoFormat.format(calendar.getTime()), data.getBigDecimal(i));
	    }
	} else {

	    // First 90 values - Hourly data
	    for (int i = 0; i < 90; i++) {
		calendar.add(Calendar.HOUR_OF_DAY, 1); // Add 1 hour
		result.put(isoFormat.format(calendar.getTime()), data.getBigDecimal(i));
	    }

	    // Next 18 values - Every 3 hours
	    for (int i = 90; i < 108; i++) {
		calendar.add(Calendar.HOUR_OF_DAY, 3); // Add 3 hours
		result.put(isoFormat.format(calendar.getTime()), data.getBigDecimal(i));
	    }

	    // Last 16 values - Every 6 hours
	    for (int i = 108; i < 124; i++) {
		calendar.add(Calendar.HOUR_OF_DAY, 6); // Add 6 hours
		result.put(isoFormat.format(calendar.getTime()), data.getBigDecimal(i));
	    }

	}

	// result.forEach((key, value) -> System.out.println(key + " -> " + value));
	return result;
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
	return false;
    }

}
