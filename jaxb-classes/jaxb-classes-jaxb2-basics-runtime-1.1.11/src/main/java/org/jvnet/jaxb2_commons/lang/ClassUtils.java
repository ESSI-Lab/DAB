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

public class ClassUtils {

	private ClassUtils() {
	}

	public static final char PACKAGE_SEPARATOR_CHAR = '.';

	public static final char INNER_CLASS_SEPARATOR_CHAR = '$';

	/**
	 * <p>
	 * Gets the class name minus the package name from a <code>Class</code>.
	 * </p>
	 * 
	 * @param cls
	 *            the class to get the short name for.
	 * @return the class name without the package name or an empty string
	 */
	@SuppressWarnings("unchecked")
	public static String getShortClassName(Class cls) {
		if (cls == null) {
			return "";
		}
		return getShortClassName(cls.getName());
	}

	/**
	 * <p>
	 * Gets the class name minus the package name from a String.
	 * </p>
	 * 
	 * <p>
	 * The string passed in is assumed to be a class name - it is not checked.
	 * </p>
	 * 
	 * @param className
	 *            the className to get the short name for
	 * @return the class name of the class without the package name or an empty
	 *         string
	 */
	public static String getShortClassName(String className) {
		if (className == null) {
			return "";
		}
		if (className.length() == 0) {
			return "";
		}
		char[] chars = className.toCharArray();
		int lastDot = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == PACKAGE_SEPARATOR_CHAR) {
				lastDot = i + 1;
			} else if (chars[i] == INNER_CLASS_SEPARATOR_CHAR) { // handle inner
				// classes
				chars[i] = PACKAGE_SEPARATOR_CHAR;
			}
		}
		return new String(chars, lastDot, chars.length - lastDot);
	}

}
