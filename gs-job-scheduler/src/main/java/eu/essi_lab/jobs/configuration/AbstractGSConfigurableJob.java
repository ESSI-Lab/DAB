package eu.essi_lab.jobs.configuration;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.model.configuration.option.GSConfOptionString;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.essi_lab.jobs.scheduler.GSJobSchedulerFactory;
import eu.essi_lab.jobs.scheduler.GS_JOB_INTERVAL_PERIOD;
import eu.essi_lab.jobs.scheduler.IGSJobScheduler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.configuration.AbstractGSconfigurableComposed;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionDate;
import eu.essi_lab.model.configuration.option.GSConfOptionInteger;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public abstract class AbstractGSConfigurableJob extends AbstractGSconfigurableComposed implements IGSConfigurableJob {

    public static final String START_DATE_KEY = "START_DATE_KEY";
    public static final String INTERVAL_PERIOD_KEY = "INTERVAL_PERIOD_KEY";
    public static final String INTERVAL_KEY = "INTERVAL_KEY";
    /**
     *
     */
    private static final long serialVersionUID = 493558083034022434L;

    @JsonIgnore
    private  transient String group = this.getClass().getPackage().toString();

    @JsonIgnore
    private transient IGSJobScheduler scheduler;

    private Map<String, GSConfOption<?>> supported = new HashMap<>();

    private Map<String, Object> map = new HashMap<>();

    private IGSConfigurable configurable;
    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public AbstractGSConfigurableJob() {

	GSConfOptionDate dateOption = new GSConfOptionDate();

	dateOption.setMandatory(true);
	dateOption.setKey(START_DATE_KEY);
	dateOption.setLabel("Start Date");

	getSupportedOptions().put(START_DATE_KEY, dateOption);

	GSConfOptionIntervalPeriod intervalPriodOption = new GSConfOptionIntervalPeriod();

	intervalPriodOption.setMandatory(true);
	intervalPriodOption.setKey(INTERVAL_PERIOD_KEY);
	intervalPriodOption.setLabel("Execution Interval Period");
	intervalPriodOption.setValue(GS_JOB_INTERVAL_PERIOD.DAYS);

	getSupportedOptions().put(INTERVAL_PERIOD_KEY, intervalPriodOption);

	GSConfOptionInteger intervalOption = new GSConfOptionInteger();

	intervalOption.setMandatory(true);
	intervalOption.setKey(INTERVAL_KEY);
	intervalOption.setLabel("Execution Interval Length");
	intervalOption.setValue(1);

	getSupportedOptions().put(INTERVAL_KEY, intervalOption);

    }

    @JsonIgnore
    public void setConfigurable(IGSConfigurable configurable) {
	this.configurable = configurable;
    }

    @JsonIgnore
    public IGSConfigurable getConfigurable() {
	return configurable;
    }

    @JsonIgnore
    public Map<String, Object> getDataMap() {
	return map;
    }

    @JsonIgnore
    public void setDataMap(Map<String, Object> data) {
	map = data;
    }

    @JsonIgnore
    public void setScheduler(IGSJobScheduler igsJobScheduler) {
	scheduler = igsJobScheduler;
    }

    @Override
    public Map<String, GSConfOption<?>> getSupportedOptions() {
	return supported;
    }

    @Override
    @JsonIgnore
    public GS_JOB_INTERVAL_PERIOD getIntervalPeriod() {
	return (GS_JOB_INTERVAL_PERIOD) getSupportedOptions().get(INTERVAL_PERIOD_KEY).getValue();
    }

    @Override
    @JsonIgnore
    public Date getStartDate() {

	return (Date) getSupportedOptions().get(START_DATE_KEY).getValue();

    }

    @Override
    @JsonIgnore
    public int getInterval() {
	return (Integer) getSupportedOptions().get(INTERVAL_KEY).getValue();
    }

    @Override
    @JsonIgnore
    public boolean completed() {
	return false;
    }

    @Override
    @JsonIgnore
    public void completed(boolean value) {

    }

    @Override
    @JsonIgnore
    public String getGroup() {
	return group;
    }

    @Override
    @JsonIgnore
    public void setGroup(String jobGroup) {

	group = jobGroup;
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {

    }

    @Override
    @JsonIgnore
    public String getId() {
	return getKey();
    }

    @Override
    @JsonIgnore
    public String getInstantiableClass() {
	return getClass().getName();
    }

    @Override
    @JsonIgnore
    public void setId(String id) {
	setKey(id);
    }

    @Override
    public void onFlush() throws GSException {

	logger.debug("onFlush invoked on {}", this);

	logger.debug("Verify if this can be scheduled");

	if (validate()) {

	    logger.debug("Verify if scheduler has been set");

	    verifyScheduler();

	    GSLoggerFactory.getLogger(this.getClass()).debug("Verify if already scheduled {}", this);

	    boolean needToBeScheduled = true;

	    boolean scheduled = scheduler.isScheduled(this);

	    if (scheduled) {

		logger.debug("Already Scheduled {}", this);

		boolean sameSchedule = scheduler.hasSameSchedule(this, getStartDate(), getIntervalPeriod(), getInterval());

		logger.debug("Found {} schedule", sameSchedule ? "same" : "different");

		if (!sameSchedule) {

		    logger.debug("Unscheduling {}", this);

		    scheduler.unscheduleJob(this);

		    logger.debug("Unscheduled {}", this);
		} else
		    needToBeScheduled = false;

	    }

	    if (!needToBeScheduled) {
		logger.debug("No need to schedule {}, returning", this);
		return;
	    }

	    Date scheduledDate = scheduler.scheduleJob(null, this, getConfigurable(), getDataMap());

	    logger.debug("Scheduling {} with {}", this, printSchedule());

	    if (logger.isDebugEnabled())
		logger.debug("[Start at {}] Scheduled {} with {}", ISO8601DateTimeUtils.getISO8601DateTime(scheduledDate), this,
			printSchedule());
	}
    }

    private void verifyScheduler() throws GSException {
	if (scheduler != null) {

	    logger.debug("Found scheduler: {}", scheduler);

	    return;
	}

	logger.debug("Scheduler was not found, retrieving one");

	IGSJobScheduler sch = new GSJobSchedulerFactory().getGSJobScheduler();

	logger.debug("Retrieved Scheduler: {}", sch);

	setScheduler(sch);

    }

    public String printSchedule() {

	if (validate())

	    return "Start Date: " + getStartDate() + " -- Period: " + getIntervalPeriod().getName() + " -- Interval: " + getInterval();

	return "Schedule Not Ready";

    }

    public boolean validate() {

	boolean period = getIntervalPeriod() != null;

	boolean interval = getInterval() > 0;

	boolean date = getStartDate() != null;

	boolean ready = period && interval && date;

	if (!ready) {

	    logger.warn("One or more of the required options for scheduling this batch job ({}) is missing, the job will not be "
		    + "scheduled: Start date {} Interval {} Interval Priod {}", toString(), date, interval, period);

	}

	return ready;

    }

    @Override
    public String toString() {

	return "{GS Configurable Job [id:" + this.getId() + "][group:" + this.getGroup() + "]} " + super.toString();

    }

    @Override
    public void onStartUp() {
	//do nothing here
    }

}
