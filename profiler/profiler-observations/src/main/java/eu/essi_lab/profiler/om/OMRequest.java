package eu.essi_lab.profiler.om;

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
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.LayerFeatureRetrieval;

/**
 * @author boldrini
 */
public class OMRequest {

    public enum APIParameters {
	FORMAT("format"), //
	USE_CACHE("useCache"), //
	INCLUDE_VALUES("includeValues", "includeData"), //
	ASYNCH_DOWNLOAD_NAME("asynchDownloadName"), //
	BEGIN_DATE("beginDate", "startDate", "beginTime", "startTime", "begin", "beginPosition"), //
	END_DATE("endDate", "endTime", "end", "endPosition"), //
	PLATFORM_CODE("sampledFeature", "feature", "featureIdentifier", "featureId", "monitoringPoint", "monitoringPointIdentifier",
		"platform", "platformCode", "site", "location", "siteCode"), //
	ONTOLOGY("ontology"), //
	ID("id"), //
	OBSERVED_PROPERTY("observedProperty"), //
	OBSERVED_PROPERTY_URI("observedPropertyUri", "observedPropertyHref"), //
	AGGREGATION_PERIOD("aggregationDuration", "aggregationPeriod"), //
	INTENDED_OBSERVATION_SPACING("intendedObservationSpacing", "timeResolution"), //
	TIME_INTERPOLATION("timeInterpolation"), //
	VARIABLE("variable", "varCode", "variableCode"), //
	SPATIAL_RELATION("spatialRelation"), //
	PREDEFINED_LAYER("predefinedLayer"), //
	OBSERVATION("observationIdentifier", "observationId", "observation"), //
	OUTPUT_PROPERTIES("outputProperties"), //
	E_MAIL_NOTIFICATIONS("eMailNotifications"), //
	COUNTRY("country"), //
	PROVIDER("provider"), //
	OFFSET("offset", "start"), //
	LIMIT("maxRecords", "limit", "count"), //
	WEST("west", "xmin", "minx"), //
	SOUTH("south", "ymin", "miny"), //
	EAST("east", "xmax", "maxx"), //
	NORTH("north", "ymax", "maxy"), //
	PROPERTY("property"), //
	RESUMPTION_TOKEN("resumptionToken")

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
    private static TimeFormatConverter timeFormatConverter = null;
    private HashMap<String, String> actualParametersMap = new HashMap<>();

    public String getParameterValue(APIParameters parameter) {
	return map.get(parameter);
    }

    public Set<Entry<String, String>> getActualParameters() {
	return actualParametersMap.entrySet();
    }
    
    private boolean asynchDownloadRequest = false;

    public OMRequest(WebRequest request) {

	String method = request.getServletRequest().getMethod();
	if (method.toLowerCase().equals("put")) {
	    asynchDownloadRequest = true;
	}
	
	if (timeFormatConverter == null) {
	    timeFormatConverter = new TimeFormatConverter();
	}

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

    public BondOperator getSpatialRelation() {
	String relation = getParameterValue(APIParameters.SPATIAL_RELATION);
	if (relation==null) {
	    return null;
	}
	switch (relation.toLowerCase()) {
	case "contain":
	case "contains":
	    return BondOperator.CONTAINS;
	case "intersect":
	case "intersects":
	    return BondOperator.INTERSECTS;
	default:
	    return null;
	}
    }

    public Optional<String> getPredefinedLayer() {
	String pred = getParameterValue(APIParameters.PREDEFINED_LAYER);
	if (pred == null || pred.isEmpty()) {
	    return Optional.empty();
	}
	String wkt = LayerFeatureRetrieval.getInstance().getFeature(pred);
	if (wkt != null) {
	    return Optional.of(wkt);
	}else {
	    return Optional.empty();
	}
    }

    public Optional<SpatialBond> getSpatialBond() {
	Double w = null;
	Double s = null;
	Double e = null;
	Double n = null;
	try {

	    String west = getParameterValue(APIParameters.WEST);
	    if (west != null && !west.equals("")) {
		w = Double.parseDouble(west);
	    } else {
		w = -180.;
	    }
	    String south = getParameterValue(APIParameters.SOUTH);
	    if (south != null && !south.equals("")) {
		s = Double.parseDouble(south);
	    } else {
		s = -90.;
	    }
	    String east = getParameterValue(APIParameters.EAST);
	    if (east != null && !east.equals("")) {
		e = Double.parseDouble(east);
	    } else {
		e = 180.;
	    }
	    String north = getParameterValue(APIParameters.NORTH);
	    if (north != null && !north.equals("")) {
		n = Double.parseDouble(north);
	    } else {
		n = 90.;
	    }

	    SpatialExtent extent = new SpatialExtent(s, w, n, e);
	    if (west == null && north == null && south == null && east == null)
		return Optional.empty();

	    // we are interested on a specific area
	    SpatialBond areaBond = BondFactory.createSpatialEntityBond(BondOperator.CONTAINS, extent);
	    return Optional.of(areaBond);

	} catch (Exception ex) {
	}

	return Optional.empty();

    }

    public Optional<SimpleValueBond> getBeginBond() {
	try {
	    String beginString = getParameterValue(APIParameters.BEGIN_DATE);
	    if (beginString != null && !beginString.equals("")) {
		beginString = ISO8601DateTimeUtils.getISO8601DateTime(timeFormatConverter.convertToJavaDate(beginString));
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

	    String endString = getParameterValue(APIParameters.END_DATE);
	    if (endString != null && !endString.equals("")) {
		endString = ISO8601DateTimeUtils.getISO8601DateTime(timeFormatConverter.convertToJavaDate(endString));
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

    public boolean isAsynchDownloadRequest() {
	return asynchDownloadRequest;
    }



}
