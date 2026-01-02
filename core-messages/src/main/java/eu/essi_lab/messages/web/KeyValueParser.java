package eu.essi_lab.messages.web;

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

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.lib.utils.StringUtils;

/**
 * A parser which creates a key/value map from a query string or from an {@link InputStream}. In the latter case, the
 * stream content should be parsable to a key/value map
 * 
 * @author Fabrizio
 */
public class KeyValueParser {

    /**
     * 
     */
    public static final String UNDEFINED = "___undefined___";
    private Map<String, String> map;
    private boolean decodeValues;

    /**
     * Creates a new parser
     */
    public KeyValueParser() {
	map = new HashMap<>();
    }

    /**
     * Creates a new parser
     */
    public KeyValueParser(boolean decodeValues) {
	this.decodeValues = decodeValues;
	map = new HashMap<>();
    }

    /**
     * Creates a new parser from the given stream parser <code>streamParser</code> and <code>inputStream</code>
     * 
     * @param streamParser
     * @param inputStream
     */
    public KeyValueParser(StreamParser streamParser, InputStream inputStream) {

	setStreamParser(streamParser, inputStream);
    }

    /**
     * Creates a new parser from the given stream parser <code>streamParser</code> and <code>inputStream</code>
     * 
     * @param streamParser
     * @param inputStream
     */
    public KeyValueParser(StreamParser streamParser, InputStream inputStream, boolean decodeValues) {

	this.decodeValues = decodeValues;
	setStreamParser(streamParser, inputStream);
    }

    /**
     * Creates a parser from the given <code>queryString</code>
     * 
     * @param queryString
     */
    public KeyValueParser(String queryString) {

	setQueryString(queryString);
    }

    /**
     * Creates a parser from the given <code>queryString</code>
     * 
     * @param queryString
     */
    public KeyValueParser(String queryString, boolean decodeValues) {

	this.decodeValues = decodeValues;
	setQueryString(queryString);
    }

    /**
     * Return the value of the given <code>key</code>, <code>null</code> if <code>key</code> does not exist,
     * {@link #UNDEFINED} if the key exists but its value is the empty string (e.g.: 'key=')
     * 
     * @param key
     * @param ignoreCase
     * @param defaultValue
     * @return
     */
    public String getValue(String key, boolean ignoreCase, String defaultValue) {

	if (!isValid(key)) {

	    return defaultValue;
	}

	return getValue(key, ignoreCase);
    }

    /**
     * @param key
     * @param ignoreCase
     * @param defaultValue
     * @return
     */
    public Optional<String> getOptionalValue(String key, boolean ignoreCase, String defaultValue) {

	return Optional.ofNullable(getValue(key, ignoreCase, defaultValue));
    }

    /**
     * Return the value of the given <code>key</code>, <code>null</code> if <code>key</code> does not exist,
     * {@link #UNDEFINED} if the key exists but its value is the empty string (e.g.: 'key=')
     * 
     * @param key
     * @param ignoreCase
     * @return
     */
    public String getValue(String key, boolean ignoreCase) {

	if (ignoreCase) {

	    Set<String> keySet = map.keySet();
	    for (String currentKey : keySet) {
		if (currentKey.equalsIgnoreCase(key)) {
		    return map.get(currentKey);
		}
	    }
	}

	return map.get(key);
    }

    /**
     * @param key
     * @param ignoreCase
     * @return
     */
    public Optional<String> getOptionalValue(String key, boolean ignoreCase) {

	return Optional.ofNullable(getValue(key, ignoreCase));
    }

    /**
     * Return the value of the given <code>key</code> (case sensitive), <code>null</code> if <code>key</code> does not
     * exist, {@link #UNDEFINED} if the key exists but its value is the empty string (e.g.: 'key=')
     * 
     * @param key
     * @return
     */
    public String getValue(String key) {

	return getValue(key, false);
    }

    /**
     * @param key
     * @return
     */
    public Optional<String> getOptionalValue(String key) {

	return Optional.ofNullable(getValue(key));
    }

    /**
     * Return the value of the given <code>key</code> (case sensitive), <code>null</code> if <code>key</code> does not
     * exist, {@link #UNDEFINED} if the key exists but its value is the empty string (e.g.: 'key=')
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    public String getValue(String key, String defaultValue) {

	if (!isValid(key)) {

	    return defaultValue;
	}

	return getValue(key, false);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public Optional<String> getOptionalValue(String key, String defaultValue) {

	return Optional.ofNullable(getValue(key, defaultValue));
    }

    /**
     * Like {@link #getValue(String)} but UTF-8 decoded and with exact case match
     * 
     * @param key
     * @return
     */
    public String getDecodedValue(String key) {

	return getDecodedValue(key, false);
    }
    
    /**
     * Like {@link #getValue(String)} but UTF-8 decoded
     * 
     * @param key
     * @param ignoreCase
     * @return
     */
    public String getDecodedValue(String key, boolean ignoreCase) {

	String value = getValue(key, ignoreCase);
	if (value == null) {
	    return null;
	}

	return StringUtils.URLDecodeUTF8(value);
    }

    /**
     * Checks whether the value of the supplied <code>key</code> is non <code>null</code> and non {@link #UNDEFINED}
     * 
     * @param key
     * @return
     */
    public boolean isValid(String key) {

	return isValid(key, false);
    }

    /**
     * Checks whether the value of the supplied <code>key</code> is non <code>null</code> and non {@link #UNDEFINED}
     * 
     * @param key
     * @param ignoreCase
     * @return
     */
    public boolean isValid(String key, boolean ignoreCase) {

	String value = getValue(key, ignoreCase);

	return Objects.nonNull(value) && !value.equals(UNDEFINED);
    }

    /**
     * @param parser
     * @param inputStream
     */
    public void setStreamParser(StreamParser streamParser, InputStream inputStream) {

	if (streamParser != null) {
	    Map<String, String> map = streamParser.getKeyValueMap(inputStream);
	    if (map != null) {
		this.map = map;
	    }
	}
    }

    /**
     * @param queryString
     */
    public void setQueryString(String queryString) {

	if (queryString != null && queryString.endsWith("&")) {
	    queryString = queryString.substring(0, queryString.length() - 1);
	}
	this.map = createMap(queryString);
    }

    /**
     * @return
     */
    public Map<String, String> getParametersMap() {

	return map;
    }

    private Map<String, String> createMap(String queryString) {

	HashMap<String, String> hashMap = new HashMap<>();
	if (queryString != null) {
	    String[] split = queryString.split("&");

	    for (String string : split) {

		String[] keyValue = string.split("=");
		String key = keyValue[0];

		// -------------------------------------------
		//
		// a token without '='; e.g: "request=A&format", "request=A&format="
		//
		if (keyValue.length == 1) {

		    hashMap.put(key, UNDEFINED);
		}

		// ---------------------------------------------------------------------------
		//
		// a token with '=' and with no '=' char in the value; e.g: "request=A"
		//
		else if (keyValue.length == 2) {

		    String value = keyValue[1];

		    if (decodeValues) {
			value = URLDecoder.decode(value, StandardCharsets.UTF_8);
		    }

		    hashMap.put(key, value);

		    // ---------------------------------------------------------------------------
		    //
		    // a token with '=' and with one or more '=' char in the value; e.g: "request=A=B=C"
		    //
		} else {

		    String value = "";
		    for (int i = 1; i < keyValue.length; i++) {
			value += keyValue[i];
			if (i < keyValue.length - 1) {
			    value += "=";
			}
		    }

		    if (decodeValues) {
			value = URLDecoder.decode(value, StandardCharsets.UTF_8);
		    }

		    hashMap.put(key, value);
		}
	    }
	}
	return hashMap;
    }
}
