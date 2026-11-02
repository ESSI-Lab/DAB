package eu.essi_lab.accessor.esmitecoaforos;

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
public class MITECOAforosConnectorSetting extends HarvestedConnectorSetting {

    public static final String MITECO_AFOROS_VARIABLES = "MITECO_AFOROS_VARIABLES";
    public static final String MITECO_AFOROS_BASINS = "MITECO_AFOROS_BASINS";
    public static final String MITECO_AFOROS_STATIONS = "MITECO_AFOROS_STATIONS";

    /**
     *
     */
    public MITECOAforosConnectorSetting() {

	Option<String> variablesOption = StringOptionBuilder.get().//
		withKey(MITECO_AFOROS_VARIABLES).//
		withLabel("Comma separated variables to harvest (empty for all): "
			+ Arrays.stream(MITECOAforosVariable.values()).map(Enum::name).collect(Collectors.joining(", "))).//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(variablesOption);

	Option<String> basinsOption = StringOptionBuilder.get().//
		withKey(MITECO_AFOROS_BASINS).//
		withLabel("Comma separated basin folders of the archive to harvest, e.g. EBRO,TAJO (empty for all)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(basinsOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(MITECO_AFOROS_STATIONS).//
		withLabel("Comma separated station codes to harvest, e.g. 9001 (empty for all)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public MITECOAforosConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public MITECOAforosConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the variables to harvest (all if none is set)
     */
    public List<MITECOAforosVariable> getVariables() {

	List<MITECOAforosVariable> ret = new ArrayList<>();
	for (String code : split(getValue(MITECO_AFOROS_VARIABLES))) {
	    Optional<MITECOAforosVariable> variable = MITECOAforosVariable.decode(code);
	    if (variable.isPresent() && !ret.contains(variable.get())) {
		ret.add(variable.get());
	    }
	}

	return ret.isEmpty() ? Arrays.asList(MITECOAforosVariable.values()) : ret;
    }

    /**
     * @param variables
     */
    public void setVariables(String variables) {

	getOption(MITECO_AFOROS_VARIABLES, String.class).get().setValue(variables);
    }

    /**
     * @return the basin folders to harvest, empty for all
     */
    public List<String> getBasins() {

	return split(getValue(MITECO_AFOROS_BASINS));
    }

    /**
     * @param basins
     */
    public void setBasins(String basins) {

	getOption(MITECO_AFOROS_BASINS, String.class).get().setValue(basins);
    }

    /**
     * @return the station codes to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getValue(MITECO_AFOROS_STATIONS));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(MITECO_AFOROS_STATIONS, String.class).get().setValue(stations);
    }

    /**
     * @param key
     * @return
     */
    private String getValue(String key) {

	return getOption(key, String.class).map(Option::getValue).orElse("");
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

	return MITECOAforosConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "MITECO Anuario de Aforos Connector settings";
    }
}
