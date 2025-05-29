/**
 * 
 */
package eu.essi_lab.api.database.marklogic.semantics;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.sem.SemanticBrowsing;
import eu.essi_lab.messages.sem.SemanticBrowsing.BrowsingAction;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticSearch;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.ontology.GSPredicate;
import eu.essi_lab.model.ontology.d2k.D2KGSOntologyLoader;

/**
 * @author Fabrizio
 */
public class SemanticQueryBuilder {

    
    private static String rootType;

    static {
	try {
	    rootType = new D2KGSOntologyLoader().getRootOWLClass().toString();
	} catch (GSException e) {

	    e.log();
	}
    }

    private static final SemanticQueryBuilder INSTANCE = new SemanticQueryBuilder();

    /**
     * @return
     */
    public static SemanticQueryBuilder getInstance() {

	return INSTANCE;
    }

    /**
     * @param message
     * @return
     */
    public String buildOperationQuery(SemanticMessage message) {

	String query = null;

	GSKnowledgeScheme knowledgeScheme = null;

	if (message.isBrowsingOperationSet()) {

	    SemanticBrowsing browsing = message.getBrowsingOperation().get();

	    BrowsingAction action = browsing.getAction();
	    List<String> searchTerms = browsing.getSearchTerms();
	    Optional<String> subjectId = browsing.getSubjectId();
	    Optional<String> ontologyId = browsing.getOntologyId();

	    knowledgeScheme = browsing.getScheme();
	    List<GSPredicate> collapsePredicates = knowledgeScheme.getCollapsePredicates();
	    List<GSPredicate> expandPredicates = knowledgeScheme.getExpandPredicates();

	    if (!subjectId.isPresent()) {

		query = //
			createOperationQuery(//
				Optional.empty(), //
				ontologyId, //
				searchTerms, //
				null, //
				knowledgeScheme, //
				Optional.of(message.getPage()), //
				false//
			);

	    } else {

		switch (action) {

		case COLLAPSE:

		    query = createOperationQuery(//
			    subjectId, //
			    ontologyId, //
			    searchTerms, //
			    collapsePredicates, //
			    knowledgeScheme, //
			    Optional.ofNullable(message.getPage()), //
			    false//
		    );

		    break;
		case EXPAND:
		default:

		    query = //
			    createOperationQuery(//
				    subjectId, //
				    ontologyId, //
				    searchTerms, //
				    expandPredicates, //
				    knowledgeScheme, //
				    Optional.ofNullable(message.getPage()), //
				    false//
			    );

		    break;
		}
	    }

	} else if (message.isSearchOperationSet()) {

	    GSLoggerFactory.getLogger(getClass()).debug("Execution of discovery request {} STARTED", message.getRequestId());

	    SemanticSearch search = message.getSearchOperation().get();

	    knowledgeScheme = search.getScheme();

	    query = createOperationQuery(//
		    Optional.empty(), //
		    search.getOntologyId(), //
		    search.getSearchTerms(), //
		    null, //
		    knowledgeScheme, //
		    Optional.ofNullable(message.getPage()), //
		    true);

	}

	return query;
    }

    /**
     * @param message
     * @return
     */
    public Optional<String> buildCountOperationQuery(SemanticMessage message) {

	Optional<String> query = Optional.empty();

	GSKnowledgeScheme knowledgeScheme = null;

	if (message.isBrowsingOperationSet()) {

	    SemanticBrowsing browsing = message.getBrowsingOperation().get();

	    BrowsingAction action = browsing.getAction();
	    Optional<String> subjectId = browsing.getSubjectId();
	    List<String> searchTerms = browsing.getSearchTerms();
	    Optional<String> ontologyId = browsing.getOntologyId();

	    knowledgeScheme = browsing.getScheme();
	    List<GSPredicate> collapsePredicates = knowledgeScheme.getCollapsePredicates();
	    List<GSPredicate> expandPredicates = knowledgeScheme.getExpandPredicates();

	    switch (action) {

	    case COLLAPSE:

		if (subjectId.isPresent()) {

		    query = Optional.of(//
			    createOperationQuery(//
				    subjectId, //
				    ontologyId, //
				    searchTerms, //
				    collapsePredicates, //
				    knowledgeScheme, //
				    Optional.empty(), //
				    false//
			    ));

		} else {
		    //
		    // if no subject id is present it is a ROOT collapsing and no action is taken
		    // since it must return an empty result set
		    //
		}

		break;

	    case EXPAND:
	    default:
		if (subjectId.isPresent()) {

		    query = Optional.of(//
			    createOperationQuery(//
				    subjectId, //
				    ontologyId, //
				    searchTerms, //
				    expandPredicates, //
				    knowledgeScheme, //
				    Optional.empty(), //
				    false//
			    )//
		    );

		} else {

		    query = Optional.of(//
			    createOperationQuery(//
				    Optional.empty(), //
				    ontologyId, //
				    searchTerms, //
				    null, //
				    knowledgeScheme, //
				    Optional.empty(), //
				    false//
			    )//
		    );
		}

		break;
	    }

	} else {

	    SemanticSearch search = message.getSearchOperation().get();
	    knowledgeScheme = search.getScheme();

	    query = Optional.of(//
		    createOperationQuery(//
			    Optional.empty(), //
			    search.getOntologyId(), //
			    search.getSearchTerms(), //
			    null, //
			    knowledgeScheme, //
			    Optional.ofNullable(message.getPage()), //
			    true)//
	    );
	}

	return query;
    }

    /**
     * @param subjectid
     * @param labelPredicate
     * @param abstractPredicate
     * @return
     */
    public String buildGetKnowlegdeResourceQuery(String subjectid, GSPredicate labelPredicate, GSPredicate abstractPredicate) {

	String query = "SELECT DISTINCT  ?type ?ontology ?label ?abstract ?ontologyName ?ontologyDescription\n";
	query += "WHERE \n";
	query += "{  \n";

	query += "<" + subjectid + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type. \n";

	query += "OPTIONAL{ <" + subjectid + "> <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> ?ontology . \n";
	query += "OPTIONAL{   ?ontology <" + labelPredicate + "> ?ontologyName } .\n";
	query += "OPTIONAL{   ?ontology <" + abstractPredicate + "> ?ontologyDescription }\n";
	query += "}.  \n";

	query += "OPTIONAL{ <" + subjectid + "> <" + labelPredicate + "> ?label }. \n";
	query += "OPTIONAL{ <" + subjectid + "> <" + abstractPredicate + "> ?abstract }. \n";
	query += "}\n";

	query += "OFFSET 0 \n";
	query += "LIMIT 10 ";

	return query;
    }

    /**
     * @param subjectId
     * @param ontologyId
     * @param searchTerms
     * @param operationPredicates
     * @param page
     * @param discovery
     * @return
     */
    private String createOperationQuery(//
	    Optional<String> subjectId, //
	    Optional<String> ontologyId, //
	    List<String> searchTerms, //
	    List<GSPredicate> operationPredicates, //
	    GSKnowledgeScheme scheme, //
	    Optional<Page> page, //
	    boolean discovery) {

	String sparqlQuery = "SELECT DISTINCT ?object ?type ?ontology ?label ?abstract ?ontologyName ?ontologyDescription ?p ?pontology "
		+ "?pontologyName ?pontologyDescription ?plabel \n";

	// -----------------------------
	//
	// NO SEARCH TEMRS
	//
	//
	if (searchTerms.isEmpty()) {

	    sparqlQuery += "WHERE \n";
	    sparqlQuery += "{  \n";

	    sparqlQuery = createBasicOperationQuery(ontologyId, sparqlQuery, operationPredicates, scheme, subjectId, discovery);

	} else {

	    // -----------------------------
	    //
	    // WITH SEARCH TEMRS
	    //
	    //

	    sparqlQuery += " { \n";
	    sparqlQuery += " { \n";

	    sparqlQuery = createBasicOperationQuery(ontologyId, sparqlQuery, operationPredicates, scheme, subjectId, discovery);

	    sparqlQuery += "     " + createFilter(searchTerms, scheme.getAbstractPredicate(), "abstract1") + " \n";
	    sparqlQuery += "   } \n";

	    sparqlQuery += "	   UNION \n";

	    sparqlQuery += "	   { \n";

	    sparqlQuery = createBasicOperationQuery(ontologyId, sparqlQuery, operationPredicates, scheme, subjectId, discovery);

	    sparqlQuery += "     " + createFilter(searchTerms, scheme.getLabelPredicate(), "label1") + " \n";
	    sparqlQuery += "   } \n";
	}

	sparqlQuery += " }\n";

	return sparqlQuery;
    }

    /**
     * @param searchTerms
     * @param predicate
     * @param target
     * @return
     */
    private String createFilter(List<String> searchTerms, GSPredicate predicate, String target) {

	String stringValue = predicate == null ? "null" : predicate.stringValue();

	String optional = " OPTIONAL{?object <" + stringValue + "> ?" + target + " }. \n";

	if (searchTerms.size() == 1) {

	    String filter = optional;

	    filter += "FILTER regex(lcase(str(?" + target + ")), lcase(\"" + searchTerms.get(0) + "\")).";

	    return filter;
	}

	String filter = optional + " FILTER (";

	for (String searchTerm : searchTerms) {

	    filter += "regex(lcase(str(?" + target + ")), lcase(\"" + searchTerm + "\")) || ";
	}

	filter = filter.substring(0, filter.length() - " || ".length());
	filter += ").";

	return filter;
    }

    /**
     * @param ontologyId
     * @param labelPredicate
     * @param discovery
     */
    private String createBasicOperationQuery(//
	    Optional<String> ontologyId, //
	    String sparqlQuery, //
	    List<GSPredicate> collapsePredicates, //
	    GSKnowledgeScheme scheme, //
	    Optional<String> subjectId, //
	    boolean discovery//
    ) {

	//
	// this is a basic constraint in order to retrieve only concepts of the supplied ontology id
	//
	if (ontologyId.isPresent()) {
	    sparqlQuery += " ?object <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> <" + ontologyId.get() + "> . \n ";
	    sparqlQuery += " ?object <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> ?ontology . \n";
	}

	if (!discovery) {

	    if (!subjectId.isPresent()) {

		sparqlQuery += " ?object <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + rootType + "> . \n";

	    } else {

		sparqlQuery += " VALUES ?p { \n";

		for (GSPredicate relation : collapsePredicates) {
		    sparqlQuery += " <" + relation.stringValue() + "> ";
		}

		sparqlQuery += " }";

		sparqlQuery += "  ?object ?p <" + subjectId.get() + "> . \n";
	    }
	}

	sparqlQuery += " ?object <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type. \n";

	sparqlQuery += " OPTIONAL{?object <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> ?ontology . " + "?ontology <"
		+ scheme.getLabelPredicate() + "> ?ontologyName . " + "?ontology <" + scheme.getAbstractPredicate()
		+ "> ?ontologyDescription }.  \n";

	if (subjectId.isPresent()) {

	    sparqlQuery += " OPTIONAL{?p <http://www.w3.org/2000/01/rdf-schema#isDefinedBy> ?pontology . ?pontology <"
		    + scheme.getLabelPredicate() + "> ?pontologyName . ?pontology <" + scheme.getAbstractPredicate() + "> "
		    + "?pontologyDescription }.  ";

	    sparqlQuery += "OPTIONAL{?p <" + scheme.getLabelPredicate() + "> ?plabel  } .";
	}

	sparqlQuery += " OPTIONAL{?object <" + scheme.getLabelPredicate() + "> ?label }. \n";
	sparqlQuery += " OPTIONAL{?object <" + scheme.getAbstractPredicate() + "> ?abstract }. \n";

	return sparqlQuery;
    }
}
