package eu.essi_lab.gssrv.rest;

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

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;

import eu.essi_lab.gssrv.rest.exceptions.GSErrorMessage;
import eu.essi_lab.gssrv.rest.exceptions.GSServiceGSExceptionHandler;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.request.executor.discover.QueryInitializer;
public abstract class AbstractProfilerService {

    public static final String ERR_ID_PROFILER_ALIEN_ERROR = "ERR_ID_PROFILER_ALIEN_ERROR";
    public static final String NO_PROFILER = "NO_PROFILER";

    /**
     * Creates a {@link WebRequest} from the given arguments, selects the suitable {@link Profiler} basing on
     * the given
     * <code>strategy</code> and serves the request by delegating to the selected {@link Profiler}
     *
     * @param strategy
     * @param httpServletRequest
     * @param uriInfo
     * @return
     */
    protected Response serve(ProfilerFilter strategy, HttpServletRequest httpServletRequest, UriInfo uriInfo) {

	Optional<Profiler> profiler = null;
	Response response = null;
	GSServiceGSExceptionHandler exHandler = null;

	WebRequest webRequest = new WebRequest();

	PluginsLoader<Profiler> pluginsLoader = new PluginsLoader<>();
	List<Profiler> profilers = pluginsLoader.loadPlugins(Profiler.class);

	profiler = profilers.stream().filter(strategy::accept).findFirst();

	if (!profiler.isPresent()) {
	    GSException ex = GSException.createException(//
		    getClass(), //
		    "No profiler listening at this URL", //
		    "No service listening at this URL", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    NO_PROFILER);

	    exHandler = new GSServiceGSExceptionHandler(new DefaultGSExceptionReader(ex));
	} else {

	    webRequest.setProfilerPath(profiler.get().getProfilerInfo().getServicePath());
	    // webRequest.setServicesPath(SERVICES_PATH);
	    webRequest.setUriInfo(uriInfo);

	    try {

		webRequest.setServletRequest(httpServletRequest);

		// String rid = webRequest.getRequestId();

		// Thread.currentThread().setName("req-" + rid);

		response = profiler.get().handle(webRequest);

	    } catch (GSException ex) {

		exHandler = new GSServiceGSExceptionHandler(new DefaultGSExceptionReader(ex));

	    } catch (Exception thr) {

		GSException ex = GSException.createException(//
			getClass(), //
			thr.getMessage(), //
			thr.getMessage(), //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			ERR_ID_PROFILER_ALIEN_ERROR, //
			thr);

		exHandler = new GSServiceGSExceptionHandler(new DefaultGSExceptionReader(ex));
	    }
	}

	if (exHandler != null) {
	    Status status = exHandler.getStatus();
	    GSErrorMessage gsMessage = exHandler.getErrorMessageForUser();

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(exHandler.getReader()));

	    // view not found is a special error (profiler-independent)
	    if (gsMessage.getCode().contains(QueryInitializer.VIEW_NOT_FOUND)) {
		JSONObject error = new JSONObject();
		error.put("view not found", "The specified view was not found");
		return Response.serverError().//
			status(Status.NOT_FOUND).//
			entity(error.toString(3)).//
			type(MediaType.APPLICATION_JSON).//
			build();
	    }

	    if (profiler.isPresent()) {
		response = profiler.get().createUncaughtError(webRequest, status, gsMessage.getMessageAndCode());
	    } else {
		JSONObject error = new JSONObject();
		error.put("service not found", "No service listening at this URL");

		return Response.serverError().//
			status(Status.NOT_FOUND).//
			entity(error.toString(3)).//
			type(MediaType.APPLICATION_JSON).//
			build();
	    }
	}

	return response;
    }
}
