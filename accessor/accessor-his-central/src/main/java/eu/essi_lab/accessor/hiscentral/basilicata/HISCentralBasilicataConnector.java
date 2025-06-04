package eu.essi_lab.accessor.hiscentral.basilicata;

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

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.cxf.helpers.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Roberto
 */
public class HISCentralBasilicataConnector extends HarvestedQueryConnector<HISCentralBasilicataConnectorSetting> {

    /**
     * 
     */
    static final String TYPE = "HISCentralBasilicataConnector";

    /**
     * 
     */
    public HISCentralBasilicataConnector() {

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

    public static final String BASE_URL = "http://rbasil.dynalias.org/Datascape/v1/";

    public static final String TOKEN_URL = "http://rbasil.dynalias.org/Datascape/connect/token";


    private static final String HIS_CENTRAL_BASILICATA_CONNECTOR_DOWNLOAD_ERROR = "HIS_CENTRAL_BASILICATA_CONNECTOR_DOWNLOAD_ERROR";

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

	try {

	    // add authorization token
	    String baseUrl = getSourceURL();
	    if (BEARER_TOKEN == null) {
		getBearerToken();
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
				ret.addRecord(HISCentralBasilicataMapper.create(datasetMetadata, sensorInfo));
			    }

			}

		    }
		}

	    } else {
		GSLoggerFactory.getLogger(getClass()).info("ERROR getting items.");
		BEARER_TOKEN = null;
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    BEARER_TOKEN = null;
	    refreshBearerToken();
	}

	return ret;
    }

    private JSONArray getMeasures(String id) throws Exception {
	// e.g:
	// http://rbasil.dynalias.org/datascape/v1/elements?category=All&category=All&station_id=177000&field=ElementName&field=ElementId&field=StationName&field=StartDate&field=EndDate&field=MeasUnit

	JSONArray arr = new JSONArray();
	String url = getSourceURL() + SENSOR_URL;
	url = url + "category=All&station_id=" + id
		+ "&field=ElementName&field=ElementId&field=StationName&field=StartDate&field=EndDate&field=MeasUnit";
	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);
	
//	HttpGet get = new HttpGet(url.trim());
//	get.addHeader("Authorization", "Bearer " + BEARER_TOKEN);
//
	InputStream stream = null;
	HttpResponse<InputStream> measureResponse = null;
//	HttpClient httpClient = HttpClientBuilder.create().build();
		
	Downloader downloader = new Downloader();
		
	int statusCode = -1;
	int tries = 0;
	try {
	    do {
		 measureResponse = downloader.downloadResponse(//
			url.trim(),//
			HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));
		
		statusCode = measureResponse.statusCode();
		if (statusCode != 200) {
		    // error try again with same token
		    Thread.sleep(2000);
		    tries++;
		}
		if (tries > 20)
		    break;
	    } while (statusCode != 200);

	    if (statusCode != 200) {
		// token expired - refresh token
		refreshBearerToken();
		do {
		    
//		    HttpGet newGet = new HttpGet(url.trim());
//		    newGet.addHeader("Authorization", "Bearer " + BEARER_TOKEN);
//		    measureResponse = httpClient.execute(newGet);
		    
		    measureResponse = downloader.downloadResponse(//
			    url.trim(), //
			    HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));
		    
		    statusCode = measureResponse.statusCode();

		    if (statusCode != 200) {
			Thread.sleep(2000);
			tries++;
		    }
		    if (tries > 20)
			break;
		} while (statusCode != 200);
	    }
	    if (statusCode != 200)
		return arr;

	    stream = measureResponse.body();
	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    BEARER_TOKEN = null;
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_BASILICATA_CONNECTOR_DOWNLOAD_ERROR);
	}

	if (stream != null) {
	    arr = new JSONArray(IOStreamUtils.asUTF8String(stream));
	    stream.close();
	    return arr;
	}

	return arr;
    }

    private JSONArray getStationsList() throws Exception {
	// e.g.
	// http://rbasil.dynalias.org/datascape/v1/stations?category=All&field=StationName&field=StationId&field=Locality&field=City&field=Prov&field=Longitude&field=Latitude&field=Altitude

	// field=StationId&field=CustomStationId&field=Basin&field=River&field=AlertZone&field=Time&field=StateId&
	// field=RegisterName&field=Address&field=Locality&field=City&field=Province&field=Prov&field=Region&field=Country&
	// field=Longitude&field=Latitude&field=Altitude&field=Gmt

	// StationName, StationId, Locality, City, Prov, Longitude, Latitude, Altitude
	JSONArray arr = new JSONArray();
	String url = getSourceURL() + STATIONS_URL;
	url = url
		+ "category=All&field=StationName&field=StationId&field=Locality&field=City&field=Prov&field=Longitude&field=Latitude&field=Altitude";
	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);
	
//	HttpGet get = new HttpGet(url.trim());
//
//	get.addHeader("Authorization", "Bearer " + BEARER_TOKEN);
//
	InputStream stream = null;
	ClonableInputStream cis = null;
//	HttpResponse stationResponse = null;
//	HttpClient httpClient = HttpClientBuilder.create().build();
	
	Downloader downloader = new Downloader();	
	HttpResponse<InputStream> stationResponse = null;
	
	int statusCode = -1;
	int tries = 0;

	try {

	    do {
//		stationResponse = httpClient.execute(get);
//		statusCode = stationResponse.getStatusLine().getStatusCode();
		
		stationResponse = downloader.downloadResponse(//
			url.trim(),//
			HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));
		
		statusCode = stationResponse.statusCode();
		
		if (statusCode != 200) {
		    // error try again with same token
		    Thread.sleep(2000);
		    tries++;
		}
		if (tries > 20)
		    break;
	    } while (statusCode != 200);

	    tries = 0;
	    if (statusCode != 200) {
		// token expired - refresh token
		refreshBearerToken();
		do {
//		    HttpGet newGet = new HttpGet(url.trim());
//		    newGet.addHeader("Authorization", "Bearer " + BEARER_TOKEN);
//		    stationResponse = httpClient.execute(newGet);
//		    statusCode = stationResponse.getStatusLine().getStatusCode();
		    		    
		    stationResponse = downloader.downloadResponse(//
				url.trim(),//
				HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));
			
		    statusCode = stationResponse.statusCode();		    
		    
		    if (statusCode != 200) {
			Thread.sleep(2000);
			tries++;
		    }
		    if (tries > 20)
			break;
		} while (statusCode != 200);
	    }
	    if (statusCode != 200)
		return arr;

	    stream = stationResponse.body();
	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    BEARER_TOKEN = null;
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_BASILICATA_CONNECTOR_DOWNLOAD_ERROR);
	}

	if (stream != null) {
	    cis = new ClonableInputStream(stream);
	    GSLoggerFactory.getLogger(getClass()).info("Stream result " + IOStreamUtils.asUTF8String(cis.clone()));
	    arr = new JSONArray(IOStreamUtils.asUTF8String(cis.clone()));
	    stream.close();
	    return arr;
	}

	return null;
    }

    public static void getBearerToken() {

	GSLoggerFactory.getLogger(HISCentralBasilicataConnector.class).info("Getting BEARER TOKEN from Basilicata Datascape service");

	try {
//	    HttpPost httpPost = new HttpPost(TOKEN_URL);
//	    HttpClient httpClient = HttpClientBuilder.create().build();
//
//	    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
//	    params.add(new BasicNameValuePair("username", CLIENT_USER));
//	    params.add(new BasicNameValuePair("password", CLIENT_SECRET));
//	    params.add(new BasicNameValuePair("grant_type", "password"));
//	    params.add(new BasicNameValuePair("client_id", CLIENT_ID));
//	    params.add(new BasicNameValuePair("client_instance", CLIENT_INSTANCE));
//	    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
	    
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("username", ConfigurationWrapper.getCredentialsSetting().getBasilicataClientUser().orElse(null));
	    params.put("password", ConfigurationWrapper.getCredentialsSetting().getBasilicataClientPassword().orElse(null));
	    params.put("grant_type", "password");
	    params.put("client_id", ConfigurationWrapper.getCredentialsSetting().getBasilicataClientId().orElse(null));
	    params.put("client_instance", ConfigurationWrapper.getCredentialsSetting().getBasilicataClientInstance().orElse(null));
	   	    
	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    TOKEN_URL,//
		    params);

	    HttpResponse<InputStream>  response = new Downloader().downloadResponse(request);
	    
	    JSONObject result = new JSONObject(IOStreamUtils.asUTF8String(response.body()));
	    // result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
	    if (result != null) {
		BEARER_TOKEN = result.optString("access_token");
		REFRESH_BEARER_TOKEN = result.optString("refresh_token");
		GSLoggerFactory.getLogger(HISCentralBasilicataConnector.class).info("BEARER TOKEN obtained: " + BEARER_TOKEN);
		GSLoggerFactory.getLogger(HISCentralBasilicataConnector.class).info("REFRESHING TOKEN obtained: " + REFRESH_BEARER_TOKEN);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(HISCentralBasilicataConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());

	}

    }

    public static void refreshBearerToken() {
	GSLoggerFactory.getLogger(HISCentralBasilicataConnector.class).info("Refreshing BEARER TOKEN from CAE service");
	try {
//	    HttpPost httpPost = new HttpPost(TOKEN_URL);
//	    HttpClient httpClient = HttpClientBuilder.create().build();
//	    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
//	    params.add(new BasicNameValuePair("username", CLIENT_USER));
//	    params.add(new BasicNameValuePair("password", CLIENT_SECRET));
//	    params.add(new BasicNameValuePair("grant_type", "password"));
//	    params.add(new BasicNameValuePair("client_id", CLIENT_ID));
//	    params.add(new BasicNameValuePair("client_instance", CLIENT_INSTANCE));
//	    params.add(new BasicNameValuePair("refresh_token", REFRESH_BEARER_TOKEN));
//	    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

//	    String result = null;
//	    String token = null;

//	    HttpResponse<InputStream> response = null;
//	    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//	    response = httpClient.execute(httpPost);
//	    int statusCode = response.getStatusLine().getStatusCode();	    
	    
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("username", ConfigurationWrapper.getCredentialsSetting().getBasilicataClientUser().orElse(null));
	    params.put("password", ConfigurationWrapper.getCredentialsSetting().getBasilicataClientId().orElse(null));
	    params.put("grant_type", "password");
	    params.put("client_id", ConfigurationWrapper.getCredentialsSetting().getBasilicataClientId().orElse(null));
	    params.put("client_instance", ConfigurationWrapper.getCredentialsSetting().getBasilicataClientInstance().orElse(null));
	    params.put("refresh_token", REFRESH_BEARER_TOKEN);
  
	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    TOKEN_URL,//
		    params);

	    HttpResponse<InputStream>  response = new Downloader().downloadResponse(request);
	    
	    int statusCode =  response.statusCode();
	    	    
	    if (statusCode > 400) {
		// token expired - refresh token
		getBearerToken();
	    }
	    String result = IOUtils.toString(response.body(), "UTF-8");
	    
	    GSLoggerFactory.getLogger(HISCentralBasilicataConnector.class).info("RESPONSE FROM CAE Basilicata " + result);
	    
	    if (result != null && !result.isEmpty()) {
		JSONObject obj = new JSONObject(result);
		BEARER_TOKEN = obj.optString("access_token");
		REFRESH_BEARER_TOKEN = obj.optString("refresh_token");
		GSLoggerFactory.getLogger(HISCentralBasilicataConnector.class).info("BEARER TOKEN obtained: " + BEARER_TOKEN);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(HISCentralBasilicataConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	}
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_BASILICATA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("rbasil.dynalias.org");
    }

    @Override
    protected HISCentralBasilicataConnectorSetting initSetting() {

	return new HISCentralBasilicataConnectorSetting();
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
