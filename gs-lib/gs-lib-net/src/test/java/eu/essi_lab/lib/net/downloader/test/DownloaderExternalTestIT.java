package eu.essi_lab.lib.net.downloader.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.failsafe.FailsafeException;
import dev.failsafe.RetryPolicy;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * @author Fabrizio
 */
public class DownloaderExternalTestIT {

    @Test
    public void defaultValuesTest() {

	Downloader downloader = new Downloader();

	long connectionTimeout = downloader.getConnectionTimeout();
	Assert.assertEquals(TimeUnit.SECONDS.toMillis(30), connectionTimeout);

	long responseTimeout = downloader.getResponseTimeout();
	Assert.assertEquals(TimeUnit.SECONDS.toMillis(60), responseTimeout);

	Version httpVersion = downloader.getHttpVersion();
	Assert.assertEquals(Version.HTTP_1_1, httpVersion);

	Assert.assertNull(downloader.getRetryPolicy());
	
	Redirect redirectStrategy = downloader.getRedirectStrategy();
	Assert.assertEquals(Redirect.NORMAL, redirectStrategy);
    }

    @Test
    public void setValuesTest() {

	Downloader downloader = new Downloader();

	downloader.setConnectionTimeout(TimeUnit.DAYS, 1);
	Assert.assertEquals(TimeUnit.DAYS.toMillis(1), downloader.getConnectionTimeout());

	downloader.setResponseTimeout(TimeUnit.HOURS, 5);
	Assert.assertEquals(TimeUnit.HOURS.toMillis(5), downloader.getResponseTimeout());

	downloader.setHttpVersion(Version.HTTP_2);
	Assert.assertEquals(Version.HTTP_2, downloader.getHttpVersion());

	Assert.assertEquals(Version.HTTP_2, downloader.getHttpVersion());

	RetryPolicy<HttpResponse<InputStream>> retryPolicy = RetryPolicy.<HttpResponse<InputStream>> //
		builder().build();

	downloader.setRetryPolicy(retryPolicy);
	Assert.assertEquals(retryPolicy, downloader.getRetryPolicy());
    
	downloader.setRedirectStrategy(Redirect.NEVER);
	Redirect redirectStrategy = downloader.getRedirectStrategy();
	Assert.assertEquals(Redirect.NEVER, redirectStrategy);
    }

    private int retry;
    private boolean onRetriesExceed;

    @Before
    public void before() {

	retry = 0;
	onRetriesExceed = false;
    }

    @Test
    public void retryTest1_1() throws IOException, InterruptedException, URISyntaxException {

	Downloader downloader = new Downloader();

	RetryPolicy<HttpResponse<InputStream>> retryPolicy = RetryPolicy.<HttpResponse<InputStream>> //
		builder().//
		handleResultIf(r -> r.statusCode() != 200).//
		withDelay(Duration.ofSeconds(1)).//
		withMaxAttempts(3).//
		onRetry(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failure #{}. Retrying...", e.getAttemptCount());
		    retry++;
		}).//
		onRetriesExceeded(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failed to connect. Max retries exceeded");
		    onRetriesExceed = true;
		}).//
		build();

	downloader.setConnectionTimeout(TimeUnit.SECONDS, 1);
	downloader.setRetryPolicy(retryPolicy);

	try {
	    downloader.downloadResponse("http://unknownhost");
	} catch (FailsafeException ex) {

	    Throwable cause = ex.getCause();
	    Assert.assertEquals(ConnectException.class, cause.getClass());
	}

	Assert.assertEquals(2, retry);
	Assert.assertTrue(onRetriesExceed);
    }

    @Test
    public void retryTest1_2() throws IOException, InterruptedException, URISyntaxException {

	Downloader downloader = new Downloader();

	RetryPolicy<HttpResponse<InputStream>> retryPolicy = RetryPolicy.<HttpResponse<InputStream>> //
		builder().//
		handleResultIf(r -> r.statusCode() != 200).//
		withDelay(Duration.ofSeconds(1)).//
		withMaxAttempts(3).//
		onRetry(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failure #{}. Retrying...", e.getAttemptCount());
		    retry++;
		}).//
		onRetriesExceeded(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failed to connect. Max retries exceeded");
		    onRetriesExceed = true;
		}).//
		build();

	downloader.setConnectionTimeout(TimeUnit.SECONDS, 1);
	downloader.setRetryPolicy(retryPolicy);

	try {

	    Optional<InputStream> downloadResponse = downloader.downloadOptionalStream("http://unknownhost");

	    Assert.assertTrue(downloadResponse.isEmpty());

	} catch (FailsafeException ex) {

	    fail("FailsafeException thrown!");
	}

	Assert.assertEquals(2, retry);
	Assert.assertTrue(onRetriesExceed);
    }

    @Test
    public void retryTest1_3() throws IOException, InterruptedException, URISyntaxException {

	Downloader downloader = new Downloader();

	downloader.setConnectionTimeout(TimeUnit.SECONDS, 1);
	downloader.setRetryPolicy(3, TimeUnit.SECONDS, 1);

	try {

	    Optional<InputStream> downloadResponse = downloader.downloadOptionalStream("http://unknownhost");

	    Assert.assertTrue(downloadResponse.isEmpty());

	} catch (FailsafeException ex) {

	    fail("FailsafeException thrown!");
	}
    }

    @Test
    public void retryTest2_1() throws IOException, InterruptedException, URISyntaxException {

	Downloader downloader = new Downloader();

	RetryPolicy<HttpResponse<InputStream>> retryPolicy = RetryPolicy.<HttpResponse<InputStream>> //
		builder().//
		handleResultIf(r -> r.statusCode() == 401).//
		withDelay(Duration.ofSeconds(1)).//
		withMaxAttempts(3).//
		onRetry(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failure #{}. Retrying...", e.getAttemptCount());
		    retry++;
		}).//
		onRetriesExceeded(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failed to connect. Max retries exceeded");
		    onRetriesExceed = true;
		}).//
		build();

	downloader.setConnectionTimeout(TimeUnit.SECONDS, 1);
	downloader.setRetryPolicy(retryPolicy);

	try {
	    HttpResponse<InputStream> downloadResponse = downloader.downloadResponse("https://postman-echo.com/basic-auth");
	    Assert.assertEquals(401, downloadResponse.statusCode());

	} catch (FailsafeException ex) {

	    fail("FailsafeException thrown!");
	}

	Assert.assertEquals(2, retry);
	Assert.assertTrue(onRetriesExceed);
    }

    @Test
    public void retryTest2_2() throws IOException, InterruptedException, URISyntaxException {

	Downloader downloader = new Downloader();

	RetryPolicy<HttpResponse<InputStream>> retryPolicy = RetryPolicy.<HttpResponse<InputStream>> //
		builder().//
		handleResultIf(r -> r.statusCode() == 401).//
		withDelay(Duration.ofSeconds(1)).//
		withMaxAttempts(3).//
		onRetry(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failure #{}. Retrying...", e.getAttemptCount());
		    retry++;
		}).//
		onRetriesExceeded(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failed to connect. Max retries exceeded");
		    onRetriesExceed = true;
		}).//
		build();

	downloader.setConnectionTimeout(TimeUnit.SECONDS, 1);
	downloader.setRetryPolicy(retryPolicy);

	try {
	    Optional<InputStream> downloadResponse = downloader.downloadOptionalStream("https://postman-echo.com/basic-auth");

	    Assert.assertTrue(downloadResponse.isPresent());

	    InputStream inputStream = downloadResponse.get();

	    Assert.assertEquals("Unauthorized", IOStreamUtils.asUTF8String(inputStream));

	} catch (FailsafeException ex) {

	    fail("FailsafeException thrown!");
	}

	Assert.assertEquals(2, retry);
	Assert.assertTrue(onRetriesExceed);
    }

    @Test
    public void retryTest3_1() throws IOException, InterruptedException, URISyntaxException {

	Downloader downloader = new Downloader();

	RetryPolicy<HttpResponse<InputStream>> retryPolicy = RetryPolicy.<HttpResponse<InputStream>> //
		builder().//
		handleResultIf(r -> r.statusCode() == 200).//
		withDelay(Duration.ofSeconds(1)).//
		withMaxAttempts(3).//
		onRetry(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failure #{}. Retrying...", e.getAttemptCount());
		    retry++;
		}).//
		onRetriesExceeded(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failed to connect. Max retries exceeded");
		    onRetriesExceed = true;
		}).//
		build();

	downloader.setConnectionTimeout(TimeUnit.SECONDS, 1);
	downloader.setRetryPolicy(retryPolicy);

	try {
	    HttpResponse<InputStream> downloadResponse = downloader.downloadResponse("https://postman-echo.com/basic-auth");
	    Assert.assertEquals(401, downloadResponse.statusCode());

	} catch (FailsafeException ex) {

	    fail("FailsafeException thrown!");
	}

	Assert.assertEquals(0, retry);
	Assert.assertFalse(onRetriesExceed);
    }

    @Test
    public void retryTest3_2() throws IOException, InterruptedException, URISyntaxException {

	Downloader downloader = new Downloader();

	RetryPolicy<HttpResponse<InputStream>> retryPolicy = RetryPolicy.<HttpResponse<InputStream>> //
		builder().//
		handleResultIf(r -> r.statusCode() == 200).//
		withDelay(Duration.ofSeconds(1)).//
		withMaxAttempts(3).//
		onRetry(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failure #{}. Retrying...", e.getAttemptCount());
		    retry++;
		}).//
		onRetriesExceeded(e -> {
		    GSLoggerFactory.getLogger(getClass()).warn("Failed to connect. Max retries exceeded");
		    onRetriesExceed = true;
		}).//
		build();

	downloader.setConnectionTimeout(TimeUnit.SECONDS, 1);
	downloader.setRetryPolicy(retryPolicy);

	try {
	    Optional<InputStream> downloadResponse = downloader.downloadOptionalStream("https://postman-echo.com/basic-auth");

	    Assert.assertTrue(downloadResponse.isPresent());

	    InputStream inputStream = downloadResponse.get();

	    Assert.assertEquals("Unauthorized", IOStreamUtils.asUTF8String(inputStream));

	} catch (FailsafeException ex) {

	    fail("FailsafeException thrown!");
	}

	Assert.assertEquals(0, retry);
	Assert.assertFalse(onRetriesExceed);
    }

    @Test
    public void optionalStringTest() {

	Downloader downloader = new Downloader();

	Optional<String> optString = downloader.downloadOptionalString("https://postman-echo.com/get?testKey=testValue");

	Assert.assertTrue(optString.isPresent());

	String stringResponse = optString.get();

	testUrl(stringResponse);
    }

    @Test
    public void optionalStreamTest() throws IOException {

	Downloader downloader = new Downloader();

	Optional<InputStream> optString = downloader.downloadOptionalStream("https://postman-echo.com/get?testKey=testValue");

	Assert.assertTrue(optString.isPresent());

	testUrl(IOUtils.toString(optString.get(), StandardCharsets.UTF_8));
    }

    @Test
    public void responseTest() throws IOException, InterruptedException, URISyntaxException {

	{

	    Downloader downloader = new Downloader();

	    HttpResponse<InputStream> response = downloader.downloadResponse(//
		    "https://postman-echo.com/get?testKey=testValue");

	    testUrl(IOUtils.toString(response.body(), StandardCharsets.UTF_8));
	}

	{
	    Downloader downloader = new Downloader();

	    HttpResponse<InputStream> response = downloader.downloadResponse(//
		    HttpRequestUtils.build(MethodNoBody.GET, //
			    "https://postman-echo.com/get?testKey=testValue"));

	    testUrl(IOUtils.toString(response.body(), StandardCharsets.UTF_8));
	}
    }

    @Test
    public void optionalResponseTest() throws IOException {

	Downloader downloader = new Downloader();

	Optional<HttpResponse<InputStream>> optionalResponse = downloader
		.downloadOptionalResponse("https://postman-echo.com/get?testKey=testValue");

	Assert.assertTrue(optionalResponse.isPresent());

	testUrl(IOUtils.toString(optionalResponse.get().body(), StandardCharsets.UTF_8));
    }

    @Test
    public void responseWithUrlEncodedRequestTest() throws IOException, URISyntaxException, FailsafeException, InterruptedException {

	Downloader downloader = new Downloader();

	String testParam = "testParam";
	String testParamValue = "testParamValue";

	HashMap<String, String> params = new HashMap<>();
	params.put(testParam, testParamValue);

	HttpRequest request = HttpRequestUtils.build(//
		MethodWithBody.POST, //
		"https://postman-echo.com/post", //
		params);

	HttpResponse<InputStream> response = downloader.downloadResponse(request);

	String stringResponse = IOUtils.toString(response.body(), StandardCharsets.UTF_8);

	JSONObject jsonObject = new JSONObject(stringResponse);

	JSONObject formObject = jsonObject.getJSONObject("form");

	boolean hasTestParam = formObject.has(testParam);
	assertTrue(hasTestParam);

	assertEquals(testParamValue, formObject.getString(testParam));
    }

    @Test
    public void optionalResponseWithHeadersTest() throws IOException {

	Downloader downloader = new Downloader();

	String header = "testheader";
	String value = "testValue";

	HttpHeaders httpHeaders = HttpHeaderUtils.build(//
		header, //
		value); //

	Optional<HttpResponse<InputStream>> optionalResponse = downloader.downloadOptionalResponse(//
		"https://postman-echo.com/headers", //
		httpHeaders);

	Assert.assertTrue(optionalResponse.isPresent());

	String stringResponse = IOUtils.toString(optionalResponse.get().body(), StandardCharsets.UTF_8);

	testHeadersReponse(stringResponse, header, value);
    }

    @Test
    public void optionalStringWithHeadersTest() throws Exception {

	Downloader downloader = new Downloader();

	String header = "testheader";
	String value = "testValue";

	HttpHeaders httpHeaders = HttpHeaderUtils.build(//
		header, //
		value); //

	Optional<String> optString = downloader.downloadOptionalString(//
		"https://postman-echo.com/headers", //
		httpHeaders);//

	testHeadersReponse(optString.get(), header, value);
    }

    @Test
    public void optionalStreamWithHeadersTest() throws Exception {

	Downloader downloader = new Downloader();

	String header = "testheader";
	String value = "testValue";

	HttpHeaders httpHeaders = HttpHeaderUtils.build(//
		header, //
		value); //

	Optional<InputStream> optStream = downloader.downloadOptionalStream("https://postman-echo.com/headers", //
		httpHeaders);//

	Assert.assertTrue(optStream.isPresent());

	testHeadersReponse(IOUtils.toString(optStream.get(), StandardCharsets.UTF_8), header, value);
    }

    @Test
    public void responseWithBasicAuthTest() throws IOException, InterruptedException, URISyntaxException {

	Downloader downloader = new Downloader();

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.GET, "https://postman-echo.com/basic-auth");

	HttpResponse<InputStream> response = downloader.downloadResponse(request);

	assertEquals(401, response.statusCode());

	{
	    response = downloader.downloadResponse(request, "postman", "password");

	    assertEquals(200, response.statusCode());
	}

	{

	    response = downloader.downloadResponse("https://postman-echo.com/basic-auth", "postman", "password");

	    assertEquals(200, response.statusCode());
	}
    }

    /**
     * @param stringResponse
     * @return
     */
    private void testUrl(String stringResponse) {

	JSONObject jsonObject = new JSONObject(stringResponse);

	String url = jsonObject.getString("url");

	assertEquals("https://postman-echo.com/get?testKey=testValue", url);
    }

    /**
     * @param stringResponse
     * @param header
     * @param value
     */
    private void testHeadersReponse(String stringResponse, String header, String value) {

	JSONObject jsonObject = new JSONObject(stringResponse);

	JSONObject headersObject = jsonObject.getJSONObject("headers");

	boolean hasTestHeader = headersObject.has(header);
	assertTrue(hasTestHeader);

	assertEquals(value, headersObject.getString(header));
    }
}
