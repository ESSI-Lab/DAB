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
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * Encoding of a single cached value as a WIS2 data item. The same encoding is used for the content of the WIS2 data
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
     * Encodes the given record as a JSON object, with properties in a fixed order. Missing properties are omitted
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
