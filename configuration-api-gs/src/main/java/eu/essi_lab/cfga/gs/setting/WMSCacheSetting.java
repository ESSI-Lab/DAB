package eu.essi_lab.cfga.gs.setting;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting.ComputationType;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author boldrini
 */
public class WMSCacheSetting extends Setting {

    private static final String WMSCACHE_MODE_KEY = "wmsCacheMode";
    private static final String WMS_CACHE_HOSTNAME_KEY = "wmsCacheHostname";
    private static final String WMS_CACHE_PASSWORD_KEY = "wmsCachePassword";
    private static final String WMS_CACHE_FOLDERNAME = "wmsCacheFolderName";

    public enum WMSCacheMode implements LabeledEnum {

	/**
	 * 
	 */
	DISABLED("Disabled"),
	/**
	 * 
	 */
	LOCAL_FILESYSTEM("Local filesystem"),
	/**
	 * 
	 */
	REDIS("Redis");

	private final String name;

	/**
	 * @param name
	 */
	WMSCacheMode(String name) {

	    this.name = name;
	}

	@Override
	public String toString() {

	    return getLabel();
	}

	@Override
	public String getLabel() {

	    return name;
	}
    }

    /**
     * 
     */
    public WMSCacheSetting() {

	setName("WMS Cache Settings");
	setDescription("Settings required by the DAB to enable and configure the WMS cache.");
	enableCompactMode(false);
	setEditable(false);
	setEnabled(false);

	Option<WMSCacheMode> compType = OptionBuilder.get(WMSCacheMode.class).//
		withKey(WMSCACHE_MODE_KEY).//
		withLabel("WMS cache mode").//
		withSingleSelection().//
		withValues(LabeledEnum.values(WMSCacheMode.class)).//
		withSelectedValue(LabeledEnum.values(WMSCacheMode.class).getFirst()).//
		cannotBeDisabled().//
		build();

	addOption(compType);

	Option<String> hostname = StringOptionBuilder.get().//
		withKey(WMS_CACHE_HOSTNAME_KEY).//
		withLabel("Hostname").//
		cannotBeDisabled().//
		build();

	addOption(hostname);

	Option<String> password = StringOptionBuilder.get().//
		withKey(WMS_CACHE_PASSWORD_KEY).//
		withLabel("Password").//
		cannotBeDisabled().//
		build();

	addOption(password);

	Option<String> folder = StringOptionBuilder.get().//
		withKey(WMS_CACHE_FOLDERNAME).//
		withLabel("Folder name").//
		cannotBeDisabled().//
		build();

	addOption(folder);
    }

    /**
     * @param object
     */
    public WMSCacheSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public WMSCacheSetting(String object) {

	super(object);
    }

    /**
     *  
     */
    public void setMode(WMSCacheMode computationType) {

	getOption(WMSCACHE_MODE_KEY, WMSCacheMode.class).get().setValue(computationType);
    }

    /**
     * @return
     */
    public WMSCacheMode getMode() {

	return getOption(WMSCACHE_MODE_KEY, WMSCacheMode.class).get().getValue();
    }

    /**
     * @param user
     */
    public void setHostname(String user) {

	getOption(WMS_CACHE_HOSTNAME_KEY, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getHostname() {

	return getOption(WMS_CACHE_HOSTNAME_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setPassword(String user) {

	getOption(WMS_CACHE_PASSWORD_KEY, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getPassword() {

	return getOption(WMS_CACHE_PASSWORD_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param user
     */
    public void setFoldername(String user) {

	getOption(WMS_CACHE_FOLDERNAME, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public Optional<String> getFoldername() {

	return getOption(WMS_CACHE_FOLDERNAME, String.class).get().getOptionalValue();
    }

}
