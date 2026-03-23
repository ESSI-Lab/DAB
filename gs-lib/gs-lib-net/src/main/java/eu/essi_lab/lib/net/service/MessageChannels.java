package eu.essi_lab.lib.net.service;

import eu.essi_lab.lib.net.service.message.*;
import redis.clients.jedis.*;

import java.util.*;

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
