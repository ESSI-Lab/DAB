package eu.essi_lab.wml._2;

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

import java.io.File;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.w3c.dom.Node;

import eu.essi_lab.jaxb.wml._2_0.CollectionType;
import eu.essi_lab.jaxb.wml._2_0.MeasurementTimeseriesType;
import eu.essi_lab.jaxb.wml._2_0.ObjectFactory;
import eu.essi_lab.jaxb.wml._2_0.om__2.OMObservationType;

public class JAXBWML2 {

    public Marshaller getMarshaller() throws JAXBException {
	    Marshaller marshaller = context.createMarshaller();
	    marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new JAXBWML2PrefixMapper());
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    return marshaller;
    }

    public Unmarshaller getUnmarshaller() throws JAXBException {
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

    private ObjectFactory factory;
    private eu.essi_lab.jaxb.wml._2_0.om__2.ObjectFactory omFactory;
    private JAXBContext context;

    public ObjectFactory getFactory() {
	return factory;
    }

    private static JAXBWML2 instance = null;

    public static JAXBWML2 getInstance() {
	if (instance == null) {
	    instance = new JAXBWML2();
	}
	return instance;
    }

    public void release() {
	instance = null;
	factory = null;
    }

    private JAXBWML2() {
	try {
	    this.factory = new ObjectFactory();
	    this.omFactory = new eu.essi_lab.jaxb.wml._2_0.om__2.ObjectFactory();
	    this.context = JAXBContext.newInstance(ObjectFactory.class, eu.essi_lab.jaxb.wml._2_0.om__2.ObjectFactory.class,
		    eu.essi_lab.jaxb.wml._2_0.sf._2_0.ObjectFactory.class, MeasurementTimeseriesType.class);
	
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

    public CollectionType unmarshalCollection(File file) throws Exception {
	Object obj = getUnmarshaller().unmarshal(file);
	return castCollection(obj);

    }

    public CollectionType unmarshalCollection(InputStream stream) throws Exception {
	Object obj = getUnmarshaller().unmarshal(stream);
	return castCollection(obj);

    }

    private CollectionType castCollection(Object obj) {
	if (obj instanceof JAXBElement<?>) {
	    obj = ((JAXBElement<?>) obj).getValue();
	}
	if (obj instanceof CollectionType) {
	    CollectionType ret = (CollectionType) obj;
	    return ret;
	}
	return null;
    }

    public OMObservationType unmarshalObservation(InputStream stream) throws Exception {
	Object obj = getUnmarshaller().unmarshal(stream);

	if (obj instanceof JAXBElement<?>) {
	    obj = ((JAXBElement<?>) obj).getValue();
	}
	if (obj instanceof OMObservationType) {
	    OMObservationType ret = (OMObservationType) obj;
	    return ret;
	}
	return null;
    }

    public MeasurementTimeseriesType unmarshalMeasurementTimeseriesType(InputStream stream) throws Exception {
	Object obj = getUnmarshaller().unmarshal(stream);

	if (obj instanceof JAXBElement<?>) {
	    obj = ((JAXBElement<?>) obj).getValue();
	}
	if (obj instanceof MeasurementTimeseriesType) {
	    MeasurementTimeseriesType ret = (MeasurementTimeseriesType) obj;
	    return ret;
	}
	return null;
    }

    public MeasurementTimeseriesType unmarshalMeasurementTimeseriesType(Node element) throws Exception {
	Object obj = getUnmarshaller().unmarshal(element);

	if (obj instanceof JAXBElement<?>) {
	    obj = ((JAXBElement<?>) obj).getValue();
	}
	if (obj instanceof MeasurementTimeseriesType) {
	    MeasurementTimeseriesType ret = (MeasurementTimeseriesType) obj;
	    return ret;
	}
	return null;
    }

    private JAXBElement<?> getJAXBElement(Object jaxbElement) {
	if (jaxbElement instanceof JAXBElement<?>) {
	    return (JAXBElement<?>) jaxbElement;

	}
	if (jaxbElement instanceof CollectionType) {
	    CollectionType collectionType = (CollectionType) jaxbElement;
	    return factory.createCollection(collectionType);
	}
	if (jaxbElement instanceof OMObservationType) {
	    OMObservationType observation = (OMObservationType) jaxbElement;
	    return omFactory.createOMObservation(observation);
	}
	return null;
    }

    public void marshal(Object jaxbElement, javax.xml.transform.Result result) throws JAXBException {
	JAXBElement<?> element = getJAXBElement(jaxbElement);
	getMarshaller().marshal(element, result);
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

    // public MeasurementTimeseriesType getResultMeasurementTimeseriesType(Result anyResult) {
    // // TODO Auto-generated method stub
    // return null;
    // }

    // public Object createResult(MeasurementTimeseriesType measurement) {
    // Result result = new Result();
    // result.setMeasurement(measurement);
    // return result;
    // }

}
