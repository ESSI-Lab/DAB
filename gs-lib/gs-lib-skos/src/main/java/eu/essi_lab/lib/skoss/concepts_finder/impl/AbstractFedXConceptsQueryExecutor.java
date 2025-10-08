/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_finder.impl;

import eu.essi_lab.lib.skoss.FedXConceptsQueryExecutor;

/**
 * @author Fabrizio
 */
public abstract class AbstractFedXConceptsQueryExecutor implements FedXConceptsQueryExecutor {

    private boolean traceQuery;

    /**
     * @return
     */
    public boolean traceQuery() {

	return traceQuery;
    }

    /**
     * @param traceQuery
     */
    public void setTraceQuery(boolean traceQuery) {

	this.traceQuery = traceQuery;
    }

}
