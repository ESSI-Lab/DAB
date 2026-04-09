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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.model.ratings.RatingCurve;
import eu.essi_lab.model.ratings.RatingCurvePoint;
import eu.essi_lab.model.ratings.RatingCurves;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class HISCentralLazioClient {

    private static final String LAZIO_REMOTE_SERVICE_ERROR = "LAZIO_REMOTE_SERVICE_ERROR";
    private static final String LAZIO_CLIENT_UNABLE_TO_GET_STATIONS_ERROR = "LAZIO_CLIENT_UNABLE_TO_GET_STATIONS_ERROR";

    
    public static String BEARER_TOKEN = null;

    public static String REFRESH_BEARER_TOKEN = null;
    
    public static final String BASE_URL = "http://rlazio.dynalias.org/datascape/v1/";

    public static final String TOKEN_URL = "http://rlazio.dynalias.org/datascape/connect/token";
    
    private String endpoint;
    private GSLogger logger;
    private static String giProxyEndpoint = null;

    public static String getGiProxyEndpoint() {
	if (giProxyEndpoint == null) {
	    giProxyEndpoint = ConfigurationWrapper.getSystemSettings().getProxyEndpoint().orElse(null);
	}
	return giProxyEndpoint;
    }

    public static void setGiProxyEndpoint(String endpoint) {
	giProxyEndpoint = endpoint;
    }
    
    public HISCentralLazioClient() {

	this(BASE_URL);
	
    }

    public HISCentralLazioClient(String endpoint) {

	this.endpoint = endpoint;
	this.logger = GSLoggerFactory.getLogger(HISCentralLazioClient.class);
    }

    public String getData(String path) throws GSException {

	return getResponse(path);

    }

    public String getStations(String path) throws GSException {

	return getResponse(path);

    }
    
    public static String getBeareToken(String tokenPath) throws GSException {

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
		    tokenPath, //
		    params);

	    Downloader downloader = new Downloader();

	    downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
	    downloader.setResponseTimeout(TimeUnit.SECONDS, 5);
	    HttpResponse<InputStream> response = downloader.downloadResponse(request);

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


    /**
     * Returns available data for the given station (it is allowed to get data only from the latest 5 days)
     * 
     * @param startTime
     * @param endTime
     * @return
     * @throws GSException
     */
    public String getLastData(String startTime, String endTime, boolean nearRealTimeData) throws GSException {
	String ret = null;
	try {

	    String parameter = nearRealTimeData ? endpoint.trim() + "&date_from=" + startTime + "&date_to=" + endTime :  endpoint.trim() + "&data_min=" + startTime + "&data_max=" + endTime + "&format=json";
	    parameter = URLEncoder.encode(parameter, "UTF-8");

	    String url = parameter;

	    String proxyEndpoint = getGiProxyEndpoint();
	    if (proxyEndpoint != null) {
		url = proxyEndpoint + "/get?url=" + url;
	    }

	    logger.info("Sending request to: {}", url);

	    HttpResponse<InputStream> response = new Downloader()
		    .downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url, HttpHeaderUtils.build("accept", "application/json")));

	    InputStream input = response.body();

	    ByteArrayOutputStream output = new ByteArrayOutputStream();

	    IOUtils.copy(input, output);

	    input.close();

	    ret = new String(output.toByteArray());

	    if (ret.contains("Too many requests, please try again later") //
		    || ret.contains("504 Gateway Time-out")//

	    ) {
		logger.warn("Invalid response: {}", ret);

	    } else {
		// responseCache.put(path, ret);
		return ret;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return ret;
    }

    private static ExpiringCache<String> responseCache;

    static {
	responseCache = new ExpiringCache<>();
	responseCache.setDuration(1000 * 60 * 30l);
	responseCache.setMaxSize(500);
    }

    public synchronized String getResponse(String path) throws GSException {

	String ret = responseCache.get(path);
	if (ret != null) {
	    return ret;
	}

	int tries = 20;
	do {
	    try {

		String parameter = endpoint.trim() + path;
		parameter = URLEncoder.encode(parameter, "UTF-8");

		String url = parameter;

		String proxyEndpoint = getGiProxyEndpoint();
		if (proxyEndpoint != null) {
		    url = proxyEndpoint + "/get?url=" + url;
		}

		logger.info("Sending request to: {}", url);
		
		int timeout = 120;
		int responseTimeout = 200;
		InputStream stream = null;
		

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

//		    GSLoggerFactory.getLogger(getClass()).info("Got " + url);
//
//		    if (stream != null) {
//			JSONArray arrayResult = new JSONArray(IOStreamUtils.asUTF8String(stream));
//			stream.close();
//			return arrayResult;
//		    }
//
//		
//		
//		
//		
//
//		HttpResponse<InputStream> response = new Downloader().downloadResponse(
//			HttpRequestUtils.build(MethodNoBody.GET, url, HttpHeaderUtils.build("accept", "application/json")));
//
//		InputStream input = response.body();

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		IOUtils.copy(stream, output);

		stream.close();

		ret = new String(output.toByteArray());

		if (ret.contains("Too many requests, please try again later") //
			|| ret.contains("504 Gateway Time-out")//

		) {
		    logger.warn("Invalid response: {}", ret);

		} else {
		    responseCache.put(path, ret);
		    return ret;
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    logger.info("Waiting 40s...(left #{} tries}", tries);
	    try {
		Thread.sleep(40000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	} while (tries-- > 0);

	throw GSException.createException( //
		getClass(), //
		"Remote service issue", //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		LAZIO_REMOTE_SERVICE_ERROR //
	);
    }

    
    

}
