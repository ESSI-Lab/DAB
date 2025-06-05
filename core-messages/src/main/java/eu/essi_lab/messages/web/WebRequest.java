package eu.essi_lab.messages.web;

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
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
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
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Message;

import eu.essi_lab.lib.net.utils.WritableHTTPServletRequest;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.rip.RuntimeInfoProvider;

/**
 * Encapsulates all the necessary objects to handle a Web request
 *
 * @author Fabrizio
 */
public class WebRequest implements RuntimeInfoProvider, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4249959411713525643L;
    /**
     *
     */
    public static final String GS_SERVICE_BASE_PATH = "/gs-service";

    /**
     * The default services path
     */
    public static final String SERVICES_PATH = "/services/essi/";

    /**
     * 
     */
    public static final String SEMANTIC_PATH = "semantic";
    /**
     * 
     */
    public static final String TOKEN_PATH = "token";
    /**
     * 
     */
    public static final String VIEW_PATH = "view";
    /**
     * 
     */
    public static final String VIEWS_PATH = "views";
    /**
     * 
     */
    public static final String HTTP_SERVLET_REQUEST_USER_ATTRIBUTE = "user";
    /**
     * 
     */
    public static final String CLIENT_IDENTIFIER_HEADER = "client_dentifier";
    public static final String ESSI_LAB_CLIENT_IDENTIFIER = "ESSILabClient";

    /**
     * 
     */
    public static final String X_FORWARDED_FOR_HEADER = "x-forwarded-for";

    /**
     * 
     */
    public static final String HTTP_SERVLET_REQUEST_ID_PROPERTY = "webRequestId";

    /**
     * 
     */
    public static String ORIGIN_HEADER = "origin";

    private transient WebServiceContext context;
    private transient UriInfo uriInfo;
    private transient HttpServletRequest servletRequest;
    private transient ClonableInputStream cloneStream;
    private transient SoapMessage soapMessage;
    private transient AsyncResponse ar;
    private transient HttpServletResponse response;
    private String queryString;
    private String servicesPath;
    private String requestId;

    private String profilerPath;
    private String requestContext;
    private String profilerName;

    /**
     * 
     */
    public WebRequest() {

	this(UUID.randomUUID().toString());
    }

    /**
     * @param id
     */
    public WebRequest(String id) {

	setServicesPath(SERVICES_PATH);
	setRequestId(id);
    }

    /**
     * @param servletRequest
     * @param readStream
     * @throws IOException
     */
    public WebRequest(HttpServletRequest servletRequest, boolean readStream) throws IOException {

	this(servletRequest, readStream, UUID.randomUUID().toString());
    }

    /**
     * @param servletRequest
     * @param readStream
     * @throws IOException
     */
    public WebRequest(HttpServletRequest servletRequest, boolean readStream, String id) throws IOException {

	setServicesPath(SERVICES_PATH);
	setRequestId(id);
	setServletRequest(servletRequest, readStream);
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> out = new HashMap<>();

	if (getProfilerName() != null) {

	    out.put(RuntimeInfoElement.PROFILER_NAME.getName(), Arrays.asList(getProfilerName()));
	}

	out.put(RuntimeInfoElement.WEB_REQUEST_TIME_STAMP.getName(), //
		Arrays.asList(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds()));

	out.put(RuntimeInfoElement.WEB_REQUEST_TIME_STAMP_MILLIS.getName(), //
		Arrays.asList(String.valueOf(System.currentTimeMillis())));

	GSUser user = getCurrentUser();
	if (user != null) {

	    String authProvider = user.getAuthProvider();
	    String identifier = user.getIdentifier();
	    String role = user.getRole();
	    String uri = user.getUri();

	    if (authProvider != null) {
		out.put(RuntimeInfoElement.USER_AUTH_PROVIDER.getName(), Arrays.asList(authProvider));
	    }

	    if (identifier != null) {
		out.put(RuntimeInfoElement.USER_IDENTIFIER.getName(), Arrays.asList(identifier));
	    }

	    if (role != null) {
		out.put(RuntimeInfoElement.USER_ROLE.getName(), Arrays.asList(role));
	    }

	    if (uri != null) {
		out.put(RuntimeInfoElement.USER_URI.getName(), Arrays.asList(uri));
	    }
	}

	Optional<String> viewId = this.extractViewId();
	viewId.ifPresent(id -> out.put(RuntimeInfoElement.VIEW_ID.getName(), Arrays.asList(id)));

	String queryString = this.getQueryString();
	if (StringUtils.isNotEmptyAndNotNull(queryString)) {

	    out.put(RuntimeInfoElement.WEB_REQUEST_QUERY_STRING.getName(), Arrays.asList(queryString));
	}

	String requestPath = this.getRequestPath();
	if (StringUtils.isNotEmptyAndNotNull(requestPath)) {

	    out.put(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), Arrays.asList(requestPath));
	}

	UriInfo uriInfo = this.getUriInfo();
	if (uriInfo != null) {

	    URI absolutePath = uriInfo.getAbsolutePath();
	    if (absolutePath != null) {

		out.put(RuntimeInfoElement.WEB_REQUEST_ABSOLUTE_PATH.getName(), Arrays.asList(absolutePath.toString()));
	    }

	    URI baseUri = uriInfo.getBaseUri();
	    if (baseUri != null) {

		out.put(RuntimeInfoElement.WEB_REQUEST_BASE_URI.getName(), Arrays.asList(baseUri.toString()));
	    }

	    URI requestUri = uriInfo.getRequestUri();
	    if (requestUri != null) {

		out.put(RuntimeInfoElement.WEB_REQUEST_REQUEST_URI.getName(), Arrays.asList(requestUri.toString()));
	    }
	}

	HttpServletRequest servletRequest = this.getServletRequest();
	if (servletRequest != null) {
	    //
	    //
	    // headers have at the moment these statistical elements:
	    //
	    // - WEB_REQUEST_host
	    // - WEB_REQUEST_origin
	    // - WEB_REQUEST_x-forwarded-for
	    //
	    //
	    Enumeration<String> headerNames = servletRequest.getHeaderNames();
	    if (headerNames != null) {
		while (headerNames.hasMoreElements()) {

		    String header = headerNames.nextElement();

		    List<String> values = Collections.list(servletRequest.getHeaders(header));
		    out.put(getName() + RuntimeInfoElement.NAME_SEPARATOR + header, values);
		}
	    }

	    String contentType = servletRequest.getContentType();
	    if (StringUtils.isNotEmptyAndNotNull(contentType)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_CONTENT_TYPE.getName(), Arrays.asList(contentType));
	    }

	    String characterEncoding = servletRequest.getCharacterEncoding();
	    if (StringUtils.isNotEmptyAndNotNull(characterEncoding)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_CHAR_ENCODING.getName(), Arrays.asList(characterEncoding));
	    }

	    long contentLengthLong = servletRequest.getContentLengthLong();
	    out.put(RuntimeInfoElement.WEB_REQUEST_CONTENT_LENGTH.getName(), Arrays.asList(String.valueOf(contentLengthLong)));

	    String localAddr = servletRequest.getLocalAddr();
	    if (StringUtils.isNotEmptyAndNotNull(localAddr)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_LOCAL_ADDRESS.getName(), Arrays.asList(String.valueOf(localAddr)));
	    }

	    String localName = servletRequest.getLocalName();
	    if (StringUtils.isNotEmptyAndNotNull(localName)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_LOCAL_NAME.getName(), Arrays.asList(localName));
	    }

	    int localPort = servletRequest.getLocalPort();
	    out.put(RuntimeInfoElement.WEB_REQUEST_LOCAL_PORT.getName(), Arrays.asList(String.valueOf(localPort)));

	    String method = servletRequest.getMethod();
	    if (StringUtils.isNotEmptyAndNotNull(method)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_METHOD.getName(), Arrays.asList(method));

		if (method.equals("POST")) {

		    try {
			String body = IOStreamUtils.asUTF8String(getBodyStream().clone());
			out.put(RuntimeInfoElement.WEB_REQUEST_BODY.getName(), Arrays.asList(body));

		    } catch (IOException e) {

			GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
		    }
		}
	    }

	    String protocol = servletRequest.getProtocol();
	    if (StringUtils.isNotEmptyAndNotNull(protocol)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_PROTOCOL.getName(), Arrays.asList(protocol));
	    }

	    String remoteAddr = servletRequest.getRemoteAddr();
	    if (StringUtils.isNotEmptyAndNotNull(remoteAddr)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_REMOTE_ADDRESS.getName(), Arrays.asList(remoteAddr));
	    }

	    String remoteHost = servletRequest.getRemoteHost();
	    if (StringUtils.isNotEmptyAndNotNull(remoteHost)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), Arrays.asList(remoteHost));
	    }

	    int remotePort = servletRequest.getRemotePort();
	    out.put(RuntimeInfoElement.WEB_REQUEST_REMOTE_PORT.getName(), Arrays.asList(String.valueOf(remotePort)));

	    String remoteUser = servletRequest.getRemoteUser();
	    if (StringUtils.isNotEmptyAndNotNull(remoteUser)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), Arrays.asList(remoteUser));
	    }

	    String scheme = servletRequest.getScheme();
	    if (StringUtils.isNotEmptyAndNotNull(scheme)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_SCHEME.getName(), Arrays.asList(scheme));
	    }

	    String serverName = servletRequest.getServerName();
	    if (StringUtils.isNotEmptyAndNotNull(serverName)) {

		out.put(RuntimeInfoElement.WEB_REQUEST_SERVER_NAME.getName(), Arrays.asList(serverName));
	    }

	    int serverPort = servletRequest.getServerPort();
	    out.put(RuntimeInfoElement.WEB_REQUEST_SERVER_PORT.getName(), Arrays.asList(String.valueOf(serverPort)));

	    Principal userPrincipal = servletRequest.getUserPrincipal();

	    if (userPrincipal != null) {
		String name = userPrincipal.getName();
		if (StringUtils.isNotEmptyAndNotNull(name)) {

		    out.put(RuntimeInfoElement.WEB_REQUEST_AUTHENTICATED_USER_NAME.getName(), Arrays.asList(serverName));
		}
	    }
	}

	return out;
    }

    @Override
    public String getName() {

	return "WEB_REQUEST";
    }

    /**
     * @param request
     * @return
     */
    public static GSUser getCurrentUser(HttpServletRequest request) {

	return (GSUser) request.getAttribute(HTTP_SERVLET_REQUEST_USER_ATTRIBUTE);
    }

    /**
     * @param user
     * @param request
     */
    public static void setCurrentUser(GSUser user, HttpServletRequest request) {

	request.setAttribute(WebRequest.HTTP_SERVLET_REQUEST_USER_ATTRIBUTE, user);
    }

    /**
     * Retrieves the complete host name from the supplied uri in the form <code>scheme://authority</code><br>
     * E.g:<br>
     * - <i>Request</i> -> http://localhost:9090/gs-service/services/essi/ssc/feed<br>
     * - <i>Complete host name</i> -> http://localhost:9090<br>
     * 
     * @param uri
     * @return
     */
    public static String retrieveCompleteHostName(URI uri) {

	String host = uri.getAuthority();
	String scheme = uri.getScheme();
	host = scheme + "://" + host;

	return host;
    }

    /**
     * @param servletRequest
     * @return
     */
    public static List<String> readXForwardedForHeaders(HttpServletRequest servletRequest) {

	ArrayList<String> list = new ArrayList<>();

	if (servletRequest != null) {

	    String header = servletRequest.getHeader(X_FORWARDED_FOR_HEADER);

	    if (StringUtils.isNotEmpty(header)) {

		String[] split = header.split(",");
		for (int i = 0; i < split.length; i++) {

		    list.add(split[i].trim());
		}
	    }
	}

	return list;
    }

    /**
     * @param request
     * @return
     */
    public static Optional<String> readOriginHeader(HttpServletRequest servletRequest) {

	if (servletRequest != null) {

	    String header = servletRequest.getHeader(ORIGIN_HEADER);

	    if (StringUtils.isNotEmpty(header)) {

		return Optional.of(header);
	    }
	}

	return Optional.empty();
    }

    /**
     * See {@link #retrieveCompleteHostName(URI)}
     * 
     * @return
     */
    public String retrieveCompleteHostName() {

	URI requestURI = null;

	UriInfo uriInfo = getUriInfo();
	if (Objects.nonNull(uriInfo)) {

	    requestURI = uriInfo.getRequestUri();

	} else {
	    try {
		requestURI = new URI(getServletRequest().getRequestURI());
	    } catch (URISyntaxException e) {
	    }
	}

	return retrieveCompleteHostName(requestURI);
    }

    /**
     * @return
     */
    public GSUser getCurrentUser() {

	return getCurrentUser(getServletRequest());
    }

    /**
     * @param profilerPath
     */
    public void setProfilerPath(String profilerPath) {

	this.profilerPath = profilerPath;
    }

    /**
     * @return
     */
    public String getProfilerPath() {

	return profilerPath;
    }

    /**
     * @param name
     */
    public void setProfilerName(String name) {

	this.profilerName = name;
    }

    /**
     * @return
     */
    public String getProfilerName() {

	return profilerName;
    }

    /**
     * @return
     */
    public Optional<String> readRemoteHostHeader() {

	HttpServletRequest servletRequest = this.getServletRequest();
	if (servletRequest != null) {

	    String remoteHost = servletRequest.getRemoteHost();

	    if (StringUtils.isNotEmpty(remoteHost)) {

		return Optional.of(remoteHost);
	    }
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public Optional<String> readClientIdentifierHeader() {

	HttpServletRequest servletRequest = this.getServletRequest();
	if (servletRequest != null) {

	    String identifier = servletRequest.getHeader(CLIENT_IDENTIFIER_HEADER);

	    if (StringUtils.isNotEmpty(identifier)) {

		return Optional.of(identifier);
	    }
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public List<String> readXForwardedForHeaders() {

	return readXForwardedForHeaders(this.getServletRequest());
    }

    /**
     * @return
     */
    public String getRequestContext() {

	return requestContext;
    }

    /**
     * @param message
     * @return
     */
    private String generateRequestContext() {

	Optional<String> origin = readOriginHeader();
	Optional<String> referer = readRefererHeader();
	String refererValue = null;

	if (referer.isPresent()) {

	    refererValue = referer.get();
	    try {
		URL url = new URL(refererValue);
		String protocol = url.getProtocol();
		String host = url.getHost();
		// this way it looks just like the origin
		refererValue = protocol + "://" + host;

	    } catch (MalformedURLException e) {
		GSLoggerFactory.getLogger(WebRequest.class).error(e.getMessage());
	    }
	}

	Optional<String> remoteHost = readRemoteHostHeader();

	String context = origin.isPresent() ? origin.get() : //
		Objects.nonNull(refererValue) ? refererValue : //
			remoteHost.orElse(getRequestId());

	return context;
    }

    /**
     * @return
     */
    public Optional<String> readOriginHeader() {

	return readHeader("origin");
    }

    /**
     * @return
     */
    public Optional<String> readRefererHeader() {

	return readHeader("referer");
    }

    /**
     * @return
     */
    private Optional<String> readHeader(String header) {

	HttpServletRequest servletRequest = this.getServletRequest();
	String value = null;
	if (Objects.nonNull(servletRequest)) {

	    value = servletRequest.getHeader(header);
	}

	return Optional.ofNullable(value);
    }

    /**
     * Set the base services path. It must begin and end with '/' (e.g: "/services/essi/"). It must be correctly set and
     * it must match the
     * request base services path in order to have a correct behavior during the path comparison of the
     * {@link GETRequestFilter}
     *
     * @param path
     */
    public void setServicesPath(String path) {

	this.servicesPath = path;
    }

    /**
     * @return
     */
    public String getServicesPath() {

	return servicesPath;
    }

    /**
     * Shortcut for {@link #setServletRequest(HttpServletRequest, boolean)} with boolean param set to <code>true</code>
     * 
     * @param servletRequest
     * @throws IOException
     */
    public void setServletRequest(HttpServletRequest servletRequest) throws IOException {

	setServletRequest(servletRequest, true);
    }

    /**
     * @param servletRequest
     * @param readStream
     * @throws IOException
     */
    public void setServletRequest(HttpServletRequest servletRequest, boolean readStream) throws IOException {

	this.servletRequest = servletRequest;

	if (readStream) {
	    InputStream inputStream = servletRequest.getInputStream();
	    if (inputStream != null) {
		cloneStream = new ClonableInputStream(inputStream);
	    }
	}

	this.requestContext = generateRequestContext();
    }

    /**
     * @return
     */
    public HttpServletRequest getServletRequest() {

	return servletRequest;
    }

    /**
     * @param response
     */
    public void setServletResponse(HttpServletResponse response) {

	this.response = response;
    }

    /**
     * @return
     */
    public HttpServletResponse getServletResponse() {

	return this.response;
    }

    /**
     * @param ar
     */
    public void setAsyncResponse(AsyncResponse ar) {

	this.ar = ar;
    }

    /**
     * @return
     */
    public AsyncResponse getAsincResponse() {

	return ar;
    }

    /**
     * @param uriInfo
     */
    public void setUriInfo(UriInfo uriInfo) {

	this.uriInfo = uriInfo;
    }

    /**
     * @return
     */
    public UriInfo getUriInfo() {

	return uriInfo;
    }

    /**
     * Overrides the current query string of {@link #getServletRequest()}getQueryString()
     *
     * @param filter
     */
    public void setQueryString(String queryString) {

	this.queryString = queryString;
    }

    /**
     * This method is a shortcut for
     * {@link #getServletRequest()}getQueryString().<br>
     * The returned value can be also set calling the {@link #setQueryString(String)} method
     * 
     * @deprecated use {@link #getOptionalQueryString()}
     * @return the query string if available, <code>null</code> otherwise
     * @see #isGetRequest()
     * @see #isPostRequest()
     */
    @Deprecated()
    public String getQueryString() {

	if (this.queryString != null) {

	    return queryString;
	}

	if (servletRequest != null) {

	    return servletRequest.getQueryString();
	}

	return null;
    }

    /**
     * This method is a shortcut for
     * {@link #getServletRequest()}getQueryString().<br>
     * The returned value can be also set calling the {@link #setQueryString(String)} method
     * 
     * @see #isGetRequest()
     * @see #isPostRequest()
     */
    public Optional<String> getOptionalQueryString() {

	return Optional.ofNullable(getQueryString());
    }

    /**
     * @return
     */
    public Optional<String> getURLDecodedQueryString() {

	Optional<String> queryString = getOptionalQueryString();

	if (queryString.isPresent()) {

	    return Optional.of(StringUtils.URLDecodeUTF8(queryString.get()));
	}

	return Optional.empty();
    }

    /**
     * This method retrieves the form data (if available) provided as body in the
     * "application/x-www-form-urlencoded" encoding, or as query
     * string in the request URL after the path. In the latter case, this method is equivalent to
     * {@link #getQueryString()}
     * 
     * @see #isPostFormRequest()
     * @see #isGetRequest()
     * @return
     */
    public Optional<String> getFormData() {

	if (isPostFormRequest()) {
	    try {
		ClonableInputStream bs = getBodyStream();
		if (bs != null) {
		    String ret = IOStreamUtils.asUTF8String(bs.clone());
		    return Optional.of(ret);
		}
	    } catch (IOException e) {
		// this should never happen
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }
	}

	String queryString = getQueryString();
	if (StringUtils.isNotNull(queryString) && !queryString.isEmpty()) {

	    return Optional.of(queryString);
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public boolean isPostFormRequest() {

	if (!isPostRequest()) {
	    return false;
	}

	String contentType = this.getServletRequest().getHeader(Message.CONTENT_TYPE);

	if (contentType != null) {
	    return contentType.contains(MediaType.APPLICATION_FORM_URLENCODED);
	}

	return false;
    }

    /**
     * Returns the path of the request following the services base path (e.g.: '/services/essi/') deprived of the last
     * '/': E.g:
     * <ul>
     * <li>current path: http://localhost/gs-service/services/essi/opensearch/description</li>
     * <li>request path: opensearch/description</li>
     * </ul>
     * If this request has an {@link UriInfo} set, this method is a shortcut of <code>getUriInfo().getPath()</code>,
     * otherwise it is
     * computed starting from {@link #getServletRequest()}
     *
     * @return
     */
    public String getRequestPath() {

	String requestPath = null;
	if (getUriInfo() != null) {
	    requestPath = getUriInfo().getPath();
	} else {

	    HttpServletRequest servletRequest = getServletRequest();
	    if (servletRequest != null) {

		String servletPath = servletRequest.getServletPath();
		if (servletPath != null) {

		    String profilerPath = servletPath.replace(servicesPath, "");
		    String pathInfo = servletRequest.getPathInfo();

		    requestPath = profilerPath + (pathInfo != null ? pathInfo : "");
		}
	    }
	}

	return requestPath;
    }

    /**
     * Return a {@link ClonableInputStream} instantiated with the {@link HttpServletRequest} input stream
     *
     * @return a {@link ClonableInputStream} instantiated with the {@link HttpServletRequest} input stream if available,
     *         <code>null</code> otherwise
     * @see #isGetRequest()
     * @see #isPostRequest()
     */
    public ClonableInputStream getBodyStream() {

	return cloneStream;
    }

    public void setSoapMessage(SoapMessage soapMessage) {

	this.soapMessage = soapMessage;
    }

    public SoapMessage getSoapMessage() {

	return soapMessage;
    }

    public void setContext(WebServiceContext context) {

	this.context = context;
    }

    public WebServiceContext getContext() {

	return context;
    }

    public Optional<String> extractQueryParameter(String key) {

	String queryString = getQueryString();

	if (queryString != null && queryString.contains(key + "=")) {

	    String split = queryString.split(key + "=")[1];

	    if (split.contains("&")) {

		return Optional.of(split.split("&")[0]);
	    }

	    return Optional.of(queryString.split(key + "=")[1]);
	}
	return Optional.empty();
    }

    /**
     * The more common way to express the token in GS-service is to put it in the first part of the path preceded by
     * "token" key in the
     * form /token/{tokenId}/service This method extracts token id from the request path.<br>
     * The other way is in the query string as value of the "token" key
     *
     * @return the tokenId or null if not found
     */
    public Optional<String> extractTokenId() {

	Optional<String> optionalToken = extractQueryParameter("token");

	if (optionalToken.isPresent()) {
	    return optionalToken;
	}

	String requestPath = getRequestPath();
	if (requestPath == null) {
	    return Optional.empty();
	}

	String[] split = requestPath.split("/");

	int index = Arrays.asList(split).indexOf("token") + 1;

	if (index > 0 && index < split.length) {

	    return Optional.of(split[index]);
	}

	return Optional.empty();
    }

    /**
     * The more common way to express the viewId in GS-service is to put it in the first part of the path (or after the
     * token path parameter) preceded by
     * "view" key in the
     * form /view/{viewId}/service This method extracts view id from the request path.<br>
     * The other way is in the query string as value of the "token" key
     *
     * @return the viewId or null if not found
     */
    public Optional<String> extractViewId() {

	String requestPath = getRequestPath();

	try {

	    if (requestPath != null) {

		String[] split = requestPath.split("/");

		if (split.length >= 2) {

		    for (int i = 0; i < split.length; i++) {

			//
			// the path "view" is included so the view id is in the next path segment
			//

			if (split[i].equals(VIEW_PATH)) {

			    return Optional.of(split[i + 1]);
			}

			//
			// the path "views" is included and it is not the last, so the last is the view id
			//
			if (split[i].equals(VIEWS_PATH) && split.length == i + 2) {

			    return Optional.of(split[i + 1]);
			}
		    }
		}
	    }

	    //
	    // we try in the query string
	    //
	    Optional<String> optionalView = extractQueryParameter("view");

	    if (optionalView.isPresent()) {

		return optionalView;
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(//
		    "Error occurred '{}' trying to extract view id from request path: {}", //
		    ex.getMessage(), //
		    requestPath);
	}

	return Optional.empty();
    }

    /**
     * Returns <code>true</code> if the path of this request contains the
     * semantic path
     */
    public boolean hasSemanticPath() {

	String requestPath = getRequestPath();
	return requestPath.contains(SEMANTIC_PATH);
    }

    public boolean isGetRequest() {

	return isMethod("get");
    }

    public boolean isOptionsRequest() {

	return isMethod("options");
    }

    public boolean isDeleteRequest() {

	return isMethod("delete");
    }

    public boolean isPostRequest() {

	return isMethod("post");
    }

    public boolean isPutRequest() {

	return isMethod("put");
    }

    /**
     * @return
     */
    public String getRequestId() {

	return requestId;
    }

    /**
     * @param id
     */
    public void setRequestId(String id) {

	this.requestId = id;
    }

    public String getRemoteAddress() {
	// in case proxys are used by the client
	// or GI-suite is deployed behind a load balancer
	List<String> forwarded = readXForwardedForHeaders();
	if (!forwarded.isEmpty()) {
	    // the first is always the original client ip, followed by the proxy / load balancer ips
	    return forwarded.get(0);
	}
	// otherwise return the direct ip address
	return getServletRequest().getRemoteAddr();
    }

    /**
     * Creates a "mocked" GET request with the given <code>requestUrl</code>.<br>
     * <i>This method should be used for test purpose</i>
     *
     * @param requestUrl
     * @return
     */
    public static WebRequest createGET(String requestUrl) {

	return create(requestUrl, null, null, "GET");
    }

    /**
     * Creates a "mocked" GET request with the given <code>requestUrl</code> and <code>remoteAddress</code>.<br>
     * <i>This method should be used for test purpose</i>
     *
     * @param requestUrl
     * @return
     */
    public static WebRequest createGET(String requestUrl, String remoteAddress) {

	return create(requestUrl, remoteAddress, null, "GET");
    }

    /**
     * Creates a "mocked" GET request with the given <code>requestUrl</code> and <code>remoteAddress</code>.<br>
     * <i>This method should be used for test purpose</i>
     *
     * @param requestUrl
     * @return
     */
    public static WebRequest createGET(String requestUrl, HashMap<String, String> headers) {

	return create(requestUrl, null, headers, "GET");
    }

    /**
     * Creates a "mocked" GET request with the given <code>requestUrl</code> and <code>remoteAddress</code>.<br>
     * <i>This method should be used for test purpose</i>
     *
     * @param requestUrl
     * @return
     */
    public static WebRequest createGET(String requestUrl, String remoteAddress, HashMap<String, String> headers) {

	return create(requestUrl, remoteAddress, headers, "GET");
    }

    /**
     * Creates a "mocked" DELETE request with the given <code>requestUrl</code>.<br>
     * <i>This method should be used for test purpose</i>
     *
     * @param requestUrl
     * @return
     */
    public static WebRequest createDELETE(String requestUrl) {

	return create(requestUrl, null, null, "DELETE");
    }

    /**
     * Creates a "mocked" DELETE request with the given <code>requestUrl</code> and <code>remoteAddress</code>.<br>
     * <i>This method should be used for test purpose</i>
     *
     * @param requestUrl
     * @return
     */
    public static WebRequest createDELETE(String requestUrl, String remoteAddress) {

	return create(requestUrl, remoteAddress, null, "DELETE");
    }

    /**
     * Creates a "mocked" DELETE request with the given <code>requestUrl</code> and <code>remoteAddress</code>.<br>
     * <i>This method should be used for test purpose</i>
     *
     * @param requestUrl
     * @return
     */
    public static WebRequest createDELETE(String requestUrl, HashMap<String, String> headers) {

	return create(requestUrl, null, headers, "DELETE");
    }

    /**
     * Creates a "mocked" DELETE request with the given <code>requestUrl</code> and <code>remoteAddress</code>.<br>
     * <i>This method should be used for test purpose</i>
     *
     * @param requestUrl
     * @return
     */
    public static WebRequest createDELETE(String requestUrl, String remoteAddress, HashMap<String, String> headers) {

	return create(requestUrl, remoteAddress, headers, "DELETE");
    }

    /**
     * Creates a "mocked" POST request with the given <code>body</code>.<br>
     * <i>This method should be used for test purpose</i>
     *
     * @param body
     * @param requestUrl
     * @return
     */
    public static WebRequest createPOST(String requestUrl, InputStream body) {

	return createWithBoby(requestUrl, body, "POST");
    }

    /**
     * Creates a "mocked" PUT request with the given <code>body</code>.<br>
     * <i>This method should be used for test purpose</i>
     *
     * @param body
     * @param requestUrl
     * @return
     */
    public static WebRequest createPUT(String requestUrl, InputStream body) {

	return createWithBoby(requestUrl, body, "PUT");
    }

    private static HashMap<String, Object> attributes;

    /**
     * @param method
     * @return
     */
    private boolean isMethod(String method) {

	if (servletRequest != null) {

	    String servletMethod = servletRequest.getMethod();
	    if (servletMethod != null) {

		return servletMethod.equalsIgnoreCase(method);
	    }
	}

	if (context != null) {

	    return context.getMessageContext().get(MessageContext.HTTP_REQUEST_METHOD).toString().equalsIgnoreCase(method);
	}

	return false;
    }

    /**
     * @param requestUrl
     * @param body
     * @return
     */
    private static WebRequest createWithBoby(String requestUrl, InputStream body, String method) {

	return new WebRequest() {

	    private static final long serialVersionUID = 1244759601771325810L;
	    private ClonableInputStream is;

	    @Override
	    public ClonableInputStream getBodyStream() {

		if (is == null) {
		    try {
			is = new ClonableInputStream(body);
		    } catch (IOException e) {

			GSLoggerFactory.getLogger(getClass()).error("Error cloning body {}", e);
		    }
		}

		return is;
	    }

	    @Override
	    public HttpServletRequest getServletRequest() {

		String query = null;
		try {
		    URL url = new URL(requestUrl);
		    query = url.getQuery();
		} catch (MalformedURLException e) {
		    GSLoggerFactory.getLogger(getClass()).error("Bad URI {}", requestUrl, e);
		}

		InputStream clone = getBodyStream().clone();

		return createServletRequest(requestUrl, query, method, clone, "");
	    }

	    @Override
	    public String getQueryString() {

		String query = null;
		try {
		    URL url = new URL(requestUrl);
		    query = url.getQuery();
		} catch (MalformedURLException e) {
		    GSLoggerFactory.getLogger(getClass()).error("Malformed URI {}", requestUrl, e);
		}

		return query;
	    }

	    @Override
	    public UriInfo getUriInfo() {

		return createUriInfo(requestUrl, getServicesPath());
	    }

	    @Override
	    public boolean isGetRequest() {

		return false;
	    }

	    @Override
	    public boolean isPostRequest() {

		return true;
	    }
	};
    }

    /**
     * @param requestUrl
     * @param remoteAddress
     * @param headers
     * @param method
     * @return
     */
    private static WebRequest create(String requestUrl, String remoteAddress, HashMap<String, String> headers, String method) {

	return new WebRequest() {

	    private static final long serialVersionUID = 7003070247488505800L;

	    @Override
	    public HttpServletRequest getServletRequest() {

		String query = null;
		try {
		    URL url = new URL(requestUrl);
		    query = url.getQuery();
		} catch (MalformedURLException e) {
		    GSLoggerFactory.getLogger(getClass()).error("Can't create URL {}", requestUrl, e);
		}

		HttpServletRequest request = createServletRequest(requestUrl, query, method, null, remoteAddress);

		if (headers != null) {

		    WritableHTTPServletRequest wrapper = new WritableHTTPServletRequest(request);

		    headers.keySet().forEach(header -> {

			wrapper.putHeader(header, headers.get(header));
		    });

		    request = wrapper;
		}

		return request;
	    }

	    @Override
	    public String getQueryString() {

		String query = null;
		try {
		    URL url = new URL(requestUrl);
		    query = url.getQuery();
		} catch (MalformedURLException e) {
		    GSLoggerFactory.getLogger(getClass()).error("Bad url {}", requestUrl, e);
		}

		return query;
	    }

	    @Override
	    public UriInfo getUriInfo() {

		return createUriInfo(requestUrl, getServicesPath());
	    }

	    @Override
	    public boolean isGetRequest() {

		return method.toUpperCase().equals("GET");
	    }

	    @Override
	    public boolean isPostRequest() {

		return false;
	    }

	    public boolean isDeleteRequest() {

		return method.toUpperCase().equals("DELETE");
	    }
	};
    }

    private static HttpServletRequest createServletRequest(//
	    String requestURL, //
	    String queryString, //
	    String method, //
	    InputStream stream, //
	    String remoteAddress) {

	return new HttpServletRequest() {

	    @Override
	    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public AsyncContext startAsync() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public void setAttribute(String name, Object o) {

		if (attributes == null) {
		    attributes = new HashMap<>();
		}

		attributes.put(name, o);
	    }

	    @Override
	    public void removeAttribute(String name) {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	    }

	    @Override
	    public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	    }

	    @Override
	    public String getRemoteHost() {
		// TODO Auto-generated method stub
		return remoteAddress == null || remoteAddress.isEmpty() ? null : remoteAddress;
	    }

	    @Override
	    public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getRealPath(String path) {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String[] getParameterValues(String name) {
		// TODO Auto-generated method stub
		return new String[0];
	    }

	    @Override
	    public Enumeration<String> getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public Map<String, String[]> getParameterMap() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getParameter(String name) {

		KeyValueParser parser = new KeyValueParser(queryString);

		String value = parser.getValue(name);

		if (value != null && !value.equals(KeyValueParser.UNDEFINED)) {

		    return value;
		}

		return null;
	    }

	    @Override
	    public Enumeration<Locale> getLocales() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public int getLocalPort() {
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
	    public ServletInputStream getInputStream() throws IOException {

		return new ServletInputStream() {

		    @Override
		    public int read() throws IOException {

			return stream.read();
		    }

		    @Override
		    public void setReadListener(ReadListener readListener) {
			// TODO Auto-generated method stub
		    }

		    @Override
		    public boolean isReady() {
			// TODO Auto-generated method stub
			return true;
		    }

		    @Override
		    public boolean isFinished() {
			// TODO Auto-generated method stub
			return false;
		    }
		};
	    }

	    @Override
	    public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public long getContentLengthLong() {
		// TODO Auto-generated method stub
		return 0;
	    }

	    @Override
	    public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	    }

	    @Override
	    public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public Object getAttribute(String name) {

		if (attributes == null) {
		    return null;
		}

		return attributes.get(name);
	    }

	    @Override
	    public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public void logout() throws ServletException {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub

	    }

	    @Override
	    public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	    }

	    @Override
	    public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
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
	    public String getServletPath() {
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

		String out = requestURL;

		try {
		    URL url = new URL(requestURL);
		    String query = url.getQuery();

		    if (query != null) {

			out = requestURL.replace("?" + query, "");
		    }
		} catch (MalformedURLException e) {
		    e.printStackTrace();
		}

		return new StringBuffer(out);
	    }

	    @Override
	    public String getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getQueryString() {

		return queryString;
	    }

	    @Override
	    public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	    }

	    @Override
	    public Part getPart(String name) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getMethod() {

		return method;
	    }

	    @Override
	    public int getIntHeader(String name) {
		// TODO Auto-generated method stub
		return 0;
	    }

	    @Override
	    public Enumeration<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public Enumeration<String> getHeaderNames() {

		return Collections.enumeration(Arrays.asList());
	    }

	    @Override
	    public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public long getDateHeader(String name) {
		// TODO Auto-generated method stub
		return 0;
	    }

	    @Override
	    public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return new Cookie[0];
	    }

	    @Override
	    public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	    }
	};
    }

    private static UriInfo createUriInfo(String requestUrl, String servicesPath) {

	return new UriInfo() {

	    @Override
	    public URI resolve(URI uri) {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public URI relativize(URI uri) {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public UriBuilder getRequestUriBuilder() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public URI getRequestUri() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public MultivaluedMap<String, String> getQueryParameters() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public List<PathSegment> getPathSegments(boolean decode) {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	    }

	    @Override
	    public List<PathSegment> getPathSegments() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	    }

	    @Override
	    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public MultivaluedMap<String, String> getPathParameters() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public String getPath(boolean decode) {

		URL url = null;
		try {
		    url = new URL(requestUrl);
		} catch (MalformedURLException e) {
		    GSLoggerFactory.getLogger(getClass()).error("Malformed URI {}", requestUrl, e);

		    return null;
		}

		String path = url.getPath();
		path = path.replace(GS_SERVICE_BASE_PATH + servicesPath, "");
		if (path.startsWith("/")) {
		    path = path.substring(1, path.length());
		}
		return path;
	    }

	    @Override
	    public String getPath() {

		return getPath(true);
	    }

	    @Override
	    public List<String> getMatchedURIs(boolean decode) {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	    }

	    @Override
	    public List<String> getMatchedURIs() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	    }

	    @Override
	    public List<Object> getMatchedResources() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	    }

	    @Override
	    public UriBuilder getBaseUriBuilder() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public URI getBaseUri() {

		try {
		    return new URI(requestUrl);
		} catch (URISyntaxException e) {
		    e.printStackTrace();
		}

		return null;
	    }

	    @Override
	    public UriBuilder getAbsolutePathBuilder() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    public URI getAbsolutePath() {

		try {
		    return new URI("http://mocked-uri");
		} catch (URISyntaxException e) {
		    GSLoggerFactory.getLogger(getClass()).error(" Bad URI syntax ", e);
		}
		return null;
	    }
	};

    }

}
