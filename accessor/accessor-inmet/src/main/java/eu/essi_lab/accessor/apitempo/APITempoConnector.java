package eu.essi_lab.accessor.apitempo;

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

import java.text.ParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.accessor.apitempo.APITempoParameter.APITempoParameterCode;
import eu.essi_lab.accessor.apitempo.APITempoStation.APITempoStationCode;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class APITempoConnector extends HarvestedQueryConnector<APITempoConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "APITempoConnector";

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	if (!endpoint.contains(".br")) {
	    return false;
	}
	APITempoClient client = new APITempoClient(endpoint);
	List<APITempoStation> stations = client.getStations();
	return !stations.isEmpty();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	String token = request.getResumptionToken();
	if (token == null) {
	    token = "0";
	}
	APITempoClient client = new APITempoClient(getSourceURL());
	List<APITempoStation> stations = client.getStations();
	int i = Integer.parseInt(token);
	APITempoStation station = stations.get(i);

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && i > mr.get() - 1) {
	    // // max record set
	    maxNumberReached = true;
	}

	if (i == stations.size() - 1 || maxNumberReached) {
	    token = null;
	} else {
	    token = "" + (i + 1);
	}
	String stationCode = station.getValue(APITempoStationCode.ID);
	List<APITempoParameter> parameters = client.getStationParameters(stationCode);
	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();
	response.setResumptionToken(token);
	for (APITempoParameter parameter : parameters) {
	    APITempoStation cloneStation = station.clone();
	    List<APITempoParameter> reducedList = new ArrayList<>();
	    String parameterCode = parameter.getValue(APITempoParameterCode.ID);
	    try {
		SimpleEntry<Date, Date> beginEnd = client.getBeginEndDates(stationCode, parameterCode);
		parameter.setValue(APITempoParameterCode.DATE_BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(beginEnd.getKey()));
		Date endDate = beginEnd.getValue();
		long gap = new Date().getTime() - endDate.getTime();
		long oneWeekInMilliseconds = 1000 * 60 * 60 * 24 * 7l; // the last week

		if (gap < oneWeekInMilliseconds) {
		    parameter.setValue(APITempoParameterCode.DATE_END, "now");
		} else {
		    parameter.setValue(APITempoParameterCode.DATE_END, ISO8601DateTimeUtils.getISO8601DateTime(beginEnd.getValue()));
		}

	    } catch (ParseException e) {
		e.printStackTrace();
	    }
	    reducedList.add(parameter);
	    cloneStation.setParameters(reducedList);
	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    String metadata = cloneStation.asString();
	    metadataRecord.setMetadata(metadata);
	    metadataRecord.setSchemeURI(CommonNameSpaceContext.APITEMPO_URI);
	    response.addRecord(metadataRecord);
	}
	return response;

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.APITEMPO_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected APITempoConnectorSetting initSetting() {

	return new APITempoConnectorSetting();
    }

}
