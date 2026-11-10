package eu.essi_lab.accessor.usnrcsawdb;

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
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class NRCSAWDBConnectorSetting extends HarvestedConnectorSetting {

    /**
     * SNOTEL, SNOLITE, cooperator snow sensors and snow courses
     */
    public static final String DEFAULT_NETWORKS = "SNTL,SNTLT,MSNT,SNOW";

    /**
     * Snow, precipitation and air temperature
     */
    public static final String DEFAULT_ELEMENTS = "WTEQ,SNWD,SNDN,PREC,PRCP,TAVG,TMAX,TMIN,TOBS";

    /**
     * The finest of these durations is harvested for each element
     */
    public static final String DEFAULT_DURATIONS = "DAILY,SEMIMONTHLY";

    public final String AWDB_NETWORKS = "AWDB_NETWORKS";
    public final String AWDB_ELEMENTS = "AWDB_ELEMENTS";
    public final String AWDB_DURATIONS = "AWDB_DURATIONS";

    /**
     *
     */
    public NRCSAWDBConnectorSetting() {

	addOption(StringOptionBuilder.get().//
		withKey(AWDB_NETWORKS).//
		withLabel("Comma separated network codes (e.g. SNTL: SNOTEL, SNTLT: SNOLITE, MSNT: cooperator snow sensors, SNOW: snow courses, SCAN)").//
		withValue(DEFAULT_NETWORKS).//
		required().//
		cannotBeDisabled().//
		build());

	addOption(StringOptionBuilder.get().//
		withKey(AWDB_ELEMENTS).//
		withLabel("Comma separated element codes (e.g. WTEQ: snow water equivalent, SNWD: snow depth); empty for all elements").//
		withValue(DEFAULT_ELEMENTS).//
		required().//
		cannotBeDisabled().//
		build());

	addOption(StringOptionBuilder.get().//
		withKey(AWDB_DURATIONS).//
		withLabel("Comma separated durations (HOURLY, DAILY, SEMIMONTHLY, MONTHLY); only the finest available one is harvested for each element").//
		withValue(DEFAULT_DURATIONS).//
		required().//
		cannotBeDisabled().//
		build());
    }

    /**
     * @param object
     */
    public NRCSAWDBConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public NRCSAWDBConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<String> getNetworks() {

	return getList(AWDB_NETWORKS, DEFAULT_NETWORKS);
    }

    /**
     * @return
     */
    public List<String> getElements() {

	return getList(AWDB_ELEMENTS, DEFAULT_ELEMENTS);
    }

    /**
     * @return
     */
    public List<String> getDurations() {

	return getList(AWDB_DURATIONS, DEFAULT_DURATIONS);
    }

    /**
     * @param key
     * @param defaultValue
     * @return
     */
    private List<String> getList(String key, String defaultValue) {

	String value = getOption(key, String.class).map(o -> o.getValue()).orElse(defaultValue);

	List<String> ret = new ArrayList<>();
	if (value == null) {
	    return ret;
	}

	for (String item : value.split(",")) {
	    if (!item.trim().isEmpty()) {
		ret.add(item.trim().toUpperCase());
	    }
	}

	return ret;
    }

    @Override
    protected String initConnectorType() {

	return NRCSAWDBConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "NRCS AWDB Connector settings";
    }
}
