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

import org.json.JSONObject;

public class APITempoParameter extends JSONObjectWrapper {
    @Override
    protected APITempoParameter clone() throws CloneNotSupportedException {
	JSONObject copy = new JSONObject(getJsonObject(), JSONObject.getNames(getJsonObject()));
	return new APITempoParameter(copy);
    }

    public APITempoParameter(JSONObject value) {
	super(value);
    }

    public String getValue(APITempoParameterCode key) {
	return super.getValue(key.getKey());
    }

    public void setValue(APITempoParameterCode key, String value) {
	super.setValue(key.getKey(), value);
    }

    public enum APITempoParameterCode {
	ID("id_variavel"), // e.g. 3
	NAME("nome"), // e.g. airTemperature
	UNITS("unidade"), // e.g. K
	INTERPOLATION("interpolation"), // e.g. instant_total
	AGGREGATION_PERIOD("aggregation_period"), // e.g. 1
	AGGREGATION_PERIOD_UNITS("aggregation_period_units"), // e.g. hour
	TIME_SPACING("time_spacing"), // e.g. 1
	TIME_SPACING_UNITS("time_spacing_units"), // e.g. hour
	DATE_BEGIN("date_begin"), DATE_END("date_end");

	private String key;

	public String getKey() {
	    return key;
	}

	APITempoParameterCode(String key) {
	    this.key = key;
	}
    }
}
