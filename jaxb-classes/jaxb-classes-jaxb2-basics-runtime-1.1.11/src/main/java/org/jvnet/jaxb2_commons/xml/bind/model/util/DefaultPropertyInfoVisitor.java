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

import org.jvnet.jaxb2_commons.xml.bind.model.MAnyAttributePropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MAnyElementPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MAttributePropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementRefPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementRefsPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MElementsPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPropertyInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MPropertyInfoVisitor;
import org.jvnet.jaxb2_commons.xml.bind.model.MValuePropertyInfo;

public class DefaultPropertyInfoVisitor<T, C extends T, V> implements
		MPropertyInfoVisitor<T, C, V> {

	public V visitPropertyInfo(MPropertyInfo<T, C> info) {
		return null;
	}

	public V visitElementPropertyInfo(MElementPropertyInfo<T, C> info) {
		return visitPropertyInfo(info);
	}

	public V visitElementsPropertyInfo(MElementsPropertyInfo<T, C> info) {
		return visitPropertyInfo(info);
	}

	public V visitAnyElementPropertyInfo(MAnyElementPropertyInfo<T, C> info) {
		return visitPropertyInfo(info);
	}

	public V visitAttributePropertyInfo(MAttributePropertyInfo<T, C> info) {
		return visitPropertyInfo(info);
	}

	public V visitAnyAttributePropertyInfo(MAnyAttributePropertyInfo<T, C> info) {
		return visitPropertyInfo(info);
	}

	public V visitValuePropertyInfo(MValuePropertyInfo<T, C> info) {
		return visitPropertyInfo(info);
	}

	public V visitElementRefPropertyInfo(MElementRefPropertyInfo<T, C> info) {
		return visitPropertyInfo(info);
	}

	public V visitElementRefsPropertyInfo(MElementRefsPropertyInfo<T, C> info) {
		return visitPropertyInfo(info);
	}

}
