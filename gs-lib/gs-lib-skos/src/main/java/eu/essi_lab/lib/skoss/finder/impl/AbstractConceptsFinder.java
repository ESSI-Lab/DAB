/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

import eu.essi_lab.lib.skoss.finder.ConceptsFinder;
import eu.essi_lab.lib.skoss.finder.FindConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public abstract class AbstractConceptsFinder implements ConceptsFinder{

    private FindConceptsQueryBuilder builder;

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
}
