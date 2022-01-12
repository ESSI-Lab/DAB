package eu.essi_lab.lib.net.utils.whos;

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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.utils.Downloader;

public class HydroOntology {
    public List<SKOSConcept> findConcepts(String variableName) {

	try {

	    InputStream queryStream = HydroOntology.class.getClassLoader()
		    .getResourceAsStream("whos/hydro-ontology-find-all-concepts.sparql");
	    String query = IOUtils.toString(queryStream, StandardCharsets.UTF_8);
	    query = query.replace("${SEARCH_TERM}", variableName);
	    return conceptQuery(query);
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {

	}

	return new ArrayList<SKOSConcept>();
    }

    /**
     * Gives the concept correspondent to the URI
     * 
     * @param variableName
     * @return
     */
    public SKOSConcept getConcept(String uri) {
	try {
	    InputStream queryStream = HydroOntology.class.getClassLoader().getResourceAsStream("whos/hydro-ontology-get-concept.sparql");
	    String query = IOUtils.toString(queryStream, StandardCharsets.UTF_8);
	    queryStream.close();
	    if (!uri.startsWith("<")) {
		uri = "<" + uri + ">";
	    }
	    query = query.replace("${CONCEPT_URI}", uri);
	    List<SKOSConcept> concepts = conceptQuery(query);
	    if (concepts.isEmpty()) {
		return null;
	    } else {
		return concepts.get(0);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {

	}
	return null;
    }

    private List<SKOSConcept> conceptQuery(String query) throws Exception {
	Downloader d = new Downloader();

	String url = "http://hydro.geodab.eu/hydro-ontology/sparql?output=json&format=application/json&query=";
	query = URLEncoder.encode(query, "UTF-8");

	InputStream stream = d.downloadStream(url + query).get();
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	IOUtils.copy(stream, baos);
	stream.close();
	baos.close();
	String str = new String(baos.toByteArray());
	JSONObject jo = new JSONObject(str);
	JSONObject results = jo.getJSONObject("results");
	JSONArray bindings = results.getJSONArray("bindings");
	HashMap<String, SKOSConcept> map = new HashMap<>();
	for (int i = 0; i < bindings.length(); i++) {

	    JSONObject binding = bindings.getJSONObject(i);
	    JSONObject conceptObject = binding.getJSONObject("concept");
	    String uri = conceptObject.getString("value");

	    SKOSConcept concept = map.get(uri);
	    if (concept == null) {
		concept = new SKOSConcept(uri);
		map.put(uri, concept);
	    }

	    JSONObject prefLabelObject = binding.getJSONObject("prefLabel");
	    String prefLabelValue = prefLabelObject.getString("value");
	    String language = null;
	    if (prefLabelObject.has("xml:lang")) {
		language = prefLabelObject.getString("xml:lang");
	    }
	    SimpleEntry<String, String> preferredLabel = new SimpleEntry<>(prefLabelValue, language);
	    concept.setPreferredLabel(preferredLabel);

	    if (binding.has("altLabel")) {
		JSONObject altLabelObject = binding.getJSONObject("altLabel");
		String altLabelValue = altLabelObject.getString("value");
		String altLanguage = null;
		if (altLabelObject.has("xml:lang")) {
		    altLanguage = altLabelObject.getString("xml:lang");
		}
		SimpleEntry<String, String> altLabel = new SimpleEntry<>(altLabelValue, altLanguage);
		concept.getAlternateLabels().add(altLabel);

	    }
	}
	return new ArrayList<SKOSConcept>(map.values());
    }

}
