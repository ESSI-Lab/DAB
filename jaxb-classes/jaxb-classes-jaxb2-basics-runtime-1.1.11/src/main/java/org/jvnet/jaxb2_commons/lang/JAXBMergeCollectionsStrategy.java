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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jvnet.jaxb2_commons.locator.ObjectLocator;

public class JAXBMergeCollectionsStrategy extends JAXBMergeStrategy {

	@Override
	protected Object mergeInternal(ObjectLocator leftLocator,
			ObjectLocator rightLocator, Collection leftCollection,
			Collection rightCollection) {

		if (leftCollection instanceof List && rightCollection instanceof List) {
			final List<Object> list = new ArrayList<Object>(leftCollection
					.size()
					+ rightCollection.size());
			list.addAll(leftCollection);
			list.addAll(rightCollection);
			return list;
		} else if (leftCollection instanceof Set
				&& rightCollection instanceof Set) {
			final Set<Object> set = new HashSet<Object>(leftCollection.size()
					+ rightCollection.size());
			set.addAll(leftCollection);
			set.addAll(rightCollection);
			return set;
		} else {
			return super.mergeInternal(leftLocator, rightLocator,
					leftCollection, rightCollection);
		}
	}

	public static final MergeStrategy2 INSTANCE = new JAXBMergeCollectionsStrategy();
}
