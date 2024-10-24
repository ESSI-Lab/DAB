package eu.essi_lab.accessor.ana.sar;

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

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONUtils;

public class ANASARStation {

    String reservoirId;
    String reservoirName;
    String reservoirCapacity;
    String latitude;
    String longitude;
    String municipality;
    String state;
    String stateAbbreviation;
    String basinName;
    String network;
    String variable;
    String variableUnits;
    String startDate;
    String endDate;
    String resolutionMs;

    public ANASARStation(JSONObject json) {

	reservoirId = json.get("res_id").toString().trim();
	reservoirName = normalizeString(JSONUtils.getString(json, "res_nome"));
	if (json.has("res_capacidade")) {
	    reservoirCapacity = json.get("res_capacidade").toString().trim();
	}
	latitude = normalizeCoordinate(JSONUtils.getString(json, "res_latitude"));
	longitude = normalizeCoordinate(JSONUtils.getString(json, "res_longitude"));
	municipality = normalizeString(JSONUtils.getString(json, "mun_nome"));
	state = normalizeString(JSONUtils.getString(json, "est_nome"));
	stateAbbreviation = JSONUtils.getString(json, "est_sigla");
	basinName = normalizeString(JSONUtils.getString(json, "bac_nome"));
	network = json.get("tsi_id").toString().trim();
	variable = JSONUtils.getString(json, "variable");
	variableUnits = JSONUtils.getString(json, "variable_units");
	startDate = JSONUtils.getString(json, "start_date");
	endDate = JSONUtils.getString(json, "end_date");
	if (json.has("resolution_ms")) {
	    resolutionMs = json.get("resolution_ms").toString().trim();
	}

    }

    private String normalizeString(String string) {
	if (string == null) {
	    return null;
	}
	return string.trim();
    }

    private String normalizeCoordinate(String value) {
	if (value == null) {
	    return null;
	}
	return value.replace(",", ".").replace(" ", "");
    }

    public String getReservoirId() {
	return reservoirId;
    }

    public String getReservoirName() {
	return reservoirName;
    }

    public String getReservoirCapacity() {
	return reservoirCapacity;
    }

    public String getLatitude() {
	return latitude;
    }

    public String getLongitude() {
	return longitude;
    }

    public String getMunicipality() {
	return municipality;
    }

    public String getState() {
	return state;
    }

    public String getStateAbbreviation() {
	return stateAbbreviation;
    }

    public String getBasinName() {
	return basinName;
    }

    public String getNetwork() {
	return network;
    }

    public String getVariable() {
	return variable;
    }

    public String getVariableUnits() {
	return variableUnits;
    }

    public String getStartDate() {
	return startDate;
    }

    public String getEndDate() {
	return endDate;
    }

    public String getResolutionMs() {
	return resolutionMs;
    }

}
