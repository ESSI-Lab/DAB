package eu.essi_lab.access.wml;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import eu.essi_lab.lib.xml.*;
import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.VariablesResponseType;
import org.cuahsi.waterml._1.essi.JAXBWML;

public class TimeSeriesTemplate {

    private boolean finalized = false;

    private File templateFile = null;

    private File dataFile = null;

    private XMLStreamReader reader;

    private XMLStreamWriter writer;

    private ObjectFactory jaxbFactory = null;

    private Marshaller jaxbMarshaller;

    public TimeSeriesTemplate(File dataFile, TimeSeriesResponseType timeSeriesTemplate) throws Exception {
	// preparing template
	this.jaxbFactory  = new ObjectFactory();
	this.jaxbMarshaller = JAXBWML.getInstance().getMarshaller();
	jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
	JAXBElement<TimeSeriesResponseType> response = jaxbFactory.createTimeSeriesResponse(timeSeriesTemplate);
	templateFile = Files.createTempFile(getClass().getSimpleName(), ".wml").toFile();
	this.dataFile = dataFile;
	JAXBWML.getInstance().marshal(response, templateFile);

	// start writing data file
	XMLInputFactory inputFactory = XMLFactories.newXMLInputFactory();
	
	XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

	this.reader = inputFactory.createXMLStreamReader(new FileInputStream(templateFile));
	this.writer = outputFactory.createXMLStreamWriter(new FileOutputStream(dataFile), "UTF-8");

	while (reader.hasNext()) {
	    int event = reader.next();

	    if (event == XMLStreamConstants.START_ELEMENT && "values".equals(reader.getLocalName())) {
	        // Write <values> start element and stop
	        writeStartElementWithNamespaces();
	        writer.writeCharacters("\n");
	        break;
	    } else {
	        switch (event) {
	            case XMLStreamConstants.START_ELEMENT:
	                writeStartElementWithNamespaces();
	                break;

	            case XMLStreamConstants.END_ELEMENT:
	                writer.writeEndElement();
	                break;

	            case XMLStreamConstants.CHARACTERS:
	                if (!reader.isWhiteSpace()) {
	                    writer.writeCharacters(reader.getText());
	                }
	                break;

	            case XMLStreamConstants.COMMENT:
	                writer.writeComment(reader.getText());
	                break;

	            case XMLStreamConstants.PROCESSING_INSTRUCTION:
	                writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
	                break;
	        }
	    }
	}

	writer.flush();
    }
    
    private void writeStartElementWithNamespaces() throws XMLStreamException {
	    String prefix = reader.getPrefix();
	    String localName = reader.getLocalName();
	    String namespaceURI = reader.getNamespaceURI();

	    if (namespaceURI != null && !namespaceURI.isEmpty()) {
	        writer.writeStartElement(prefix != null ? prefix : "", localName, namespaceURI);

	        // Set prefix if needed
	        if (prefix != null) {
	            writer.setPrefix(prefix, namespaceURI);
	        }

	        // Write namespace declarations
	        for (int i = 0; i < reader.getNamespaceCount(); i++) {
	            String nsPrefix = reader.getNamespacePrefix(i);
	            String nsURI = reader.getNamespaceURI(i);
	            writer.writeNamespace(nsPrefix == null ? "" : nsPrefix, nsURI);
	        }
	    } else {
	        writer.writeStartElement(localName);
	    }

	    // Write attributes (including namespaced ones)
	    for (int i = 0; i < reader.getAttributeCount(); i++) {
	        String attrPrefix = reader.getAttributePrefix(i);
	        String attrNamespace = reader.getAttributeNamespace(i);
	        String attrLocalName = reader.getAttributeLocalName(i);
	        String attrValue = reader.getAttributeValue(i);

	        if (attrNamespace != null && !attrNamespace.isEmpty()) {
	            writer.writeAttribute(attrPrefix, attrNamespace, attrLocalName, attrValue);
	        } else {
	            writer.writeAttribute(attrLocalName, attrValue);
	        }
	    }
	}


    public File getDataFile() throws Exception {
	if (!finalized) {
	    // finalize

	    // Resume reading the rest of the XML
	    while (reader.hasNext()) {
		int event = reader.next();

		switch (event) {
		case XMLStreamConstants.START_ELEMENT:
		    writer.writeStartElement(reader.getLocalName());
		    for (int i = 0; i < reader.getAttributeCount(); i++) {
			writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
		    }
		    break;
		case XMLStreamConstants.END_ELEMENT:
		    writer.writeEndElement();
		    break;
		case XMLStreamConstants.CHARACTERS:
		    writer.writeCharacters(reader.getText());
		    break;
		}
	    }

	    // Close everything
	    writer.writeEndDocument();
	    writer.flush();
	    writer.close();
	    reader.close();

	    templateFile.delete();
	    finalized = true;
	}
	return dataFile;
    }
    private final static QName _Value_QNAME = new QName("http://www.cuahsi.org/waterML/1.1/", "value");

    public void addValue(ValueSingleVariable value) throws JAXBException {
	JAXBElement<ValueSingleVariable> jaxb = new JAXBElement<ValueSingleVariable>(_Value_QNAME, ValueSingleVariable.class, null, value);
	jaxbMarshaller.marshal(jaxb, writer);

    }

}
