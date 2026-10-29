package eu.essi_lab.accessor.ththaiwater;

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
public class ThaiWaterConnectorSetting extends HarvestedConnectorSetting {

    public static final String THAIWATER_STATION_TYPES = "THAIWATER_STATION_TYPES";
    public static final String THAIWATER_STATIONS = "THAIWATER_STATIONS";

    /**
     *
     */
    public static final String DEFAULT_STATION_TYPES = Arrays.stream(ThaiWaterStationType.values()).//
	    map(ThaiWaterStationType::getCode).//
	    collect(Collectors.joining(","));

    /**
     *
     */
    public ThaiWaterConnectorSetting() {

	Option<String> typesOption = StringOptionBuilder.get().//
		withKey(THAIWATER_STATION_TYPES).//
		withLabel("Comma separated station types to harvest (tele_waterlevel = river water level and discharge, canal = canal"
			+ " water level, flow = canal flow, dam = large reservoirs)").//
		withValue(DEFAULT_STATION_TYPES).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(typesOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(THAIWATER_STATIONS).//
		withLabel("Comma separated station identifiers to harvest, optionally prefixed by the station type (e.g. 737,"
			+ " dam|25); empty for all the stations").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public ThaiWaterConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ThaiWaterConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<ThaiWaterStationType> getStationTypes() {

	String value = getOption(THAIWATER_STATION_TYPES, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_STATION_TYPES);

	List<ThaiWaterStationType> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<ThaiWaterStationType> type = ThaiWaterStationType.decode(code);
	    if (type.isPresent() && !ret.contains(type.get())) {
		ret.add(type.get());
	    }
	}

	return ret;
    }

    /**
     * @param types
     */
    public void setStationTypes(String types) {

	getOption(THAIWATER_STATION_TYPES, String.class).get().setValue(types);
    }

    /**
     * @return the stations to harvest (identifiers, or station types and identifiers separated by "|"), empty for all
     */
    public List<String> getStations() {

	return split(getOption(THAIWATER_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(THAIWATER_STATIONS, String.class).get().setValue(stations);
    }

    /**
     * @param series
     * @return true if the series belongs to the configured stations
     */
    public boolean accept(ThaiWaterEntity series) {

	List<String> stations = getStations();

	return stations.isEmpty() || stations.contains(series.getStationId()) || stations.contains(series.getStationKey());
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

	return ThaiWaterConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "ThaiWater Connector settings";
    }
}
