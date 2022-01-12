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
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
public class HttpRequestExecutor {

    public static final String ERR_ID_WRONG_ENDPOINT = "ERR_ID_WRONG_ENDPOINT";
    public static final String ERR_HTTPS_CLIENT = "ERR_HTTPS_CLIENT";

    private Integer timeOut;
    private Integer socketTimeOut;
    private Integer connectionTimeOut;
    private Integer connectionRequestTimeOut;

    private transient Logger logger = GSLoggerFactory.getLogger(HttpRequestExecutor.class);

    public static boolean logEnabled = true;

    /**
     * 
     */
    public HttpRequestExecutor() {

	setTimeout(120000);
    }

    /**
     * @return
     */
    public Integer getTimeout() {

	return timeOut;
    }

    /**
     * @param timeOut
     */
    public void setTimeout(Integer timeOut) {

	this.timeOut = timeOut;
    }

    /**
     * @return
     */
    public Integer getSocketTimeOut() {

	return socketTimeOut;
    }

    /**
     * @param socketTimeOut
     */
    public void setSocketTimeOut(Integer socketTimeOut) {

	this.socketTimeOut = socketTimeOut;
    }

    /**
     * @return
     */
    public Integer getConnectionTimeOut() {

	return connectionTimeOut;
    }

    /**
     * @param connectionTimeOut
     */
    public void setConnectionTimeOut(Integer connectionTimeOut) {

	this.connectionTimeOut = connectionTimeOut;
    }

    /**
     * @return
     */
    public Integer getConnectionRequestTimeOut() {

	return connectionRequestTimeOut;
    }

    /**
     * @param connectionRequestTimeOut
     */
    public void setConnectionRequestTimeOut(Integer connectionRequestTimeOut) {

	this.connectionRequestTimeOut = connectionRequestTimeOut;
    }

    /**
     * Executes the {@link HttpRequestBase} creating the client (http and https supported)
     *
     * @param request
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public HttpResponse execute(HttpRequestBase request, String username, String password) throws ClientProtocolException, IOException {

	HttpClient client;

	URI requestURI = request.getURI();

	String loguri = requestURI.toString();

	if (logEnabled) {
	    logger.trace("Execution of {} STARTED", loguri);
	}

	URL requestURL = requestURI.toURL();
	if (requestURL.getProtocol().toLowerCase().equals("https")) {

	    client = createHttpClient(true, requestURL.getHost(), requestURL.getPort(), username, password);

	} else {
	    client = createHttpClient(false, requestURL.getHost(), requestURL.getPort(), username, password);
	}

	HttpClientContext context = HttpClientContext.create();

	HttpResponse response = client.execute(request, context);

	if (logEnabled) {
	    logger.trace("Execution of {} ENDED with code {}", loguri, response.getStatusLine().getStatusCode());
	}

	return response;

    }

    /**
     * Executes the {@link HttpRequestBase} creating the client (http and https supported)
     *
     * @param request
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public HttpResponse execute(HttpRequestBase request) throws ClientProtocolException, IOException {

	return execute(request, null, null);

    }

    private HttpClient createHttpClient(boolean https, String hostname, int port, String username, String password) {

	CloseableHttpClient client = null;

	HttpClientBuilder builder = HttpClientBuilder.create();

	if (username != null && password != null) {
	    CredentialsProvider credsProvider = new BasicCredentialsProvider();
	    credsProvider.setCredentials(new AuthScope(hostname, port), new UsernamePasswordCredentials(username, password));
	    builder.setDefaultCredentialsProvider(credsProvider);
	}

	if (https) {

	    SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
	    SSLContext sslContext = null;

	    try {

		TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
		    @Override
		    public boolean isTrusted(X509Certificate[] certificate, String authType) {
			return true;
		    }
		};
		sslContextBuilder.loadTrustMaterial(acceptingTrustStrategy);

		sslContext = sslContextBuilder.build();

	    } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException ex) {
		//
		// these kind of exceptions should not happen in a healthy environment
		//

		if (logEnabled) {
		    logger.error("Unable to set SSL context to client builder !!!", ex);
		}
	    }

	    if (sslContext != null) {

		SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

		builder = builder.setSSLSocketFactory(sslSocketFactory);

		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
			.register("https", sslSocketFactory).build();

		HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
		builder.setConnectionManager(ccm);
	    }
	}

	Builder custom = RequestConfig.custom();

	if (timeOut != null && timeOut > 0) {

	    custom = custom.setConnectTimeout(timeOut).//
		    setConnectionRequestTimeout(timeOut).//
		    setSocketTimeout(timeOut);
	} else {

	    if (connectionTimeOut != null && timeOut > 0) {

		custom = custom.setConnectTimeout(this.connectionTimeOut);
	    }

	    if (connectionRequestTimeOut != null && timeOut > 0) {

		custom = custom.setConnectionRequestTimeout(this.connectionRequestTimeOut);
	    }

	    if (socketTimeOut != null && timeOut > 0) {

		custom = custom.setSocketTimeout(this.socketTimeOut);
	    }
	}

	RequestConfig config = custom.build();
	builder = builder.setDefaultRequestConfig(config);

	DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy() {
	    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
		boolean isRedirect = false;
		try {
		    isRedirect = super.isRedirected(request, response, context);
		} catch (org.apache.http.ProtocolException e) {
		    e.printStackTrace();
		}
		if (!isRedirect) {
		    int responseCode = response.getStatusLine().getStatusCode();
		    if (responseCode == 301 || responseCode == 302) {
			return true;
		    }
		}
		return isRedirect;
	    }
	};

	builder = builder.setRedirectStrategy(redirectStrategy);
	client = builder.build();

	return client;
    }
}
