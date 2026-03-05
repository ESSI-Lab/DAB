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

import org.jvnet.jaxb2_commons.locator.ObjectLocator;

public interface ToStringStrategy2 {
/*
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, boolean value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, byte value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, char value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, double value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, float value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, int value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, long value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, short value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, Object value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, boolean[] value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, byte[] value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, char[] value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, double[] value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, float[] value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, int[] value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, long[] value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, short[] value);
	public StringBuilder append(ObjectLocator locator, StringBuilder stringBuilder, Object[] value);
*/
	public StringBuilder appendStart(ObjectLocator parentLocator, Object parent, StringBuilder stringBuilder);
	public StringBuilder appendEnd(ObjectLocator parentLocator, Object parent, StringBuilder stringBuilder);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, boolean value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, byte value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, char value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, double value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, float value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, int value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, long value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, short value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, Object value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, boolean[] value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, byte[] value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, char[] value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, double[] value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, float[] value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, int[] value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, long[] value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, short[] value, boolean valueSet);
	public StringBuilder appendField(ObjectLocator parentLocator, Object parent, String fieldName, StringBuilder stringBuilder, Object[] value, boolean valueSet);
}
