package eu.essi_lab.cfga.gs.setting.harvesting;

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

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.scheduler.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.scheduling.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.JobStatus.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.*;
import org.json.*;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.stream.*;

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
     * Fired time (epoch ms) per setting id for jobs currently in {@code GS_QRTZ_FIRED_TRIGGERS}.
     */
    private Map<String, Long> executingStartMsMap;

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
    private static final SchedulerSupport INSTANCE = new SchedulerSupport();

    /**
     *
     */
    private SchedulerSupport() {

	executingSettings = new ArrayList<>();
	executingStartMsMap = new HashMap<>();
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
     * Reloads only the executing jobs snapshot from Quartz ({@code GS_QRTZ_FIRED_TRIGGERS}).
     * Lightweight compared to {@link #update()} and enough to detect currently running harvests.
     */
    public void refreshExecuting() {

	synchronized (SchedulerSupport.this) {

	    userDateTimeZone = ConfigurationWrapper.getSchedulerSetting().getUserDateTimeZone();
	    Scheduler scheduler = SchedulerFactory.getScheduler(ConfigurationWrapper.getSchedulerSetting());

	    loadExecutingSettings(scheduler);
	}
    }

    /**
     * @param scheduler
     */
    private void loadExecutingSettings(Scheduler scheduler) {

	try {

	    long stepStartMs = System.currentTimeMillis();
	    Map<String, Long> startMsMap = new HashMap<>();
	    executingSettings = scheduler.//
		    listExecutingSettings().//
		    stream().//
		    map(s -> {
			startMsMap.put(s.getIdentifier(), s.getFiredTime().get().getTime());
			return s.getIdentifier() + "/" + parseDate(s.getFiredTime().get());
		    }).//
		    collect(Collectors.toList());
	    executingStartMsMap = startMsMap;
	    GSLoggerFactory.getLogger(SchedulerSupport.class).info("SchedulerSupport executing snapshot: {} entries, {} ms",
		    executingSettings.size(), System.currentTimeMillis() - stepStartMs);

	} catch (Exception e) {

	    executingSettings = new ArrayList<>();
	    executingStartMsMap = new HashMap<>();

	    GSLoggerFactory.getLogger(SchedulerSupport.class).error("List executing settings failed: {}", e.getMessage(), e);
	}
    }

    /**
     * @param setting
     * @return fired time in epoch milliseconds for a job currently executing, if known
     */
    public synchronized Optional<Long> getExecutingStartMs(Setting setting) {

	return Optional.ofNullable(executingStartMsMap.get(setting.getIdentifier()));
    }

    /**
     *
     */
    public void update() {

	synchronized (SchedulerSupport.this) {

	    long updateStartMs = System.currentTimeMillis();
	    GSLoggerFactory.getLogger(SchedulerSupport.class).info("SchedulerSupport.update() STARTED");

	    userDateTimeZone = ConfigurationWrapper.getSchedulerSetting().getUserDateTimeZone();

	    Scheduler scheduler = SchedulerFactory.getScheduler(ConfigurationWrapper.getSchedulerSetting());

	    loadExecutingSettings(scheduler);

	    try {

		long stepStartMs = System.currentTimeMillis();
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

			Optional<Date> optional = list.getFirst();

			optional.ifPresent(date -> nextFireTimeMap.put(key, parseDate(date)));
		    }
		});
		GSLoggerFactory.getLogger(SchedulerSupport.class).info("SchedulerSupport.update() listScheduledSettings: {} entries, {} ms",
			nextFireTimeMap.size(), System.currentTimeMillis() - stepStartMs);

	    } catch (Exception e) {

		nextFireTimeMap = new HashMap<>();

		GSLoggerFactory.getLogger(SchedulerSupport.class).error("List scheduled settings failed: {}", e.getMessage(), e);
	    }

	    //
	    //
	    //

	    try {
		long stepStartMs = System.currentTimeMillis();
		statusList = scheduler.getJobStatuslist();
		GSLoggerFactory.getLogger(SchedulerSupport.class).info("SchedulerSupport.update() getJobStatuslist: {} entries, {} ms",
			statusList.size(), System.currentTimeMillis() - stepStartMs);

	    } catch (SQLException e) {

		GSLoggerFactory.getLogger(SchedulerSupport.class).error("List job status failed: {}", e.getMessage(), e);
	    }

	    //
	    //
	    //

	    GSLoggerFactory.getLogger(SchedulerSupport.class).info("SchedulerSupport.update() ENDED in {} ms",
		    System.currentTimeMillis() - updateStartMs);
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
		map(this::retrieveFiredTime);//

	if (time.isPresent()) {

	    return time.get();
	}

	if (schedulingDisabled(setting)) {

	    return "";
	}

	Optional<SchedulerJobStatus> jobStatus = getJobStatus(setting);

	if (jobStatus.isPresent() && jobStatus.get().getPhase() != JobPhase.RESCHEDULED) {

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

	if (schedulingDisabled(setting)) {

	    return "";
	}

	if (getRepeatCount(setting).equals("Once") && getIsRunning(setting).equals("Yes")) {

	    return "";
	}

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

	if (schedulingDisabled(setting)) {

	    return "";
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
		anyMatch(s -> retrieveIdentifier(s).equals(setting.getIdentifier())) ? "Yes" : "";
    }

    /**
     * @param setting
     * @return
     */
    public synchronized Optional<SchedulerJobStatus> getJobStatus(Setting setting) {

	return statusList.//
		stream().//
		filter(s -> s.getSettingId().equals(setting.getIdentifier())).//
		max(Comparator.comparing(s -> s.getStartTime().orElse(""), Comparator.nullsLast(String::compareTo)));

    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getJobPhase(Setting setting) {

	if (isExecuting(setting)) {

	    return JobPhase.RUNNING.getLabel();
	}

	return resolveEffectivePhase(getJobStatus(setting)).map(JobPhase::getLabel).orElse("");
    }

    /**
     * A job is running only while Quartz reports it in {@code GS_QRTZ_FIRED_TRIGGERS}. A persisted
     * {@link JobPhase#RUNNING} phase without a matching fired trigger is treated as stale.
     */
    private boolean isExecuting(Setting setting) {

	return executingSettings.//
		stream().//
		anyMatch(s -> retrieveIdentifier(s).equals(setting.getIdentifier()));
    }

    /**
     * @param jobStatus
     * @return
     */
    private Optional<JobPhase> resolveEffectivePhase(Optional<SchedulerJobStatus> jobStatus) {

	if (jobStatus.isEmpty()) {

	    return Optional.empty();
	}

	SchedulerJobStatus status = jobStatus.get();
	JobPhase phase = status.getPhase();

	if (phase != JobPhase.RUNNING) {

	    return Optional.of(phase);
	}

	//
	// Stale RUNNING: the job is not in fired triggers but JOB_STATUS was not finalized
	//
	if (status.getEndTime().isPresent()) {

	    if (!status.getErrorMessages().isEmpty()) {

		return Optional.of(JobPhase.ERROR);
	    }

	    return Optional.of(JobPhase.COMPLETED);
	}

	return Optional.empty();
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getJobHostName(Setting setting) {

	return getJobStatus(setting).flatMap(SchedulerJobStatus::getHostName).orElse("");
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getJobAwsTaskId(Setting setting) {

	return getJobStatus(setting).flatMap(SchedulerJobStatus::getAwsTaskId).orElse("");
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

	if (schedulingDisabled(setting)) {

	    return "";
	}

	return "";
    }

    /**
     * @param setting
     * @return
     */
    public String getSize(Setting setting) {

	Optional<SchedulerJobStatus> jobStatus = getJobStatus(setting);

	return jobStatus.map(schedulerJobStatus -> schedulerJobStatus.getSize().orElse("")).orElse("");

    }

    /**
     * @param setting
     * @return
     */
    public String getRepeatCount(Setting setting) {

	if (schedulingDisabled(setting)) {

	    return "";
	}

	JSONObject scheduling = setting.getObject().getJSONObject("scheduling");

	if (!scheduling.getJSONObject("repeatCount").has("enabled") || scheduling.getJSONObject("repeatCount").getBoolean("enabled")) {

	    if (!scheduling.getJSONObject("repeatCount").has("values")) {

		return "Indefinitely";
	    }

	    return scheduling.getJSONObject("repeatCount").getJSONArray("values").getInt(0) + " times";
	}

	return "Once";
    }

    public static void main(String[] args) {

	String formatted = String.format("% 4d", 4);
	System.out.println(formatted);
    }

    /**
     * @param setting
     * @return
     */
    public String getRepeatInterval(Setting setting) {

	if (schedulingDisabled(setting)) {

	    return "";
	}

	JSONObject scheduling = setting.getObject().getJSONObject("scheduling");

	if (scheduling.getJSONObject("repeatCount").has("enabled") && !scheduling.getJSONObject("repeatCount").getBoolean("enabled")) {

	    return "";
	}

	JSONObject interval = scheduling.getJSONObject("repeatInterval");
	if (interval.has("values")) {

	    String intervalValue = interval.getJSONArray("values").get(0).toString();
	    intervalValue = StringUtils.leftPad(intervalValue, 4, "0");

	    String unit = scheduling.getJSONObject("repeatIntervalUnit").getJSONArray("values").get(0).toString().toLowerCase();
	    return switch (unit) {
		case "days" -> intervalValue + " day/s";
		case "hours" -> intervalValue + " hour/s";
		case "minutes" -> intervalValue + " minute/s";
		default -> intervalValue + " N/A";
	    };
	}

	return "";
    }

    /**
     * @param setting
     * @return
     */
    private boolean schedulingDisabled(Setting setting) {

	JSONObject scheduling = setting.getObject().getJSONObject("scheduling");

	return scheduling.has("enabled") && !scheduling.getBoolean("enabled");
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

	long duration =
		ISO8601DateTimeUtils.parseISO8601ToDate(endTime).get().getTime() - ISO8601DateTimeUtils.parseISO8601ToDate(startTime).get()
			.getTime();

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
