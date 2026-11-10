package eu.essi_lab.accessor.ukeafloodmonitoring;

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
public class EAFloodMonitoringConnectorSetting extends HarvestedConnectorSetting {

    public static final String EA_FLOOD_MONITORING_PARAMETERS = "EA_FLOOD_MONITORING_PARAMETERS";
    public static final String EA_FLOOD_MONITORING_STATIONS = "EA_FLOOD_MONITORING_STATIONS";

    /**
     * Water level (river stage, downstream stage, tidal level, groundwater level, ...), flow and water temperature. Rainfall, air
     * temperature and wind are not included by default
     */
    public static final String DEFAULT_PARAMETERS = "level,flow,temperature:Water";

    /**
     *
     */
    public EAFloodMonitoringConnectorSetting() {

	Option<String> parametersOption = StringOptionBuilder.get().//
		withKey(EA_FLOOD_MONITORING_PARAMETERS).//
		withLabel("Comma separated parameters to harvest, as parameter or parameter:qualifier (e.g. level, flow,"
			+ " temperature:Water, rainfall, temperature, wind)").//
		withValue(DEFAULT_PARAMETERS).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(parametersOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(EA_FLOOD_MONITORING_STATIONS).//
		withLabel("Comma separated station references to harvest (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public EAFloodMonitoringConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public EAFloodMonitoringConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the parameters to harvest, as "parameter" or "parameter:qualifier"
     */
    public List<String> getParameters() {

	List<String> ret = split(getOption(EA_FLOOD_MONITORING_PARAMETERS, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_PARAMETERS));

	return ret.isEmpty() ? split(DEFAULT_PARAMETERS) : ret;
    }

    /**
     * @param parameters
     */
    public void setParameters(String parameters) {

	getOption(EA_FLOOD_MONITORING_PARAMETERS, String.class).get().setValue(parameters);
    }

    /**
     * @return the station references to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(EA_FLOOD_MONITORING_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(EA_FLOOD_MONITORING_STATIONS, String.class).get().setValue(stations);
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
		distinct().//
		collect(Collectors.toList());
    }

    @Override
    protected String initConnectorType() {

	return EAFloodMonitoringConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "EA Flood Monitoring Connector settings";
    }
}
