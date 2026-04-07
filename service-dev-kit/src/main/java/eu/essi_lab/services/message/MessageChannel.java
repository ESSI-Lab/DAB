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

import java.io.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public interface MessageChannel extends ReadableMessageChannel {

    /**
     * @author Fabrizio
     */
    class Message {

	private String timestamp;
	private MessageLevel level;
	private String message;
	private String serviceId;

	/**
	 * @param serviceId
	 * @param level
	 * @param message
	 * @return
	 */
	static Message of(String serviceId, MessageLevel level, String message) {

	    return of(serviceId, level, ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(), message);
	}

	/**
	 * @param serviceId
	 * @param level
	 * @param timestamp
	 * @param message
	 * @return
	 */
	static Message of(String serviceId, MessageLevel level, String timestamp, String message) {

	    Message msg = new Message();
	    msg.timestamp = timestamp;
	    msg.level = level;
	    msg.serviceId = serviceId;
	    msg.message = message;

	    return msg;
	}

	/**
	 * @param redisMessage
	 * @return
	 */
	static Message of(String redisMessage) {

	    String[] parts = redisMessage.split("\\|", 4);
	    String serviceId = parts[0];
	    String timestamp = parts[1];
	    String level = parts[2];
	    String msg = parts[3];

	    return Message.of(serviceId, MessageLevel.valueOf(level), timestamp, msg);
	}

	/**
	 * @return
	 */
	static Comparator<Message> getComparator() {

	    return (Comparator<Message> & Serializable) (m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp());
	}

	/**
	 * @return
	 */
	public String getTimestamp() {

	    return timestamp;
	}

	/**
	 * @return
	 */
	public MessageLevel getLevel() {

	    return level;
	}

	/**
	 * @return
	 */
	public String getMessage() {

	    return message;
	}
    }

    /**
     * @author Fabrizio
     */
    enum MessageLevel {
	/**
	 *
	 */
	DEBUG,
	/**
	 *
	 */
	INFO,
	/**
	 *
	 */
	WARN,
	/**
	 *
	 */
	ERROR
    }

    /**
     * @param serviceId
     * @param level
     * @param message
     */
    void publish(String serviceId, MessageLevel level, String message);

    /**
     * @param serviceId
     */
    void clear(String serviceId);

    /**
     * @param serviceId
     * @return
     */
    int size(String serviceId);
}
