package eu.essi_lab.profiler.os.handler.discover.eiffel;

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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.eiffel.api.DefaultEiffelAPI;
import eu.essi_lab.eiffel.api.EiffelAPI;
import eu.essi_lab.eiffel.api.EiffelAPI.SearchIdentifiersApi;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.DiscoveryMessage.EiffelAPIDiscoveryOption;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.UserBondMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.wrt.WebRequestParameter;
import eu.essi_lab.profiler.os.OSParameter;
import eu.essi_lab.profiler.os.OSParameters;
import eu.essi_lab.profiler.os.OSRequestParser;
import eu.essi_lab.profiler.os.handler.discover.eiffel.EiffelDiscoveryHelper.CountSetWrapper;
import eu.essi_lab.request.executor.discover.QueryInitializer;

/**
 * @author Fabrizio
 * @param <String>
 */
public class EiffelDiscoveryHandler extends DiscoveryHandler<String> {

    /**
     * 
     */
    private static final HashMap<String, List<String>> FILTER_AND_SORT_IDS_TO_SORT_MAP = new HashMap<>();

    /**
     * 
     */
    private static final HashMap<String, List<String>> SORT_AND_FILTER_MERGED_IDS_MAP = new HashMap<>();

    /**
     * 
     */
    private static final HashMap<String, List<String>> SORTED_IDS_MAP_SEARCH_API = new HashMap<String, List<String>>();

    /**
     * 
     */
    private static final HashMap<String, List<String>> SORTED_IDS_MAP_FILTER_API = new HashMap<String, List<String>>();

    public EiffelDiscoveryHandler() {

	super();
    }

    @Override
    public Response handleMessageRequest(DiscoveryMessage message) throws GSException {

	String rid = message.getWebRequest().getRequestId();

	Optional<WebRequest> owr = Optional.ofNullable(message.getWebRequest());

	GSLoggerFactory.getLogger(getClass()).info("[2/2] Message authorization check STARTED");
	PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MESSAGE_AUTHORIZATION, rid, owr);

	boolean authorized = getExecutor().isAuthorized(message);

	pl.logPerformance(GSLoggerFactory.getLogger(getClass()));
	GSLoggerFactory.getLogger(getClass()).info("[2/2] Message authorization check ENDED");

	GSLoggerFactory.getLogger(getClass()).info("Message authorization {}", (authorized ? "approved" : "denied"));

	if (!authorized) {

	    return handleNotAuthorizedRequest((DiscoveryMessage) message);
	}

	return handleEiffelAPIDiscovery(message);
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private Response handleEiffelAPIDiscovery(DiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Handling Eiffel API discovery query STARTED");

	//
	// this is for test purpose
	//
	message.setRequestTimeout(1200);

	//
	// 2) get the mandatory search terms, if missing returns an empty response
	//

	Optional<String> queryString = message.getWebRequest().getFormData();

	KeyValueParser keyValueParser = new KeyValueParser(queryString.get());
	OSRequestParser parser = new OSRequestParser(keyValueParser);

	String searchTerms = parser.parse(OSParameters.SEARCH_TERMS);

	if (searchTerms == null || searchTerms.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).warn("No search terms provided!");

	    return createEmptyResponse(message);
	}

	EiffelAPIDiscoveryOption option = message.getEiffelAPIDiscoveryOption().get();

	Response response = null;
	switch (option) {
	case SORT_AND_FILTER:

	    SearchIdentifiersApi sortAndFilterApiOption = EiffelDiscoveryHelper.getSortAndFilterApiOption();

	    switch (sortAndFilterApiOption) {
	    case FILTER:

		GSLoggerFactory.getLogger(getClass()).info("Executing 'sort and filter' option using filter API STARTED");

		response = handleSortAndFilterOptionWithFilterApi(searchTerms, message);

		GSLoggerFactory.getLogger(getClass()).info("Executing 'sort and filter' option using filter API ENDED");

		break;

	    case SEARCH:

		GSLoggerFactory.getLogger(getClass()).info("Executing 'sort and filter' option using search API STARTED");

		response = handleSortAndFilterOptionWithSearchApi(searchTerms, message, null);

		GSLoggerFactory.getLogger(getClass()).info("Executing 'sort and filter' option using search API ENDED");

		break;
	    }

	    break;

	case FILTER_AND_SORT:

	    //
	    // 1) first executes the user query. in case of 0 results, the process ends
	    //

	    GSLoggerFactory.getLogger(getClass()).info("Executing user query STARTED");

	    ResultSet<GSResource> userQueryResponse = getExecutor().retrieve(message);

	    CountSet userQueryCountSet = userQueryResponse.getCountResponse();

	    GSLoggerFactory.getLogger(getClass()).info("Executing user query ENDED");

	    if (userQueryCountSet.getCount() == 0) {

		GSLoggerFactory.getLogger(getClass()).info("User query has 0 results, nothing else to do");

		ResultSet<String> mappedResponse = getMessageResponseMapper().map(message, userQueryResponse);

		response = getMessageResponseFormatter().format(message, mappedResponse);

		publish(message, mappedResponse);

		GSLoggerFactory.getLogger(getClass()).info("Handling Eiffel API discovery query ENDED");

		return response;
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Executing 'filter and sort' option STARTED");

	    response = handleFilterAndSortOption(searchTerms, message, userQueryCountSet);

	    GSLoggerFactory.getLogger(getClass()).info("Executing 'filter and sort' option ENDED");

	    break;
	}

	return response;
    }

    /**
     * @param searchTerms
     * @param message
     * @return
     * @throws GSException
     */
    private Response handleSortAndFilterOptionWithFilterApi(//
	    String searchTerms, //
	    DiscoveryMessage message //

    ) throws GSException {

	//
	// 1) since no query is performed with this message at this point, the message
	// must be initialized (it usually happen in the distributor) in order to add the user bond, normalized bond,
	// ...
	//

	QueryInitializer queryInitializer = new QueryInitializer();
	queryInitializer.initializeQuery(message);

	//
	// 2) retrieves all the available identifiers from the Eiffel Search API which are globally
	// sorted according to the given search terms
	// this API returns a max. of 10.000 records, also according to the value of the treshold parameter
	//

	List<String> sortedIds = SORTED_IDS_MAP_FILTER_API.get(searchTerms);

	if (sortedIds == null) {

	    sortedIds = searchIdentifiers(searchTerms, SearchIdentifiersApi.FILTER);

	    sortedIds = EiffelDiscoveryHelper.addEiffelPrefixId(sortedIds);

	    boolean useCache = EiffelDiscoveryHelper.useEiffelFilterApiCache();

	    if (useCache) {

		SORTED_IDS_MAP_FILTER_API.put(searchTerms, sortedIds);
	    }

	} else {

	    GSLoggerFactory.getLogger(getClass()).info("Using cached sorted ids");
	}

	GSLoggerFactory.getLogger(getClass()).info("Total number of retrieved sorted ids: {}", sortedIds.size());

	//
	// 3) merges the user constraints (deprived of the search terms) with the sorted ids
	//

	GSLoggerFactory.getLogger(getClass()).info("Merging bond STARTED");

	Bond idsBond = EiffelDiscoveryHelper.getIdsBond(sortedIds);

	EiffelDiscoveryHelper.mergeBonds(message, idsBond);

	GSLoggerFactory.getLogger(getClass()).info("Merging bond ENDED");

	//
	// 4) retrieves the merged records ids
	//

	String key = getKey(message);

	List<String> mergedRecordsIds = SORT_AND_FILTER_MERGED_IDS_MAP.get(key);

	if (mergedRecordsIds == null) {

	    GSLoggerFactory.getLogger(getClass()).info("Executing merged query for identifers STARTED");

	    DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(ConfigurationWrapper.getDatabaseURI());

	    mergedRecordsIds = executor.getIndexValues(message, MetadataElement.IDENTIFIER, 0, EiffelAPI.DEFAULT_MAX_SORT_IDENTIFIERS);

	    boolean useCache = EiffelDiscoveryHelper.useEiffelMergedIdsCache();

	    if (useCache) {

		SORT_AND_FILTER_MERGED_IDS_MAP.put(key, mergedRecordsIds);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Executing merged query for identifers ENDED");

	} else {

	    GSLoggerFactory.getLogger(getClass()).info("Using cached merged query identifers");
	}

	GSLoggerFactory.getLogger(getClass()).info("Matched results of merged query: {}", mergedRecordsIds.size());

	//
	// 5) removes from the sorted ids list all the elements that are not contained in the merged records ids list.
	// ideally this should be not necessary, but we still miss some records
	//

	GSLoggerFactory.getLogger(getClass()).info("Computing retained records STARTED");

	ArrayList<String> retainedSortedIds = new ArrayList<>(sortedIds);

	retainedSortedIds.retainAll(mergedRecordsIds);

	GSLoggerFactory.getLogger(getClass()).info("Missing records: {}", (sortedIds.size() - retainedSortedIds.size()));

	GSLoggerFactory.getLogger(getClass()).info("Computing retained records ENDED");

	if (retainedSortedIds.isEmpty()) {

	    return createEmptyResponse(message);
	}

	//
	// 6) updates the user bond with the retained sorted ids, according to the user pagination
	//

	int start = message.getPage().getStart() - 1;
	int end = Math.min(retainedSortedIds.size(), start + message.getPage().getSize());

	idsBond = EiffelDiscoveryHelper.getIdsBond(retainedSortedIds.subList(start, end));

	message.setUserBond(idsBond);

	//
	// 7) executes the sorted ids query
	//

	Page originalPage = message.getPage();

	// page must always start from index 1 since pagination is handled in the retained sorted ids list
	message.setPage(new Page(1, message.getPage().getSize()));

	GSLoggerFactory.getLogger(getClass()).info("Executing sorted ids query STARTED");

	ResultSet<GSResource> mergedExecutorResponse = getExecutor().retrieve(message);

	GSLoggerFactory.getLogger(getClass()).info("Executing  sorted ids  query ENDED");

	//
	// 8) maps and sorts the returned records
	//

	GSLoggerFactory.getLogger(getClass()).info("Mapping records STARTED");

	ResultSet<String> mappedMergedResponse = ((EiffelMapper) getMessageResponseMapper()).map(//
		message, //
		mergedExecutorResponse, //
		retainedSortedIds);

	List<String> mappedMergedResultsList = mappedMergedResponse.getResultsList();

	GSLoggerFactory.getLogger(getClass()).info("Mapping records ENDED");

	//
	// 9) creates a new count set according to the actual number of available records
	//

	int actualPageCount = (int) (Math.ceil(((double) retainedSortedIds.size() / originalPage.getSize())));

	int pageIndex = (originalPage.getStart() + originalPage.getSize() - 1) / originalPage.getSize();

	CountSetWrapper countSetWrapper = new CountSetWrapper(//
		mergedExecutorResponse.getCountResponse(), //
		retainedSortedIds.size(), //
		actualPageCount, //
		pageIndex);

	//
	// 11) returns the response using the merged count response
	//
	return publishResponse(//
		message, //
		mappedMergedResponse, //
		mappedMergedResultsList, //
		countSetWrapper);
    }

    /**
     * @param searchTerms
     * @param message
     * @param userQueryResultSet
     * @return
     * @throws GSException
     */
    private Response handleSortAndFilterOptionWithSearchApi(//
	    String searchTerms, //
	    DiscoveryMessage message, //
	    CountSet userQueryCountSet) throws GSException {

	ResultSet<String> mappedMergedResponse = null;

	List<String> retrievedResultsList = new ArrayList<>();

	//
	//
	//

	int userQueryCount = userQueryCountSet.getCount();

	GSLoggerFactory.getLogger(getClass()).info("User query count: {}", userQueryCount);

	int targetResults = Math.min(//
		message.getPage().getSize(), //
		userQueryCount - (message.getPage().getStart() - 1));

	GSLoggerFactory.getLogger(getClass()).info("Target number of records to retrieve: {}", targetResults);

	//
	// 1) get a clone of the original user bond
	//
	Optional<Bond> originalUserBond = Optional.empty();

	if (message instanceof UserBondMessage) {

	    Optional<Bond> userBond = ((UserBondMessage) message).getUserBond();
	    if (userBond.isPresent()) {

		originalUserBond = Optional.of(userBond.get().clone());
	    }
	}

	//
	// 2) retrieves all the available identifiers from the Eiffel Search API which are sorted according to the
	// given search terms and split them in several lists of a fixed number of elements
	//

	List<String> sortedIds = SORTED_IDS_MAP_SEARCH_API.get(searchTerms);

	if (sortedIds == null) {

	    sortedIds = searchIdentifiers(searchTerms, SearchIdentifiersApi.SEARCH);

	    sortedIds = EiffelDiscoveryHelper.addEiffelPrefixId(sortedIds);

	    SORTED_IDS_MAP_SEARCH_API.put(searchTerms, sortedIds);
	}

	if (sortedIds.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).warn("No API ids retrieved, exit!");

	    return createEmptyResponse(message);
	}

	final int MAX_PARTITION_SIZE = EiffelDiscoveryHelper.getSortAndFilterIdsPartitionSize();

	List<List<String>> idsParition = Lists.partition(sortedIds, MAX_PARTITION_SIZE);

	GSLoggerFactory.getLogger(getClass()).info("Total number of retrieved sorted ids: {}", sortedIds.size());

	GSLoggerFactory.getLogger(getClass()).info("There are {} partitions with a max. of {} ids", idsParition.size(), MAX_PARTITION_SIZE);

	int idsPartionsIndex = 0;

	int totalMergedCount = 0;

	int userPageStart = message.getPage().getStart();

	//
	// 3) loops until the requested number of records (e.g.: 10) are retrieved
	//

	while (retrievedResultsList.size() < targetResults && idsPartionsIndex < idsParition.size()) {

	    //
	    // 3.1) set the original user bond to avoid that
	    // QueryInitializer recursively add user bond at each iteration generating extremely long queries
	    //

	    ((UserBondMessage) message).setUserBond(originalUserBond.orElse(null));

	    //
	    // 3.3) merges the user constraints (deprived of the search terms) with the current partition of sorted ids
	    //

	    List<String> currentIdsPartion = idsParition.get(idsPartionsIndex);

	    Bond idsBond = EiffelDiscoveryHelper.getIdsBond(currentIdsPartion);

	    EiffelDiscoveryHelper.mergeBonds(message, idsBond);

	    //
	    // 3.4) executes the merged query
	    //

	    GSLoggerFactory.getLogger(getClass()).info("Executing merged query STARTED");

	    ResultSet<GSResource> mergedExecutorResponse = getExecutor().retrieve(message);

	    GSLoggerFactory.getLogger(getClass()).info("Executing merged query ENDED");

	    int mergedCount = mergedExecutorResponse.getCountResponse().getCount();

	    totalMergedCount += mergedCount;

	    GSLoggerFactory.getLogger(getClass()).info("Matched results of merged query: {}", mergedCount);

	    //
	    // 3.5) maps and sorts according to the current partial list of global sorted ids the returned records
	    //

	    mappedMergedResponse = ((EiffelMapper) getMessageResponseMapper()).map(//
		    message, //
		    mergedExecutorResponse, //
		    currentIdsPartion);

	    List<String> mappedMergedResultsList = mappedMergedResponse.getResultsList();

	    //
	    // the returned records are kept only if the page start index is less then the total number of
	    // matched record, otherwise they are skipped since the requested ones are in the next pages
	    //

	    boolean keepRecords = userPageStart < totalMergedCount;

	    if (!keepRecords) {

		GSLoggerFactory.getLogger(getClass()).warn("Requested records are in the next pages, skipping current result set");
	    }

	    //
	    // if the merged query has some results
	    //
	    if (!mappedMergedResultsList.isEmpty() && keepRecords) {

		retrievedResultsList.addAll(mappedMergedResultsList);

		if (retrievedResultsList.size() > targetResults) {

		    retrievedResultsList = retrievedResultsList.subList(0, targetResults);
		}
	    }

	    //
	    // 4) if the required number records are retrieved, the procedure ends otherwise it goes on with another
	    // partition of identifiers from the Eiffel Search API
	    //

	    GSLoggerFactory.getLogger(getClass()).info("Total records retrieved: [{}/{}]", retrievedResultsList.size(), targetResults);

	    if (retrievedResultsList.size() >= targetResults) {

		GSLoggerFactory.getLogger(getClass()).info("Target number of records reached");

	    } else {

		// updates index for the next ids partition
		idsPartionsIndex++;

		// overrides the page start index according to the total number of merged results
		int startIndex = userPageStart - totalMergedCount;

		if (startIndex < 0) {

		    GSLoggerFactory.getLogger(getClass()).info("Pagination limit reached, exit");

		    break;
		}

		message.getPage().setStart(startIndex);

		GSLoggerFactory.getLogger(getClass()).info("Missing records to retrieve: {}",
			(targetResults - retrievedResultsList.size()));

		GSLoggerFactory.getLogger(getClass()).info("Ids partitions index updated: [{}/{}]", idsPartionsIndex, idsParition.size());
	    }
	}

	if (idsPartionsIndex >= idsParition.size()) {

	    GSLoggerFactory.getLogger(getClass()).warn("All available ids elaborated, exit!");
	}

	return publishResponse(//
		message, //
		mappedMergedResponse, //
		retrievedResultsList, //
		userQueryCountSet);
    }

    /**
     * @param message
     * @return
     */
    private String getKey(DiscoveryMessage message) {

	String key = "";

	String queryString = message.getWebRequest().getFormData().get();

	KeyValueParser keyValueParser = new KeyValueParser(queryString);
	OSRequestParser parser = new OSRequestParser(keyValueParser);

	List<OSParameter> parameters = WebRequestParameter.findParameters(OSParameters.class);

	for (OSParameter osParameter : parameters) {

	    if (osParameter != OSParameters.START_INDEX && //
		    osParameter != OSParameters.COUNT && //
		    osParameter != OSParameters.SOURCES && //
		    osParameter != OSParameters.SEARCH_FIELDS && //
		    osParameter != OSParameters.TERM_FREQUENCY && //
		    osParameter != OSParameters.OUTPUT_FORMAT && //
		    osParameter != OSParameters.OUTPUT_VERSION && //
		    osParameter != OSParameters.EVENT_ORDER) {

		String parsed = parser.parse(osParameter);
		if (parsed != null) {

		    key += parsed;
		}
	    }
	}

	return key;
    }

    /**
     * @param searchTerms
     * @param searchMode
     * @return
     * @throws GSException
     */
    private List<String> searchIdentifiers(String searchTerms, SearchIdentifiersApi searchMode) throws GSException {

	int pagesCount = 0;
	int page = 1;

	List<String> sortedIds = new ArrayList<>();

	do {

	    GSLoggerFactory.getLogger(getClass()).info("Retrieving sorted ids from page [{}/{}] STARTED", page,
		    pagesCount == 0 ? "?" : pagesCount);

	    DefaultEiffelAPI api = new DefaultEiffelAPI();

	    SimpleEntry<List<String>, Integer> apiResponse = api.searchIdentifiers(//
		    searchMode, //
		    searchTerms, //
		    page, //
		    EiffelAPI.DEFAULT_MAX_RECORDS_PER_PAGE);

	    List<String> reponseIds = apiResponse.getKey();

	    GSLoggerFactory.getLogger(getClass()).info("Actual number of sorted API ids from page {}: {}", page, reponseIds.size());

	    sortedIds.addAll(reponseIds);

	    if (pagesCount == 0) {

		pagesCount = apiResponse.getValue();

		GSLoggerFactory.getLogger(getClass()).info("Total number of pages: {}", pagesCount);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Retrieving sorted ids from page [{}/{}] ENDED", page, pagesCount);

	    page++;

	} while (page <= pagesCount);

	return sortedIds;
    }

    /**
     * @param searchTerms
     * @param message
     * @param userQueryCountSet
     * @return
     * @throws GSException
     */
    private Response handleFilterAndSortOption(//
	    String searchTerms, //
	    DiscoveryMessage message, //
	    CountSet userQueryCountSet) throws GSException {

	//
	// 1) updates the user query count, according to the maximum number of ids that can be
	// sorted by the Eiffel API
	//

	int userQueryCount = userQueryCountSet.getCount();

	GSLoggerFactory.getLogger(getClass()).info("User query count: {}", userQueryCount);

	if (userQueryCount > EiffelDiscoveryHelper.getMaxSortIdentifiers()) {

	    userQueryCount = EiffelDiscoveryHelper.getMaxSortIdentifiers();

	    GSLoggerFactory.getLogger(getClass()).info("User query count reduced to the maximum number or sortable ids: {}",
		    userQueryCount);
	}

	//
	// 2) since the user query count can be less than the original one, also the pagination start must be
	// possibly reduced, according to this new value
	//

	int messagePageStart = message.getPage().getStart() > userQueryCount ? userQueryCount : message.getPage().getStart();

	//
	// 3) if the user count set is greater then the maximum number of ids that can be
	// sorted by the Eiffel API, overrides the count set
	//
	CountSet countSet = userQueryCountSet;

	if (userQueryCountSet.getCount() > EiffelDiscoveryHelper.getMaxSortIdentifiers()) {

	    int actualPageCount = (int) (Math.ceil(((double) userQueryCount / message.getPage().getSize())));

	    countSet = new CountSetWrapper(//
		    userQueryCountSet, //
		    userQueryCount, //
		    actualPageCount, //
		    messagePageStart);

	}

	//
	// 4) retrieves the ids of all the results of the user query
	//

	String key = getKey(message);

	List<String> recordsIds = FILTER_AND_SORT_IDS_TO_SORT_MAP.get(key);

	if (recordsIds == null) {

	    GSLoggerFactory.getLogger(getClass()).info("[1/4] Retrieving user query records ids STARTED");

	    DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(ConfigurationWrapper.getDatabaseURI());

	    recordsIds = executor.getIndexValues(message, MetadataElement.IDENTIFIER, 0, EiffelAPI.DEFAULT_MAX_SORT_IDENTIFIERS);

	    recordsIds = EiffelDiscoveryHelper.removeEiffelIdPrefix(recordsIds);

	    GSLoggerFactory.getLogger(getClass()).info("[1/4] Retrieving user query records ids ENDED");

	    FILTER_AND_SORT_IDS_TO_SORT_MAP.put(key, recordsIds);

	} else {

	    GSLoggerFactory.getLogger(getClass()).info("Using cached identifiers");
	}

	//
	// 5) let the Eiffel API sort the identifiers according to the given search terms
	//

	GSLoggerFactory.getLogger(getClass()).info("[2/4] Sorting of {} identifiers with Eiffel API STARTED", userQueryCount);

	DefaultEiffelAPI api = new DefaultEiffelAPI();

	//
	//
	//

	List<String> sortedIds = api.sortIdentifiers(searchTerms, recordsIds);

	GSLoggerFactory.getLogger(getClass()).info("[2/4] Sorting of {} identifiers with Eiffel API ENDED", userQueryCount);

	//
	// 7) makes a sublist of the sorted ids according to the user query pagination and retrieves
	// the records with such identifiers
	//

	int end = Math.min(userQueryCount, messagePageStart + message.getPage().getSize() - 1);

	GSLoggerFactory.getLogger(getClass()).info("[3/4] Retrieving records {}/{} from sorted identifiers STARTED", messagePageStart, end);

	sortedIds = sortedIds.subList(messagePageStart - 1, end);

	GSLoggerFactory.getLogger(getClass()).info("Number of sorted ids after subsetting: {}", sortedIds.size());

	//
	// 8)
	//

	sortedIds = EiffelDiscoveryHelper.addEiffelPrefixId(sortedIds);

	//
	// 9) converts the ids in ids bonds and prepares the page for the final query to the DAB
	//

	Bond sortedIdsBond = EiffelDiscoveryHelper.getIdsBond(sortedIds);

	message.setPage(new Page(1, sortedIds.size()));
	message.setUserBond(sortedIdsBond);

	//
	// 8) executes the query
	//

	ResultSet<GSResource> sortedIdsQueryResponse = getExecutor().retrieve(message);

	GSLoggerFactory.getLogger(getClass()).info("[3/4] Retrieving records {}/{} from sorted identifiers ENDED", messagePageStart, end);

	//
	// 9) finally maps and sorts the results according to the sorted ids list
	//

	GSLoggerFactory.getLogger(getClass()).info("[4/4] Mapping and sorting result set STARTED");

	ResultSet<String> mappedResponse = ((EiffelMapper) getMessageResponseMapper()).map(//
		message, //
		sortedIdsQueryResponse, //
		sortedIds);

	GSLoggerFactory.getLogger(getClass()).info("[4/4] Mapping and sorting result set ENDED");

	//
	// 10) returns the response
	//

	return publishResponse(//
		message, //
		mappedResponse, //
		mappedResponse.getResultsList(), //
		countSet);
    }

    /**
     * @param message
     * @param mappedMergedResponse
     * @param retrievedResultsList
     * @param userQueryResultSet
     * @return
     * @throws GSException
     */
    private Response publishResponse(//
	    DiscoveryMessage message, //
	    ResultSet<String> mappedMergedResponse, //
	    List<String> retrievedResultsList, //
	    CountSet userQueryCountSet) throws GSException {

	//
	// sets the complete sorted list to the response
	//
	mappedMergedResponse.setResultsList(retrievedResultsList);

	//
	// set the count response of the user query executor response
	//
	mappedMergedResponse.setCountResponse(userQueryCountSet);

	Response response = getMessageResponseFormatter().format(message, mappedMergedResponse);

	publish(message, mappedMergedResponse);

	onHandlingEnded(response);

	GSLoggerFactory.getLogger(getClass()).info("Handling Eiffel API discovery query ENDED");

	return response;
    }

    /**
     * @param mappedMergedResponse
     * @param message
     * @param retrievedResultsList
     * @return
     * @throws GSException
     */
    private Response createEmptyResponse(//
	    DiscoveryMessage message) throws GSException {

	List<String> sortedIds = new ArrayList<>();

	// forces an empty result set
	sortedIds.add("missingId1");
	sortedIds.add("missingId2");

	Bond idsBond = EiffelDiscoveryHelper.getIdsBond(sortedIds);

	((UserBondMessage) message).setUserBond(idsBond);

	ResultSet<GSResource> resultSet = getExecutor().retrieve(message);
	ResultSet<String> mappedResponse = getMessageResponseMapper().map(message, resultSet);

	return publishResponse(//
		message, //
		mappedResponse, //
		new ArrayList<>(), //
		mappedResponse.getCountResponse());
    }
}
