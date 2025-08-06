package eu.essi_lab.request.executor.discover;

import java.util.AbstractMap.SimpleEntry;

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
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure;
import java.util.concurrent.StructuredTaskScope.Subtask;

import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.request.executor.AbstractAuthorizedExecutor;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IDiscoveryNodeExecutor;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;
import eu.essi_lab.request.executor.IDistributor;
import eu.essi_lab.request.executor.discover.submitter.DatabaseQueryExecutor;
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
public class DiscoveryExecutor extends AbstractAuthorizedExecutor
	implements IDiscoveryExecutor, IDiscoveryNodeExecutor, IDiscoveryStringExecutor {

    private QueryExecutorInitializer queryExecutorInitializer;

    /**
     * 
     */
    public DiscoveryExecutor() {

	queryExecutorInitializer = new QueryExecutorInitializer();
    }

    @Override
    public CountSet count(DiscoveryMessage message) throws GSException {

	new QueryInitializer().initializeQuery(message);

	IDistributor distributor = initDistributor(message);

	CountSet result = distributor.count(message);

	return result;
    }

    @Override
    public ResultSet<GSResource> retrieve(DiscoveryMessage message) throws GSException {

	return retrieve(message, GSResource.class);
    }

    @Override
    public ResultSet<Node> retrieveNodes(DiscoveryMessage message) throws GSException {

	return retrieve(message, Node.class);

    }

    @Override
    public ResultSet<String> retrieveStrings(DiscoveryMessage message) throws GSException {

	return retrieve(message, String.class);
    }

    @Override
    public boolean isAuthorized(DiscoveryMessage message) throws GSException {

	return isAuthorized(message, "DiscoveryExecutorAuthorizationError");
    }

    /**
     * @param <R>
     * @param message
     * @param clazz
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "incomplete-switch" })
    private <R> ResultSet<R> retrieve(DiscoveryMessage message, Class<R> clazz) throws GSException {

	new QueryInitializer().initializeQuery(message);

	ResultSet<R> result = null;

	//
	// if the request do not include distributed sources, the Distributor is bypassed and the request
	// is directly handled by the DatabaseQueryExecutor
	//
	if (!hasDistributedSources(message)) {

	    DatabaseQueryExecutor executor = new DatabaseQueryExecutor();

	    //
	    // - if the request do not require term frequency targets, and the count value in retrieval is not
	    // required (default), than the count operation can be completely bypassed
	    //
	    // - if the DB implementation is OpenSearch, the tf targets and the count in retrieval can be
	    // handled directly in the discovery query
	    //
	    // enters here if:
	    // - the DB impl. is OpenSearch
	    // - the DB impl. is MarkLogic and term frequency targets and count in retrieval are not required
	    //

	    if ((message.getTermFrequencyTargets().isEmpty() && !message.isCountInRetrievalIncluded()) ||

		    DatabaseFactory.get(message.getDataBaseURI()).getImplementation() == DatabaseImpl.OPENSEARCH) {

		if (clazz.equals(GSResource.class)) {

		    result = (ResultSet<R>) executor.retrieve(message, message.getPage());

		} else if (clazz.equals(String.class)) {

		    result = (ResultSet<R>) executor.retrieveStrings(message, message.getPage());

		} else {

		    result = (ResultSet<R>) executor.retrieveNodes(message, message.getPage());
		}

	    } else {

		//
		// if the DB implementation is MarkLogic and the request requires term frequency targets and or the
		// count value, the count and retrieve operations are performed concurrently to (hopefully) improve
		// performances. unless OpenSearch impl., at the moment MarkLogic DB impl. do not support discovery and
		// count in the same request
		//
		// enters here if:
		// - the DB impl. is MarkLogic and term frequency targets and/or the count in retrieval are required
		//

		try (@SuppressWarnings("preview")
		ShutdownOnFailure scope = new StructuredTaskScope.ShutdownOnFailure()) {

		    Subtask<CountSet> countTask = scope.fork(() -> {

			SimpleEntry<String, DiscoveryCountResponse> count = executor.count(message);

			CountSet countSet = new CountSet();
			countSet.addCountPair(count);

			int pageCount = (int) (countSet.getCount() < message.getPage().getSize() ? 1
				: Math.ceil(((double) countSet.getCount() / message.getPage().getSize())));

			countSet.setPageCount(countSet.getCount() == 0 || message.getPage().getSize() == 0 ? 0 : pageCount);

			int pageIndex = message.getPage().getSize() == 0 ? 0
				: message.getPage().getStart() <= message.getPage().getSize() ? 1
					: (message.getPage().getStart() / message.getPage().getSize()) + 1;

			countSet.setPageIndex(pageIndex);

			return countSet;
		    });

		    var retrieveTask = scope.fork(() -> {

			if (clazz.equals(GSResource.class)) {

			    return executor.retrieve(message, message.getPage());
			}

			if (clazz.equals(String.class)) {

			    return executor.retrieveStrings(message, message.getPage());
			}

			return executor.retrieveNodes(message, message.getPage());
		    });

		    try {
			scope.join();
			scope.throwIfFailed();

		    } catch (Exception ex) {

			throw GSException.createException(getClass(), "DiscoveryExecutorStructuredTaskScopeError", ex);
		    }

		    CountSet countSet = countTask.get();

		    result = (ResultSet<R>) retrieveTask.get();
		    result.setCountResponse(countSet);
		}
	    }

	} else {

	    //
	    // the Distributor is necessary if distributed sources are included 
	    //

	    IDistributor distributor = initDistributor(message);

	    if (clazz.equals(GSResource.class)) {

		result = (ResultSet<R>) distributor.retrieve(message);

	    } else if (clazz.equals(String.class)) {

		result = (ResultSet<R>) distributor.retrieveStrings(message);

	    } else {

		result = (ResultSet<R>) distributor.retrieveNodes(message);
	    }
	}

	return result;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private IDistributor initDistributor(DiscoveryMessage message) throws GSException {

	Distributor distributor = new Distributor();

	List<IQueryExecutor> querySubmitters = queryExecutorInitializer.initQueryExecutors(message);

	distributor.setQuerySubmitters(querySubmitters);
	return distributor;
    }

    /**
     * @param message
     * @return
     */
    private boolean hasDistributedSources(DiscoveryMessage message) {

	List<GSSource> distributedSources = ConfigurationWrapper.getDistributedSources();
	List<GSSource> sources = message.getSources();

	return sources.stream().anyMatch(distributedSources::contains);
    }
}
