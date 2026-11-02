package eu.essi_lab.accessor.czchmi;

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
public class CHMIConnectorSetting extends HarvestedConnectorSetting {

    public static final String CHMI_PARAMETERS = "CHMI_PARAMETERS";
    public static final String CHMI_STATIONS = "CHMI_STATIONS";

    /**
     * All the series types
     */
    public static final String DEFAULT_PARAMETERS = Arrays.stream(CHMIParameter.values()).//
	    map(CHMIParameter::name).//
	    collect(Collectors.joining(","));

    /**
     *
     */
    public CHMIConnectorSetting() {

	Option<String> parametersOption = StringOptionBuilder.get().//
		withKey(CHMI_PARAMETERS).//
		withLabel("Comma separated series types to harvest. Operational (current year, completed with the validated archive):"
			+ " H = water level, Q = discharge, TH = water temperature. Validated archive: HD, QD, TD, PD = daily mean water"
			+ " level, discharge, water temperature, suspended sediment concentration; HH, QH = hourly mean water level,"
			+ " discharge; HC, TO = instantaneous water level, water temperature (stations without operational data); QM, TM ="
			+ " monthly mean discharge, water temperature; QK = monthly maximum discharge; QNEX, QNEY = monthly naturalised"
			+ " discharge; HR, TR = morning water level, water temperature; QC = measured discharge").//
		withValue(DEFAULT_PARAMETERS).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(parametersOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(CHMI_STATIONS).//
		withLabel("Comma separated station identifiers (WIGOS identifier, e.g. 0-203-1-001000, or database number, e.g. 001000)"
			+ " to harvest (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public CHMIConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public CHMIConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<CHMIParameter> getParameters() {

	String value = getOption(CHMI_PARAMETERS, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_PARAMETERS);

	List<CHMIParameter> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<CHMIParameter> parameter = CHMIParameter.decode(code);
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

	getOption(CHMI_PARAMETERS, String.class).get().setValue(parameters);
    }

    /**
     * @return the station identifiers to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(CHMI_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(CHMI_STATIONS, String.class).get().setValue(stations);
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

	return CHMIConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "CHMI Connector settings";
    }
}
