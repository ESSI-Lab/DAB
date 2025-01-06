/**
 * 
 */
package eu.essi_lab.accessor.odatahidro;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.accessor.odatahidro.client.ClientResponseWrapper;
import eu.essi_lab.accessor.odatahidro.client.ODataHidrologyClient;
import eu.essi_lab.accessor.odatahidro.client.ODataHidrologyClient.InformationTarget;
import eu.essi_lab.accessor.odatahidro.client.ODataHidrologyClient.Variable;
import eu.essi_lab.accessor.odatahidro.client.ODataOriginalMetadata;
import eu.essi_lab.adk.timeseries.StationConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class ODataHidrologyConnector extends StationConnector<ODataHidrologyConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ODataHidrologyConnector";

    private static final int DEFAULT_OFFSET = 20;

    private static final String ODATA_HIDROLOGY_CLIENT_ERROR = "ODATA_HIDROLOGY_CLIENT_ERROR";

    private ODataHidrologyClient client;

    private int offset;

    @Override
    public ListRecordsResponse<OriginalMetadata> listTimeseries(String stationId) throws GSException {

	if (client == null) {

	    client = new ODataHidrologyClient();
	}

	GSLoggerFactory.getLogger(getClass()).debug("Station request for id: {}", stationId);

	JSONObject placeQueryResponse;
	try {
	    placeQueryResponse = client.execStationQuery(0, 1000, stationId);
	} catch (Exception e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ODATA_HIDROLOGY_CLIENT_ERROR, //
		    e);
	}

	return listRecords(placeQueryResponse);
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	if (client == null) {

	    client = new ODataHidrologyClient();
	}

	String token = request.getResumptionToken();

	if (token != null) {

	    offset = Integer.valueOf(token);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Current offset {}", offset);

	JSONObject placeQueryResponse;
	try {
	    placeQueryResponse = client.execPlaceQuery(offset, DEFAULT_OFFSET);
	} catch (Exception e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ODATA_HIDROLOGY_CLIENT_ERROR, //
		    e);
	}

	return listRecords(placeQueryResponse);

    }

    public ListRecordsResponse<OriginalMetadata> listRecords(JSONObject placeQueryResponse) throws GSException {
	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	try {

	    ClientResponseWrapper placeQueryResponseWrapper = new ClientResponseWrapper(placeQueryResponse);

	    int responseSize = placeQueryResponseWrapper.getResponseSize();

	    GSLoggerFactory.getLogger(getClass()).debug("Response size {}", responseSize);

	    for (int i = 0; i < responseSize; i++) {

		Optional<String> variableId = placeQueryResponseWrapper.getVariableId(i);

		Optional<Variable> optionalVariable = Variable.fromId(variableId.get());

		if (optionalVariable.isPresent()) {

		    ODataOriginalMetadata originalMetadata = new ODataOriginalMetadata();

		    originalMetadata.setPlaceQueryResponse(placeQueryResponse, i);

		    Optional<String> placeId = placeQueryResponseWrapper.getPlaceId(i);

		    // if (placeId.isPresent()) {
		    // JSONObject placeResponse = client.execInformativeQuery(InformationTarget.PLACE, placeId.get());
		    // System.out.println();
		    // } else {
		    // GSLoggerFactory.getLogger(getClass())
		    // .warn("Missing place id of place: " + placeQueryResponseWrapper.getPlaceId(i));
		    // }

		    Optional<String> hardwareId = placeQueryResponseWrapper.getHardwareId(i);

		    if (hardwareId.isPresent()) {
			JSONObject hardResponse = client.execInformativeQuery(InformationTarget.HARDWARE, hardwareId.get());
			originalMetadata.setHardwareResponse(hardResponse);
		    } else {
			GSLoggerFactory.getLogger(getClass())
				.warn("Missing hardware id of place: " + placeQueryResponseWrapper.getPlaceId(i));
		    }

		    Optional<String> ownerId = placeQueryResponseWrapper.getOwnerId(i);

		    if (ownerId.isPresent()) {
			JSONObject ownerResponse = client.execInformativeQuery(InformationTarget.OWNER, ownerId.get());
			originalMetadata.setOwnerResponse(ownerResponse);
		    } else {
			GSLoggerFactory.getLogger(getClass()).warn("Missing owner id of place: " + placeQueryResponseWrapper.getPlaceId(i));
		    }

		    Optional<String> statusId = placeQueryResponseWrapper.getStatusId(i);

		    if (statusId.isPresent()) {

			JSONObject statusResponse = client.execInformativeQuery(InformationTarget.STATUS, statusId.get());
			originalMetadata.setStatusResponse(statusResponse);
		    } else {
			GSLoggerFactory.getLogger(getClass())
				.warn("Missing status id of place: " + placeQueryResponseWrapper.getPlaceId(i));
		    }

		    Variable variable = optionalVariable.get();

		    GSLoggerFactory.getLogger(getClass()).debug("Creating original md for variable {}", variable.getVariableName());

		    JSONObject varAscQueryResponse = client.execVariableQuery(optionalVariable.get(), placeId.get(), "asc", 0, 1, null,
			    null);
		    JSONObject varDescQueryResponse = client.execVariableQuery(optionalVariable.get(), placeId.get(), "desc", 0, 1, null,
			    null);

		    originalMetadata.setVariable(variable);
		    originalMetadata.setVarAscendingQueryResponse(varAscQueryResponse);
		    originalMetadata.setVarDescendingQueryResponse(varDescQueryResponse);

		    response.addRecord(originalMetadata);

		} else {

		    GSLoggerFactory.getLogger(getClass())
			    .warn("No variable found for place id: " + placeQueryResponseWrapper.getPlaceId(i));
		}
	    }

	    // means there are more records
	    if (responseSize >= DEFAULT_OFFSET) {

		offset += DEFAULT_OFFSET;

		Optional<Integer> optionalMaxRecords = getSetting().getMaxRecords();

		if (!getSetting().isMaxRecordsUnlimited()) {

		    Integer maxRecords = optionalMaxRecords.get();

		    if (offset < maxRecords) {

			response.setResumptionToken(String.valueOf(offset));

		    } else {

			GSLoggerFactory.getLogger(getClass()).debug("Max records of {} reached, process ended", maxRecords);
		    }
		} else {

		    response.setResumptionToken(String.valueOf(offset));
		}
	    } else {

		GSLoggerFactory.getLogger(getClass()).info("Response size {} < default offset {}, process ended", responseSize,
			DEFAULT_OFFSET);

	    }

	} catch (Exception e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ODATA_HIDROLOGY_CLIENT_ERROR, //
		    e);
	}

	return response;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(ODataHidrologyMapper.ODATA_HIDROLOGY_SCHEME_URI);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("http://rajapinnat.ymparisto.fi/api/Hydrologiarajapinta/1.0");
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected ODataHidrologyConnectorSetting initSetting() {

	return new ODataHidrologyConnectorSetting();
    }
}
