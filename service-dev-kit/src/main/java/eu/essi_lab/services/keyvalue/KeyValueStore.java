package eu.essi_lab.services.keyvalue;

/**
 * @author Fabrizio
 */
public interface KeyValueStore extends ReadableKeyValueStore {

    /**
     * @param serviceId
     * @param key
     * @param value
     */
    void upsert(String serviceId, String key, String value);

    /**
     * @param serviceId
     * @param key
     */
    void remove(String serviceId, String key);

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
