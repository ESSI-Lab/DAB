/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.impl;

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

import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author boldrini
 */
public class DefaultConceptsExpander implements ConceptsExpander {

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

	ArrayList<SKOSConcept> results = new ArrayList<SKOSConcept>();

	List<SKOSConcept> currentLevelResults = new ArrayList<SKOSConcept>();

	currentLevelResults.add(SKOSConcept.of(null, null, new HashSet<String>(concepts), Set.of(), Set.of()));

	for (int i = 0; i <= targetLevel.getValue(); i++) {

	    ExecutorService executor = Executors.newFixedThreadPool(4);

	    List<Callable<List<SKOSConcept>>> tasks = new ArrayList<>();

	    MultipleExpandConceptsQueryBuilder builder = new MultipleExpandConceptsQueryBuilder();

	    List<SKOSConcept>queryConcepts = new ArrayList<SKOSConcept>();
	    
	    for (SKOSConcept currentLevelResult : currentLevelResults) {
		Set<String> expandeds = currentLevelResult.getExpanded();
		for (String expanded : expandeds) {
		    SKOSConcept sc = SKOSConcept.of(expanded);
		    sc.getExpandedFrom().add(currentLevelResult.getConcept());
		    queryConcepts.add(sc );
		}
	    }
	    
	    List<String> queryConceptsURIs = queryConcepts.stream().map(SKOSConcept::getConcept).collect(Collectors.toList());

	    String query = builder.build(queryConceptsURIs, searchLangs, expansionRelations, ExpansionLevel.HIGH, ExpansionLevel.NONE);

	    for (String ontologyURL : ontologyUrls) {
		Callable<List<SKOSConcept>> task = new QueryTask(ontologyURL, query, queryConcepts);

		tasks.add(task);
	    }

	    currentLevelResults = new ArrayList<SKOSConcept>();

	    try {
		List<Future<List<SKOSConcept>>> futures = executor.invokeAll(tasks);

		for (Future<List<SKOSConcept>> future : futures) {
		    currentLevelResults.addAll(future.get());
		}

	    } catch (InterruptedException | ExecutionException e) {
		e.printStackTrace();
	    } finally {
		executor.shutdown();
	    }

	    results.addAll(currentLevelResults);

	}

	SKOSResponse ret = SKOSResponse.of(results);

	return SKOSResponse.of(ret.getAggregatedResults());

    }
}
