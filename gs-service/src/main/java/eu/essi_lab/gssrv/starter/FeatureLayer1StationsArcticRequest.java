package eu.essi_lab.gssrv.starter;

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
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;

public class FeatureLayer1StationsArcticRequest extends WebRequest {

    /**
     * 
     */
    private static final long serialVersionUID = 2582160357551181549L;

    public FeatureLayer1StationsArcticRequest() {
	super();
	try {
	    setServletRequest(new HttpServletRequest() {

		// "https://whos.geodab.eu/gs-service/services/essi/view/whos-arctic/ArcGIS/rest/services/WHOS/FeatureServer/1/query?f=json&returnIdsOnly=true&returnCountOnly=true&where=(1%3D1)&returnGeometry=false&spatialRel=esriSpatialRelIntersects&outSR=102100";

		@Override
		public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		   
		    return null;
		}

		@Override
		public AsyncContext startAsync() throws IllegalStateException {
		   
		    return null;
		}

		@Override
		public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		   

		}

		@Override
		public void setAttribute(String name, Object o) {
		   

		}

		@Override
		public void removeAttribute(String name) {
		   

		}

		@Override
		public boolean isSecure() {
		   
		    return false;
		}

		@Override
		public boolean isAsyncSupported() {
		   
		    return false;
		}

		@Override
		public boolean isAsyncStarted() {
		   
		    return false;
		}

		@Override
		public ServletContext getServletContext() {
		   
		    return null;
		}

		@Override
		public int getServerPort() {
		   
		    return 0;
		}

		@Override
		public String getServerName() {
		   
		    return null;
		}

		@Override
		public String getScheme() {
		   
		    return null;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String path) {
		   
		    return null;
		}

		@Override
		public int getRemotePort() {
		   
		    return 0;
		}

		@Override
		public String getRemoteHost() {
		   
		    return null;
		}

		@Override
		public String getRemoteAddr() {
		   
		    return null;
		}

		@Override
		public String getRealPath(String path) {
		   
		    return null;
		}

		@Override
		public BufferedReader getReader() throws IOException {
		   
		    return null;
		}

		@Override
		public String getProtocol() {
		   
		    return null;
		}

		@Override
		public String[] getParameterValues(String name) {
		   
		    return null;
		}

		@Override
		public Enumeration<String> getParameterNames() {
		   
		    return null;
		}

		@Override
		public Map<String, String[]> getParameterMap() {
		    Map<String, String[]> map = new HashMap<String, String[]>();
		    map.put("f", new String[] { "json" });
		    map.put("returnIdsOnly", new String[] { "true" });
		    map.put("returnCountOnly", new String[] { "true" });
		    map.put("where", new String[] { "(1=1)" });
		    map.put("returnGeometry", new String[] { "false" });
		    map.put("spatialRel", new String[] { "esriSpatialRelIntersects" });
		    map.put("outSR", new String[] { "102100" });
		    return map;
		}

		@Override
		public String getParameter(String name) {
		   
		    return null;
		}

		@Override
		public Enumeration<Locale> getLocales() {
		   
		    return null;
		}

		@Override
		public Locale getLocale() {
		   
		    return null;
		}

		@Override
		public int getLocalPort() {
		   
		    return 0;
		}

		@Override
		public String getLocalName() {
		   
		    return null;
		}

		@Override
		public String getLocalAddr() {
		   
		    return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
		   
		    return null;
		}

		@Override
		public DispatcherType getDispatcherType() {
		   
		    return null;
		}

		@Override
		public String getContentType() {
		   
		    return null;
		}

		@Override
		public long getContentLengthLong() {
		   
		    return 0;
		}

		@Override
		public int getContentLength() {
		   
		    return 0;
		}

		@Override
		public String getCharacterEncoding() {
		   
		    return null;
		}

		@Override
		public Enumeration<String> getAttributeNames() {
		   
		    return null;
		}

		@Override
		public Object getAttribute(String name) {
		   
		    return null;
		}

		@Override
		public AsyncContext getAsyncContext() {
		   
		    return null;
		}

		@Override
		public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		   
		    return null;
		}

		@Override
		public void logout() throws ServletException {
		   

		}

		@Override
		public void login(String username, String password) throws ServletException {
		   

		}

		@Override
		public boolean isUserInRole(String role) {
		   
		    return false;
		}

		@Override
		public boolean isRequestedSessionIdValid() {
		   
		    return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
		   
		    return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
		   
		    return false;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
		   
		    return false;
		}

		@Override
		public Principal getUserPrincipal() {
		   
		    return null;
		}

		@Override
		public HttpSession getSession(boolean create) {
		   
		    return null;
		}

		@Override
		public HttpSession getSession() {
		   
		    return null;
		}

		@Override
		public String getServletPath() {
		   
		    return null;
		}

		@Override
		public String getRequestedSessionId() {
		   
		    return null;
		}

		@Override
		public StringBuffer getRequestURL() {
		   
		    return null;
		}

		@Override
		public String getRequestURI() {
		   
		    return null;
		}

		@Override
		public String getRemoteUser() {
		   
		    return null;
		}

		@Override
		public String getQueryString() {
		   
		    return null;
		}

		@Override
		public String getPathTranslated() {
		   
		    return null;
		}

		@Override
		public String getPathInfo() {
		    return "/essi/view/whos-arctic/ArcGIS/rest/services/WHOS/FeatureServer/1/query";
		}

		@Override
		public Collection<Part> getParts() throws IOException, ServletException {
		   
		    return null;
		}

		@Override
		public Part getPart(String name) throws IOException, ServletException {
		   
		    return null;
		}

		@Override
		public String getMethod() {
		   
		    return null;
		}

		@Override
		public int getIntHeader(String name) {
		   
		    return 0;
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
		   
		    return null;
		}

		@Override
		public Enumeration<String> getHeaderNames() {
		   
		    return null;
		}

		@Override
		public String getHeader(String name) {
		   
		    return null;
		}

		@Override
		public long getDateHeader(String name) {
		   
		    return 0;
		}

		@Override
		public Cookie[] getCookies() {
		   
		    return null;
		}

		@Override
		public String getContextPath() {
		   
		    return null;
		}

		@Override
		public String getAuthType() {
		   
		    return null;
		}

		@Override
		public String changeSessionId() {
		   
		    return null;
		}

		@Override
		public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		   
		    return false;
		}
	    });
	} catch (IOException e) {
	    
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }

}
