package eu.essi_lab.accessor.hiscentral.puglia.arpa;

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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.github.jsonldjava.shaded.com.google.common.base.Charsets;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Roberto
 */
public class HISCentralARPAPugliaConnector extends HarvestedQueryConnector<HISCentralARPAPugliaConnectorSetting> {

    /**
     * 
     */
    static final String TYPE = "HISCentralARPAPugliaConnector";
    private Downloader downloader;
    private List<CSVRecord> csvParameters;

    /**
     * 
     */
    public HISCentralARPAPugliaConnector() {
	downloader = new Downloader();
	originalMetadata = new JSONObject();
    }

    /**
     * 
     */

    static final String STATIONS_URL = "Stations";

    public static final String BASE_URL = "https://cloud.arpa.puglia.it/QualitaAria/";

    private static final int STEP = 10;

    private JSONObject allStation;

    private JSONObject stationsParameter;

    /**
     * 
     */
    static final String SENSOR_URL = "Pollutants";

    private static final String HIS_CENTRAL_ARPA_PUGLIA_CONNECTOR_DOWNLOAD_ERROR = "HIS_CENTRAL_ARPA_PUGLIA_CONNECTOR_DOWNLOAD_ERROR";

    /**
     * 
     */

    JSONObject originalMetadata;

    private int maxRecords;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private int partialNumbers;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	int page = 0;

	if (request.getResumptionToken() != null) {

	    page = Integer.valueOf(request.getResumptionToken());
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && page > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}
	if (allStation == null) {
	    allStation = getMetadataList(STATIONS_URL);
	}

	if (stationsParameter == null) {
	    stationsParameter = getMetadataList("");
	}

	if (csvParameters == null) {
	    getCSVRecords();
	}

	JSONArray metadataArray = allStation.optJSONArray("features");

	if (page < metadataArray.length() && !maxNumberReached) {

	    JSONObject datasetMetadata = metadataArray.getJSONObject(page);
	    JSONObject stationProperty = datasetMetadata.optJSONObject("properties");
	    String stationId = stationProperty.optString("id_station");
	    JSONArray checkArray = allStation.optJSONArray("features");
	    for (int i = 0; i < checkArray.length(); i++) {
		JSONObject feature = checkArray.getJSONObject(i);
		JSONObject properties = feature.getJSONObject("properties");
		String targetId = properties.getString("id_station");
		String variable = properties.getString("inquinante_misurato");
		if (stationId.equals(targetId)) {

		}
	    }
	    JSONArray stationsArray = datasetMetadata.optJSONArray("station");
	    JSONArray aggregationArray = datasetMetadata.optJSONArray("aggregation");
	    List<JSONObject> stationList = new ArrayList<JSONObject>();
	    List<JSONObject> aggregationList = new ArrayList<JSONObject>();
	    for (int j = 0; j < stationsArray.length(); j++) {
		stationList.add(stationsArray.optJSONObject(j));
	    }
	    for (int k = 0; k < aggregationArray.length(); k++) {
		aggregationList.add(aggregationArray.optJSONObject(k));
	    }
	    datasetMetadata.remove("station");
	    datasetMetadata.remove("aggregation");
	    for (JSONObject stationInfo : stationList) {
		for (JSONObject aggregationInfo : aggregationList) {
		    ret.addRecord(HISCentralARPAPugliaMapper.create(datasetMetadata, stationInfo, aggregationInfo));
		    partialNumbers++;
		}
	    }

	    if (page == (metadataArray.length() - 1)) {
		ret.setResumptionToken(null);
	    } else {
		ret.setResumptionToken(String.valueOf(page + 1));
	    }
	    logger.debug("ADDED {} records for variable {}", partialNumbers, datasetMetadata.optString("description"));

	} else {
	    ret.setResumptionToken(null);

	    logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, metadataArray.length());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    private void getCSVRecords() {

	Optional<InputStream> stream = downloader.downloadOptionalStream(getSourceURL() + SENSOR_URL);

	try {
	    if (stream.isPresent()) {
		CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
		try (CSVParser parser = new CSVParser(new InputStreamReader(stream.get(), Charsets.ISO_8859_1), format)) {

		    for (CSVRecord record : parser) {
			csvParameters.add(record);

		    }

		}

	    }
	} catch (Exception e) {
	    logger.error(HIS_CENTRAL_ARPA_PUGLIA_CONNECTOR_DOWNLOAD_ERROR + ": Error to read CSV file");
	}

    }

    private JSONObject getInfo(String param) throws GSException {

	String url = getSourceURL() + param;

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url.trim());

	    stream = getStationResponse.body();

	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    if (stream != null) {
		JSONObject jsonResult = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return jsonResult;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_ARPA_PUGLIA_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    private JSONObject getParameters(String id) throws GSException {

	String url = getSourceURL() + STATIONS_URL + "/" + id;

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url.trim());

	    stream = getStationResponse.body();

	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    if (stream != null) {
		JSONObject jsonResult = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return jsonResult;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_ARPA_PUGLIA_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    private JSONObject getMetadataList(String path) throws GSException {

	String url = getSourceURL() + path + "?format=GeoJSON";

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url.trim() //
	    );

	    stream = getStationResponse.body();

	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    if (stream != null) {
		JSONObject jsonResult = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return jsonResult;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_ARPA_PUGLIA_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_ARPA_PUGLIA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("cloud.arpa.puglia.it");
    }

    @Override
    protected HISCentralARPAPugliaConnectorSetting initSetting() {

	return new HISCentralARPAPugliaConnectorSetting();
    }

    @Override
    public String getSourceURL() {

	String url = super.getSourceURL();
	if (!url.endsWith("/")) {
	    url += "/";
	}

	return url;

    }

    public static void main(String[] args) throws Exception {

    }

}
