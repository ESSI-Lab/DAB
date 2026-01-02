package eu.essi_lab.accessor.stac.harvested;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.wrapper.WrappedConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class STACConnector extends WrappedConnector {

    /**
     * 
     */
    public static final String TYPE = "STACConnector";

    private static final String STAC_GET_NODES_IDS_ERROR = "STAC_GET_NODES_IDS_ERROR";

    

    private int recordsCount;
    private List<String> nodesIds;

    /**
     * tested with https://explorer.digitalearth.africa/stac/collections
     */
    public STACConnector() {

	super();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

//	if (nodesIds == null) {
//	    try {
//		nodesIds = getNodesIds();
//
//	    } catch (Exception e) {
//
//		throw GSException.createException(//
//			getClass(), //
//			e.getMessage(), //
//			null, //
//			ErrorInfo.ERRORTYPE_SERVICE, ErrorInfo.SEVERITY_ERROR, //
//			STAC_GET_NODES_IDS_ERROR, //
//			e);
//	    }
//	}

	int resourceCounter = 0;
	String token = request.getResumptionToken();
	if (token != null) {
	    resourceCounter = Integer.valueOf(token);
	}

	try {
	    List<JSONObject> datasets = getDatasetsCollection();

	    for (JSONObject jsonObject : datasets) {

		recordsCount++;

		//jsonObject.put("nodeId", nodesIds.get(resourceCounter));

		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(STACCollectionMapper.STAC_SCHEME_URI);
		metadata.setMetadata(jsonObject.toString());

		response.addRecord(metadata);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Current page: {} -- Current records count: {}", resourceCounter,
		    recordsCount);

	    boolean hasResults = recordsCount < datasets.size();

	    Optional<Integer> mr = getSetting().getMaxRecords();

	    if (hasResults && (getSetting().isMaxRecordsUnlimited() || (mr.isPresent() && recordsCount <= mr.get()))) {

		response.setResumptionToken(String.valueOf(resourceCounter + 1));
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
    protected List<JSONObject> getDatasetsCollection() {

	String url = getSourceURL();

	Downloader downloader = new Downloader();
	JSONObject jsonObject = new JSONObject(downloader.downloadOptionalString(url).get());

	return StreamUtils.iteratorToStream(jsonObject.getJSONArray("collections").iterator()).//
		map(o -> (JSONObject) o).//
		collect(Collectors.toList());
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();
	return endpoint.contains("explorer.digitalearth.africa");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(STACCollectionMapper.STAC_SCHEME_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }


}
