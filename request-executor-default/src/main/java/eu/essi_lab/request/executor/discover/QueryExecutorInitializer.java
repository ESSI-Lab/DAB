package eu.essi_lab.request.executor.discover;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.SharedRepositoryInfo;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.request.executor.discover.submitter.DatabaseQueryExecutor;
import eu.essi_lab.request.executor.discover.submitter.DistributedQueryExecutor;
import eu.essi_lab.request.executor.query.IDistributedQueryExecutor;
import eu.essi_lab.request.executor.query.IQueryExecutor;
import eu.essi_lab.shared.yellowpage.GSYellowPage;
import eu.essi_lab.shared.yellowpage.GSYellowPageFactory;
public class QueryExecutorInitializer {

    public static final String QUERY_EXECUTOR_INITIALIZER_ILLEGAL_SOURCES = "QUERY_SUBMITTER_INITIALIZER_ILLEGAL_SOURCES";

    private AccessorFactory accessorFactory;

    private BrokeringStrategyResolver strategyResolver;

    private transient Logger logger = GSLoggerFactory.getLogger(QueryExecutorInitializer.class);
    
    public QueryExecutorInitializer() {
	
	strategyResolver = new BrokeringStrategyResolver();
    }

    private void throwGSException(String errorId, String description, String correction) throws GSException {
	GSException gse = new GSException();
	ErrorInfo info = new ErrorInfo();
	info.setContextId(QueryExecutorInitializer.class.getName());
	info.setErrorId(errorId);
	info.setErrorDescription(description);
	info.setErrorCorrection(correction);

	info.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);
	info.setSeverity(ErrorInfo.SEVERITY_ERROR);
	gse.addInfo(info);
	throw gse;

    }

    public AccessorFactory getAccessorFactory() {
	return accessorFactory;
    }

    public void setAccessorFactory(AccessorFactory accessorFactory) {
	this.accessorFactory = accessorFactory;
    }

    /**
     * Initializes a list of ordered query submitters from the current discovery message
     *
     * @param message
     * @return
     * @throws GSException if more than one harvested block of sources is present
     */
    public List<IQueryExecutor> initQueryExecutors(DiscoveryMessage message) throws GSException {

	List<GSSource> sources = message.getSources();

	List<IQueryExecutor> ret = new ArrayList<>();

	DatabaseQueryExecutor dbQuerySubmitter = null;
	for (GSSource source : sources) {
	    BrokeringStrategy strategy = source.getBrokeringStrategy();
	    if (strategy == null) {
		throwGSException(QUERY_EXECUTOR_INITIALIZER_ILLEGAL_SOURCES,
			"Null brokering strategy for " + source.getLabel() + " in discovery executor", null);
	    }

	    BrokeringStrategy rsolvedStrategy = strategy;
	    try {

		rsolvedStrategy = strategyResolver.resolveStrategy(source, message);

	    } catch (GSException gse) {

		DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(gse)));

	    }

	    switch (rsolvedStrategy) {
	    case DISTRIBUTED:

		try {

		    IDistributedQueryExecutor querySubmitter = createDistributedExecutor(source);

		    GSYellowPage yp = getYellowPages(message);
		    querySubmitter.setYp(yp);

		    //TODO read this from message like yp
		    IdentifierDecorator decorator = new IdentifierDecorator();
		    querySubmitter.setIdentifierDecorator(decorator);

		    ret.add(querySubmitter);

		} catch (GSException e) {

		    logger.warn("Exception adding query submitter for source {}, skipping source", source.getUniqueIdentifier(), e);

		    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
		}
		break;
	    case HARVESTED:

		if (dbQuerySubmitter == null) {
		    dbQuerySubmitter = createDatabaseQueryExecutor();
		    ret.add(0, dbQuerySubmitter);
		}
		dbQuerySubmitter.addHarvestedSource(source);

		break;
	    default:
		throwGSException(QUERY_EXECUTOR_INITIALIZER_ILLEGAL_SOURCES, "Unexpected brokering strategy in discovery executor",
			"Provide a known brokering strategy");
	    }
	}

	return ret;
    }

    public GSYellowPage getYellowPages(DiscoveryMessage message) {
	SharedRepositoryInfo sharedRepoInfo = message.getSharedRepositoryInfo();
	return GSYellowPageFactory.getYellowPageReader(sharedRepoInfo);

    }

    /**
     * Creates the database query executor
     *
     * @return {@link DatabaseQueryExecutor}
     */
    protected DatabaseQueryExecutor createDatabaseQueryExecutor() {
	return new DatabaseQueryExecutor();
    }

    /**
     * Creates the distributed query executor, given a source
     *
     * @param source
     * @return {@link DistributedQueryExecutor}
     * @throws GSException
     */
    private IDistributedQueryExecutor createDistributedExecutor(GSSource source) throws GSException {
	IDistributedAccessor accessor = getAccessorFactory().getDistributedAccessor(source);
	return new DistributedQueryExecutor(accessor, source.getUniqueIdentifier());
    }
}
