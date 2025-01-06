/**
 *
 */
package eu.essi_lab.accessor.trigger;

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

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
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

/**
 * @author Roberto
 */
public class TRIGGERConnector extends HarvestedQueryConnector<TRIGGERConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "TRIGGERConnector";


    /**
     *
     */
    private int recordsCount = 0;

    private int partialNumbers = 0;

    // private int maxNumberRequest = 1000;

    private static final String TRIGGER_CONNECTOR_DOWNLOAD_ERROR = "TRIGGER_CONNECTOR_DOWNLOAD_ERROR";

    /**
     * In the API V2 it is required
     */
    // private static final String BEARER_TOKEN =
    // "0kCjPHBPlQQJiIXT3Y14_qCxHKumC4i0WTyTqofGM4M.fAqUuOfnL6XYxF4kqJ9JBf67gsfnsefUxEz02B7OvmY";

    private static final String TOKEN_REQUEST_URL = "https://trigger-io.difa.unibo.it/api/";
    private static final String REFRESH_REQUEST_URL = "https://app.meteotracker.com/auth/refreshtoken";
    public static final String SESSION_URL = "https://app.meteotracker.com/api/points/session?";

    public static final String BASE_URL = "https://trigger-io.difa.unibo.it/api/";
    private static final String MYAIR_URL = "myair/?";
    private static final String ECG_URL = "ecg/?";
    private static final String PPG_URL = "ppg/?";
    private static final String GPS_URL = "gps/?";
    private static final String SLEEP_URL = "sleep/?";
    private static final String SMARTWATCHHIGH_URL = "smartwatchhigh/?";
    private static final String SMARTWATCHLOW_URL = "smartwatchlow/?";

    private static final String DEVICE_ID = "deviceId";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";

    Map<String, List<String>> deviceMap = new HashMap<String, List<String>>();

    // private static final String REFRESH_REQUEST_URL = "https://app.meteotracker.com/auth/refreshtoken";

    public static String TRIGGER_TOKEN = null;
    public static String METEOTRACKER_REFRESH_TOKEN = null;
    // private boolean isLastSearchTerm = false;
    private String[] SEARCH_TERMS = { MYAIR_URL, ECG_URL, PPG_URL, SMARTWATCHHIGH_URL, SMARTWATCHLOW_URL };
    private static int searchIndex = 0;

    protected Map<String, List<TRIGGERTimePosition>> latLonMap = new HashMap<String, List<TRIGGERTimePosition>>();

    // https://app.meteotracker.com/api/sessions?
    // https://app.meteotracker.com/api/points/session?id=62b9a83a9ab32d28d7530849&page=2

    public enum TRIGGER_VARIABLES {
	// MYAIR
	TEMPERATURE("2m temperature", "myair", "°C"), HUMIDITY("2m rel. humidity", "myair", "%"), PRESSURE("Pressure", "myair", "mbar"),
	// HDX("Humidex", "°C"),
	SOUND("Sound", "myair", "km/h"), LIGHT("Solar Radiation Index", "myair", "#"), UVB("Ultraviolet B", "myair", "#"),
	// CO2("CO2", "ppm"),
	PM1("Mass Concentration PM1.0", "myair", "μg/m³"), PM25("Mass Concentration PM2.5", "myair", "μg/m³"), PM10(
		"Mass Concentration PM10", "myair", "μg/m³"), PC03("Number of particulate concentration PC0.3", "myair", "#/cm³"), PC05(
			"Number of particulate concentration PM0.5", "myair", "#/cm³"), PC1("Number of particulate concentration PC1.0",
				"myair", "#/cm³"), PC25("Number of particulate concentration PC2.5", "myair", "#/cm³"), PC5(
					"Number of particulate concentration PC5.0", "myair",
					"#/cm³"), PC10("Number of particulate concentration PC10", "myair", "#/cm³"),

	// ECG
	ECG("Electrocardiography (ECG)", "ecg", "mV"),

	// PPG
	PPG("Photoplethysmography (PPG)", "ppg", "µV"),

	// SLEEP

	// SMARTWATCHLOW
	BPHIGH("Systolic Blood Pressure", "smartwatchlow", "mmHg"), BPLOW("Diastolic Blood Pressure", "smartwatchlow",
		"mmHg"), BODYTEMP("Body Temperature", "smartwatchlow", "°C"), SKINTEMP("Skin Temperature", "smartwatchlow", "°C"),

	// SMARTWATCHHIGH
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

	private TRIGGER_VARIABLES(String label, String category, String units) {
	    this.label = label;
	    this.category = category;
	    this.units = units;
	}

	public static TRIGGER_VARIABLES decode(String parameterCode) {
	    for (TRIGGER_VARIABLES var : values()) {
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
     * T0 air temperature [°C] H relative humidity [%] a altitude [m] P pressure
     * [mbar] td dew point [°C] HDX humidex [°C] i vertical temperature gradient
     * [°C/100m] s speed [km/h] L solar radiation index bt bluetooth RSSI [dBm] CO2
     * [ppm] m1 mass concentration PM1.0 [μg/m3] m2 mass concentration PM2.5 [μg/m3]
     * m4 mass concentration PM4.0 [μg/m3] m10 mass concentration PM10 [μg/m3] n0
     * number concentration PM0.5 [#/cm3] n1 number concentration PM1.0 [#/cm3] n2
     * number concentration PM2.5 [#/cm3] n4 number concentration PM4.0 [#/cm3] n10
     * number concentration PM10 [#/cm3] tps typical part size [μm] EAQ EPA Air
     * Quality FAQ Fast Air Quality O3 [ppb]
     */

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("trigger-io.difa.unibo.it");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	// if (latLonMap.isEmpty())
	// latLonMap = getGPSValues();

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {
	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();
//	if (pageSize == 0)
//	    pageSize = 100;

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && recordsCount > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	if (!maxNumberReached) {

	    HarvestingProperties properties = request.getHarvestingProperties();

	    Long time = null;

	    /**
	     * TODO: TRY
	     */
	    // if (properties != null && !properties.isEmpty() && !request.getRecovering()) {
	    //
	    // String timestamp = properties.getEndHarvestingTimestamp();
	    // if (timestamp != null) {
	    // @SuppressWarnings("deprecation")
	    // Date date = ISO8601DateTimeUtils.parseISO8601(timestamp);
	    // time = date.getTime();
	    // GSLoggerFactory.getLogger(getClass()).info("Incremental harvesting enabled starting from: " + timestamp);
	    // }
	    // }
	    try {

		String queryPath = SEARCH_TERMS[searchIndex];
		LocalDateTime currentDateTime = LocalDateTime.now();
		int currentMonth = currentDateTime.getMonthValue();
		Map<String, TRIGGERDevice> results = new HashMap<String, TRIGGERDevice>();
		// current month for 2024
		for (int i = 1; i <= currentMonth; i++) {

		    String url = getSourceURL().endsWith("/") ? getSourceURL() + queryPath + "year=2024&month=" + i// +
														   // start
														   // +
			    // "&items=" +
			    // pageSize
			    : getSourceURL() + "/" + queryPath + "year=2024&month=" + i;// "&reverseSort=false&page=" +
											// start +
		    // "&items=" +
		    // pageSize;

		    GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

		    Map<String, TRIGGERDevice> partialResults = getResultList(url);

		    for (Map.Entry<String, TRIGGERDevice> entry : partialResults.entrySet()) {
			String id = entry.getKey();
			TRIGGERDevice device = entry.getValue();
			if (results.containsKey(id)) {
			    TRIGGERDevice resDevice = results.get(id);
			    LocalDateTime startDate = resDevice.getBeginDate();
			    LocalDateTime endDate = resDevice.getEndDate();
			    if (startDate.isAfter(device.getBeginDate())) {
				startDate = device.getBeginDate();
			    }
			    if (endDate.isBefore(device.getEndDate())) {
				endDate = device.getEndDate();
			    }
			    device.setBeginDate(startDate);
			    device.setEndDate(endDate);
			    results.put(id, device);
			} else {
			    results.put(id, device);
			}
		    }

		    boolean hasResults = partialResults.size() > 0;

		    Optional<Integer> maxRecords = getSetting().getMaxRecords();
		}

		for (Map.Entry<String, TRIGGERDevice> entry : results.entrySet()) {

		    String deviceId = entry.getKey();
		    if (deviceId == null || deviceId.isEmpty())
			continue;

		    TRIGGERDevice device = entry.getValue();
		    JSONObject obj = device.getJSONObject();

		    List<TRIGGER_VARIABLES> variableNames = getVariablesList(obj, queryPath);

		    for (TRIGGER_VARIABLES v : variableNames) {

			// for (METEOTRACKER_INTERPOLATION inter : interpolationValues) {
			if (latLonMap.containsKey(deviceId)) {
			    response.addRecord(TRIGGERMapper.create(device, v.name(), queryPath, latLonMap.get(deviceId)));
			} else {
			    response.addRecord(TRIGGERMapper.create(device, v.name(), queryPath, null));
			}
			partialNumbers++;
			recordsCount++;
			// }

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
	    GSLoggerFactory.getLogger(TRIGGERConnector.class).debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers,
		    recordsCount);
	    return response;
	}

	return response;
    }

    private Map<String, List<TRIGGERTimePosition>> getGPSValues() {
	Map<String, List<TRIGGERTimePosition>> ret = new HashMap<String, List<TRIGGERTimePosition>>();
	String url = BASE_URL + GPS_URL + "year=2024";

	try {
	    // add authorization token
	    if (TRIGGER_TOKEN == null) {
		TRIGGER_TOKEN = getBearerToken();
	    }

	    // HttpGet get = new HttpGet(url.trim());
	    // get.addHeader("token", TRIGGER_TOKEN);
	    //
	    // InputStream stream = null;
	    // HttpResponse triggerResponse = null;
	    // HttpClient httpClient = HttpClientBuilder.create().build();
	    //
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("refreshToken", TRIGGER_TOKEN);

	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    url.trim(), //
		    params);

	    HttpResponse<InputStream> triggerResponse = new Downloader().downloadResponse(request);

	    int statusCode = triggerResponse.statusCode();
	    if (statusCode > 400) {
		// token expired - refresh token
		getBearerToken();

		params = new HashMap<String, String>();
		params.put("refreshToken", TRIGGER_TOKEN);

		request = HttpRequestUtils.build(//
			MethodWithBody.POST, //
			url.trim(), //
			params);

		triggerResponse = new Downloader().downloadResponse(request);

	    }
	    InputStream stream = triggerResponse.body();
	    GSLoggerFactory.getLogger(TRIGGERConnector.class).info("Got " + url);

	    if (stream != null) {

		ClonableInputStream clone = new ClonableInputStream(stream);

		JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

		for (int i = 0; i < array.length(); i++) {

		    JSONObject obj = array.getJSONObject(i);
		    String deviceId = obj.optString(DEVICE_ID);
		    Double lon = obj.optDoubleObject(LONGITUDE);
		    Double lat = obj.optDoubleObject(LATITUDE);
		    int year = obj.optInt("year");
		    int month = obj.optInt("month");
		    int day = obj.optInt("day");
		    int hour = obj.optInt("hour");
		    int minute = obj.optInt("minute");
		    int second = obj.optInt("second");
		    LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second);
		    TRIGGERTimePosition tp = new TRIGGERTimePosition(lon, lat, dateTime);
		    List<TRIGGERTimePosition> timePositionList = new ArrayList<TRIGGERTimePosition>();
		    if (ret.containsKey(deviceId)) {
			timePositionList = ret.get(deviceId);
			timePositionList.add(tp);
			ret.put(deviceId, timePositionList);
		    } else {
			// first element
			timePositionList.add(tp);
			ret.put(deviceId, timePositionList);
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

    private List<TRIGGER_VARIABLES> getVariablesList(JSONObject result, String pathCategory) {

	List<TRIGGER_VARIABLES> variables = new ArrayList<TRIGGERConnector.TRIGGER_VARIABLES>();

	for (TRIGGER_VARIABLES var : TRIGGER_VARIABLES.values()) {
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

    public static void refreshBearerToken() {
	// https://app.meteotracker.com/auth/refreshtoken
	GSLoggerFactory.getLogger(TRIGGERConnector.class).info("Refreshing BEARER TOKEN from TRIGGER service");

	HashMap<String, String> params = new HashMap<String, String>();
	params.put("refreshToken", METEOTRACKER_REFRESH_TOKEN);

	String result = null;
	String token = null;

	try {

	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    REFRESH_REQUEST_URL, //
		    params);

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(request);

	    int statusCode = response.statusCode();
	    if (statusCode > 400) {
		// token expired - refresh token
		getBearerToken();
	    }
	    result = IOUtils.toString(response.body(), "UTF-8");
	    GSLoggerFactory.getLogger(TRIGGERConnector.class).info("RESPONSE FROM MeteoTracker " + result);
	    if (result != null && !result.isEmpty()) {
		JSONObject obj = new JSONObject(result);
		TRIGGER_TOKEN = obj.optString("accessToken");
		METEOTRACKER_REFRESH_TOKEN = obj.optString("refreshToken");
		GSLoggerFactory.getLogger(TRIGGERConnector.class).info("BEARER TOKEN obtained: " + TRIGGER_TOKEN);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(TRIGGERConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	}
    }

    public static String getBearerToken() {

	GSLoggerFactory.getLogger(TRIGGERConnector.class).info("Getting TOKEN from TRIGGER service");

	TRIGGERClient client = new TRIGGERClient(BASE_URL);
	client.setUser(ConfigurationWrapper.getCredentialsSetting().getTriggerUser().orElse(null));
	client.setPassword(ConfigurationWrapper.getCredentialsSetting().getTriggerPassword().orElse(null));
	String token = null;

	try {
	    token = client.getToken();
	} catch (GSException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(TRIGGERConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}
	return token;
    }

    public static Map<String, TRIGGERDevice> getResultList(String url) throws Exception {

	Map<String, TRIGGERDevice> map = new HashMap<String, TRIGGERDevice>();

	// add authorization token
	if (TRIGGER_TOKEN == null) {
	    TRIGGER_TOKEN = getBearerToken();
	}

	InputStream stream = null;

	try {

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
	    
	    stream = triggerResponse.body();
	    GSLoggerFactory.getLogger(TRIGGERConnector.class).info("Got " + url);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(TRIGGERConnector.class).error("Unable to retrieve " + url);
	    TRIGGER_TOKEN = null;
	    throw GSException.createException(//
		    TRIGGERConnector.class, //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    TRIGGER_CONNECTOR_DOWNLOAD_ERROR);
	}

	if (stream != null) {

	    ClonableInputStream clone = new ClonableInputStream(stream);

	    JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

	    for (int i = 0; i < array.length(); i++) {

		JSONObject object = array.getJSONObject(i);
		String deviceId = object.optString(DEVICE_ID);
		int year = object.optInt("year");
		int month = object.optInt("month");
		int day = object.optInt("day");
		int hour = object.optInt("hour");
		int minute = object.optInt("minute");
		int second = object.optInt("second");
		LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, second);

		if (map.containsKey(deviceId)) {
		    TRIGGERDevice device = map.get(deviceId);
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
		    map.put(deviceId, device);
		} else {
		    // first time
		    TRIGGERDevice device = new TRIGGERDevice(dateTime, dateTime, object);
		    map.put(deviceId, device);
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
	ret.add(CommonNameSpaceContext.TRIGGER);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected TRIGGERConnectorSetting initSetting() {

	return new TRIGGERConnectorSetting();
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {
	// TODO Auto-generated method stub
	return false;
    }

    public static void main(String[] args) throws Exception {
	TRIGGERClient client = new TRIGGERClient("https://trigger-io.difa.unibo.it/api/");
	client.setUser("test@test.com");
	client.setPassword("test");
	String res = "NZRrGawKCNXGNQWoWhqfuCPMuvcEUzupxtvDnIaZdJ";// client.getToken();

	ArrayList<JSONObject> out = Lists.newArrayList();
	Date today = new Date();
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(today);
	int year = calendar.get(Calendar.YEAR);
	int month = calendar.get(Calendar.MONTH) + 1;

	// String[] search = { MYAIR_URL, ECG_URL, GPS_URL, PPG_URL, SLEEP_URL, SMARTWATCHLOW_URL, SMARTWATCHHIGH_URL };

	String[] search = { ECG_URL };
	for (String s : search) {

	    String url = BASE_URL + s + "year=2024&month=03";
	 
	    InputStream stream = null;
 
	    HttpResponse<InputStream> meteoTrackerResponse = new Downloader().downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("token", res));

	 	    
	    int statusCode = meteoTrackerResponse.statusCode();
	    if (statusCode > 400) {
		// token expired - refresh token
		System.err.println("ERROR");
	    }
	    stream = meteoTrackerResponse.body();
	    GSLoggerFactory.getLogger(TRIGGERConnector.class).info("Got " + url);
	    Map<String, Integer> map = new HashMap<String, Integer>();
	    if (stream != null) {

		ClonableInputStream clone = new ClonableInputStream(stream);

		JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

		System.out.println(IOStreamUtils.asUTF8String(clone.clone()));
		for (int i = 0; i < array.length(); i++) {

		    JSONObject object = array.getJSONObject(i);
		    String deviceId = object.optString("deviceId");
		    if (map.containsKey(deviceId)) {
			map.put(deviceId, map.get(deviceId) + 1);
		    } else {
			map.put(deviceId, 1);
		    }
		}
		stream.close();
	    }
	    System.out.println("CATEGORY: " + s);
	    map.entrySet().stream()
		    // ... some other Stream processings
		    .forEach(e -> System.out.println(e.getKey() + ":" + e.getValue()));

	}
    }
}
