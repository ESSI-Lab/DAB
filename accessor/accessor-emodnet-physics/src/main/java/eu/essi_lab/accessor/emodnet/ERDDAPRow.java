package eu.essi_lab.accessor.emodnet;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

public class ERDDAPRow {

    private String[] headers = null;
    private Object[] values;

    public Object[] getValues() {
	return values;
    }

    public void setValues(Object[] values) {	
	for (int i = 0; i < values.length; i++) {
	    if (values[i] == org.json.JSONObject.NULL) {
		values[i] = null;
	    }
	}
	this.values = values;
    }
    
    public ERDDAPRow() {	
    }

    public ERDDAPRow(JSONObject json) throws Exception {
	ObjectMapper mapper = new ObjectMapper();
	// Deserialize back
	ERDDAPRow restored = mapper.readValue(json.toString(), ERDDAPRow.class);
	this.headers = restored.getHeaders();
	this.values = restored.getValues();
	for (int i = 0; i < values.length; i++) {
	    if (values[i] == org.json.JSONObject.NULL) {
		values[i] = null;
	    }
	}
    }

    public ERDDAPRow(String[] headers, Object[] values) {
	this.headers = headers;
	this.values = values;
	for (int i = 0; i < values.length; i++) {
	    if (values[i] == org.json.JSONObject.NULL) {
		values[i] = null;
	    }
	}
    }

    public Object getValue(String header) {
	Integer f = null;
	for (int i = 0; i < headers.length; i++) {
	    if (headers[i].equals(header)) {
		f = i;
		break;
	    }
	}
	if (f == null) {
	    return null;
	}
	return values[f];
    }

    public String[] getHeaders() {
	return headers;

    }

    @Override
    public String toString() {
	String ret = "";
	for (int i = 0; i < headers.length; i++) {
	    String header = headers[i];
	    ret += header + ": " + values[i].toString() + "\n";
	}
	return ret;
    }

    public JSONObject toJSONObject() throws Exception {
	ObjectMapper mapper = new ObjectMapper();
	String json = mapper.writeValueAsString(this);
	return new JSONObject(json);
    }

}
