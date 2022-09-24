package eu.essi_lab.messages.bond.jaxb;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.ViewBond;

public class ViewFactory {
    public ViewFactory() {
    }

    public Marshaller createMarshaller() {
	try {
	    JAXBContext jc = createContext();
	    Marshaller m = jc.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    return m;
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
	// this shouldn't happen as JAXBFactory test is used to check it will not happen
	return null;
    }

    public Unmarshaller createUnmarshaller() {
	try {
	    JAXBContext jc = createContext();
	    Unmarshaller u = jc.createUnmarshaller();
	    return u;
	} catch (JAXBException e) {
	    e.printStackTrace();
	}
	// this shouldn't happen as JAXBFactory test is used to check it will not happen
	return null;
    }

    private JAXBContext createContext() throws JAXBException {
	return JAXBContext.newInstance(View.class, ViewBond.class, LogicalBond.class, ResourcePropertyBond.class, SimpleValueBond.class,
		SpatialBond.class);
    }

    public View createView(String id, String label, Bond bond) {
	View ret = new View();
	ret.setId(id);
	ret.setLabel(label);
	ret.setBond(bond);
	return ret;
    }

}
