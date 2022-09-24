package eu.essi_lab.iso.datamodel.classes;

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

import java.io.InputStream;
import java.math.BigDecimal;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.DecimalPropertyType;
import net.opengis.iso19139.gmd.v_20060504.EXGeographicBoundingBoxType;

/**
 * EX_GeographicBoundingBox
 * 
 * @author Fabrizio
 */
public class GeographicBoundingBox extends ISOMetadata<EXGeographicBoundingBoxType> {

    public GeographicBoundingBox(InputStream stream) throws JAXBException {

	super(stream);
    }

    public GeographicBoundingBox() {

	this(new EXGeographicBoundingBoxType());
    }

    public GeographicBoundingBox(EXGeographicBoundingBoxType type) {

	super(type);
    }

    /**
     * @XPathDirective(target = "@id")
     * @param id
     */
    public void setId(String id) {

	type.setId(id);
    }

    /**
     * @XPathDirective(target = "@id")
     * @return
     */
    public String getId() {

	return type.getId();
    }

    /**
     * @XPathDirective(target = "gmd:westBoundLongitude/gco:Decimal")
     * @param value
     */
    public void setWest(Double value) {

	if (value == null) {
	    type.setWestBoundLongitude(null);
	    return;
	}

	DecimalPropertyType decimalPropertyType = new DecimalPropertyType();
	decimalPropertyType.setDecimal(new BigDecimal(String.valueOf(value)));

	type.setWestBoundLongitude(decimalPropertyType);
    }

    /**
     * @XPathDirective(target = "gmd:westBoundLongitude/gco:Decimal")
     * @return
     */
    public Double getWest() {

	try {
	    return Double.valueOf(type.getWestBoundLongitude().getDecimal().toString());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:eastBoundLongitude/gco:Decimal")
     * @param value
     */
    public void setEast(Double value) {

	if (value == null) {
	    type.setEastBoundLongitude(null);
	    return;
	}

	DecimalPropertyType decimalPropertyType = new DecimalPropertyType();
	decimalPropertyType.setDecimal(new BigDecimal(String.valueOf(value)));

	type.setEastBoundLongitude(decimalPropertyType);
    }

    /**
     * @XPathDirective(target = "gmd:eastBoundLongitude/gco:Decimal")
     * @return
     */
    public Double getEast() {

	try {
	    return Double.valueOf(type.getEastBoundLongitude().getDecimal().toString());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:southBoundLatitude/gco:Decimal")
     * @param value
     */
    public void setSouth(Double value) {

	if (value == null) {
	    type.setSouthBoundLatitude(null);
	    return;
	}

	DecimalPropertyType decimalPropertyType = new DecimalPropertyType();
	decimalPropertyType.setDecimal(new BigDecimal(String.valueOf(value)));

	type.setSouthBoundLatitude(decimalPropertyType);
    }

    /**
     * @XPathDirective(target = "gmd:southBoundLatitude/gco:Decimal")
     * @return
     */
    public Double getSouth() {

	try {
	    return Double.valueOf(type.getSouthBoundLatitude().getDecimal().toString());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:northBoundLatitude/gco:Decimal")
     * @param value
     */
    public void setNorth(Double value) {

	if (value == null) {
	    type.setNorthBoundLatitude(null);
	    return;
	}

	DecimalPropertyType decimalPropertyType = new DecimalPropertyType();
	decimalPropertyType.setDecimal(new BigDecimal(String.valueOf(value)));

	type.setNorthBoundLatitude(decimalPropertyType);
    }

    /**
     * @XPathDirective(target = "gmd:northBoundLatitude/gco:Decimal")
     * @return
     */
    public Double getNorth() {

	try {
	    return Double.valueOf(type.getNorthBoundLatitude().getDecimal().toString());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    public JAXBElement<EXGeographicBoundingBoxType> getElement() {

	JAXBElement<EXGeographicBoundingBoxType> element = ObjectFactories.GMD().createEXGeographicBoundingBox(type);
	return element;
    }
}
