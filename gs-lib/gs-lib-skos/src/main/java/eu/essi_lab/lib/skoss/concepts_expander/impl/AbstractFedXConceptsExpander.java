/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_expander.impl;

import eu.essi_lab.lib.skoss.FedXEngine;

/**
 * @author Fabrizio
 */
public abstract class AbstractFedXConceptsExpander extends AbstractConceptsExpander {

    private FedXEngine engine;

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
