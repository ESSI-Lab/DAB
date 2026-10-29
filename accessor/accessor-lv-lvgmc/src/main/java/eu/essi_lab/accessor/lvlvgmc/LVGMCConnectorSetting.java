package eu.essi_lab.accessor.lvlvgmc;

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
public class LVGMCConnectorSetting extends HarvestedConnectorSetting {

    public static final String LVGMC_PARAMETERS = "LVGMC_PARAMETERS";
    public static final String LVGMC_STATIONS = "LVGMC_STATIONS";

    /**
     *
     */
    public LVGMCConnectorSetting() {

	Option<String> parametersOption = StringOptionBuilder.get().//
		withKey(LVGMC_PARAMETERS).//
		withLabel("Comma separated abbreviations of the parameters to harvest, e.g. LIMEN = water level, WTEMD = water"
			+ " temperature, SEDUT = sea water temperature, SEHL = sea level (empty for all the parameters)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(parametersOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(LVGMC_STATIONS).//
		withLabel("Comma separated station identifiers to harvest, e.g. HD073548 (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public LVGMCConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public LVGMCConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the parameter abbreviations to harvest, empty for all
     */
    public List<String> getParameters() {

	return split(getOption(LVGMC_PARAMETERS, String.class).map(Option::getValue).orElse("")).stream().//
		map(String::toUpperCase).//
		distinct().//
		collect(Collectors.toList());
    }

    /**
     * @param parameters
     */
    public void setParameters(String parameters) {

	getOption(LVGMC_PARAMETERS, String.class).get().setValue(parameters);
    }

    /**
     * @return the station identifiers to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(LVGMC_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(LVGMC_STATIONS, String.class).get().setValue(stations);
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

	return LVGMCConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "LVGMC Connector settings";
    }
}
