/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.List;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.eclipse.rdf4j.federated.FedXFactory;
import org.eclipse.rdf4j.federated.repository.FedXRepository;
import org.eclipse.rdf4j.federated.repository.FedXRepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 * @author Fabrizio
 */
public class FedXEngine {

    private List<String> ontologyUrls;
    private FedXRepositoryConnection connection;
    private FedXRepository repo;

    /**
     * @param ontologyUrls
     * @return
     */
    public static FedXEngine of(List<String> ontologyUrls) {

	return new FedXEngine(ontologyUrls, null);
    }

    /**
     * @param ontologyUrls
     * @param config
     * @return
     */
    public static FedXEngine of(List<String> ontologyUrls, FedXConfig config) {

	return new FedXEngine(ontologyUrls, config);
    }

    /**
     * @return the
     */
    public FedXRepositoryConnection getConnection() {

	return connection;
    }

    /**
     * @return the
     */
    public List<String> getOntologyUrls() {

	return ontologyUrls;
    }

    /**
     * @param ontologyUrls
     * @param config
     */
    private FedXEngine(List<String> ontologyUrls, FedXConfig config) {

	FedXFactory fed = FedXFactory.newFederation();
	fed.withSparqlEndpoints(ontologyUrls);

	if (config != null) {

	    fed.withConfig(config);
	}

	repo = fed.create();
	connection = repo.getConnection();
    }

    /**
     * 
     */
    public void close() throws RepositoryException {

	connection.close();
	repo.shutDown();
    }
}
