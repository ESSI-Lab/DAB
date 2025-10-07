/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_expander.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.federated.repository.FedXRepositoryConnection;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import eu.essi_lab.lib.skoss.FedXEngine;
import eu.essi_lab.lib.skoss.SKOSSResponse;
import eu.essi_lab.lib.skoss.SKOSSResponseItem;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXConceptsExpander extends AbstractFedXConceptsExpander {

    /**
     * 
     */
    public FedXConceptsExpander() {
    }

    @Override
    public SKOSSResponse expand(//
	    List<String> concepts, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs, //
	    List<String> searchLangs, //
	    List<String> expansionRelations, //
	    ExpansionLevel targetLevel, //
	    int limit) throws Exception {

	GSLoggerFactory.getLogger(getClass()).info("Epanding concepts STARTED");

	FedXEngine engine = getEngine() == null ? FedXEngine.of(ontologyUrls) : getEngine();

	FedXRepositoryConnection conn = engine.getConnection();

	List<SKOSSResponseItem> results = new ArrayList<>();
	Set<String> visited = new HashSet<>();

	for (String concept : concepts) {

	    expandConcept(//
		    conn, //
		    concept, //
		    searchLangs, //
		    expansionRelations, //
		    visited, //
		    results, //
		    targetLevel, //
		    ExpansionLevel.NONE);
	}

	GSLoggerFactory.getLogger(getClass()).info("Expanding matching concepts ENDED");

	engine.close();

	if (results.size() > limit) {

	    results = results.subList(0, limit);
	}

	GSLoggerFactory.getLogger(getClass()).info("Expanding concepts ENDED");

	return SKOSSResponse.of(results);
    }

    /**
     * @param concept
     * @param conn
     * @param searchLangs
     * @param expansionRelations
     * @param targetLevel
     * @param visited
     * @param results
     * @param currentLevel
     * @throws Exception
     */
    private void expandConcept(//
	    RepositoryConnection conn, //
	    String concept, //
	    List<String> searchLangs, //
	    List<String> expansionRelations, //
	    Set<String> visited, //
	    List<SKOSSResponseItem> results, //
	    ExpansionLevel targetLevel, //
	    ExpansionLevel currentLevel) {

	GSLoggerFactory.getLogger(getClass()).info("Expanding concept {} STARTED", concept);

	GSLoggerFactory.getLogger(getClass()).info("Current level: {}", currentLevel);

	if (visited.contains(concept) || currentLevel.getValue() > targetLevel.getValue()) {

	    GSLoggerFactory.getLogger(getClass()).info("Ending recursive call");

	    return;
	}

	visited.add(concept);

	String queryStr = getQueryBuilder().build(//
		concept, //
		searchLangs, //
		expansionRelations, //
		targetLevel, //
		currentLevel);

	// GSLoggerFactory.getLogger(getClass()).info("Current query: \n{}", queryStr);

	TupleQuery tupleQuery = conn.prepareTupleQuery(queryStr);

	try (TupleQueryResult res = tupleQuery.evaluate()) {

	    while (res.hasNext()) {

		var bs = res.next();

		SKOSSResponseItem item = SKOSSResponseItem.of(//
			concept, //
			bs.getValue("pref") != null ? bs.getValue("pref").stringValue() : null, //
			bs.getValue("expanded") != null ? bs.getValue("expanded").stringValue() : null, //
			bs.getValue("alt") != null ? bs.getValue("alt").stringValue() : null);

		if (!results.contains(item)) {

		    results.add(item);
		}

		if (bs.getValue("closeMatch") != null) {

		    expandConcept(//
			    conn, //
			    bs.getValue("closeMatch").stringValue(), //
			    searchLangs, //
			    expansionRelations, //
			    visited, //
			    results, //
			    targetLevel, //
			    currentLevel.next().get());

		} else if (bs.getValue("expanded") != null) {

		    expandConcept(//
			    conn, //
			    bs.getValue("expanded").stringValue(), //
			    searchLangs, //
			    expansionRelations, //
			    visited, //
			    results, //
			    targetLevel, //
			    currentLevel.next().get());
		}
	    }
	} catch (QueryEvaluationException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	GSLoggerFactory.getLogger(getClass()).info("Expanding concept {} ENDED", concept);
    }

}
