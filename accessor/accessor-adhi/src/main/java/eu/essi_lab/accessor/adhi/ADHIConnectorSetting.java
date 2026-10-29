package eu.essi_lab.accessor.adhi;

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
public class ADHIConnectorSetting extends HarvestedConnectorSetting {

    public static final String ADHI_VARIABLES = "ADHI_VARIABLES";
    public static final String ADHI_STATIONS = "ADHI_STATIONS";

    /**
     * The annual percentiles of daily discharge (ANNUAL_Q05 ... ANNUAL_Q99) are not included by default
     */
    public static final String DEFAULT_VARIABLES = "MONTHLY_MEAN,MONTHLY_MAX,MONTHLY_MIN,ANNUAL_MEAN,ANNUAL_MAX,ANNUAL_7DAY_MIN";

    /**
     *
     */
    public ADHIConnectorSetting() {

	Option<String> variablesOption = StringOptionBuilder.get().//
		withKey(ADHI_VARIABLES).//
		withLabel("Comma separated series to harvest (MONTHLY_MEAN, MONTHLY_MAX, MONTHLY_MIN, ANNUAL_MEAN, ANNUAL_MAX,"
			+ " ANNUAL_7DAY_MIN, ANNUAL_Q05, ANNUAL_Q10, ANNUAL_Q25, ANNUAL_Q50, ANNUAL_Q75, ANNUAL_Q90, ANNUAL_Q95, ANNUAL_Q99)").//
		withValue(DEFAULT_VARIABLES).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(variablesOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(ADHI_STATIONS).//
		withLabel("Comma separated ADHI station identifiers to harvest, e.g. ADHI_1,ADHI_2 (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public ADHIConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ADHIConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<ADHIVariable> getVariables() {

	String value = getOption(ADHI_VARIABLES, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_VARIABLES);

	List<ADHIVariable> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<ADHIVariable> variable = ADHIVariable.decode(code);
	    if (variable.isPresent() && !ret.contains(variable.get())) {
		ret.add(variable.get());
	    }
	}

	return ret;
    }

    /**
     * @param variables
     */
    public void setVariables(String variables) {

	getOption(ADHI_VARIABLES, String.class).get().setValue(variables);
    }

    /**
     * @return the station identifiers to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(ADHI_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(ADHI_STATIONS, String.class).get().setValue(stations);
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

	return ADHIConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "ADHI Connector settings";
    }
}
