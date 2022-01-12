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

import java.util.Arrays;

import org.slf4j.Logger;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseConsumerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.bond.parser.IdentifierBondHandler;
import eu.essi_lab.messages.bond.parser.ParentIdBondHandler;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
public class BrokeringStrategyResolver {

    private transient Logger logger = GSLoggerFactory.getLogger(BrokeringStrategyResolver.class);
    private static final String UNKNOWN_BROKERING_STRATEGY_ERR_ID = "UNKNOWN_BROKERING_STRATEGY_ERR_ID";

    public BrokeringStrategyResolver() {
    }

    public BrokeringStrategy resolveStrategy( GSSource source, DiscoveryMessage message) throws GSException {

	BrokeringStrategy originalStrategy = source.getBrokeringStrategy();

	switch (originalStrategy) {
	case HARVESTED:
	case DISTRIBUTED:
	    return originalStrategy;
	case MIXED:
	    return resolveMixedStrategy(source, message);
	}

	throw GSException.createException(//
		BrokeringStrategyResolver.class, //
		"Unknown original strategy for source " + source.getUniqueIdentifier(), //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		UNKNOWN_BROKERING_STRATEGY_ERR_ID);
    }

    /**
     * @param source
     * @param message
     * @return
     * @throws GSException
     */
    private BrokeringStrategy resolveMixedStrategy(GSSource source, DiscoveryMessage message) throws GSException {

	Boolean secondLevel = isMixedSecondLevel(source, message);

	boolean distributedGetById = isDistributedGetById(source, message);

	return secondLevel || distributedGetById ? BrokeringStrategy.DISTRIBUTED : BrokeringStrategy.HARVESTED;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private boolean isDistributedGetById(GSSource source, DiscoveryMessage message) throws GSException {

	IdentifierBondHandler handler = new IdentifierBondHandler(message.getNormalizedBond());

	if (handler.getIdentifiers().size() == 1) {

	    DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	    discoveryMessage.setRequestId(message.getRequestId());
	    discoveryMessage.setPage(new Page(1, 1));

	    discoveryMessage.setSources(Arrays.asList(source));
	    discoveryMessage.setDataBaseURI(message.getDataBaseURI());
	    discoveryMessage.setSharedRepositoryInfo(message.getSharedRepositoryInfo());

	    SimpleValueBond bond = BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.IDENTIFIER, //
		    handler.getIdentifiers().get(0));

	    discoveryMessage.setPermittedBond(bond);
	    discoveryMessage.setUserBond(bond);
	    discoveryMessage.setNormalizedBond(bond);

	    DatabaseReader reader = new DatabaseConsumerFactory().createDataBaseReader(message.getDataBaseURI());

	    DiscoveryCountResponse countResponse = reader.count(message);

	    if (countResponse.getCount() == 0) {

		logger.trace("Found distributed get record by id query");

		return true;
	    }
	}

	logger.trace("No distributed get record by id query found");

	return false;
    }

    /**
     * Checks if the provided {@link DiscoveryMessage} is a second-level query for the provided {@link GSSource}. The
     * provided {@link
     * GSSource} MUST be of type {@link eu.essi_lab.model.BrokeringStrategy#MIXED}
     *
     * @param source
     * @param message
     * @return
     * @throws GSException
     */
    public Boolean isMixedSecondLevel(GSSource source, DiscoveryMessage message) throws GSException {

	logger.trace("Checking if message is second-level for source {} [{}]", source.getLabel(), source.getUniqueIdentifier());

	DiscoveryBondParser parser = new DiscoveryBondParser(message.getNormalizedBond());

	ParentIdBondHandler parentBondHandler = new ParentIdBondHandler();
	parser.parse(parentBondHandler);

	if (parentBondHandler.isParentIdFound()) {

	    logger.trace("Found a parent id {}", parentBondHandler.getParentValue());

	    DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	    discoveryMessage.setRequestId(message.getRequestId());
	    discoveryMessage.setPage(new Page(1, 1));

	    discoveryMessage.setSources(Arrays.asList(source));
	    discoveryMessage.setDataBaseURI(message.getDataBaseURI());
	    discoveryMessage.setSharedRepositoryInfo(message.getSharedRepositoryInfo());

	    SimpleValueBond bond = BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.IDENTIFIER, //
		    parentBondHandler.getParentValue());

	    discoveryMessage.setPermittedBond(bond);
	    discoveryMessage.setUserBond(bond);
	    discoveryMessage.setNormalizedBond(bond);

	    DatabaseReader reader = new DatabaseConsumerFactory().createDataBaseReader(message.getDataBaseURI());

	    ResultSet<GSResource> results = reader.discover(discoveryMessage);

	    if (!results.getResultsList().isEmpty()) {

		logger.trace("Found on DB resource with id {} from source {}", parentBondHandler.getParentValue(),
			results.getResultsList().get(0).getSource().getUniqueIdentifier());

		if (results.getResultsList().get(0).getSource().getUniqueIdentifier().equalsIgnoreCase(source.getUniqueIdentifier())) {

		    message.addParentGSResource(results.getResultsList().get(0));

		    return true;
		}
	    }

	    logger.trace("Empty result set was found on DB for resources with id {}", parentBondHandler.getParentValue());

	    return false;

	} else {
	    logger.trace("No second-level constraint was found in query");
	    return false;
	}
    }
}
