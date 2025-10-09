/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

import eu.essi_lab.lib.skoss.finder.ConceptsFinder;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public abstract class AbstractConceptsFinder implements ConceptsFinder {

    private ConceptsQueryBuilder builder;
    private ConceptsQueryExecutor executor;

    /**
     * 
     */
    public AbstractConceptsFinder() {

	setQueryBuilder(new DefaultConceptsQueryBuilder());
    }

    /**
     * @return the builder
     */
    public ConceptsQueryBuilder getQueryBuilder() {

	return builder;
    }

    /**
     * @param builder
     */
    public void setQueryBuilder(ConceptsQueryBuilder builder) {

	this.builder = builder;
    }

    /**
     * @return the executor
     */
    public ConceptsQueryExecutor getExecutor() {

	return executor;
    }

    /**
     * @param executor
     */
    public void setExecutor(ConceptsQueryExecutor executor) {

	this.executor = executor;
    }
}
