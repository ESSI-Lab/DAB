package eu.essi_lab.messages.bond.jaxb;

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

import com.sun.xml.bind.marshaller.*;
import eu.essi_lab.jaxb.common.*;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.*;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.messages.bond.ViewBond;

/**
 *
 */
public class ViewFactory {

    public ViewFactory() {
    }

    /**
     * @return
     */
    public static Marshaller createMarshaller() {
	try {
	    JAXBContext jc = createContext();
	    Marshaller m = jc.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    m.setProperty(NameSpace.NAMESPACE_PREFIX_MAPPER_IMPL, new CommonNameSpaceContext());

	    return m;
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(ViewFactory.class).error(e);
	}
	// this shouldn't happen as JAXBFactory test is used to check it will not happen
	return null;
    }

    /**
     * @return
     */
    public static Unmarshaller createUnmarshaller() {
	try {
	    JAXBContext jc = createContext();
	    return jc.createUnmarshaller();
	} catch (JAXBException e) {
	    GSLoggerFactory.getLogger(ViewFactory.class).error(e);
	}
	// this shouldn't happen as JAXBFactory test is used to check it will not happen
	return null;
    }

    /**
     * @return
     * @throws JAXBException
     */
    private static JAXBContext createContext() throws JAXBException {
	return JAXBContext.newInstance(//
		View.class, //
		ViewBond.class, //
		LogicalBond.class, //
		ResourcePropertyBond.class, //
		SimpleValueBond.class, // FS
		SpatialBond.class);
    }

    /**
     * @param id
     * @param label
     * @param bond
     * @return
     */
    public View createView(String id, String label, Bond bond) {

	return createView(id, label, bond, null, null, null, null);
    }

    /**
     * @param id
     * @param label
     * @param creator
     * @param bond
     * @return
     */
    public View createView(String id, String label, String creator, Bond bond) {

	return createView(id, label, bond, creator, null, null, null);
    }

    /**
     * @param id
     * @param label
     * @param creator
     * @param bond
     * @param viewVisibility
     * @return
     */
    public View createView(String id, String label, String creator, Bond bond, ViewVisibility viewVisibility) {

	return createView(id, label, bond, creator, null, viewVisibility, null);
    }

    /**
     * @param id
     * @param label
     * @param creator
     * @param owner
     * @param bond
     * @param viewVisibility
     * @return
     */
    public View createView(String id, String label, String creator, String owner, Bond bond, ViewVisibility viewVisibility) {

	return createView(id, label, bond, creator, owner, viewVisibility, null);
    }

    /**
     * @param id
     * @param label
     * @param bond
     * @param creator
     * @param owner
     * @param viewVisibility
     * @return
     */
    public View createView(//
	    String id, //
	    String label, //
	    Bond bond, //
	    String creator, //
	    String owner, //
	    ViewVisibility viewVisibility, //
	    String sourceDeployment) {

	View ret = new View();
	ret.setId(id);
	ret.setLabel(label);
	ret.setBond(bond);
	ret.setCreator(creator);
	ret.setOwner(owner);
	if (viewVisibility != null) {
	    ret.setVisibility(viewVisibility);
	}
	if (sourceDeployment != null) {
	    ret.setSourceDeployment(sourceDeployment);
	}
	return ret;
    }

}
