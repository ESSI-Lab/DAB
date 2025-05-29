package eu.essi_lab.accessor.arcgis.handler;

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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author ilsanto
 */
public class LogicalOperatorsTree {

    private List<String> list = new ArrayList<>();
    private Logger logger = GSLoggerFactory.getLogger(getClass());

    public void add(String operator) {
	logger.trace("Adding operator {}", operator);

	list.add(operator);
    }

    public String removeLast() {
	logger.trace("Removing last");

	if (!isEmpty())
	    return list.remove(list.size() - 1);

	return null;
    }

    public boolean isEmpty() {
	return list.isEmpty();
    }

    public String getLast() {
	return list.get(list.size() - 1);
    }
}
