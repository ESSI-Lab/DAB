package eu.essi_lab.demo.profiler;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.demo.extensions.DemoProvider;
import eu.essi_lab.demo.extensions.oaipmh.SimpleOAIPMHProfile;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.Profiler;
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

/**
 * Basic implementation of {@link Profiler}.<br>
 * The discovery interface (available at the path <b>query</b>)is defined by 4 parameters provided by the
 * {@link DemoParameters} class.<br>
 * The service information interface (available at the path <b>info</b>) is defined by the {@link DemoInfoHandler}
 * class.<br>
 * The {@link DiscoveryHandler} is set with an instance of {@link DemoRequestTransformer} and
 * {@link DiscoveryResultSetMapper}
 * and {@link MessageResponseFormatter} which provide the result set in JSON format.<br>
 * Example queries:
 * <ul>
 * 
 * <li>In the metadata formats list of the OAI-PMH service, the additional "demo" format provided by the {@link SimpleOAIPMHProfile} can be found:<br>
 * <code>http://localhost:9090/gs-service/services/oaipmh?verb=ListMetadataFormats</code></li>
 * 
 * <li>Here the profiler information provided by the {@link DemoInfoHandler} can be found:<br>
 * <code>http://localhost:9090/gs-service/services/demo/info</code></li>
 * 
 * <li>An example query with default implicit parameters "count=10" and "start=1": <code>http://localhost:9090/gs-service/services/demo/query</code></li>
 * 
 * <li>An example query with default which uses the custom parameter "contactCity": <code>http://localhost:9090/gs-service/services/demo/query?contactCity=ROME&count=1&start=4</code></li>
 * 
 * <li>An example query with default which uses the custom parameters "contactCity" and "onlineName": <code>http://localhost:9090/gs-service/services/demo/query?contactCity=ROME&onlineName=SST</code></li>
 * </ul>
 * 
 * @author Fabrizio
 */
public class DemoProfiler extends Profiler {

    public static final ProfilerSetting DEMO_SERVICE_INFO = new ProfilerSetting();

    public static final String SERVICE_TYPE = "Demo";

    static {
	DEMO_SERVICE_INFO.setServiceName("Demo");
	DEMO_SERVICE_INFO.setServiceType(SERVICE_TYPE);
	DEMO_SERVICE_INFO.setServicePath("demo");
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
		new GETRequestFilter("demo/query"), //
		handler);

	// registers the info handler
	selector.register(//
		new GETRequestFilter("demo/info"), //
		new DemoInfoHandler());

	return selector;
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

    @Override
    protected ProfilerSetting initSetting() {

	return DEMO_SERVICE_INFO;
    }
}
