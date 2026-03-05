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

@Deprecated
public interface CopyStrategy {

	public boolean copy(ObjectLocator locator, boolean value);

	public byte copy(ObjectLocator locator, byte value);

	public char copy(ObjectLocator locator, char value);

	public double copy(ObjectLocator locator, double value);

	public float copy(ObjectLocator locator, float value);

	public int copy(ObjectLocator locator, int value);

	public long copy(ObjectLocator locator, long value);

	public short copy(ObjectLocator locator, short value);

	public Object copy(ObjectLocator locator, Object value);

	public boolean[] copy(ObjectLocator locator, boolean[] value);

	public byte[] copy(ObjectLocator locator, byte[] value);

	public char[] copy(ObjectLocator locator, char[] value);

	public double[] copy(ObjectLocator locator, double[] value);

	public float[] copy(ObjectLocator locator, float[] value);

	public int[] copy(ObjectLocator locator, int[] value);

	public long[] copy(ObjectLocator locator, long[] value);

	public short[] copy(ObjectLocator locator, short[] value);

	public Object[] copy(ObjectLocator locator, Object[] value);

}
