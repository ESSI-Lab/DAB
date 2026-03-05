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
import org.jvnet.jaxb2_commons.xml.bind.model.MCustomizations;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.MWildcardTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MWildcardTypeInfoOrigin;

public class CMWildcardTypeInfo<T, C extends T> implements
		MWildcardTypeInfo<T, C> {

	private final T targetType;
	private final MWildcardTypeInfoOrigin origin;
	private final MCustomizations customizations = new CMCustomizations();

	public CMWildcardTypeInfo(MWildcardTypeInfoOrigin origin, T targetType) {
		Validate.notNull(origin);
		this.origin = origin;
		this.targetType = targetType;
	}

	public MCustomizations getCustomizations() {
		return customizations;
	}

	public T getTargetType() {
		return targetType;
	}

	@Override
	public QName getTypeName() {
		return null;
	}

	@Override
	public boolean isSimpleType() {
		return false;
	}

	public MWildcardTypeInfoOrigin getOrigin() {
		return origin;
	}

	public <V> V acceptTypeInfoVisitor(MTypeInfoVisitor<T, C, V> visitor) {
		return visitor.visitWildcardTypeInfo(this);
	}

}
