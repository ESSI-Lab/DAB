/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_finder.impl;

import eu.essi_lab.lib.skoss.ConceptsFinder;
import eu.essi_lab.lib.skoss.FindConceptsQueryBuilder;

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
