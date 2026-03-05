package org.jvnet.jaxb2_commons.xml.bind.model.util;

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

import org.jvnet.jaxb2_commons.xml.bind.model.MBuiltinLeafInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassRef;
import org.jvnet.jaxb2_commons.xml.bind.model.MEnumLeafInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MID;
import org.jvnet.jaxb2_commons.xml.bind.model.MIDREF;
import org.jvnet.jaxb2_commons.xml.bind.model.MIDREFS;
import org.jvnet.jaxb2_commons.xml.bind.model.MList;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.MWildcardTypeInfo;

public class DefaultTypeInfoVisitor<T, C extends T, V> implements
		MTypeInfoVisitor<T, C, V> {

	public V visitTypeInfo(MTypeInfo<T, C> typeInfo) {
		return null;
	}

	public V visitList(MList<T, C> info) {
		return visitTypeInfo(info);
	}

	public V visitID(MID<T, C> info) {
		return visitTypeInfo(info);
	}

	public V visitIDREF(MIDREF<T, C> info) {
		return visitTypeInfo(info);
	}

	public V visitIDREFS(MIDREFS<T, C> info) {
		return visitTypeInfo(info);
	}

	public V visitBuiltinLeafInfo(MBuiltinLeafInfo<T, C> info) {
		return visitTypeInfo(info);
	}

	public V visitEnumLeafInfo(MEnumLeafInfo<T, C> info) {
		return visitTypeInfo(info);
	}

	public V visitWildcardTypeInfo(MWildcardTypeInfo<T, C> info) {
		return visitTypeInfo(info);
	}

	public V visitClassInfo(MClassInfo<T, C> info) {
		return visitTypeInfo(info);
	}
	
	public V visitClassRef(MClassRef<T, C> info) {
		return visitTypeInfo(info);
	}
}
