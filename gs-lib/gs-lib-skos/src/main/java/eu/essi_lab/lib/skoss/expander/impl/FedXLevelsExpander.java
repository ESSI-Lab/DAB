package eu.essi_lab.lib.skoss.expander.impl;

import java.time.Duration;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.eclipse.rdf4j.federated.monitoring.MonitoringUtil;
import org.eclipse.rdf4j.federated.repository.FedXRepositoryConnection;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.ThreadMode.MultiThreadMode;
import eu.essi_lab.lib.skoss.ThreadMode.SingleThreadMode;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit;
import eu.essi_lab.lib.skoss.fedx.FedXEngine;
import eu.essi_lab.lib.skoss.fedx.QueryBinding;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class FedXLevelsExpander extends FedXConceptsExpander {

    public FedXLevelsExpander() {

	setQueryBuilder(new MultipleExpandConceptsQueryBuilder());
    }

    @Override
    public SKOSResponse expand(//
	    List<String> concepts, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    ExpansionLevel targetLevel, //
	    ExpansionLimit limit) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Epanding concepts STARTED");

	GSLoggerFactory.getLogger(getClass()).trace("Thread mode: {} ", threadMode.getClass().getSimpleName());

	engine = FedXEngine.of(ontologyUrls, getEngineConfig().orElse(new FedXConfig()));
	repository = engine.getRepository();

	FedXRepositoryConnection conn = engine.getConnection();

	List<SKOSConcept> results = Collections.synchronizedList(new ArrayList<>());

	Set<String> stampSet = Collections.synchronizedSet(new HashSet<>());

	Set<String> visited = Collections.synchronizedSet(new HashSet<>());

	ExecutorService executor = switch (getThreadMode()) {
	case MultiThreadMode multi -> multi.getExecutor();
	case SingleThreadMode single -> Executors.newSingleThreadExecutor();
	default -> throw new IllegalArgumentException();// no way
	};

	String stamp = concepts + ":" + ExpansionLevel.NONE.getValue();

	stampSet.add(stamp);

	List<SimpleEntry<String, String>> fatherConcepts = new ArrayList<>();
	for (String concept : concepts) {
	    fatherConcepts.add(new SimpleEntry<String, String>(null, concept));
	}

	expandConcepts(//
		stampSet, //
		executor, //
		conn, //
		fatherConcepts, //
		searchLangs, //
		expansionRelations, //
		visited, //
		results, //
		targetLevel, //
		ExpansionLevel.NONE, //
		limit);

	while (!stampSet.isEmpty() && !executor.isShutdown()) {

	    Thread.sleep(Duration.ofMillis(1000));
	}

	engine.close();
	executor.shutdown();

	GSLoggerFactory.getLogger(getClass()).debug("Expanding concepts ENDED");

	return SKOSResponse.of(results);
    }

    /**
     * @param stampSet
     * @param executor
     * @param conn
     * @param fatherConcepts
     * @param searchLangs
     * @param expansionRelations
     * @param visited
     * @param results
     * @param targetLevel
     * @param currentLevel
     * @param limit
     */
    private void expandConcepts(//
	    Set<String> stampSet, //
	    ExecutorService executor, //
	    RepositoryConnection conn, //
	    List<SimpleEntry<String, String>> fatherConcepts, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    Set<String> visited, //
	    List<SKOSConcept> results, //
	    ExpansionLevel targetLevel, //
	    ExpansionLevel currentLevel, //
	    ExpansionLimit limit) {

	if (limitReached(limit, results) || fatherConcepts.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info("Limit reached, shutting down");
	    executor.shutdownNow();
	    engine.close();
	    stampSet.clear();
	    return;
	}

	List<SimpleEntry<String, String>> toVisit = new ArrayList<>();
	Set<String> urisToVisit = new HashSet<>();

	for (SimpleEntry<String, String> fatherConcept : fatherConcepts) {

	    String concept = fatherConcept.getValue();

	    if (visited.contains(concept) || //
		    currentLevel.getValue() > targetLevel.getValue() || //
		    limitReached(limit, results) || //
		    executor.isShutdown()) {

		return;
	    }

	    urisToVisit.add(concept);
	    toVisit.add(fatherConcept);
	}

	String stamp = urisToVisit + ":" + currentLevel.getValue();

	stampSet.add(stamp);

	executor.submit(() -> {

	    GSLoggerFactory.getLogger(getClass()).debug("Expanding concepts of level {} STARTED", currentLevel);

	    visited.addAll(urisToVisit);

	    String query = getQueryBuilder().build(//
		    urisToVisit, //
		    searchLangs, //
		    expansionRelations, //
		    targetLevel, //
		    currentLevel);

	    if (traceQuery()) {

		GSLoggerFactory.getLogger(getClass()).trace("\n{}", query);
	    }

	    TupleQuery tupleQuery = conn.prepareTupleQuery(query);

	    List<SKOSConcept> tmpResults = new ArrayList<SKOSConcept>();
	    SKOSResponse tmpResponse = SKOSResponse.of(tmpResults);

	    try (TupleQueryResult res = tupleQuery.evaluate()) {

		while (res.hasNext()) {

		    var queryBindingSet = res.next();

		    String concept = queryBindingSet.getValue(QueryBinding.CONCEPT.getLabel()) != null
			    ? queryBindingSet.getValue(QueryBinding.CONCEPT.getLabel()).stringValue()
			    : null;

		    String father = null;

		    if (concept != null) {
			for (SimpleEntry<String, String> fatherConcept : fatherConcepts) {
			    String tmpConcept = fatherConcept.getValue();
			    if (tmpConcept.equals(concept)) {
				father = fatherConcept.getKey();
			    }
			}
		    }

		    SKOSConcept item = SKOSConcept.of(//
			    concept, //
			    queryBindingSet.getValue(QueryBinding.PREF.getLabel()) != null
				    ? queryBindingSet.getValue(QueryBinding.PREF.getLabel()).stringValue()
				    : null, //
			    queryBindingSet.getValue(QueryBinding.EXPANDED.getLabel()) != null
				    ? queryBindingSet.getValue(QueryBinding.EXPANDED.getLabel()).stringValue()
				    : null, //
			    father, //
			    queryBindingSet.getValue(QueryBinding.ALT.getLabel()) != null
				    ? queryBindingSet.getValue(QueryBinding.ALT.getLabel()).stringValue()
				    : null);

		    tmpResults.add(item);
		}
	    } catch (QueryEvaluationException ex) {

		if (!executor.isShutdown()) {

		    GSLoggerFactory.getLogger(getClass()).error(ex);
		}
	    }

	    if (engine.getConfiguration().isEnableMonitoring()) {
		MonitoringUtil.printMonitoringInformation(repository.getFederationContext());
	    }

	    List<SKOSConcept> assembledResults = null;
	    synchronized (results) {

		assembledResults = SKOSResponse.getAggregatedResults(limit, tmpResponse, results);
		results.addAll(assembledResults);
	    }

	    if (limitReached(limit, results) || currentLevel.next().isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).info("Limit reached, shutting down");
		executor.shutdownNow();
		stampSet.clear();
		return;
	    }

	    List<SimpleEntry<String, String>> nextLevel = new ArrayList<>();

	    for (SKOSConcept assembledResult : assembledResults) {

		for (String expanded : assembledResult.getExpanded()) {

		    SimpleEntry<String, String> next = new SimpleEntry<>(assembledResult.getConcept(), expanded);
		    nextLevel.add(next);
		}
	    }

	    expandConcepts(//
		    stampSet, //
		    executor, //
		    conn, //
		    nextLevel, //
		    searchLangs, //
		    expansionRelations, //
		    visited, //
		    results, //
		    targetLevel, //
		    currentLevel.next().get(), //
		    limit);

	    stampSet.remove(stamp);

	    GSLoggerFactory.getLogger(getClass()).debug("Expanding concepts of level {} ENDED", currentLevel);
	});
    }

}
