/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;

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

import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.bond.parser.IdentifierBondHandler;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMapType;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class OpenSearchFinder implements DatabaseFinder {

    private OpenSearchDatabase database;
    private OpenSearchWrapper wrapper;

    @Override
    public void setDatabase(Database database) {

	this.database = (OpenSearchDatabase) database;
	this.wrapper = new OpenSearchWrapper(this.database.getClient());
    }

    @Override
    public OpenSearchDatabase getDatabase() {

	return (OpenSearchDatabase) database;
    }

    @Override
    public boolean supports(StorageInfo info) {

	return OpenSearchDatabase.isSupported(info);
    }

    @Override
    public DiscoveryCountResponse count(DiscoveryMessage message) throws GSException {

	try {

	    int total = (int) discover_(message).hits().total().value();

	    DiscoveryCountResponse response = new DiscoveryCountResponse();

	    if (message.isOutputSources()) {

		response.setCount(message.getSources().size());

	    } else {
		response.setCount(total);
	    }

	    TermFrequencyMapType mapType = new TermFrequencyMapType();
	    TermFrequencyMap termFrequencyMap = new TermFrequencyMap(mapType);

	    response.setTermFrequencyMap(termFrequencyMap);

	    return response;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderCountError", ex);
	}
    }

    @Override
    public ResultSet<GSResource> discover(DiscoveryMessage message) throws GSException {
	
	ResultSet<GSResource> resultSet = new ResultSet<>();

	try {

	    SearchResponse<Object> response = discover_(message);

	    PerformanceLogger pl = new PerformanceLogger(//
		    PerformanceLogger.PerformancePhase.OPENSEARCH_FINDER_RESOURCES_CREATION, //
		    message.getRequestId(), //
		    Optional.ofNullable(message.getWebRequest()));

	    List<GSResource> resources = ConversionUtils.toBinaryList(response).//
		    stream().//
		    map(binary -> GSResource.createOrNull(binary)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

	    //
	    //
	    //

	    if (message.isOutputSources()) {

		List<GSResource> collect = resources.//
			stream().//
			filter(StreamUtils.distinctBy(GSResource::getSource)).//
			collect(Collectors.toList());

		resultSet.setResultsList(collect);

	    } else {

		resultSet.setResultsList(resources);
	    }

	    return resultSet;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscoverError", ex);
	}
    }

    @Override
    public ResultSet<Node> discoverNodes(DiscoveryMessage message) throws GSException {

	ResultSet<Node> resultSet = new ResultSet<>();

	try {

	    SearchResponse<Object> response = discover_(message);

	    List<Node> nodes = ConversionUtils.toNodeList(response);

	    resultSet.setResultsList(nodes);

	    return resultSet;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscoverError", ex);
	}
    }

    @Override
    public ResultSet<String> discoverStrings(DiscoveryMessage message) throws GSException {

	ResultSet<String> resultSet = new ResultSet<>();

	try {

	    SearchResponse<Object> response = discover_(message);

	    List<String> nodes = ConversionUtils.toStringList(response);

	    resultSet.setResultsList(nodes);

	    return resultSet;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscoverError", ex);
	}
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private SearchResponse<Object> discover_(DiscoveryMessage message) throws GSException {

	OpenSearchDiscoveryBondHandler handler = new OpenSearchDiscoveryBondHandler(message);
	DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getPermittedBond());
	bondParser.parse(handler);
	
	Query query = handler.getQuery();
	System.out.println(ConversionUtils.toJsonObject(query).toString(3));
	
	try {

	    int start = message.getPage().getStart() - 1;
	    int size = message.getPage().getSize();

	    GSLoggerFactory.getLogger(getClass()).debug("\n\n{}\n\n",
		    new JSONObject(ConversionUtils.toJsonObject(query).toString(3)).toString(3));

	    SearchResponse<Object> search = wrapper.search(query, start, size);

	    return search;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscover_Error", ex);
	}
    }
}
