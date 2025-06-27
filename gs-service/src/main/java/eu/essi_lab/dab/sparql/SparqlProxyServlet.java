package eu.essi_lab.dab.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

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

public class SparqlProxyServlet extends HttpServlet {

    // Configure your remote SPARQL endpoint here
    private static final String REMOTE_SPARQL_ENDPOINT = "http://localhost:3030/test/sparql";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	forwardRequest("GET", request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	forwardRequest("POST", request, response);
    }

    private void forwardRequest(String method, HttpServletRequest request, HttpServletResponse response)
	    throws IOException {

	String urlWithParams = REMOTE_SPARQL_ENDPOINT;
	if ("GET".equalsIgnoreCase(method)) {
	    String queryString = request.getQueryString();
	    if (queryString != null) {
		urlWithParams += "?" + queryString;
	    }
	}

	URL url = new URL(urlWithParams);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setRequestMethod(method);
	conn.setDoInput(true);
	conn.setDoOutput("POST".equalsIgnoreCase(method));

	// Copy headers
	Enumeration<String> headerNames = request.getHeaderNames();
	while (headerNames.hasMoreElements()) {
	    String headerName = headerNames.nextElement();
	    if (!headerName.equalsIgnoreCase("host") && !headerName.equalsIgnoreCase("content-length")) {
		conn.setRequestProperty(headerName, request.getHeader(headerName));
	    }
	}

	// For POST, forward body
	if ("POST".equalsIgnoreCase(method)) {
	    conn.setRequestProperty("Content-Type", request.getContentType());
	    try (OutputStream os = conn.getOutputStream(); InputStream is = request.getInputStream()) {
		is.transferTo(os);
	    }
	}

	// Relay response
	int status = conn.getResponseCode();
	response.setStatus(status);
	response.setContentType(conn.getContentType());

	try (InputStream input = (status < 400 ? conn.getInputStream() : conn.getErrorStream());
		OutputStream out = response.getOutputStream()) {
	    if (input != null) {
		input.transferTo(out);
	    }
	} finally {
	    conn.disconnect();
	}
    }
}
