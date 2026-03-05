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

import java.io.StringWriter;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class ContextUtils {

	public static String getContextPath(Class<?>... classes) {
        if (classes == null) {
            throw new IllegalArgumentException("The validated object is null");
        }
        for (int i = 0; i < classes.length; i++) {
            if (classes[i] == null) {
                throw new IllegalArgumentException("The validated array contains null element at index: " + i);
            }
        }

		final StringBuilder contextPath = new StringBuilder();

		for (int index = 0; index < classes.length; index++) {
			if (index > 0) {
				contextPath.append(':');
			}
			contextPath.append(classes[index].getPackage().getName());
		}
		return contextPath.toString();
	}

	public static String toString(JAXBContext context, Object object)
			throws JAXBException {
		final Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		final StringWriter sw = new StringWriter();
		marshaller.marshal(object, sw);
		return sw.toString();
	}
}
