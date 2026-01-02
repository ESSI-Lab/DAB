/**
 * 
 */
package eu.essi_lab.accessor.odatahidro.client;

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

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.odatahidro.ODataHidrologyMapper;
import eu.essi_lab.accessor.odatahidro.client.ODataHidrologyClient.Variable;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class ODataOriginalMetadata extends OriginalMetadata {

    private static final String PLACE_RESPONSE = "PLACE_RESPONSE";
    private static final String HARDWARE_RESPONSE = "HARDWARE_RESPONSE";
    private static final String OWNER_RESPONSE = "OWNER_RESPONSE";
    private static final String STATUS_RESPONSE = "STATUS_RESPONSE";
    private static final String VAR_ASC_QUERY_RESPONSE = "VAR_ASC_QUERY_RESPONSE";
    private static final String VAR_DESC_QUERY_RESPONSE = "VAR_DESC_QUERY_RESPONSE";
    private static final String VARIABLE = "VARIABLE";

    private JSONObject jsonMetadata;

    /**
     * 
     */
    public ODataOriginalMetadata() {

	setSchemeURI(ODataHidrologyMapper.ODATA_HIDROLOGY_SCHEME_URI);
	jsonMetadata = new JSONObject();
    }

    /**
     * @return
     */
    public ClientResponseWrapper getPlaceQueryResponseWrapper() {

	return new ClientResponseWrapper(this.jsonMetadata.getJSONObject(PLACE_RESPONSE));
    }

    /**
     * @param placeQueryResponse
     * @param arrayIndex
     */
    @SuppressWarnings("unchecked")
    public void setPlaceQueryResponse(JSONObject placeQueryResponse, int arrayIndex) {

	JSONArray jsonArray = new JSONArray();
	jsonArray.put(placeQueryResponse.getJSONArray("value").getJSONObject(arrayIndex));

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("value", jsonArray);

	this.jsonMetadata.put(PLACE_RESPONSE, jsonObject);

	setMetadata(this.jsonMetadata.toString(3));
    }

    /**
     * @param hardResponse
     */
    public void setHardwareResponse(JSONObject hardResponse) {

	this.jsonMetadata.put(HARDWARE_RESPONSE, hardResponse);

	setMetadata(this.jsonMetadata.toString(3));
    }

    /**
     * @return
     */
    public Optional<ClientResponseWrapper> getHardwareResponseWrapper() {

	if (this.jsonMetadata.has(HARDWARE_RESPONSE)) {

	    return Optional.of(new ClientResponseWrapper(this.jsonMetadata.getJSONObject(HARDWARE_RESPONSE)));
	}

	return Optional.empty();
    }

    /**
     * @param ownerResponse
     */
    public void setOwnerResponse(JSONObject ownerResponse) {

	this.jsonMetadata.put(OWNER_RESPONSE, ownerResponse);

	setMetadata(this.jsonMetadata.toString(3));
    }

    /**
     * @return
     */
    public Optional<ClientResponseWrapper> getOwnerResponseWrapper() {

	if (this.jsonMetadata.has(OWNER_RESPONSE)) {

	    return Optional.of(new ClientResponseWrapper(this.jsonMetadata.getJSONObject(OWNER_RESPONSE)));
	}

	return Optional.empty();
    }

    /**
     * @param statusResponse
     */
    public void setStatusResponse(JSONObject statusResponse) {

	this.jsonMetadata.put(STATUS_RESPONSE, statusResponse);
    }

    /**
     * @return
     */
    public Optional<ClientResponseWrapper> getStatusResponseWrapper() {

	if (this.jsonMetadata.has(STATUS_RESPONSE)) {

	    return Optional.of(new ClientResponseWrapper(this.jsonMetadata.getJSONObject(STATUS_RESPONSE)));
	}

	return Optional.empty();
    }

    /**
     * @param variable
     */
    public void setVariable(Variable variable) {

	this.jsonMetadata.put(VARIABLE, variable.getVariableId());
    }

    /**
     * @return
     */
    public Variable getVariable() {

	return Variable.fromId(this.jsonMetadata.getString(VARIABLE).toString()).get();
    }

    /**
     * @param varAscQueryResponse
     */
    public void setVarAscendingQueryResponse(JSONObject varAscQueryResponse) {

	this.jsonMetadata.put(VAR_ASC_QUERY_RESPONSE, varAscQueryResponse);

	setMetadata(this.jsonMetadata.toString(3));
    }

    /**
     * @return
     */
    public ClientResponseWrapper getVarAscendingQueryResponseWrapper() {

	return new ClientResponseWrapper(this.jsonMetadata.getJSONObject(VAR_ASC_QUERY_RESPONSE));
    }

    /**
     * @param varDescQueryResponse
     */
    public void setVarDescendingQueryResponse(JSONObject varDescQueryResponse) {

	this.jsonMetadata.put(VAR_DESC_QUERY_RESPONSE, varDescQueryResponse);

	setMetadata(this.jsonMetadata.toString(3));
    }

    /**
     * @return
     */
    public ClientResponseWrapper getVarDescendingQueryResponseWrapper() {

	return new ClientResponseWrapper(this.jsonMetadata.getJSONObject(VAR_DESC_QUERY_RESPONSE));
    }
}
