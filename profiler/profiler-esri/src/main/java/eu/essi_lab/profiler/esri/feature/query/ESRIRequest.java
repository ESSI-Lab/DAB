package eu.essi_lab.profiler.esri.feature.query;

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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.messages.web.WebRequest;

public class ESRIRequest {

    private String path;
    private Map<String, String> parameters = new HashMap<>();

    public String getParameter(String name) {
	return parameters.get(name.toLowerCase());
    }
    
    public ESRIRequest(WebRequest request) {
	this.path = request.getServletRequest().getPathInfo();

	try {
	    Map<String, String[]> map = request.getServletRequest().getParameterMap();
	    Set<Entry<String, String[]>> entrySet = map.entrySet();
	    for (Entry<String, String[]> entry : entrySet) {
		String value = null;
		if (entry.getValue().length > 0) {
		    value = entry.getValue()[0];
		}
		parameters.put(entry.getKey().toLowerCase(), value);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	ClonableInputStream stream = request.getBodyStream();
	if (stream != null) {
	    InputStream clone = stream.clone();
	    if (clone != null) {
		try {
		    String str = IOUtils.toString(clone, StandardCharsets.UTF_8);
		    String[] split;
		    if (str.contains("&")) {
			split = str.split("&");
		    } else {
			split = new String[] { str };
		    }
		    for (String s : split) {
			if (s != null) {
			    if (s.contains("=")) {
				String[] tlit = s.split("=");
				String name = tlit[0].toLowerCase();
				if (tlit.length > 1) {
				    String value = URLDecoder.decode(tlit[1], "UTF-8");
				    parameters.put(name, value);
				}
			    }
			}
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}

    }

    public boolean isQuery() {
	if (path.contains("?")) {
	    path = path.substring(0, path.indexOf("?"));
	}
	if (path.endsWith("/query")) {
	    return true;
	}
	path = path.substring(0, path.lastIndexOf("/"));

	return path.endsWith("/query");
    }

    public boolean isCount() {
	String countOnly = parameters.get("returncountonly");
	if (countOnly != null && countOnly.equals("true")) {
	    return true;
	} else {
	    return false;
	}
    }

    public boolean isIdentifiersOnly() {
	String idsOnly = parameters.get("returnidsonly");
	if (idsOnly != null && idsOnly.equals("true")) {
	    return true;
	} else {
	    return false;
	}
    }

    public boolean isDistinctValues() {
	String parameterValue = parameters.get("returndistinctvalues");
	if (parameterValue != null && parameterValue.equals("true")) {
	    return true;
	} else {
	    return false;
	}
    }

}
