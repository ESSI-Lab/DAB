package eu.essi_lab.request.executor.discover;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.w3c.dom.Node;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.request.executor.AbstractAuthorizedExecutor;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IDiscoveryNodeExecutor;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;
import eu.essi_lab.request.executor.IDistributor;
import eu.essi_lab.request.executor.query.IQueryExecutor;

/**
 * The default implementation of IDiscoverExecutor uses two subcomponents to enable discovery of the resources matching
 * the user discovery
 * queries (both count and retrieval): the query initializer and the Distributor. The steps to be done are as in the
 * following:
 * <ol>
 * <li>Query Initializer is called to initialize the query (e.g. to get a query in normal form)</li>
 * <li>The Distributor is initialized with an ordered list of query submitters. These are initialized by the discovery
 * executor from the ordered list of sources, found as a field of the discovery message. In general, there will be (at
 * most) one database
 * query submitter and (zero or more) distributed query submitters.</li>
 * <li>Distributor: to execute the normalized query</li>
 * </ol>
 *
 * @author boldrini
 */
public class DiscoveryExecutor extends AbstractAuthorizedExecutor implements IDiscoveryExecutor, IDiscoveryNodeExecutor, IDiscoveryStringExecutor {

    private static final String DISCOVERY_EXECUTOR_PDP_ENGINE_ERROR = "DISCOVERY_EXECUTOR_PDP_ENGINE_ERROR";
    private QueryExecutorInitializer queryExecutorInitializer;

    public DiscoveryExecutor() {

	queryExecutorInitializer = new QueryExecutorInitializer();

    }

    @Override
    public CountSet count(DiscoveryMessage message) throws GSException {

	/**
	 * Initializes the query, creating 1) the permitted bond 2) the normalized bond 3) the ordered source list
	 */
	new QueryInitializer().initializeQuery(message);

	/**
	 * Initializes the distributor, creating the query submitters from the SOURCES discovery message parameter
	 */
	IDistributor distributor = initDistributor(message);

	CountSet result = distributor.count(message);

	return result;
    }

    @Override
    public ResultSet<GSResource> retrieve(DiscoveryMessage message) throws GSException {

	QueryInitializer queryInitializer = new QueryInitializer();

	/**
	 * Initializes the query, creating 1) the permitted bond 2) the normalized bond
	 */
	queryInitializer.initializeQuery(message);

	/**
	 * Initializes the distributor, creating the query submitters from the SOURCES discovery message parameter
	 */
	IDistributor distributor = initDistributor(message);

	ResultSet<GSResource> result = distributor.retrieve(message);

	return result;
    }

    @Override
    public ResultSet<Node> retrieveNodes(DiscoveryMessage message) throws GSException {

	QueryInitializer queryInitializer = new QueryInitializer();

	/**
	 * Initializes the query, creating 1) the permitted bond 2) the normalized bond
	 */
	queryInitializer.initializeQuery(message);

	/**
	 * Initializes the distributor, creating the query submitters from the SOURCES discovery message parameter
	 */
	IDistributor distributor = initDistributor(message);

	ResultSet<Node> result = distributor.retrieveNodes(message);

	return result;
    }
    
    @Override
    public ResultSet<String> retrieveStrings(DiscoveryMessage message) throws GSException {

	QueryInitializer queryInitializer = new QueryInitializer();

	/**
	 * Initializes the query, creating 1) the permitted bond 2) the normalized bond
	 */
	queryInitializer.initializeQuery(message);

	/**
	 * Initializes the distributor, creating the query submitters from the SOURCES discovery message parameter
	 */
	IDistributor distributor = initDistributor(message);

	ResultSet<String> result = distributor.retrieveStrings(message);

	return result;
    }

    @Override
    public boolean isAuthorized(DiscoveryMessage message) throws GSException {

	return isAuthorized(message, DISCOVERY_EXECUTOR_PDP_ENGINE_ERROR);
    }

    private IDistributor initDistributor(DiscoveryMessage message) throws GSException {
	Distributor distributor = new Distributor();

	List<IQueryExecutor> querySubmitters = queryExecutorInitializer.initQueryExecutors(message);

	distributor.setQuerySubmitters(querySubmitters);
	return distributor;
    }

    public QueryExecutorInitializer getQueryExecutorInitializer() {
	return queryExecutorInitializer;
    }

    public void setQueryExecutorInitializer(QueryExecutorInitializer queryExecutorInitializer) {
	this.queryExecutorInitializer = queryExecutorInitializer;
    }
}
