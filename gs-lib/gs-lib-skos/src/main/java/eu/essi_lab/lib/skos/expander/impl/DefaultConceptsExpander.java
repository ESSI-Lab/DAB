/**
 * 
 */
package eu.essi_lab.lib.skos.expander.impl;

import java.util.ArrayList;
import java.util.HashSet;

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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import eu.essi_lab.lib.skos.SKOSConcept;
import eu.essi_lab.lib.skos.SKOSResponse;
import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ExpandConceptsQueryBuilder;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.rdf4j.RDF4JQueryTask;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ThreadMode;

/**
 * @author boldrini
 */
public class DefaultConceptsExpander extends AbstractConceptsExpander<RDF4JQueryTask> {

    /**
     * 
     */
    public DefaultConceptsExpander() {

	setQueryBuilder(new MultipleExpandConceptsQueryBuilder());
	setThreadMode(ThreadMode.MULTI(() -> Executors.newFixedThreadPool(4)));
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

	List<SKOSConcept> results = new ArrayList<SKOSConcept>();

	List<SKOSConcept> currentLevelResults = new ArrayList<SKOSConcept>();

	currentLevelResults.add(SKOSConcept.of(new HashSet<String>(concepts)));

	for (int currentLevel = 0; currentLevel <= targetLevel.getValue(); currentLevel++) {

	    ExecutorService executor = getThreadMode().getExecutor();

	    List<Callable<List<SKOSConcept>>> tasks = new ArrayList<>();

	    List<SKOSConcept> queryConcepts = new ArrayList<SKOSConcept>();

	    for (SKOSConcept currentLevelResult : currentLevelResults) {

		Set<String> expandeds = currentLevelResult.getExpanded();

		for (String expanded : expandeds) {

		    SKOSConcept sc = SKOSConcept.of(expanded);

		    sc.getExpandedFrom().add(currentLevelResult.getConceptURI());
		    queryConcepts.add(sc);
		}
	    }

	    List<String> conceptsURIs = queryConcepts.stream().map(SKOSConcept::getConceptURI).collect(Collectors.toList());

	    ExpandConceptsQueryBuilder builder = getQueryBuilder();

	    String query = builder.build(conceptsURIs, searchLangs, relations, ExpansionLevel.HIGH, ExpansionLevel.NONE);

	    if (traceQuery()) {
		GSLoggerFactory.getLogger(getClass()).trace("\n" + query);
	    }

	    for (String ontologyURL : ontologyUrls) {

		RDF4JQueryTask task = new RDF4JQueryTask(//
			ontologyURL, //
			query, //
			queryConcepts);

		getTaskConsumer().ifPresent(consumer -> consumer.accept(task));

		tasks.add(task);
	    }

	    currentLevelResults = new ArrayList<SKOSConcept>();

	    try {
		List<Future<List<SKOSConcept>>> futures = executor.invokeAll(tasks);

		for (Future<List<SKOSConcept>> future : futures) {

		    currentLevelResults.addAll(future.get());
		}

	    } catch (InterruptedException | ExecutionException ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);

	    } finally {

		executor.shutdown();
	    }

	    results.addAll(currentLevelResults);

	    for (SKOSConcept skosConcept : currentLevelResults) {

		skosConcept.setLevel(ExpansionLevel.of(currentLevel).get());
	    }
	}

	if (isExcludeNoPrefConcepts()) {

	    results = results.stream().//
		    filter(c -> c.getPref().isPresent() && !c.getPref().get().equals(SKOSResponse.NONE_VALUE)).//
		    collect(Collectors.toList());
	}

	GSLoggerFactory.getLogger(getClass()).debug("Epanding concepts ENDED");

	return SKOSResponse.of(SKOSResponse.getAggregatedResults(limit, results));
    }

    /**
     * @return the
     */
    public DefaultExpandConceptsQueryBuilder getQueryBuilder() {

	return (DefaultExpandConceptsQueryBuilder) super.getQueryBuilder();
    }
}
