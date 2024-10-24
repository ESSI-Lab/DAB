package eu.essi_lab.accessor.bndmet;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.accessor.bndmet.model.BNDMETParameter;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation.BNDMET_Station_Code;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class BNDMETConnector extends HarvestedQueryConnector<BNDMETConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "BNDMETConnector";

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (!endpoint.contains(".br")) {
	    return false;
	}
	BNDMETClient client = new BNDMETClient(endpoint);
	List<BNDMETStation> stations = client.getAutomaticStations();
	return !stations.isEmpty();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	String token = request.getResumptionToken();
	if (token == null) {
	    token = "0";
	}
	BNDMETClient client = new BNDMETClient(getSourceURL());
	List<BNDMETStation> stations = client.getStations();
	int i = Integer.parseInt(token);
	BNDMETStation station = stations.get(i);
	if (i == stations.size() - 1) {
	    token = null;
	} else {
	    token = "" + (i + 1);
	}
	List<BNDMETParameter> parameters = client.getStationParameters(station.getValue(BNDMET_Station_Code.CD_ESTACAO));
	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();
	response.setResumptionToken(token);
	for (BNDMETParameter parameter : parameters) {
	    BNDMETStation cloneStation = station.clone();
	    List<BNDMETParameter> reducedList = new ArrayList<>();
	    reducedList.add(parameter);
	    cloneStation.setParameters(reducedList);
	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    String metadata = cloneStation.asString();
	    metadataRecord.setMetadata(metadata);
	    metadataRecord.setSchemeURI(CommonNameSpaceContext.BNDMET_URI);
	    response.addRecord(metadataRecord);
	}
	return response;

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.BNDMET_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected BNDMETConnectorSetting initSetting() {

	return new BNDMETConnectorSetting();
    }
}
