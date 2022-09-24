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

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceContext;

import eu.essi_lab.gssrv.health.HealthCheck;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author ilsanto
 */
@WebService
@Path("/")
public class HealthCheckService {

    /**
     * 
     */
    private static Boolean lastResponse;
    private static HealthCheck lastChecker;

    private static final Timer RESPONSE_TIMER = new Timer();
    static {

	RESPONSE_TIMER.scheduleAtFixedRate(new TimerTask() {

	    @Override
	    public void run() {

		synchronized (RESPONSE_TIMER) {

		    lastResponse = null;
		}
	    }
	}, //
		TimeUnit.MINUTES.toMillis(2), //
		TimeUnit.MINUTES.toMillis(2));

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/check")
    public Response health(@Context HttpServletRequest hsr, @Context UriInfo uriInfo, @Context WebServiceContext wscontext) {

	synchronized (RESPONSE_TIMER) {

	    if (lastResponse == null) {

		String id = UUID.randomUUID().toString();

//		GSLoggerFactory.getLogger(getClass()).info("[HEALTH_CHECK] {} STARTED", id);

		lastChecker = new HealthCheck();

		lastResponse = lastChecker.isHealthy();

		if(!lastResponse){
		    
		    GSLoggerFactory.getLogger(getClass()).info("[HEALTH_CHECK] {} FAILED", id);
		}
		
//		GSLoggerFactory.getLogger(getClass()).info("[HEALTH_CHECK] {} ENDED: {}", id, lastResponse);
	    }
	}

	return Response.//
		status(responseStatus(lastResponse)).//
		type(MediaType.APPLICATION_JSON_TYPE).//
		entity(lastChecker.toJson(lastResponse, false)).build();
    }

    HealthCheck newChecker() {
	return new HealthCheck();
    }

    Response.Status responseStatus(boolean healthy) {

	if (healthy) {
	    return Response.Status.OK;
	}

	return Response.Status.INTERNAL_SERVER_ERROR;
    }
}
