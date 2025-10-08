/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_expander.impl;

import eu.essi_lab.lib.skoss.ConceptsExpander;
import eu.essi_lab.lib.skoss.ExpandConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public abstract class AbstractConceptsExpander implements ConceptsExpander {

    private ExpandConceptsQueryBuilder builder;
    private boolean traceQuery;

    /**
     * 
     */
    public AbstractConceptsExpander() {

	setQueryBuilder(new DefaultExpandConceptsQueryBuilder());
    }

    /**
     * @return the
     */
    public ExpandConceptsQueryBuilder getQueryBuilder() {

	return builder;
    }

    /**
     * @param builder
     */
    public void setQueryBuilder(ExpandConceptsQueryBuilder builder) {

	this.builder = builder;
    }

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
