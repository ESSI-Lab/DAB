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

import org.jvnet.jaxb2_commons.lang.Validate;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MCustomizable;
import org.jvnet.jaxb2_commons.xml.bind.model.MCustomizations;
import org.jvnet.jaxb2_commons.xml.bind.model.MPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MPropertyInfoOrigin;

public abstract class CMPropertyInfo<T, C extends T> implements
		MPropertyInfo<T, C>, MCustomizable {

	private CMCustomizations customizations = new CMCustomizations();
	private MPropertyInfoOrigin origin;
	private MClassInfo<T, C> classInfo;

	private final String privateName;

	private final boolean collection;

	private final boolean required;

	public CMPropertyInfo(MPropertyInfoOrigin origin,
			MClassInfo<T, C> classInfo, String privateName, boolean collection,
			boolean required) {
		Validate.notNull(origin);
		Validate.notNull(classInfo);
		Validate.notNull(privateName);
		this.origin = origin;
		this.classInfo = classInfo;
		this.privateName = privateName;
		this.collection = collection;
		this.required = required;
	}

	public MCustomizations getCustomizations() {
		return customizations;
	}

	public MPropertyInfoOrigin getOrigin() {
		return origin;
	}

	public MClassInfo<T, C> getClassInfo() {
		return classInfo;
	}

	public String getPrivateName() {
		return privateName;
	}

	public String getPublicName() {
		// TODO
		return this.getPrivateName();
	}

	public boolean isCollection() {
		return collection;
	}

	public boolean isRequired() {
		return required;
	}

}
