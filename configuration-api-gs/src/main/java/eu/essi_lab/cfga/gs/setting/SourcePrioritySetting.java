package eu.essi_lab.cfga.gs.setting;

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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class SourcePrioritySetting extends Setting implements EditableSetting {

    /**
     * 
     */
    private static final String SOURCES_SETTING_IDENTIFIER = "availableSources";

    /**
     * 
     */
    public SourcePrioritySetting() {

	setName("Priority sources");
	setDescription(
		"Collections and datasets of priority sources maintain their original id during harvesting.\nSelect one or more sources to prioritize");
	setCanBeDisabled(false);
	enableCompactMode(false);

	//
	// setting with available sources
	//
	Setting sourcesSetting = new Setting();
	sourcesSetting.setCanBeDisabled(false);
	sourcesSetting.setEditable(false);
	sourcesSetting.setShowHeader(false);

	sourcesSetting.setIdentifier(SOURCES_SETTING_IDENTIFIER);
	sourcesSetting.setSelectionMode(SelectionMode.MULTI);

	addSetting(sourcesSetting);

	if (ConfigurationWrapper.getConfiguration().isPresent()) {

	    ConfigurationWrapper.getHarvestedAndMixedSources().forEach(s -> {

		if (s.getUniqueIdentifier() != null && !s.getUniqueIdentifier().isEmpty()) {

		    Setting setting = new Setting();
		    setting.setCanBeDisabled(false);
		    setting.setEditable(false);
		    setting.setName(s.getLabel());
		    setting.setIdentifier(s.getUniqueIdentifier());

		    sourcesSetting.addSetting(setting);
		}
	    });
	}

	//
	// set the rendering extension
	//
	setExtension(new SourcePrioritySettingComponentInfo());
    }

    /**
     * @param object
     */
    public SourcePrioritySetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public SourcePrioritySetting(String object) {

	super(object);
    }

    /**
     * @author Fabrizio
     */
    public static class SourcePrioritySettingComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public SourcePrioritySettingComponentInfo() {

	    setComponentName(SourcePrioritySettingComponentInfo.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(TabIndex.SOURCE_PRIORITY_SETTING.getIndex()).//
		    withShowDirective("Priority sources").//
		    build();

	    setTabInfo(tabInfo);
	}
    }

    /**
     * @param sourceIdentifier
     */
    public boolean deselectSource(String sourceIdentifier) {

	Optional<Setting> optSetting = getSetting(SOURCES_SETTING_IDENTIFIER).//
		get().//
		getSettings().//
		stream().//
		filter(s -> s.isSelected() && s.getIdentifier().equals(sourceIdentifier)).//
		findFirst();

	if (optSetting.isPresent()) {

	    return getSetting(SOURCES_SETTING_IDENTIFIER).//
		    get().removeSetting(optSetting.get());
	}

	return false;
    }

    /**
     * @param sourceIdentifiers
     */
    public void selectPrioritySources(List<String> sourceIdentifiers) {

	getSetting(SOURCES_SETTING_IDENTIFIER).get().select(s -> sourceIdentifiers.contains(s.getIdentifier()));
    }

    /**
     * @param source
     * @return
     */
    public boolean isPrioritySource(GSSource source) {

	String sourceId = source.getUniqueIdentifier();

	return getSelectedSourcesIds().contains(sourceId);
    }

    /**
     * @return
     */
    public List<String> getSelectedSourcesIds() {

	return getSetting(SOURCES_SETTING_IDENTIFIER).//
		get().//
		getSettings().//
		stream().//
		filter(s -> s.isSelected()).//
		map(s -> s.getIdentifier()).//
		collect(Collectors.toList());
    }
}
