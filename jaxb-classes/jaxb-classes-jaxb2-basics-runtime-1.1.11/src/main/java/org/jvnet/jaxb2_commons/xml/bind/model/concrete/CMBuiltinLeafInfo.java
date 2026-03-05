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
import org.jvnet.jaxb2_commons.xml.bind.model.MBuiltinLeafInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MCustomizations;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MBuiltinLeafInfoOrigin;

public class CMBuiltinLeafInfo<T, C extends T> implements MBuiltinLeafInfo<T, C> {

	private final MBuiltinLeafInfoOrigin origin;
	private final T targetType;
	private final QName typeName;
	private final MCustomizations customizations = new CMCustomizations();

	public CMBuiltinLeafInfo(MBuiltinLeafInfoOrigin origin, T targetType,
			QName typeName) {
		Validate.notNull(origin);
		Validate.notNull(targetType);
		Validate.notNull(typeName);
		this.origin = origin;
		this.targetType = targetType;
		this.typeName = typeName;
	}

	public MCustomizations getCustomizations() {
		return customizations;
	}

	public T getTargetType() {
		return targetType;
	}

	public MBuiltinLeafInfoOrigin getOrigin() {
		return origin;
	}

	public QName getTypeName() {
		return typeName;
	}

	@Override
	public boolean isSimpleType() {
		return true;
	}

	public String toString() {
		return "BuiltinLeafInfo [" + getTypeName() + "]";
	}

	public <V> V acceptTypeInfoVisitor(MTypeInfoVisitor<T, C, V> visitor) {
		return visitor.visitBuiltinLeafInfo(this);
	}
}
