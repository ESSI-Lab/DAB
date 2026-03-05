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

import org.jvnet.jaxb2_commons.lang.Validate;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MSingleTypePropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MPropertyInfoOrigin;

public abstract class CMSingleTypePropertyInfo<T, C extends T> extends
		CMPropertyInfo<T, C> implements MSingleTypePropertyInfo<T, C> {

	private final MTypeInfo<T, C> typeInfo;
	private final String defaultValue;
	private final NamespaceContext defaultValueNamespaceContext;

	public CMSingleTypePropertyInfo(MPropertyInfoOrigin origin,
			MClassInfo<T, C> classInfo, String privateName, boolean collection,
			MTypeInfo<T, C> typeInfo, boolean required, String defaultValue,
			NamespaceContext defaultValueNamespaceContext) {
		super(origin, classInfo, privateName, collection, required);
		Validate.notNull(typeInfo);
		this.typeInfo = typeInfo;
		this.defaultValue = defaultValue;
		this.defaultValueNamespaceContext = defaultValueNamespaceContext;
	}

	@Override
	public String getDefaultValue() {
		return this.defaultValue;
	}

	@Override
	public NamespaceContext getDefaultValueNamespaceContext() {
		return this.defaultValueNamespaceContext;
	}

	public MTypeInfo<T, C> getTypeInfo() {
		return typeInfo;
	}

}
