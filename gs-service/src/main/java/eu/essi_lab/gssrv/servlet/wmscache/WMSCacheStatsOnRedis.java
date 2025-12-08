package eu.essi_lab.gssrv.servlet.wmscache;

import java.util.ArrayList;
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
import java.util.Map.Entry;
import java.util.Set;

import com.mchange.v1.util.SimpleMapEntry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
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
		result.add(new SimpleMapEntry(t.getElement(), t.getScore()));
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
}
