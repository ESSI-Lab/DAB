package eu.essi_lab.cfga.setting.scheduling;

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

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import eu.essi_lab.cfga.option.ISODateTime;
import eu.essi_lab.cfga.option.ISODateTimeOptionBuilder;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class Scheduling extends Setting {

    /**
     * 
     */
    public static final String SCHEDULING_OBJECT_TYPE = "scheduling";

    private static final String REPEAT_COUNT_OPTION_KEY = "repeatCount";
    private static final String REPEAT_INTERVAL_OPTION_KEY = "repeatInterval";
    private static final String REPEAT_INTERVAL_UNIT_OPTION_KEY = "repeatIntervalUnit";
    private static final String START_TIME_OPTION_KEY = "startTime";
    private static final String END_TIME_OPTION_KEY = "endTime";

    /**
     * 
     */
    public Scheduling() {

	setName("Scheduling");

	setEditable(false);
	enableCompactMode(false);

	setDescription("When enabled, the default configuration of a scheduling is to run once and to start now");

	Option<Integer> repeatCountOption = IntegerOptionBuilder.get().//
		withKey(REPEAT_COUNT_OPTION_KEY).//
		withLabel("Repeat count").//
		withDescription(
			"Enable this option and select how many times to repeat the task, or leave it unset to run indefinitely. Disable this option to run once (default)")
		.//
		withMinValue(2).//
		disabled().//
		build();

	addOption(repeatCountOption);

	Option<Integer> repeatIntervalOption = IntegerOptionBuilder.get().//
		withKey(REPEAT_INTERVAL_OPTION_KEY).//
		withLabel("Repeat interval").//
		withValue(1).//
		withMinValue(1).//
		withDescription(
			"The interval between the task ripetitions expressed according by the 'Repeat interval time unit' option. If the scheduler is set to run once, this option is ignored")
		.//
		cannotBeDisabled().//
		build();

	addOption(repeatIntervalOption);

	Option<TimeUnit> repeatIntervalUnit = OptionBuilder.get(TimeUnit.class).//
		withKey(REPEAT_INTERVAL_UNIT_OPTION_KEY).//
		withLabel("Repeat interval time unit").//
		withDescription(
			"The time unit of the 'Repeat interval' option. If the scheduler is set to run once, this option is ignored")
		.//
		withSingleSelection().//
		withValues(Arrays.asList(TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS)).//
		withSelectedValue(TimeUnit.DAYS).//
		cannotBeDisabled().//
		build();

	addOption(repeatIntervalUnit);

	Option<ISODateTime> startTimeOption = ISODateTimeOptionBuilder.get().//
		withKey(START_TIME_OPTION_KEY).//
		withLabel("Start time").//
		withDescription("Disable this option to start now (default)").//
		disabled().//
		build();

	addOption(startTimeOption);

	Option<ISODateTime> endTimeOption = ISODateTimeOptionBuilder.get().//
		withKey(END_TIME_OPTION_KEY).//
		withLabel("End time").//
		withDescription(
			"If enabled, an eventual value of 'Repeat count' will be ignored and the scheduling will run indefinitely until this date according to the the 'Repeat interval' and the 'Repeat interval time unit' options")
		.//
		build();

	addOption(endTimeOption);

	//
	// also disables all the options
	//
	setEnabled(false);
    }

    /**
     * @param scheduling
     * @param userDateTimeZone
     * @param validationResponse
     */
    public static void validate(Scheduling scheduling, DateTimeZone userDateTimeZone, ValidationResponse validationResponse) {

	Optional<ISODateTime> optStartTime = scheduling.getStartTime();
	Optional<ISODateTime> optEndTime = scheduling.getEndTime();

	if (optStartTime.isPresent() && optEndTime.isPresent()) {

	    ISODateTime startTime = optStartTime.get();
	    ISODateTime endTime = optEndTime.get();

	    String startTimeValue = startTime.getValue();
	    startTimeValue = startTimeValue.substring(0, startTimeValue.lastIndexOf(":"));
	    String endTimeValue = endTime.getValue();
	    endTimeValue = endTimeValue.substring(0, endTimeValue.lastIndexOf(":"));

	    if (endTimeValue.compareTo(startTimeValue) < 0) {

		validationResponse.getErrors().add("End time must be after start time");
		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);

	    } else if (startTimeValue.equals(endTimeValue)) {

		validationResponse.getErrors().add("Start time and end time cannot be equals");
		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
	    }
	}

	boolean startInThePast = false;

	if (optStartTime.isPresent()) {

	    //
	    // this ISO8601 date time is expressed according to the user date time zone (e.g.: Europe/Berlin)
	    // while new Date(System.currentTimeMillis()) is in GMT, so it must be converted
	    //
	    ISODateTime startTime = optStartTime.get();
	    Date gmtDateTime = ISO8601DateTimeUtils.toGMTDateTime(startTime.getValue(), userDateTimeZone);

	    if (gmtDateTime.compareTo(new Date(System.currentTimeMillis())) < 0) {

		startInThePast = true;
		validationResponse.getWarnings().add("Start time is in the past. The job will start when the configuration is saved");
	    }
	}

	Option<ISODateTime> startTimeOption = scheduling.getOption(START_TIME_OPTION_KEY, ISODateTime.class).get();
	boolean startTimeEnabled = startTimeOption.isEnabled();
	if (startTimeEnabled) {

	    if (!optStartTime.isPresent()) {

		validationResponse.getErrors().add("Start time option enabled but the date/time set is incomplete");
		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
	    }
	}

	Option<ISODateTime> endTimeOption = scheduling.getOption(END_TIME_OPTION_KEY, ISODateTime.class).get();
	boolean endTimeEnabled = endTimeOption.isEnabled();
	boolean endTimePresent = false;

	if (endTimeEnabled) {

	    if (!optEndTime.isPresent()) {

		validationResponse.getErrors().add("End time option enabled  but the date/time set is incomplete");
		validationResponse.setResult(ValidationResult.VALIDATION_FAILED);
	    } else {
		endTimePresent = true;
	    }
	}

	if (scheduling.isEnabled()) {

	    if (scheduling.isRunOnceSet()) {

		validationResponse.getWarnings().add("The scheduling is set to run once");

	    } else if (scheduling.isRunIndefinitelySet() && !scheduling.getEndTime().isPresent()) {

		validationResponse.getWarnings().add(
			"The scheduling is set to run indefinitely at interval of " + scheduling.getRepeatInterval() + " " +scheduling.getRepeatIntervalUnit());

	    } else if (endTimePresent) {

		validationResponse.getWarnings().add("The scheduling is set to run until " + optEndTime.get()
			+ " (an eventual value of 'Repeat count' will be ignored)");

	    } else {

		validationResponse.getWarnings().add("The scheduling is set to run " + scheduling.getRepeatCount().get()
			+ " times at interval of " + scheduling.getRepeatInterval() + " " + scheduling.getRepeatIntervalUnit());
	    }
	}

	if (!scheduling.isEnabled()) {

	    validationResponse.getWarnings().add("The scheduling is disabled");

	} else if (!startTimeEnabled) {

	    validationResponse.getWarnings().add("The scheduling is set to start when the configuration is saved");

	} else if (!startInThePast && optStartTime.isPresent()) {

	    validationResponse.getWarnings().add("The scheduling is set to start " + optStartTime.get());
	}
    }

    /**
     * @param object
     */
    public Scheduling(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public Scheduling(String object) {

	super(object);
    }

    /**
     *  
     */
    public boolean isRunOnceSet() {

	return !getOption(REPEAT_COUNT_OPTION_KEY, Integer.class).get().isEnabled();
    }

    /**
     *  
     */
    public boolean isRunIndefinitelySet() {

	Option<Integer> option = getOption(REPEAT_COUNT_OPTION_KEY, Integer.class).get();

	return option.isEnabled() && !option.getOptionalValue().isPresent();
    }

    /**
     * 
     */
    public void setRunIndefinitely() {

	getOption(REPEAT_COUNT_OPTION_KEY, Integer.class).get().setEnabled(true);

	getOption(REPEAT_COUNT_OPTION_KEY, Integer.class).get().clearValues();
    }

    /**
     * 
     */
    public void setRunOnce() {

	getOption(REPEAT_COUNT_OPTION_KEY, Integer.class).get().setEnabled(false);

	getOption(REPEAT_COUNT_OPTION_KEY, Integer.class).get().clearValues();
    }

    /**
     *  
     */
    public Optional<ISODateTime> getStartTime() {

	Option<ISODateTime> option = getOption(START_TIME_OPTION_KEY, ISODateTime.class).get();
	if (option.isEnabled()) {

	    return option.getOptionalValue();
	}

	return Optional.empty();
    }

    /**
     *  
     */
    public Optional<ISODateTime> getEndTime() {

	Option<ISODateTime> option = getOption(END_TIME_OPTION_KEY, ISODateTime.class).get();
	if (option.isEnabled()) {

	    return option.getOptionalValue();
	}

	return Optional.empty();
    }

    /**
     * @param startTime
     */
    public void setStartTime(Date startTime) {

	setStartTime(new ISODateTime(startTime).getValue());
    }

    /**
     * @param isoStartTime
     */
    public void setStartTime(String isoStartTime) {

	getOption(START_TIME_OPTION_KEY, ISODateTime.class).get().setEnabled(true);

	getOption(START_TIME_OPTION_KEY, ISODateTime.class).get().setValue(new ISODateTime(isoStartTime));
    }

    /**
     * @param isoEndTime
     */
    public void setEndTime(String isoEndTime) {

	getOption(END_TIME_OPTION_KEY, ISODateTime.class).get().setEnabled(true);

	getOption(END_TIME_OPTION_KEY, ISODateTime.class).get().setValue(new ISODateTime(isoEndTime));

	getOption(REPEAT_COUNT_OPTION_KEY, Integer.class).get().clearValues();
    }

    /**
     * @param endTime
     */
    public void setEndTime(Date endTime) {

	setEndTime(new ISODateTime(endTime).getValue());
    }

    /**
     * @param repeatCount
     */
    public void setRepeatCount(int repeatCount) {

	if (repeatCount < 1) {
	    throw new IllegalArgumentException("Repeat count must be greater than zero");
	}

	getOption(REPEAT_COUNT_OPTION_KEY, Integer.class).get().setEnabled(true);

	getOption(REPEAT_COUNT_OPTION_KEY, Integer.class).get().setValue(repeatCount);
    }

    /**
     * @param repeatInterval
     * @param repeatIntervalUnit
     */
    public void setRepeatInterval(int repeatInterval, TimeUnit repeatIntervalUnit) {

	getOption(REPEAT_INTERVAL_OPTION_KEY, Integer.class).get().setValue(repeatInterval);

	getOption(REPEAT_INTERVAL_UNIT_OPTION_KEY, TimeUnit.class).get().select(v -> v == repeatIntervalUnit);
    }

    /**
     * @return
     */
    public Optional<Integer> getRepeatCount() {

	return getOption(REPEAT_COUNT_OPTION_KEY, Integer.class).get().getOptionalValue();
    }

    /**
     * @return
     */
    public Integer getRepeatInterval() {

	return getOption(REPEAT_INTERVAL_OPTION_KEY, Integer.class).get().getValue();
    }

    /**
     * @return
     */
    public TimeUnit getRepeatIntervalUnit() {

	return getOption(REPEAT_INTERVAL_UNIT_OPTION_KEY, TimeUnit.class).get().getSelectedValue();
    }

    /**
     * @return
     */
    public Long getRepeatIntervalMillis() {

	return getRepeatIntervalUnit().toMillis(getRepeatInterval());
    }

    /**
     * 
     */
    @Override
    protected String initObjectType() {

	return SCHEDULING_OBJECT_TYPE;
    }

}
