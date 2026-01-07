package eu.essi_lab.pdk;

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
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisTool {

    private JedisPool pool;
    private String endpoint;
    private int port;

    public RedisTool(String endpoint, int port) {
	this.pool = new JedisPool(endpoint, port);
	this.endpoint = endpoint;
	this.port = port;
    }

    public void printSummary(String hash) {
	Jedis jedis = pool.getResource();

	System.out.println("Summary of REDIS db at " + endpoint + ":" + port);

	System.out.println("Requests: " + jedis.lrange("{" + hash + "}.request", 0, -1));

	System.out.println("Executing: " + jedis.lrange("{" + hash + "}.executing", 0, -1));

	Set<String> executingKeys = jedis.keys("{" + hash + "}.executing*");
	for (String key : executingKeys) {
	    System.out.println("Executing on: " + jedis.lrange(key, 0, -1));
	}
	Set<String> waitingKeys = jedis.keys("{" + hash + "}.waiting*");
	for (String key : waitingKeys) {
	    System.out.println("Waiting on: " + jedis.lrange(key, 0, -1));
	}
	System.out.println("\n");
    }

    public void printAll() {
	Jedis jedis = pool.getResource();

	System.out.println("Content of REDIS db at " + endpoint + ":" + port);
	List<String> keys = new ArrayList<>(jedis.keys("*"));
	keys.sort(null);
	// Iterate through keys

	for (String key : keys) {
	    // System.out.println(key);
	    // if(true)
	    // continue;

	    if (key.startsWith("backup")) {
		continue;
	    }
	    String type = jedis.type(key);
	    System.out.print("[" + type.toUpperCase() + "]" + key + ": ");

	    // Determine the type of value associated with the key

	    // Retrieve and print values based on their type
	    switch (type) {
	    case "string":
		System.out.println(jedis.get(key));
		break;
	    case "list":
		System.out.println(jedis.lrange(key, 0, -1));
		break;
	    case "set":
		System.out.println(jedis.smembers(key));
		break;
	    case "zset":
		System.out.println(jedis.zrange(key, 0, -1));
		break;
	    case "hash":
		System.out.println(jedis.hgetAll(key));
		break;
	    default:
		System.out.println("Unsupported data type for key: " + type);
	    }
	}
	System.out.println("\n\n");

	jedis.close();

    }

    public void deleteDatabase() {
	Jedis jedis = pool.getResource();

	Set<String> keys = jedis.keys("req*");
	for (String key : keys) {
	    jedis.del(key);
	}
	jedis.close();
    }

    public void deleteKeysWithPattern(String pattern) {
	Jedis jedis = pool.getResource();

	Set<String> keys = jedis.keys(pattern);
	for (String key : keys) {
	    jedis.del(key);
	}
	jedis.close();
    }

    public void deleteKey(String... keys) {
	Jedis jedis = pool.getResource();

	for (String key : keys) {
	    jedis.del(key);
	}
	jedis.close();
    }

    public void close() {
	pool.close();

    }

    public static void main(String[] args) {
	RedisTool tool = new RedisTool("essi-lab.eu", 6379);

	Jedis jedis = tool.getJedis();

	// boolean exist = jedis.exists("{prod-access}.request#397d10b9-8704-4650-a167-16556ede78d9");
	// System.out.println(exist);

	// List<String> requests = jedis.lrange("{prod-access}.executing_on_149.139.19.86", 0, -1);
	// for (String req : requests) {
	// System.out.println(req);
	// }

	System.out.println(jedis.hgetAll("{prod-intensive}.request#aa3251bf-3269-4104-ab4d-cd24855c0027"));

	jedis.close();

	tool.close();
    }

    public Jedis getJedis() {
	return pool.getResource();
    }

    public JedisPool getPool() {
	return pool;
    }

    public List<String> getListMembers(String list) {
	try (Jedis jedis = pool.getResource()) {
	    List<String> ret = jedis.lrange(list, 0, -1);
	    return ret;
	}

    }
    
    public Set<String> getSetMembers(String list) {
	try (Jedis jedis = pool.getResource()) {
	    Set<String> ret = jedis.smembers(list);
	    return ret;
	}

    }

    public String getValue(String key) {
	try (Jedis jedis = pool.getResource()) {
	    String ret = jedis.get(key);
	    return ret;
	}
    }

    public String getHashValue(String key, String field) {
	try (Jedis jedis = pool.getResource()) {
	    String ret = jedis.hget(key,field);
	    return ret;
	}
    }

}
