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

import javax.xml.namespace.QName;

import org.jvnet.jaxb2_commons.lang.Validate;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassRef;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassTypeInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.MContainer;
import org.jvnet.jaxb2_commons.xml.bind.model.MCustomizations;
import org.jvnet.jaxb2_commons.xml.bind.model.MPackageInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MClassRefOrigin;
import org.jvnet.jaxb2_commons.xml.bind.model.util.XmlTypeUtils;

public class CMClassRef<T, C extends T> implements MClassRef<T, C> {

	private CMCustomizations customizations = new CMCustomizations();
	private final MClassRefOrigin origin;
	private final C targetType;
	private final Class<?> targetClass;
	private final MPackageInfo _package;
	private final String name;
	private final String localName;
	private final MContainer container;
	private final QName typeName;

	public CMClassRef(MClassRefOrigin origin, C targetType,
			Class<?> targetClass, MPackageInfo _package, MContainer container,
			String localName) {
		super();
		Validate.notNull(origin);
		Validate.notNull(targetType);
		Validate.notNull(_package);
		Validate.notNull(localName);
		this.origin = origin;
		this.targetType = targetType;
		this.name = _package.getPackagedName(localName);
		this.localName = localName;
		this._package = _package;
		this.container = container;
		this.targetClass = targetClass;
		this.typeName = targetClass == null ? null : XmlTypeUtils
				.getTypeName(targetClass);
	}

	@Override
	public MClassRefOrigin getOrigin() {
		return this.origin;
	}

	public String getName() {
		return name;
	}

	public String getLocalName() {
		return localName;
	}

	public C getTargetType() {
		return targetType;
	}

	@Override
	public QName getTypeName() {
		return typeName;
	}

	@Override
	public boolean isSimpleType() {
		return false;
	}

	public <V> V acceptTypeInfoVisitor(MTypeInfoVisitor<T, C, V> visitor) {
		return visitor.visitClassRef(this);
	}

	public MCustomizations getCustomizations() {
		return customizations;
	}

	public MPackageInfo getPackageInfo() {
		return _package;
	}

	public MContainer getContainer() {
		return container;
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

	public C getTargetClass() {
		return targetType;
	}

	@Override
	public <V> V acceptClassTypeInfoVisitor(
			MClassTypeInfoVisitor<T, C, V> visitor) {
		return visitor.visitClassRef(this);
	}

}
