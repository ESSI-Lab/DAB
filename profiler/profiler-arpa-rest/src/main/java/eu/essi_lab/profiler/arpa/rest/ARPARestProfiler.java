package eu.essi_lab.profiler.arpa.rest;

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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.DELETERequestFilter;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.handler.selector.POSTRequestFilter;

/**
 * @author boldrini
 */
public class ARPARestProfiler extends Profiler<ARPARestProfilerSetting> {

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// -------------------------
	//
	// Sources
	//
	selector.register(//
		new GETRequestFilter("arpa-rest/sources"), //
		new ARPARESTSourcesHandler());

	// -------------------------
	//
	// Views
	//
	selector.register(//
		new GETRequestFilter("arpa-rest/whos/views"), //
		new ARPARESTViewsHandler());
	selector.register(//
		new GETRequestFilter("arpa-rest/whos/views/*"), //
		new ARPARESTViewsHandler());
	selector.register(//
		new POSTRequestFilter("arpa-rest/whos/views"), //
		new ARPARESTViewsHandler());
	selector.register(//
		new DELETERequestFilter("arpa-rest/whos/views/*"), //
		new ARPARESTViewsHandler());

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

	return createUncaughtError(webRequest, Status.BAD_REQUEST, "Unsupported ARPA-REST request");
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
    protected ARPARestProfilerSetting initSetting() {

	return new ARPARestProfilerSetting();
    }
}
