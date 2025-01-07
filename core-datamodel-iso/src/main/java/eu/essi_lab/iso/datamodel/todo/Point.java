package eu.essi_lab.iso.datamodel.todo;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.InputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.gml.v_3_2_0.PointType;

public class Point extends ISOMetadata<PointType> {
    public Point(InputStream stream) throws JAXBException {

	super(stream);
    }

    public Point() {

	this(new PointType());
    }

    public Point(PointType type) {

	super(type);
    }

    @Override
    public JAXBElement<PointType> getElement() {

	return ObjectFactories.GML().createPoint(type);

    }
    // GML methods

    /**
     * @XPathDirective(target = "gml:pos")
     */
    public String getGMLPos() {
	return null;
    }

    /**
     * @XPathDirective(target = "@gml:id")
     */
    public String getGMLID() {
	return null;
    }

    /**
     * @XPathDirective(target = "@srsName")
     */
    public String getSRSName() {
	return null;
    }

    /**
     * @XPathDirective(target = "gml:pos")
     */
    public void setGMLPos(String value) {
	//TODO
    }

    /**
     * @XPathDirective(target = "@gml:id")
     */
    public void setGMLID(String value) {
	//TODO
    }

    /**
     * @XPathDirective(target = "@srsName")
     */
    public void setSRSName(String value) {
        //TODO
    }

}
