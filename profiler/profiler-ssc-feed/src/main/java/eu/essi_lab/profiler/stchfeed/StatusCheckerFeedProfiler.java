/**
 * 
 */
package eu.essi_lab.profiler.stchfeed;

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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;

/**
 * @author Fabrizio
 */
public class StatusCheckerFeedProfiler extends Profiler<StatusCheckerFeedProfilerSetting> {

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	selector.register(//
		new GETRequestFilter(new StatusCheckerFeedProfilerSetting().getServicePath() + "/feed"), //
		new StatusCheckerFeedHandler());

	return selector;
    }

    @Override
    public Response createUncaughtError(WebRequest request, Status status, String message) {

	ResponseBuilder builder = Response.status(status);

	builder.entity(createXMLError(status, message));
	builder.type(MediaType.APPLICATION_XML_TYPE);

	return builder.build();
    }

    private String createXMLError(Status status, String message) {

	String error = "<gs:error xmlns:gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\">";
	error += "<gs:status>" + status + "</gs:status>";
	error += "<gs:message>" + message + "</gs:message>";
	error += "</gs:error>";

	return error;
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	return null;
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	return createUncaughtError(request, Status.BAD_REQUEST, "Unknonwn request");
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected StatusCheckerFeedProfilerSetting initSetting() {

	return new StatusCheckerFeedProfilerSetting();
    }
}
