package eu.essi_lab.accessor.mxconagua;

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
public class CONAGUAConnectorSetting extends HarvestedConnectorSetting {

    public static final String CONAGUA_PROPERTIES = "CONAGUA_PROPERTIES";
    public static final String CONAGUA_STATIONS = "CONAGUA_STATIONS";

    /**
     *
     */
    public static final String DEFAULT_PROPERTIES = "LEVEL,DISCHARGE";

    /**
     *
     */
    public CONAGUAConnectorSetting() {

	Option<String> propertiesOption = StringOptionBuilder.get().//
		withKey(CONAGUA_PROPERTIES).//
		withLabel("Comma separated properties to harvest (LEVEL = water level, DISCHARGE = discharge)").//
		withValue(DEFAULT_PROPERTIES).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(propertiesOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(CONAGUA_STATIONS).//
		withLabel("Comma separated station codes to harvest (empty for all the stations of the catalogue)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public CONAGUAConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public CONAGUAConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<CONAGUAProperty> getObservedProperties() {

	String value = getOption(CONAGUA_PROPERTIES, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_PROPERTIES);

	List<CONAGUAProperty> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<CONAGUAProperty> property = CONAGUAProperty.decode(code);
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

	getOption(CONAGUA_PROPERTIES, String.class).get().setValue(properties);
    }

    /**
     * @return the station codes to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(CONAGUA_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(CONAGUA_STATIONS, String.class).get().setValue(stations);
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

	return CONAGUAConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "CONAGUA Connector settings";
    }
}
