package eu.essi_lab.authentication.configuration;

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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.authentication.GoogleOAuth2Authenticator;
import eu.essi_lab.authentication.OAuthAuthenticator;
import eu.essi_lab.model.configuration.AbstractGSconfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.GSException;
public class GoogleOAuthAuthenticatorConfigurable extends AbstractGSconfigurable implements IOAuthAuthenticatorConfigurable {

    private static final long serialVersionUID = -4228692430555830670L;
    public static final String GOOGLE_CLIENT_ID_KEY = "GOOGLE_CLIENT_ID_KEY";
    public static final String GOOGLE_CLIENT_SECRET_KEY = "GOOGLE_CLIENT_SECRET_KEY";
    private final String AUTH_GOOGLE_COMPONENT_KEY = "AUTH_GOOGLE_COMPONENT_KEY";
    private Map<String, GSConfOption<?>> supported = new HashMap<>();

    public GoogleOAuthAuthenticatorConfigurable() {
	setKey(AUTH_GOOGLE_COMPONENT_KEY);

	setLabel("Google OAuth");

	GSConfOptionString googleClientid = new GSConfOptionString();
	googleClientid.setKey(GOOGLE_CLIENT_ID_KEY);
	googleClientid.setLabel("Google Client Id");
	getSupportedOptions().put(GOOGLE_CLIENT_ID_KEY, googleClientid);

	GSConfOptionString googleClientSecret = new GSConfOptionString();
	googleClientSecret.setKey(GOOGLE_CLIENT_SECRET_KEY);
	googleClientSecret.setLabel("Google Client Secret");
	getSupportedOptions().put(GOOGLE_CLIENT_SECRET_KEY, googleClientSecret);

	GSOAuthAuthenticator instantiable = new GSOAuthAuthenticator();
	instantiable.setProviderName("google");

	instantiable.setComponentId(getKey());

	instantiable.setComponent(this);

	setInstantiableType(instantiable);
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return supported;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

    }

    @Override
    public void onFlush() throws GSException {

    }

    @Override
    @JsonIgnore
    @XmlTransient
    public OAuthAuthenticator getAuthenticator() {

	String clientid = (String) getSupportedOptions().get(GOOGLE_CLIENT_ID_KEY).getValue();

	String clientsecret = (String) getSupportedOptions().get(GOOGLE_CLIENT_SECRET_KEY).getValue();

	GoogleOAuth2Authenticator authenticator = new GoogleOAuth2Authenticator();

	authenticator.setClientId(clientid);
	authenticator.setClientSecret(clientsecret);
	authenticator.setHttpClient(HttpClients.createDefault());

	return authenticator;

    }
}
