/**
 * 
 */
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

import java.util.Date;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class SchedulerWorkerSetting extends Setting {

    /**
     * @author Fabrizio
     */
    public enum SchedulingGroup implements LabeledEnum {

	/**
	 * 
	 */		
	DEFAULT("defaultSchedulingGroup"),
	/**
	 * 
	 */
	HARVESTING("harvestingSchedulingGroup"),
	/**
	 * 
	 */
	AUGMENTING("augmentingSchedulingGroup"),
	/**
	 * 
	 */
	ASYNCH_ACCESS("asynchAccessSchedulingGroup"),
	/**
	 * 
	 */
	BULK_DOWNLOAD("bulkDownloadSchedulingGroup"), 
	/**
	 * 
	 */
	CUSTOM_TASK("customTaskGroup");

	private String label;

	/**
	 * @param label
	 */
	private SchedulingGroup(String label) {

	    this.label = label;
	}

	/**
	 * @return the label
	 */
	@Override
	public String getLabel() {

	    return label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}
    }

    private static final String SCHEDULING_SETTING_ID = "scheduling";
    private Date nextFireTime;
    private Date firedTime;

    /**
     * @param scheduling
     */
    public SchedulerWorkerSetting() {

	setName("Scheduling settings");

	setGroup(SchedulingGroup.DEFAULT);

	Scheduling scheduling = new Scheduling();
	scheduling.setIdentifier(SCHEDULING_SETTING_ID);

	addSetting(scheduling);
    }

    /**
     * @param object
     */
    public SchedulerWorkerSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public SchedulerWorkerSetting(String object) {

	super(object);
    }

    /**
     * @return the group
     */
    public SchedulingGroup getGroup() {

	return LabeledEnum.valueOf(SchedulingGroup.class, getObject().getString("group")).get();
    }

    /**
     * @param schedulingGroup the group to set
     */
    public void setGroup(SchedulingGroup schedulingGroup) {

	getObject().put("group", schedulingGroup.getLabel());
    }

    /**
     * @return
     */
    public Scheduling getScheduling() {

	return new Scheduling(getObject().getJSONObject(SCHEDULING_SETTING_ID));
    }

    /**
     * Not serializable property
     * 
     * @param nextFireTime
     */
    public void setNextFireTime(Date nextFireTime) {

	this.nextFireTime = nextFireTime;
    }

    /**
     * Not serializable property
     *
     * @return the nextFireTime
     */
    public Optional<Date> getNextFireTime() {

	return Optional.ofNullable(nextFireTime);
    }

    /**
     * Not serializable property
     * 
     * @param firedTime
     */
    public void setFiredTime(Date firedTime) {

	this.firedTime = firedTime;
    }

    /**
     * Not serializable property
     * 
     * @return 
     */
    public Optional<Date> getFiredTime() {

	return Optional.ofNullable(firedTime);
    }
}
