package eu.essi_lab.profiler.arpa;

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

import java.util.HashMap;

import eu.essi_lab.messages.web.WebRequest;

public class HydroCSVParameters {

    public enum HydroCSVParameter {

	DISTINCT_BY("distinctBy"), //
	RESULTS("results"), //
	TYPE("type"), // views or time_series (default when absent)
	START("start"), //
	COUNT("count"), //
	SITE_CODE("siteCode"), //
	VARIABLE_CODE("variableCode"), //
	TIMESERIES_CODE("timeseriesCode");

	String key;

	private HydroCSVParameter(String key) {
	    this.key = key;
	}

	@Override
	public String toString() {
	    return key;
	}

    }

    public static final String HITS = "hits";

    private HashMap<HydroCSVParameter, String> map;

    public String getParameter(HydroCSVParameter key) {
	return map.get(key);
    }

    public HydroCSVParameters(WebRequest request) {

	HashMap<HydroCSVParameter, String> map = new HashMap<>();
	for (HydroCSVParameter parameter : HydroCSVParameter.values()) {
	    String[] values = request.getServletRequest().getParameterMap().get(parameter.key);
	    if (values == null || values.length == 0) {

	    } else {
		map.put(parameter, values[0]);
	    }
	}

	// checking mandatory parameters. Either:
	// 0) type = views
	// 1) results=hits
	// 2) start=... & count=...
	// must be present

	String type = map.get(HydroCSVParameter.TYPE);
	String results = map.get(HydroCSVParameter.RESULTS);
	String start = map.get(HydroCSVParameter.START);
	String count = map.get(HydroCSVParameter.COUNT);
	if (type != null && type.equals("views")) {
	    // o.k.
	} else if (results != null && results.equals(HITS)) {
	    // o.k.
	} else if (start != null && count != null) {
	    // o.k.
	} else {
	    throw new IllegalArgumentException(
		    "Mandatory parameter not found: either type=views or results=hits or start and count parameters must be present");
	}

	this.map = map;

    }

}
