package eu.essi_lab.accessor.smartcitizenkit;

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
public class SmartCitizenKitConnector extends HarvestedQueryConnector<SmartCitizenKitConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "SmartCitizenKitConnector";

    /**
     * SERVICE ENDPOINT: https://api.smartcitizen.me/v0/
     * GET I-CHANGE DEVICES: https://api.smartcitizen.me/v0/devices?with_tags=I-CHANGE&page=1&per_page=100
     * GET DATA FROM DEVICES AND SENSOR:
     * https://api.smartcitizen.me/v0/devices/{deviceId}/readings?sensor_id={sensorId}&rollup=1m&from={startDate}&to={endDate}
     * COLLECTIONS: https://polytope.ecmwf.int/api/v1/collections
     */
    private static final String SMART_CITIZEN_KIT_STATION_ERROR = "Unable to find stations URL";
    private static final String SMART_CITIZEN_KIT_PARSING_STATION_ERROR = "SMART_CITIZEN_KIT_PARSING_STATION_ERROR";
    private static final String SMART_CITIZEN_KIT_STATIONS_URL_NOT_FOUND_ERROR = "SMART_CITIZEN_KIT_STATIONS_URL_NOT_FOUND_ERROR";

    static final String DEVICE_PATH = "devices";
    static final String MEASUREMENTS_PATH = "measurements";
    // I-CHANGE Device
    // https://api.smartcitizen.me/v0/devices?with_tags=I-CHANGE&page=1&per_page=100
    // Variables
    // https://api.smartcitizen.me/v0/measurements?page=1&per_page=100

    static final String BASE_URL = "https://api.smartcitizen.me/v0/";

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private int partialNumbers;

    private List<String> variablesIdentifier;

    public SmartCitizenKitConnector() {

	this.downloader = new Downloader();

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest listRecords) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	// GET REQUEST to read i-change devices:
	// https://api.smartcitizen.me/v0/devices?with_tags=I-CHANGE&page=1&per_page=100
	//
	//

	String token = listRecords.getResumptionToken();
	int start = 1;
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

	    if (variablesIdentifier == null) {
		variablesIdentifier = new ArrayList<String>();
		JSONArray varArray = getData(null, null);
		if (varArray != null && !varArray.isEmpty()) {
		    for (int j = 0; j < varArray.length(); j++) {
			JSONObject varObj = varArray.optJSONObject(j);
			String varId = varObj.optString("uuid");
			variablesIdentifier.add(varId);
		    }
		}

	    }

	    JSONArray deviceArray = getData(start, pageSize);
	    int recordCount = 0;
	    if (!deviceArray.isEmpty()) {

		recordCount = deviceArray.length();

		for (int i = 0; i < recordCount; i++) {
		    try {
		    JSONObject datasetMetadata = deviceArray.getJSONObject(i);
		    String id = datasetMetadata.optString("id");
		    if (id != null && !id.isEmpty()) {

			JSONObject dataObj = datasetMetadata.optJSONObject("data");

			if (dataObj != null) {
			    JSONArray sensorArray = dataObj.optJSONArray("sensors");
			    if (sensorArray != null) {
				for (int k = 0; k < sensorArray.length(); k++) {

				    JSONObject sensorInfo = sensorArray.getJSONObject(k);
				    JSONObject mesurementObj = sensorInfo.optJSONObject("measurement");
				    if(mesurementObj != null) {
				    String key = mesurementObj.optString("uuid");

				    if (key != null && variablesIdentifier.contains(key)
					    && !key.equals("49a89988-2385-4a92-8bde-dc48de240aab")) {
					ret.addRecord(SmartCitizenKitMapper.create(datasetMetadata, sensorInfo));
					partialNumbers++;
				    }
				    }
				}
			    }
			}

		    }
		    }catch (Exception e) {
			e.printStackTrace();
		    }

		}
		ret.setResumptionToken(String.valueOf(start + 1));
		logger.debug("ADDED {} records. Number of analyzed records: {}", partialNumbers, recordCount);
	    } else {
		ret.setResumptionToken(null);

		logger.debug("Added Collection records: {}", partialNumbers);
		partialNumbers = 0;
		return ret;
	    }

	} catch (Exception e) {
	    throw GSException.createException(//
		    this.getClass(), e.getMessage(), null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SMART_CITIZEN_KIT_PARSING_STATION_ERROR);

	}

	return ret;

    }

    private JSONArray getData(Integer startPage, Integer pageSize) throws Exception {
	JSONArray arr = new JSONArray();
	String url;
	if (startPage != null && pageSize != null) {
	    url = getSourceURL() + DEVICE_PATH + "?with_tags=I-CHANGE&page=" + startPage + "&per_page=" + pageSize;
	} else {
	    url = getSourceURL() + MEASUREMENTS_PATH + "?page=1&per_page=100";
	}

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);
	Optional<InputStream> getResult = getDownloader().downloadOptionalStream(url);
	InputStream stream = null;
	ClonableInputStream cis = null;
	if (getResult.isPresent()) {
	    stream = getResult.get();
	    cis = new ClonableInputStream(stream);
	    //GSLoggerFactory.getLogger(getClass()).info("Stream result " + IOStreamUtils.asUTF8String(cis.clone()));
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

    /**
     * @param stationsURL
     * @throws GSException
     * @throws IOException
     * @throws ClientProtocolException
     */
    private Map<String, SmartCitizenKitDevice> parseStations(File stations) throws GSException {

	// TODO: to be implemented
	// Map<String, PolytopeStation> stationMap = new HashMap<>();
	//
	// try (BufferedReader bfReader = new BufferedReader(new FileReader(stations))) {
	//
	// String temp = bfReader.readLine(); // skip header line
	//
	// logger.trace("Reading header line ENDED");
	//
	// logger.trace("Executing while STARTED");
	//
	// while ((temp = bfReader.readLine()) != null) {
	//
	// if (!temp.equals("")) {
	//
	// String[] split = temp.split(",", -1);
	// // 0 STATION CODE
	// String stationCode = split[0].replace("\"", "");
	// // 1 WBAN
	// // 2 STATION NAME - name of the station that collected thunder count
	// String stationName = split[2].replace("\"", "");
	// // 3 - CTRY - is the country of the station
	// String stationCountry = split[3].replace("\"", "");
	// // 4 - STATE -
	// String stationState = split[4].replace("\"", "");
	// // 5 - ICAO -
	// String stationIcao = split[5].replace("\"", "");
	// // 6 - LAT -
	// String stationLat = split[6].replace("\"", "");
	// // 7 - LON -
	// String stationLon = split[7].replace("\"", "");
	// // 8 - ELEV (M) -
	// String stationElevation = split[8].replace("\"", "");
	// // 9 - BEGIN -
	// String stationBegin = split[9].replace("\"", "");
	// // 10 - END -
	// String stationEnd = split[10].replace("\"", "");
	// String startTime = split[11].replace("\"", "");
	// String endTime = split[12].replace("\"", "");
	//
	// PolytopeStation station = new PolytopeStation(stationCode, stationName, stationLat, stationLon,
	// stationElevation,
	// stationBegin, stationEnd, startTime, endTime);
	// if (stationCountry != null && !stationCountry.equals("")) {
	// station.setCountry(stationCountry);
	// } else {
	// station.setCountry("");
	// }
	// if (stationIcao != null && !stationIcao.equals("")) {
	// station.setIcao(stationIcao);
	// } else {
	// station.setIcao("");
	// }
	// if (stationState != null && !stationState.equals("")) {
	// station.setState(stationState);
	// } else {
	// station.setState("");
	// }
	//
	// // add station to the map
	// if (!stationMap.containsKey(stationCode)) {
	// stationMap.put(stationCode, station);
	// }
	// }
	// }
	//
	// return stationMap;
	//
	// } catch (Exception e) {
	//
	// throw GSException.createException(//
	// this.getClass(), //
	// POLYTOPE_STATION_ERROR, //
	// null, //
	// ErrorInfo.ERRORTYPE_SERVICE, //
	// ErrorInfo.SEVERITY_ERROR, //
	// POLYTOPE_STATIONS_URL_NOT_FOUND_ERROR);
	// }
	return null;
    }

    /**
     * @param station
     * @param name
     * @return
     */
    private String createOriginalMetadata(SmartCitizenKitDevice station, String txtname) {

	logger.trace("Creating metadata results from {} STARTED", station.getName());

	String endpoint = getSourceURL() + "/" + txtname;

	String metadataRecord = createRecord(station, endpoint);

	logger.trace("Creating metadata results from {} ENDED", station.getName());

	return metadataRecord;

    }

    private String createRecord(SmartCitizenKitDevice station, String txtname) {

	StringBuilder sb = new StringBuilder();

	sb.append(station.getStationCode());
	sb.append(",");
	sb.append(station.getName());
	sb.append(",");
	sb.append(station.getMinLat());
	sb.append(",");
	sb.append(station.getMinLon());
	sb.append(",");
	sb.append(station.getMaxLat());
	sb.append(",");
	sb.append(station.getMaxLon());
	sb.append(",");
	sb.append(station.getMinElevation());
	sb.append(",");
	sb.append(station.getMaxElevation());
	sb.append(",");
	sb.append(station.getStartDateTime());
	sb.append(",");
	sb.append(station.getEndDateTime());
	sb.append(",");
	sb.append(station.getIcao());
	sb.append(",");
	sb.append(station.getState());
	sb.append(",");
	sb.append(station.getCountry());
	sb.append(",");
	sb.append(getSourceURL());
	sb.append(",");

	sb.append(txtname);

	return sb.toString();
    }

    @Override
    public List<String> listMetadataFormats() {
	List<String> toret = new ArrayList<>();
	toret.add(CommonNameSpaceContext.POLYTOPE_IONBEAM);
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

	return source.getEndpoint().contains("polytope.ecmwf.int") || source.getEndpoint().contains("ionbeam-dev.ecmwf.int") || source.getEndpoint().contains("api.smartcitizen.me");

    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected SmartCitizenKitConnectorSetting initSetting() {

	return new SmartCitizenKitConnectorSetting();
    }
}
