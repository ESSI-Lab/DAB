package eu.essi_lab.accessor.hiscentral.sicilia;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.CredentialsSetting;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the CAE Datascape (AEGIS) REST API of the Protezione Civile Regione Siciliana, e.g.
 * https://www.protezionecivilesicilia.it:8443/DatascapeA. Credentials are read from the {@link CredentialsSetting}.
 *
 * @author Roberto
 */
public class HISCentralSiciliaClient {

    private static final String SICILIA_REMOTE_SERVICE_ERROR = "SICILIA_REMOTE_SERVICE_ERROR";

    static final String ELEMENT_FIELDS = "elementId,elementName,stationId,stationName,quantity,measUnit,startDate,endDate,dtr,"
	    + "isVirtual,basin,river";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")
	    .withZone(ZoneOffset.UTC);

    private static final long TOKEN_EXPIRATION_MARGIN_MS = TimeUnit.SECONDS.toMillis(60);

    private record AccessToken(String value, long expiresAt) {

	boolean isValid() {
	    return System.currentTimeMillis() < expiresAt;
	}
    }

    private static final Map<String, AccessToken> TOKENS = new ConcurrentHashMap<>();

    private static CredentialsSetting credentialsOverride;

    private final String root;

    /**
     * @param endpoint the Datascape root (e.g. https://host:8443/DatascapeA), with or without trailing slash or
     *        {@code /v3} suffix
     */
    public HISCentralSiciliaClient(String endpoint) {

	this.root = normalizeRoot(endpoint);
    }

    /**
     * Uses the given credentials instead of the ones of the {@link ConfigurationWrapper} (e.g. in standalone tests)
     */
    public static void setCredentials(CredentialsSetting credentials) {

	credentialsOverride = credentials;
	TOKENS.clear();
    }

    public static String normalizeRoot(String endpoint) {

	String ret = endpoint.trim();
	while (ret.endsWith("/")) {
	    ret = ret.substring(0, ret.length() - 1);
	}
	if (ret.endsWith("/v3")) {
	    ret = ret.substring(0, ret.length() - 3);
	}
	return ret;
    }

    /**
     * @return the base of the data download linkages: {@code <root>/v3/data-combo/}
     */
    public static String getDataLinkagePrefix(String endpoint) {

	return normalizeRoot(endpoint) + "/v3/data-combo/";
    }

    /**
     * @return the stations as returned by {@code /v3/locations} (i: id, n: name, o: owner, x: lon, y: lat, z: altitude)
     */
    public JSONArray getStations() throws GSException {

	return new JSONArray(get(root + "/v3/locations?category=All"));
    }

    /**
     * @return all the elements (sensors) with the {@link #ELEMENT_FIELDS}
     */
    public JSONArray getElements() throws GSException {

	StringBuilder url = new StringBuilder(root + "/v3/elements?category=All&ui_culture=it");
	for (String field : ELEMENT_FIELDS.split(",")) {
	    url.append("&field=").append(field);
	}
	return new JSONArray(get(url.toString()));
    }

    /**
     * @param elementId
     * @param begin
     * @param end
     * @return the plausible data as an array of [epoch milliseconds, value] pairs
     */
    public JSONArray getData(String elementId, Instant begin, Instant end) throws GSException {

	String url = getDataLinkagePrefix(root) + elementId + //
		"?from=" + encode(DATE_FORMATTER.format(begin)) + //
		"&to=" + encode(DATE_FORMATTER.format(end)) + //
		"&basicType=Plausible&part=EpochTime&part=ValueWithInvalid&timing=Original&loadAlsoExtemp=false";

	JSONObject response = new JSONObject(get(url));
	return Optional.ofNullable(response.optJSONArray("plausibleData")).orElse(new JSONArray());
    }

    /**
     * Executes an authenticated GET; in case of 401 the token is acquired again and the request repeated once
     */
    String get(String url) throws GSException {

	for (int attempt = 0; attempt < 2; attempt++) {

	    String token = getToken();

	    try {

		GSLoggerFactory.getLogger(getClass()).info("Sending request to: {}", url);

		Downloader downloader = new Downloader();
		downloader.setConnectionTimeout(TimeUnit.SECONDS, 60);
		downloader.setResponseTimeout(TimeUnit.SECONDS, 300);

		HttpResponse<InputStream> response = downloader.downloadResponse(//
			HttpRequestUtils.build(//
				MethodNoBody.GET, //
				url, //
				HttpHeaderUtils.build("Authorization", "Bearer " + token)));

		String body = IOStreamUtils.asUTF8String(response.body());

		if (response.statusCode() == 200) {
		    return body;
		}

		if (response.statusCode() == 401) {
		    GSLoggerFactory.getLogger(getClass()).warn("Unauthorized, acquiring a new token");
		    TOKENS.remove(root);
		    continue;
		}

		throw error("Request " + url + " failed with status " + response.statusCode() + ": " + body, null);

	    } catch (GSException e) {
		throw e;
	    } catch (Exception e) {
		throw error("Request " + url + " failed: " + e.getMessage(), e);
	    }
	}

	throw error("Request " + url + " unauthorized", null);
    }

    private String getToken() throws GSException {

	AccessToken token = TOKENS.get(root);
	if (token != null && token.isValid()) {
	    return token.value();
	}

	CredentialsSetting credentials = credentialsOverride != null ? credentialsOverride
		: ConfigurationWrapper.getCredentialsSetting();

	HashMap<String, String> params = new HashMap<>();
	params.put("grant_type", "password");
	params.put("username", credentials.getSiciliaClientUser().orElseThrow(() -> missingCredential("siciliaUsername")));
	params.put("password", credentials.getSiciliaClientPassword().orElseThrow(() -> missingCredential("siciliaPassword")));
	params.put("client_id", credentials.getSiciliaClientId().orElseThrow(() -> missingCredential("siciliaId")));
	params.put("client_instance", credentials.getSiciliaClientInstance().orElseThrow(() -> missingCredential("siciliaInstance")));

	try {

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	    downloader.setResponseTimeout(TimeUnit.SECONDS, 60);

	    HttpResponse<InputStream> response = downloader.downloadResponse(//
		    HttpRequestUtils.build(MethodWithBody.POST, root + "/connect/token", params));

	    String body = IOStreamUtils.asUTF8String(response.body());

	    if (response.statusCode() != 200) {
		throw error("Token request failed with status " + response.statusCode() + ": " + body, null);
	    }

	    JSONObject json = new JSONObject(body);
	    long expiresIn = TimeUnit.SECONDS.toMillis(json.optLong("expires_in", 300));

	    token = new AccessToken(json.getString("access_token"),
		    System.currentTimeMillis() + Math.max(0, expiresIn - TOKEN_EXPIRATION_MARGIN_MS));

	    TOKENS.put(root, token);

	    return token.value();

	} catch (GSException e) {
	    throw e;
	} catch (Exception e) {
	    throw error("Token request failed: " + e.getMessage(), e);
	}
    }

    private static String encode(String value) {

	return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private GSException missingCredential(String key) {

	return error("Missing HIS-Central Sicilia credential: " + key, null);
    }

    private GSException error(String message, Exception cause) {

	GSLoggerFactory.getLogger(getClass()).error(message);

	return GSException.createException(//
		getClass(), //
		message, //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		SICILIA_REMOTE_SERVICE_ERROR, //
		cause);
    }
}
