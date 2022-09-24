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
import java.util.Iterator;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.todo.MDBand;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.RecordTypePropertyType;
import net.opengis.iso19139.gco.v_20060504.RecordTypeType;
import net.opengis.iso19139.gmd.v_20060504.MDCoverageContentTypeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDCoverageDescriptionType;

public class CoverageDescription extends ISOMetadata<MDCoverageDescriptionType> {
    public CoverageDescription(InputStream stream) throws JAXBException {

	super(stream);
    }

    public CoverageDescription() {

	this(createDefaultCoverageDescription());
    }

    private static MDCoverageDescriptionType createDefaultCoverageDescription() {
	MDCoverageDescriptionType ret = new MDCoverageDescriptionType();
	MDCoverageContentTypeCodePropertyType contentType = new MDCoverageContentTypeCodePropertyType();
	contentType.setMDCoverageContentTypeCode(createCodeListValueType(MD_COVERAGE_CONTENT_TYPE_CODE_CODELIST, "physicalMeasurement",
		ISO_19115_CODESPACE, "physicalMeasurement"));
	ret.setContentType(contentType);
	return ret;
    }

    public CoverageDescription(MDCoverageDescriptionType type) {

	super(type);
    }

    public JAXBElement<MDCoverageDescriptionType> getElement() {

	JAXBElement<MDCoverageDescriptionType> element = ObjectFactories.GMD().createMDCoverageDescription(type);
	return element;
    }

    /**
     * @XPathDirective(target = "gmd:attributeDescription/gco:RecordType")
     */
    public String getAttributeDescription() {

	RecordTypePropertyType attributeDescription = type.getAttributeDescription();
	if (attributeDescription != null) {

	    RecordTypeType recordType = attributeDescription.getRecordType();
	    if (recordType != null) {

		return recordType.getValue();
	    }

	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:attributeDescription/gco:RecordType")
     */
    public void setAttributeDescription(String description) {

	RecordTypePropertyType attributeDescription = type.getAttributeDescription();
	if (!type.isSetAttributeDescription()) {

	    attributeDescription = new RecordTypePropertyType();
	    type.setAttributeDescription(attributeDescription);

	}

	RecordTypeType recordType = attributeDescription.getRecordType();
	if (!attributeDescription.isSetRecordType()) {
	    recordType = new RecordTypeType();
	    attributeDescription.setRecordType(recordType);
	}
	recordType.setValue(description);
    }

    public String getAttributeIdentifier() {

	RecordTypePropertyType attributeDescription = type.getAttributeDescription();
	if (attributeDescription != null) {

	    RecordTypeType recordType = attributeDescription.getRecordType();
	    if (recordType != null) {

		return recordType.getHref();
	    }

	}

	return null;
    }
    
    public String getAttributeTitle() {

 	RecordTypePropertyType attributeDescription = type.getAttributeDescription();
 	if (attributeDescription != null) {

 	    RecordTypeType recordType = attributeDescription.getRecordType();
 	    if (recordType != null) {

 		return recordType.getTitle();
 	    }

 	}

 	return null;
     }

    public void setAttributeIdentifier(String identifier) {

	RecordTypePropertyType attributeDescription = type.getAttributeDescription();
	if (!type.isSetAttributeDescription()) {

	    attributeDescription = new RecordTypePropertyType();
	    type.setAttributeDescription(attributeDescription);

	}

	RecordTypeType recordType = attributeDescription.getRecordType();
	if (!attributeDescription.isSetRecordType()) {
	    recordType = new RecordTypeType();
	    attributeDescription.setRecordType(recordType);
	}
	recordType.setHref(identifier);
    }

    public void setAttributeTitle(String title) {
	RecordTypePropertyType attributeDescription = type.getAttributeDescription();
	if (!type.isSetAttributeDescription()) {

	    attributeDescription = new RecordTypePropertyType();
	    type.setAttributeDescription(attributeDescription);

	}

	RecordTypeType recordType = attributeDescription.getRecordType();
	if (!attributeDescription.isSetRecordType()) {
	    recordType = new RecordTypeType();
	    attributeDescription.setRecordType(recordType);
	}
	recordType.setTitle(title);

    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_RangeDimension/gmd:descriptor/gco:CharacterString")
     */
    void setRangeDimension(String descriptor) {
    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_RangeDimension/gmd:descriptor/gco:CharacterString")
     */
    String getRangeDimension() {
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:maxValue/gco:Real")
     */
    void setRangeMax(Double value) {
    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:maxValue/gco:Real")
     */
    Double getRangeMax() {
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:minValue/gco:Real")
     */
    void setRangeMin(Double value) {
    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:minValue/gco:Real")
     */
    Double getRangeMin() {
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:bitsPerValue/gco:Integer")
     */
    void setBitsPerValue(Integer value) {
    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:bitsPerValue/gco:Integer")
     */
    Integer getBitsPerValue() {
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:descriptor/gco:CharacterString")
     */
    void setBandDimensionDescription(String descriptor) {
    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_Band/gmd:descriptor/gco:CharacterString")
     */
    String getBandDimensionDescription() {
	return null;
    }

    /**
     * @XPathDirective(target = ".", parent = "gmd:dimension", position = Position.LAST)
     */
    void addBand(MDBand band) {
    }

    /**
     * @XPathDirective(clear = "gmd:dimension")
     */
    void clearBands() {
    }

    /**
     * @XPathDirective(target = "gmd:dimension/gmd:MD_Band | gmd:dimension/gmi:MI_Band")
     */
    Iterator<MDBand> getBands() {
	return null;
    }

}
