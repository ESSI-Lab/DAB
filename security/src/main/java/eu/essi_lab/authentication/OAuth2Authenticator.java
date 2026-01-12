package eu.essi_lab.authentication;

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

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import eu.essi_lab.authentication.token.*;
import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.gs.setting.oauth.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.exceptions.*;
import org.apache.http.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.json.*;

import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

/**
 * OAuthAuthenticator2 types implements oauth2 requests flow services. This flow is made up by two/three steps:<br> -
 * {@link OAuthAuthenticator#handleLogin(HttpServletRequest, HttpServletResponse) handleLogin} method covers initial step/steps,<br> -
 * {@link OAuthAuthenticator#handleCallback(HttpServletRequest) handleCallback} method covers later steps.<br> Once we have an access_token
 * we use it to obtain a user identifier, usually his/her email. This is also covered in later step.
 *
 * @author pezzati/Fabrizio
 */
public abstract class OAuth2Authenticator implements Configurable<OAuthSetting> {

    public static final String NULL_HTTP_RESPONSE_PROVIDED_ERR_ID = "NULL_HTTP_RESPONSE_PROVIDED_ERR_ID";
    public static final String REDIRECT_IOEXCEPTIO_ERR_ID = "REDIRECT_IOEXCEPTIO_ERR_ID";
    public static final String NULL_HTTP_REQUEST_PROVIDED_ERR_ID = "NULL_HTTP_REQUEST_PROVIDED_ERR_ID";
    public static final String NULL_OR_EMPTY_CODE_ERR_ID = "NULL_OR_EMPTY_CODE_ERR_ID";
    public static final String ERR_WITH_FACEBOOK_MSG = "An error occourred communicating with Facebook, please try to login with a differrent account or system.";
    public static final String ERR_WITH_TWITTER_MSG = "An error occourred communicating with Twitter, please try to login with a differrent account or system.";
    public static final String IOEXCEPTION_TOKEN_ERR_ID = "IOEXCEPTION_TOKEN_ERR_ID";
    public static final String IOEXCEPTION_EMAIL_ERR_ID = "IOEXCEPTION_EMAIL_ERR_ID";

    public final static String CLIENT_URL_JSON_KEY = "clienturl";

    private String loginUrl;
    private String tokenUrl;
    private String tokeUrlHost;
    private String userInfoUrl;
    private URI redirectUri;
    private CloseableHttpClient httpClient;
    private OAuthSetting setting;

    /**
     *
     */
    private final String LOGIN_QUERY = "client_id=%s&redirect_uri=%s&response_type=code&scope=openid profile email&state=%s";
    /**
     *
     */
    private final String TOKEN_QUERY = "client_id=%s&client_secret=%s&redirect_uri=%s&code=%s&grant_type=authorization_code";

    /**
     *
     */
    public OAuth2Authenticator() {

	this.httpClient = HttpClients.createDefault();
    }

    @Override
    public void configure(OAuthSetting setting) {

	this.setting = setting;

	this.loginUrl = setting.getSelectedProviderSetting().getLoginURL().get();
	this.loginUrl = loginUrl.endsWith("?") ? loginUrl + LOGIN_QUERY : loginUrl + "?" + LOGIN_QUERY;

	this.tokenUrl = setting.getSelectedProviderSetting().getTokenURL().get();
	this.tokenUrl = tokenUrl.endsWith("?") ? tokenUrl + TOKEN_QUERY : tokenUrl + "?" + TOKEN_QUERY;

	this.tokeUrlHost = URI.create(tokenUrl.substring(0,tokenUrl.indexOf("?") )).getHost();

	this.userInfoUrl = setting.getSelectedProviderSetting().getUserInfoURL().get();
    }

    /**
     * @param httpRequest
     * @param httpResponse
     * @param clienturl
     * @throws GSException
     */
    public void handleLogin(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String clienturl) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Handling login request STARTED");

	try {

	    String stateJson = "{\"" + CLIENT_URL_JSON_KEY + "\":\"" + clienturl + "\"}";
	    String state = URLEncoder.encode(stateJson, "UTF-8");

	    String loginRedirectURL = buildLoginRedirectURL(state);

	    GSLoggerFactory.getLogger(getClass()).debug("Login redirect URL: {}", loginRedirectURL);

	    httpResponse.sendRedirect(loginRedirectURL);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    this.getClass(), //
		    getClass().getSimpleName() + "LoginError", //
		    e);
	}

	GSLoggerFactory.getLogger(getClass()).info("Handling login request ENDED");
    }

    /**
     * @param httpRequest
     * @return
     * @throws GSException
     */
    public Token handleCallback(HttpServletRequest httpRequest) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Handling callback STARTED");

	String authCode = httpRequest.getParameter("code");

	if (authCode == null || authCode.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).error("Authentication token missing in callback");

	    throw GSException.createException(//
		    this.getClass(), //
		    "Authentication token missing in callback", //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    getClass().getSimpleName() + "NullOrEmptyAuthCodeInCallbackError");
	}

	String stateJson = httpRequest.getParameter("state");

	String clienturl = null;

	if (stateJson != null) {

	    try {
		JsonNode jsonNode = new ObjectMapper().readValue(stateJson, JsonNode.class);

		clienturl = jsonNode.get(CLIENT_URL_JSON_KEY).asText();

		GSLoggerFactory.getLogger(getClass()).debug("Found client url: {}", clienturl);

	    } catch (IOException e) {

		GSLoggerFactory.getLogger(getClass()).warn("Exception reading state, can't set client redirect", e);
	    }

	} else {

	    GSLoggerFactory.getLogger(getClass()).warn("No state parameter found, can't set client redirect");
	}

	ObjectMapper objM = new ObjectMapper();
	JsonNode jsonTokenEntity = null;

	try {

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving token STARTED");

	    String getTokenUrl = buildGetTokenURL(authCode);

	    GSLoggerFactory.getLogger(getClass()).trace("Get token url: {}", getTokenUrl);

	    jsonTokenEntity = getToken(getTokenUrl, getHttpClient(), objM);

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving token ENDED");

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    this.getClass(), //
		    getClass().getSimpleName() + "TokenRetrievalError", //
		    e);
	}

	String accessToken = jsonTokenEntity.get("access_token").asText();
	String tokenType = jsonTokenEntity.get("token_type").asText();

	String email = null;

	try {

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving user e-mail STARTED");

	    GSLoggerFactory.getLogger(getClass()).trace("Get user info url: {}", getUserInfoUrl());

	    HttpGet getUserEmailReq = new HttpGet(getUserInfoUrl());

	    getUserEmailReq.setHeader("Authorization", String.format("Bearer %s", accessToken));

	    CloseableHttpResponse getUserEmailResp = getHttpClient().execute(getUserEmailReq);
	    JsonNode node = objM.readValue(getUserEmailResp.getEntity().getContent(), JsonNode.class);

	    email = readUserEmail(node);

	    GSLoggerFactory.getLogger(getClass()).debug("User e-mail response: {}", toString(node));

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving user e-mail ENDED");

	} catch (GSException ex) {

	    throw ex;

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    this.getClass(), //
		    getClass().getSimpleName() + "UserEmailRetrievalError", //
		    e);
	}

	GSLoggerFactory.getLogger(getClass()).info("Succesfull login user {}. Token is in session", email);

	Token token = new Token();
	token.setEmail(email);
	token.setToken(accessToken);
	token.setType(tokenType);
	token.setServiceProvider(getProvider());
	token.setClientURL(clienturl);

	GSLoggerFactory.getLogger(getClass()).info("Handling callback ENDED");

	return token;
    }

    @Override
    public OAuthSetting getSetting() {

	return this.setting;
    }

    /**
     * @return
     */
    public String getLoginUrl() {

	return loginUrl;
    }

    /**
     * @return
     */
    public String getTokenUrl() {

	return tokenUrl;
    }

    /**
     * @return
     */
    public String getUserInfoUrl() {

	return userInfoUrl;
    }

    /**
     * @return
     */
    public URI getRedirectUri() {

	return redirectUri;
    }

    /**
     * @return
     */
    public CloseableHttpClient getHttpClient() {

	return httpClient;
    }

    /**
     * @param uri
     */
    public void setRedirectURI(URI uri) {

	redirectUri = uri;
    }

    /**
     * @return
     */
    public String getClientId() {

	return getSetting().getClientId().get();
    }

    /**
     * @param clientId
     */
    public void setClientId(String clientId) {

	getSetting().setClientId(clientId);
    }

    /**
     * @return
     */
    public String getClientSecret() {

	return getSetting().getClientSecret().get();
    }

    /**
     * @param clientSecret
     */
    public void setClientSecret(String clientSecret) {

	getSetting().setClientSecret(clientSecret);
    }

    @Override
    public String getType() {

	return getClass().getSimpleName();
    }

    /**
     * @return
     */
    protected abstract String getProvider();

    /**
     * @param node
     * @return
     * @throws GSException
     */
    protected String readUserEmail(JsonNode node) throws GSException {

	if (!node.has("email")) {

	    GSLoggerFactory.getLogger(getClass()).error(new JSONObject(node.toString()).toString(3));

	    throw GSException.createException(//
		    this.getClass(), //
		    "Missing email address in reponse token: " + new JSONObject(node.toString()).toString(3), //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "MissingEmailAddressInResponseTokenError");
	}

	TextNode arrayNode = (TextNode) node.get("email");
	String email = arrayNode.asText();

	return email;
    }

    /**
     * @param node
     * @return
     */
    protected String toString(JsonNode node) {

	return new JSONObject(node.toString()).toString(3);
    }

    /**
     * @param postTokenUrl
     * @param httpClient
     * @param objM
     * @return
     * @throws IOException
     */
    private JsonNode getToken(String postTokenUrl, CloseableHttpClient httpClient, ObjectMapper objM) throws Exception {

	URI uri = null;
	try {
	    uri = URI.create(postTokenUrl);

	} catch (Exception e) {

	    throw new Exception("Invalid token URL", e);
	}

	if (!uri.getScheme().equalsIgnoreCase("https")) {
	    throw new Exception("Invalid URL scheme");
	}

	if (uri.getHost() == null || !uri.getHost().equals(tokeUrlHost)) {
	    throw new Exception("Untrusted token host");
	}

	URI baseUri = new URI(
		uri.getScheme(),
		uri.getAuthority(),
		uri.getPath(),
		null,
		null
	);

	HttpPost httpPost = new HttpPost(baseUri);

	List<NameValuePair> params = extractParamsFromUrl(postTokenUrl);

	httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

	CloseableHttpResponse resp = httpClient.execute(httpPost);

	JsonNode value = objM.readValue(resp.getEntity().getContent(), JsonNode.class);

	GSLoggerFactory.getLogger(getClass()).debug("Token retrieved: {} ", toString(value));

	return value;
    }

    /**
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     */
    private List<NameValuePair> extractParamsFromUrl(String url) throws UnsupportedEncodingException {

	List<NameValuePair> params = new ArrayList<>();

	String query = URI.create(url).getQuery();
	if (query == null || query.isEmpty()) {
	    return params;
	}

	String[] pairs = query.split("&");

	for (String pair : pairs) {

	    String[] keyValue = pair.split("=", 2);
	    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
	    String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "";

	    params.add(new BasicNameValuePair(key, value));
	}

	return params;
    }

    /**
     * @param state
     * @return
     */
    private String buildLoginRedirectURL(String state) {

	return String.format(getLoginUrl(), getClientId(), getRedirectUri().toString(), state);
    }

    /**
     * @param code
     * @return
     */
    private String buildGetTokenURL(String code) {

	return String.format(getTokenUrl(), getClientId(), getClientSecret(), getRedirectUri().toString(), code);
    }

}
