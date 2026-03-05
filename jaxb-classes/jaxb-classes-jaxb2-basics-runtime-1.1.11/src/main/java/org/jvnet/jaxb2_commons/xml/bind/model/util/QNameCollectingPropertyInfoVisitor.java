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

import javax.xml.namespace.QName;

import org.jvnet.jaxb2_commons.lang.Validate;
import org.jvnet.jaxb2_commons.xml.bind.model.MAnyAttributePropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MAnyElementPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MAttributePropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElement;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementRefPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementRefsPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementTypeRef;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementsPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPropertyInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.MValuePropertyInfo;

public class QNameCollectingPropertyInfoVisitor<T, C extends T> implements
		MPropertyInfoVisitor<T, C, Void> {

	private final QNameCollector collector;

	public QNameCollectingPropertyInfoVisitor(QNameCollector collector) {
		Validate.notNull(collector);
		this.collector = collector;
	}

	public Void visitElementPropertyInfo(MElementPropertyInfo<T, C> info) {
		QName wrapperElementName = info.getWrapperElementName();
		if (wrapperElementName != null) {
			collector.element(wrapperElementName);
		}
		QName elementName = info.getElementName();
		collector.element(elementName);
		return null;
	}

	public Void visitElementsPropertyInfo(MElementsPropertyInfo<T, C> info) {
		QName wrapperElementName = info.getWrapperElementName();
		if (wrapperElementName != null) {
			collector.element(wrapperElementName);
		}
		for (MElementTypeRef<T, C> elementTypeInfo : info.getElementTypeInfos()) {
			QName elementName = elementTypeInfo.getElementName();
			collector.element(elementName);
		}
		return null;
	}

	public Void visitAnyElementPropertyInfo(MAnyElementPropertyInfo<T, C> info) {
		return null;
	}

	public Void visitAttributePropertyInfo(MAttributePropertyInfo<T, C> info) {
		collector.attribute(info.getAttributeName());
		return null;
	}

	public Void visitAnyAttributePropertyInfo(
			MAnyAttributePropertyInfo<T, C> info) {
		return null;
	}

	public Void visitValuePropertyInfo(MValuePropertyInfo<T, C> info) {
		return null;
	}

	public Void visitElementRefPropertyInfo(MElementRefPropertyInfo<T, C> info) {
		QName wrapperElementName = info.getWrapperElementName();
		if (wrapperElementName != null) {
			collector.element(wrapperElementName);
		}
		QName elementName = info.getElementName();
		collector.element(elementName);
		return null;
	}

	public Void visitElementRefsPropertyInfo(MElementRefsPropertyInfo<T, C> info) {
		QName wrapperElementName = info.getWrapperElementName();
		if (wrapperElementName != null) {
			collector.element(wrapperElementName);
		}
		for (MElement<T, C> elementTypeInfo : info.getElementTypeInfos()) {
			QName elementName = elementTypeInfo.getElementName();
			collector.element(elementName);
		}
		return null;
	}
}
