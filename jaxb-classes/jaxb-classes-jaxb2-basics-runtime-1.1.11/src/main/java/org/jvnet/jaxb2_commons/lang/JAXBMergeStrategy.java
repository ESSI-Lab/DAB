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

import org.jvnet.jaxb2_commons.locator.ObjectLocator;

public class JAXBMergeStrategy extends DefaultMergeStrategy {

	@SuppressWarnings("unchecked")
	@Override
	protected Object mergeInternal(ObjectLocator leftLocator,
			ObjectLocator rightLocator, Object left, Object right) {
		if (left instanceof Collection && right instanceof Collection) {
			Collection leftCollection = (Collection) left;
			Collection rightCollection = (Collection) right;
			return mergeInternal(leftLocator, rightLocator, leftCollection,
					rightCollection);
		} else {
			return super.mergeInternal(leftLocator, rightLocator, left, right);
		}
	}

	@SuppressWarnings("unchecked")
	protected Object mergeInternal(ObjectLocator leftLocator,
			ObjectLocator rightLocator, Collection leftCollection,
			Collection rightCollection) {
		return !leftCollection.isEmpty() ? leftCollection : rightCollection;
	}

	public static final JAXBMergeStrategy INSTANCE = new JAXBMergeStrategy();

}
