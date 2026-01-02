package eu.essi_lab.request.executor.discover;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.request.executor.AbstractAuthorizedExecutor;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IDiscoveryNodeExecutor;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;
import eu.essi_lab.request.executor.IDistributor;
import eu.essi_lab.request.executor.query.IQueryExecutor;
import eu.essi_lab.request.executor.query.IQueryExecutor.Type;

/**
 * @author Fabrizio
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <R> ResultSet<R> retrieve(DiscoveryMessage message, Class<R> clazz) throws GSException {

	new QueryInitializer().initializeQuery(message);

	ResultSet<R> result = null;

	//
	// if the request do not include distributed sources, the Distributor is bypassed and the request
	// is directly handled by the DatabaseFinder
	//
	if (!ConfigurationWrapper.hasDistributedSources(message)) {

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

	    DatabaseFinder finder = DatabaseProviderFactory.getFinder(message.getDataBaseURI());

	    if ((message.getTermFrequencyTargets().isEmpty() && !message.isCountInRetrievalIncluded()) ||

		    DatabaseFactory.get(message.getDataBaseURI()).getImplementation() == DatabaseImpl.OPENSEARCH) {

		if (clazz.equals(GSResource.class)) {

		    result = (ResultSet<R>) finder.discover(message);

		} else if (clazz.equals(String.class)) {

		    result = (ResultSet<R>) finder.discoverStrings(message);

		} else {

		    result = (ResultSet<R>) finder.discoverNodes(message);
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

		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

		Callable<FutureWrapper> countTask = () -> {

		    DiscoveryCountResponse count = finder.count(message);

		    CountSet countSet = new CountSet();
		    countSet.addCountPair(new SimpleEntry<String, DiscoveryCountResponse>(Type.DATABASE.toString(), count));

		    int pageCount = (int) (countSet.getCount() < message.getPage().getSize() ? 1
			    : Math.ceil(((double) countSet.getCount() / message.getPage().getSize())));

		    countSet.setPageCount(countSet.getCount() == 0 || message.getPage().getSize() == 0 ? 0 : pageCount);

		    int pageIndex = message.getPage().getSize() == 0 ? 0
			    : message.getPage().getStart() <= message.getPage().getSize() ? 1
				    : (message.getPage().getStart() / message.getPage().getSize()) + 1;

		    countSet.setPageIndex(pageIndex);

		    return new FutureWrapper(countSet);
		};

		Callable<FutureWrapper> retrieveTask = () -> {

		    if (clazz.equals(GSResource.class)) {

			return new FutureWrapper(finder.discover(message));
		    }

		    if (clazz.equals(String.class)) {

			return new FutureWrapper(finder.discoverStrings(message));
		    }

		    return new FutureWrapper(finder.discoverNodes(message));
		};

		try {

		    List<Future<FutureWrapper>> futures = executor.invokeAll(Arrays.asList(countTask, retrieveTask));

		    CountSet countSet = futures.get(0).get().getCountSet();
		    result = futures.get(1).get().getResultSet();

		    result.setCountResponse(countSet);

		} catch (Exception e) {

		    throw GSException.createException(getClass(), "DiscoveryExecutorMarkLogicRetrievalError", e);
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
     * @author Fabrizio
     */
    private static class FutureWrapper<R> {

	private CountSet countSet;
	private ResultSet<R> resultSet;

	/**
	 * @param countSet
	 */
	public FutureWrapper(CountSet countSet) {

	    this.countSet = countSet;
	}

	/**
	 * @param resultSet
	 */
	public FutureWrapper(ResultSet<R> resultSet) {

	    this.resultSet = resultSet;
	}

	/**
	 * @return
	 */
	public CountSet getCountSet() {

	    return countSet;
	}

	/**
	 * @return
	 */
	public ResultSet<R> getResultSet() {

	    return resultSet;
	}
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
}
