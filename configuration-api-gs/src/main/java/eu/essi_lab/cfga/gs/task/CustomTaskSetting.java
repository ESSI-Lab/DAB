package eu.essi_lab.cfga.gs.task;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import com.vaadin.flow.data.provider.SortDirection;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.TabIndex;
import eu.essi_lab.cfga.gs.setting.harvesting.SchedulerSupport;
import eu.essi_lab.cfga.gs.setting.menuitems.RowValuesFormatterMenuItem;
import eu.essi_lab.cfga.gui.components.grid.ColumnDescriptor;
import eu.essi_lab.cfga.gui.components.grid.ContextMenuItem;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.option.ValuesLoader;
import eu.essi_lab.cfga.scheduler.Task;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.cfga.setting.validation.Validator;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class CustomTaskSetting extends SchedulerWorkerSetting implements EditableSetting {

    /**
     * 
     */
    private static final String CONFIGURABLE_TYPE = "CustomTaskWorker";

    /**
     * 
     */
    private static final String TASK_NAME_OPTION_KEY = "taskNameOption";

    /**
     * 
     */
    private static final String TASK_DESCRIPTION_OPTION_KEY = "taskDescription";

    /**
     * 
     */
    private static final String TASK_OPTIONS_OPTION_KEY = "taskOptions";

    /**
     * 
     */
    private static final String EMAIL_RECIPIENTS_OPTIONS_OPTION_KEY = "emailRecipients";

    /**
     * 
     */
    public CustomTaskSetting() {

	setCanBeRemoved(true);
	setCanBeDisabled(false);
	enableCompactMode(false);

	setName("Custom task settings");

	setDescription("A customizable, schedulable task which executes the code provided by an implementation of the 'Task' interface");

	//
	// the class to configure when the job raises
	//
	setConfigurableType(CONFIGURABLE_TYPE);

	//
	// the scheduling group
	//
	setGroup(SchedulingGroup.CUSTOM_TASK);

	//
	// Scheduling is disabled and set to run once
	//
	getScheduling().setRunOnce();
	getScheduling().setEnabled(false);

	//
	// Options
	//

	Option<String> taskNameOption = StringOptionBuilder.get().//
		withKey(TASK_NAME_OPTION_KEY).//
		withLabel("Task name").//
		cannotBeDisabled().//
		withSingleSelection().//
		withValuesLoader(new CustomTaskValuesLoader()).//
		withValues(CustomTaskValuesLoader.getValues()).//
		withSelectedValue(DefaultCustomTask.getTaskName()).//
		required().//
		build();

	addOption(taskNameOption);

	Option<String> taskDescriptionOption = StringOptionBuilder.get().//
		withKey(TASK_DESCRIPTION_OPTION_KEY).//
		withLabel("Task description").//
		withValue("No description provided").//
		cannotBeDisabled().//
		required().//
		build();

	addOption(taskDescriptionOption);

	Option<String> taskOptionsOption = StringOptionBuilder.get().//
		withKey(TASK_OPTIONS_OPTION_KEY).//
		withLabel("Custom task options").//
		withTextArea().//
		withDescription("A customizable set of options for the task. The content of these options, the way"
			+ " they are provided and parsed, are completely demanded to the specific task implementation")
		.//
		cannotBeDisabled().//
		build();

	addOption(taskOptionsOption);

	Option<String> emailRecipientsOption = StringOptionBuilder.get().//
		withKey(EMAIL_RECIPIENTS_OPTIONS_OPTION_KEY).//
		withLabel("Comma separated list of email recipients").//
		withTextArea().//
		withDescription("The supplied recipients will be notified with an email when the task is done."
			+ " The system email setting must be configured in order to send the email")
		.//
		cannotBeDisabled().//
		build();

	addOption(emailRecipientsOption);

	//
	// set the component extension
	//
	setExtension(new TaskComponentInfo());

	//
	// set the validator
	//
	setValidator(new TaskSettingValidator());
    }

    /**
     * @author Fabrizio
     */
    public static class CustomTaskValuesLoader extends ValuesLoader<String> {

	public static List<String> getValues() {

	    ServiceLoader<CustomTask> loader = ServiceLoader.load(CustomTask.class);

	    return StreamUtils.iteratorToStream(loader.iterator()).//
		    map(t -> t.getName()).//
		    sorted().//
		    collect(Collectors.toList());
	}

	@Override
	protected List<String> loadValues(Optional<String> input) throws Exception {

	    return getValues();
	}
    }

    /**
     * @author Fabrizio
     */
    public static class TaskComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public TaskComponentInfo() {

	    setComponentName(CustomTaskSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(TabIndex.CUSTOM_TASKS_SETTING.getIndex()).//
		    withShowDirective("Custom tasks", SortDirection.ASCENDING).//

		    withAddDirective(//
			    "Add task", //
			    CustomTaskSetting.class.getCanonicalName())
		    .//
		    withRemoveDirective("Remove task", false, CustomTaskSetting.class.getCanonicalName()).//
		    withEditDirective("Edit task", ConfirmationPolicy.ON_WARNINGS).//

		    withGridInfo(Arrays.asList(//
			    
			    ColumnDescriptor.create("Id", true, true, false, (s) -> s.getIdentifier()),//

			    ColumnDescriptor.create("Name", true, true, (s) -> getName(s)), //

			    ColumnDescriptor.create("Description", true, true, (s) -> getDescription(s)), //

			    ColumnDescriptor.create("Repeat count", 50, true, true, (s) -> SchedulerSupport.getInstance().getRepeatCount(s)), //

			    ColumnDescriptor.create("Repeat interval", 50, true, true,
				    (s) -> SchedulerSupport.getInstance().getRepeatInterval(s)), //

			    ColumnDescriptor.create("Status", 100, true, true, (s) -> SchedulerSupport.getInstance().getJobPhase(s)), //

			    ColumnDescriptor.create("Fired time", 150, true, true, (s) -> SchedulerSupport.getInstance().getFiredTime(s)), //

			    ColumnDescriptor.create("End time", 150, true, true, (s) -> SchedulerSupport.getInstance().getEndTime(s)), //

			    ColumnDescriptor.create("El. time (HH:mm:ss)", 170, true, true,
				    (s) -> SchedulerSupport.getInstance().getElapsedTime(s)), //

			    ColumnDescriptor.create("Next fire time", 150, true, true,
				    (s) -> SchedulerSupport.getInstance().getNextFireTime(s)), //

			    ColumnDescriptor.create("Info", true, true, false, (s) -> SchedulerSupport.getInstance().getAllMessages(s))//
		    ), getItemsList()).//

		    reloadable(() -> SchedulerSupport.getInstance().update()).//

		    build();

	    setTabInfo(tabInfo);
	}

	/**
	 * @return
	 */
	private List<ContextMenuItem> getItemsList() {

	    ArrayList<ContextMenuItem> list = new ArrayList<>();
//	    list.add(new CustomTaskSettingEditorMenuItem());
	    list.add(new RowValuesFormatterMenuItem());
	    list.add(new CustomTaskStarter());

	    return list;
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getDescription(Setting setting) {

	    return setting.getObject().getJSONObject(TASK_DESCRIPTION_OPTION_KEY).getJSONArray("values").getString(0);
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getName(Setting setting) {

	    return setting.getObject().getJSONObject(TASK_NAME_OPTION_KEY).getJSONArray("values").getString(0);
	}
    }

    /**
     * @author Fabrizio
     */
    public static class TaskSettingValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    CustomTaskSetting thisSetting = (CustomTaskSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	    ValidationResponse validationResponse = new ValidationResponse();

	    //
	    //
	    //

	    String taskClassName = thisSetting.getTaskClassName();

	    if (taskClassName == null || taskClassName.isEmpty()) {

		validationResponse.getErrors().add("A task name must be provided");
		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);

	    } else {

		try {
		    Class<?> taskClass = Class.forName(taskClassName);

		    if (!Task.class.isAssignableFrom(taskClass)) {

			validationResponse.getErrors().add("Custom task class do not implement the 'Task' interface");
			validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		    }

		} catch (ClassNotFoundException e) {

		    validationResponse.getErrors().add("Custom task class not found");
		    validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
		}
	    }

	    //
	    //
	    //

	    String taskDescription = thisSetting.getTaskDescription();

	    if (taskDescription == null || taskDescription.trim().isEmpty()) {

		validationResponse.getErrors().add("Task description missing");
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
    public Optional<String> getTaskOptions() {

	return getOption(TASK_OPTIONS_OPTION_KEY, String.class).get().getOptionalValue();
    }

    /**
     * @param options
     */
    public void setTaskOptions(String options) {

	getOption(TASK_OPTIONS_OPTION_KEY, String.class).get().setValue(options);
    }

    /**
     * @return
     */
    public String getTaskDescription() {

	return getOption(TASK_DESCRIPTION_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param desc
     */
    public void setTaskDescription(String desc) {

	getOption(TASK_DESCRIPTION_OPTION_KEY, String.class).get().setValue(desc);
    }

    /**
     * @return
     */
    public List<String> getEmailRecipients() {

	Optional<String> optional = getOption(EMAIL_RECIPIENTS_OPTIONS_OPTION_KEY, String.class).get().getOptionalValue();

	if (optional.isPresent()) {

	    return Arrays
		    .asList(getOption(EMAIL_RECIPIENTS_OPTIONS_OPTION_KEY, String.class).//
			    get().//
			    getValue().//
			    split(","))
		    .//
		    stream().//
		    map(v -> v.trim()).//
		    collect(Collectors.toList());
	}

	return new ArrayList<>();
    }

    /**
     * @param recipients
     */
    public void setEmailRecipients(String... recipients) {

	getOption(EMAIL_RECIPIENTS_OPTIONS_OPTION_KEY, String.class).//
		get().//
		setValue(Arrays.asList(recipients).//
			stream().//
			collect(Collectors.joining(",")));
    }

    /**
     * @return
     */
    public void selectTaskName(String name) {

	getOption(TASK_NAME_OPTION_KEY, String.class).get().select(n -> n.equals(name));
    }

    /**
     * @return
     */
    public String getSelectedTaskName() {

	return getOption(TASK_NAME_OPTION_KEY, String.class).get().getSelectedValue();
    }

    /**
     * @return
     */
    public String getTaskClassName() {

	ServiceLoader<CustomTask> loader = ServiceLoader.load(CustomTask.class);

	String taskName = getSelectedTaskName();

	if (taskName != null) {

	    Optional<CustomTask> task = StreamUtils.iteratorToStream(loader.iterator()).//
		    filter(t -> t.getName().equals(taskName)).//
		    findFirst();

	    if (task.isPresent()) {

		return task.get().//
			getClass().//
			getCanonicalName();
	    }
	}

	return null;
    }

    /**
     * @return
     */
    @Override
    public String getWorkerName() {

	return getSelectedTaskName();
    }

    /**
     * @param object
     */
    public CustomTaskSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public CustomTaskSetting(String object) {

	super(object);
    }
    
    public static void main(String[] args) {
	
	System.out.println(new CustomTaskSetting());
    }
}
