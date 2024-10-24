package eu.essi_lab.accessor.smartcitizenkit;

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
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.json.JSONArray;
import org.json.JSONObject;

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
import eu.essi_lab.netcdf.timeseries.NetCDFVariable;
import eu.essi_lab.netcdf.trajectory.H13SingleTrajectoryWriter;
import eu.essi_lab.netcdf.trajectory.SimpleTrajectory;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class SmartCitizenKitDownloader extends WMLDataDownloader {

    private Downloader downloader;

    public SmartCitizenKitDownloader() {
	downloader = new Downloader();
    }

    @Override
    public boolean canDownload() {
	return (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.SMARTCITIZENKIT));
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
	    ret = getData(online.getLinkage(), startString, endString);
	    String identifier = online.getName();
	    String[] splittedId = identifier.split(":");
	    String units = (splittedId.length > 2) ? splittedId[splittedId.length - 1] : splittedId[1];
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

	    if (units.contains("°C") || units.contains("ºC")) {
		units = units.replace("°C", "K").replace("ºC", "K");
		isTemperature = true;
	    }

	    Set<Long> timeSet = new HashSet<>();

	    if (ret != null) {
		TimeSeriesResponseType tsrt = getTimeSeriesTemplate();
		DatatypeFactory xmlFactory = DatatypeFactory.newInstance();
		JSONArray data = ret.optJSONArray("readings");
		for (int j = 0; j < data.length(); j++) {

		    JSONArray dataTime = data.optJSONArray(j);

		    BigDecimal value = dataTime.optBigDecimal(1, null);// obj.optBigDecimal(varId.toLowerCase(),
								       // null);// data.optString("value");

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
			
			value = value.setScale(2, BigDecimal.ROUND_FLOOR);
			//int valueInteger = value.multiply(new BigDecimal(100)).intValue();
			
			variable.setValue(value);

			String date = dataTime.optString(0, null);

			if (date != null) {
			    Optional<Date> parsed = ISO8601DateTimeUtils.parseISO8601ToDate(date);

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
			    }
			}
		    }
		}

		JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
		tempFile = File.createTempFile(getClass().getSimpleName(), ".wml");

		tempFile.deleteOnExit();
		JAXBWML.getInstance().marshal(response, tempFile);

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
		"SMART CITIZEN KIT ERROR");
    }

    private JSONObject getData(String linkage, String startString, String endString) {
	JSONObject ret;
	String dataURL = linkage + "&from=" + startString + "&to=" + endString;
	Optional<String> dataResult = downloader.downloadOptionalString(dataURL);
	ret = (dataResult != null && dataResult.isPresent()) ? new JSONObject(dataResult.get()) : null;
	return ret;
    }

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

}
