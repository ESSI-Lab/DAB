package eu.essi_lab.accessor.icos;

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
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class ICOSConnectorSetting extends HarvestedConnectorSetting {

    private static final String GEOSS_OPTION_KEY = "geoss_option_key";

    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 50;

    /**
     * 
     */
    public ICOSConnectorSetting() {

	setPageSize(DEFAULT_PAGE_SIZE);
	
	Option<BooleanChoice> option = BooleanChoiceOptionBuilder.get().//
		withKey(GEOSS_OPTION_KEY).//
		withLabel("ICOS in GEOSS").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(option);

    }
    
    
    
    /**
     * @param set
     */
    public void setUseICOSInGEOSS(boolean set) {

	getOption(GEOSS_OPTION_KEY, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(set));
    }

    /**
     * @return
     */
    public boolean isUseICOSInGEOSS() {

	return BooleanChoice.toBoolean(getOption(GEOSS_OPTION_KEY, BooleanChoice.class).get().getSelectedValue());
    }


    /**
     * @param object
     */
    public ICOSConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ICOSConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return ICOSConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "ICOS Connector settings";
    }
}
