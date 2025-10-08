/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.impl;

/**
 * @author Fabrizio
 */
public abstract class AbstractFedXConceptsFinder extends AbstractConceptsFinder {

    /**
     * 
     */
    public AbstractFedXConceptsFinder() {

	setExecutor(new FedXConceptsQueryExecutor());
    }
}
