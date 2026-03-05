package org.jvnet.jaxb2_commons.lang;

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

import jakarta.xml.bind.JAXBElement;

import org.jvnet.jaxb2_commons.locator.ObjectLocator;

public class JAXBToStringStrategy extends DefaultToStringStrategy {

	private String jaxbElementStart = "<";

	private String jaxbElementEnd = ">";

	protected void appendJAXBElementStart(StringBuilder stringBuilder) {
		stringBuilder.append(jaxbElementStart);
	}

	protected void appendJAXBElementEnd(StringBuilder stringBuilder) {
		stringBuilder.append(jaxbElementEnd);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected StringBuilder appendInternal(ObjectLocator locator,
			StringBuilder stringBuilder, Object value) {
		if (value instanceof JAXBElement) {
			final JAXBElement jaxbElement = (JAXBElement) value;
			appendInternal(locator, stringBuilder, jaxbElement);
		} else {
			super.appendInternal(locator, stringBuilder, value);
		}
		return stringBuilder;
	}

	@SuppressWarnings("unchecked")
	protected StringBuilder appendInternal(ObjectLocator locator,
			StringBuilder stringBuilder, JAXBElement value) {
		appendJAXBElementStart(stringBuilder);
		stringBuilder.append(value.getName());
		appendContentStart(stringBuilder);
		append(locator, stringBuilder, value.getValue());
		appendContentEnd(stringBuilder);
		appendJAXBElementEnd(stringBuilder);
		return stringBuilder;
	}
	
	public static final JAXBToStringStrategy INSTANCE = new JAXBToStringStrategy();

}
