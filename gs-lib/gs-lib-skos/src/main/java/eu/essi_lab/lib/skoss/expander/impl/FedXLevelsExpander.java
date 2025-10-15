package eu.essi_lab.lib.skoss.expander.impl;

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
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

import eu.essi_lab.lib.skoss.QueryTask;
import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit;
import eu.essi_lab.lib.skoss.rdf4j.FedXEngine;
import eu.essi_lab.lib.skoss.rdf4j.QueryBinding;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ThreadMode.MultiThreadMode;
import eu.essi_lab.lib.utils.ThreadMode.SingleThreadMode;

public class FedXLevelsExpander<T> extends FedXConceptsExpander<QueryTask> {

    private List<String> ontologyUrls;

    public FedXLevelsExpander() {

	setQueryBuilder(new MultipleExpandConceptsQueryBuilder());
    }

    @Override
    public SKOSResponse expand(//
	    List<String> concepts, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> relations, //
	    ExpansionLevel targetLevel, //
	    ExpansionLimit limit) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Epanding concepts STARTED");

	GSLoggerFactory.getLogger(getClass()).trace("Thread mode: {} ", threadMode.getClass().getSimpleName());

	// engine = FedXEngine.of(ontologyUrls, getEngineConfig().orElse(new FedXConfig()));
	// repository = engine.getRepository();

	// FedXRepositoryConnection conn = engine.getConnection();

	this.ontologyUrls = ontologyUrls;

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
		fatherConcepts, //
		searchLangs, //
		relations, //
		visited, //
		results, //
		targetLevel, //
		ExpansionLevel.NONE, //
		limit);

	while (!stampSet.isEmpty() && !executor.isShutdown()) {

	    Thread.sleep(Duration.ofMillis(1000));
	}

	// engine.close();
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
	    List<SimpleEntry<String, String>> fatherConcepts, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    Set<String> visited, //
	    List<SKOSConcept> results, //
	    ExpansionLevel targetLevel, //
	    ExpansionLevel currentLevel, //
	    ExpansionLimit limit) {

	if (limitReached(limit, results)) {

	    GSLoggerFactory.getLogger(getClass()).info("Limit reached, shutting down");

	    executor.shutdownNow();
	    stampSet.clear();
	    return;
	}

	if (fatherConcepts.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).info("Father concepts empty, shutting down");

	    executor.shutdownNow();
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

	    FedXEngine engine = FedXEngine.of(ontologyUrls, getEngineConfig().orElse(new FedXConfig()));

	    TupleQuery tupleQuery = engine.getConnection().prepareTupleQuery(query);

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
		MonitoringUtil.printMonitoringInformation(engine.getRepository().getFederationContext());
	    }

	    List<SKOSConcept> assembledResults = null;
	    synchronized (results) {

		assembledResults = SKOSResponse.getAggregatedResults(limit, tmpResponse, results);
		results.addAll(assembledResults);
	    }

	    if (limitReached(limit, results)) {
		GSLoggerFactory.getLogger(getClass()).info("Limit reached, shutting down");
		executor.shutdownNow();
		stampSet.clear();
		return;
	    }

	    if (currentLevel.next().isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).info("No next level, shutting down");
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
