/**
 * 
 */
package eu.essi_lab.api.database.marklogic.semantics;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.impl.SimpleValueFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.semantics.SPARQLMimeTypes;
import com.marklogic.client.semantics.SPARQLQueryDefinition;
import com.marklogic.client.semantics.SPARQLQueryManager;
import com.marklogic.xcc.exceptions.RequestException;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseSemanticsExecutor;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.api.database.marklogic.MarkLogicReader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.count.SemanticCountResponse;
import eu.essi_lab.messages.sem.SemanticBrowsing;
import eu.essi_lab.messages.sem.SemanticBrowsing.BrowsingAction;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticResponse;
import eu.essi_lab.messages.sem.SemanticSearch;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeOntology;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.ontology.GSPredicate;
import eu.essi_lab.model.ontology.JSONBindingWrapper;
import eu.essi_lab.model.ontology.RelationToParent;
import eu.essi_lab.model.ontology.d2k.D2KGSOntologyLoader;
import eu.essi_lab.model.ontology.d2k.GSKnowledgeResourceLoader;
import eu.essi_lab.model.ontology.d2k.resources.GSKnowledgeResource;

/**
 * @author Fabrizio
 */
public class MarkLogicSemanticsExecutor implements DatabaseSemanticsExecutor {

    private static final String MARK_LOGIC_SEM_COUNT_ERROR = "MARK_LOGIC_SEM_COUNT_ERROR";
    private static final String MARK_LOGIC_SEM_EXECUTION_ERROR = "MARK_LOGIC_SEM_EXECUTION_ERROR";
    private static final String MARK_LOGIC_GET_CONCEPT_ERROR = "MARK_LOGIC_GET_CONCEPT_ERROR";

    private MarkLogicDatabase markLogicDB;
    private StorageInfo dbUri;

    private static String rootType;

    /**
    * 
    */
    public MarkLogicSemanticsExecutor() {
    }

    /**
     * @param markLogicDB
     */
    public MarkLogicSemanticsExecutor(MarkLogicDatabase markLogicDB) {

	try {
	    rootType = new D2KGSOntologyLoader().getRootOWLClass().toString();
	} catch (GSException e) {

	    e.log();
	}

	this.markLogicDB = markLogicDB;
    }

    @Override
    public boolean supports(StorageInfo dbUri) {

	if (MarkLogicDatabase.isSupported(dbUri)) {

	    this.dbUri = dbUri;
	    return true;
	}

	return false;
    }

    /**
    * 
    */
    public SemanticResponse<GSKnowledgeResourceDescription> execute(SemanticMessage message) throws GSException {

	SemanticResponse<GSKnowledgeResourceDescription> response = new SemanticResponse<>();
	List<GSKnowledgeResourceDescription> list = new ArrayList<>();

	//
	// executes the counting before
	//
	SemanticCountResponse countResponse = count(message);
	response.setCountResponse(countResponse);

	Optional<String> query = Optional.empty();

	GSKnowledgeScheme scheme = null;

	if (countResponse.getCount() > 0) {

	    query = Optional.of(SemanticQueryBuilder.getInstance().buildOperationQuery(message));

	    if (message.isBrowsingOperationSet()) {

		GSLoggerFactory.getLogger(getClass()).debug("Execution of browsing request {} STARTED", message.getRequestId());

		SemanticBrowsing browsing = message.getBrowsingOperation().get();

		scheme = browsing.getScheme();

		logBrowsingAction(message.getBrowsingOperation().get());

		Optional<String> subjectId = message.getBrowsingOperation().get().getSubjectId();

		if (subjectId.isPresent() && message.getBrowsingOperation().get().getAction() == BrowsingAction.EXPAND) {

		    GSLoggerFactory.getLogger(getClass()).trace("Looking for parent of request {}", message.getRequestId());

		    Optional<GSKnowledgeResourceDescription> parent = getKnowlegdeResource(scheme, subjectId.get());

		    parent.ifPresent(p -> {

			GSLoggerFactory.getLogger(MarkLogicReader.class).trace("Setting parent object to resource {}",
				p.getResource().stringValue());

			response.setParentObject(p);
		    });
		}
	    } else if (message.isSearchOperationSet()) {

		GSLoggerFactory.getLogger(getClass()).debug("Execution of discovery request {} STARTED", message.getRequestId());

		SemanticSearch search = message.getSearchOperation().get();

		scheme = search.getScheme();

		logSemanticDiscovery(search);
	    }
	}

	if (query.isPresent()) {

	    try {

		list = execSPARQLQuery(//
			query.get(), //
			Optional.empty(), //
			scheme, //
			Optional.ofNullable(message.getPage()));

	    } catch (RequestException e) {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			MARK_LOGIC_SEM_EXECUTION_ERROR, //
			e);
	    }
	}

	response.setResultsList(list);

	GSLoggerFactory.getLogger(getClass()).debug("Execution of request {} ENDED", message.getRequestId());

	return response;
    }

    /**
     * 
     */
    public SemanticCountResponse count(SemanticMessage message) throws GSException {

	int count = 0;

	Optional<String> query = SemanticQueryBuilder.getInstance().buildCountOperationQuery(message);

	GSKnowledgeScheme scheme = null;

	if (message.isBrowsingOperationSet()) {

	    GSLoggerFactory.getLogger(getClass()).trace("Counting of browsing request {} STARTED", message.getRequestId());

	    SemanticBrowsing browsing = message.getBrowsingOperation().get();

	    scheme = browsing.getScheme();

	    logBrowsingAction(browsing);

	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("Counting of discovery request {} STARTED", message.getRequestId());

	    SemanticSearch search = message.getSearchOperation().get();

	    scheme = search.getScheme();

	    logSemanticDiscovery(search);
	}

	try {

	    if (query.isPresent()) {

		count = countSPARQLQuery(query.get(), scheme);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_SEM_COUNT_ERROR, //
		    e);
	}

	SemanticCountResponse countResponse = new SemanticCountResponse();
	countResponse.setCount(count);

	GSLoggerFactory.getLogger(getClass()).trace("Found {} concepts", count);

	GSLoggerFactory.getLogger(

		getClass()).trace("Counting of request {} ENDED", message.getRequestId());

	return countResponse;
    }

    /**
     * 
     */
    public Optional<GSKnowledgeResourceDescription> getKnowlegdeResource(GSKnowledgeScheme scheme, String subjectid) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving concept {}", subjectid);

	String query = SemanticQueryBuilder.getInstance().buildGetKnowlegdeResourceQuery(//
		subjectid, //
		scheme.getLabelPredicate(), //
		scheme.getAbstractPredicate());

	try {

	    List<GSKnowledgeResourceDescription> response = execSPARQLQuery(//
		    query, //
		    Optional.of(subjectid), //
		    scheme, //
		    Optional.empty());

	    if (!response.isEmpty()) {

		return Optional.of(response.get(0));
	    }

	    GSLoggerFactory.getLogger(getClass()).warn("Could not find concept {}", subjectid);

	    return Optional.empty();

	} catch (RequestException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MARK_LOGIC_GET_CONCEPT_ERROR, //
		    e);

	}
    }

    /**
     * @param query
     * @param labelPredicate
     * @param abstractPredicate
     * @param scheme
     * @return
     * @throws RequestException
     */
    private List<GSKnowledgeResourceDescription> execSPARQLQuery(//
	    String query, //
	    Optional<String> optionalId, //
	    GSKnowledgeScheme scheme, //
	    Optional<Page> page //
    ) throws RequestException {

	SPARQLQueryManager manager = markLogicDB.getWrapper().getSPARQLQueryManager();

	SPARQLQueryDefinition sparqlQuery = manager.newQueryDefinition(query);

	//
	// Removing the comment below, should allow the execution of transitive queries
	//
	// sparqlQuery.setRulesets(//
	// SPARQLRuleset.EQUIVALENT_PROPERTY, //
	// SPARQLRuleset.RDFS);

	JacksonHandle handle = new JacksonHandle();
	handle.setMimetype(SPARQLMimeTypes.SPARQL_JSON);

	handle = manager.executeSelect(sparqlQuery, handle);

	JsonNode tuples = handle.get();

	JSONObject jsonObject = new JSONObject(tuples.toString());

	JSONObject results = jsonObject.getJSONObject("results");

	JSONArray bindings = results.getJSONArray("bindings");

	List<GSKnowledgeResourceDescription> out = Lists.newArrayList();

	//
	// if the id is present, this is a getConcept query so the result is a single
	// concept or the same concept with different languages. in the latter case, since the object element is missing
	// for this
	// kind of results, to apply the reduction we use the ontology element
	//
	String groupByTarget = optionalId.isPresent() ? "ontology" : "object";

	List<JSONBindingWrapper> reducedBindings = JSONBindingWrapper.reduceBindings(groupByTarget, bindings, Arrays.asList("label"));

	for (JSONBindingWrapper bindingWrapper : reducedBindings) {

	    Optional<GSKnowledgeResource> optionalResource = createResource(bindingWrapper, optionalId);

	    if (!optionalResource.isPresent()) {
		continue;
	    }

	    GSKnowledgeResourceDescription obj = mapProperties(//
		    bindingWrapper, //
		    optionalResource.get(), //
		    scheme);

	    out.add(obj);
	}

	//
	// applies the pagination on the results list
	//
	// since some concepts can have multi-language labels, in order to respect the pagination values,
	// pagination must be handled after the tuples reduction instead of server-side by the SPARQL engine
	//
	if (page.isPresent()) {

	    int from = page.get().getStart() - 1;

	    int to = Math.min(from + page.get().getSize(), out.size());

	    out = out.subList(from, to);
	}

	return out;
    }

    /**
     * @param query
     * @param scheme
     * @return
     * @throws RequestException
     */
    private int countSPARQLQuery(String query, GSKnowledgeScheme scheme) throws RequestException {

	List<GSKnowledgeResourceDescription> list = execSPARQLQuery(//
		query, //
		Optional.empty(), //
		scheme, //
		Optional.empty());

	return list.size();
    }

    /**
     * @param bindingWrapper
     * @param optionalId
     * @return
     */
    private Optional<GSKnowledgeResource> createResource(JSONBindingWrapper bindingWrapper, Optional<String> optionalId) {

	Optional<String> optionalType = bindingWrapper.readValue("type");

	if (!optionalType.isPresent()) {

	    GSLoggerFactory.getLogger(MarkLogicReader.class)
		    .warn("Found a concept with no type in Marklogic response, skipping [json is {}]", bindingWrapper.toString());

	    return Optional.empty();
	}

	Optional<GSKnowledgeResource> optionalResource = Optional.empty();

	if (!optionalId.isPresent()) {

	    optionalId = bindingWrapper.readValue("object");

	    if (!optionalId.isPresent()) {

		GSLoggerFactory.getLogger(MarkLogicReader.class)
			.warn("Found a concept with no id in Marklogic response, skipping [json is {}]", bindingWrapper.toString());

		return Optional.empty();
	    }
	}

	if (optionalId.isPresent()) {

	    optionalResource = GSKnowledgeResourceLoader.instantiateKnowledgeResource(optionalId.get(), optionalType.get());

	} else {

	    GSLoggerFactory.getLogger(MarkLogicReader.class).warn("No class was found for type {}, skipping [json is {}]",
		    optionalType.get(), bindingWrapper.toString());

	    return Optional.empty();
	}

	return optionalResource;
    }

    /**
     * @param bindingWrapper
     * @param obj
     */
    private void addRelationToParent(GSKnowledgeScheme scheme, JSONBindingWrapper bindingWrapper, GSKnowledgeResourceDescription obj) {

	bindingWrapper.readValue("p").ifPresent(relationToParent -> {

	    RelationToParent rtp = new RelationToParent();

	    GSPredicate predicate = GSPredicate.create(scheme, relationToParent);

	    rtp.setRelation(predicate);

	    obj.setRelationToParent(rtp);

	    bindingWrapper.readValue("plabel").ifPresent(rtp::setRelationName);

	    bindingWrapper.readValue("pontology").ifPresent(relationtOnt -> {

		GSKnowledgeOntology ontologySource = new GSKnowledgeOntology();

		ontologySource.setId(relationtOnt);

		bindingWrapper.readValue("pontologyName").ifPresent(ontologySource::setName);

		bindingWrapper.readValue("pontologyDescription").ifPresent(ontologySource::setDescription);

		rtp.setOntology(ontologySource);
	    });
	});
    }

    /**
     * @param bindingWrapper
     * @param resource
     * @param scheme
     * @return
     */
    private GSKnowledgeResourceDescription mapProperties(//
	    JSONBindingWrapper bindingWrapper, //
	    GSKnowledgeResource resource, //
	    GSKnowledgeScheme scheme) {

	SimpleValueFactory factory = SimpleValueFactory.getInstance();

	GSKnowledgeResourceDescription obj = new GSKnowledgeResourceDescription(resource);

	int labelsCount = bindingWrapper.getElementsCount("label");

	for (int i = 0; i < labelsCount; i++) {

	    String label = bindingWrapper.readValue("label", i).get();
	    Optional<String> lan = bindingWrapper.readLanguage("label", i);

	    if (lan.isPresent()) {

		obj.add(scheme.getLabelPredicate(), factory.createLiteral(label, lan.get()));

	    } else {

		obj.add(scheme.getLabelPredicate(), factory.createLiteral(label));
	    }
	}

	bindingWrapper.readValue("abstract").ifPresent(def -> obj.add(scheme.getAbstractPredicate(), factory.createLiteral(def)));

	mapOntology(bindingWrapper, obj);

	addRelationToParent(scheme, bindingWrapper, obj);

	return obj;
    }

    /**
     * @param bindingWrapper
     * @param concept
     */
    private void mapOntology(JSONBindingWrapper bindingWrapper, GSKnowledgeResourceDescription concept) {

	bindingWrapper.readValue("ontology").ifPresent(ontid -> {

	    GSKnowledgeOntology source = new GSKnowledgeOntology();

	    source.setId(ontid);

	    concept.getResource().setSource(source);

	    bindingWrapper.readValue("ontologyName").ifPresent(source::setName);

	    bindingWrapper.readValue("ontologyDescription").ifPresent(source::setDescription);
	});
    }

    /**
     * @param browsing
     */
    private void logBrowsingAction(SemanticBrowsing browsing) {

	BrowsingAction action = browsing.getAction();

	List<String> searchTerms = browsing.getSearchTerms();
	Optional<String> subjectId = browsing.getSubjectId();
	Optional<String> ontologyId = browsing.getOntologyId();

	GSKnowledgeScheme knowledgeScheme = browsing.getScheme();

	GSLoggerFactory.getLogger(getClass()).debug("Browsing action {}", action);
	GSLoggerFactory.getLogger(getClass()).debug("Search terms {}", searchTerms);
	GSLoggerFactory.getLogger(getClass()).debug("Subject id {}", subjectId);
	GSLoggerFactory.getLogger(getClass()).debug("Ontology id {}", ontologyId);
	GSLoggerFactory.getLogger(getClass()).debug("Knowledge schema id {}", knowledgeScheme.getNamespace());
    }

    /**
     * @param search
     */
    private void logSemanticDiscovery(SemanticSearch search) {

	List<String> searchTerms = search.getSearchTerms();
	Optional<String> ontologyId = search.getOntologyId();

	GSKnowledgeScheme knowledgeScheme = search.getScheme();

	GSLoggerFactory.getLogger(getClass()).debug("Search terms {}", searchTerms);
	GSLoggerFactory.getLogger(getClass()).debug("Ontology id {}", ontologyId);
	GSLoggerFactory.getLogger(getClass()).debug("Knowledge schema id {}", knowledgeScheme.getNamespace());
    }

    @Override
    public void setDatabase(Database dataBase) {

	this.markLogicDB = (MarkLogicDatabase) dataBase;
    }

    @Override
    public MarkLogicDatabase getDatabase() {

	return this.markLogicDB;
    }

}
