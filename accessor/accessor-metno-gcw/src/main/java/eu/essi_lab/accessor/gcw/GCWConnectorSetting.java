package eu.essi_lab.accessor.gcw;

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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author boldrini
 */
public class GCWConnectorSetting extends HarvestedConnectorSetting {

    /**
     * Quality flags and station housekeeping variables (battery, logger, GPS, tilt, fan) of the automatic weather
     * stations
     */
    public static final String DEFAULT_EXCLUDED_VARIABLES = "(?i)(.*_qc|qc_.*|.*_flag.*|.*_q|.*_q_.*|batt_.*|fan_.*|gps_.*|t_log|t_rad|tilt_.*|rot_true|rec)";

    public final String GCW_EXCLUDED_VARIABLES = "GCW_EXCLUDED_VARIABLES";

    /**
     *
     */
    public GCWConnectorSetting() {

	addOption(StringOptionBuilder.get().//
		withKey(GCW_EXCLUDED_VARIABLES).//
		withLabel("Regular expression of the NetCDF variable names not to harvest (empty to harvest all the variables)").//
		withValue(DEFAULT_EXCLUDED_VARIABLES).//
		cannotBeDisabled().//
		build());
    }

    /**
     * @param object
     */
    public GCWConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public GCWConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the pattern of the variables not to harvest, or null to harvest all the variables
     */
    public Pattern getExcludedVariables() {

	String value = getOption(GCW_EXCLUDED_VARIABLES, String.class).map(o -> o.getValue()).orElse(DEFAULT_EXCLUDED_VARIABLES);

	if (value == null || value.trim().isEmpty()) {
	    return null;
	}

	try {
	    return Pattern.compile(value.trim());
	} catch (PatternSyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Invalid excluded variables pattern {}, using the default one", value);
	    return Pattern.compile(DEFAULT_EXCLUDED_VARIABLES);
	}
    }

    @Override
    protected String initConnectorType() {

	return GCWConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "GCW Connector settings";
    }
}
