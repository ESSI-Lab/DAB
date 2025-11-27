package eu.essi_lab.cfga.patch;

import eu.essi_lab.cfga.*;

/**
 * @author Fabrizio
 */
public abstract class Patch {

    private Configuration configuration;

    /**
     * @param configuration
     */
    public void setConfiguration(Configuration configuration) {

	this.configuration = configuration;
    }

    /**
     * @return
     */
    public Configuration getConfiguration() {

	return configuration;
    }

    /**
     * @throws Exception
     */
    abstract void patch() throws Exception;
}
