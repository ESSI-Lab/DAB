package org.jvnet.jaxb2_commons.locator;

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

import jakarta.xml.bind.ValidationEventLocator;

import org.jvnet.jaxb2_commons.i18n.Reportable;

/**
 * Object locator denotes a location in an object structure.
 * 
 * @author Aleksei Valikov
 * 
 */
public interface ObjectLocator extends ValidationEventLocator, Reportable {

	/**
	 * @return Parent locator, may be <code>null</code>.
	 */
	public ObjectLocator getParentLocator();

	/**
	 * @return Path to this locator from the root.
	 */
	public ObjectLocator[] getPath();

	/**
	 * @return Path to this locator in string form;
	 */
	public String getPathAsString();

	/**
	 * Creates a locator for the property, relative to this locator.
	 * 
	 * @param propertyName
	 *            name of the property, must not be <code>null</code>.
	 * @param propertyValue
	 *            value of the property, may be <code>null</code>.
	 * @return Child property locator.
	 */
	public PropertyObjectLocator property(String propertyName,
			Object propertyValue);

	/**
	 * Creates a locator for the item (like list or array item) relative to this
	 * locator.
	 * 
	 * @param itemIndex
	 *            index of the item.
	 * @param itemValue
	 *            value of the item.
	 * @return Child item locator.
	 */
	public ItemObjectLocator item(int itemIndex, Object itemValue);

}
