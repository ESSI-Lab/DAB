package eu.essi_lab.profiler.ckan;

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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.LayerFeatureRetrieval;

/**
 * @author boldrini
 */
public class CKANRequest {

    public enum APIParameters {
	LIMIT("limit"), //
	OFFSET("offset", "start"), //

	;

	private String[] keys;

	public String[] getKeys() {
	    return keys;
	}

	private APIParameters(String... keys) {
	    this.keys = keys;
	}

    }

    private HashMap<APIParameters, String> map = new HashMap<>();
    private HashMap<String, String> actualParametersMap = new HashMap<>();

    public String getParameterValue(APIParameters parameter) {
	return map.get(parameter);
    }

    public Set<Entry<String, String>> getActualParameters() {
	return actualParametersMap.entrySet();
    }

    public CKANRequest(WebRequest request) {

	for (APIParameters parameter : APIParameters.values()) {
	    String[] keys = parameter.getKeys();
	    for (String key : keys) {
		String value = request.getServletRequest().getParameter(key);
		if (value != null && !value.equals("")) {
		    map.put(parameter, value);
		    actualParametersMap.put(key, value);
		    break;
		}
	    }

	}

	if (request.isPostRequest()) {

	    try {
		InputStream clone = request.getBodyStream().clone();
		XMLDocumentReader reader = new XMLDocumentReader(clone);

		String xPath = "";
		xPath = xPath.substring(0, xPath.length() - 1);

		for (APIParameters parameter : APIParameters.values()) {
		    String[] keys = parameter.getKeys();
		    for (String key : keys) {
			String value = reader.evaluateString("//*:" + key);
			if (value != null && !value.equals("")) {
			    map.put(parameter, value);
			    actualParametersMap.put(key, value);
			    break;
			}
		    }

		}

	    } catch (Exception e) {
		throw new IllegalArgumentException("Error reading XML POST body");
	    }
	}
    }

}
