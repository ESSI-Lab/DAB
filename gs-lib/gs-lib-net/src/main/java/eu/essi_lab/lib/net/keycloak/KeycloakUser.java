/**
 * 
 */
package eu.essi_lab.lib.net.keycloak;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Fabrizio
 */
public class KeycloakUser {

    private boolean enabled;
    private String identifier;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private List<Entry<String, List<String>>> attributes;

    /**
     * 
     */
    private KeycloakUser() {

	this.attributes = new ArrayList<>();
	this.enabled = true;
    }

    /**
     * @return
     */
    public Optional<String> getIdentifier() {

	return Optional.ofNullable(identifier);
    }

    /**
     * @param identifier
     */
    void setIdentifier(String identifier) {

	this.identifier = identifier;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {

	return enabled;
    }

    /**
     * @return the userName
     */
    public String getUserName() {

	return userName;
    }

    /**
     * @return the email
     */
    public Optional<String> getEmail() {

	return Optional.ofNullable(email);
    }

    /**
     * @return the firstName
     */
    public Optional<String> getFirstName() {

	return Optional.ofNullable(firstName);
    }

    /**
     * @return the lastName
     */
    public Optional<String> getLastName() {

	return Optional.ofNullable(lastName);
    }

    /**
     * @return the attributes
     */
    public List<Entry<String, List<String>>> getAttributes() {

	return attributes;
    }

    /**
     * @return
     */
    public JSONObject toJSON() {

	JSONObject user = new JSONObject();
	user.put("username", userName);
	user.put("email", email);
	user.put("id", identifier);
	user.put("enabled", enabled);

	if (firstName != null) {
	    user.put("firstName", firstName);
	}

	if (lastName != null) {
	    user.put("lastName", lastName);
	}

	if (!attributes.isEmpty()) {

	    JSONObject attr = new JSONObject();
	    attributes.forEach(a -> attr.put(a.getKey(), new JSONArray(a.getValue())));

	    user.put("attributes", attr);
	}

	return user;
    }

    /**
     * @param object
     * @return
     */
    public static KeycloakUser of(JSONObject object) {

	KeycloakUser out = new KeycloakUser();

	out.identifier = object.getString("id");
	out.userName = object.getString("username");
	out.enabled = object.getBoolean("enabled");

	out.email = optValue(object, "email");
	out.firstName = optValue(object, "firstName");
	out.lastName = optValue(object, "lastName");

	JSONObject attributes = object.optJSONObject("attributes");

	if (attributes != null) {

	    attributes.keySet().forEach(key -> {

		JSONArray values = attributes.getJSONArray(key);

		out.attributes.add(Map.entry(key, values.toList().stream().map(v -> v.toString()).collect(Collectors.toList())));
	    });
	}

	return out;
    }

    /**
     * @param object
     * @param key
     * @return
     */
    private static String optValue(JSONObject object, String key) {

	return Optional.ofNullable(object.optString(key)).map(v -> v.isEmpty() ? null : v).orElse(null);
    }

    /**
     * @author Fabrizio
     */
    public static class KeycloakUserBuilder {

	private KeycloakUser user;

	/**
	 * 
	 */
	public KeycloakUserBuilder() {

	    user = new KeycloakUser();
	}

	/**
	 * @return
	 */
	public static KeycloakUserBuilder get() {

	    return new KeycloakUserBuilder();
	}

	/**
	 * @return
	 */
	public KeycloakUser build() {

	    return user;
	}

	/**
	 * @param enabled
	 * @return
	 */
	public KeycloakUserBuilder enabled(boolean enabled) {

	    user.enabled = enabled;
	    return this;
	}

	/**
	 * @param identifier
	 * @return
	 */
	public KeycloakUserBuilder withIdentifier(String identifier) {

	    if (identifier == null || identifier.isEmpty()) {

		throw new IllegalArgumentException();
	    }

	    user.identifier = identifier;
	    return this;
	}

	/**
	 * @param userName
	 * @return
	 */
	public KeycloakUserBuilder withUserName(String userName) {

	    if (userName == null || userName.isEmpty()) {

		throw new IllegalArgumentException();
	    }

	    user.userName = userName;
	    return this;
	}

	/**
	 * @param email
	 * @return
	 */
	public KeycloakUserBuilder withEmail(String email) {

	    if (email == null || email.isEmpty()) {

		throw new IllegalArgumentException();
	    }

	    user.email = email;
	    return this;
	}

	/**
	 * @param firstName
	 * @return
	 */
	public KeycloakUserBuilder withFirstName(String firstName) {

	    if (firstName == null || firstName.isEmpty()) {

		throw new IllegalArgumentException();
	    }

	    user.firstName = firstName;
	    return this;
	}

	/**
	 * @param lastName
	 * @return
	 */
	public KeycloakUserBuilder withLastName(String lastName) {

	    if (lastName == null || lastName.isEmpty()) {

		throw new IllegalArgumentException();
	    }

	    user.lastName = lastName;
	    return this;
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 */
	public KeycloakUserBuilder withAttribute(String key, String... value) {

	    if (key == null || key.isEmpty() || value == null || value.length == 0) {

		throw new IllegalArgumentException();
	    }

	    user.attributes.add(Map.entry(key, Arrays.asList(value)));
	    return this;
	}
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

	KeycloakUser user = new KeycloakUser.KeycloakUserBuilder().//
		enabled(false).//
		withUserName("pippo").//
		withEmail("pippo@gmail.com").//
		withFirstName("Pippo").//
		withLastName("Pluto").//
		withAttribute("key1", "value1").//
		withAttribute("key2", "value2").//
		build();

	System.out.println(user.toJSON().toString(3));
    }
}
