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

package eu.essi_lab.profiler.sta;

import java.math.BigDecimal;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.stax.GIResourceParser;
import org.json.JSONObject;

/**
 * Maps harmonized metadata (via GIResourceParser) to STA entity JSON objects.
 * Centralizes parsing logic used across multiple handlers.
 */
public final class STAResourceMapper {

    private STAResourceMapper() {
    }

    /**
     * Maps a resource to a STA Datastream JSON object.
     *
     * @param parser  parsed harmonized metadata
     * @param baseUrl base URL for links
     * @return Datastream JSON, or null if id is missing
     */
    public static JSONObject datastreamFromParser(GIResourceParser parser, String baseUrl) {
	return datastreamFromParser(parser, baseUrl, null);
    }

    /**
     * Maps a resource to a STA Datastream JSON object, with optional platform/thing id override
     * (e.g. when expanding Datastreams for a known Thing).
     *
     * @param parser           parsed harmonized metadata
     * @param baseUrl          base URL for links
     * @param platformIdOverride when non-null, used instead of parser.getUniquePlatformCode()
     * @return Datastream JSON, or null if id is missing
     */
    public static JSONObject datastreamFromParser(GIResourceParser parser, String baseUrl, String platformIdOverride) {
	String id = parser.getOnlineId();
	if (id == null) {
	    return null;
	}
	String name = parser.getTitle();
	String description = parser.getAbstract();

	String phenomenonTime = "";
	String begin = parser.getTmpExtentBegin();
	String end = parser.getTmpExtentEnd();
	String endNow = parser.getTmpExtentEndNow();
	if (endNow != null && endNow.equals("true")) {
	    end = ISO8601DateTimeUtils.getISO8601DateTime();
	}
	if (begin != null && end != null) {
	    phenomenonTime = begin + "/" + end;
	} else if (begin != null) {
	    phenomenonTime = begin;
	} else if (end != null) {
	    phenomenonTime = end;
	}
	BigDecimal lon = null;
	BigDecimal lat = null;
	if (parser.getBBOX() != null) {
	    lon = parser.getBBOX().getBigDecimalWest();
	    lat = parser.getBBOX().getBigDecimalNorth();
	}
	String unitName = parser.getUnits();
	String unitSymbol = parser.getUnitsAbbreviation();
	if (unitSymbol == null || unitSymbol.isEmpty()) {
	    unitSymbol = unitName;
	}
	JSONObject properties = new JSONObject();
	properties.put("resultType", "Timeseries");
	String platformId = platformIdOverride != null ? platformIdOverride : parser.getUniquePlatformCode();
	if (platformId != null) {
	    properties.put("platformId", platformId);
	}
	return STAJsonWriter.datastream(id, name, description,
		"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
		unitName, unitSymbol, null, lon, lat, phenomenonTime, properties, platformId, baseUrl);
    }

    /**
     * Maps a resource to a STA Thing JSON object.
     *
     * @param parser  parsed harmonized metadata
     * @param baseUrl base URL for links
     * @return Thing JSON, or null if id is missing
     */
    public static JSONObject thingFromParser(GIResourceParser parser, String baseUrl) {
	String id = parser.getUniquePlatformCode();
	if (id == null) {
	    return null;
	}
	String name = parser.getPlatformName();
	if (name == null) {
	    name = id;
	}
	return STAJsonWriter.thing(id, name, "", baseUrl);
    }

    /**
     * Maps a resource to a STA ObservedProperty JSON object.
     *
     * @param parser  parsed harmonized metadata
     * @param baseUrl base URL for links
     * @return ObservedProperty JSON, or null if attributeCode is missing
     */
    public static JSONObject observedPropertyFromParser(GIResourceParser parser, String baseUrl) {
	String uniqueAttributeId = parser.getAttributeCode();
	if (uniqueAttributeId == null || uniqueAttributeId.isEmpty()) {
	    return null;
	}
	long id = observedPropertyId(uniqueAttributeId);
	String name = parser.getAttributeName();
	if (name == null || name.isEmpty()) {
	    name = uniqueAttributeId;
	}
	String description = parser.getAttributeDescription();
	if (description == null) {
	    description = "";
	}
	String definition = parser.getAttributeURI();
	if (definition == null) {
	    definition = "";
	}
	JSONObject props = new JSONObject();
	props.put("variableCode", uniqueAttributeId);
	return STAJsonWriter.observedProperty(id, name, description, definition, props, baseUrl);
    }

    /**
     * Computes a numeric id for an ObservedProperty from its unique attribute identifier.
     */
    public static long observedPropertyId(String uniqueAttributeId) {
	if (uniqueAttributeId == null || uniqueAttributeId.isEmpty()) {
	    return 0;
	}
	long h = 0;
	for (char c : uniqueAttributeId.toCharArray()) {
	    h = 31 * h + c;
	}
	return Math.abs(h);
    }
}
