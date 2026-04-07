package eu.essi_lab.services.keyvalue;

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
