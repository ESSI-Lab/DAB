package eu.essi_lab.accessor.chexistenzbafu;

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
public class CHExistenzBafuConnectorSetting extends HarvestedConnectorSetting {

    public final String CH_EXISTENZ_BAFU_STATION_NAME = "CH_EXISTENZ_BAFU_STATION_NAME";

    /**
     * 
     */
    public CHExistenzBafuConnectorSetting() {

	Option<String> option = StringOptionBuilder.get().//
		withKey(CH_EXISTENZ_BAFU_STATION_NAME).//
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

	return getOption(CH_EXISTENZ_BAFU_STATION_NAME, String.class).get().getValue();
    }

    /**
     * @param object
     */
    public CHExistenzBafuConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public CHExistenzBafuConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return CHExistenzBafuConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "CH Existenz BAFU Hydrology Connector settings";
    }
}
