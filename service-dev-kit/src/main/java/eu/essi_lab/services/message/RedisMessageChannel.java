package eu.essi_lab.services.message;

import eu.essi_lab.lib.utils.*;
import redis.clients.jedis.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class RedisMessageChannel implements MessageChannel {

    private final JedisPool pool;
    private final int channelSize;

    /**
     * @param pool
     */
    public RedisMessageChannel(JedisPool pool) {

	this.pool = pool;
	this.channelSize = 100;
    }

    /**
     * @param pool
     * @param channelSize
     */
    public RedisMessageChannel(JedisPool pool, int channelSize) {

	this.pool = pool;
	this.channelSize = channelSize;
    }

    @Override
    public void publish(String serviceId, MessageLevel level, String message) {

	try (Jedis jedis = pool.getResource()) {

	    String msg = serviceId + "|" + //
		    ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds() + "|" + //
		    level.name() + "|" + //
		    message;//

	    jedis.rpush(key(serviceId), msg);

	    jedis.sadd("services:messages:index", serviceId);

	    jedis.ltrim(key(serviceId), -channelSize, -1);
	}
    }

    @Override
    public List<Message> read(String serviceId) {

	return read(serviceId, 0);
    }

    @Override
    public List<Message> read(String serviceId, MessageLevel minLevel) {

	return read(serviceId, 0, minLevel);
    }

    @Override
    public List<Message> read(String serviceId, int max) {

	try (Jedis jedis = pool.getResource()) {

	    return jedis.lrange(key(serviceId), -max, -1). //
		    stream().//
		    map(Message::of).//
		    sorted(Message.getComparator()).//
		    toList();//
	}
    }

    @Override
    public List<Message> read(String serviceId, int max, MessageLevel minLevel) {

	return read(serviceId, max).//
		stream().//
		filter(m -> m.getLevel().ordinal() >= minLevel.ordinal()).//
		toList();//
    }

    @Override
    public void removeAll(String serviceId) {

	try (Jedis jedis = pool.getResource()) {

	    jedis.del(key(serviceId));
	    jedis.srem("services:messages:index", serviceId);
	}
    }

    @Override
    public int size(String serviceId) {

	try (Jedis jedis = pool.getResource()) {

	    return (int) jedis.llen(key(serviceId));
	}
    }

    /**
     * @param serviceId
     * @return
     */
    private String key(String serviceId) {

	return "service:" + serviceId + ":messages";
    }
}