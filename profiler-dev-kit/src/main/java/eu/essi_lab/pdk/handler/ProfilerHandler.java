package eu.essi_lab.pdk.handler;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.AbstractMap.SimpleEntry;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.UserBondMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;
import eu.essi_lab.pdk.rsm.MessageResponseMapper;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IRequestExecutor;
import eu.essi_lab.rip.RuntimeInfoProvider;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchInfoPublisher;

/**
 * This {@link WebRequestHandler} is in charge to handle discovery or access requests, according to the type &ltM&gt of
 * the received {@link
 * RequestMessage}. This handler can be registered to a {@link DABPRofiler} with the {@link DABPRofiler#getSelector()}
 * method
 * <h3>The workflow</h3>
 * When the {@link DABPRofiler} invokes the {@link #handle(WebRequest)} method (see
 * {@link DABPRofiler#handle(WebRequest)}), the following
 * workflow is executed:<br>
 * <br>
 * <ol>
 * <li>the {@link WebRequest} is transformed in a {@link RequestMessage} of type &ltM&gt by the
 * {@link #getRequestTransformer()}
 * invoking the
 * {@link WebRequestTransformer#transform(WebRequest)} method</li>
 * <li>the transformed {@link RequestMessage} is checked according to the
 * {@link IRequestExecutor#isAuthorized(RequestMessage, AnonymousUserPolicy)} method</li>
 * <li>the discovery/access operation is executed by an internal implementation of {@link IDiscoveryExecutor} and the
 * {@link MessageResponse} of type &ltI&gt is retrieved</li>
 * <li>the {@link MessageResponse} of type &ltI&gt is mapped in to a {@link MessageResponse} of type &ltT&gt by the
 * {@link #getMessageResponseMapper()} invoking the {@link MessageResponseMapper#map(RequestMessage, MessageResponse)}
 * method</li>
 * <li>the {@link MessageResponse} of type &ltT&gt is formatted in a {@link Response} entity by the
 * {@link #getMessageResponseFormatter()} invoking the
 * {@link MessageResponseFormatter#format(RequestMessage, MessageResponse)}
 * method</li>
 * </ol>
 * The same workflow by including also the <code>on</code> methods (in bold):<br>
 * <br>
 * <ol>
 * <li><b>the {@link #onHandlingStarted(WebRequest)} method is invoked</b></li>
 * <li>the {@link WebRequest} is transformed in a {@link RequestMessage} of type &ltM&gt by the
 * {@link #getRequestTransformer()}
 * invoking the
 * {@link WebRequestTransformer#transform(WebRequest)} method</li>
 * <li><b>the {@link #onTransformedRequest(RequestMessage)} method is invoked</b></li>
 * <li>the discovery/access operation is executed by an internal implementation of {@link IDiscoveryExecutor} and the
 * {@link MessageResponse} of of type &ltI&gt is retrieved</li>
 * <li><b>the {@link #onRetrievedMessageResponse(RequestMessage, MessageResponse)} method is invoked</b></li>
 * <li>the {@link MessageResponse} of of type &ltI&gt is mapped in to a {@link MessageResponse} of type &ltT&gt by the
 * {@link #getMessageResponseMapper()} invoking the {@link MessageResponseMapper#map(RequestMessage, MessageResponse)}
 * method</li>
 * <li><b>the {@link #onMappedMessageResponse(MessageResponse, MessageResponse)} method is invoked</b></li>
 * <li>the {@link MessageResponse} of of type &ltT&gt is formatted in a {@link Response} entity by the
 * {@link #getMessageResponseFormatter()} invoking the
 * {@link MessageResponseFormatter#format(RequestMessage, MessageResponse)}
 * method</li>
 * <li><b>the {@link #onHandlingEnded(Response)} method is invoked</b></li>
 * </ol>
 * <h3>Usage Notes</h3>
 * This handler is a <i>strong composition</i> of {@link IRequestExecutor}, {@link WebRequestTransformer},
 * {@link MessageResponseMapper} and {@link MessageResponseFormatter}. According to the <i>strategy pattern</i>
 * its behavior can be modified also at runtime using the the <code>set</code> methods. Because of this, in the most
 * part of the cases,
 * there is no need to extend this class. In some particular cases, for example if more control of the above
 * workflow is needed, subclasses can provide an implementation of the <code>on</code> methods<br>
 * <br>
 *
 * @param <M> the type of the incoming {@link RequestMessage}
 * @param <I> the type of the resources provided by the {@link MessageResponse} <code>IN</code>
 * @param <O> the type of the resources provided by the {@link MessageResponse} <code>OUT</code>
 * @param <CR> the type of the {@link AbstractCountResponse} provided as parameter to <code>IN</code> and
 *        <code>OUT</code>
 * @param <IN> the type of {@link MessageResponse} provided as input to the {@link IRequestExecutor} and {@link
 *        MessageResponseMapper}</code>
 * @param <OUT> the type of the {@link MessageResponse} generated as result of the mapping by the
 *        {@link MessageResponseMapper} and provided
 *        as input for the {@link MessageResponseFormatter}
 * @author Fabrizio
 */
public abstract class ProfilerHandler//
<//
	M extends RequestMessage, //
	I, //
	O, //
	CR extends AbstractCountResponse, //
	IN extends MessageResponse<I, CR>, //
	OUT extends MessageResponse<O, CR> //
> implements WebRequestHandler {

    /**
     * 
     */
    private static final int PARTIAL_MODE_PAGE_SIZE = 10;

    private IRequestExecutor<M, I, CR, IN> executor;
    private WebRequestTransformer<M> transformer;
    private MessageResponseMapper<M, I, O, CR, IN, OUT> mapper;
    private MessageResponseFormatter<M, O, CR, OUT> formatter;

    private Logger logger = GSLoggerFactory.getLogger(getClass());

    protected ProfilerHandler() {

	executor = createExecutor();
    }

    /**
     * Handles the supplied <code>request</code> delegating to the two phases:
     * <ol>
     * <li>handleWebRequest</li>
     * <li>handleMessageRequest</li>
     * </ol>
     *
     * @throws GSException if errors occurred during the workflow execution
     */
    @Override
    public final Response handle(WebRequest request) throws GSException {

	M message = handleWebRequest(request);

	return handleMessageRequest(message);
    }

    /**
     * Handles the supplied <code>request</code> by executing the following workflow:
     * <ol>
     * <li>the {@link #onHandlingStarted(WebRequest)} method is invoked</li>
     * <li>the {@link WebRequest} is transformed in a {@link RequestMessage} of type &lt;M&gt; by the
     * {@link #getRequestTransformer()}
     * invoking the
     * {@link WebRequestTransformer#transform(WebRequest)} method</li>
     * <li>the {@link #onTransformedRequest(RequestMessage)} method is invoked</li>
     * </ol>
     *
     * @throws GSException if errors occurred during the workflow execution
     */
    public final M handleWebRequest(WebRequest request) throws GSException {

	onHandlingStarted(request);

	logger.info("[1/5] Request transformation STARTED");

	M message = getRequestTransformer().transform(request);

	logger.info("[1/5] Request transformation ENDED");

	onTransformedRequest(message);

	publish(message, message);

	return message;
    }

    /**
     * Handles the supplied <code>request</code> by executing the following workflow:
     * <ol>
     * <li>the discovery/access operation is executed by an internal implementation of {@link IDiscoveryExecutor} and
     * the
     * {@link MessageResponse} of type &lt;I&gt; is retrieved</li>
     * <li>the {@link #onRetrievedMessageResponse(RequestMessage, MessageResponse)} method is invoked</li>
     * <li>the {@link MessageResponse} of type &lt;I&gt; is mapped in to a {@link MessageResponse} of type &lt;T&gt; by
     * the
     * {@link #getMessageResponseMapper()} invoking the
     * {@link MessageResponseMapper#map(RequestMessage, MessageResponse)}
     * method</li>
     * <li>the {@link #onMappedMessageResponse(MessageResponse, MessageResponse)} method is invoked</li>
     * <li>the {@link MessageResponse} of type &lt;T&gt; is formatted in a {@link Response} entity by the
     * {@link #getMessageResponseFormatter()} invoking the
     * {@link MessageResponseFormatter#format(RequestMessage, MessageResponse)}
     * method</li>
     * <li>the {@link #onHandlingEnded(Response)} method is invoked</li>
     * </ol>
     *
     * @throws GSException if errors occurred during the workflow execution
     */
    public Response handleMessageRequest(M message) throws GSException {

	String rid = message.getWebRequest().getRequestId();

	Optional<WebRequest> owr = Optional.ofNullable(message.getWebRequest());

	IN executorResponse = null;

	logger.info("[2/5] Message authorization check STARTED");
	PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MESSAGE_AUTHORIZATION, rid, owr);

	boolean authorized = getExecutor().isAuthorized(message);

	pl.logPerformance(logger);
	logger.info("[2/5] Message authorization check ENDED");

	logger.info("Message authorization {}", (authorized ? "approved" : "denied"));

	if (!authorized) {

	    return handleNotAuthorizedRequest(message);
	}

	Optional<IterationMode> iterationMode = message.getIteratedWorkflow();
	if (iterationMode.isPresent()) {

	    return handleIteratedWorkflow(message);
	}

	logger.info("[3/5] Result set retrieving STARTED");
	pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_OVERALL_RETRIEVING, rid, owr);

	executorResponse = getExecutor().retrieve(message);

	pl.logPerformance(logger);
	logger.info("[3/5] Result set retrieving ENDED");

	onRetrievedMessageResponse(message, executorResponse);

	logger.info("[4/5] Result set mapping STARTED");
	pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_MAPPING, rid, owr);

	OUT mappedResponse = getMessageResponseMapper().map(message, executorResponse);

	// set the property handler to the mapped response
	mappedResponse.setPropertyHandler(executorResponse.getPropertyHandler());

	message.getProfilerName().ifPresent(name -> mappedResponse.setProfilerName(name));

	publish(message, mappedResponse);

	pl.logPerformance(logger);
	logger.info("[4/5] Result set mapping ENDED");

	onMappedMessageResponse(executorResponse, mappedResponse);

	logger.info("[5/5] Result set formatting STARTED");
	pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_FORMATTING, rid, owr);

	Response response = getMessageResponseFormatter().format(message, mappedResponse);

	pl.logPerformance(logger);
	logger.info("[5/5] Result set formatting ENDED");

	onHandlingEnded(response);

	return response;
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private Response handleIteratedWorkflow(M message) throws GSException {

	IterationMode iterationMode = message.getIteratedWorkflow().get();

	String rid = message.getWebRequest().getRequestId();

	Optional<WebRequest> owr = Optional.ofNullable(message.getWebRequest());

	IN executorResponse = null;
	OUT mappedResponse = null;
	ArrayList<O> completeList = new ArrayList<>();

	Page page = message.getPage();

	//
	// this is OK for partial iteration mode, but it must be set
	// to the total result set size in case of full iteration mode
	//
	int responseCount = page.getSize();

	int pageSize = iterationMode == IterationMode.FULL_RESPONSE ? page.getSize() : PARTIAL_MODE_PAGE_SIZE;
	page.setSize(pageSize);

	int maxIterations = -1;
	int iterationsCounter = 0;

	logger.info("[" + message.getRequestId() + "] Handling of iterated workflow STARTED");

	logger.info("[" + message.getRequestId() + "] Iteration mode: " + iterationMode);

	PerformanceLogger workflowPl = new PerformanceLogger(PerformanceLogger.PerformancePhase.ITERATED_WORKFLOW, rid, owr);

	Optional<Bond> userBond = Optional.empty();

	if (message instanceof UserBondMessage) {

	    userBond = ((UserBondMessage) message).getUserBond();
	}

	//
	// sorting is necessary to use the OpenSearch search after feature
	//

	message.setSortedFields(SortedFields.of(ResourceProperty.RESOURCE_TIME_STAMP, SortOrder.ASCENDING));

	do {

	    //
	    // set the search after to the message according to the executor response
	    //
	    if (executorResponse != null) {
		executorResponse.getSearchAfter().ifPresent(sa -> message.setSearchAfter(sa));
	    }

	    //
	    // set the original user bond. this is required to avoid that QueryInitializer.initializeQuery at row 87
	    // recursively add user bond at each iteration generating extremely long queries
	    //
	    if (message instanceof UserBondMessage) {

		((UserBondMessage) message).setUserBond(userBond.orElse(null));
	    }

	    logger.info("[" + message.getRequestId() + "] Iteration [" + (iterationsCounter + 1) + "/"
		    + (maxIterations == -1 ? "X" : maxIterations) + "] STARTED");

	    logger.info("[" + message.getRequestId() + "] [3/5] Result set retrieving STARTED");
	    PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_OVERALL_RETRIEVING, rid, owr);

	    executorResponse = getExecutor().retrieve(message);

	    pl.logPerformance(logger);
	    logger.info("[" + message.getRequestId() + "] [3/5] Result set retrieving ENDED");

	    onRetrievedMessageResponse(message, executorResponse);

	    //
	    // for full iteration mode, the number of resources to handle
	    // is set to the size of the result set
	    //
	    if (iterationMode == IterationMode.FULL_RESPONSE) {

		int count = executorResponse.getCountResponse().getCount();

		responseCount = count;
	    }

	    //
	    // number of iterations to do
	    //
	    maxIterations = (int) (Math.ceil((double) responseCount / pageSize));

	    //
	    // updates the page start value
	    //
	    page.setStart(page.getStart() + pageSize);

	    logger.info("[" + message.getRequestId() + "] [4/5] Result set mapping STARTED");
	    pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_MAPPING, rid, owr);

	    mappedResponse = getMessageResponseMapper().map(message, executorResponse);

	    List<O> resultsList = mappedResponse.getResultsList();

	    //
	    // adds the partial results to the complete list
	    //
	    completeList.addAll(resultsList);

	    pl.logPerformance(logger);

	    logger.info("[" + message.getRequestId() + "] [4/5] Result set mapping ENDED");

	    onMappedMessageResponse(executorResponse, mappedResponse);

	    logger.info("[" + message.getRequestId() + "] Iteration [" + (iterationsCounter + 1) + "/" + maxIterations + "] ENDED");

	    iterationsCounter++;

	} while (iterationsCounter < maxIterations);

	logger.info("[" + message.getRequestId() + "] Handling of iterated workflow ENDED");
	workflowPl.logPerformance(logger);

	logger.info("[" + message.getRequestId() + "] [5/5] Result set formatting STARTED");
	PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_FORMATTING, rid, owr);

	//
	// sets the complete list to the result set
	//
	mappedResponse.setResultsList(completeList);

	Response response = getMessageResponseFormatter().format(message, mappedResponse);

	pl.logPerformance(logger);
	logger.info("[" + message.getRequestId() + "] [5/5] Result set formatting ENDED");

	publish(message, mappedResponse);

	onHandlingEnded(response);

	return response;
    }

    /**
     * @param message
     * @return
     */
    protected Response handleNotAuthorizedRequest(M message) {

	return Response.status(Status.FORBIDDEN).build();
    }

    /**
     * @return
     */
    protected abstract IRequestExecutor<M, I, CR, IN> createExecutor();

    /**
     * @return
     */
    public IRequestExecutor<M, I, CR, IN> getExecutor() {

	return executor;
    }

    /**
     * @return
     */
    public WebRequestTransformer<M> getRequestTransformer() {

	return transformer;
    }

    /**
     * @param mapper
     */
    public void setRequestTransformer(WebRequestTransformer<M> mapper) {

	this.transformer = mapper;
    }

    /**
     * @return
     */
    public MessageResponseMapper<M, I, O, CR, IN, OUT> getMessageResponseMapper() {

	return mapper;
    }

    /**
     * @param mapper
     */
    public void setMessageResponseMapper(MessageResponseMapper<M, I, O, CR, IN, OUT> mapper) {

	this.mapper = mapper;
    }

    /**
     * @return the resultSetFormatter
     */
    public MessageResponseFormatter<M, O, CR, OUT> getMessageResponseFormatter() {

	return formatter;
    }

    /**
     * @param formatter
     */
    public void setMessageResponseFormatter(MessageResponseFormatter<M, O, CR, OUT> formatter) {

	this.formatter = formatter;
    }

    /**
     * @param message
     * @param provider
     */
    protected void publish(RequestMessage message, RuntimeInfoProvider provider) {

	Optional<ElasticsearchInfoPublisher> publisher = ElasticsearchInfoPublisher.create(message.getWebRequest());

	if (publisher.isPresent()) {

	    try {

		publisher.get().publish(provider);

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("Error initializing ElasticSearch: {}", e.getMessage());
	    }
	}
    }

    /**
     * @param request
     */
    protected void onHandlingStarted(WebRequest request) {
    }

    /**
     * @param discoveryMsg
     */
    protected void onTransformedRequest(M discoveryMsg) {
    }

    /**
     * @param discoveryMsg
     * @param messageResponse
     */
    protected void onRetrievedMessageResponse(M discoveryMsg, IN messageResponse) {
    }

    /**
     * @param resSet
     * @param mappedResSet
     */
    protected void onMappedMessageResponse(IN resSet, OUT messageResponse) {
    }

    /**
     * @param response
     */
    protected void onHandlingEnded(Response response) {
    }
}
