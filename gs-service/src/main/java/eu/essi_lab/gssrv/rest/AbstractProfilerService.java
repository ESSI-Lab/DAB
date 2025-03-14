package eu.essi_lab.gssrv.rest;

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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.pdk.ChronometerInfoProvider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.ResponseInfoProvider;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchInfoPublisher;

/**
 * Subclasses provides a concrete implementation of a JAX-RS/JAX-WS service
 * provider which is in charge to serve one or more {@link WebRequest}s by
 * selecting the suitable {@link Profiler}
 *
 * @author Fabrizio
 * @see Profiler
 * @see ProfilerSettingFilter
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractProfilerService {

    /**
     * 
     */
    private static final HashMap<String, Class<? extends Profiler>> PROFILERS_MAP = new HashMap<>();

    static {

	PluginsLoader<Profiler> pluginsLoader = new PluginsLoader<>();
	List<Profiler> profilers = pluginsLoader.loadPlugins(Profiler.class);

	for (Profiler profiler : profilers) {

	    PROFILERS_MAP.put(profiler.getType(), profiler.getClass());
	}
    }

    /**
     * Creates a {@link WebRequest} from the given arguments, selects the suitable
     * {@link Profiler} basing on the given <code>strategy</code> and serves the
     * request by delegating to the selected {@link Profiler}
     *
     * @param strategy
     * @param httpServletRequest
     * @param uriInfo
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Response serve(ProfilerSettingFilter strategy, HttpServletRequest httpServletRequest, UriInfo uriInfo) {

	ChronometerInfoProvider chronometer = new ChronometerInfoProvider(TimeFormat.MIN_SEC_MLS);
	chronometer.start();

	Optional<Profiler> optProfiler = Optional.empty();
	Response response = null;

	WebRequest webRequest = new WebRequest(Thread.currentThread().getName());

	webRequest.setUriInfo(uriInfo);

	GSException ex = null;

	try {
	    webRequest.setServletRequest(httpServletRequest);

	} catch (IOException thr) {

	    ex = GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "ServletRequestError", //
		    thr);
	}

	if (ex == null) {

	    Optional<ProfilerSetting> profilerSetting = ConfigurationWrapper.//
		    getProfilerSettings().//
		    stream().//
		    filter(strategy::accept).//
		    findFirst();

	    if (profilerSetting.isPresent()) {

		try {

		    optProfiler = Optional
			    .of(PROFILERS_MAP.get(profilerSetting.get().getConfigurableType()).getDeclaredConstructor().newInstance());

		} catch (Exception e) {

		    ex = GSException.createException(//
			    getClass(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    "ProfilerConfigurableCreationError", //
			    e);
		}
	    }

	    if (optProfiler.isEmpty()) {

		if (ex == null) {

		    ex = GSException.createException(//
			    getClass(), //
			    "No profiler listening at this URL: " + uriInfo.getPath(), //
			    "No service listening at this URL: " + uriInfo.getPath(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    "NoProfilerListeningError");
		}
	    } else {

		Profiler profiler = optProfiler.get();

		webRequest.setProfilerName(profiler.getName());
		webRequest.setProfilerPath(profiler.getSetting().getServicePath());

		try {

		    //
		    // configures the profiler
		    //

		    profiler.configure(profilerSetting.get());

		    //
		    // handles the request
		    //

		    response = profiler.handle(webRequest);

		} catch (GSException gsEx) {

		    ex = gsEx;

		} catch (Exception thr) {

		    ex = GSException.createException(//
			    getClass(), //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    "ProfilerServeError", //
			    thr);
		}
	    }
	}

	if (ex != null) {

	    ex.log();

	    if (optProfiler.isPresent()) {

		response = optProfiler.get().createUncaughtError(webRequest, Status.INTERNAL_SERVER_ERROR,
			ExceptionUtils.getStackTrace(ex));

	    } else {

		JSONObject error = new JSONObject();
		error.put("Service not found", "No service listening at this URL");

		response = Response.serverError().//
			status(Status.NOT_FOUND).//
			entity(error.toString(3)).//
			type(MediaType.APPLICATION_JSON).//
			build();
	    }
	}

	Optional<ElasticsearchInfoPublisher> optPublisher = ElasticsearchInfoPublisher.create(webRequest);
	if (optPublisher.isPresent()) {
	    try {
		ElasticsearchInfoPublisher p = optPublisher.get();
		p.publish(chronometer);
		p.publish(webRequest);
		p.publish(new ResponseInfoProvider(response));
		p.write();

	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	return response;
    }
}
