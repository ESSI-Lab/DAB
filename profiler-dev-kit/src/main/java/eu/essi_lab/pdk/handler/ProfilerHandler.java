package eu.essi_lab.pdk.handler;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.MessageResponse;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.count.AbstractCountResponse;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;
import eu.essi_lab.pdk.rsm.MessageResponseMapper;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IRequestExecutor;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchInfoPublisher;
public abstract class ProfilerHandler//
<//
	M extends RequestMessage, //
	I, //
	O, //
	CR extends AbstractCountResponse, //
	IN extends MessageResponse<I, CR>, //
	OUT extends MessageResponse<O, CR> //
> implements WebRequestDelegatorHandler<M> {

    private IRequestExecutor<M, I, CR, IN> executor;
    private WebRequestTransformer<M> transformer;
    private MessageResponseMapper<M, I, O, CR, IN, OUT> mapper;
    private MessageResponseFormatter<M, O, CR, OUT> formatter;

    private transient Logger logger = GSLoggerFactory.getLogger(getClass());

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
    @Override
    public final M handleWebRequest(WebRequest request) throws GSException {

	onHandlingStarted(request);

	logger.info("[1/5] Request transformation STARTED");

	M message = getRequestTransformer().transform(request);

	logger.info("[1/5] Request transformation ENDED");

	onTransformedRequest(message);

	try {
	    ElasticsearchInfoPublisher publisher = new ElasticsearchInfoPublisher(//
		    ConfigurationUtils.getGIStatsEndpoint(), //
		    ConfigurationUtils.getGIStatsDbname(), //
		    ConfigurationUtils.getGIStatsUser(), //
		    ConfigurationUtils.getGIStatsPassword(), //
		    message.getRequestId(), //
		    message.getWebRequest().getRequestContext());
	    //
	    // publishes message info
	    //
	    publisher.publish(message);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error initializing ElasticSearch: {}", e.getMessage());
	}

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
    @Override
    public Response handleMessageRequest(M message) throws GSException {

	String context = message.getWebRequest().getRequestContext();

	String rid = message.getWebRequest().getRequestId();

	Optional<WebRequest> owr = Optional.ofNullable(message.getWebRequest());

	IN executorResponse = null;

	logger.info("[2/5] Message authorization check STARTED");
	PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MESSAGE_AUTHORIZATION, rid, owr);

	boolean authorized = getExecutor().isAuthorized(message);
	
	if (message.getRequestAbsolutePath().startsWith("http://localhost")){
	    // developer machine
	    authorized = true;
	}
	
	pl.logPerformance(logger);
	logger.info("[2/5] Message authorization check ENDED");

	logger.info("Message authorization {}", (authorized ? "approved" : "denied"));

	if (!authorized) {

	    return handleNotAuthorizedRequest(message);
	}

	boolean isIterated = message.isIteratedWorkflow();
	if (isIterated) {

	    return handleIteratedWorkflow(message);
	}

	logger.info("[3/5] Result set retrieving STARTED");
	pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_RETRIEVING, rid, owr);

	executorResponse = getExecutor().retrieve(message);

	pl.logPerformance(logger);
	logger.info("[3/5] Result set retrieving ENDED");

	onRetrievedMessageResponse(message, executorResponse);

	logger.info("[4/5] Result set mapping STARTED");
	pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_MAPPING, rid, owr);

	OUT mappedResponse = getMessageResponseMapper().map(message, executorResponse);

	ElasticsearchInfoPublisher publisher = null;

	try {
	    publisher = new ElasticsearchInfoPublisher(//
		    ConfigurationUtils.getGIStatsEndpoint(), //
		    ConfigurationUtils.getGIStatsDbname(), //
		    ConfigurationUtils.getGIStatsUser(), //
		    ConfigurationUtils.getGIStatsPassword(), //
		    message.getRequestId(), //
		    context);

	    //
	    // publishes mapped response info
	    //
	    publisher.publish(mappedResponse);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error initializing ElasticSearch: {}", e.getMessage());
	}

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

	String context = message.getWebRequest().getRequestContext();

	String rid = message.getWebRequest().getRequestId();

	Optional<WebRequest> owr = Optional.ofNullable(message.getWebRequest());

	IN executorResponse = null;
	OUT mappedResponse = null;
	ArrayList<O> completeList = new ArrayList<>();
	int pageSize = message.getPage().getSize();
	int maxIterations = -1;
	int iterationsCounter = 1;
	int responseCount = 0;
	Page page = message.getPage();

	logger.info("Handling of iterated workflow STARTED");
	PerformanceLogger workflowPl = new PerformanceLogger(PerformanceLogger.PerformancePhase.ITERATED_WORKFLOW, rid, owr);

	do {

	    logger.info("Iteration [" + iterationsCounter + "/" + (maxIterations == -1 ? "X" : maxIterations) + "] STARTED");

	    logger.info("[3/5] Result set retrieving STARTED");
	    PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_RETRIEVING, rid, owr);

	    executorResponse = getExecutor().retrieve(message);

	    pl.logPerformance(logger);
	    logger.info("[3/5] Result set retrieving ENDED");

	    onRetrievedMessageResponse(message, executorResponse);

	    //
	    // total number of resources to discover
	    //
	    responseCount = executorResponse.getCountResponse().getCount();
	    //
	    // number of iterations to do
	    //
	    maxIterations = (int) (Math.ceil((double) responseCount / pageSize));
	    //
	    // updates the page start value
	    //
	    page.setStart(page.getStart() + pageSize);

	    logger.info("[4/5] Result set mapping STARTED");
	    pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_MAPPING, rid, owr);

	    mappedResponse = getMessageResponseMapper().map(message, executorResponse);

	    List<O> resultsList = mappedResponse.getResultsList();

	    //
	    // adds the partial results to the complete list
	    //
	    completeList.addAll(resultsList);

	    try {
		ElasticsearchInfoPublisher publisher = new ElasticsearchInfoPublisher(//
			ConfigurationUtils.getGIStatsEndpoint(), //
			ConfigurationUtils.getGIStatsDbname(), //
			ConfigurationUtils.getGIStatsUser(), //
			ConfigurationUtils.getGIStatsPassword(), //
			message.getRequestId(), //
			context);
		//
		// publishes mapped response info
		//
		publisher.publish(mappedResponse);
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("Error initializing ElasticSearch: {}", e.getMessage());
	    }

	    pl.logPerformance(logger);

	    logger.info("[4/5] Result set mapping ENDED");

	    onMappedMessageResponse(executorResponse, mappedResponse);

	    logger.info("Iteration [" + iterationsCounter + "/" + (maxIterations == -1 ? "X" : maxIterations) + "] ENDED");

	    iterationsCounter++;

	} while (responseCount > completeList.size());

	logger.info("Handling of iterated workflow ENDED");
	workflowPl.logPerformance(logger);

	logger.info("[5/5] Result set formatting STARTED");
	PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.RESULT_SET_FORMATTING, rid, owr);

	//
	// sets the complete list to the result set
	//
	mappedResponse.setResultsList(completeList);

	Response response = getMessageResponseFormatter().format(message, mappedResponse);

	pl.logPerformance(logger);
	logger.info("[5/5] Result set formatting ENDED");

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
