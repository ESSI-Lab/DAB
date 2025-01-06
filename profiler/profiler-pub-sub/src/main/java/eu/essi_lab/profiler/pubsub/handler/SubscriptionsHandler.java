package eu.essi_lab.profiler.pubsub.handler;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser.PubSubRequestParam;
import eu.essi_lab.profiler.pubsub.PubSubWorker;
import eu.essi_lab.profiler.pubsub.Subscription;

/**
 * @author Fabrizio
 */
public class SubscriptionsHandler extends DefaultRequestHandler {

    private static final String CALLBACK_EQUAL = "callback=";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return message;
    }

    @Override
    public String getStringResponse(WebRequest request) throws GSException {

	return getResponse(request, null);
    }

    /**
     * @param request
     * @param subId
     * @return
     * @throws GSException
     */
    protected String getResponse(WebRequest request, String subId) {

	String query = request.getQueryString();
	String clientID = null;

	if (query != null) {
	    PubSubRequestParser reader = new PubSubRequestParser(query);
	    clientID = reader.getParamValue(PubSubRequestParam.CLIENT_ID);
	    GSLoggerFactory.getLogger(getClass()).info("Serving get subscriptions request: {}", clientID);
	}

	ArrayList<Subscription> subscriptions = new ArrayList<>();
	subscriptions.addAll(PubSubWorker.getInstance().getSubscriptions());

	JSONArray array = new JSONArray();
	for (Subscription sub : subscriptions) {
	    if (checkStringValue(clientID) && sub.getClientID().equals(clientID)) {

		if ((subId == null) || sub.getId().equals(subId))
		    array.put(sub.toJSON());

	    } else if (!checkStringValue(clientID)) {
		array.put(sub.toJSON());
	    }
	}

	String callback = getCallback(query);
	return getOut(callback, array.toString(3));
    }

    /**
     * @param callback
     * @param obj
     * @return
     */
    protected String getOut(String callback, String obj) {

	if (callback == null) {
	    return obj;
	}

	return callback + "(" + obj + ")";
    }

    /**
     * @param query
     * @return
     */
    protected String getCallback(String query) {

	if (query == null || query.indexOf(CALLBACK_EQUAL) == -1) {
	    return null;
	}

	return query.substring(query.indexOf(CALLBACK_EQUAL) + CALLBACK_EQUAL.length(), query.lastIndexOf('&'));
    }

    /**
     * @param value
     * @return
     */
    protected boolean checkStringValue(String value) {

	return value != null && !value.equals("");
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_JSON_TYPE;
    }
}
