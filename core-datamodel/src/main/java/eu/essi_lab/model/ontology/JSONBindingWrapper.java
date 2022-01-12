package eu.essi_lab.model.ontology;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;
public class JSONBindingWrapper {

    private final JSONObject jsonObject;

    /**
     * @param binding
     */
    public JSONBindingWrapper(String binding) {

	this(new JSONObject(binding));
    }

    /**
     * @param jsonObject
     */
    public JSONBindingWrapper(JSONObject jsonObject) {

	this.jsonObject = jsonObject;
    }

    /**
     * @param groupByTarget the element to use as grouping target
     * @param bindingsArray the array with the SPARQL query bindings
     * @param targets the elements on which operate the reduction
     * @return
     */
    public static List<JSONBindingWrapper> reduceBindings(String groupByTarget, JSONArray bindingsArray, List<String> targets) {

	Map<String, List<JSONBindingWrapper>> objectMap = //
		Lists.newArrayList(bindingsArray.iterator()).//
			stream().//
			map(o -> new JSONBindingWrapper((JSONObject) o)).//
			filter(w -> w.readValue(groupByTarget).isPresent()).//
			collect(//
				Collectors.groupingBy(w -> w.readValue(groupByTarget).get()));
	Set<String> keySet = objectMap.keySet();

	List<JSONBindingWrapper> out = new ArrayList<>();

	for (String objectId : keySet) {

	    List<JSONBindingWrapper> list = objectMap.get(objectId);

	    if (list.size() > 1) {

		//
		// at the moment the binding used as reduced binding is randomly
		// choosed. this is always fine only if targets contains all the possible
		// multi language elements
		//
		JSONBindingWrapper reducedWrapper = list.get(0);
		JSONObject binding = reducedWrapper.getBinding();

		for (String target : targets) {

		    JSONArray targetArray = new JSONArray();
		    ArrayList<String> arrayList = new ArrayList<>();

		    for (int i = 0; i < list.size(); i++) {

			Optional<String> lan = list.get(i).readLanguage(target);
			String val = list.get(i).readValue(target).get();
			String type = list.get(i).readType(target).get();

			JSONObject element = new JSONObject();

			lan.ifPresent(l -> element.put("xml:lang", l));
			element.put("value", val);
			element.put("type", type);

			if (!arrayList.contains(element.toString())) {
			    arrayList.add(element.toString());
			    targetArray.put(element);
			}
		    }

		    binding.put(target, targetArray);
		}

		reducedWrapper = new JSONBindingWrapper(binding);

		out.add(reducedWrapper);

	    } else {

		out.add(list.get(0));
	    }
	}

	return out;
    }

    /**
     * @return
     */
    public JSONObject getBinding() {

	return jsonObject;
    }

    /**
     * @return
     */
    public int getElementsCount(String key) {

	if (jsonObject.has(key)) {

	    Object object = jsonObject.get(key);

	    if (object instanceof JSONObject) {

		return 1;

	    } else if (object instanceof JSONArray) {

		return ((JSONArray) object).length();
	    }
	}

	return 0;
    }

    /**
     * Reads the value of the binding element with the provided key at the provided index (e.g:
     * "http://eu.floraresearch.essi.core/test/target2", "Target 1.2");
     * 
     * @param key
     * @param index
     * @return
     */
    public Optional<String> readValue(String key, int index) {

	return read(key, "value", index);
    }

    /**
     * Reads the language of the binding element with the provided key at the provided index.<br>
     * It can be included only for element of type "literal" (e.g: "en");
     * 
     * @param key
     * @param index
     * @return
     */
    public Optional<String> readLanguage(String key, int index) {

	return read(key, "xml:lang", index);
    }

    /**
     * Reads the type of the of the binding element with the provided key at the provided index (e.g: "uri", "literal");
     *
     * @param key
     * @param index
     * @return
     */
    public Optional<String> readType(String key, int index) {

	return read(key, "type", index);
    }

    /**
     * Reads the value of the first binding element with the provided key (e.g:
     * "http://eu.floraresearch.essi.core/test/target2", "Target 1.2");
     * 
     * @param key
     * @return
     */
    public Optional<String> readValue(String key) {

	return read(key, "value", 0);
    }

    /**
     * Reads the language of the first binding element with the provided key.<br>
     * It can be included only for element of type "literal" (e.g: "en");
     * 
     * @param key
     * @return
     */
    public Optional<String> readLanguage(String key) {

	return read(key, "xml:lang", 0);
    }

    /**
     * Reads the type of the first binding element with the provided key (e.g: "uri", "literal");
     * 
     * @param key
     * @return
     */
    public Optional<String> readType(String key) {

	return read(key, "type", 0);
    }

    /**
     * @param key
     * @param index
     * @return
     */
    private Optional<String> read(String key, String target, int index) {

	JSONObject json = null;

	if (jsonObject.has(key)) {

	    Object object = jsonObject.get(key);

	    if (object instanceof JSONObject) {

		if (index > 0) {

		    return Optional.empty();
		}

		json = (JSONObject) object;

	    } else if (object instanceof JSONArray) {

		JSONArray array = ((JSONArray) object);
		if (array.length() > index) {

		    json = array.getJSONObject(index);

		}
	    }
	}

	if (json != null && json.has(target)) {

	    return Optional.of(json.getString(target).toString());
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    @Override
    public String toString() {

	return jsonObject.toString(3);
    }

    @Override
    public boolean equals(Object o) {

	if (o instanceof JSONBindingWrapper) {

	    return this.toString().equals(o.toString());
	}

	return false;
    }

    @Override
    public int hashCode() {

	return toString().hashCode();
    }

}
