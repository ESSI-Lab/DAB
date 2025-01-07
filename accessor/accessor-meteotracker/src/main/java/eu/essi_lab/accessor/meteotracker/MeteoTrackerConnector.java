/**
 *
 */
package eu.essi_lab.accessor.meteotracker;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

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
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
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
public class MeteoTrackerConnector extends HarvestedQueryConnector<MeteoTrackerConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "MeteoTrackerConnector";

    /**
     *
     */
    private int recordsCount = 0;

    private int partialNumbers = 0;

    // private int maxNumberRequest = 1000;

    private static final String METEOTRACKER_CONNECTOR_DOWNLOAD_ERROR = "METEOTRACKER_CONNECTOR_DOWNLOAD_ERROR";

    /**
     * In the API V2 it is required
     */
    // private static final String BEARER_TOKEN =
    // "0kCjPHBPlQQJiIXT3Y14_qCxHKumC4i0WTyTqofGM4M.fAqUuOfnL6XYxF4kqJ9JBf67gsfnsefUxEz02B7OvmY";

    private static final String TOKEN_REQUEST_URL = "https://app.meteotracker.com/auth/login/api";
    private static final String REFRESH_REQUEST_URL = "https://app.meteotracker.com/auth/refreshtoken";
    public static final String SESSION_URL = "https://app.meteotracker.com/api/points/session?";
    // private static final String REFRESH_REQUEST_URL = "https://app.meteotracker.com/auth/refreshtoken";

    public static String METEOTRACKER_BEARER_TOKEN = null;
    public static String METEOTRACKER_REFRESH_TOKEN = null;
    // private boolean isLastSearchTerm = false;
    private String[] SEARCH_TERMS = { "living_lab", "i-change", "Dublin%20LL" };
    private static int searchIndex = 0;

    // https://app.meteotracker.com/api/sessions?
    // https://app.meteotracker.com/api/points/session?id=62b9a83a9ab32d28d7530849&page=2

    public enum METEOTRACKER_VARIABLES {
	T0("2m temperature", "°C"), H("2m rel. humidity", "%"), P("Pressure", "mbar"), HDX("Humidex", "°C"), s("Speed", "km/h"), L(
		"Solar Radiation Index", "%"), CO2("CO2", "ppm"), m1("Mass Concentration PM1.0", "μg/m³"), m2("Mass Concentration PM2.5",
			"μg/m³"), m4("Mass Concentration PM4.0", "μg/m³"), m10("Mass Concentration PM10",
				"μg/m³"), n0("Number concentration PM0.5", "#/cm³"), n1("Number concentration PM1.0",
					"#/cm³"), n2("Number concentration PM2.5", "#/cm³"), n4("Number concentration PM4.5",
						"#/cm³"), n10("Number concentration PM10", "#/cm³"), tps("Typical Part size", "μm");

	// td("Dew Point", "°C") EAQ("EPA Air Quality", i("Vertical temperature gradient", "°C/100m"), bt("Bluetooth
	// RSSI","dBm"),
	// "%"), FAQ("Fast Air Quality", "%"), O3("Relative Humidity", "%");

	private String label;
	private String units;

	public String getLabel() {
	    return label;
	}

	public String getUnits() {
	    return units;
	}

	private METEOTRACKER_VARIABLES(String label, String units) {
	    this.label = label;
	    this.units = units;
	}

	public static METEOTRACKER_VARIABLES decode(String parameterCode) {
	    for (METEOTRACKER_VARIABLES var : values()) {
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

	return source.getEndpoint().contains("app.meteotracker.com");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	// String page = "0";
	//
	// if (request.getResumptionToken() != null) {
	//
	// page = request.getResumptionToken();
	// }

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

	    if (properties != null && !properties.isEmpty() && !request.isRecovered()) {

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

	    String querySearch = SEARCH_TERMS[searchIndex];

	    // if (isLastSearchTerm) {
	    // querySearch = SEARCH_TERMS[1];
	    // }

	    String url = getSourceURL().endsWith("?")
		    ? getSourceURL() + "by=" + querySearch + "&reverseSort=false&page=" + start + "&items=" + pageSize
		    : getSourceURL() + "?by=" + querySearch + "&reverseSort=false&page=" + start + "&items=" + pageSize;
	    if (time != null) {
		try {
		    url = url + "&startTime=" + URLEncoder.encode("{\"$gte\":" + time + "}", "UTF-8");
		} catch (UnsupportedEncodingException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	    try {

		List<JSONObject> results = getResultList(url);

		boolean hasResults = results.size() > 0;

		Optional<Integer> maxRecords = getSetting().getMaxRecords();

		// METEOTRACKER_INTERPOLATION[] interpolationValues = METEOTRACKER_INTERPOLATION.values();

		for (JSONObject result : results) {

		    List<METEOTRACKER_VARIABLES> variableNames = getVariablesList(result);

		    for (METEOTRACKER_VARIABLES v : variableNames) {

			// for (METEOTRACKER_INTERPOLATION inter : interpolationValues) {

			response.addRecord(MeteoTrackerMapper.create(result, v.name()));
			partialNumbers++;
			recordsCount++;
			// }

		    }
		}

		GSLoggerFactory.getLogger(getClass()).debug("Current records count: {}", recordsCount);

		// try again, probably the token is expired
		if (METEOTRACKER_BEARER_TOKEN == null) {
		    response.setResumptionToken(String.valueOf(Integer.valueOf(start)));
		    return response;
		}

		if (hasResults) {
		    response.setResumptionToken(String.valueOf(Integer.valueOf(start) + 1));
		} else {
		    // use the next search term
		    searchIndex++;
		    if (searchIndex < SEARCH_TERMS.length) {
			response.setResumptionToken("0");
		    } else {
			response.setResumptionToken(null);
			METEOTRACKER_BEARER_TOKEN = null;
			searchIndex = 0;
		    }
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		METEOTRACKER_BEARER_TOKEN = null;
		refreshBearerToken();
	    }

	} else {
	    response.setResumptionToken(null);
	    METEOTRACKER_BEARER_TOKEN = null;
	    GSLoggerFactory.getLogger(MeteoTrackerConnector.class).debug("Added Collection records: {} . TOTAL STATION SIZE: {}",
		    partialNumbers, recordsCount);
	    return response;
	}

	return response;
    }

    private List<METEOTRACKER_VARIABLES> getVariablesList(JSONObject result) {

	List<METEOTRACKER_VARIABLES> variables = new ArrayList<MeteoTrackerConnector.METEOTRACKER_VARIABLES>();

	for (METEOTRACKER_VARIABLES var : METEOTRACKER_VARIABLES.values()) {
	    JSONObject obj = result.optJSONObject(var.name());
	    if (obj != null && obj.length() > 0) {
		variables.add(var);
	    }
	}

	return variables;
    }

    public static void refreshBearerToken() {
	// https://app.meteotracker.com/auth/refreshtoken
	GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("Refreshing BEARER TOKEN from MeteoTracker service");

	// HttpPost httpPost = new HttpPost(REFRESH_REQUEST_URL);
	// HttpClient httpClient = HttpClientBuilder.create().build();
	// List<NameValuePair> params = new ArrayList<NameValuePair>(2);
	// params.add(new BasicNameValuePair("refreshToken", METEOTRACKER_REFRESH_TOKEN));

	String result = null;
	String token = null;
	try {
	    // httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("refreshToken", METEOTRACKER_REFRESH_TOKEN);

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
	    GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("RESPONSE FROM MeteoTracker " + result);
	    if (result != null && !result.isEmpty()) {
		JSONObject obj = new JSONObject(result);
		METEOTRACKER_BEARER_TOKEN = obj.optString("accessToken");
		METEOTRACKER_REFRESH_TOKEN = obj.optString("refreshToken");
		GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("BEARER TOKEN obtained: " + METEOTRACKER_BEARER_TOKEN);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	}
    }

    public static String getBearerToken() {

	GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("Getting BEARER TOKEN from MeteoTracker service");
	// HttpPost httpPost = new HttpPost(TOKEN_REQUEST_URL);

	// HttpClient httpClient = HttpClientBuilder.create().build();
	// List<NameValuePair> params = new ArrayList<NameValuePair>(2);
	// params.add(new BasicNameValuePair("email", CLIENT_ID));
	// params.add(new BasicNameValuePair("password", CLIENT_SECRET));

	String result = null;
	String token = null;

	try {
	    // httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("email", ConfigurationWrapper.getCredentialsSetting().getMeteotrackerUser().orElse(null));
	    params.put("password", ConfigurationWrapper.getCredentialsSetting().getMeteotrackerPassword().orElse(null));

	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    TOKEN_REQUEST_URL, //
		    params);

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(request);

	    int statusCode = response.statusCode();
	    if (statusCode > 400) {
		// token expired - refresh token
		refreshBearerToken();
	    }

	    result = IOUtils.toString(response.body(), "UTF-8");
	    GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("RESPONSE FROM MeteoTracker " + result);
	    if (result != null && !result.isEmpty()) {
		JSONObject obj = new JSONObject(result);
		token = obj.optString("accessToken");
		METEOTRACKER_REFRESH_TOKEN = obj.optString("refreshToken");
		GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("BEARER TOKEN obtained: " + token);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}
	return token;
    }

    public static List<JSONObject> getResultList(String url) throws Exception {

	ArrayList<JSONObject> out = Lists.newArrayList();

	// add authorization token
	if (METEOTRACKER_BEARER_TOKEN == null) {
	    METEOTRACKER_BEARER_TOKEN = getBearerToken();
	}

	// HttpGet get = new HttpGet(url.trim());
	// get.addHeader("Authorization", "Bearer " + METEOTRACKER_BEARER_TOKEN);

	InputStream stream = null;
	// HttpResponse meteoTrackerResponse = null;
	// HttpClient httpClient = HttpClientBuilder.create().build();
	try {
	    // meteoTrackerResponse = httpClient.execute(get);

	    HttpResponse<InputStream> meteoTrackerResponse = new Downloader().downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("Authorization", "Bearer " + METEOTRACKER_BEARER_TOKEN));

	    int statusCode = meteoTrackerResponse.statusCode();

	    if (statusCode > 400) {
		// token expired - refresh token
		refreshBearerToken();

		// HttpGet newGet = new HttpGet(url.trim());
		// newGet.addHeader("Authorization", "Bearer " + METEOTRACKER_BEARER_TOKEN);
		// meteoTrackerResponse = httpClient.execute(newGet);
		//
		meteoTrackerResponse = new Downloader().downloadResponse(//
			url.trim(), //
			HttpHeaderUtils.build("Authorization", "Bearer " + METEOTRACKER_BEARER_TOKEN));
	    }
	    stream = meteoTrackerResponse.body();
	    GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("Got " + url);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(MeteoTrackerConnector.class).error("Unable to retrieve " + url);
	    METEOTRACKER_BEARER_TOKEN = null;
	    throw GSException.createException(//
		    MeteoTrackerConnector.class, //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    METEOTRACKER_CONNECTOR_DOWNLOAD_ERROR);
	}

	if (stream != null) {

	    ClonableInputStream clone = new ClonableInputStream(stream);

	    JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

	    for (int i = 0; i < array.length(); i++) {

		JSONObject object = array.getJSONObject(i);
		out.add(object);
	    }
	}

	if (stream != null) {
	    stream.close();
	}

	return out;
    }

    public boolean hasResults(ClonableInputStream stream) throws Exception {

	JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(stream.clone()));
	if (array == null || array.length() == 0) {
	    METEOTRACKER_BEARER_TOKEN = null;
	    return false;
	}
	return array.length() > 0;
    }

    @Override
    public String getSourceURL() {

	String url = super.getSourceURL();
	if (!url.endsWith("?")) {
	    url += "?";
	}

	return url;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.METEOTRACKER);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected MeteoTrackerConnectorSetting initSetting() {

	return new MeteoTrackerConnectorSetting();
    }

    @Override
    public boolean supportsIncrementalHarvesting() throws GSException {
	// TODO Auto-generated method stub
	return true;
    }

    public static void main(String[] args) throws Exception, IOException {
	final String[] TYPES = { "living_lab", "i-change", "Dublin CC" };
	int i = Arrays.asList(TYPES).indexOf("Dublin CC");
	System.out.println(i);
	System.out.println(Arrays.asList(TYPES).lastIndexOf("i-change"));
	System.out.println(TYPES.length);

    }
}
