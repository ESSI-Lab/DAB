package eu.essi_lab.profiler.wof;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.w3c.dom.Node;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author boldrini
 */
public abstract class WOFRequest {

    public enum Parameter {
	REQUEST("request"), //
	FORMAT("format"), //
	CONCEPT_KEYWORD("conceptKeyword"), //
	NETWORK_IDS("networkIDs"), //
	BEGIN_DATE("beginDate", "startDate", "beginTime", "startTime"), //
	END_DATE("endDate", "endTime"), //
	SITE_CODE("site", "location", "siteCode"), //
	VARIABLE("variable", "varCode", "variableCode"), //
	START("start"), //
	COUNT("count"), //
	HIDE_NO_DATA_VALUES("hideNoDataValues"), //
	INCLUDE_SERIES("IncludeSeries"), //
	WEST("west", "xmin", "minx"), //
	SOUTH("south", "ymin", "miny"), //
	EAST("east", "xmax", "maxx"), //
	NORTH("north", "ymax", "maxy");

	private String[] keys;

	public String[] getKeys() {
	    return keys;
	}

	private Parameter(String... keys) {
	    this.keys = keys;
	}

    }

    private HashMap<Parameter, String> map = new HashMap<>();
    private static TimeFormatConverter timeFormatConverter = null;
    private HashMap<String, String> actualParametersMap = new HashMap<>();

    public String getParameterValue(Parameter parameter) {
	return map.get(parameter);
    }

    public boolean isIncludeSeries() {
	String includeSeries = getParameterValue(Parameter.INCLUDE_SERIES);
	return Boolean.parseBoolean(includeSeries);
    }

    public abstract String[] getRequestNames();

    public Set<Entry<String, String>> getActualParameters() {
	return actualParametersMap.entrySet();
    }

    public String getActualRequestName() {
	return map.get(Parameter.REQUEST);
    }

    public WOFRequest(WebRequest request) {

	if (timeFormatConverter == null) {
	    timeFormatConverter = new TimeFormatConverter();
	}

	String[] names = getRequestNames();

	for (Parameter parameter : Parameter.values()) {
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

	if (request.isGetRequest()) {

	    String requestValue = request.getServletRequest().getParameter("request");
	    map.put(Parameter.REQUEST, requestValue);

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
	    if (!found) {

		for (String name : names) {
		    if (requestValue.equals(name)) {
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
			    actualParametersMap.put(key, value);
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
    }

    public Optional<SpatialBond> getSpatialBond() {

	String west = getParameterValue(Parameter.WEST);
	if (west == null || west.equals("")) {
	    return Optional.empty();
	}
	String south = getParameterValue(Parameter.SOUTH);
	if (south == null || south.equals("")) {
	    return Optional.empty();
	}
	String east = getParameterValue(Parameter.EAST);
	if (east == null || east.equals("")) {
	    return Optional.empty();
	}
	String north = getParameterValue(Parameter.NORTH);
	if (north == null || north.equals("")) {
	    return Optional.empty();
	}

	try {
	    double w = Double.parseDouble(west);
	    double s = Double.parseDouble(south);
	    double e = Double.parseDouble(east);
	    double n = Double.parseDouble(north);
	    SpatialExtent extent = new SpatialExtent(s, w, n, e);

	    // we are interested on a specific area
	    SpatialBond areaBond = BondFactory.createSpatialEntityBond(BondOperator.CONTAINS, extent);
	    return Optional.of(areaBond);

	} catch (Exception e) {
	}

	return Optional.empty();

    }

    public Optional<SimpleValueBond> getBeginBond() {
	try {
	    String beginString = getParameterValue(Parameter.BEGIN_DATE);
	    if (beginString != null && !beginString.equals("")) {
		beginString = timeFormatConverter.convertGetSeriesTimeFormatToISO8601(beginString);
		if (beginString.length() > 0) {
		    // here intersection is chosen
		    SimpleValueBond ret = BondFactory.createSimpleValueBond(//
			    BondOperator.GREATER_OR_EQUAL, //
			    MetadataElement.TEMP_EXTENT_END, //
			    beginString);//

		    return Optional.of(ret);
		}

	    }

	} catch (Exception e) {
	}

	return Optional.empty();
    }

    public Optional<SimpleValueBond> getEndBond() {
	try {

	    String endString = getParameterValue(Parameter.END_DATE);
	    if (endString != null && !endString.equals("")) {
		endString = timeFormatConverter.convertGetSeriesTimeFormatToISO8601(endString);
		if (endString.length() > 0) {
		    // here intersection is chosen
		    SimpleValueBond ret = BondFactory.createSimpleValueBond(//
			    BondOperator.LESS_OR_EQUAL, //
			    MetadataElement.TEMP_EXTENT_BEGIN, //
			    endString);// co

		    return Optional.of(ret);
		}
	    }

	} catch (Exception e) {
	}

	return Optional.empty();
    }

    public Optional<SimpleValueBond> getKeywordBond() {
	try {
	    String conceptString = getParameterValue(Parameter.CONCEPT_KEYWORD).toLowerCase();
	    if (conceptString.length() > 0 && !conceptString.equalsIgnoreCase("all")) {

		SimpleValueBond ret = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ATTRIBUTE_TITLE, conceptString);//

		return Optional.of(ret);
	    }
	} catch (Exception e) {
	}

	return Optional.empty();
    }

    public Optional<Bond> getSourcesBond() {
	try {
	    String networkIds = getParameterValue(Parameter.NETWORK_IDS);
	    if (networkIds.length() > 0) {

		List<GSSource> allSources = ConfigurationWrapper.getAllSources();

		List<Bond> operands = new ArrayList<>();
		String[] networkIdArray = networkIds.split(",");

		f1: for (GSSource source : allSources) {

		    String sourceId = source.getUniqueIdentifier();
		    String cuahsiId = HISCentralProfiler.generateUniqueIdFromString(source.getUniqueIdentifier());

		    f2: for (String networkId : networkIdArray) {

			if (cuahsiId.equals(networkId)) {
			    operands.add(BondFactory.createSourceIdentifierBond(sourceId));//
			    if (operands.size() == networkIdArray.length) {
				break f1;
			    } else {
				break f2;
			    }
			}
		    }
		}
		switch (operands.size()) {
		case 0:
		    return Optional.empty();
		case 1:
		    return Optional.of(operands.get(0));
		default:
		    return Optional.of(BondFactory.createOrBond(operands));
		}

	    }
	} catch (Exception e) {
	}

	return Optional.empty();
    }

}
