package eu.essi_lab.accessor.siarso;

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
public class ARSOConnectorSetting extends HarvestedConnectorSetting {

    public static final String ARSO_PARAMETERS = "ARSO_PARAMETERS";
    public static final String ARSO_STATIONS = "ARSO_STATIONS";

    /**
     *
     */
    public ARSOConnectorSetting() {

	String parameters = Arrays.stream(ARSOParameter.values()).//
		map(ARSOParameter::name).//
		collect(Collectors.joining(", "));

	Option<String> parametersOption = StringOptionBuilder.get().//
		withKey(ARSO_PARAMETERS).//
		withLabel("Comma separated parameters to harvest (empty for all the published quantities): " + parameters).//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(parametersOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(ARSO_STATIONS).//
		withLabel("Comma separated station codes to harvest, e.g. 1060 (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public ARSOConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ARSOConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the parameters to harvest, empty for all
     */
    public List<ARSOParameter> getParameters() {

	String value = getOption(ARSO_PARAMETERS, String.class).//
		map(Option::getValue).//
		orElse("");

	List<ARSOParameter> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<ARSOParameter> parameter = ARSOParameter.decode(code);
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

	getOption(ARSO_PARAMETERS, String.class).get().setValue(parameters);
    }

    /**
     * @return the station codes to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(ARSO_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(ARSO_STATIONS, String.class).get().setValue(stations);
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

	return ARSOConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "ARSO Connector settings";
    }
}
