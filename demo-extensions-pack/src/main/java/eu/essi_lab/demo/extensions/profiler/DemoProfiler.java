package eu.essi_lab.demo.extensions.profiler;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONObject;

import eu.essi_lab.demo.extensions.DemoProvider;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.ProfilerInfo;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatterFactory;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;
import eu.essi_lab.pdk.rsf.impl.json.jsapi._2_0.JS_API_ResultSetFormatter_2_0;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapperFactory;
import eu.essi_lab.pdk.rsm.impl.json.jsapi.JS_API_ResultSetMapper;
public class DemoProfiler extends Profiler {

    public static final ProfilerInfo DEMO_SERVICE_INFO = new ProfilerInfo();

    public static final String SERVICE_TYPE = "demo";

    static {
	DEMO_SERVICE_INFO.setServiceName("Demo");
	DEMO_SERVICE_INFO.setServiceType(SERVICE_TYPE);
	DEMO_SERVICE_INFO.setServicePath("ext");
	DEMO_SERVICE_INFO.setServiceVersion("1.0.0");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	// creates the selector
	HandlerSelector selector = new HandlerSelector();

	// creates the discovery handler
	DiscoveryHandler<String> handler = new DiscoveryHandler<String>();

	// set the request transformer
	handler.setRequestTransformer(new DemoRequestTransformer());

	// creates a result set mapper for JSON
	DiscoveryResultSetMapper<String> mapper = DiscoveryResultSetMapperFactory.loadMappers(//
		new ESSILabProvider(), //
		JS_API_ResultSetMapper.JS_API_MAPPING_SCHEMA, String.class).get(0); //

	// creates a result set formatter for JSON
	DiscoveryResultSetFormatter<String> formatter = DiscoveryResultSetFormatterFactory.loadFormatters(//
		new ESSILabProvider(), //
		JS_API_ResultSetFormatter_2_0.JS_API_FORMATTING_ENCODING, String.class).get(0);

	// set the result set mapper
	handler.setMessageResponseMapper(mapper);

	// set the result set formatter
	handler.setMessageResponseFormatter(formatter);

	// registers the discovery handler
	selector.register(//
		new GETRequestFilter("query"), //
		handler);

	// registers the info handler
	selector.register(//
		new GETRequestFilter("info"), //
		new DemoInfoHandler());

	return selector;
    }

    @Override
    public ProfilerInfo getProfilerInfo() {

	return DEMO_SERVICE_INFO;
    }

    @Override
    public Provider getProvider() {

	return DemoProvider.getInstance();
    }

    @Override
    public Response createUncaughtError(WebRequest webRequest, Status status, String message) {

	JSONObject error = new JSONObject();
	error.put("unexpected error", message);

	return Response.serverError().//
		status(Status.INTERNAL_SERVER_ERROR).//
		entity(error.toString(3)).//
		encoding(MediaType.APPLICATION_JSON).//
		build();
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	JSONObject error = new JSONObject();
	error.put("parsing error", message.getError());
	error.put("parameter", message.getLocator());

	return Response.serverError().//
		status(Status.BAD_REQUEST).//
		entity(error.toString(3)).//
		encoding(MediaType.APPLICATION_JSON).//
		build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	JSONObject info = new JSONObject();
	info.put("invalid path", "supported paths are: 'info' and 'query'");

	return Response.serverError().//
		status(Status.NOT_FOUND).//
		entity(info.toString(3)).//
		encoding(MediaType.APPLICATION_JSON).//
		build();
    }
}
