package eu.essi_lab.profiler.arpa;

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
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;

/**
 * @author boldrini
 */
public class HydroCSVProfiler extends Profiler<HydroCSVProfilerSetting> {

  
    public HydroCSVProfiler() {
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	DiscoveryHandler<String> discoveryHandler = new DiscoveryHandler<String>();
	discoveryHandler.setRequestTransformer(new HydroCSVRequestTransformer());
	discoveryHandler.setMessageResponseMapper(new HydroCSVResultSetMapper());
	discoveryHandler.setMessageResponseFormatter(new HydroCSVResultSetFormatter());
	selector.register(new HydroCSVTimeSeriesFilter(), discoveryHandler);
	
	HydroCSVViewsHandler viewsHandler = new HydroCSVViewsHandler();
	selector.register(new HydroCSVViewsFilter(), viewsHandler);

	return selector;
    }

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

	return createUncaughtError(webRequest, Status.BAD_REQUEST, "Unsupported HydroCSV request");
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
    protected HydroCSVProfilerSetting initSetting() {

	return new HydroCSVProfilerSetting();
    }
}
