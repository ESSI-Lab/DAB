package eu.essi_lab.access.datacache;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.whos.WIS2DataEncoding;

/**
 * Encoding of a single cached value as a WIS2 data item (see {@link WIS2DataEncoding}). The same encoding is used for the content of the WIS2 data
 * notification messages and for the data served at their canonical link, so that the integrity checksum of the
 * notifications matches the served data. For this reason the encoding must be deterministic: fixed properties order,
 * normalized numbers and dates, and no property depending on the request or on the encoding time
 *
 * @author boldrini
 */
public class WIS2DataItem {

    private static final char ID_SEPARATOR = '.';

    private WIS2DataItem() {
    }

    /**
     * @param record
     * @return the identifier of the item of the given record, made of the data identifier of its series (base64url
     *         encoded, so that it can be used in a URL path) and of its time (epoch milliseconds)
     */
    public static String getItemId(DataRecord record) {

	return getItemId(record.getDataIdentifier(), record.getDate());
    }

    /**
     * @param dataIdentifier
     * @param date
     * @return
     */
    public static String getItemId(String dataIdentifier, Date date) {

	String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(dataIdentifier.getBytes(StandardCharsets.UTF_8));

	return encoded + ID_SEPARATOR + date.getTime();
    }

    /**
     * @param itemId
     * @return the data identifier and the time of the given item identifier, if valid
     */
    public static Optional<SimpleEntry<String, Date>> parseItemId(String itemId) {

	if (itemId == null) {
	    return Optional.empty();
	}

	int separator = itemId.lastIndexOf(ID_SEPARATOR);
	if (separator < 1 || separator == itemId.length() - 1) {
	    return Optional.empty();
	}

	try {

	    String dataIdentifier = new String(Base64.getUrlDecoder().decode(itemId.substring(0, separator)), StandardCharsets.UTF_8);
	    Date date = new Date(Long.parseLong(itemId.substring(separator + 1)));

	    return Optional.of(new SimpleEntry<>(dataIdentifier, date));

	} catch (IllegalArgumentException e) {

	    return Optional.empty();
	}
    }

    /**
     * Encodes the given record with the given encoding. For {@link WIS2DataEncoding#OM_JSON} the station of the series
     * is read from the cache
     *
     * @param record
     * @param encoding
     * @param dataCacheConnector
     * @return
     * @throws Exception
     */
    public static String encode(DataRecord record, WIS2DataEncoding encoding, DataCacheConnector dataCacheConnector) throws Exception {

	Optional<StationRecord> station = encoding == WIS2DataEncoding.OM_JSON ? findStation(dataCacheConnector, record.getDataIdentifier())
		: Optional.empty();

	return encode(record, encoding, station);
    }

    /**
     * @param record
     * @param encoding
     * @param station the station of the series, used by {@link WIS2DataEncoding#OM_JSON}
     * @return
     */
    public static String encode(DataRecord record, WIS2DataEncoding encoding, Optional<StationRecord> station) {

	return switch (encoding) {
	case CSV -> encodeCSV(record);
	case OM_JSON -> encodeOMJSON(record, station);
	default -> encode(record);
	};
    }

    /**
     * @param dataCacheConnector
     * @param dataIdentifier
     * @return the station record of the given series, if any
     * @throws Exception
     */
    public static Optional<StationRecord> findStation(DataCacheConnector dataCacheConnector, String dataIdentifier) throws Exception {

	List<StationRecord> stations = dataCacheConnector.getStationsWithProperties(null, 0, 1, true,
		new SimpleEntry<>("dataIdentifier", dataIdentifier));

	return stations.stream().filter(s -> dataIdentifier.equals(s.getDataIdentifier())).findFirst();
    }

    /**
     * Encodes the given record as a JSON object ({@link WIS2DataEncoding#JSON}), with properties in a fixed order.
     * Missing properties are omitted
     *
     * @param record
     * @return
     */
    public static String encode(DataRecord record) {

	StringBuilder builder = new StringBuilder("{");

	append(builder, "id", JSONObject.quote(getItemId(record)));
	append(builder, "source", quote(record.getSourceIdentifier()));
	append(builder, "series", quote(record.getDataIdentifier()));
	append(builder, "observedProperty", quote(record.getObservedProperty()));
	append(builder, "observedPropertyUri", quote(record.getObservedPropertyURI()));
	append(builder, "uom", quote(record.getUom()));
	append(builder, "phenomenonTime", JSONObject.quote(ISO8601DateTimeUtils.getISO8601DateTime(record.getDate())));
	append(builder, "result", number(record.getValue()));
	append(builder, "quality", quote(record.getQuality()));

	LatitudeLongitude location = record.getLatitudeLongitude();
	if (location != null && location.getLatitude() != null && location.getLongitude() != null) {

	    append(builder, "geometry", "{\"type\":\"Point\",\"coordinates\":[" + number(location.getLongitude()) + ","
		    + number(location.getLatitude()) + "]}");
	}

	return builder.append("}").toString();
    }

    /**
     * Encodes the given record ({@link WIS2DataEncoding#CSV}) as a header line followed by a line with the value, both
     * ending with a line feed. Missing values are empty, values with commas, quotes or line breaks are quoted (RFC 4180)
     *
     * @param record
     * @return
     */
    static String encodeCSV(DataRecord record) {

	LatitudeLongitude location = record.getLatitudeLongitude();
	boolean located = location != null && location.getLatitude() != null && location.getLongitude() != null;

	String[] values = new String[] { //
		getItemId(record), //
		record.getSourceIdentifier(), //
		record.getDataIdentifier(), //
		record.getObservedProperty(), //
		record.getObservedPropertyURI(), //
		record.getUom(), //
		ISO8601DateTimeUtils.getISO8601DateTime(record.getDate()), //
		number(record.getValue()), //
		record.getQuality(), //
		located ? number(location.getLongitude()) : null, //
		located ? number(location.getLatitude()) : null };

	return String.join(",", CSV_HEADER) + "\n" + String.join(",", Arrays.stream(values).map(WIS2DataItem::csv).toArray(String[]::new))
		+ "\n";
    }

    private static final String[] CSV_HEADER = new String[] { "id", "source", "series", "observedProperty", "observedPropertyUri", "uom",
	    "phenomenonTime", "result", "quality", "longitude", "latitude" };

    /**
     * @param value
     * @return the given value as a CSV field
     */
    private static String csv(String value) {

	if (value == null || value.isBlank()) {
	    return "";
	}

	String trimmed = value.trim();

	if (trimmed.contains(",") || trimmed.contains("\"") || trimmed.contains("\n") || trimmed.contains("\r")) {
	    return "\"" + trimmed.replace("\"", "\"\"") + "\"";
	}

	return trimmed;
    }

    /**
     * Encodes the given record ({@link WIS2DataEncoding#OM_JSON}) as a time series observation with a single point,
     * with the same structure of the observations provided by the DAB OM API, and properties in a fixed order. Missing
     * properties are omitted
     *
     * @param record
     * @param station the station of the series, providing the feature of interest
     * @return
     */
    static String encodeOMJSON(DataRecord record, Optional<StationRecord> station) {

	String time = JSONObject.quote(ISO8601DateTimeUtils.getISO8601DateTime(record.getDate()));

	String featureOfInterest = null;
	if (station.isPresent() && quote(station.get().getPlatformIdentifier()) != null) {
	    featureOfInterest = object("href", quote(station.get().getPlatformIdentifier()), //
		    "title", quote(station.get().getPlatformName()));
	}

	String observedProperty = null;
	if (quote(record.getObservedPropertyURI()) != null || quote(record.getObservedProperty()) != null) {
	    observedProperty = object("href", quote(record.getObservedPropertyURI()), //
		    "title", quote(record.getObservedProperty()));
	}

	String parameter = null;
	if (quote(record.getSourceIdentifier()) != null) {
	    parameter = "[" + object("name", JSONObject.quote("sourceId"), "value", quote(record.getSourceIdentifier())) + "]";
	}

	String pointMetadata = null;
	if (quote(record.getQuality()) != null) {
	    pointMetadata = object("quality", object("term", quote(record.getQuality())));
	}

	String value = number(record.getValue());

	String point = object("time", object("instant", time), //
		"value", value == null ? "null" : value, //
		"metadata", pointMetadata);

	String result = object("metadata", "{}", //
		"defaultPointMetadata", object("uom", quote(record.getUom())), //
		"points", "[" + point + "]");

	return object("id", quote(record.getDataIdentifier()), //
		"type", JSONObject.quote("TimeSeriesObservation"), //
		"featureOfInterest", featureOfInterest, //
		"observedProperty", observedProperty, //
		"phenomenonTime", object("begin", time, "end", time), //
		"parameter", parameter, //
		"result", result);
    }

    /**
     * @param keysAndValues keys followed by their encoded values; null values are omitted
     * @return a JSON object with the given properties, in the given order
     */
    private static String object(String... keysAndValues) {

	StringBuilder builder = new StringBuilder("{");

	for (int i = 0; i < keysAndValues.length; i += 2) {
	    append(builder, keysAndValues[i], keysAndValues[i + 1]);
	}

	return builder.append("}").toString();
    }

    /**
     * @param builder
     * @param key
     * @param value the encoded value, or null to omit the property
     */
    private static void append(StringBuilder builder, String key, String value) {

	if (value == null) {
	    return;
	}

	if (builder.length() > 1) {
	    builder.append(",");
	}

	builder.append(JSONObject.quote(key)).append(":").append(value);
    }

    /**
     * @param value
     * @return
     */
    private static String quote(String value) {

	return value == null || value.isBlank() ? null : JSONObject.quote(value.trim());
    }

    /**
     * @param value
     * @return the given number without trailing zeros, so that e.g. 2.370 and 2.37 are encoded in the same way
     */
    private static String number(BigDecimal value) {

	if (value == null) {
	    return null;
	}

	return value.signum() == 0 ? "0" : value.stripTrailingZeros().toPlainString();
    }
}
