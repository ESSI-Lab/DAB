package eu.essi_lab.lib.whos;

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


import java.util.Arrays;
import java.util.Optional;

import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * Encodings of the data items published to WIS2: the content of the data notification messages and the data served at
 * their canonical link
 *
 * @author boldrini
 */
public enum WIS2DataEncoding implements LabeledEnum {

    /**
     * A flat JSON object
     */
    JSON("JSON", "json", "application/json"),

    /**
     * A header line followed by a line with the value
     */
    CSV("CSV", "csv", "text/csv"),

    /**
     * A time series observation, as provided by the DAB OM API
     */
    OM_JSON("OM-JSON", "omjson", "application/json");

    private final String label;
    private final String id;
    private final String mediaType;

    WIS2DataEncoding(String label, String id, String mediaType) {

	this.label = label;
	this.id = id;
	this.mediaType = mediaType;
    }

    @Override
    public String getLabel() {

	return label;
    }

    @Override
    public String toString() {

	return getLabel();
    }

    /**
     * @return the identifier used as value of the format parameter of the item URLs (e.g. f=csv)
     */
    public String getId() {

	return id;
    }

    /**
     * @return
     */
    public String getMediaType() {

	return mediaType;
    }

    /**
     * @param id
     * @return the encoding with the given identifier, ignoring case
     */
    public static Optional<WIS2DataEncoding> fromId(String id) {

	return Arrays.stream(values()).filter(e -> e.getId().equalsIgnoreCase(id)).findFirst();
    }
}
