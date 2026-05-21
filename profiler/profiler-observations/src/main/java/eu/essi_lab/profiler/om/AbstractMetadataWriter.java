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

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Extracts metadata values from OM JSON observations. Column names and field order are defined in
 * {@link MetadataCsvWriter} and {@link MetadataShapefileWriter}.
 */
abstract class AbstractMetadataWriter {

    protected AbstractMetadataWriter() {
    }

    protected static String extractObservationId(JSONObject observation) {
	return observation.optString("id", null);
    }

    protected static String extractObservedPropertyTitle(JSONObject observation) {
	JSONObject op = observation.optJSONObject("observedProperty");
	if (op == null) {
	    return null;
	}
	return op.optString("title", null);
    }

    protected static String extractObservedPropertyUri(JSONObject observation) {
	JSONObject op = observation.optJSONObject("observedProperty");
	if (op == null) {
	    return null;
	}
	if (op.has("href")) {
	    return op.optString("href", null);
	}
	return op.optString("uri", null);
    }

    protected static String extractFeatureId(JSONObject observation) {
	JSONObject foi = observation.optJSONObject("featureOfInterest");
	if (foi == null) {
	    return null;
	}
	if (foi.has("id")) {
	    return foi.optString("id", null);
	}
	return foi.optString("href", null);
    }

    protected static String extractFeatureName(JSONObject observation) {
	JSONObject foi = observation.optJSONObject("featureOfInterest");
	if (foi == null) {
	    return null;
	}
	if (foi.has("name")) {
	    return foi.optString("name", null);
	}
	return foi.optString("title", null);
    }

    protected static String extractLatitude(JSONObject observation) {
	SimpleEntry<BigDecimal, BigDecimal> latLon = coordinates(observation);
	return latLon != null && latLon.getKey() != null ? latLon.getKey().toPlainString() : null;
    }

    protected static String extractLongitude(JSONObject observation) {
	SimpleEntry<BigDecimal, BigDecimal> latLon = coordinates(observation);
	return latLon != null && latLon.getValue() != null ? latLon.getValue().toPlainString() : null;
    }

    protected static String extractAltitude(JSONObject observation) {
	JSONObject foi = observation.optJSONObject("featureOfInterest");
	if (foi == null) {
	    return null;
	}
	JSONObject shape = foi.optJSONObject("shape");
	if (shape == null) {
	    return null;
	}
	JSONArray coords = shape.optJSONArray("coordinates");
	if (coords != null && coords.length() >= 3) {
	    Object alt = coords.get(2);
	    return alt != null ? alt.toString() : null;
	}
	return null;
    }

    private static SimpleEntry<BigDecimal, BigDecimal> coordinates(JSONObject observation) {
	JSONObject foi = observation.optJSONObject("featureOfInterest");
	if (foi == null) {
	    return null;
	}
	JSONFeature feature = new JSONFeature(foi);
	return feature.getLatLonPoint();
    }

    protected static String extractAggregationDuration(JSONObject observation) {
	JSONObject result = observation.optJSONObject("result");
	if (result == null) {
	    return null;
	}
	JSONObject defaultPointMetadata = result.optJSONObject("defaultPointMetadata");
	if (defaultPointMetadata == null) {
	    return null;
	}
	return defaultPointMetadata.optString("aggregationDuration", null);
    }

    protected static String extractInterpolationType(JSONObject observation) {
	JSONObject result = observation.optJSONObject("result");
	if (result == null) {
	    return null;
	}
	JSONObject defaultPointMetadata = result.optJSONObject("defaultPointMetadata");
	if (defaultPointMetadata == null) {
	    return null;
	}
	JSONObject interpolation = defaultPointMetadata.optJSONObject("interpolationType");
	if (interpolation == null) {
	    return null;
	}
	return interpolation.optString("title", interpolation.optString("href", null));
    }

    protected static String extractIntendedObservationSpacing(JSONObject observation) {
	JSONObject result = observation.optJSONObject("result");
	if (result == null) {
	    return null;
	}
	JSONObject metadata = result.optJSONObject("metadata");
	if (metadata == null) {
	    return null;
	}
	return metadata.optString("intendedObservationSpacing", null);
    }

    protected static String extractUom(JSONObject observation) {
	JSONObject result = observation.optJSONObject("result");
	if (result == null) {
	    return null;
	}
	JSONObject defaultPointMetadata = result.optJSONObject("defaultPointMetadata");
	if (defaultPointMetadata == null) {
	    return null;
	}
	return defaultPointMetadata.optString("uom", null);
    }

    protected static String extractPhenomenonBegin(JSONObject observation) {
	JSONObject time = observation.optJSONObject("phenomenonTime");
	if (time == null) {
	    return null;
	}
	return time.optString("begin", null);
    }

    protected static String extractPhenomenonEnd(JSONObject observation) {
	JSONObject time = observation.optJSONObject("phenomenonTime");
	if (time == null) {
	    return null;
	}
	return time.optString("end", null);
    }

    protected static String extractProviderId(JSONObject observation) {
	String sourceId = extractParameter(observation, "sourceId");
	if (sourceId != null && !sourceId.isEmpty()) {
	    return sourceId;
	}
	return extractParameter(observation, "source");
    }

    protected static String extractProviderLabel(JSONObject observation) {
	String label = extractParameter(observation, "source");
	if (label != null && !label.isEmpty()) {
	    return label;
	}
	return null;
    }

    protected static String extractCountry(JSONObject observation) {
	JSONObject foi = observation.optJSONObject("featureOfInterest");
	if (foi == null) {
	    return null;
	}
	return extractParameter(foi, "country");
    }

    protected static String extractParameter(JSONObject observation, String name) {
	JSONArray parameters = observation.optJSONArray("parameter");
	if (parameters == null) {
	    return null;
	}
	for (int i = 0; i < parameters.length(); i++) {
	    JSONObject parameter = parameters.optJSONObject(i);
	    if (parameter != null && name.equals(parameter.optString("name"))) {
		return parameter.optString("value", null);
	    }
	}
	return null;
    }
}
