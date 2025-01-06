package eu.essi_lab.accessor.acronet;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 */
public class AcronetConnector extends HarvestedQueryConnector<AcronetConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "AcronetConnector";

    /**
     * SERVICE ENDPOINT: https://api.smartcitizen.me/v0/
     * GET I-CHANGE DEVICES: https://api.smartcitizen.me/v0/devices?with_tags=I-CHANGE&page=1&per_page=100
     * GET DATA FROM DEVICES AND SENSOR:
     * https://api.smartcitizen.me/v0/devices/{deviceId}/readings?sensor_id={sensorId}&rollup=1m&from={startDate}&to={endDate}
     * COLLECTIONS: https://polytope.ecmwf.int/api/v1/collections
     */
    private static final String ACRONET_STATION_ERROR = "Unable to find stations URL";
    private static final String ACRONET_PARSING_STATION_ERROR = "ACRONET_PARSING_STATION_ERROR";
    private static final String ACRONET_STATIONS_URL_NOT_FOUND_ERROR = "ACRONET_STATIONS_URL_NOT_FOUND_ERROR";

    static final String DEVICE_PATH = "devices";
    static final String SENSORS_PATH = "sensors/list/";
    static final String STATIONGROUP = "ComuneLive%25IChange";
    // I-CHANGE Device
    // https://api.smartcitizen.me/v0/devices?with_tags=I-CHANGE&page=1&per_page=100
    // Variables
    // https://api.smartcitizen.me/v0/measurements?page=1&per_page=100

    static final String BASE_URL = "https://webdrops.cimafoundation.org/app/";

    static final String TOKEN_URL = "https://testauth.cimafoundation.org/auth/realms/webdrops/protocol/openid-connect/token";

    public static String ACRONET_BEARER_TOKEN = null;

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private int partialNumbers;

    private List<String> variablesIdentifier;

    public AcronetConnector() {

	this.downloader = new Downloader();

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	// GET REQUEST to read acronet sensors:
	// https://webdrops.cimafoundation.org/app/sensors/classes/
	//
	// GET SENSOR LIST
	// https://webdrops.cimafoundation.org/app/sensors/list/TERMOMETRO/?stationgroup=ComuneLive%25IChange

	String token = listRecords.getResumptionToken();
	int start = 1;
	if (token != null) {

	    start = Integer.valueOf(token);
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	AcronetClient client = new AcronetClient(BASE_URL);

	try {

	    if (variablesIdentifier == null) {
		variablesIdentifier = new ArrayList<String>();
		JSONArray varArray = client.getVariables();
		if (varArray != null && !varArray.isEmpty()) {
		    for (int j = 0; j < varArray.length(); j++) {
			String varId = varArray.optString(j);
			if (varId != null && !varId.isEmpty()) {
			    AcronetVariable acrVar = AcronetVariable.decode(varId);
			    if (acrVar != null) {
				variablesIdentifier.add(varId);
			    }
			}
		    }
		}

	    }

	    for (String s : variablesIdentifier) {
		List<JSONObject> sensorList = client.getSensors(s);
		if (sensorList != null && !sensorList.isEmpty()) {
		    List<String> duplicationList = new ArrayList<String>(); 
		    for (JSONObject obj : sensorList) {
			String stationName = obj.optString("name");
			if(!duplicationList.contains(stationName)) {
			    duplicationList.add(stationName);
			    ret.addRecord(AcronetMapper.create(obj, s));
			    partialNumbers++;
			} else {
			    logger.debug("Station {} already added", stationName);
			}
		    }
		}
	    }
	    logger.debug("ADDED {} records. Number of Variables: {}", partialNumbers, variablesIdentifier.size());

	    // int recordCount = 0;
	    // if (!deviceArray.isEmpty()) {
	    //
	    // recordCount = deviceArray.length();
	    //
	    // for (int i = 0; i < recordCount; i++) {
	    //
	    // JSONObject datasetMetadata = deviceArray.getJSONObject(i);
	    // String id = datasetMetadata.optString("id");
	    // if (id != null && !id.isEmpty()) {
	    //
	    // JSONObject dataObj = datasetMetadata.optJSONObject("data");
	    //
	    // if (dataObj != null) {
	    // JSONArray sensorArray = dataObj.optJSONArray("sensors");
	    // if (sensorArray != null) {
	    // for (int k = 0; k < sensorArray.length(); k++) {
	    //
	    // JSONObject sensorInfo = sensorArray.getJSONObject(k);
	    // JSONObject mesurementObj = sensorInfo.optJSONObject("measurement");
	    // String key = mesurementObj.optString("uuid");
	    //
	    // if (key != null && variablesIdentifier.contains(key)
	    // && !key.equals("49a89988-2385-4a92-8bde-dc48de240aab")) {
	    // ret.addRecord(AcronetMapper.create(datasetMetadata, sensorInfo));
	    // partialNumbers++;
	    // }
	    // }
	    // }
	    // }
	    //
	    // }
	    //
	    // }
	    // ret.setResumptionToken(String.valueOf(start + 1));
	    // logger.debug("ADDED {} records. Number of analyzed records: {}", partialNumbers, recordCount);
	    // } else {
	    // ret.setResumptionToken(null);
	    //
	    // logger.debug("Added Collection records: {}", partialNumbers);
	    // partialNumbers = 0;
	    // return ret;
	    // }

	} catch (Exception e) {
	    throw GSException.createException(//
		    this.getClass(), e.getMessage(), null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ACRONET_PARSING_STATION_ERROR);

	}

	return ret;

    }

    private JSONArray getData(Integer startPage, Integer pageSize) throws Exception {
	JSONArray arr = new JSONArray();
	String url;
	if (startPage != null && pageSize != null) {
	    url = getSourceURL() + DEVICE_PATH + "?with_tags=I-CHANGE&page=" + startPage + "&per_page=" + pageSize;
	} else {
	    url = getSourceURL() + SENSORS_PATH + "?page=1&per_page=100";
	}

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);
	Optional<InputStream> getResult = getDownloader().downloadOptionalStream(url);
	InputStream stream = null;
	ClonableInputStream cis = null;
	if (getResult.isPresent()) {
	    stream = getResult.get();
	    cis = new ClonableInputStream(stream);
	    GSLoggerFactory.getLogger(getClass()).info("Stream result " + IOStreamUtils.asUTF8String(cis.clone()));
	    arr = new JSONArray(IOStreamUtils.asUTF8String(cis.clone()));
	    stream.close();

	}

	return arr;
    }

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public void setDownloader(Downloader downloader) {

	this.downloader = downloader;
    }

    /**
     * @return
     * @throws GSException
     */
    public Optional<File> findStations() throws GSException {

	// TODO: to be implemented
	//
	// logger.trace("Stations URL finding STARTED");
	//
	// String polytopeUrl = getSourceURL();
	//
	// logger.trace("Polytope URL: {}", polytopeUrl);
	//
	// String url = addCredentialsInRequests(polytopeUrl);
	//
	// File metadataFile = null;
	//
	// try {
	// metadataFile = downloader.downloadStream(url, POLYTOPE_URL_METADATA_PATH);
	// } catch (Exception e) {
	// throw GSException.createException(//
	// this.getClass(), e.getMessage(), null, //
	// ErrorInfo.ERRORTYPE_SERVICE, //
	// ErrorInfo.SEVERITY_ERROR, //
	// POLYTOPE_PARSING_STATION_ERROR);
	// }
	//
	// return Optional.of(metadataFile);
	return null;
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.ACRONET);
	// toret.add(CommonNameSpaceContext.POLYTOPE_METEOTRACKER);
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

	return source.getEndpoint().contains("cimafoundation.org");

    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected AcronetConnectorSetting initSetting() {

	return new AcronetConnectorSetting();
    }
}
