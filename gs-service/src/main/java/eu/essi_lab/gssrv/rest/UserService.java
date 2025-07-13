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

import java.io.IOException;
import java.net.URI;
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

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.authentication.OAuthAuthenticator;
import eu.essi_lab.authentication.OAuthAuthenticatorFactory;
import eu.essi_lab.authentication.model.Token;
import eu.essi_lab.authentication.util.TokenProvider;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * UserService endpoints are exposed by this root path: <b>../auth/..</b><br>
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
public class UserService {

    private Logger logger = GSLoggerFactory.getLogger(UserService.class);

    private static JsonNode authConf;
    private TokenProvider tokenProvider;

    private static final String DEFAULT_CLIENT_PATH = "conf";
    private static final String DEFAULT_LOGOUT_CLIENT_PATH = "";
    private static final String DETECTED_REQ_PORT = "Detected request port is {}";
    private static final String OAUTH_NOT_CONFIGURED = "OAuth not configured";
    private static final String REMOVING_PORT_80 = "Removing port from callBack url (port is 80)";

    private static final String USER_SERVICE_OAUTH_NOT_CONFIGURED_ERROR = "USER_SERVICE_OAUTH_NOT_CONFIGURED_ERROR";
    private static final String USER_SERVICE_LOGIN_ERROR = "USER_SERVICE_LOGIN_ERROR";
    private static final String USER_SERVICE_CALLBACK_ERROR = "USER_SERVICE_CALLBACK_ERROR";
    private static final String USER_SERVICE_LOGOUT_ERROR = "USER_SERVICE_LOGOUT_ERROR";
    
    /**
     * 
     */
    public UserService() {

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
	    @Context HttpServletResponse httpResponse, @PathParam("provider") String oAuthProvider, // this param is
												    // ignored, it must
												    // be consistent
												    // with the provider
												    // set in the
												    // configuration
	    @QueryParam("url") String clienturl) {

	logger.info("Login Request with provider {} and client url {}", oAuthProvider, clienturl);

	try {

	    StringBuffer url = httpRequest.getRequestURL();

	    URL requestURL = new URL(url.toString());

	    String host = requestURL.getHost();

	    int port = requestURL.getPort();

	    logger.trace(DETECTED_REQ_PORT, port);

	    String path = requestURL.getPath();

	    String protocol = requestURL.getProtocol();

	    if (!host.contains("localhost") && !host.contains("127.0.0.1")) {

		protocol += "s";
	    }

	    String callBackURL = protocol + "://" + host + ":" + port + path.replace("login", "callback");

	    logger.trace("Generated callback url is {}", callBackURL);

	    if (port + 1 == 0) {

		logger.trace(REMOVING_PORT_80);

		callBackURL = protocol + "://" + host + path.replace("login", "callback");

		logger.trace("New callback url is {}", callBackURL);
	    }

	    OAuthSetting setting = ConfigurationWrapper.getOAuthSetting();

	    if (setting != null) {

		OAuthAuthenticator authenticator = OAuthAuthenticatorFactory.getOAuthAuthenticator(setting);

		logger.debug("Authenticating with {}", authenticator);

		JsonNode conf = getConf();

		if (conf != null) {
		    authenticator.initialize(conf.get(oAuthProvider));

		    authenticator.setRedirectURI(new URI(callBackURL));

		    httpResponse.setHeader("Access-Control-Allow-Origin", "*");

		    authenticator.handleLogin(httpRequest, httpResponse, clienturl);

		    return Response.ok().build();
		}
	    }

	    throw GSException.createException(//
		    this.getClass(), //
		    OAUTH_NOT_CONFIGURED, //
		    null, //
		    "This functionality was not configured, please contact the administrator to fix this issue", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    USER_SERVICE_OAUTH_NOT_CONFIGURED_ERROR);

	} catch (GSException ex) {

	    return createErrorResponse(ex);

	} catch (Exception ex) {

	    return createErrorResponse(//
		    GSException.createException(//
			    getClass(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    USER_SERVICE_LOGIN_ERROR, //
			    ex));
	}
    }

    @Path("/callback/{provider}/{param1}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response callbackProviderParam(//
	    @Context HttpServletRequest httpRequest, //
	    @Context HttpServletResponse httpResponse, //
	    @PathParam("provider") String oAuthProvider, // this param is ignored, it must be consistent with the
							 // provider set in the configuration
	    @PathParam("param1") String param1) {

	logger.info("Login Callback with provider {} and path parameter {}", oAuthProvider, param1);

	try {

	    OAuthSetting setting = ConfigurationWrapper.getOAuthSetting();

	    if (setting != null) {

		OAuthAuthenticator authenticator = OAuthAuthenticatorFactory.getOAuthAuthenticator(setting);

		logger.debug("Verifying with {}", authenticator);

		JsonNode conf = getConf();

		if (conf != null) {

		    authenticator.initialize(conf.get(oAuthProvider));

		    StringBuffer url = httpRequest.getRequestURL();

		    URL requestURL = new URL(url.toString());

		    String host = requestURL.getHost();

		    int port = requestURL.getPort();

		    logger.trace(DETECTED_REQ_PORT, port);

		    String path = requestURL.getPath();

		    String protocol = requestURL.getProtocol();

		    if (!host.contains("localhost") && !host.contains("127.0.0.1")) {

			protocol += "s";
		    }

		    String redirect = protocol + "://" + host + ":" + port + path;

		    logger.trace("Generated redirect url is {}", redirect);

		    if (port + 1 == 0) {
			logger.trace(REMOVING_PORT_80);

			redirect = protocol + "://" + host + path;

			logger.trace("New redirect url is {}", redirect);
		    }

		    authenticator.setRedirectURI(new URI(redirect));

		    Token token = authenticator.handleCallback(httpRequest);

		    return createSuccessRedirect(oAuthProvider, param1, httpRequest, httpResponse, token);
		}
	    }

	    throw GSException.createException(this.getClass(), OAUTH_NOT_CONFIGURED, null,
		    "This functionality was not configured, please " + "contact the administrator to fix this issue",
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, USER_SERVICE_OAUTH_NOT_CONFIGURED_ERROR);

	} catch (GSException ex) {

	    return createErrorResponse(ex);

	} catch (Exception ex) {

	    return createErrorResponse(//
		    GSException.createException(//
			    getClass(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    USER_SERVICE_CALLBACK_ERROR, //
			    ex));
	}
    }

    @Path("/callback/{provider}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response callbackProvider(//
	    @Context HttpServletRequest httpRequest, //
	    @Context HttpServletResponse httpResponse, //
	    @PathParam("provider") String oAuthProvider) { // this param is ignored, it must be consistent with the
							   // provider set in the configuration

	logger.info("Login Callback with provider {}", oAuthProvider);

	try {

	    OAuthSetting setting = ConfigurationWrapper.getOAuthSetting();

	    if (setting != null) {

		OAuthAuthenticator authenticator = OAuthAuthenticatorFactory.getOAuthAuthenticator(setting);

		logger.debug("Verifying with {}", authenticator);

		JsonNode conf = getConf();

		if (conf != null) {

		    authenticator.initialize(conf.get(oAuthProvider));

		    StringBuffer url = httpRequest.getRequestURL();

		    URL requestURL = new URL(url.toString());

		    String host = requestURL.getHost();

		    int port = requestURL.getPort();

		    logger.trace(DETECTED_REQ_PORT, port);

		    String path = requestURL.getPath();

		    String protocol = requestURL.getProtocol();

		    if (!host.contains("localhost") && !host.contains("127.0.0.1")) {

			protocol += "s";
		    }

		    String redirect = protocol + "://" + host + ":" + port + path;

		    logger.trace("Generated redirect url is {}", redirect);

		    if (port + 1 == 0) {

			logger.trace(REMOVING_PORT_80);

			redirect = protocol + "://" + host + path;

			logger.trace("New redirect url is {}", redirect);
		    }

		    authenticator.setRedirectURI(new URI(redirect));

		    Token token = authenticator.handleCallback(httpRequest);

		    return createSuccessRedirect(oAuthProvider, token.getClientURL(), httpRequest, httpResponse, token);
		}
	    }

	    throw GSException.createException(//
		    this.getClass(), //
		    OAUTH_NOT_CONFIGURED, //
		    null, //
		    "This functionality was not configured, please contact the administrator to fix this issue", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    USER_SERVICE_OAUTH_NOT_CONFIGURED_ERROR);

	} catch (GSException ex) {

	    return createErrorResponse(ex);

	} catch (Exception ex) {

	    return createErrorResponse(//
		    GSException.createException(//
			    getClass(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    USER_SERVICE_CALLBACK_ERROR, //
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

	logger.info("Logout requested with client url {}", clienturl);

	try {

	    httpResponse.setHeader("Set-Cookie", TokenProvider.USER_COOKIE_NAME + "=deleted;Path=/;Expires=Thu, 01 Jan 1970 00:00:00 GMT");

	    String redirect = clienturl;

	    if (redirect == null) {

		StringBuffer url = httpRequest.getRequestURL();

		URL requestURL = new URL(url.toString());

		String host = requestURL.getHost();

		int port = requestURL.getPort();

		String path = requestURL.getPath();

		String protocol = requestURL.getProtocol();

		if (!host.contains("localhost") && !host.contains("127.0.0.1")) {

		    protocol += "s";
		}

		redirect = protocol + "://" + host + ":" + port + path.replace("auth/user/logout", DEFAULT_LOGOUT_CLIENT_PATH);
	    }

	    httpResponse.sendRedirect(redirect);

	    return Response.ok().build();

	} catch (Exception ex) {

	    return createErrorResponse(//
		    GSException.createException(//
			    getClass(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    USER_SERVICE_LOGOUT_ERROR, //
			    ex));
	}
    }

    private static JsonNode getConf() {
	if (authConf == null) {
	    try {

		authConf = new ObjectMapper().readValue(
			Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/authentication.json"), JsonNode.class);

	    } catch (IOException e) {

		GSLoggerFactory.getLogger(UserService.class).error("Can't read configuration", e);

	    }
	}

	return authConf;
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
     */
    private Response createSuccessRedirect(String oAuthProvider, String redirect, HttpServletRequest httpRequest,
	    HttpServletResponse httpResponse, Token token) throws IOException, GSException {

	if (redirect == null) {

	    StringBuffer url = httpRequest.getRequestURL();

	    URL requestURL = new URL(url.toString());

	    String host = requestURL.getHost();

	    int port = requestURL.getPort();

	    String path = requestURL.getPath();

	    String protocol = requestURL.getProtocol();

	    redirect = protocol + "://" + host + ":" + port + path.replace("auth/user/callback/" + oAuthProvider, DEFAULT_CLIENT_PATH);
	}

	httpResponse.setHeader("Set-Cookie", TokenProvider.USER_COOKIE_NAME + "=" + tokenProvider.getToken(token) + ";Path=/");

	httpResponse.sendRedirect(redirect);

	return Response.ok().build();
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
