package eu.essi_lab.testit.connector.configuration;

import eu.essi_lab.cdk.IDriverConnector;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public interface Connectors {

    /**
     * @param serviceId
     * @param source
     * @return
     */
    public IDriverConnector getConnector(int serviceId, GSSource source);
}
