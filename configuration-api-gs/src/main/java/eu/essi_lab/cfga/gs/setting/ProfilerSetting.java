/**
 * 
 */
package eu.essi_lab.cfga.gs.setting;

import java.util.Arrays;

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

import com.vaadin.flow.data.provider.SortDirection;

import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.KeyValueOptionDecorator;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public abstract class ProfilerSetting extends Setting implements KeyValueOptionDecorator {

    private static final String PATH_OPTION_KEY = "pathOption";
    private static final String TYPE_OPTION_KEY = "typeOption";
    private static final String VERSION_OPTION_KEY = "versionOption";
    private static final String ONLINE_OPTION = "onlineOption";

    public ProfilerSetting() {

	setEditable(true);
	enableCompactMode(false);
	enableFoldedMode(true);
	setCanBeDisabled(false);

	Option<String> typeOption = StringOptionBuilder.get().//
		withKey(TYPE_OPTION_KEY).//
		withLabel("Type").//
		readOnly().//
		cannotBeDisabled().//
		build();

	addOption(typeOption);

	Option<String> pathOption = StringOptionBuilder.get().//
		withKey(PATH_OPTION_KEY).//
		withLabel("Path").//
		readOnly().//
		cannotBeDisabled().//
		build();

	addOption(pathOption);

	Option<String> versionOption = StringOptionBuilder.get().//
		withKey(VERSION_OPTION_KEY).//
		withLabel("Version").//
		readOnly().//
		cannotBeDisabled().//
		build();

	addOption(versionOption);

	//
	//
	//

	Option<String> onlineOption = StringOptionBuilder.get().//
		withKey(ONLINE_OPTION).//
		withLabel("Service state").//
		withDescription("If the service is offline, requests forwarded on the service path return a 404 'Not Found' error code").//
		withSingleSelection().//
		withValues(Arrays.asList("Online", "Offline")).//
		withSelectedValue("Online").//
		cannotBeDisabled().//
		build();

	addOption(onlineOption);

	//
	//
	//

	addKeyValueOption();

	//
	// set the component extension
	//
	setExtension(new ProfilerComponentInfo());
    }

    /**
     * @author Fabrizio
     */
    public static class ProfilerComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public ProfilerComponentInfo() {

	    setComponentName(ProfilerSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(TabIndex.PROFILER_SETTING.getIndex()).//
		    withShowDirective("Profilers", SortDirection.ASCENDING).//
		    build();

	    setTabInfo(tabInfo);
	}
    }

    /**
     * @return
     */
    public boolean isOnline() {

	return getOption(ONLINE_OPTION, String.class).get().getSelectedValue().equals("Online");
    }

    /**
     * Get the path where the "GI-suite service" is expected to receive the
     * {@link Profiler} requests from the suitable clients
     * 
     * @return a non <code>null</code> string which contains only alphabetic
     *         characters
     */
    public String getServicePath() {

	return getOption(PATH_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * Set the path where the "GI-suite service" is expected to receive the
     * {@link Profiler} requests from the suitable clients
     * 
     * @param path a non <code>null</code> string which contains only alphabetic
     *        characters
     */
    public void setServicePath(String path) {

	getOption(PATH_OPTION_KEY, String.class).get().setValue(path);
    }

    /**
     * Returns the {@link Profiler} name
     * 
     * @return a non <code>null</code> string
     */
    public String getServiceName() {

	return getName();
    }

    /**
     * Set the {@link Profiler} name
     * 
     * @param name a non <code>null</code> string
     */
    public void setServiceName(String name) {

	setName(name);
    }

    /**
     * Returns the type of the {@link Profiler} service
     * 
     * @return a non <code>null</code> string
     */
    public String getServiceType() {

	return getOption(TYPE_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * Set the type of the {@link Profiler} service (e.g: "OAI-PMH", "OpenSearch",
     * etc..) by adding the suffix "Profiler" to the type.<br>
     * This method also set the {@link #getConfigurableType()} with the same value
     * 
     * @param type
     */
    public void setServiceType(String type) {

	type = type + "Profiler";

	getOption(TYPE_OPTION_KEY, String.class).get().setValue(type);
	setConfigurableType(type);
    }

    /**
     * Get the version of the {@link Profiler} service
     * 
     * @return a non <code>null</code> string
     */
    public String getServiceVersion() {

	return getOption(VERSION_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * Set the version of the {@link Profiler} service (e.g: "OAI-PMH",
     * "OpenSearch", etc..)
     * 
     * @param version a non <code>null</code> string
     */
    public void setServiceVersion(String version) {

	getOption(VERSION_OPTION_KEY, String.class).get().setValue(version);
    }

}
