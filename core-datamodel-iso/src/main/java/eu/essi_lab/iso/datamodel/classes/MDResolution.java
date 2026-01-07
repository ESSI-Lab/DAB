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
import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.gml.v_3_2_0.LengthType;
import net.opengis.iso19139.gco.v_20060504.DistancePropertyType;
import net.opengis.iso19139.gco.v_20060504.IntegerPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDRepresentativeFractionPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDRepresentativeFractionType;
import net.opengis.iso19139.gmd.v_20060504.MDResolutionType;

public class MDResolution extends ISOMetadata<MDResolutionType> {
    public MDResolution(InputStream stream) throws JAXBException {

	super(stream);
    }

    public MDResolution() {

	this(new MDResolutionType());
    }

    public MDResolution(MDResolutionType type) {

	super(type);
    }

    public JAXBElement<MDResolutionType> getElement() {

	JAXBElement<MDResolutionType> element = ObjectFactories.GMD().createMDResolution(type);
	return element;
    }

    public void clearDistance() {
	getElement().getValue().setDistance(null);
    }

    public void setDistance(String uom, Double value) {

	DistancePropertyType distanceProperty = new DistancePropertyType();
	LengthType lengthType = new LengthType();
	lengthType.setUom(uom);
	lengthType.setValue(value);
	distanceProperty.setDistance(lengthType);
	getElement().getValue().setDistance(distanceProperty);
    }

    public String getDistanceUOM() {
	try {
	    return getElement().getValue().getDistance().getDistance().getUom();
	} catch (Exception e) {
	    return null;
	}
    }

    public Double getDistanceValue() {
	try {
	    return getElement().getValue().getDistance().getDistance().getValue();
	} catch (Exception e) {
	    return null;
	}
    }

    public void clearEquivalentScale() {
	getElement().getValue().setEquivalentScale(null);
    }

    public void setEquivalentScale(BigInteger value) {
	MDRepresentativeFractionPropertyType property = new MDRepresentativeFractionPropertyType();
	MDRepresentativeFractionType fraction = new MDRepresentativeFractionType();
	IntegerPropertyType ipt = new IntegerPropertyType();
	ipt.setInteger(value);
	fraction.setDenominator(ipt);
	property.setMDRepresentativeFraction(fraction);
	getElement().getValue().setEquivalentScale(property);
    }

    public BigInteger getEquivalentScale() {
	try {
	    return getElement().getValue().getEquivalentScale().getMDRepresentativeFraction().getDenominator().getInteger();
	} catch (Exception e) {
	    return null;
	}
    }

}
