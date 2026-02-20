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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

	String token = request.getResumptionToken();
	int index; // index of the collection to process (token = DOI of that collection; null = first)
	if (token == null || token.isEmpty()) {
	    index = 0;
	} else {
	    int idx = findCollectionIndexByDoi(cache, token);
	    if (idx < 0) {
		logger.warn("Resumption token (DOI) '{}' not found in collection cache; starting from first collection", token);
		index = 0;
	} else {
		index = idx;
	    }
	}

	int total = cache.size();
	String currentDoi = cache.get(index).doi;
	int currentNum = index + 1;
	double percent = total > 0 ? (currentNum * 100.0 / total) : 0;
	logger.info("Processing collection {}/{} (DOI: {}), {}%", currentNum, total, currentDoi, String.format("%.1f", percent));

	if (index >= cache.size()) {
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

	List<String> recordsForCollection = getOriginalMetadataForCollection(cache.get(index), remainingBudget);
	for (String om : recordsForCollection) {
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

	// Next resumption token = DOI of the next collection, or null if this was the last or maxRecords reached
	String nextToken = null;
	if (maxRecords <= 0 || partialNumbers < maxRecords) {
	    if (index + 1 < cache.size()) {
		nextToken = cache.get(index + 1).doi;
	    }
	}
	ret.setResumptionToken(nextToken);

	logger.debug("ADDED {} records for collection {} (DOI: {}). Next token: {}", recordsForCollection.size(),
		cache.get(index).id, cache.get(index).doi, nextToken);

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
     * Fetches original metadata (collection record + all time-series records) for a single dataset
     * collection. Does not load all collections into memory.
     *
     * @param remainingRecordBudget max records to add (0 = no limit). Used when maxRecords is set globally.
     */
    private List<String> getOriginalMetadataForCollection(DataStreamClient.DatasetMetadata collectionMinimal,
	    int remainingRecordBudget) throws GSException {

	List<String> ret = new ArrayList<>();
	try {
	    DataStreamClient.DatasetMetadata dataset = client.getMetadataByDoi(collectionMinimal.doi);
	    if (dataset == null || dataset.doi == null || dataset.doi.isEmpty()) {
		return ret;
	    }

	    // Collection-level record
	    JSONObject collectionJson = new JSONObject();
	    collectionJson.put("type", "collection");
	    collectionJson.put("doi", dataset.doi);
	    collectionJson.put("datasetId", dataset.id);
	    collectionJson.put("datasetName", dataset.name);
	    collectionJson.put("metadata", dataset.raw);
	    ret.add(collectionJson.toString());

	    int emitted = 1;
	    if (remainingRecordBudget > 0 && emitted >= remainingRecordBudget) {
		return ret;
	    }

	    List<DataStreamClient.Location> locations = client.listLocationsByDoi(dataset.doi);
	    int observationSampleSet = getSetting().getObservationSampleSet();

	    for (DataStreamClient.Location location : locations) {
		Set<String> characteristicNames = client.getCharacteristicNames(dataset.doi, location.id, observationSampleSet);
		for (String cn : characteristicNames) {
		    JSONObject seriesJson = new JSONObject();
		    seriesJson.put("type", "dataset");
		    seriesJson.put("doi", dataset.doi);
		    seriesJson.put("datasetId", dataset.id);
		    seriesJson.put("datasetName", dataset.name);
		    seriesJson.put("metadata", dataset.raw);
		    seriesJson.put("characteristicName", cn);
		    seriesJson.put("location", location.raw);

		    DataStreamClient.ObservationDateRange dateRange = client
			    .getObservationDateRange(dataset.doi, location.id, cn, observationSampleSet);
		    if (dateRange != null) {
			if (dateRange.firstActivityStartDate != null) {
			    seriesJson.put("firstObservationDate", dateRange.firstActivityStartDate);
			}
			if (dateRange.firstActivityStartTime != null) {
			    seriesJson.put("firstObservationTime", dateRange.firstActivityStartTime);
			}
			if (dateRange.lastActivityStartDate != null) {
			    seriesJson.put("lastObservationDate", dateRange.lastActivityStartDate);
			}
			if (dateRange.lastActivityStartTime != null) {
			    seriesJson.put("lastObservationTime", dateRange.lastActivityStartTime);
			}
			if (dateRange.resultUnit != null && !dateRange.resultUnit.isEmpty()) {
			    seriesJson.put("resultUnit", dateRange.resultUnit);
			}
		    }

		    ret.add(seriesJson.toString());
		    emitted++;
		    if (remainingRecordBudget > 0 && emitted >= remainingRecordBudget) {
			return ret;
		    }
		}
	    }
	} catch (IOException | InterruptedException e) {
	    logger.warn("Skipping collection {} (DOI: {}) due to error: {}", collectionMinimal.id, collectionMinimal.doi,
		    e.getMessage(), e);
	}
	return ret;
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

