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
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
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

    private static final int TOKEN_ACQUIRE_MAX_ATTEMPTS = 100;

    private static final long TOKEN_ACQUIRE_RETRY_WAIT_MS = 5000L;

    private final String datascapeRoot;
    private GSLogger logger;
    private static String giProxyEndpoint = null;

    /**
     * Normalizes the configured source URL to the Datascape <strong>root</strong> (no trailing slash), without
     * {@code /v1}. Accepts either {@code http://host/datascape}, {@code http://host/datascape/}, or
     * {@code http://host/datascape/v1/}.
     */
    public static String normalizeDatascapeRoot(String configuredUrl) {

	String root = configuredUrl.trim();
	while (root.endsWith("/")) {
	    root = root.substring(0, root.length() - 1);
	}
	if (root.endsWith("/v1")) {
	    root = root.substring(0, root.length() - 3);
	    while (root.endsWith("/")) {
		root = root.substring(0, root.length() - 1);
	    }
	}
	return root;
    }

    public static String apiV1BaseFromConfiguredUrl(String configuredUrl) {

	return normalizeDatascapeRoot(configuredUrl) + "/v1/";
    }

    /**
     * {@code true} if {@code linkage} is a data download URL under this source's {@code /v1/data/} API.
     */
    public static boolean matchesDownloadLinkage(String linkage, String configuredSourceUrl) {

	if (linkage == null || configuredSourceUrl == null) {
	    return false;
	}
	String prefix = apiV1BaseFromConfiguredUrl(configuredSourceUrl) + "data/";
	return linkage.startsWith(prefix);
    }

    public static String getGiProxyEndpoint() {
	if (giProxyEndpoint == null) {
	    giProxyEndpoint = ConfigurationWrapper.getSystemSettings().getProxyEndpoint().orElse(null);
	}
	return giProxyEndpoint;
    }

    public static void setGiProxyEndpoint(String endpoint) {
	giProxyEndpoint = endpoint;
    }
    
    /**
     * @param configuredSourceUrl connector endpoint: Datascape root ({@code .../datascape}) or API base
     *        ({@code .../datascape/v1/}); see {@link #normalizeDatascapeRoot(String)}
     */
    public HISCentralLazioClient(String configuredSourceUrl) {

	this.datascapeRoot = normalizeDatascapeRoot(configuredSourceUrl);
	this.logger = GSLoggerFactory.getLogger(HISCentralLazioClient.class);
    }

    public String getDatascapeRoot() {

	return datascapeRoot;
    }

    public String getTokenUrl() {

	return datascapeRoot + "/connect/token";
    }

    public String getRevokeSsoTokensUrl() {

	return datascapeRoot + "/connect/revoke-sso-tokens";
    }

    private String apiV1Base() {

	return datascapeRoot + "/v1/";
    }

    /**
     * Full HTTP URL for an API request: either {@code pathOrUrl} if it is already absolute, or {@code apiV1Base() +}
     * relative path (e.g. {@code stations?...}).
     */
    private String fullRequestUrl(String pathOrUrl) {

	String p = pathOrUrl.trim();
	if (p.startsWith("http://") || p.startsWith("https://")) {
	    return p;
	}
	return apiV1Base() + p;
    }

    public String getData(String path) throws GSException {

	return getResponse(path);

    }

    public String getStations(String path) throws GSException {

	return getResponse(path);

    }
    
    /**
     * Single token request (no retry). Prefer {@link #getResponse(String)} / instance methods for harvest, which
     * acquire, use, and revoke tokens.
     */
    public static String getBearerToken(String tokenPath) throws GSException {

	return requestAccessTokenFromEndpoint(tokenPath);
    }

    private static String requestAccessTokenFromEndpoint(String tokenPath) throws GSException {

	String token = null;

	try {

	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("username", ConfigurationWrapper.getCredentialsSetting().getLazioClientUser().orElse(null));
	    params.put("password", ConfigurationWrapper.getCredentialsSetting().getLazioClientPassword().orElse(null));
	    params.put("grant_type", "password");
	    params.put("client_id", ConfigurationWrapper.getCredentialsSetting().getLazioClientId().orElse(null));
	    params.put("client_instance", ConfigurationWrapper.getCredentialsSetting().getLazioClientInstance().orElse(null));

	    String postUrl = tokenPath;
	    String proxyEndpoint = getGiProxyEndpoint();
	    if (proxyEndpoint != null) {
		postUrl = proxyEndpoint + "/post?url=" + URLEncoder.encode(tokenPath, "UTF-8");
	    }

	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    postUrl, //
		    params);

	    Downloader downloader = new Downloader();

	    downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
	    downloader.setResponseTimeout(TimeUnit.SECONDS, 5);
	    HttpResponse<InputStream> response = downloader.downloadResponse(request);

	    JSONObject result = new JSONObject(IOStreamUtils.asUTF8String(response.body()));

	    if (result != null) {
		token = result.optString("access_token");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(HISCentralLazioConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}
	return token;
    }

    private String acquireBearerTokenOnce() throws GSException {

	return requestAccessTokenFromEndpoint(getTokenUrl());
    }

    private String acquireBearerTokenWithRetries() throws GSException {

	for (int attempt = 1; attempt <= TOKEN_ACQUIRE_MAX_ATTEMPTS; attempt++) {

	    String token = acquireBearerTokenOnce();
	    if (token != null && !token.isEmpty()) {
		return token;
	    }
	    logger.warn("Lazio token acquisition failed (attempt {}/{}), waiting {} ms before retry", //
		    attempt, TOKEN_ACQUIRE_MAX_ATTEMPTS, TOKEN_ACQUIRE_RETRY_WAIT_MS);
	    if (attempt < TOKEN_ACQUIRE_MAX_ATTEMPTS) {
		try {
		    Thread.sleep(TOKEN_ACQUIRE_RETRY_WAIT_MS);
		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		    throw GSException.createException(//
			    getClass(), //
			    "Interrupted while waiting to retry Lazio token acquisition", //
			    null, //
			    ErrorInfo.ERRORTYPE_SERVICE, //
			    ErrorInfo.SEVERITY_ERROR, //
			    LAZIO_REMOTE_SERVICE_ERROR);
		}
	    }
	}
	throw GSException.createException(//
		getClass(), //
		"Unable to acquire Lazio bearer token after " + TOKEN_ACQUIRE_MAX_ATTEMPTS + " attempts", //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		LAZIO_REMOTE_SERVICE_ERROR);
    }

    private void revokeAccessToken(String accessToken) {

	if (accessToken == null || accessToken.isEmpty()) {
	    return;
	}
	String revokeUrl = getRevokeSsoTokensUrl();
	try {
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("accessToken", accessToken);

	    String postUrl = revokeUrl;
	    String proxyEndpoint = getGiProxyEndpoint();
	    if (proxyEndpoint != null) {
		postUrl = proxyEndpoint + "/post?url=" + URLEncoder.encode(revokeUrl, "UTF-8");
	    }

	    HttpRequest request = HttpRequestUtils.build(MethodWithBody.POST, postUrl, params);

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
	    downloader.setResponseTimeout(TimeUnit.SECONDS, 5);
	    downloader.downloadResponse(request);
	} catch (Exception e) {
	    logger.warn("Lazio revoke-sso-tokens failed: {}", e.getMessage());
	}
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
	String token = acquireBearerTokenWithRetries();
	try {

	    String parameter = nearRealTimeData ? apiV1Base().trim() + "&date_from=" + startTime + "&date_to=" + endTime
		    : apiV1Base().trim() + "&data_min=" + startTime + "&data_max=" + endTime + "&format=json";
	    parameter = URLEncoder.encode(parameter, "UTF-8");

	    String url = parameter;

	    String proxyEndpoint = getGiProxyEndpoint();
	    if (proxyEndpoint != null) {
		url = proxyEndpoint + "/get?url=" + url;
	    }

	    logger.info("Sending request to: {}", url);

	    Map<String, String> headers = new HashMap<String, String>();
	    headers.put("accept", "application/json");
	    headers.put("Authorization", "Bearer " + token);

	    HttpResponse<InputStream> response = new Downloader()
		    .downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url, HttpHeaderUtils.build(headers)));

	    InputStream input = response.body();

	    ByteArrayOutputStream output = new ByteArrayOutputStream();

	    IOUtils.copy(input, output);

	    input.close();

	    ret = new String(output.toByteArray());

	    if (ret.contains("Too many requests, please try again later") //
		    || ret.contains("504 Gateway Time-out")//

	    ) {
		logger.warn("Invalid response: {}", ret);
		ret = null;

	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    revokeAccessToken(token);
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
	    String token = null;
	    try {
		token = acquireBearerTokenWithRetries();
		try {

		    String parameter = fullRequestUrl(path);
		    parameter = URLEncoder.encode(parameter, "UTF-8");

		    String url = parameter;

		    String proxyEndpoint = getGiProxyEndpoint();
		    if (proxyEndpoint != null) {
			url = proxyEndpoint + "/get?url=" + url;
		    }

		    logger.info("Sending request to: {}", url);

		    int timeout = 120;
		    int responseTimeout = 200;

		    Downloader downloader = new Downloader();
		    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
		    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);

		    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
			    url.trim(), //
			    HttpHeaderUtils.build("Authorization", "Bearer " + token));

		    InputStream stream = getStationResponse.body();

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

		} finally {
		    revokeAccessToken(token);
		}

	    } catch (GSException e) {
		throw e;
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    logger.info("Waiting 40s...(left #{} tries}", tries);
	    try {
		Thread.sleep(40000);
	    } catch (InterruptedException e) {
		Thread.currentThread().interrupt();
		throw GSException.createException( //
			getClass(), //
			"Interrupted while waiting to retry Lazio request", //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			LAZIO_REMOTE_SERVICE_ERROR //
		);
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
