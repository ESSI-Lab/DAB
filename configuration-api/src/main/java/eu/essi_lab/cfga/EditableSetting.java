package eu.essi_lab.cfga;

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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * A marker interface for {@link Setting}s that can be edited, for example through a GUI with an "Edit" button.<br>
 * Editing a {@link Setting} consists in the following 3 steps:
 * <ol>
 * <li>clonation of the setting to edit</li>
 * <li>reset of the cloned setting using the {@link Setting#reset()} method</li>
 * <li>gather all the selections and properties (for example the enabled state) of the original setting (the setting to
 * edit)</li>
 * <li>apply all these selections and properties to the cloned reset setting</li>
 * </ol>
 * At the end of this algorithm, the resulting setting will have all the settings and options values of a brand
 * new instance of the setting to edit, but it will also have all its current selections and properties.<br>
 * Note that if the setting to edit has been cleared using the {@link Selectable#clean()} method, all the not selected
 * settings
 * and not selected options values removed, will be reset to their original state according to the no-args constructor
 * (see
 * {@link Setting#reset()} ).<br>
 * The above algorithm works only if the setting to edit is valid according to the {@link #isEditable(Setting)}
 * method.<br>
 * This algorithm is implemented by the {@link SelectionUtils#resetAndSelect(Setting)} method and it works only if
 * the setting to edit is valid according to the {@link #isEditable(Setting)} method
 * 
 * @author Fabrizio
 */
public interface EditableSetting {

    /**
     * The given <code>targetSetting</code> is editable if:
     * <ol>
     * <li>all its children have
     * <i>static</i> and <i>distinct</i> identifiers (<i>not randomly created</i> and <i>all different</i>)</li>
     * <li>all the options of the children settings within the same settings, must have <i>static</i> and
     * <i>distinct</i> keys (<i>not randomly created</i> and <i>all different</i>)</li>
     * <li>the reset setting <i>recursively</i> has the same options and settings of <code>targetSetting</code>, or it
     * has more
     * options and/or setting than <code>targetSetting</code>. Otherwise, if the reset setting misses one or more
     * options and/or settings, this would prevent to select
     * the missing objects from <code>targetSetting</code> to the reset setting</li>
     * </ol>
     * If all these conditions are respected, when <code>targetSetting</code> is reset, it is possible to correctly
     * apply the
     * {@link SelectionUtils#resetAndSelect(Setting)} method
     * 
     * @param targetSetting
     * @return
     */
    public static boolean isEditable(Setting targetSetting) {

	return isEditable(targetSetting, true);
    }

    /**
     * The given <code>targetSetting</code> is editable if:
     * <ol>
     * <li>all its children have
     * <i>static</i> and <i>distinct</i> identifiers (<i>not randomly created</i> and <i>all different</i>)</li>
     * <li>all the options of the children settings within the same settings, must have <i>static</i> and
     * <i>distinct</i> keys (<i>not randomly created</i> and <i>all different</i>)</li>
     * <li>the reset setting <i>recursively</i> has the same options and settings of <code>targetSetting</code>, or it
     * has more
     * options and/or setting than <code>targetSetting</code>. Otherwise, if the reset setting misses one or more
     * options and/or settings, this would prevent to select
     * the missing objects from <code>targetSetting</code> to the reset setting</li>
     * </ol>
     * If all these conditions are respected, when <code>targetSetting</code> is reset, it is possible to correctly
     * apply the
     * {@link SelectionUtils#resetAndSelect(Setting)} method
     * 
     * @param targetSetting
     * @param log
     * @return
     */
    public static boolean isEditable(Setting targetSetting, boolean log) {

	// makes the test on the clone
	Setting clonedSetting = targetSetting.clone();

	//
	// 1) All the setting identifiers must be distinct
	//

	ArrayList<String> targetSettingIds = new ArrayList<>();

	SettingUtils.deepPerform(clonedSetting, s -> targetSettingIds.add(s.getIdentifier()));

	int size = targetSettingIds.size();

	long distinct = targetSettingIds.stream().distinct().count();

	boolean distinctIds = (size == distinct);

	if (!distinctIds) {

	    if (log) {
		GSLoggerFactory.getLogger(EditableSetting.class).error("Distinct ids test failed: distinct/total: {}/{}", distinct, size);
	    }

	    // if this test failed, the next test which assumes distinct setting ids cannot be executed
	    return false;
	}

	//
	// 2) All the options keys within the same child setting, must be distinct
	//

	HashMap<String, List<String>> settingIdToOptionsKeyMap = new HashMap<>();

	SettingUtils.deepPerform(clonedSetting, setting -> {

	    List<String> keys = setting.getOptions().stream().map(Option::getKey).collect(Collectors.toList());

	    settingIdToOptionsKeyMap.put(setting.getIdentifier(), keys);
	});

	boolean distinctOptIds = true;

	Set<String> keySet = settingIdToOptionsKeyMap.keySet();
	for (String settingId : keySet) {

	    List<String> optionsKeys = settingIdToOptionsKeyMap.get(settingId);

	    int optIdsCount = optionsKeys.size();

	    long distinctOptIdsCount = optionsKeys.stream().distinct().count();

	    distinctOptIds &= (optIdsCount == distinctOptIdsCount);

	    if (!distinctOptIds && log) {

		GSLoggerFactory.getLogger(EditableSetting.class).error("Options keys test failed:");
		GSLoggerFactory.getLogger(EditableSetting.class).error("- Setting id: {}", settingId);
		GSLoggerFactory.getLogger(EditableSetting.class).error("- Distinct/total: {}/{}", distinctOptIdsCount, optIdsCount);
	    }
	}

	//
	// 3) Reset test
	//

	//
	//
	// 3.1) The identifiers of the setting before the reset
	// -> must be equals to the reset setting identifiers
	// -> or the reset setting identifiers, must contain all the identifiers of the setting to edit. This is the
	// case where the setting to edit has been cleaned, so it lost some not selected settings thus it has
	// a number of identifiers less then the reset setting
	//

	// the id of the setting is not considered, only the children are tested
	targetSettingIds.remove(clonedSetting.getIdentifier());
	targetSettingIds.sort(String::compareTo);

	// resets the clone of the setting to edit
	clonedSetting.reset();

	ArrayList<String> resetIds = new ArrayList<>();

	SettingUtils.deepPerform(clonedSetting, s -> resetIds.add(s.getIdentifier()));

	// the id of the setting is not considered, only the children are tested
	resetIds.remove(clonedSetting.getIdentifier());
	resetIds.sort(String::compareTo);

	//
	//
	//

	boolean sameIds = targetSettingIds.equals(resetIds);

	// this happens when the setting to edit is clean and it lost some selected settings
	boolean containsAll = resetIds.containsAll(targetSettingIds);

	if (!sameIds && !containsAll) {

	    if (log) {
		GSLoggerFactory.getLogger(EditableSetting.class).error("Reset test failed:");
		GSLoggerFactory.getLogger(EditableSetting.class).error("- Setting id: {}", targetSetting.getIdentifier());
		GSLoggerFactory.getLogger(EditableSetting.class).error("- Setting name: {}", targetSetting.getName());
	    }

	    targetSettingIds.removeAll(resetIds);

	    if (log) {
		GSLoggerFactory.getLogger(EditableSetting.class)
			.error("- Reset setting misses the follwing settings ids: {}", targetSettingIds);
	    }
	}

	//
	// 3.2) The keys of all the children setting to edit options must be the same of the reset setting
	//

	// get all the key of the setting to edit
	List<String> targetSettingOptionsKeys = new ArrayList<>();

	SettingUtils.deepPerform(targetSetting, //
		setting -> setting.getOptions().forEach(opt -> targetSettingOptionsKeys.add(opt.getKey())));

	targetSettingOptionsKeys.sort(String::compareTo);

	// get all the key of the reset setting to edit
	List<String> resetSettingOptionsKeys = new ArrayList<>();

	SettingUtils.deepPerform(clonedSetting, //
		setting -> setting.getOptions().forEach(opt -> resetSettingOptionsKeys.add(opt.getKey())));

	resetSettingOptionsKeys.sort(String::compareTo);

	boolean sameOptionsKeys = targetSettingOptionsKeys.equals(resetSettingOptionsKeys);

	// this happens when the setting to edit is clean and it lost some selected settings with their options
	boolean containsOptionsKeys = resetSettingOptionsKeys.containsAll(targetSettingOptionsKeys);

	if (!sameOptionsKeys && !containsOptionsKeys) {

	    if (log) {
		GSLoggerFactory.getLogger(EditableSetting.class).error("Reset test failed:");
		GSLoggerFactory.getLogger(EditableSetting.class).error("- Setting id: {}", targetSetting.getIdentifier());
		GSLoggerFactory.getLogger(EditableSetting.class).error("- Setting name: {}", targetSetting.getName());
	    }

	    targetSettingOptionsKeys.removeAll(resetSettingOptionsKeys);

	    if (log) {
		GSLoggerFactory.getLogger(EditableSetting.class)
			.error("- Reset setting misses the following options keys: {}", targetSettingOptionsKeys);
	    }
	}

	//
	//
	//

	//
	//
	//
	return distinctOptIds && (sameIds || containsAll) && (sameOptionsKeys || containsOptionsKeys);
    }

    /**
     * Executes the {@link #isEditable(Setting)} test twice, the second time after a
     * deep clean (see {@link SelectionUtils#deepClean(Setting)}.<br>
     * Returns <code>true</code> if both tests succeeds
     * 
     * @param settingToEdit
     */
    public static boolean test(Setting settingToEdit) {

	boolean valid = EditableSetting.isEditable(settingToEdit);

	//
	//
	//

	Setting clone = settingToEdit.clone();

	SelectionUtils.deepClean(clone);

	valid &= EditableSetting.isEditable(clone, false);

	return valid;
    }
}
