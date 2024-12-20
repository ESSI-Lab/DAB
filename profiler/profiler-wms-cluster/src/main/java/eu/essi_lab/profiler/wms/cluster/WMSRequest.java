package eu.essi_lab.profiler.wms.cluster;

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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.web.WebRequest;

public abstract class WMSRequest {

    public enum Parameter {
	SERVICE("SERVICE"), //
	REQUEST("REQUEST"), //
	VERSION("VERSION"), //
	LAYERS("LAYERS","LAYER"), //
	STYLES("STYLES"), //
	TRANSPARENT("TRANSPARENT"), //
	BGCOLOR("BGCOLOR"), //
	EXCEPTIONS("EXCEPTIONS"), //
	ELEVATION("ELEVATION"), //	
	FORMAT("FORMAT"), //
	INFO_FORMAT("INFO_FORMAT"), //
	TIME("TIME"), //
	CRS("CRS", "SRS"), //
	BBOX("BBOX"), //
	WIDTH("WIDTH"), //
	HEIGHT("HEIGHT"), //
	FEATURE_COUNT("FEATURE_COUNT"), //
	FILE("FILE"), //
	I("I","X"), //
	J("J","Y"),//
	;

	private String[] keys;

	public String[] getKeys() {
	    return keys;
	}

	private Parameter(String... keys) {
	    this.keys = keys;
	}

    }

    private HashMap<Parameter, String> map = new HashMap<>();

    public String getParameterValue(Parameter parameter) {
	return map.get(parameter);
    }
    public abstract String[] getRequestNames();
    

    public WMSRequest(WebRequest request) {

	String[] names = getRequestNames();

	if (request.isGetRequest()) {

	    Map<String, String[]> servletMap = request.getServletRequest().getParameterMap();
	    for (String key : servletMap.keySet()) {
		String[] values = servletMap.get(key);
		if (values != null && values.length > 0) {
		    for (Parameter parameter : Parameter.values()) {
			for (String parameterKey : parameter.getKeys()) {
			    if (parameterKey.equalsIgnoreCase(key)) {
				String v = values[0];
				if (v != null) {
				    map.put(parameter, v);
				}
			    }
			}
		    }
		}
	    }

	    // check if it is in the path
	    boolean found = false;
	    String nameList = "";
	    for (String name : names) {
		nameList += name + " ";
		if (request.getRequestPath().contains(name)) {
		    found = true;
		    break;
		}
	    }

	    // otherwise check if it is in the KVP parameters
	    String requestValue = map.get(Parameter.REQUEST);
	    if (!found) {

		for (String name : names) {
		    if (requestValue.equalsIgnoreCase(name)) {
			found = true;
			break;
		    }
		}
	    }

	    if (!found) {
		throw new IllegalArgumentException("Request should be one of: " + nameList);
	    }

	} else if (request.isPostRequest()) {

	    try {
		InputStream clone = request.getBodyStream().clone();
		XMLDocumentReader reader = new XMLDocumentReader(clone);

		String xPath = "";
		String nameList = "";
		for (String name : names) {
		    nameList += name + " ";
		    xPath += "//*:" + name + "|";
		}
		xPath = xPath.substring(0, xPath.length() - 1);

		Node mainNode = reader.evaluateNode(xPath);
		if (mainNode == null) {
		    throw new IllegalArgumentException("Missing expected element. One of: " + nameList);
		}
		for (Parameter parameter : Parameter.values()) {
		    String[] keys = parameter.getKeys();
		    for (String key : keys) {
			String value = reader.evaluateString("//*:" + key);
			if (value != null && !value.equals("")) {
			    map.put(parameter, value);
			    break;
			}
			value = reader.evaluateString("//*:" + key.toLowerCase());
			if (value != null && !value.equals("")) {
			    map.put(parameter, value);
			    break;
			}
			value = reader.evaluateString("//*:" + key.toUpperCase());
			if (value != null && !value.equals("")) {
			    map.put(parameter, value);
			    break;
			}
		    }

		}

	    } catch (Exception e) {
		throw new IllegalArgumentException("Error reading XML POST body");
	    }
	} else {
	    throw new IllegalArgumentException("Supported HTTP requests: GET or POST");
	}

	// String path = request.getRequestPath();
	// path = path.substring(path.lastIndexOf("/") + 1);
	// if (!path.equals("wms")) {
	// path = path.replace("urn-uuid-", "urn:uuid:");
	// map.put(Parameter.LAYERS, path);
	// }
    }

}
