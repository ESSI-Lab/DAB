/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.jws.WebService;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.SchedulerException;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SystemSetting.KeyValueOptionKeys;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.gs.setting.harvesting.SchedulerSupport;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.gssrv.rest.conf.requests.EditSourceRequest;
import eu.essi_lab.gssrv.rest.conf.requests.HarvestSchedulingRequest;
import eu.essi_lab.gssrv.rest.conf.requests.HarvestUnschedulingRequest;
import eu.essi_lab.gssrv.rest.conf.requests.ListSourcesRequest;
import eu.essi_lab.gssrv.rest.conf.requests.PutSourceRequest;
import eu.essi_lab.gssrv.rest.conf.requests.RemoveSourceDataRequest;
import eu.essi_lab.gssrv.rest.conf.requests.RemoveSourceRequest;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

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

/**
 * @author Fabrizio
 */
@WebService
@Path("/{authToken}")
public class ConfigService {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response config(@Context HttpServletRequest hsr, @Context UriInfo uriInfo) {

	Properties keyValeOptions = ConfigurationWrapper.getSystemSettings().getKeyValueOptions().get();
	String configServiceAuthToken = keyValeOptions.getProperty(KeyValueOptionKeys.CONFIG_SERVICE_AUTHTOKEN.getLabel(), null);
	if (configServiceAuthToken == null) {

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Configuration service authentication token not defined");
	}

	Optional<String> path = uriInfo.getPathSegments().stream().map(s -> s.getPath()).findFirst();
	if (path.isEmpty()) {

	    return buildErrorResponse(Status.METHOD_NOT_ALLOWED, "Missing required authentication token");
	}

	if (!path.get().equals(configServiceAuthToken)) {

	    return buildErrorResponse(Status.UNAUTHORIZED, "Unrecognized authentication token");
	}

	String stringStream = null;
	try {

	    ServletInputStream inputStream = hsr.getInputStream();

	    stringStream = IOStreamUtils.asUTF8String(inputStream);

	} catch (IOException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	JSONObject requestObject = null;

	try {

	    requestObject = new JSONObject(stringStream);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    return buildErrorResponse(Status.METHOD_NOT_ALLOWED, "Missing or corrupted request body");
	}

	Optional<String> optRequestName = ConfigRequest.getName(requestObject);

	if (optRequestName.isEmpty()) {

	    return buildErrorResponse(Status.METHOD_NOT_ALLOWED, "Invalid request body. Missing request name");
	}

	String requestName = optRequestName.get();

	GSLoggerFactory.getLogger(getClass()).info("Serving '{}' request STARTED", requestName);

	Response response = null;

	if (requestName.equals(ConfigRequest.computeName(PutSourceRequest.class))) {

	    PutSourceRequest request = new PutSourceRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    response = validate.isPresent() ? validate.get() : handlePutSourceRequest(request);
	}

	if (requestName.equals(ConfigRequest.computeName(EditSourceRequest.class))) {

	    EditSourceRequest request = new EditSourceRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    response = validate.isPresent() ? validate.get() : handleEditSourceRequest(request);
	}

	if (requestName.equals(ConfigRequest.computeName(HarvestSchedulingRequest.class))) {

	    HarvestSchedulingRequest request = new HarvestSchedulingRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    response = validate.isPresent() ? validate.get() : handleHarvestSchedulingRequest(request);
	}

	if (requestName.equals(ConfigRequest.computeName(HarvestUnschedulingRequest.class))) {

	    HarvestUnschedulingRequest request = new HarvestUnschedulingRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    response = validate.isPresent() ? validate.get() : handleHarvestUnschedulingRequest(request);
	}

	if (requestName.equals(ConfigRequest.computeName(RemoveSourceRequest.class))) {

	    RemoveSourceRequest request = new RemoveSourceRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    response = validate.isPresent() ? validate.get() : handleRemoveSourceRequest(request);
	}

	if (requestName.equals(ConfigRequest.computeName(RemoveSourceDataRequest.class))) {

	    RemoveSourceDataRequest request = new RemoveSourceDataRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    response = validate.isPresent() ? validate.get() : handleRemoveSourceDataRequest(request);
	}

	if (requestName.equals(ConfigRequest.computeName(ListSourcesRequest.class))) {

	    ListSourcesRequest request = new ListSourcesRequest(requestObject);

	    response = handleListSourcesRequest(request);
	}

	GSLoggerFactory.getLogger(getClass()).info("Serving '{}' request ENDED", requestName);

	return response != null ? response : buildErrorResponse(Status.METHOD_NOT_ALLOWED, "Unknown request '" + requestName + "'");
    }

    /**
     * @param putSourceRequest
     * @return
     */
    private Response handlePutSourceRequest(PutSourceRequest putSourceRequest) {

	Optional<String> optSourceId = putSourceRequest.read(PutSourceRequest.SOURCE_ID).map(v -> v.toString());

	Optional<String> randomId = Optional.empty();

	//
	// source id presence check
	//

	if (optSourceId.isPresent()) {

	    String sourceId = optSourceId.get();

	    if (ConfigurationWrapper.getAllSources().//
		    stream().//
		    filter(s -> s.getUniqueIdentifier().equals(sourceId)).//
		    findFirst().//
		    isPresent()) {

		return buildErrorResponse(Status.BAD_REQUEST, "Source with id '" + sourceId + "' already exists");
	    }

	} else {

	    //
	    // random id creation
	    //

	    randomId = Optional.of(UUID.randomUUID().toString());

	    GSLoggerFactory.getLogger(getClass()).trace("Random id created:  {}", randomId);

	    putSourceRequest.put(PutSourceRequest.SOURCE_ID, randomId.get());
	}

	//
	// putting new harvesting setting and flushing
	//

	HarvestingSetting setting = HarvestingSettingUtils.build(putSourceRequest);

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	boolean put = configuration.put(setting);

	if (!put) {

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to put new source");

	} else {

	    Optional<Response> errorResponse = flush(configuration);

	    if (errorResponse.isPresent()) {

		return errorResponse.get();
	    }
	}

	//
	// optional harvesting scheduling
	//

	if (!putSourceRequest.readCompositeParameters().isEmpty()) {

	    HarvestSchedulingRequest harvestSchedulingRequest = new HarvestSchedulingRequest();

	    putSourceRequest.readNestedParameters().forEach(subParam -> {

		Object value = putSourceRequest.read(PutSourceRequest.HARVEST_SCHEDULING, subParam).get();

		harvestSchedulingRequest.put(subParam, value.toString());
	    });

	    harvestSchedulingRequest.put(PutSourceRequest.SOURCE_ID, putSourceRequest.read(PutSourceRequest.SOURCE_ID).get().toString());

	    handleHarvestSchedulingRequest(harvestSchedulingRequest);
	}

	//
	// in case of random id, it is provided in the response
	//

	if (randomId.isPresent()) {

	    JSONObject object = new JSONObject();
	    object.put(PutSourceRequest.SOURCE_ID, randomId.get());

	    return Response.status(Status.CREATED).//
		    entity(object.toString(3)).//
		    type(MediaType.APPLICATION_JSON.toString()).//
		    build();
	}

	//
	//
	//

	return Response.status(Status.CREATED).build();
    }

    /**
     * @param editSourceRequest
     * @return
     */
    private Response handleEditSourceRequest(EditSourceRequest editSourceRequest) {

	//
	// finding setting id from the given source id
	//

	SettingFinder finder = getFinder(editSourceRequest);

	if (finder.getErrorResponse().isPresent()) {

	    return finder.getErrorResponse().get();
	}

	//
	// building harvesting setting
	//

	String settingId = finder.getSetting().get().getIdentifier();

	HarvestingSetting setting = HarvestingSettingUtils.build(editSourceRequest, finder.getSetting().get());
	setting.setIdentifier(settingId);

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	//
	// replacing and flushing
	//

	boolean replaced = configuration.replace(setting);

	if (!replaced) {

	    return buildErrorResponse(Status.BAD_REQUEST, "No changes to apply");
	}

	//
	// flushing configuration
	//

	return flush(configuration).orElse(Response.status(Status.OK).build());
    }

    /**
     * @param harvestSourceRequest
     * @return
     */
    private Response handleHarvestSchedulingRequest(HarvestSchedulingRequest harvestSourceRequest) {

	//
	// finding setting id from the given source id
	//

	SettingFinder finder = getFinder(harvestSourceRequest);

	if (finder.getErrorResponse().isPresent()) {

	    return finder.getErrorResponse().get();
	}

	//
	// harvesting underway check in case of start harvesting now
	//

	Optional<Object> startTime = harvestSourceRequest.read(HarvestSchedulingRequest.START_TIME);

	String sourceId = harvestSourceRequest.read(PutSourceRequest.SOURCE_ID).map(v -> v.toString()).get();

	if (startTime.isEmpty() && isHarvestingUnderway(sourceId)) {

	    return buildErrorResponse(Status.BAD_REQUEST,
		    "Unable to start harvesting now since harvesting of the requested source is currently underway");
	}

	//
	// start time in the past check
	//

	if (startTime.isPresent()) {

	    String currentTime = ISO8601DateTimeUtils.getISO8601DateTime("Europe/Berlin");

	    if (startTime.get().toString().compareTo(currentTime) < 0) {

		return buildErrorResponse(Status.METHOD_NOT_ALLOWED, "The provided start time '" + startTime.get() + "' is in the past");
	    }
	}

	//
	// updating harvesting setting scheduling
	//

	String settingId = finder.getSetting().get().getIdentifier();

	HarvestingSetting setting = ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals(settingId)).//
		findFirst().get();

	setting = SettingUtils.downCast(SelectionUtils.resetAndSelect(setting, false), HarvestingSettingLoader.load().getClass());

	Scheduling scheduling = setting.getScheduling();

	HarvestingSettingUtils.udpate(harvestSourceRequest, scheduling);

	SelectionUtils.deepClean(setting);
	SelectionUtils.deepAfterClean(setting);

	//
	// replacing setting. if the operation fails, it means that the scheduling is not changed
	//

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	boolean replaced = configuration.replace(setting);

	if (replaced) {

	    Optional<Response> errorResponse = flush(configuration);

	    if (errorResponse.isPresent()) {

		return errorResponse.get();
	    }
	}

	//
	// scheduling or rescheduling
	//

	SchedulerSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

	try {
	    boolean contains = scheduler.//
		    listScheduledSettings().//
		    stream().//
		    map(s -> s.getIdentifier()).//
		    collect(Collectors.toList()).//
		    contains(settingId);

	    if (contains) {

		scheduler.reschedule(setting);

	    } else {

		scheduler.schedule(setting);
	    }
	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to schedule task: " + ex.getMessage());
	}

	//
	//
	//

	return Response.status(Status.OK).build();
    }

    /**
     * @param request
     * @return
     */
    private Response handleHarvestUnschedulingRequest(HarvestUnschedulingRequest harvestUnschedulingRequest) {

	//
	// finding setting id from the given source id
	//

	SettingFinder finder = getFinder(harvestUnschedulingRequest);

	if (finder.getErrorResponse().isPresent()) {

	    return finder.getErrorResponse().get();
	}

	//
	// unscheduling
	//

	SchedulerSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

	String settingId = finder.getSetting().get().getIdentifier();

	HarvestingSetting setting = ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals(settingId)).//
		findFirst().//
		get();

	try {

	    scheduler.pause(setting);

	} catch (SchedulerException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unscheduling failed: " + e.getMessage());
	}

	//
	// disabling scheduling and flushing configuration
	//

	setting.getScheduling().setEnabled(false);

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	boolean replaced = configuration.replace(setting);

	if (replaced) {

	    Optional<Response> errorResponse = flush(configuration);

	    if (errorResponse.isPresent()) {

		return errorResponse.get();
	    }
	}

	//
	//
	//

	return Response.status(Status.OK).build();
    }

    /**
     * @param removeSourceRequest
     * @return
     */
    private Response handleRemoveSourceRequest(RemoveSourceRequest removeSourceRequest) {

	//
	// finding setting id from the given source id
	//

	SettingFinder finder = getFinder(removeSourceRequest);

	if (finder.getErrorResponse().isPresent()) {

	    return finder.getErrorResponse().get();
	}

	//
	// harvesting underway check
	//

	String sourceId = removeSourceRequest.read(PutSourceRequest.SOURCE_ID).map(v -> v.toString()).get();

	if (isHarvestingUnderway(sourceId)) {

	    return buildErrorResponse(Status.BAD_REQUEST,
		    "The requested source is currently being harvested and cannot be removed until harvesting is complete");
	}

	String settingId = finder.getSetting().get().getIdentifier();

	//
	// unscheduling job
	//

	SchedulerSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

	HarvestingSetting harvestingSetting = ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals(settingId)).//
		findFirst().//
		get();

	try {

	    scheduler.unschedule(harvestingSetting);

	} catch (SchedulerException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unscheduling failed: " + e.getMessage());
	}

	//
	// removing setting and flushing configuration
	//

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	boolean removed = configuration.remove(settingId);

	if (!removed) {

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to remove source");

	} else {

	    Optional<Response> errorResponse = flush(configuration);

	    if (errorResponse.isPresent()) {

		return errorResponse.get();
	    }
	}

	//
	// removing source data
	//

	try {

	    removeSourceData(sourceId);

	} catch (GSException e) {

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to remove source data: " + e.getMessage());
	}

	//
	//
	//

	return Response.status(Status.OK).build();
    }

    /**
     * @param request
     * @return
     */
    private Response handleRemoveSourceDataRequest(RemoveSourceDataRequest request) {

	//
	// finding setting id from the given source id
	//

	SettingFinder finder = getFinder(request);

	if (finder.getErrorResponse().isPresent()) {

	    return finder.getErrorResponse().get();
	}

	//
	// harvesting underway check
	//

	String sourceId = request.read(PutSourceRequest.SOURCE_ID).map(v -> v.toString()).get();

	if (isHarvestingUnderway(sourceId)) {

	    return buildErrorResponse(Status.BAD_REQUEST,
		    "The requested source is currently being harvested and its data cannot be removed until harvesting is complete");
	}

	//
	// removing source data
	//

	try {

	    removeSourceData(sourceId);

	} catch (GSException e) {

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to remove source data: " + e.getMessage());
	}

	//
	//
	//

	return Response.status(Status.OK).build();
    }

    /**
     * @param request
     * @return
     */
    private Response handleListSourcesRequest(ListSourcesRequest request) {

	//
	// updating scheduling support
	//

	SchedulerSupport support = SchedulerSupport.getInstance();
	support.update();

	//
	// reading optional source ids
	//

	JSONArray out = new JSONArray();

	final List<String> sourceIds = new ArrayList<String>();

	Optional<Object> optional = request.read(ListSourcesRequest.SOURCE_ID);

	if (optional.isPresent()) {

	    sourceIds.addAll(Arrays.asList(optional.get().toString().split(",")).//
		    stream().//
		    map(v -> v.trim().strip()).//
		    collect(Collectors.toList()));

	    List<String> sources = ConfigurationWrapper.getHarvestedAndMixedSources().//
		    stream().//
		    map(s -> s.getUniqueIdentifier()).//
		    collect(Collectors.toList());

	    String missingSources = sourceIds.stream().filter(id -> !sources.contains(id)).collect(Collectors.joining(","));

	    if (missingSources != null && !missingSources.isEmpty()) {

		String message = missingSources.contains(",") ? "Sources with id '" + missingSources + "' not found"
			: "Source with id '" + missingSources + "' not found";

		return buildErrorResponse(Status.NOT_FOUND, message);
	    }
	}

	//
	//
	//

	List<HarvestingSetting> settings = ConfigurationWrapper.getHarvestingSettings();

	settings.forEach(setting -> {

	    AccessorSetting accessorSetting = setting.getSelectedAccessorSetting();

	    GSSource source = accessorSetting.getSource();

	    if (sourceIds.isEmpty() || sourceIds.contains(source.getUniqueIdentifier())) {

		JSONObject sourceObject = new JSONObject();
		out.put(sourceObject);

		sourceObject.put(PutSourceRequest.SOURCE_ID, source.getUniqueIdentifier());
		sourceObject.put(PutSourceRequest.SOURCE_LABEL, source.getLabel());
		sourceObject.put(PutSourceRequest.SOURCE_ENDPOINT, source.getEndpoint());
		sourceObject.put(PutSourceRequest.SERVICE_TYPE, accessorSetting.getAccessorType());

		JSONObject scheduling = new JSONObject();

		String jobPhase = support.getJobPhase(setting);
		if (!jobPhase.isEmpty()) {

		    scheduling.put("phase", jobPhase);
		}

		String elapsedTime = support.getElapsedTime(setting);
		if (!elapsedTime.isEmpty()) {

		    scheduling.put("elapsedTime", elapsedTime);
		}

		String endTime = support.getEndTime(setting);
		if (!endTime.isEmpty()) {

		    scheduling.put("endTime", endTime);
		}

		String firedTime = support.getFiredTime(setting);
		if (!firedTime.isEmpty()) {

		    scheduling.put("firedTime", firedTime);
		}

		String nextFireTime = support.getNextFireTime(setting);
		if (!nextFireTime.isEmpty()) {

		    scheduling.put("nextFireTime", nextFireTime);
		}

		String repeatCount = support.getRepeatCount(setting);
		if (!repeatCount.isEmpty()) {

		    scheduling.put("repeatCount", repeatCount);
		}

		String repeatInterval = support.getRepeatInterval(setting);
		if (!repeatInterval.isEmpty()) {

		    scheduling.put("repeatInterval", repeatInterval);
		}

		String size = support.getSize(setting);
		if (!size.isEmpty()) {

		    scheduling.put("size", size);
		}

		if (!scheduling.keySet().isEmpty()) {

		    sourceObject.put("scheduling", scheduling);
		}
	    }
	});

	return Response.status(Status.OK).//
		entity(out.toString(3)).//
		type(MediaType.APPLICATION_JSON.toString()).//
		build();
    }

    /**
     * @param sourceId
     * @throws GSException
     */
    private void removeSourceData(String sourceId) throws GSException {

	Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());

	Optional<DatabaseFolder> metaFolder = database.getMetaFolder(sourceId);

	if (metaFolder.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).trace("Removing meta folder '{}' STARTED", metaFolder.get().getName());

	    database.removeFolder(metaFolder.get().getName());

	    GSLoggerFactory.getLogger(getClass()).trace("Removing meta folder '{}' ENDED", metaFolder.get().getName());
	}

	List<DatabaseFolder> dataFolders = database.getDataFolders(sourceId);

	for (DatabaseFolder folder : dataFolders) {

	    GSLoggerFactory.getLogger(getClass()).trace("Removing data folder '{}' STARTED", folder.getName());

	    database.removeFolder(folder.getName());

	    GSLoggerFactory.getLogger(getClass()).trace("Removing data folder '{}' ENDED", folder.getName());
	}
    }

    /**
     * @param configuration
     * @return
     */
    private Optional<Response> flush(Configuration configuration) {

	try {

	    configuration.flush();

	    return Optional.empty();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    return Optional.of(buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to save changes: " + ex.getMessage()));
	}
    }

    /**
     * @param sourceId
     * @return
     */
    private boolean isHarvestingUnderway(String sourceId) {

	SchedulerSupport support = SchedulerSupport.getInstance();
	support.update();

	return ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getSelectedAccessorSetting().getSource().getUniqueIdentifier().equals(sourceId)).//
		filter(s -> support.getJobPhase(s).equals(JobPhase.RUNNING.getLabel())).//
		findFirst().//
		isPresent();

    }

    /**
     * @param status
     * @param message
     * @return
     */
    private Response buildErrorResponse(Status status, String message) {

	JSONObject object = new JSONObject();
	JSONObject error = new JSONObject();
	object.put("error", error);
	error.put("statusCode", status.getStatusCode());
	error.put("reasonPrase", status.toString());
	error.put("message", message);

	GSLoggerFactory.getLogger(getClass()).error(message);

	return Response.status(status).//
		entity(object.toString(3)).//
		type(MediaType.APPLICATION_JSON).//
		build();
    }

    /**
     * @param request
     * @return
     */
    private Optional<Response> validate(ConfigRequest request) {

	try {

	    request.validate();

	    return Optional.empty();

	} catch (IllegalArgumentException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());

	    return Optional.of(buildErrorResponse(Status.METHOD_NOT_ALLOWED, ex.getMessage()));
	}
    }

    /**
     * @author Fabrizio
     */
    private class SettingFinder {

	private HarvestingSetting setting;
	private Response errorResponse;

	/**
	 * @param setting
	 */
	private SettingFinder(HarvestingSetting setting) {

	    this.setting = setting;
	}

	/**
	 * @param response
	 */
	private SettingFinder(Response response) {

	    errorResponse = response;
	}

	/**
	 * @return
	 */
	public Optional<HarvestingSetting> getSetting() {

	    return Optional.ofNullable(setting);
	}

	/**
	 * @return
	 */
	public Optional<Response> getErrorResponse() {

	    return Optional.ofNullable(errorResponse);
	}
    }

    /**
     * @param request
     * @return
     */
    private SettingFinder getFinder(ConfigRequest request) {

	Optional<String> optSourceId = request.read(PutSourceRequest.SOURCE_ID).map(v -> v.toString());

	HarvestingSetting setting = null;

	if (!optSourceId.isPresent()) {

	    return new SettingFinder(buildErrorResponse(Status.METHOD_NOT_ALLOWED, "Missing source identifier"));

	} else {

	    String sourceId = optSourceId.get();

	    if (!ConfigurationWrapper.getAllSources().//
		    stream().//
		    filter(s -> s.getUniqueIdentifier().equals(sourceId)).//
		    findFirst().//
		    isPresent()) {

		return new SettingFinder(buildErrorResponse(Status.NOT_FOUND, "Source with id '" + sourceId + "' not found"));
	    }

	    setting = ConfigurationWrapper.getHarvestingSettings().//
		    stream().//
		    filter(s -> s.getSelectedAccessorSetting().getSource().getUniqueIdentifier().equals(sourceId)).//
		    findFirst().//
		    get();
	}

	return new SettingFinder(setting);
    }
}
