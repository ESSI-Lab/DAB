package eu.essi_lab.accessor.meteotracker;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Roberto
 */
public class MeteoTrackerAccessorExternalTestIT {

    @Test
    public void getPOSTHTTPTokenTest() throws Exception {
	String CLIENT_ID = System.getProperty("meteotracker.user");
	String CLIENT_SECRET = System.getProperty("meteotracker.password");
	String TOKEN_REQUEST_URL = "https://app.meteotracker.com/auth/login/api";

	HashMap<String, String> params = new HashMap<String, String>();
	params.put("email", CLIENT_ID);
	params.put("password", CLIENT_SECRET);

	HttpRequest request = HttpRequestUtils.build(//
		MethodWithBody.POST, //
		TOKEN_REQUEST_URL, //
		params);

	HttpResponse<InputStream> response = null;
	try {
	    response = new Downloader().downloadResponse(request);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	String result = null;
	try {
	    result = IOUtils.toString(response.body(), "UTF-8");
	    if (result != null && !result.isEmpty()) {
		JSONObject obj = new JSONObject(result);
		String token = obj.optString("accessToken");
		System.out.println(token);
		assertTrue(token != null);
	    }

	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void checkForDuplicates() throws Exception {

	String CLIENT_ID = System.getProperty("meteotracker.user");
	String CLIENT_SECRET = System.getProperty("meteotracker.password");
	String TOKEN_REQUEST_URL = "https://app.meteotracker.com/auth/login/api";

	HashMap<String, String> params = new HashMap<String, String>();
	params.put("email", CLIENT_ID);
	params.put("password", CLIENT_SECRET);

	HttpRequest request = HttpRequestUtils.build(//
		MethodWithBody.POST, //
		TOKEN_REQUEST_URL, //
		params);

	HttpResponse<InputStream> response = null;
	try {
	    response = new Downloader().downloadResponse(request);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	String result = null;
	String token = null;
	try {
	    result = IOUtils.toString(response.body(), "UTF-8");
	    if (result != null && !result.isEmpty()) {
		JSONObject obj = new JSONObject(result);
		token = obj.optString("accessToken");
		System.out.println(token);
		assertTrue(token != null);
	    }

	} catch (IOException e) {
	    e.printStackTrace();
	}

	int page = 0;
	Map<String, Integer> map = new HashMap<String, Integer>();

	boolean finished = false;
	boolean finished2 = false;
	boolean isDuplicateFound = false;
	while (!finished) {

	    String url = "https://app.meteotracker.com/api/sessions?by=_living_lab&reverseSort=true&page=" + page + "&items=1000";

	    HttpResponse<InputStream> resp2 = null;

	    InputStream stream = null;
	    try {

		resp2 = new Downloader().//
			downloadResponse(//
				url.trim(), //
				HttpHeaderUtils.build("Authorization", "Bearer " + token));

		// RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
		// .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
		// CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		// CloseableHttpResponse meteoTrackerResponse = client.execute(get);
		int statusCode = resp2.statusCode();

		if (statusCode > 400) {
		    // MeteoTrackerConnector.refreshBearerToken();

		    resp2 = new Downloader().//
			    downloadResponse(//
				    url.trim(), //
				    HttpHeaderUtils.build("Authorization", "Bearer " + token));
		}
		stream = resp2.body();

		GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("Got " + url);

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(MeteoTrackerConnector.class).error("Unable to retrieve " + url);
	    }

	    if (stream != null) {
		page++;
		ClonableInputStream clone = new ClonableInputStream(stream);

		JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

		if (array == null || array.length() == 0)
		    finished = true;

		for (int i = 0; i < array.length(); i++) {

		    JSONObject object = array.getJSONObject(i);
		    String id = object.optString("_id");
		    if (map.containsKey(id)) {
			map.put(id, map.get(id) + 1);
			System.out.println("DULPICATED: " + id);
			isDuplicateFound = true;
		    } else {
			map.put(id, 1);
		    }

		}
	    }

	    if (stream != null) {
		stream.close();
	    }

	}

	page = 0;
	System.out.println("MAP SIZE: " + map.size());
	while (!finished2) {
	    String url = "https://app.meteotracker.com/api/sessions?by=i-change&reverseSort=true&page=" + page + "&items=1000";

	    // HttpGet get = new HttpGet(url.trim());
	    // get.addHeader("Authorization", "Bearer " + token);

	    int timeout = 120;
	    int responseTimeout = 200;
	    InputStream stream = null;
	    try {

		// RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
		// .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
		// CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		// CloseableHttpResponse meteoTrackerResponse = client.execute(get);
		// stream = meteoTrackerResponse.getEntity().getContent();
		//
		Downloader downloader = new Downloader();
		downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
		downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);

		HttpResponse<InputStream> meteoTrackerResponse = downloader.downloadResponse(url.trim(),
			HttpHeaderUtils.build("Authorization", "Bearer " + token));

		stream = meteoTrackerResponse.body();

		GSLoggerFactory.getLogger(MeteoTrackerConnector.class).info("Got " + url);

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(MeteoTrackerConnector.class).error("Unable to retrieve " + url);
	    }

	    if (stream != null) {
		page++;
		ClonableInputStream clone = new ClonableInputStream(stream);

		JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(clone.clone()));

		if (array == null || array.length() == 0)
		    finished2 = true;

		for (int i = 0; i < array.length(); i++) {

		    JSONObject object = array.getJSONObject(i);
		    String id = object.optString("_id");
		    if (map.containsKey(id)) {
			map.put(id, map.get(id) + 1);
			System.out.println("DULPICATED: " + id);
			isDuplicateFound = true;
		    } else {
			map.put(id, 1);
		    }

		}
	    }

	    if (stream != null) {
		stream.close();
	    }

	}

	Assert.assertTrue(!isDuplicateFound);
	System.out.println("MAP SIZE: " + map.size());
    }

}
