/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

import eu.essi_lab.lib.skoss.finder.ConceptsQueryExecutor;

/**
 * @author Fabrizio
 */
public abstract class AbstractFedXConceptsFinder extends AbstractConceptsFinder {

    private ConceptsQueryExecutor executor;

    /**
     * 
     */
    public AbstractFedXConceptsFinder() {

	setExecutor(new FedXConceptsQueryExecutor());
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
