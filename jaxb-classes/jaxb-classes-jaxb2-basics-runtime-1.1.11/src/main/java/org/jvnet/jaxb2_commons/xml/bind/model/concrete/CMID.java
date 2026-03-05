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

import javax.xml.namespace.QName;

import org.jvnet.jaxb2_commons.lang.Validate;
import org.jvnet.jaxb2_commons.xml.bind.model.MCustomizations;
import org.jvnet.jaxb2_commons.xml.bind.model.MID;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MTypeInfoVisitor;
import org.jvnet.jaxb2_commons.xmlschema.XmlSchemaConstants;

public class CMID<T, C extends T> implements MID<T, C> {

	private final MTypeInfo<T, C> valueTypeInfo;
	private final T targetType;
	private final MCustomizations customizations = new CMCustomizations();

	public CMID(T targetType, MTypeInfo<T, C> itemTypeInfo) {
		Validate.notNull(targetType);
		Validate.notNull(itemTypeInfo);
		this.targetType = targetType;
		this.valueTypeInfo = itemTypeInfo;
	}

	public MCustomizations getCustomizations() {
		return customizations;
	}

	public T getTargetType() {
		return targetType;
	}

	public MTypeInfo<T, C> getValueTypeInfo() {
		return valueTypeInfo;
	}

	public QName getTypeName() {
		return XmlSchemaConstants.ID;
	}
	
	@Override
	public boolean isSimpleType() {
		return getValueTypeInfo().isSimpleType();
	}

	@Override
	public String toString() {
		return MessageFormat.format("ID [{0}]", getValueTypeInfo());
	}

	public <V> V acceptTypeInfoVisitor(MTypeInfoVisitor<T, C, V> visitor) {
		return visitor.visitID(this);
	}
}
