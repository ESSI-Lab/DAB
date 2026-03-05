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

import org.jvnet.jaxb2_commons.lang.Validate;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfo;

public abstract class CMElementTypeInfo<T, C extends T, O> implements
		MElementTypeInfo<T, C, O> {

	private final QName elementName;

	private final MTypeInfo<T, C> typeInfo;

	private final boolean nillable;

	private final String defaultValue;

	private final O origin;

	private final NamespaceContext defaultValueNamespaceContext;

	public CMElementTypeInfo(O origin, QName elementName,
			MTypeInfo<T, C> typeInfo, boolean nillable, String defaultValue,
			NamespaceContext defaultValueNamespaceContext) {
		Validate.notNull(origin);
		Validate.notNull(elementName);
		Validate.notNull(typeInfo);
		this.origin = origin;
		this.elementName = elementName;
		this.typeInfo = typeInfo;
		this.nillable = nillable;
		this.defaultValue = defaultValue;
		this.defaultValueNamespaceContext = defaultValueNamespaceContext;
	}

	@Override
	public O getOrigin() {
		return this.origin;
	}

	public QName getElementName() {
		return elementName;
	}

	public MTypeInfo<T, C> getTypeInfo() {
		return typeInfo;
	}

	public boolean isNillable() {
		return this.nillable;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public NamespaceContext getDefaultValueNamespaceContext() {
		return defaultValueNamespaceContext;
	}

}
