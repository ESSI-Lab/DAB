package eu.essi_lab.services.message;

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
