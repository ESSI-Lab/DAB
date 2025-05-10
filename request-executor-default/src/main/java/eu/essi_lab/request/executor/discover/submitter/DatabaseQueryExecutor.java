package eu.essi_lab.request.executor.discover.submitter;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.lib.servlet.RequestManager;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.request.executor.query.IDatabaseQueryExecutor;

/**
 * @author Fabrizio
 */
public class DatabaseQueryExecutor implements IDatabaseQueryExecutor {

    List<GSSource> sources = new ArrayList<>();

    public DatabaseQueryExecutor() {
    }

    @Override
    public String getSourceIdentifier() {
	return "TheDatabase";
    }

    @Override
    public SimpleEntry<String, DiscoveryCountResponse> count(DiscoveryMessage message) throws GSException {

	RequestManager.getInstance().updateThreadName(getClass(), message.getRequestId());

	StorageInfo uri = message.getDataBaseURI();
	
	DatabaseFinder finder = DatabaseProviderFactory.getFinder(uri);

	GSLoggerFactory.getLogger(getClass()).info("Count STARTED");

	DiscoveryCountResponse countResult = finder.count(message);

	GSLoggerFactory.getLogger(getClass()).info("Count ENDED");

	SimpleEntry<String, DiscoveryCountResponse> countPair = new SimpleEntry<>(getSourceIdentifier(), countResult);

	return countPair;
    }

    @Override
    public ResultSet<GSResource> retrieve(DiscoveryMessage message, Page page) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Retrieve STARTED");

	RequestManager.getInstance().updateThreadName(getClass(), message.getRequestId());

	StorageInfo uri = message.getDataBaseURI();

	DatabaseFinder finder = DatabaseProviderFactory.getFinder(uri);

	message.setPage(page);

	ResultSet<GSResource> resultSet = finder.discover(message);

	GSLoggerFactory.getLogger(getClass()).info("Retrieve ENDED");

	return resultSet;
    }

    @Override
    public ResultSet<Node> retrieveNodes(DiscoveryMessage message, Page page) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Retrieve STARTED");

	RequestManager.getInstance().updateThreadName(getClass(), message.getRequestId());

	StorageInfo uri = message.getDataBaseURI();

	DatabaseFinder finder = DatabaseProviderFactory.getFinder(uri);

	message.setPage(page);

	ResultSet<Node> resultSet = finder.discoverNodes(message);

	GSLoggerFactory.getLogger(getClass()).info("Retrieve ENDED");

	return resultSet;
    }

    @Override
    public ResultSet<String> retrieveStrings(DiscoveryMessage message, Page page) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Retrieve STARTED");

	RequestManager.getInstance().updateThreadName(getClass(), message.getRequestId());

	StorageInfo uri = message.getDataBaseURI();

	DatabaseFinder finder = DatabaseProviderFactory.getFinder(uri);

	message.setPage(page);

	ResultSet<String> resultSet = finder.discoverDistinctStrings(message);

	GSLoggerFactory.getLogger(getClass()).info("Retrieve ENDED");

	return resultSet;
    }

    @Override
    public Type getType() {
	return Type.DATABASE;
    }

    public List<GSSource> getHarvestedSources() {
	return sources;
    }

    public void addHarvestedSource(GSSource source) {
	sources.add(source);

    }

}
