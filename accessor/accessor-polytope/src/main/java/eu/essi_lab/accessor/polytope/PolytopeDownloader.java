package eu.essi_lab.accessor.polytope;

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
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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

public class PolytopeDownloader extends WMLDataDownloader {

    private Downloader downloader;

    public PolytopeDownloader() {
	downloader = new Downloader();
    }

    @Override
    public boolean canDownload() {
	return (online.getFunctionCode() != null && //
		online.getFunctionCode().equals("download") && //
		online.getLinkage() != null && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.POLYTOPE));
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

	    Optional<String> dataResponse = downloader.downloadOptionalString(online.getLinkage());

	    if (dataResponse.isPresent()) {

		TimeSeriesResponseType tsrt = addValues(startString, endString, dataResponse.get());

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
		"POLYTOPE ERROR");

    }

    private TimeSeriesResponseType addValues(String startString, String endString, String csv) {

	TimeSeriesResponseType tsrt = getTimeSeriesTemplate();

	try {
	    // delimiter seems to be ; by default
	    Reader in = new StringReader(csv);
	    String d = ";";
	    char delimiter = d.charAt(0);
	    Iterable<CSVRecord> records = CSVFormat.RFC4180.withDelimiter(delimiter).withFirstRecordAsHeader().parse(in);
	    tsrt = getValues(startString, endString, records);

	} catch (Exception e) {
	    // TODO: handle exception
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	    Reader reader = new StringReader(csv);
	    Iterable<CSVRecord> records = null;
	    try {
		records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);
	    } catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	    tsrt = getValues(startString, endString, records);

	}

	return tsrt;
    }

    private TimeSeriesResponseType getValues(String startString, String endString, Iterable<CSVRecord> records) {

	TimeSeriesResponseType tsrt = getTimeSeriesTemplate();

	Optional<Date> optStart = ISO8601DateTimeUtils.parseISO8601ToDate(startString);
	Optional<Date> optEnd = ISO8601DateTimeUtils.parseISO8601ToDate(endString);
	for (CSVRecord record : records) {
	    String date = record.get("andate@desc");
	    String time = record.get("antime@desc");
	    String varValue = record.get("obsvalue@body");
	    Date valueDate = PolytopeMapper.setTime(date, time);
	    if (valueDate != null && optStart.isPresent() && optEnd.isPresent()) {
		if (!isValid(optStart.get(), optEnd.get(), valueDate))
		    continue;

		ValueSingleVariable variable = new ValueSingleVariable();

		if (varValue != null && !varValue.isEmpty()) {
		    //
		    // value
		    //

		    BigDecimal dataValue = new BigDecimal(varValue);
		    dataValue = dataValue.setScale(2, BigDecimal.ROUND_DOWN);
		    variable.setValue(dataValue);

		    //
		    // date
		    //

		    GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		    gregCal.setTime(valueDate);

		    XMLGregorianCalendar xmlGregCal = null;
		    try {
			xmlGregCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCal);
		    } catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    variable.setDateTimeUTC(xmlGregCal);

		    addValue(tsrt, variable);
		}

	    }
	}

	return tsrt;
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
