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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.rdf4j.federated.FedXConfig;
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
import eu.essi_lab.lib.skoss.fedx.FedXEngine;
import eu.essi_lab.lib.skoss.fedx.QueryBinding;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXConceptsExpander extends AbstractConceptsExpander {

    private ThreadMode threadMode;
    private FedXConfig engineConfig;

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
	    int limit) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Epanding concepts STARTED");

	GSLoggerFactory.getLogger(getClass()).trace("Thread mode: {} ", threadMode.getClass().getSimpleName());

	FedXEngine engine = FedXEngine.of(ontologyUrls, getEngineConfig().orElse(new FedXConfig()));

	FedXRepositoryConnection conn = engine.getConnection();

	List<SKOSConcept> results = Collections.synchronizedList(new ArrayList<>());

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
		    null,
		    concept, //
		    searchLangs, //
		    expansionRelations, //
		    visited, //
		    results, //
		    targetLevel, //
		    ExpansionLevel.NONE, //
		    limit);
	}

	while (!stampSet.isEmpty()) {

	    Thread.sleep(Duration.ofMillis(100));
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
    private void expandConcept(//
	    Set<String> stampSet, //
	    ExecutorService executor, //
	    RepositoryConnection conn, //
	    String father,
	    String concept, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    Set<String> visited, //
	    List<SKOSConcept> results, //
	    ExpansionLevel targetLevel, //
	    ExpansionLevel currentLevel, //
	    int limit) {

	if (results.size() >= limit) {

	    executor.shutdownNow();
	    return;
	}

	if (visited.contains(concept) || //
		currentLevel.getValue() > targetLevel.getValue() || //
		results.size() >= limit || //
		executor.isShutdown()) {

	    return;
	}

	executor.submit(() -> {

	    GSLoggerFactory.getLogger(getClass()).debug("Expanding concept {} STARTED", concept);

	    GSLoggerFactory.getLogger(getClass()).trace("Current level: {}", currentLevel);

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

		    if (results.size() >= limit) {

			executor.shutdownNow();
			return;
		    }

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
		   if (queryBindingSet.getValue(QueryBinding.EXPANDED.getLabel()) != null) {

			expandConcept(//
				stampSet, //
				executor, //
				conn, //
				concept, //
				queryBindingSet.getValue(QueryBinding.EXPANDED.getLabel()).stringValue(), //
				searchLangs, //
				expansionRelations, //
				visited, //
				results, //
				targetLevel, //
				currentLevel.next().get(), //
				limit);
		    }
		}
	    } catch (QueryEvaluationException ex) {

		if (!executor.isShutdown()) {

		    GSLoggerFactory.getLogger(getClass()).error(ex);
		}

	    } finally {

		// always release the stamp
		stampSet.remove(concept + currentLevel.getValue());
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Expanding concept {} ENDED", concept);
	});
    }

}
