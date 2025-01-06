package eu.essi_lab.cdk.harvest;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public abstract class FirstSiteConnectorSetting extends HarvestedConnectorSetting {

    /**
     * 
     */
    private static final String FIRST_SITE_ONLY_OPTION_KEY = "firstSiteOnly";

    /**
     * 
     */
    public FirstSiteConnectorSetting() {

	Option<BooleanChoice> option = BooleanChoiceOptionBuilder.get().//
		withKey(FIRST_SITE_ONLY_OPTION_KEY).//
		withLabel("Harvest first site only").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @param object
     */
    public FirstSiteConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public FirstSiteConnectorSetting(String object) {

	super(object);
    }

    /**
     * @param set
     */
    public void setHarvestFirstSiteOnly(boolean set) {

	getOption(FIRST_SITE_ONLY_OPTION_KEY, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(set));
    }

    /**
     * @return
     */
    public boolean isFirstSiteHarvestOnlySet() {

	return BooleanChoice.toBoolean(getOption(FIRST_SITE_ONLY_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
    }

}
