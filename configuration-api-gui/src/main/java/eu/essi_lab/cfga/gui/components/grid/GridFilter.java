package eu.essi_lab.cfga.gui.components.grid;

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

/**
 * @author Fabrizio
 */
class GridFilter {

    /**
     *
     */
    private static final HashMap<String, List<Map.Entry<String, String>>> SHARED_MAP = new HashMap<>();

    /**
     *
     */
    private final HashMap<String, String> selectionMap;

    /**
     * @param items
     */
    GridFilter() {

	selectionMap = new HashMap<>();
    }

    /**
     * @param item
     * @return
     */
    boolean test(HashMap<String, String> item) {

	boolean match = true;

	for (String colum : selectionMap.keySet()) {

	    String itemValue = item.get(colum);

	    match &= itemValue == null || itemValue.toLowerCase().contains(selectionMap.get(colum).toLowerCase());
	}

	return match;
    }

    /**
     * @param tabLabel
     * @param columnName
     * @param value
     */
    void filter(String tabLabel, String columnName, String value) {

	List<Map.Entry<String, String>> entries = SHARED_MAP.computeIfAbsent(tabLabel, k -> new ArrayList<>());

	entries.stream().//
		filter(e -> e.getKey().equals(columnName)).//
		findFirst(). //
		ifPresent(entries::remove);

	entries.add(Map.entry(columnName, value));

	selectionMap.put(columnName, value);
    }

    /**
     * @param tabLabel
     * @param columnName
     * @return
     */
    static Optional<String> get(String tabLabel, String columnName) {

	List<Map.Entry<String, String>> entries = Optional.ofNullable(SHARED_MAP.get(tabLabel)).orElse(new ArrayList<>());

	return entries.stream().//
		filter(entry -> entry.getKey().equals(columnName)).//
		map(Map.Entry::getValue).//
		findFirst();//
    }

    /**
     *
     */
    static void clear() {

	SHARED_MAP.clear();
    }
}
