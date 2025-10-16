package eu.essi_lab.accessor.emodnet;

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

    String[] headers = null;
    private Object[] values;

    public ERDDAPRow(String[] headers, Object[] values) {
	this.headers = headers;
	this.values = values;
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
	    ret += header + ": " + values[i].toString()+"\n";
	}
	return ret;
    }

}
