package eu.essi_lab.accessor.nrfa;

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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class NRFAConnector extends HarvestedQueryConnector<NRFAConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "NRFAConnector";

    public NRFAConnector() {
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.NRFA_URI);
	return ret;
    }

    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().contains(CommonNameSpaceContext.NRFA_URI);
    }

    List<String> stations = null;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	NRFAClient client = new NRFAClient(getSourceURL().trim());

	if (stations == null) {
	    stations = client.getStationIdentifiers();
	}

	String token = request.getResumptionToken();

	if (token == null) {
	    token = stations.get(0);
	}

	for (int i = 0; i < stations.size(); i++) {
	    String station = stations.get(i);
	    if (station.equals(token)) {
		if (i == stations.size() - 1) {
		    token = null;
		} else {
		    token = stations.get(i + 1);
		}
		ret.setResumptionToken(token);

		StationInfo info = client.getStationInfo(station);

		String jsonString = info.getJSON().toString();

		List<ParameterInfo> parameters = info.getParameterInfos();
		for (ParameterInfo parameter : parameters) {
		    JSONObject json = new JSONObject(jsonString);
		    JSONObject summary = json.getJSONObject("data-summary");
		    List<JSONObject> list = new ArrayList<>();
		    list.add(parameter.getJSON());
		    summary.put("data-types", list);
		    OriginalMetadata metadataRecord = new OriginalMetadata();
		    metadataRecord.setSchemeURI(CommonNameSpaceContext.NRFA_URI);
		    metadataRecord.setMetadata(json.toString());
		    ret.addRecord(metadataRecord);
		}

	    }

	}

	return ret;

    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected NRFAConnectorSetting initSetting() {

	return new NRFAConnectorSetting();
    }

}
