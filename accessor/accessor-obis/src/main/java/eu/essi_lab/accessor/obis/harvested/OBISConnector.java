package eu.essi_lab.accessor.obis.harvested;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class OBISConnector extends HarvestedQueryConnector<OBISConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "OBISConnector";

    private static final String OBIS_GET_NODES_IDS_ERROR = "OBIS_GET_NODES_IDS_ERROR";

    /**
     * 
     */
    private static final String DATASET_URL = "https://api.obis.org/v3/dataset?";

    private int recordsCount;
    private List<String> nodesIds;

    /**
     * tested with http://api.iobis.org/
     */
    public OBISConnector() {

	super();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	if (nodesIds == null) {
	    try {
		nodesIds = getNodesIds();

	    } catch (Exception e) {

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, //
			OBIS_GET_NODES_IDS_ERROR, //
			e);
	    }
	}

	int nodeCounter = 0;
	String token = request.getResumptionToken();
	if (token != null) {
	    nodeCounter = Integer.valueOf(token);
	}

	try {
	    List<JSONObject> datasets = getDatasets(nodesIds.get(nodeCounter));

	    for (JSONObject jsonObject : datasets) {

		recordsCount++;

		jsonObject.put("nodeId", nodesIds.get(nodeCounter));

		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(OBISResourceMapper.OBIS_SCHEME_URI);
		metadata.setMetadata(jsonObject.toString());

		response.addRecord(metadata);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Current page: {}/{} -- Current records count: {}", nodeCounter, nodesIds,
		    recordsCount);

	    boolean hasResults = nodeCounter < nodesIds.size();

	    Optional<Integer> mr = getSetting().getMaxRecords();

	    if (hasResults && (getSetting().isMaxRecordsUnlimited() || (mr.isPresent() && recordsCount <= mr.get()))) {

		response.setResumptionToken(String.valueOf(nodeCounter + 1));
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Exception parsing datasets", e);
	}

	return response;
    }

    /**
     * @param nodeId
     * @return
     */
    private List<JSONObject> getDatasets(String nodeId) {

	String url = DATASET_URL + "nodeid=" + nodeId;

	Downloader downloader = new Downloader();
	JSONObject jsonObject = new JSONObject(downloader.downloadOptionalString(url).get());

	return StreamUtils.iteratorToStream(jsonObject.getJSONArray("results").iterator()).//
		map(o -> (JSONObject) o).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    private List<String> getNodesIds() {

	// String suffix = (String) getSupportedOptions().get(NODE_PATH_SUFFIX_OPTION_KEY).getValue();

	String url = "https://api.obis.org/node";// refineSourceURL() + "/" + suffix + "/";

	Downloader downloader = new Downloader();

	Optional<InputStream> stream = downloader.downloadOptionalStream(url);

	if (stream.isPresent()) {

	    InputStream inputStream = stream.get();

	    try {

		JSONObject json = JSONUtils.fromStream(inputStream);

		JSONArray results = json.getJSONArray("results");

		return StreamUtils.iteratorToStream(results.iterator()).//
			map(o -> (JSONObject) o).//
			map(o -> o.getString("id")).//
			collect(Collectors.toList());

	    } catch (IOException e) {

		GSLoggerFactory.getLogger(getClass()).warn("Error parsing datasets from {}", url, e);

	    }
	}

	return new ArrayList<>();
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("http://api.iobis.org");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(OBISResourceMapper.OBIS_SCHEME_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected OBISConnectorSetting initSetting() {

	return new OBISConnectorSetting();
    }

}
