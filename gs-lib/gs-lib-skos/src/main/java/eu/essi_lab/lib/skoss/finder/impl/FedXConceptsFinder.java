/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.lib.skoss.fedx.FedXEngine;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.finder.FindConceptsQueryBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXConceptsFinder extends AbstractFedXConceptsFinder {

    private FedXEngine engine;

    /**
     * @param engine
     */
    public FedXConceptsFinder() {
    }

    @Override
    public List<String> find(String searchTerm, List<String> ontologyUrls, List<String> sourceLangs) throws Exception {

	GSLoggerFactory.getLogger(getClass()).info("Finding concepts STARTED");

	List<String> results = new ArrayList<>();

	FedXEngine engine = getEngine() == null ? FedXEngine.of(ontologyUrls) : getEngine();

	FindConceptsQueryBuilder queryBuilder = getQueryBuilder();

	ConceptsQueryExecutor executor = getExecutor();

	try {

	    results = executor.execute(queryBuilder, searchTerm, ontologyUrls, sourceLangs);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	} finally {

	    engine.close();
	}

	GSLoggerFactory.getLogger(getClass()).info("Found {} concepts: {}", results.size(), results);

	GSLoggerFactory.getLogger(getClass()).info("Finding concepts ENDED");

	return results;
    }

    /**
     * @return
     */
    public FedXEngine getEngine() {

	return engine;
    }

    /**
     * @param config
     */
    public void setEngine(FedXEngine engine) {

	this.engine = engine;
    }
}
