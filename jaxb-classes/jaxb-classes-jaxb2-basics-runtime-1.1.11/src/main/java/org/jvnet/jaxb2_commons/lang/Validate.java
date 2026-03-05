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

import java.util.Collection;
import java.util.Iterator;

public class Validate {

	public static void notNull(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("The validated object is null");
		}
	}

	public static void isTrue(boolean expression) {
		if (expression == false) {
			throw new IllegalArgumentException(
					"The validated expression is false");
		}
	}

	public static void notEmpty(Collection collection) {
		if (collection == null || collection.size() == 0) {
			throw new IllegalArgumentException(
					"The validated collection is empty");
		}
	}

	public static void noNullElements(Collection<?> collection) {
		Validate.notNull(collection);
		int i = 0;
		for (Iterator<?> it = collection.iterator(); it.hasNext(); i++) {
			if (it.next() == null) {
				throw new IllegalArgumentException(
						"The validated collection contains null element at index: "
								+ i);
			}
		}
	}

}
