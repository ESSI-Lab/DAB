package eu.essi_lab.accessor.enhydris;

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
public class EnhydrisConnectorSetting extends HarvestedConnectorSetting {

    public static final String ENHYDRIS_STATIONS = "ENHYDRIS_STATIONS";
    public static final String ENHYDRIS_OWNERS = "ENHYDRIS_OWNERS";

    /**
     *
     */
    public EnhydrisConnectorSetting() {

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(ENHYDRIS_STATIONS).//
		withLabel("Comma separated identifiers of the stations to harvest, e.g. 2022,1407 (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);

	Option<String> ownersOption = StringOptionBuilder.get().//
		withKey(ENHYDRIS_OWNERS).//
		withLabel(
			"Comma separated identifiers of the organizations owning the stations to harvest, e.g. 11 for HCMR on OpenHi.net (empty for all the owners)")
		.//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(ownersOption);
    }

    /**
     * @param object
     */
    public EnhydrisConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public EnhydrisConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the identifiers of the stations to harvest, empty for all
     */
    public List<String> getStations() {

	return getValues(ENHYDRIS_STATIONS);
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(ENHYDRIS_STATIONS, String.class).get().setValue(stations);
    }

    /**
     * @return the identifiers of the organizations owning the stations to harvest, empty for all
     */
    public List<String> getOwners() {

	return getValues(ENHYDRIS_OWNERS);
    }

    /**
     * @param owners
     */
    public void setOwners(String owners) {

	getOption(ENHYDRIS_OWNERS, String.class).get().setValue(owners);
    }

    /**
     * @param key
     * @return
     */
    private List<String> getValues(String key) {

	String value = getOption(key, String.class).map(Option::getValue).orElse("");
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

	return EnhydrisConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Enhydris Connector settings";
    }
}
