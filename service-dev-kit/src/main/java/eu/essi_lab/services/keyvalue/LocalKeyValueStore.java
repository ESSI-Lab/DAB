package eu.essi_lab.services.keyvalue;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
public class LocalKeyValueStore implements KeyValueStore {

    /**
     *
     */
    private static final Map<String, List<Map.Entry<String, String>>> KEY_VALUE_MAP = new ConcurrentHashMap<>();

    /**
     * @param serviceId
     * @param key
     * @param value
     */
    @Override
    public void upsert(String serviceId, String key, String value) {

	synchronized (KEY_VALUE_MAP) {

	    List<Map.Entry<String, String>> entries = KEY_VALUE_MAP.computeIfAbsent(serviceId, k -> new ArrayList<>());

	    entries.add(Map.entry(key, value));

	    KEY_VALUE_MAP.put(serviceId, entries);
	}
    }

    /**
     * @param serviceId
     * @param key
     */
    @Override
    public void remove(String serviceId, String key) {

	synchronized (KEY_VALUE_MAP) {

	    List<Map.Entry<String, String>> entries = KEY_VALUE_MAP.get(serviceId);

	    if(entries != null) {

		 entries.stream(). //
			 filter(entry -> entry.getKey().equals(key)).//
			 findFirst().//
			 ifPresent(entries::remove);//
 	    }
	}
    }

    /**
     * @param serviceId
     * @return
     */
    @Override
    public List<Map.Entry<String, String>> get(String serviceId) {

	synchronized (KEY_VALUE_MAP) {

	    List<Map.Entry<String, String>> entries = KEY_VALUE_MAP.get(serviceId);

	    if (entries == null) {

		return Collections.emptyList();
	    }

	    return entries;
	}
    }

    /**
     * @param serviceId
     */
    @Override
    public void clear(String serviceId) {

	KEY_VALUE_MAP.remove(serviceId);
    }

    /**
     * @param serviceId
     * @return
     */
    @Override
    public int size(String serviceId) {

	List<Map.Entry<String, String>> store = KEY_VALUE_MAP.get(serviceId);

	return store == null ? 0 : store.size();
    }

}
