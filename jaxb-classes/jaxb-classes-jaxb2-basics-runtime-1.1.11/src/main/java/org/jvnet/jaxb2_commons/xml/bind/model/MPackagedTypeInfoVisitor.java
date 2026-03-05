package org.jvnet.jaxb2_commons.xml.bind.model;

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

public abstract class MPackagedTypeInfoVisitor<T, C extends T, V> implements
		MTypeInfoVisitor<T, C, V> {

	public abstract V visitPackagedTypeInfo(MPackagedTypeInfo<T, C> info);

	public V visitList(MList<T, C> info) {
		return null;
	}

	public V visitBuiltinLeafInfo(MBuiltinLeafInfo<T, C> info) {
		return null;
	}

	public V visitID(MID<T, C> info) {
		return null;
	}

	public V visitIDREF(MIDREF<T, C> info) {
		return null;
	}

	public V visitIDREFS(MIDREFS<T, C> info) {
		return null;
	}

	public V visitEnumLeafInfo(MEnumLeafInfo<T, C> info) {
		return visitPackagedTypeInfo(info);
	}

	public V visitWildcardTypeInfo(MWildcardTypeInfo<T, C> info) {
		return null;
	}

	public V visitClassInfo(MClassInfo<T, C> info) {
		return visitPackagedTypeInfo(info);
	}

}
