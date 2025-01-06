package eu.essi_lab.authentication;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import eu.essi_lab.authentication.model.Token;
import eu.essi_lab.authentication.util.RFC3986Encoder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class GoogleOAuth2Authenticator extends OAuth2Authenticator {

    public static final String GOOGLE = "google";
    private Logger logger = GSLoggerFactory.getLogger(GoogleOAuth2Authenticator.class);
    private static final String NULL_TOKEN_ERR_ID = "NULL_TOKEN_ERR_ID";

    @Override
    public void handleLogin(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String clienturl) throws GSException {

	logger.info("Handling login request.");

	if (httpResponse == null) {

	    logger.error("Found null HttpServletResponse");

	    throw GSException.createException(this.getClass(), "Found null HttpServletResponse", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NULL_HTTP_RESPONSE_PROVIDED_ERR_ID);
	}

	try {

	    String stateJson = "{\"" + OAuthAuthenticator.CLIENT_URL_JSON_KEY + "\":\"" + clienturl + "\"}";

	    String state = RFC3986Encoder.encode(stateJson, "UTF-8");
	    httpResponse.sendRedirect(String.format(loginUrl, getClientId(), redirectUri.toString(), state));

	} catch (IOException e) {

	    logger.error("IOException sending redirect during login handling");

	    throw GSException.createException(this.getClass(), "IOException sending redirect during login handling", null, null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, REDIRECT_IOEXCEPTIO_ERR_ID, e);
	}
    }

    @Override
    public Token handleCallback(HttpServletRequest httpRequest) throws GSException {
	if (httpRequest == null) {

	    logger.error("Found null HttpServletRequest");

	    throw GSException.createException(this.getClass(), "Found null HttpServletRequest", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NULL_HTTP_REQUEST_PROVIDED_ERR_ID);
	}
	String code = httpRequest.getParameter("code");
	if (code == null || code.isEmpty()) {
	    logger.error("Found null or empty code in callback");

	    throw GSException.createException(this.getClass(), "Found null or empty code in callback", null, ERR_WITH_GOOGLE_MSG,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, NULL_OR_EMPTY_CODE_ERR_ID);
	}

	String stateJson = httpRequest.getParameter("state");

	String clienturl = null;
	if (stateJson != null) {

	    try {
		JsonNode jsonNode = new ObjectMapper().readValue(stateJson, JsonNode.class);

		clienturl = jsonNode.get(OAuthAuthenticator.CLIENT_URL_JSON_KEY).asText();

		logger.debug("Found client url {}", clienturl);

	    } catch (IOException e) {
		logger.warn("Exception reading state, can't set client redirect", e);
	    }

	} else {
	    logger.warn("No state parameter found, can't set client redirect");
	}

	logger.info("Handling callback request.");

	String getTokenUrl = String.format(tokenUrl, getClientId(), getClientSecret(), redirectUri.toString(), code);

	logger.trace("GetToken url {}", getTokenUrl);

	ObjectMapper objM = new ObjectMapper();
	JsonNode jsonTokenEntity = null;
	try {

	    jsonTokenEntity = getToken(getTokenUrl, httpClient, objM);

	    if (jsonTokenEntity == null) {

		logger.trace("json token null");
		throw GSException.createException(this.getClass(), "Null token from Google", null, ERR_WITH_GOOGLE_MSG,
			ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, NULL_TOKEN_ERR_ID);
	    } else
		logger.trace("json token not null");

	} catch (IOException e) {

	    logger.error("Can't get token from Google");

	    throw GSException.createException(this.getClass(), "Can't get token from Google", null, ERR_WITH_GOOGLE_MSG,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, IOEXCEPTION_TOKEN_ERR_ID, e);

	}
	
	GSLoggerFactory.getLogger(getClass()).debug("Token:\n {}", jsonTokenEntity);

	String accessToken = jsonTokenEntity.get("access_token").asText();
	String tokenType = jsonTokenEntity.get("token_type").asText();

	JsonNode jsonUserEmailEntity = null;

	try {

	    userInfoUrl += "personFields=emailAddresses";

	    jsonUserEmailEntity = getUserEmail(userInfoUrl, httpClient, objM, accessToken);

	} catch (IOException e) {

	    logger.error("Can't get user email from Google");

	    throw GSException.createException(//
		    this.getClass(), //
		    e.getMessage(), //
		    null, //
		    ERR_WITH_GOOGLE_MSG, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    IOEXCEPTION_EMAIL_ERR_ID, //
		    e);

	}

	if (!jsonUserEmailEntity.has("emailAddresses")) {

	    logger.error(new JSONObject(jsonUserEmailEntity.toString()).toString(3));

	    throw GSException.createException(//
		    this.getClass(), //
		    "Can't get user email from Google", //
		    null, //
		    ERR_WITH_GOOGLE_MSG, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, IOEXCEPTION_EMAIL_ERR_ID);

	}

	ArrayNode arrayNode = (ArrayNode) jsonUserEmailEntity.get("emailAddresses");
	String email = arrayNode.get(0).get("value").asText();

	logger.info("Succesfull login about user {}. Token is in session.", email);

	Token token = new Token();
	token.setEmail(email);
	token.setToken(accessToken);
	token.setType(tokenType);
	token.setServiceProvider(GOOGLE);
	token.setClientURL(clienturl);
	return token;
    }

    private JsonNode getToken(String postTokenUrl, CloseableHttpClient httpClient, ObjectMapper objM) throws IOException {
	HttpPost postTokenReq = new HttpPost(postTokenUrl);

	CloseableHttpResponse postTokenResp = httpClient.execute(postTokenReq);
	return objM.readValue(postTokenResp.getEntity().getContent(), JsonNode.class);

    }

    private JsonNode getUserEmail(String getUserEmailUrl, CloseableHttpClient httpClient, ObjectMapper objM, String accessToken)
	    throws IOException {
	HttpGet getUserEmailReq = new HttpGet(getUserEmailUrl);

	getUserEmailReq.setHeader("Authorization", String.format("Bearer %s", accessToken));
	CloseableHttpResponse getUserEmailResp = httpClient.execute(getUserEmailReq);
	return objM.readValue(getUserEmailResp.getEntity().getContent(), JsonNode.class);

    }
}
