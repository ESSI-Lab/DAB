package eu.essi_lab.gssrv.conf;

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

import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.value.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public abstract class AbstractGridFilter<G extends GridDataModel> {

    /**
     *
     */
    private final HashMap<String, String> selectionMap;

    /**
     *
     */
    public AbstractGridFilter() {

	selectionMap = new HashMap<>();
    }

    /**
     * @param gridData
     * @return
     */
    public boolean test(G gridData) {

	boolean match = true;

	for (String colum : selectionMap.keySet()) {

	    String itemValue = getItemValue(colum, gridData);

	    match &= itemValue == null || itemValue.toLowerCase().contains(selectionMap.get(colum).toLowerCase());
	}

	return match;
    }

    /**
     * @param colum
     * @param gridData
     * @return
     */
    protected abstract String getItemValue(String colum, G gridData);

    /**
     * @param columnKey
     * @param value
     */
    public void filter(String columnKey, String value) {

	selectionMap.put(columnKey, value);
    }
}
