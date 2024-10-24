package eu.essi_lab.accessor.polytope;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.model.GSSource;

public class PolytopeConnectorTestExternalTestIT {

    private PolytopeConnector connector;
    private GSSource source;

    @Before
    public void init() {

	this.connector = new PolytopeConnector();
	this.source = Mockito.mock(GSSource.class);
    }

    @Test
    public void testPolytopeMetadataAPIRest() throws Exception {

	String username = System.getProperty("polytope.user");
	String psw = System.getProperty("polytope.password");
	String endpoint = "https://ionbeam-dev.ecmwf.int/api/v1/stations";

	JSONArray arr = new JSONArray();
	String url = endpoint;
	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	InputStream stream = null;

	Downloader downloader = new Downloader();

	HttpResponse<InputStream> stationResponse = null;

	int statusCode = -1;
	int tries = 0;
	do {

	    stationResponse = downloader.downloadResponse(url.trim(), HttpHeaderUtils.build("Authorization", "Bearer " + psw));

 	    statusCode = stationResponse.statusCode();
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
	    do {
		 		
		stationResponse = downloader.downloadResponse(//
			url.trim(),//
			HttpHeaderUtils.build("Authorization", "Bearer " + psw));
		
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
	    GSLoggerFactory.getLogger(getClass()).info("ERROR " + url);

	stream = stationResponse.body();
	GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	if (stream != null) {
	    ClonableInputStream cis = new ClonableInputStream(stream);
	    GSLoggerFactory.getLogger(getClass()).info(IOStreamUtils.asUTF8String(cis.clone()));
	    arr = new JSONArray(IOStreamUtils.asUTF8String(cis.clone()));
	    stream.close();
	}

	// InputStream postRequest =
	// PolytopeConnectorTestExternalTestIT.class.getClassLoader().getResourceAsStream("postRequest.json");
	//
	// HttpPost post = new HttpPost(endpoint);
	//
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	//
	// IOUtils.copy(postRequest, baos);
	//
	// ByteArrayEntity inputEntity = new ByteArrayEntity(baos.toByteArray());
	// inputEntity.setChunked(false);
	// post.setEntity(inputEntity);
	// post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
	// post.setHeader(HttpHeaders.AUTHORIZATION, "EmailKey " + username + ":" + psw);
	// System.out.println("Sending POLYTOPE Request to: " + endpoint);
	//
	// HttpRequestExecutor executor = new HttpRequestExecutor();
	// executor.setTimeout(10000);
	// HttpResponse response = executor.execute(post);
	//
	// InputStream output = response.getEntity().getContent();
	//
	// JSONObject jsonObject = JSONUtils.fromStream(output);
	//
	// Header[] location = response.getHeaders("Location");
	//
	// if (location != null) {
	// for (Header h : location) {
	// String loc = h.getValue();
	//
	// Map<String, String> headers = new HashMap<String, String>();
	// headers.put(HttpHeaders.AUTHORIZATION, "EmailKey " + username + ":" + psw);
	// // headers.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
	//
	// InputStream is;
	//
	// Downloader downloader = new Downloader();
	// downloader.setTimeout(120000);
	// downloader.setRequestHeaders(headers);
	// boolean skip = false;
	// while (!skip) {
	//
	// DownloaderResponse responseLoc = downloader.downloadResponse(loc);
	// if (responseLoc != null) {
	// int code = responseLoc.getResponseCode();
	// is = responseLoc.getResponseStream();
	// if (code == 200) {
	// skip = true;
	//
	// } else {
	// Thread.sleep(2000);
	// }
	// String res = IOStreamUtils.asUTF8String(is);
	//
	// System.out.println(res);
	//
	// }
	// }
	// }
	// }

    }

//    @Test
    public void testPolytopeAPIRest() throws Exception {

	String username = System.getProperty("polytope.user");
	String psw = System.getProperty("polytope.password");
	String endpoint = "https://polytope.ecmwf.int/api/v1/requests/ichange";

	InputStream body = PolytopeConnectorTestExternalTestIT.class.getClassLoader().getResourceAsStream("postRequest.json");

	// HttpPost post = new HttpPost(endpoint);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	IOUtils.copy(body, baos);

	// ByteArrayEntity inputEntity = new ByteArrayEntity(baos.toByteArray());
	// inputEntity.setChunked(false);
	// post.setEntity(inputEntity);
	//
	// post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
	// post.setHeader(HttpHeaders.AUTHORIZATION, "EmailKey " + username + ":" + psw);

	HashMap<String, String> headers = new HashMap<String, String>();
	headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
	headers.put(HttpHeaders.AUTHORIZATION, "EmailKey " + username + ":" + psw);

	System.out.println("Sending POLYTOPE Request to: " + endpoint);

	HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, endpoint, baos.toByteArray(), HttpHeaderUtils.build(headers));

	Downloader executor = new Downloader();
	executor.setConnectionTimeout(TimeUnit.SECONDS, 10);
	HttpResponse<InputStream> response = executor.downloadResponse(postRequest);

	InputStream output = response.body();

	JSONObject jsonObject = JSONUtils.fromStream(output);

	List<String> locations = response.headers().allValues("Location");

	for (String loc : locations) {

	    headers = new HashMap<String, String>();
	    headers.put(HttpHeaders.AUTHORIZATION, "EmailKey " + username + ":" + psw);
	    // headers.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");

	    InputStream is;

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MINUTES, 2);

	    boolean skip = false;
	    while (!skip) {

		HttpResponse<InputStream> responseLoc = downloader.downloadResponse(loc, HttpHeaderUtils.build(headers));

		if (responseLoc != null) {
		    int code = responseLoc.statusCode();
		    is = responseLoc.body();
		    if (code == 200) {
			skip = true;

		    } else {
			Thread.sleep(2000);
		    }
		    String res = IOStreamUtils.asUTF8String(is);

		    System.out.println(res);
		}
	    }
	}
    }

}
