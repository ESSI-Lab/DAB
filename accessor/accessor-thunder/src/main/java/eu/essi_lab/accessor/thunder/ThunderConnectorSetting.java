package eu.essi_lab.accessor.thunder;

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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author Fabrizio
 */
public class ThunderConnectorSetting extends HarvestedConnectorSetting {

    private static final String FTP_PASSWORD_OPTION_KEY = "thunderFtpPassword";
    private static final int DEFAULT_PAGE_SIZE = 50;

    /**
     * 
     */
    public ThunderConnectorSetting() {

	setPageSize(DEFAULT_PAGE_SIZE);

	Option<String> option = StringOptionBuilder.//
		get().//
		withKey(FTP_PASSWORD_OPTION_KEY).//
		withLabel("FTP password").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(option);
    }

    /**
     * @param password
     */
    public void setFTPPassword(String password) {

	getOption(FTP_PASSWORD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     * @return
     */
    public Optional<String> getFTPassword() {

	return getOption(FTP_PASSWORD_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param object
     */
    public ThunderConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ThunderConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return ThunderConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Thunder Connector settings";
    }
}
