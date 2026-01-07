package eu.essi_lab.gssrv.rest;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.essi_lab.authentication.OAuth2Authenticator;
import eu.essi_lab.authentication.OAuth2AuthenticatorFactory;
import eu.essi_lab.authentication.token.Token;
import eu.essi_lab.authentication.token.TokenProvider;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * AuthenticationService endpoints are exposed by this root path: <b>../auth/..</b><br>
 * Here are enpoints who expose services about users:<br>
 * -
 * login
 * <br>
 * - logout <br>
 * - callback about oauth/oauth2 authorization provider
 *
 * @author pezzati
 */
@WebService
@Path("/")
public class
AuthenticationService {

    private TokenProvider tokenProvider;

    private static final String DEFAULT_CLIENT_PATH = "conf";
    private static final String DEFAULT_LOGOUT_CLIENT_PATH = "";
    private static final String DETECTED_REQ_PORT = "Detected request port is {}: ";
    private static final String REMOVING_PORT_80 = "Removing port from callBack url (port is 80)";

    /**
     * 
     */
    public AuthenticationService() {

	tokenProvider = new TokenProvider();
    }

    /**
     * Provide redirection to authentication provider specified by url param.
     *
     * @param httpResponse response to redirect user.
     * @param oAuthProvider a code representing the desired oauth/oauth2 provider.
     * @return Response with status code:<br>
     *         - 200 when everything is fine,<br>
     *         - 404 when specified provider is not supported,<br>
     *         - 500
     *         when exception occurs.
     */
    @Path("/login/{provider}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response login(//
	    @Context HttpServletRequest httpRequest, //
	    @Context HttpServletResponse httpResponse, //
	    @PathParam("provider") String oAuthProvider, //
	    @QueryParam("url") String clienturl) {//

	try {

	    GSLoggerFactory.getLogger(AuthenticationService.class).info("Login request with provider {} and client url {} STARTED",
		    oAuthProvider, clienturl);

	    String callBackURL = builCallbackURL(httpRequest);

	    OAuthSetting setting = ConfigurationWrapper.getOAuthSetting();

	    OAuth2Authenticator authenticator = OAuth2AuthenticatorFactory.get(setting);

	    GSLoggerFactory.getLogger(AuthenticationService.class).debug("Authenticating with: {}", authenticator);

	    authenticator.configure(setting);

	    authenticator.setRedirectURI(new URI(callBackURL));

	    httpResponse.setHeader("Access-Control-Allow-Origin", "*");

	    authenticator.handleLogin(httpRequest, httpResponse, clienturl);

	    Response response = Response.ok().build();

	    GSLoggerFactory.getLogger(AuthenticationService.class).info("Login request with provider {} and client url {} ENDED",
		    oAuthProvider, clienturl);

	    return response;

	} catch (GSException ex) {

	    return createErrorResponse(ex);

	} catch (Exception ex) {

	    return createErrorResponse(//
		    GSException.createException(//
			    getClass(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    "AuthenticationServiceLoginError", //
			    ex));
	}
    }

    @Path("/callback/{provider}/{param1}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response callbackProviderParam(//
	    @Context HttpServletRequest httpRequest, //
	    @Context HttpServletResponse httpResponse, //
	    @PathParam("provider") String oAuthProvider, //
	    @PathParam("parameter") String parameter) {

	GSLoggerFactory.getLogger(AuthenticationService.class).info("Login callback with provider {} and path parameter {} STARTED",
		oAuthProvider, parameter);

	try {

	    Token token = getToken(httpRequest, oAuthProvider);

	    Response response = createSuccessRedirect(oAuthProvider, parameter, httpRequest, httpResponse, token);

	    GSLoggerFactory.getLogger(AuthenticationService.class).info("Login callback with provider {} and path parameter {} ENDED",
		    oAuthProvider, parameter);

	    return response;

	} catch (GSException ex) {

	    return createErrorResponse(ex);

	} catch (Exception ex) {

	    return createErrorResponse(//
		    GSException.createException(//
			    getClass(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    "AuthenticationServiceCallbackProviderParamError", //
			    ex));
	}
    }

    @Path("/callback/{provider}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response callbackProvider(//
	    @Context HttpServletRequest httpRequest, //
	    @Context HttpServletResponse httpResponse, //
	    @PathParam("provider") String oAuthProvider) { //

	GSLoggerFactory.getLogger(AuthenticationService.class).info("Login callback with provider {} STARTED", oAuthProvider);

	try {

	    Token token = getToken(httpRequest, oAuthProvider);

	    Response response = createSuccessRedirect(oAuthProvider, token.getClientURL(), httpRequest, httpResponse, token);

	    GSLoggerFactory.getLogger(AuthenticationService.class).info("Login callback with provider {} ENDED", oAuthProvider);

	    return response;

	} catch (GSException ex) {

	    return createErrorResponse(ex);

	} catch (Exception ex) {

	    return createErrorResponse(//
		    GSException.createException(//
			    getClass(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    "AuthenticationServiceCallbackProviderError", //
			    ex));
	}
    }

    /**
     * Set cookie to expire
     *
     * @param httpRequest ServletRequest bringing current session.
     * @return Response with status code 200.
     */
    @Path("/logout")
    @GET
    public Response logout(//
	    @Context HttpServletRequest httpRequest, //
	    @Context HttpServletResponse httpResponse, //
	    @QueryParam("url") String clienturl) {

	try {
	    GSLoggerFactory.getLogger(AuthenticationService.class).info("Logout requested with client url {} STARTED", clienturl);

	    httpResponse.setHeader("Set-Cookie", TokenProvider.USER_COOKIE_NAME + "=deleted;Path=/;Expires=Thu, 01 Jan 1970 00:00:00 GMT");

	    String redirect = clienturl;

	    if (redirect == null) {

		redirect = buildLogoutRedirectURL(httpRequest);
	    }

	    if(!redirect.startsWith("http://") && !redirect.startsWith("https://")) {

		throw new IllegalArgumentException("Invalid redirect URL: " + redirect);
	    }

	    if(!redirect.endsWith("gs-service/configuration/")) {

		throw new IllegalArgumentException("Invalid redirect URL: " + redirect);
	    }

	    httpResponse.sendRedirect(redirect);

	    Response response = Response.ok().build();

	    GSLoggerFactory.getLogger(AuthenticationService.class).info("Logout requested with client url {} ENDED", clienturl);

	    return response;

	} catch (Exception ex) {

	    return createErrorResponse(//
		    GSException.createException(//
			    getClass(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    "AuthenticationServiceLogoutError", //
			    ex));
	}
    }

    /**
     * @param httpRequest
     * @param oAuthProvider
     * @return
     * @throws GSException
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private Token getToken(HttpServletRequest httpRequest, String oAuthProvider)
	    throws GSException, MalformedURLException, URISyntaxException {

	OAuthSetting setting = ConfigurationWrapper.getOAuthSetting();

	OAuth2Authenticator authenticator = OAuth2AuthenticatorFactory.get(setting);

	GSLoggerFactory.getLogger(AuthenticationService.class).debug("Verifying with {}", authenticator);

	authenticator.configure(setting);

	String redirect = buildRedirectURL(httpRequest);

	authenticator.setRedirectURI(new URI(redirect));

	return authenticator.handleCallback(httpRequest);
    }

    /**
     * @param httpRequest
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private String builCallbackURL(HttpServletRequest httpRequest) throws MalformedURLException, URISyntaxException {

	StringBuffer url = httpRequest.getRequestURL();

	URL requestURL = new URI(url.toString()).toURL();

	String host = requestURL.getHost();

	int port = requestURL.getPort();

	GSLoggerFactory.getLogger(AuthenticationService.class).trace(DETECTED_REQ_PORT, port);

	String path = requestURL.getPath();

	String protocol = requestURL.getProtocol();

	if (isNotLocalHost(host)) {

	    protocol += "s";
	}

	String callBackURL = protocol + "://" + host + ":" + port + path.replace("login", "callback");

	GSLoggerFactory.getLogger(AuthenticationService.class).trace("Generated callback url is {}", callBackURL);

	if (port + 1 == 0) {

	    GSLoggerFactory.getLogger(AuthenticationService.class).trace(REMOVING_PORT_80);

	    callBackURL = protocol + "://" + host + path.replace("login", "callback");

	    GSLoggerFactory.getLogger(AuthenticationService.class).trace("New callback url is {}", callBackURL);
	}

	return callBackURL;
    }

    /**
     * @param httpRequest
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private String buildRedirectURL(HttpServletRequest httpRequest) throws MalformedURLException, URISyntaxException {

	StringBuffer url = httpRequest.getRequestURL();

	URL requestURL = new URI(url.toString()).toURL();

	String host = requestURL.getHost();

	int port = requestURL.getPort();

	GSLoggerFactory.getLogger(AuthenticationService.class).trace(DETECTED_REQ_PORT, port);

	String path = requestURL.getPath();

	String protocol = requestURL.getProtocol();

	if (isNotLocalHost(host)) {

	    protocol += "s";
	}

	String redirect = protocol + "://" + host + ":" + port + path;

	GSLoggerFactory.getLogger(AuthenticationService.class).trace("Generated redirect url is {}", redirect);

	if (port + 1 == 0) {

	    GSLoggerFactory.getLogger(AuthenticationService.class).trace(REMOVING_PORT_80);

	    redirect = protocol + "://" + host + path;

	    GSLoggerFactory.getLogger(AuthenticationService.class).trace("New redirect url is {}", redirect);
	}

	return redirect;
    }

    /**
     * @param httpRequest
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private String buildLogoutRedirectURL(HttpServletRequest httpRequest) throws MalformedURLException, URISyntaxException {

	StringBuffer url = httpRequest.getRequestURL();

	URL requestURL = new URI(url.toString()).toURL();

	String host = requestURL.getHost();

	int port = requestURL.getPort();

	String path = requestURL.getPath();

	String protocol = requestURL.getProtocol();

	if (!host.contains("localhost") && !host.contains("127.0.0.1")) {

	    protocol += "s";
	}

	return protocol + "://" + host + ":" + port + path.replace("auth/user/logout", DEFAULT_LOGOUT_CLIENT_PATH);
    }

    /**
     * @param oAuthProvider
     * @param redirect
     * @param httpRequest
     * @param httpResponse
     * @param token
     * @return
     * @throws IOException
     * @throws GSException
     * @throws URISyntaxException
     */
    private Response createSuccessRedirect(//
	    String oAuthProvider, //
	    String redirect, //
	    HttpServletRequest httpRequest, //
	    HttpServletResponse httpResponse, //
	    Token token) throws IOException, GSException, URISyntaxException {

	if (redirect == null) {

	    StringBuffer url = httpRequest.getRequestURL();

	    URL requestURL = new URI(url.toString()).toURL();

	    String host = requestURL.getHost();

	    int port = requestURL.getPort();

	    String path = requestURL.getPath();

	    String protocol = requestURL.getProtocol();

	    redirect = protocol + "://" + host + ":" + port + path.replace("auth/user/callback/" + oAuthProvider, DEFAULT_CLIENT_PATH);
	}

	httpResponse.setHeader("Set-Cookie", TokenProvider.USER_COOKIE_NAME + "=" + tokenProvider.getToken(token) + ";Path=/");

	if(!redirect.startsWith("http://") && !redirect.startsWith("https://")) {

	    throw new IllegalArgumentException("Invalid redirect URL: " + redirect);
	}

	if(!redirect.endsWith("gs-service/configuration/")) {

	    throw new IllegalArgumentException("Invalid redirect URL: " + redirect);
	}

	httpResponse.sendRedirect(redirect);

	return Response.ok().build();
    }

    /**
     * @param host
     * @return
     */
    private boolean isNotLocalHost(String host) {

	return !host.contains("localhost") && !host.contains("127.0.0.1") && !host.contains("essi-lab.eu");
    }

    /**
     * @param ex
     * @return
     */
    private Response createErrorResponse(GSException ex) {

	return Response.status(Response.Status.INTERNAL_SERVER_ERROR).//
		type(MediaType.APPLICATION_JSON_TYPE).//
		entity(ex.getErrorInfoList().get(0).toJSONObject().toString(3)).//
		build();
    }
}
