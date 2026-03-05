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

import java.text.MessageFormat;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.jvnet.jaxb2_commons.lang.Validate;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MContainer;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPackageInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MElementInfoOrigin;

public class CMElementInfo<T, C extends T> implements MElementInfo<T, C> {

	private final MElementInfoOrigin origin;

	private final MPackageInfo _package;

	private final MContainer container;

	private final String localName;

	private final QName elementName;

	private final MClassInfo<T, C> scope;

	private final MTypeInfo<T, C> typeInfo;

	private final QName substitutionHead;

	private final String defaultValue;

	private final NamespaceContext defaultValueNamespaceContext;

	public CMElementInfo(MElementInfoOrigin origin, MPackageInfo _package,
			MContainer container, String localName, QName elementName,
			MClassInfo<T, C> scope, MTypeInfo<T, C> typeInfo,
			QName substitutionHead, String defaultValue,
			NamespaceContext defaultValueNamespaceContext) {
		super();
		Validate.notNull(origin);
		Validate.notNull(elementName);
		Validate.notNull(_package);
		this.origin = origin;
		this._package = _package;
		this.container = container;
		this.localName = localName;
		this.elementName = elementName;
		this.scope = scope;
		this.typeInfo = typeInfo;
		this.substitutionHead = substitutionHead;
		this.defaultValue = defaultValue;
		this.defaultValueNamespaceContext = defaultValueNamespaceContext;
	}

	public MElementInfoOrigin getOrigin() {
		return origin;
	}

	public MPackageInfo getPackageInfo() {
		return _package;
	}

	public MContainer getContainer() {
		return container;
	}

	public String getLocalName() {
		return localName;
	}

	public String getContainerLocalName(String delimiter) {
		final String localName = getLocalName();
		if (localName == null) {
			return null;
		} else {
			final MContainer container = getContainer();
			if (container == null) {
				return localName;
			} else {
				final String containerLocalName = container
						.getContainerLocalName(delimiter);
				return containerLocalName == null ? localName
						: containerLocalName + delimiter + localName;
			}
		}
	}

	public QName getElementName() {
		return elementName;
	}

	public MClassInfo<T, C> getScope() {
		return scope;
	}

	public MTypeInfo<T, C> getTypeInfo() {
		return typeInfo;
	}

	public QName getSubstitutionHead() {
		return substitutionHead;
	}

	@Override
	public boolean isNillable() {
		return true;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public NamespaceContext getDefaultValueNamespaceContext() {
		return defaultValueNamespaceContext;
	}

	public String toString() {
		return MessageFormat.format("ElementInfo [{0}: {1}]", getElementName(),
				getTypeInfo());
	}

}
