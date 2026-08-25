package eu.essi_lab.accessor.hiscentral.abruzzo;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Connector for Regione Abruzzo Polaris API
 * (https://idrodataabruzzo.siapmicros.com/api/polaris/).
 * <p>
 * Harvests stations and their measures; each station/measure pair becomes one
 * original metadata record.
 * </p>
 * 
 * @author Roberto
 */
public class HISCentralAbruzzoConnector extends HarvestedQueryConnector<HISCentralAbruzzoConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "HISCentralAbruzzoConnector";

    /**
     * 
     */
    public static final String BASE_URL = "https://idrodataabruzzo.siapmicros.com/api/polaris/";

    /**
     * API token for Abruzzo Polaris, loaded from {@link eu.essi_lab.cfga.gs.setting.CredentialsSetting}
     */
    public static String API_TOKEN = null;

    static final String STATIONS_PATH = "stations";

    static final String MEASURES_PATH = "measures";

    static final String DATA_SERIES_PATH = "data/series";

    private static final String HIS_CENTRAL_ABRUZZO_CONNECTOR_DOWNLOAD_ERROR = "HIS_CENTRAL_ABRUZZO_CONNECTOR_DOWNLOAD_ERROR";

    private List<JSONObject> stations;

    private int partialNumbers;

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public HISCentralAbruzzoConnector() {

	this.downloader = new Downloader();
	this.stations = new ArrayList<>();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint != null && endpoint.contains("idrodataabruzzo.siapmicros.com");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	if (API_TOKEN == null) {
	    API_TOKEN = ConfigurationWrapper.getCredentialsSetting().getAbruzzoApiToken().orElse(null);
	}

	if (stations.isEmpty()) {
	    stations = getAllStations();
	}

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {
	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    maxNumberReached = true;
	}

	if (start < stations.size() && !maxNumberReached) {

	    int end = start + pageSize;
	    if (end > stations.size()) {
		end = stations.size();
	    }

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		end = mr.get();
	    }

	    int count = 0;

	    for (int i = start; i < end; i++) {

		JSONObject station = stations.get(i);
		int stationId = station.optInt("id", -1);
		if (stationId < 0) {
		    count++;
		    continue;
		}

		JSONArray measures = getMeasuresForStation(stationId);
		if (measures != null) {
		    for (int j = 0; j < measures.length(); j++) {
			JSONObject measure = measures.getJSONObject(j);
			ret.addRecord(HISCentralAbruzzoMapper.create(station, measure));
			partialNumbers++;
		    }
		}

		count++;
	    }

	    ret.setResumptionToken(String.valueOf(start + count));
	    logger.debug("ADDED {} records. Analyzed stations: {}", partialNumbers, String.valueOf(start + count));

	} else {

	    ret.setResumptionToken(null);
	    logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, stations.size());
	    partialNumbers = 0;
	    stations.clear();
	    return ret;
	}

	return ret;
    }

    /**
     * @return source URL ending with {@code /}
     */
    private String getBaseURL() {

	String url = getSourceURL();
	if (url == null || url.isEmpty()) {
	    url = BASE_URL;
	}
	if (!url.endsWith("/")) {
	    url = url + "/";
	}
	return url;
    }

    /**
     * @return all stations from the Polaris API
     * @throws GSException
     */
    private List<JSONObject> getAllStations() throws GSException {

	logger.trace("SIR ABRUZZO stations finding STARTED");

	List<JSONObject> ret = new ArrayList<>();
	int page = 1;
	boolean hasNext = true;

	while (hasNext) {

	    String url = getBaseURL() + STATIONS_PATH + "?api_token=" + API_TOKEN + "&limit=100&page=" + page;
	    logger.trace("SIR ABRUZZO stations URL: {}", url);

	    Optional<String> response = downloader.downloadOptionalString(url);
	    if (!response.isPresent()) {
		throw GSException.createException(//
			this.getClass(), //
			"Unable to retrieve stations from Abruzzo Polaris API", //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			HIS_CENTRAL_ABRUZZO_CONNECTOR_DOWNLOAD_ERROR);
	    }

	    JSONObject json = new JSONObject(response.get());
	    JSONArray items = json.optJSONArray("items");
	    if (items != null) {
		for (int i = 0; i < items.length(); i++) {
		    ret.add(items.getJSONObject(i));
		}
	    }

	    JSONObject metadata = json.optJSONObject("metadata");
	    if (metadata != null) {
		JSONObject pageMeta = metadata.optJSONObject("page");
		if (pageMeta != null && !pageMeta.isNull("next")) {
		    page = pageMeta.optInt("next", page + 1);
		} else {
		    hasNext = false;
		}
	    } else {
		hasNext = false;
	    }
	}

	logger.trace("SIR ABRUZZO stations finding ENDED. Found {} stations", ret.size());
	return ret;
    }

    /**
     * @param stationId
     * @return measures for the given station
     */
    private JSONArray getMeasuresForStation(int stationId) {

	String url = getBaseURL() + MEASURES_PATH + "?api_token=" + API_TOKEN + "&limit=200&filter%5Bstation_id%5D="
		+ stationId;
	logger.trace("SIR ABRUZZO measures URL: {}", url);

	try {
	    Optional<String> response = downloader.downloadOptionalString(url);
	    if (response.isPresent()) {
		JSONObject json = new JSONObject(response.get());
		JSONObject items = json.optJSONObject("items");
		if (items != null) {
		    JSONArray measures = items.optJSONArray(String.valueOf(stationId));
		    if (measures != null) {
			return measures;
		    }
		    // fallback: flatten all arrays under items
		    JSONArray flattened = new JSONArray();
		    Iterator<String> keys = items.keys();
		    while (keys.hasNext()) {
			JSONArray arr = items.optJSONArray(keys.next());
			if (arr != null) {
			    for (int i = 0; i < arr.length(); i++) {
				flattened.put(arr.get(i));
			    }
			}
		    }
		    return flattened;
		}
	    }
	} catch (Exception e) {
	    logger.error("Unable to retrieve measures for station {}", stationId, e);
	}

	return null;
    }

    /**
     * Ensures {@link #API_TOKEN} is loaded from credentials settings.
     */
    public static void ensureApiToken() {

	if (API_TOKEN == null) {
	    API_TOKEN = ConfigurationWrapper.getCredentialsSetting().getAbruzzoApiToken().orElse(null);
	}
    }

    /**
     * Builds the data/series download linkage for a station/measure pair.
     * 
     * @param stationId
     * @param measureId
     * @return linkage URL (dates are appended by the downloader)
     */
    public static String buildDataSeriesLinkage(String baseUrl, int stationId, int measureId) {

	ensureApiToken();

	String base = baseUrl;
	if (!base.endsWith("/")) {
	    base = base + "/";
	}
	return base + DATA_SERIES_PATH + "?api_token=" + API_TOKEN + "&measures%5B" + stationId + "_" + measureId
		+ "%5D=val_data";
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_ABRUZZO_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected HISCentralAbruzzoConnectorSetting initSetting() {

	return new HISCentralAbruzzoConnectorSetting();
    }
}
