package eu.essi_lab.profiler.arpa;

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

import java.util.HashMap;

public class HydroCSVTimeSeriesEncoder {

    private String separator = ",";
    private String separatorEncoding = "%2C";

    public String getSeparator() {
	return separator;
    }

    public void setSeparator(String separator) {
	this.separator = separator;
    }

    public String getSeparatorEncoding() {
	return separatorEncoding;
    }

    public void setSeparatorEncoding(String separatorEncoding) {
	this.separatorEncoding = separatorEncoding;
    }

    public enum CSV_Field {

	SITE_CODE("Site Code"), //
	SITE_NAME("Site Name"), //
	LATITUDE("Latitude"), //
	LONGITUDE("Longitude"), //
	VARIABLE_CODE("Variable code"), //
	VARIABLE_NAME("Variable name"), //
	TIME_BEGIN("Time begin"), //
	TIME_END("Time end"), //
	TIMESERIES_CODE("Timeseries code"), //
	WATERML_1_1_DOWNLOAD_URL("WML 2.0"), //
	GRAPH_URL("GRAPH"); //

	private CSV_Field(String header) {
	    this.header = header;
	}

	private String header;

	@Override
	public String toString() {
	    return header;
	}

    }

    public CSV_Field[] getFields() {
	return CSV_Field.values();
    }

    private HashMap<CSV_Field, String> map;

    public HydroCSVTimeSeriesEncoder() {
	this.map = new HashMap<CSV_Field, String>();
    }

    public void add(CSV_Field field, String value) {
	map.put(field, value);

    }

    @Override
    public String toString() {
	String ret = "";
	CSV_Field[] fields = getFields();
	for (CSV_Field csv_Field : fields) {
	    String value = map.get(csv_Field);
	    if (value == null) {
		value = "";
	    }
	    value = value.replace("\n", " ");
	    ret += value.replace(separator, separatorEncoding) + separator;
	}
	return ret.substring(0, ret.length() - 1);
    }
}
