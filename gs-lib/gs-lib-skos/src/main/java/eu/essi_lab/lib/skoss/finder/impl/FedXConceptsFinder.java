/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.ThreadMode.MultiThreadMode;
import eu.essi_lab.lib.skoss.ThreadMode.SingleThreadMode;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXConceptsFinder extends AbstractConceptsFinder {

    /**
     * 
     */
    private ThreadMode threadMode;

    /**
     *  
     */
    public FedXConceptsFinder() {

	setExecutor(new FedXConceptsQueryExecutor());
	setThreadMode(ThreadMode.SINGLE());
    }

    @Override
    public List<String> find(String searchTerm, List<String> ontologyUrls, List<String> sourceLangs) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Finding concepts STARTED");

	List<String> out = switch (getThreadMode()) {

	case SingleThreadMode single -> find_(searchTerm, ontologyUrls, sourceLangs).collect(Collectors.toList());

	case MultiThreadMode multi -> {

	    ExecutorService executor = multi.getExecutor();

	    List<CompletableFuture<Stream<String>>> futures = ontologyUrls.//
		    stream().//
		    map(url -> CompletableFuture.supplyAsync(() -> find_(searchTerm, Arrays.asList(url), sourceLangs), executor)).//
		    toList();

	    yield futures.stream().flatMap(CompletableFuture::join).toList();
	}

	default -> null;// no way
	};

	GSLoggerFactory.getLogger(getClass()).debug("Finding concepts ENDED");

	GSLoggerFactory.getLogger(getClass()).debug("Concepts found: {}", out.size());

	out.forEach(con -> GSLoggerFactory.getLogger(getClass()).debug("{}", con));

	return out;
    }

    /**
     * @param searchTerm
     * @param ontologyUrls
     * @param sourceLangs
     * @return
     */
    private Stream<String> find_(String searchTerm, List<String> ontologyUrls, List<String> sourceLangs) {

	ConceptsQueryBuilder queryBuilder = getQueryBuilder();

	ConceptsQueryExecutor executor = getExecutor();

	try {

	    return executor.execute(queryBuilder, searchTerm, ontologyUrls, sourceLangs).stream();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return Stream.of();
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
}
