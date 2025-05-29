package eu.essi_lab.accessor.hiscentral.sardegna;

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

import org.json.JSONArray;
import org.json.JSONObject;

public class HISCentralSardegnaVariable {


    
    private JSONObject json;

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public HISCentralSardegnaVariable(JSONObject jsonObject) {
	this.json = jsonObject;
	
    }
    
    public JSONArray getKeywords() {
	return json.optJSONArray("keyword");
    }

    public String getId() {
	return json.getString("ARPAS_CODIFICA");
    }

    public String getVariableURI() {
	return json.getString("WMO_observed_property");
    }

    public String getVariableName() {
	return json.getString("WMO_name");
    }

    public String getAggregationPeriodUnits() {
	return json.getString("Risoluzione temporale (ISO8601)");
    }
    
    public String getInterpolation() {
	return json.getString("Tipo interpolazione");
    }
    
    public String getVariableUnits() {
	return json.getString("Unita di misura");
    }

    public String getStringValue(String key) {
	return json.getString(key);
    }

    public void set(String key, String value) {
	json.put(key, value);

    }

}
