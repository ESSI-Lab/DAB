package eu.essi_lab.profiler.gwis;

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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.AccessHandler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.rsm.access.DefaultAccessResultSetMapper;
import eu.essi_lab.profiler.gwis.request.GWISGetCodeRequestFilter;
import eu.essi_lab.profiler.gwis.request.GWISPlotRequestFilter;
import eu.essi_lab.profiler.gwis.request.data.GWISDataRequestFilter;

/**
 * @author boldrini
 */
public class GWISProfiler extends Profiler<GWISProfilerSetting> {

    protected static final List<String> SUPPORTED_VERSIONS = new ArrayList<>();

    protected static final List<String> SUPPORTED_OUTPUT_FORMATS = new ArrayList<>();

    private static final String KEY_REQUEST = "request";

    static {

	SUPPORTED_VERSIONS.add("0.0");
	SUPPORTED_OUTPUT_FORMATS.add("application/xml");
	SUPPORTED_OUTPUT_FORMATS.add("text/xml");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	DiscoveryHandler<GSResource> handler = new DiscoveryHandler<>();
	handler.setRequestTransformer(new GWISRequestTransformer());
	handler.setMessageResponseMapper(new GWISResultSetMapper());
	handler.setMessageResponseFormatter(new GWISResultSetFormatter());
	selector.register(new GWISPlotRequestFilter(), handler);

	GWISGetCodeHandler handler2 = new GWISGetCodeHandler();
	selector.register(new GWISGetCodeRequestFilter(), handler2);

	AccessHandler<DataObject> handler3 = new AccessHandler<>();
	handler3.setRequestTransformer(new GWISDataRequestTransformer());
	handler3.setMessageResponseMapper(new DefaultAccessResultSetMapper());
	handler3.setMessageResponseFormatter(new GWISDataResultSetFormatter());
	selector.register(new GWISDataRequestFilter(), handler3);

	return selector;
    }

    @Override
    public Response createUncaughtError(WebRequest webRequest, Status status, String message) {

	ValidationMessage vm = new ValidationMessage();
	vm.setError(message);
	vm.setErrorCode(ExceptionCode.NO_APPLICABLE_CODE.toString());

	return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XML).entity("").build();
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_XML).entity("").build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());
	String req = parser.getValue(KEY_REQUEST, true);
	ValidationMessage message = new ValidationMessage();

	if (req == null) {

	    message.setError("Missing mandatory request parameter");
	    message.setErrorCode(ExceptionCode.MISSING_PARAMETER.toString());
	    message.setLocator(KEY_REQUEST);
	} else {
	    message.setErrorCode(ExceptionCode.INVALID_PARAMETER.getCode());
	    message.setError("Invalid request parameter");
	    message.setLocator(KEY_REQUEST);
	}

	return onValidationFailed(request, message);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected GWISProfilerSetting initSetting() {

	return new GWISProfilerSetting();
    }
}
