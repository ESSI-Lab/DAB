/**
 * 
 */
package eu.essi_lab.authorization.userfinder;

import java.util.ArrayList;

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
     * @param keycloakUser
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static GSUser toGSUser(KeycloakUser keycloakUser) {

	GSUser gsUser = new GSUser();

	gsUser.setEnabled(keycloakUser.isEnabled());

	//
	// keycloak username set as gsuser identifier
	//
	gsUser.setIdentifier(keycloakUser.getUserProfileAttribute(UserProfileAttribute.USERNAME).get());

	//
	// all the keycloak user attributes, except role and authProvider (they have a
	// specific field in the GSUser so they are set separately), are mapped to the GSUser properties
	//
	List<GSProperty> properties = keycloakUser.getAttributes().//
		stream().filter(attr -> !attr.getKey().equals(GSUser.ROLE) && !attr.getKey().equals(GSUser.AUTH_PROVIDER)).//
		// keycloak attributes can have multiple values, here only the first is taken!
		map(attr -> GSProperty.of(attr.getKey(), attr.getValue().getFirst())).//
		collect(Collectors.toList());


	//
	// all the keycloak user profile attributes, except username, are mapped to the GSUser properties
	//
	UserProfileAttribute.getAttributes().//
		stream().//
		filter(attr -> !attr.equals(UserProfileAttribute.USERNAME.getAttribute())).//
		forEach(attr -> keycloakUser.getUserProfileAttribute(UserProfileAttribute.of(attr).get())
			.ifPresent(v -> properties.add(GSProperty.of(attr, v))));

	gsUser.setAttributes(properties);

	//
	// role and authProvider have a specific field in the GSUser
	//
	keycloakUser.getAttributeValue(GSUser.ROLE).ifPresent(gsUser::setRole);
	keycloakUser.getAttributeValue(GSUser.AUTH_PROVIDER).ifPresent(gsUser::setAuthProvider);

	return gsUser;
    }

    /**
     * @param gsUser
     * @return
     */
    public static KeycloakUser toKeycloakUser(GSUser gsUser) {

	//
	// all the GSUser properties, except the keycloak user profile attributes, are mapped
	// to the keycloak user attributes
	//
	List<Entry<String, String>> attributes = gsUser.getProperties().//
		stream().//
		filter(prop -> !UserProfileAttribute.getAttributes().contains(prop.getName())).//
		map(prop -> Map.entry(prop.getName(), prop.getValue().toString())).//
		collect(Collectors.toList());

	//
	// GSUser authProvider and role are mapped to the keycloak user attributes
	//
	if (gsUser.getAuthProvider() != null) {

	    attributes.add(Map.entry(GSUser.AUTH_PROVIDER, gsUser.getAuthProvider()));
	}

	if (gsUser.getRole() != null) {

	    attributes.add(Map.entry(GSUser.ROLE, gsUser.getRole()));
	}

	ArrayList<Entry<UserProfileAttribute, String>> userProfileAttr = new ArrayList<>();

	// the GSUser identifier mapped to keycloak username
	userProfileAttr.add(Map.entry(UserProfileAttribute.USERNAME, gsUser.getIdentifier()));

	//
	// all the GSUser properties which correspond to a keycloak user profile attribute are mapped
	//
	UserProfileAttribute.//
		getAttributes().//
		forEach(attr -> gsUser.getStringPropertyValue(attr).ifPresent(v ->
		userProfileAttr.add(Map.entry(UserProfileAttribute.of(attr).get(), v))));

	return new KeycloakUser.KeycloakUserBuilder().//
		enabled(gsUser.isEnabled()).//
		withUserProfileAttributes(userProfileAttr).//
		withAttributes(attributes).//
		build();
    }
}
