package eu.essi_lab.accessor.fdsn;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class FDSNConnectorSetting extends DistributedConnectorSetting {

    private static final String IGNORE_COMPLEX_QUERIES_OPTION_KEY = "ignoreComplexQueries";

    /**
     * 
     */
    public FDSNConnectorSetting() {

	Option<BooleanChoice> option = BooleanChoiceOptionBuilder.get().//
		withKey(IGNORE_COMPLEX_QUERIES_OPTION_KEY).//
		withLabel("Ignore complex queries").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.TRUE).//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @param object
     */
    public FDSNConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public FDSNConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public boolean isIgnoreComplexQueries() {

	return BooleanChoice.toBoolean(//
		getOption(IGNORE_COMPLEX_QUERIES_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
    }

    /**
     * @param ignore
     */
    public void setIgnoreComplexQueries(boolean ignore) {

	getOption(IGNORE_COMPLEX_QUERIES_OPTION_KEY, BooleanChoice.class).get().//
		select(v -> v == BooleanChoice.fromBoolean(ignore));
    }

    @Override
    protected String initSettingName() {

	return "FDSN Connector settings";
    }

    @Override
    protected String initConnectorType() {

	return FDSNConnector.CONNECTOR_TYPE;
    }
}
