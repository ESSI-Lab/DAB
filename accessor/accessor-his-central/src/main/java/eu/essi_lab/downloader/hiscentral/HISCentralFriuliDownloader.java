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
import eu.essi_lab.accessor.hiscentral.friuli.HISCentralFriuliConnector;
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
 * https://omnia-develop.osmer.fvg.it/api/ws/data?measure_id=17968&from=202201010000&to=202202020000
 */

public class HISCentralFriuliDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_FRIULI_DOWNLOAD_ERROR = "HISCENTRAL_FRIULI_DOWNLOAD_ERROR";

    private HISCentralFriuliConnector connector;
    private Downloader downloader;

    /**
     * 
     */
    public HISCentralFriuliDownloader() {

	connector = new HISCentralFriuliConnector();
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

	    Optional<Date> startDate = ISO8601DateTimeUtils.parseISO8601ToDate(startString);
	    Optional<Date> endDate = ISO8601DateTimeUtils.parseISO8601ToDate(endString);
	    List<Date[]> dates = new ArrayList<Date[]>();
	    if (startDate.isPresent() && endDate.isPresent()) {
		dates = buildDates(startDate.get(), endDate.get());
	    }

	    TimeSeriesResponseType tsrt = getTimeSeriesTemplate();
	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    for (Date[] d : dates) {

		startString = convertDate(ISO8601DateTimeUtils.getISO8601DateTime(d[0]));
		endString = convertDate(ISO8601DateTimeUtils.getISO8601DateTime(d[1]));
		String linkage = online.getLinkage() + "&from=" + startString + "&to=" + endString;
		JSONObject jsonObj = getData(linkage);

		if (jsonObj != null && !jsonObj.isEmpty()) {

		    JSONArray valuesData = jsonObj.optJSONArray("data");

		    DateFormat iso8601OutputFormat = null;

		    if (valuesData != null) {

			for (Object arr : valuesData) {

			    JSONObject data = (JSONObject) arr;

			    String valueString = data.optString("value");

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

				String date = data.optString("datetime");

				if (iso8601OutputFormat == null) {
				    iso8601OutputFormat = date.contains(" ") ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN)
					    : new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
				    iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				}

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
		HISCENTRAL_FRIULI_DOWNLOAD_ERROR);

    }

    private JSONObject getData(String linkage) throws GSException {
	GSLoggerFactory.getLogger(getClass()).info("Getting BEARER TOKEN from FVG Omnia service");

	String result = null;
	String token = null;

	try {

	    if (HISCentralFriuliConnector.BEARER_TOKEN == null) {
		HISCentralFriuliConnector.BEARER_TOKEN = HISCentralFriuliConnector.getBearerToken(connector.getSourceURL());
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
		    HttpHeaderUtils.build("Authorization", "Bearer " + HISCentralFriuliConnector.BEARER_TOKEN));

	    stream = getStationResponse.body();

	    GSLoggerFactory.getLogger(getClass()).info("Got " + linkage);

	    if (stream != null) {
		JSONObject obj = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
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
		    HISCENTRAL_FRIULI_DOWNLOAD_ERROR);
	}

	return null;
    }

    private List<Date[]> buildDates(Date startDate, Date endDate) {
	List<Date[]> dateRanges = new ArrayList<>();

	// Calculate the difference in days between the two dates
	long diffInMillies = Math.abs(endDate.getTime() - startDate.getTime());
	long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

	// If the difference is 30 days or less, return the original range
	if (diffInDays <= 30) {
	    dateRanges.add(new Date[] { startDate, endDate });
	    return dateRanges;
	}

	// Otherwise, split the dates into ranges of at most 30 days
	Date currentStartDate = startDate;
	boolean maxRequestsReached = false;
	int count = 0;
	while (diffInDays > 30) {
	    Date currentEndDate = new Date(currentStartDate.getTime() + TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS));
	    dateRanges.add(new Date[] { currentStartDate, currentEndDate });
	    currentStartDate = new Date(currentEndDate.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
	    diffInDays -= 30;

	    // if (diff > 2) {
	    // maxRequestsReached = true;
	    // endDate = new Date(currentStartDate.getTime() + TimeUnit.MILLISECONDS.convert(10, TimeUnit.DAYS));
	    // }
	}

	// Add the final range
	dateRanges.add(new Date[] { currentStartDate, endDate });

	return dateRanges;
    }

    /**
     * CONVERT DATE FROM ISO TO OMNIA SERVICE PARAMETER (YYYYMMDDHHmm)
     **/
    private String convertDate(String date) {
	// TODO Auto-generated method stub
	// 2022-09-12T09:36:00Z -> 202209120936
	// 2022-09-19T09:36:00Z -> 202209190936
	String result;
	result = date.substring(0, date.length() - 4).replaceAll("-", "").replaceAll(":", "").replace("T", "");
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
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_FRIULI_NS_URI));
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
}
