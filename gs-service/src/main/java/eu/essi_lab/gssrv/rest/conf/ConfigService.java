/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
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
import eu.essi_lab.gssrv.rest.conf.requests.ListSourcesRequest;
import eu.essi_lab.gssrv.rest.conf.requests.PutSourceRequest;
import eu.essi_lab.gssrv.rest.conf.requests.RemoveSourceRequest;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.model.GSSource;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
@Path("/")
public class ConfigService {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response config(@Context HttpServletRequest hsr, @Context UriInfo uriInfo) {

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

	    return buildErrorResponse(Status.BAD_REQUEST, "Missing or corrupted request body");
	}

	Optional<String> optRequestName = ConfigRequest.getName(requestObject);

	if (optRequestName.isEmpty()) {

	    return buildErrorResponse(Status.BAD_REQUEST, "Invalid request body. Missing request name");
	}

	String requestName = optRequestName.get();

	if (requestName.equals(ConfigRequest.computeName(PutSourceRequest.class))) {

	    PutSourceRequest request = new PutSourceRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    return validate.isPresent() ? validate.get() : handlePutSourceRequest(request);
	}

	if (requestName.equals(ConfigRequest.computeName(EditSourceRequest.class))) {

	    EditSourceRequest request = new EditSourceRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    return validate.isPresent() ? validate.get() : handleEditSourceRequest(request);
	}

	if (requestName.equals(ConfigRequest.computeName(HarvestSchedulingRequest.class))) {

	    HarvestSchedulingRequest request = new HarvestSchedulingRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    return validate.isPresent() ? validate.get() : handleHarvestSourceRequest(request);
	}

	if (requestName.equals(ConfigRequest.computeName(RemoveSourceRequest.class))) {

	    RemoveSourceRequest request = new RemoveSourceRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    return validate.isPresent() ? validate.get() : handleRemoveSourceRequest(request);
	}

	if (requestName.equals(ConfigRequest.computeName(ListSourcesRequest.class))) {

	    ListSourcesRequest request = new ListSourcesRequest(requestObject);

	    return handleListSourcesRequest(request);
	}

	return buildErrorResponse(Status.BAD_REQUEST, "Unknown request '" + requestName + "'");
    }

    /**
     * @param putSourceRequest
     * @return
     */
    private Response handlePutSourceRequest(PutSourceRequest putSourceRequest) {

	Optional<String> optSourceId = putSourceRequest.read(PutSourceRequest.SOURCE_ID).map(v -> v.toString());

	Optional<String> randomId = Optional.empty();

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

	    randomId = Optional.of(UUID.randomUUID().toString());

	    putSourceRequest.put(PutSourceRequest.SOURCE_ID, randomId.get());
	}

	HarvestingSetting setting = HarvestingSettingUtils.build(putSourceRequest);

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	boolean put = configuration.put(setting);

	if (!put) {

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to put new source");

	} else {

	    try {

		configuration.flush();

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);

		return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to save changes: " + ex.getMessage());
	    }
	}

	if (!putSourceRequest.readNestedRootParameters().isEmpty()) {

	    HarvestSchedulingRequest harvestSchedulingRequest = new HarvestSchedulingRequest();

	    putSourceRequest.readNestedParameters().forEach(subParam -> {

		Object value = putSourceRequest.read(PutSourceRequest.HARVEST_SCHEDULING, subParam).get();

		harvestSchedulingRequest.put(subParam, value.toString());
	    });

	    harvestSchedulingRequest.put(PutSourceRequest.SOURCE_ID, putSourceRequest.read(PutSourceRequest.SOURCE_ID).get().toString());

	    handleHarvestSourceRequest(harvestSchedulingRequest);
	}

	if (randomId.isPresent()) {

	    JSONObject object = new JSONObject();
	    object.put(PutSourceRequest.SOURCE_ID, randomId.get());

	    return Response.status(Status.CREATED).//
		    entity(object.toString(3)).//
		    type(MediaType.APPLICATION_JSON.toString()).//
		    build();
	}

	return Response.status(Status.CREATED).//
		type(MediaType.APPLICATION_JSON.toString()).//
		build();
    }

    /**
     * @param editSourceRequest
     * @return
     */
    private Response handleEditSourceRequest(EditSourceRequest editSourceRequest) {

	SettingIdHolder holder = getHolder(editSourceRequest);

	if (holder.getErrorResponse().isPresent()) {

	    return holder.getErrorResponse().get();
	}

	String settingId = holder.getSettingId().get();

	HarvestingSetting setting = HarvestingSettingUtils.build(editSourceRequest);
	setting.setIdentifier(settingId);

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	boolean replaced = configuration.replace(setting);

	if (!replaced) {

	    return buildErrorResponse(Status.NOT_MODIFIED, "Source not modified");

	} else {

	    try {

		configuration.flush();

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);

		return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to save changes: " + ex.getMessage());
	    }
	}

	return Response.status(Status.OK).//
		type(MediaType.APPLICATION_JSON.toString()).//
		build();
    }

    /**
     * @param harvestSourceRequest
     * @return
     */
    private Response handleHarvestSourceRequest(HarvestSchedulingRequest harvestSourceRequest) {

	SettingIdHolder holder = getHolder(harvestSourceRequest);

	if (holder.getErrorResponse().isPresent()) {

	    return holder.getErrorResponse().get();
	}

	//
	//
	//

	Optional<Object> startTime = harvestSourceRequest.read(HarvestSchedulingRequest.START_TIME);

	String sourceId = harvestSourceRequest.read(PutSourceRequest.SOURCE_ID).map(v -> v.toString()).get();

	if (startTime.isEmpty() && isHarvestingUnderway(sourceId)) {

	    return buildErrorResponse(Status.BAD_REQUEST, "Unable to start harvesting now since harvesting of the requested source is currently underway");
	}

	//
	//
	//

	String settingId = holder.getSettingId().get();

	HarvestingSetting setting = ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals(settingId)).//
		findFirst().get();

	setting = SettingUtils.downCast(SelectionUtils.resetAndSelect(setting, false), HarvestingSettingLoader.load().getClass());

	Scheduling scheduling = setting.getScheduling();

	boolean reschedule = scheduling.isEnabled();

	HarvestingSettingUtils.udpate(harvestSourceRequest, scheduling);

	SelectionUtils.deepClean(setting);
	SelectionUtils.deepAfterClean(setting);

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	boolean replaced = configuration.replace(setting);

	if (!replaced) {

	    return buildErrorResponse(Status.BAD_REQUEST, "No changes to apply");

	} else {

	    try {

		configuration.flush();

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);

		return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to save changes: " + ex.getMessage());
	    }
	}

	//
	//
	//

	SchedulerSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

	try {

	    if (reschedule) {

		scheduler.reschedule(setting);

	    } else {

		scheduler.schedule(setting);
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to schedule task: " + ex.getMessage());
	}

	return Response.status(Status.OK).//
		type(MediaType.APPLICATION_JSON.toString()).//
		build();
    }

    /**
     * @param removeSourceRequest
     * @return
     */
    private Response handleRemoveSourceRequest(RemoveSourceRequest removeSourceRequest) {

	SettingIdHolder holder = getHolder(removeSourceRequest);

	if (holder.getErrorResponse().isPresent()) {

	    return holder.getErrorResponse().get();
	}

	//
	//
	//

	String sourceId = removeSourceRequest.read(PutSourceRequest.SOURCE_ID).map(v -> v.toString()).get();

	if (isHarvestingUnderway(sourceId)) {

	    return buildErrorResponse(Status.BAD_REQUEST,
		    "The requested source is currently being harvested and cannot be removed until harvest is complete");
	}

	String settingId = holder.getSettingId().get();

	//
	//
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

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Job unscheduling failed: " + e.getMessage());
	}

	//
	//
	//

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	boolean removed = configuration.remove(settingId);

	if (!removed) {

	    return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to remove source");
	}

	return Response.status(Status.OK).build();
    }

    /**
     * @param request
     * @return
     */
    private Response handleListSourcesRequest(ListSourcesRequest request) {

	SchedulerSupport support = SchedulerSupport.getInstance();
	support.update();

	List<HarvestingSetting> settings = ConfigurationWrapper.getHarvestingSettings();

	JSONArray out = new JSONArray();

	final List<String> sourceIds = new ArrayList<String>();

	Optional<Object> optional = request.read(ListSourcesRequest.SOURCE_ID);
	if (optional.isPresent()) {

	    sourceIds.addAll(Arrays.asList(optional.get().toString().split(",")).//
		    stream().//
		    map(v -> v.trim().strip()).//
		    collect(Collectors.toList()));
	}

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

	return Response.status(Status.BAD_REQUEST).//
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

	    return Optional.of(buildErrorResponse(Status.BAD_REQUEST, ex.getMessage()));
	}
    }

    /**
     * @author Fabrizio
     */
    private class SettingIdHolder {

	private String settingId;
	private Response errorResponse;

	/**
	 * @param settingId
	 */
	private SettingIdHolder(String settingId) {

	    this.settingId = settingId;
	}

	/**
	 * @param response
	 */
	private SettingIdHolder(Response response) {

	    errorResponse = response;
	}

	/**
	 * @return
	 */
	public Optional<String> getSettingId() {

	    return Optional.ofNullable(settingId);
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
    private SettingIdHolder getHolder(ConfigRequest request) {

	Optional<String> optSourceId = request.read(PutSourceRequest.SOURCE_ID).map(v -> v.toString());

	String settingId = null;

	if (!optSourceId.isPresent()) {

	    return new SettingIdHolder(buildErrorResponse(Status.BAD_REQUEST, "Missing source identifier"));

	} else {

	    String sourceId = optSourceId.get();

	    if (!ConfigurationWrapper.getAllSources().//
		    stream().//
		    filter(s -> s.getUniqueIdentifier().equals(sourceId)).//
		    findFirst().//
		    isPresent()) {

		return new SettingIdHolder(buildErrorResponse(Status.NOT_FOUND, "Source with id '" + sourceId + "' do not exists"));
	    }

	    settingId = ConfigurationWrapper.getHarvestingSettings().//
		    stream().//
		    filter(s -> s.getSelectedAccessorSetting().getSource().getUniqueIdentifier().equals(sourceId)).//
		    findFirst().//
		    get().//
		    getIdentifier();
	}

	return new SettingIdHolder(settingId);
    }
}
