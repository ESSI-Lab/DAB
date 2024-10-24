package eu.essi_lab.gssrv.health.methods;

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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.gssrv.health.GSPingMethod;
import eu.essi_lab.gssrv.health.db.HCStorageInfo;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IRequestExecutor;

/**
 * @author Fabrizio
 */
public class DiscoveryMethod implements GSPingMethod {

    private Page page;

    public DiscoveryMethod() {

	page = new Page(1, 1);
    }

    @Override
    public String getDescription() {

	return "Checks the discovery workflow";
    }

    @Override
    public Boolean applicableTo(ExecutionMode mode) {

	switch (mode) {
	case CONFIGURATION:
	case LOCAL_PRODUCTION:
	case BATCH:
	    return false;
	case MIXED:
	case FRONTEND:
	case ACCESS:
	case INTENSIVE:
	default:
	    return true;
	}
    }

    @Override
    public void ping() throws Exception {

	DiscoveryMessage message = createMessage();

	ResultSet<GSResource> response = createExecutor().retrieve(message);

	validateResult(response, message);
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
    private DiscoveryMessage createMessage() {

	DiscoveryMessage message = new DiscoveryMessage();
	message.setRequestId(getClass().getSimpleName() + "-" + UUID.randomUUID().toString());

	GSSource source = ConfigurationWrapper.getAllSources().//
		stream().//
		filter(s -> s.getBrokeringStrategy().equals(BrokeringStrategy.HARVESTED)
			|| s.getBrokeringStrategy().equals(BrokeringStrategy.MIXED))
		.//
		findAny().//
		get();

	message.setSources(Arrays.asList(source));

	message.setPage(page);

	message.setDataBaseURI(new HCStorageInfo());

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
