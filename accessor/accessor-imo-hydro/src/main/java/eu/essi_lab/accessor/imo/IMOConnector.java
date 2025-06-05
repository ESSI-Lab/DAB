package eu.essi_lab.accessor.imo;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import eu.essi_lab.adk.timeseries.StationConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class IMOConnector extends StationConnector<IMOConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "IMOConnector";
    private static final String IMO_CONNECTOR_LIST_RECORDS_ERROR = "IMO_CONNECTOR_LIST_RECORDS_ERROR";

    public IMOConnector() {
    }

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.contains("customer.vedur.is");
    }

    private IMOClient client = null;

    public IMOClient getClient() {
	return client;
    }

    public void setClient(IMOClient client) {
	this.client = client;
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listTimeseries(String stationId) throws GSException {
	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken(stationId);
	ListRecordsResponse<OriginalMetadata> ret = listRecords(request);
	return ret;

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	if (client == null) {
	    client = new IMOClient();
	    String sourceUrl = getSourceURL();
	    if (sourceUrl != null) {
		client.setEndpoint(sourceUrl);
	    }
	}

	String requiredStationId = request.getResumptionToken();

	List<ZRXPDocument> zrxps;
	try {
	    zrxps = client.downloadAll();
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    IMO_CONNECTOR_LIST_RECORDS_ERROR, //
		    e);
	}

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	HashMap<String, List<ZRXPBlock>> map = ZRXPBlock.aggregateBlocks(zrxps);

	Set<String> keys = map.keySet();

	for (String key : keys) {
	    GSLoggerFactory.getLogger(getClass()).info("Found parameter@station: {}", key);
	}

	Collection<List<ZRXPBlock>> blockLists = map.values();

	b1: for (List<ZRXPBlock> blocks : blockLists) {

	    String metadata = "";
	    for (ZRXPBlock block : blocks) {
		String stationId = block.getStationIdentifier().replace("V", "");
		if (requiredStationId == null || stationId.equals(requiredStationId)) {
		    if (block.getLatitude() == null || block.getLatitude().isEmpty()) {
			System.err.println("Attention, station " + block.getStationIdentifier() + " (" + block.getStationName()
				+ ") has no latitude info");
			continue b1;
		    }
		    String blockString = block.asString();
		    metadata += blockString;
		}
	    }

	    if (!metadata.equals("")) {
		OriginalMetadata metadataRecord = new OriginalMetadata();

		metadataRecord.setSchemeURI(CommonNameSpaceContext.IMO_URI);

		metadataRecord.setMetadata(metadata);

		ret.addRecord(metadataRecord);
	    }

	}

	for (ZRXPDocument zrxp : zrxps) {
	    zrxp.getFile().delete();
	}

	ret.setResumptionToken(null);

	return ret;

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<String>();
	ret.add(CommonNameSpaceContext.IMO_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected IMOConnectorSetting initSetting() {

	return new IMOConnectorSetting();
    }

}
