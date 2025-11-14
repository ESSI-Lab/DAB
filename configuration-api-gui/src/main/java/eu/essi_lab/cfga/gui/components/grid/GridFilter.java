package eu.essi_lab.cfga.gui.components.grid;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
    private String columnName;
    /**
     * 
     */
    private String value;
    /**
     * 
     */
    private static final HashMap<String, String> VALUES_MAP = new HashMap<>();

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

	String itemValue = item.get(this.columnName);

	return itemValue == null || itemValue.toLowerCase().contains(value.toLowerCase());
    }

    /**
     * @param columnName
     * @param value
     */
    public void filter(String columnName, String value) {

	VALUES_MAP.put(columnName, value);

	this.columnName = columnName;
	this.value = value;
    }

    /**
     * @param columnName
     * @return
     */
    public static Optional<String> getValue(String columnName) {

	return Optional.ofNullable(VALUES_MAP.get(columnName));
    }

    /**
     * 
     */
    public static void clearValuesCache() {

	VALUES_MAP.clear();
    }
}
