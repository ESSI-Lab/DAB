package eu.essi_lab.shared.driver;

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;

/**
 * @author ilsanto
 */
public class ContentTable {

    @SuppressWarnings("rawtypes")
    private Map<String, SharedContent> map;

    public ContentTable() {

	map = new HashMap<>();

	Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

	    synchronized (ContentTable.this) {
		map = new HashMap<>();
	    }

	}, 30L, 30L, TimeUnit.MINUTES);
    }

    public synchronized SharedContent<?> getContent(String identifier) {

	return map.get(identifier);
    }

    public synchronized void storeContent(SharedContent<?> c) {

	map.put(c.getIdentifier(), c);
    }

    public synchronized Long size() {

	return Long.valueOf(map.size());
    }

    public synchronized Long size(SharedContentType type) {

	return map.values().stream().filter(o -> o.getType() == type).count();
    }
}
