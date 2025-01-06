package eu.essi_lab.accessor.kisters;

import java.util.Arrays;

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
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class KISTERSConnectorSetting extends HarvestedConnectorSetting {

    public final String KISTERS_SITE_NAME = "KISTERS_SITE_NAME";

    /**
     * 
     */
    public KISTERSConnectorSetting() {
	Option<String> option = StringOptionBuilder.get().//
		withKey(KISTERS_SITE_NAME).//
		withLabel("Site name (empty for all sites)").//
		withValue("").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @return
     */
    public String getSiteName() {

	return getOption(KISTERS_SITE_NAME, String.class).get().getValue();
    }

    /**
     * @param object
     */
    public KISTERSConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public KISTERSConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return KISTERSConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "KISTERS Connector settings";
    }

}
