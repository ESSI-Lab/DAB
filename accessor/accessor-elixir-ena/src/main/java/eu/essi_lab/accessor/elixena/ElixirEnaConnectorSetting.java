package eu.essi_lab.accessor.elixena;

import java.util.Arrays;

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
 * @author Fabrizio
 */
public class ElixirEnaConnectorSetting extends HarvestedConnectorSetting {

    /**
     * 
     */
    public static final String ELIXIR_ENA_HARVESTING_TYPE_KEY = "elixirEnaHarvestingType";

    public static final String DEFAULT_STUDIES = "DEFAULT";

    public static final String LOW_CONFIDENCE = "LOW";

    public static final String MEDIUM_CONFIDENCE = "MEDIUM";

    public static final String MEDIUM_PLUS_HIGH_CONFIDENCE = "MEDIUM_PLUS_HIGH";

    public static final String HIGH_CONFIDENCE = "HIGH";

    public static String[] HARVESTING_TYPES = new String[] { DEFAULT_STUDIES, LOW_CONFIDENCE, MEDIUM_CONFIDENCE, MEDIUM_PLUS_HIGH_CONFIDENCE,
	    HIGH_CONFIDENCE };

    /**
     * 
     */
    public ElixirEnaConnectorSetting() {

	Option<String> option = StringOptionBuilder.get(String.class).//
		withKey(ELIXIR_ENA_HARVESTING_TYPE_KEY).//
		withLabel("Harvesting type").//
		withSingleSelection().//
		withValues(Arrays.asList(HARVESTING_TYPES)).//
		withSelectedValue(LOW_CONFIDENCE).//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @param object
     */
    public ElixirEnaConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ElixirEnaConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public String getHarvestingType() {

	return getOption(ELIXIR_ENA_HARVESTING_TYPE_KEY, String.class).get().getSelectedValue();
    }

    @Override
    protected String initConnectorType() {

	return ElixirENAConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Elixir ENA Connector settings";
    }
}
