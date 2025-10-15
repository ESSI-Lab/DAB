/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

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
