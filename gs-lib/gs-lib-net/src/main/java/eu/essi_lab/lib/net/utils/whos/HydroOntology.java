package eu.essi_lab.lib.net.utils.whos;

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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;

public abstract class HydroOntology {

    private String sparqlEndpoint = null;
    private String scheme = null;

    protected HydroOntology(String baseEndpoint) {
	this.sparqlEndpoint = baseEndpoint + "/sparql?";
	this.scheme = baseEndpoint + "/concept/scheme";
    }

    /**
     * Given a variable name, find correspondent concepts in the ontology
     * 
     * @param variableName
     * @return
     */
    public List<SKOSConcept> findConcepts(String variableName) {
	return findConcepts(variableName, false, true);
    }

    /**
     * Given a variable name, find correspondent concepts in the ontology optionally including children and equivalent
     * concepts
     * 
     * @param searchTerm can be a variable name in any language or a concept URI
     * @return
     */
    public List<SKOSConcept> findConcepts(String searchTerm, boolean includeChildrenAndEquivalents, boolean inScheme) {

	try

	{
	    String children = null;

	    if (includeChildrenAndEquivalents) {
		children = "?father skos:narrower* ?concept1.\n"//
			+ "  ?concept1 skos:closeMatch* ?concept.\n";
	    } else {
		children = "?father skos:closeMatch* ?concept.\n";
	    }

	    String query = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" //
		    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" //
		    + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"//
		    + "SELECT DISTINCT ?concept ?prefLabel ?altLabel\n" //
		    + "WHERE \n"//
		    + "{\n";

	    if (searchTerm.startsWith("http")) {

		query += "BIND(<${SEARCH_TERM}> AS ?father)\n" //
			+ children //
			+ "${IN_SCHEME}\n" //
			+ "OPTIONAL{?concept skos:prefLabel ?prefLabel}\n" //
			+ "OPTIONAL{?concept skos:altLabel ?altLabel} ";

	    } else {
		query += "?father rdf:type skos:Concept.\n" //
			+ "?father (skos:prefLabel|skos:altLabel) ?match.\n" //
			+ children //
			+ "${IN_SCHEME}\n" //
			+ "OPTIONAL{?concept skos:prefLabel ?prefLabel}\n" //
			+ "OPTIONAL{?concept skos:altLabel ?altLabel }\n" //
			+ "FILTER(regex(?match,\"^${SEARCH_TERM}$\",\"i\"))\n";
	    }

	    query += "}";

	    String is = "";
	    if (inScheme) {
		is = //
		     // "BIND(<" + scheme + "> AS ?s).\n" + //
		     // "?concept skos:inScheme ?s.\n";
			"?concept skos:inScheme <" + scheme + ">.\n";
	    }
	    query = query.replace("${IN_SCHEME}", is);
	    // regex special characters escaping
	    searchTerm = searchTerm.replace("(", "\\\\(").replace(")", "\\\\)");
	    query = query.replace("${SEARCH_TERM}", searchTerm);
	    return conceptQuery(query);
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {

	}

	return new ArrayList<SKOSConcept>();
    }

    public List<SKOSConcept> getBroaders(String uri) {
	try {
	    InputStream queryStream = HydroOntology.class.getClassLoader().getResourceAsStream("whos/hydro-ontology-get-broaderTransitive.sparql");
	    String query = IOUtils.toString(queryStream, StandardCharsets.UTF_8);
	    queryStream.close();
	    if (!uri.startsWith("<")) {
		uri = "<" + uri + ">";
	    }
	    query = query.replace("${CONCEPT_URI}", uri);
	    List<SKOSConcept> concepts = conceptQuery(query);
	    return concepts;
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

    private static ExpiringCache<ArrayList<SKOSConcept>> cache = new ExpiringCache<>();

    static {
	cache.setDuration(1200000);
    }

    private List<SKOSConcept> conceptQuery(String query) throws Exception {
	query = URLEncoder.encode(query, "UTF-8");
	WMOOntology wmoOntology = new WMOOntology();
	ArrayList<SKOSConcept> ret = cache.get(query);
	if (ret != null) {
	    return ret;
	}
	Downloader d = new Downloader();
	d.setConnectionTimeout(TimeUnit.SECONDS, 20);

	String url = sparqlEndpoint + "output=json&format=application/json&query=";

	InputStream stream = d.downloadOptionalStream(url + query).get();

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

	    if (uri.contains("http://server/unset-base")) {
		continue;
	    }
	    SKOSConcept concept = map.get(uri);
	    if (concept == null) {
		if (uri.contains("codes.wmo.int/wmdr")) {
		    concept = wmoOntology.getVariable(uri);
		} else {
		    concept = new SKOSConcept(uri);
		}
		map.put(uri, concept);
	    }

	    if (binding.has("prefLabel")) {
		JSONObject prefLabelObject = binding.getJSONObject("prefLabel");
		String prefLabelValue = prefLabelObject.getString("value");
		String language = null;
		if (prefLabelObject.has("xml:lang")) {
		    language = prefLabelObject.getString("xml:lang");
		}
		SimpleEntry<String, String> preferredLabel = new SimpleEntry<>(prefLabelValue, language);
		concept.setPreferredLabel(preferredLabel);
	    }

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

	    if (binding.has("close")) {
		JSONObject closeObject = binding.getJSONObject("close");
		String closeValue = closeObject.getString("value");
		if (!closeValue.equals(uri)) {
		    concept.getCloseMatches().add(closeValue);
		}

	    }
	}
	ret = new ArrayList<SKOSConcept>(map.values());
	cache.put(query, ret);
	return ret;
    }

}
