package eu.essi_lab.services;

import eu.essi_lab.services.keyvalue.*;
import redis.clients.jedis.*;

/**
 * @author Fabrizio
 */
public class KeyValueStoreProvider {

    private static KeyValueStore INSTANCE;

    /**
     * @return
     */
    public static ReadableKeyValueStore get() {

	return INSTANCE;
    }

    /**
     *
     */
    static void init() {

	INSTANCE = new LocalKeyValueStore();
    }

    /**
     * @param pool
     * @param channelSize
     */
    static void init(JedisPool pool) {

	INSTANCE = new RedisKeyValueStore(pool);
    }

    static KeyValueStore getWritable() {

	return INSTANCE;
    }
}
