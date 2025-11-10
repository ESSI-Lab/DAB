package eu.essi_lab.lib.net.publisher;

/**
 *
 */
@FunctionalInterface
public interface MessagePublisher {

    /**
     * @param topic
     * @param message
     */
    void publish(String topic, String message) throws Exception;
}
