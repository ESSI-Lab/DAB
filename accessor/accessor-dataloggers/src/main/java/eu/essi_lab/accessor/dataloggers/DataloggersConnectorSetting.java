package eu.essi_lab.accessor.dataloggers;

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

import java.util.Arrays;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author Generated
 */
public class DataloggersConnectorSetting extends HarvestedConnectorSetting {

    public static final String TEMPORAL_EXTENT_SOURCE_KEY = "temporalExtentSource";

    public static final String TEMPORAL_EXTENT_AVAILABLE = "available";
    public static final String TEMPORAL_EXTENT_REAL_DATA = "real_data";

    private static final int DEFAULT_PAGE_SIZE = 50;

    /**
     * 
     */
    public DataloggersConnectorSetting() {

	setPageSize(DEFAULT_PAGE_SIZE);

	Option<String> temporalExtentOption = StringOptionBuilder.get().//
		withKey(TEMPORAL_EXTENT_SOURCE_KEY).//
		withLabel("Temporal extent source").//
		withSingleSelection().//
		withValues(Arrays.asList(TEMPORAL_EXTENT_AVAILABLE, TEMPORAL_EXTENT_REAL_DATA)).//
		withSelectedValue(TEMPORAL_EXTENT_AVAILABLE).//
		cannotBeDisabled().//
		build();

	addOption(temporalExtentOption);
    }

    /**
     * @param object
     */
    public DataloggersConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public DataloggersConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return DataloggersConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Dataloggers Connector settings";
    }

    /**
     * @return {@value #TEMPORAL_EXTENT_AVAILABLE} or {@value #TEMPORAL_EXTENT_REAL_DATA}
     */
    public String getTemporalExtentSource() {

	return getOption(TEMPORAL_EXTENT_SOURCE_KEY, String.class).map(Option::getValue).orElse(TEMPORAL_EXTENT_AVAILABLE);
    }

    public boolean useRealDataTemporalExtent() {

	return TEMPORAL_EXTENT_REAL_DATA.equals(getTemporalExtentSource());
    }
}

