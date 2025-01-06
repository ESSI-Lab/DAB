/**
 *
 */
package eu.essi_lab.accessor.saeon;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
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
 * @author Fabrizio
 */
public class SAEONConnector extends HarvestedQueryConnector<SAEONConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "SAEONConnector";

    /**
     *
     */
    private int recordsCount;

    private static final String SAEON_CONNECTOR_DOWNLOAD_ERROR = "SAEON_CONNECTOR_DOWNLOAD_ERROR";

    /**
     * In the API V2 it is required
     */
    // private static final String BEARER_TOKEN =
    // "0kCjPHBPlQQJiIXT3Y14_qCxHKumC4i0WTyTqofGM4M.fAqUuOfnL6XYxF4kqJ9JBf67gsfnsefUxEz02B7OvmY";

    private static final String TOKEN_REQUEST_URL = "https://auth.odp.saeon.ac.za/oauth2/token";
    private static final String SCOPE = "odp.catalog:read";

    private String BEARER_TOKEN = null;
    // https://proto.saeon.ac.za/api/catalog/SAEON/records

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://proto.saeon.ac.za/api/catalog/SAEON/records");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	String page = "1";

	if (request.getResumptionToken() != null) {

	    page = request.getResumptionToken();
	}

	String url = getSourceURL() + "page=" + page;
	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);
	// HttpGet get = new HttpGet(url.trim());

	// add authorization token
	if (BEARER_TOKEN == null) {
	    BEARER_TOKEN = getBearerToken();
	}

	// get.addHeader("Authorization", "Bearer " + BEARER_TOKEN);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

	    // RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
	    // .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
	    // CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	    // CloseableHttpResponse saeonResponse = client.execute(get);
	    // stream = saeonResponse.getEntity().getContent();

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);

	    HttpResponse<InputStream> saeonResponse = downloader.downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));

	    stream = saeonResponse.body();

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
		    SAEON_CONNECTOR_DOWNLOAD_ERROR);
	}

	// String query = "https://proto.saeon.ac.za/api/catalog/SAEON/records?page=72"

	if (stream != null) {
	    try {

		Optional<Integer> maxRecords = getSetting().getMaxRecords();

		ClonableInputStream clone = new ClonableInputStream(stream);

		List<String> results = getResultList(clone);
		for (String result : results) {

		    recordsCount++;

		    if (getSetting().isMaxRecordsUnlimited() || (maxRecords.isPresent() && recordsCount <= maxRecords.get())) {

			OriginalMetadata metadata = new OriginalMetadata();

			metadata.setSchemeURI(SAEONMapper.SAEON_SCHEME_URI);
			metadata.setMetadata(result);

			response.addRecord(metadata);
		    }
		}

		GSLoggerFactory.getLogger(getClass()).debug("Current records count: {}", recordsCount);

		// try again, probably the token is expired
		if (BEARER_TOKEN == null) {
		    response.setResumptionToken(String.valueOf(Integer.valueOf(page)));
		    if (stream != null) {
			stream.close();
		    }
		    return response;
		}

		boolean hasResults = hasResults(clone);

		if (hasResults && (getSetting().isMaxRecordsUnlimited() || (maxRecords.isPresent() && recordsCount <= maxRecords.get()))) {
		    response.setResumptionToken(String.valueOf(Integer.valueOf(page) + 1));
		} else {
		    response.setResumptionToken(null);
		    BEARER_TOKEN = null;
		}
		if (stream != null) {
		    stream.close();
		}
	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		BEARER_TOKEN = null;
	    }
	} else {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve page {}", page);
	    BEARER_TOKEN = null;
	}

	return response;
    }

    private String getBearerToken() {


	GSLoggerFactory.getLogger(getClass()).info("Getting BEARER TOKEN from SAEON service");

	String clientID = ConfigurationWrapper.getCredentialsSetting().getSAEONUser().orElse(null);
	String clientSescret = ConfigurationWrapper.getCredentialsSetting().getSAEONPassword().orElse(null);

	String base64Credentials = Base64.getEncoder().encodeToString((clientID + ":" + clientSescret).getBytes());

	HashMap<String, String> hashMap = new HashMap<>();
	hashMap.put("Content-Type", "application/x-www-form-urlencoded");
	hashMap.put("Authorization", "Basic " + base64Credentials);

	String result = null;
	String token = null;

	try {

	    String grant_type = "client_credentials";

	    HttpRequest httpPost = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    TOKEN_REQUEST_URL,
		    "grant_type=" + grant_type + "&scope=" + SCOPE, //
		    hashMap);

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(httpPost);

	    result = IOUtils.toString(response.body(), "UTF-8");

	    GSLoggerFactory.getLogger(getClass()).info("RESPONSE FROM SAEON " + result);
	    if (result != null && !result.isEmpty()) {
		JSONObject obj = new JSONObject(result);
		token = obj.optString("access_token");
		GSLoggerFactory.getLogger(getClass()).info("BEARER TOKEN obtained: " + token);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}
	return token;
    }

    public List<String> getResultList(ClonableInputStream stream) throws Exception {

	ArrayList<String> out = Lists.newArrayList();
	JSONObject obj = new JSONObject(IOStreamUtils.asUTF8String(stream.clone()));

	JSONArray array = obj.optJSONArray("items");
	// JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(stream.clone()));
	if (array == null) {
	    GSLoggerFactory.getLogger(getClass()).info("ERROR getting items.");
	    BEARER_TOKEN = null;
	} else {

	    for (int i = 0; i < array.length(); i++) {

		JSONObject object = array.getJSONObject(i);
		out.add(object.toString());
	    }
	}

	return out;
    }

    public boolean hasResults(ClonableInputStream stream) throws Exception {

	JSONObject obj = new JSONObject(IOStreamUtils.asUTF8String(stream.clone()));

	JSONArray array = obj.optJSONArray("items");
	if (array == null) {
	    BEARER_TOKEN = null;
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

	return Arrays.asList(SAEONMapper.SAEON_SCHEME_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected SAEONConnectorSetting initSetting() {

	return new SAEONConnectorSetting();
    }
}
