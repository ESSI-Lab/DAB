package eu.essi_lab.gssrv.rest;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.SchedulerSupport;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.HostNamePropertyUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.model.GSSource;
import jakarta.jws.WebService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST API for harvesting scheduler status (same data as the configuration GUI
 * Brokering &rarr; Harvesting tab).
 * <p>
 * By default uses the cached {@link SchedulerSupport} snapshot (like the GUI grid)
 * and does not read per-source {@link HarvestingProperties} files.
 */
@WebService
@Path("/")
public class HarvestStatusService {

    private static final Logger LOGGER = GSLoggerFactory.getLogger(HarvestStatusService.class);

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/harvest-status")
    public Response harvestStatus(//
	    @QueryParam("hours") Long hoursParam, //
	    @QueryParam("from") String fromParam, //
	    @QueryParam("to") String toParam, //
	    @QueryParam("refresh") Boolean refreshParam, //
	    @QueryParam("includeProperties") Boolean includePropertiesParam) {

	try {
	    long requestStartMs = System.currentTimeMillis();
	    boolean refreshScheduler = Boolean.TRUE.equals(refreshParam);
	    boolean includeProperties = Boolean.TRUE.equals(includePropertiesParam);

	    LOGGER.info("harvest-status START hours={} refresh={} includeProperties={}", hoursParam, refreshScheduler,
		    includeProperties);

	    long defaultHours = 2;
	    long hours = hoursParam != null && hoursParam > 0 ? hoursParam : defaultHours;

	    long windowEndMs = System.currentTimeMillis();
	    long windowStartMs = windowEndMs - TimeUnit.HOURS.toMillis(hours);

	    if (fromParam != null && !fromParam.isBlank() && toParam != null && !toParam.isBlank()) {
		Optional<Date> fromDate = ISO8601DateTimeUtils.parseISO8601ToDate(fromParam.trim());
		Optional<Date> toDate = ISO8601DateTimeUtils.parseISO8601ToDate(toParam.trim());
		if (fromDate.isPresent() && toDate.isPresent() && fromDate.get().before(toDate.get())) {
		    windowStartMs = fromDate.get().getTime();
		    windowEndMs = toDate.get().getTime();
		    hours = Math.max(1, (windowEndMs - windowStartMs) / TimeUnit.HOURS.toMillis(1));
		}
	    }

	    SchedulerSupport schedulerSupport = SchedulerSupport.getInstance();
	    long schedulerUpdateMs = 0;
	    if (refreshScheduler) {
		long schedulerUpdateStartMs = System.currentTimeMillis();
		schedulerSupport.update();
		schedulerUpdateMs = System.currentTimeMillis() - schedulerUpdateStartMs;
		LOGGER.info("harvest-status schedulerSupport.update() took {} ms", schedulerUpdateMs);
	    } else {
		long refreshExecutingStartMs = System.currentTimeMillis();
		schedulerSupport.refreshExecuting();
		schedulerUpdateMs = System.currentTimeMillis() - refreshExecutingStartMs;
		LOGGER.info("harvest-status schedulerSupport.refreshExecuting() took {} ms", schedulerUpdateMs);
	    }

	    long databaseInitMs = 0;
	    Database database = null;
	    if (includeProperties) {
		long databaseStartMs = System.currentTimeMillis();
		database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());
		databaseInitMs = System.currentTimeMillis() - databaseStartMs;
		LOGGER.info("harvest-status DatabaseFactory.get() took {} ms", databaseInitMs);
	    }

	    long nowMs = System.currentTimeMillis();

	    long settingsStartMs = System.currentTimeMillis();
	    List<HarvestingSetting> harvestingSettings = ConfigurationWrapper.getHarvestingSettings();
	    harvestingSettings.sort(Comparator.comparing(HarvestingSetting::getName, String.CASE_INSENSITIVE_ORDER));
	    long loadSettingsMs = System.currentTimeMillis() - settingsStartMs;
	    LOGGER.info("harvest-status loaded {} harvesting settings in {} ms", harvestingSettings.size(), loadSettingsMs);

	    JSONArray jobs = new JSONArray();
	    int runningCount = 0;
	    int completedCount = 0;
	    int errorCount = 0;
	    int inWindowCount = 0;
	    int withSchedulerData = 0;

	    long buildJobsStartMs = System.currentTimeMillis();
	    List<JSONObject> slowSources = new ArrayList<>();

	    for (HarvestingSetting setting : harvestingSettings) {
		long jobStartMs = System.currentTimeMillis();
		JSONObject job = buildJobEntry(setting, schedulerSupport, database, includeProperties, windowStartMs, windowEndMs,
			nowMs);
		long jobMs = System.currentTimeMillis() - jobStartMs;
		if (jobMs >= 100) {
		    JSONObject slow = new JSONObject();
		    slow.put("label", job.optString("label", setting.getName()));
		    slow.put("sourceId", job.optString("sourceId", ""));
		    slow.put("ms", jobMs);
		    slowSources.add(slow);
		    LOGGER.warn("harvest-status slow source build: {} ({}) took {} ms", job.optString("label"),
			    job.optString("sourceId"), jobMs);
		}
		jobs.put(job);

		if (job.optBoolean("hasSchedulerData")) {
		    withSchedulerData++;
		}
		if (job.optBoolean("inWindow")) {
		    inWindowCount++;
		    if (job.optBoolean("executing") || JobPhase.RUNNING.getLabel().equals(job.optString("status", ""))) {
			runningCount++;
		    } else if (JobPhase.COMPLETED.getLabel().equals(job.optString("status", ""))) {
			completedCount++;
		    } else if (JobPhase.ERROR.getLabel().equals(job.optString("status", ""))) {
			errorCount++;
		    }
		}
	    }
	    long buildJobsMs = System.currentTimeMillis() - buildJobsStartMs;
	    LOGGER.info("harvest-status built {} job entries in {} ms ({} slow sources >= 100 ms)", jobs.length(), buildJobsMs,
		    slowSources.size());

	    JSONObject window = new JSONObject();
	    window.put("hours", hours);
	    window.put("startMs", windowStartMs);
	    window.put("endMs", windowEndMs);
	    window.put("startIso", ISO8601DateTimeUtils.getISO8601DateTime(new Date(windowStartMs)));
	    window.put("endIso", ISO8601DateTimeUtils.getISO8601DateTime(new Date(windowEndMs)));

	    JSONObject summary = new JSONObject();
	    summary.put("totalSettings", harvestingSettings.size());
	    summary.put("withSchedulerData", withSchedulerData);
	    summary.put("jobsInWindow", inWindowCount);
	    summary.put("running", runningCount);
	    summary.put("completed", completedCount);
	    summary.put("error", errorCount);

	    JSONObject debug = new JSONObject();
	    debug.put("schedulerSupportClass", schedulerSupport.getClass().getName());
	    debug.put("schedulerRefreshed", refreshScheduler);
	    debug.put("executingRefreshed", !refreshScheduler);
	    debug.put("includeProperties", includeProperties);
	    debug.put("dataSource", "scheduler cache (same as configuration GUI harvesting grid)");

	    long totalMs = System.currentTimeMillis() - requestStartMs;
	    JSONObject timing = new JSONObject();
	    timing.put("schedulerUpdateMs", schedulerUpdateMs);
	    timing.put("databaseInitMs", databaseInitMs);
	    timing.put("loadSettingsMs", loadSettingsMs);
	    timing.put("buildJobsMs", buildJobsMs);
	    timing.put("totalMs", totalMs);
	    timing.put("slowSources", new JSONArray(slowSources));
	    debug.put("timing", timing);

	    JSONObject output = new JSONObject();
	    output.put("status", "success");
	    output.put("window", window);
	    output.put("summary", summary);
	    output.put("jobs", jobs);
	    output.put("debug", debug);

	    LOGGER.info(
		    "harvest-status END total={} ms (schedulerRefresh={} ms, database={} ms, settings={} ms, buildJobs={} ms, jobs={}, inWindow={})",
		    totalMs, schedulerUpdateMs, databaseInitMs, loadSettingsMs, buildJobsMs, jobs.length(), inWindowCount);

	    return Response.ok(output.toString(), MediaType.APPLICATION_JSON).build();

	} catch (Exception e) {
	    LOGGER.error("harvest-status failed: {}", e.getMessage(), e);
	    return Response.serverError().entity(errorResponse(e.getMessage()).toString()).build();
	}
    }

    private static JSONObject buildJobEntry(//
	    HarvestingSetting setting, //
	    SchedulerSupport schedulerSupport, //
	    Database database, //
	    boolean includeProperties, //
	    long windowStartMs, //
	    long windowEndMs, //
	    long nowMs) throws Exception {

	GSSource source = setting.getSelectedAccessorSetting().getSource();
	String phaseLabel = schedulerSupport.getJobPhase(setting);
	Optional<SchedulerJobStatus> jobStatus = schedulerSupport.getJobStatus(setting);
	boolean executing = "Yes".equals(schedulerSupport.getIsRunning(setting));

	if (phaseLabel.isBlank() && executing) {
	    phaseLabel = JobPhase.RUNNING.getLabel();
	}

	HarvestingProperties properties = null;
	long harvestingPropertiesMs = 0;
	if (includeProperties && database != null) {
	    long stepStartMs = System.currentTimeMillis();
	    SourceStorage storage = database.getStorage(source.getUniqueIdentifier());
	    properties = storage.getHarvestingProperties();
	    harvestingPropertiesMs = System.currentTimeMillis() - stepStartMs;
	}

	Optional<Long> startMsOpt = Optional.empty();
	String startSource = "none";

	if (executing) {
	    Optional<Long> firedStart = schedulerSupport.getExecutingStartMs(setting);
	    if (firedStart.isPresent()) {
		startMsOpt = firedStart;
		startSource = "firedTrigger";
	    }
	}

	if (startMsOpt.isEmpty()) {
	    startMsOpt = jobStatus.flatMap(s -> parseIsoToMillis(s.getStartTime().orElse(null)));
	    if (startMsOpt.isPresent()) {
		startSource = "scheduler";
	    }
	}

	if (startMsOpt.isEmpty() && properties != null) {
	    startMsOpt = parseIsoToMillis(properties.getStartHarvestingTimestamp());
	    if (startMsOpt.isPresent()) {
		startSource = "harvestingProperties";
	    }
	}

	boolean running = executing || JobPhase.RUNNING.getLabel().equals(phaseLabel);
	Long endMs = null;
	String endSource = "none";

	if (startMsOpt.isPresent()) {
	    if (running) {
		endMs = nowMs;
		endSource = "now";
	    } else {
		Optional<Long> schedulerEnd = jobStatus.flatMap(s -> parseIsoToMillis(s.getEndTime().orElse(null)));
		if (schedulerEnd.isPresent()) {
		    endMs = schedulerEnd.get();
		    endSource = "scheduler";
		} else if (properties != null) {
		    Optional<Long> propsEnd = parseIsoToMillis(properties.getEndHarvestingTimestamp());
		    if (propsEnd.isPresent()) {
			endMs = propsEnd.get();
			endSource = "harvestingProperties";
		    } else {
			endMs = startMsOpt.get();
			endSource = "startFallback";
		    }
		} else {
		    endMs = startMsOpt.get();
		    endSource = "startFallback";
		}
	    }
	}

	boolean inWindow = executing //
		|| startMsOpt.isPresent() && endMs != null //
			&& startMsOpt.get() < windowEndMs && endMs > windowStartMs;

	JSONObject debug = new JSONObject();
	debug.put("executing", executing);
	debug.put("startSource", startSource);
	debug.put("endSource", endSource);
	debug.put("storedPhase", jobStatus.map(s -> s.getPhase().getLabel()).orElse(""));
	debug.put("resolvedPhase", phaseLabel);
	debug.put("hasSchedulerStartTime", jobStatus.flatMap(SchedulerJobStatus::getStartTime).isPresent());
	debug.put("hasSchedulerEndTime", jobStatus.flatMap(SchedulerJobStatus::getEndTime).isPresent());
	if (includeProperties) {
	    debug.put("harvestingPropertiesMs", harvestingPropertiesMs);
	    if (properties != null) {
		debug.put("harvestingPropertiesCompleted", properties.isCompleted().map(Object::toString).orElse(""));
		debug.put("harvestingPropertiesStart", nullToEmpty(properties.getStartHarvestingTimestamp()));
		debug.put("harvestingPropertiesEnd", nullToEmpty(properties.getEndHarvestingTimestamp()));
	    }
	}

	if (!phaseLabel.isBlank() && startMsOpt.isEmpty()) {
	    debug.put("skipReason", "resolved phase present but no parseable start time");
	} else if (phaseLabel.isBlank() && startMsOpt.isEmpty()) {
	    debug.put("skipReason", "no scheduler phase and no start timestamp");
	} else if (!inWindow) {
	    debug.put("skipReason", "outside selected time window");
	}

	JSONObject job = new JSONObject();
	job.put("settingId", setting.getIdentifier());
	job.put("settingName", setting.getName());
	job.put("sourceId", source.getUniqueIdentifier());
	job.put("label", source.getLabel());
	job.put("status", phaseLabel.isBlank() ? JSONObject.NULL : phaseLabel);
	job.put("statusKey", phaseLabel.isBlank() ? "unknown" : phaseLabel.trim().toLowerCase());
	job.put("executing", executing);
	job.put("hasSchedulerData", jobStatus.isPresent());
	job.put("inWindow", inWindow);

	if (startMsOpt.isPresent()) {
	    long startMs = startMsOpt.get();
	    job.put("startMs", startMs);
	    job.put("startIso", ISO8601DateTimeUtils.getISO8601DateTime(new Date(startMs)));
	}
	if (endMs != null) {
	    job.put("endMs", endMs);
	    if (!running) {
		job.put("endIso", ISO8601DateTimeUtils.getISO8601DateTime(new Date(endMs)));
	    }
	}
	if (startMsOpt.isPresent() && endMs != null) {
	    job.put("durationMs", Math.max(1, endMs - startMsOpt.get()));
	}

	putIfNotEmpty(job, "firedTime", schedulerSupport.getFiredTime(setting));
	putIfNotEmpty(job, "endTime", running ? null : schedulerSupport.getEndTime(setting));
	putIfNotEmpty(job, "elapsedTime", schedulerSupport.getElapsedTime(setting));
	putIfNotEmpty(job, "nextFireTime", schedulerSupport.getNextFireTime(setting));
	String hostName = schedulerSupport.getJobHostName(setting);
	job.put("host", hostName == null || hostName.isBlank() ? "" : hostName);
	debug.put("host", hostName == null || hostName.isBlank() ? "" : hostName);

	String awsTaskId = schedulerSupport.getJobAwsTaskId(setting);
	job.put("awsTaskId", awsTaskId == null || awsTaskId.isBlank() ? "" : awsTaskId);
	if (awsTaskId != null && !awsTaskId.isBlank()) {
	    HostNamePropertyUtils.getEcsTaskLogsUrl(awsTaskId).ifPresent(url -> job.put("awsTaskLogsUrl", url));
	}
	debug.put("awsTaskId", awsTaskId == null || awsTaskId.isBlank() ? "" : awsTaskId);

	if (properties != null) {
	    job.put("resourcesCount", properties.getResourcesCount());
	    job.put("harvestingCount", properties.getHarvestingCount());
	    job.put("sourceUp", properties.isSourceUp().orElse(true));
	}

	job.put("debug", debug);

	return job;
    }

    private static Optional<Long> parseIsoToMillis(String iso) {
	if (iso == null || iso.isBlank()) {
	    return Optional.empty();
	}
	return ISO8601DateTimeUtils.parseISO8601ToDate(iso).map(Date::getTime);
    }

    private static void putIfNotEmpty(JSONObject target, String key, String value) {
	if (value != null && !value.isBlank()) {
	    target.put(key, value);
	}
    }

    private static String nullToEmpty(String value) {
	return value == null ? "" : value;
    }

    private static JSONObject errorResponse(String message) {
	JSONObject ret = new JSONObject();
	ret.put("status", "error");
	ret.put("message", message);
	return ret;
    }
}
