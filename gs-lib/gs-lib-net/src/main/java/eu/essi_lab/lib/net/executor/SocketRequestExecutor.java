package eu.essi_lab.lib.net.executor;

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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class SocketRequestExecutor extends HTTPExecutor {

    public static void main(String[] args) throws Exception {
	URL url = new URL("https://sigedac.meteorologia.gov.py/api/v1/observations_hourly");
	String host = url.getHost();
	int port = url.getPort();
	String path = url.getPath();
	String protocol = url.getProtocol();
	String body = "{\"date_start\":\"1899-12-31T00:00:00\",\"station_id\":\"8\",\"measure_element_id\":\"1\",\"date_end\":\"2029-12-30T00:00:00\",\"page\":1}";
	List<SimpleEntry<String, String>> headers = new ArrayList<>();
	headers.add(new SimpleEntry<String, String>("Content-Type", "application/json"));
	headers.add(new SimpleEntry<String, String>("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"));
	
	
	String httpMethod = "GET";

	if (port == -1) {
	    if (protocol.equals("http")) {
		port = 80;
	    } else if (protocol.equals("https")) {
		port = 443;
	    }
	}

	try (Socket socket = createSocket(protocol, host, port)) {
	    OutputStream out = socket.getOutputStream();

	    String requestBody = "";
	    if (body != null) {
		requestBody = new String(body);
	    }

	    String headersString = "";
	    for (SimpleEntry<String, String> header : headers) {
		headersString += header.getKey() + ": " + header.getValue() + "\r\n";
	    }

	    String request = httpMethod.toUpperCase() + " " + path + " HTTP/1.1\r\n" + "Host: " + host + "\r\n" + "Content-Length: "
		    + requestBody.length() + "\r\n" + headersString + "\r\n" + requestBody;

	    out.write(request.getBytes());

	    // Read and process the response from the server
	    InputStream in = socket.getInputStream();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

	    String line;
	    StringBuilder responseBuilder = new StringBuilder();

	    // Read the HTTP response headers
	    while ((line = reader.readLine()) != null && !line.isEmpty()) {
		System.out.println(line); // Print headers if needed
	    }

	    // Read the response body
	    while ((line = reader.readLine()) != null) {
		responseBuilder.append(line).append("\n");
	    }
	    socket.close();
	}

    }

    @Override
    public HTTPExecutorResponse execute(String urlString, String httpMethod, List<SimpleEntry<String, String>> headers, byte[] body)
	    throws Exception {

	URL url = new URL(urlString);

	HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

	connection.setRequestMethod("GET");

	try (InputStream inputStream = connection.getInputStream()) {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

	    String line;
	    StringBuilder responseBuilder = new StringBuilder();

	    // Read the HTTP response headers
	    while ((line = reader.readLine()) != null && !line.isEmpty()) {
		System.out.println(line); // Print headers if needed
	    }

	    // Read the response body
	    while ((line = reader.readLine()) != null) {
		responseBuilder.append(line).append("\n");
	    }
	}

	String host = url.getHost();
	int port = url.getPort();
	String path = url.getPath();
	String protocol = url.getProtocol();

	if (port == -1) {
	    if (protocol.equals("http")) {
		port = 80;
	    } else if (protocol.equals("https")) {
		port = 443;
	    }
	}

	try (Socket socket = createSocket(protocol, host, port)) {
	    OutputStream out = socket.getOutputStream();

	    String requestBody = "";
	    if (body != null) {
		requestBody = new String(body);
	    }

	    String headersString = "";
	    for (SimpleEntry<String, String> header : headers) {
		headersString += header.getKey() + ": " + header.getValue() + "\r\n";
	    }

	    String request = httpMethod.toUpperCase() + " " + path + " HTTP/1.1\r\n" + "Host: " + host + "\r\n" + "Content-Length: "
		    + requestBody.length() + "\r\n" + headersString + "\r\n" + requestBody;

	    out.write(request.getBytes());

	    // Read and process the response from the server
	    InputStream in = socket.getInputStream();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

	    String line;
	    StringBuilder responseBuilder = new StringBuilder();

	    // Read the HTTP response headers
	    while ((line = reader.readLine()) != null && !line.isEmpty()) {
		System.out.println(line); // Print headers if needed
	    }

	    // Read the response body
	    while ((line = reader.readLine()) != null) {
		responseBuilder.append(line).append("\n");
	    }

	    String responseBody = responseBuilder.toString();
	    System.out.println("Response Body:\n" + responseBody);

	    Integer responseCode = 0;
	    SimpleEntry<String, String>[] responseHeaders = null;
	    InputStream responseStream = new ByteArrayInputStream(responseBody.getBytes());
	    HTTPExecutorResponse ret = new HTTPExecutorResponse(responseCode, responseHeaders, responseStream);
	    return ret;

	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw e;
	}

    }

    private static Socket createSocket(String protocol, String host, int port) throws Exception {

	if (protocol.equals("https")) {

	    SSLContext sslContext = SSLContext.getInstance("TLS");

//	    sslContext.init(null, new TrustManager[] { new CustomTrustManager() }, null);

	    SSLSocketFactory socketFactory = sslContext.getSocketFactory();

	    // Create an SSL socket
	    SSLSocket socket = (SSLSocket) socketFactory.createSocket(host, port);

	    // Enable all supported SSL/TLS protocols
	    socket.setEnabledProtocols(socket.getSupportedProtocols());

	    // Handshake
	    socket.startHandshake();
	    return socket;

	}
	if (protocol.equals("http")) {
	    return new Socket(host, port);
	}
	throw new RuntimeException("Unexpected protocol: " + protocol);
    }



}
