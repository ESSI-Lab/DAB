package eu.essi_lab.accessor.whos;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author boldrini
 */
public class DMHStation extends DMHJSON {

    public DMHStation(JSONObject json) {
	super(json);
    }

    public BigDecimal getId() {
	return getBigDecimal("id");
    }

    public String getCode() {
	return json.getString("code");
    }

    public String getName() {
	return json.getString("name");
    }

    public BigDecimal getAltitude() {
	return getBigDecimal("altitude");
    }

    public BigDecimal getLatitude() {
	return getBigDecimal("latitude");
    }

    public BigDecimal getLongitude() {
	return getBigDecimal("longitude");
    }

    public List<DMHVariable> getVariables() {
	List<DMHVariable> ret = new ArrayList<>();
	JSONArray variables = json.getJSONArray("variables");
	HashMap<String, DMHVariable> variableMap = new HashMap<>();
	for (int i = 0; i < variables.length(); i++) {
	    JSONObject variable = variables.getJSONObject(i);
	    DMHVariable var = new DMHVariable(variable);
	    DMHVariable existing = variableMap.get(var.getVariableName());
	    if (existing==null) {
		existing = var;
	    }else {
		if (existing.getObservationsStart().after(var.getObservationsStart())) {
		    existing.set("observations_start",var.getStringValue("observations_start"));
		}
		if (existing.getObservationsEnd().before(var.getObservationsEnd())) {
		    existing.set("observations_end",var.getStringValue("observations_end"));
		}
	    }	    
	    variableMap.put(var.getVariableName(), existing);	    
	}
	ret.addAll(variableMap.values());
	return ret;
    }

    public DMHVariable getVariable(String parameterCode) {
	List<DMHVariable> variables = getVariables();
	for (DMHVariable variable : variables) {
	    if (parameterCode.equals(variable.getVariableName())) {
		return variable;
	    }
	}
	return null;
    }

}
