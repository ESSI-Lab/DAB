package org.jvnet.jaxb2_commons.lang;

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

import static org.jvnet.jaxb2_commons.locator.util.LocatorUtils.item;
import static org.jvnet.jaxb2_commons.locator.util.LocatorUtils.property;

import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.jvnet.jaxb2_commons.locator.ObjectLocator;

public class JAXBHashCodeStrategy extends DefaultHashCodeStrategy {

	public JAXBHashCodeStrategy() {
		super();
	}

	public JAXBHashCodeStrategy(int multiplierNonZeroOddNumber) {
		super(multiplierNonZeroOddNumber);
	}

	protected int hashCodeInternal(ObjectLocator locator, int hashCode,
			Object value) {
		if (value instanceof JAXBElement<?>) {
			final JAXBElement<?> element = (JAXBElement<?>) value;
			return hashCodeInternal(locator, hashCode, element);
		} else if (value instanceof List<?>) {
			final List<?> list = (List<?>) value;
			return hashCodeInternal(locator, hashCode, list);
		} else {
			return super.hashCodeInternal(locator, hashCode, value);
		}
	}

	protected int hashCodeInternal(ObjectLocator locator, int hashCode,
			final JAXBElement<?> element) {
		int currentHashCode = hashCode;
		currentHashCode = hashCode(
				property(locator, "name", element.getName()), currentHashCode,
				element.getName());
		currentHashCode = hashCode(
				property(locator, "declaredType", element.getDeclaredType()),
				currentHashCode, element.getDeclaredType());
		currentHashCode = hashCode(
				property(locator, "scope", element.getScope()),
				currentHashCode, element.getScope());
		currentHashCode = hashCode(
				property(locator, "value", element.getValue()),
				currentHashCode, element.getValue());
		return currentHashCode;
	}

	protected int hashCodeInternal(ObjectLocator locator, int hashCode,
			final List<?> list) {
		// Treat empty lists as nulls
		if (list.isEmpty()) {
			return super.hashCode(locator, hashCode, (Object) null);
		} else {
			int currentHashCode = hashCode;
			for (int index = 0; index < list.size(); index++) {
				final Object item = list.get(index);
				currentHashCode = hashCode(item(locator, index, item),
						currentHashCode, item);
			}
			return currentHashCode;
		}
	}

	public static JAXBHashCodeStrategy INSTANCE = new JAXBHashCodeStrategy();

}
