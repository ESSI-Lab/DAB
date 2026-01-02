/**
 * 
 */
package eu.essi_lab.model.resource;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Fabrizio
 */
public class BNHSPropertyReader {

    /**
     * 
     */
    public static final String SEPARATOR = "\t";

    /**
     * @param resource
     * @return
     */
    public static List<SimpleEntry<BNHSProperty, String>> readProperties(GSResource resource) {

	return Arrays.asList(BNHSProperty.values()).//
		stream().//
		map(e -> new SimpleEntry<BNHSProperty, Optional<String>>(e, readProperty(resource, e))).//
		filter(e -> e.getValue().isPresent()).//
		map(e -> new SimpleEntry<BNHSProperty, String>(e.getKey(), e.getValue().get())).//
		collect(Collectors.toList());
    }

    /**
     * @param resource
     * @return
     */
    public static List<SimpleEntry<BNHSProperty, String>> readPropertiesAlsoNulls(GSResource resource) {

	return Arrays.asList(BNHSProperty.values()).//
		stream().//
		map(e -> new SimpleEntry<BNHSProperty, Optional<String>>(e, readProperty(resource, e))).//
		map(e -> new SimpleEntry<BNHSProperty, String>(e.getKey(), e.getValue().isPresent() ? e.getValue().get() : null)).//
		collect(Collectors.toList());
    }

    /**
     * @param resource
     * @param property
     * @return
     */
    public static Optional<String> readProperty(GSResource resource, BNHSProperty property) {

	Optional<String> bnhsInfo = resource.getExtensionHandler().getBNHSInfo();

	if (bnhsInfo.isPresent()) {

	    return readProperty(bnhsInfo.get(), property);
	}

	return Optional.empty();
    }

    /**
     * @param info
     * @param property
     * @return
     */
    private static Optional<String> readProperty(String info, BNHSProperty property) {

	info = info.replace(SEPARATOR + SEPARATOR, SEPARATOR + "NONE" + SEPARATOR);
	String[] split = info.split("\t");
	for (int i = 0; i < split.length; i++) {
	    String label = split[i];
	    if (label.equals(property.getLabel())) {

		String value = split[i + 1];
		if (!value.equals("NONE")) {
		    return Optional.of(value);
		}
	    }
	}

	return Optional.empty();
    }
}
