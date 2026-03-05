package org.jvnet.jaxb2_commons.xml.bind.model.concrete;

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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.jvnet.jaxb2_commons.xml.bind.model.MElementTypeRef;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MElementTypeRefOrigin;

public class CMElementTypeRef<T, C extends T> extends
		CMElementTypeInfo<T, C, MElementTypeRefOrigin> implements
		MElementTypeRef<T, C> {

	public CMElementTypeRef(MElementTypeRefOrigin origin, QName elementName,
			MTypeInfo<T, C> typeInfo, boolean nillable, String defaultValue,
			NamespaceContext defaultValueNamespaceContext) {
		super(origin, elementName, typeInfo, nillable, defaultValue,
				defaultValueNamespaceContext);
	}

	@Override
	public String toString() {
		return "Element [" + getElementName() + ":" + getTypeInfo() + "]";
	}
}
