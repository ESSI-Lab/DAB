package eu.essi_lab.profiler.rest.views;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.DELETERequestFilter;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.handler.selector.POSTRequestFilter;
import eu.essi_lab.pdk.handler.selector.PUTRequestFilter;

/**
 * @author boldrini
 */
public class RestViewsProfiler extends Profiler {

    public static final ProfilerSetting REST_VIEWS_SERVICE_INFO = new ProfilerSetting();
    static {
	REST_VIEWS_SERVICE_INFO.setServiceName("REST-VIEWS");
	REST_VIEWS_SERVICE_INFO.setServiceType("REST-VIEWS");
	REST_VIEWS_SERVICE_INFO.setServicePath("rest-views");
	REST_VIEWS_SERVICE_INFO.setServiceVersion("1.0.0");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// -------------------------
	//
	// Sources
	//
	selector.register(//
		new GETRequestFilter("rest-views/sources"), //
		new RestViewsSourcesHandler());

	// -------------------------
	//
	// Views
	//
	selector.register(//
		new GETRequestFilter("rest-views/views"), //
		new RestViewsHandler());
	selector.register(//
		new GETRequestFilter("rest-views/views/*"), //
		new RestViewsHandler());
	selector.register(//
		new POSTRequestFilter("rest-views/views"), //
		new RestViewsHandler());
	selector.register(//
		new POSTRequestFilter("rest-views/views/*"), //
		new RestViewsHandler());
	selector.register(//
		new PUTRequestFilter("rest-views/views"), //
		new RestViewsHandler());
	selector.register(//
		new PUTRequestFilter("rest-views/views/*"), //
		new RestViewsHandler());
	selector.register(//
		new DELETERequestFilter("rest-views/views/*"), //
		new RestViewsHandler());

	return selector;
    }

    @Override
    /**
     * @param status
     * @param message
     * @return
     */
    public Response createUncaughtError(WebRequest request, Status status, String message) {

	return Response.status(status).type(MediaType.TEXT_PLAIN).entity(message).build();
    }

    /**
     * This cannot happens, the transformer checks the request
     */
    protected Response onHandlerNotFound(WebRequest webRequest) {

	return createUncaughtError(webRequest, Status.BAD_REQUEST, "Unsupported rest-views request");
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message.getError() + " " + message.getErrorCode())
		.build();
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected ProfilerSetting initSetting() {

	return REST_VIEWS_SERVICE_INFO;
    }
}
