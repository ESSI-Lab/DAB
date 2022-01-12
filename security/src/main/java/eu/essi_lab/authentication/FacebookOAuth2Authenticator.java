package eu.essi_lab.authentication;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.authentication.model.Token;
import eu.essi_lab.authentication.util.RFC3986Encoder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class FacebookOAuth2Authenticator extends OAuth2Authenticator {

    public static final String FACEBOOK = "facebook";
    private Logger log = GSLoggerFactory.getLogger(FacebookOAuth2Authenticator.class);

    @Override
    public void handleLogin(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String clienturl) throws GSException {
	if (httpResponse == null) {

	    log.error("Found null HttpServletResponse");

	    throw GSException.createException(this.getClass(), "Found null HttpServletResponse", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NULL_HTTP_RESPONSE_PROVIDED_ERR_ID);
	}

	try {

	    String stateJson = "{\"" + OAuthAuthenticator.CLIENT_URL_JSON_KEY + "\":\"" + clienturl + "\"}";

	    String state = RFC3986Encoder.encode(stateJson, "UTF-8");

	    httpResponse.sendRedirect(String.format(loginUrl, getClientId(), redirectUri.toString(), state));

	} catch (IOException e) {

	    log.error("IOException sending redirect during login handling");

	    throw GSException.createException(this.getClass(), "IOException sending redirect during login handling", null, null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, REDIRECT_IOEXCEPTIO_ERR_ID, e);
	}
    }

    @Override
    public Token handleCallback(HttpServletRequest httpRequest) throws GSException {
	if (httpRequest == null) {

	    log.error("Found null HttpServletRequest");

	    throw GSException.createException(this.getClass(), "Found null HttpServletRequest", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NULL_HTTP_REQUEST_PROVIDED_ERR_ID);
	}

	String code = httpRequest.getParameter("code");

	if (code == null || code.isEmpty()) {

	    log.error("Found null or empty code in callback");

	    throw GSException.createException(this.getClass(), "Found null or empty code in callback", null, ERR_WITH_FACEBOOK_MSG,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, NULL_OR_EMPTY_CODE_ERR_ID);
	}

	String stateJson = httpRequest.getParameter("state");

	String clienturl = null;
	if (stateJson != null) {

	    try {
		JsonNode jsonNode = new ObjectMapper().readValue(stateJson, JsonNode.class);

		clienturl = jsonNode.get(OAuthAuthenticator.CLIENT_URL_JSON_KEY).asText();

		log.debug("Found client url {}", clienturl);

	    } catch (IOException e) {
		log.warn("Exception reading state, can't set client redirect", e);
	    }

	} else {
	    log.warn("No state parameter found, can't set client redirect");
	}

	log.info("Handling callback request.");
	String getTokenUrl = String.format(tokenUrl, getClientId(), redirectUri.toString(), getClientSecret(), code);
	ObjectMapper objM = new ObjectMapper();
	JsonNode jsonTokenEntity = null;
	try {
	    jsonTokenEntity = getToken(getTokenUrl, httpClient, objM);
	} catch (IOException e) {
	    log.error("Can't get token from Facebook");

	    throw GSException.createException(this.getClass(), "Can't get token from Facebook", null, ERR_WITH_FACEBOOK_MSG,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, IOEXCEPTION_TOKEN_ERR_ID, e);
	}
	String accessToken = jsonTokenEntity.get("access_token").asText();
	String tokenType = jsonTokenEntity.get("token_type").asText();

	String getUserEmailUrl = String.format(userInfoUrl, accessToken);
	JsonNode jsonUserEmailEntity = null;
	try {

	    jsonUserEmailEntity = getUserEmail(getUserEmailUrl, httpClient, objM);

	} catch (IOException e) {
	    log.error("Can't get user email from Facebook");

	    throw GSException.createException(this.getClass(), "Can't get user email from Facebook", null, ERR_WITH_FACEBOOK_MSG,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, IOEXCEPTION_EMAIL_ERR_ID, e);
	}

	String email = jsonUserEmailEntity.get("email").asText();
	log.info(String.format("Succesfull login about user %s. Token is in session.", email));
	Token token = new Token();
	token.setEmail(email);
	token.setToken(accessToken);
	token.setType(tokenType);
	token.setServiceProvider(FACEBOOK);
	token.setClientURL(clienturl);
	return token;
    }

    private JsonNode getToken(String getTokenUrl, CloseableHttpClient httpClient, ObjectMapper objM) throws IOException {
	HttpGet getTokenReq = new HttpGet(getTokenUrl);

	CloseableHttpResponse getTokenResp = httpClient.execute(getTokenReq);
	return objM.readValue(getTokenResp.getEntity().getContent(), JsonNode.class);

    }

    private JsonNode getUserEmail(String getUserEmailUrl, CloseableHttpClient httpClient, ObjectMapper objM) throws IOException {
	HttpGet getUserEmailReq = new HttpGet(getUserEmailUrl);
	CloseableHttpResponse getUserEmailResp = httpClient.execute(getUserEmailReq);
	return objM.readValue(getUserEmailResp.getEntity().getContent(), JsonNode.class);

    }
}
