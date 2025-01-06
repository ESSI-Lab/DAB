package eu.essi_lab.accessor.inumet;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class INUMETConnector extends HarvestedQueryConnector<INUMETConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "INUMETConnector";

    public INUMETConnector() {
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.INUMET_URI);
	return ret;
    }

    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().contains(CommonNameSpaceContext.INUMET_URI);
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	String token = request.getResumptionToken();
	if (token == null) {

	    INUMETClient client = new INUMETClient(getSourceURL().trim());

	    Map<String, String> variable = client.getVariables().get(0);

	    JSONObject variableJson = new JSONObject();
	    for (String key : variable.keySet()) {
		String value = variable.get(key);
		variableJson.put(key, value);
	    }

	    List<Map<String, String>> stations = client.getStations();

	    JSONObject metadata = new JSONObject();

	    for (Map<String, String> station : stations) {
		JSONObject stationJson = new JSONObject();
		for (String key : station.keySet()) {
		    String value = station.get(key);
		    stationJson.put(key, value);
		}
		metadata.put("station", stationJson);
		metadata.put("variable", variableJson);
		OriginalMetadata metadataRecord = new OriginalMetadata();
		metadataRecord.setSchemeURI(CommonNameSpaceContext.INUMET_URI);
		metadataRecord.setMetadata(metadata.toString());
		ret.addRecord(metadataRecord);
	    }

	}

	ret.setResumptionToken(null); // 1 call gives all
	return ret;

    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected INUMETConnectorSetting initSetting() {

	return new INUMETConnectorSetting();
    }

}
