package eu.essi_lab.accessor.hiscentral.friuli;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Variable-code to units mapping for the Friuli (FVG) HIS-Central connector.
 */
public final class HISCentralFriuliVariableUnits {

    private static final String UNITS_RESOURCE = "/fvg-units.csv";

    private static final Map<String, String> UNITS_BY_CODE = loadUnits();

    private HISCentralFriuliVariableUnits() {
    }

    /**
     * @param variableCode provider variable identifier (measure {@code nome})
     * @return mapped units, if known
     */
    public static Optional<String> getUnits(String variableCode) {

	if (variableCode == null || variableCode.isEmpty()) {
	    return Optional.empty();
	}

	return Optional.ofNullable(UNITS_BY_CODE.get(variableCode));
    }

    private static Map<String, String> loadUnits() {

	Map<String, String> unitsByCode = new HashMap<>();

	try (InputStream stream = HISCentralFriuliVariableUnits.class.getResourceAsStream(UNITS_RESOURCE)) {

	    if (stream == null) {
		GSLoggerFactory.getLogger(HISCentralFriuliVariableUnits.class).warn("Units resource not found: {}", UNITS_RESOURCE);
		return Collections.emptyMap();
	    }

	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

		String line;
		boolean headerSkipped = false;
		while ((line = reader.readLine()) != null) {
		    if (!headerSkipped) {
			headerSkipped = true;
			continue;
		    }
		    if (line.isBlank()) {
			continue;
		    }

		    String[] parts = line.split(";", 3);
		    if (parts.length < 3) {
			continue;
		    }

		    String code = parts[0].trim();
		    String units = parts[2].trim();
		    if (!code.isEmpty() && !units.isEmpty()) {
			unitsByCode.put(code, units);
		    }
		}
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(HISCentralFriuliVariableUnits.class).error("Unable to load Friuli variable units", e);
	    return Collections.emptyMap();
	}

	return Collections.unmodifiableMap(unitsByCode);
    }
}
