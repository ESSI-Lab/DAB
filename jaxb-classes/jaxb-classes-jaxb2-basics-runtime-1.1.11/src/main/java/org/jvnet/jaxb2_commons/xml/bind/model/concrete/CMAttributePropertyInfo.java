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
import org.jvnet.jaxb2_commons.xml.bind.model.MAttributePropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPropertyInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MPropertyInfoOrigin;

public class CMAttributePropertyInfo<T, C extends T> extends
		CMSingleTypePropertyInfo<T, C> implements MAttributePropertyInfo<T, C> {

	private final QName attributeName;

	public CMAttributePropertyInfo(MPropertyInfoOrigin origin,
			MClassInfo<T, C> classInfo, String privateName,
			MTypeInfo<T, C> typeInfo, QName attributeName, boolean required,
			String defaultValue, NamespaceContext defaultValueNamespaceContext) {
		super(origin, classInfo, privateName, false, typeInfo, required,
				defaultValue, defaultValueNamespaceContext);
		Validate.notNull(attributeName);
		this.attributeName = attributeName;
	}

	public QName getAttributeName() {
		return attributeName;
	}

	public <V> V acceptPropertyInfoVisitor(MPropertyInfoVisitor<T, C, V> visitor) {
		return visitor.visitAttributePropertyInfo(this);
	}

}
