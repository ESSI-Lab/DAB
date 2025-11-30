package eu.essi_lab.gssrv.rest;

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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

public class SimpleHttpServletRequest implements HttpServletRequest {

    private final URI uri;

    public SimpleHttpServletRequest(URI uri) {
	this.uri = uri;
    }

    @Override
    public String getMethod() {
	return "GET";
    }

    @Override
    public String getRequestURI() {
	return uri.getPath();
    }

    @Override
    public String getQueryString() {
	return uri.getQuery();
    }

    @Override
    public String getScheme() {
	return uri.getScheme();
    }

    @Override
    public String getServerName() {
	return uri.getHost();
    }

    @Override
    public int getServerPort() {
	return uri.getPort() == -1 ? 80 : uri.getPort();
    }

    @Override
    public Object getAttribute(String name) {
	return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
	return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
	// TODO Auto-generated method stub

    }

    @Override
    public int getContentLength() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public long getContentLengthLong() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public String getContentType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getParameter(String name) {
	if (name == null) {
	    return null;
	}

	// Use the parameter map to get all values
	String[] values = getParameterMap().get(name);

	// Return the first value if present
	return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
	String query = uri.getQuery(); // e.g., "id=7&name=Enrico"

	if (query == null || query.isEmpty()) {
	    return Collections.emptyEnumeration();
	}

	String[] pairs = query.split("&");
	List<String> names = new ArrayList<>();

	for (String pair : pairs) {
	    int idx = pair.indexOf("=");
	    if (idx > 0) {
		names.add(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8));
	    } else {
		// Parameter without value
		names.add(URLDecoder.decode(pair, StandardCharsets.UTF_8));
	    }
	}

	return Collections.enumeration(names);
    }

    @Override
    public String[] getParameterValues(String name) {
	if (name == null) {
	    return null;
	}
	// getParameterMap() already parses and caches all parameters
	Map<String, String[]> paramMap = getParameterMap();
	String[] values = paramMap.get(name);

	// Return a copy to prevent external modification
	return values != null ? Arrays.copyOf(values, values.length) : null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
	Map<String, String[]> parameterMap = new HashMap<>();
	String query = uri.getQuery(); // e.g., "id=7&name=Enrico&id=8"

	if (query == null || query.isEmpty()) {
	    return Collections.emptyMap();
	}

	String[] pairs = query.split("&");

	for (String pair : pairs) {
	    int idx = pair.indexOf('=');
	    String key;
	    String value;

	    if (idx > 0) {
		key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
		value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
	    } else {
		// Parameter without value
		key = URLDecoder.decode(pair, StandardCharsets.UTF_8);
		value = "";
	    }

	    // Append to array of values
	    parameterMap.merge(key, new String[] { value }, (oldValues, newValues) -> {
		String[] combined = new String[oldValues.length + 1];
		System.arraycopy(oldValues, 0, combined, 0, oldValues.length);
		combined[oldValues.length] = newValues[0];
		return combined;
	    });
	}

	return parameterMap;
    }

    @Override
    public String getProtocol() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public BufferedReader getReader() throws IOException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getRemoteAddr() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getRemoteHost() {
	// TODO Auto-generated method stub
	return null;
    }

    private HashMap<String, Object> attributes = new HashMap<>();

    @Override
    public void setAttribute(String name, Object o) {
	attributes.put(name, o);

    }

    @Override
    public void removeAttribute(String name) {
	attributes.remove(name);

    }

    @Override
    public Locale getLocale() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean isSecure() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getRealPath(String path) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int getRemotePort() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public String getLocalName() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getLocalAddr() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int getLocalPort() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public ServletContext getServletContext() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean isAsyncStarted() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean isAsyncSupported() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getAuthType() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Cookie[] getCookies() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public long getDateHeader(String name) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public String getHeader(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int getIntHeader(String name) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public String getPathInfo() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getPathTranslated() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getContextPath() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getRemoteUser() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean isUserInRole(String role) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public Principal getUserPrincipal() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getRequestedSessionId() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public StringBuffer getRequestURL() {
	String scheme = getScheme(); // "http" or "https"
	String serverName = getServerName(); // e.g., "example.com"
	int serverPort = getServerPort(); // e.g., 8080
	String requestURI = getRequestURI(); // e.g., "/app/api/items"

	StringBuffer url = new StringBuffer();
	url.append(scheme).append("://").append(serverName);

	boolean isDefaultPort = (scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443);

	if (!isDefaultPort && serverPort > 0) {
	    url.append(":").append(serverPort);
	}

	url.append(requestURI);
	return url;
    }

    @Override
    public String getServletPath() {
	return uri.getPath();
    }

    @Override
    public HttpSession getSession(boolean create) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public HttpSession getSession() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String changeSessionId() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {
	// TODO Auto-generated method stub

    }

    @Override
    public void logout() throws ServletException {
	// TODO Auto-generated method stub

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
	// TODO Auto-generated method stub
	return null;
    }

}
