package org.jvnet.jaxb2_commons.xml.bind.model.concrete.origin;

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
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MElementTypeRefOrigin;

import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeRef;

public class CMElementTypeRefOrigin<T, C, EPI extends ElementPropertyInfo<T, C>, TR extends TypeRef<T, C>>
		implements MElementTypeRefOrigin {

	private final EPI source;
	private final TR typeRef;

	public CMElementTypeRefOrigin(EPI source, TR typeRef) {
		Validate.notNull(source);
		Validate.notNull(typeRef);
		this.source = source;
		this.typeRef = typeRef;
	}

	public EPI getSource() {
		return source;
	}

	public TR getTypeRef() {
		return typeRef;
	}
}
