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
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private int partialNumbers;

    public PolytopeIonBeamMetadataConnector() {

	this.downloader = new Downloader();
	stationsArray = new JSONArray();

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

	    Long time = null;

	    if (properties != null && !properties.isEmpty() && !listRecords.isRecovered()) {

		String timestamp = properties.getEndHarvestingTimestamp();
		if (timestamp != null) {
		    @SuppressWarnings("deprecation")
		    Date date = ISO8601DateTimeUtils.parseISO8601(timestamp);
		    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		    calendar.setTime(date);
		    calendar.set(Calendar.HOUR_OF_DAY, 0);
		    calendar.set(Calendar.MINUTE, 0);
		    calendar.set(Calendar.SECOND, 0);
		    calendar.set(Calendar.MILLISECOND, 0);
		    Date updatedDate = calendar.getTime();
		    time = updatedDate.getTime();
		    GSLoggerFactory.getLogger(getClass()).info("Incremental harvesting enabled starting from: " + timestamp);
		}
	    }

	    if (stationsArray.isEmpty()) {
		stationsArray = getList(STATIONS_URL, time);
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

				for (PolytopeIonBeamMetadataMeteoTrackerVariable var : PolytopeIonBeamMetadataMeteoTrackerVariable
					.values()) {
				    ret.addRecord(PolytopeIonBeamMetadataMeteoTrackerMapper.create(datasetMetadata, var.getKey()));
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
		if(isLast) {
		    ret.setResumptionToken(null);    
		} else {
		    ret.setResumptionToken(String.valueOf(start + count));
		}
		logger.debug("ADDED {} records. Number of analyzed stations: {}", partialNumbers, String.valueOf(start + count));
	    } else {
		GSLoggerFactory.getLogger(getClass()).info("ERROR getting items.");
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

    private JSONArray getList(String path, Long time) throws Exception {

	String url = getSourceURL() + path;

	if (time != null) {
	    // query with time
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
	return false;
    }

    @Override
    protected PolytopeIonBeamMetadataConnectorSetting initSetting() {

	return new PolytopeIonBeamMetadataConnectorSetting();
    }

    public static String getBearerToken() {
	return ConfigurationWrapper.getCredentialsSetting().getPolytopePassword().orElse(null);
    }
}
