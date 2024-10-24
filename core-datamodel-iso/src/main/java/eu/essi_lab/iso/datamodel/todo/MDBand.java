package eu.essi_lab.iso.datamodel.todo;

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

import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gmd.v_20060504.MDBandType;

public class MDBand extends ISOMetadata<MDBandType> {
    public MDBand(InputStream stream) throws JAXBException {

	super(stream);
    }

    public MDBand() {

	this(new MDBandType());
    }

    public MDBand(MDBandType type) {

	super(type);
    }

    public JAXBElement<MDBandType> getElement() {

	JAXBElement<MDBandType> element = ObjectFactories.GMD().createMDBand(type);
	return element;
    }
    /**
    *    @XPathDirective(target = "./@id")
    */
    public void setIdentifier(String identifier) {
    }

    /**
    *    @XPathDirective(target = "./@id")
    */
    public String getIdentifier() {
	return null;
    }
}
