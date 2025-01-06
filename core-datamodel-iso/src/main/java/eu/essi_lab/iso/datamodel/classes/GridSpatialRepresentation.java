package eu.essi_lab.iso.datamodel.classes;

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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gco.v_20060504.IntegerPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDCellGeometryCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDDimensionPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDGridSpatialRepresentationType;

public class GridSpatialRepresentation extends ISOMetadata<MDGridSpatialRepresentationType> {
    public GridSpatialRepresentation(InputStream stream) throws JAXBException {

	super(stream);
    }

    public GridSpatialRepresentation() {

	this(new MDGridSpatialRepresentationType());
    }

    public GridSpatialRepresentation(MDGridSpatialRepresentationType type) {

	super(type);
    }

    public JAXBElement<MDGridSpatialRepresentationType> getElement() {

	JAXBElement<MDGridSpatialRepresentationType> element = ObjectFactories.GMD().createMDGridSpatialRepresentation(type);
	return element;
    }

    /**
     * @XPathDirective(target = "gmd:numberOfDimensions/gco:Integer")
     */
    public Integer getNumberOfDimensions() {
	try {
	    return type.getNumberOfDimensions().getInteger().intValue();
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @XPathDirective(target = "gmd:numberOfDimensions/gco:Integer")
     */
    public void setNumberOfDimensions(Integer value) {
	if (value == null) {
	    type.setNumberOfDimensions(null);
	    return;
	}
	IntegerPropertyType ipt = new IntegerPropertyType();
	ipt.setInteger(new BigInteger("" + value));
	type.setNumberOfDimensions(ipt);
    }

    /**
     * @XPathDirective(target = "//gmd:axisDimensionProperties/gmd:MD_Dimension")
     */
    public Iterator<Dimension> getAxisDimensions() {
	ArrayList<Dimension> ret = new ArrayList<Dimension>();
	List<MDDimensionPropertyType> properties = type.getAxisDimensionProperties();
	for (MDDimensionPropertyType property : properties) {
	    ret.add(new Dimension(property.getMDDimension()));
	}
	return ret.iterator();
    }

    public Dimension getAxisDimension() {
	try {
	    return getAxisDimensions().next();
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @XPathDirective(target = ".", parent = "gmd:axisDimensionProperties", after = "gmd:numberOfDimensions", position
     *                        = Position.FIRST)
     */
    public void addAxisDimension(Dimension value) {
	MDDimensionPropertyType property = new MDDimensionPropertyType();
	property.setMDDimension(value.getElementType());
	type.getAxisDimensionProperties().add(property);
    }

    /**
     * @XPathDirective(clear = "//gmd:axisDimensionProperties")
     */
    public void clearAxisDimensions() {
	type.getAxisDimensionProperties().clear();
    }

    /**
     * @XPathDirective(target = "gmd:cellGeometry/gmd:MD_CellGeometryCode")
     */
    public String getCellGeometryCode() {
	try {
	    return type.getCellGeometry().getMDCellGeometryCode().getCodeListValue();
	} catch (Exception e) {
	    return null;
	}
    }

    public void setCellGeometryCode(String value) {
	CodeListValueType list = createCodeListValueType(MD_CELL_GEOMETRY_CODE_CODELIST, value, ISO_19115_CODESPACE, value);
	MDCellGeometryCodePropertyType geometry = new MDCellGeometryCodePropertyType();
	geometry.setMDCellGeometryCode(list);
	type.setCellGeometry(geometry);
    }

}
