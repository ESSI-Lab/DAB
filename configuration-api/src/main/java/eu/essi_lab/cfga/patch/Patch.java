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
    public void patch() throws Exception {

	doPatch();

	getConfiguration().getSource().backup();
	getConfiguration().flush();
    }

    /**
     *
     */
    public abstract void doPatch() throws Exception;

}
