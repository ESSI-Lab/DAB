package eu.essi_lab.accessor.hiscentral.valdaosta;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
public class HISCentralValdaostaConnector extends HarvestedQueryConnector<HISCentralValdaostaConnectorSetting> {

    /**
     * 
     */
    static final String TYPE = "HISCentralValdaostaConnector";

    /**
     * 
     */
    public HISCentralValdaostaConnector() {

    }

    /**
     * 
     */

    static final String STATIONS_URL = "stations";

    static final String ORGANIZATION_URL = "organization";
    
    static final String PARAMETERS = "parameters";
    /**
     * 
     */
    static final String SENSOR_URL = "elements?";

    public static final String BASE_URL = "https://cf-api.regione.vda.it/ws2/";

    public static final String TOKEN_URL = "https://cf-api.regione.vda.it/ws2/login";

    private static final String HIS_CENTRAL_VALDAOSTA_CONNECTOR_DOWNLOAD_ERROR = "HIS_CENTRAL_VALDAOSTA_CONNECTOR_DOWNLOAD_ERROR";

    /**
     * 
     */

    private int maxRecords;

    public static String BEARER_TOKEN = null;

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

	JSONObject organizationInfo = getInfo(ORGANIZATION_URL);
	JSONObject parameterInfo = getInfo(PARAMETERS);
	JSONObject stations = getStationsList();

	if (stations != null) {
	    JSONArray stationsArray = stations.optJSONArray("stations");

	    if (stationsArray != null) {

		maxRecords = stationsArray.length();
		getSetting().getMaxRecords().ifPresent(v -> maxRecords = v);
		for (int i = 0; i < maxRecords; i++) {

		    JSONObject datasetMetadata = stationsArray.getJSONObject(i);
		    String id = datasetMetadata.optString("station-id");
		    if (id != null && !id.isEmpty()) {
			JSONObject parameterObject = getParameters(id);

			if (parameterObject != null) {
			    JSONObject stationObject = parameterObject.optJSONObject("station");
			    if (stationObject != null) {
				JSONArray parameterArray = stationObject.optJSONArray("parameters");
				if (parameterArray != null) {
				    for (int j = 0; j < parameterArray.length(); j++) {
					JSONObject sensorInfo = parameterArray.getJSONObject(j);
					ret.addRecord(HISCentralValdaostaMapper.create(datasetMetadata, sensorInfo, organizationInfo, parameterInfo));
				    }

				}
			    }
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

    private JSONObject getInfo(String param) throws GSException {
	// e.g. curl -H "Authorization: Bearer $JWT_TOKEN" https://cf-api.regione.vda.it/ws2/stations | jq
	String url = getSourceURL() + param;

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);
	
//	HttpGet get = new HttpGet(url.trim());
//	get.addHeader("Authorization", "Bearer " + BEARER_TOKEN);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

//	    RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
//		    .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
//	    CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
//	    CloseableHttpResponse getStationResponse = client.execute(get);
//	    stream = getStationResponse.getEntity().getContent();
//	    	    
	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);
	    
	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));

	    stream = getStationResponse.body();
	    
	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    if (stream != null) {
		JSONObject jsonResult = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return jsonResult;
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
		    HIS_CENTRAL_VALDAOSTA_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    private JSONObject getParameters(String id) throws GSException {
	// e.g: curl -H "Authorization: Bearer $JWT_TOKEN" https://cf-api.regione.vda.it/ws2/stations/1000 | jq

	String url = getSourceURL() + STATIONS_URL + "/" + id;

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);
	
//	HttpGet get = new HttpGet(url.trim());
//	get.addHeader("Authorization", "Bearer " + BEARER_TOKEN);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

//	    RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
//		    .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
//	    CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
//	    CloseableHttpResponse getStationResponse = client.execute(get);
//	    stream = getStationResponse.getEntity().getContent();
//	    	    
	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);
	    
	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));

	    stream = getStationResponse.body();
	    	    
	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    if (stream != null) {
		JSONObject jsonResult = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return jsonResult;
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
		    HIS_CENTRAL_VALDAOSTA_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    private JSONObject getStationsList() throws GSException {
	// e.g. curl -H "Authorization: Bearer $JWT_TOKEN" https://cf-api.regione.vda.it/ws2/stations | jq
	String url = getSourceURL() + STATIONS_URL;

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);
	
//	HttpGet get = new HttpGet(url.trim());
//	get.addHeader("Authorization", "Bearer " + BEARER_TOKEN);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

//	    RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
//		    .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
//	    CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
//	    CloseableHttpResponse getStationResponse = client.execute(get);
//	    stream = getStationResponse.getEntity().getContent();
	    
	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);
	    
	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));

	    stream = getStationResponse.body();
	        
	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    if (stream != null) {
		JSONObject jsonResult = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return jsonResult;
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
		    HIS_CENTRAL_VALDAOSTA_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    public static String getBearerToken(String baseUrl) {

	GSLoggerFactory.getLogger(HISCentralValdaostaConnector.class).info("Getting BEARER TOKEN from Valle d'Aosta CFVDA service");

//	StringEntity input = null;
	String token = null;

	try {
//	    HttpPost httpPost = new HttpPost(TOKEN_URL);
//	    HttpClient httpClient = HttpClientBuilder.create().build();
//
//	    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
//	    params.add(new BasicNameValuePair("email", CLIENT_USER));
//	    params.add(new BasicNameValuePair("password", CLIENT_SECRET));
//
//	    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//
//	    HttpResponse response = null;
//	    	    	    
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("email", ConfigurationWrapper.getCredentialsSetting().getAostaClientId().orElse(null));
	    params.put("password", ConfigurationWrapper.getCredentialsSetting().getAostaClientPassword().orElse(null));

	    Downloader downloader = new Downloader();
	    
	    HttpResponse<InputStream> response = downloader.downloadResponse(HttpRequestUtils.build(
		    MethodWithBody.POST,//
		    TOKEN_URL,//
		    params ));
	    

 	    JSONObject result = new JSONObject(IOStreamUtils.asUTF8String(response.body()));
	    // result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
	    if (result != null) {
		token = result.optString("token");
		GSLoggerFactory.getLogger(HISCentralValdaostaConnector.class).info("BEARER TOKEN obtained: " + BEARER_TOKEN);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(HISCentralValdaostaConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}
	return token;

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_VALDAOSTA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("cf-api.regione.vda.it");
    }

    @Override
    protected HISCentralValdaostaConnectorSetting initSetting() {

	return new HISCentralValdaostaConnectorSetting();
    }

    public static void main(String[] args) throws Exception {

//	HttpPost httpPost = new HttpPost(TOKEN_URL);
//	HttpClient httpClient = HttpClientBuilder.create().build();
//
//	List<NameValuePair> params = new ArrayList<NameValuePair>(2);
//
//	params.add(new BasicNameValuePair("username", CLIENT_USER));
//	params.add(new BasicNameValuePair("password", CLIENT_SECRET));

	// params.add(new BasicNameValuePair("refresh_token",
	// "CfDJ8M4beJMvqa9Hn7wcS6XbAQVPympbQWe1dsG-jsqRW3580XldR0A33uyASg97uYWr2xbhbhv4NMXyVHY_QFU4fAtfdnTMgtaY_Hp0mtk6verPtQ95OjpQ1H39qCFHUzSAvo2zqDQM7GBW5CCCqgducyF_gUOuass0Ea-LX-cbpLXI7zVDay2D8axM52tDrPSEpZqCnGD-gZ8H37V-tLmkbOyfsFr5upEEti5R3_MH8ZDegRN_navJYQCqYr-QH4Hpd8DqmaIFWjKIRqEBLPkl8sAOma4I9Q1NfgCXouV5C8M_mycXEz-CHfBnWJG2S_4JFSztBJfLUxtqeZufPebrW_pPNRnx9NwOL6U66A755oKSXL0k7ceWHN_2kfMMbwtfDb_QM8i9yBMIhXAfTUNdhabPZEm6nlTbJ9igMGI2B9gLDxtRyCjGwHwyGEvibOjKsTkJ-AvKmrVxMcih9TzxB590791XG5bJmf-1-Nniuo2PCuKksPpvO2xxUPlo-P72fwnrY4yDo9cHUqs_ajHnG50yb8RGnxIRZXUoCErc2Y8yPgy-vXJ2vF1TkUkWOmjmxr2bGUMezMeEzxnfSrN3HpDwmO2mR7dEO9KBk2uvO8_oLMVDrdObVx6a4MVZZm3wkNvRUoZD6XoU7hKE9Kb13BFUP7S5AmbnVHmcZwUmnocnyt39Ta8e_ln8x9oBRcvFt31uqmdFSgZ8rdmOt5nkatS5tMOSG-69z35cz2jRRpFUv77E9wHN8wHg4urmESjS03qFvUaZdd7Uu1VYqToUeeJ_I0biNlioQVVRbvPAvLsg_sGsS1EY3ByqpM94yrLw2o8JqYc9_SvFTX0wK5eOb_9uJQStlv7WPHFjHYb44FwKrr7kZ1B4D9hlEOYezakPOw"));

//	httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//	System.out.println(params);

//	HttpResponse response = httpClient.execute(httpPost);
		
	HashMap<String, String> params = new HashMap<String, String>();
	params.put("username", ConfigurationWrapper.getCredentialsSetting().getAostaClientId().orElse(null));
	params.put("password", ConfigurationWrapper.getCredentialsSetting().getAostaClientPassword().orElse(null));
	
	System.out.println(params);
	 
	HttpRequest request = HttpRequestUtils.build(//
		MethodWithBody.POST, //
		TOKEN_URL, //
		params);
	
	HttpResponse<InputStream> response = new Downloader().downloadResponse(request);

	JSONObject obj = new JSONObject(IOStreamUtils.asUTF8String(response.body()));

	if (obj != null) {
	    String bearer = obj.optString("token");
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
