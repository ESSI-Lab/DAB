/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

import eu.essi_lab.lib.skoss.fedx.FedXEngine;
import eu.essi_lab.lib.skoss.fedx.QueryBinding;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXConceptsQueryExecutor implements ConceptsQueryExecutor {

    private boolean traceQuery;
    private FedXConfig engineConfig;

    /**
     * 
     */
    public FedXConceptsQueryExecutor() {
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
     * @return
     */
    public Optional<FedXConfig> getEngineConfig() {

	return Optional.ofNullable(engineConfig);
    }

    /**
     * @param config
     */
    public void setEngineConfig(FedXConfig engine) {

	this.engineConfig = engine;
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
	    ConceptsQueryBuilder queryBuilder, //
	    String searchTerm, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs) throws Exception {

	String query = queryBuilder.build(searchTerm, sourceLangs);

	if (traceQuery()) {

	    GSLoggerFactory.getLogger(getClass()).trace("\n{}", query);
	}

	TupleQuery tupleQuery = FedXEngine.of(ontologyUrls, getEngineConfig().orElse(new FedXConfig())).//
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
