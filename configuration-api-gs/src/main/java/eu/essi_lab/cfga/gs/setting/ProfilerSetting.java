/**
 * 
 */
package eu.essi_lab.cfga.gs.setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import com.vaadin.flow.data.provider.SortDirection;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.menuitems.ProfilerStateOfflineItemHandler;
import eu.essi_lab.cfga.gs.setting.menuitems.ProfilerStateOnlineItemHandler;
import eu.essi_lab.cfga.gui.components.grid.ColumnDescriptor;
import eu.essi_lab.cfga.gui.components.grid.GridMenuItemHandler;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.KeyValueOptionDecorator;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.cfga.setting.validation.Validator;

/**
 * @author Fabrizio
 */
public abstract class ProfilerSetting extends Setting implements KeyValueOptionDecorator {

    private static final String PATH_OPTION_KEY = "pathOption";
    private static final String TYPE_OPTION_KEY = "typeOption";
    private static final String VERSION_OPTION_KEY = "versionOption";
    private static final String STATE_OPTION = "stateOption";

    /**
     * 
     */
    public ProfilerSetting() {

	setCanBeRemoved(true);
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
		withDescription("Service path must be unique can contains alphanumeric characters and underscores").//
		withInputPattern(InputPattern.ALPHANUMERIC_AND_UNDERSCORE).//
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
		withKey(STATE_OPTION).//
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

	//
	// set the validator
	//
	setValidator(new ProfilerSettingValidator());
    }

    /**
     * @author Fabrizio
     */
    public static class ProfilerSettingValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    ProfilerSetting thisSetting = (ProfilerSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	    ValidationResponse validationResponse = new ValidationResponse();

	    if (context.getContext().equals(ValidationContext.PUT)) {

		String servicePath = thisSetting.getServicePath();

		boolean exists = configuration.list(ProfilerSetting.class, false).//
			stream().//
			anyMatch(s -> s.getServicePath().equals(servicePath));

		if (exists) {

		    validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		    validationResponse.getErrors().add("Another profiler with path '" + servicePath + "' already exists");
		}
	    }

	    return validationResponse;
	}
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
		    withIndex(GSTabIndex.PROFILERS.getIndex()).//
		    withAddDirective("Add profiler", ProfilerSettingSelector.class). //
		    withEditDirective("Edit profiler", ConfirmationPolicy.ON_WARNINGS).//
		    withRemoveDirective("Remove profiler", false, ProfilerSetting.class).//
		    withShowDirective("Profilers", SortDirection.ASCENDING).//
		    withGridInfo(Arrays.asList(//

			    ColumnDescriptor.createPositionalDescriptor(), //

			    ColumnDescriptor.create("Name", true, true, (s) -> s.getName()), //

			    ColumnDescriptor.create("State", 150, true, true, //

				    (s) -> getServiceState(s), //

				    (item1, item2) -> item1.get("State").compareTo(item2.get("State")), //

				    new ProfilerStateColumnRenderer()), //

			    ColumnDescriptor.create("Path", 200, true, true, (s) -> getServicePath(s)), //

			    ColumnDescriptor.create("Type", 300, true, true, (s) -> getServiceType(s)), //

			    ColumnDescriptor.create("Version", true, true, (s) -> getServiceVersion(s)) //

		    ), getItemsList(), com.vaadin.flow.component.grid.Grid.SelectionMode.MULTI).//

		    build();

	    setTabInfo(tabInfo);
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getServiceState(Setting setting) {

	    return setting.getOption(STATE_OPTION, String.class).get().getSelectedValue();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getServicePath(Setting setting) {

	    return setting.getOption(PATH_OPTION_KEY, String.class).get().getValue();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getServiceType(Setting setting) {

	    return setting.getOption(TYPE_OPTION_KEY, String.class).get().getValue();
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getServiceVersion(Setting setting) {

	    return setting.getOption(VERSION_OPTION_KEY, String.class).get().getValue();
	}

	/**
	 * @return
	 */
	private List<GridMenuItemHandler> getItemsList() {

	    ArrayList<GridMenuItemHandler> list = new ArrayList<>();

	    list.add(new ProfilerStateOnlineItemHandler());
	    list.add(new ProfilerStateOfflineItemHandler(true, false));

	    return list;
	}
    }

    /**
     * @param online
     */
    public void setOnline(boolean online) {

	getOption(STATE_OPTION, String.class).get().select(state -> online ? state.equals("Online") : state.equals("Offline"));
    }

    /**
     * @return
     */
    public boolean isOnline() {

	return getOption(STATE_OPTION, String.class).get().getSelectedValue().equals("Online");
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
