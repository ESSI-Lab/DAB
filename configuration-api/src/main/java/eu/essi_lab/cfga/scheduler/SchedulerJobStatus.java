package eu.essi_lab.cfga.scheduler;

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

import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.*;
import org.json.*;
import org.quartz.*;

import java.text.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class SchedulerJobStatus extends JobStatus {

    /**
     *
     */
    private static final DecimalFormat decimalFormat;

    static {

	decimalFormat = new DecimalFormat();
	decimalFormat.setGroupingSize(3);
	decimalFormat.setGroupingUsed(true);

	DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	symbols.setGroupingSeparator('.');
	symbols.setDecimalSeparator(',');

	decimalFormat.setDecimalFormatSymbols(symbols);
    }

    /**
     *
     */
    private final JSONObject object;

    /**
     * @param context
     * @param workerClassName
     * @param settingId
     */
    public SchedulerJobStatus(JobExecutionContext context, String settingId, String workerClassName) {

	object = new JSONObject();
	setStartTime();
	setPhase(JobPhase.RUNNING);

	boolean recovering = context.isRecovering();
	object.put("recovering", recovering);

	String group = context.getJobDetail().getKey().getGroup();
	setJobGroup(group);

	String name = context.getJobDetail().getKey().getName();
	object.put("jobName", name);

	setJobId(name);

	object.put("settingId", settingId);
	object.put("workerClassName", workerClassName);

	object.put("hostName", HostNamePropertyUtils.getHostNameProperty());
    }

    /**
     * @return
     */
    public Optional<String> getHostName() {

	return Optional.ofNullable(object.optString("hostName", null));
    }

    /**
     * @return
     */
    public boolean isRecovering() {

	return getObject().getBoolean("recovering");
    }

    /**
     * @return
     */
    public String getWorkerClassName() {

	return getObject().getString("workerClassName");
    }

    /**
     * @param object
     */
    public SchedulerJobStatus(JSONObject object) {

	this.object = object;
    }

    @Override
    public JSONObject getObject() {

	return object;
    }

    /**
     * @return the settingId
     */
    public String getSettingId() {

	return getObject().getString("settingId");
    }

    /**
     * @param size
     */
    public void setSize(int size) {

	getObject().put("size", String.valueOf(size));
    }

    /**
     * @return
     */
    public Optional<String> getSize() {

	if (getObject().has("size")) {

	    Integer size = Integer.valueOf(getObject().getString("size"));

	    return Optional.of(decimalFormat.format(size));
	}

	return Optional.empty();
    }

    /**
     * @return
     */
    public String format() {

	return this.toString();
    }

    /**
     * @param value
     * @return
     */
    public static String parse(String value) {

	if (value.isEmpty()) {
	    value = "0";
	} else {
	    try {
		value = SchedulerJobStatus.decimalFormat.parse(value).toString();
	    } catch (ParseException e) {
	    }
	}

	return value;
    }
}
