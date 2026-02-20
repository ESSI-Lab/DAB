/**
 *
 */
package eu.essi_lab.api.database.opensearch;

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

import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.opensearch.index.mappings.*;
import eu.essi_lab.api.database.opensearch.query.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.bond.parser.*;
import eu.essi_lab.messages.count.*;
import eu.essi_lab.messages.stats.*;
import eu.essi_lab.messages.termfrequency.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.request.executor.query.IQueryExecutor.*;
import org.json.*;
import org.opensearch.client.opensearch._types.*;
import org.opensearch.client.opensearch._types.aggregations.*;
import org.opensearch.client.opensearch._types.query_dsl.*;
import org.opensearch.client.opensearch.core.*;
import org.opensearch.client.opensearch.core.search.*;
import org.w3c.dom.Node;
import org.xml.sax.*;

import javax.xml.bind.*;
import javax.xml.parsers.*;
import java.io.*;
import java.math.*;
import java.util.AbstractMap.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class OpenSearchFinder implements DatabaseFinder {

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
    private static HashMap<String, String> SOURCES_DATA_FOLDER_MAP = new HashMap<>();

    /**
     * @author Fabrizio
     */
    private static class CachedMapUpdater extends TimerTask {

	private final OpenSearchDatabase database;
	private final OpenSearchWrapper wrapper;

	/**
	 * @param database
	 * @param wrapper
	 */
	private CachedMapUpdater(OpenSearchDatabase database, OpenSearchWrapper wrapper) {

	    this.database = database;
	    this.wrapper = wrapper;
	}

	@Override
	public void run() {

	    synchronized (this) {

		try {

		    GSLoggerFactory.getLogger(getClass()).debug("Updating sources to data folder map STARTED");

		    SOURCES_DATA_FOLDER_MAP = getSourcesDataMap(//
			    database, //
			    wrapper, //
			    ConfigurationWrapper.getHarvestedAndMixedSources().//
				    stream().//
				    map(GSSource::getUniqueIdentifier).//
				    collect(Collectors.toList()),

			    false);

		    GSLoggerFactory.getLogger(getClass()).debug("Updating sources to data folder map ENDED");

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error("Error occurred while updating sources to data folder map cache");

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }
	}
    }

    /**
     *
     */
    public OpenSearchFinder() {

    }

    @Override
    public void setDatabase(Database database) {

	this.database = (OpenSearchDatabase) database;
	this.wrapper = new OpenSearchWrapper(this.database);

	if (MAP_UPDATER_TASK == null) {

	    MAP_UPDATER_TASK = new CachedMapUpdater(this.database, wrapper);

	    Timer timer = new Timer();
	    timer.scheduleAtFixedRate(MAP_UPDATER_TASK, TimeUnit.MINUTES.toMillis(10), MAP_UPDATE_PERIOD);
	}
    }

    @Override
    public OpenSearchDatabase getDatabase() {

	return database;
    }

    @Override
    public boolean supports(StorageInfo info) {

	return OpenSearchDatabase.isSupported(info);
    }

    @Override
    public DiscoveryCountResponse count(DiscoveryMessage message) throws GSException {

	// debugQueries = true;

	SearchResponse<Object> searchResponse = search_(message, true);

	Map<String, Aggregate> aggregations = searchResponse.aggregations();

	int total;

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

	    setTermFrequencyMap(searchResponse, response);
	}

	return response;
    }

    @Override
    public ResultSet<GSResource> discover(DiscoveryMessage message) throws GSException {

	// debugQueries = true;

	ResultSet<GSResource> resultSet = new ResultSet<>();
	List<GSResource> resources;

	try {

	    if (message.getDistinctValuesElement().isPresent()) {

		Query query = buildQuery(message, false);

		List<Queryable> queryables = message.getResourceSelector().getIndexesQueryables();

		resources = wrapper.aggregateWithNestedAgg(//

			query, //
			queryables.stream().map(Queryable::getName).collect(Collectors.toList()), //
			message.getDistinctValuesElement().get(), //
			message.getPage().getSize(), //
			message.isResourceBinaryExcluded(), true).

			stream().//
			map(s -> OpenSearchUtils.toGSResource(s).orElse(null)).//
			filter(Objects::nonNull).//
			collect(Collectors.toList());

		if (message.isCountInRetrievalIncluded()) {

		    CountSet countSet = new CountSet();

		    DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

		    updateCountSet(countSet, countResponse, message, resources.size());

		    countSet.addCountPair(new SimpleEntry<>(Type.DATABASE.toString(), countResponse));

		    resultSet.setCountResponse(countSet);
		}

	    } else {

		SearchResponse<Object> response = search_(message, false);

		PerformanceLogger pl = new PerformanceLogger(//
			PerformanceLogger.PerformancePhase.OPENSEARCH_FINDER_RESOURCES_CREATION, //
			message.getRequestId(), //
			Optional.ofNullable(message.getWebRequest()));

		resources = OpenSearchUtils.toGSResourcesList(response);

		pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

		//
		// set the search after, if present
		//

		OpenSearchUtils.getSearchAfter(response).ifPresent(resultSet::setSearchAfter);

		//
		// handles the tf targets and the count
		//

		if (!message.getTermFrequencyTargets().isEmpty() || message.isCountInRetrievalIncluded()) {

		    CountSet countSet = new CountSet();

		    DiscoveryCountResponse countResponse = new DiscoveryCountResponse();

		    if (message.isCountInRetrievalIncluded()) {

			updateCountSet(countSet, countResponse, message, (int) response.hits().total().value());

		    }

		    if (!message.getTermFrequencyTargets().isEmpty()) {

			setTermFrequencyMap(response, countResponse);
		    }

		    countSet.addCountPair(new SimpleEntry<>(Type.DATABASE.toString(), countResponse));

		    resultSet.setCountResponse(countSet);
		}

		//
		// handles the optional bbox union
		//

		if (message.isBboxUnionIncluded()) {

		    setBboxUnion(response, resultSet);
		}
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

	} catch (OpenSearchException osex) {

	    throw createGSException(osex, "OpenSearchFinderDiscoverError");

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscoverError", ex);
	}
    }

    /**
     * @param response
     * @param resultSet
     */
    private void setBboxUnion(SearchResponse<Object> response, ResultSet<GSResource> resultSet) {

	Aggregate agg = response.aggregations().get(OpenSearchWrapper.BBOX_AGG);

	GeoBoundsAggregate geoBound = (agg != null && agg.isGeoBounds()) ? agg.geoBounds() : null;

	if (geoBound != null && geoBound.bounds() != null) {

	    try {

		ComputationResult result = new ComputationResult();
		result.setTarget(OpenSearchWrapper.BBOX_AGG);

		GeographicBoundingBox bbox = new GeographicBoundingBox();

		TopLeftBottomRightGeoBounds tlbr = geoBound.bounds().tlbr();

		bbox.setBigDecimalSouth(new BigDecimal(tlbr.bottomRight().latlon().lat()));
		bbox.setBigDecimalWest(new BigDecimal(tlbr.topLeft().latlon().lon()));

		bbox.setBigDecimalNorth(new BigDecimal(tlbr.topLeft().latlon().lat()));
		bbox.setBigDecimalEast(new BigDecimal(tlbr.bottomRight().latlon().lon()));

		resultSet.setBBoxUnion(bbox);

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	}
    }

    /**
     * @param countSet
     * @param countResponse
     * @param message
     * @param count
     */
    private void updateCountSet(CountSet countSet, DiscoveryCountResponse countResponse, DiscoveryMessage message, int count) {

	countResponse.setCount(count);

	int pageCount = (int) (countResponse.getCount() < message.getPage().getSize()
		? 1
		: Math.ceil(((double) countResponse.getCount() / message.getPage().getSize())));

	countSet.setPageCount(countResponse.getCount() == 0 || message.getPage().getSize() == 0 ? 0 : pageCount);

	int pageIndex = message.getPage().getSize() == 0
		? 0
		: message.getPage().getStart() <= message.getPage().getSize()
			? 1
			: (message.getPage().getStart() / message.getPage().getSize()) + 1;

	countSet.setPageIndex(pageIndex);
    }

    /*
     * +
     */
    private void setTermFrequencyMap(SearchResult<Object> response, DiscoveryCountResponse discoveryCountResponse) {

	Map<String, Aggregate> aggregations = response.aggregations();

	HashMap<String, Aggregate> clone = new HashMap<>();

	aggregations.keySet().stream().filter(key -> !key.equals(OpenSearchWrapper.BBOX_AGG)).forEach(key ->

		clone.put(key, aggregations.get(key)));

	TermFrequencyMapType mapType = OpenSearchUtils.fromAgg(clone);

	TermFrequencyMap tfMap = new TermFrequencyMap(mapType);

	discoveryCountResponse.setTermFrequencyMap(tfMap);
    }

    @Override
    public ResultSet<Node> discoverNodes(DiscoveryMessage message) throws GSException {

	ResultSet<Node> out = new ResultSet<>();

	ResultSet<GSResource> response = discover(message);

	out.setCountResponse(response.getCountResponse());

	if (response.getProfilerName().isPresent()) {

	    out.setProfilerName(response.getProfilerName().get());
	}

	out.setPropertyHandler(response.getPropertyHandler());

	if (response.getSearchAfter().isPresent()) {

	    out.setSearchAfter(response.getSearchAfter().get());
	}

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
    }

    @Override
    public ResultSet<String> discoverStrings(DiscoveryMessage message) throws GSException {

	ResultSet<String> out = new ResultSet<>();

	ResultSet<GSResource> response = discover(message);

	out.setCountResponse(response.getCountResponse());

	if (response.getProfilerName().isPresent()) {

	    out.setProfilerName(response.getProfilerName().get());
	}

	out.setPropertyHandler(response.getPropertyHandler());

	if (response.getSearchAfter().isPresent()) {

	    out.setSearchAfter(response.getSearchAfter().get());
	}

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
			identifiers.getFirst());//
	    }
	}

	HashMap<String, String> map = getSourcesDataMap(message);

	DiscoveryBondParser bondParser = new DiscoveryBondParser(message.getPermittedBond());

	OpenSearchBondHandler handler = new OpenSearchBondHandler(wrapper, message, map, count);

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
     * Returns couples [source id -> query folder ] only of the sources that are currently harvested, or more in general, of the sources
     * that have a writing folder (for example a source in its first harvesting, or a source with interrupted harvesting having both data-1
     * and data-2 folder where one of them is the writing folder and the other is the query folder)
     *
     * @param sourceIds
     * @param useCache
     * @return
     * @throws GSException
     */
    private static HashMap<String, String> getSourcesDataMap(//
	    OpenSearchDatabase database, //
	    OpenSearchWrapper wrapper, //
	    List<String> sourceIds, //
	    boolean useCache) throws GSException {

	HashMap<String, String> out = new HashMap<>();

	if (useCache && !SOURCES_DATA_FOLDER_MAP.isEmpty()) {

	    synchronized (MAP_UPDATER_TASK) {

		SOURCES_DATA_FOLDER_MAP.keySet().//
			stream().//
			filter(sourceIds::contains).//
			forEach(id -> out.put(id, SOURCES_DATA_FOLDER_MAP.get(id)));

	    }

	} else if (!sourceIds.isEmpty()) {

	    Query query = OpenSearchQueryBuilder.buildDataFolderQuery(database.getIdentifier(), sourceIds);

	    try {

		List<JSONObject> aggregateWithNestedAgg = wrapper.aggregateWithNestedAgg(//
			query, //
			Arrays.asList(ResourceProperty.SOURCE_ID.getName(), MetaFolderMapping.DATA_FOLDER), //
			ResourceProperty.SOURCE_ID, //
			sourceIds.size(), //
			true, // binaries excluded
			OpenSearchDatabase.debugQueries()//
		);

		List<String> incrementalSourceIds = ConfigurationWrapper.//
			getIncrementalSources().//
			stream().//
			map(GSSource::getUniqueIdentifier).//
			toList();

		List<JSONObject> incrementalExcluded = aggregateWithNestedAgg.//
			stream().//
			filter(v -> !incrementalSourceIds.contains(v.getString(MetaFolderMapping.SOURCE_ID))).//
			toList();

		incrementalExcluded.forEach(agg -> {

		    String writingFolder = agg.getString(MetaFolderMapping.DATA_FOLDER);
		    // query folder is opposite of the writing folder
		    String queryFolder = writingFolder.equals(SourceStorageWorker.DATA_1_SHORT_POSTFIX) //
			    ? SourceStorageWorker.DATA_2_SHORT_POSTFIX //
			    : SourceStorageWorker.DATA_1_SHORT_POSTFIX;

		    out.put(agg.getString(MetaFolderMapping.SOURCE_ID), queryFolder);
		});

	    } catch (OpenSearchException osex) {

		throw createGSException(osex, "OpenSearchFinderSourceDataFolderMapError");

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(OpenSearchFinder.class).error(ex);
		throw GSException.createException(OpenSearchFinder.class, "OpenSearchFinderSourceDataFolderMapError", ex);
	    }
	}

	return out;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private HashMap<String, String> getSourcesDataMap(RequestMessage message) throws GSException {

	if (message.getSources().isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).error("Missing sources in message {}", message.getRequestId());
	    return new HashMap<>();
	}

	List<GSSource> sources;

	Optional<View> view = message.getView();
	if (view.isPresent()) {

	    sources = ConfigurationWrapper.getViewSources(view.get());

	} else {

	    sources = message.getSources();
	}

	return getSourcesDataMap(//
		database, //
		wrapper, //
		sources.stream().//
			map(GSSource::getUniqueIdentifier).//
			collect(Collectors.toList()), //
		message.isCachedSourcesDataFolderMapUsed());
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private SearchResponse<Object> search_(DiscoveryMessage message, boolean count) throws GSException {

	PerformanceLogger pl;

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

	    if (OpenSearchDatabase.debugQueries()) {

		GSLoggerFactory.getLogger(getClass()).debug(count ? "\n\n--- COUNT ---\n" : "\n\n--- DISCOVER ---\n");
	    }

	    SearchResponse<Object> response;

	    if (count) {

		response = wrapper.count(query, message);

	    } else {

		response = wrapper.search(//
			DataFolderMapping.get().getIndex(), // index
			query, // search query
			message, false // request cache
		);
	    }

	    pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

	    return response;

	} catch (OpenSearchException osex) {

	    throw createGSException(osex, "OpenSearchFinderDiscover_Error");

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(ex);

	    throw GSException.createException(getClass(), "OpenSearchFinderDiscover_Error", ex);
	}
    }

    /**
     * @param osex
     * @throws GSException
     */
    private static GSException createGSException(OpenSearchException osex, String errorType) throws GSException {

	ErrorCause error = osex.error();
	String jsonString = error.toJsonString();

	GSLoggerFactory.getLogger(OpenSearchDatabase.class).error(new JSONObject(jsonString).toString(3));

	return GSException.createException(OpenSearchFinder.class, errorType, osex);
    }

}
