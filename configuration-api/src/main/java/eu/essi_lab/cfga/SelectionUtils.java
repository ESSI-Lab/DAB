package eu.essi_lab.cfga;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;

import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.AfterCleanFunction;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;

/**
 * @author Fabrizio
 */
public class SelectionUtils {

    /**
     * Recursively removes all unselected settings and options values of the given <code>configuration</code>
     * from those settings and options that have a {@link SelectionMode}
     * different from {@link SelectionMode#UNSET}
     * 
     * @param configuration
     */
    public static void deepClean(Configuration configuration) {

	configuration.setWritable(true);

	List<Setting> list = configuration.list();

	list.forEach(l -> deepClean(l));

	configuration.setWritable(false);
    }

    /**
     * Performs recursively after clean operations according to the implementation of the {@link AfterCleanFunction}
     * 
     * @param setting
     */
    public static void deepAfterClean(Configuration configuration) {

	configuration.setWritable(true);

	List<Setting> list = configuration.list();

	list.forEach(l -> deepAfterClean(l));

	configuration.setWritable(false);
    }

    /**
     * Recursively removes all unselected settings (except the {@link SchedulerSetting} which cannot be removed)
     * and options values of the given <code>setting</code>
     * from those settings and options that have a {@link SelectionMode}
     * different from {@link SelectionMode#UNSET}
     * 
     * @param setting
     */
    public static void deepClean(Setting setting) {

	setting.clean();

	setting.getOptions().forEach(o -> o.clean());

	setting.getSettings().forEach(s -> deepClean(s));
    }

    /**
     * Performs recursively after clean operations according to the implementation of the {@link AfterCleanFunction}
     * 
     * @param setting
     */
    public static void deepAfterClean(Setting setting) {

	setting.afterClean();

	setting.getSettings().forEach(s -> deepAfterClean(s));
    }

    /**
     * Creates a new setting which is a <i>reset</i> clone of
     * <code>targetSetting</code>, then recursively applies all the selections (settings and options values) of
     * <code>targetSetting</code>. The resulting setting is not {@link Selectable#clean()} <br>
     * 
     * @param targetSetting
     * @param check
     * @return
     */
    public static Setting resetAndSelect(Setting targetSetting, boolean check) {

	if (check && !EditableSetting.isEditable(targetSetting)) {

	    throw new IllegalArgumentException("Target setting is not valid according to the EditableSetting.isValid test");
	}

	//
	// Step 1: makes a reset clone of the original setting
	//

	Setting resetSetting = targetSetting.clone();
	resetSetting.reset();

	//
	// Step 2:
	//

	applySettingPropertiesAndOptionsState(targetSetting, resetSetting);

	//
	// Step 3: selected settings
	//

	ArrayList<String> selectedSettingsIds = new ArrayList<>();

	findSelectedSettings(targetSetting, selectedSettingsIds);

	setSelectedSettings(resetSetting, selectedSettingsIds);

	//
	// Step 4: option values
	//

	HashMap<String, JSONArray> valuesMap = new HashMap<>();

	findUnsetSelectionModeOptionsValues(targetSetting, resetSetting, valuesMap);

	setUnsetSelectionModeOptionsValues(resetSetting, valuesMap);

	//
	// Step 5: option selection
	//

	HashMap<String, List<Object>> selectedValuesMap = new HashMap<>();

	findSelectedValues(targetSetting, selectedValuesMap);

	setSelectedValues(resetSetting, selectedValuesMap);

	return resetSetting;
    }

    /**
     * {@link Setting} properties can be set outside the default constructor, that is after its initialization, so this
     * method copies all the <code>targetSetting</code> properties to its <code>resetSetting</code>.
     * 
     * @param resetSetting
     * @param targetSetting
     */
    private static void applySettingProperties(Setting targetSetting, Setting resetSetting) {

	//
	// ConfigurationObject properties
	//

	resetSetting.setEnabled(targetSetting.isEnabled());

//	resetSetting.setCanBeDisabled(targetSetting.canBeDisabled());
//
//	resetSetting.setVisible(targetSetting.isVisible());
//
//	resetSetting.setEditable(targetSetting.isEditable());
//
//	targetSetting.getDescription().ifPresent(desc -> resetSetting.setDescription(desc));

	//
	// AbstractSetting properties
	//

//	resetSetting.enableCompactMode(targetSetting.isCompactModeEnabled());
//
//	resetSetting.enableFoldedMode(targetSetting.isFoldedModeEnabled());
//
//	resetSetting.setCanBeRemoved(targetSetting.canBeRemoved());
//
//	resetSetting.setCanBeCleaned(targetSetting.canBeCleaned());
//
//	resetSetting.setShowHeader(targetSetting.isShowHeaderSet());
//
//	targetSetting.getOptionalExtensionClass().ifPresent(clazz -> resetSetting.getObject().put("extensionClass", clazz.getName()));
//
//	targetSetting.getOptionalValidatorClass().ifPresent(clazz -> resetSetting.getObject().put("validatorClass", clazz.getName()));

	//
	// Setting properties
	//

	resetSetting.setIdentifier(targetSetting.getIdentifier());

//	resetSetting.setName(targetSetting.getName());
//
//	targetSetting.getOptionalAfterCleanFunctionClass()
//		.ifPresent(clazz -> resetSetting.getObject().put("afterCleanFunction", clazz.getName()));

//	try {
//	    resetSetting.setConfigurableType(targetSetting.getConfigurableType());
//	} catch (RuntimeException ex) {
//	}
	
	//
	// this option becomes UNSET after a clean, so since target setting is clean and its original mode is SINGLE or
	// MULTI, we would set to the reset setting a wrong value UNSET instead of SINGLE or MULTI.
	//
	// because of this, this options is ignored here and we consider only its value according
	// to the target setting constructor
	//
	// resetSetting.setSelectionMode(targetSetting.getSelectionMode());
	//

    }

    /**
     * @param targetSetting
     * @param resetSetting
     */
    private static void applySettingPropertiesAndOptionsState(Setting targetSetting, Setting resetSetting) {

	// boolean enabled = targetSetting.isEnabled();
	// resetSetting.setEnabled(enabled);

	applySettingProperties(targetSetting, resetSetting);

	targetSetting.getOptions().forEach(targetOption -> {

	    Optional<Option<?>> outOption = resetSetting.//
		    getOptions().//
		    stream().//
		    filter(opt -> opt.getKey().equals(targetOption.getKey())).//
		    findFirst();

	    //
	    // it can be empty in a synch case where the option is missing from the
	    // current setting version
	    //
	    if (outOption.isPresent()) {

		outOption.get().setEnabled(targetOption.isEnabled());
	    }
	});

	targetSetting.getSettings().forEach(originalChild -> {

	    ArrayList<Setting> list = new ArrayList<Setting>();
	    SettingUtils.deepFind(resetSetting, set -> set.getIdentifier().equals(originalChild.getIdentifier()), list);

	    if (!list.isEmpty()) {

		applySettingPropertiesAndOptionsState(originalChild, list.get(0));
	    }
	});
    }

    /**
     * @param originalSetting
     * @param outSetting
     * @param valuesMap
     */
    private static void findUnsetSelectionModeOptionsValues(//
	    Setting originalSetting, //
	    Setting outSetting, //
	    HashMap<String, JSONArray> valuesMap) {

	originalSetting.getOptions().forEach(targetOption -> {

	    ArrayList<Setting> list = new ArrayList<Setting>();
	    SettingUtils.deepFind(outSetting, set -> set.getIdentifier().equals(originalSetting.getIdentifier()), list);

	    if (!list.isEmpty()) {

		Optional<Option<?>> resetOption = list.//
			get(0).//
			getOptions().//
			stream().//
			filter(opt -> opt.getKey().equals(targetOption.getKey())).//
			findFirst();//

		//
		// it can be empty in a synch case where the option is missing from the
		// current setting version
		//
		if (resetOption.isPresent()) {

		    if (resetOption.get().getSelectionMode() == SelectionMode.UNSET && !targetOption.getValues().isEmpty()) {

			valuesMap.put(composeMapKey(originalSetting, targetOption), targetOption.getObject().getJSONArray("values"));
		    }
		}
	    }
	});

	originalSetting.getSettings().forEach(s -> findUnsetSelectionModeOptionsValues(s, outSetting, valuesMap));
    }

    /**
     * @param outSetting
     * @param valuesMap
     */
    private static void setUnsetSelectionModeOptionsValues(Setting outSetting, HashMap<String, JSONArray> valuesMap) {

	outSetting.getOptions().forEach(outOption -> {

	    valuesMap.keySet().forEach(key -> {

		String settingIdentifier = retrieveSettingIdentifier(key);
		String optionKey = retrieveOptionKey(key);

		if (outSetting.getIdentifier().equals(settingIdentifier) && outOption.getKey().equals(optionKey)) {

		    JSONArray jsonArray = valuesMap.get(key);
		    outOption.getObject().put("values", jsonArray);
		}
	    });
	});

	outSetting.getSettings().forEach(s -> setUnsetSelectionModeOptionsValues(s, valuesMap));
    }

    /**
     * @param originalSetting
     * @param selectedValuesMap
     */
    @SuppressWarnings("unchecked")
    private static void findSelectedValues(Setting originalSetting, HashMap<String, List<Object>> selectedValuesMap) {

	originalSetting.getOptions().forEach(targetOption -> {

	    List<Object> selectedValues = (List<Object>) targetOption.getSelectedValues();
	    if (!selectedValues.isEmpty()) {

		selectedValuesMap.put(targetOption.getKey(), selectedValues);
	    }
	});

	originalSetting.getSettings().forEach(s -> findSelectedValues(s, selectedValuesMap));
    }

    /**
     * @param outSetting
     * @param selectedValuesMap
     */
    private static void setSelectedValues(Setting outSetting, HashMap<String, List<Object>> selectedValuesMap) {

	outSetting.getOptions().forEach(outOption -> {

	    if (selectedValuesMap.keySet().contains(outOption.getKey())) {

		List<Object> list = selectedValuesMap.get(outOption.getKey());

		//
		// special case: the option has no values at init time since they are optionally loaded with a loader,
		// so now after the reset, it has no value and the selection would fail. In order to allow the selection
		// to work, the selected values are first added as the option values. At the end, the option will have
		// as values exactly the selected values
		//
		if (outOption.getLoader().isPresent()) {

		    outOption.setObjectValues(list);
		}

		outOption.select(v -> list.contains(v));
	    }
	});

	outSetting.getSettings().forEach(s -> setSelectedValues(s, selectedValuesMap));
    }

    /**
     * @param targetSetting
     * @param identifiers
     */
    private static void findSelectedSettings(Setting targetSetting, List<String> identifiers) {

	if (targetSetting.isSelected()) {

	    identifiers.add(targetSetting.getIdentifier());
	}

	targetSetting.getSettings().forEach(s -> findSelectedSettings(s, identifiers));
    }

    /**
     * @param outSetting
     * @param selectedSettingsIds
     */
    private static void setSelectedSettings(Setting outSetting, ArrayList<String> selectedSettingsIds) {

	boolean isSelected = selectedSettingsIds.contains(outSetting.getIdentifier());

	outSetting.setSelected(isSelected);

	outSetting.getSettings().forEach(s -> setSelectedSettings(s, selectedSettingsIds));
    }

    /**
     * @param owner
     * @param option
     * @return
     */
    private static String composeMapKey(Setting owner, Option<?> option) {

	return owner.getIdentifier() + "_SEP_" + option.getKey();
    }

    /**
     * @param owner
     * @param option
     * @return
     */
    private static String retrieveOptionKey(String keyValue) {

	return keyValue.split("_SEP_")[1];
    }

    /**
     * @param owner
     * @param option
     * @return
     */
    private static String retrieveSettingIdentifier(String keyValue) {

	return keyValue.split("_SEP_")[0];
    }
}
