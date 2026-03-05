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

public interface HashCodeStrategy2 {
	
	public int hashCode(ObjectLocator locator, int hashCode, boolean value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, byte value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, char value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, double value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, float value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, int value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, long value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, short value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, Object value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, boolean[] value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, byte[] value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, char[] value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, double[] value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, float[] value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, int[] value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, long[] value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, short[] value, boolean valueSet);
	public int hashCode(ObjectLocator locator, int hashCode, Object[] value, boolean valueSet);
	
}
