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
import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.gml.v_3_2_0.MeasureType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gco.v_20060504.IntegerPropertyType;
import net.opengis.iso19139.gco.v_20060504.MeasurePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDDimensionNameTypeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDDimensionType;

public class Dimension extends ISOMetadata<MDDimensionType> {
    public Dimension(InputStream stream) throws JAXBException {

	super(stream);
    }

    public Dimension() {

	this(new MDDimensionType());
    }

    public Dimension(MDDimensionType type) {

	super(type);
    }

    public JAXBElement<MDDimensionType> getElement() {

	JAXBElement<MDDimensionType> element = ObjectFactories.GMD().createMDDimension(type);
	return element;
    }

    /**
     * @XPathDirective(target = ".", parent = "gmd:dimensionName", position = Position.FIRST)
     */
    public void setDimensionNameTypeCode(String dimensionNameTypeCode) {
	MDDimensionNameTypeCodePropertyType property = new MDDimensionNameTypeCodePropertyType();
	CodeListValueType list = createCodeListValueType(MD_DIMENSION_NAME_TYPE_CODE_CODELIST, dimensionNameTypeCode, ISO_19115_CODESPACE,
		dimensionNameTypeCode);
	property.setMDDimensionNameTypeCode(list);
	type.setDimensionName(property);
    }

    /**
     * @XPathDirective(target = "gmd:dimensionName/gmd:MD_DimensionNameTypeCode")
     */
    public String getDimensionNameTypeCode() {
	try {
	    return type.getDimensionName().getMDDimensionNameTypeCode().getCodeListValue();
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @XPathDirective(target = "gmd:dimensionSize/gco:Integer")
     */
    public void setDimensionSize(BigInteger value) {
	if (value == null) {
	    type.setDimensionSize(null);
	    return;
	}
	IntegerPropertyType ipt = new IntegerPropertyType();
	ipt.setInteger(value);
	type.setDimensionSize(ipt);
    }

    /**
     * @XPathDirective(target = "gmd:dimensionSize/gco:Integer")
     */
    public BigInteger getDimensionSize() {
	try {
	    return type.getDimensionSize().getInteger();
	} catch (Exception e) {
	    return null;
	}
    }

    public void clearResolution() {
	type.setResolution(null);
    }

    /**
     * @XPathDirective(target = "gmd:resolution/gco:Measure")
     */
    public void setResolution(String uom, double value) {
	MeasurePropertyType property = new MeasurePropertyType();
	MeasureType measure = new MeasureType();
	if (uom != null && !uom.equals("")) {
	    measure.setUom(uom);
	}else{
	    measure.setUom("unknown");
	}
	measure.setValue(value);
	JAXBElement<?> jaxb = ObjectFactories.GCO().createMeasure(measure);
	property.setMeasure(jaxb);
	type.setResolution(property);
    }

    /**
     * @XPathDirective(target = "gmd:resolution/gco:Measure")
     */
    public void setResolution(double value) {

	setResolution(null, value);
    }

    /**
     * @XPathDirective(target = "gmd:resolution/gco:Measure/@uom")
     */
    public String getResolutionUOM() {
	try {
	    Object obj = type.getResolution().getMeasure().getValue();
	    if (obj instanceof MeasureType) {
		MeasureType measure = (MeasureType) obj;
		return measure.getUom();
	    }
	} catch (Exception e) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:resolution/gco:Measure")
     */
    public Double getResolutionValue() {
	try {
	    Object obj = type.getResolution().getMeasure().getValue();
	    if (obj instanceof MeasureType) {
		MeasureType measure = (MeasureType) obj;
		return measure.getValue();
	    }
	} catch (Exception e) {
	}
	return null;
    }

}
