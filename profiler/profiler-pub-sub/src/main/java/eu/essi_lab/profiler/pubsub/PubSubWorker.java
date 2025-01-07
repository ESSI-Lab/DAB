package eu.essi_lab.profiler.pubsub;

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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DiscoveryHandler;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatterFactory;
import eu.essi_lab.pdk.rsf.impl.json.jsapi._2_0.JS_API_ResultSetFormatter_2_0;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapperFactory;
import eu.essi_lab.pdk.rsm.impl.json.jsapi.JS_API_ResultSetMapper;

/**
 * @author Fabrizio
 */
public class PubSubWorker implements Runnable {

    public static final int MAX_SUBSCRIPTIONS = 50;
    private static final long REQUEST_INTERVAL = 3000;
    private static final boolean SEND_EMPTY_EVENTS = true;
    private static int updatesCheckInterval = 1000 * 60; // 60 seconds

    private List<Subscription> subscriptions;

    private Long from;
    private Long until;
    private boolean resynch;
    private Thread thread;
    private static PubSubWorker instance = new PubSubWorker();
    private String queryKey = "query";
    private String erroKey = "error";

    private PubSubWorker() {

	subscriptions = new ArrayList<>();

	thread = new Thread(this);
	thread.setName("PUB-SUB-WORKER");
	thread.start();
    }

    public static PubSubWorker getInstance() {

	return instance;
    }

    @Override
    public void run() {

	while (true) {

	    synchronized (PubSubWorker.this) {

		until = new Date().getTime();

		HashMap<String, HashSet<Subscription>> map = groupByConstraints();

		Collection<HashSet<Subscription>> values = map.values();

		for (HashSet<Subscription> set : values) {

		    Subscription[] subs = set.toArray(new Subscription[] {});
		    JSONObject object = null;

		    for (int i = 0; i < subs.length; i++) {

			Subscription sub = subs[i];

			while (i == 0) {

			    Object monitor = new Object();
			    synchronized (monitor) {
				try {
				    monitor.wait(REQUEST_INTERVAL);
				} catch (InterruptedException e) {
				    GSLoggerFactory.getLogger(getClass()).warn("Interrupted", e);

				    Thread.currentThread().interrupt();
				}
			    }

			    object = query(sub);
			}

			PrintWriter writer = sub.getWriter();
			notify(writer, object, sub.getId());

			sub.setEnabled(true);
		    }
		}

		// the time occurred to perform the queries plus the total request interval
		long td = System.currentTimeMillis() - until;

		if (resynch) {
		    from = until - td;
		    resynch = false;
		} else {
		    from = new Date().getTime() - td;
		}
	    }

	    try {
		Thread.sleep(updatesCheckInterval);
	    } catch (InterruptedException e) {
		GSLoggerFactory.getLogger(getClass()).warn("Interrupted", e);

		Thread.currentThread().interrupt();

	    }
	}
    }

    public void subscribe(Subscription subscription) {

	synchronized (this) {

	    if (!checkSubscription(subscription)) {

		GSLoggerFactory.getLogger(getClass()).debug("Subscription {} rejected", subscription.getLabel());

		notify(subscription.getWriter(), null, subscription.getId());

		return;

	    }

	    /**
	     * ( 1 ) wake up (in case it's sleeping) and adds the new subscribed
	     */
	    from = null;
	    if (until == null) {
		until = new Date().getTime();
	    }

	    thread.interrupt();
	    subscriptions.add(subscription);

	    /**
	     * ( 2 ) notifies the new subscribed (if it wants -> subscription.isInit()) with the
	     * changes from (null) until the last until value
	     */
	    if (subscription.isInit()) {

		JSONObject result = query(subscription);
		notify(subscription.getWriter(), result, subscription.getId());
	    }

	    /**
	     * ( 3 ) notifies all the subscribed with the changes from (last until value) until (now)
	     */
	    from = until;
	    resynch = true;
	}
    }

    public boolean unsubscribe(String subscriptionID, boolean expired) {

	synchronized (this) {

	    Iterator<Subscription> it = subscriptions.iterator();
	    while (it.hasNext()) {
		Subscription next = it.next();
		if (next.getId().equals(subscriptionID)) {

		    JSONObject data = new JSONObject();

		    if (expired) {
			next.getWriter().print("event: expiration\n");
			data.put("event", "expiration");
		    } else {
			next.getWriter().print("event: close\n");
			data.put("event", "close");
		    }

		    data.put("subscriptionID", subscriptionID);
		    next.getWriter().print("data: " + data.toString() + "\n\n");

		    next.getWriter().flush();
		    next.getWriter().close();

		    // interrupts the subscription thread
		    next.getThread().interrupt();
		    it.remove();

		    return true;
		}
	    }

	    return false;
	}
    }

    public List<Subscription> getSubscriptions() {

	return subscriptions;
    }

    /**
     * @param out
     * @param result the result of the query; it is null in case of too many connections or connection rejected
     * @param subscriptionID it is null in case of too many connections
     */
    public void notify(PrintWriter out, JSONObject result, String subscriptionID) {

	// the data object conform to the SSE protocol.
	// it is null in case of too many connections or connection rejected
	JSONObject data = new JSONObject();

	// the result object contains the reports array and the resultSet object
	if (result != null) {

	    JSONObject resultSet = (JSONObject) result.get("resultSet");

	    String count = resultSet.get("size").toString();
	    data.put("updates", count);

	    if (!resultSet.has(erroKey)) {

		if (!SEND_EMPTY_EVENTS && Integer.parseInt(count) == 0) {
		    return;
		}

		// removes the query object from a result clone
		JSONObject res = new JSONObject(result.toString());
		res.remove(queryKey);

		data.put("result", res.toString());
		out.print("event: " + subscriptionID + "\n");
	    }
	} else {

	    if (subscriptionID == null) {
		data.put(erroKey, "TOO_MANY_CONNECTIONS");
	    } else {
		data.put(erroKey, "SUBSCRIPTION_REJECTED");
	    }

	    out.print("event: error\n");
	}

	// results contains the query object (ignored by the API) if no error has occurred
	if (result != null && result.has(queryKey)) {
	    // the query object is put directly in to the data object
	    JSONObject query = (JSONObject) result.get(queryKey);
	    data.put(queryKey, query);
	}

	JSONObject fromObject = new JSONObject();
	if (from != null) {
	    fromObject.put("millis", from);
	    fromObject.put("ISO", ISO8601DateTimeUtils.getISO8601DateTime(new Date(from)));

	    data.put("from", fromObject);
	}

	JSONObject untilObject = new JSONObject();
	untilObject.put("millis", until);
	untilObject.put("ISO", ISO8601DateTimeUtils.getISO8601DateTime(new Date(until)));

	data.put("until", untilObject);

	// in case of server connection lost, new connection attempt
	// is set after 5 minutes (sse default is 3 seconds)
	out.print("retry: 300000\n");

	out.print("data: " + data.toString() + "\n\n");

	out.flush();
    }

    private JSONObject query(Subscription subscription) {

	JSONObject result = null;

	try {

	    result = execQuery(subscription);

	    JSONObject osQuery = getOpenSearchQuery(subscription);
	    result.put(queryKey, osQuery);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    JSONObject resultSet = new JSONObject();
	    String msg = (e.getMessage() != null) ? e.getMessage() : "internal error";

	    resultSet.put("size", "0");
	    resultSet.put("pageCount", "0");
	    resultSet.put("pageIndex", "0");
	    resultSet.put("pageSize", "0");
	    resultSet.put("start", "0");
	    resultSet.put(erroKey, msg);

	    result = new JSONObject();
	    result.put("resultSet", resultSet);
	}

	return result;
    }

    private HashMap<String, HashSet<Subscription>> groupByConstraints() {

	for (Subscription s : subscriptions) {
	    s.setEquivalent(false);
	}

	HashMap<String, HashSet<Subscription>> map = new HashMap<>();

	synchronized (this) {

	    for (int i = 0; i < subscriptions.size(); i++) {

		Subscription si = subscriptions.get(i);

		for (int j = 0; j < subscriptions.size(); j++) {

		    Subscription sj = subscriptions.get(j);

		    if (!si.equals(sj) && si.equivalent(sj)) {

			String groupID = si.createGroupID();
			si.setGroupID(groupID);
			sj.setGroupID(groupID);

			HashSet<Subscription> set = map.get(groupID);
			if (set == null) {
			    set = new HashSet<>();
			    map.put(groupID, set);
			}

			si.setEquivalent(true);
			sj.setEquivalent(true);

			set.add(si);
			set.add(sj);
		    }
		}
	    }

	    for (Subscription s : subscriptions) {

		if (!s.isEquivalent()) {

		    String groupID = s.createGroupID();
		    s.setGroupID(groupID);
		    HashSet<Subscription> set = new HashSet<>();
		    map.put(groupID, set);
		    set.add(s);
		}
	    }

	    return map;
	}
    }

    private JSONObject execQuery(Subscription subscription) throws GSException {

	DiscoveryHandler<String> discoveryHandler = new DiscoveryHandler<>();

	PubSubRequestTransformer transformer = new PubSubRequestTransformer(from, until);

	discoveryHandler.setMessageResponseMapper( //
		DiscoveryResultSetMapperFactory.loadMappers(//
			JS_API_ResultSetMapper.JS_API_MAPPING_SCHEMA, //
			String.class).get(0));

	discoveryHandler.setMessageResponseFormatter( //
		DiscoveryResultSetFormatterFactory.loadFormatters(//
			JS_API_ResultSetFormatter_2_0.JS_API_FORMATTING_ENCODING, //
			String.class).get(0));

	discoveryHandler.setRequestTransformer(transformer);

	Response response = discoveryHandler.handle(subscription.getWebRequest());

	String entity = response.getEntity().toString();

	return new JSONObject(entity);
    }

    private JSONObject getOpenSearchQuery(Subscription subscription) {

	JSONObject query = new JSONObject();

	String openSearchQuery = subscription.getOpenSearchQuery();

	String requestUrl = subscription.getRequestURL().replace("pubsub/subscribe", "opensearch?") + openSearchQuery;

	if (!requestUrl.endsWith("&outputFormat=application/json")) {
	    query.put("json", requestUrl + "application/json");
	} else {
	    query.put("json", requestUrl);
	    requestUrl = requestUrl.replace("application/json", "");
	}

	query.put("atom", requestUrl + "application/atom+xml");

	return query;
    }

    private boolean checkSubscription(Subscription subscription) {

	for (Subscription s : subscriptions) {

	    if (s.getLabel().equals(subscription.getLabel()) && s.getClientID().equals(subscription.getClientID())) {
		return false;
	    }
	}

	return true;
    }
}
