package eu.essi_lab.services.message;

import java.util.*;

/**
 * @author Fabrizio
 */
public interface ReadableMessageChannel {

    /**
     * @param serviceId
     * @return
     */
    List<MessageChannel.Message> read(String serviceId);

    /**
     * @param serviceId
     * @param minLevel
     * @return
     */
    List<MessageChannel.Message> read(String serviceId, MessageChannel.MessageLevel minLevel);

    /**
     * @param serviceId
     * @param max
     * @return
     */
    List<MessageChannel.Message> read(String serviceId, int max);

    /**
     * @param serviceId
     * @param max
     * @param minLevel
     * @return
     */
    List<MessageChannel.Message> read(String serviceId, int max, MessageChannel.MessageLevel minLevel);
}
