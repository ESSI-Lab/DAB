package eu.essi_lab.accessor.ukhydrology;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class UKHydrologyConnectorSetting extends HarvestedConnectorSetting {

    public final String UK_HYDROLOGY_STATION_NAME = "UK_HYDROLOGY_STATION_NAME";

    /**
     * 
     */
    public UKHydrologyConnectorSetting() {

	Option<String> option = StringOptionBuilder.get().//
		withKey(UK_HYDROLOGY_STATION_NAME).//
		withLabel("Station name prefix (empty for all stations)").//
		withValue("").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @return
     */
    public String getStationNamePrefix() {

	return getOption(UK_HYDROLOGY_STATION_NAME, String.class).get().getValue();
    }

    /**
     * @param object
     */
    public UKHydrologyConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public UKHydrologyConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return UKHydrologyConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "UK Hydrology Connector settings";
    }
}
