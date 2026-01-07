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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceContext;

import eu.essi_lab.gssrv.health.AvailableDiskSpaceChecker;
import eu.essi_lab.gssrv.health.HealthCheck;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.pdk.ChronometerInfoProvider;
import eu.essi_lab.rip.RuntimeInfoProvider;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchInfoPublisher;

/**
 * @author Fabrizio
 */
@WebService
@Path("/")
public class HealthCheckService implements RuntimeInfoProvider {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/check")
    public Response health(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @Context WebServiceContext wscontext) {

	WebRequest webRequest = new WebRequest();

	webRequest.setUriInfo(uriInfo);

	try {
	    webRequest.setServletRequest(hsr);

	} catch (IOException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	Optional<ElasticsearchInfoPublisher> publisher = ElasticsearchInfoPublisher.create(webRequest);

	ChronometerInfoProvider chronometer = new ChronometerInfoProvider(TimeFormat.MIN_SEC_MLS);
	chronometer.start();

	HealthCheck healthCheck = new HealthCheck();

	boolean healthy = new HealthCheck().isHealthy();

	//
	// This class is dedicated to check if the temporary directory has at least 1GB of available disk space.
	// If it's not the case, an alarm mail is sent
	//
	AvailableDiskSpaceChecker.check();

	Status status = responseStatus(healthy);
	Response response = Response.//
		status(status).//
		type(MediaType.APPLICATION_JSON_TYPE).//
		entity(healthCheck.toJson(healthy, false)).build();

	String elapsedTime = chronometer.formatElapsedTime();
	GSLoggerFactory.getLogger(getClass()).trace("HealthCheck elapsed time: {}", elapsedTime);

	if (publisher.isPresent()) {

	    try {

		publisher.get().publish(webRequest);
		publisher.get().publish(chronometer);
		publisher.get().publish(new RuntimeInfoProvider() {

		    @Override
		    public HashMap<String, List<String>> provideInfo() {
			HashMap<String, List<String>> ret = new HashMap<String, List<String>>();
			ret.put(RuntimeInfoElement.RESPONSE_STATUS.getName(), Arrays.asList("" + status.getStatusCode()));
			return ret;
		    }

		    @Override
		    public String getName() {
			return getBaseType();
		    }
		});
		publisher.get().write();

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	}

	return response;
    }

    /**
     * @param healthy
     * @return
     */
    private Response.Status responseStatus(boolean healthy) {

	if (healthy) {
	    return Response.Status.OK;
	}

	return Response.Status.INTERNAL_SERVER_ERROR;
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	return new HashMap<>();
    }

    @Override
    public String getName() {

	return getClass().getSimpleName();
    }

    @Override
    public String getBaseType() {

	return "HealthCheck";
    }
}
