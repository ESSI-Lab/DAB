package eu.essi_lab.gssrv.servlet.wmscache;

import java.util.*;

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

import java.util.Map.Entry;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Tuple;

public class WMSCacheStatsOnRedis implements WMSCacheStats {

    private final JedisPool pool;
    private int port;

    public int getPort() {
	return port;
    }

    public String getHost() {
	return host;
    }

    private String host;

    public WMSCacheStatsOnRedis(String redisHost, int redisPort) {
	this.pool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
	this.host = redisHost;
	this.port = redisPort;
    }

    @Override
    public long incrementUsage(String view, String layer, String reqid) {
	try (Jedis jedis = pool.getResource()) {
	    return jedis.incr("wms:reqcount:view" + view + ":layer:" + layer + ":" + reqid);
	}catch (Exception e) {
	   return 0;
	}
    }

    @Override
    public void storeRequest(String view, String layer, String reqid, String request) {
	try (Jedis jedis = pool.getResource()) {
	    jedis.set("wms:reqmeta:view:" + view + ":layer:" + layer + ":" + reqid, request);
	}catch (Exception e) {
	   
	}
    }

    @Override
    public String loadRequest(String view, String layer, String reqid) {

	try (Jedis jedis = pool.getResource()) {
	    return jedis.get("wms:reqmeta:view:" + view + ":layer:" + layer + ":" + reqid);
	}

    }

    @Override
    public void updateLeaderboard(String view, String layer, String reqid) {

	try (Jedis jedis = pool.getResource()) {
	    jedis.zincrby("wms:reqleaderboard:view" + view + ":layer:" + layer, 1.0, reqid);
	}catch (Exception e) {
	  
	}

    }

    @Override
    public List<Entry<String, Double>> getTopRequests(String view, String layer, int limit) {
	List<Entry<String, Double>> result = new ArrayList<>();

	String key = "wms:reqleaderboard:view:" + view + ":layer:" + layer;

	try (Jedis jedis = pool.getResource()) {

	    List<Tuple> tuples = jedis.zrevrangeWithScores(key, 0, limit - 1);

	    for (Tuple t : tuples) {
		result.add(new AbstractMap.SimpleEntry<>(t.getElement(), t.getScore()));
	    }
	}

	return result;
    }

    @Override
    public List<String> getViews() {
	Set<String> result = new HashSet<>();

	try (Jedis jedis = pool.getResource()) {
	    String cursor = "0";
	    do {
		ScanResult<String> scan = jedis.scan(cursor);
		for (String key : scan.getResult()) {

		    // Match only leaderboard keys
		    if (key.startsWith("wms:reqleaderboard:view")) {

			// Key example:
			// wms:reqleaderboard:viewABC:layer:XYZ

			int start = "wms:reqleaderboard:view".length();
			int end = key.indexOf(":layer:");

			if (end > start) {
			    String view = key.substring(start, end);
			    result.add(view);
			}
		    }
		}
		cursor = scan.getCursor();
	    } while (!cursor.equals("0"));
	}

	return new ArrayList<>(result);
    }

    @Override
    public List<String> getLayers(String view) {
	Set<String> result = new HashSet<>();

	String prefix = "wms:reqleaderboard:view" + view + ":layer:";

	try (Jedis jedis = pool.getResource()) {
	    String cursor = "0";
	    do {
		ScanResult<String> scan = jedis.scan(cursor);
		for (String key : scan.getResult()) {

		    if (key.startsWith(prefix)) {

			// Key example:
			// wms:reqleaderboard:viewABC:layer:LAYER1

			String layer = key.substring(prefix.length());
			result.add(layer);
		    }
		}
		cursor = scan.getCursor();
	    } while (!cursor.equals("0"));
	}

	return new ArrayList<>(result);
    }

    @Override
    public void deleteLayer(String view, String layer) {

	GSLoggerFactory.getLogger(getClass()).info("Deleting WMS cache stats for view={}, layer={}", view, layer);

	try (Jedis jedis = pool.getResource()) {

	    String primaryLeaderboard = leaderboardKey(view, layer);
	    String alternateLeaderboard = alternateLeaderboardKey(view, layer);

	    List<String> reqIds = jedis.zrange(primaryLeaderboard, 0, -1);
	    if (reqIds.isEmpty()) {
		reqIds = jedis.zrange(alternateLeaderboard, 0, -1);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("WMS cache stats delete: {} request id(s) listed in leaderboard",
		    reqIds.size());

	    long leaderboardRemoved = jedis.del(primaryLeaderboard, alternateLeaderboard);

	    int removedByReqId = 0;
	    if (!reqIds.isEmpty()) {
		Pipeline pipeline = jedis.pipelined();
		for (String reqId : reqIds) {
		    pipeline.del(countKey(view, layer, reqId));
		    pipeline.del(metaKey(view, layer, reqId));
		}
		pipeline.sync();
		removedByReqId = reqIds.size() * 2;
		GSLoggerFactory.getLogger(getClass()).info(
			"WMS cache stats delete: removed {} count/meta key(s) from leaderboard members", removedByReqId);
	    }

	    int orphanCountKeys = 0;
	    int orphanMetaKeys = 0;
	    if (reqIds.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).info(
			"WMS cache stats delete: no leaderboard members; removing keys by prefix scan");
		orphanCountKeys = deleteKeysWithPrefix(jedis, countKeyPrefix(view, layer), "count");
		orphanMetaKeys = deleteKeysWithPrefix(jedis, metaKeyPrefix(view, layer), "meta");
	    }

	    GSLoggerFactory.getLogger(getClass()).info(
		    "WMS cache stats delete completed for view={}, layer={} (leaderboardKeys={}, byReqId={}, orphanCount={}, orphanMeta={})",
		    view, layer, leaderboardRemoved, removedByReqId, orphanCountKeys, orphanMetaKeys);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Failed to delete WMS cache stats for view={}, layer={}", view, layer,
		    e);
	}
    }

    private static String countKeyPrefix(String view, String layer) {

	return "wms:reqcount:view" + view + ":layer:" + layer + ":";
    }

    private static String metaKeyPrefix(String view, String layer) {

	return "wms:reqmeta:view:" + view + ":layer:" + layer + ":";
    }

    private static String countKey(String view, String layer, String reqId) {

	return countKeyPrefix(view, layer) + reqId;
    }

    private static String metaKey(String view, String layer, String reqId) {

	return metaKeyPrefix(view, layer) + reqId;
    }

    private static String leaderboardKey(String view, String layer) {

	return "wms:reqleaderboard:view" + view + ":layer:" + layer;
    }

    /** Legacy/alternate key shape used by {@link #getTopRequests}. */
    private static String alternateLeaderboardKey(String view, String layer) {

	return "wms:reqleaderboard:view:" + view + ":layer:" + layer;
    }

    /**
     * Deletes keys matching {@code prefix*} via SCAN (scoped to the prefix, not the whole database).
     */
    private int deleteKeysWithPrefix(Jedis jedis, String prefix, String kind) {

	ScanParams scanParams = new ScanParams().match(prefix + "*").count(500);
	String cursor = ScanParams.SCAN_POINTER_START;
	int deleted = 0;
	int page = 0;

	do {

	    page++;
	    ScanResult<String> scan = jedis.scan(cursor, scanParams);
	    List<String> keys = scan.getResult();

	    if (!keys.isEmpty()) {
		deleted += keys.size();
		jedis.del(keys.toArray(String[]::new));
		GSLoggerFactory.getLogger(getClass()).info(
			"WMS cache stats delete ({}): page={}, keys={}", kind, page, keys.size());
	    }

	    cursor = scan.getCursor();

	} while (!cursor.equals(ScanParams.SCAN_POINTER_START));

	return deleted;
    }
}
