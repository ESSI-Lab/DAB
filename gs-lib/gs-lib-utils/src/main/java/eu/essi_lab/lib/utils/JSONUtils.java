package eu.essi_lab.lib.utils;

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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Fabrizio
 * @author boldrini
 */
public class JSONUtils {

    /**
     * @param array
     * @return
     */
    public static List<JSONObject> map(JSONArray array) {

	return array.toList().//
		stream().//
		map(v -> new JSONObject((HashMap<String, String>) v)).//
		collect(Collectors.toList());
    }

    /**
     * @param stream
     * @return
     * @throws IOException
     */
    public static JSONObject fromStream(InputStream stream) throws IOException {

	String jsonResp = IOStreamUtils.asUTF8String(stream);
	return new JSONObject(jsonResp);
    }

    /**
     * Removes all the possible JSON objects representing name space declarations coming from the original XML document
     * 
     * @param jsonObject
     */
    public static void clearNSDeclarations(JSONObject jsonObject) {

	Iterator<String> keys = jsonObject.keys();
	while (keys.hasNext()) {

	    String key = keys.next();
	    Object obj = jsonObject.get(key);

	    if (obj instanceof JSONObject) {

		JSONObject clear = clearNSDeclarations_((JSONObject) obj);
		jsonObject.put(key, clear);

		clearNSDeclarations(clear);

	    } else if (obj instanceof JSONArray) {

		JSONArray array = (JSONArray) obj;
		array.forEach(o -> {

		    if (o instanceof JSONObject) {

			clearNSDeclarations((JSONObject) o);
		    }
		});
	    }
	}
    }

    private static JSONObject clearNSDeclarations_(JSONObject jsonObject) {

	List<String> allowedList = StreamUtils.iteratorToStream(jsonObject.keys()).//
		filter(key -> !key.startsWith("xmlns")).//
		collect(Collectors.toList());

	JSONObject clone = new JSONObject();

	for (String key : allowedList) {

	    clone.put(key, jsonObject.get(key));
	}

	return clone;
    }

    /**
     * Returns the value held by the given key as sting, if present and not null, else null
     * 
     * @param json JsonPath. array of arrays are not supported
     * @param key
     * @return
     */
    public static <T> T getValue(JSONObject json, Class<T> clazz, String path) {
	if (path.contains("/")) {
	    String[] split = path.split("/");
	    String firstPart = split[0];
	    String childPath = path.substring(path.indexOf("/") + 1);
	    JSONObject child;
	    if (firstPart.contains("[")) {
		String arrayName = firstPart.substring(0, firstPart.indexOf("["));
		if (!json.has(arrayName)) {
		    return null;
		}
		String posString = firstPart.substring(firstPart.indexOf("[")).replace("[", "").replace("]", "");
		Integer pos = Integer.parseInt(posString);
		JSONArray array = json.getJSONArray(arrayName);
		child = array.getJSONObject(pos);
	    } else {
		if (!json.has(firstPart)) {
		    return null;
		}
		child = json.getJSONObject(firstPart);
	    }
	    return getValue(child, clazz, childPath);
	} else {
	    if (path.contains("[")) {
		String arrayName = path.substring(0, path.indexOf("["));
		if (!json.has(arrayName)) {
		    return null;
		}
		String posString = path.substring(path.indexOf("[")).replace("[", "").replace("]", "");
		Integer pos = Integer.parseInt(posString);
		JSONArray array = json.getJSONArray(arrayName);
		if (array.isNull(pos)) {
		    return null;
		}
		if (clazz.equals(BigDecimal.class)) {
		    return (T) array.getBigDecimal(pos);
		} else if (clazz.equals(Integer.class)) {
		    return (T) new Integer(array.getInt(pos));
		} else if (clazz.equals(String.class)) {
		    return (T) array.getString(pos);
		} else if (clazz.equals(Double.class)) {
		    return (T) new Double(array.getDouble(pos));
		} else if (clazz.equals(BigInteger.class)) {
		    return (T) array.getBigInteger(pos);
		} else if (clazz.equals(Long.class)) {
		    return (T) new Long(array.getLong(pos));
		} else if (clazz.equals(Boolean.class)) {
		    return (T) new Boolean(array.getBoolean(pos));
		} else if (clazz.equals(JSONObject.class)) {
		    return (T) array.getJSONObject(pos);
		} else if (clazz.equals(JSONArray.class)) {
		    return (T) array.getJSONArray(pos);
		}
	    } else {
		if (json.isNull(path)) {
		    return null;
		}
		if (clazz.equals(BigDecimal.class)) {
		    return (T) json.getBigDecimal(path);
		} else if (clazz.equals(Integer.class)) {
		    return (T) new Integer(json.getInt(path));
		} else if (clazz.equals(String.class)) {
		    return (T) json.getString(path);
		} else if (clazz.equals(Double.class)) {
		    return (T) new Double(json.getDouble(path));
		} else if (clazz.equals(BigInteger.class)) {
		    return (T) json.getBigInteger(path);
		} else if (clazz.equals(Long.class)) {
		    return (T) new Long(json.getLong(path));
		} else if (clazz.equals(Boolean.class)) {
		    return (T) new Boolean(json.getBoolean(path));
		} else if (clazz.equals(JSONObject.class)) {
		    return (T) json.getJSONObject(path);
		} else if (clazz.equals(JSONArray.class)) {
		    return (T) json.getJSONArray(path);
		}
	    }

	}

	return null;

    }

    /**
     * Returns the JSON object at the given JsonPath, otherwise null
     * 
     * @param json
     * @param path
     * @return
     */
    public static JSONObject getJSONObject(JSONObject json, String path) {
	return getValue(json, JSONObject.class, path);
    }

    /**
     * Returns the big decimal at the given JsonPath, otherwise null
     * 
     * @param json
     * @param path
     * @return
     */
    public static BigDecimal getBigDecimal(JSONObject json, String path) {
	return getValue(json, BigDecimal.class, path);
    }

    /**
     * Returns the integer at the given JsonPath, otherwise null
     * 
     * @param json
     * @param path
     * @return
     */
    public static Integer getInteger(JSONObject json, String path) {
	return getValue(json, Integer.class, path);
    }

    public static Boolean getBoolean(JSONObject json, String path) {
	return getValue(json, Boolean.class, path);
    }

    /**
     * Returns the string at the given JsonPath, otherwise null
     * 
     * @param json
     * @param path
     * @return
     */
    public static String getString(JSONObject json, String path) {
	return getValue(json, String.class, path);
    }

    public static List<String> getStrings(JSONObject json, String path) {
	JSONArray array = getValue(json, JSONArray.class, path);
	List<String> ret = new ArrayList<>();
	if (array == null) {
	    return ret;
	}
	for (int i = 0; i < array.length(); i++) {
	    ret.add(array.getString(i));
	}
	return ret;
    }

}
