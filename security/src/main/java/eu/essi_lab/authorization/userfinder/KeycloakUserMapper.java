/**
 * 
 */
package eu.essi_lab.authorization.userfinder;

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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import eu.essi_lab.lib.net.keycloak.KeycloakUser;
import eu.essi_lab.lib.net.keycloak.KeycloakUser.UserProfileAttribute;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.auth.GSUser;

/**
 * @author Fabrizio
 */
public class KeycloakUserMapper {

    /**
     * @param user
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSUser toGSUser(KeycloakUser user) {

	GSUser gsUser = new GSUser();
	gsUser.setEnabled(user.isEnabled());
	gsUser.setIdentifier(user.getUserProfileAttribute(UserProfileAttribute.USERNAME).get());

	List<GSProperty> properties = user.getAttributes().//
		stream().//
		filter(attr -> !attr.getKey().equals("role") && !attr.getKey().equals("authProvider")).//
		map(attr -> GSProperty.of(attr.getKey(), attr.getValue().get(0))).//
		collect(Collectors.toList());

	user.getUserProfileAttribute(UserProfileAttribute.FIRST_NAME).ifPresent(v -> properties.add(GSProperty.of("firstName", v)));
	user.getUserProfileAttribute(UserProfileAttribute.LAST_NAME).ifPresent(v -> properties.add(GSProperty.of("lastName", v)));
	user.getUserProfileAttribute(UserProfileAttribute.EMAIL).ifPresent(v -> properties.add(GSProperty.of("email", v)));

	gsUser.setAttributes(properties);

	user.getAttributeValue("role").ifPresent(v -> gsUser.setRole(v));

	user.getAttributeValue("authProvider").ifPresent(v -> gsUser.setAuthProvider(v));

	return gsUser;
    }

    /**
     * @param user
     * @return
     */
    public static KeycloakUser toKeycloakUser(GSUser user) {

	List<Entry<String, String>> attributes = user.getProperties().//
		stream().//
		filter(prop -> !prop.getName().equals("email") && !prop.getName().equals("firstName") && !prop.getName().equals("lastName"))
		.//
		map(prop -> Map.entry(prop.getName(), prop.getValue().toString())).//
		collect(Collectors.toList());

	if (user.getAuthProvider() != null) {

	    attributes.add(Map.entry("authProvider", user.getAuthProvider()));
	}

	if (user.getRole() != null) {

	    attributes.add(Map.entry("role", user.getRole()));
	}

	return new KeycloakUser.KeycloakUserBuilder().enabled(user.isEnabled()).//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, user.getIdentifier()).//

		withOptionalUserProfileAttribute(UserProfileAttribute.EMAIL, user.getStringPropertyValue("email")).//
		withOptionalUserProfileAttribute(UserProfileAttribute.FIRST_NAME, user.getStringPropertyValue("firstName")).//
		withOptionalUserProfileAttribute(UserProfileAttribute.LAST_NAME, user.getStringPropertyValue("lastName")).//

		withAttributes(attributes).//
		build();
    }
}
