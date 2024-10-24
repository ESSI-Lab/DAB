package eu.essi_lab.jaxb.common.schemas;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class SchemaValidator {

    private Unmarshaller unmarshaller;

    public SchemaValidator() {
	try {

	    unmarshaller = CommonContext.createUnmarshaller();

	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(SchemaValidator.class).error(e.getMessage(), e);
	}
    }

    /**
     * Supported types:
     * <ul>
     * <li>java.io.File</li>
     * <li>java.io.InputStream</li>
     * <li>org.w3c.dom.Node</li>
     * <li>javax.xml.transform.Source</li>
     * <li>org.xml.sax.InputSource</li>
     * <li>java.io.Reader</li>
     * <li>java.net.URL</li>
     * </ul>
     * 
     * @param element
     * @return
     * @throws JAXBException
     */
    public BooleanValidationHandler validate(Object element, Schema schema) throws JAXBException {

	BooleanValidationHandler handler = new BooleanValidationHandler();
	unmarshaller.setSchema(schema);

	unmarshal(element, handler);

	return handler;
    }

    private Object unmarshal(Object element, BooleanValidationHandler handler) throws JAXBException {

	Object o = null;
	unmarshaller.setEventHandler(handler);

	if (element instanceof File) {
	    o = unmarshaller.unmarshal((File) element);
	}

	else if (element instanceof InputStream) {
	    o = unmarshaller.unmarshal((InputStream) element);
	}

	else if (element instanceof Node) {
	    o = unmarshaller.unmarshal((Node) element);
	}

	else if (element instanceof InputSource) {
	    o = unmarshaller.unmarshal((InputSource) element);
	}

	else if (element instanceof Reader) {
	    o = unmarshaller.unmarshal((Reader) element);
	}

	else if (element instanceof Source) {
	    o = unmarshaller.unmarshal((Source) element);
	}

	else if (element instanceof URL) {
	    o = unmarshaller.unmarshal((URL) element);
	}

	return o;
    }
}
