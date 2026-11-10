package eu.essi_lab.accessor.ieopw;

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
public class OPWConnectorSetting extends HarvestedConnectorSetting {

    public static final String OPW_PARAMETERS = "OPW_PARAMETERS";
    public static final String OPW_STATIONS = "OPW_STATIONS";

    /**
     * Precipitation is not included, since the OPW rainfall stations are not suitable for republication
     */
    public static final String DEFAULT_PARAMETERS = "S,Q,TWater";

    /**
     *
     */
    public OPWConnectorSetting() {

	Option<String> parametersOption = StringOptionBuilder.get().//
		withKey(OPW_PARAMETERS).//
		withLabel("Comma separated station parameters to harvest (S = water level, Q = discharge, TWater = water temperature,"
			+ " P = precipitation)").//
		withValue(DEFAULT_PARAMETERS).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(parametersOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(OPW_STATIONS).//
		withLabel("Comma separated station numbers to harvest (empty for all the stations suitable for republication, 00001-41000)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public OPWConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public OPWConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<OPWParameter> getParameters() {

	String value = getOption(OPW_PARAMETERS, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_PARAMETERS);

	List<OPWParameter> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<OPWParameter> parameter = OPWParameter.decode(code);
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

	getOption(OPW_PARAMETERS, String.class).get().setValue(parameters);
    }

    /**
     * @return the station numbers to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(OPW_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(OPW_STATIONS, String.class).get().setValue(stations);
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

	return OPWConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "OPW Connector settings";
    }
}
