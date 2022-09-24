/**
 * 
 */
package eu.essi_lab.lib.net.sparql;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeOntology;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.JSONBindingWrapper;
import eu.essi_lab.model.ontology.d2k.resources.GSExternalKnowledgeResource;

/**
 * @author Fabrizio
 */
public class SPARQLHarvester {

    private int limit;
    private int step;
    private String endpoint;
    private GSKnowledgeResourceDescription rootDescription;
    private boolean storeRoot;
    private String jsonOutputParam;
    private DescriptionEnhancer enhancer;

    /**
     * @throws GSException
     */
    public SPARQLHarvester() throws GSException {

	setStep(100);
	setJsonOutputParam("Accept=application/sparql-results%2Bjson");
	setEnhancer(new SKOSDescriptionEnhancer());
    }

    /**
     * @return
     */
    public int getLimit() {

	return limit;
    }

    /**
     * @param limit
     */
    public void setLimit(int limit) {

	this.limit = limit;
    }

    /**
     * @return
     */
    public int getStep() {

	return step;
    }

    /**
     * @param step
     */
    public void setStep(int step) {

	this.step = step;
    }

    /**
     * @return
     */
    public String getSPARQLEndpoint() {

	return endpoint;
    }

    /**
     * @param sparqlEndpoint
     */
    public void setSPARQLEndpoint(String sparqlEndpoint) {

	if (!sparqlEndpoint.endsWith("?")) {
	    sparqlEndpoint += "?";
	}

	sparqlEndpoint += "query=";

	this.endpoint = sparqlEndpoint;
    }

    /**
     * @param rootDescription
     */
    public void setRootDescription(GSKnowledgeResourceDescription rootDescription) {

	this.rootDescription = rootDescription;
    }

    /**
     * @return
     */
    public String getJsonOutputParam() {

	return jsonOutputParam;
    }

    /**
     * @param jsonOutputParam
     */
    public void setJsonOutputParam(String jsonOutputParam) {

	this.jsonOutputParam = jsonOutputParam;
    }

    /**
     * @param enhancer
     */
    public void setEnhancer(DescriptionEnhancer enhancer) {

	this.enhancer = enhancer;
    }

    /**
     * 
     */
    public void collectLabels() {

	enhancer.collectLabels();
    }

    /**
     * @param storeRoot
     */
    public void storeRoot(boolean storeRoot) {

	this.storeRoot = storeRoot;
    }

    /**
     * @param consumer
     * @throws Exception
     */
    public void harvest(Consumer<GSKnowledgeResourceDescription> consumer) throws Exception {

	GSKnowledgeOntology ontology = rootDescription.getResource().getSource();

	if (storeRoot) {

	    //
	    // stores the root
	    //
	    GSLoggerFactory.getLogger(getClass()).debug("Storing ontology root STARTED");

	    consumer.accept(this.rootDescription);

	    GSLoggerFactory.getLogger(getClass()).debug("Storing ontology root ENDED");
	}

	int distinctSubjects = countDistinctSubjects(endpoint);

	GSLoggerFactory.getLogger(getClass()).debug("Found {} distinct subjects", distinctSubjects);

	if (limit > 0) {

	    GSLoggerFactory.getLogger(getClass()).debug("Limiting harvesting to {} concepts", limit);

	    distinctSubjects = limit;
	}

	for (int offset = 0; offset < distinctSubjects; offset += step) {
	    
	    GSLoggerFactory.getLogger(getClass()).debug("Current offset: {}/{}", offset,distinctSubjects);

	    //
	    // distinct subjects query
	    //
	    List<String> subjects = getDistinctSubjects(endpoint, offset, step);

	    for (String subject : subjects) {

		GSLoggerFactory.getLogger(getClass()).debug("Get related concepts query of subject {} STARTED", subject);

		String query = endpoint + createGetRelatedConceptsQuery(subject);

		GSLoggerFactory.getLogger(getClass()).debug("Get related concepts query of subject  {} ENDED", subject);

		JSONArray bindings = getBindings(query);

		//
		// creates the resource
		//
		GSExternalKnowledgeResource resource = new GSExternalKnowledgeResource();
		resource.setId(subject);
		resource.setSource(ontology);

		//
		// creates the resource description
		//
		GSKnowledgeResourceDescription description = new GSKnowledgeResourceDescription(resource);

		for (int i = 0; i < bindings.length(); i++) {

		    JSONObject binding = bindings.getJSONObject(i);

		    JSONBindingWrapper wrapper = new JSONBindingWrapper(binding);

		    String objectValue = StringUtils.trimNBSP(wrapper.readValue("obj").get());
		    String predValue = StringUtils.trimNBSP(wrapper.readValue("pred").get());
		    Optional<String> objLan = wrapper.readLanguage("obj");

		    //
		    // enhances description
		    //
		    enhancer.enhanceDescription(description, objectValue, predValue, objLan);
		}

		consumer.accept(description);
	    }
	}
    }

    /**
     * @return
     */
    public List<String> getLabels() {

	return enhancer.getLabels();
    }

    /**
     * @param query
     * @return
     */
    private JSONArray getBindings(String query) {

	Downloader downloader = new Downloader();
	
	Optional<String> optional = downloader.downloadString(query);
	
	if(!optional.isPresent()){
	    
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to get bindings");
	    
	    return new JSONArray();
	}
	
	String response = optional.get();

	return new JSONObject(response).getJSONObject("results").getJSONArray("bindings");
    }

    /**
     * @param endpoint
     * @return
     * @throws Exception
     */
    private int countDistinctSubjects(String endpoint) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Distinct subjects query STARTED");

	String query = endpoint + createDistinctSubjectsQuery(-1, -1);

	JSONArray bindings = getBindings(query);

	GSLoggerFactory.getLogger(getClass()).debug("Distinct subjects query ENDED");

	return bindings.length();
    }

    /**
     * @param offset
     * @param limit
     * @return
     */
    private List<String> getDistinctSubjects(String endpoint, int offset, int limit) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Distinct subjects query STARTED");
 
	String query = endpoint + createDistinctSubjectsQuery(offset, limit);

	JSONArray bindings = getBindings(query);

	GSLoggerFactory.getLogger(getClass()).debug("Distinct subjects query ENDED");

	if (bindings.length() == 0) {
	    return new ArrayList<>();
	}

	return Lists.newArrayList(bindings.iterator()).//
		stream().//
		map(o -> new JSONBindingWrapper((JSONObject) o)).//
		map(w -> w.readValue("sub").get()).//
		collect(Collectors.toList());//
    }

    /**
     * @param offset
     * @param limit
     * @return
     * @throws UnsupportedEncodingException
     */
    private String createDistinctSubjectsQuery(int offset, int limit) throws UnsupportedEncodingException {

	String out = "SELECT DISTINCT ?sub \n";
	out += "	WHERE {\n";
	out += "	  ?sub ?pred ?obj \n";
	out += "	  .\n";
	out += "	  ?sub <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept>.\n";
	out += "	}\n";

	if (offset > -1) {
	    out += "	OFFSET " + offset + " LIMIT " + limit + "\n";
	}

	return URLEncoder.encode(out, "UTF-8") + "&" + getJsonOutputParam();
    }

    /**
     * @param subject
     * @return
     * @throws UnsupportedEncodingException
     */
    private String createGetRelatedConceptsQuery(String subject) throws UnsupportedEncodingException {

	String out = "SELECT DISTINCT ?pred ?obj ";
	out += "	    WHERE {  ";
	out += "	      <" + subject + "> ?pred ?obj ";
	out += "	   }  ";
	out += "	   OFFSET 0 LIMIT 1000";

	return URLEncoder.encode(out, "UTF-8") + "&" + getJsonOutputParam();

	// String out = "SELECT DISTINCT ?pred ?obj \n";
	// out += " { { \n";
	// out += " <" + subject + "> ?pred ?obj } \n";
	// out += " UNION \n";
	// out += " { \n";
	// out += " ?obj ?pred <" + subject + "> \n";
	// out += " } } \n";
	// out += " OFFSET 0 LIMIT 1000";
	//
	// return URLEncoder.encode(out, "UTF-8") + "&Accept=application/sparql-results%2Bjson";
    }
}
