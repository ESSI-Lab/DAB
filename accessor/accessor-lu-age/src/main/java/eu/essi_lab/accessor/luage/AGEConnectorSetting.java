package eu.essi_lab.accessor.luage;

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
public class AGEConnectorSetting extends HarvestedConnectorSetting {

    public static final String AGE_STATIONS = "AGE_STATIONS";

    /**
     *
     */
    public AGEConnectorSetting() {

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(AGE_STATIONS).//
		withLabel("Comma separated station codes to harvest, e.g. Hesperange,Ettelbrück-Alzette (empty for all the stations operated by AGE)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public AGEConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public AGEConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the station codes to harvest, empty for all
     */
    public List<String> getStations() {

	String value = getOption(AGE_STATIONS, String.class).map(Option::getValue).orElse("");
	if (value == null) {
	    return new ArrayList<>();
	}

	return Arrays.stream(value.split(",")).//
		map(String::trim).//
		filter(v -> !v.isEmpty()).//
		collect(Collectors.toList());
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(AGE_STATIONS, String.class).get().setValue(stations);
    }

    @Override
    protected String initConnectorType() {

	return AGEConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "AGE Connector settings";
    }
}
