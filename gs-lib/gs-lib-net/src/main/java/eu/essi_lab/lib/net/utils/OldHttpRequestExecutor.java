//package eu.essi_lab.lib.net.utils;

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
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.Authenticator;
//import java.net.PasswordAuthentication;
//import java.net.Socket;
//import java.net.URI;
//import java.net.URL;
//import java.net.http.HttpClient;
//import java.net.http.HttpClient.Builder;
//import java.net.http.HttpClient.Redirect;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.security.KeyStore;
//import java.security.SecureRandom;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.time.Duration;
//import java.time.temporal.ChronoUnit;
//import java.util.concurrent.TimeUnit;
//
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLEngine;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.TrustManagerFactory;
//import javax.net.ssl.X509ExtendedTrustManager;
//
//import eu.essi_lab.lib.utils.GSLoggerFactory;
//
///**
// * @author Fabrizio
// */
//public class Downloader {
//
//    private Long timeOut;
//
//    /**
//     * 
//     */
//    public Downloader() {
//
//	setTimeout(120000l);
//    }
//
//    /**
//     * @return milliseconds
//     */
//    public Long getTimeout() {
//
//	return timeOut;
//    }
//
//    /**
//     * @param timeOut milliseconds
//     */
//    public void setTimeout(Long timeOut) {
//
//	this.timeOut = timeOut;
//    }
//
//    /**
//     * @param timeOut milliseconds
//     */
//    public void setTimeout(Integer timeOut) {
//
//	setTimeout((long) timeOut);
//    }
//
//    /**
//     * @param timeUnit
//     * @param value
//     */
//    public void setTimeout(TimeUnit timeUnit, int value) {
//
//	setTimeout(timeUnit.toMillis(value));
//    }
//
//    /**
//     * @param request
//     * @param username
//     * @param password
//     * @return
//     * @throws IOException
//     * @throws InterruptedException
//     */
//    public HttpResponse<InputStream> execute(//
//	    HttpRequest request, //
//	    String username, //
//	    String password) throws IOException, InterruptedException {
//
//	return execute(request, username, password, null, null, null);
//    }
//
//    /**
//     * @param request
//     * @param username
//     * @param password
//     * @param keystore
//     * @param keystorePassword
//     * @param certificatePassword
//     * @return
//     * @throws IOException
//     * @throws InterruptedException
//     */
//    public HttpResponse<InputStream> execute(//
//	    HttpRequest request, //
//	    String username, //
//	    String password, //
//	    InputStream keystore, //
//	    String keystorePassword, //
//	    String certificatePassword) throws IOException, InterruptedException {
//
//	HttpClient client;
//
//	URI requestURI = request.uri();
//
//	String loguri = requestURI.toString();
//
//	GSLoggerFactory.getLogger(getClass()).trace("Execution of {} STARTED", loguri);
//
//	URL requestURL = requestURI.toURL();
//	if (requestURL.getProtocol().toLowerCase().equals("https")) {
//
//	    client = createHttpClient(true, requestURL.getHost(), requestURL.getPort(), username, password, keystore, keystorePassword,
//		    certificatePassword);
//
//	} else {
//	    client = createHttpClient(false, requestURL.getHost(), requestURL.getPort(), username, password, keystore, keystorePassword,
//		    certificatePassword);
//	}
//
//	// HttpClientContext context = HttpClientContext.create();
//
//	HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
//
//	GSLoggerFactory.getLogger(getClass()).trace("Execution of {} ENDED with code {}", loguri, response.statusCode());
//
//	return response;
//
//    }
//
//    /**
//     * @param request
//     * @return
//     * @throws IOException
//     * @throws InterruptedException
//     */
//    public HttpResponse<InputStream> execute(HttpRequest request) throws IOException, InterruptedException {
//
//	return execute(request, null, null);
//
//    }
//
//    /**
//     * @param https
//     * @param hostname
//     * @param port
//     * @param username
//     * @param password
//     * @param keystore
//     * @param keystorePassword
//     * @param certificatePassword
//     * @return
//     */
//    private HttpClient createHttpClient(//
//	    boolean https, //
//	    String hostname, //
//	    int port, //
//	    String username, //
//	    String password, //
//	    InputStream keystore, //
//	    String keystorePassword, //
//	    String certificatePassword) {
//
//	HttpClient client = null;
//
//	// HttpClientBuilder builder = HttpClientBuilder.create();
//
//	Builder builder = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2);
//
//	if (username != null && password != null) {
//
//	    // CredentialsProvider credsProvider = new BasicCredentialsProvider();
//
//	    // credsProvider.setCredentials(new AuthScope(hostname, port), new UsernamePasswordCredentials(username,
//	    // password));
//
//	    builder = builder.authenticator(new Authenticator() {
//		@Override
//		protected PasswordAuthentication getPasswordAuthentication() {
//		    return new PasswordAuthentication(username, password.toCharArray());
//		}
//	    });
//
//	    // builder.setDefaultCredentialsProvider(credsProvider);
//	}
//
//	if (https) {
//
//	    //
//	    // https://medium.com/javarevisited/java-http-client-invalid-certificate-93673415fdec
//	    //
//
//	    // SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
//
//	    try {
//
//		SSLContext sslContext = SSLContext.getInstance("SSL");
//
//		// TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
//		// @Override
//		// public boolean isTrusted(X509Certificate[] certificate, String authType) {
//		// return true;
//		// }
//		// };
//
//		TrustManager x509ExtendedTrustManager = new X509ExtendedTrustManager() {
//		    @Override
//		    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//			return new java.security.cert.X509Certificate[0];
//		    }
//
//		    @Override
//		    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
//			    throws CertificateException {
//		    }
//
//		    @Override
//		    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
//		    }
//
//		    @Override
//		    public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {
//		    }
//
//		    @Override
//		    public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) throws CertificateException {
//		    }
//
//		    @Override
//		    public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {
//
//		    }
//
//		    @Override
//		    public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) throws CertificateException {
//		    }
//		};
//
//		// sslContextBuilder = sslContextBuilder.loadTrustMaterial(acceptingTrustStrategy);
//
//		//
//		// https://stackoverflow.com/questions/52988677/allow-insecure-https-connection-for-java-jdk-11-httpclient
//		//
//		if (keystore != null) {
//
//		    if (certificatePassword == null) {
//			certificatePassword = "";
//		    }
//
//		    KeyStore keyStore = readStore(keystore, keystorePassword);
//
//		    KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
//		    kmf.init(keyStore, keystorePassword.toCharArray());
//
//		    TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
//		    tmf.init(keyStore);
//		    //
//		    // try {
//		    // sslContextBuilder = sslContextBuilder.loadKeyMaterial(readStore(keystore,keystorePassword),
//		    // certificatePassword.toCharArray());
//		    //
//		    // } catch (Exception e) {
//		    // e.printStackTrace();
//		    // return null;
//		    // }
//
//		    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
//
//		} else {
//
//		    sslContext.init(null, new TrustManager[] { x509ExtendedTrustManager }, new SecureRandom());
//		}
//
//		builder = builder.sslContext(sslContext);
//
//		// sslContext = sslContextBuilder.build();
//
//	    } catch (Exception ex) {
//
//		GSLoggerFactory.getLogger(getClass()).error(ex);
//	    }
//
//	    // if (sslContext != null) {
//	    //
//	    // SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, new
//	    // NoopHostnameVerifier());
//	    //
//	    // builder = builder.setSSLSocketFactory(sslSocketFactory);
//	    //
//	    // Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
//	    // .register("https", sslSocketFactory).build();
//	    //
//	    // HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
//	    // builder.setConnectionManager(ccm);
//	    // }
//	}
//
//	// Builder custom = RequestConfig.custom();
//
//	if (timeOut != null && timeOut > 0) {
//
//	    builder = builder.connectTimeout(Duration.of((long) timeOut, ChronoUnit.MILLIS));
//
//	    // custom = custom.setConnectTimeout(timeOut).//
//	    // setConnectionRequestTimeout(timeOut).//
//	    // setSocketTimeout(timeOut);
//	} else {
//
//	    // if (connectionTimeOut != null && timeOut > 0) {
//	    //
//	    // custom = custom.setConnectTimeout(this.connectionTimeOut);
//	    // }
//	    //
//	    // if (connectionRequestTimeOut != null && timeOut > 0) {
//	    //
//	    // custom = custom.setConnectionRequestTimeout(this.connectionRequestTimeOut);
//	    // }
//	    //
//	    // if (socketTimeOut != null && timeOut > 0) {
//	    //
//	    // custom = custom.setSocketTimeout(this.socketTimeOut);
//	    // }
//	}
//
//	// RequestConfig config = custom.build();
//	// builder = builder.setDefaultRequestConfig(config);
//	//
//	// DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy() {
//	//
//	// public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
//	//
//	// boolean isRedirect = false;
//	//
//	// try {
//	// isRedirect = super.isRedirected(request, response, context);
//	//
//	// } catch (org.apache.http.ProtocolException e) {
//	// e.printStackTrace();
//	// }
//	//
//	// if (!isRedirect) {
//	// int responseCode = response.getStatusLine().getStatusCode();
//	//
//	// if (responseCode == 301 || responseCode == 302) {
//	// return true;
//	// }
//	// }
//	// return isRedirect;
//	// }
//	// };
//
//	builder = builder.followRedirects(Redirect.NORMAL);
//
//	// builder = builder.setRedirectStrategy(redirectStrategy);
//
//	client = builder.build();
//
//	return client;
//    }
//
//    /**
//     * @param keyStoreStream
//     * @param keystorePassword
//     * @return
//     * @throws Exception
//     */
//    private KeyStore readStore(InputStream keyStoreStream, String keystorePassword) throws Exception {
//	if (keystorePassword == null) {
//	    keystorePassword = "";
//	}
//
//	KeyStore keyStore = KeyStore.getInstance("PKCS12"); // JKS or "PKCS12"
//	keyStore.load(keyStoreStream, keystorePassword.toCharArray());
//	keyStoreStream.close();
//	return keyStore;
//
//    }
//}
