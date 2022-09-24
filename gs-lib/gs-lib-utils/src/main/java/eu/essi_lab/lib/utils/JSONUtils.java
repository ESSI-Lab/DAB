package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Fabrizio
 */
public class JSONUtils {

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
     * @param json
     * @param key
     * @return
     */
    public static String getValue(JSONObject json, String key) {
	if (!json.has(key)) {
	    return null;
	}
	String value = json.get(key).toString().trim();
	if (value.equals("null")) {
	    return null;
	}
	return value;
    }
}
