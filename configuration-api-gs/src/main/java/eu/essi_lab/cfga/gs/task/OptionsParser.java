package eu.essi_lab.cfga.gs.task;

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

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OptionsParser {

    public static <E extends Enum<E> & OptionsKey> EnumMap<E, String> parseOptions(String input, Class<E> enumClass) {

	EnumMap<E, String> options = new EnumMap<>(enumClass);

        // Build map from normalized enum name to enum constant
        Map<String, E> normalizedKeyMap = Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(e -> normalizeKey(e.name()), e -> e));

        for (String line : input.split("\\n")) {
            String[] parts = line.trim().split("=", 2);
            if (parts.length == 2) {
                String rawKey = parts[0].trim();
                String value = parts[1].trim();

                String normalized = normalizeKey(rawKey);
                E enumKey = normalizedKeyMap.get(normalized);
                if (enumKey != null) {
                    options.put(enumKey, value);
                }
            }
        }

        return options;
    }

    private static String normalizeKey(String key) {
	return key.replaceAll("[_\\s]", "") // remove underscores and spaces
		.toUpperCase(); // convert to uppercase
    }
}
