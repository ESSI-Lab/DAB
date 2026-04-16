/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.oauth;

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

import org.json.JSONObject;

/**
 * @author Fabrizio
 */
public class KeycloakProviderSetting extends OAuthProviderSetting {

    static final String IDENTIFIER = "keycloakProviderSetting";

    /**
     * 
     */
    public KeycloakProviderSetting() {

	setIdentifier(IDENTIFIER);
	setName("Keycloak provider");
    }

    /**
     * @param object
     */
    public KeycloakProviderSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public KeycloakProviderSetting(String object) {

	super(object);
    }

    @Override
    protected String getDefaultLoginURL() {

	return "http://localhost:8080/realms/myrealm/protocol/openid-connect/auth?";
    }

    @Override
    protected String getDefaultTokenURL() {

	return "http://localhost:8080/realms/myrealm/protocol/openid-connect/token?";
    }

    @Override
    protected String getDefaultUserInfoURL() {

	return "http://localhost:8080/realms/myrealm/protocol/openid-connect/userinfo?";
    }
}
