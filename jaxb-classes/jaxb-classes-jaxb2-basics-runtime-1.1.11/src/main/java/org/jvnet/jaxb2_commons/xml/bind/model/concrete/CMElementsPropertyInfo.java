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
import org.jvnet.jaxb2_commons.xml.bind.model.MElementTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementTypeRef;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementsPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPropertyInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MElementOrigin;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MPropertyInfoOrigin;

public class CMElementsPropertyInfo<T, C extends T> extends
		CMPropertyInfo<T, C> implements MElementsPropertyInfo<T, C> {

	private final List<MElementTypeRef<T, C>> elementTypeInfos = new ArrayList<MElementTypeRef<T, C>>();
	private final List<MElementTypeRef<T, C>> unmodifiableElementTypeInfos = Collections
			.unmodifiableList(elementTypeInfos);
	private final QName wrapperElementName;

	public CMElementsPropertyInfo(MPropertyInfoOrigin origin,
			MClassInfo<T, C> classInfo, String privateName, boolean collection,
			boolean required,
			Collection<MElementTypeRef<T, C>> elementTypeInfos,
			QName wrapperElementName) {
		super(origin, classInfo, privateName, collection, required);
		Validate.noNullElements(elementTypeInfos);
		Validate.notEmpty(elementTypeInfos);
		Validate.isTrue(elementTypeInfos.size() > 1);
		this.elementTypeInfos.addAll(elementTypeInfos);
		this.wrapperElementName = wrapperElementName;
	}

	public List<MElementTypeRef<T, C>> getElementTypeInfos() {
		return unmodifiableElementTypeInfos;
	}

	public QName getWrapperElementName() {
		return wrapperElementName;
	}

	public <V> V acceptPropertyInfoVisitor(MPropertyInfoVisitor<T, C, V> visitor) {
		return visitor.visitElementsPropertyInfo(this);
	}

}
