package eu.essi_lab.jaxb.wms.extension;

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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import eu.essi_lab.jaxb.wms._1_3_0.ObjectFactory;
import eu.essi_lab.lib.xml.NameSpace;

public class JAXBWMS {

    public Marshaller getMarshaller() throws JAXBException {
	    Marshaller marshaller = context.createMarshaller();
	    marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new JAXBWMSPrefixMapper());
	    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.opengis.net/wms http://schemas.opengis.net/wms/1.3.0/capabilities_1_3_0.xsd");
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

    private eu.essi_lab.jaxb.wms._1_3_0.ObjectFactory factory;
    private JAXBContext context;

    public ObjectFactory getFactory() {
	return factory;
    }

    private static JAXBWMS instance = null;

    public static JAXBWMS getInstance() {
	if (instance == null) {
	    instance = new JAXBWMS();
	}
	return instance;
    }

    public void release() {
	instance = null;
	factory = null;
    }

    private JAXBWMS() {
	try {
	    this.factory = new ObjectFactory();
	    this.context = JAXBContext.newInstance(ObjectFactory.class, eu.essi_lab.jaxb.wms.exceptions._1_3_0.ObjectFactory.class);
	
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

  
}
