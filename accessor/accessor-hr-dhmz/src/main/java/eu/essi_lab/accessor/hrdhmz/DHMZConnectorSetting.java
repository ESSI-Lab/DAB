package eu.essi_lab.accessor.hrdhmz;

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
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author boldrini
 */
public class DHMZConnectorSetting extends HarvestedConnectorSetting {

    public static final String DHMZ_PROPERTIES = "DHMZ_PROPERTIES";
    public static final String DHMZ_REAL_TIME = "DHMZ_REAL_TIME";
    public static final String DHMZ_STATIONS = "DHMZ_STATIONS";

    /**
     * All the data types
     */
    public static final String DEFAULT_PROPERTIES = Arrays.stream(DHMZProperty.values()).//
	    map(DHMZProperty::name).//
	    collect(Collectors.joining(","));

    /**
     *
     */
    public DHMZConnectorSetting() {

	Option<String> propertiesOption = StringOptionBuilder.get().//
		withKey(DHMZ_PROPERTIES).//
		withLabel("Comma separated data types to harvest (WATER_LEVEL, DISCHARGE, TOTAL_DISCHARGE, WATER_TEMPERATURE,"
			+ " SUSPENDED_SEDIMENT_CONCENTRATION, SUSPENDED_SEDIMENT_LOAD)").//
		withValue(DEFAULT_PROPERTIES).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(propertiesOption);

	Option<BooleanChoice> realTimeOption = BooleanChoiceOptionBuilder.get().//
		withKey(DHMZ_REAL_TIME).//
		withLabel("Harvest also the real time water levels of the automatic stations (last 15 days, not verified)").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.TRUE).//
		cannotBeDisabled().//
		build();

	addOption(realTimeOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(DHMZ_STATIONS).//
		withLabel("Comma separated station codes (e.g. 5001) to harvest (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public DHMZConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public DHMZConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<DHMZProperty> getDataTypes() {

	String value = getOption(DHMZ_PROPERTIES, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_PROPERTIES);

	List<DHMZProperty> ret = new ArrayList<>();
	for (String name : split(value)) {
	    Optional<DHMZProperty> property = DHMZProperty.decode(name);
	    if (property.isPresent() && !ret.contains(property.get())) {
		ret.add(property.get());
	    }
	}

	return ret;
    }

    /**
     * @param properties
     */
    public void setDataTypes(String properties) {

	getOption(DHMZ_PROPERTIES, String.class).get().setValue(properties);
    }

    /**
     * @return true if the real time series must be harvested
     */
    public boolean isRealTime() {

	return getOption(DHMZ_REAL_TIME, BooleanChoice.class).//
		map(option -> BooleanChoice.toBoolean(option.getSelectedValue())).//
		orElse(true);
    }

    /**
     * @param realTime
     */
    public void setRealTime(boolean realTime) {

	getOption(DHMZ_REAL_TIME, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(realTime));
    }

    /**
     * @return the station codes to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(DHMZ_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(DHMZ_STATIONS, String.class).get().setValue(stations);
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

	return DHMZConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "DHMZ Connector settings";
    }
}
