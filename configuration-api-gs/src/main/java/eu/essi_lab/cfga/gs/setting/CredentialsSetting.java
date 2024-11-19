package eu.essi_lab.cfga.gs.setting;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

    private static final String HIS_CENTRAL_PUGLIA_TOKEN = "pugliaToken";
    private static final String AGROSTAC_TOKEN = "agrostacToken";
    private static final String HIS_CENTRAL_SARDEGNA_API_KEY = "sardegnaApiKey";
    private static final String HIS_CENTRAL_LOMBARDIA_KEYSTORE_PASSWORD = "lombardiaKeystorePassword";
    private static final String HIS_CENTRAL_LOMBARDIA_USERNAME = "lombardiaUser";
    private static final String HIS_CENTRAL_LOMBARDIA_PASSWORD = "lombardiaPassword";
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
    private static final String SAEON_USER_OPTION_KEY = "saeonUser";
    private static final String SAEON_PASSWORD_OPTION_KEY = "saeonPassword";
    private static final String ACRONET_USER_OPTION_KEY = "acronetUser";
    private static final String ACRONET_PASSWORD_OPTION_KEY = "acronetPassword";
    private static final String ACRONET_CLIENT_ID = "acronetClientId";
    private static final String ACRONET_CLIENT_PASSWORD = "acronetClientPassword";

    private static final String HIS_CENTRAL_BASILICATA_CLIENT_USERNAME = "basilicataUsername";
    private static final String HIS_CENTRAL_BASILICATA_CLIENT_PASSWORD = "basilicataPassword";
    private static final String HIS_CENTRAL_BASILICATA_CLIENT_INSTANCE = "basilicataInstance";
    private static final String HIS_CENTRAL_BASILICATA_CLIENT_ID = "basilicataId";

    private static final String HIS_CENTRAL_LAZIO_CLIENT_USERNAME = "lazioUsername";
    private static final String HIS_CENTRAL_LAZIO_CLIENT_PASSWORD = "lazioPassword";
    private static final String HIS_CENTRAL_LAZIO_CLIENT_INSTANCE = "lazioInstance";
    private static final String HIS_CENTRAL_LAZIO_CLIENT_ID = "lazioId";

    private static final String HIS_CENTRAL_FRIULI_CLIENT_PASSWORD = "friuliPassword";
    private static final String HIS_CENTRAL_FRIULI_CLIENT_ID = "friuliId";

    private static final String HIS_CENTRAL_AOSTA_CLIENT_PASSWORD = "aostaPassword";
    private static final String HIS_CENTRAL_AOSTA_CLIENT_ID = "aostaId";

    // i-change-trigger
    private static final String METEOTRACKER_PASSWORD = "meteotrackerPassword";
    private static final String METEOTRACKER_USER = "meteotrackerUser";

    // i-change-trigger
    private static final String POLYTOPE_PASSWORD = "polytopePassword";
    private static final String POLYTOPE_USER = "polytopeUser";

    private static final String TRIGGER_PASSWORD = "triggerPassword";
    private static final String TRIGGER_USER = "triggerUser";

    public static void main(String[] args) {

	CredentialsSetting credentialsSetting = new CredentialsSetting();
	System.out.println(credentialsSetting);
    }

    /**
     * 
     */
    public CredentialsSetting() {

	setName("Credentials settings");
	enableCompactMode(false);
	setCanBeDisabled(false);
	//
	// LOMBARDIA
	//
	{

	    Option<String> keystore = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_LOMBARDIA_KEYSTORE_PASSWORD).//
		    withLabel("The password for the TLS certificate to be used by Lombardia client").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(keystore);

	    Option<String> username = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_LOMBARDIA_USERNAME).//
		    withLabel("The username used by the Lombardia connector and downloader").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(username);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_LOMBARDIA_PASSWORD).//
		    withLabel("The password used by the Lombardia connector and downloader").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);
	}

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

	{
	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_SARDEGNA_API_KEY).//
		    withLabel("The Api-key used by the Sardegna connector and downloader").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);
	}

	{
	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_PUGLIA_TOKEN).//
		    withLabel("The token used by the HIS-Central Puglia connector and downloader").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);
	}

	//
	// AGROSTAC
	//

	{
	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(AGROSTAC_TOKEN).//
		    withLabel("The token used by the AGROSTAC connector and WorldCereal profiler").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);
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

	// SAEON
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(SAEON_USER_OPTION_KEY).//
		    withLabel("The username used by SAEON connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(SAEON_PASSWORD_OPTION_KEY).//
		    withLabel("The password used by SAEON connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

	}

	// ACRONET
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(ACRONET_USER_OPTION_KEY).//
		    withLabel("The username used by ACRONET connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(ACRONET_PASSWORD_OPTION_KEY).//
		    withLabel("The password used by ACRONET connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

	    Option<String> id = StringOptionBuilder.//
		    get().//
		    withKey(ACRONET_CLIENT_ID).//
		    withLabel("The client id used by Acronet connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(id);

	    Option<String> clientSecret = StringOptionBuilder.//
		    get().//
		    withKey(ACRONET_CLIENT_PASSWORD).//
		    withLabel("The client secret used by Acronet connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(clientSecret);

	}

	// HIS-Central Basilicata
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_BASILICATA_CLIENT_USERNAME).//
		    withLabel("The username used by HIS-Central Basilicata connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_BASILICATA_CLIENT_PASSWORD).//
		    withLabel("The password used by HIS-Central Basilicata connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

	    Option<String> instance = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_BASILICATA_CLIENT_INSTANCE).//
		    withLabel("The client instance used by HIS-Central Basilicata connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(instance);

	    Option<String> id = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_BASILICATA_CLIENT_ID).//
		    withLabel("The client id used by HIS-Central Basilicata connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(id);

	}

	// HIS-Central Lazio
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_LAZIO_CLIENT_USERNAME).//
		    withLabel("The username used by HIS-Central Lazio connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_LAZIO_CLIENT_PASSWORD).//
		    withLabel("The password used by HIS-Central Lazio connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

	    Option<String> instance = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_LAZIO_CLIENT_INSTANCE).//
		    withLabel("The client instance used by HIS-Central Lazio connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(instance);

	    Option<String> id = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_LAZIO_CLIENT_ID).//
		    withLabel("The client id used by HIS-Central Lazio connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(id);

	}

	// HIS-Central Friuli
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_FRIULI_CLIENT_ID).//
		    withLabel("The username used by HIS-Central Friuli connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_FRIULI_CLIENT_PASSWORD).//
		    withLabel("The password used by HIS-Central Friuli connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

	}

	// HIS-Central Valdaosta
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_AOSTA_CLIENT_ID).//
		    withLabel("The username used by HIS-Central Valle d'Aosta connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(HIS_CENTRAL_AOSTA_CLIENT_PASSWORD).//
		    withLabel("The password used by HIS-Central Valle d'Aosta connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

	}

	// Meteotracker
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(METEOTRACKER_USER).//
		    withLabel("The username used by Meteotracker connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(METEOTRACKER_PASSWORD).//
		    withLabel("The password used by Meteotracker connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

	}

	// Polytope
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(POLYTOPE_USER).//
		    withLabel("The username used by Polytope connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(POLYTOPE_PASSWORD).//
		    withLabel("The password used by Polytope connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

	}

	// Trigger
	{

	    Option<String> user = StringOptionBuilder.//
		    get().//
		    withKey(TRIGGER_USER).//
		    withLabel("The username used by TRIGGER connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(user);

	    Option<String> password = StringOptionBuilder.//
		    get().//
		    withKey(TRIGGER_PASSWORD).//
		    withLabel("The password used by TRIGGER connector").//
		    required().//
		    cannotBeDisabled().//
		    build();

	    addOption(password);

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

	/**
	 * 
	 */
	public CredentialsSettingComponentInfo() {

	    setComponentName(CredentialsSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(TabIndex.CREDENTIALS_SETTING.getIndex()).//
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

    /**
     * @param user
     */
    public void setSAEONlUser(String user) {

	getOption(SAEON_USER_OPTION_KEY, String.class).get().setValue(user);
    }

    /**
     * 
     */
    public Optional<String> getSAEONUser() {

	return getOption(SAEON_USER_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setSAEONPassword(String password) {

	getOption(SAEON_PASSWORD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getSAEONPassword() {

	return getOption(SAEON_PASSWORD_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setACRONETUser(String user) {

	getOption(ACRONET_USER_OPTION_KEY, String.class).get().setValue(user);
    }

    /**
     * 
     */
    public Optional<String> getACRONETUser() {

	return getOption(ACRONET_USER_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setACRONETPassword(String password) {

	getOption(ACRONET_PASSWORD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getACRONETPassword() {

	return getOption(ACRONET_PASSWORD_OPTION_KEY, String.class).get().getOptionalValue();
    }

    @Override
    public String getType() {

	return "CredentialsSetting";
    }

    /**
     * @param user
     */
    public void setACRONETClientId(String user) {

	getOption(ACRONET_CLIENT_ID, String.class).get().setValue(user);
    }

    /**
     * 
     */
    public Optional<String> getACRONETClientId() {

	return getOption(ACRONET_CLIENT_ID, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setACRONETClientPassword(String password) {

	getOption(ACRONET_CLIENT_PASSWORD, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getACRONETClientPassword() {

	return getOption(ACRONET_CLIENT_PASSWORD, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setLombardiaKeystorePassword(String password) {

	getOption(HIS_CENTRAL_LOMBARDIA_KEYSTORE_PASSWORD, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getLombardiaKeystorePassword() {

	return getOption(HIS_CENTRAL_LOMBARDIA_KEYSTORE_PASSWORD, String.class).get().getOptionalValue();
    }

    /**
     * @param username
     */
    public void setLombardiaUsername(String username) {

	getOption(HIS_CENTRAL_LOMBARDIA_USERNAME, String.class).get().setValue(username);
    }

    /**
     *
     */
    public Optional<String> getLombardiaUsername() {

	return getOption(HIS_CENTRAL_LOMBARDIA_USERNAME, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setLombardiaPassword(String password) {

	getOption(HIS_CENTRAL_LOMBARDIA_PASSWORD, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getLombardiaPassword() {

	return getOption(HIS_CENTRAL_LOMBARDIA_PASSWORD, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setBasilicataClientPassword(String password) {

	getOption(HIS_CENTRAL_BASILICATA_CLIENT_PASSWORD, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getBasilicataClientPassword() {

	return getOption(HIS_CENTRAL_BASILICATA_CLIENT_PASSWORD, String.class).get().getOptionalValue();
    }

    /**
     * @param id
     */
    public void setBasilicataClientId(String id) {

	getOption(HIS_CENTRAL_BASILICATA_CLIENT_ID, String.class).get().setValue(id);
    }

    /**
     *
     */
    public Optional<String> getBasilicataClientId() {

	return getOption(HIS_CENTRAL_BASILICATA_CLIENT_ID, String.class).get().getOptionalValue();
    }

    /**
     * @param instance
     */
    public void setBasilicataClientInstance(String instance) {

	getOption(HIS_CENTRAL_BASILICATA_CLIENT_INSTANCE, String.class).get().setValue(instance);
    }

    /**
     *
     */
    public Optional<String> getBasilicataClientInstance() {

	return getOption(HIS_CENTRAL_BASILICATA_CLIENT_INSTANCE, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setBasilicataClientUser(String user) {

	getOption(HIS_CENTRAL_BASILICATA_CLIENT_USERNAME, String.class).get().setValue(user);
    }

    /**
     *
     */
    public Optional<String> getBasilicataClientUser() {

	return getOption(HIS_CENTRAL_BASILICATA_CLIENT_USERNAME, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setLazioClientPassword(String password) {

	getOption(HIS_CENTRAL_LAZIO_CLIENT_PASSWORD, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getLazioClientPassword() {

	return getOption(HIS_CENTRAL_LAZIO_CLIENT_PASSWORD, String.class).get().getOptionalValue();
    }

    /**
     * @param id
     */
    public void setLazioClientId(String id) {

	getOption(HIS_CENTRAL_LAZIO_CLIENT_ID, String.class).get().setValue(id);
    }

    /**
     *
     */
    public Optional<String> getLazioClientId() {

	return getOption(HIS_CENTRAL_LAZIO_CLIENT_ID, String.class).get().getOptionalValue();
    }

    /**
     * @param instance
     */
    public void setLazioClientInstance(String instance) {

	getOption(HIS_CENTRAL_LAZIO_CLIENT_INSTANCE, String.class).get().setValue(instance);
    }

    /**
     *
     */
    public Optional<String> getLazioClientInstance() {

	return getOption(HIS_CENTRAL_LAZIO_CLIENT_INSTANCE, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setLazioClientUser(String user) {

	getOption(HIS_CENTRAL_LAZIO_CLIENT_USERNAME, String.class).get().setValue(user);
    }

    /**
     *
     */
    public Optional<String> getLazioClientUser() {

	return getOption(HIS_CENTRAL_LAZIO_CLIENT_USERNAME, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setFriuliClientId(String user) {

	getOption(HIS_CENTRAL_FRIULI_CLIENT_ID, String.class).get().setValue(user);
    }

    /**
     *
     */
    public Optional<String> getFriuliClientId() {

	return getOption(HIS_CENTRAL_FRIULI_CLIENT_ID, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setFriuliClientPassword(String password) {

	getOption(HIS_CENTRAL_FRIULI_CLIENT_PASSWORD, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getFriuliClientPassword() {

	return getOption(HIS_CENTRAL_FRIULI_CLIENT_PASSWORD, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setAostaClientId(String user) {

	getOption(HIS_CENTRAL_AOSTA_CLIENT_ID, String.class).get().setValue(user);
    }

    /**
     *
     */
    public Optional<String> getAostaClientId() {

	return getOption(HIS_CENTRAL_AOSTA_CLIENT_ID, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setAostaClientPassword(String password) {

	getOption(HIS_CENTRAL_AOSTA_CLIENT_PASSWORD, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getAostaClientPassword() {

	return getOption(HIS_CENTRAL_AOSTA_CLIENT_PASSWORD, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setSardegnaApiKey(String password) {

	getOption(HIS_CENTRAL_SARDEGNA_API_KEY, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getSardegnaApiKey() {

	return getOption(HIS_CENTRAL_SARDEGNA_API_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setPugliaToken(String password) {

	getOption(HIS_CENTRAL_PUGLIA_TOKEN, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getPugliaToken() {

	return getOption(HIS_CENTRAL_PUGLIA_TOKEN, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setAGROSTACToken(String password) {

	getOption(AGROSTAC_TOKEN, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getAGROSTACToken() {

	return getOption(AGROSTAC_TOKEN, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setMeteotrackerUser(String user) {

	getOption(METEOTRACKER_USER, String.class).get().setValue(user);
    }

    /**
     *
     */
    public Optional<String> getMeteotrackerUser() {

	return getOption(METEOTRACKER_USER, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setMeteotrackerPassword(String password) {

	getOption(METEOTRACKER_PASSWORD, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getMeteotrackerPassword() {

	return getOption(METEOTRACKER_PASSWORD, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setPolytopeUser(String user) {

	getOption(POLYTOPE_USER, String.class).get().setValue(user);
    }

    /**
     *
     */
    public Optional<String> getPolytopeUser() {

	return getOption(POLYTOPE_USER, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setPolytopePassword(String password) {

	getOption(POLYTOPE_PASSWORD, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getPolytopePassword() {

	return getOption(POLYTOPE_PASSWORD, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setTriggerUser(String user) {

	getOption(TRIGGER_USER, String.class).get().setValue(user);
    }

    /**
     *
     */
    public Optional<String> getTriggerUser() {

	return getOption(TRIGGER_USER, String.class).get().getOptionalValue();
    }

    /**
     * @param password
     */
    public void setTriggerPassword(String password) {

	getOption(TRIGGER_PASSWORD, String.class).get().setValue(password);
    }

    /**
     *
     */
    public Optional<String> getTriggerPassword() {

	return getOption(TRIGGER_PASSWORD, String.class).get().getOptionalValue();
    }

}
