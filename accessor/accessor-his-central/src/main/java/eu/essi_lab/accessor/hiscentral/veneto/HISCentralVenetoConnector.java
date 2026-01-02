package eu.essi_lab.accessor.hiscentral.veneto;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
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
 * @author Roberto
 */
public class HISCentralVenetoConnector extends HarvestedQueryConnector<HISCentralVenetoConnectorSetting> {

    /**
     * YEAR: from 2010 until now
     * TIPOLOGIA SENSORI:
     * 0 -- > 23 precipitazione
     * 1 -- > 18 temperatura dell’aria
     * 2 -- > 20005 livello idrometrico
     * 3 -- > 10001 portata idrometrica
     * GET METADATA:
     * YEAR: last current year to have the complete list of stations
     * e.g. https://api.arpa.veneto.it/REST/v1/meteo_storici?anno=2022&coordcd=23
     * GET DATA
     * e.g. https://api.arpa.veneto.it/REST/v1/meteo_storici_tabella?anno=2021&codseq=300000301
     */

    /**
     * 
     */
    static final String TYPE = "HISCentralVenetoConnector";

    /**
     * 
     */
    public HISCentralVenetoConnector() {

	this.downloader = new Downloader();
	Calendar c = Calendar.getInstance();
	currentYear = c.get(Calendar.YEAR);

    }

    public static final String BASE_URL = "https://api.arpa.veneto.it/REST/v1/";

    /**
     * 
     */
    public static final String DATA_URL = "meteo_storici_tabella";
    /**
     * 
     */
    public static final String METADATA_URL = "meteo_storici";
    /**
     * 
     */
    private static final String HIS_CENTRAL_VENETO_CONNECTOR_DOWNLOAD_ERROR = "HIS_CENTRAL_VENETO_CONNECTOR_DOWNLOAD_ERROR";

    private int countDataset = 0;

    /**
     * 
     */
    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private int maxRecords;

    private Downloader downloader;

    private int currentYear;

    private int startYear = 2010;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	int page = 0;

	String rt = request.getResumptionToken();

	if (rt != null) {

	    page = Integer.valueOf(rt);
	}

	String sensorType = "";

	switch (page) {
	case 0:
	    sensorType = "23";
	    break;

	case 1:
	    sensorType = "18";
	    break;

	case 2:
	    sensorType = "20005";
	    break;
	case 3:
	    sensorType = "10001";
	    break;

	default:
	    break;
	}

	Map<String, VenetoStation> map = new HashMap<String, VenetoStation>();

	if (!sensorType.isEmpty()) {

	    map = createStationMap(sensorType);

	    for (Map.Entry<String, VenetoStation> entry : map.entrySet()) {
		VenetoStation station = entry.getValue();
		String codSeq = entry.getKey();
		JSONObject datasetMetadata = station.getMetadataStation();
		JSONObject measuresObject = getMeasures(codSeq, station.getEndDate());
		if (measuresObject != null) {
		    JSONArray measuresArray = measuresObject.optJSONArray("data");
		    if (measuresArray != null && measuresArray.length() > 0) {
			JSONObject sensorInfo = measuresArray.getJSONObject(measuresArray.length() - 1);
			String value = sensorInfo.optString("valore");

			if (value.contains(",") && value.contains(":")) {
			    JSONObject jsonValues = new JSONObject(value);
			    Iterator<String> keys = jsonValues.keys();
			    while (keys.hasNext()) {
				String key = (String) keys.next();
				ret.addRecord(HISCentralVenetoMapper.create(datasetMetadata, sensorInfo, key, station.getStartDate(),
					station.getEndDate()));
				countDataset++;
			    }

			} else {
			    ret.addRecord(HISCentralVenetoMapper.create(datasetMetadata, sensorInfo, "Cumulata giornaliera",
				    station.getStartDate(), station.getEndDate()));
			    countDataset++;
			}
		    } else {
			logger.info("Data not found for dataset with codseq {}.", entry.getKey());
		    }
		}
	    }
	}

	// JSONObject stationsObject = getStationsList(sensorType);
	//
	// if (stationsObject != null) {
	//
	// JSONArray array = stationsObject.optJSONArray("data");
	// // JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(stream.clone()));
	// if (array == null) {
	// logger.info("ERROR getting items.");
	// } else {
	// // maxRecords = array.length();
	// // getSetting().getMaxRecords().ifPresent(v -> maxRecords = v);
	// for (int i = 0; i < array.length(); i++) {
	//
	// JSONObject datasetMetadata = array.getJSONObject(i);
	// String sensorCode = datasetMetadata.optString("codseq");
	// if (sensorCode != null && !sensorCode.isEmpty()) {
	// JSONObject measuresObject = getMeasures(sensorCode);
	// if (measuresObject != null) {
	// JSONArray measuresArray = measuresObject.optJSONArray("data");
	// if (measuresArray != null && measuresArray.length() > 0) {
	// // for (int j = 0; j < measuresArray.length(); j++) {
	// JSONObject sensorInfo = measuresArray.getJSONObject(measuresArray.length() - 1);
	// String value = sensorInfo.optString("valore");
	//
	// if (value.contains(",") && value.contains(":")) {
	// JSONObject jsonValues = new JSONObject(value);
	// Iterator<String> keys = jsonValues.keys();
	// while (keys.hasNext()) {
	// String key = (String) keys.next();
	// ret.addRecord(HISCentralVenetoMapper.create(datasetMetadata, sensorInfo, key));
	// countDataset++;
	// }
	//
	// } else {
	// ret.addRecord(HISCentralVenetoMapper.create(datasetMetadata, sensorInfo, "Cumulata giornaliera"));
	// countDataset++;
	// }
	// // }
	//
	// } else {
	// logger.info("Data not found for dataset with codseq {}.", sensorCode);
	// }
	// }
	//
	// }
	// }
	// }
	//
	// }
	// }

	Optional<Integer> mr = getSetting().getMaxRecords();
	// metadataTemplate = null;
	page = page + 1;
	if (page > 3) {
	    ret.setResumptionToken(null);
	    // GSLoggerFactory.getLogger(getClass()).info("Dataset with time interval: {}", countTimeDataset);
	    logger.info("Total number of dataset: {}", countDataset);
	} else {
	    ret.setResumptionToken(String.valueOf(page));
	}

	return ret;

    }

    private Map<String, VenetoStation> createStationMap(String sensorType) throws GSException {

	Map<String, VenetoStation> map = new HashMap<String, VenetoStation>();

	for (int i = startYear; i <= currentYear; i++) {
	    String base_station_url = BASE_URL + METADATA_URL + "?";
	    base_station_url += "anno=" + i + "&coordcd=" + sensorType;
	    logger.info("Getting " + base_station_url);
	    logger.trace("SIR VENETO LIST STATION FOR YEAR: {}", i);

	    try {
		Optional<String> listResponse = downloader.downloadOptionalString(base_station_url);

		if (listResponse.isPresent()) {
		    JSONObject obj = new JSONObject(listResponse.get());

		    JSONArray array = obj.optJSONArray("data");
		    // JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(stream.clone()));
		    if (array != null && array.length() > 0) {
			// first round - map is empty - put all elements
			if (map.isEmpty()) {
			    for (int j = 0; j < array.length(); j++) {
				JSONObject stationMetadata = array.getJSONObject(j);
				String sensorCode = stationMetadata.optString("codseq");
				VenetoStation vs = new VenetoStation();
				vs.setStartDate(i);
				vs.setEndDate(i);
				vs.setMetadataStation(stationMetadata);
				map.put(sensorCode, vs);
			    }
			} else {
			    // from here - check map duplicates and updates
			    for (int j = 0; j < array.length(); j++) {
				JSONObject stationMetadata = array.getJSONObject(j);
				String sensorCode = stationMetadata.optString("codseq");
				if (map.containsKey(sensorCode)) {
				    VenetoStation updateStation = map.get(sensorCode);
				    updateStation.setEndDate(i);
				    map.put(sensorCode, updateStation);
				} else {
				    VenetoStation vs = new VenetoStation();
				    vs.setStartDate(i);
				    vs.setEndDate(i);
				    vs.setMetadataStation(stationMetadata);
				    map.put(sensorCode, vs);
				}

			    }
			}

		    }

		} else {
		    logger.info("ERROR getting items from: {}", base_station_url);
		}

	    } catch (Exception e) {
		logger.error("Unable to retrieve " + base_station_url);
		throw GSException.createException(//
			getClass(), //
			"Unable to retrieve " + base_station_url + " after several tries", //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			HIS_CENTRAL_VENETO_CONNECTOR_DOWNLOAD_ERROR);
	    }

	}

	return map;

    }

    private JSONObject getMeasures(String id, int year) throws GSException {

	String base_url = BASE_URL + DATA_URL + "?";// + STATIONS_URL;

	base_url += "anno=" + year + "&codseq=" + id;
	logger.info("Getting " + base_url);
	logger.trace("SIR VENETO DATA URL: {}", base_url);

	try {
	    Optional<String> listResponse = downloader.downloadOptionalString(base_url);

	    if (listResponse.isPresent()) {
		JSONObject obj = new JSONObject(listResponse.get());
		return obj;
	    }

	} catch (Exception e) {
	    logger.error("Unable to retrieve " + base_url);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + base_url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_VENETO_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;

    }

    private JSONObject getStationsList(String sensorId) throws GSException {

	String base_url = BASE_URL + METADATA_URL + "?";// + STATIONS_URL;

	base_url += "anno=" + currentYear + "&coordcd=" + sensorId;
	logger.info("Getting " + base_url);
	logger.trace("SIR VENETO LIST COLLECTION IDENTIFIER URL: {}", base_url);

	try {
	    Optional<String> listResponse = downloader.downloadOptionalString(base_url);

	    if (listResponse.isPresent()) {
		JSONObject obj = new JSONObject(listResponse.get());
		return obj;
	    }

	} catch (Exception e) {
	    logger.error("Unable to retrieve " + base_url);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + base_url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_VENETO_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_VENETO_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("api.arpa.veneto.it");
    }

    @Override
    protected HISCentralVenetoConnectorSetting initSetting() {

	return new HISCentralVenetoConnectorSetting();
    }
}
