package eu.essi_lab.lib.net.downloader;

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

import dev.failsafe.*;
import dev.failsafe.function.*;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.*;
import eu.essi_lab.lib.net.utils.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.exceptions.*;
import org.apache.commons.io.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpClient.*;
import java.nio.charset.*;
import java.security.*;
import java.security.cert.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
public class Downloader {

    /**
     *
     */
    public static final String DEFAULT_KEY_STORE_TYPE = "PKCS12";

    /**
     * 30 seconds
     */
    private static final long DEFAULT_CONNECTION_TIMEOUT = 30;

    /**
     * 1 minute
     */
    private static final long DEFAULT_REPONSE_TIMEOUT = 60;

    /**
     *
     */
    private Long connectionTimeout;

    /**
     *
     */
    private Long responseTimeout;

    /**
     *
     */
    private Version version;

    /**
     *
     */
    private HttpRequest request;

    /**
     *
     */
    private RetryPolicy<HttpResponse<InputStream>> retryPolicy;

    /**
     *
     */
    private Redirect redirect;

    /**
     *
     */
    public Downloader() {

	setConnectionTimeout(TimeUnit.SECONDS, DEFAULT_CONNECTION_TIMEOUT);
	setResponseTimeout(TimeUnit.SECONDS, DEFAULT_REPONSE_TIMEOUT);
	setHttpVersion(Version.HTTP_1_1);
	setRedirectStrategy(Redirect.NORMAL);
    }

    /**
     * @param retryPolicy
     */
    public void setRetryPolicy(RetryPolicy<HttpResponse<InputStream>> retryPolicy) {

	this.retryPolicy = retryPolicy;
    }

    /**
     * Set a retry policy with the given attempts and delay and with a retry condition which is approved if the {@link HttpResponse} returns
     * a status code different from 200
     *
     * @param delayTimeUnit
     * @param attempts
     * @param delay
     */
    public void setRetryPolicy(//
	    int attempts, //
	    TimeUnit delayTimeUnit, //
	    int delay) {

	setRetryPolicy(r -> r.statusCode() != 200, attempts, delayTimeUnit, delay);
    }

    /**
     * @param condition the condition by which a retry should be done
     * @param delayTimeUnit
     * @param attempts
     * @param delay
     */
    public void setRetryPolicy(//
	    CheckedPredicate<HttpResponse<InputStream>> condition, //
	    int attempts, //
	    TimeUnit delayTimeUnit, //
	    int delay) {

	setRetryPolicy(RetryPolicy.<HttpResponse<InputStream>> //
		builder().//
		handleResultIf(condition).//
		withDelay(Duration.ofSeconds(delayTimeUnit.toSeconds(delay))).//
		withMaxAttempts(attempts).//
		onRetry(e -> {
	    GSLoggerFactory.getLogger(getClass()).warn("Failure #{}. Retrying...", e.getAttemptCount());
	}).//
		onRetriesExceeded(e -> {
	    GSLoggerFactory.getLogger(getClass()).warn("Failed to connect. Max retries exceeded");
	}).//
		build());
    }

    /**
     * @param redirect
     */
    public void setRedirectStrategy(Redirect redirect) {

	this.redirect = redirect;
    }

    /**
     * @param timeUnit
     * @param timeOut
     */
    public void setConnectionTimeout(TimeUnit timeUnit, long timeOut) {

	this.connectionTimeout = timeUnit.toMillis(timeOut);
    }

    /**
     * @param timeUnit
     * @param timeOut
     */
    public void setResponseTimeout(TimeUnit timeUnit, long timeOut) {

	this.responseTimeout = timeUnit.toMillis(timeOut);
    }

    /**
     * @param version
     */
    public void setHttpVersion(Version version) {

	this.version = version;
    }

    /**
     * @return
     */
    public long getConnectionTimeout() {

	return connectionTimeout;
    }

    /**
     * @return
     */
    public long getResponseTimeout() {

	return responseTimeout;
    }

    /**
     * @return
     */
    public Version getHttpVersion() {

	return version;
    }

    /**
     * @return
     */
    public RetryPolicy<HttpResponse<InputStream>> getRetryPolicy() {

	return retryPolicy;
    }

    /**
     * @return
     */
    public Redirect getRedirectStrategy() {

	return redirect;
    }

    /**
     * @param url
     * @return
     */
    public Optional<String> downloadOptionalString(String url) {

	String[] userInfo = getUserInfo(url);

	return downloadOptionalString(url, userInfo[0], userInfo[1], HttpHeaderUtils.buildEmpty());
    }

    /**
     * @param url
     * @return
     */
    public Optional<String> downloadOptionalString(String url, String user, String pwd) {

	return downloadOptionalString(url, user, pwd, HttpHeaderUtils.buildEmpty());
    }

    /**
     * @param url
     * @param headers
     * @return
     */
    public Optional<String> downloadOptionalString(String url, HttpHeaders headers) {

	String[] userInfo = getUserInfo(url);

	return downloadOptionalString(url, userInfo[0], userInfo[1], headers);
    }

    /**
     * @param url
     * @param user
     * @param password
     * @param headers
     * @return
     */
    public Optional<String> downloadOptionalString(String url, String user, String pwd, HttpHeaders headers) {

	try {

	    if (user != null && pwd != null) {

		HttpResponse<InputStream> response = downloadResponse(url, user, pwd);

		Optional<InputStream> stream = getStream(response);

		if (stream.isPresent()) {

		    return Optional.of(IOUtils.toString(stream.get(), StandardCharsets.UTF_8));
		}
	    }

	    Optional<InputStream> optionalStream = downloadOptionalStream(url, headers);

	    if (optionalStream.isPresent()) {

		InputStream stream = optionalStream.get();
		String response = IOUtils.toString(stream, StandardCharsets.UTF_8);
		stream.close();

		if (response.isEmpty()) {

		    return Optional.empty();
		}

		return Optional.of(response);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Failed to download string from: " + url, e);
	}

	return Optional.empty();
    }

    /**
     * @param url
     * @return
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws GSException
     */
    public Optional<InputStream> downloadOptionalStream(String url) {

	return downloadOptionalStream(url, HttpHeaderUtils.buildEmpty());
    }

    /**
     * @param url
     * @param headers
     * @return
     */
    public Optional<InputStream> downloadOptionalStream(String url, HttpHeaders headers) {

	try {

	    HttpResponse<InputStream> ret = downloadResponse(url, headers);

	    return getStream(ret);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Failed to download stream from: " + url, e);
	}

	return Optional.empty();
    }

    /**
     * @param response
     * @return
     */
    public static Optional<InputStream> getStream(HttpResponse<InputStream> response) {

	boolean emptyBody = HttpConnectionUtils.emptyBody(response);

	int statusCode = response.statusCode();

	if (emptyBody || statusCode != 200) {

	    return Optional.empty();
	}

	return Optional.of(response.body());
    }

    /**
     * @param url
     * @return
     */
    public Optional<HttpResponse<InputStream>> downloadOptionalResponse(String url) {

	return downloadOptionalResponse(url, HttpHeaderUtils.buildEmpty());
    }

    /**
     * @param url
     * @param headers
     * @return
     */
    public Optional<HttpResponse<InputStream>> downloadOptionalResponse(String url, HttpHeaders headers) {

	Optional<HttpResponse<InputStream>> opt = Optional.empty();

	try {

	    opt = Optional.of(downloadResponse(url, headers));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Failed to download response from: " + url, e);
	}

	return opt;
    }

    /**
     * @param url
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    public HttpResponse<InputStream> downloadResponse(//
	    String url) throws FailsafeException, IOException, InterruptedException, URISyntaxException {

	return downloadResponse(url, HttpHeaderUtils.buildEmpty());
    }

    /**
     * @param request
     * @param username
     * @param password
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    public HttpResponse<InputStream> downloadResponse(//
	    String url, //
	    String username, //
	    String password) throws FailsafeException, IOException, InterruptedException, URISyntaxException {

	return downloadResponse(//
		HttpRequestUtils.build(MethodNoBody.GET, url, HttpHeaderUtils.buildEmpty()), //
		username, //
		password, //
		null, //
		null //
	);
    }

    /**
     * @param url
     * @param headers
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    public HttpResponse<InputStream> downloadResponse(//
	    String url, //
	    HttpHeaders headers) throws FailsafeException, IOException, InterruptedException, URISyntaxException {

	return downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url, headers));
    }

    /**
     * @param request
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public HttpResponse<InputStream> downloadResponse(HttpRequest request) throws FailsafeException, IOException, InterruptedException {

	return downloadResponse(request, null, null, null, null);
    }

    /**
     * @param request
     * @param username
     * @param password
     * @return
     * @throws FailsafeException
     * @throws IOException
     * @throws InterruptedException
     */
    public HttpResponse<InputStream> downloadResponse(//
	    HttpRequest request, //
	    String username, //
	    String password) throws FailsafeException, IOException, InterruptedException {

	return downloadResponse(request, username, password, null, null);
    }

    /**
     * @param request
     * @param keystore
     * @param keystorePassword
     * @return
     * @throws FailsafeException
     * @throws IOException
     * @throws InterruptedException
     */
    public HttpResponse<InputStream> downloadResponse(//
	    HttpRequest request, //
	    InputStream keystore, //
	    String keystorePassword) throws FailsafeException, IOException, InterruptedException {

	return downloadResponse(request, null, null, keystore, keystorePassword);
    }

    /**
     * @param request
     * @param username
     * @param password
     * @param trustStore
     * @param trustStorePwd
     * @return
     * @throws FailsafeException
     * @throws IOException
     * @throws InterruptedException
     */
    public HttpResponse<InputStream> downloadResponse(//
	    HttpRequest request, //
	    String username, //
	    String password, //
	    InputStream trustStore, //
	    String trustStorePwd) throws FailsafeException, IOException, InterruptedException {

	GSLoggerFactory.getLogger(getClass()).trace("Execution of {} STARTED", request.uri().toString());

	HttpClient client = createHttpClient(//
		request.uri().toURL().getProtocol().equalsIgnoreCase("https"), //
		request.uri().toURL().getHost(), //
		request.uri().toURL().getPort(), //
		username, //
		password, //
		trustStore, //
		trustStorePwd);

	// set the response timeout to the request
	java.net.http.HttpRequest.Builder requestBuilder = HttpRequestUtils.fromRequest(request);
	requestBuilder.timeout(Duration.of((long) responseTimeout, ChronoUnit.MILLIS));
	request = requestBuilder.build();

	// required to be used in the Failsafe get
	this.request = request;

	HttpResponse<InputStream> response = null;

	if (retryPolicy != null) {

	    response = Failsafe.with(retryPolicy).get(() -> client.send(this.request, HttpResponse.BodyHandlers.ofInputStream()));

	} else {

	    response = client.send(this.request, HttpResponse.BodyHandlers.ofInputStream());
	}

	GSLoggerFactory.getLogger(getClass()).trace("Execution of {} ENDED with code {}", request.uri().toString(), response.statusCode());

	return response;
    }

    /**
     * @param url
     * @return
     */
    private String[] getUserInfo(String url) {

	String user = null;
	String pwd = null;

	try {

	    String userInfo = new URL(url).getUserInfo();

	    if (userInfo != null) {
		String[] split = userInfo.split(":");

		if (split.length == 2) {
		    user = split[0];
		    pwd = split[1];
		}
	    }
	} catch (MalformedURLException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return new String[] { user, pwd };
    }

    /**
     * @param https
     * @param hostname
     * @param port
     * @param username
     * @param password
     * @param trustStoreStream
     * @param trustStorePassword
     * @param certificatePassword
     * @return
     */
    private HttpClient createHttpClient(//
	    boolean https, //
	    String hostname, //
	    int port, //
	    String username, //
	    String password, //
	    InputStream trustStoreStream, //
	    String trustStorePassword) {

	Builder builder = HttpClient.newBuilder().version(version);

	if (username != null && password != null) {

	    GSLoggerFactory.getLogger(getClass()).debug("Using basic auth: {}-{}", username, password);

	    builder.authenticator(new Authenticator() {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
		    return new PasswordAuthentication(username, password.toCharArray());
		}
	    });
	}

	if (trustStoreStream == null) {

	    if (System.getProperty("dab.net.ssl.trustStore") != null) {

		GSLoggerFactory.getLogger(getClass()).debug("Using dab trust store from system property");

		try {
		    trustStoreStream = new FileInputStream(System.getProperty("dab.net.ssl.trustStore"));

		    trustStorePassword = System.getProperty("dab.net.ssl.trustStorePassword");

		} catch (IOException e) {

		    throw new RuntimeException(e);
		}
	    }
	}

	if (https && trustStoreStream != null) {

	    try {

		GSLoggerFactory.getLogger(getClass()).debug("Using trust store");

		X509TrustManager combinedTm = getX509TrustManager(trustStoreStream, trustStorePassword, hostname);

		SSLContext sslContext = SSLContext.getInstance("TLS");

		sslContext.init(//
			null, // key store, required only in case of HTTPS with mutual TLS (mTLS)
			new TrustManager[] { combinedTm }, //
			null); // randomness source

		builder.sslContext(sslContext);

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	}

	builder.connectTimeout(Duration.of((long) connectionTimeout, ChronoUnit.MILLIS));

	builder.followRedirects(redirect);

	return builder.build();

    }

    /**
     * @param stream
     * @param pwd
     * @return
     * @throws Exception
     */
    private X509TrustManager getX509TrustManager(InputStream stream, String pwd, String hostName) throws Exception {

	TrustManagerFactory defaultTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	defaultTmf.init((KeyStore) null); // system truststore

	char[] charArray = null;

	if (pwd != null) {

	    charArray = pwd.toCharArray();
	}

	KeyStore store = KeyStore.getInstance(DEFAULT_KEY_STORE_TYPE); // JKS or "PKCS12"
	store.load(stream, charArray);

	stream.close();

	TrustManagerFactory tmf = TrustManagerFactory.getInstance(//
		TrustManagerFactory.getDefaultAlgorithm());

	tmf.init(store);

	X509TrustManager defaultTm = getX509Tm(defaultTmf);
	X509TrustManager customTm = getX509Tm(tmf);

	return new X509TrustManager() {
	    @Override
	    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
		    customTm.checkClientTrusted(chain, authType);
		} catch (CertificateException e) {
		    defaultTm.checkClientTrusted(chain, authType);
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    }

	    @Override
	    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
		    customTm.checkServerTrusted(chain, authType);
		} catch (CertificateException e) {
		    defaultTm.checkServerTrusted(chain, authType);
		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage() + ": " + hostName);
		}
	    }

	    @Override
	    public X509Certificate[] getAcceptedIssuers() {
		X509Certificate[] a = customTm.getAcceptedIssuers();
		X509Certificate[] b = defaultTm.getAcceptedIssuers();
		X509Certificate[] all = new X509Certificate[a.length + b.length];
		System.arraycopy(a, 0, all, 0, a.length);
		System.arraycopy(b, 0, all, a.length, b.length);
		return all;
	    }
	};
    }

    private static X509TrustManager getX509Tm(TrustManagerFactory tmf) {
	for (TrustManager tm : tmf.getTrustManagers()) {
	    if (tm instanceof X509TrustManager) {
		return (X509TrustManager) tm;
	    }
	}
	throw new IllegalStateException("No X509TrustManager found");
    }

}
