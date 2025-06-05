/**
 * 
 */
package eu.essi_lab.model.index;

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

import eu.essi_lab.model.RuntimeInfoElement;

/**
 * @author Fabrizio
 */
public class IndexedRuntimeInfoElement extends IndexedElement {

    private RuntimeInfoElement element;

    /**
     * Creates a new indexed element related to the supplied <code>element</code>
     * 
     * @param element
     */
    public IndexedRuntimeInfoElement(RuntimeInfoElement element) {
	this(element, null);
    }

    /**
     * Creates a new indexed element related to the supplied <code>element</code> and with the given
     * <code>value</code>
     * 
     * @param element
     * @param value a non <code>null</code> string, empty string is admitted
     */
    public IndexedRuntimeInfoElement(RuntimeInfoElement element, String value) {
	super(element.getName(), value);
	this.element = element;
    }

    /**
     * Returns this {@link RuntimeInfoElement}
     * 
     * @return
     */
    public RuntimeInfoElement getStatisticalElement() {
	return element;
    }

}
