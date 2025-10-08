/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

import eu.essi_lab.lib.skoss.finder.ConceptsFinder;
import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.finder.FindConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public abstract class AbstractConceptsFinder implements ConceptsFinder {

    private FindConceptsQueryBuilder builder;
    private ConceptsQueryExecutor executor;

    /**
     * 
     */
    public AbstractConceptsFinder() {

	setQueryBuilder(new DefaultFindConceptsQueryBuilder());
    }

    /**
     * @return the builder
     */
    public FindConceptsQueryBuilder getQueryBuilder() {

	return builder;
    }

    /**
     * @param builder
     */
    public void setQueryBuilder(FindConceptsQueryBuilder builder) {

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
