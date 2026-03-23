package eu.essi_lab.lib.net.service;

import eu.essi_lab.lib.net.service.message.*;

/**
 * @author Fabrizio
 */
public interface ManagedService {

    /**
     * @param id
     */
    void setId(String id);

    /**
     * @return
     */
    String getId();

    /**
     *
     */
    void start();

    /**
     *
     */
    void stop();

    /**
     *
     */
    default String getName() {

	return getClass().getSimpleName();
    }

    /**
     * @param serviceId
     * @param level
     * @param message
     */
    default void publish(MessageChannel.MessageLevel level, String message) {

        MessageChannels.getWritable().publish(getId(), level, message);
    }

    /**
     * @param serviceId
     */
    default void removeAll() {

        MessageChannels.getWritable().removeAll(getId());
    }
}
