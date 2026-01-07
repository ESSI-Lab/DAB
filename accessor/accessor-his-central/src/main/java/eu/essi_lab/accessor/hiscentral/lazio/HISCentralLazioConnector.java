package eu.essi_lab.accessor.hiscentral.lazio;

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

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

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
public class HISCentralLazioConnector extends HarvestedQueryConnector<HISCentralLazioConnectorSetting> {

    /**
     * 
     */
    static final String TYPE = "HISCentralLazioConnector";

    /**
     * 
     */
    public HISCentralLazioConnector() {

    }

    /**
     * 
     */

    public static final String LOGIN_URL = "login";

    static final String STATIONS_URL = "stations?";

    /**
     * 
     */
    static final String SENSOR_URL = "elements?";

    public static final String BASE_URL = "http://rlazio.dynalias.org/datascape/v1/";

    public static final String TOKEN_URL = "http://rlazio.dynalias.org/datascape/connect/token";

    private static final String HIS_CENTRAL_LAZIO_CONNECTOR_DOWNLOAD_ERROR = "HIS_CENTRAL_LAZIO_CONNECTOR_DOWNLOAD_ERROR";

    /**
     * 
     */

    private int maxRecords;

    public static String BEARER_TOKEN = null;

    public static String REFRESH_BEARER_TOKEN = null;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	String page = "1";

	if (request.getResumptionToken() != null) {

	    page = request.getResumptionToken();
	}

	// add authorization token
	String baseUrl = getSourceURL();
	if (BEARER_TOKEN == null) {
	    BEARER_TOKEN = getBearerToken(baseUrl);
	}

	JSONArray stationsArray = getStationsList();

	if (stationsArray != null) {

	    maxRecords = stationsArray.length();
	    getSetting().getMaxRecords().ifPresent(v -> maxRecords = v);
	    for (int i = 0; i < maxRecords; i++) {

		JSONObject datasetMetadata = stationsArray.getJSONObject(i);
		String id = datasetMetadata.optString("stationId");
		if (id != null && !id.isEmpty()) {
		    JSONArray measuresArray = getMeasures(id);

		    if (measuresArray != null) {
			for (int j = 0; j < measuresArray.length(); j++) {
			    JSONObject sensorInfo = measuresArray.getJSONObject(j);
			    ret.addRecord(HISCentralLazioMapper.create(datasetMetadata, sensorInfo));
			}

		    }

		}
	    }

	} else {
	    GSLoggerFactory.getLogger(getClass()).info("ERROR getting items.");
	    BEARER_TOKEN = null;
	}

	return ret;
    }

    private JSONArray getMeasures(String id) throws GSException {
	// e.g:
	// http://rlazio.dynalias.org/datascape/v1/elements?category=All&station_id=325200&field=ElementName&field=ElementId&
	// field=CustomElementId&field=StationName&field=StationId&field=CustomStationId&field=QuantityOrgId&
	// field=QuantityId&field=Basin&field=River&field=StartDate&field=EndDate&field=Decimals&field=MeasUnit&
	// field=Dtr&field=IsVirtual&field=Time&field=Value&field=Trend&field=StateId&field=StateDescr

	// http://rlazio.dynalias.org/datascape/v1/elements?category=All&station_id=247100&field=ElementName&field=ElementId&field=CustomElementId&
	// field=StationName&field=StationId&field=CustomStationId&field=QuantityOrgId&field=QuantityId&field=Basin&field=River&field=StartDate&
	// field=EndDate&field=Decimals&field=MeasUnit&field=Dtr&field=IsVirtual&field=Time&field=Value&field=Trend&field=StateId&field=StateDescr

	String url = getSourceURL() + SENSOR_URL;
	url = url + "category=All&station_id=" + id
		+ "&field=ElementName&field=ElementId&field=StationName&field=StartDate&field=EndDate&field=MeasUnit";
	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	// HttpGet get = new HttpGet(url.trim());
	// get.addHeader("Authorization", "Bearer " + BEARER_TOKEN);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

	    // RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
	    // .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
	    // CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	    // CloseableHttpResponse getStationResponse = client.execute(get);
	    // stream = getStationResponse.getEntity().getContent();
	    //
	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(url.trim(),
		    HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));

	    stream = getStationResponse.body();

	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    if (stream != null) {
		JSONArray arrayResult = new JSONArray(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return arrayResult;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    BEARER_TOKEN = null;
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_LAZIO_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    private JSONArray getStationsList() throws GSException {
	// e.g. http://rlazio.dynalias.org/datascape/v1/stations?category=All&field=StationName&
	// field=StationId&field=CustomStationId&field=Basin&field=River&field=AlertZone&field=Time&field=StateId&
	// field=RegisterName&field=Address&field=Locality&field=City&field=Province&field=Prov&field=Region&field=Country&
	// field=Longitude&field=Latitude&field=Altitude&field=Gmt

	// StationName, StationId, Locality, City, Prov, Longitude, Latitude, Altitude

	String url = getSourceURL() + STATIONS_URL;
	url = url
		+ "category=All&field=StationName&field=StationId&field=Locality&field=City&field=Prov&field=Longitude&field=Latitude&field=Altitude";
	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	// HttpGet get = new HttpGet(url.trim());
	// get.addHeader("Authorization", "Bearer " + BEARER_TOKEN);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

	    // RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
	    // .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
	    // CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	    // CloseableHttpResponse getStationResponse = client.execute(get);
	    // stream = getStationResponse.getEntity().getContent();

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));

	    stream = getStationResponse.body();

	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    if (stream != null) {
		JSONArray arrayResult = new JSONArray(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return arrayResult;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    BEARER_TOKEN = null;
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_LAZIO_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    public static String getBearerToken(String baseUrl) {

	GSLoggerFactory.getLogger(HISCentralLazioConnector.class).info("Getting BEARER TOKEN from Lazio Datascape service");

	// StringEntity input = null;
	String token = null;

	try {
	    // HttpPost httpPost = new HttpPost(TOKEN_URL);
	    // HttpClient httpClient = HttpClientBuilder.create().build();
	    //
	    // List<NameValuePair> params = new ArrayList<NameValuePair>(2);
	    // params.add(new BasicNameValuePair("username", CLIENT_USER));
	    // params.add(new BasicNameValuePair("password", CLIENT_SECRET));
	    // params.add(new BasicNameValuePair("grant_type", "password"));
	    // params.add(new BasicNameValuePair("client_id", CLIENT_ID));
	    // params.add(new BasicNameValuePair("client_instance", CLIENT_INSTANCE));
	    // httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
	    //
	    // HttpResponse response = null;
	    //
	    // response = httpClient.execute(httpPost);

	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("username", ConfigurationWrapper.getCredentialsSetting().getLazioClientUser().orElse(null));
	    params.put("password", ConfigurationWrapper.getCredentialsSetting().getLazioClientPassword().orElse(null));
	    params.put("grant_type", "password");
	    params.put("client_id", ConfigurationWrapper.getCredentialsSetting().getLazioClientId().orElse(null));
	    params.put("client_instance", ConfigurationWrapper.getCredentialsSetting().getLazioClientInstance().orElse(null));


	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    TOKEN_URL, //
		    params);

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(request);

	    JSONObject result = new JSONObject(IOStreamUtils.asUTF8String(response.body()));

	    // result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
	    if (result != null) {
		token = result.optString("access_token");
		REFRESH_BEARER_TOKEN = result.optString("refresh_token");
		GSLoggerFactory.getLogger(HISCentralLazioConnector.class).info("BEARER TOKEN obtained: " + BEARER_TOKEN);
		GSLoggerFactory.getLogger(HISCentralLazioConnector.class).info("BEARER TOKEN obtained: " + REFRESH_BEARER_TOKEN);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(HISCentralLazioConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}
	return token;

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_LAZIO_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("rlazio.dynalias.org");
    }

    @Override
    protected HISCentralLazioConnectorSetting initSetting() {

	return new HISCentralLazioConnectorSetting();
    }

    public static void main(String[] args) throws Exception {

	// HttpPost httpPost = new HttpPost(TOKEN_URL);
	// HttpClient httpClient = HttpClientBuilder.create().build();
	//
	// List<NameValuePair> params = new ArrayList<NameValuePair>(2);
	//
	// params.add(new BasicNameValuePair("username", CLIENT_USER));
	// params.add(new BasicNameValuePair("password", CLIENT_SECRET));
	// params.add(new BasicNameValuePair("grant_type", "password"));
	// params.add(new BasicNameValuePair("client_id", CLIENT_ID));
	// params.add(new BasicNameValuePair("client_instance", CLIENT_INSTANCE));
	// // params.add(new BasicNameValuePair("refresh_token",
	// //
	// "CfDJ8M4beJMvqa9Hn7wcS6XbAQVPympbQWe1dsG-jsqRW3580XldR0A33uyASg97uYWr2xbhbhv4NMXyVHY_QFU4fAtfdnTMgtaY_Hp0mtk6verPtQ95OjpQ1H39qCFHUzSAvo2zqDQM7GBW5CCCqgducyF_gUOuass0Ea-LX-cbpLXI7zVDay2D8axM52tDrPSEpZqCnGD-gZ8H37V-tLmkbOyfsFr5upEEti5R3_MH8ZDegRN_navJYQCqYr-QH4Hpd8DqmaIFWjKIRqEBLPkl8sAOma4I9Q1NfgCXouV5C8M_mycXEz-CHfBnWJG2S_4JFSztBJfLUxtqeZufPebrW_pPNRnx9NwOL6U66A755oKSXL0k7ceWHN_2kfMMbwtfDb_QM8i9yBMIhXAfTUNdhabPZEm6nlTbJ9igMGI2B9gLDxtRyCjGwHwyGEvibOjKsTkJ-AvKmrVxMcih9TzxB590791XG5bJmf-1-Nniuo2PCuKksPpvO2xxUPlo-P72fwnrY4yDo9cHUqs_ajHnG50yb8RGnxIRZXUoCErc2Y8yPgy-vXJ2vF1TkUkWOmjmxr2bGUMezMeEzxnfSrN3HpDwmO2mR7dEO9KBk2uvO8_oLMVDrdObVx6a4MVZZm3wkNvRUoZD6XoU7hKE9Kb13BFUP7S5AmbnVHmcZwUmnocnyt39Ta8e_ln8x9oBRcvFt31uqmdFSgZ8rdmOt5nkatS5tMOSG-69z35cz2jRRpFUv77E9wHN8wHg4urmESjS03qFvUaZdd7Uu1VYqToUeeJ_I0biNlioQVVRbvPAvLsg_sGsS1EY3ByqpM94yrLw2o8JqYc9_SvFTX0wK5eOb_9uJQStlv7WPHFjHYb44FwKrr7kZ1B4D9hlEOYezakPOw"));
	//
	// httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

	HashMap<String, String> params = new HashMap<String, String>();
	params.put("username", ConfigurationWrapper.getCredentialsSetting().getLazioClientUser().orElse(null));
	params.put("password", ConfigurationWrapper.getCredentialsSetting().getLazioClientPassword().orElse(null));
	params.put("grant_type", "password");
	params.put("client_id", ConfigurationWrapper.getCredentialsSetting().getLazioClientId().orElse(null));
	params.put("client_instance", ConfigurationWrapper.getCredentialsSetting().getLazioClientInstance().orElse(null));

	HttpRequest request = HttpRequestUtils.build(//
		MethodWithBody.POST, //
		TOKEN_URL, //
		params);
	
	System.out.println(params);

	HttpResponse<InputStream> response = new Downloader().downloadResponse(request);

 	JSONObject obj = new JSONObject(IOStreamUtils.asUTF8String(response.body()));

	if (obj != null) {
	    String bearer = obj.optString("access_token");
	    System.out.println(bearer);
	}

    }

    @Override
    public String getSourceURL() {

	String url = super.getSourceURL();
	if (!url.endsWith("/")) {
	    url += "/";
	}

	return url;

    }

}
