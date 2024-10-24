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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.w3c.dom.Node;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.request.executor.IDistributor;
import eu.essi_lab.request.executor.query.IDatabaseQueryExecutor;
import eu.essi_lab.request.executor.query.IDistributedQueryExecutor;
import eu.essi_lab.request.executor.query.IQueryExecutor;
import eu.essi_lab.request.executor.query.IQueryExecutor.Type;

/**
 * The default implementation of {@link IDistributor} is initialized with an ordered list of query submitters. It
 * distributes the discovery
 * message to the query submitters and collect back the results.
 * <p>
 * It's in charge of creating a reduced discovery message for each distributed query submitter, where only the subset of
 * the original bond
 * that is pertinent for a given source is forwarded. The result page requested by the user will be here translated into
 * a query submitter
 * relative result page. Finally, it collects back the results from the query submitter.
 *
 * @author boldrini
 */
public class Distributor implements IDistributor {

    /**
     * 
     */
    private static final int DEFAULT_EXECUTION_TIMEOUT = 120; // in seconds
    /**
     * 
     */
    private static final String DISTRIBUTOR_COUNT_JOIN_ERROR = "DISTRIBUTOR_COUNT_JOIN_ERROR";
    /**
     * 
     */
    private static final String DISTRIBUTOR_RETRIEVE_JOIN_ERROR = "DISTRIBUTOR_RETRIEVE_JOIN_ERROR";
    /**
     * 
     */
    private static final String DISTRIBUTOR_RETRIEVE_SOURCE_TIMEOUT_ERROR = "DISTRIBUTOR_RETRIEVE_SOURCE_TIMEOUT_ERROR";
    /**
     * 
     */
    private static final String DISTRIBUTOR_COUNT_SOURCE_TIMEOUT_ERROR = "DISTRIBUTOR_COUNT_SOURCE_TIMEOUT_ERROR";

    private BondReducer bondReducer;

    private List<? extends IQueryExecutor> queryExecutors = new ArrayList<>();

    private Logger log = GSLoggerFactory.getLogger(getClass());

    private static Integer timeoutErrorsDuringCount = 0;

    public static Integer getTimeoutErrorsDuringCount() {
	return timeoutErrorsDuringCount;
    }

    public static void addTimeoutErrorDuringCount(int timeout) {
	synchronized (timeoutErrorsDuringCount) {
	    timeoutErrorsDuringCount++;
	}
	GSLoggerFactory.getLogger(Distributor.class).info("Added distributor count error ({}s timeout) (now {})", timeout,
		getTimeoutErrorsDuringCount());
    }

    public Distributor() {

	bondReducer = new BondReducer();
    }

    @Override
    public void setQuerySubmitters(List<? extends IQueryExecutor> querySubmitters) {
	this.queryExecutors = querySubmitters;

    }

    public IQueryExecutor getDistributedQuerySubmitter(String sourceIdentifier) {
	for (IQueryExecutor querySubmitter : queryExecutors) {
	    if (querySubmitter.getType().equals(Type.DISTRIBUTED) && querySubmitter.getSourceIdentifier().equals(sourceIdentifier)) {
		return querySubmitter;
	    }
	}
	return null;
    }

    public IQueryExecutor getDatabaseQuerySubmitter() {
	for (IQueryExecutor querySubmitter : queryExecutors) {
	    if (querySubmitter.getType().equals(Type.DATABASE)) {
		return querySubmitter;
	    }
	}
	return null;
    }

    @Override
    public CountSet count(DiscoveryMessage message) throws GSException {
	int size = queryExecutors.size();
	TaskListExecutor<SimpleEntry<String, DiscoveryCountResponse>> taskList = new TaskListExecutor<>(size);
	for (IQueryExecutor queryExecutor : queryExecutors) {

	    switch (queryExecutor.getType()) {
	    case DISTRIBUTED:

		String sourceIdentifier = queryExecutor.getSourceIdentifier();
		Bond normalizedBond = message.getNormalizedBond();
		// a reduced bond is submitted to the distributed query submitter,
		// calculated from the normalized bond
		Bond reducedBond;
		try {
		    reducedBond = bondReducer.getReducedBond(normalizedBond, sourceIdentifier);
		    ReducedDiscoveryMessage reducedMessage = new ReducedDiscoveryMessage(message, reducedBond);
		    taskList.addTask(() -> ((IDistributedQueryExecutor) queryExecutor).count(reducedMessage));

		    // GSLoggerFactory.getLogger(getClass()).info("Distributed counting task created for source {}",
		    // sourceIdentifier);

		} catch (GSException e) {
		    // the reduced bond couldn't be calculated
		    // e.g. because the source bond is not found
		    log.warn("Can't calculate reduced bond during count for distributed source {}", sourceIdentifier);
		}
		break;
	    case DATABASE:
	    default:
		// the Database is able to directly execute the normalized bond
		taskList.addTask(() -> ((IDatabaseQueryExecutor) queryExecutor).count(message));

		// GSLoggerFactory.getLogger(getClass()).info("Harvested counting task created");

		break;
	    }
	}

	// GSLoggerFactory.getLogger(getClass()).info("Counting tasks STARTED");

	int timeout = getTimeout(message);

	List<Future<SimpleEntry<String, DiscoveryCountResponse>>> futures = taskList.executeAndWait(timeout);

	// GSLoggerFactory.getLogger(getClass()).info("Counting tasks ENDED");

	CountSet ret = new CountSet();
	for (int i = 0; i < futures.size(); i++) {

	    Future<SimpleEntry<String, DiscoveryCountResponse>> future = futures.get(i);

	    try {
		SimpleEntry<String, DiscoveryCountResponse> countPair = future.get();
		ret.addCountPair(countPair);

	    } catch (CancellationException ex) {

		addTimeoutErrorDuringCount(timeout);

		//
		// this exception is thrown when the current future is timed out
		// see TaskListExecutor.executeAndWait(long timeout)
		//
		GSLoggerFactory.getLogger(getClass()).warn("Source timed out");
		addError(//
			message.getException(), //
			i, //
			DISTRIBUTOR_COUNT_SOURCE_TIMEOUT_ERROR, //
			"Source timed out: ");

	    } catch (InterruptedException ie) {
		//
		// in case of exceptions no count pair will be provided for the given source
		//
		Thread.currentThread().interrupt();

		addError(//
			message.getException(), //
			i, //
			DISTRIBUTOR_COUNT_JOIN_ERROR, //
			"Interrupted exception joining source: ");

	    } catch (ExecutionException ee) {

		GSLoggerFactory.getLogger(getClass()).error(ee.getMessage(), ee);

		//
		// in case of exceptions no count pair will be provided for the given source
		//
		Throwable cause = ee.getCause();
		if (cause instanceof GSException) {

		    GSException gse = (GSException) cause;
		    message.getException().getErrorInfoList().addAll(gse.getErrorInfoList());

		} else {

		    addError(//
			    message.getException(), //
			    i, //
			    DISTRIBUTOR_COUNT_JOIN_ERROR, //
			    "Unexpected runtime exception joining source: ");
		}

	    }
	}
	return ret;
    }

    private int getTimeout(DiscoveryMessage message) {
	Integer timeout = message.getRequestTimeout();
	if (timeout == null) {
	    timeout = DEFAULT_EXECUTION_TIMEOUT;
	    // String msg = "Timeout not specified in message. Using default of: " + timeout + "s";
	    // GSLoggerFactory.getLogger(getClass()).info(msg);
	} else {
	    String msg = "Timeout set to: " + timeout + "s";
	    GSLoggerFactory.getLogger(getClass()).info(msg);
	}

	return timeout;
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

    /**
     * Throws the exception only if the BondReducer is unable to work. In all the other cases, a ResultSet is always
     * returned, possible
     * empty and/or with a set of exceptions
     */
    public <T> ResultSet<T> retrieve(DiscoveryMessage message, Class<T> clazz) throws GSException {

	CountSet countSet = count(message);

	int size = queryExecutors.size();
	log.debug("Retrieving results from {} executors", size);

	TaskListExecutor<ResultSet<T>> taskList = new TaskListExecutor<>(size);

	Page page = message.getPage();
	// the page start value is always >= 1 but here starting from 0 is better
	int start = page.getStart() - 1;
	int count = page.getSize();
	int queryExecutorStart = 0;

	for (IQueryExecutor queryExecutor : queryExecutors) {

	    String sourceIdentifier = queryExecutor.getSourceIdentifier();
	    Integer executorCount = countSet.getCount(sourceIdentifier);

	    if (executorCount == null) {
		// we skip the query submitter that gave problems
		log.warn("Found a null count from query executor of source {}", sourceIdentifier);
		continue;
	    }

	    int queryExecutorEnd = queryExecutorStart + executorCount;

	    if (start >= queryExecutorStart && start < queryExecutorEnd && count > 0) {

		// we must query this query submitter
		int relativeStart = start - queryExecutorStart;
		int availableRecords = executorCount - relativeStart;
		int relativeCount = Math.min(count, availableRecords);
		count = count - relativeCount;
		start = start + relativeCount;
		// here the page start is augmented of 1 since externally
		// the min page start value must be always >=1
		Page newPage = new Page(relativeStart + 1, relativeCount);

		// log.debug("Requesting {} results starting at {} to source {}", relativeCount, relativeStart,
		// sourceIdentifier);

		switch (queryExecutor.getType()) {
		case DISTRIBUTED:
		    Bond normalizedBond = message.getNormalizedBond();
		    // a reduced bond is submitted to the distributed query submitter,
		    // calculated from the normalized bond
		    Bond reducedBond;
		    try {
			reducedBond = bondReducer.getReducedBond(normalizedBond, sourceIdentifier);
			ReducedDiscoveryMessage newMessage = new ReducedDiscoveryMessage(message, reducedBond);
			if (clazz.equals(GSResource.class)) {
			    taskList.addTask(new Callable<ResultSet<T>>() {

				@Override
				public ResultSet<T> call() throws Exception {
				    return (ResultSet<T>) ((IDistributedQueryExecutor) queryExecutor).retrieve(newMessage, newPage);
				}
			    });

			} else if (clazz.equals(Node.class)) {
			    taskList.addTask(new Callable<ResultSet<T>>() {

				@Override
				public ResultSet<T> call() throws Exception {
				    return (ResultSet<T>) ((IDistributedQueryExecutor) queryExecutor).retrieveNodes(newMessage, newPage);
				}
			    });

			} else if (clazz.equals(String.class)) {
			    taskList.addTask(new Callable<ResultSet<T>>() {

				@Override
				public ResultSet<T> call() throws Exception {
				    return (ResultSet<T>) ((IDistributedQueryExecutor) queryExecutor).retrieveStrings(newMessage, newPage);
				}
			    });

			} else {
			    log.error("Unexpected result type: {}", clazz.getCanonicalName());
			}

			// log.info("Distributed retrieving task created for source {}", sourceIdentifier);

		    } catch (GSException e) {
			// the reduced bond couldn't be calculated
			// e.g. because the source bond is not found
			log.warn("Can't calculate reduced bond during retrieval for distributed source {}", sourceIdentifier);
		    }
		    break;
		case DATABASE:
		default:
		    // the Database is able to directly execute the normalized bond
		    if (clazz.equals(GSResource.class)) {
			taskList.addTask(new Callable<ResultSet<T>>() {
			    public eu.essi_lab.messages.ResultSet<T> call() throws Exception {
				return (ResultSet<T>) ((IDatabaseQueryExecutor) queryExecutor).retrieve(message, newPage);
			    };
			});
		    } else if (clazz.equals(Node.class)) {
			taskList.addTask(new Callable<ResultSet<T>>() {

			    @Override
			    public ResultSet<T> call() throws Exception {
				return (ResultSet<T>) ((IDatabaseQueryExecutor) queryExecutor).retrieveNodes(message, newPage);
			    }
			});

		    } else if (clazz.equals(String.class)) {
			taskList.addTask(new Callable<ResultSet<T>>() {

			    @Override
			    public ResultSet<T> call() throws Exception {
				return (ResultSet<T>) ((IDatabaseQueryExecutor) queryExecutor).retrieveStrings(message, newPage);
			    }
			});

		    } else {
			log.error("Unexpected result type: {}", clazz.getCanonicalName());
		    }

		    // log.info("Harvested retrieving task created");

		    break;
		}
	    }

	    queryExecutorStart = queryExecutorEnd;
	}

	ResultSet<T> outputSet = initResultSet(countSet, page);

	// GSLoggerFactory.getLogger(getClass()).info("Retrieving tasks STARTED");

	int timeout = getTimeout(message);

	List<Future<ResultSet<T>>> futures = taskList.executeAndWait(timeout);

	// GSLoggerFactory.getLogger(getClass()).info("Retrieving tasks ENDED futures: {}", futures.size());

	// restores the original page because the data base modifies the page of the message
	message.setPage(page);

	for (int i = 0; i < futures.size(); i++) {

	    Future<ResultSet<T>> future = futures.get(i);

	    try {

		ResultSet<T> futureSet = future.get();

		List<T> resultsList = futureSet.getResultsList();

		GSLoggerFactory.getLogger(getClass()).info("Result size: {}", resultsList.size());

		List<T> results = outputSet.getResultsList();
		results.addAll(resultsList);

	    } catch (CancellationException ex) {

		GSLoggerFactory.getLogger(getClass()).info("Cancellation exception");

		//
		// this exception is thrown when the current future is timed out
		// see TaskListExecutor.executeAndWait(long timeout)
		//
		GSLoggerFactory.getLogger(getClass()).warn("Source timed out");
		addError(//
			outputSet.getException(), //
			i, //
			DISTRIBUTOR_RETRIEVE_SOURCE_TIMEOUT_ERROR, //
			"Source timed out: ");

	    } catch (InterruptedException e) {

		GSLoggerFactory.getLogger(getClass()).info("Interrupoted exception");

		Thread.currentThread().interrupt();

		addError(//
			outputSet.getException(), //
			i, //
			DISTRIBUTOR_RETRIEVE_JOIN_ERROR, //
			"Interrupted exception joining source: ");

	    } catch (ExecutionException e) {

		GSLoggerFactory.getLogger(getClass()).info("Execution exception");

		Throwable cause = e.getCause();
		if (cause instanceof GSException) {

		    GSException gse = (GSException) cause;
		    outputSet.getException().getErrorInfoList().addAll(gse.getErrorInfoList());

		} else {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		    addError(//
			    message.getException(), //
			    i, //
			    DISTRIBUTOR_RETRIEVE_JOIN_ERROR, //
			    "Unexpected runtime exception joining source: ");
		}
	    }
	}

	return outputSet;
    }

    private <T> ResultSet<T> initResultSet(CountSet countSet, Page page) {

	ResultSet<T> result = new ResultSet<>();

	int pageCount = (int) (countSet.getCount() < page.getSize() ? 1 : Math.ceil(((double) countSet.getCount() / page.getSize())));

	countSet.setPageCount(countSet.getCount() == 0 ? 0 : pageCount);

	log.info("Page start [{}], page count [{}]", page.getStart(), page.getSize());

	int pageIndex = page.getSize() == 0 ? 0 : page.getStart() <= page.getSize() ? 1 : (page.getStart() / page.getSize()) + 1;
	countSet.setPageIndex(pageIndex);

	result.setCountResponse(countSet);

	return result;
    }

    /**
     * @param errorId
     * @param errorDescription
     * @return
     */
    private ErrorInfo createErrorInfo(String errorId, String errorDescription) {
	ErrorInfo info = new ErrorInfo();
	info.setCaller(this.getClass());
	info.setErrorDescription(errorDescription);
	info.setErrorId(errorId);
	info.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);
	info.setErrorCorrection("");
	info.setSeverity(ErrorInfo.SEVERITY_ERROR);
	return info;
    }

    /**
     * @param ex
     * @param executor
     * @param errorCode
     * @param message
     */
    private void addError(GSException ex, int executor, String errorCode, String message) {

	ex.getErrorInfoList()
		.add(createErrorInfo(//
			errorCode, //
			message + queryExecutors.get(executor).getSourceIdentifier()));

    }
}
