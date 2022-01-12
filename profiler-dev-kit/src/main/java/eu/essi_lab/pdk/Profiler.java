package eu.essi_lab.pdk;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.lib.servlet.RequestManager;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;
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
public abstract class Profiler implements WebRequestHandler, Pluggable, RuntimeInfoProvider {

    @Override
    public String getBaseType() {
	return "profiler";
    }

    private static final String NO_VALIDATOR_FOUND = "NO_VALIDATOR_FOUND";

    private static final int MAX_SIMULTANEOUS_REQUESTS_PER_IP = 2;

    private transient GSLogger logger = GSLoggerFactory.getLogger(Profiler.class);

    private static boolean everythingIsBlocked = false;

    public static boolean everythingIsBlocked() {
	return everythingIsBlocked;
    }

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
     * Creates a new instance of <code>DiscoveryProfiler</code>
     */
    public Profiler() {
    }

    /**
     * Returns the {@link HandlerSelector}
     *
     * @param request
     * @return a non <code>null</code> {@link HandlerSelector}
     */
    public abstract HandlerSelector getSelector(WebRequest request);

    /**
     * Returns a {@link ProfilerInfo} for this <code>Profiler</code>
     *
     * @return a non <code>null</code> {@link ProfilerInfo}
     */
    public abstract ProfilerInfo getProfilerInfo();

    /**
     * Returns a {@link ProfilerConfigurable} which provide the options supported by this
     * <code>Profiler</code>. This default instance provides information from the {@link #getProfilerInfo()}
     * and allow the profiler to be enabled/disabled
     *
     * @return a non <code>null</code> {@link ProfilerConfigurable}
     */
    public ProfilerConfigurable getConfigurable() {

	return new ProfilerConfigurable(this);
    }

    public static final RequestBouncer bouncer = new RequestBouncer(MAX_SIMULTANEOUS_REQUESTS_PER_IP);

    private static ExpiringCache<String> successRequests;
    static {
	successRequests = new ExpiringCache<String>();
	successRequests.setDuration(1200000);
    }

    private static Integer concurrentRequests = 0;

    private static Integer executingRequests = 0;

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

	logger.traceFreeMemory(getRequestLogPrefix(request) + " FHSM STARTED: ");

	String address = request.getRemoteAddress();

	String rid = request.getRequestId();

	RequestManager.getInstance().addThreadName(rid);

	synchronized (concurrentRequests) {
	    concurrentRequests++;
	    logger.info("{} QUEUED Remote address: {} {} concurrent requests, {} executing", getRequestLogPrefix(request), address,
		    concurrentRequests, executingRequests);
	}

	boolean ret;
	try {
	    ret = bouncer.askForExecutionAndWait(address, rid, 10, TimeUnit.MINUTES);
	} catch (InterruptedException e1) {
	    logger.info("{} INTERRUPTED Remote address: {}", getRequestLogPrefix(request), address);
	    GSException gse = new GSException();
	    ErrorInfo info = new ErrorInfo();
	    info.setUserErrorDescription("Interrupted while waiting for other pending requests to complete from address: " + address);
	    gse.addInfo(info);
	    throw gse;
	}

	if (!ret) {
	    // if after 10 minutes waiting no free slot is made available (e.g. other two requests from this IP are
	    // being executed)
	    logger.info("{} BLOCKED Remote address: {} {} concurrent requests, {} executing", getRequestLogPrefix(request), address,
		    concurrentRequests, executingRequests);
	    GSException gse = new GSException();
	    ErrorInfo info = new ErrorInfo();
	    info.setUserErrorDescription(
		    "Waiting for other pending requests to complete before accepting new requests on this node from address: " + address);
	    gse.addInfo(info);

	    if (successRequests.size() == 0) {
		everythingIsBlocked = true;
	    }

	    throw gse;
	}

	ElasticsearchInfoPublisher publisher = null;
	try {

	    synchronized (executingRequests) {
		executingRequests++;
		logger.info("{} STARTED Remote address: {} {} concurrent requests, {} executing", getRequestLogPrefix(request), address,
			concurrentRequests, executingRequests);
	    }
	    ChronometerInfoProvider chronometer = new ChronometerInfoProvider(TimeFormat.MIN_SEC_MLS);
	    chronometer.start();

	    PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.REQUEST_HANDLING, rid,
		    Optional.ofNullable(request));

	    Optional<String> remoteHost = request.readRemoteHostHeader();

	    GSLoggerFactory.getLogger(getClass()).debug("{} Remote host: {}", getRequestLogPrefix(request), remoteHost.orElse("n.a."));

	    logRequest(request);

	    try {
		publisher = new ElasticsearchInfoPublisher(//
			ConfigurationUtils.getGIStatsEndpoint(), //
			ConfigurationUtils.getGIStatsDbname(), //
			ConfigurationUtils.getGIStatsUser(), //
			ConfigurationUtils.getGIStatsPassword(), //
			rid, //
			request.getRequestContext());
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("Error initializing ElasticSearch: {}", e.getMessage());
	    }
	    //
	    // publishes request and profiler info
	    //
	    if (publisher != null)
		publisher.publish(request);

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
		    throw GSException.createException(getClass(), "Can't find validator", null, ErrorInfo.ERRORTYPE_INTERNAL,
			    ErrorInfo.SEVERITY_ERROR, NO_VALIDATOR_FOUND);
		}

		String validatorInfo = "[using validator: " + validator.getClass().getSimpleName() + "]";
		ValidationMessage message = validator.validate(request);

		// get the validation result
		ValidationResult result = message.getResult();

		// in case of validation failed, throws a GSException with
		// the validation error message
		if (result == ValidationResult.VALIDATION_FAILED) {

		    logger.warn("{} Validation FAILED {}", getRequestLogPrefix(request), validatorInfo);
		    return onValidationFailed(request, message);
		}

		logger.info("{} Validation SUCCESSFUL {}", getRequestLogPrefix(request), validatorInfo);
		onRequestValidated(request, validator, type);

		Response response = handler.handle(request);

		onHandlingEnded(response);

		pl.logPerformance(logger);

		logger.info("{} ENDED - handling time: {}", getRequestLogPrefix(request), chronometer.formatElapsedTime());

		successRequests.put(rid, rid);

		//
		// publishes the elapsed time in milliseconds
		//
		if (publisher != null)
		    publisher.publish(chronometer);

		//
		// publishes response info
		//
		if (publisher != null)
		    publisher.publish(new ResponseInfoProvider(response));

		logger.traceFreeMemory(getRequestLogPrefix(request) + " FHSM ENDED: ");

		return response;
	    }

	} catch (Throwable e) {
	    logger.info("EXCEPTION {}", e.getMessage());

	    throw e;

	} finally {

	    if (publisher != null) {
		publisher.write();
	    }

	    synchronized (concurrentRequests) {
		concurrentRequests--;
	    }
	    synchronized (executingRequests) {
		executingRequests--;
	    }

	    RequestManager.getInstance().printRequestInfo(rid);
	    RequestManager.getInstance().removeRequest(rid);

	    bouncer.notifyExecutionEnded(address, rid);
	}

	logger.warn("{} handler not found!", getRequestLogPrefix(request));
	return onHandlerNotFound(request);
    }

    private String getRequestLogPrefix(WebRequest request) {
	return "[Request handling] REQ# " + request.getRequestId();
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	HashMap<String, List<String>> map = new HashMap<>();
	map.put(RuntimeInfoElement.PROFILER_NAME.getName(), Arrays.asList(getName()));
	map.put(RuntimeInfoElement.PROFILER_TYPE.getName(), Arrays.asList(getProfilerInfo().getServiceType()));
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

	if (!request.isGetRequest()) {

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
