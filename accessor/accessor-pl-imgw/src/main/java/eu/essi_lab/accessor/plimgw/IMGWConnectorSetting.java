package eu.essi_lab.accessor.plimgw;

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
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class IMGWConnectorSetting extends HarvestedConnectorSetting {

    public static final String IMGW_PROPERTIES = "IMGW_PROPERTIES";
    public static final String IMGW_STATIONS = "IMGW_STATIONS";

    public static final String DEFAULT_PROPERTIES = "H,Q,T";

    /**
     *
     */
    public IMGWConnectorSetting() {

	Option<String> propertiesOption = StringOptionBuilder.get().//
		withKey(IMGW_PROPERTIES).//
		withLabel("Comma separated observed properties to harvest (H = water level, Q = discharge, T = water temperature)").//
		withValue(DEFAULT_PROPERTIES).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(propertiesOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(IMGW_STATIONS).//
		withLabel("Comma separated station codes to harvest (e.g. 153190040, empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public IMGWConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public IMGWConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<IMGWProperty> getObservedProperties() {

	String value = getOption(IMGW_PROPERTIES, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_PROPERTIES);

	List<IMGWProperty> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<IMGWProperty> property = IMGWProperty.decode(code);
	    if (property.isPresent() && !ret.contains(property.get())) {
		ret.add(property.get());
	    }
	}

	return ret;
    }

    /**
     * @param properties
     */
    public void setObservedProperties(String properties) {

	getOption(IMGW_PROPERTIES, String.class).get().setValue(properties);
    }

    /**
     * @return the station codes to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(IMGW_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(IMGW_STATIONS, String.class).get().setValue(stations);
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

	return IMGWConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "IMGW Connector settings";
    }
}
