/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.oauth;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public abstract class OAuthProviderSetting extends Setting {

    
    private static final String LOGIC_URL_ID_OPTION_KEY = "loginUrlOptionKey";
    private static final String TOKEN_URL_OPTION_KEY = "tokenUrlOptionKey";
    private static final String USER_INFO_URL_OPTION_KEY = "userInfoUrlOptionKey";

    /**
     * 
     */
    public OAuthProviderSetting() {

	setCanBeDisabled(false);
	setEditable(false);
	enableCompactMode(false);

	Option<String> loginUrlOption = StringOptionBuilder.get().//
		withKey(LOGIC_URL_ID_OPTION_KEY).//
		withLabel("Login URL").//
		withValue(getDefaultLoginURL()).//
		cannotBeDisabled().//
		required().//
		build();

	addOption(loginUrlOption);

	Option<String> tokeUrlOption = StringOptionBuilder.get().//
		withKey(TOKEN_URL_OPTION_KEY).//
		withLabel("Get token URL").//
		withValue(getDefaultTokenURL()).//
		cannotBeDisabled().//
		required().//
		build();

	addOption(tokeUrlOption);

	Option<String> userInfoUrlOption = StringOptionBuilder.get().//
		withKey(USER_INFO_URL_OPTION_KEY).//
		withLabel("Get user info URL").//
		withValue(getDefaultUserInfoURL()).//
		cannotBeDisabled().//
		required().//
		build();

	addOption(userInfoUrlOption);
    }

    /**
     * @param object
     */
    public OAuthProviderSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public OAuthProviderSetting(String object) {

	super(object);
    }

    /**
     * @param url
     */
    public void setLoginURL(String url) {

	getOption(LOGIC_URL_ID_OPTION_KEY, String.class).get().setValue(url);
    }

    /**
     * @param url
     */
    public void setTokenURL(String url) {

	getOption(TOKEN_URL_OPTION_KEY, String.class).get().setValue(url);
    }

    /**
     * @param url
     */
    public void setUserInfoURL(String url) {

	getOption(USER_INFO_URL_OPTION_KEY, String.class).get().setValue(url);
    }

    /**
     * @return
     */
    public Optional<String> getLoginURL() {

	return getOption(LOGIC_URL_ID_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @return
     */
    public Optional<String> getTokenURL() {

	return getOption(TOKEN_URL_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @return
     */
    public Optional<String> getUserInfoURL() {

	return getOption(USER_INFO_URL_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @return
     */
    protected abstract String getDefaultLoginURL();

    /**
     * @return
     */
    protected abstract String getDefaultTokenURL();

    /**
     * @return
     */
    protected abstract String getDefaultUserInfoURL();

}
