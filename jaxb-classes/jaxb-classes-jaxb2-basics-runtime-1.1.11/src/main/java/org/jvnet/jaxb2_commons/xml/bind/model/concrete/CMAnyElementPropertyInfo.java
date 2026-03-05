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

import org.jvnet.jaxb2_commons.xml.bind.model.MAnyElementPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MClassInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPropertyInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MPropertyInfoOrigin;

public class CMAnyElementPropertyInfo<T, C extends T> extends
		CMPropertyInfo<T, C> implements MAnyElementPropertyInfo<T, C> {

	private final boolean mixed;
	private final boolean domAllowed;
	private final boolean typedObjectAllowed;

	public CMAnyElementPropertyInfo(MPropertyInfoOrigin origin,
			MClassInfo<T, C> classInfo, String privateName, boolean collection,
			boolean required, boolean mixed, boolean domAllowed,
			boolean typedObjectAllowed) {
		super(origin, classInfo, privateName, collection, required);
		this.mixed = mixed;
		this.domAllowed = domAllowed;
		this.typedObjectAllowed = typedObjectAllowed;
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
		return visitor.visitAnyElementPropertyInfo(this);
	}

}
