/**
 * 
 */
package eu.essi_lab.cfga.gs;

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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.checker.CheckMethod;
import eu.essi_lab.cfga.checker.CheckResponse;
import eu.essi_lab.cfga.checker.CheckResponse.CheckResult;
import eu.essi_lab.cfga.gs.setting.GDCSourcesSetting;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSettingLoader;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Property;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * This {@link CheckMethod} applies the {@link Setting#similar(Setting, List)} method to all 
 * the {@link Configuration} 
 * settings comparing them with a brand new instance of a setting having the same {@link Setting#getSettingClass()}.<br> 
 * The new setting is deeply cleaned using the {@link SelectionUtils#deepClean(Setting)} before the comparison with 
 * the related configuration setting.<br>
 * All the {@link Setting#getProperties()} are excluded from the similarity check, so the check fails only 
 * in the following four cases:<ol>
 * <li>one or more {@link Option}/s has/have been <b>removed</b> from a setting</li>
 * <li>one or more {@link Option}/s has/have been <b>added</b> to a setting</li>
 * <li>one or more  sub-{@link Setting}/s has/have  been <b>removed</b> from a setting</li>
 * <li>one or more  sub-{@link Setting}/s has/have  been <b>added</b> to a setting</li>
 * </ol>
 * 
 * @author Fabrizio
 */
public class SimilarityCheckMethod implements CheckMethod {

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
    private List<Property<?>> exclusions;

    /**
     * 
     */
    public SimilarityCheckMethod() {

	exclusions = new ArrayList<>(DEFAULT_EXCLUSIONS);
    }

    @Override
    public CheckResponse check(Configuration configuration) {

	GSLoggerFactory.getLogger(getClass()).info("Configuration similarity check STARTED");

	CheckResponse checkResponse = new CheckResponse(getName());

	configuration.list().forEach(configSetting -> {

	    boolean similar = true;
	    Setting newSetting = null;

	    newSetting = SettingUtils.create(configSetting.getSettingClass());

	    if (modifier != null) {
		modifier.accept(newSetting);
	    }

	    if (configSetting.getSettingClass().equals(DistributionSetting.class)) {

		DistributionSetting distSetting = SettingUtils.downCast(configSetting, DistributionSetting.class, true);
		DistributionSetting newDistSetting = SettingUtils.downCast(newSetting, DistributionSetting.class, true);

		String identifier = distSetting.getSelectedAccessorSetting().getIdentifier();
		newDistSetting.getAccessorsSetting().select(s -> s.getIdentifier().equals(identifier));

		newSetting = newDistSetting;

	    } else if (configSetting.getSettingClass().equals(HarvestingSettingLoader.load().getClass())) {

		HarvestingSetting harvSetting = SettingUtils.downCast(configSetting, HarvestingSettingLoader.load().getClass(), true);
		HarvestingSetting newHarvSetting = SettingUtils.downCast(newSetting, HarvestingSettingLoader.load().getClass(), true);

		String selHarvSettingId = harvSetting.getSelectedAccessorSetting().getIdentifier();
		newHarvSetting.getAccessorsSetting().select(s -> s.getIdentifier().equals(selHarvSettingId));

		harvSetting.getSelectedAugmenterSettings().//
			stream().//
			map(s -> s.getIdentifier()).//
			forEach(id -> newHarvSetting.getAugmentersSetting().select(s -> s.getIdentifier().equals(id)));

		List<String> selAugIds = harvSetting.getSelectedAugmenterSettings().//
			stream().//
			map(s -> s.getIdentifier()).//
			collect(Collectors.toList());

		newHarvSetting.getAugmentersSetting().select(s -> selAugIds.contains(s.getIdentifier()));

		newSetting = newHarvSetting;

	    } else if (configSetting.getSettingClass().equals(AugmenterWorkerSettingLoader.load().getClass())) {

		AugmenterWorkerSetting augSetting = SettingUtils.downCast(configSetting, AugmenterWorkerSettingLoader.load().getClass(), true);
		AugmenterWorkerSetting newAugSetting = SettingUtils.downCast(newSetting, AugmenterWorkerSettingLoader.load().getClass(),
			true);

		List<String> selAugIds = augSetting.getSelectedAugmenterSettings().//
			stream().//
			map(s -> s.getIdentifier()).//
			collect(Collectors.toList());

		newAugSetting.getAugmentersSetting().select(s -> selAugIds.contains(s.getIdentifier()));

		newSetting = newAugSetting;

	    } else if (configSetting.getSettingClass().equals(GDCSourcesSetting.class)) {

		GDCSourcesSetting gdcSourceSetting = SettingUtils.downCast(configSetting, GDCSourcesSetting.class, true);
		GDCSourcesSetting newGdcSourceSetting = SettingUtils.downCast(newSetting, GDCSourcesSetting.class, true);

		newGdcSourceSetting.selectGDCSources(gdcSourceSetting.getSelectedSourcesIds());

		newSetting = newGdcSourceSetting;

	    } else if (configSetting.getSettingClass().equals(SourcePrioritySetting.class)) {

		SourcePrioritySetting sourcePrioritySetting = SettingUtils.downCast(configSetting, SourcePrioritySetting.class, true);
		SourcePrioritySetting newSourcePrioritySetting = SettingUtils.downCast(newSetting, SourcePrioritySetting.class, true);

		newSourcePrioritySetting.selectPrioritySources(sourcePrioritySetting.getSelectedSourcesIds());

		newSetting = newSourcePrioritySetting;
	    }

	    SelectionUtils.deepClean(newSetting);

	    similar = newSetting.similar(//
		    configSetting, //
		    getExclusions().stream().map(p -> p.getKey()).collect(Collectors.toList()));

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
