package eu.essi_lab.cfga.gs.setting;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.gui.extension.*;
import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.GSTabIndex;

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

import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class SourcePrioritySetting extends Setting implements EditableSetting {

    /**
     *
     */
    private static final String SOURCES_SETTING_IDENTIFIER = "availableSources";
    private static final String MANTAIN_UUID_KEY = "MANTAIN_UUID_KEY";
    private static final String MANTAIN_COLLECTIONID_KEY = "MANTAIN_COLLECTIONID_KEY";
    private static final String PRESERVE_ID_KEY = "PRESERVE_ID_KEY";
    private static final String NULL_ORIGINAL_ID_KEY = "NULL_ORIGINAL_ID_KEY";

    /**
     *
     */
    public SourcePrioritySetting() {

	setName("Identifier Management");
	setDescription(
		"Define global (DAB-level) Identifier Management options and optionally select one or more sources to prioritize. Collections and datasets of 'priority sources' maintain their original id during harvesting");
	setCanBeDisabled(false);
	enableCompactMode(false);

	//
	// setting with available sources
	//
	Setting sourcesSetting = new Setting();
	sourcesSetting.setName("Select one or more 'priority source'");
	sourcesSetting.setCanBeDisabled(false);
	sourcesSetting.setEditable(false);
	sourcesSetting.setShowHeader(true);
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

	Option<BooleanChoice> mantainuuid = BooleanChoiceOptionBuilder.get().//
		withKey(MANTAIN_UUID_KEY).//
		withLabel("Always use original ID when it is a valid UUID").//
		withDescription(
		"Metadata with an original identifier which is a valid UUID will maintain the original id after the " + "mapping").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.TRUE).//
		cannotBeDisabled().//
		build();

	addOption(mantainuuid);

	Option<BooleanChoice> collectionid = BooleanChoiceOptionBuilder.get().//
		withKey(MANTAIN_COLLECTIONID_KEY).//
		withLabel("Always use original ID when the metadata describes a collection").//
		withDescription("Metadata which describe a colleciton will maintain the original id after the " + "mapping").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.TRUE).//
		cannotBeDisabled().//
		build();

	addOption(collectionid);

	Option<BooleanChoice> preserveid = BooleanChoiceOptionBuilder.get().//
		withKey(PRESERVE_ID_KEY).//
		withLabel("Mantain persistent id").//
		withDescription(
		"Metadata with the same original identifier will mantain the same DAB identifier after each " + "re-harvesting").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(preserveid);

	Option<BooleanChoice> null_original = BooleanChoiceOptionBuilder.get().//
		withKey(NULL_ORIGINAL_ID_KEY).//
		withLabel("Allow null original id").//
		withDescription("Metadata with a null original identifier are allowed, DAB will generate a random id which will change "
		+ "after each re-harvesting").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.TRUE).//
		cannotBeDisabled().//
		build();

	addOption(null_original);
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
    public static class SourcePrioritySettingComponentInfo extends TabPlaceholder {

	private final TabDescriptor descriptor;

	/**
	 *
	 */
	public SourcePrioritySettingComponentInfo() {

	    setLabel("Identifier management");

	    descriptor = TabDescriptorBuilder.get(SourcePrioritySetting.class).//
		    build();
	}

	/**
	 * @return
	 */
	public TabDescriptor getDescriptor() {

	    return descriptor;
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

	return optSetting.filter(setting -> getSetting(SOURCES_SETTING_IDENTIFIER).//
		get().removeSetting(setting)).isPresent();

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
    public Boolean mantainCollectionId() {

	return BooleanChoice.toBoolean(getOption(MANTAIN_COLLECTIONID_KEY, BooleanChoice.class).get().getValue());
    }

    /**
     * @return
     */
    public Boolean mantainUUID() {

	return BooleanChoice.toBoolean(getOption(MANTAIN_UUID_KEY, BooleanChoice.class).get().getValue());
    }

    /**
     * @return
     */
    public Boolean preserveIdentifiers() {

	return BooleanChoice.toBoolean(getOption(PRESERVE_ID_KEY, BooleanChoice.class).get().getValue());
    }

    /**
     * @return
     */
    public Boolean allowNullOriginalId() {

	return BooleanChoice.toBoolean(getOption(NULL_ORIGINAL_ID_KEY, BooleanChoice.class).get().getValue());
    }

    /**
     * @return
     */
    public List<String> getSelectedSourcesIds() {

	return getSetting(SOURCES_SETTING_IDENTIFIER).//
		get().//
		getSettings().//
		stream().//
		filter(Setting::isSelected).//
		map(Setting::getIdentifier).//
		collect(Collectors.toList());
    }
}
