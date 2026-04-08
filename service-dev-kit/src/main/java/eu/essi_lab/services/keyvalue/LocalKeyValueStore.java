package eu.essi_lab.services.keyvalue;

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
