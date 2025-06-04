package eu.essi_lab.authentication;

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

import java.net.URI;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.JsonNode;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public abstract class OAuth2Authenticator extends OAuthAuthenticator {

    public static final String NULL_HTTP_RESPONSE_PROVIDED_ERR_ID = "NULL_HTTP_RESPONSE_PROVIDED_ERR_ID";
    public static final String REDIRECT_IOEXCEPTIO_ERR_ID = "REDIRECT_IOEXCEPTIO_ERR_ID";
    public static final String NULL_HTTP_REQUEST_PROVIDED_ERR_ID = "NULL_HTTP_REQUEST_PROVIDED_ERR_ID";
    public static final String NULL_OR_EMPTY_CODE_ERR_ID = "NULL_OR_EMPTY_CODE_ERR_ID";
    public static final String ERR_WITH_FACEBOOK_MSG = "An error occourred communicating with Facebook, please try to login with a differrent account or system.";
    public static final String ERR_WITH_GOOGLE_MSG = "An error occourred communicating with Google, please try to login with a differrent account or system.";
    public static final String ERR_WITH_TWITTER_MSG = "An error occourred communicating with Twitter, please try to login with a differrent account or system.";
    public static final String IOEXCEPTION_TOKEN_ERR_ID = "IOEXCEPTION_TOKEN_ERR_ID";
    public static final String IOEXCEPTION_EMAIL_ERR_ID = "IOEXCEPTION_EMAIL_ERR_ID";
    public static final String MISSING_OATH_CONF_FILE_ERR_ID = "MISSING_OATH_CONF_FILE_ERR_ID";
    public static final String INVALID_OAUTH_PARAM_VALUE_ERR_ID = "INVALID_OAUTH_PARAM_VALUE_ERR_ID";

    protected String loginUrl;
    protected String tokenUrl;
    protected String userInfoUrl;

    @Override
    public void initialize(JsonNode conf) throws GSException {

	if (conf == null)
	    throw GSException.createException(//
		    this.getClass(), //
		    "Missing configuraiton file", //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MISSING_OATH_CONF_FILE_ERR_ID);

	try {

	    redirectUri = new URI(OAuthAuthenticator.getConfigurationValue("redirect-uri", conf));

	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(OAuth2Authenticator.class).error("Invalid redirect URI found {}",
		    OAuthAuthenticator.getConfigurationValue("redirect-uri", conf));

	    throw GSException.createException(//
		    this.getClass(), //
		    "Invalid redirect URI found", //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INVALID_OAUTH_PARAM_VALUE_ERR_ID, //
		    e);

	}

	loginUrl = OAuthAuthenticator.getConfigurationValue("login-url", conf);
	tokenUrl = OAuthAuthenticator.getConfigurationValue("token-url", conf);
	userInfoUrl = OAuthAuthenticator.getConfigurationValue("userinfo-url", conf);
    }
}
