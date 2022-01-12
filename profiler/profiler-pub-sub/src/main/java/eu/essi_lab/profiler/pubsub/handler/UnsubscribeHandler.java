package eu.essi_lab.profiler.pubsub.handler;

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

import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser;
import eu.essi_lab.profiler.pubsub.PubSubRequestParser.PubSubRequestParam;
import eu.essi_lab.profiler.pubsub.PubSubWorker;
public class UnsubscribeHandler extends SubscriptionsHandler {

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	String query = webRequest.getQueryString();

	PubSubRequestParser reader = new PubSubRequestParser(query);

	String subId = reader.getParamValue(PubSubRequestParam.SUBSCRIPTION_ID);

	return getResponse(webRequest, subId);
    }

    @Override
    public String getResponse(WebRequest webRequest, String subId) {

	String query = webRequest.getQueryString();
	// CSWUnsubscribeHandler do not set the client ID
	if (query != null) {
	    PubSubRequestParser reader = new PubSubRequestParser(query);
	    String clientID = reader.getParamValue(PubSubRequestParam.CLIENT_ID);
	    GSLoggerFactory.getLogger(getClass()).info("Serving unsubscribe request: " + clientID);
	}

	GSLoggerFactory.getLogger(getClass()).info("Subscription id: " + subId);

	boolean done = PubSubWorker.getInstance().unsubscribe(subId, false);

	JSONObject obj = new JSONObject();
	if (done) {
	    GSLoggerFactory.getLogger(getClass()).info("Subscription " + subId + " canceled");
	    obj.put("message", "Subscription " + subId + " canceled");
	    obj.put("id", subId);

	} else {
	    GSLoggerFactory.getLogger(getClass()).info("Subscription " + subId + " not found");
	    obj.put("message", "Subscription " + subId + " not found");
	    obj.put("id", subId);
	}

	String callback = getCallback(query);

	return getOut(callback, obj.toString(3));
    }
}
