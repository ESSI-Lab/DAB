package eu.essi_lab.services;

import eu.essi_lab.services.message.*;
import redis.clients.jedis.*;

/**
 * @author Fabrizio
 */
public class MessageChannels {

    private static MessageChannel INSTANCE;

    /**
     * @return
     */
    public static ReadableMessageChannel get() {

	return INSTANCE;
    }

    /**
     *
     */
    static void init(int channelSize) {

	INSTANCE = new LocalMessageChannel(channelSize);
    }

    /**
     * @param pool
     * @param channelSize
     */
    static void init(JedisPool pool, int channelSize) {

	INSTANCE = new RedisMessageChannel(pool, channelSize);
    }

    static MessageChannel getWritable() {

	return INSTANCE;
    }
}
