package eu.essi_lab.gssrv.health.components;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import static eu.essi_lab.configuration.sync.ConfigurationSync.ERR_ID_NODBMANAGER;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indeed.status.core.Urgency;

import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.configuration.GSConfigurationManager;
import eu.essi_lab.gssrv.health.db.HCDBStorageURI;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IRequestExecutor;
public class DiscoveryPingableComponent implements IGSHealthCheckPingableComponent {

    private Logger logger = LoggerFactory.getLogger(DiscoveryPingableComponent.class);
    private Page page;

    public DiscoveryPingableComponent() {

	page = new Page(1, 1);
    }

    @Override
    public String getDescription() {
	return "Checks the discovery workflow";
    }

    @Override
    public Urgency getUrgency() {
	return Urgency.REQUIRED;
    }

    @Override
    public String getId() {
	return "DiscoverPingableComponent";
    }

    @Override
    public Boolean applicableTo(ExecutionMode mode) {

	switch (mode) {
	case BATCH:
	    return false;

	case MIXED:
	case FRONTEND:
	default:
	    return true;
	}
    }

    @Override
    public void ping() throws Exception {

	try {

	    GSConfigurationManager manager = new GSConfigurationManager();

	    GSConfiguration c = manager.getConfiguration();

	    if (c == null) {
		// system not initialized
		return;
	    }

	} catch (GSException e) {

	    DefaultGSExceptionReader reader = new DefaultGSExceptionReader(e);
	    DefaultGSExceptionHandler handler = new DefaultGSExceptionHandler(reader);
	    DefaultGSExceptionLogger.log(handler);

	    if (!reader.getLastErrorIdentifier().equalsIgnoreCase(ERR_ID_NODBMANAGER)) {
		throw new Exception("Can't get configuration with GSException " + handler.createGSErrorCode());
	    }

	    return;

	}

	DiscoveryMessage message = createMessage();

	if (message == null) {
	    throw new Exception("Healthy suite: initialized, no sources.");
	}

	logger.trace("Executing Health Check discovery message");

	ResultSet<GSResource> response = createExecutor().retrieve(message);

	logger.debug("Completed Health Check discovery message");

	logger.debug("Validating Health Check discovery message result");

	validateResult(response, message);

	logger.debug("Validation of discovery message done.");
    }

    /**
     * @return
     */
    IRequestExecutor<DiscoveryMessage, GSResource, CountSet, ResultSet<GSResource>> createExecutor() {

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);

	Iterator<IDiscoveryExecutor> it = loader.iterator();

	if (it.hasNext()) {
	    return it.next();
	}

	return null;
    }

    /**
     * @return
     * @throws GSException
     */
    private DiscoveryMessage createMessage() throws GSException {

	DiscoveryMessage message = new DiscoveryMessage();

	message.setRequestId(getClass().getSimpleName() + "-" + UUID.randomUUID().toString());
	//
	// disables the query registration, not useful here
	//
	message.setQueryRegistrationEnabled(false);

	logger.debug("Generating discovery message");

	Optional<GSSource> optional = ConfigurationUtils.getAllSources().stream()
		.filter(gsSource -> gsSource.getBrokeringStrategy().equals(BrokeringStrategy.HARVESTED)
			|| gsSource.getBrokeringStrategy().equals(BrokeringStrategy.MIXED))
		.findAny();

	if (!optional.isPresent()) {

	    return null;
	}

	GSSource source = optional.get();

	message.setSources(Arrays.asList(source));

	message.setPage(page);

	message.setDataBaseURI(new HCDBStorageURI());

	return message;
    }

    /**
     * @param response
     * @param message
     * @throws Exception
     */
    void validateResult(ResultSet<GSResource> response, DiscoveryMessage message) throws Exception {

	List<GSResource> list = response.getResultsList();

	if (list.isEmpty()) {
	    throw new Exception("No results in health check response");
	}

	GSResource first = list.get(0);

	if (first == null) {
	    throw new Exception("First result is null in health check response");
	}

	String sourceid = first.getSource().getUniqueIdentifier();

	if (!sourceid.equalsIgnoreCase(message.getSources().get(0).getUniqueIdentifier())) {
	    throw new Exception("First result does not belong to requested source in health check response, expected "
		    + message.getSources().get(0).getUniqueIdentifier() + " but found " + sourceid);

	}

    }
}
