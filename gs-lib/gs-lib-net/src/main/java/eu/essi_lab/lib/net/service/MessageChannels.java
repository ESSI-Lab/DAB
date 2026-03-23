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

    /**
     * @return
     */
    public static MessageChannel get() {

	return INSTANCE;
    }

    /**
     * @param serviceId
     * @return
     */
    public static List<MessageChannel.Message> readMessages(String serviceId) {

	return readMessages(serviceId, Integer.MAX_VALUE);
    }

    /**
     * @param serviceId
     * @param maxMessages
     * @return
     */
    public static List<MessageChannel.Message> readMessages(String serviceId, int maxMessages) {

	return INSTANCE.read(serviceId, maxMessages).//
		stream(). //
		sorted(Comparator.comparing(MessageChannel.Message::getTimestamp)).//
		toList();
    }
}
