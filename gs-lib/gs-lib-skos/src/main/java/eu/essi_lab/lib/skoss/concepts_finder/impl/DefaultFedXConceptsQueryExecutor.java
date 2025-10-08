/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_finder.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

import eu.essi_lab.lib.skoss.FindConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.fedx.FedXEngine;
import eu.essi_lab.lib.skoss.fedx.QueryBinding;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class DefaultFedXConceptsQueryExecutor extends AbstractFedXConceptsQueryExecutor {

    /**
     * @param searchTerm
     * @param ontologyUrls
     * @param sourceLangs
     * @return
     * @throws Exception
     */
    @Override
    public List<String> execute(//
	    FedXEngine engine, //
	    FindConceptsQueryBuilder queryBuilder, //
	    String searchTerm, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs) throws Exception {

	String query = queryBuilder.build(searchTerm, sourceLangs);

	if (traceQuery()) {

	    GSLoggerFactory.getLogger(getClass()).trace("\n{}", query);
	}

	TupleQuery tupleQuery = engine.getConnection().prepareTupleQuery(query);

	List<String> concepts = new ArrayList<>();

	try (TupleQueryResult res = tupleQuery.evaluate()) {

	    while (res.hasNext()) {

		String next = res.next().getValue(QueryBinding.CONCEPT.getLabel()).stringValue();

		if (!concepts.contains(next)) {

		    concepts.add(next);
		}
	    }

	} catch (QueryEvaluationException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	return concepts;
    }
}
