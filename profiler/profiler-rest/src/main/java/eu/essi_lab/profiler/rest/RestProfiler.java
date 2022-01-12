package eu.essi_lab.profiler.rest;

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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.ProfilerInfo;
import eu.essi_lab.pdk.handler.AccessHandler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.SemanticHandler;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.handler.selector.POSTRequestFilter;
import eu.essi_lab.pdk.rsf.AccessResultSetFormatterAttachment;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatterFactory;
import eu.essi_lab.pdk.rsf.JSONSemanticResponseFormatter;
import eu.essi_lab.pdk.rsf.impl.xml.gs.GS_XML_ResultSetFormatter;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapperFactory;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.pdk.rsm.access.DefaultAccessResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.json.sem.JSONSemanticResponseMapper;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.profiler.rest.handler.info.AccessInfoHandler;
import eu.essi_lab.profiler.rest.handler.info.DiscoveryInfoHandler;
import eu.essi_lab.profiler.rest.handler.info.FullStatisticsHandler;
import eu.essi_lab.profiler.rest.handler.info.GWPStatisticsHandler;
import eu.essi_lab.profiler.rest.handler.info.MessageFormat;
import eu.essi_lab.profiler.rest.handler.info.RestParameter;
import eu.essi_lab.profiler.rest.handler.info.ServiceInfoHandler;
import eu.essi_lab.profiler.rest.handler.info.SourcesInfoHandler;
public class RestProfiler extends Profiler {

    public static final String REST_PROFILER_TYPE = "Rest";
    public static final String REST_SEMANTIC_BROWSING_PATH = "rest/" + WebRequest.SEMANTIC_PATH + "/browsing/";
    public static final String REST_SEMANTIC_SEARCH_PATH = "rest/" + WebRequest.SEMANTIC_PATH + "/search/";
    private DiscoveryHandler<String> discoveryHandler;

    public static final ProfilerInfo REST_SERVICE_INFO = new ProfilerInfo();
    static {
	REST_SERVICE_INFO.setServiceName("Rest");
	REST_SERVICE_INFO.setServiceType(REST_PROFILER_TYPE);
	REST_SERVICE_INFO.setServicePath("rest");
	REST_SERVICE_INFO.setServiceVersion("1.0.0");
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();

	// -------------------------
	//
	// Access query
	//
	AccessHandler<DataObject> accessHandler = new AccessHandler<>();
	accessHandler.setRequestTransformer(new RestAccessRequestTransformer());
	accessHandler.setMessageResponseMapper(new DefaultAccessResultSetMapper());
	accessHandler.setMessageResponseFormatter(new AccessResultSetFormatterAttachment());

	selector.register(//
		new GETRequestFilter("rest/access/query"), //
		accessHandler);

	selector.register(//
		new POSTRequestFilter("rest/access/query"), //
		accessHandler);

	// -------------------------
	//
	// Discovery query
	//
	discoveryHandler = new DiscoveryHandler<>();

	if (request.isGetRequest()) {
	    discoveryHandler.setRequestTransformer(new GetRecordsByIdRequestTransformer());
	} else {
	    discoveryHandler.setRequestTransformer(new RestDiscoveryRequestTransformer());
	}

	selector.register(//
		new GETRequestFilter("rest/discovery/query"), //
		discoveryHandler);

	selector.register(//
		new POSTRequestFilter("rest/discovery/query"), //
		discoveryHandler);

	// --------------------------
	//
	// Semantic query
	//

	SemanticHandler<JSONObject> semanticHandler = new SemanticHandler<>();

	semanticHandler.setRequestTransformer(new RestSemanticRequestTransformer());
	semanticHandler.setMessageResponseMapper(new JSONSemanticResponseMapper());
	semanticHandler.setMessageResponseFormatter(new JSONSemanticResponseFormatter());

	selector.register(//
		new GETRequestFilter(REST_SEMANTIC_SEARCH_PATH), //
		semanticHandler);

	selector.register(//
		new GETRequestFilter(REST_SEMANTIC_BROWSING_PATH), //
		semanticHandler);

	selector.register(//
		new GETRequestFilter(REST_SEMANTIC_BROWSING_PATH + "*"), //
		semanticHandler);

	// -------------------------
	//
	// Info
	//
	selector.register(//
		new GETRequestFilter("rest/discovery/info"), //
		new DiscoveryInfoHandler());
	
	selector.register(//
		new GETRequestFilter("rest/discovery/info",true), //
		new DiscoveryInfoHandler());
		
	selector.register(//
		new GETRequestFilter("rest/sources/info"), //
		new SourcesInfoHandler());
	
	selector.register(//
		new GETRequestFilter("rest/sources/info", true), //
		new SourcesInfoHandler());

	selector.register(//
		new GETRequestFilter("rest/access/info"), //
		new AccessInfoHandler());
	
	selector.register(//
		new GETRequestFilter("rest/access/info", true), //
		new AccessInfoHandler());

	selector.register(//
		new GETRequestFilter("rest/service/info"), //
		new ServiceInfoHandler());
	
	selector.register(//
		new GETRequestFilter("rest/service/info", true), //
		new ServiceInfoHandler());

	// -------------------------
	//
	// Statistics
	//
	selector.register(//
		new GETRequestFilter("rest/stats"), //
		new FullStatisticsHandler());
	
	selector.register(//
		new GETRequestFilter("rest/stats", true), //
		new FullStatisticsHandler());

	selector.register(//
		new GETRequestFilter("rest/stats/gp"), //
		new GWPStatisticsHandler());
	
	selector.register(//
		new GETRequestFilter("rest/stats/gp", true), //
		new GWPStatisticsHandler());

	selector.register(//
		new GETRequestFilter("rest/stats/gp/discovery"), //
		new GWPStatisticsHandler());
	
	selector.register(//
		new GETRequestFilter("rest/stats/gp/discovery", true), //
		new GWPStatisticsHandler());

	selector.register(//
		new GETRequestFilter("rest/stats/gp/access"), //
		new GWPStatisticsHandler());
	
	selector.register(//
		new GETRequestFilter("rest/stats/gp/access", true), //
		new GWPStatisticsHandler());

	return selector;
    }

    @Override
    public Response createUncaughtError(WebRequest request, Status status, String message) {

	ResponseBuilder builder = Response.status(status);

	String queryString = request.getQueryString();
	KeyValueParser parser = new KeyValueParser(queryString);
	String reqEncoding = parser.getValue("requestEncoding");

	String error = null;
	if (reqEncoding == null || //
		reqEncoding.equals(KeyValueParser.UNDEFINED) || //
		queryString.contains("xml")) {

	    error = createXMLError(status, message);
	    builder.entity(error);
	    builder.type(MediaType.APPLICATION_XML_TYPE);

	} else {
	    // JSON
	}

	return builder.build();
    }

    @Override
    protected void onRequestValidated(WebRequest webRequest, WebRequestValidator validator, RequestType type) throws GSException {

	KeyValueParser parser = new KeyValueParser(webRequest.getQueryString());

	MessageFormat format = MessageFormat.fromFormat(//
		parser.getValue(//
			RestParameter.RESPONSE_FORMAT.getName(), //
			MessageFormat.XML.getFormat()));

	switch (format) {
	case JSON:

	    // not yet supported!
	    break;

	case XML:

	    discoveryHandler.setMessageResponseMapper( //
		    DiscoveryResultSetMapperFactory.loadMappers(//
			    MappingSchema.GS_DATA_MODEL_MAPPING_SCHEMA, //
			    String.class).get(0));

	    discoveryHandler.setMessageResponseFormatter( //
		    DiscoveryResultSetFormatterFactory.loadFormatters(//
			    GS_XML_ResultSetFormatter.GS_FORMATTING_ENCODING, //
			    String.class).get(0));

	    break;
	}
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	ResponseBuilder builder = Response.status(Status.BAD_REQUEST);

	String queryString = request.getQueryString();
	KeyValueParser parser = new KeyValueParser(queryString);
	String reqEncoding = parser.getValue("requestEncoding");

	String error = null;
	if (!parser.isValid(reqEncoding) || //
		queryString.contains("xml")) {

	    error = createXMLError(Status.BAD_REQUEST, message);

	    builder.entity(error);
	    builder.type(MediaType.APPLICATION_XML_TYPE);
	} else {
	    // JSON
	}

	return builder.build();
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	return createUncaughtError(request, Status.BAD_REQUEST, "Unknonwn request");
    }

    private String createXMLError(Status status, ValidationMessage message) {

	String error = "<gs:error xmlns:gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\">";
	error += "<gs:status>" + status + "</gs:status>";
	error += "<gs:message>" + message.getError() + "</gs:message>";
	error += "<gs:locator>" + message.getLocator() + "</gs:locator>";
	error += "</gs:error>";

	return error;
    }

    private String createXMLError(Status status, String message) {

	String error = "<gs:error xmlns:gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\">";
	error += "<gs:status>" + status + "</gs:status>";
	error += "<gs:message>" + message + "</gs:message>";
	error += "</gs:error>";

	return error;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public ProfilerInfo getProfilerInfo() {

	return REST_SERVICE_INFO;
    }

}
