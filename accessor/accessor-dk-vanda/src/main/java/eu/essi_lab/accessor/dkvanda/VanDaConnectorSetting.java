package eu.essi_lab.accessor.dkvanda;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class VanDaConnectorSetting extends HarvestedConnectorSetting {

    public static final String VANDA_EXCLUDED_EXAMINATION_TYPES = "VANDA_EXCLUDED_EXAMINATION_TYPES";
    public static final String VANDA_STATIONS = "VANDA_STATIONS";

    /**
     * Gauge datum surveys (21, Kotemåling) and data logger battery voltage (42, Batterispænding) are not observations of the water
     * body
     */
    public static final String DEFAULT_EXCLUDED_EXAMINATION_TYPES = "21,42";

    /**
     *
     */
    public VanDaConnectorSetting() {

	Option<String> excludedOption = StringOptionBuilder.get().//
		withKey(VANDA_EXCLUDED_EXAMINATION_TYPES).//
		withLabel("Comma separated examination type codes not to harvest (21 = gauge datum survey, 42 = logger battery voltage,"
			+ " 22 = staff gauge reading, 24 = discharge field measurement, 25 = water level, 27 = discharge,"
			+ " 30/31 = groundwater level, 29 = temperature, 28/33 = oxygen, 32 = conductivity, 34 = salinity)").//
		withValue(DEFAULT_EXCLUDED_EXAMINATION_TYPES).//
		cannotBeDisabled().//
		build();

	addOption(excludedOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(VANDA_STATIONS).//
		withLabel("Comma separated 8 digits station identifiers to harvest (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public VanDaConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public VanDaConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<Integer> getExcludedExaminationTypes() {

	String value = getOption(VANDA_EXCLUDED_EXAMINATION_TYPES, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_EXCLUDED_EXAMINATION_TYPES);

	List<Integer> ret = new ArrayList<>();
	for (String code : split(value)) {
	    try {
		ret.add(Integer.valueOf(code));
	    } catch (NumberFormatException e) {
		// ignored
	    }
	}

	return ret;
    }

    /**
     * @param types
     */
    public void setExcludedExaminationTypes(String types) {

	getOption(VANDA_EXCLUDED_EXAMINATION_TYPES, String.class).get().setValue(types);
    }

    /**
     * @return the station identifiers to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(VANDA_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(VANDA_STATIONS, String.class).get().setValue(stations);
    }

    /**
     * @param value
     * @return
     */
    private static List<String> split(String value) {

	if (value == null) {
	    return new ArrayList<>();
	}

	return Arrays.stream(value.split(",")).//
		map(String::trim).//
		filter(v -> !v.isEmpty()).//
		collect(Collectors.toList());
    }

    @Override
    protected String initConnectorType() {

	return VanDaConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "VanDa Connector settings";
    }
}
