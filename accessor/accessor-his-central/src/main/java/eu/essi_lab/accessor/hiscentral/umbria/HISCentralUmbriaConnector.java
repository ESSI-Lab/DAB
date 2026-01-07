package eu.essi_lab.accessor.hiscentral.umbria;

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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;

public class HISCentralUmbriaConnector extends HarvestedQueryConnector<HISCentralUmbriaConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "HISCentralUmbriaConnector";

    private static final String HISCENTRAL_UMBRIA_READ_ERROR = "Unable to find stations URL";

    private static final String HISCENTRAL_UMBRIA_URL_NOT_FOUND_ERROR = "HISCENTRAL_UMBRIA_URL_NOT_FOUND_ERROR";

    private static final String STATION_URL = "4080d1ab-4d18-4546-abbc-ca90d5ff5d6b";

    private static final String SENSOR_URL = "332277cd-aefa-407b-a358-3e9e238c787d";

    private static final String HYSTORICAL_RAIN_ID = "65b788a9-0f3b-4e62-aa58-5441f9cd38e4";
    private static final String HYSTORICAL_TEMPERATURE_ID = "bb38b77a-b5a0-46c1-ac99-997642c42639";
    private static final String HYSTORICAL_LEVEL_ID = "b709e02b-4bfb-4b4e-b186-5b01e54bba1b";
    private static final String HYSTORICAL_FLOW_RATE_ID = "81ac32fd-9a75-4c53-a37d-4b9d134af4e5";

    private static final String CURRENT_RAIN_ID = "dc8a39db-1d1c-4b14-8e7d-ede0adc1022c";
    private static final String CURRENT_TEMPERATURE_ID = "b5784219-a065-4178-8910-dad0e68bc21b";
    private static final String CURRENT_LEVEL_ID = "53027c92-9ce3-4520-91d1-87303ccb4dd1";
    private static final String CURRENT_FLOW_RATE_ID = "50dbe5f8-5339-435f-9161-fe250669a97d";

    public static final List<String> timeList = Arrays.asList("0-24", "9-9", "NA");

    public static final String BASE_URL = "https://dati.regione.umbria.it/api/3/action/datastore_search";
    public static final String BASE_SQL_URL = "https://dati.regione.umbria.it/api/3/action/datastore_search_sql";

    private static final String METADATA_FULL = "floats/";

    private Map<String, JSONObject> stationsMap;

    /**
     * BASE ENDPOINT: https://dati.regione.umbria.it/api/3/action/datastore_search?
     */

    private int partialNumbers;

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public enum SORT_ORDER {
	ASC, DESC
    };

    public enum UMBRIA_Variable {

	TMEDIA("Temperatura", "Temperatura media giornaliera", "°C", InterpolationType.AVERAGE), //
	TMAX("Temperatura", "Temperatura massima giornaliera", "°C", InterpolationType.MAX), //
	TMIN("Temperatura", "Temperatura minima giornaliera", "°C", InterpolationType.MIN), //
	PTOT("Precipitazione", "Precipitazione cumulata giornaliera", "mm", InterpolationType.TOTAL), //
	PORTATAMEDIA("Portata", "Portata media giornaliera", "m³/s", InterpolationType.AVERAGE), //
	PORTATAMAX("Portata", "Portata massima giornaliera", "m³/s", InterpolationType.MAX), PORTATAMIN("Portata",
		"Portata minima giornaliera", "m³/s",
		InterpolationType.MIN), LIVELLOMEDIO("Livello", "Livello medio giornaliero", "m", InterpolationType.AVERAGE), //
	LIVELLOMAX("Livello", "Livello massimo giornaliero", "m", InterpolationType.MAX), //

	LIVELLOMIN("Livello", "Livello minimo giornaliero", "m", InterpolationType.MIN), //

	UMIN("Umidità", "umin", "%", InterpolationType.MIN), //
	UMMEDIA("Umidità", "umedia", "%", InterpolationType.AVERAGE), //
	UMAX("Umidità", "umax", "%", InterpolationType.MAX), //
	VMEDIA("Velocità del vento", "vmedia", "m/s", InterpolationType.AVERAGE), //
	VRAFFICA("Velocità del vento", "vraffica", "m/s", InterpolationType.MAX), //

	RADD("Radiazione", "rtot", "MJ/m²", InterpolationType.TOTAL), //

	HS("Altezza neve dal suolo", "hs", "cm", InterpolationType.MAX), //
	HN("Altezza neve fresca", "hn", "cm", InterpolationType.TOTAL); //

	// pluviometria giornaliera?
	// radiazioni??

	private String label;
	private String paramDescription;
	private String units;
	private InterpolationType interpolation;
	// private PiemonteStationType stationType;

	// public PiemonteStationType getStationType() {
	// return stationType;
	// }

	public InterpolationType getInterpolation() {
	    return interpolation;
	}

	public String getLabel() {
	    return label;
	}

	public String getParamDescription() {
	    return paramDescription;
	}

	public String getUnits() {
	    return units;
	}

	UMBRIA_Variable(String label, String paramDescription, String units, InterpolationType interpolation) {
	    this.label = label;
	    this.paramDescription = paramDescription;
	    this.units = units;
	    this.interpolation = interpolation;
	}

	public static UMBRIA_Variable decode(String parameterCode) {
	    for (UMBRIA_Variable var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;
	}

	// public static List<UMBRIA_Variable> values(PiemonteStationType type) {
	// UMBRIA_Variable[] values = values();
	// List<UMBRIA_Variable> ret = new ArrayList<>();
	// for (UMBRIA_Variable piemonte_Variable : values) {
	// if (piemonte_Variable.getStationType().equals(type)) {
	// ret.add(piemonte_Variable);
	// }
	// }
	// return ret;
	// }

    }

    public HISCentralUmbriaConnector() {

	this.downloader = new Downloader();

	this.stationsMap = new HashMap<>();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("dati.regione.umbria.it");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	int pageSize = getSetting().getPageSize();
	 
	// get all the stations
	if (stationsMap.isEmpty()) {
	    stationsMap = getStationsMap(pageSize);
	}

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {

	    start = Integer.valueOf(token);
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	List<JSONObject> sensorList = getSensorList(start, pageSize);

	if (sensorList.size() > 0 && !maxNumberReached) {

	    int count = 0;

	    for (int i = 0; i < sensorList.size(); i++) {
		JSONObject sensorObj = sensorList.get(i);

		if (sensorObj != null) {

		    String stationId = sensorObj.optString("ID_STAZIONE");
		    if (stationId != null && !stationId.isEmpty()) {
			if (stationsMap.containsKey(stationId)) {
			    JSONObject stationDescription = stationsMap.get(stationId);

			    Map<UMBRIA_Variable, List<String>> resMap = postData(sensorObj, SORT_ORDER.ASC, null);

			    for (Map.Entry<UMBRIA_Variable, List<String>> entry : resMap.entrySet()) {

				String variableName = entry.getKey().name();
				String startDate = entry.getValue().get(1);
				String time = entry.getValue().get(0);
				String resourceId = entry.getValue().get(2);
				ret.addRecord(HISCentralUmbriaMapper.create(stationDescription, sensorObj, variableName, resourceId, time,
					startDate));
				partialNumbers++;
				count++;
			    }
			}
		    }

		}
	    }
	    ret.setResumptionToken(String.valueOf(start + pageSize));
	    logger.debug("ADDED {} records. Number of analyzed floats: {}", partialNumbers, String.valueOf(start + pageSize));

	} else {
	    ret.setResumptionToken(null);

	    logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, stationsMap.size());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    public static Map<UMBRIA_Variable, List<String>> postData(JSONObject sensorObj, SORT_ORDER sort_order, String resourceIdentifier) {

	Map<UMBRIA_Variable, List<String>> ret = new HashMap<UMBRIA_Variable, List<String>>();

	try {
	    String sensorType = sensorObj.optString("TIPO_STRUMENTO");
	    String sensorId = sensorObj.optString("ID_SENSORE");

	    List<String> resourceId = new ArrayList<String>();
	    List<UMBRIA_Variable> varList = new ArrayList<UMBRIA_Variable>();

	    if (sensorType != null && !sensorType.isEmpty()) {
		if (sensorType.toLowerCase().contains("termometro")) {
		    resourceId.add(HYSTORICAL_TEMPERATURE_ID);
		    resourceId.add(CURRENT_TEMPERATURE_ID);
		    varList.add(UMBRIA_Variable.TMEDIA);
		    varList.add(UMBRIA_Variable.TMIN);
		    varList.add(UMBRIA_Variable.TMAX);
		} else if (sensorType.toLowerCase().contains("pluviometro")) {
		    resourceId.add(HYSTORICAL_RAIN_ID);
		    resourceId.add(CURRENT_RAIN_ID);
		    varList.add(UMBRIA_Variable.PTOT);
		} else if (sensorType.toLowerCase().contains("idrometro")) {
		    resourceId.add(HYSTORICAL_LEVEL_ID);
		    resourceId.add(CURRENT_LEVEL_ID);
		    varList.add(UMBRIA_Variable.LIVELLOMEDIO);
		    varList.add(UMBRIA_Variable.LIVELLOMIN);
		    varList.add(UMBRIA_Variable.LIVELLOMAX);
		} else if (sensorType.toLowerCase().contains("portata")) {
		    resourceId.add(HYSTORICAL_FLOW_RATE_ID);
		    resourceId.add(CURRENT_FLOW_RATE_ID);
		    varList.add(UMBRIA_Variable.PORTATAMEDIA);
		    varList.add(UMBRIA_Variable.PORTATAMIN);
		    varList.add(UMBRIA_Variable.PORTATAMAX);
		}
		
		if(resourceIdentifier != null) {
		    resourceId.clear();
		    resourceId.add(resourceIdentifier);
		}

		int limit = 10;
		String sort = null;
		if (SORT_ORDER.ASC.equals(sort_order)) {
		    sort = "ANNO asc, MESE asc, GIORNO asc";
		} else if (SORT_ORDER.DESC.equals(sort_order)) {
		    sort = "ANNO desc, MESE desc, GIORNO desc";
		}

		for (String resource : resourceId) {
		    boolean breakLoop = false;
		    for (String type : timeList) {

			String postRequest = "{\"resource_id\": \"" + resource + "\",\"filters\":{\"ID_SENSORE_DETTAGLIO\":\"" + sensorId
				+ "\", \"TIPOLOGIA_RILEVAZIONE\":\"" + type + "\"},\"limit\": \"" + limit + "\", \"sort\": \"" + sort
				+ "\"}";

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("accept", "text/plain");
			map.put("Content-Type", "application/json");

			GSLoggerFactory.getLogger(HISCentralUmbriaConnector.class).debug("POST REQUEST: " + postRequest);

			HttpRequest request = HttpRequestUtils.build(MethodWithBody.POST, BASE_URL, postRequest,
				HttpHeaderUtils.build(map));

			HttpResponse<InputStream> response = new Downloader().downloadResponse(request);
			int statusCode = response.statusCode();
			if (statusCode > 400) {
			    breakLoop = true;
			    postRequest = "{\"resource_id\": \"" + resource + "\",\"filters\":{\"ID_SENSORE_DETTAGLIO\":\"" + sensorId
				    + "\"},\"limit\": \"" + limit + "\", \"sort\": \"" + sort + "\"}";
			    GSLoggerFactory.getLogger(HISCentralUmbriaConnector.class).debug("POST REQUEST: " + postRequest);
			    HttpRequest newRequest = HttpRequestUtils.build(MethodWithBody.POST, BASE_URL, postRequest,
				    HttpHeaderUtils.build(map));
			    response = new Downloader().downloadResponse(newRequest);
			}

			InputStream input = response.body();

			ByteArrayOutputStream output = new ByteArrayOutputStream();

			IOUtils.copy(input, output);

			String result = new String(output.toByteArray());

			JSONObject resultObj = new JSONObject(result);

			if (resultObj != null) {
			    JSONObject res = resultObj.optJSONObject("result");

			    if (res != null) {
				JSONArray arrayResults = res.optJSONArray("records");

				if (arrayResults != null && arrayResults.length() > 0) {
				    JSONObject firstData = arrayResults.getJSONObject(0);
				    int year = firstData.optInt("ANNO");
				    int month = firstData.optInt("MESE");
				    int day = firstData.optInt("GIORNO");
				    String date = ISO8601DateTimeUtils.getISO8601DateTime(year, month, day);
				    // Optional<Date> optDate = ISO8601DateTimeUtils.parseISO8601ToDate(date);
				    if (date != null && !date.isEmpty()) {
					List<String> toAdd = new ArrayList<String>();
					toAdd.add(type);
					toAdd.add(date);
					toAdd.add(resource);
					for (UMBRIA_Variable var : varList) {
					    ret.put(var, toAdd);
					}
				    }
				}
			    }
			}
			if (input != null)
			    input.close();
			if(breakLoop)
			    break;
		    }
		}

	    }
	    return ret;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(HISCentralUmbriaConnector.class).error(e.getMessage());
	    return ret;
	}

    }

    private List<JSONObject> getSensorList(int start, int pageSize) throws GSException {
	logger.trace("SIR UMBRIA List Sensor finding STARTED");

	List<JSONObject> ret = new ArrayList<JSONObject>();

	String sirUmbriaUrl = getQueryStringURL();

	logger.trace("SIR UMBRIA LIST COLLECTION IDENTIFIER URL: {}", sirUmbriaUrl);

	String getStationURL = sirUmbriaUrl + "resource_id=" + SENSOR_URL + "&limit=" + pageSize + "&offset=" + start;

	Optional<String> listResponse = downloader.downloadOptionalString(getStationURL);

	if (listResponse.isPresent()) {

	    JSONObject jsonRes = new JSONObject(listResponse.get());

	    // JSONObject feature = features.optJSONObject("features");

	    if (jsonRes != null) {

		JSONObject result = jsonRes.optJSONObject("result");

		if (result != null) {

		    JSONArray arrayResults = result.optJSONArray("records");

		    for (Object arr : arrayResults) {

			JSONObject data = (JSONObject) arr;

			if (data != null) {

			    ret.add(data);
			}

		    }

		}
	    }

	} else {

	    throw GSException.createException(//
		    this.getClass(), //
		    HISCENTRAL_UMBRIA_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_UMBRIA_URL_NOT_FOUND_ERROR);

	}

	logger.trace("SIR Umbria List Sensor finding ENDED");

	return ret;
    }

    private Map<String, JSONObject> getStationsMap(int pageSize) throws GSException {
	logger.trace("SIR UMBRIA List Stations finding STARTED");

	Map<String, JSONObject> ret = new HashMap<String, JSONObject>();

	String sirUmbriaUrl = getQueryStringURL();

	boolean finished = false;
	int offset = 0;

	while (!finished) {

	    logger.trace("SIR UMBRIA LIST COLLECTION IDENTIFIER URL: {}", sirUmbriaUrl);

	    String getStationURL = sirUmbriaUrl + "resource_id=" + STATION_URL + "&limit=" + pageSize + "&offset=" + offset;

	    Optional<String> listResponse = downloader.downloadOptionalString(getStationURL);

	    if (listResponse.isPresent()) {

		JSONObject jsonRes = new JSONObject(listResponse.get());

		// JSONObject feature = features.optJSONObject("features");

		if (jsonRes != null) {

		    JSONObject result = jsonRes.optJSONObject("result");

		    if (result != null) {

			JSONArray arrayResults = result.optJSONArray("records");

			for (Object arr : arrayResults) {

			    JSONObject data = (JSONObject) arr;

			    if (data != null) {

				String stationId = data.optString("ID_STAZIONE");
				ret.put(stationId, data);
			    }

			}
			offset += pageSize;
			if (arrayResults.length() == 0)
			    finished = true;
		    }
		}

	    } else {
		finished = true;
		throw GSException.createException(//
			this.getClass(), //
			HISCENTRAL_UMBRIA_READ_ERROR, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			HISCENTRAL_UMBRIA_URL_NOT_FOUND_ERROR);

	    }

	    logger.trace("SIR Umbria List Stations finding ENDED");
	}

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_UMBRIA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected HISCentralUmbriaConnectorSetting initSetting() {

	return new HISCentralUmbriaConnectorSetting();
    }

    public static void main(String[] args) throws Exception {
	String urlPlatform = "https://dati.regione.umbria.it/api/3/action/datastore_search?resource_id=4080d1ab-4d18-4546-abbc-ca90d5ff5d6b&limit=50&offset=";
	String urlSensor = "https://dati.regione.umbria.it/api/3/action/datastore_search?resource_id=332277cd-aefa-407b-a358-3e9e238c787d&limit=50&offset=";
	Downloader d = new Downloader();
	boolean finished = false;
	String url = "https://dati.regione.umbria.it/api/3/action/datastore_search";

	String resourceId = "bb38b77a-b5a0-46c1-ac99-997642c42639";
	int limit = 50;
	String ascSort = "ANNO asc, MESE asc, GIORNO asc";
	String descSort = "ANNO desc, MESE desc, GIORNO desc";
	String id_sensore = "37726";
	String login = "{\"resource_id\": \"" + resourceId + "\",\"filters\":{\"ID_SENSORE_DETTAGLIO\":\"" + id_sensore
		+ "\"},\"limit\": \"" + limit + "\", \"sort\": \"" + descSort + "\"}";

	HashMap<String, String> map = new HashMap<String, String>();
	map.put("accept", "text/plain");
	map.put("Content-Type", "application/json");

	HttpRequest request = HttpRequestUtils.build(MethodWithBody.POST, url, login, HttpHeaderUtils.build(map));

	HttpResponse<InputStream> response = new Downloader().downloadResponse(request);

	InputStream input = response.body();

	ByteArrayOutputStream output = new ByteArrayOutputStream();

	IOUtils.copy(input, output);

	String result = new String(output.toByteArray());

	System.out.println("got result: " + result);

	JSONObject obj = new JSONObject(result);

	JSONObject resultObj = obj.optJSONObject("result");

	JSONArray arrayResults = resultObj.optJSONArray("records");
	int total = resultObj.optInt("total");

	for (int i = 0; i < arrayResults.length(); i++) {
	    String rilevationType = obj.optString("TIPOLOGIA_RILEVAZIONE");
	    String sensorType = obj.optString("TIPO_STRUMENTO");
	    String avgInterp = obj.optString("AVGDAY");
	    String minInterp = obj.optString("MINDAY");
	    String maxInterp = obj.optString("MAXDAY");
	    String cumulInterp = obj.optString("CUMDAY");
	    String year = obj.optString("ANNO");
	    String month = obj.optString("MESE");
	    String day = obj.optString("GIORNO");
	}

	/**
	 * list sensors
	 */
	int offset = 0;
	// retrieve stations
	Set<String> sensorSet = new HashSet<String>();
	Set<String> sensorTypeSet = new HashSet<String>();
	while (!finished) {

	    String requestURL = urlSensor + offset;
	    Optional<String> res = d.downloadOptionalString(requestURL);
	    System.out.println(res.get());

	    JSONObject object = new JSONObject(res.get());

	    JSONObject stationResultObj = object.optJSONObject("result");

	    JSONArray stationArrayResults = stationResultObj.optJSONArray("records");

	    for (int i = 0; i < stationArrayResults.length(); i++) {

		JSONObject stationObj = stationArrayResults.getJSONObject(i);

		String stationId = stationObj.optString("ID_STAZIONE");
		String sensorId = stationObj.optString("ID_SENSORE");
		String sensor = stationObj.optString("STRUMENTO");
		String sensorType = stationObj.optString("TIPO_STRUMENTO");
		String unitsOfMeasure = stationObj.optString("UNITA_MISURA");
		int sensorStatus = stationObj.optInt("STATO_SENSORE");
		if (sensorType != null && !sensorType.isEmpty()) {
		    sensorSet.add(sensor);
		    sensorTypeSet.add(sensorType);
		} else {
		    System.out.println("NULL: " + sensorType);
		}

	    }
	    offset += limit;
	    if (stationArrayResults.length() == 0) {
		finished = true;
	    }
	}

	System.out.println("STRUMENTO: " + sensorSet.toString());
	System.out.println("TIPO STRUMENTO: " + sensorTypeSet.toString());

	/**
	 * list stations
	 */
	// int offset = 0;
	// //retrieve stations
	// while (!finished) {
	//
	// String requestURL = urlPlatform + offset;
	// Optional<String> res = d.downloadString(requestURL);
	// System.out.println(res.get());
	//
	// JSONObject object = new JSONObject(res.get());
	//
	// JSONObject stationResultObj = object.optJSONObject("result");
	//
	// JSONArray stationArrayResults = stationResultObj.optJSONArray("records");
	//
	// for (int i = 0; i < stationArrayResults.length(); i++) {
	//
	// JSONObject stationObj = stationArrayResults.getJSONObject(i);
	//
	// String stationId = stationObj.optString("ID_STAZIONE");
	// String stationName = stationObj.optString("NOME_STAZIONE");
	// Double lat = stationObj.optDouble("LAT");
	// Double lon = stationObj.optDouble("LON");
	// String stationLocation = stationObj.optString("LOCALITA");
	// String stationCity = stationObj.optString("COMUNE");
	// String stationProvince = stationObj.optString("PROVINCIA");
	//
	// }
	// offset += limit;
	// if (stationArrayResults.length() == 0) {
	// finished = true;
	// }
	// }

    }
}
