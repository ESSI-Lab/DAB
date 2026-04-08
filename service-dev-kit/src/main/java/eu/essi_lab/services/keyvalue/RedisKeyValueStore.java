package eu.essi_lab.services.keyvalue;

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

import redis.clients.jedis.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class RedisKeyValueStore implements KeyValueStore {

    private final JedisPool pool;

    /**
     * @param pool
     */
    public RedisKeyValueStore(JedisPool pool) {

	this.pool = pool;
    }

    /**
     * @param serviceId
     * @param key
     * @param value
     */
    @Override
    public void upsert(String serviceId, String key, String value) {

	try (Jedis jedis = pool.getResource()) {

	    jedis.hset(key(serviceId), key, value);
	    jedis.sadd(memberKey(), serviceId);
	}
    }

    /**
     * @param serviceId
     * @return
     */
    @Override
    public List<Map.Entry<String, String>> get(String serviceId) {

	try (Jedis jedis = pool.getResource()) {

	    Map<String, String> map = jedis.hgetAll(key(serviceId));

	    return map.entrySet().stream().toList();
	}
    }

    /**
     * @param serviceId
     * @param key
     */
    @Override
    public void remove(String serviceId, String key) {

	try (Jedis jedis = pool.getResource()) {

	    jedis.hdel(key(serviceId), key);
	}
    }

    /**
     * @param serviceId
     */
    @Override
    public void clear(String serviceId) {

	try (Jedis jedis = pool.getResource()) {

	    jedis.del(key(serviceId));
	    jedis.srem(memberKey(), serviceId);
	}
    }

    /**
     * @param serviceId
     * @return
     */
    @Override
    public int size(String serviceId) {

	try (Jedis jedis = pool.getResource()) {

	    return (int) jedis.hlen(key(serviceId));
	}
    }

    /**
     * @param serviceId
     * @return
     */
    private String key(String serviceId) {

	return "service:" + serviceId + ":kvstore";
    }

    /**
     * @return
     */
    private String memberKey() {

	return "services:kvstore:index";
    }

    /**
     * @param redisMessage
     * @return
     */
    private Map.Entry<String, String> of(String redisMessage) {

	String[] parts = redisMessage.split("\\|", 2);
	String key = parts[0];
	String value = parts[1];

	return Map.entry(key, value);
    }
}
