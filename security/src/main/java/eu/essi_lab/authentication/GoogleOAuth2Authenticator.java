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

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class GoogleOAuth2Authenticator extends OAuth2Authenticator {

    /**
     * 
     */
    private final String USER_INFO_QUERY = "personFields=emailAddresses";

    /**
     * @param getUserEmailUrl
     * @param httpClient
     * @param objM
     * @param accessToken
     * @return
     * @throws IOException
     * @throws GSException
     */
    @Override
    protected String readUserEmail(JsonNode node) throws GSException {

	if (!node.has("emailAddresses")) {

	    throw GSException.createException(//
		    this.getClass(), //
		    "Missing email address in Google token: " + toString(node), //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "MissingEmailAddressInResponseTokenError");
	}

	ArrayNode arrayNode = (ArrayNode) node.get("emailAddresses");
	String email = arrayNode.get(0).get("value").asText();

	return email;
    }

    /**
     * Google People API is not a standard OAuth 2.0 endpoint, it uses a proprietary API which needs to specify the
     * required claims (in OAuth 2.0 they are previously set in the login "scope" parameter)
     * 
     * @return
     */
    @Override
    public String getUserInfoUrl() {

	return super.getUserInfoUrl().endsWith("?") ? //
		super.getUserInfoUrl() + USER_INFO_QUERY : //
		super.getUserInfoUrl() + "?" + USER_INFO_QUERY;
    }

    @Override
    protected String getProvider() {

	return "Google";
    }
}
