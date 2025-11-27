package eu.essi_lab.gssrv.servlet.wmscache;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class WMSCacheStatsOnMemory implements WMSCacheStats {

    // Map for request counts: key = view:layer:reqid
    private final ConcurrentHashMap<String, AtomicLong> reqCount = new ConcurrentHashMap<>();

    // Map for request metadata: key = view:layer:reqid
    private final ConcurrentHashMap<String, String> reqMeta = new ConcurrentHashMap<>();

    // Map for leaderboards: key = view:layer -> Map<reqid, score>
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> leaderboards = new ConcurrentHashMap<>();

    @Override
    public List<String> getViews() {
	Enumeration<String> keys = leaderboards.keys();
	Set<String> ret = new HashSet<String>();
	while (keys.hasMoreElements()) {
	    String key = (String) keys.nextElement();
	    String[] split = key.split(":");
	    String view = split[0];
	    ret.add(view);
	}
	return new ArrayList<String>(ret);
    }

    @Override
    public List<String> getLayers(String view) {
	Enumeration<String> keys = leaderboards.keys();
	Set<String> ret = new HashSet<String>();
	while (keys.hasMoreElements()) {
	    String key = (String) keys.nextElement();
	    String[] split = key.split(":");
	    String v = split[0];
	    String l = key.replace(v + ":", "");
	    if (v.equals(view)) {
		ret.add(l);
	    }
	}
	return new ArrayList<String>(ret);
    }

    private String makeReqCountKey(String view, String layer, String reqid) {
	return view + ":" + layer + ":" + reqid;
    }

    private String makeLeaderboardKey(String view, String layer) {
	return view + ":" + layer;
    }

    @Override
    public long incrementUsage(String view, String layer, String reqid) {
	String key = makeReqCountKey(view, layer, reqid);
	return reqCount.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    @Override
    public void storeRequest(String view, String layer, String reqid, String request) {
	String key = makeReqCountKey(view, layer, reqid);
	reqMeta.put(key, request);
    }

    @Override
    public String loadRequest(String view, String layer, String reqid) {
	String key = makeReqCountKey(view, layer, reqid);
	return reqMeta.get(key);
    }

    @Override
    public void updateLeaderboard(String view, String layer, String reqid) {
	String lbKey = makeLeaderboardKey(view, layer);
	leaderboards.computeIfAbsent(lbKey, k -> new ConcurrentHashMap<>()).merge(reqid, 1.0, Double::sum);
    }

    @Override
    public List<Entry<String, Double>> getTopRequests(String view, String layer, int limit) {
	String lbKey = makeLeaderboardKey(view, layer);
	Map<String, Double> lb = leaderboards.getOrDefault(lbKey, new ConcurrentHashMap<>());
	Set<Entry<String, Double>> lbe = lb.entrySet();
	List<Entry<String, Double>> entries = new ArrayList<>(lbe);

	entries.sort(new Comparator<Entry<String, Double>>() {

	    @Override
	    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
		return o2.getValue().compareTo(o1.getValue());
	    }
	});

	entries = entries.subList(0, Math.min(entries.size(), limit));

	return entries;
    }

    public void clearAll() {
	reqCount.clear();
	reqMeta.clear();
	leaderboards.clear();
    }
}
