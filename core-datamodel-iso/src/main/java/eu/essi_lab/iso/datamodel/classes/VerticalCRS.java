package eu.essi_lab.iso.datamodel.classes;

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

import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.gml.v_3_2_0.VerticalCRSType;

public class VerticalCRS extends ISOMetadata<VerticalCRSType> {

    public VerticalCRS(InputStream stream) throws JAXBException {

	super(stream);
    }

    public VerticalCRS() {

	this(new VerticalCRSType());
    }

    public VerticalCRS(VerticalCRSType type) {

	super(type);
    }

    public String getId() {
	return type.getId();
    }

    public void setId(String id) {
	type.setId(id);
    }

    public JAXBElement<VerticalCRSType> getElement() {

	JAXBElement<VerticalCRSType> element = ObjectFactories.GML().createVerticalCRS(type);
	return element;
    }

}
