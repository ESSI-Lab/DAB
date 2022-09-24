package eu.essi_lab.cfga.gs.setting;

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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.ConfigurableSetting;

/**
 * @author Fabrizio
 */
public class CredentialsSetting extends ConfigurableSetting implements EditableSetting {

    private static final String DINAGUA_USER_OPTION_KEY = "dinaguaUser";
    private static final String DINAGUA_PASSWORD_OPTION_KEY = "dinaguaPassword";
    private static final String DINAGUA_TOKEN_OPTION_KEY = "dinaguaToken";
    private static final String INUMET_USER_OPTION_KEY = "inumetUser";
    private static final String INUMET_PASSWORD_OPTION_KEY = "inumetPassword";
    private static final String DMH_TOKEN_OPTION_KEY = "dmhToken";
    private static final String WEKEO_USER_OPTION_KEY = "wekeoUser";
    private static final String WEKEO_PASSWORD_OPTION_KEY = "wekeoPassword";
    private static final String NVE_TOKEN_OPTION_KEY = "nveToken";
    private static final String SENTINEL_USER_OPTION_KEY = "sentinelUser";
    private static final String SENTINEL_PASSWORD_OPTION_KEY = "sentinelPassword";
    private static final String SENTINEL_DOWNLOADER_TOKEN_OPTION_KEY = "sentinelDownloaderToken";
    private static final String SOS_TAHMO_TOKEN_OPTION = "sosTahmoToken";

    /**
     * 
     */
    public CredentialsSetting() {

	setName("Credentials settings");
	enableCompactMode(false);
	setCanBeDisabled(false);

	//
	// DINAGUA
	//
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(DINAGUA_USER_OPTION_KEY).//
		    withLabel("The username used by the DINAGUA connector and downloader").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(DINAGUA_PASSWORD_OPTION_KEY).//
		    withLabel("The password used by the DINAGUA connector and downloader").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

	    Option<String> token = StringOptionBuilder.//
		    get().//
		    withKey(DINAGUA_TOKEN_OPTION_KEY).//
		    withLabel("The token used by the DINAGUA connector and downloader").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(token);
	}

	//
	// INUMET
	//
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(INUMET_USER_OPTION_KEY).//
		    withLabel("The username used by the INUMET connector and downloader").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(INUMET_PASSWORD_OPTION_KEY).//
		    withLabel("The password used by the INUMET connector and downloader").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);
	}
	
	//
	// DMH
	//
	{

	    Option<String> token = StringOptionBuilder.//
		    get().//
		    withKey(DMH_TOKEN_OPTION_KEY).//
		    withLabel("The token used by the DMH client to authorize the connection").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(token);
	}

	//
	// WEkEO
	//

	{
	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(WEKEO_USER_OPTION_KEY).//
		    withLabel("The username used by the WEkEO connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(WEKEO_PASSWORD_OPTION_KEY).//
		    withLabel("The password used by the WEkEO connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);
	}

	//
	// NVE
	//

	Option<String> nveToken = StringOptionBuilder.//
		get().//
		withKey(NVE_TOKEN_OPTION_KEY).//
		withLabel("The token used by the Norwegian Water Resources and Energy Directorat connector and downloader").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(nveToken);

	//
	// Sentinel
	//

	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(SENTINEL_USER_OPTION_KEY).//
		    withLabel("The username used by Sentinel connector and mapper").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(SENTINEL_PASSWORD_OPTION_KEY).//
		    withLabel("The password used by Sentinel connector and mapper").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

	    Option<String> token = StringOptionBuilder.//
		    get().//
		    withKey(SENTINEL_DOWNLOADER_TOKEN_OPTION_KEY).//
		    withLabel("The token used by Sentinel downloader for generating previews").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(token);
	}

	{

	    Option<String> token = StringOptionBuilder.//
		    get().//
		    withKey(SOS_TAHMO_TOKEN_OPTION).//
		    withLabel("The token used by SOS THAMO connector to access the service").//
		    required().//
		    withTextArea().//
		    cannotBeDisabled().//
		    build();

	    addOption(token);
	}
	//
	// set the rendering extension
	//
	setExtension(new CredentialsSettingComponentInfo());
    }

    /**
     * @author Fabrizio
     */
    public static class CredentialsSettingComponentInfo extends ComponentInfo {

	public static int tabIndex = 8;

	/**
	 * 
	 */
	public CredentialsSettingComponentInfo() {

	    setComponentName(CredentialsSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(tabIndex).//
		    withShowDirective("Credentials").//
		    build();

	    setTabInfo(tabInfo);
	}
    }

    /**
     * @param object
     */
    public CredentialsSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public CredentialsSetting(String object) {

	super(object);
    }

    /**
     * @param user
     */
    public void setDinaguaUser(String user) {

	getOption(DINAGUA_USER_OPTION_KEY, String.class).get().setValue(user);
    }

    /**
     * 
     */
    public Optional<String> getDinaguaUser() {

	return getOption(DINAGUA_USER_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setDinaguaPassword(String password) {

	getOption(DINAGUA_PASSWORD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getDinaguaPassword() {

	return getOption(DINAGUA_PASSWORD_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param token
     */
    public void setDinaguaToken(String token) {

	getOption(DINAGUA_TOKEN_OPTION_KEY, String.class).get().setValue(token);
    }

    /**
     * 
     */
    public Optional<String> getDinaguaToken() {

	return getOption(DINAGUA_TOKEN_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setINUMETUser(String user) {

	getOption(INUMET_USER_OPTION_KEY, String.class).get().setValue(user);
    }

    /**
     *
     */
    public Optional<String> getINUMETUser() {

	return getOption(INUMET_USER_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setINUMETPassword(String password) {

	getOption(INUMET_PASSWORD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     * 
     */
    public Optional<String> getINUMETPassword() {

	return getOption(INUMET_PASSWORD_OPTION_KEY, String.class).get().getOptionalValue();
    }
    
    /**
     * @param token
     */
    public void setDMHToken(String token) {

	getOption(DMH_TOKEN_OPTION_KEY, String.class).get().setValue(token);
    }

    /**
     * 
     */
    public Optional<String> getDMHToken() {

	return getOption(DMH_TOKEN_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setWekeoUser(String user) {

	getOption(WEKEO_USER_OPTION_KEY, String.class).get().setValue(user);
    }

    /**
     * 
     */
    public Optional<String> getWekeUser() {

	return getOption(WEKEO_USER_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setWekeoPassword(String password) {

	getOption(WEKEO_PASSWORD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     * 
     */
    public Optional<String> getWekeoPassword() {

	return getOption(WEKEO_PASSWORD_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setNVEToken(String password) {

	getOption(NVE_TOKEN_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     * 
     */
    public Optional<String> getNVEToken() {

	return getOption(NVE_TOKEN_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setSentinelUser(String user) {

	getOption(SENTINEL_USER_OPTION_KEY, String.class).get().setValue(user);
    }

    /**
     * 
     */
    public Optional<String> getSentinelUser() {

	return getOption(SENTINEL_USER_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setSentinelPassword(String password) {

	getOption(SENTINEL_PASSWORD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getSentinelPassword() {

	return getOption(SENTINEL_PASSWORD_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param token
     */
    public void setSentinelDownloaderToken(String token) {

	getOption(SENTINEL_DOWNLOADER_TOKEN_OPTION_KEY, String.class).get().setValue(token);
    }

    /**
     *
     */
    public Optional<String> getSentinelDownloaderToken() {

	return getOption(SENTINEL_DOWNLOADER_TOKEN_OPTION_KEY, String.class).get().getOptionalValue();
    }
    
    /**
     * @param token
     */
    public void setSOSTahmoToken(String token) {

	getOption(SOS_TAHMO_TOKEN_OPTION, String.class).get().setValue(token);
    }

    /**
     *
     */
    public Optional<String> getSOSTahmoToken() {

	return getOption(SOS_TAHMO_TOKEN_OPTION, String.class).get().getOptionalValue();
    }

    @Override
    public String getType() {

	return "CredentialsSetting";
    }

}
