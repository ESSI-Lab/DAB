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

import java.util.HashMap;
import java.util.Optional;

/**
 * @author Fabrizio
 */
public class GridFilter {

    /**
     * 
     */
    private static final HashMap<String, String> SELECTION_MAP = new HashMap<>();

    /**
     * @param items
     */
    public GridFilter() {
    }

    /**
     * @param item
     * @return
     */
    public boolean test(HashMap<String, String> item) {

	boolean match = true;

	for(String colum: SELECTION_MAP.keySet()){

	    String itemValue = item.get(colum);

	    match &= itemValue == null || itemValue.toLowerCase().contains(SELECTION_MAP.get(colum).toLowerCase());
	}

	return match;
    }

    /**
     * @param columnName
     * @param value
     */
    public void filter(String columnName, String value) {

	SELECTION_MAP.put(columnName, value);
    }

    /**
     * @param columnName
     * @return
     */
    public static Optional<String> getValue(String columnName) {

	return Optional.ofNullable(SELECTION_MAP.get(columnName));
    }

    /**
     * 
     */
    public static void clearSelection() {

	SELECTION_MAP.clear();
    }
}
