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

	    Scheduler scheduler = SchedulerFactory.getScheduler(ConfigurationWrapper.getSchedulerSetting());

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

		executingSettings = new ArrayList<>();

		GSLoggerFactory.getLogger(HarvestingSetting.class).error("List executing settings failed: {}", e.getMessage(), e);
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

			Optional<Date> optional = list.getFirst();

			optional.ifPresent(date -> nextFireTimeMap.put(key, parseDate(date)));
		    }
		});

	    } catch (Exception e) {

		nextFireTimeMap = new HashMap<>();

		GSLoggerFactory.getLogger(HarvestingSetting.class).error("List scheduled settings failed: {}", e.getMessage(), e);
	    }

	    //
	    //
	    //

	    try {
		statusList = scheduler.getJobStatuslist();

	    } catch (SQLException e) {

		GSLoggerFactory.getLogger(HarvestingSetting.class).error("List job status failed: {}", e.getMessage(), e);
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

	if (schedulingDisabled(setting)) {

	    return "";
	}

	Optional<String> time = executingSettings.//
		stream().//
		filter(s -> retrieveIdentifier(s).equals(setting.getIdentifier())).//
		findFirst().//
		map(this::retrieveFiredTime);//

	if (time.isPresent()) {

	    return time.get();
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

	if (schedulingDisabled(setting)) {

	    return "";
	}

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
		findFirst();

    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getJobPhase(Setting setting) {

	boolean present = executingSettings.//
		stream().//
		anyMatch(s -> retrieveIdentifier(s).equals(setting.getIdentifier()));

	if (present) {

	    return JobPhase.RUNNING.getLabel();
	}

	return getJobStatus(setting).map(status -> status.getPhase().getLabel()).orElse("");
    }

    /**
     * @param setting
     * @return
     */
    public synchronized String getJobHostName(Setting setting) {

	return getJobStatus(setting).map(SchedulerJobStatus::getHostName).orElse("");
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

	if (schedulingDisabled(setting)) {

	    return "";
	}

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
