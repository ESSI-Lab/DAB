/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;

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
import org.xml.sax.SAXException;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchBondHandler;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.bond.parser.IdentifierBondHandler;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMapType;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class OpenSearchFinder implements DatabaseFinder {

    static boolean debugQueries = false;

    private OpenSearchDatabase database;
    private OpenSearchWrapper wrapper;

    /**
     * 
     */
    private static final long MAP_UPDATE_PERIOD = TimeUnit.MINUTES.toMillis(5);
    /**
     * 
     */
    private static CachedMapUpdater MAP_UPDATER_TASK;

    /**
     * 
     */
    private static HashMap<String, String> SOURCES_DATA_FOLDER_MAP = new HashMap<String, String>();

    /**
     * @author Fabrizio
     */
    private static class CachedMapUpdater extends TimerTask {

	private OpenSearchFinder finder;

	/**
	 * @param finder
	 */
	public void setOpenSearchFinder(OpenSearchFinder finder) {

	    this.finder = finder;
	}

	@Override
	public void run() {

	    synchronized (this) {

		try {

		    GSLoggerFactory.getLogger(getClass()).debug("Updating sources to data folder map STARTED");

		    SOURCES_DATA_FOLDER_MAP = finder.getSourcesDataMap(//

			    ConfigurationWrapper.getHarvestedAndMixedSources().//
				    stream().//
				    map(s -> s.getUniqueIdentifier()).//
				    collect(Collectors.toList()),

			    false);

		    GSLoggerFactory.getLogger(getClass()).debug("Updating sources to data folder map ENDED");

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error("Error occurred while updating sources to data folder map cache");

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }
	};
    }

    /**
     * 
     */
    public OpenSearchFinder() {

	if (MAP_UPDATER_TASK == null) {

	    MAP_UPDATER_TASK = new CachedMapUpdater();

	    Timer timer = new Timer();
	    timer.scheduleAtFixedRate(MAP_UPDATER_TASK, 0, MAP_UPDATE_PERIOD);
	}

	MAP_UPDATER_TASK.setOpenSearchFinder(this);
    }

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
	     debugQueries = true;

	    SearchResponse<Object> searchResponse = search_(message, true);

	    Map<String, Aggregate> aggregations = searchResponse.aggregations();

	    int total = 0;

	    Optional<Queryable> element = message.getDistinctValuesElement();

	    if (element.isPresent()) {

		total = getCardinalityValue(aggregations, element);

	    } else {

		total = (int) searchResponse.hits().total().value();
	    }

	    DiscoveryCountResponse response = new DiscoveryCountResponse();

	    if (message.isOutputSources()) {

		response.setCount(message.getSources().size());

	    } else {

		response.setCount(total);
	    }

	    if (element.isEmpty()) {

		TermFrequencyMapType mapType = ConversionUtils.fromAgg(aggregations);

		TermFrequencyMap tfMap = new TermFrequencyMap(mapType);

		response.setTermFrequencyMap(tfMap);
	    }

	    return response;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderCountError", ex);
	}
    }

    @Override
    public ResultSet<GSResource> discover(DiscoveryMessage message) throws GSException {

	 debugQueries = true;

	ResultSet<GSResource> resultSet = new ResultSet<>();
	List<GSResource> resources = null;

	try {

	    if (message.getDistinctValuesElement().isPresent()) {

		Query query = buildQuery(message, false);

		List<Queryable> queryables = message.getResourceSelector().getIndexesQueryables();

		resources = wrapper.aggregateWithNestedAgg(//

			query, //
			queryables, //
			message.getDistinctValuesElement().get(), //
			message.getPage().getSize()).//

			stream().//
			map(s -> ConversionUtils.toGSResource(s).orElse(null)).//
			filter(Objects::nonNull).//
			collect(Collectors.toList());

	    } else {

		SearchResponse<Object> response = search_(message, false);

		PerformanceLogger pl = new PerformanceLogger(//
			PerformanceLogger.PerformancePhase.OPENSEARCH_FINDER_RESOURCES_CREATION, //
			message.getRequestId(), //
			Optional.ofNullable(message.getWebRequest()));

		resources = ConversionUtils.toJSONSourcesList(response).//
			stream().//
			map(s -> ConversionUtils.toGSResource(s).orElse(null)).//
			filter(Objects::nonNull).//
			collect(Collectors.toList());

		pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

		handleSearchAfter(response, resultSet);
	    }

	    //
	    //
	    //

	    if (message.isOutputSources()) {

		List<GSResource> list = resources.//
			stream().//
			filter(StreamUtils.distinctBy(GSResource::getSource)).//
			collect(Collectors.toList());

		resultSet.setResultsList(list);

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

	ResultSet<Node> out = new ResultSet<>();

	try {

	    ResultSet<GSResource> response = discover(message);

	    out.setCountResponse(response.getCountResponse());

	    List<Node> nodes = response.getResultsList().stream().map(res -> {

		try {
		    return res.asDocument(true);

		} catch (ParserConfigurationException | JAXBException | SAXException | IOException e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);
		}

		return null;

	    }).filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    out.setResultsList(nodes);

	    return out;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscoverError", ex);
	}
    }

    @Override
    public ResultSet<String> discoverStrings(DiscoveryMessage message) throws GSException {

	ResultSet<String> out = new ResultSet<>();

	try {

	    ResultSet<GSResource> response = discover(message);

	    out.setCountResponse(response.getCountResponse());

	    List<String> strings = response.getResultsList().stream().map(res -> {

		try {
		    return res.asString(true);

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);
		}

		return null;

	    }).filter(Objects::nonNull).//
		    collect(Collectors.toList());

	    out.setResultsList(strings);

	    return out;

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
    public Query buildQuery(DiscoveryMessage message, boolean count) throws GSException {

	if (message.getUserBond().isPresent()) {

	    IdentifierBondHandler parser = new IdentifierBondHandler(message.getUserBond().get());

	    if (parser.isCanonicalQueryByIdentifiers()) {

		List<String> identifiers = parser.getIdentifiers();

		return OpenSearchQueryBuilder.buildSearchQuery(//
			database.getIdentifier(), //
			MetadataElement.IDENTIFIER.getName(), //
			identifiers.get(0));//
	    }
	}

	PerformanceLogger pl = new PerformanceLogger(//
		PerformanceLogger.PerformancePhase.OPENSEARCH_FINDER_GET_SOURCES_DATA_DIR_MAP, //
		message.getRequestId(), //
		Optional.ofNullable(message.getWebRequest()));

	HashMap<String, String> map = getSourcesDataMap(message);

	pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

	DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getPermittedBond());

	OpenSearchBondHandler handler = new OpenSearchBondHandler(wrapper, message, map);

	bondParser.parse(handler);

	return handler.getQuery(count);
    }

    /**
     * @param aggregations
     * @param element
     * @return
     */
    private int getCardinalityValue(Map<String, Aggregate> aggregations, Optional<Queryable> element) {

	Aggregate aggregate = aggregations.get(DataFolderMapping.toKeywordField(element.get().getName()));
	return (int) aggregate.cardinality().value();
    }

    /**
     * @param sourceIds
     * @return
     * @throws GSException
     */
    private HashMap<String, String> getSourcesDataMap(List<String> sourceIds, boolean useCache) throws GSException {

	HashMap<String, String> out = new HashMap<>();

	if (useCache && !SOURCES_DATA_FOLDER_MAP.isEmpty()) {

	    synchronized (MAP_UPDATER_TASK) {

		SOURCES_DATA_FOLDER_MAP.keySet().//
			stream().//
			filter(id -> sourceIds.contains(id)).//
			forEach(id -> out.put(id, SOURCES_DATA_FOLDER_MAP.get(id)));

	    }

	} else {

	    Query query = OpenSearchQueryBuilder.buildDataFolderQuery(getDatabase().getIdentifier(), sourceIds);

	    if (debugQueries) {

		GSLoggerFactory.getLogger(getClass()).debug("--- GET SOURCES DATA MAP ---");
		GSLoggerFactory.getLogger(getClass()).debug("\n\n{}\n\n", ConversionUtils.toJSONObject(query).toString(3));
	    }

	    try {

		SearchResponse<Object> response = wrapper.search(//
			MetaFolderMapping.get().getIndex(), //
			query, //
			Arrays.asList(MetaFolderMapping.SOURCE_ID, MetaFolderMapping.DATA_FOLDER), //
			0, //
			sourceIds.size(), //
			Optional.empty(), //
			Optional.empty(), //
			Optional.empty(), //
			true, // requesting cache
			false);

		response.//
			hits().//
			hits().//
			stream().//
			map(hit -> ConversionUtils.toJSONObject(hit.source()))//
			.forEach(obj -> {

			    out.put(obj.getString(MetaFolderMapping.SOURCE_ID), //
				    obj.getString(MetaFolderMapping.DATA_FOLDER));
			});

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);
		throw GSException.createException(getClass(), "OpenSearchFinderSourceDataFolderMapError", ex);
	    }

	    sourceIds.forEach(id -> {
		//
		// this is to avoid retrieval of resources belonging to a source that is
		// referenced in the query, but that is currently executing its first harvesting
		// or that is not yet been harvested
		//
		if (out.get(id) == null) {

		    out.put(id, "not-available");
		}
	    });
	}

	return out;
    }

    /**
     * @param message
     * @param useCache
     * @return
     * @throws GSException
     */
    private HashMap<String, String> getSourcesDataMap(RequestMessage message) throws GSException {

	return getSourcesDataMap(message.getSources().//
		stream().//
		map(s -> s.getUniqueIdentifier()).//
		collect(Collectors.toList()), //
		message.isCachedSourcesDataFolderMapUsed());
    }

    /**
     * @param response
     * @param resultSet
     */
    private void handleSearchAfter(SearchResponse<Object> response, ResultSet<GSResource> resultSet) {

	List<Hit<Object>> hits = response.hits().hits();
	int size = hits.size();
	if (size > 0) {

	    Hit<Object> hit = hits.get(size - 1);
	    List<FieldValue> sortVals = hit.sortVals();

	    if (!sortVals.isEmpty()) {

		FieldValue fieldValue = sortVals.get(0);

		if (fieldValue.isString()) {

		    resultSet.setSearchAfter(SearchAfter.of(fieldValue.stringValue()));

		} else if (fieldValue.isDouble()) {

		    resultSet.setSearchAfter(SearchAfter.of(fieldValue.doubleValue()));

		} else if (fieldValue.isLong()) {

		    resultSet.setSearchAfter(SearchAfter.of(fieldValue.longValue()));
		}
	    }
	}
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private SearchResponse<Object> search_(DiscoveryMessage message, boolean count) throws GSException {

	PerformanceLogger pl = null;

	if (count) {

	    pl = new PerformanceLogger(//
		    PerformanceLogger.PerformancePhase.OPENSEARCH_FINDER_COUNT, //
		    message.getRequestId(), //
		    Optional.ofNullable(message.getWebRequest()));
	} else {

	    pl = new PerformanceLogger(//
		    PerformanceLogger.PerformancePhase.OPENSEARCH_FINDER_DISCOVERY, //
		    message.getRequestId(), //
		    Optional.ofNullable(message.getWebRequest()));
	}

	Query query = buildQuery(message, count);

	try {

	    if (debugQueries) {

		GSLoggerFactory.getLogger(getClass()).debug(count ? "--- COUNT ---" : "--- DISCOVER ---");
		GSLoggerFactory.getLogger(getClass()).debug("\n\n{}\n\n", ConversionUtils.toJSONObject(query).toString(3));
	    }

	    SearchResponse<Object> response = null;

	    if (count) {

		response = wrapper.count(query, message);

	    } else {

		int start = message.getPage().getStart() - 1;
		int size = message.getPage().getSize();

		response = wrapper.search(//
			DataFolderMapping.get().getIndex(), //
			query, //
			start, //
			size, //
			message.getSortProperty(), //
			message.getSortOrder(), //
			message.getSearchAfter(), //
			message.isResourceBinaryExcluded());
	    }

	    pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

	    return response;

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscover_Error", ex);
	}
    }
}
