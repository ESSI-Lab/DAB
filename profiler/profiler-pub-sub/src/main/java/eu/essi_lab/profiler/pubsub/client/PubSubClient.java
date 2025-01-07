package eu.essi_lab.profiler.pubsub.client;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import eu.essi_lab.profiler.pubsub.Subscription;

public class PubSubClient {

    private String endpoint;
    private Subscription subscription;
    private PubSubSubscriber listener;

    public PubSubClient(String endpoint, Subscription subscription, PubSubSubscriber listener) {
	this.endpoint = endpoint;
	this.subscription = subscription;
	this.listener = listener;
    }

    public void connect() throws IOException {

	String subscribeRequest = "subscriptionID=" + subscription.getId();
	subscribeRequest += "&clientID=" + subscription.getClientID();
	subscribeRequest += "&label=" + subscription.getLabel();
	subscribeRequest += "&creation=" + subscription.getCreationDate();
	subscribeRequest += "&expiration=" + subscription.getExpirationDate();
	subscribeRequest += "&init=" + subscription.isInit();

	if (!endpoint.endsWith("?")) {
	    endpoint += "?";
	}

	URL url = new URL(endpoint + subscribeRequest);
	URLConnection conn = url.openConnection();

	try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

	    String response;

	    while ((response = in.readLine()) != null) {

		listener.onReceive(response);

		if (response.startsWith("data")) {

		    listener.onData(response);
		}

		else if (response.startsWith("event")) {

		    if (response.contains("expiration")) {

			listener.onExpiration();

		    } else if (response.contains("close")) {

			listener.onClose();

		    } else if (response.contains("error")) {

			listener.onError();
		    }
		}
		
		if (response.contains("retry")) {

		    listener.onRetry(response.replaceAll("retry: ", "").trim());
		}
	    }
	}catch(Exception ex){
	    
	    ex.printStackTrace();
	}
    }
}
