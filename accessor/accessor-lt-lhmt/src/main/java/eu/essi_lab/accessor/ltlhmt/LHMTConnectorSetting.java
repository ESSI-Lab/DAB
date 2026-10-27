package eu.essi_lab.accessor.ltlhmt;

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
public class LHMTConnectorSetting extends HarvestedConnectorSetting {

    public static final String LHMT_PARAMETERS = "LHMT_PARAMETERS";
    public static final String LHMT_STATIONS = "LHMT_STATIONS";
    public static final String LHMT_STATIONS_WITHOUT_TEMPERATURE = "LHMT_STATIONS_WITHOUT_TEMPERATURE";

    public static final String DEFAULT_STATIONS_WITHOUT_TEMPERATURE = String.join(",",
	    LHMTClient.DEFAULT_STATIONS_WITHOUT_TEMPERATURE);

    public static final String DEFAULT_PARAMETERS = Arrays.stream(LHMTParameter.values()).//
	    map(LHMTParameter::name).//
	    collect(Collectors.joining(","));

    /**
     *
     */
    public LHMTConnectorSetting() {

	Option<String> parametersOption = StringOptionBuilder.get().//
		withKey(LHMT_PARAMETERS).//
		withLabel("Comma separated parameters to harvest (WATER_LEVEL = hourly water level, WATER_TEMPERATURE = hourly water"
			+ " temperature, WATER_LEVEL_DAILY = daily mean water level, DISCHARGE_DAILY = daily mean discharge)").//
		withValue(DEFAULT_PARAMETERS).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(parametersOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(LHMT_STATIONS).//
		withLabel("Comma separated station codes to harvest, e.g. nemajunu-vms (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);

	Option<String> noTemperatureOption = StringOptionBuilder.get().//
		withKey(LHMT_STATIONS_WITHOUT_TEMPERATURE).//
		withLabel("Comma separated codes of the stations where the water temperature is not measured (at the other stations the"
			+ " water temperature series is kept also when missing, e.g. with ice cover)").//
		withValue(DEFAULT_STATIONS_WITHOUT_TEMPERATURE).//
		cannotBeDisabled().//
		build();

	addOption(noTemperatureOption);
    }

    /**
     * @param object
     */
    public LHMTConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public LHMTConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<LHMTParameter> getParameters() {

	String value = getOption(LHMT_PARAMETERS, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_PARAMETERS);

	List<LHMTParameter> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<LHMTParameter> parameter = LHMTParameter.decode(code);
	    if (parameter.isPresent() && !ret.contains(parameter.get())) {
		ret.add(parameter.get());
	    }
	}

	return ret;
    }

    /**
     * @param parameters
     */
    public void setParameters(String parameters) {

	getOption(LHMT_PARAMETERS, String.class).get().setValue(parameters);
    }

    /**
     * @return the station codes to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(LHMT_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(LHMT_STATIONS, String.class).get().setValue(stations);
    }

    /**
     * @return the codes of the stations where the water temperature is not measured
     */
    public List<String> getStationsWithoutTemperature() {

	return split(getOption(LHMT_STATIONS_WITHOUT_TEMPERATURE, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_STATIONS_WITHOUT_TEMPERATURE));
    }

    /**
     * @param stations
     */
    public void setStationsWithoutTemperature(String stations) {

	getOption(LHMT_STATIONS_WITHOUT_TEMPERATURE, String.class).get().setValue(stations);
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

	return LHMTConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "LHMT Connector settings";
    }
}
