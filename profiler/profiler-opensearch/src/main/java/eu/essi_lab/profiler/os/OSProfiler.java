package eu.essi_lab.profiler.os;

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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatterFactory;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;
import eu.essi_lab.pdk.rsf.impl.atom.AtomGPResultSetFormatter;
import eu.essi_lab.pdk.rsf.impl.json.jsapi._1_0.JS_API_ResultSetFormatter_1_0;
import eu.essi_lab.pdk.rsf.impl.json.jsapi._2_0.JS_API_ResultSetFormatter_2_0;
import eu.essi_lab.pdk.rsf.impl.json.jsapi._2_0.JS_API_ResultSet_2_0;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapperFactory;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.pdk.rsm.impl.atom.AtomGPResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.json.jsapi.JS_API_ResultSetMapper;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.profiler.os.handler.discover.OSDiscoveryRequestFilter;
import eu.essi_lab.profiler.os.handler.discover.OSRequestTransformer;
import eu.essi_lab.profiler.os.handler.discover.OS_XML_ResultSetFormatter;
import eu.essi_lab.profiler.os.handler.srvinfo.OSDescriptionDocumentHandler;
import eu.essi_lab.profiler.os.handler.srvinfo.OSGetSourcesFilter;
import eu.essi_lab.profiler.os.handler.srvinfo.OSGetSourcesHandler;

/**
 * @author Fabrizio
 */
public class OSProfiler extends Profiler {

    /**
     * The OpenSearch profiler type
     */
    public static final String OPEN_SEARCH_PROFILER_TYPE = "OpenSearch";

    /**
     * The OpenSearch service info
     */
    public static final ProfilerSetting OPENSEARCH_SERVICE_INFO = new ProfilerSetting();

    private static final String INVALID_OS_REQUEST = "INVALID_OS_REQUEST";
    private DiscoveryHandler<String> discoveryHandler;

    private Logger logger = GSLoggerFactory.getLogger(getClass());

    static {
	OPENSEARCH_SERVICE_INFO.setServiceName("OpenSearch Service");
	OPENSEARCH_SERVICE_INFO.setServiceType(OPEN_SEARCH_PROFILER_TYPE);
	OPENSEARCH_SERVICE_INFO.setServicePath("opensearch");
	OPENSEARCH_SERVICE_INFO.setServiceVersion("1.0.0");
    }

    public OSProfiler() {

	discoveryHandler = new DiscoveryHandler<>();
	discoveryHandler.setRequestTransformer(new OSRequestTransformer());
    }

    @Override
    public HandlerSelector getSelector(WebRequest request) {

	HandlerSelector selector = new HandlerSelector();
	
	//
	// Discovery
	//

	selector.register(//
		new OSDiscoveryRequestFilter(), //
		discoveryHandler);

	//
	// Get sources
	//

	selector.register(//
		new OSGetSourcesFilter(), //
		new OSGetSourcesHandler());

	//
	// Description
	//

	selector.register(//
		new GETRequestFilter("opensearch"), //
		new OSDescriptionDocumentHandler());

	selector.register(//
		new GETRequestFilter("opensearch/description"), //
		new OSDescriptionDocumentHandler());

	return selector;
    }

    /**
     * @param status
     * @param message
     * @return
     */
    @Override
    public Response createUncaughtError(WebRequest request, Status status, String message) {

	String outputFormat = readOutputFormat(request);

	switch (outputFormat) {
	case MediaType.APPLICATION_JSON:

	    JSONObject jsonError = new JSONObject();
	    jsonError.put("Error occurred", message);

	    return Response.status(status).type(outputFormat).entity(jsonError.toString()).build();

	case MediaType.APPLICATION_ATOM_XML:
	case NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE:
	default:

	    String error = "<gs:error xmlns:gs=\"" + NameSpace.GS_DATA_MODEL_SCHEMA_URI + "\">Error occurred: " + message + "</gs:error>";
	    return Response.status(status).type(MediaType.APPLICATION_XML).entity(error).build();
	}
    }

    /**
     * Here (in case the request is not a GetDescriptionDocument) the right {@link DiscoveryResultSetMapper} and {@link
     * MessageResponseFormatter} are set, according to the requested output format
     */
    @Override
    protected void onRequestValidated(WebRequest request, WebRequestValidator validator, RequestType type) throws GSException {

	if (type != RequestType.DISCOVERY) {
	    return;
	}

	OSRequestParser parser = new OSRequestParser(request);

	String outputFormat = readOutputFormat(request);

	String version = parser.parse(OSParameters.OUTPUT_VERSION);

	switch (outputFormat) {
	case MediaType.APPLICATION_JSON:

	    logger.debug("Selecting mapper, encoding and formatter in APPLICATION_JSON");

	    DiscoveryResultSetMapper<String> mapper = DiscoveryResultSetMapperFactory.loadMappers(//
		    new ESSILabProvider(), //
		    JS_API_ResultSetMapper.JS_API_MAPPING_SCHEMA, String.class).get(0); //

	    FormattingEncoding encoding = version.equals("1.0") ? JS_API_ResultSetFormatter_1_0.JS_API_FORMATTING_ENCODING
		    : JS_API_ResultSetFormatter_2_0.JS_API_FORMATTING_ENCODING;

	    DiscoveryResultSetFormatter<String> formatter = DiscoveryResultSetFormatterFactory.loadFormatters(//
		    new ESSILabProvider(), //
		    encoding, //
		    String.class).get(0);

	    discoveryHandler.setMessageResponseMapper(mapper);
	    discoveryHandler.setMessageResponseFormatter(formatter);

	    logger.debug("Selected mapper: {}", mapper.getClass().getName());
	    logger.debug("Selected encoding: {}", encoding.getClass().getName());
	    logger.debug("Selected formatter: {}", formatter.getClass().getName());

	    break;

	case NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE:

	    logger.debug("Selecting mapper and formatter in GS_DATA_MODEL_XML_MEDIA_TYPE");

	    mapper = DiscoveryResultSetMapperFactory.loadMappers(//
		    new ESSILabProvider(), //
		    MappingSchema.GS_DATA_MODEL_MAPPING_SCHEMA, //
		    String.class).get(0); //

	    formatter = DiscoveryResultSetFormatterFactory.loadFormatters(//
		    new ESSILabProvider(), //
		    OS_XML_ResultSetFormatter.OS_XML_FORMATTING_ENCODING, //
		    String.class).get(0);

	    discoveryHandler.setMessageResponseMapper(mapper);
	    discoveryHandler.setMessageResponseFormatter(formatter);

	    logger.debug("Selected mapper: {}", mapper.getClass().getName());

	    logger.debug("Selected formatter: {}", formatter.getClass().getName());
	    break;

	case MediaType.APPLICATION_ATOM_XML:
	default:

	    logger.debug("Selecting mapper and formatter in APPLICATION_ATOM_XML");

	    discoveryHandler.setMessageResponseMapper(new AtomGPResultSetMapper());
	    discoveryHandler.setMessageResponseFormatter(new AtomGPResultSetFormatter());

	    break;
	}
    }

    /**
     * @param parser
     * @return
     */
    public static String readOutputFormat(WebRequest request) {

	OSRequestParser parser = new OSRequestParser(request);

	String outputFormat = parser.parse(OSParameters.OUTPUT_FORMAT);

	if (outputFormat == null) {
	    outputFormat = MediaType.APPLICATION_ATOM_XML;
	}

	return outputFormat;
    }

    @Override
    protected Response onHandlerNotFound(WebRequest request) {

	return createUncaughtError(request, Status.BAD_REQUEST, "Unsupported request");
    }

    @Override
    protected Response onValidationFailed(WebRequest request, ValidationMessage message) {

	String outputFormat = readOutputFormat(request);
	String error = message.getError();
	OSRequestParser parser = new OSRequestParser(request);

	JSONObject resultSet = null;

	String out = null;

	switch (outputFormat) {
	case MediaType.APPLICATION_JSON:

	    String version = parser.parse(OSParameters.OUTPUT_VERSION);
	    switch (version) {
	    case "1.0":

		// in this version the error message is not included in the
		// result set, it is part of the response object. so we return only the
		// empty result set
		resultSet = new JSONObject();
		resultSet.put("size", 0);
		resultSet.put("start", 1);
		resultSet.put("pageSize", 0);
		resultSet.put("pageCount", 0);
		resultSet.put("pageIndex", 0);

		break;
	    case "2.0":
	    default:

		GSException exception = GSException.createException(//
			getClass(), //
			error, //
			null, //
			ErrorInfo.ERRORTYPE_CLIENT, //
			ErrorInfo.SEVERITY_ERROR, //
			INVALID_OS_REQUEST);

		resultSet = new JS_API_ResultSet_2_0(exception).asJSONObject();

		break;
	    }

	    JSONObject object = new JSONObject();
	    object.put("resultSet", resultSet);
	    object.put("reports", new JSONArray());

	    out = object.toString();

	    break;

	case NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE:

	    out = OS_XML_ResultSetFormatter.getEmptyResponse(request.getQueryString());
	    outputFormat = MediaType.APPLICATION_XML.toString();
	    break;

	case MediaType.APPLICATION_ATOM_XML:

	    out = AtomGPResultSetFormatter.getEmptyFeed(request.getQueryString(), error);
	    outputFormat = MediaType.APPLICATION_XML.toString();
	    break;
	}

	return Response.status(Status.BAD_REQUEST).type(outputFormat).entity(out.toString()).build();
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    protected ProfilerSetting initSetting() {

	return OPENSEARCH_SERVICE_INFO;
    }
}
