package eu.essi_lab.accessor.hiscentral.emilia;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class HISCentralEmiliaConnector extends HarvestedQueryConnector<HISCentralEmiliaConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "HISCentralEmiliaConnector";

    private static final String HISCENTRAL_EMILIA_READ_ERROR = "Unable to find stations URL";

    private static final String HISCENTRAL_EMILIA_URL_NOT_FOUND_ERROR = "HISCENTRAL_EMILIA_URL_NOT_FOUND_ERROR";

    public static final String BASE_URL = "https://dati-simc.arpae.it/opendata/osservati/meteo/realtime/realtime.jsonl";

    private List<HISCentralEmiliaStation> stations;

    private int partialNumbers;

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public HISCentralEmiliaConnector() {

	this.downloader = new Downloader();

	this.stations = new ArrayList<>();
    }

    public enum EMILIA_VARIABLE {
	B13011("PRECIPITATION", "kg/m²"), B12101("TEMPERATURE", "K"), B13215("River level", "m"), B13003("RELATIVE HUMIDITY", "%"), B11002(
		"WIND SPEED", "m/s"), B11001("WIND DIRECTION", "°"), B11041("WIND GUST SPEED", "m/s"), B11043("WIND GUST DIRECTION",
			"°"), B10004("PRESSURE", "Pa"), B13013("SNOW DEPTH", "m"), B14198("Global radiation flux", "W/m²"), B22037(
				"Tidal elevation with respect to national land datum",
				"m"), B22043("SEA TEMPERATURE", "K"), B13083("DISSOLVED OXYGEN", "Kg/m³"), B13080("WATER PH",
					"pH"), B22062("SALINITY", "‰"), B13231("Ossigeno disciolto sat",
						"%"), B22004("DIRECTION OF CURRENT", "°"), B22032("SPEED OF SEA SURFACE CURRENT",
							"m/s"), B13082("WATER TEMPERATURE", "K"), B13081("WATER CONDUCTIVITY", "s/m"),
	// B13084("",""), not found
	B22001("DIRECTION OF WAVES", "°"), B22070("SIGNIFICANT WAVE HEIGHT", "m"),
	// B22196("",""), not found
	B22074("AVERAGE WAVE PERIOD", "s"), B22073("MAXIMUM WAVE HEIGHT", "m"), B22071("SPECTRAL PEAK WAVE PERIOD", "s");
	// B13085("",""); not found

	private String label;
	private String units;

	public String getLabel() {
	    return label;
	}

	public String getUnits() {
	    return units;
	}

	private EMILIA_VARIABLE(String label, String units) {
	    this.label = label;
	    this.units = units;
	}

	public static EMILIA_VARIABLE decode(String parameterCode) {
	    for (EMILIA_VARIABLE var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;

	}

    }

    public enum EMILIA_LEVEL {
	RESERVED("Reserved", 0), GROUND_OR_WATER_SURFACE("Ground or Water Surface", 1), HEIGHT_LEVEL_ABOVE_GROUND(
		"Specified Height Level Above Ground", 103), MEAN_SEA_LEVEL("Mean Sea Level", 101), DEPTH_BELOW_MEAN_SEA_LEVEL(
			"Depth Below Mean Sea Level", 160), DEPTH_BELOW_WATER_SURFACE("Depth Below Water Surface", 161);

	private String label;
	private int code;

	public String getLabel() {
	    return label;
	}

	public int getCode() {
	    return code;
	}

	private EMILIA_LEVEL(String label, int code) {
	    this.label = label;
	    this.code = code;
	}

	public static EMILIA_LEVEL decode(String parameterCode) {
	    for (EMILIA_LEVEL var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;

	}

    }

    public enum EMILIA_TIMERANGE {
	AVERAGE("Average", 0), ACCUMULATION("Accumulation", 1), MAXIMUM("Maximum", 2), MINIMUM("Minimum", 3), VECTORIAL("Vectorial",
		200), VALID("Valid", 205), ISTANTANEOUS("Istantaneous", 254);

	private String label;
	private int code;

	public String getLabel() {
	    return label;
	}

	public int getCode() {
	    return code;
	}

	private EMILIA_TIMERANGE(String label, int code) {
	    this.label = label;
	    this.code = code;
	}

	public static EMILIA_TIMERANGE decode(String parameterCode) {
	    for (EMILIA_TIMERANGE var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;

	}

    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("arpae.it");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

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
	if (stations.isEmpty()) {
	    populateOM();
	}

	if (start < stations.size() && !maxNumberReached) {

	    HISCentralEmiliaStation station = stations.get(start);
	    GSLoggerFactory.getLogger(getClass()).info("Adding station " + station.getName());

	    List<HISCentralEmiliaVariable> vars = station.getVariables();

	    EMILIA_TIMERANGE[] tr_values = EMILIA_TIMERANGE.values();
	    // EMILIA_LEVEL[] lev_values = EMILIA_LEVEL.values();

	    for (HISCentralEmiliaVariable s : vars) {

		Integer iCode = s.getInterpolationCode();
		// Integer level = s.getLevel();
		String interpolation = null;
		for (EMILIA_TIMERANGE et : tr_values) {
		    if (et.getCode() == iCode) {
			interpolation = et.name();
			break;
		    }
		}

		// String lv = null;
		//
		// for (EMILIA_LEVEL lev : lev_values) {
		// if (lev.getCode() == level) {
		// lv = lev.getLabel();
		// break;
		// }
		// }

		ret.addRecord(HISCentralEmiliaMapper.create(station.getJSON(), s.getVar().name(), interpolation,
			// lv,
			ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(station.getStartDate()),
			ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(station.getEndDate())));
		partialNumbers++;
	    }

	    ret.setResumptionToken(String.valueOf(start + 1));
	    logger.debug("ADDED {} records for station {}", partialNumbers, stations.get(start).getName());

	} else {
	    ret.setResumptionToken(null);

	    logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, stations.size());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    private void populateOM() {
	String url = getSourceURL();
	Optional<String> res = downloader.downloadOptionalString(url);
	// EMILIA_TIMERANGE[] tr_values = EMILIA_TIMERANGE.values();
	// EMILIA_LEVEL[] lev_values = EMILIA_LEVEL.values();
	if (res.isPresent()) {
	    String lines[] = res.get().split("\\r?\\n");
	    Map<String, HISCentralEmiliaStation> map = new HashMap<>();

	    for (int i = 0; i < lines.length; i++) {
		JSONObject json = new JSONObject(lines[i]);
		JSONArray jArray = json.optJSONArray("data");

		if (jArray != null) {
		    String date = json.optString("date");
		    Date d = null;
		    if (date != null) {
			Optional<Date> parsedDate = ISO8601DateTimeUtils.parseISO8601ToDate(date);
			if (parsedDate.isPresent()) {
			    d = parsedDate.get();
			}
		    }
		    String stationName = null;
		    Set<String> set = new HashSet<>();
		    // Integer level = null;
		    Integer timeRange = null;
		    for (int k = 0; k < jArray.length(); k++) {
			JSONObject js = (JSONObject) jArray.get(k);
			if (k == 0) {
			    // station element
			    JSONObject jsonStation = js.optJSONObject("vars");
			    HISCentralEmiliaStation station = new HISCentralEmiliaStation(jsonStation);
			    stationName = station.getName();
			    if (!map.containsKey(stationName)) {
				station.setStartDate(d);
				station.setEndDate(d);
				map.put(stationName, station);
			    }

			} else {
			    // variables elements
			    JSONObject propertiesObject = js.optJSONObject("vars");
			    if (propertiesObject != null) {
				// JSONArray names = propertiesObject.names();

				Iterator<String> it = propertiesObject.keys();
				while (it.hasNext()) {
				    String variable = it.next();
				    set.add(variable);
				}
			    }

			    JSONArray timeObj = js.optJSONArray("timerange");
			    timeRange = (Integer) timeObj.get(0);

			    // JSONArray levelObj = js.optJSONArray("level");
			    // level = (Integer) levelObj.get(0);

			}

			HISCentralEmiliaStation st = map.get(stationName);
			List<HISCentralEmiliaVariable> toAdd = new ArrayList<HISCentralEmiliaVariable>();
			if (st != null) {
			    // add variable
			    List<HISCentralEmiliaVariable> varList = st.getVariables();

			    for (String s : set) {
				EMILIA_VARIABLE var = EMILIA_VARIABLE.decode(s);
				if (var != null) {
				    HISCentralEmiliaVariable v = new HISCentralEmiliaVariable(var, timeRange);
				    if (varList.isEmpty()) {
					toAdd.add(v);
				    } else {
					// check if already exist
					boolean exist = false;
					for (HISCentralEmiliaVariable em : varList) {
					    if (em.getVar().equals(var) && em.getInterpolationCode().equals(timeRange)) {
						// && em.getLevel().equals(level)) {
						exist = true;
						break;
					    }

					}
					if (!exist)
					    toAdd.add(v);
				    }
				}

			    }
			    for (HISCentralEmiliaVariable hem : toAdd) {
				st.addVariable(hem);
			    }

			    // check date
			    Date starDate = st.getStartDate();
			    Date endDate = st.getEndDate();
			    if (starDate != null && endDate != null) {
				if (starDate.after(d)) {
				    st.setStartDate(d);
				}
				if (endDate.before(d)) {
				    st.setEndDate(d);
				}
			    }
			}
			map.put(stationName, st);
		    }
		}
	    }
	    stations.addAll(map.values());
	}

    }

    private List<String> getOriginalMetadata(int startPosition, int pageSize) throws GSException {
	logger.trace("SIR EMILIA List Data finding STARTED");

	List<String> ret = new ArrayList<>();

	String sirEmiliaUrl = getSourceURL();

	logger.trace("SIR EMILIA LIST COLLECTION IDENTIFIER URL: {}", sirEmiliaUrl);

	boolean errorResponse = true;
	int tries = 0;
	do {

	    String getRequests = sirEmiliaUrl + "&offset=" + startPosition + "&limit=" + pageSize;

	    Optional<String> listResponse = downloader.downloadOptionalString(getRequests);

	    if (listResponse.isPresent()) {

		if (listResponse.get().startsWith("<html>")) {
		    // error
		    try {
			GSLoggerFactory.getLogger(getClass()).info("Sleeping 1 minute before retrying");
			Thread.sleep(TimeUnit.MINUTES.toMillis(1));
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }

		    continue;

		}
		errorResponse = false;
		// JSONObject feature = features.optJSONObject("features");
		JSONObject object = new JSONObject(listResponse.get());

		JSONObject results = object.optJSONObject("result");
		JSONArray arrayResults = results.optJSONArray("records");

		for (Object arr : arrayResults) {
		    ret.add(arr.toString());
		}

	    } else {

		throw GSException.createException(//
			this.getClass(), //
			HISCENTRAL_EMILIA_READ_ERROR, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			HISCENTRAL_EMILIA_URL_NOT_FOUND_ERROR);

	    }

	} while (errorResponse && tries < 3);

	logger.trace("SIR EMILIA List Data finding ENDED");

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_EMILIA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected HISCentralEmiliaConnectorSetting initSetting() {

	return new HISCentralEmiliaConnectorSetting();
    }

    /**
     * @return
     * @throws IOException
     */
    private Integer getRecordsCount() {
	String baseUrl = getSourceURL();

	try {
	    String requesturl = baseUrl + "&offset=0&limit=1";
	    boolean notFound = true;
	    do {

		Optional<String> result = downloader.downloadOptionalString(requesturl);

		if (result.isPresent()) {

		    if (result.get().startsWith("<html>")) {
			// error

			Thread.sleep(TimeUnit.MINUTES.toMillis(1));
			continue;

		    }
		    notFound = false;
		    JSONObject object = new JSONObject(result.get());

		    JSONObject results = object.optJSONObject("result");

		    int total = results.optInt("total");
		    if (total == 0) {
			return null;
		    }
		    return total;
		}
	    } while (notFound);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(HISCentralEmiliaConnector.class).error("Get Number of Records Error");
	    GSLoggerFactory.getLogger(HISCentralEmiliaConnector.class).error(e.getMessage(), e);
	    return null;
	}
	return null;
    }

    public static void main(String[] args) throws Exception {
	String urlPlatform = "https://dati.arpae.it/api/action/datastore_search?resource_id=1396fb33-d6a1-442b-a1a1-90ff7a3d3642";
	Downloader d = new Downloader();

	Map<String, Integer> countVariable = new HashMap<String, Integer>();
	Set<Entry<String, Integer>> sets = new HashSet<Entry<String, Integer>>();
	int offset = 0;
	int limit = 50;

	for (int i = 0; i < 100; i++) {
	    String getRequests = urlPlatform + "&offset=" + offset + "&limit=" + limit;
	    Optional<String> res = d.downloadOptionalString(getRequests);
	    System.out.println(res.get());
	    if (res.isPresent()) {
		if (res.get().contains("<html>")) {
		    continue;
		}

		JSONObject object = new JSONObject(res.get());

		JSONObject results = object.optJSONObject("result");
		JSONArray arrayResults = results.optJSONArray("records");

		for (Object arr : arrayResults) {
		    JSONObject data = (JSONObject) arr;

		    Map<String, Object> map = data.toMap();

		    Set<Entry<String, Object>> treemap = map.entrySet();

		    String time = data.optString("date");

		    JSONArray dataArray = data.optJSONArray("data");

		    for (Object o : dataArray) {
			JSONObject variable = (JSONObject) o;
			if (variable.has("timerange")) {
			    String timeRange = variable.optString("timerange");
			    // JSONObject timeRange = variable.optJSONObject("timerange");
			    JSONObject parametersObj = variable.optJSONObject("vars");
			    Map<String, Object> parameterMap = parametersObj.toMap();
			    for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
				String key = entry.getKey();

				if (countVariable.containsKey(key)) {
				    Integer value = countVariable.get(key);
				    countVariable.put(key, value + 1);
				} else {
				    countVariable.put(key, 0);
				}
			    }
			    Set<Entry<String, Object>> parameterTree = parameterMap.entrySet();
			    Set<String> keySets = parameterMap.keySet();
			    // JSONObject level = variable.optJSONObject("level");
			    String level = variable.optString("level");
			} else {
			    // fixed fields: LAT=B05001,LON=B06001, STATION_NAME=B01019,HEIGHT OF STATION=B07030,HEIGHT
			    // OF
			    // BAROMETER ABOVE MEAN SEA LEVEL=B07031
			    // JSONObject vars = variable.optJSONObject("vars");
			    // if (vars != null) {
			    // JSONObject lonObj = vars.optJSONObject("B06001");
			    // JSONObject latObj = vars.optJSONObject("B05001");
			    // JSONObject stationNameObj = vars.optJSONObject("B01019");
			    // JSONObject vExtent1Obj = vars.optJSONObject("B07030");
			    // JSONObject vExtent2Obj = vars.optJSONObject("B07031");
			    // double longitude = lonObj.optDouble("v");
			    // double latitude = latObj.optDouble("v");
			    // double vEtx1 = vExtent1Obj.optDouble("v");
			    // double vEtx2 = vExtent2Obj.optDouble("v");
			    // }
			}

		    }

		}
	    }

	    offset = offset + limit;

	}

	System.out.println("VARIABLE CONSIDERED: " + countVariable.size());
    }
}
