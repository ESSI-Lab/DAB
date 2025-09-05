package eu.essi_lab.gssrv.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.zip.GZIPUnzipper;

@SuppressWarnings("serial")
public class SparqlProxyServlet extends HttpServlet {

    static {

	try {

	    GSLoggerFactory.getLogger(SparqlProxyServlet.class).debug("Disabling certificate validation STARTED");

	    disableCertificateValidation();

	    GSLoggerFactory.getLogger(SparqlProxyServlet.class).debug("Disabling certificate validation ENDED");

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(SparqlProxyServlet.class).error(e);
	}
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	forwardRequest("GET", request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	forwardRequest("POST", request, response);
    }

    private void forwardRequest(String method, HttpServletRequest request, HttpServletResponse response) throws IOException {

	GSLoggerFactory.getLogger(getClass()).debug("Forwarding STARTED");

	String urlWithParams = ConfigurationWrapper.getSparqlProxyEndpoint();

	if ("GET".equalsIgnoreCase(method)) {

	    GSLoggerFactory.getLogger(getClass()).debug("GET Request handling STARTED");

	    String queryString = request.getQueryString();

	    if (queryString != null) {

		urlWithParams += "?" + queryString;
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Request url: {}", urlWithParams);

	    GSLoggerFactory.getLogger(getClass()).debug("GET Request handling ENDED");
	}

	GSLoggerFactory.getLogger(getClass()).debug("Creating URL STARTED");

	URL url = createURL(urlWithParams);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setRequestMethod(method);
	conn.setDoInput(true);
	conn.setDoOutput("POST".equalsIgnoreCase(method));

	GSLoggerFactory.getLogger(getClass()).debug("Creating URL ENDED");

	//
	// copy headers from original request to the forwarded request
	//

	GSLoggerFactory.getLogger(getClass()).debug("Copy headers from original request to forwarded request STARTED");

	Enumeration<String> headerNames = request.getHeaderNames();

	while (headerNames.hasMoreElements()) {

	    String headerName = headerNames.nextElement();

	    if (headerName != null && //

		    !headerName.equalsIgnoreCase("connection") && //
		    !headerName.equalsIgnoreCase("content-length") && //
		    !headerName.equalsIgnoreCase("host") && //
		    !headerName.equalsIgnoreCase("te") && //
		    !headerName.equalsIgnoreCase("trailer") && //
		    !headerName.equalsIgnoreCase("keep-alive") //
	    ) {

		String headerValue = request.getHeader(headerName);

		if (ConfigurationWrapper.forceSparqlProxyAcceptHeader() && headerName.toLowerCase().equals("accept")) {

		    headerValue = "application/sparql-results+json; charset=utf-8";
		}

		GSLoggerFactory.getLogger(getClass()).debug("Request header: {}:{}", headerName, headerValue);

		conn.setRequestProperty(headerName, headerValue);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Copy headers from original request to forwarded request ENDED");

	//
	// for POST, forward body
	//
	if ("POST".equalsIgnoreCase(method)) {

	    GSLoggerFactory.getLogger(getClass()).debug("Handling POST STARTED");

	    conn.setRequestProperty("Content-Type", request.getContentType());

	    try (OutputStream os = conn.getOutputStream(); InputStream is = request.getInputStream()) {
		is.transferTo(os);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Handling POST ENDED");
	}

	//
	// status code
	//

	GSLoggerFactory.getLogger(getClass()).debug("Setting status code STARTED");

	int status = conn.getResponseCode();
	response.setStatus(status);

	GSLoggerFactory.getLogger(getClass()).debug("Status code: {}", status);

	GSLoggerFactory.getLogger(getClass()).debug("Setting status code ENDED");

	//
	// headers to response
	//

	GSLoggerFactory.getLogger(getClass()).debug("Copy headers to response STARTED");

	Map<String, List<String>> headerFields = conn.getHeaderFields();
	Set<Entry<String, List<String>>> entrySet = headerFields.entrySet();

	for (Entry<String, List<String>> entry : entrySet) {
	    String headerName = entry.getKey();
	    String headerValue = entry.getValue().stream().collect(Collectors.joining(","));
	    if (headerName != null && headerValue != null && !headerValue.isEmpty()) {

		if (ConfigurationWrapper.forceSparqlProxyAcceptHeader() && headerName.toLowerCase().equals("content-type")) {
		    headerValue = "application/sparql-results+json; charset=utf-8";
		}

		switch (headerName.toLowerCase()) {
		// add here headers to be copied
		case "content-type":
		    // case "content-length": // this shouldn't be added!
		case "content-encoding":
		    // case "transfer-encoding": //this is automatically added, shouldn't be added explicitly
		case "date":
		case "server":
		case "last-modified":
		case "vary":
		case "retry-after":
		case "cache-control":
		case "pragma":

		{

		    GSLoggerFactory.getLogger(getClass()).debug("Copied header: {}:{}", headerName, headerValue);

		    response.setHeader(headerName, headerValue);

		    break;
		}
		default:
		    GSLoggerFactory.getLogger(getClass()).debug("Skipped header: {}:{}", headerName, headerValue);
		    break;
		}

	    }
	}
	
	response.setHeader("Content-Length", null); // needed

	GSLoggerFactory.getLogger(getClass()).debug("Copy headers to response ENDED");

	//
	// copy stream
	//

	GSLoggerFactory.getLogger(getClass()).debug("Handling connection stream STARTED");

	ClonableInputStream connStream = new ClonableInputStream(conn.getInputStream());

	try {
	    GZIPUnzipper unzipper = new GZIPUnzipper(connStream.clone());
	    File unzip = unzipper.unzip();
	    FileInputStream unzippedStream = new FileInputStream(unzip);

	    GSLoggerFactory.getLogger(getClass()).debug("Unzipped response stream:\n {}\n", IOStreamUtils.asUTF8String(unzippedStream));

	    unzippedStream.close();
	    unzip.delete();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	try (InputStream input = (status < 400 ? connStream.clone() : conn.getErrorStream());
		OutputStream out = response.getOutputStream()) {

	    if (input != null) {
		input.transferTo(out);
	    }

	    out.flush();

	} finally {

	    GSLoggerFactory.getLogger(getClass()).debug("Disconnect STARTED");

	    conn.disconnect();

	    GSLoggerFactory.getLogger(getClass()).debug("Disconnect ENDED");
	}

	GSLoggerFactory.getLogger(getClass()).debug("Handling connection stream ENDED");

	GSLoggerFactory.getLogger(getClass()).debug("Forwarding ENDED");
    }

    /**
     * @param urlWithParams
     * @return
     */
    private URL createURL(String urlWithParams) {

	try {
	    return new URI(urlWithParams).toURL();
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }

    /**
     * @throws Exception
     */
    private static void disableCertificateValidation() throws Exception {

	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	    public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	    }

	    public void checkClientTrusted(X509Certificate[] certs, String authType) {
	    }

	    public void checkServerTrusted(X509Certificate[] certs, String authType) {
	    }
	} };

	SSLContext sc = SSLContext.getInstance("TLS");
	sc.init(null, trustAllCerts, new SecureRandom());
	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}
