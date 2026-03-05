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

public interface MergeStrategy2 {

	public Boolean shouldBeMergedAndSet(ObjectLocator leftLocator,
			ObjectLocator rightLocator, boolean leftSet, boolean rightSet);

	public boolean merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			boolean left, boolean right, boolean leftSet, boolean rightSet);

	public byte merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			byte left, byte right, boolean leftSet, boolean rightSet);

	public char merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			char left, char right, boolean leftSet, boolean rightSet);

	public double merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			double left, double right, boolean leftSet, boolean rightSet);

	public float merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			float left, float right, boolean leftSet, boolean rightSet);

	public int merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			int left, int right, boolean leftSet, boolean rightSet);

	public long merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			long left, long right, boolean leftSet, boolean rightSet);

	public short merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			short left, short right, boolean leftSet, boolean rightSet);

	public Object merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			Object left, Object right, boolean leftSet, boolean rightSet);

	public boolean[] merge(ObjectLocator leftLocator,
			ObjectLocator rightLocator, boolean[] left, boolean[] right,
			boolean leftSet, boolean rightSet);

	public byte[] merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			byte[] left, byte[] right, boolean leftSet, boolean rightSet);

	public char[] merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			char[] left, char[] right, boolean leftSet, boolean rightSet);

	public double[] merge(ObjectLocator leftLocator,
			ObjectLocator rightLocator, double[] left, double[] right,
			boolean leftSet, boolean rightSet);

	public float[] merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			float[] left, float[] right, boolean leftSet, boolean rightSet);

	public int[] merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			int[] left, int[] right, boolean leftSet, boolean rightSet);

	public long[] merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			long[] left, long[] right, boolean leftSet, boolean rightSet);

	public short[] merge(ObjectLocator leftLocator, ObjectLocator rightLocator,
			short[] left, short[] right, boolean leftSet, boolean rightSet);

	public Object[] merge(ObjectLocator leftLocator,
			ObjectLocator rightLocator, Object[] left, Object[] right,
			boolean leftSet, boolean rightSet);
}
