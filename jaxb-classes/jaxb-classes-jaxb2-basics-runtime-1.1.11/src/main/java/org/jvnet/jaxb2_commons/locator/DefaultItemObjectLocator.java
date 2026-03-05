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

import java.text.MessageFormat;

/**
 * Locator for the collection item.
 */
public final class DefaultItemObjectLocator extends AbstractObjectLocator
		implements ItemObjectLocator {
	/**
	 * Item index.
	 */
	protected final int index;

	/**
	 * Constructs a new item locator.
	 * 
	 * @param parentLocator
	 *            parent locator.
	 * @param itemIndex
	 *            item index.
	 * @param itemValue
	 *            item value.
	 */
	protected DefaultItemObjectLocator(final ObjectLocator parentLocator,
			final int itemIndex, Object itemValue) {
		super(parentLocator, itemValue);
		this.index = itemIndex;
	}

	/**
	 * Returns item index.
	 * 
	 * @return Index of the item.
	 */
	public int getIndex() {
		return index;
	}

	public Object[] getMessageParameters() {
		return new Object[] { getObject(), Integer.valueOf(getIndex()) };
	}

	@Override
	protected String getDefaultMessage() {
		return MessageFormat.format("Item index: {1}\nItem value: {0}.",
				getMessageParameters());
	}

	@Override
	protected String getStepAsString() {
		return "[" + getIndex() + "]";
	}
}
