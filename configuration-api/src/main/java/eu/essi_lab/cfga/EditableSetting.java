package eu.essi_lab.cfga;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * A marker interface for {@link Setting}s that can be edit by the client clicking on the "EDIT" button.<br>
 * Editing a {@link Setting} consists in the following 3 steps:
 * <ol>
 * <li>clone the setting to edit</li>
 * <li>reset the cloned setting using the {@link Setting#reset()} method</li>
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
     * The given <code>setting</code> is editable if:
     * <ol>
     * <li>all its children have
     * <i>static</i> and <i>distinct</i> identifiers (<i>not randomly created</i> and <i>all different</i>)</li>
     * <li>all the options of the children settings within the same settings, must have <i>static</i> and
     * <i>distinct</i> keys (<i>not randomly created</i> and <i>all different</i>)</li>
     * </ol>
     * This way, when the setting is reset, it is possible to correctly apply the
     * {@link SelectionUtils#resetAndSelect(Setting)} method
     * 
     * @param settingToEdit
     * @return
     */
    public static boolean isEditable(Setting settingToEdit) {

	// makes the test on the clone
	Setting clonedSetting = settingToEdit.clone();

	//
	// 1) All the setting identifiers must be distinct
	//

	ArrayList<String> settingToEditIds = new ArrayList<>();

	SettingUtils.deepPerform(clonedSetting, s -> settingToEditIds.add(s.getIdentifier()));

	int size = settingToEditIds.size();

	long distinct = settingToEditIds.stream().distinct().count();

	boolean distinctIds = (size == distinct);

	if (!distinctIds) {

	    GSLoggerFactory.getLogger(EditableSetting.class).error("Distinct ids test failed: distinct/total: " + distinct + "/" + size);

	    // if this test failed, the next test which assumes distinct setting ids cannot be executed
	    return false;
	}

	//
	// 2) All the options keys within the same child setting, must be distinct
	//

	HashMap<String, List<String>> settingIdToOptionsKeyMap = new HashMap<>();

	SettingUtils.deepPerform(clonedSetting, setting -> {

	    List<String> keys = setting.getOptions().stream().map(o -> o.getKey()).collect(Collectors.toList());

	    settingIdToOptionsKeyMap.put(setting.getIdentifier(), keys);
	});

	boolean distinctOptIds = true;

	Set<String> keySet = settingIdToOptionsKeyMap.keySet();
	for (String settingId : keySet) {

	    List<String> optionsKeys = settingIdToOptionsKeyMap.get(settingId);

	    int optIdsCount = optionsKeys.size();

	    long distinctOptIdsCount = optionsKeys.stream().distinct().count();

	    distinctOptIds &= (optIdsCount == distinctOptIdsCount);

	    if (!distinctOptIds) {

		GSLoggerFactory.getLogger(EditableSetting.class).error("Options keys test failed:");
		GSLoggerFactory.getLogger(EditableSetting.class).error("- setting id: " + settingId);
		GSLoggerFactory.getLogger(EditableSetting.class).error("- distinct/total: " + distinctOptIdsCount + "/" + optIdsCount);
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
	settingToEditIds.remove(clonedSetting.getIdentifier());
	settingToEditIds.sort(String::compareTo);

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

	boolean sameIds = settingToEditIds.equals(resetIds);

	// this happens when the setting to edit is clean and it lost some selected settings
	boolean containsAll = resetIds.containsAll(settingToEditIds);

	if (!sameIds && !containsAll) {

	    GSLoggerFactory.getLogger(EditableSetting.class).error("Reset test failed:");
	    GSLoggerFactory.getLogger(EditableSetting.class).error("- setting id: " + settingToEdit.getIdentifier());
	    GSLoggerFactory.getLogger(EditableSetting.class).error("- setting ids different from reset setting ids");
	    GSLoggerFactory.getLogger(EditableSetting.class).error("- reset ids do not contain setting ids");
	}

	//
	// 3.2) The keys of all the children setting to edit options must be the same of the reset setting
	//

	// get all the key of the setting to edit
	List<String> settingToEditOptionsKeys = new ArrayList<>();

	SettingUtils.deepPerform(settingToEdit, //
		setting -> setting.getOptions().forEach(opt -> settingToEditOptionsKeys.add(opt.getKey())));

	settingToEditOptionsKeys.sort(String::compareTo);

	// get all the key of the reset setting to edit
	List<String> resetSettingOptionsKeys = new ArrayList<>();

	SettingUtils.deepPerform(clonedSetting, //
		setting -> setting.getOptions().forEach(opt -> resetSettingOptionsKeys.add(opt.getKey())));

	resetSettingOptionsKeys.sort(String::compareTo);

	boolean sameOptionsKeys = settingToEditOptionsKeys.equals(resetSettingOptionsKeys);

	// this happens when the setting to edit is clean and it lost some selected settings with their options
	boolean containsOptionsKeys = resetSettingOptionsKeys.containsAll(settingToEditOptionsKeys);

	if (!sameOptionsKeys && !containsOptionsKeys) {

	    GSLoggerFactory.getLogger(EditableSetting.class).error("Reset test failed:");
	    GSLoggerFactory.getLogger(EditableSetting.class).error("- setting id: " + settingToEdit.getIdentifier());
	    GSLoggerFactory.getLogger(EditableSetting.class).error("- reset options keys different from setting ids");
	    GSLoggerFactory.getLogger(EditableSetting.class).error("- reset options keys do not contain setting ids");
	}

	//
	//
	//

	return distinctIds && //
		distinctOptIds && //
		(sameIds || containsAll) && //
		(sameOptionsKeys || containsOptionsKeys);
    }

    /**
     * To use as JUnit test. Executes the {@link #isEditable(Setting)} test twice, the second time after a
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

	valid &= EditableSetting.isEditable(clone);

	return valid;
    }
}
