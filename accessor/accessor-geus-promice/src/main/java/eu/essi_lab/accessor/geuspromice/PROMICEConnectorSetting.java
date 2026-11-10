package eu.essi_lab.accessor.geuspromice;

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
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class PROMICEConnectorSetting extends HarvestedConnectorSetting {

    /**
     * Meteorology (upper boom), radiation, surface energy balance, surface and snow height, rainfall and 10 m
     * subsurface temperature; station housekeeping variables (boom heights, tilt, GPS, battery) are excluded
     */
    public static final String DEFAULT_VARIABLES = "t_u,rh_u,qh_u,p_u,wspd_u,wdir_u,dsr_cor,usr_cor,dlr,ulr,albedo,cc,t_surf,dlhf_u,dshf_u,z_surf_combined,snow_height,rainfall_cor_u,t_i_10m";

    /**
     * Value to harvest all the variables on the time dimension
     */
    public static final String ALL_VARIABLES = "*";

    public final String PROMICE_VARIABLES = "PROMICE_VARIABLES";

    /**
     *
     */
    public PROMICEConnectorSetting() {

	addOption(StringOptionBuilder.get().//
		withKey(PROMICE_VARIABLES).//
		withLabel("Comma separated NetCDF variable names (* for all variables)").//
		withValue(DEFAULT_VARIABLES).//
		required().//
		cannotBeDisabled().//
		build());
    }

    /**
     * @param object
     */
    public PROMICEConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public PROMICEConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the variables to harvest, or null for all the variables
     */
    public List<String> getVariables() {

	String value = getOption(PROMICE_VARIABLES, String.class).map(o -> o.getValue()).orElse(DEFAULT_VARIABLES);

	if (value == null || value.trim().equals(ALL_VARIABLES)) {
	    return null;
	}

	List<String> ret = new ArrayList<>();
	for (String item : value.split(",")) {
	    if (!item.trim().isEmpty()) {
		ret.add(item.trim());
	    }
	}

	return ret.isEmpty() ? null : ret;
    }

    @Override
    protected String initConnectorType() {

	return PROMICEConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "PROMICE Connector settings";
    }
}
