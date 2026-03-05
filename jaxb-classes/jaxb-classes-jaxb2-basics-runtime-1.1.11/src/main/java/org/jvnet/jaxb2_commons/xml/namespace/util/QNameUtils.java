package org.jvnet.jaxb2_commons.xml.namespace.util;

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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

public class QNameUtils {

	private QNameUtils() {
	}

	public static String getKey(QName name) {
		if (name == null) {
			return null;
		}
		final StringBuilder sb = new StringBuilder();
		final String namespaceURI = name.getNamespaceURI();
		if (!namespaceURI.equals(XMLConstants.NULL_NS_URI)) {
			sb.append("{").append(namespaceURI).append("}");
		}
		final String prefix = name.getPrefix();
		if (!XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
			sb.append(prefix).append(":");
		}
		final String localPart = name.getLocalPart();
		sb.append(localPart);
		return sb.toString();

	}
}
