package eu.essi_lab.accessor.datahub;

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
 * @author Generated
 */
public class DatahubConnectorSetting extends HarvestedConnectorSetting {

    private static final int DEFAULT_PAGE_SIZE = 50;
    
    private static final String USERNAME_OPTION_KEY = "username";
    private static final String PASSWORD_OPTION_KEY = "password";
    private static final String IDENTIFIERS_URL_OPTION_KEY = "identifiersUrl";

    /**
     * 
     */
    public DatahubConnectorSetting() {

	 setPageSize(DEFAULT_PAGE_SIZE);
	
	// Add username option
	Option<String> usernameOption = StringOptionBuilder.get().//
		withKey(USERNAME_OPTION_KEY).//
		withLabel("Username").//
		cannotBeDisabled().//
		build();
		addOption(usernameOption);
	
	// Add password option
	Option<String> passwordOption = StringOptionBuilder.get().//
		withKey(PASSWORD_OPTION_KEY).//
		withLabel("Password").//
		cannotBeDisabled().//
		build();
	addOption(passwordOption);
	
	// Add identifiers URL option
	Option<String> identifiersUrlOption = StringOptionBuilder.get().//
		withKey(IDENTIFIERS_URL_OPTION_KEY).//
		withLabel("Identifiers URL (http:// or file://)").//
		cannotBeDisabled().//
		build();
	addOption(identifiersUrlOption);
    }
    
    /**
     * @return
     */
    public Optional<String> getUsername() { 
		Optional<Option<String>> option = getOption(USERNAME_OPTION_KEY, String.class);
		if (option.isPresent()) {
			return option.get().getOptionalValue();
		}
		return Optional.empty();
    }
    
    /**
     * @return
     */
    public Optional<String> getPassword() {
		Optional<Option<String>> option = getOption(PASSWORD_OPTION_KEY, String.class);
	if (option.isPresent()) {
	    return option.get().getOptionalValue();
	}
	return Optional.empty();
    }
    
    /**
     * @return
     */
    public Optional<String> getIdentifiersUrl() {
		Optional<Option<String>> option = getOption(IDENTIFIERS_URL_OPTION_KEY, String.class);
		if (option.isPresent()) {
			return option.get().getOptionalValue();
		}
		return Optional.empty();
	}

    /**
     * @param object
     */
    public DatahubConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public DatahubConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return DatahubConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Datahub Connector settings";
    }
}

