package eu.essi_lab.profiler.pubsub.handler;

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

import java.io.PrintWriter;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser.PubSubRequestParam;
import eu.essi_lab.profiler.pubsub.PubSubWorker;
import eu.essi_lab.profiler.pubsub.Subscription;

/**
 * @author Fabrizio
 */
public class SubscribeHandler extends DefaultRequestHandler {

    private static final long SLEEP_TIME = 5000;

    private static final String PUB_SUB_PRINT_WRITER_ERROR = "PUB_SUB_PRINT_WRITER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public String getStringResponse(WebRequest request) throws GSException {

	String query = request.getQueryString();
	PubSubRequestParser reader = new PubSubRequestParser(query);

	GSLoggerFactory.getLogger(getClass()).info("Serving subscribe request: " + query);

	Subscription subscription = new Subscription();

	String subscriptionID = reader.getParamValue(PubSubRequestParam.SUBSCRIPTION_ID);
	Thread.currentThread().setName("PUB-SUB-SUBSCRIBE-HANDLER-" + subscriptionID);

	subscription.setId(subscriptionID);
	subscription.setLabel(reader.getParamValue(PubSubRequestParam.LABEL));
	subscription.setClientID(reader.getParamValue(PubSubRequestParam.CLIENT_ID));
	subscription.setCreationDate(Long.valueOf(reader.getParamValue(PubSubRequestParam.CREATION)));
	subscription.setExpirationDate(Long.valueOf(reader.getParamValue(PubSubRequestParam.EXPIRATION)));// 1 hour
	subscription.setInit(Boolean.parseBoolean(reader.getParamValue(PubSubRequestParam.INIT)));

	subscription.setTimeStart(reader.getParamValue(PubSubRequestParam.TIME_START));
	subscription.setTimeEnd(reader.getParamValue(PubSubRequestParam.TIME_END));
	subscription.setBbox(reader.getParamValue(PubSubRequestParam.BBOX));
	subscription.setSearchTerms(reader.getParamValue(PubSubRequestParam.SEARCH_TERMS));
	subscription.setParents(reader.getParamValue(PubSubRequestParam.PARENTS));
	subscription.setSources(reader.getParamValue(PubSubRequestParam.SOURCES));

	subscription.setStart(reader.getParamValue(PubSubRequestParam.START_INDEX));
	subscription.setPageSize(reader.getParamValue(PubSubRequestParam.COUNT));
	subscription.setSearchFields(reader.getParamValue(PubSubRequestParam.SEARCH_FIELDS));
	subscription.setSpatialRelation(reader.getParamValue(PubSubRequestParam.SPATIAL_RELATION));

	subscription.setTermFrequency(reader.getParamValue(PubSubRequestParam.TERM_FREQUENCY));
	subscription.setExtensionRelation(reader.getParamValue(PubSubRequestParam.EXTENSION_RELATION));
	subscription.setExtensionConcepts(reader.getParamValue(PubSubRequestParam.EXTENSION_CONCEPTS));

	String osQuery = PubSubRequestParam.REQUEST_ID + "=" + reader.getParamValue(PubSubRequestParam.REQUEST_ID) + "&";
	osQuery += PubSubRequestParam.TIME_START + "=" + reader.getParamValue(PubSubRequestParam.TIME_START) + "&";
	osQuery += PubSubRequestParam.TIME_END + "=" + reader.getParamValue(PubSubRequestParam.TIME_END) + "&";
	osQuery += PubSubRequestParam.BBOX + "=" + reader.getParamValue(PubSubRequestParam.BBOX) + "&";
	osQuery += PubSubRequestParam.SEARCH_TERMS + "=" + reader.getParamValue(PubSubRequestParam.SEARCH_TERMS) + "&";
	osQuery += PubSubRequestParam.PARENTS + "=" + reader.getParamValue(PubSubRequestParam.PARENTS) + "&";
	osQuery += PubSubRequestParam.SOURCES + "=" + reader.getParamValue(PubSubRequestParam.SOURCES) + "&";
	osQuery += PubSubRequestParam.OUTPUT_FORMAT + "=" + reader.getParamValue(PubSubRequestParam.OUTPUT_FORMAT);

	subscription.setOpenSearchQuery(osQuery);

	return getResponse(request, subscription);
    }

    /**
     * @param request
     * @param subscription
     * @return
     * @throws GSException
     */
    protected String getResponse(WebRequest request, Subscription subscription) throws GSException {

	request.getServletResponse().setContentType("text/event-stream;charset=UTF-8");
	request.getServletResponse().setHeader("Cache-Control", "no-cache");
	request.getServletResponse().setHeader("Connection", "keep-alive");
	request.getServletResponse().setHeader("Access-Control-Allow-Origin", "*");
	request.getServletResponse().setHeader("Access-Control-Expose-Headers", "*");
	request.getServletResponse().setHeader("Access-Control-Allow-Credentials", "true");

	subscription.setWebRequest(request);
	subscription.setThread(Thread.currentThread());

	PrintWriter out = null;
	try {

	    out = request.getServletResponse().getWriter();
	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve PrintWriter from the WebRequest", ex);

	    throw GSException.createException(//
		    getClass(), //
		    ex.getMessage(), //
		    "Unable to retrieve PrintWriter from the WebRequest", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    PUB_SUB_PRINT_WRITER_ERROR, //
		    ex); //
	}

	subscription.setWriter(out);
	subscription.setRequestURL(request.getServletRequest().getRequestURL().toString());

	GSLoggerFactory.getLogger(getClass()).info("Subscription " + subscription.getId() + " ready");

	// waits for a free slot
	while (PubSubWorker.getInstance().getSubscriptions().size() == PubSubWorker.MAX_SUBSCRIPTIONS) {

	    try {
		GSLoggerFactory.getLogger(getClass()).warn("No free slots available at the moment");

		// notifies the client that no slots are available
		PubSubWorker.getInstance().notify(subscription.getWriter(), null, null);

		Thread.sleep(SLEEP_TIME);
	    } catch (InterruptedException e) {
		GSLoggerFactory.getLogger(getClass()).warn("Interrupted!", e);
		Thread.currentThread().interrupt();
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Free slot available");

	// makes the subscription
	PubSubWorker.getInstance().subscribe(subscription);
	GSLoggerFactory.getLogger(getClass()).info("Subscription " + subscription.getId() + " submitted");

	// loops until the subscription expires or it is canceled
	while (true) {
	    try {
		Thread.sleep(SLEEP_TIME);

		long time = new Date().getTime();
		if (time > subscription.getExpirationDate()) {
		    GSLoggerFactory.getLogger(getClass()).info("Subscription " + subscription.getId() + " expired");
		    PubSubWorker.getInstance().unsubscribe(subscription.getId(), true);

		    return null;
		}

	    } catch (InterruptedException e) {
		GSLoggerFactory.getLogger(getClass()).warn("Interrupted!", e);
		Thread.currentThread().interrupt();
	    }
	}
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return null;
    }
}
