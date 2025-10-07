/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_finder.impl;

import eu.essi_lab.lib.skoss.FedXConceptsQueryExecutor;

/**
 * @author Fabrizio
 */
public abstract class AbstractFedXConceptsFinder extends AbstractConceptsFinder {

    private FedXConceptsQueryExecutor executor;

    /**
     * 
     */
    public AbstractFedXConceptsFinder() {

	setExecutor(new DefaultFedXConceptsQueryExecutor());
    }

    /**
     * @return the executor
     */
    public FedXConceptsQueryExecutor getExecutor() {

	return executor;
    }

    /**
     * @param executor
     */
    public void setExecutor(FedXConceptsQueryExecutor executor) {

	this.executor = executor;
    }
}
