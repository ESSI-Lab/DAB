package org.jvnet.jaxb2_commons.locator.util;

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

import org.jvnet.jaxb2_commons.locator.PropertyObjectLocator;
import org.jvnet.jaxb2_commons.locator.ItemObjectLocator;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.xml.sax.Locator;

public class LocatorUtils {

	private LocatorUtils() {

	}

	public static String getLocation(Locator locator) {
		if (locator == null) {
			return "<unknown>";
		} else {
			return locator.getPublicId() + ":" + locator.getSystemId() + ":"
					+ locator.getLineNumber() + ":" + locator.getColumnNumber();
		}
	}

	public static PropertyObjectLocator property(ObjectLocator locator, String name,
			Object value) {
		return locator == null ? null : locator.property(name, value);
	}

	public static PropertyObjectLocator property(ObjectLocator locator, String name,
			boolean value) {
		return locator == null ? null : locator.property(name, Boolean
				.valueOf(value));
	}

	public static PropertyObjectLocator property(ObjectLocator locator, String name,
			byte value) {
		return locator == null ? null : locator
				.property(name, Byte.valueOf(value));
	}

	public static PropertyObjectLocator property(ObjectLocator locator, String name,
			char value) {
		return locator == null ? null : locator.property(name, Character
				.valueOf(value));
	}

	public static PropertyObjectLocator property(ObjectLocator locator, String name,
			double value) {
		return locator == null ? null : locator.property(name, Double
				.valueOf(value));
	}

	public static PropertyObjectLocator property(ObjectLocator locator, String name,
			float value) {
		return locator == null ? null : locator.property(name, Float
				.valueOf(value));
	}

	public static PropertyObjectLocator property(ObjectLocator locator, String name,
			int value) {
		return locator == null ? null : locator.property(name, Integer
				.valueOf(value));
	}

	public static PropertyObjectLocator property(ObjectLocator locator, String name,
			long value) {
		return locator == null ? null : locator
				.property(name, Long.valueOf(value));
	}

	public static PropertyObjectLocator property(ObjectLocator locator, String name,
			short value) {
		return locator == null ? null : locator.property(name, Short
				.valueOf(value));
	}

	public static ItemObjectLocator item(ObjectLocator locator,
			int index, Object value) {
		return locator == null ? null : locator.item(index, value);
	}

	public static ItemObjectLocator item(ObjectLocator locator,
			int index, boolean value) {
		return locator == null ? null : locator.item(index, Boolean
				.valueOf(value));
	}

	public static ItemObjectLocator item(ObjectLocator locator,
			int index, byte value) {
		return locator == null ? null : locator.item(index, Byte
				.valueOf(value));
	}

	public static ItemObjectLocator item(ObjectLocator locator,
			int index, char value) {
		return locator == null ? null : locator.item(index, Character
				.valueOf(value));
	}

	public static ItemObjectLocator item(ObjectLocator locator,
			int index, double value) {
		return locator == null ? null : locator.item(index, Double
				.valueOf(value));
	}

	public static ItemObjectLocator item(ObjectLocator locator,
			int index, float value) {
		return locator == null ? null : locator.item(index, Float
				.valueOf(value));
	}

	public static ItemObjectLocator item(ObjectLocator locator,
			int index, int value) {
		return locator == null ? null : locator.item(index, Integer
				.valueOf(value));
	}

	public static ItemObjectLocator item(ObjectLocator locator,
			int index, long value) {
		return locator == null ? null : locator.item(index, Long
				.valueOf(value));
	}

	public static ItemObjectLocator item(ObjectLocator locator,
			int index, short value) {
		return locator == null ? null : locator.item(index, Short
				.valueOf(value));
	}

}
