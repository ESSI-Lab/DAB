/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

import java.util.ArrayList;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.ThreadMode.MultiThreadMode;
import eu.essi_lab.lib.skoss.ThreadMode.SingleThreadMode;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.impl.MultipleExpandConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.expander.impl.QueryTask;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author boldrini
 */
public class DefaultConceptsFinder extends AbstractConceptsFinder {

    public DefaultConceptsFinder() {

    }

    @Override
    public List<String> find(String searchTerm, List<String> ontologyUrls, List<String> sourceLangs) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Finding concepts STARTED");

	ExecutorService executor = Executors.newFixedThreadPool(4);

	List<SKOSConcept> queryConcepts = new ArrayList<SKOSConcept>();

	DefaultConceptsQueryBuilder builder = new DefaultConceptsQueryBuilder();

	String query = builder.build(searchTerm, sourceLangs);

	List<Callable<List<SKOSConcept>>> tasks = new ArrayList<>();

	for (String ontologyURL : ontologyUrls) {
	    Callable<List<SKOSConcept>> task = new QueryTask(ontologyURL, query, null);

	    tasks.add(task);
	}

	ArrayList<SKOSConcept> results = new ArrayList<SKOSConcept>();

	try {
	    List<Future<List<SKOSConcept>>> futures = executor.invokeAll(tasks);

	    for (Future<List<SKOSConcept>> future : futures) {
		results.addAll(future.get());
	    }

	} catch (InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	} finally {
	    executor.shutdown();
	}

	results.addAll(results);

	 List<String> out = results.stream().map(c->c.getConcept()).toList();

	// GSLoggerFactory.getLogger(getClass()).debug("Concepts found: {}", out.size());

	// out.forEach(con -> GSLoggerFactory.getLogger(getClass()).debug("{}", con));

	return out;
    }

}
