package eu.essi_lab.model.index;

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

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.ResourcePropertyHandler;

/**
 * An {@link IndexedElement} related to <i>non-harmonized</i> {@link Queryable} properties.
 * The {@link #getElementName()} is set according to {@link getResourceProperty#getName()}, and the
 * value/s is/are set directly with {@link GSResource} methods in case of <i>mandatory</i> properties,
 * or by means of the {@link ResourcePropertyHandler}
 * 
 * @author Fabrizio
 */
public class IndexedResourceProperty extends IndexedElement {

    private ResourceProperty property;

    /**
     * Creates a new indexed element related to the supplied <code>property</code>
     * 
     * @param element
     */
    public IndexedResourceProperty(ResourceProperty property) {
	this(property, null);
    }

    /**
     * Creates a new indexed element related to the supplied <code>property</code> and with the given
     * <code>value</code>
     * 
     * @param property
     * @param value a non <code>null</code> string, empty string is admitted
     */
    public IndexedResourceProperty(ResourceProperty property, String value) {
	super(property.getName(), value);
	this.property = property;
    }

    /**
     * Returns this {@link ResourceProperty}
     * 
     * @return
     */
    public ResourceProperty getResourceProperty() {
	return property;
    }

}
