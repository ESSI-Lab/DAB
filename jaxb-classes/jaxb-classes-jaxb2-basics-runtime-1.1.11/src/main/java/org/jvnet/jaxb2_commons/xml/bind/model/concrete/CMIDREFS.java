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

import java.text.MessageFormat;

import org.jvnet.jaxb2_commons.xml.bind.model.MIDREFS;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfoVisitor;
import org.jvnet.jaxb2_commons.xmlschema.XmlSchemaConstants;

public class CMIDREFS<T, C extends T> extends CMList<T, C> implements MIDREFS<T, C> {

	public CMIDREFS(T targetType, MTypeInfo<T, C> itemTypeInfo) {
		super(targetType, itemTypeInfo, XmlSchemaConstants.IDREFS);
	}

	@Override
	public String toString() {
		return MessageFormat.format("IDREFS [{0}]", getItemTypeInfo());
	}

	public <V> V acceptTypeInfoVisitor(MTypeInfoVisitor<T, C, V> visitor) {
		return visitor.visitIDREFS(this);
	}
}
