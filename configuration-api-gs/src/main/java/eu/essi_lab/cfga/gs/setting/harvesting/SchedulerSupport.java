package eu.essi_lab.cfga.gs.setting.harvesting;

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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.JobStatus.JobPhase;

/**
 * @author Fabrizio
 */
public class SchedulerSupport {

    /**
     * 
     */
    private List<SchedulerJobStatus> statusList;

    /**
     * 
     */
    private List<String> executingSettings;

    /**
     * 
     */
    private HashMap<String, String> nextFireTimeMap;

    /**
     * 
     */
    private DateTimeZone userDateTimeZone;

    /**
     * 
     */
    private Scheduler scheduler;

    /**
     * 
     */
    private static final SchedulerSupport INSTANCE = new SchedulerSupport();

    /**
     * 
     */
    private SchedulerSupport() {

	executingSettings = new ArrayList<>();
	statusList = new ArrayList<>();
	nextFireTimeMap = new HashMap<>();
	userDateTimeZone = DateTimeZone.UTC;
    }

    /**
     * @return
     */
    public static SchedulerSupport getInstance() {

	return INSTANCE;
    }

    /**
     *	
     */
    public void updateDelayed() {

	new Timer().schedule(new TimerTask() {

	    @Override
	    public void run() {

		try {

		    update();

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }

	}, TimeUnit.SECONDS.toMillis(10));
    }

    /**
     * 
     */
    public void update() {

	synchronized (SchedulerSupport.this) {

	    GSLoggerFactory.getLogger(HarvestingSetting.class).debug("Updating scheduler support STARTED");

	    userDateTimeZone = ConfigurationWrapper.getSchedulerSetting().getUserDateTimeZone();

	    scheduler = SchedulerFactory.getScheduler(ConfigurationWrapper.getSchedulerSetting());

	    //
	    //
	    //

	    try {

		executingSettings = scheduler.//
			listExecutingSettings().//
			stream().//
			map(s -> s.getIdentifier() + "/" + parseDate(s.getFiredTime().get())).//
			collect(Collectors.toList());

		//
		//
		//

	    } catch (Exception e) {

		executingSettings = new ArrayList<String>();

		GSLoggerFactory.getLogger(HarvestingSetting.class).error("List executing settings failed: " + e.getMessage(), e);
	    }

	    try {

		HashMap<String, List<Optional<Date>>> tempMap = scheduler.//
			listScheduledSettings().//
			stream().//
			collect(Collectors.groupingBy(//
				SchedulerWorkerSetting::getIdentifier, //
				HashMap::new, //
				Collectors.mapping(SchedulerWorkerSetting::getNextFireTime, Collectors.toList())));

		nextFireTimeMap = new HashMap<>();

		tempMap.keySet().forEach(key -> {

		    List<Optional<Date>> list = tempMap.get(key);
		    if (list != null) {

			Optional<Date> optional = list.get(0);

			if (optional.isPresent()) {

			    Date date = optional.get();

			    nextFireTimeMap.put(key, parseDate(date));
			}
		    }
		});

	    } catch (Exception e) {

		nextFireTimeMap = new HashMap<>();

		GSLoggerFactory.getLogger(HarvestingSetting.class).error("List scheduled settings failed: " + e.getMessage(), e);
	    }

	    //
	    //
	    //

	    try {
		statusList = scheduler.getJobStatuslist();

	    } catch (SQLException e) {

		GSLoggerFactory.getLogger(HarvestingSetting.class).error("List job status failed: " + e.getMessage(), e);
	    }

	    //
	    //
	    //

	    GSLoggerFactory.getLogger(HarvestingSetting.class).debug("Updating scheduler support ENDED");
	}
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getFiredTime(Setting setting) {

	Optional<String> time = executingSettings.//
		stream().//
		filter(s -> retrieveIdentifier(s).equals(setting.getIdentifier())).//
		findFirst().//
		map(s -> retrieveFiredTime(s));//

	if (time.isPresent()) {

	    return time.get();
	}

	Optional<SchedulerJobStatus> jobStatus = getJobStatus(setting);

	if (jobStatus.isPresent()) {

	    Optional<String> startTime = jobStatus.get().getStartTime();

	    if (startTime.isPresent()) {

		return parseTime(startTime.get()).replace("T", " ");
	    }
	}

	return "";
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getNextFireTime(Setting setting) {

	return nextFireTimeMap.getOrDefault(setting.getIdentifier(), "");
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getEndTime(Setting setting) {

	Optional<SchedulerJobStatus> jobStatus = getJobStatus(setting);

	if (jobStatus.isPresent()) {

	    Optional<String> endTime = jobStatus.get().getEndTime();

	    if (endTime.isPresent()) {

		return parseTime(endTime.get()).replace("T", " ");
	    }
	}

	return "";
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getIsRunning(Setting setting) {

	return executingSettings.//
		stream().//
		filter(s -> retrieveIdentifier(s).equals(setting.getIdentifier())).//
		findFirst().isPresent() ? "Yes" : "";
    }

    /**
     * @param setting
     * @return
     */
    public synchronized Optional<SchedulerJobStatus> getJobStatus(Setting setting) {

	return statusList.//
		stream().//
		filter(s -> s.getSettingId().equals(setting.getIdentifier())).//
		findFirst();

    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getJobPhase(Setting setting) {

	boolean present = executingSettings.//
		stream().//
		filter(s -> retrieveIdentifier(s).equals(setting.getIdentifier())).//
		findFirst().isPresent();

	if (present) {

	    return JobPhase.RUNNING.getLabel();
	}

	return getJobStatus(setting).map(status -> status.getPhase().getLabel()).orElse("");
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getAllMessages(Setting setting) {

	Optional<SchedulerJobStatus> jobStatus = getJobStatus(setting);

	String out = "";

	if (jobStatus.isPresent()) {

	    String messages = jobStatus.get().getJoinedMessages();
	    String recovering = jobStatus.get().isRecovering() ? "Yes" : "No";

	    out += "- Recovering: " + recovering + "\n";

	    out += messages;
	}

	return out.trim();
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getElapsedTime(Setting setting) {

	Optional<SchedulerJobStatus> jobStatus = getJobStatus(setting);

	if (jobStatus.isPresent()) {

	    Optional<String> optStartTime = jobStatus.get().getStartTime();
	    Optional<String> optEndTime = jobStatus.get().getEndTime();

	    if (optStartTime.isPresent() && optEndTime.isPresent()) {

		String startTime = parseTime(optStartTime.get());
		String endTime = parseTime(optEndTime.get());

		return computeElapsedTime(startTime, endTime);
	    }
	}

	return "";
    }

    /**
     * @param setting
     * @return
     */
    public String getSize(Setting setting) {

	Optional<SchedulerJobStatus> jobStatus = getJobStatus(setting);

	if (jobStatus.isPresent()) {

	    return jobStatus.get().getSize().orElse("");
	}

	return "";
    }

    /**
     * @param setting
     * @return
     */
    public String getRepeatInterval(Setting setting) {

	JSONObject scheduling = setting.getObject().getJSONObject("scheduling");

	if (scheduling.has("enabled") && !scheduling.getBoolean("enabled")) {

	    return "";
	}

	JSONObject interval = scheduling.getJSONObject("repeatInterval");
	if (interval.has("values")) {

	    String intervalValue = interval.getJSONArray("values").get(0).toString();
	    if (intervalValue.length() == 1) {
		intervalValue = "0" + intervalValue;
	    }
	    String unit = scheduling.getJSONObject("repeatIntervalUnit").getJSONArray("values").get(0).toString().toLowerCase();
	    if (unit.equals("hours")) {
		intervalValue = "00-" + intervalValue;
	    } else {
		intervalValue = intervalValue + "-00";
	    }

	    return intervalValue;
	}

	return "";
    }

    /**
     * @param date
     * @return
     */
    private String parseDate(Date date) {

	String dateTime = ISO8601DateTimeUtils.toDateTime(date, userDateTimeZone).toString();

	dateTime = dateTime.substring(0, dateTime.indexOf(".")).replace("T", " ");

	//
	// in some cases the NEXT_FIRE_TIME value is -1 so the date is 1970-01-01
	//
	if (dateTime.startsWith("1970-01-01")) {

	    dateTime = "";
	}

	return dateTime;
    }

    /**
     * @param time
     * @return
     */
    private String parseTime(String time) {

	time = ISO8601DateTimeUtils.toDateTime(time, userDateTimeZone).toString();
	time = time.substring(0, time.indexOf("."));

	return time;
    }

    /**
     * @param startTime
     * @param endTime
     * @return
     */
    private String computeElapsedTime(String startTime, String endTime) {

	long duration = ISO8601DateTimeUtils.parseISO8601ToDate(endTime).get().getTime()
		- ISO8601DateTimeUtils.parseISO8601ToDate(startTime).get().getTime();

	long HH = TimeUnit.MILLISECONDS.toHours(duration);
	long mm = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
	long ss = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;

	return String.format("%02d:%02d:%02d", HH, mm, ss);
    }

    /**
     * @param mangling
     * @return
     */
    private String retrieveIdentifier(String mangling) {

	return mangling.substring(0, mangling.indexOf("/"));
    }

    /**
     * @param mangling
     * @return
     */
    private String retrieveFiredTime(String mangling) {

	return mangling.substring(mangling.indexOf("/") + 1);
    }
}
