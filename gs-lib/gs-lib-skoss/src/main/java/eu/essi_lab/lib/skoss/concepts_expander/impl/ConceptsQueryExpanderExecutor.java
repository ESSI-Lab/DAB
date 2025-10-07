/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_expander.impl;

import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import eu.essi_lab.lib.skoss.ExpandConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.SKOSSResponseItem;
import eu.essi_lab.lib.skoss.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public abstract class ConceptsQueryExpanderExecutor {

    /**
     * @param conn
     * @param concept
     * @param builder
     * @param searchLangs
     * @param expansionRelations
     * @param visited
     * @param results
     * @param targetLevel
     * @param currentLevel
     */
    public void expand(//
	    RepositoryConnection conn, //
	    String concept, //
	    ExpandConceptsQueryBuilder builder, //
	    List<String> searchLangs, //
	    List<String> expansionRelations, //
	    Set<String> visited, //
	    List<SKOSSResponseItem> results, //
	    ExpansionLevel targetLevel, //
	    ExpansionLevel currentLevel) {

	String queryStr = builder.build(//
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

		    expand(//
			    conn, //
			    bs.getValue("closeMatch").stringValue(), //
			    builder, //
			    searchLangs, //
			    expansionRelations, //
			    visited, //
			    results, //
			    targetLevel, //
			    currentLevel.next().get());

		} else if (bs.getValue("expanded") != null) {

		    expand(//
			    conn, //
			    bs.getValue("expanded").stringValue(), //
			    builder, //
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
    }
}
