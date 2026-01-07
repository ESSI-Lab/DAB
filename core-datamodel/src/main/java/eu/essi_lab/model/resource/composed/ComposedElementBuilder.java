/**
 * 
 */
package eu.essi_lab.model.resource.composed;

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

import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class ComposedElementBuilder {

    private ComposedElement composedElement;

    /**
     * @param elementName
     */
    private ComposedElementBuilder(String elementName) {

	this.composedElement = new ComposedElement(elementName);
    }

    /**
     * @return
     */
    public static ComposedElementBuilder get(String elementName) {

	return new ComposedElementBuilder(elementName);
    }

    /**
     * @param item
     * @return
     */
    public ComposedElementBuilder addItem(ComposedElementItem item) {

	composedElement.addItem(item);

	return this;
    }

    /**
     * @param name
     * @param type
     * @param value
     * @return
     */
    public ComposedElementBuilder addItem(String name, ContentType type) {

	composedElement.addItem(new ComposedElementItem(name, type));

	return this;
    }

    /**
     * @return
     */
    public ComposedElement build() {

	return composedElement;
    }

}
