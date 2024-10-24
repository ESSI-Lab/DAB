package eu.essi_lab.profiler.wfs;

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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import eu.essi_lab.lib.xml.NameSpace;
import net.opengis.wfs.v_1_1_0.ObjectFactory;

public class JAXBWFS {

    public Marshaller getMarshaller() throws JAXBException {
	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new JAXBWFSPrefixMapper());
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	return marshaller;
    }

    public Unmarshaller getUnmarshaller() throws JAXBException {
	Unmarshaller unmarshaller = context.createUnmarshaller();
	unmarshaller.setEventHandler(new ValidationEventHandler() {
	    @Override
	    public boolean handleEvent(ValidationEvent event) {
		return true;
	    }
	});
	return unmarshaller;
    }

    private ObjectFactory factory;
    private JAXBContext context;

    public ObjectFactory getFactory() {
	return factory;
    }

    private static JAXBWFS instance = null;

    public static JAXBWFS getInstance() {
	if (instance == null) {
	    instance = new JAXBWFS();
	}
	return instance;
    }

    public void release() {
	instance = null;
	factory = null;
    }

    private JAXBWFS() {
	try {
	    this.factory = new ObjectFactory();
	    this.context = JAXBContext.newInstance(ObjectFactory.class);

	} catch (JAXBException e) {
	    e.printStackTrace();
	}
    }

}
