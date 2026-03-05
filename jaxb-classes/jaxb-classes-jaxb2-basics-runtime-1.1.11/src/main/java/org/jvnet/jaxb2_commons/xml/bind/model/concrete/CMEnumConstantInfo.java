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
import org.jvnet.jaxb2_commons.xml.bind.model.MEnumConstantInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.MEnumLeafInfo;
import org.jvnet.jaxb2_commons.xml.bind.model.origin.MEnumConstantInfoOrigin;

public class CMEnumConstantInfo<T, C extends T> implements
		MEnumConstantInfo<T, C> {

	private final MEnumConstantInfoOrigin origin;
	private final MEnumLeafInfo<T, C> enumLeafInfo;
	private final String lexicalValue;

	public CMEnumConstantInfo(MEnumConstantInfoOrigin origin,
			MEnumLeafInfo<T, C> enumLeafInfo, String lexicalValue) {
		Validate.notNull(origin);
		Validate.notNull(enumLeafInfo);
		Validate.notNull(lexicalValue);
		this.origin = origin;
		this.enumLeafInfo = enumLeafInfo;
		this.lexicalValue = lexicalValue;
	}

	public MEnumConstantInfoOrigin getOrigin() {
		return origin;
	}

	public MEnumLeafInfo<T, C> getEnumLeafInfo() {
		return enumLeafInfo;
	}

	public String getLexicalValue() {
		return lexicalValue;
	}
}
