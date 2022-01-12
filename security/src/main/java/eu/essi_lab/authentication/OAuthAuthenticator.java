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

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;

import eu.essi_lab.authentication.model.Token;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public interface OAuthAuthenticator {

    String MISSING_OATH_CONF_PARAMAER_ERR_ID = "MISSING_OATH_CONF_PARAMAER_ERR_ID";

    public final String CLIENT_URL_JSON_KEY = "clienturl";

    /**
     * Implements service inizialization.
     *
     * @param conf json containing configuration the service needs.
     * @throws GSException
     */
    public void initialize(JsonNode conf) throws GSException;

    public void setRedirectURI(URI uri);

    /**
     * Implements the initial phase of oauth/oauth2 request flow: user wants to
     * login and the service (gs-service, is the client in oauth/oauth2 flow)
     * redirect him/her to facebook, google or twitter (the service provider).
     *
     * @param httpRequest servlet's request as by user login request.
     * @param httpResponse servlet's response as by user login request.
     * @throws GSException
     */
    public void handleLogin(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String clienturl) throws GSException;

    /**
     * Implements the final phase of oauth/oauth2 request flow: service provider
     * provides some code and tells the user-agent to redirect here. Service
     * will use the code to ask service provider for an access token. Obtained
     * token is returned with a user identifier (usually it is user's email)
     * wrapped as {@link Token} object.
     *
     * @param httpRequest servlet's request as by service provider request.
     * @return Token object who wrap access_token and user's email.
     * @throws GSException
     */
    public Token handleCallback(HttpServletRequest httpRequest) throws GSException;

    /**
     * Helper method to get a not null value from json configuration.
     *
     * @param propertyName name of wanted property.
     * @param conf json configuration.
     * @return property value if present.
     * @throws GSException when property not present.
     */
    public static String getConfigurationValue(String propertyName, JsonNode conf) throws GSException {
	if (conf.get(propertyName) != null && conf.get(propertyName).asText() != null && !conf.get(propertyName).asText().isEmpty()) {
	    return conf.get(propertyName).asText();
	} else {
	    throw GSException.createException(OAuthAuthenticator.class, "Missing configuraiton " + conf, null, null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, MISSING_OATH_CONF_PARAMAER_ERR_ID);

	}
    }

    public String getClientId();

    public void setClientId(String clientId);

    public String getClientSecret();

    public void setClientSecret(String clientSecret);
}
