/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_expander.impl;

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

import eu.essi_lab.lib.skoss.FedXEngine;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSResponseItem;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.concepts_expander.impl.ThreadMode.MultiThreadMode;
import eu.essi_lab.lib.skoss.concepts_expander.impl.ThreadMode.SingleThreadMode;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class FedXConceptsExpander extends AbstractFedXConceptsExpander {

    /**
     * @author Fabrizio
     */
    private enum Binding implements LabeledEnum {

	/**
	 * 
	 */
	PREF("pref"),
	/**
	 * 
	 */
	ALT("alt"),
	/**
	 * 
	 */
	CLOSE_MATCH("closeMatch"),
	/**
	 * 
	 */
	EXPANDED("expanded");

	private String label;

	/**
	 * @param label
	 */
	private Binding(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return label;
	}

    }

    private ThreadMode threadMode;

    /**
     * 
     */
    public FedXConceptsExpander() {

	setThreadMode(ThreadMode.MULTI());
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

	GSLoggerFactory.getLogger(getClass()).info("Epanding concepts STARTED");

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

	GSLoggerFactory.getLogger(getClass()).info("Expanding matching concepts ENDED");

	engine.close();

	executor.shutdown();

	if (results.size() > limit) {

	    results = results.subList(0, limit);
	}

	GSLoggerFactory.getLogger(getClass()).info("Expanding concepts ENDED");

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

	    GSLoggerFactory.getLogger(getClass()).info("Expanding concept {} STARTED", concept);

	    GSLoggerFactory.getLogger(getClass()).info("Current level: {}", currentLevel);

	    if (visited.contains(concept) || currentLevel.getValue() > targetLevel.getValue()) {

		GSLoggerFactory.getLogger(getClass()).info("Ending recursive call");

		return;
	    }

	    stampSet.add(concept + currentLevel.getValue());

	    visited.add(concept);

	    String queryStr = getQueryBuilder().build(//
		    concept, //
		    searchLangs, //
		    expansionRelations, //
		    targetLevel, //
		    currentLevel);

	    if (traceQuery()) {

		GSLoggerFactory.getLogger(getClass()).trace("Current query: \n{}", queryStr);
	    }

	    TupleQuery tupleQuery = conn.prepareTupleQuery(queryStr);

	    try (TupleQueryResult res = tupleQuery.evaluate()) {

		while (res.hasNext()) {

		    var queryBindingSet = res.next();

		    SKOSResponseItem item = SKOSResponseItem.of(//
			    concept, //
			    queryBindingSet.getValue(Binding.PREF.getLabel()) != null
				    ? queryBindingSet.getValue(Binding.PREF.getLabel()).stringValue()
				    : null, //
			    queryBindingSet.getValue(Binding.EXPANDED.getLabel()) != null
				    ? queryBindingSet.getValue(Binding.EXPANDED.getLabel()).stringValue()
				    : null, //
			    queryBindingSet.getValue(Binding.ALT.getLabel()) != null
				    ? queryBindingSet.getValue(Binding.ALT.getLabel()).stringValue()
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
		    if (queryBindingSet.getValue(Binding.CLOSE_MATCH.getLabel()) != null) {

			expandConcept(//
				stampSet, //
				executor, //
				conn, //
				queryBindingSet.getValue(Binding.CLOSE_MATCH.getLabel()).stringValue(), //
				searchLangs, //
				expansionRelations, //
				visited, //
				results, //
				targetLevel, //
				currentLevel.next().get());

		    } else if (queryBindingSet.getValue(Binding.EXPANDED.getLabel()) != null) {

			expandConcept(//
				stampSet, //
				executor, //
				conn, //
				queryBindingSet.getValue(Binding.EXPANDED.getLabel()).stringValue(), //
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

	    GSLoggerFactory.getLogger(getClass()).info("Expanding concept {} ENDED", concept);
	});
    }

}
