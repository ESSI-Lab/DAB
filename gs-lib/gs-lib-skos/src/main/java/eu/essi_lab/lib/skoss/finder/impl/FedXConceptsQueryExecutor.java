/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

import eu.essi_lab.lib.skoss.fedx.FedXEngine;
import eu.essi_lab.lib.skoss.fedx.QueryBinding;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.finder.FindConceptsQueryBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXConceptsQueryExecutor implements ConceptsQueryExecutor {

    private boolean traceQuery;
    private Function<List<String>, FedXEngine> engineBuilder;

    /**
     * 
     */
    public FedXConceptsQueryExecutor() {

	setEngineBuilder(ontologyUrls -> FedXEngine.of(ontologyUrls));
    }

    /**
     * @return
     */
    public boolean traceQuery() {

	return traceQuery;
    }

    /**
     * @param traceQuery
     */
    public void setTraceQuery(boolean traceQuery) {

	this.traceQuery = traceQuery;
    }

    /**
     * @param engine
     */
    public void setEngineBuilder(Function<List<String>, FedXEngine> engineBuilder) {

	this.engineBuilder = engineBuilder;
    }

    /**
     * @return the engineBuilder
     */
    public Function<List<String>, FedXEngine> getEngineBuilder() {

	return engineBuilder;
    }

    /**
     * @param searchTerm
     * @param ontologyUrls
     * @param sourceLangs
     * @return
     * @throws Exception
     */
    @Override
    public List<String> execute(//
	    FindConceptsQueryBuilder queryBuilder, //
	    String searchTerm, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs) throws Exception {

	String query = queryBuilder.build(searchTerm, sourceLangs);

	if (traceQuery()) {

	    GSLoggerFactory.getLogger(getClass()).trace("\n{}", query);
	}

	TupleQuery tupleQuery = getEngineBuilder().//
		apply(ontologyUrls).//
		getConnection().//
		prepareTupleQuery(query);

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
