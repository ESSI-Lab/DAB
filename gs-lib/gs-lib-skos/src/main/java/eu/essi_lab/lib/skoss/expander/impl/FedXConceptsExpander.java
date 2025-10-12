/**
 * 
 */
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.eclipse.rdf4j.federated.monitoring.MonitoringUtil;
import org.eclipse.rdf4j.federated.repository.FedXRepository;
import org.eclipse.rdf4j.federated.repository.FedXRepositoryConnection;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.ThreadMode.MultiThreadMode;
import eu.essi_lab.lib.skoss.ThreadMode.SingleThreadMode;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit;
import eu.essi_lab.lib.skoss.fedx.FedXEngine;
import eu.essi_lab.lib.skoss.fedx.QueryBinding;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXConceptsExpander extends AbstractConceptsExpander {

    protected ThreadMode threadMode;
    protected FedXConfig engineConfig;
    protected FedXRepository repository;
    protected FedXEngine engine;

    /**
     * 
     */
    public FedXConceptsExpander() {

	setThreadMode(ThreadMode.SINGLE());
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

	concepts.forEach(con -> stampSet.add(con + ":" + ExpansionLevel.NONE.getValue()));

	List<SimpleEntry<String, String>> fatherConcepts = new ArrayList<>();

	for (String concept : concepts) {
	    fatherConcepts.add(new SimpleEntry<String, String>(null, concept));
	}

	for (SimpleEntry<String, String> fatherConcept : fatherConcepts) {

	    expandConcept(//
		    stampSet, //
		    executor, //
		    conn, //
		    fatherConcept, //
		    searchLangs, //
		    expansionRelations, //
		    visited, //
		    results, //
		    targetLevel, //
		    ExpansionLevel.NONE, //
		    limit);
	}

	while (!stampSet.isEmpty() && !executor.isShutdown()) {

	    Thread.sleep(Duration.ofMillis(1000));
	}

	engine.close();
	executor.shutdown();

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
     * @param limit
     * @throws Exception
     */
    protected void expandConcepts(//
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

	for (SimpleEntry<String, String> fatherConcept : fatherConcepts) {

	    expandConcept(//
		    stampSet, //
		    executor, //
		    conn, //
		    fatherConcept, //
		    searchLangs, //
		    expansionRelations, //
		    visited, //
		    results, //
		    targetLevel, //
		    currentLevel, //
		    limit);
	}
    }

    /**
     * @param limit
     * @param results
     * @return
     */
    protected boolean limitReached(ExpansionLimit limit, List<SKOSConcept> results) {

	synchronized (results) {
	    return switch (limit.getTarget()) {
	    case CONCEPTS -> results.size() >= limit.getLimit();
	    case LABELS -> SKOSResponse.of(results).getLabels().size() >= limit.getLimit();
	    case ALT_LABELS -> SKOSResponse.of(results).getAltLabels().size() >= limit.getLimit();
	    };
	}
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
     * @param limit
     * @throws Exception
     */
    private void expandConcept(//
	    Set<String> stampSet, //
	    ExecutorService executor, //
	    RepositoryConnection conn, //
	    SimpleEntry<String, String> fatherConcept, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    Set<String> visited, //
	    List<SKOSConcept> results, //
	    ExpansionLevel targetLevel, //
	    ExpansionLevel currentLevel, //
	    ExpansionLimit limit) {

	String father = fatherConcept.getKey();
	String concept = fatherConcept.getValue();

	if (limitReached(limit, results)) {
	    GSLoggerFactory.getLogger(getClass()).info("Limit reached, shutting down");
	    executor.shutdownNow();
	    stampSet.clear();
	    return;
	}

	if (visited.contains(concept) || //
		currentLevel.getValue() > targetLevel.getValue() || //
		limitReached(limit, results) || //
		executor.isShutdown()) {

	    return;
	}

	String stamp = concept + ":" + currentLevel.getValue();
	stampSet.add(stamp);

	executor.submit(() -> {

	    GSLoggerFactory.getLogger(getClass()).debug("Expanding concept {} STARTED");

	    GSLoggerFactory.getLogger(getClass()).trace("Current level: {}", currentLevel);

	    visited.add(concept);

	    ArrayList<String> concepts = new ArrayList<String>();

	    concepts.add(concept);

	    String query = getQueryBuilder().build(//
		    concepts, //
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

	    if (limitReached(limit, results)) {
		GSLoggerFactory.getLogger(getClass()).info("Limit reached, shutting down");
		executor.shutdownNow();
		stampSet.clear();
		return;
	    }

	    assembledResults.//
		    stream().//
		    flatMap(res -> res.getExpanded().stream()).//
		    forEach(expanded -> expandConcept(//
			    stampSet, //
			    executor, //
			    conn, //
			    new SimpleEntry<String, String>(concept, expanded), //
			    searchLangs, //
			    expansionRelations, //
			    visited, //
			    results, //
			    targetLevel, //
			    currentLevel.next().get(), //
			    limit));

	    stampSet.remove(stamp);

	    GSLoggerFactory.getLogger(getClass()).debug("Expanding concept {} ENDED");
	});
    }

}
