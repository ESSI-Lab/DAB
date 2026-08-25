package eu.essi_lab.downloader.hiscentral;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.hiscentral.abruzzo.HISCentralAbruzzoConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
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
 * Downloads Abruzzo Polaris time series as WaterML 1.1.
 * <p>
 * Data request example:<br>
 * {@code .../data/series?api_token=...&date_start=202608010000&date_end=202608120000&measures[10_882]=val_data}
 * </p>
 * 
 * @author Roberto
 */
public class HISCentralAbruzzoDownloader extends WMLDataDownloader {

    private static final String HISCENTRAL_ABRUZZO_DOWNLOAD_ERROR = "HISCENTRAL_ABRUZZO_DOWNLOAD_ERROR";

    private static final BigDecimal NO_DATA_VALUE = new BigDecimal("-9999");

    private HISCentralAbruzzoConnector connector;
    private Downloader downloader;

    public HISCentralAbruzzoDownloader() {

	connector = new HISCentralAbruzzoConnector();
	downloader = new Downloader();
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

	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	if (extent != null) {

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

	    String dateStart = convertDate(startString);
	    String dateEnd = convertDate(endString);

	    HISCentralAbruzzoConnector.API_TOKEN = ConfigurationWrapper.getCredentialsSetting().getAbruzzoApiToken()
		    .orElse(null);

	    String linkage = online.getLinkage();
	    linkage = ensureApiTokenInLinkage(linkage);
	    if (linkage.contains("?")) {
		linkage = linkage + "&date_start=" + dateStart + "&date_end=" + dateEnd;
	    } else {
		linkage = linkage + "?date_start=" + dateStart + "&date_end=" + dateEnd;
	    }

	    JSONObject response = getData(linkage);

	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    DateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
	    DatatypeFactory xmlFactory = DatatypeFactory.newInstance();

	    if (response != null) {

		JSONArray seriesArray = response.optJSONArray("series");
		if (seriesArray != null) {

		    for (int i = 0; i < seriesArray.length(); i++) {

			JSONObject series = seriesArray.getJSONObject(i);
			JSONObject data = series.optJSONObject("data");
			if (data == null) {
			    continue;
			}

			Iterator<String> keys = data.keys();
			List<String> sortedKeys = new ArrayList<>();
			while (keys.hasNext()) {
			    sortedKeys.add(keys.next());
			}
			sortedKeys.sort(String::compareTo);

			for (String dateKey : sortedKeys) {

			    String valueString = data.optString(dateKey, null);

			    ValueSingleVariable variable = new ValueSingleVariable();

			    if (valueString != null && !valueString.isEmpty() && !"null".equalsIgnoreCase(valueString)) {
				variable.setValue(new BigDecimal(valueString));
			    } else {
				variable.setValue(NO_DATA_VALUE);
			    }

			    Date parsed = iso8601OutputFormat.parse(dateKey);
			    GregorianCalendar gregCal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			    gregCal.setTime(parsed);
			    XMLGregorianCalendar xmlGregCal = xmlFactory.newXMLGregorianCalendar(gregCal);
			    variable.setDateTimeUTC(xmlGregCal);

			    addValue(tsrt, variable);
			}
		    }
		}
	    }

	    return tsrt.getDataFile();

	} catch (Exception e) {
	    ex = e;
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	throw GSException.createException(//
		getClass(), //
		ex != null ? ex.getMessage() : "Error occurred, unable to download data", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		HISCENTRAL_ABRUZZO_DOWNLOAD_ERROR, //
		ex);
    }

    private JSONObject getData(String linkage) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Getting data from Abruzzo Polaris service: {}", linkage);

	try {

	    if (HISCentralAbruzzoConnector.API_TOKEN == null) {
		HISCentralAbruzzoConnector.API_TOKEN = ConfigurationWrapper.getCredentialsSetting().getAbruzzoApiToken()
			.orElse(null);
	    }

	    Optional<String> res = downloader.downloadOptionalString(linkage);
	    if (res.isPresent()) {
		return new JSONObject(res.get());
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + linkage, e);
	    HISCentralAbruzzoConnector.API_TOKEN = null;
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + linkage, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_ABRUZZO_DOWNLOAD_ERROR, //
		    e);
	}

	return null;
    }

    /**
     * Replaces or appends {@code api_token} using the current credentials value.
     */
    private String ensureApiTokenInLinkage(String linkage) {

	String token = HISCentralAbruzzoConnector.API_TOKEN;
	if (token == null) {
	    return linkage;
	}

	if (linkage.contains("api_token=")) {
	    return linkage.replaceAll("api_token=[^&]*", "api_token=" + token);
	}

	if (linkage.contains("?")) {
	    return linkage + "&api_token=" + token;
	}

	return linkage + "?api_token=" + token;
    }

    /**
     * Converts ISO-8601 datetime to Polaris {@code YmdHi} format (YYYYMMDDhhmm).
     */
    private static String convertDate(String date) {

	Optional<Date> optional = ISO8601DateTimeUtils.parseISO8601ToDate(date);
	if (optional.isPresent()) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
	    sdf.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));
	    return sdf.format(optional.get());
	}

	// fallback: strip separators from date part and append midnight
	String cleaned = date.replace("Z", "").replace("T", " ");
	String[] parts = cleaned.split(" ");
	String day = parts[0].replace("-", "");
	String time = "0000";
	if (parts.length > 1) {
	    time = parts[1].replace(":", "");
	    if (time.length() >= 4) {
		time = time.substring(0, 4);
	    }
	}
	return day + time;
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
		online.getLinkage().contains("idrodataabruzzo.siapmicros.com") && //
		online.getProtocol() != null && //
		online.getProtocol().equals(CommonNameSpaceContext.HISCENTRAL_ABRUZZO_NS_URI));
    }

    @Override
    public boolean canConnect() throws GSException {

	try {
	    HISCentralAbruzzoConnector.ensureApiToken();
	    return HttpConnectionUtils.checkConnectivity(HISCentralAbruzzoConnector.BASE_URL + "ping?api_token="
		    + HISCentralAbruzzoConnector.API_TOKEN);
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
