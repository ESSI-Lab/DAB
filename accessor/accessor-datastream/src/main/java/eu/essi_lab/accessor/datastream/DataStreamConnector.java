package eu.essi_lab.accessor.datastream;

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

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Connector for the DataStream OData API.
 *
 * It exposes one {@link OriginalMetadata} record for each dataset collection
 * (DataStream Metadata entry) and one for each time series, defined as the
 * combination of (dataset DOI, location, CharacteristicName).
 */
public class DataStreamConnector extends HarvestedQueryConnector<DataStreamConnectorSetting> {

    public static final String TYPE = "DataStreamConnector";

    private static final String DATASTREAM_READ_ERROR = "Unable to retrieve DataStream content";
    private static final String DATASTREAM_URL_NOT_FOUND_ERROR = "DATASTREAM_URL_NOT_FOUND_ERROR";
    /** Max locations per resumption token (one block per request). */
    private static final int LOCATION_BLOCK_SIZE = 100;

    private final Logger logger = GSLoggerFactory.getLogger(getClass());

    /** Cache of dataset collection identifiers (id, doi, name only), sorted alphabetically by DOI. */
    private List<DataStreamClient.DatasetMetadata> collectionIdCache;
    private int partialNumbers;
    private DataStreamClient client;

    public DataStreamConnector() {
	this.collectionIdCache = new ArrayList<>();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint != null && !endpoint.isEmpty();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	ensureCollectionCacheFilled();

	List<DataStreamClient.DatasetMetadata> cache = collectionIdCache;
	if (cache.isEmpty()) {
	    ret.setResumptionToken(null);
	    return ret;
	}

	TokenParts tokenParts = parseResumptionToken(request.getResumptionToken());
	int index;
	int locationOffset;
	if (tokenParts.doi == null || tokenParts.doi.isEmpty()) {
	    index = 0;
	    locationOffset = 0;
	} else {
	    int idx = findCollectionIndexByDoi(cache, tokenParts.doi);
	    if (idx < 0) {
		logger.warn("Resumption token '{}' not found in collection cache; starting from first collection", request.getResumptionToken());
		index = 0;
		locationOffset = 0;
	    } else {
		index = idx;
		locationOffset = tokenParts.locationOffset;
	    }
	}

	int totalCollections = cache.size();
	if (index >= totalCollections) {
	    ret.setResumptionToken(null);
	    logger.debug("DataStream listRecords completed. Total records emitted: {}", partialNumbers);
	    partialNumbers = 0;
	    return ret;
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	int maxRecords = (!getSetting().isMaxRecordsUnlimited() && mr.isPresent()) ? mr.get() : 0;
	int remainingBudget = maxRecords > 0 ? Math.max(0, maxRecords - partialNumbers) : 0;
	if (maxRecords > 0 && remainingBudget <= 0) {
	    ret.setResumptionToken(null);
	    partialNumbers = 0;
	    return ret;
	}

	String currentDoi = cache.get(index).doi;
	CollectionHarvestResult result = getOriginalMetadataForCollection(cache.get(index), remainingBudget, locationOffset,
		LOCATION_BLOCK_SIZE);

	int locationEnd = Math.min(locationOffset + LOCATION_BLOCK_SIZE, result.totalLocations);
	int currentNum = index + 1;
	double collectionPercent = totalCollections > 0 ? (currentNum * 100.0 / totalCollections) : 0;
	double locationPercent = result.totalLocations > 0 ? (locationEnd * 100.0 / result.totalLocations) : 0;
	logger.info("Processing collection {}/{} (DOI: {}), locations {}-{} of {} ({}% within collection), collections {}%",
		currentNum, totalCollections, currentDoi, locationOffset, locationEnd, result.totalLocations,
		String.format("%.1f", locationPercent), String.format("%.1f", collectionPercent));

	for (String om : result.records) {
	    if (om != null && !om.isEmpty()) {
		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(CommonNameSpaceContext.DATASTREAM_NS_URI);
		metadata.setMetadata(om);
		ret.addRecord(metadata);
		partialNumbers++;
		if (maxRecords > 0 && partialNumbers >= maxRecords) {
		    break;
		}
	    }
	}

	// Next token: same collection next location block (DOI:offset), or next collection (nextDoi:0), or null
	String nextToken = null;
	if (maxRecords <= 0 || partialNumbers < maxRecords) {
	    if (locationOffset + LOCATION_BLOCK_SIZE < result.totalLocations) {
		nextToken = currentDoi + ":" + (locationOffset + LOCATION_BLOCK_SIZE);
	    } else if (index + 1 < cache.size()) {
		nextToken = cache.get(index + 1).doi + ":0";
	    }
	}
	ret.setResumptionToken(nextToken);

	logger.debug("ADDED {} records for collection {} (DOI: {}), locations {}-{}. Next token: {}", result.records.size(),
		cache.get(index).id, currentDoi, locationOffset, locationEnd, nextToken);

	return ret;
    }

    private void ensureCollectionCacheFilled() throws GSException {
	if (!collectionIdCache.isEmpty()) {
	    return;
	}
	String endpoint = getSourceURL();
	if (endpoint == null || endpoint.isEmpty()) {
	    throw GSException.createException(//
		    this.getClass(), //
		    DATASTREAM_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATASTREAM_URL_NOT_FOUND_ERROR);
	}
	try {
	    String apiKey = getSetting().getApiKey();
	    this.client = new DataStreamClient(endpoint, apiKey);
	    List<DataStreamClient.DatasetMetadata> list = client.listDatasetIdentifiers(0);
	    list.removeIf(d -> d.doi == null || d.doi.isEmpty());
	    Collections.sort(list, Comparator.comparing(d -> d.doi != null ? d.doi : ""));
	    collectionIdCache = list;
	    logger.trace("DataStream collection cache filled with {} DOIs", collectionIdCache.size());
	} catch (IOException | InterruptedException e) {
	    logger.error("Error retrieving DataStream collection list", e);
	    throw GSException.createException(//
		    this.getClass(), //
		    DATASTREAM_READ_ERROR + ": " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATASTREAM_URL_NOT_FOUND_ERROR);
	}
    }

    private static int findCollectionIndexByDoi(List<DataStreamClient.DatasetMetadata> cache, String doi) {
	for (int i = 0; i < cache.size(); i++) {
	    if (doi.equals(cache.get(i).doi)) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Parses a resumption token into DOI and location offset.
     * Format: {@code DOI} (treated as offset 0) or {@code DOI:offset} (e.g. DOI1:1000, DOI1:2000).
     */
    private static TokenParts parseResumptionToken(String token) {
	TokenParts parts = new TokenParts();
	if (token == null || token.isEmpty()) {
	    parts.doi = null;
	    parts.locationOffset = 0;
	    return parts;
	}
	int colon = token.indexOf(':');
	if (colon < 0) {
	    parts.doi = token;
	    parts.locationOffset = 0;
	    return parts;
	}
	parts.doi = token.substring(0, colon);
	try {
	    parts.locationOffset = colon < token.length() - 1 ? Integer.parseInt(token.substring(colon + 1)) : 0;
	} catch (NumberFormatException e) {
	    parts.locationOffset = 0;
	}
	return parts;
    }

    private static class TokenParts {
	String doi;
	int locationOffset;
    }

    private static class CollectionHarvestResult {
	List<String> records = new ArrayList<>();
	int totalLocations;
    }

    /**
     * Fetches original metadata (collection record + time-series records) for a single dataset
     * collection, for a block of locations [locationOffset, locationOffset+locationLimit).
     *
     * @param remainingRecordBudget max records to add (0 = no limit). Used when maxRecords is set globally.
     * @param locationOffset        index of first location to process in this collection.
     * @param locationLimit         max number of locations to process in this block (e.g. 1000).
     */
    private CollectionHarvestResult getOriginalMetadataForCollection(DataStreamClient.DatasetMetadata collectionMinimal,
	    int remainingRecordBudget, int locationOffset, int locationLimit) throws GSException {

	CollectionHarvestResult result = new CollectionHarvestResult();
	try {
	    DataStreamClient.DatasetMetadata dataset = client.getMetadataByDoi(collectionMinimal.doi);
	    if (dataset == null || dataset.doi == null || dataset.doi.isEmpty()) {
		return result;
	    }

	    List<DataStreamClient.Location> locations = client.listLocationsByDoi(dataset.doi);
	    result.totalLocations = locations.size();

	    // Collection-level record only when starting from the first location block
	    if (locationOffset == 0) {
		JSONObject collectionJson = new JSONObject();
		collectionJson.put("type", "collection");
		collectionJson.put("doi", dataset.doi);
		collectionJson.put("datasetId", dataset.id);
		collectionJson.put("datasetName", dataset.name);
		collectionJson.put("metadata", dataset.raw);
		result.records.add(collectionJson.toString());
	    }

	    int emitted = result.records.size();
	    if (remainingRecordBudget > 0 && emitted >= remainingRecordBudget) {
		return result;
	    }

	    int fromIndex = Math.min(locationOffset, result.totalLocations);
	    int toIndex = Math.min(locationOffset + locationLimit, result.totalLocations);
	    List<DataStreamClient.Location> block = locations.subList(fromIndex, toIndex);
	    int observationSampleSet = getSetting().getObservationSampleSet();

	    LocalDate now = LocalDate.now();
	    LocalDate oneYearAgo = now.minusYears(1);
	    String defaultFirstDate = oneYearAgo.toString();
	    String defaultLastDate = now.toString();

	    for (DataStreamClient.Location location : block) {
		Map<String, String> characteristicNamesToUnits = client.getCharacteristicNamesWithUnits(dataset.doi, location.id,
			observationSampleSet);
		for (Map.Entry<String, String> entry : characteristicNamesToUnits.entrySet()) {
		    String cn = entry.getKey();
		    String unit = entry.getValue();
		    JSONObject seriesJson = new JSONObject();
		    seriesJson.put("type", "dataset");
		    seriesJson.put("doi", dataset.doi);
		    seriesJson.put("datasetId", dataset.id);
		    seriesJson.put("datasetName", dataset.name);
		    seriesJson.put("metadata", dataset.raw);
		    seriesJson.put("characteristicName", cn);
		    seriesJson.put("location", location.raw);
		    seriesJson.put("firstObservationDate", defaultFirstDate);
		    seriesJson.put("lastObservationDate", defaultLastDate);
		    if (unit != null && !unit.isEmpty()) {
			seriesJson.put("resultUnit", unit);
		    }

		    result.records.add(seriesJson.toString());
		    emitted++;
		    if (remainingRecordBudget > 0 && emitted >= remainingRecordBudget) {
			return result;
		    }
		}
	    }
	} catch (IOException | InterruptedException e) {
	    logger.warn("Skipping collection {} (DOI: {}) due to error: {}", collectionMinimal.id, collectionMinimal.doi,
		    e.getMessage(), e);
	}
	return result;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.DATASTREAM_NS_URI);
	return ret;
    }

    @Override
    public String getType() {
	return TYPE;
    }

    @Override
    protected DataStreamConnectorSetting initSetting() {
	return new DataStreamConnectorSetting();
    }

    public DataStreamClient getClient() {
	return client;
    }

    public void setClient(DataStreamClient client) {
	this.client = client;
    }
}

