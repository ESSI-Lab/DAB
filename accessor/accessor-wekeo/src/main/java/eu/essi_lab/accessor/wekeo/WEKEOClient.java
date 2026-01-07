package eu.essi_lab.accessor.wekeo;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.model.exceptions.GSException;

public class WEKEOClient {

    /**
     * get token request
     * https://wekeo-broker.apps.mercator.dpi.wekeo.eu/databroker/gettoken
     */

    /**
     * get collections request
     * https://wekeo-broker.apps.mercator.dpi.wekeo.eu/databroker/datasets?q=&size=1000&page=0
     */

    /**
     * get metadata request
     * https://wekeo-broker.apps.mercator.dpi.wekeo.eu/databroker/datasets/{id}
     * e.g. {id} = EO:ECMWF:DAT:ERA5_HOURLY_VARIABLES_ON_PRESSURE_LEVELS
     */

    private String endpoint = "https://wekeo-broker.apps.mercator.dpi.wekeo.eu/databroker/";

    private Logger logger;

    public WEKEOClient() {
	this.logger = GSLoggerFactory.getLogger(getClass());
    }

    private String user;
    private String password;

    public String getUser() {
	return user;
    }

    public void setUser(String user) {
	this.user = user;
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public String getUserCredentials() {
	return user + ":" + password;
    }

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

    public String getToken() throws GSException {

	String request = getEndpoint() + "gettoken";

	String token = null;

	try {

	    logger.info("Getting token: " + request);

	    token = "Basic " + new String(Base64.getEncoder().encode(getUserCredentials().getBytes()));

	    InputStream stream = getRequestStream(request, token);

	    JSONObject jsonObject = JSONUtils.fromStream(stream);

	    token = getString(jsonObject, "access_token");

	    logger.info("Received access token: " + token);

	    if (stream != null)
		stream.close();

	    return token;

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }

    private InputStream getRequestStream(String request, String token) {

	try {
	    URL tokenURL = new URL(request);
	    HttpURLConnection tokenURLConnection = (HttpURLConnection) tokenURL.openConnection();

	    tokenURLConnection.setRequestProperty("Authorization", token);
	    tokenURLConnection.setRequestMethod("GET");
	    tokenURLConnection.setUseCaches(false);
	    tokenURLConnection.setDoInput(true);
	    tokenURLConnection.setDoOutput(true);

	    return tokenURLConnection.getInputStream();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }

    public List<String> getCollections(String token) {
	String request = getEndpoint() + "datasets?q=&size=1000&page=0";
	List<String> ret = new ArrayList<>();

	try {

	    boolean completed = false;
	    while (!completed) {
		logger.info("Getting list fo collection identifiers: " + request);

		InputStream stream = getRequestStream(request, token);

		if (stream == null) {
		    throw new RuntimeException("Error downloading List of identifier");
		}

		logger.info("Received identifiers: " + request);

		JSONObject jsonObject = JSONUtils.fromStream(stream);

		JSONArray jsonArray = getJSONArray(jsonObject, "content");

		for (int i = 0; i < jsonArray.length(); i++) {
		    JSONObject obj = jsonArray.getJSONObject(i);
		    String identifier = obj.get("datasetId").toString();
		    if (!ret.contains(identifier))
			ret.add(identifier);
		    // System.out.println(identifier);
		}
		
		String nextURL = jsonObject.optString("nextPage");
		if(nextURL == null || nextURL == "null" || nextURL.isEmpty()) {
		    completed = true;
		}else {
		    request = nextURL;
		}

		if (stream != null)
		    stream.close();
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
	return ret;
    }

    public String getMetadataCollection(String identifier, String token) {
	String request = getEndpoint() + "datasets/" + identifier;

	String ret = null;

	try {
	    logger.info("Getting Metadata for collection {}: {}", identifier, request);

	    InputStream stream = getRequestStream(request, token);

	    if (stream == null) {
		throw new RuntimeException("Error downloading List of identifier");
	    }

	    logger.info("Received metadata for collection {}: {}", identifier, request);

	    // JSONObject jsonObject = JSONUtils.fromStream(stream);

	    ret = IOStreamUtils.asUTF8String(stream);

	    if (stream != null)
		stream.close();

	    return ret;

	} catch (Exception e) {
	    e.printStackTrace();
	}
	return ret;
    }

    public static String getString(JSONObject result, String key) {
	try {
	    String ret = result.optString(key, null);
	    if (ret == null || "".equals(ret) || "[]".equals(ret) || "null".equals(ret)) {
		return null;
	    }
	    return ret;
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(WEKEOClient.class).warn("Error reading key {}: ", key, e);
	    return null;
	}
    }

    public JSONArray getJSONArray(JSONObject result, String key) {
	try {
	    boolean hasKey = result.has(key);
	    if (!hasKey) {
		return new JSONArray();
	    }
	    JSONArray ret = result.getJSONArray(key);
	    if (ret == null || ret.length() == 0) {
		ret = new JSONArray();
	    }
	    return ret;
	} catch (Exception e) {
	    logger.warn("Error getting json array", e);
	    return new JSONArray();
	}

    }

    private boolean isJSON(String text) {
	try {
	    new JSONObject(text);
	} catch (JSONException ex) {
	    // e.g. in case JSONArray is valid as well...
	    try {
		new JSONArray(text);
	    } catch (JSONException ex1) {
		return false;
	    }
	}
	return true;
    }

    public static void main(String[] args) throws Exception {

	// GET TOKEN CODE OK

	// String serviceURL = "https://wekeo-broker.apps.mercator.dpi.wekeo.eu/databroker/gettoken";
	// URL myURL = new URL(serviceURL);
	// HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
	//
	// String userCredentials = "the secret token";
	//
	// String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
	//
	// myURLConnection.setRequestProperty("Authorization", basicAuth);
	// myURLConnection.setRequestMethod("GET");
	// myURLConnection.setUseCaches(false);
	// myURLConnection.setDoInput(true);
	// myURLConnection.setDoOutput(true);
	//
	// InputStream is = myURLConnection.getInputStream();
	//
	// ClonableInputStream cis = new ClonableInputStream(is);
	//
	// String result = IOUtils.toString(cis.clone(), StandardCharsets.UTF_8.name());
	//
	// System.out.println(result);
	//
	// JSONObject jsonObject = JSONUtils.fromStream(cis.clone());
	//
	// String token = (String) jsonObject.get("access_token");
	//
	// System.out.println(token);

	String request = "https://wekeo-broker.apps.mercator.dpi.wekeo.eu/databroker/datasets?q=&size=1000&page=0";

	// logger.info("Getting token: " + request);
	URL tokenURL = new URL(request);
	HttpURLConnection tokenURLConnection = (HttpURLConnection) tokenURL.openConnection();

	// String basicAuth = "Basic " + new String(Base64.getEncoder().encode(USER_CREDENTIALS.getBytes()));

	tokenURLConnection.setRequestProperty("Authorization", "the token");
	tokenURLConnection.setRequestMethod("GET");
	tokenURLConnection.setUseCaches(false);
	tokenURLConnection.setDoInput(true);
	tokenURLConnection.setDoOutput(true);

	InputStream stream = tokenURLConnection.getInputStream();

	// HttpGet get = new HttpGet(request.trim());
	//
	// // add authorization token
	// get.addHeader("Authorization", token);
	//
	// int timeout = 10;
	// int responseTimeout = 20;
	// RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout *
	// 1000).setConnectionRequestTimeout(responseTimeout * 1000)
	// .setSocketTimeout(timeout * 1000).build();
	// CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	//
	// CloseableHttpResponse response = client.execute(get);
	//
	// InputStream stream = response.getEntity().getContent();

	if (stream == null) {
	    throw new RuntimeException("Error downloading SOS GetObservation from remote service");
	}

	// String ret = IOUtils.toString(stream, "UTF-8");
	// if (stream != null) {
	// stream.close();
	// }

	// if (isJSON(ret)) {
	// // error case
	// ret = buildErrorResponse(ret);
	// }

	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	//
	// IOUtils.copy(stream, baos);
	// stream.close();
	JSONObject jsonObject = JSONUtils.fromStream(stream);

	// baos.close();

	JSONArray jsonArray = jsonObject.getJSONArray("content");
	// System.out.println("SIZE: " + jsonArray.length());
	for (int i = 0; i < jsonArray.length(); i++) {
	    JSONObject obj = jsonArray.getJSONObject(i);
	    String identifier = obj.get("datasetId").toString();
	    System.out.println(identifier);
	}

    }

}
