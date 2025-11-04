/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.augmenter.worker;

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

import java.util.*;
import java.util.stream.Collectors;

import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import com.vaadin.flow.data.provider.SortDirection;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.SchedulerSupport;
import eu.essi_lab.cfga.gs.setting.menuitems.HarvestingInfoItemHandler;
import eu.essi_lab.cfga.gui.components.grid.ColumnDescriptor;
import eu.essi_lab.cfga.gui.components.grid.GridMenuItemHandler;
import eu.essi_lab.cfga.gui.components.grid.renderer.JobPhaseColumnRenderer;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.option.ValuesLoader;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public abstract class AugmenterWorkerSetting extends SchedulerWorkerSetting implements EditableSetting {

    /**
     * 
     */
    private static final String NAME_OPTION_KEY = "nameOption";

    /**
     * 
     */
    private static final String MAX_RECORDS_KEY = "maxRecords";
    /**
     * 
     */
    private static final String LESS_RECENT_ORDERING_KEY = "lessRecentOrdering";
    /**
     * 
     */
    private static final String TIME_BACK_OPTION_KEY = "timeBackOption";
    /**
     * 
     */
    private static final String SOURCES_OPTION_KEY = "sourcesOption";

    /**
     * 
     */
    private static final String VIEW_OPTION_KEY = "viewOption";

    /**
     * 
     */
    public AugmenterWorkerSetting() {

	setCanBeRemoved(true);
	setCanBeDisabled(false);
	enableCompactMode(false);

	setName("Augmenters job settings");

	//
	// the class to configure when the job raises
	//
	setConfigurableType(initConfigurableType());

	//
	// the scheduling group
	//
	setGroup(SchedulingGroup.AUGMENTING);

	//
	// Scheduling is disabled and set to run once
	//
	getScheduling().setRunOnce();
	getScheduling().setEnabled(false);

	//
	// Options
	//

	Option<String> nameOption = StringOptionBuilder.get().//
		withKey(NAME_OPTION_KEY).//
		withLabel("A name for this augmentation job").//
		withValue("Default augmentation job").//
		cannotBeDisabled().//
		required().//
		build();

	addOption(nameOption);

	Option<String> viewOption = StringOptionBuilder.get().//
		withKey(VIEW_OPTION_KEY).//
		withLabel("Identifier of an existing view").//
		withSingleSelection().//
		withValuesLoader(getViewIdentifiersLoader()).//
		cannotBeDisabled().//
		build();

	addOption(viewOption);

	Option<Integer> maxRecordsOption = IntegerOptionBuilder.get().//
		required().//
		withKey(MAX_RECORDS_KEY).//
		withLabel("Maximum number of records to augment (0 = no limitation)").//
		withValue(0).//
		withMinValue(0).//
		cannotBeDisabled().//
		build();

	addOption(maxRecordsOption);

	Option<BooleanChoice> mostRecentOption = BooleanChoiceOptionBuilder.get().//
		required().//
		withKey(LESS_RECENT_ORDERING_KEY).//
		withLabel("Elaborates before the less recent (according to the resource time stamp) records").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(mostRecentOption);

	Option<Integer> timeBackOption = IntegerOptionBuilder.get().//
		required().//
		withKey(TIME_BACK_OPTION_KEY).//
		withLabel("Elaborates only records created since the given amount of minutes. 0 to elaborates all records").//
		withValue(0).//
		withMinValue(0).//
		cannotBeDisabled().//
		build();

	addOption(timeBackOption);

	Option<String> sourcesOption = StringOptionBuilder.get().//
		withKey(SOURCES_OPTION_KEY).//
		withLabel("Sources to augment").//
		withDescription("Leave unset to augment all the available sources").//
		withMultiSelection().//
		withValuesLoader(new SourcesLoader()).//
		withTextArea().//
		cannotBeDisabled().//
		build();

	addOption(sourcesOption);

	//
	// Augmenter settings
	//

	Setting augmentersSetting = initAugmentersSetting();

	addSetting(augmentersSetting);

	//
	// set the component extension
	//
	setExtension(new AugmenterWorkerComponentInfo());

	//
	// set the validator
	//
	setValidator(new AugmenterWorkerSettingValidator());
    }

    /**
     * @return
     */
    protected abstract ValuesLoader<String> getViewIdentifiersLoader();

    /**
     * @author Fabrizio
     */
    public static class SourcesLoader extends ValuesLoader<String> {

	@Override
	protected List<String> loadValues(Optional<String> input) {

	    return ConfigurationWrapper.getHarvestedAndMixedSources().stream().map(GSSource::getLabel).collect(Collectors.toList());
	}
    }

    /**
     * @author Fabrizio
     */
    public static class AugmenterWorkerComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public AugmenterWorkerComponentInfo() {

	    setComponentName(AugmenterWorkerSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(GSTabIndex.AUGMENTERS.getIndex()).//
		    withShowDirective("Augmenters", SortDirection.ASCENDING).//

		    withAddDirective(//
			    "Add augmentation job", //
			    "eu.essi_lab.augmenter.worker.AugmenterWorkerSettingImpl")
		    .//
		    withRemoveDirective("Remove augmenter", false, "eu.essi_lab.augmenter.worker.AugmenterWorkerSettingImpl").//
		    withEditDirective("Edit augmenter", ConfirmationPolicy.ON_WARNINGS).//

		    withGridInfo(Arrays.asList(//

			    ColumnDescriptor.create("Name", true, true, this::getName), //

			    ColumnDescriptor.create("Repeat count", 150, true, true,
				    (s) -> SchedulerSupport.getInstance().getRepeatCount(s)), //

			    ColumnDescriptor.create("Repeat interval", 150, true, true,
				    (s) -> SchedulerSupport.getInstance().getRepeatInterval(s)), //

			    ColumnDescriptor.create("Status", 100, true, true, (s) -> SchedulerSupport.getInstance().getJobPhase(s), //

				    Comparator.comparing(item -> item.get("Status")), //

				    new JobPhaseColumnRenderer()), //

			    ColumnDescriptor.create("Fired time", 150, true, true, (s) -> SchedulerSupport.getInstance().getFiredTime(s)), //

			    ColumnDescriptor.create("End time", 150, true, true, (s) -> SchedulerSupport.getInstance().getEndTime(s)), //

			    ColumnDescriptor.create("El. time (HH:mm:ss)", 170, true, true,
				    (s) -> SchedulerSupport.getInstance().getElapsedTime(s)), //

			    ColumnDescriptor.create("Next fire time", true, true, (s) -> SchedulerSupport.getInstance().getNextFireTime(s)), //

			    ColumnDescriptor.create("Info", true, true, false, (s) -> SchedulerSupport.getInstance().getAllMessages(s))//
		    ), getItemsList()).//

		    reloadable(() -> SchedulerSupport.getInstance().update()).//

		    build();

	    setTabInfo(tabInfo);
	}

	/**
	 * @return
	 */
	private List<GridMenuItemHandler> getItemsList() {

	    ArrayList<GridMenuItemHandler> list = new ArrayList<>();
	    list.add(new HarvestingInfoItemHandler());

	    return list;
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getName(Setting setting) {

	    return setting.getObject().getJSONObject("nameOption").getJSONArray("values").getString(0);
	}
    }

    /**
     * @author Fabrizio
     */
    public static class AugmenterWorkerSettingValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    AugmenterWorkerSetting thisSetting = (AugmenterWorkerSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	    ValidationResponse validationResponse = new ValidationResponse();

	    //
	    //
	    //

	    String name = thisSetting.getName();

	    if (name == null || name.isEmpty()) {

		validationResponse.getErrors().add("A name must be provided");
		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
	    }

	    //
	    //
	    //

	    List<AugmenterSetting> selectedAugmenterSettings = thisSetting.getSelectedAugmenterSettings();
	    if (selectedAugmenterSettings.isEmpty()) {

		validationResponse.getErrors().add("At least one augmenter must be selected");
		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
	    }

	    //
	    //
	    //

	    Scheduling scheduling = thisSetting.getScheduling();

	    DateTimeZone userDateTimeZone = ConfigurationWrapper.getSchedulerSetting().getUserDateTimeZone();

	    Scheduling.validate(scheduling, userDateTimeZone, validationResponse);

	    return validationResponse;
	}
    }

    /**
     * @return
     */
    protected abstract String initConfigurableType();

    /**
     * @param object
     */
    public AugmenterWorkerSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public AugmenterWorkerSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public Setting getAugmentersSetting() {

	return getSetting(getAugmentersSettingIdentifier()).get();
    }

    /**
     * @return
     */
    public List<AugmenterSetting> getSelectedAugmenterSettings() {

	return getAugmentersSetting().//
		getSettings(AugmenterSetting.class, false).//
		stream().//
		filter(Setting::isSelected).//
		collect(Collectors.toList());
    }

    /**
     * 
     */
    protected abstract Setting initAugmentersSetting();

    /**
     * 
     */
    protected abstract String getAugmentersSettingIdentifier();

    /**
     * @return
     */
    public void setViewIdentifier(String viewId) {

	getOption(VIEW_OPTION_KEY, String.class).get().setValue(viewId);
    }

    /**
     * @return
     */
    public Optional<String> getViewIdentifier() {

	return getOption(VIEW_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param maxRecords
     */
    public void setMaxRecords(int maxRecords) {

	getOption(MAX_RECORDS_KEY, Integer.class).get().setValue(maxRecords);
    }

    /**
     * @return
     */
    public int getMaxRecords() {

	return getOption(MAX_RECORDS_KEY, Integer.class).get().getValue();
    }

    /**
     * @param maxRecords
     */
    public void setAugmentationJobName(String name) {

	getOption(NAME_OPTION_KEY, String.class).get().setValue(name);
    }

    /**
     * @return
     */
    public String getAugmentationJobName() {

	return getOption(NAME_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param maxAge
     */
    public void setMaxAge(int maxAge) {

	getOption(TIME_BACK_OPTION_KEY, Integer.class).get().setValue(maxAge);
    }

    /**
     * @return
     */
    public int getMaxAge() {

	return getOption(TIME_BACK_OPTION_KEY, Integer.class).get().getValue();
    }

    /**
     * @param set
     */
    public void setLessRecentSort(boolean set) {

	getOption(LESS_RECENT_ORDERING_KEY, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(set));
    }

    /**
     * @return
     */
    public Boolean isLessRecentSortSet() {

	return BooleanChoice.toBoolean(getOption(LESS_RECENT_ORDERING_KEY, BooleanChoice.class).get().getSelectedValue());
    }

    /**
     * @param sourcesLabels
     */
    public void setSelectedSources(List<String> sourcesLabels) {

	getOption(SOURCES_OPTION_KEY, String.class).get().select(sourcesLabels::contains);
    }

    /**
     * @return
     */
    public List<String> getSelectedSourcesIds() {

	List<String> selectedValues = getOption(SOURCES_OPTION_KEY, String.class).//
		get().//
		getSelectedValues();

	if (selectedValues.isEmpty()) {

	    return ConfigurationWrapper.//
		    getHarvestedAndMixedSources().//
		    stream().//
		    map(GSSource::getUniqueIdentifier).//
		    collect(Collectors.toList());
	}

	return selectedValues.//
		stream().//
		map(l -> getSourceIdentifierFromLabel(l)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public List<GSSource> getSelectedSources() {

	return ConfigurationWrapper.getHarvestedAndMixedSources().//
		stream().//
		filter(s -> getSelectedSourcesIds().contains(s.getUniqueIdentifier())).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public Bond getSourcesBond() {

	List<ResourcePropertyBond> list = getSelectedSourcesIds().//
		stream().//
		map(BondFactory::createSourceIdentifierBond).//
		toList();

	Bond sourcesBond;

	if (list.size() == 1) {

	    sourcesBond = list.getFirst();

	} else {

	    LogicalBond orBond = BondFactory.createOrBond();
	    list.forEach(b -> orBond.getOperands().add(b));

	    sourcesBond = orBond;
	}

	return sourcesBond;
    }

    /**
     * @return
     */
    public String getWorkerName() {

	return getAugmentationJobName();
    }

    /**
     * @param label
     * @return
     */
    private String getSourceIdentifierFromLabel(String label) {

	try {
	    List<GSSource> sources = ConfigurationWrapper.getHarvestedAndMixedSources();

	    return sources.//
		    stream().//
		    filter(s -> s.getLabel().equals(label)).//
		    map(GSSource::getUniqueIdentifier).//
		    findFirst().//
		    get();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Source with label {} not found", label);
	}

	return null;
    }

}
