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
import net.opengis.iso19139.gco.v_20060504.RealPropertyType;
import net.opengis.iso19139.gmd.v_20060504.EXVerticalExtentType;
import net.opengis.iso19139.gsr.v_20060504.SCCRSPropertyType;

public class VerticalExtent extends ISOMetadata<EXVerticalExtentType> {

    public VerticalExtent(InputStream stream) throws JAXBException {

	super(stream);
    }

    public VerticalExtent() {

	this(new EXVerticalExtentType());
    }

    public VerticalExtent(EXVerticalExtentType type) {

	super(type);
    }

    public JAXBElement<EXVerticalExtentType> getElement() {

	JAXBElement<EXVerticalExtentType> element = ObjectFactories.GMD().createEXVerticalExtent(type);
	return element;
    }

    /**
     * @XPathDirective(target = "gmd:minimumValue/gco:Real")
     */
    public void setMinimumValue(Double min) {
	if (min == null) {
	    type.setMinimumValue(null);
	    return;
	}
	RealPropertyType value = new RealPropertyType();
	value.setReal(min);
	type.setMinimumValue(value);
    }

    /**
     * @XPathDirective(target = "gmd:maximumValue/gco:Real")
     */
    public void setMaximumValue(Double max) {
	if (max == null) {
	    type.setMaximumValue(null);
	    return;
	}
	RealPropertyType value = new RealPropertyType();
	value.setReal(max);
	type.setMaximumValue(value);
    }

    /**
     * @XPathDirective(target = "gmd:minimumValue/gco:Real")
     */
    public Double getMinimumValue() {
	RealPropertyType container = type.getMinimumValue();
	if (container == null) {
	    return null;
	}
	return container.getReal();
    }

    /**
     * @XPathDirective(target = "gmd:maximumValue/gco:Real")
     */
    public Double getMaximumValue() {
	RealPropertyType container = type.getMaximumValue();
	if (container == null) {
	    return null;
	}
	return container.getReal();
    }

    void setUnits(String units) {
	// TODO implement
    }

    public VerticalCRS getVerticalCRS() {
	SCCRSPropertyType property = type.getVerticalCRS();
	Object abstractCRS = property.getAbstractCRS().getValue();
	if (abstractCRS instanceof VerticalCRSType) {
	    VerticalCRSType verticalCRS = (VerticalCRSType) abstractCRS;
	    return new VerticalCRS(verticalCRS);
	}
	return null;
    }

    public void setVerticalCRS(VerticalCRS verticalCRS) {
	if (verticalCRS == null) {
	    type.setVerticalCRS(null);
	} else {
	    SCCRSPropertyType property = new SCCRSPropertyType();
	    JAXBElement<VerticalCRSType> verticalCRSType = ObjectFactories.GML().createVerticalCRS(verticalCRS.getElementType());
	    property.setAbstractCRS(verticalCRSType);
	    type.setVerticalCRS(property);
	}
    }

}
