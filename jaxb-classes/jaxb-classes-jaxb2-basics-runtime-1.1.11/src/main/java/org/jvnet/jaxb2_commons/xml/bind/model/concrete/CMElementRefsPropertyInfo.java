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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.jvnet.jaxb2_commons.lang.Validate;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElement;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementRefsPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPropertyInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MElementOrigin;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MPropertyInfoOrigin;

public class CMElementRefsPropertyInfo<T, C extends T> extends
		CMPropertyInfo<T, C> implements MElementRefsPropertyInfo<T, C> {

	private final QName wrapperElementName;

	private final List<MElement<T, C>> elementTypeInfos = new ArrayList<MElement<T, C>>();
	private final List<MElement<T, C>> unmodifiableElementTypeInfos = Collections
			.unmodifiableList(elementTypeInfos);

	private final boolean mixed;
	private final boolean domAllowed;
	private final boolean typedObjectAllowed;

	public CMElementRefsPropertyInfo(
			MPropertyInfoOrigin origin,
			MClassInfo<T, C> classInfo,
			String privateName,
			boolean collection,
			boolean required,
			Collection<MElement<T, C>> elementTypeInfos,
			QName wrapperElementName, boolean mixed, boolean domAllowed,
			boolean typedObjectAllowed) {
		super(origin, classInfo, privateName, collection, required);
		Validate.noNullElements(elementTypeInfos);
		// Validate.notEmpty(elementTypeInfos);
		// Validate.isTrue(elementTypeInfos.size() > 1);
		this.elementTypeInfos.addAll(elementTypeInfos);
		this.wrapperElementName = wrapperElementName;
		this.mixed = mixed;
		this.domAllowed = domAllowed;
		this.typedObjectAllowed = typedObjectAllowed;
	}

	public List<MElement<T, C>> getElementTypeInfos() {
		return unmodifiableElementTypeInfos;
	}

	public QName getWrapperElementName() {
		return wrapperElementName;
	}

	public boolean isMixed() {
		return mixed;
	}

	public boolean isDomAllowed() {
		return domAllowed;
	}

	public boolean isTypedObjectAllowed() {
		return typedObjectAllowed;
	}

	public <V> V acceptPropertyInfoVisitor(MPropertyInfoVisitor<T, C, V> visitor) {
		return visitor.visitElementRefsPropertyInfo(this);
	}

}
