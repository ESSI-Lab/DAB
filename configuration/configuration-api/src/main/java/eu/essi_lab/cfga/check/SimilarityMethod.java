/**
 * 
 */
package eu.essi_lab.cfga.check;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.function.Consumer;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Property;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * This {@link CheckMethod} applies the {@link Setting#similar(Setting, List)} method to all
 * the {@link Configuration}
 * settings comparing them with a brand new instance of a setting having the same {@link Setting#getSettingClass()}.<br>
 * The new setting is deeply selected (*) with the {@link SelectionUtils#deepSelect(Setting, Setting)} method and than
 * it is
 * deeply
 * cleaned using the {@link SelectionUtils#deepClean(Setting)} before the comparison with
 * the related configuration setting.<br>
 * All the {@link Setting#getProperties()} are excluded from the similarity check, so the check fails only
 * in the following four cases:
 * <ol>
 * <li>one or more {@link Option}/s has/have been <b>removed</b> from a setting</li>
 * <li>one or more {@link Option}/s has/have been <b>added</b> to a setting</li>
 * <li>one or more sub-{@link Setting}/s has/have been <b>removed</b> from a setting</li>
 * <li>one or more sub-{@link Setting}/s has/have been <b>added</b> to a setting</li>
 * </ol>
 * <br>
 * <b>(*)</b>: Unless the config setting, the new related setting <i>is not clean</i> and its selectable settings (only
 * the ones
 * that can be cleaned, since the settings that cannot be cleaned maintain all the sub-settings) must have the
 * same selected settings of the config setting, before to be cleaned and compared with the
 * {@link Setting#similar(Setting)} method
 * 
 * @author Fabrizio
 */
public class SimilarityMethod implements CheckMethod {

    /**
     * 
     */
    private static final List<Property<?>> DEFAULT_EXCLUSIONS = Arrays.asList(//

	    // Setting.IDENTIFIER, //
	    // Setting.OBJECT_TYPE, //
	    // Setting.SETTING_CLASS, //

	    Setting.AFTER_CLEAN_FUNCTION, //

	    Setting.CAN_BE_CLEANED, //
	    Setting.CAN_BE_DISABLED, //
	    Setting.CAN_BE_REMOVED, //

	    Setting.COMPACT_MODE, //

	    Setting.CONFIGURABLE_TYPE, //

	    Setting.DESCRIPTION, //
	    Setting.EDITABLE, //
	    Setting.ENABLED, //

	    Setting.EXTENSION, //
	    Setting.FOLDED_MODE, //

	    Setting.NAME, //
	    Setting.SELECTED, //

	    Setting.SELECTION_MODE, //

	    Setting.SHOW_HEADER, //

	    Setting.VALIDATOR, //
	    Setting.VISIBLE//
    );

    /**
     * 
     */
    private Consumer<Setting> modifier;

    /**
     * 
     */
    private final List<Property<?>> exclusions;

    /**
     * 
     */
    public SimilarityMethod() {

	exclusions = new ArrayList<>(DEFAULT_EXCLUSIONS);
    }

    @Override
    public CheckResponse check(Configuration configuration) {

	GSLoggerFactory.getLogger(getClass()).info("Configuration similarity check STARTED");

	CheckResponse checkResponse = new CheckResponse(getName());

	configuration.list().forEach(configSetting -> {

	    boolean similar = true;
	    Setting newSetting = SettingUtils.create(configSetting.getSettingClass());

	    if (modifier != null) {
		modifier.accept(newSetting);
	    }

	    SelectionUtils.deepSelect(configSetting, newSetting);

	    SelectionUtils.deepClean(newSetting);

	    SelectionUtils.deepAfterClean(newSetting);

	    similar = newSetting.similar(//
		    configSetting, //
		    getExclusions().stream().map(Property::getKey).toList());

	    if (!similar) {

		checkResponse.getMessages().add("Similarity check failed for setting class: " + newSetting.getSettingClass());
		checkResponse.setCheckResult(CheckResult.CHECK_FAILED);
		checkResponse.getSettings().add(configSetting);
	    }
	});

	GSLoggerFactory.getLogger(getClass()).info("Configuration similarity check ENDED");

	return checkResponse;
    }

    /**
     * To use for test purpose
     * 
     * @param modifier
     */
    public void setModifier(Consumer<Setting> modifier) {

	this.modifier = modifier;
    }

    /**
     * @return
     */
    public List<Property<?>> getExclusions() {

	return exclusions;
    }
}
