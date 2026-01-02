package eu.essi_lab.jaxb.sos.factory;

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

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import eu.essi_lab.jaxb.sos._2_0.ObjectFactory;
import eu.essi_lab.lib.xml.NameSpace;

public class JAXBSOS {

    private ObjectFactory factory;
    private eu.essi_lab.jaxb.sos._2_0.swes_2.ObjectFactory swesFactory;
    private JAXBContext context;

    public ObjectFactory getFactory() {
	return factory;
    }

    public eu.essi_lab.jaxb.sos._2_0.swes_2.ObjectFactory getSWESFactory() {
	return swesFactory;
    }

    private static JAXBSOS instance = null;

    public static JAXBSOS getInstance() {
	if (instance == null) {
	    instance = new JAXBSOS();
	}
	return instance;
    }

    public void release() {

	instance = null;
	factory = null;
	swesFactory = null;
    }

    private JAXBSOS() {
	try {
	    this.factory = new ObjectFactory();
	    this.swesFactory = new eu.essi_lab.jaxb.sos._2_0.swes_2.ObjectFactory();
	    this.context = JAXBContext.newInstance(//
		    ObjectFactory.class, //
		    eu.essi_lab.jaxb.sos._2_0.swe._2.ObjectFactory.class, //
		    eu.essi_lab.jaxb.sos._2_0.swes_2.ObjectFactory.class, //
		    eu.essi_lab.jaxb.sos._2_0.fes._2.ObjectFactory.class, //
		    eu.essi_lab.jaxb.sos._2_0.sams._2_0.ObjectFactory.class, //
		    eu.essi_lab.jaxb.sos._2_0.sf._2_0.ObjectFactory.class, //
		    eu.essi_lab.jaxb.sos._2_0.gda.ObjectFactory.class, //
		    eu.essi_lab.jaxb.sos._2_0.om__2.ObjectFactory.class, //
		    eu.essi_lab.jaxb.sos._2_0.wml.ObjectFactory.class
	    // net.opengis.waterml.v_2_0.ObjectFactory.class //
	    // net.opengis.sampling.v_2_0.ObjectFactory.class, //
	    // net.opengis.samplingspatial.v_2_0.ObjectFactory.class//
	    );

	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    private JAXBElement<?> getJAXBElement(Object jaxbElement) {
	if (jaxbElement instanceof JAXBElement<?>) {
	    return (JAXBElement<?>) jaxbElement;

	}
	return null;
    }

    public void marshal(Object jaxbElement, javax.xml.transform.Result result) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, result);
    }

    private Marshaller getMarshaller() throws JAXBException {
	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new JAXBSOSPrefixMapper());
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	return marshaller;
    }

    public void marshal(Object jaxbElement, java.io.OutputStream os) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, os);
    }

    public void marshal(Object jaxbElement, File output) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, output);
    }

    public void marshal(Object jaxbElement, java.io.Writer writer) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, writer);
    }

    public void marshal(Object jaxbElement, org.xml.sax.ContentHandler handler) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, handler);
    }

    public void marshal(Object jaxbElement, org.w3c.dom.Node node) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, node);
    }

    public void marshal(Object jaxbElement, javax.xml.stream.XMLStreamWriter writer) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, writer);
    }

    public void marshal(Object jaxbElement, javax.xml.stream.XMLEventWriter writer) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, writer);
    }

    public Object unmarshal(String xml) throws JAXBException {
	StringReader reader = new StringReader(xml);
	Object ret = getUnmarshaller().unmarshal(reader);
	return ret;
    }

    public Object unmarshal(Reader reader) throws JAXBException {
	Object ret = getUnmarshaller().unmarshal(reader);
	return ret;
    }

    private Unmarshaller getUnmarshaller() throws JAXBException {

	Unmarshaller unmarshaller = context.createUnmarshaller();
	unmarshaller.setEventHandler(new ValidationEventHandler() {
	    @Override
	    public boolean handleEvent(ValidationEvent event) {
		System.out.println(event.getMessage());
		return true;
	    }
	});
	return unmarshaller;
    }

    public Object unmarshal(File file) throws JAXBException {
	Object ret = getUnmarshaller().unmarshal(file);
	return ret;
    }

    public Object unmarshal(InputStream stream) throws JAXBException {
	Object ret = getUnmarshaller().unmarshal(stream);
	return ret;

    }

}
