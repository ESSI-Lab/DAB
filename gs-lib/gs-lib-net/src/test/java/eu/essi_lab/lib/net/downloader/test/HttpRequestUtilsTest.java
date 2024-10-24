package eu.essi_lab.lib.net.downloader.test;

import static java.nio.file.Files.createTempFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;

/**
 * @author Fabrizio
 */
public class HttpRequestUtilsTest {

    @Test
    public void fromRequestTest1() throws URISyntaxException {

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.GET, "http://test");

	Builder builder = HttpRequestUtils.fromRequest(request);

	Assert.assertEquals("GET", builder.build().method());

	Assert.assertEquals("http://test", builder.build().uri().toString());

	Assert.assertEquals(0, builder.build().headers().map().size());
    }

    @Test
    public void fromRequestTest2() throws URISyntaxException {

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.HEAD, "http://test");

	Builder builder = HttpRequestUtils.fromRequest(request);

	Assert.assertEquals("HEAD", builder.build().method());

	Assert.assertEquals("http://test", builder.build().uri().toString());

	Assert.assertEquals(0, builder.build().headers().map().size());

    }

    @Test
    public void fromRequestTest3() throws URISyntaxException {

	HttpHeaders httpHeaders = HttpHeaderUtils.build("h1", "v1");

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.HEAD, "http://test", httpHeaders);

	Builder builder = HttpRequestUtils.fromRequest(request);

	Assert.assertEquals("HEAD", builder.build().method());

	Assert.assertEquals("http://test", builder.build().uri().toString());

	Assert.assertEquals(httpHeaders, builder.build().headers());
    }

    @Test
    public void fromRequestTest4() throws URISyntaxException {

	HttpRequest request = HttpRequestUtils.build(MethodWithBody.POST, "http://test", "body");

	Builder builder = HttpRequestUtils.fromRequest(request);

	Assert.assertEquals("POST", builder.build().method());

	Assert.assertEquals("http://test", builder.build().uri().toString());

	Assert.assertEquals("body".length(), builder.build().bodyPublisher().get().contentLength());
    }

    /**
     * @throws URISyntaxException
     */
    @Test
    public void fromRequestTest5() throws URISyntaxException {

	Builder builder = HttpRequest.newBuilder();
	builder = builder.uri(new URI("http://test"));
	builder = builder.timeout(Duration.ofMinutes(5));
	builder = builder.version(Version.HTTP_2);

	HttpRequest httpRequest1 = builder.build();

	Builder builder2 = HttpRequestUtils.fromRequest(httpRequest1);
	HttpRequest httpRequest2 = builder2.build();

	Assert.assertEquals(httpRequest1.timeout().get(), httpRequest2.timeout().get());
	Assert.assertEquals(httpRequest1.version().get(), httpRequest2.version().get());
    }

    @Test
    public void fromRequestTest6() throws URISyntaxException {

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.GET, "http://test");

	Builder builder = HttpRequestUtils.fromRequest(request);

	Assert.assertTrue(builder.build().version().isEmpty());
    }

    @Test
    public void getRequestTest() throws URISyntaxException {

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.GET, "http://test");

	Assert.assertEquals("http://test", request.uri().toString());
	Assert.assertEquals("GET", request.method());

	Assert.assertEquals(0, request.bodyPublisher().get().contentLength());
    }

    @Test
    public void getRequestWithHeadersTest() throws URISyntaxException {

	HttpHeaders headers = HttpHeaderUtils.build("h1", "v1");

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.GET, "http://test", headers);

	Assert.assertEquals("http://test", request.uri().toString());
	Assert.assertEquals("GET", request.method());

	Assert.assertEquals(0, request.bodyPublisher().get().contentLength());

	HttpHeaders reqHeaders = request.headers();
	Assert.assertEquals(headers, reqHeaders);
    }

    @Test
    public void getRequestWithMapHeadersTest() throws URISyntaxException {

	HashMap<String, String> headers = new HashMap<String, String>();
	headers.put("h1", "v1");
	headers.put("h2", "v2");

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.GET, "http://test", headers);

	Assert.assertEquals("http://test", request.uri().toString());
	Assert.assertEquals("GET", request.method());

	Assert.assertEquals(0, request.bodyPublisher().get().contentLength());

	HttpHeaders reqHeaders = request.headers();

	Assert.assertEquals(headers.get(headers.keySet().toArray()[0]),
		reqHeaders.firstValue(headers.keySet().toArray()[0].toString()).get());
	Assert.assertEquals(headers.get(headers.keySet().toArray()[1]),
		reqHeaders.firstValue(headers.keySet().toArray()[1].toString()).get());

    }

    @Test(expected = URISyntaxException.class)
    public void getRequestTestWithException() throws URISyntaxException {

	HttpRequestUtils.build(MethodNoBody.GET, "://test");
    }

    @Test
    public void headRequestTest() throws URISyntaxException {

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.HEAD, "http://test");

	Assert.assertEquals("http://test", request.uri().toString());
	Assert.assertEquals("HEAD", request.method());

	Assert.assertEquals(0, request.bodyPublisher().get().contentLength());
    }

    @Test
    public void deleteRequestTest() throws URISyntaxException {

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.DELETE, "http://test");

	Assert.assertEquals("http://test", request.uri().toString());
	Assert.assertEquals("DELETE", request.method());

	Assert.assertEquals(0, request.bodyPublisher().get().contentLength());
    }

    //
    //
    //

    @Test
    public void postRequestNoHeadersTest() throws URISyntaxException, IOException {

	String body = "bodyString1";

	{
	    HttpRequest request = buildRequest(MethodWithBody.POST, body.getBytes(StandardCharsets.UTF_8), null, null);

	    requestBodyTest(MethodWithBody.POST, request, body, null, null);
	}

	{
	    HttpRequest request = buildRequest(MethodWithBody.POST, body, null, null);

	    requestBodyTest(MethodWithBody.POST, request, body, null, null);
	}

	{

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

	    HttpRequest request = buildRequest(MethodWithBody.POST, inputStream, null, null);

	    requestBodyTest(MethodWithBody.POST, request, body, null, null);
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.POST, tempFile, null, null);

	    requestBodyTest(MethodWithBody.POST, request, body, null, null);
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.POST, tempFile.toFile(), null, null);

	    requestBodyTest(MethodWithBody.POST, request, body, null, null);
	}

	{

	    HashMap<String, String> parameters = new HashMap<String, String>();
	    parameters.put("p1", "v1");
	    parameters.put("p2", "v2");

	    HttpRequest request = buildRequest(MethodWithBody.POST, parameters, null, null);

	    requestBodyTest(MethodWithBody.POST, request, HttpRequestUtils.toUrlEncodedForm(parameters), null, null);
	}

    }

    @Test
    public void putRequestNoHeadersTest() throws URISyntaxException, IOException {

	String body = "bodyString2";

	{
	    HttpRequest request = buildRequest(MethodWithBody.PUT, body.getBytes(StandardCharsets.UTF_8), null, null);

	    requestBodyTest(MethodWithBody.PUT, request, body, null, null);
	}

	{
	    HttpRequest request = buildRequest(MethodWithBody.PUT, body, null, null);

	    requestBodyTest(MethodWithBody.PUT, request, body, null, null);
	}

	{
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

	    HttpRequest request = buildRequest(MethodWithBody.PUT, inputStream, null, null);

	    requestBodyTest(MethodWithBody.PUT, request, body, null, null);
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.PUT, tempFile, null, null);

	    requestBodyTest(MethodWithBody.PUT, request, body, null, null);
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.PUT, tempFile.toFile(), null, null);

	    requestBodyTest(MethodWithBody.PUT, request, body, null, null);
	}

	{
	    HashMap<String, String> parameters = new HashMap<String, String>();
	    parameters.put("p1", "v1");
	    parameters.put("p2", "v2");

	    HttpRequest request = buildRequest(MethodWithBody.PUT, parameters, null, null);

	    requestBodyTest(MethodWithBody.PUT, request, HttpRequestUtils.toUrlEncodedForm(parameters), null, null);
	}
    }

    //
    //
    //

    @Test
    public void postRequestWithHeadersTest() throws URISyntaxException, IOException {

	String body = "bodyString3";

	{
	    HttpRequest request = buildRequest(MethodWithBody.POST, body.getBytes(StandardCharsets.UTF_8), null,
		    HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.POST, request, body, null, HttpHeaderUtils.build("h1", "v1"));
	}

	{
	    HttpRequest request = buildRequest(MethodWithBody.POST, body, null, HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.POST, request, body, null, HttpHeaderUtils.build("h1", "v1"));
	}

	{
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

	    HttpRequest request = buildRequest(MethodWithBody.POST, inputStream, null, HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.POST, request, body, null, HttpHeaderUtils.build("h1", "v1"));
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.POST, tempFile, null, HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.POST, request, body, null, HttpHeaderUtils.build("h1", "v1"));
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.POST, tempFile.toFile(), null, HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.POST, request, body, null, HttpHeaderUtils.build("h1", "v1"));
	}

	{
	    HashMap<String, String> parameters = new HashMap<String, String>();
	    parameters.put("p1", "v1");
	    parameters.put("p2", "v2");

	    HttpRequest request = buildRequest(MethodWithBody.POST, parameters, null, HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.POST, request, HttpRequestUtils.toUrlEncodedForm(parameters), null,
		    HttpHeaderUtils.build("h1", "v1"));
	}
    }

    @Test
    public void putRequestWithHeadersTest() throws URISyntaxException, IOException {

	String body = "bodyString4";

	{
	    HttpRequest request = buildRequest(MethodWithBody.PUT, body.getBytes(StandardCharsets.UTF_8), null,
		    HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.PUT, request, body, null, HttpHeaderUtils.build("h1", "v1"));
	}

	{
	    HttpRequest request = buildRequest(MethodWithBody.PUT, body, null, HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.PUT, request, body, null, HttpHeaderUtils.build("h1", "v1"));
	}

	{
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

	    HttpRequest request = buildRequest(MethodWithBody.PUT, inputStream, null, HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.PUT, request, body, null, HttpHeaderUtils.build("h1", "v1"));
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.PUT, tempFile, null, HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.PUT, request, body, null, HttpHeaderUtils.build("h1", "v1"));
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.PUT, tempFile.toFile(), null, HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.PUT, request, body, null, HttpHeaderUtils.build("h1", "v1"));
	}

	{
	    HashMap<String, String> parameters = new HashMap<String, String>();
	    parameters.put("p1", "v1");
	    parameters.put("p2", "v2");

	    HttpRequest request = buildRequest(MethodWithBody.PUT, parameters, null, HttpHeaderUtils.build("h1", "v1"));

	    requestBodyTest(MethodWithBody.PUT, request, HttpRequestUtils.toUrlEncodedForm(parameters), null,
		    HttpHeaderUtils.build("h1", "v1"));
	}
    }

    //
    //
    //

    @Test
    public void postRequestWithMapHeadersTest() throws URISyntaxException, IOException {

	String body = "bodyString5";

	HashMap<String, String> map = new HashMap<String, String>();
	map.put("h1", "v1");
	map.put("h2", "v2");

	{
	    HttpRequest request = buildRequest(MethodWithBody.POST, body.getBytes(StandardCharsets.UTF_8), map, null);

	    requestBodyTest(MethodWithBody.POST, request, body, map, null);
	}

	{
	    HttpRequest request = buildRequest(MethodWithBody.POST, body, map, null);

	    requestBodyTest(MethodWithBody.POST, request, body, map, null);
	}

	{
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

	    HttpRequest request = buildRequest(MethodWithBody.POST, inputStream, map, null);

	    requestBodyTest(MethodWithBody.POST, request, body, map, null);
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.POST, tempFile, map, null);

	    requestBodyTest(MethodWithBody.POST, request, body, map, null);
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.POST, tempFile.toFile(), map, null);

	    requestBodyTest(MethodWithBody.POST, request, body, map, null);
	}

	{
	    HashMap<String, String> parameters = new HashMap<String, String>();
	    parameters.put("p1", "v1");
	    parameters.put("p2", "v2");

	    HttpRequest request = buildRequest(MethodWithBody.POST, parameters, map, null);

	    requestBodyTest(MethodWithBody.POST, request, HttpRequestUtils.toUrlEncodedForm(parameters), map, null);
	}
    }

    /**
     * @throws URISyntaxException
     * @throws IOException
     */
    @Test
    public void putRequestWithMapHeadersTest() throws URISyntaxException, IOException {

	String body = "bodyString6";

	HashMap<String, String> map = new HashMap<String, String>();
	map.put("h1", "v1");
	map.put("h2", "v2");

	{
	    HttpRequest request = buildRequest(MethodWithBody.PUT, body.getBytes(StandardCharsets.UTF_8), map, null);

	    requestBodyTest(MethodWithBody.PUT, request, body, map, null);
	}

	{
	    HttpRequest request = buildRequest(MethodWithBody.PUT, body, map, null);

	    requestBodyTest(MethodWithBody.PUT, request, body, map, null);
	}

	{
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

	    HttpRequest request = buildRequest(MethodWithBody.PUT, inputStream, map, null);

	    requestBodyTest(MethodWithBody.PUT, request, body, map, null);
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.PUT, tempFile, map, null);

	    requestBodyTest(MethodWithBody.PUT, request, body, map, null);
	}

	{
	    Path tempFile = createTempFile("test", ".tmp");

	    Files.writeString(tempFile, body, StandardOpenOption.WRITE);

	    HttpRequest request = buildRequest(MethodWithBody.PUT, tempFile.toFile(), map, null);

	    requestBodyTest(MethodWithBody.PUT, request, body, map, null);
	}

	{
	    HashMap<String, String> parameters = new HashMap<String, String>();
	    parameters.put("p1", "v1");
	    parameters.put("p2", "v2");

	    HttpRequest request = buildRequest(MethodWithBody.PUT, parameters, map, null);

	    requestBodyTest(MethodWithBody.PUT, request, HttpRequestUtils.toUrlEncodedForm(parameters), map, null);
	}
    }

    //
    //
    //

    /**
     * @param method
     * @param bodyContent
     * @param mapHeaders
     * @param httpHeaders
     * @throws URISyntaxException
     */
    private void requestBodyTest(//
	    MethodWithBody method, //
	    HttpRequest request, //
	    String bodyContent, //
	    HashMap<String, String> mapHeaders, //
	    HttpHeaders httpHeaders) throws URISyntaxException {

	Assert.assertEquals("http://test", request.uri().toString());
	Assert.assertEquals(method.name(), request.method());

	if (mapHeaders == null && httpHeaders == null) {

	    if (bodyContent.contains("&")) {

		Assert.assertEquals(1, request.headers().map().size());

		Assert.assertEquals("application/x-www-form-urlencoded", request.headers().firstValue("Content-Type").get());

	    } else {

		Assert.assertEquals(0, request.headers().map().size());

	    }
	} else if (httpHeaders != null) {

	    if (bodyContent.contains("&")) {

		Assert.assertEquals("application/x-www-form-urlencoded", request.headers().firstValue("Content-Type").get());

		Assert.assertEquals(((List<?>) httpHeaders.map().values().toArray()[0]).get(0), //
			request.headers().firstValue(httpHeaders.map().keySet().toArray()[0].toString()).get());

	    } else {

		Assert.assertEquals(request.headers(), httpHeaders);
	    }

	} else {

	    if (bodyContent.contains("&")) {

		Assert.assertEquals(request.headers().map().size(), mapHeaders.size() + 1);

		Assert.assertEquals("application/x-www-form-urlencoded", request.headers().firstValue("Content-Type").get());

		Assert.assertEquals(((List<?>) request.headers().map().values().toArray()[1]).get(0), //
			mapHeaders.get(mapHeaders.keySet().toArray()[0]));

		Assert.assertTrue(request.headers().map().containsKey(mapHeaders.keySet().toArray()[0]));
		Assert.assertTrue(request.headers().map().containsKey(mapHeaders.keySet().toArray()[1]));

		Assert.assertEquals(request.headers().map().get(mapHeaders.keySet().toArray()[0]).get(0), //
			mapHeaders.get(mapHeaders.keySet().toArray()[0]));//

		Assert.assertEquals(request.headers().map().get(mapHeaders.keySet().toArray()[1]).get(0), //
			mapHeaders.get(mapHeaders.keySet().toArray()[1]));//

	    } else {

		Assert.assertEquals(request.headers().map().size(), mapHeaders.size());

		Assert.assertTrue(request.headers().map().containsKey(mapHeaders.keySet().toArray()[0]));
		Assert.assertTrue(request.headers().map().containsKey(mapHeaders.keySet().toArray()[1]));

		Assert.assertEquals(request.headers().map().get(mapHeaders.keySet().toArray()[0]).get(0), //
			mapHeaders.get(mapHeaders.keySet().toArray()[0]));//

		Assert.assertEquals(request.headers().map().get(mapHeaders.keySet().toArray()[1]).get(0), //
			mapHeaders.get(mapHeaders.keySet().toArray()[1]));//

	    }
	}

	TestSubscriber sub = new TestSubscriber();

	request.bodyPublisher().get().subscribe(sub);

	Subscription subscription = sub.getSubscription();
	subscription.request(Long.MAX_VALUE);

	ByteBuffer byteBufferBody = sub.getItem();

	byte[] bytes = new byte[byteBufferBody.remaining()];
	byteBufferBody.get(bytes);
	String stringBody = new String(bytes, StandardCharsets.UTF_8);

	Assert.assertEquals(bodyContent, stringBody);

	Assert.assertTrue(sub.isComplete());
    }

    /**
     * @param method
     * @param bodyContent
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    private HttpRequest buildRequest(MethodWithBody method, byte[] bodyContent, HashMap<String, String> mapHeaders, HttpHeaders httpHeaders)
	    throws URISyntaxException {

	HttpRequest request = null;

	if (mapHeaders == null && httpHeaders == null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent //
	    );

	} else if (mapHeaders != null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    mapHeaders);
	} else {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    httpHeaders);
	}

	return request;
    }

    /**
     * @param method
     * @param bodyContent
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    private HttpRequest buildRequest(MethodWithBody method, InputStream bodyContent, HashMap<String, String> mapHeaders,
	    HttpHeaders httpHeaders) throws URISyntaxException {

	HttpRequest request = null;

	if (mapHeaders == null && httpHeaders == null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent //
	    );

	} else if (mapHeaders != null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    mapHeaders);
	} else {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    httpHeaders);
	}

	return request;
    }

    /**
     * @param method
     * @param bodyContent
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    private HttpRequest buildRequest(MethodWithBody method, String bodyContent, HashMap<String, String> mapHeaders, HttpHeaders httpHeaders)
	    throws URISyntaxException {

	HttpRequest request = null;

	if (mapHeaders == null && httpHeaders == null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent //
	    );

	} else if (mapHeaders != null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    mapHeaders);
	} else {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    httpHeaders);
	}

	return request;
    }

    /**
     * @param method
     * @param bodyContent
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    private HttpRequest buildRequest(MethodWithBody method, File bodyContent, HashMap<String, String> mapHeaders, HttpHeaders httpHeaders)
	    throws URISyntaxException, FileNotFoundException {

	HttpRequest request = null;

	if (mapHeaders == null && httpHeaders == null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent //
	    );

	} else if (mapHeaders != null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    mapHeaders);
	} else {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    httpHeaders);
	}

	return request;
    }

    /**
     * @param method
     * @param bodyContent
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    private HttpRequest buildRequest(MethodWithBody method, Path bodyContent, HashMap<String, String> mapHeaders, HttpHeaders httpHeaders)
	    throws URISyntaxException, FileNotFoundException {

	HttpRequest request = null;

	if (mapHeaders == null && httpHeaders == null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent //
	    );

	} else if (mapHeaders != null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    mapHeaders);
	} else {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    httpHeaders);
	}

	return request;
    }

    /**
     * @param method
     * @param bodyContent
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    private HttpRequest buildRequest(MethodWithBody method, HashMap<String, String> bodyContent, HashMap<String, String> mapHeaders,
	    HttpHeaders httpHeaders) throws URISyntaxException, FileNotFoundException {

	HttpRequest request = null;

	if (mapHeaders == null && httpHeaders == null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent //
	    );

	} else if (mapHeaders != null) {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    mapHeaders);
	} else {

	    request = HttpRequestUtils.build(//
		    method, //
		    "http://test", //
		    bodyContent, //
		    httpHeaders);
	}

	return request;
    }

    /**
     * @author Fabrizio
     */
    private class TestSubscriber implements Subscriber<ByteBuffer> {

	private Subscription subscription;
	private ByteBuffer item;
	private boolean complete;

	public final boolean isComplete() {

	    return complete;
	}

	public final ByteBuffer getItem() {

	    return item;
	}

	public Subscription getSubscription() {

	    return subscription;
	}

	@Override
	public void onSubscribe(Subscription subscription) {

	    this.subscription = subscription;
	}

	@Override
	public void onNext(ByteBuffer item) {

	    this.item = item;
	}

	@Override
	public void onError(Throwable throwable) {

	}

	@Override
	public void onComplete() {

	    this.complete = true;
	}
    }
}
