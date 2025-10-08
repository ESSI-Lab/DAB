/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.rdf4j.federated.repository.FedXRepositoryConnection;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSResponseItem;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.expander.impl.ThreadMode.MultiThreadMode;
import eu.essi_lab.lib.skoss.expander.impl.ThreadMode.SingleThreadMode;
import eu.essi_lab.lib.skoss.fedx.FedXEngine;
import eu.essi_lab.lib.skoss.fedx.QueryBinding;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXConceptsExpander extends AbstractConceptsExpander {

    private ThreadMode threadMode;
    private FedXEngine engine;

    /**
     * 
     */
    public FedXConceptsExpander() {

	setThreadMode(ThreadMode.MULTI());
    }

    /**
     * @return
     */
    public FedXEngine getEngine() {

	return engine;
    }

    /**
     * @param config
     */
    public void setEngine(FedXEngine engine) {

	this.engine = engine;
    }

    /**
     * @return
     */
    public ThreadMode getThreadMode() {

	return threadMode;
    }

    /**
     * @param threadMode
     */
    public void setThreadMode(ThreadMode threadMode) {

	this.threadMode = threadMode;
    }

    @Override
    public SKOSResponse expand(//
	    List<String> concepts, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    ExpansionLevel targetLevel, //
	    int limit) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Epanding concepts STARTED");

	FedXEngine engine = getEngine() == null ? FedXEngine.of(ontologyUrls) : getEngine();

	FedXRepositoryConnection conn = engine.getConnection();

	List<SKOSResponseItem> results = Collections.synchronizedList(new ArrayList<>());

	Set<String> stampSet = Collections.synchronizedSet(new HashSet<>());

	Set<String> visited = Collections.synchronizedSet(new HashSet<>());

	ExecutorService executor = switch (getThreadMode()) {
	case MultiThreadMode multi -> multi.getExecutor();
	case SingleThreadMode single -> Executors.newSingleThreadExecutor();
	default -> throw new IllegalArgumentException();// no way
	};

	concepts.forEach(con -> stampSet.add(con + ExpansionLevel.NONE.getValue()));

	for (String concept : concepts) {

	    expandConcept(//
		    stampSet, //
		    executor, //
		    conn, //
		    concept, //
		    searchLangs, //
		    expansionRelations, //
		    visited, //
		    results, //
		    targetLevel, //
		    ExpansionLevel.NONE);
	}

	while (!stampSet.isEmpty()) {

	    try {
		Thread.sleep(Duration.ofSeconds(1));

	    } catch (InterruptedException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	engine.close();

	executor.shutdown();

	if (results.size() > limit) {

	    results = results.subList(0, limit);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Expanding concepts ENDED");

	return SKOSResponse.of(results);
    }

    /**
     * @param executor
     * @param stampSet
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
	    Set<String> stampSet, //
	    ExecutorService executor, //
	    RepositoryConnection conn, //
	    String concept, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    Set<String> visited, //
	    List<SKOSResponseItem> results, //
	    ExpansionLevel targetLevel, //
	    ExpansionLevel currentLevel) {

	executor.submit(() -> {

	    GSLoggerFactory.getLogger(getClass()).debug("Expanding concept {} STARTED", concept);

	    GSLoggerFactory.getLogger(getClass()).trace("Current level: {}", currentLevel);

	    if (visited.contains(concept) || currentLevel.getValue() > targetLevel.getValue()) {

		return;
	    }

	    stampSet.add(concept + currentLevel.getValue());

	    visited.add(concept);

	    String query = getQueryBuilder().build(//
		    concept, //
		    searchLangs, //
		    expansionRelations, //
		    targetLevel, //
		    currentLevel);

	    if (traceQuery()) {

		GSLoggerFactory.getLogger(getClass()).trace("\n{}", query);
	    }

	    TupleQuery tupleQuery = conn.prepareTupleQuery(query);

	    try (TupleQueryResult res = tupleQuery.evaluate()) {

		while (res.hasNext()) {

		    var queryBindingSet = res.next();

		    SKOSResponseItem item = SKOSResponseItem.of(//
			    concept, //
			    queryBindingSet.getValue(QueryBinding.PREF.getLabel()) != null
				    ? queryBindingSet.getValue(QueryBinding.PREF.getLabel()).stringValue()
				    : null, //
			    queryBindingSet.getValue(QueryBinding.EXPANDED.getLabel()) != null
				    ? queryBindingSet.getValue(QueryBinding.EXPANDED.getLabel()).stringValue()
				    : null, //
			    queryBindingSet.getValue(QueryBinding.ALT.getLabel()) != null
				    ? queryBindingSet.getValue(QueryBinding.ALT.getLabel()).stringValue()
				    : null);

		    //
		    // it shouldn't be necessary but some ontologies seem to have duplicates
		    //
		    if (!results.contains(item)) {

			results.add(item);
		    }

		    //
		    // if the ExpandConceptsQueryBuilder don't put closeMatch in the SELECT clause
		    // (default), this case never occurs
		    //
		    if (queryBindingSet.getValue(QueryBinding.CLOSE_MATCH.getLabel()) != null) {

			expandConcept(//
				stampSet, //
				executor, //
				conn, //
				queryBindingSet.getValue(QueryBinding.CLOSE_MATCH.getLabel()).stringValue(), //
				searchLangs, //
				expansionRelations, //
				visited, //
				results, //
				targetLevel, //
				currentLevel.next().get());

		    } else if (queryBindingSet.getValue(QueryBinding.EXPANDED.getLabel()) != null) {

			expandConcept(//
				stampSet, //
				executor, //
				conn, //
				queryBindingSet.getValue(QueryBinding.EXPANDED.getLabel()).stringValue(), //
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

	    } finally {

		// always release the stamp
		stampSet.remove(concept + currentLevel.getValue());
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Expanding concept {} ENDED", concept);
	});
    }

}
