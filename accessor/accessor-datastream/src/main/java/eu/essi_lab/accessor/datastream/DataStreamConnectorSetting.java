package eu.essi_lab.accessor.datastream;

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
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * Connector settings for the DataStream accessor.
 *
 * It adds a configuration option for the DataStream API key, which will be
 * used to populate the {@code x-api-key} HTTP header on all requests.
 */
public class DataStreamConnectorSetting extends HarvestedConnectorSetting {

    public static final String DATASTREAM_API_KEY = "DATASTREAM_API_KEY";
    public static final String MAX_CHARACTERISTICS = "MAX_CHARACTERISTICS";

    private static final int DEFAULT_PAGE_SIZE = 50;
    /** -1 means no limit (consider all characteristics). */
    private static final int DEFAULT_MAX_CHARACTERISTICS = -1;

    public DataStreamConnectorSetting() {

	setPageSize(DEFAULT_PAGE_SIZE);

	Option<String> apiKeyOption = StringOptionBuilder.get().//
		withKey(DATASTREAM_API_KEY).//
		withLabel("DataStream API key (sent as x-api-key header)").//
		withValue("").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(apiKeyOption);

	Option<Integer> maxCharsOption = IntegerOptionBuilder.get().//
		withKey(MAX_CHARACTERISTICS).//
		withLabel("Max number of characteristic names per location (-1 = all)").//
		withValue(DEFAULT_MAX_CHARACTERISTICS).//
		cannotBeDisabled().//
		build();
	addOption(maxCharsOption);
    }

    public DataStreamConnectorSetting(JSONObject object) {
	super(object);
    }

    public DataStreamConnectorSetting(String object) {
	super(object);
    }

    @Override
    protected String initConnectorType() {
	return DataStreamConnector.TYPE;
    }

    @Override
    protected String initSettingName() {
	return "DataStream Connector settings";
    }

    /**
     * @return the configured API key value (may be empty but never null)
     */
    public String getApiKey() {
	return getOption(DATASTREAM_API_KEY, String.class).map(Option::getValue).orElse("");
    }

    /**
     * Max number of characteristic names to consider per location.
     *
     * @return -1 for unlimited, otherwise the configured positive limit
     */
    public int getMaxCharacteristics() {
	return getOption(MAX_CHARACTERISTICS, Integer.class).map(Option::getValue).orElse(DEFAULT_MAX_CHARACTERISTICS);
    }
}

