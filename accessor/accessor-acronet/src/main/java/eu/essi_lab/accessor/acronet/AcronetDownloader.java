package eu.essi_lab.accessor.acronet;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
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

public class AcronetDownloader extends WMLDataDownloader {

    private Downloader downloader;

    public AcronetDownloader() {
	downloader = new Downloader();
    }

    @Override
    public boolean canDownload() {
	return (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.ACRONET));
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
	    AcronetClient client = new AcronetClient(online.getLinkage());
	    String name = online.getName();
	    String[] splittedId = name.split(":");

	    String variableName = splittedId[1];
	    String stationId = splittedId[0];
	    String units = splittedId[2];

	    Optional<Date> startDate = ISO8601DateTimeUtils.parseISO8601ToDate(startString);
	    Optional<Date> endDate = ISO8601DateTimeUtils.parseISO8601ToDate(endString);

	    List<Date[]> dates = new ArrayList<Date[]>();
	    if (startDate.isPresent() && endDate.isPresent()) {
		dates = checkDates(startDate.get(), endDate.get());
	    }

	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");
	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    File tempFile;
	    boolean isTemperature = false;

	    if (units.contains("°C") || units.contains("ºC") || units.contains("K")) {
		units = units.replace("°C", "K").replace("ºC", "K");
		isTemperature = true;
	    }

	    for (Date[] d : dates) {

		startString = convertISODateToAcronetDate(ISO8601DateTimeUtils.getISO8601DateTime(d[0]));
		endString = convertISODateToAcronetDate(ISO8601DateTimeUtils.getISO8601DateTime(d[1]));
		ret = client.getData(variableName, stationId, startString, endString);
		int count = 0;
		if (ret != null && !ret.isEmpty()) {

		    JSONArray data = ret.optJSONArray("values");
		    JSONArray timeLine = ret.optJSONArray("timeline");
		    System.out.println(data.length());
		    System.out.println(timeLine.length());
		    for (int j = 0; j < data.length(); j++) {

			JSONArray dataTime = data.optJSONArray(j);

			BigDecimal value = data.optBigDecimal(j, null);// data.optBigDecimal(1, null);//
								       // obj.optBigDecimal(varId.toLowerCase(),
								       // null);// data.optString("value");
			ValueSingleVariable variable = new ValueSingleVariable();

			if (value != null && value.doubleValue() != -9998.0) {

			    //
			    // value
			    //

			    // BigDecimal dataValue = new BigDecimal(value);
			    if (isTemperature) {
				// from Celsius to Kelvin
				BigDecimal kelvin = new BigDecimal("273.15");
				value = value.add(kelvin);
			    }

			    value = value.setScale(2, BigDecimal.ROUND_FLOOR);
			    // int valueInteger = value.multiply(new BigDecimal(100)).intValue();

			    variable.setValue(value);

			    String date = timeLine.optString(j, null);

			    if (date != null) {

				Optional<Date> parsed = ISO8601DateTimeUtils.parseNotStandard2ToDate(date);

				// Date parsed =
				// Date.from(dateTime.atZone(ZoneId.of(TimeZone.getTimeZone("GMT").getID())).toInstant());

				if (parsed != null && parsed.isPresent()) {

				    // Date parsed = iso8601OutputFormat.parse(date);

				    GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
				    gregCal.setTime(parsed.get());

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

		}
		if(count > 0)
		    break;
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
		"ACRONET ERROR");
    }

    private List<Date[]> checkDates(Date startDate, Date endDate) {
	List<Date[]> dateRanges = new ArrayList<>();

	// Calculate the difference in days between the two dates
	long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
	long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

	// If the difference is 10 days or less, return the original range
	if (diffInDays <= 10) {
	    dateRanges.add(new Date[] { startDate, endDate });
	    return dateRanges;
	} else if (diffInDays > 30) {
	    //take last 30 days
	    startDate = new Date(endDate.getTime() - TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
	    diffInDays = 30;
	}

	// Otherwise, split the dates into ranges of at most 10 days
	Date currentStartDate = startDate;
	boolean maxRequestsReached = false;
	int count = 0;
	while (diffInDays > 10 && !maxRequestsReached) {
	    Date currentEndDate = new Date(currentStartDate.getTime() + TimeUnit.MILLISECONDS.convert(10, TimeUnit.DAYS));
	    dateRanges.add(new Date[] { currentStartDate, currentEndDate });

	    currentStartDate = new Date(currentEndDate.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
	    diffInDays -= 10;
	    count++;
	    if (count > 2) {
		maxRequestsReached = true;
		endDate = new Date(currentStartDate.getTime() + TimeUnit.MILLISECONDS.convert(10, TimeUnit.DAYS));
	    }
	}

	// Add the final range
	dateRanges.add(new Date[] { currentStartDate, endDate });

	return dateRanges;
    }

    private String convertISODateToAcronetDate(String date) {
	String[] splitted = date.split("T");
	String acronetDate = splitted[0].replace("-", "") + splitted[1].replace(":", "").substring(0, 4);
	return acronetDate;
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

}
