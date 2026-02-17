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

    private List<String> originalMetadata;
    private int partialNumbers;
    private DataStreamClient client;

    public DataStreamConnector() {
	this.originalMetadata = new ArrayList<>();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint != null && !endpoint.isEmpty();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	if (originalMetadata.isEmpty()) {

	    Optional<Integer> mr = getSetting().getMaxRecords();
	    int maxRecords = 0;
	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent()) {
		maxRecords = mr.get();
	    }

	    originalMetadata = getOriginalMetadata(maxRecords);
	}

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {
	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    maxNumberReached = true;
	}

	if (start < originalMetadata.size() && !maxNumberReached) {
	    int end = start + pageSize;
	    if (end > originalMetadata.size()) {
		end = originalMetadata.size();
	    }

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		end = mr.get();
	    }
	    int count = 0;

	    for (int i = start; i < end; i++) {
		String om = originalMetadata.get(i);

		if (om != null && !om.isEmpty()) {
		    OriginalMetadata metadata = new OriginalMetadata();
		    metadata.setSchemeURI(CommonNameSpaceContext.DATASTREAM_NS_URI);
		    metadata.setMetadata(om);
		    ret.addRecord(metadata);
		    partialNumbers++;
		    count++;
		}
	    }
	    ret.setResumptionToken(String.valueOf(start + count));
	    logger.debug("ADDED {} records. Number of analyzed DataStream logical series: {}", partialNumbers,
		    String.valueOf(start + count));

	} else {
	    ret.setResumptionToken(null);
	    logger.debug("Added DataStream records: {} . TOTAL DATASTREAM SIZE: {}", partialNumbers, originalMetadata.size());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    private List<String> getOriginalMetadata(int maxRecords) throws GSException {

	logger.trace("DataStream list metadata STARTED");

	List<String> ret = new ArrayList<>();

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

	    // 1) list datasets (Metadata entries), possibly limited by maxRecords
	    int datasetTop = maxRecords > 0 ? maxRecords : 0;
	    List<DataStreamClient.DatasetMetadata> datasets = client.listDatasets(datasetTop);

	    int emitted = 0;
	    boolean limitReached = false;

	    for (DataStreamClient.DatasetMetadata dataset : datasets) {

		if (dataset.doi == null || dataset.doi.isEmpty()) {
		    continue;
		}

		//
		// Collection-level original metadata
		//
		JSONObject collectionJson = new JSONObject();
		collectionJson.put("type", "collection");
		collectionJson.put("doi", dataset.doi);
		collectionJson.put("datasetId", dataset.id);
		collectionJson.put("datasetName", dataset.name);
		collectionJson.put("metadata", dataset.raw);
		ret.add(collectionJson.toString());
		emitted++;
		if (maxRecords > 0 && emitted >= maxRecords) {
		    limitReached = true;
		    break;
		}

		if (limitReached) {
		    break;
		}

		//
		// Time-series level original metadata: one per (location, CharacteristicName)
		// Each series also carries the collection-level metadata so that key fields
		// such as Abstract, Licence, TopicCategoryCode, Keywords and organisation
		// info can be propagated into the dataset-level ISO mapping.
		//
		List<DataStreamClient.Location> locations = client.listLocationsByDoi(dataset.doi);
		for (DataStreamClient.Location location : locations) {

		    int observationSampleSet = getSetting().getObservationSampleSet();
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

			//
			// Optional temporal extent enrichment: first/last observation dates
			// for this (DOI, Location, CharacteristicName) series.
			//
			DataStreamClient.ObservationDateRange dateRange = client
				.getObservationDateRange(dataset.doi, location.id, cn,observationSampleSet);
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
			if (maxRecords > 0 && emitted >= maxRecords) {
			    limitReached = true;
			    break;
			}
		    }

		    if (limitReached) {
			break;
		    }
		}

		if (limitReached) {
		    break;
		}
	    }

	} catch (IOException | InterruptedException e) {
	    logger.error("Error retrieving DataStream content", e);
	    throw GSException.createException(//
		    this.getClass(), //
		    DATASTREAM_READ_ERROR + ": " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATASTREAM_URL_NOT_FOUND_ERROR);
	}

	logger.trace("DataStream list metadata ENDED");
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

