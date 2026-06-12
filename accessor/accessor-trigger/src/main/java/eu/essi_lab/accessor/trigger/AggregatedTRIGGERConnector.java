/**
 *
 */
package eu.essi_lab.accessor.trigger;

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

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Roberto
 */
public class AggregatedTRIGGERConnector extends HarvestedQueryConnector<AggregatedTRIGGERConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "AggregatedTRIGGERConnector";//

    /**
     *
     */
    private int recordsCount = 0;

    private int partialNumbers = 0;

    private static final String AGGREGATED_TRIGGER_CONNECTOR_DOWNLOAD_ERROR = "AGGREGATED_TRIGGER_CONNECTOR_DOWNLOAD_ERROR";

    /**
     * In the API V2 it is required
     */

    private static final String TOKEN_REQUEST_URL = "auth";
    private static final String REFRESH_REQUEST_URL = "https://app.meteotracker.com/auth/refreshtoken";

    public static final String BASE_URL = "https://trigger-io.difa.unibo.it/api/";
    private static final String MYAIR_URL = "call/myair_daily?";
    //private static final String ECG_URL = "ecg/?";
    //private static final String PPG_URL = "ppg/?";
    private static final String GPS_URL = "call/gps_daily?";
    private static final String SLEEP_URL = "call/sleep_tidy?";
    private static final String SMARTWATCHHIGH_URL = "call/smartwatchhigh_daily?";
    private static final String SMARTWATCHLOW_URL = "call/smartwatchlow_daily?";
    private static final String MIN_VALID_PARAM = "min_valid_n=1";

    private static final String DEVICE_ID = "deviceId";
    private static final String EMAIL = "email";
    private static final String USER_ID = "userId";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";

    Map<String, List<JSONObject>> stationList = new HashMap<>();

    Map<String, List<TRIGGERTimePosition>> latLonMap = new HashMap<>();

    public static String TRIGGER_TOKEN = null;

    // private boolean isLastSearchTerm = false;
    //MYAIR_URL, SLEEP_URL to be added and tested
    private String[] SEARCH_TERMS = { SMARTWATCHHIGH_URL, SMARTWATCHLOW_URL };
    private static int searchIndex = 0;

    public enum AGGREGATED_TRIGGER_VARIABLES {
	// MYAIR
	TEMPERATURE("2m temperature", "myair", "°C"), HUMIDITY("2m rel. humidity", "myair", "%"), PRESSURE("Pressure", "myair",
		"mbar"), // HDX("Humidex",
	// "°C"),
	SOUND("Sound", "myair", "km/h"), LIGHT("Solar Radiation Index", "myair", "#"), UVB("Ultraviolet B", "myair", "#"), // CO2("CO2",
	// "ppm"),
	PM1("Mass Concentration PM1.0", "myair", "μg/m³"), PM25("Mass Concentration PM2.5", "myair", "μg/m³"), PM10(
		"Mass Concentration PM10", "myair", "μg/m³"), PC03("Number of particulate concentration PC0.3", "myair", "#/cm³"), PC05(
		"Number of particulate concentration PM0.5", "myair", "#/cm³"), PC1("Number of particulate concentration PC1.0", "myair",
		"#/cm³"), PC25("Number of particulate concentration PC2.5", "myair", "#/cm³"), PC5(
		"Number of particulate concentration PC5.0", "myair", "#/cm³"), PC10("Number of particulate concentration PC10", "myair",
		"#/cm³"),

	// ECG
	ECG("Electrocardiography (ECG)", "ecg", "mV"),

	// PPG
	PPG("Photoplethysmography (PPG)", "ppg", "µV"),

	// SLEEP

	// SMARTWATCHLOW
	BPHIGH("Systolic Blood Pressure", "smartwatchlow", "mmHg"), BPLOW("Diastolic Blood Pressure", "smartwatchlow", "mmHg"), BODYTEMP(
		"Body Temperature", "smartwatchlow", "°C"), SKINTEMP("Skin Temperature", "smartwatchlow", "°C"),

	BPLOWMIN("Minimum Diastolic Blood Pressure", "bplow_min", "mmHg"), BPLOWMAX("Maximum Diastolic Blood Pressure", "bplow_max",
		"mmHg"), BPLOWMEAN("Average Diastolic Blood Pressure", "bplow_mean", "mmHg"), BPHIGHMIN("Minimum Systolic Blood Pressure",
		"bphigh_min", "mmHg"), BPHIGHMAX("Maximum Systolic Blood Pressure", "bphigh_max", "mmHg"), BPHIGHMEAN(
		"Average Systolic Blood Pressure", "bphigh_mean", "mmHg"), BODYTEMPMIN("Minimum Body Temperature", "bodytemp_min",
		"°C"), BODYTEMPMAX("Maximum Body Temperature", "bodytemp_max", "°C"), BODYTEMPMEAN("Average Body Temperature",
		"bodytemp_mean", "°C"),

	SKINTEMPMIN("Minimum Skin Temperature", "skintemp_min", "°C"), SKINTEMPMAX("Maximum Skin Temperature", "skintemp_max",
		"°C"), SKINTEMPMEAN("Average Skin Temperature", "skintemp_mean", "°C"),

	// SMARTWATCHHIGH
	HEARTRATEMIN("Minimum Heart Rate", "heartrate_min", "BPM"), HEARTRATEMAX("Maximum Heart Rate", "heartrate_max",
		"BPM"), HEARTRATEMEAN("Average Heart Rate", "heartrate_mean", "BPM"), SLEEPRATEMIN("Minimum Sleep Rate", "sleeprate_min",
		"#"), SLEEPRATEMAX("Maximum Sleep Rate", "sleeprate_max", "#"), SLEEPRATEMEAN("Average Sleep Rate", "sleeprate_mean", "#"),

	OXYGENSMIN("Minimum Oxygen", "oxygens_min", "#"), OXYGENSMAX("Maximum Oxygen", "oxygens_max", "#"), OXYGENSMEAN("Average Oxygen",
		"oxygens_mean", "%"), BREATHRATEMIN("Minimum Breaths Rate", "breathrate_min", "BPM"), BREATHRATEMAX("Maximum Breaths Rate",
		"breathrate_max", "BPM"), BREATHRATEMEAN("Average Breaths Rate", "breathrate_mean", "BPM"),

	HEARTRATE("Heart Rate", "smartwatchhigh", "BPM"), SLEEPRATE("Sleep Rate", "smartwatchhigh", "#"), OXYGENS("Oxygen",
		"smartwatchhigh", "#"), BREATHRATE("Breaths Rate", "smartwatchhigh", "BPM");

	private String label;
	private String category;
	private String units;

	public String getLabel() {
	    return label;
	}

	public String getCategory() {
	    return category;
	}

	public String getUnits() {
	    return units;
	}

	private AGGREGATED_TRIGGER_VARIABLES(String label, String category, String units) {
	    this.label = label;
	    this.category = category;
	    this.units = units;
	}

	public static AGGREGATED_TRIGGER_VARIABLES decode(String parameterCode) {
	    for (AGGREGATED_TRIGGER_VARIABLES var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;

	}
    }

    // public enum METEOTRACKER_INTERPOLATION {
    // AVERAGE("Average", "avgVal"), MAXIMUM("Maximum", "maxVal"), MINIMUM("Minimum", "minVal");
    //
    // private String label;
    // private String code;
    //
    // public String getLabel() {
    // return label;
    // }
    //
    // public String getCode() {
    // return code;
    // }
    //
    // private METEOTRACKER_INTERPOLATION(String label, String code) {
    // this.label = label;
    // this.code = code;
    // }
    //
    // public static METEOTRACKER_INTERPOLATION decode(String parameterCode) {
    // for (METEOTRACKER_INTERPOLATION var : values()) {
    // if (parameterCode.equals(var.name())) {
    // return var;
    // }
    // }
    // return null;
    //
    // }
    //
    // }

    /**
     * T0 air temperature [°C] H relative humidity [%] a altitude [m] P pressure [mbar] td dew point [°C] HDX humidex [°C] i vertical
     * temperature gradient [°C/100m] s speed [km/h] L solar radiation index bt bluetooth RSSI [dBm] CO2 [ppm] m1 mass concentration PM1.0
     * [μg/m3] m2 mass concentration PM2.5 [μg/m3] m4 mass concentration PM4.0 [μg/m3] m10 mass concentration PM10 [μg/m3] n0 number
     * concentration PM0.5 [#/cm3] n1 number concentration PM1.0 [#/cm3] n2 number concentration PM2.5 [#/cm3] n4 number concentration PM4.0
     * [#/cm3] n10 number concentration PM10 [#/cm3] tps typical part size [μm] EAQ EPA Air Quality FAQ Fast Air Quality O3 [ppb]
     */

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("trigger-io.difa.unibo.it");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	if (latLonMap.isEmpty()) {

	    String resp = getResponse(BASE_URL + GPS_URL + MIN_VALID_PARAM);
	    getGPSValues(resp);
	}

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {
	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();
	// if (pageSize == 0)
	// pageSize = 100;

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && recordsCount > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	if (!maxNumberReached) {

	    HarvestingProperties properties = request.getHarvestingProperties();

	    Long time = null;

	    try {

		String queryPath = SEARCH_TERMS[searchIndex];
		// LocalDateTime currentDateTime = LocalDateTime.now();
		// int currentMonth = currentDateTime.getMonthValue();
		// int currentYear = currentDateTime.getYear();

		Map<String, TRIGGERDevice> results = new HashMap<String, TRIGGERDevice>();

		String url = BASE_URL + queryPath + MIN_VALID_PARAM;

		GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

		String queryRes = getResponse(url);

		if (queryRes != null) {
		    JSONArray root = new JSONArray(queryRes);
		    JSONArray dataArray = extractDataArray(root);

		    Set<String> processedUsers = new HashSet<>();
		    for(int i = 0; i < dataArray.length(); i++) {
			JSONObject obj = dataArray.getJSONObject(i);

			String userId = obj.getString("userId");

			if (!latLonMap.containsKey(userId)) {
			    GSLoggerFactory.getLogger(getClass()).warn("No coordinates found for userID: {} ", userId);
			    continue;
			}

			if (!processedUsers.add(userId)) {
			    continue;
			}

			for (AGGREGATED_TRIGGER_VARIABLES var : AGGREGATED_TRIGGER_VARIABLES.values()) {
			    String field = var.getCategory();


			    if (!obj.has(field) || obj.isNull(field)) {
				continue;
			    }

			    response.addRecord(AggregatedTRIGGERMapper.create(obj, var.name(), queryPath, latLonMap.get(userId)));
			    partialNumbers++;
			    recordsCount++;

			}


		    }

		}


		GSLoggerFactory.getLogger(getClass()).debug("Current records count: {}", recordsCount);

		// try again, probably the token is expired
		if (TRIGGER_TOKEN == null) {
		    response.setResumptionToken(String.valueOf(Integer.valueOf(start)));
		    return response;
		}

		// if (hasResults) {
		// response.setResumptionToken(String.valueOf(Integer.valueOf(start) + 1));
		// } else {
		// use the next search term
		searchIndex++;
		if (searchIndex < SEARCH_TERMS.length) {
		    response.setResumptionToken(String.valueOf(searchIndex));
		} else {
		    response.setResumptionToken(null);
		    TRIGGER_TOKEN = null;
		    latLonMap = new HashMap<>();
		    searchIndex = 0;
		}
		// }

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		TRIGGER_TOKEN = null;
		getBearerToken();
	    }

	} else {
	    response.setResumptionToken(null);
	    TRIGGER_TOKEN = null;
	    latLonMap = new HashMap<>();
	    GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class)
		    .debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, recordsCount);
	    return response;
	}

	return response;
    }

    private List<String> getStationList() {

	List<String> ret = new ArrayList<String>();
	try {
	    if (TRIGGER_TOKEN == null) {
		TRIGGER_TOKEN = getBearerToken();
	    }

	    String url = BASE_URL + "&limit=1000";
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("token", TRIGGER_TOKEN);

	    HttpResponse<InputStream> triggerResponse = new Downloader().downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("token", TRIGGER_TOKEN));

	    int statusCode = triggerResponse.statusCode();
	    if (statusCode > 400) {
		// token expired - refresh token
		getBearerToken();

		triggerResponse = new Downloader().downloadResponse(//
			url.trim(), //
			HttpHeaderUtils.build("token", TRIGGER_TOKEN));

	    }
	    InputStream stream = triggerResponse.body();
	    GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("Got " + url);

	    if (stream != null) {

		ClonableInputStream clone = new ClonableInputStream(stream);

		JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

		for (int i = 0; i < array.length(); i++) {
		    JSONObject obj = array.getJSONObject(i);
		    String email = obj.optString(EMAIL);
		    String userId = obj.optString(USER_ID);
		    if (email != null) {
			ret.add(email);
		    }
		}

		if (stream != null)
		    stream.close();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return ret;
    }

    private void getGPSValues(String response) {
	Map<String, List<TRIGGERTimePosition>> ret = new HashMap<String, List<TRIGGERTimePosition>>();

	try {

	    if (response != null) {
		JSONArray root = new JSONArray(response);

		JSONArray dataArray = extractDataArray(root);

		for (int i = 0; i < dataArray.length(); i++) {
		    JSONObject obj = dataArray.getJSONObject(i);

		    String userId = obj.getString("userId");

		    LocalDateTime dateTime = LocalDateTime.parse(obj.getString("date") + "T00:00:00");

		    double lat = obj.optDouble("latitude_mean", Double.NaN);
		    double lon = obj.optDouble("longitude_mean", Double.NaN);

		    if (Double.isNaN(lat) || Double.isNaN(lon))
			continue;

		    TRIGGERTimePosition position = new TRIGGERTimePosition(lon, lat, dateTime);

		    latLonMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(position);
		}

		// ordina per data
		for (List<TRIGGERTimePosition> list : latLonMap.values()) {
		    list.sort(Comparator.comparing(TRIGGERTimePosition::getDateTime));
		}

		latLonMap.forEach((userId, list) -> {
		    System.out.println("User: " + userId);

		    list.forEach(p -> System.out.println("  " + p));

		    System.out.println();
		});

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private List<AGGREGATED_TRIGGER_VARIABLES> getVariablesList(JSONObject result, String pathCategory) {

	List<AGGREGATED_TRIGGER_VARIABLES> variables = new ArrayList<AGGREGATED_TRIGGER_VARIABLES>();

	for (AGGREGATED_TRIGGER_VARIABLES var : AGGREGATED_TRIGGER_VARIABLES.values()) {
	    String cat = var.getCategory();
	    if (pathCategory.contains(cat)) {
		String obj = result.optString(var.name().toLowerCase());
		if (obj != null && !obj.isEmpty()) {
		    variables.add(var);
		}
	    }
	}

	return variables;
    }

    public static String getBearerToken() {

	GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("Getting TOKEN from TRIGGER service");

	TRIGGERClient client = new TRIGGERClient(BASE_URL);
	client.setUser(ConfigurationWrapper.getCredentialsSetting().getTriggerUser().orElse(null));
	client.setPassword(ConfigurationWrapper.getCredentialsSetting().getTriggerPassword().orElse(null));
	String token = null;

	try {
	    token = client.getToken();
	} catch (GSException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}
	return token;
    }

    public static String getResponse(String url) {

	GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("Getting Data from TRIGGER service");

	String response = null;

	if (TRIGGER_TOKEN == null) {
	    TRIGGER_TOKEN = getBearerToken();
	}

	try {

	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("token", TRIGGER_TOKEN);

	    HttpResponse<InputStream> triggerResponse = new Downloader().downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("token", TRIGGER_TOKEN));

	    int statusCode = triggerResponse.statusCode();
	    if (statusCode > 400) {
		// token expired - refresh token
		getBearerToken();

		triggerResponse = new Downloader().downloadResponse(//
			url.trim(), //
			HttpHeaderUtils.build("token", TRIGGER_TOKEN));

	    }

	    InputStream stream = triggerResponse.body();
	    GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("Got " + url);

	    if (stream != null) {

		ClonableInputStream clone = new ClonableInputStream(stream);
		response = IOStreamUtils.asUTF8String(clone.clone());
	    }

	    if (stream != null) {
		stream.close();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return response;
    }

    public static Map<String, TRIGGERDevice> getResultList(String url) throws Exception {

	Map<String, TRIGGERDevice> map = new HashMap<String, TRIGGERDevice>();

	// add authorization token
	if (TRIGGER_TOKEN == null) {
	    TRIGGER_TOKEN = getBearerToken();
	}

	InputStream stream = null;

	try {

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, 120);
	    HttpResponse<InputStream> triggerResponse = downloader.downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("token", TRIGGER_TOKEN));

	    int statusCode = triggerResponse.statusCode();

	    if (statusCode > 400) {
		// token expired - refresh token
		getBearerToken();

		triggerResponse = new Downloader().downloadResponse(//
			url.trim(), //
			HttpHeaderUtils.build("token", TRIGGER_TOKEN));

	    }

	    stream = triggerResponse.body();
	    GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("Got " + url);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).error("Unable to retrieve " + url);
	    TRIGGER_TOKEN = null;
	    throw GSException.createException(//
		    AggregatedTRIGGERConnector.class, //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    AGGREGATED_TRIGGER_CONNECTOR_DOWNLOAD_ERROR);
	}

	if (stream != null) {

	    ClonableInputStream clone = new ClonableInputStream(stream);

	    JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

	    for (int i = 0; i < array.length(); i++) {

		JSONObject object = array.getJSONObject(i);
		String userId = object.optString(USER_ID);
		int year = object.optInt("year");
		int month = object.optInt("month");
		int day = object.optInt("day");
		int hour = object.optInt("hour");
		int minute = object.optInt("minute");
		int second = object.optInt("second");
		LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second);

		if (map.containsKey(userId)) {
		    TRIGGERDevice device = map.get(userId);
		    LocalDateTime beginDate = device.getBeginDate();
		    LocalDateTime endDate = device.getEndDate();
		    if (dateTime.isBefore(beginDate)) {
			device.setBeginDate(dateTime);
		    }
		    if (dateTime.isAfter(endDate)) {
			device.setEndDate(dateTime);
			// take last object
			device.setJSONObject(object);
		    }
		    map.put(userId, device);
		} else {
		    // first time
		    TRIGGERDevice device = new TRIGGERDevice(dateTime, dateTime, object);
		    map.put(userId, device);
		}

	    }
	}

	if (stream != null) {
	    stream.close();
	}

	return map;
    }

    public boolean hasResults(ClonableInputStream stream) throws Exception {

	JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(stream.clone()));
	if (array == null || array.length() == 0) {
	    TRIGGER_TOKEN = null;
	    return false;
	}
	return array.length() > 0;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.AGGREGATED_TRIGGER);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected AggregatedTRIGGERConnectorSetting initSetting() {

	return new AggregatedTRIGGERConnectorSetting();
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {
	// TODO Auto-generated method stub
	return false;
    }

    public static void main(String[] args) throws Exception {
	String url = "https://trigger-io.difa.unibo.it/api/call/gps_daily?min_valid_n=1";

	// add authorization token
	String TRIGGER_TOKEN = "NukcqpubNSFbeXqzHiDBYSNhNiWHyfouxMyaNgvhoG";

	// HttpGet get = new HttpGet(url.trim());
	// get.addHeader("token", TRIGGER_TOKEN);
	//
	// InputStream stream = null;
	// HttpResponse triggerResponse = null;
	// HttpClient httpClient = HttpClientBuilder.create().build();
	//
	HashMap<String, String> params = new HashMap<String, String>();
	params.put("token", TRIGGER_TOKEN);

	HttpResponse<InputStream> triggerResponse = new Downloader().downloadResponse(//
		url.trim(), //
		HttpHeaderUtils.build("token", TRIGGER_TOKEN));

	InputStream stream = triggerResponse.body();
	GSLoggerFactory.getLogger(AggregatedTRIGGERConnector.class).info("Got " + url);

	Map<String, List<TRIGGERTimePosition>> latLonMap = new HashMap<>();

	if (stream != null) {

	    ClonableInputStream clone = new ClonableInputStream(stream);

	    JSONArray root = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

	    JSONArray dataArray = extractDataArray(root);

	    for (int i = 0; i < dataArray.length(); i++) {
		JSONObject obj = dataArray.getJSONObject(i);

		String userId = obj.getString("userId");

		LocalDateTime dateTime = LocalDateTime.parse(obj.getString("date") + "T00:00:00");

		double lat = obj.optDouble("latitude_mean", Double.NaN);
		double lon = obj.optDouble("longitude_mean", Double.NaN);

		if (Double.isNaN(lat) || Double.isNaN(lon))
		    continue;

		TRIGGERTimePosition position = new TRIGGERTimePosition(lon, lat, dateTime);

		latLonMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(position);
	    }

	    // ordina per data
	    for (List<TRIGGERTimePosition> list : latLonMap.values()) {
		list.sort(Comparator.comparing(TRIGGERTimePosition::getDateTime));
	    }

	    latLonMap.forEach((userId, list) -> {
		System.out.println("User: " + userId);

		list.forEach(p -> System.out.println("  " + p));

		System.out.println();
	    });

	}
	if (stream != null) {
	    stream.close();
	}

    }

    public static JSONArray extractDataArray(JSONArray root) {
	for (int i = 0; i < root.length(); i++) {
	    Object element = root.get(i);

	    if (element instanceof JSONArray) {
		JSONArray arr = (JSONArray) element;

		if (arr.length() > 0) {
		    Object first = arr.get(0);

		    if (first instanceof JSONObject && ((JSONObject) first).has("userId")) {
			return arr;
		    }
		}
	    }
	}

	throw new RuntimeException("No valid data array found in JSON");
    }
}
