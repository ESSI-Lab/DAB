package eu.essi_lab.accessor.datalakes;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
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
 * Connector for the Datalakes REST API.
 *
 * It exposes one {@link OriginalMetadata} record for each observable parameter
 * ({@code y*} axis) of each dataset.
 */
public class DatalakesConnector extends HarvestedQueryConnector<DatalakesConnectorSetting> {

    public static final String TYPE = "DatalakesConnector";

    private static final String DATALAKES_READ_ERROR = "Unable to retrieve Datalakes content";
    private static final String DATALAKES_URL_NOT_FOUND_ERROR = "DATALAKES_URL_NOT_FOUND_ERROR";

    private final Logger logger = GSLoggerFactory.getLogger(getClass());

    private final List<String> recordCache = new ArrayList<>();
    private DatalakesClient client;
    private int partialNumbers;

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint != null && endpoint.toLowerCase().contains("datalakes");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();
	ensureRecordCacheFilled();

	if (recordCache.isEmpty()) {
	    response.setResumptionToken(null);
	    return response;
	}

	int index = 0;
	if (request.getResumptionToken() != null) {
	    try {
		index = Integer.parseInt(request.getResumptionToken());
	    } catch (NumberFormatException e) {
		logger.warn("Invalid resumption token '{}', restarting from 0", request.getResumptionToken());
		index = 0;
	    }
	}

	if (index >= recordCache.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	Optional<Integer> maxRecords = getSetting().getMaxRecords();
	int remainingBudget = getSetting().isMaxRecordsUnlimited() || !maxRecords.isPresent() ? Integer.MAX_VALUE
		: Math.max(0, maxRecords.get() - partialNumbers);

	int pageSize = Math.max(1, getSetting().getPageSize());
	int emitted = 0;
	while (index < recordCache.size() && emitted < pageSize && remainingBudget > 0) {
	    OriginalMetadata metadata = new OriginalMetadata();
	    metadata.setSchemeURI(CommonNameSpaceContext.DATALAKES_NS_URI);
	    metadata.setMetadata(recordCache.get(index));
	    response.addRecord(metadata);
	    index++;
	    emitted++;
	    partialNumbers++;
	    remainingBudget--;
	}

	if (index < recordCache.size() && remainingBudget > 0) {
	    response.setResumptionToken(String.valueOf(index));
	} else {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	}

	return response;
    }

    private void ensureRecordCacheFilled() throws GSException {
	if (!recordCache.isEmpty()) {
	    return;
	}

	String endpoint = getSourceURL();
	if (endpoint == null || endpoint.isEmpty()) {
	    throw GSException.createException(getClass(), DATALAKES_READ_ERROR, null, ErrorInfo.ERRORTYPE_SERVICE,
		    ErrorInfo.SEVERITY_ERROR, DATALAKES_URL_NOT_FOUND_ERROR);
	}

	try {
	    client = new DatalakesClient(endpoint, getSetting().getApiKey());
	    JSONObject selectionTables = client.getSelectionTables();
	    List<JSONObject> datasets = client.listDatasets();
	    datasets.sort(Comparator.comparingInt(d -> d.getInt("id")));

	    for (JSONObject dataset : datasets) {
		int datasetId = dataset.getInt("id");
		List<JSONObject> datasetParameters = client.getDatasetParameters(datasetId);
		String payloadType = DatalakesClient.resolvePayloadType(datasetParameters);
		boolean downloadable = DatalakesClient.isTimeSeriesRecord(payloadType, dataset);

		for (JSONObject datasetParameter : datasetParameters) {
		    if (!DatalakesClient.isObservableParameter(datasetParameter)) {
			continue;
		    }
		    JSONObject record = buildRecord(dataset, datasetParameter, datasetParameters, payloadType, downloadable,
			    selectionTables);
		    recordCache.add(record.toString());
		}
	    }

	    logger.info("Datalakes connector cache filled with {} records", recordCache.size());
	} catch (IOException | InterruptedException e) {
	    logger.error("Error retrieving Datalakes catalog", e);
	    throw GSException.createException(getClass(), DATALAKES_READ_ERROR + ": " + e.getMessage(), null,
		    ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, DATALAKES_URL_NOT_FOUND_ERROR);
	}
    }

    private JSONObject buildRecord(JSONObject dataset, JSONObject datasetParameter, List<JSONObject> datasetParameters,
	    String payloadType, boolean downloadable, JSONObject selectionTables) {

	JSONObject record = new JSONObject();
	record.put("datasetId", dataset.getInt("id"));
	record.put("axis", datasetParameter.getString("axis"));
	record.put("parseparameter", datasetParameter.optString("parseparameter"));
	record.put("parameterId", datasetParameter.optInt("parameters_id"));
	record.put("datasetParameterId", datasetParameter.optInt("id"));
	record.put("unit", datasetParameter.optString("unit"));
	record.put("payloadType", payloadType);
	record.put("downloadable", downloadable);
	record.put("dataset", dataset);
	record.put("datasetParameter", datasetParameter);

	JSONObject parameter = DatalakesClient.findLookupEntry(selectionTables.optJSONArray("parameters"),
		datasetParameter.optInt("parameters_id"));
	if (parameter != null) {
	    record.put("parameter", parameter);
	}

	JSONObject lake = DatalakesClient.findLookupEntry(selectionTables.optJSONArray("lakes"), dataset.optInt("lakes_id"));
	if (lake != null) {
	    record.put("lake", lake);
	}

	JSONObject organisation = DatalakesClient.findLookupEntry(selectionTables.optJSONArray("organisations"),
		dataset.optInt("organisations_id"));
	if (organisation != null) {
	    record.put("organisation", organisation);
	}

	JSONObject license = DatalakesClient.findLookupEntry(selectionTables.optJSONArray("licenses"),
		dataset.optInt("licenses_id"));
	if (license != null) {
	    record.put("license", license);
	}

	JSONObject sensor = DatalakesClient.findLookupEntry(selectionTables.optJSONArray("sensors"),
		datasetParameter.optInt("sensors_id"));
	if (sensor != null) {
	    record.put("sensor", sensor);
	}

	JSONArray observableParameters = new JSONArray();
	for (JSONObject candidate : datasetParameters) {
	    if (DatalakesClient.isObservableParameter(candidate)) {
		observableParameters.put(candidate);
	    }
	}
	record.put("observableParameters", observableParameters);

	return record;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> formats = new ArrayList<>();
	formats.add(CommonNameSpaceContext.DATALAKES_NS_URI);
	return formats;
    }

    @Override
    public String getType() {
	return TYPE;
    }

    @Override
    protected DatalakesConnectorSetting initSetting() {
	return new DatalakesConnectorSetting();
    }

    public DatalakesClient getClient() {
	return client;
    }

    public void setClient(DatalakesClient client) {
	this.client = client;
    }
}
