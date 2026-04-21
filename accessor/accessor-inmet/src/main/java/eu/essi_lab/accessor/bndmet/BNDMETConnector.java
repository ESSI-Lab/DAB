package eu.essi_lab.accessor.bndmet;

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
import java.util.Optional;

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

    private int partialNumbers;

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
	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	if (stations.isEmpty()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int i = Integer.parseInt(token);
	if (i < 0 || i >= stations.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean unlimited = getSetting().isMaxRecordsUnlimited();
	if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	BNDMETStation station = stations.get(i);
	List<BNDMETParameter> parameters = client.getStationParameters(station.getValue(BNDMET_Station_Code.CD_ESTACAO));

	boolean stoppedByMax = false;
	for (BNDMETParameter parameter : parameters) {
	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }
	    BNDMETStation cloneStation = station.clone();
	    List<BNDMETParameter> reducedList = new ArrayList<>();
	    reducedList.add(parameter);
	    cloneStation.setParameters(reducedList);
	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    String metadata = cloneStation.asString();
	    metadataRecord.setMetadata(metadata);
	    metadataRecord.setSchemeURI(CommonNameSpaceContext.BNDMET_URI);
	    response.addRecord(metadataRecord);
	    partialNumbers++;
	}

	if (stoppedByMax) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	if (i == stations.size() - 1) {
	    token = null;
	    partialNumbers = 0;
	} else {
	    token = "" + (i + 1);
	}
	response.setResumptionToken(token);
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
