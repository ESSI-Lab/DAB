package eu.essi_lab.services.keyvalue;

import java.util.*;

/**
 * @author Fabrizio
 */
public interface ReadableKeyValueStore {

    /**
     * @param serviceId
     * @return
     */
    List<Map.Entry<String, String>> get(String serviceId);

    /**
     * @param serviceId
     * @param key
     * @return
     */
    default Optional<Map.Entry<String, String>> get(String serviceId, String key){

        return get(serviceId).stream().filter(e -> e.getKey().equals(key)).findFirst();
    }
}
