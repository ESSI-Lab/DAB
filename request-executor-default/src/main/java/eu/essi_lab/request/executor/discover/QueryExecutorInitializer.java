package eu.essi_lab.request.executor.discover;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.request.executor.discover.submitter.DatabaseQueryExecutor;
import eu.essi_lab.request.executor.discover.submitter.DistributedQueryExecutor;
import eu.essi_lab.request.executor.query.IDistributedQueryExecutor;
import eu.essi_lab.request.executor.query.IQueryExecutor;

/**
 * Initializes the query executors from the ordered source list that is present in the discovery message
 *
 * @author boldrini
 */
public class QueryExecutorInitializer {

    public static final String QUERY_EXECUTOR_INITIALIZER_ILLEGAL_SOURCES = "QUERY_SUBMITTER_INITIALIZER_ILLEGAL_SOURCES";

    private static final String UNABLE_TO_CREATE_DISTRIBUTED_ACCESSOR = "QUERY_SUBMITTER_UNABLE_TO_CREATE_DISTRIBUTED_ACCESSOR";

    private static final String UNABLE_TO_FIND_DISTRIBUTED_ACCESSOR = "QUERY_SUBMITTER_UNABLE_TO_FIND_DISTRIBUTED_ACCESSOR";

    private BrokeringStrategyResolver strategyResolver;

    private Logger logger = GSLoggerFactory.getLogger(QueryExecutorInitializer.class);

    public QueryExecutorInitializer() {

	strategyResolver = new BrokeringStrategyResolver();
    }

    /**
     * @param errorId
     * @param description
     * @param correction
     * @throws GSException
     */
    private void throwGSException(String errorId, String description, String correction) throws GSException {

	throw GSException.createException(//
		getClass(), //
		description, //
		correction, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		errorId);
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

		gse.log();

	    }

	    switch (rsolvedStrategy) {
	    case DISTRIBUTED:

		try {

		    IDistributedQueryExecutor querySubmitter = createDistributedExecutor(source);

		    IdentifierDecorator decorator = new IdentifierDecorator();
		    querySubmitter.setIdentifierDecorator(decorator);

		    ret.add(querySubmitter);

		} catch (GSException e) {

		    logger.warn("Exception adding query submitter for source {}, skipping source", source.getUniqueIdentifier(), e);

		    e.log();
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

	Optional<AccessorSetting> accessorSetting = ConfigurationWrapper.getAccessorSetting(source);

	if (accessorSetting.isPresent()) {

	    try {

		@SuppressWarnings("rawtypes")
		IDistributedAccessor accessor = AccessorFactory.getConfiguredDistributedAccessor(accessorSetting.get());

		return new DistributedQueryExecutor(accessor, source.getUniqueIdentifier());

	    } catch (Exception e) {

		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			UNABLE_TO_CREATE_DISTRIBUTED_ACCESSOR);
	    }
	}

	throw GSException.createException(//
		getClass(), //
		"Distributed accessor from source [" + source.getLabel() + "] not found", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		UNABLE_TO_FIND_DISTRIBUTED_ACCESSOR);

    }

}
