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

import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting.OAuthProvider;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class OAuthAuthenticatorFactory {

    /**
     * 
     */
    private static final String AUTHENTICATOR_FACTORY_ERROR = "AUTHENTICATOR_FACTORY_ERROR";

    /**
     * @param setting
     * @return
     * @throws GSException
     */
    public static OAuthAuthenticator getOAuthAuthenticator(OAuthSetting setting) throws GSException {

	try {

	    OAuthAuthenticator authenticator = null;

	    OAuthProvider provider = setting.getSelectedProvider();
	    switch (provider) {
	    case FACEBOOK:

		authenticator = new FacebookOAuth2Authenticator();

		break;
	    case GOOGLE:

		authenticator = new GoogleOAuth2Authenticator();

		break;
	    case TWITTER:

		authenticator = new TwitterOAuthAuthenticator();

		break;
	    }

	    authenticator.configure(setting);

	    return authenticator;

	} catch (Exception e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(OAuthAuthenticatorFactory.class).error(e.getMessage(), e);

	    throw GSException.createException(//
		    OAuthAuthenticatorFactory.class, //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    AUTHENTICATOR_FACTORY_ERROR);
	}
    }
}
