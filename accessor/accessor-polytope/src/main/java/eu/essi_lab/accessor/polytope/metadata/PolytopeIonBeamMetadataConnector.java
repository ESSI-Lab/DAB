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
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 */
public class PolytopeIonBeamMetadataConnector extends HarvestedQueryConnector<PolytopeIonBeamMetadataConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "PolytopeIonBeamConnector";

    /**
     * SERVICE ENDPOINT: https://polytope.ecmwf.int/api/v1/
     * COLLECTIONS: https://polytope.ecmwf.int/api/v1/collections
     * 25 columns (first column (0) empty)
     * ;obstype@body;codetype@body;entryno@body;varno@body;date@hdr;time@hdr;stalt@hdr;statid@hdr;obstype@hdr;codetype@hdr;source@hdr;
     * groupid@hdr;reportype@hdr;class@desc;type@desc;stream@desc;expver@desc;levtype@desc;andate@desc;antime@desc;lat@hdr;lon@hdr;obsvalue@body;stationid@hdr,
     * column0: number of measures per day (cumulative)
     * column4: varno (identifier of variable)
     * column19: andate (date of measure)
     * column20: antime (time of measure)
     * column21: lat (station latitude)
     * column22: lon (station longitude)
     * column23: obsvalue (value of variable)
     * column24: stationid (Name of station)
     */
    private static final String POLYTOPE_STATION_ERROR = "Unable to find stations URL";
    private static final String POLYTOPE_PARSING_STATION_ERROR = "POLYTOPE_PARSING_STATION_ERROR";
    private static final String POLYTOPE_STATIONS_URL_NOT_FOUND_ERROR = "POLYTOPE_STATIONS_URL_NOT_FOUND_ERROR";

    static final String STATIONS_URL = "stations";

    static final String MARS_REQUEST_URL = "list";

    static final String BASE_URL = "http://ionbeam-ichange.ecmwf-ichange.f.ewcloud.host/api/v1/";

    static final String RETRIEVE_URL = "retrieve";

    public static String BEARER_TOKEN;

    private Downloader downloader;

    private JSONArray stationsArray;

    private List<CSVRecord> meteotrackerCSV;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private int partialNumbers;

    public PolytopeIonBeamMetadataConnector() {

	this.downloader = new Downloader();
	stationsArray = new JSONArray();
	meteotrackerCSV = new ArrayList<CSVRecord>();

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	// post requests with Polytope Rest API to get data
	// e.g. endpoint: http://ionbeam-ichange.ecmwf-ichange.f.ewcloud.host/api/v1/stations
	// Authorization Bearer + psw
	//

	Optional<String> pswOpt = getSetting().getPolytopePassword();
	if (pswOpt.isPresent()) {
	    BEARER_TOKEN = pswOpt.get();
	} else {
	    BEARER_TOKEN = getBearerToken();
	}

	int count = 0;

	String token = listRecords.getResumptionToken();
	int start = 0;
	if (token != null) {

	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	try {

	    HarvestingProperties properties = listRecords.getHarvestingProperties();

	    Date dateTime = null;
	    String iso8601DateTime = null;

	    if (properties != null && !properties.isEmpty() && !listRecords.isRecovered()) {

		String timestamp = properties.getEndHarvestingTimestamp();
		if (timestamp != null) {
		    @SuppressWarnings("deprecation")
		    long time = ISO8601DateTimeUtils.parseISO8601(timestamp).getTime();
		    iso8601DateTime = ISO8601DateTimeUtils.getISO8601DateTime(new Date(time));
		    // dateTime = new Date(time);
		    GSLoggerFactory.getLogger(getClass()).info("Incremental harvesting enabled starting from: " + iso8601DateTime);
		}
	    }

	    if (start == 0) {
		// first loop
		stationsArray = getList(STATIONS_URL, iso8601DateTime);
	    } else {
		if (stationsArray.isEmpty()) {
		    stationsArray = getList(STATIONS_URL, iso8601DateTime);
		}
	    }

	    if (meteotrackerCSV.isEmpty()) {
		if(iso8601DateTime == null) {
		    iso8601DateTime = ISO8601DateTimeUtils.getISO8601DateTime(new Date());
		}
		Date[] dates = enlargeDates(iso8601DateTime, iso8601DateTime);
		String linkage = getSourceURL() + RETRIEVE_URL
			+ "?class=rd&expver=xxxx&stream=lwda&aggregation_type=by_time&platform=meteotracker";
		String startTime = ISO8601DateTimeUtils.getISO8601DateTime(dates[0]);
		String endTime = ISO8601DateTimeUtils.getISO8601DateTime(new Date());
		meteotrackerCSV = getCSVData(linkage, startTime, endTime, null, "meteotracker");
	    }

	    boolean isLast = false;
	    GSLoggerFactory.getLogger(getClass()).info("Station number: " + stationsArray.length());
	    if (start < stationsArray.length() && !maxNumberReached) {

		int end = start + pageSize;
		if (end > stationsArray.length()) {
		    end = stationsArray.length();
		    isLast = true;
		}

		if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		    end = mr.get();
		}

		for (int i = start; i < end; i++) {
		    count++;
		    JSONObject datasetMetadata = stationsArray.getJSONObject(i);
		    String id = datasetMetadata.optString("internal_id");

		    if (id != null && !id.isEmpty()) {

			String platform = datasetMetadata.optString("platform");

			if (platform != null) {

			    // JSONArray jsonArray = new JSONArray();
			    // for (int k = 0; k < marsRequestArray.length(); k++) {
			    // JSONObject m = marsRequestArray.getJSONObject(k);
			    // String req = m.optString("url");
			    // if (req.contains(id)) {
			    // jsonArray.put(m);
			    // }
			    // }

			    if (platform.toLowerCase().contains("acronet")) {
				for (PolytopeIonBeamMetadataAcronetVariable var : PolytopeIonBeamMetadataAcronetVariable.values()) {
				    ret.addRecord(PolytopeIonBeamMetadataMapper.create(datasetMetadata, var.getKey()));
				    partialNumbers++;
				}

			    } else if (platform.toLowerCase().contains("meteo")) {

				List<CSVRecord> mtCSV = new ArrayList<CSVRecord>();
				for(CSVRecord csv: meteotrackerCSV) {
				    String toMatch = csv.get("station_id");
				    if(toMatch.equals(id)) {
					mtCSV.add(csv);
				    }
				}
				for (PolytopeIonBeamMetadataMeteoTrackerVariable var : PolytopeIonBeamMetadataMeteoTrackerVariable
					.values()) {
				    ret.addRecord(PolytopeIonBeamMetadataMeteoTrackerMapper.create(datasetMetadata, var.getKey(), mtCSV));
				    partialNumbers++;
				}
			    } else if (platform.toLowerCase().contains("smart_citizen_kit")) {
				for (PolytopeIonBeamMetadataSmartKitVariable var : PolytopeIonBeamMetadataSmartKitVariable.values()) {
				    ret.addRecord(PolytopeIonBeamMetadataMapper.create(datasetMetadata, var.getKey()));
				    partialNumbers++;
				}
			    }

			}
		    }
		}
		if (isLast) {
		    ret.setResumptionToken(null);
		    meteotrackerCSV = new ArrayList<CSVRecord>();
		} else {
		    ret.setResumptionToken(String.valueOf(start + count));
		}
		logger.debug("ADDED {} records. Number of analyzed stations: {}", partialNumbers, String.valueOf(start + count));
	    } else {
		GSLoggerFactory.getLogger(getClass()).info("Getting empty stations/device items.");
	    }

	} catch (Exception e) {
	    throw GSException.createException(//
		    this.getClass(), e.getMessage(), null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    POLYTOPE_PARSING_STATION_ERROR);

	}

	return ret;

    }

    private JSONArray getList(String path, String dateString) throws Exception {

	String url = getSourceURL() + path;

	if (dateString != null) {
	    // String iso8601DateTime = ISO8601DateTimeUtils.getISO8601DateTime(date);
	    url += "?start_time=" + dateString;
	}

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	Downloader downloader = new Downloader();
	downloader.setRetryPolicy(20, TimeUnit.SECONDS, 2);

	HttpResponse<InputStream> stationResponse = downloader.downloadResponse(//
		url.trim(), //
		HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));

	InputStream stream = stationResponse.body();

	GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	if (stream != null) {
	    ClonableInputStream cis = new ClonableInputStream(stream);
	    // GSLoggerFactory.getLogger(getClass()).info("Stream result " + IOStreamUtils.asUTF8String(cis.clone()));
	    JSONArray arr = new JSONArray(IOStreamUtils.asUTF8String(cis.clone()));
	    stream.close();
	    return arr;
	}

	return null;
    }

    public static List<JSONObject> getResultList(String url) throws Exception {
	ArrayList<JSONObject> out = Lists.newArrayList();

	GSLoggerFactory.getLogger(PolytopeIonBeamMetadataConnector.class).info("Getting " + url);

	Downloader downloader = new Downloader();
	downloader.setRetryPolicy(20, TimeUnit.SECONDS, 2);

	if (BEARER_TOKEN == null) {
	    BEARER_TOKEN = getBearerToken();
	}

	HttpResponse<InputStream> stationResponse = downloader.downloadResponse(//
		url.trim(), //
		HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));

	InputStream stream = stationResponse.body();

	GSLoggerFactory.getLogger(PolytopeIonBeamMetadataConnector.class).info("Got " + url);

	if (stream != null) {
	    ClonableInputStream cis = new ClonableInputStream(stream);
	    // GSLoggerFactory.getLogger(PolytopeIonBeamMetadataConnector.class)
	    // .info("Stream result " + IOStreamUtils.asUTF8String(cis.clone()));
	    JSONArray arr = new JSONArray(IOStreamUtils.asUTF8String(cis.clone()));
	    if (arr != null) {
		for (int i = 0; i < arr.length(); i++) {
		    out.add(arr.optJSONObject(i));
		}
	    }
	    stream.close();
	    return out;
	}

	return null;

    }

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {

	this.downloader = downloader;
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.POLYTOPE_IONBEAM);
	toret.add(CommonNameSpaceContext.IONBEAM_TRACKER);
	return toret;
    }

    @Override
    public String getSourceURL() {
	String url = super.getSourceURL();
	if (!url.endsWith("/")) {
	    url += "/";
	}

	return url;
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("polytope.ecmwf.int") || source.getEndpoint().contains("ionbeam-dev.ecmwf.int")
		|| source.getEndpoint().contains("ionbeam-ichange");

    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {
	return true;
    }

    @Override
    protected PolytopeIonBeamMetadataConnectorSetting initSetting() {

	return new PolytopeIonBeamMetadataConnectorSetting();
    }

    public static List<CSVRecord> getCSVData(String linkage, String startTime, String endTime, String stationId, String platform)
	    throws Exception {

	List<CSVRecord> ret = new ArrayList<CSVRecord>();

	Iterable<CSVRecord> out = null;

	if (PolytopeIonBeamMetadataConnector.BEARER_TOKEN == null) {
	    PolytopeIonBeamMetadataConnector.BEARER_TOKEN = PolytopeIonBeamMetadataConnector.getBearerToken();
	}

	// Create the new parameters for the request
	// ?format=csv&start_time=2025-01-31T00%3A00%3A00Z&end_time=2025-01-31T23%3A59%3A59Z&station_id=246ede0dd7e9d4c8
	String updatedParameters = "";
	String updatedUrl = "";
	//get meteotracker sessions
	if (stationId == null) {
	    updatedParameters = "&start_time=" + startTime + "&end_time=" + endTime + "&format=csv";
	    updatedUrl = linkage + updatedParameters;

	//get data for downloaders
	} else {
	    updatedParameters = "format=csv&platform=" + platform + "&start_time=" + startTime + "&end_time=" + endTime + "&station_id="
		    + stationId;
	    updatedUrl = linkage.split("\\?")[0] + "?" + updatedParameters;
	}

	GSLoggerFactory.getLogger(PolytopeIonBeamMetadataConnector.class).info("Getting " + updatedUrl);

	HashMap<String, String> headers = new HashMap<>();
	headers.put("Authorization", "Bearer " + PolytopeIonBeamMetadataConnector.BEARER_TOKEN);
	Downloader downloader = new Downloader();
	Optional<String> response = downloader.downloadOptionalString(updatedUrl.trim(), HttpHeaderUtils.build(headers));

	if (response.isPresent()) {

	    String responseString = response.get();
	    // *no data use-case
	    if (responseString.toLowerCase().contains("no data found")) {
		return ret;
	    }
	    // *multiple requests needed
	    if (responseString.toLowerCase().contains("200 data granules")) {
		return null;
	    }
	    try {
		// delimiter seems to be ; by default
		Reader in = new StringReader(responseString);
		out = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
		for (CSVRecord r : out) {
		    ret.add(r);
		}

	    } catch (Exception e) {
		// multiple requests needeed
		GSLoggerFactory.getLogger(PolytopeIonBeamMetadataConnector.class).error(e.getMessage());

	    }

	}

	return ret;
    }
    
    public static Date[] enlargeDates(String startDate, String endDate) {
	Date[] dateRanges = null;
	Optional<Date> optStartDate = ISO8601DateTimeUtils.parseISO8601ToDate(startDate);
	Optional<Date> optEndDate = ISO8601DateTimeUtils.parseISO8601ToDate(endDate);
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	Date newStartDate = optStartDate.get();
	Calendar startCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startCalendar.setTime(newStartDate);

        // Set minutes, seconds, and milliseconds to zero (HH:00:00.000)
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        startCalendar.add(Calendar.HOUR_OF_DAY, -1);

        // Format the adjusted date back to string
        Optional<Date> newOptStartDate = ISO8601DateTimeUtils.parseISO8601ToDate(sdf.format(startCalendar.getTime()));
       
	Date newEndDate = optEndDate.get();
	
	Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endCalendar.setTime(newEndDate);

        endCalendar.add(Calendar.HOUR_OF_DAY, 1);
        endCalendar.set(Calendar.MINUTE, 0);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);
	
        Optional<Date> endOptStartDate = ISO8601DateTimeUtils.parseISO8601ToDate(sdf.format(endCalendar.getTime()));
	
        dateRanges = new Date[] { newOptStartDate.get(), endOptStartDate.get() };

	return dateRanges;
    }

    public static String getBearerToken() {
	return ConfigurationWrapper.getCredentialsSetting().getPolytopePassword().orElse(null);
    }
}
