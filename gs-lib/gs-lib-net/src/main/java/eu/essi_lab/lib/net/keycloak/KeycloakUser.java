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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class KeycloakUser {

    public static final String ID_FIELD = "id";
    public static final String ENABLED_FIELD = "enabled";

    /**
     * @author Fabrizio
     */
    public enum UserProfileAttribute {

	/**
	 *
	 */
	USERNAME("username"),

	/**
	 *
	 */
	EMAIL("email"),

	/**
	 *
	 */
	FIRST_NAME("firstName"),

	/**
	 *
	 */
	LAST_NAME("lastName");

	private String attribute;

	/**
	 * @param attribute
	 */
	private UserProfileAttribute(String attribute) {

	    this.attribute = attribute;
	}

	/***
	 *
	 */
	public String getAttribute() {

	    return attribute;
	}

	/**
	 * @return
	 */
	public static List<String> getAttributes() {

	    return Arrays.stream(values()).map(UserProfileAttribute::getAttribute).collect(Collectors.toList());
	}

	/**
	 * @param attribute
	 * @return
	 */
	public static boolean isUserProfileAttribute(String attribute) {

	    return getAttributes().contains(attribute);
	}

	/**
	 * @param attr
	 * @return
	 */
	public static Optional<UserProfileAttribute> of(String attr) {

	    return Arrays.stream(values()).filter(v -> v.getAttribute().equals(attr)).findFirst();
	}
    }

    private boolean enabled;
    private String identifier;
    private List<Entry<String, List<String>>> attributes;
    private HashMap<String, String> userProfileAttributes;

    /**
     *
     */
    private KeycloakUser() {

	this.attributes = new ArrayList<>();
	this.userProfileAttributes = new HashMap<>();
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
     * @return the attributes
     */
    public List<Entry<String, List<String>>> getAttributes() {

	return attributes;
    }

    /**
     * @return
     */
    public Optional<Entry<String, List<String>>> getAttribute(String name) {

	return attributes.stream().filter(attr -> attr.getKey().equals(name)).findFirst();
    }

    /**
     * @return
     */
    public List<String> getAttributeValues(String name) {

	return getAttribute(name).map(Entry::getValue).orElse(List.of());
    }

    /**
     * @return
     */
    public Optional<String> getAttributeValue(String name) {

	return getAttribute(name).map(attr -> attr.getValue().getFirst());
    }

    /**
     * @param attr
     * @return
     */
    public Optional<String> getUserProfileAttribute(UserProfileAttribute attr) {

	return Optional.ofNullable(userProfileAttributes.get(attr.getAttribute()));
    }

    /**
     * @return
     */
    public JSONObject toJSON() {

	JSONObject user = new JSONObject();
	user.put(ID_FIELD, identifier);
	user.put(ENABLED_FIELD, enabled);

	userProfileAttributes.keySet().forEach(attr -> {

	    String attrValue = userProfileAttributes.get(attr);

	    if (attrValue != null) {

		user.put(attr, attrValue);
	    }
	});

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

	out.identifier = object.getString(ID_FIELD);
	out.enabled = object.getBoolean(ENABLED_FIELD);

	UserProfileAttribute.getAttributes().forEach(attr -> {

	    String optString = object.optString(attr, null);

	    if (optString != null) {

		out.userProfileAttributes.put(attr, optString);
	    }
	});

	JSONObject attributes = object.optJSONObject("attributes");

	if (attributes != null) {

	    attributes.keySet().forEach(key -> {

		JSONArray values = attributes.getJSONArray(key);

		out.attributes.add(Map.entry(key, values.toList().stream().map(Object::toString).collect(Collectors.toList())));
	    });
	}

	return out;
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
	 * @param attr
	 * @param value
	 * @return
	 */
	public KeycloakUserBuilder withOptionalUserProfileAttribute(UserProfileAttribute attr, Optional<String> value) {

	    if (attr == null || value == null) {

		throw new IllegalArgumentException();
	    }

	    value.ifPresent(v -> user.userProfileAttributes.put(attr.getAttribute(), v));

	    return this;
	}

	/**
	 * @param attr
	 * @param value
	 * @return
	 */
	public KeycloakUserBuilder withUserProfileAttribute(UserProfileAttribute attr, String value) {

	    if (attr == null || value == null || value.isEmpty()) {

		throw new IllegalArgumentException();
	    }

	    user.userProfileAttributes.put(attr.getAttribute(), value);
	    return this;
	}

	/**
	 * @param attributes
	 * @return
	 */
	public KeycloakUserBuilder withUserProfileAttributes(List<Map.Entry<UserProfileAttribute, String>> attributes) {

	    if (attributes == null) {

		throw new IllegalArgumentException();
	    }

	    attributes.forEach(entry -> user.userProfileAttributes.put(entry.getKey().getAttribute(), entry.getValue()));

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

	/**
	 * @param attributes
	 * @return
	 */
	public KeycloakUserBuilder withAttributes(List<Map.Entry<String, String>> attributes) {

	    if (attributes == null) {

		throw new IllegalArgumentException();
	    }

	    user.attributes.addAll(attributes.//
		    stream().//
		    map(attr -> Map.entry(attr.getKey(), Arrays.asList(attr.getValue()))).//
		    collect(Collectors.toList()));

	    return this;
	}
    }

    @Override
    public String toString() {

	return toJSON().toString(3);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

	KeycloakUser user = new KeycloakUser.KeycloakUserBuilder().//
		enabled(false).//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pippo").//
		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pippo@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pippo").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "Pluto").//
		withAttribute("key1", "value1").//
		withAttribute("key2", "value2").//
		build();

	System.out.println(user.toJSON().toString(3));
    }
}
