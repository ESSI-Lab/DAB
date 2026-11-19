package eu.essi_lab.accessor.canswe;

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

import java.util.Set;
import java.util.TreeSet;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class CanSWEConnectorSetting extends HarvestedConnectorSetting {

    /**
     * Automatic measurements: snow pillows or snow scales (2) and passive gamma sensors (3)
     */
    public static final String DEFAULT_MEASUREMENT_TYPES = "2,3";

    /**
     * Value to harvest the stations of all the measurement types
     */
    public static final String ALL_MEASUREMENT_TYPES = "*";

    public static final String CANSWE_MEASUREMENT_TYPES = "CANSWE_MEASUREMENT_TYPES";

    /**
     *
     */
    public CanSWEConnectorSetting() {

	addOption(StringOptionBuilder.get().//
		withKey(CANSWE_MEASUREMENT_TYPES).//
		withLabel("Comma separated WMO measurement type codes of the stations to harvest (* for all): "
			+ "0 multi point manual snow survey, 1 single point manual SWE measurement, 2 snow pillow or snow scale, "
			+ "3 passive gamma, 4 GNSS/GPS, 5 cosmic ray attenuation, 6 time domain reflectometry")
		.//
		withValue(DEFAULT_MEASUREMENT_TYPES).//
		required().//
		cannotBeDisabled().//
		build());
    }

    /**
     * @return the measurement type codes of the stations to harvest, or null for all the stations
     */
    public Set<Long> getMeasurementTypes() {

	String value = getOption(CANSWE_MEASUREMENT_TYPES, String.class).map(o -> o.getValue()).orElse(DEFAULT_MEASUREMENT_TYPES);

	return parseMeasurementTypes(value);
    }

    /**
     * @param value comma separated codes, or {@link #ALL_MEASUREMENT_TYPES}
     * @return the codes, or null for all the measurement types
     */
    static Set<Long> parseMeasurementTypes(String value) {

	if (value == null || value.trim().isEmpty() || value.trim().equals(ALL_MEASUREMENT_TYPES)) {
	    return null;
	}

	Set<Long> ret = new TreeSet<>();
	for (String item : value.split(",")) {
	    if (!item.trim().isEmpty()) {
		try {
		    ret.add(Long.parseLong(item.trim()));
		} catch (NumberFormatException e) {
		    throw new IllegalArgumentException("Invalid CanSWE measurement type: " + item.trim());
		}
	    }
	}

	return ret.isEmpty() ? null : ret;
    }

    /**
     * @param object
     */
    public CanSWEConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public CanSWEConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return CanSWEConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "CanSWE Connector settings";
    }
}
