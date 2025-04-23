/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

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

import org.json.JSONObject;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;

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
    @Path("source")
    @Produces(MediaType.APPLICATION_JSON)
    public Response source(@Context HttpServletRequest hsr, @Context UriInfo uriInfo) {

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

	Optional<String> optRequestName = ConfigRequest.getRequestName(requestObject);

	if (optRequestName.isEmpty()) {

	    return buildErrorResponse(Status.BAD_REQUEST, "Invalid request body. Missing request name");
	}

	String requestName = optRequestName.get();

	if (requestName.equals(PutSourceRequest.class.getSimpleName())) {

	    PutSourceRequest request = new PutSourceRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    return validate.isPresent() ? validate.get() : handlePutSourceRequest(request);
	}

	if (requestName.equals(EditSourceRequest.class.getSimpleName())) {

	    EditSourceRequest request = new EditSourceRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    return validate.isPresent() ? validate.get() : handleEditSourceRequest(request);
	}

	if (requestName.equals(HarvestSourceRequest.class.getSimpleName())) {

	    HarvestSourceRequest request = new HarvestSourceRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    return validate.isPresent() ? validate.get() : handleHarvestSourceRequest(request);
	}

	if (requestName.equals(RemoveSourceRequest.class.getSimpleName())) {

	    RemoveSourceRequest request = new RemoveSourceRequest(requestObject);

	    Optional<Response> validate = validate(request);

	    return validate.isPresent() ? validate.get() : handleRemoveSourceRequest(request);
	}

	return buildErrorResponse(Status.BAD_REQUEST, "Unknown request '" + requestName + "'");
    }

    /**
     * @param putSourceRequest
     * @return
     */
    private Response handlePutSourceRequest(PutSourceRequest putSourceRequest) {

	Optional<String> optSourceId = putSourceRequest.read(PutSourceRequest.SOURCE_ID).map(v -> v.toString());

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

	    putSourceRequest.put(PutSourceRequest.SOURCE_ID, UUID.randomUUID().toString());
	}

	HarvestingSetting setting = HarvestingSettingBuilder.build(putSourceRequest);

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

	return Response.status(Status.CREATED).//
		entity(putSourceRequest.toString()).//
		type(MediaType.APPLICATION_JSON.toString()).//
		build();
    }

    /**
     * @param editSourceRequest
     * @return
     */
    private Response handleEditSourceRequest(EditSourceRequest editSourceRequest) {

	Optional<String> optSourceId = editSourceRequest.read(EditSourceRequest.SOURCE_ID).map(v -> v.toString());

	String settingId = null;

	if (!optSourceId.isPresent()) {

	    return buildErrorResponse(Status.BAD_REQUEST, "Missing source identifier");

	} else {

	    String sourceId = optSourceId.get();

	    if (!ConfigurationWrapper.getAllSources().//
		    stream().//
		    filter(s -> s.getUniqueIdentifier().equals(sourceId)).//
		    findFirst().//
		    isPresent()) {

		return buildErrorResponse(Status.BAD_REQUEST, "Source with id '" + sourceId + "' no not exists");
	    }

	    settingId = ConfigurationWrapper.getHarvestingSettings().//
		    stream().//
		    filter(s -> s.getSelectedAccessorSetting().getSource().getUniqueIdentifier().equals(sourceId)).//
		    findFirst().//
		    get().//
		    getIdentifier();
	}

	HarvestingSetting setting = HarvestingSettingBuilder.build(editSourceRequest);
	setting.setIdentifier(settingId);

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	boolean replaced = configuration.replace(setting);

	if (!replaced) {

	    return buildErrorResponse(Status.NOT_MODIFIED, "Unable to edit source");

	} else {

	    try {

		configuration.flush();

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);

		return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "Unable to save changes: " + ex.getMessage());
	    }
	}

	return Response.status(Status.OK).//
		entity(editSourceRequest.toString()).//
		type(MediaType.APPLICATION_JSON.toString()).//
		build();
    }

    /**
     * @param harvestSourceRequest
     * @return
     */
    private Response handleHarvestSourceRequest(HarvestSourceRequest harvestSourceRequest) {

	return Response.status(Status.OK).build();
    }

    /**
     * @param removeSourceRequest
     * @return
     */
    private Response handleRemoveSourceRequest(RemoveSourceRequest removeSourceRequest) {

	return Response.status(Status.OK).build();
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
}
