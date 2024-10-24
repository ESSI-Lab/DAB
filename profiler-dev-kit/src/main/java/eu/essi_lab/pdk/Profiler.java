package eu.essi_lab.pdk;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.lib.servlet.RequestManager;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Pluggable;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.handler.AccessHandler;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.handler.ProfilerHandler;
import eu.essi_lab.pdk.handler.SemanticHandler;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.handler.selector.WebRequestFilter;
import eu.essi_lab.pdk.rsf.FormattingEncoding;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.rip.RuntimeInfoProvider;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchInfoPublisher;

/**
 * A <code>Profiler</code> implements the business logic of a "GI-suite web service" (from here referred as "profiler
 * service"), that is a
 * web service that publishes a "discovery/access interface" and optionally (and preferably) a "service information
 * interface"
 * <h3>Discovery interface</h3> The "discovery interface" is in charge to
 * provide a set of parameters that can be used by supported clients to build a "discovery query".<br>
 * A discovery query is a query which
 * uses the constraints provided by the interface parameters in order to retrieve a filtered set of compliant "metadata
 * records".<br>
 * <br>
 * This is a very simple example of discovery query from the <a href="http://www.opensearch.org/Home">OpenSearch</a>
 * specification:
 * <code>"http://example.com/search?q=temperature"</code>. The semantic of the "q" parameter is specified by the
 * discovery interface specification, in this case provided by <a href="http://www.opensearch.org/Home">OpenSearch</a>.
 * This query discovers
 * all the metadata records with a textual content matching the keyword "temperature".<br>
 * <br>
 * The GI-suite provides a set of components which are in charge to "harmonize" the conceptual model of the profiler
 * service interface to
 * the internal conceptual model. From the <code>Profiler</code> point of view, this process consists of three
 * steps:<br>
 * <br>
 * <ol>
 * <li><b><span style="background-color: lightgreen">request handling</span></b>: the service discovery request is
 * harmonized in to the internal request</li>
 * <li><b><span style="background-color: yellow">execution</span></b>: the harmonized internal request is executed by
 * creating an harmonized result set of suitable metadata records</li>
 * <li><b><span style="background-color: orange">response handling</span></b>: the harmonized metadata records are then
 * presented to the client according to the profiler service interface</li>
 * </ol>
 * The following table summarizes how the above concepts are mapped to the internal model and the suite components that
 * are in charge to
 * implement the above process:<br>
 * <br>
 * <html>
 * <head>
 * <style>
 * table { font-family: arial, sans-serif; border-collapse: collapse; width: 100%; } td, th { border: 1px solid #dddddd;
 * text-align: left;
 * padding: 8px; }
 * </style>
 * </head>
 * <body>
 * <table>
 * <tr>
 * <td style="background-color: lightgreen">Service discovery request</td>
 * <td>represented by {@link DiscoveryMessage}</td>
 * <td>created by {@link DiscoveryRequestTransformer}</td>
 * </tr>
 * <tr>
 * <td style="background-color: lightgreen">Service discovery constraints</td>
 * <td>represented by {@link Bond}s</td>
 * <td>provided as property of the {@link DiscoveryMessage}</td>
 * </tr>
 * <tr>
 * <td style="background-color: yellow">Harmonized discovery response</td>
 * <td>represented by {@link ResultSet} of {@link GSResource}s</td>
 * <td>provided by internal implementation of {@link IDiscoveryExecutor}</td>
 * </tr>
 * <tr>
 * <td style="background-color: orange">Service metadata records</td>
 * <td>represented by {@link ResultSet} of <code>String</code>s</td>
 * <td>created by {@link DiscoveryResultSetMapper}</td>
 * </tr>
 * <tr>
 * <td style="background-color: orange">Service response</td>
 * <td>represented as {@link Response} entity</td>
 * <td>created by {@link MessageResponseFormatter}</td>
 * </tr>
 * </table>
 * </body>
 * </html> <br>
 * All the steps of the discovery process are handled as a workflow by the {@link DiscoveryHandler}, selected by the
 * provided {@link
 * #getSelector()}.<br>
 * <br>
 * According to the supplied {@link WebRequestFilter} the {@link DiscoveryHandler} can handles several kind of discovery
 * queries. This
 * happens if the supplied {@link WebRequestFilter} accepts more than one {@link WebRequest}s.
 * <br>
 * <br>
 * The components which define the {@link DiscoveryHandler} workflow, can be set once and for all, or on the fly
 * depending on the current
 * {@link WebRequest}; see {@link #onRequestValidated(WebRequest, WebRequestValidator, boolean)} method<br>
 * <h3>Service information interface</h3> The "service information interface" is in charge to provide a
 * description of the service and its discovery interface that can be used by supported clients to choose if and how to
 * build a proper
 * discovery query.<br>
 * This interface is typically provided by services as a set of one or more descriptive documents like the
 * "capabilities" document of the "OGC CSW". Another example is the
 * <a href="http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_description_document">description
 * document</a> of a <a href="http://www.opensearch.org/Home">OpenSearch</a> compliant service.<br>
 * <br>
 * The requests of this interface are handled by {@link DefaultRequestHandler}s that can be registered using the {@link
 * #getServiceInfoHandlers()} method. As for the {@link DiscoveryHandler}, according to the supplied
 * {@link WebRequestFilter} the same
 * {@link DefaultRequestHandler} can handles several kind of requests or just one. Registering of
 * {@link DefaultRequestHandler}s is not
 * mandatory, but it is <b>strongly recommended</b>
 * <h3>Implementation notes</h3> As for all the {@link Pluggable}, in order to be
 * loaded the <code>Profiler</code> implementation <b>MUST</b> be registered with the {@link ServiceLoader} API. New
 * instances of profilers
 * are loaded at query time by the "GI-suite service" according to the current request path (see
 * {@link ProfilerSetting#getPath()})
 * <h3>Implementation guide lines</h3>
 * Implementing a new <code>Profiler</code> mainly consists in configuring one or more {@link DiscoveryHandler}s with
 * its {@link Pluggable}
 * components. The following table shows all the {@link Pluggable} components involved in the development of a new
 * <code>Profiler</code>
 * along with a short description and the component which triggers it<br>
 * <br>
 * <html>
 * <head>
 * <style>
 * table { font-family: arial, sans-serif; border-collapse: collapse; width: 100%; } td, th { border: 1px solid #dddddd;
 * text-align: left;
 * padding: 8px; }
 * </style>
 * </head>
 * <body>
 * <table>
 * <tr>
 * <td style="background-color: #ffbf00"><b>{@link Pluggable} component</b></td>
 * <td style="background-color: #ffbf00"><b>Main responsibility</b></td>
 * <td style="background-color: #ffbf00"><b>Triggering component</b></td>
 * </tr>
 * <tr>
 * <td><code>Profiler</code></td>
 * <td>Dispatches the registered {@link WebRequestHandler} to handle the current {@link WebRequest}</td>
 * <td>GI-suite service</td>
 * </tr>
 * <tr>
 * <td>{@link DefaultRequestHandler}</td>
 * <td>Provides information about the profiler service and its discovery interface</td>
 * <td><code>Profiler</code></td>
 * </tr>
 * <tr>
 * <td>{@link DiscoveryHandler}</td>
 * <td>Handles a discovery query by executing a workflow which involves three {@link Pluggable} components</td>
 * <td><code>Profiler</code></td>
 * </tr>
 * <tr>
 * <td>{@link DiscoveryRequestTransformer}</td>
 * <td>It contributes to the execution of the workflow by transforming a {@link WebRequest} in to a
 * {@link DiscoveryMessage} according to the specification of the profiler service interface</td>
 * <td>{@link DiscoveryHandler}</td>
 * </tr>
 * <tr>
 * <td>{@link DiscoveryResultSetMapper}</td>
 * <td>It contributes to the execution of the workflow by mapping a <code>ResultSet&lt;GSResource&gt;</code> in to a
 * <code>ResultSet&lt;String&gt;</code> according to a {@link MappingSchema}</td>
 * <td>{@link DiscoveryHandler}</td>
 * </tr>
 * <tr>
 * <td>{@link MessageResponseFormatter}</td>
 * <td>It contributes to the execution of the workflow by formatting the <code>ResultSet&lt;String&gt;</code> in a
 * {@link Response} entity suitable for the client which triggered the discovery query, according to a
 * {@link FormattingEncoding}</td>
 * <td>{@link DiscoveryHandler}</td>
 * </tr>
 * </table>
 * </body>
 * </html> <br>
 *
 * @author Fabrizio
 */
public abstract class Profiler implements Configurable<ProfilerSetting>, WebRequestHandler, Pluggable, RuntimeInfoProvider {

    private static final String NO_VALIDATOR_FOUND = "NO_VALIDATOR_FOUND";

    private ProfilerSetting setting;

    //
    //
    //

    /**
     * The type of the handled request
     *
     * @author Fabrizio
     */
    public enum RequestType {

	/**
	 *
	 */
	DISCOVERY,
	/**
	 *
	 */
	ACCESS,
	/**
	 *
	 */
	SEMANTIC,
	/**
	 *
	 */
	OTHER
    }

    /**
     * Creates a new instance of <code>Profiler</code>
     */
    public Profiler() {
	configure(initSetting());
    }

    /**
     * Returns the {@link HandlerSelector}
     *
     * @param request
     * @return a non <code>null</code> {@link HandlerSelector}
     */
    public abstract HandlerSelector getSelector(WebRequest request);

    /**
     * Returns a {@link ProfilerSetting} for this <code>Profiler</code>
     *
     * @return a non <code>null</code> {@link ProfilerSetting}
     */
    protected abstract ProfilerSetting initSetting();

    /**
     * @param setting
     */
    public void configure(ProfilerSetting setting) {

	this.setting = setting;
    }

    /**
     * @return
     */
    public ProfilerSetting getSetting() {

	return setting;
    }

    @Override
    public String getType() {

	return getSetting().getServiceType();
    }

    /**
     * Selects a {@link WebRequestHandler} with the {@link HandlerSelector} and delegates the handling of the
     * <code>request</code>.<br>
     * Before to invoke the {@link WebRequestHandler#handle(WebRequest)} method, the request is validated by calling the
     * current {@link
     * WebRequestValidator#validate(WebRequest)} method. If the validation fails, a {@link GSException} is throwed with
     * the error id
     * "DISCOVER_VALIDATION_FAILED"
     *
     * @param request
     * @return
     */
    @Override
    public final Response handle(WebRequest request) throws GSException {

	Optional<ElasticsearchInfoPublisher> publisher = null;

	String rid = request.getRequestId();

	RequestManager.getInstance().updateThreadName(getClass(), rid);

	publisher = ElasticsearchInfoPublisher.create(request);
	if (publisher.isPresent()) {
	    publisher.get().publish(request);
	}

	Response cached = ProxyCache.getInstance().getCachedResponse(request);
	if (cached != null) {
	    ChronometerInfoProvider chronometer = new ChronometerInfoProvider(TimeFormat.MIN_SEC_MLS);
	    chronometer.start();
	    GSLoggerFactory.getLogger(getClass()).info("{} ENDED - handling time: {}", getRequestLogPrefix(request),
		    chronometer.formatElapsedTime());
	    publisher.get().publish(chronometer);
	    publisher.get().publish(new ResponseInfoProvider(cached));
	    publisher.get().publish(this);
	    publisher.get().write();
	    return cached;
	}

	GSLoggerFactory.getLogger(getClass()).traceMemoryUsage(getRequestLogPrefix(request) + " FHSM STARTED: ");

	Response response = null;

	ChronometerInfoProvider chronometer = new ChronometerInfoProvider(TimeFormat.MIN_SEC_MLS);
	chronometer.start();

	ValidationMessage validationMessage = null;

	try {

	    PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.REQUEST_HANDLING, rid,
		    Optional.ofNullable(request));

	    Optional<String> remoteHost = request.readRemoteHostHeader();

	    GSLoggerFactory.getLogger(getClass()).debug("{} Remote host: {}", getRequestLogPrefix(request), remoteHost.orElse("n.a."));

	    logRequest(request);

	    HandlerSelector selector = getSelector(request);
	    Optional<WebRequestHandler> optHandler = selector.select(request);

	    if (optHandler.isPresent()) {

		WebRequestHandler handler = optHandler.get();

		GSLoggerFactory.getLogger(getClass()).info("{} Request accepted by {}", getRequestLogPrefix(request),
			handler.getClass().getSimpleName());

		onFilterAccept(request, handler);

		WebRequestValidator validator = null;
		RequestType type = null;

		if (handler instanceof ProfilerHandler) {

		    @SuppressWarnings("rawtypes")
		    ProfilerHandler dh = (ProfilerHandler) handler;
		    validator = dh.getRequestTransformer();

		    if (handler instanceof DiscoveryHandler<?>) {

			type = RequestType.DISCOVERY;

		    } else if (handler instanceof AccessHandler<?>) {

			type = RequestType.ACCESS;

		    } else if (handler instanceof SemanticHandler<?>) {

			type = RequestType.SEMANTIC;
		    }

		} else if (handler instanceof WebRequestValidator) {

		    validator = (WebRequestValidator) handler;
		    type = RequestType.OTHER;
		} else {
		    throw GSException.createException(//
			    getClass(), //
			    "Can't find validator", //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    NO_VALIDATOR_FOUND);
		}

		String validatorInfo = "[using validator: " + validator.getClass().getSimpleName() + "]";
		validationMessage = validator.validate(request);

		// get the validation result
		ValidationResult result = validationMessage.getResult();

		// in case of validation failed, throws a GSException with
		// the validation error message
		if (result == ValidationResult.VALIDATION_FAILED) {

		    GSLoggerFactory.getLogger(getClass()).warn("{} Validation FAILED {}", getRequestLogPrefix(request), validatorInfo);
		    return onValidationFailed(request, validationMessage);
		}

		GSLoggerFactory.getLogger(getClass()).info("{} Validation SUCCESSFUL {}", getRequestLogPrefix(request), validatorInfo);
		onRequestValidated(request, validator, type);

		response = handler.handle(request);

		onHandlingEnded(response);

		pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

		response = ProxyCache.getInstance().cache(request, response);
		return response;
	    }

	} catch (Throwable e) {
	    
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

	    throw e;

	} finally {

	    GSLoggerFactory.getLogger(getClass()).traceMemoryUsage(getRequestLogPrefix(request) + " FHSM ENDED: ");

	    GSLoggerFactory.getLogger(getClass()).info("{} ENDED - handling time: {}", getRequestLogPrefix(request),
		    chronometer.formatElapsedTime());

	    if (publisher.isPresent()) {

		publisher.get().publish(chronometer);

		if (validationMessage != null) {
		    publisher.get().publish(validationMessage);
		}

		if (response != null) {
		    publisher.get().publish(new ResponseInfoProvider(response));
		}
		publisher.get().publish(this);

		publisher.get().write();
	    }

	}

	GSLoggerFactory.getLogger(getClass()).warn("{} handler not found!", getRequestLogPrefix(request));
	return onHandlerNotFound(request);
    }

    private String getRequestLogPrefix(WebRequest request) {

	return "[Request handling] REQ# " + request.getRequestId();
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = new HashMap<>();
	map.put(RuntimeInfoElement.PROFILER_NAME.getName(), Arrays.asList(getName()));
	map.put(RuntimeInfoElement.PROFILER_TYPE.getName(), Arrays.asList(initSetting().getServiceType()));
	map.put(RuntimeInfoElement.PROFILER_TIME_STAMP_MILLIS.getName(), Arrays.asList(String.valueOf(System.currentTimeMillis())));

	return map;
    }

    @Override
    public String getName() {

	return getClass().getSimpleName();
    }

    /**
     * This method is called by the GI-suite service in case of uncaught errors.<br>
     * Creates an error {@link Response} formatted according
     * to the service interface specification of this
     * <code>Profiler</code>.<br>
     * The content of the error message is defined by the supplied <code>status</code> and <code>message</code>
     *
     * @param request
     * @param status
     * @param message
     * @return a non <code>null</code> {@link Response}
     */
    public abstract Response createUncaughtError(WebRequest request, Status status, String message);

    /**
     * This method is called in case the {@link WebRequestHandler} validation failed.<br>
     * Creates an error {@link Response} formatted
     * according to the service interface specification of this
     * <code>Profiler</code>.<br>
     * The content of the error message is defined by the supplied <code>message</code>
     *
     * @param message the validation message
     * @param request
     * @return a non <code>null</code> {@link Response} which informs the client that the discovery request is not valid
     * @see #handle(WebRequest)
     */
    protected abstract Response onValidationFailed(WebRequest request, ValidationMessage message);

    /**
     * This method is called in case no {@link WebRequestHandler} is registered to handle the supplied
     * <code>request</code>.<br>
     * Creates an error {@link Response} formatted according to the service interface specification of this
     * <code>Profiler</code>
     *
     * @param request the not supported {@link WebRequest}
     * @return a non <code>null</code> {@link Response} which informs the client that the request is not supported
     */
    protected abstract Response onHandlerNotFound(WebRequest request);

    /**
     * This method is invoked just after a successful invocation of the {@link WebRequestFilter#accept(WebRequest)}
     * method
     *
     * @param request the accepted {@link WebRequest}
     * @param handler the registered {@link WebRequestHandler}
     */
    protected void onFilterAccept(WebRequest request, WebRequestHandler handler) {
    }

    /**
     * This method is invoked just after the called of the {@link WebRequestValidator#validate(WebRequest)} method on
     * the supplied
     * <code>request</code> and just before the handling of validated <code>request</code><br>
     * . It can be implemented to perform some preliminary actions, for example to set the components which define the
     * workflow of the
     * {@link DiscoveryHandler} depending
     * <code>webReqeust</code>
     *
     * @param webRequest the validated {@link WebRequest}
     * @param validator the {@link WebRequestValidator} which validated the request
     * @param type the current type of request to handle
     */
    protected void onRequestValidated(WebRequest webRequest, WebRequestValidator validator, RequestType type) throws GSException {
    }

    /**
     * This method is called just before <code>response</code> is returned. It can be implemented for example, in case
     * of unauthorized
     * request (response status is {@link Status#FORBIDDEN}) in order to return also some explaining message
     *
     * @param response the {@link Response} ready to be presented to the client
     */
    protected void onHandlingEnded(Response response) {
    }

    private void logRequest(WebRequest request) {

	String fullRequest = request.getServletRequest().getRequestURL().toString();
	String queryString = request.getQueryString();
	String method = request.getServletRequest().getMethod();

	if (queryString != null) {
	    fullRequest = fullRequest + "?" + queryString;
	}

	GSLoggerFactory.getLogger(getClass()).info("{} Serving {} request: {}", getRequestLogPrefix(request), method, fullRequest);

	if (request.isPostRequest() || request.isPutRequest()) {

	    try {
		String query = IOStreamUtils.asUTF8String(request.getBodyStream().clone());
		query = query.replace("\n", " ").replace("\r", " ");
		GSLoggerFactory.getLogger(getClass()).info("{} {} request body: {}", getRequestLogPrefix(request), method, query);
	    } catch (IOException e) {

		GSLoggerFactory.getLogger(getClass()).warn("{} Unable to log {} request body stream", getRequestLogPrefix(request), method,
			e);
	    }
	}
    }
}
