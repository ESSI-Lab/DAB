package eu.essi_lab.shared.driver;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.shared.model.SharedContent;
public class SharedTable {

    private static SharedTable instance;
    private Map<String, SharedContent> map = new HashMap<>();

    private final Object lock = new Object();

    private SharedTable() {

	Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

	    synchronized (lock) {
		map = new HashMap<>();
	    }

	}, 30L, 30L, TimeUnit.MINUTES);

    }

    public static SharedTable getInstance() {
	if (instance == null)
	    instance = new SharedTable();

	return instance;
    }

    public SharedContent getContent(String identifier) {

	return map.get(identifier);

    }

    public void storeContent(SharedContent c) {

	map.put(c.getIdentifier(), c);

    }

    public Long size() {
	return Long.valueOf(map.size());
    }
}
