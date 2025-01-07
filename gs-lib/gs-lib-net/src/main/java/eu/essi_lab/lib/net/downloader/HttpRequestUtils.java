package eu.essi_lab.lib.net.downloader;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Fabrizio
 */
public class HttpRequestUtils {

    /**
     * @author Fabrizio
     */
    public enum MethodWithBody {

	/**
	 * 
	 */
	POST,
	/**
	 * 
	 */
	PUT
    }

    /**
     * @author Fabrizio
     */
    public enum MethodNoBody {

	/**
	 * 
	 */
	GET,
	/**
	 * 
	 */
	HEAD,
	/**
	 * 
	 */
	DELETE,
	/**
	 * 
	 */
	OPTIONS;
    }

    /**
     * @param method
     * @param url
     * @return
     * @throws URISyntaxException
     */
    public static HttpRequest build(MethodNoBody method, String url) throws URISyntaxException {

	return build(method.name(), url);
    }

    /**
     * @param method
     * @param url
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    public static HttpRequest build(MethodNoBody method, String url, HttpHeaders headers) throws URISyntaxException {

	return build(method.name(), url, headers);
    }

    /**
     * @param method
     * @param url
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    public static HttpRequest build(MethodNoBody method, String url, Map<String, String> headers) throws URISyntaxException {

	return build(method.name(), url, HttpHeaderUtils.build(headers));
    }

    /**
     * @param method
     * @param url
     * @param body
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, byte[] body) throws URISyntaxException {

	return build(method.name(), url, BodyPublishers.ofByteArray(body));
    }

    /**
     * @param method
     * @param url
     * @param publisher
     * @return
     * @throws URISyntaxException
     */
    public static HttpRequest build(MethodWithBody method, String url, BodyPublisher publisher) throws URISyntaxException {

	return build(method.name(), url, publisher);
    }

    /**
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, byte[] body, HttpHeaders headers) throws URISyntaxException {

	return build(method.name(), url, BodyPublishers.ofByteArray(body), headers);
    }

    /**
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, byte[] body, HashMap<String, String> headers)
	    throws URISyntaxException {

	return build(method.name(), url, BodyPublishers.ofByteArray(body), HttpHeaderUtils.build(headers));
    }

    /**
     * @param method
     * @param url
     * @param body
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, String body) throws URISyntaxException {

	return build(method.name(), url, BodyPublishers.ofString(body));
    }

    /**
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, String body, HttpHeaders headers) throws URISyntaxException {

	return build(method.name(), url, BodyPublishers.ofString(body), headers);
    }

    /**
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, String body, HashMap<String, String> headers)
	    throws URISyntaxException {

	return build(method.name(), url, BodyPublishers.ofString(body), HttpHeaderUtils.build(headers));
    }

    /**
     * @param method
     * @param url
     * @param body
     * @return
     * @throws FileNotFoundException
     * @throws URISyntaxException
     */
    public static HttpRequest build(MethodWithBody method, String url, Path body) throws FileNotFoundException, URISyntaxException {

	return build(method.name(), url, BodyPublishers.ofFile(body));
    }

    /**
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, Path body, HttpHeaders headers)
	    throws URISyntaxException, FileNotFoundException {

	return build(method.name(), url, BodyPublishers.ofFile(body), headers);
    }

    /**
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, Path body, HashMap<String, String> headers)
	    throws URISyntaxException, FileNotFoundException {

	return build(method.name(), url, BodyPublishers.ofFile(body), HttpHeaderUtils.build(headers));
    }

    /**
     * @param method
     * @param url
     * @param body
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, File body) throws URISyntaxException, FileNotFoundException {

	return build(method.name(), url, BodyPublishers.ofFile(body.toPath()));
    }

    /**
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, File body, HttpHeaders headers)
	    throws URISyntaxException, FileNotFoundException {

	return build(method.name(), url, BodyPublishers.ofFile(body.toPath()), headers);
    }

    /**
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    public static HttpRequest build(MethodWithBody method, String url, File body, HashMap<String, String> headers)
	    throws URISyntaxException, FileNotFoundException {

	return build(method.name(), url, BodyPublishers.ofFile(body.toPath()), HttpHeaderUtils.build(headers));
    }

    /**
     * @param method
     * @param url
     * @param body
     * @return
     * @throws URISyntaxException
     */
    public static HttpRequest build(MethodWithBody method, String url, InputStream body) throws URISyntaxException {

	return build(method.name(), url, BodyPublishers.ofInputStream(() -> body));
    }

    /**
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    public static HttpRequest build(MethodWithBody method, String url, InputStream body, HttpHeaders headers) throws URISyntaxException {

	return build(method.name(), url, BodyPublishers.ofInputStream(() -> body), headers);
    }

    /**
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    public static HttpRequest build(MethodWithBody method, String url, InputStream body, HashMap<String, String> headers)
	    throws URISyntaxException {

	return build(method.name(), url, BodyPublishers.ofInputStream(() -> body), HttpHeaderUtils.build(headers));
    }

    /**
     * @param method
     * @param url
     * @param parameters
     * @return
     * @throws URISyntaxException
     */
    public static HttpRequest build(//
	    MethodWithBody method, //
	    String url, //
	    HashMap<String, String> parameters)//
	    throws URISyntaxException {

	return build(method, url, parameters, HttpHeaderUtils.buildEmpty());
    }

    /**
     * @param method
     * @param url
     * @param parameters
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    public static HttpRequest build(//
	    MethodWithBody method, //
	    String url, //
	    HashMap<String, String> parameters, //
	    HashMap<String, String> headers)//
	    throws URISyntaxException {

	return build(method, url, parameters, HttpHeaderUtils.build(headers));
    }

    /**
     * @param method
     * @param url
     * @param parameters
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    public static HttpRequest build(//
	    MethodWithBody method, //
	    String url, //
	    HashMap<String, String> parameters, //
	    HttpHeaders headers)//
	    throws URISyntaxException {

	String form = toUrlEncodedForm(parameters);

	Map<String, List<String>> map = new HashMap<String, List<String>>(headers.map());
	map.put("Content-Type", Arrays.asList("application/x-www-form-urlencoded"));

	return build(method.name(), url, BodyPublishers.ofString(form), HttpHeaderUtils.buildMultiValue(map));
    }

    /**
     * @param parameters
     * @return
     */
    public static String toUrlEncodedForm(HashMap<String, String> parameters) {

	return parameters.//
		entrySet().//
		stream().//
		map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).//
		collect(Collectors.joining("&"));
    }

    /**
     * @param request
     * @return
     */
    public static Builder fromRequest(HttpRequest request) {

	Builder builder = HttpRequest.newBuilder();

	URI uri = request.uri();
	builder = builder.uri(uri);

	String method = request.method();
	BodyPublisher bodyPublisher = request.bodyPublisher().orElse(BodyPublishers.noBody());
	builder = builder.method(method, bodyPublisher);

	HttpHeaders headers = request.headers();
	builder = addHeaders(builder, headers);

	boolean expectContinue = request.expectContinue();
	builder = builder.expectContinue(expectContinue);

	Optional<Duration> timeout = request.timeout();
	if (timeout.isPresent()) {
	    builder = builder.timeout(timeout.get());
	}

	Optional<Version> version = request.version();
	if (version.isPresent()) {
	    builder = builder.version(version.get());
	}

	return builder;
    }

    /**
     * @param method
     * @param url
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    private static HttpRequest build(//
	    String method, //
	    String url) throws URISyntaxException {

	return build(method, url, BodyPublishers.noBody(), HttpHeaderUtils.buildEmpty());
    }

    /**
     * @param method
     * @param url
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    private static HttpRequest build(//
	    String method, //
	    String url, //
	    HttpHeaders headers) throws URISyntaxException {

	return build(method, url, BodyPublishers.noBody(), headers);
    }

    /**
     * @param method
     * @param url
     * @param headers
     * @return
     * @throws URISyntaxException
     * @throws FileNotFoundException
     */
    private static HttpRequest build(//
	    String method, //
	    String url, //
	    BodyPublisher bp) throws URISyntaxException {

	return build(method, url, bp, HttpHeaderUtils.buildEmpty());
    }

    /**
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws URISyntaxException
     */
    private static HttpRequest build(//
	    String method, //
	    String url, //
	    BodyPublisher bp, //
	    HttpHeaders headers) throws URISyntaxException {

	Builder builder = HttpRequest.//
		newBuilder().//
		uri(new URI(url)).//
		method(method, bp);//

	builder = addHeaders(builder, headers);
	return builder.build();
    }

    /**
     * @param builder
     * @param headers
     * @return
     */
    private static Builder addHeaders(Builder builder, HttpHeaders headers) {

	Map<String, List<String>> map = headers.map();

	for (String key : map.keySet()) {

	    List<String> values = map.get(key);

	    values.forEach(v -> builder.header(key, v));
	}

	return builder;
    }
}
