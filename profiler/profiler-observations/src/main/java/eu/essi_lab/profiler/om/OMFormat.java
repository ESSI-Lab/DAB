package eu.essi_lab.profiler.om;

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

public enum OMFormat {
    CSV(".csv"), JSON(".json"), SHAPEFILE(".zip"), NETCDF(".nc"), WATERML_1(".xml"), WATERML_2(".xml");

    private String extension;

    private OMFormat(String extension) {
	this.extension = extension;
    }

    public static String stringOptions() {
	String ret = "";
	for (String str : stringValues()) {
	    ret += str + ",";
	}
	if (ret.endsWith(",")) {
	    ret = ret.substring(0, ret.length() - 1);
	}
	return ret;
    }

    public static String[] stringValues() {
	String[] ret = new String[values().length];
	for (int i = 0; i < values().length; i++) {
	    OMFormat f = values()[i];
	    ret[i] = f.name();
	}
	return ret;
    }

    public static OMFormat decode(String format) {
	if (format == null) {
	    return null;
	}
	format = format.toLowerCase();
	if (format.contains("csv")) {
	    return CSV;
	}
	if (format.contains("json")) {
	    return JSON;
	}
	if (format.contains("shp") || format.contains("shape")) {
	    return SHAPEFILE;
	}
	if (format.contains("wml") || format.contains("water")) {
	    if (format.contains("2")) {
		return WATERML_2;
	    } else {
		return WATERML_1;
	    }
	}
	if (format.contains("nc") || format.contains("cdf")) {
	    return NETCDF;
	}
	return null;
    }

    public String getExtension() {
	return extension;
    }

    public String getLabel() {
	switch (this) {
	case CSV:
	    return "CSV";
	case JSON:
	    return "JSON";
	case SHAPEFILE:
	    return "Shapefile";
	case NETCDF:
	    return "NetCDF";
	case WATERML_1:
	    return "WaterML 1.0";
	case WATERML_2:
	    return "WaterML 2.0";
	default:
	    return name();
	}
    }

    /**
     * Comma-separated capabilities for bulk download: {@code data}, {@code metadata}, or both.
     */
    public String getSupport() {
	switch (this) {
	case CSV:
	case JSON:
	    return "data,metadata";
	case SHAPEFILE:
	    return "metadata";
	case NETCDF:
	case WATERML_1:
	case WATERML_2:
	    return "data";
	default:
	    return "data";
	}
    }
}
