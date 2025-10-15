/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.rdf4j.RDF4JQueryTask;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ThreadMode;

/**
 * @author boldrini
 */
public class DefaultConceptsFinder extends AbstractConceptsFinder<RDF4JQueryTask> {

    /**
     * 
     */
    public DefaultConceptsFinder() {

	setQueryBuilder(new DefaultConceptsQueryBuilder());
	setThreadMode(ThreadMode.MULTI(() -> Executors.newFixedThreadPool(4)));
    }

    @Override
    public List<String> find(String searchTerm, List<String> ontologyUrls, List<String> sourceLangs) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Finding concepts STARTED");

	ExecutorService executor = getThreadMode().getExecutor();

	String query = getQueryBuilder().build(searchTerm, sourceLangs);

	if (traceQuery()) {
	    GSLoggerFactory.getLogger(getClass()).trace("\n" + query);
	}

	List<Callable<List<SKOSConcept>>> tasks = new ArrayList<>();

	for (String ontologyURL : ontologyUrls) {

	    RDF4JQueryTask task = new RDF4JQueryTask(ontologyURL, query, null);

	    getTaskConsumer().ifPresent(consumer -> consumer.accept(task));

	    tasks.add(task);
	}

	ArrayList<SKOSConcept> results = new ArrayList<SKOSConcept>();

	try {
	    List<Future<List<SKOSConcept>>> futures = executor.invokeAll(tasks);

	    for (Future<List<SKOSConcept>> future : futures) {
		results.addAll(future.get());
	    }

	} catch (InterruptedException | ExecutionException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	} finally {

	    executor.shutdown();
	}

	List<String> out = results.stream().map(c -> c.getConcept()).toList();

	GSLoggerFactory.getLogger(getClass()).debug("Found {} concepts: \n{}", out.size(), out);

	GSLoggerFactory.getLogger(getClass()).debug("Finding concepts ENDED");

	return out;
    }
}
