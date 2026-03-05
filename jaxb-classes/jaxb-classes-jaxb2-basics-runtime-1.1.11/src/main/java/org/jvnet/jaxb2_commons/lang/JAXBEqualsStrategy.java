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

import java.util.Iterator;
import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.jvnet.jaxb2_commons.locator.ObjectLocator;

public class JAXBEqualsStrategy extends DefaultEqualsStrategy {

	@Override
	protected boolean equalsInternal(ObjectLocator leftLocator,
			ObjectLocator rightLocator, Object left, Object right) {
		if (left instanceof JAXBElement<?> && right instanceof JAXBElement<?>) {
			return equalsInternal(leftLocator, rightLocator,
					(JAXBElement<?>) left, (JAXBElement<?>) right);
		} else if (left instanceof List<?> && right instanceof List<?>) {
			return equalsInternal(leftLocator, rightLocator, (List<?>) left,
					(List<?>) right);
		} else {
			return super.equalsInternal(leftLocator, rightLocator, left, right);

		}
	}

	protected boolean equalsInternal(ObjectLocator leftLocator,
			ObjectLocator rightLocator, final List<?> left, final List<?> right) {
		final Iterator<?> e1 = left.iterator();
		final Iterator<?> e2 = right.iterator();
		int index = 0;
		while (e1.hasNext() && e2.hasNext()) {
			Object o1 = e1.next();
			Object o2 = e2.next();
			if (!(o1 == null ? o2 == null : equals(
					item(leftLocator, index, o1),
					item(rightLocator, index, o2), o1, o2))) {
				return false;
			}
			index = index + 1;
		}
		return !(e1.hasNext() || e2.hasNext());
	}

	protected boolean equalsInternal(ObjectLocator leftLocator,
			ObjectLocator rightLocator, final JAXBElement<?> left,
			final JAXBElement<?> right) {
		return
		//
		equals(property(leftLocator, "name", left.getName()),
				property(rightLocator, "name", right.getName()),
				left.getName(), right.getName()) &&
		//
				equals(property(leftLocator, "value", left.getValue()),
						property(rightLocator, "name", right.getValue()),
						left.getValue(), right.getValue());
	}

	public static JAXBEqualsStrategy INSTANCE = new JAXBEqualsStrategy();
}
