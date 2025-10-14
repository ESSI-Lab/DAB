/**
 * 
 */
package eu.essi_lab.lib.skoss.rdf4j;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.List;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.eclipse.rdf4j.federated.FedXFactory;
import org.eclipse.rdf4j.federated.repository.FedXRepository;
import org.eclipse.rdf4j.federated.repository.FedXRepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class FedXEngine {

    private List<String> ontologyUrls;
    private FedXRepositoryConnection connection;
    private FedXRepository repo;
    private FedXConfig config;

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

	GSLoggerFactory.getLogger(getClass()).info("FedXEngine init STARTED");

	FedXFactory fed = FedXFactory.newFederation();

	fed.withSparqlEndpoints(ontologyUrls);

	if (config != null) {

	    fed.withConfig(config);
	}

	this.config = config;
	this.repo = fed.create();
	this.connection = repo.getConnection();

	GSLoggerFactory.getLogger(getClass()).info("FedXEngine init ENDED");
    }

    /**
     * @return
     */
    public FedXConfig getConfiguration() {

	return config;
    }

    /**
     * @return
     */
    public FedXRepository getRepository() {

	return repo;
    }

    /**
     * 
     */
    public void close() throws RepositoryException {

	connection.close();
	repo.shutDown();
    }
}
