package eu.essi_lab.iso.datamodel;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLFactories;

public abstract class DOMSerializer {

    public void toStream(OutputStream out, boolean omitXMLdeclaration) throws JAXBException {

	Marshaller marshaller = createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FRAGMENT, omitXMLdeclaration);
	marshaller.marshal(getElement(), out);
    }

    public void toStream(OutputStream out) throws JAXBException {

	toStream(out, false);
    }

    public String asString(boolean omitXMLdeclaration) throws JAXBException, UnsupportedEncodingException {

	Marshaller marshaller = createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FRAGMENT, omitXMLdeclaration);

	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	marshaller.marshal(getElement(), outputStream);

	return outputStream.toString("UTF-8").trim();
    }

    public InputStream asStream(boolean omitXMLdeclaration) throws JAXBException, UnsupportedEncodingException {

	return new ByteArrayInputStream(asString(omitXMLdeclaration).getBytes(StandardCharsets.UTF_8));
    }

    public InputStream asStream() throws JAXBException, UnsupportedEncodingException {

	return asStream(false);
    }

    public Document asDocument(boolean omitXMLdeclaration) throws ParserConfigurationException, JAXBException, SAXException, IOException {

	InputStream asStream = asStream(omitXMLdeclaration);

	DocumentBuilderFactory dbf = XMLFactories.newDocumentBuilderFactory();
	
	DocumentBuilder builder = dbf.newDocumentBuilder();

	return builder.parse(asStream);
    }

    public abstract Object fromStream(InputStream stream) throws JAXBException;

    public abstract Object fromNode(Node stream) throws JAXBException;

    protected abstract Unmarshaller createUnmarshaller() throws JAXBException;

    protected abstract Marshaller createMarshaller() throws JAXBException;

    protected abstract Object getElement() throws JAXBException;

}
