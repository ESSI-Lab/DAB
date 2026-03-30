package eu.essi_lab.services;

import eu.essi_lab.cfga.*;
import eu.essi_lab.services.message.*;

/**
 * @author Fabrizio
 */
public interface ManagedService extends Configurable<ManagedServiceSetting> {

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

    @Override
    default String getType() {

	return getClass().getName();
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
