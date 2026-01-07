package eu.essi_lab.lib.net.utils;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSession;

import dev.failsafe.FailsafeException;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class HttpConnectionUtils {

    /**
     * @param response
     * @param body
     * @return
     */
    public static HttpResponse<InputStream> wrap(HttpResponse<InputStream> response, InputStream body) {

	return new HttpResponse<InputStream>() {

	    @Override
	    public int statusCode() {

		return response.statusCode();
	    }

	    @Override
	    public HttpRequest request() {

		return response.request();
	    }

	    @Override
	    public Optional<HttpResponse<InputStream>> previousResponse() {

		return response.previousResponse();
	    }

	    @Override
	    public HttpHeaders headers() {

		return response.headers();
	    }

	    @Override
	    public InputStream body() {

		return body;
	    }

	    @Override
	    public Optional<SSLSession> sslSession() {

		return response.sslSession();
	    }

	    @Override
	    public URI uri() {

		return response.uri();
	    }

	    @Override
	    public Version version() {

		return response.version();
	    }
	};
    }

    /**
     * @param resp
     * @return
     */
    public static boolean emptyBody(HttpResponse<InputStream> resp) {

	HttpHeaders respHeaders = resp.headers();

	Optional<String> cLength = respHeaders.firstValue("content-length");
	if (cLength.isPresent() && cLength.get().equals("0")) {

	    return true;
	}

	return false;
    }

    /**
     * @param url
     * @return
     */
    public static String resolveRedirect(String url) {

	boolean fr = HttpURLConnection.getFollowRedirects();

	try {
	    HttpURLConnection.setFollowRedirects(false);
	    URLConnection connection = new URL(url).openConnection();

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

	    GSLoggerFactory.getLogger(Downloader.class).error(e);
	}

	HttpURLConnection.setFollowRedirects(fr);

	return url;
    }

    /**
     * @param url
     * @return
     * @throws URISyntaxException
     */
    public static boolean checkConnectivity(String url) throws URISyntaxException {

	return checkConnectivity(url, null, 0);
    }

    /**
     * @param url
     * @return
     * @throws URISyntaxException
     */
    public static boolean checkConnectivity(String url, TimeUnit timeUnit, long timeout) throws URISyntaxException {

	GSLoggerFactory.getLogger(Downloader.class).trace("Checking connectivity of {} STARTED", url);

	Optional<Integer> responseCode = getOptionalResponseCode(url, timeUnit, timeout);

	if (responseCode.isPresent()) {

	    GSLoggerFactory.getLogger(HttpRequestUtils.class).trace("Obtained status code: {}", responseCode.get());

	    String statusString = "" + responseCode.get();
	    if (statusString.startsWith("2")) {
		return true;
	    }
	}

	GSLoggerFactory.getLogger(Downloader.class).trace("Checking connectivity of {} ENDED", url);

	return false;
    }

    /**
     * @param url
     * @return
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    public static Optional<Integer> getOptionalResponseCode(String url) throws URISyntaxException {

	return getOptionalResponseCode(url, null, 0);
    }

    /**
     * @param url
     * @param timeUnit
     * @param timeOut
     * @return
     * @throws URISyntaxException
     */
    public static Optional<Integer> getOptionalResponseCode(String url, TimeUnit timeUnit, long timeOut) throws URISyntaxException {

	try {

	    return Optional.of(getResponseCode(url, timeUnit, timeOut));

	} catch (UnsupportedOperationException | IOException | InterruptedException e) {

	    GSLoggerFactory.getLogger(HttpRequestUtils.class).error(e.getMessage(), e);
	}

	return Optional.empty();
    }

    /**
     * @param url
     * @param timeUnit
     * @param timeOut
     * @return
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     * @throws FailsafeException
     */
    public static int getResponseCode(String url, TimeUnit timeUnit, long timeOut)
	    throws URISyntaxException, FailsafeException, IOException, InterruptedException {

	HttpRequest request = HttpRequestUtils.build(MethodNoBody.HEAD, url);

	Downloader executor = getDownloader(timeUnit, timeOut);

	HttpResponse<InputStream> response = executor.downloadResponse(request);

	return response.statusCode();
    }

    /**
     * @param timeOut
     * @return
     */
    private static Downloader getDownloader(TimeUnit timeUnit, long timeOut) {

	Downloader ret = new Downloader();

	if (timeUnit != null) {
	    ret.setConnectionTimeout(timeUnit, timeOut);
	    ret.setResponseTimeout(timeUnit, timeOut);
	}

	return ret;
    }
}
