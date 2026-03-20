package eu.essi_lab.lib.net.services;

import redis.clients.jedis.*;
import redis.clients.jedis.params.*;

/**
 * @author Fabrizio
 */
public class RedisDistributedLock {

    private final JedisPool jedisPool;
    private final String key;
    private final String value;
    private final int ttlSeconds;

    private static final String RENEW_SCRIPT = //
	    "if redis.call('GET', KEYS[1]) == ARGV[1] then " + //
		    "  return redis.call('EXPIRE', KEYS[1], ARGV[2]) " + //
		    "else return 0 end";

    private static final String RELEASE_SCRIPT = //
	    "if redis.call('GET', KEYS[1]) == ARGV[1] then " + //
		    "  return redis.call('DEL', KEYS[1]) " + //
		    "else return 0 end";

    /**
     * @param jedisPool
     * @param serviceId
     * @param ttlSeconds
     * @param nodeId
     */
    public RedisDistributedLock(JedisPool jedisPool, String serviceId, int ttlSeconds, String nodeId) {
	this.jedisPool = jedisPool;
	this.key = getKey(serviceId);
	this.value = nodeId + ":" + serviceId;
	this.ttlSeconds = ttlSeconds;
    }

    /**
     * @param serviceId
     * @return
     */
    static String getKey(String serviceId) {

	return "service:" + serviceId + ":lock";
    }

    /**
     * @param key
     * @return
     */
    static String getServiceId(String key) {

	return key.replace("service:", "").replace(":lock", "");
    }

    /**
     * @return
     */
    public boolean tryAcquire() {

	try (Jedis jedis = jedisPool.getResource()) {

	    String result = jedis.set(key, value, SetParams.setParams().nx().ex(ttlSeconds));
	    return "OK".equals(result);
	}
    }

    /**
     * @return
     */
    public boolean renew() {

	try (Jedis jedis = jedisPool.getResource()) {

	    Object result = jedis.eval(RENEW_SCRIPT, 1, key, value, String.valueOf(ttlSeconds));
	    return Long.valueOf(1).equals(result);
	}
    }

    /**
     *
     */
    public void release() {

	try (Jedis jedis = jedisPool.getResource()) {

	    jedis.eval(RELEASE_SCRIPT, 1, key, value);
	}
    }

    /**
     * @return
     */
    public String getValue() {

	return value;
    }
}
