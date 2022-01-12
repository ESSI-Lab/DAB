package eu.essi_lab.lib.xml;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
public class StAXDocumentReader implements Iterator<XMLDocumentReader> {

    private static final boolean DEBUG = false;
    private XMLEventReader reader;
    private String searchElement;
    private ByteArrayOutputStream nextSplit = null;
    private transient Logger logger = GSLoggerFactory.getLogger(StAXDocumentReader.class);

    public StAXDocumentReader(InputStream stream, String searchElement) throws XMLStreamException {
	XMLInputFactory factory = XMLInputFactory.newInstance();
	this.reader = factory.createXMLEventReader(stream);
	this.searchElement = searchElement;

    }

    public void close() throws XMLStreamException {
	logger.info("Closing reader");
	reader.close();
    }

    @Override
    public boolean hasNext() {
	try {
	    if (nextSplit != null) {
		return true;
	    }

	    nextSplit = new ByteArrayOutputStream();
	    XMLEventWriter writer = createXMLWriter(nextSplit);

	    while (reader.hasNext()) {

		XMLEvent event = reader.nextEvent();
		printEvent(event);

		if (event.isStartElement()) {

		    StartElement startElement = event.asStartElement();

		    if (startElement.getName().getLocalPart().equals(searchElement)) {

			while (!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(searchElement))) {
			    // System.out.println("Adding " + event.getEventType() + " ");
			    writer.add(event);
			    if (reader.hasNext()) {
				event = reader.nextEvent();
				printEvent(event);
			    }
			}

			writer.add(event);

			writer.close(); // Also closes any open Element(s) and the document

			return true;
		    }

		}
	    }
	    close();
	} catch (Exception e) {
	    logger.error("Probably not a valid XML. Inner message: " + e.getMessage());
	}
	return false;
    }

    @Override
    public XMLDocumentReader next() {
	if (nextSplit == null) {
	    if (!hasNext()) {
		return null;
	    }
	}
	try {
	    ByteArrayInputStream bais = new ByteArrayInputStream(nextSplit.toByteArray());
	    nextSplit = null;
	    return new XMLDocumentReader(bais);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    private XMLEventWriter createXMLWriter(ByteArrayOutputStream baos) throws Exception {
	XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
	XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
	xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
	XMLEventWriter writer = xmlOutputFactory.createXMLEventWriter(baos);
	// writer.setDefaultNamespace(DOCUMENT_NS);
	writer.add(xmlEventFactory.createStartDocument());
	return writer;
    }

    private void printEvent(XMLEvent event) {
	if (DEBUG) {
	    switch (event.getEventType()) {
	    case XMLStreamConstants.START_ELEMENT:
		System.out.println("Start element: " + event.asStartElement().getName().getLocalPart());
		break;
	    case XMLStreamConstants.END_ELEMENT:
		System.out.println("End element:  " + event.asEndElement().getName().getLocalPart());
		break;
	    case XMLStreamConstants.CHARACTERS:
		System.out.println("Characters: " + event.asCharacters().getData());
		break;
	    case XMLStreamConstants.ATTRIBUTE:
		System.out.println("Attribute");
		break;
	    case XMLStreamConstants.NAMESPACE:
		System.out.println("Namespace");
		break;
	    case XMLStreamConstants.PROCESSING_INSTRUCTION:
		System.out.println("Processing instruction");
		break;
	    case XMLStreamConstants.COMMENT:
		System.out.println("Comment");
		break;
	    case XMLStreamConstants.START_DOCUMENT:
		System.out.println("Start document");
		break;
	    case XMLStreamConstants.END_DOCUMENT:
		System.out.println("End document");
		break;
	    case XMLStreamConstants.DTD:
		System.out.println("DTD");
		break;
	    default:
		System.out.println("Unknown");
		break;
	    }
	}
    }

}
