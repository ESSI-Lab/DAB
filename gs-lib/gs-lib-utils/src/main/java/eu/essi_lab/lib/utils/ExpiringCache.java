package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A generic optionally limited cache whose values will expire after the given optional duration
 * 
 * @author boldrini
 * @param <T>
 */
public class ExpiringCache<T> {

    private Long duration = null;

    private Integer maxSize = null;

    public Integer getMaxSize() {
	return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
	this.maxSize = maxSize;
    }

    public long getDuration() {
	return duration;
    }

    /**
     * Milliseconds duration
     * 
     * @param duration
     */
    public void setDuration(long duration) {
	this.duration = duration;
    }

    private HashMap<String, SimpleEntry<Long, T>> cache = new HashMap<>();

    public void clear() {
	synchronized (cache) {
	    cache.clear();
	}
    }

    public synchronized Set<String> keySet() {
	Set<String> ret = new HashSet<>();
	Set<Entry<String, SimpleEntry<Long, T>>> entries = cache.entrySet();
	for (Entry<String, SimpleEntry<Long, T>> entry : entries) {
	    String key = entry.getKey();
	    if (!isExpired(entry.getValue().getKey())) {
		ret.add(key);
	    }
	}
	return ret;
    }

    public synchronized Set<Entry<String, T>> entrySet() {
	Set<Entry<String, T>> ret = new HashSet<>();
	Set<Entry<String, SimpleEntry<Long, T>>> entries = cache.entrySet();
	for (Entry<String, SimpleEntry<Long, T>> entry : entries) {
	    String key = entry.getKey();
	    if (!isExpired(entry.getValue().getKey())) {
		ret.add(new SimpleEntry(key, entry.getValue().getValue()));
	    }
	}
	return ret;
    }
    

    public synchronized void remove(String key) {
	cache.remove(key);
	
    }

    public void put(String key, T value) {
	SimpleEntry<Long, T> entry = new SimpleEntry<>(System.currentTimeMillis(), value);
	synchronized (cache) {
	    if (maxSize == null) {
		cache.put(key, entry);
	    } else {
		if (cache.size() == maxSize) {
		    // removing the oldest entry
		    Set<Entry<String, SimpleEntry<Long, T>>> entrySet = cache.entrySet();
		    Entry<String, SimpleEntry<Long, T>> oldestEntry = null;
		    for (Entry<String, SimpleEntry<Long, T>> existingEntry : entrySet) {

			if (existingEntry != null
				&& (oldestEntry == null || existingEntry.getValue().getKey() < oldestEntry.getValue().getKey())) {
			    oldestEntry = existingEntry;
			}
		    }
		    if (oldestEntry != null) {
			cache.remove(oldestEntry.getKey());
		    }
		}
		cache.put(key, entry);
	    }
	}

    }

    public int size() {
	int i = 0;
	synchronized (cache) {
	    Set<Entry<String, SimpleEntry<Long, T>>> entrySet = cache.entrySet();
	    for (Entry<String, SimpleEntry<Long, T>> entry : entrySet) {
		Long time = entry.getValue().getKey();
		if (!isExpired(time)) {
		    i++;
		}
	    }
	}
	return i;
    }

    private boolean isExpired(Long time) {
	long gap = System.currentTimeMillis() - time;
	return gap > duration;
    }

    public T get(String key) {
	SimpleEntry<Long, T> entry = cache.get(key);
	if (entry == null) {
	    return null;
	}
	if (isExpired(entry.getKey())) {
	    return null;
	}

	return entry.getValue();
    }


}
