package eu.essi_lab.accessor.datalakes;

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

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * Connector settings for the Datalakes accessor.
 */
public class DatalakesConnectorSetting extends HarvestedConnectorSetting {

    public static final String DATALAKES_API_KEY = "DATALAKES_API_KEY";

    private static final int DEFAULT_PAGE_SIZE = 50;

    public DatalakesConnectorSetting() {
	setPageSize(DEFAULT_PAGE_SIZE);

	addOption(StringOptionBuilder.get().//
		withKey(DATALAKES_API_KEY).//
		withLabel("Datalakes API key (sent as api_key header for embargoed data)").//
		withValue("").//
		cannotBeDisabled().//
		build());
    }

    public DatalakesConnectorSetting(JSONObject object) {
	super(object);
    }

    public DatalakesConnectorSetting(String object) {
	super(object);
    }

    @Override
    protected String initConnectorType() {
	return DatalakesConnector.TYPE;
    }

    @Override
    protected String initSettingName() {
	return "Datalakes Connector settings";
    }

    public String getApiKey() {
	return getOption(DATALAKES_API_KEY, String.class).map(Option::getValue).orElse("");
    }
}
