package eu.essi_lab.lib.net.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
public class Downloader implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7252043511765390589L;
    private Integer timeOut;
    private Map<String, String> requestHeaders;

    /**
     * @param milliseconds in milliseconds
     */
    public void setTimeout(Integer milliseconds) {

	this.timeOut = milliseconds;
    }

    /**
     * @param url
     * @return
     */
    public Optional<String> downloadString(String url) {

	String string = null;

	try {

	    Optional<InputStream> optionalStream = downloadStream(url);

	    if (optionalStream.isPresent()) {
		InputStream stream = optionalStream.get();
		string = IOUtils.toString(stream, StandardCharsets.UTF_8);
		stream.close();

	    }

	} catch (UnsupportedOperationException | IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return Optional.ofNullable(string);
    }

    /**
     * @param url
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws GSException
     */
    public Optional<InputStream> downloadStream(String url) {
	return downloadStream(url, null);
    }

    public Optional<InputStream> downloadStream(String url, List<Header> headers) {
	Optional<SimpleEntry<Header[], InputStream>> ret = downloadHeadersAndBody(url, headers);
	if (ret.isPresent()) {
	    return Optional.ofNullable(ret.get().getValue());
	}
	return Optional.empty();
    }

    public Optional<SimpleEntry<Header[], InputStream>> downloadHeadersAndBody(String url) {

	return downloadHeadersAndBody(url, null);

    }

    public boolean checkConnectivity(String url) {

	GSLoggerFactory.getLogger(getClass()).trace("Checking connectivity of: {}", url);

	HttpRequestExecutor executor = getExecutor();

	HttpHead head = new HttpHead(url);
	HttpResponse response;
	try {
	    response = executor.execute(head);
	    int statusCode = response.getStatusLine().getStatusCode();

	    GSLoggerFactory.getLogger(getClass()).trace("Obtained status code: {}", statusCode);

	    String statusString = "" + statusCode;
	    if (statusString.startsWith("2")) {
		return true;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return false;

    }

    /**
     * @param url
     * @param headers
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public Optional<SimpleEntry<Header[], InputStream>> downloadHeadersAndBodyWithException(String url, List<Header> headers)
	    throws ClientProtocolException, IOException {

	HttpGet httpGet = getHttpGetRequest(url);

	if (headers != null) {
	    for (Header header : headers) {
		httpGet.addHeader(header);
	    }
	}

	HttpRequestExecutor executor = getExecutor();

	HttpResponse response = executor.execute(httpGet);

	int statusCode = response.getStatusLine().getStatusCode();

	GSLoggerFactory.getLogger(getClass()).trace("Obtained status code: {}", statusCode);

	HttpEntity entity = response.getEntity();
	InputStream body = null;
	if (Objects.nonNull(entity)) {

	    body = entity.getContent();
	}

	Header[] responseHeaders = response.getAllHeaders();

	SimpleEntry<Header[], InputStream> ret = new SimpleEntry<>(responseHeaders, body);

	return Optional.of(ret);
    }

    /**
     * @param url
     * @param headers
     * @return
     */
    public Optional<SimpleEntry<Header[], InputStream>> downloadHeadersAndBody(String url, List<Header> headers) {

	Optional<SimpleEntry<Header[], InputStream>> opt = Optional.empty();

	try {
	    opt = downloadHeadersAndBodyWithException(url, headers);

	} catch (UnsupportedOperationException | IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return opt;
    }

    private HttpGet getHttpGetRequest(String url) {
	HttpGet ret = new HttpGet(url);
	if (requestHeaders != null) {
	    for (String key : requestHeaders.keySet()) {
		String value = requestHeaders.get(key);
		ret.addHeader(key, value);
	    }
	}
	return ret;
    }

    private HttpRequestExecutor getExecutor() {
	HttpRequestExecutor ret = new HttpRequestExecutor();
	ret.setTimeout(timeOut);
	return ret;
    }

    public Optional<Integer> getResponseCode(String url) {
	HttpGet httpGet = getHttpGetRequest(url);

	HttpRequestExecutor executor = getExecutor();

	Optional<Integer> opt = Optional.empty();

	try {

	    HttpResponse response = executor.execute(httpGet);

	    opt = Optional.of(response.getStatusLine().getStatusCode());

	} catch (UnsupportedOperationException | IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return opt;
    }

    public void setRequestHeaders(Map<String, String> headers) {
	this.requestHeaders = headers;

    }

    public String resolveRedirect(String url) {
	boolean fr = HttpURLConnection.getFollowRedirects();
	try {
	    URL u = new URL(url);
	    HttpURLConnection.setFollowRedirects(false);
	    URLConnection connection = u.openConnection();
	    if (connection instanceof HttpURLConnection) {
		HttpURLConnection huc = (HttpURLConnection) connection;
		int responseCode = huc.getResponseCode();
		String responseCodeString = "" + responseCode;
		if (responseCodeString.startsWith("3")) {
		    String field = huc.getHeaderField("Location");
		    HttpURLConnection.setFollowRedirects(fr);
		    return field;
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	HttpURLConnection.setFollowRedirects(fr);
	return url;
    }
}
