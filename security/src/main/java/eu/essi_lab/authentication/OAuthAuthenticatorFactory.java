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

import eu.essi_lab.authentication.configuration.GSOAuthAuthenticator;
import eu.essi_lab.authentication.configuration.IOAuthAuthenticatorConfigurable;
import eu.essi_lab.model.GSOAuthProvider;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class OAuthAuthenticatorFactory {

    private static final String BAD_PROVIDER_OBJECT_TYPE_ERR_ID = "BAD_PROVIDER_OBJECT_TYPE_ERR_ID";

    public OAuthAuthenticator getOAuthAuthenticator(GSOAuthProvider provider) throws GSException {

	if (!GSOAuthAuthenticator.class.isAssignableFrom(provider.getClass()))
	    throw GSException.createException(this.getClass(), "The provided object is not a GSOAuthAuthenticator", null, null,
		    ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_ERROR, BAD_PROVIDER_OBJECT_TYPE_ERR_ID);

	IOAuthAuthenticatorConfigurable configurable = (IOAuthAuthenticatorConfigurable) ((GSOAuthAuthenticator) provider)
		.getOauthConfigurable();

	return configurable.getAuthenticator();

    }
}
