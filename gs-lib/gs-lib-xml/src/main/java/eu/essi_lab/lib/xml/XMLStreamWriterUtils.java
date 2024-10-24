package eu.essi_lab.lib.xml;

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

import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.soap.SOAPConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XMLStreamWriterUtils {

    private XMLStreamWriterUtils() {
    }

    public static XMLStreamWriter getSOAPWriter(OutputStream baos) throws XMLStreamException {
	return getSOAPWriter(baos, null);
    }

    public static XMLStreamWriter getSOAPWriter(OutputStream baos, NamespaceContext nc) throws XMLStreamException {
	XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(baos);
	if (nc != null) {
	    writer.setNamespaceContext(nc);
	}
	writer.writeStartDocument();
	writer.writeStartElement("soap", "Envelope", SOAPConstants.URI_NS_SOAP_ENVELOPE);
	writer.setPrefix("soap", SOAPConstants.URI_NS_SOAP_ENVELOPE);
	writer.writeNamespace("soap", SOAPConstants.URI_NS_SOAP_ENVELOPE);
	writer.setPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
	writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
	writer.setPrefix("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
	writer.writeNamespace("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);

	writer.writeStartElement("soap", "Body", SOAPConstants.URI_NS_SOAP_ENVELOPE);
	return writer;
    }

}
