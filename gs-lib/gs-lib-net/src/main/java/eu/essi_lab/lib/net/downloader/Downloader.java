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

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.commons.io.IOUtils;

import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeException;
import dev.failsafe.RetryPolicy;
import dev.failsafe.function.CheckedPredicate;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class Downloader {

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
     * Set a retry policy with the given attempts and delay and with a retry condition which is approved if the
     * {@link HttpResponse} returns a status code different from 200
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

	return downloadOptionalString(url, HttpHeaderUtils.buildEmpty());
    }

    /**
     * @param url
     * @param headers
     * @return
     */
    public Optional<String> downloadOptionalString(String url, HttpHeaders headers) {

	try {

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

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
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

	    boolean emptyBody = HttpConnectionUtils.emptyBody(ret);

	    int statusCode = ret.statusCode();
	    	    
	    if (emptyBody || statusCode != 200) {

		return Optional.empty();
	    }

	    return Optional.of(ret.body());

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return Optional.empty();
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

	    GSLoggerFactory.getLogger(getClass()).error(e);
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
		null, //
		null);
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

	return downloadResponse(request, null, null);
    }

    /**
     * @param request
     * @param username
     * @param password
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public HttpResponse<InputStream> downloadResponse(//
	    HttpRequest request, //
	    String username, //
	    String password) throws FailsafeException, IOException, InterruptedException {

	return downloadResponse(request, username, password, null, null, null);
    }

    /**
     * @param request
     * @param keystore
     * @param keystorePassword
     * @param certificatePassword
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public HttpResponse<InputStream> downloadResponse(//
	    HttpRequest request, //
	    InputStream keystore, //
	    String keystorePassword, //
	    String certificatePassword) throws FailsafeException, IOException, InterruptedException {

	return downloadResponse(request, null, null, keystore, keystorePassword, certificatePassword);
    }

    /**
     * @param request
     * @param username
     * @param password
     * @param keystore
     * @param keystorePassword
     * @param certificatePassword
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public HttpResponse<InputStream> downloadResponse(//
	    HttpRequest request, //
	    String username, //
	    String password, //
	    InputStream keystore, //
	    String keystorePassword, //
	    String certificatePassword) throws FailsafeException, IOException, InterruptedException {

	GSLoggerFactory.getLogger(getClass()).trace("Execution of {} STARTED", request.uri().toString());

	HttpClient client = createHttpClient(//
		request.uri().toURL().getProtocol().toLowerCase().equals("https"), //
		request.uri().toURL().getHost(), //
		request.uri().toURL().getPort(), //
		username, //
		password, //
		keystore, //
		keystorePassword, //
		certificatePassword);

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
     * @param https
     * @param hostname
     * @param port
     * @param username
     * @param password
     * @param keystore
     * @param keystorePassword
     * @param certificatePassword
     * @return
     */
    private HttpClient createHttpClient(//
	    boolean https, //
	    String hostname, //
	    int port, //
	    String username, //
	    String password, //
	    InputStream keystore, //
	    String keystorePassword, //
	    String certificatePassword) {

	Builder builder = HttpClient.newBuilder().version(version);

	//
	// https://www.baeldung.com/java-httpclient-basic-auth
	// https://dev.to/noelopez/http-client-api-in-java-part-2-75e#:~:text=Basic%20Authentication%20is%20a%20simple,in%20the%20Authorization%20HTTP%20header.
	//

	if (username != null && password != null) {

	    GSLoggerFactory.getLogger(getClass()).debug("Using basic auth: {}-{}", username, password);

	    builder = builder.authenticator(new Authenticator() {
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
		    return new PasswordAuthentication(username, password.toCharArray());
		}
	    });
	}

	if (https) {

	    try {

		SSLContext sslContext = SSLContext.getInstance("SSL");

		//
		// https://stackoverflow.com/questions/52988677/allow-insecure-https-connection-for-java-jdk-11-httpclient
		//

		if (keystore != null) {

		    GSLoggerFactory.getLogger(getClass()).debug("Using key store");

		    // certificatePassword seems to be not necessary
		    if (certificatePassword == null) {
			certificatePassword = "";
		    }

		    KeyStore keyStore = readStore(keystore, keystorePassword);

		    KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
		    kmf.init(keyStore, keystorePassword.toCharArray());

		    TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
		    tmf.init(keyStore);

		    sslContext.init(//
			    kmf.getKeyManagers(), //
			    tmf.getTrustManagers(), //
			    new SecureRandom());

		} else {

		    TrustManager trustManager = createTrustManager();

		    sslContext.init(//
			    null, //
			    new TrustManager[] { trustManager }, //
			    new SecureRandom());
		}

		builder = builder.sslContext(sslContext);

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	}

	builder = builder.connectTimeout(Duration.of((long) connectionTimeout, ChronoUnit.MILLIS));

	builder = builder.followRedirects(redirect);

	HttpClient client = builder.build();

	return client;
    }

    /**
     * https://medium.com/javarevisited/java-http-client-invalid-certificate-93673415fdec
     * 
     * @return
     */
    private X509ExtendedTrustManager createTrustManager() {

	return new X509ExtendedTrustManager() {
	    @Override
	    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return new java.security.cert.X509Certificate[0];
	    }

	    @Override
	    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
	    }

	    @Override
	    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws FailsafeException, CertificateException {
	    }

	    @Override
	    public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {
	    }

	    @Override
	    public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) throws CertificateException {
	    }

	    @Override
	    public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {

	    }

	    @Override
	    public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
		    throws FailsafeException, CertificateException {
	    }
	};
    }

    /**
     * @param keyStoreStream
     * @param keystorePassword
     * @return
     * @throws Exception
     */
    private KeyStore readStore(InputStream keyStoreStream, String keystorePassword) throws Exception {
	if (keystorePassword == null) {
	    keystorePassword = "";
	}

	KeyStore keyStore = KeyStore.getInstance("PKCS12"); // JKS or "PKCS12"
	keyStore.load(keyStoreStream, keystorePassword.toCharArray());
	keyStoreStream.close();
	return keyStore;
    }
}
