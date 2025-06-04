package eu.essi_lab.iso.datamodel.classes;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gmd.v_20060504.AbstractEXGeographicExtentType;
import net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationType;
import net.opengis.iso19139.gmd.v_20060504.CIResponsiblePartyPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIResponsiblePartyType;
import net.opengis.iso19139.gmd.v_20060504.CIRoleCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.DSAssociationTypeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.EXBoundingPolygonType;
import net.opengis.iso19139.gmd.v_20060504.EXExtentPropertyType;
import net.opengis.iso19139.gmd.v_20060504.EXExtentType;
import net.opengis.iso19139.gmd.v_20060504.EXGeographicBoundingBoxType;
import net.opengis.iso19139.gmd.v_20060504.EXGeographicDescriptionType;
import net.opengis.iso19139.gmd.v_20060504.EXGeographicExtentPropertyType;
import net.opengis.iso19139.gmd.v_20060504.EXTemporalExtentPropertyType;
import net.opengis.iso19139.gmd.v_20060504.EXTemporalExtentType;
import net.opengis.iso19139.gmd.v_20060504.EXVerticalExtentPropertyType;
import net.opengis.iso19139.gmd.v_20060504.EXVerticalExtentType;
import net.opengis.iso19139.gmd.v_20060504.MDAggregateInformationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDAggregateInformationType;
import net.opengis.iso19139.gmd.v_20060504.MDBrowseGraphicPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDCharacterSetCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDDataIdentificationType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierType;
import net.opengis.iso19139.gmd.v_20060504.MDResolutionPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDSpatialRepresentationTypeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;
import net.opengis.iso19139.gmd.v_20060504.ObjectFactory;

/**
 * MD_IDentification
 *
 * @author Fabrizio
 */
public class DataIdentification extends Identification {

    public DataIdentification(InputStream stream) throws JAXBException {

	super(stream);
    }

    public DataIdentification(MDDataIdentificationType type) {

	super(type);
    }

    public DataIdentification() {

	this(new MDDataIdentificationType());
    }

    @Override
    public MDDataIdentificationType getElementType() {

	return ((MDDataIdentificationType) type);
    }

    // ------------------------------------
    //
    // topic category
    //
    /**
     * @XPathDirective(target = ".//gmd:topicCategory")
     * @return
     */
    public Iterator<MDTopicCategoryCodeType> getTopicCategories() {

	List<MDTopicCategoryCodePropertyType> topicCategory = getElementType().getTopicCategory();
	ArrayList<MDTopicCategoryCodeType> out = new ArrayList<MDTopicCategoryCodeType>();

	for (MDTopicCategoryCodePropertyType mdTopicCategoryCodePropertyType : topicCategory) {

	    MDTopicCategoryCodeType mdTopicCategoryCode = mdTopicCategoryCodePropertyType.getMDTopicCategoryCode();
	    if (mdTopicCategoryCode != null) {

		out.add(mdTopicCategoryCode);
	    }
	}

	return out.iterator();
    }

    /**
     * @XPathDirective(clear = ".//gmd:topicCategory")
     */
    public void clearTopicCategories() {

	getElementType().getTopicCategory().clear();
    }

    public MDTopicCategoryCodeType getTopicCategory() {

	return getTopicCategories().hasNext() ? getTopicCategories().next() : null;
    }

    public String getTopicCategoryString() {

	return getTopicCategory() != null ? getTopicCategory().value() : null;
    }

    public Iterator<String> getTopicCategoriesStrings() {

	Iterator<MDTopicCategoryCodeType> topicCategories = getTopicCategories();
	ArrayList<String> out = new ArrayList<String>();

	while (topicCategories.hasNext()) {
	    MDTopicCategoryCodeType type = topicCategories.next();
	    out.add(type.value());

	}

	return out.iterator();
    }

    /**
     * @XPathDirective(target = ".", after = "gmd:characterSet", position =
     *                        Position.FIRST)
     * @param topic
     */
    public void addTopicCategory(MDTopicCategoryCodeType topic) {

	MDTopicCategoryCodePropertyType type = new MDTopicCategoryCodePropertyType();
	type.setMDTopicCategoryCode(topic);

	getElementType().getTopicCategory().add(type);
    }

    // --------------------------------------------------------
    //
    // resource languages
    //
    /**
     * @XPathDirective(target = ".//gmd:language/gco:CharacterString")
     * @return
     */
    public Iterator<String> getLanguages() {

	List<CharacterStringPropertyType> languageTypes = getElementType().getLanguage();
	ArrayList<String> out = new ArrayList<String>();
	for (CharacterStringPropertyType languageType : languageTypes) {
	    String value = getStringFromCharacterString(languageType);
	    if (value != null) {
		out.add(value);
	    }
	}
	return out.iterator();
    }

    /**
     * @XPathDirective(create = "gmd:language/gco:CharacterString", target = ".",
     *                        after = "gmd:spatialResolution", position =
     *                        Position.FIRST)
     * @param language
     */
    public void addLanguage(String language) {
	if (language != null) {
	    getElementType().getLanguage().add(createCharacterStringPropertyType(language));
	}
    }

    /**
     * @XPathDirective(create = "gmd:language/gco:CharacterString", target = ".",
     *                        after = "gmd:spatialResolution", position =
     *                        Position.FIRST)
     */
    public void clearLanguages() {

	getElementType().unsetLanguage();

    }

    // --------------------------------------------------------
    //
    // Character set
    //

    /**
     * @XPathDirective(target = "gmd:characterSet")
     * @param code
     */
    public void setCharacterSetCode(String code) {

	if (code == null) {
	    getElementType().unsetCharacterSet();
	    return;
	}

	MDCharacterSetCodePropertyType propertyType = new MDCharacterSetCodePropertyType();
	propertyType.setMDCharacterSetCode(createCodeListValueType(MD_CHARACTER_SET_CODE_CODELIST, code, ISO_19115_CODESPACE, code));

	getElementType().getCharacterSet().clear();
	getElementType().getCharacterSet().add(propertyType);
    }

    /**
     * @XPathDirective(target = "gmd:characterSet/gmd:MD_CharacterSetCode")
     * @return
     */
    public String getCharacterSetCode() {

	try {
	    return getElementType().getCharacterSet().get(0).getMDCharacterSetCode().getCodeListValue();
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	}

	return null;
    }

    // -------------------------
    //
    // Temporal extent
    //
    /**
     * @XPathDirective(target =
     *                        "./*:extent/gmd:EX_Extent/gmd:temporalElement//gmd:EX_TemporalExtent")
     * @return
     */
    public Iterator<TemporalExtent> getTemporalExtents() {

	ArrayList<TemporalExtent> out = new ArrayList<TemporalExtent>();
	try {
	    List<EXExtentPropertyType> extent = getElementType().getExtent();
	    for (EXExtentPropertyType exExtentPropertyType : extent) {
		List<EXTemporalExtentPropertyType> temporalElement = exExtentPropertyType.getEXExtent().getTemporalElement();
		for (EXTemporalExtentPropertyType exTemporalExtentPropertyType : temporalElement) {
		    EXTemporalExtentType value = exTemporalExtentPropertyType.getEXTemporalExtent().getValue();
		    TemporalExtent temporalExtent = new TemporalExtent(value);
		    out.add(temporalExtent);
		}
	    }
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	}

	return out.iterator();
    }

    public TemporalExtent getTemporalExtent() {

	Iterator<TemporalExtent> temporalExtents = getTemporalExtents();
	if (temporalExtents.hasNext()) {
	    return temporalExtents.next();
	}

	return null;
    }

    /**
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent", parent =
     *                        "gmd:temporalElement", before =
     *                        "*:extent/gmd:EX_Extent/gmd:verticalElement", position
     *                        = Position.LAST)
     * @param extent
     */
    public void addTemporalExtent(TemporalExtent extent) {

	EXTemporalExtentPropertyType exTemporalExtentPropertyType = new EXTemporalExtentPropertyType();
	exTemporalExtentPropertyType.setEXTemporalExtent(extent.getElement());

	EXExtentType exExtentType = new EXExtentType();
	exExtentType.setTemporalElement(Lists.newArrayList(exTemporalExtentPropertyType));

	EXExtentPropertyType exExtentPropertyType = new EXExtentPropertyType();
	exExtentPropertyType.setEXExtent(exExtentType);

	getElementType().getExtent().add(exExtentPropertyType);
    }

    public void addTemporalExtent(String beginPosition, String endPosition) {

	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition(beginPosition);
	temporalExtent.setEndPosition(endPosition);

	addTemporalExtent(temporalExtent);
    }

    public void addTemporalExtent(String periodID, String beginPosition, String endPosition) {

	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition(beginPosition);
	temporalExtent.setEndPosition(endPosition);
	temporalExtent.setTimePeriodId(periodID);

	addTemporalExtent(temporalExtent);
    }

    // ----------------------------
    //
    // Bounding box
    //
    /**
     * Use bigdecimal method
     * 
     * @param description
     * @param north
     * @param west
     * @param south
     * @param east
     */
    @Deprecated
    public void addGeographicBoundingBox(String description, double north, double west, double south, double east) {
	addGeographicBoundingBox(description, new BigDecimal(north), new BigDecimal(west), new BigDecimal(south), new BigDecimal(east));
    }

    /**
     * @XPathDirective(target =
     *                        "./gmd:extent/gmd:EX_Extent/gmd:description/gco:CharacterString")
     * @param description
     * @param north
     * @param west
     * @param south
     * @param east
     */
    public void addGeographicBoundingBox(String description, BigDecimal north, BigDecimal west, BigDecimal south, BigDecimal east) {

	GeographicBoundingBox bbox = new GeographicBoundingBox();
	bbox.setBigDecimalNorth(north);
	bbox.setBigDecimalSouth(south);
	bbox.setBigDecimalEast(east);
	bbox.setBigDecimalWest(west);

	EXGeographicExtentPropertyType exGeographicExtentPropertyType = new EXGeographicExtentPropertyType();
	exGeographicExtentPropertyType.setAbstractEXGeographicExtent(bbox.getElement());

	EXExtentType exExtentType = new EXExtentType();
	if (description != null) {
	    exExtentType.setDescription(createCharacterStringPropertyType(description));
	}
	exExtentType.setGeographicElement(Lists.newArrayList(exGeographicExtentPropertyType));

	EXExtentPropertyType exExtentPropertyType = new EXExtentPropertyType();
	exExtentPropertyType.setEXExtent(exExtentType);

	getElementType().getExtent().add(exExtentPropertyType);
    }

    public String getGeographicDescription() {
	try {
	    return getStringFromCharacterString(getElementType().getExtent().get(0).getEXExtent().getDescription());
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent", parent =
     *                        "gmd:geographicElement", position = Position.FIRST)
     * @param bbox
     */
    public void addGeographicBoundingBox(GeographicBoundingBox bbox) {

	addGeographicBoundingBox(null, bbox.getBigDecimalNorth(), bbox.getBigDecimalWest(), bbox.getBigDecimalSouth(),
		bbox.getBigDecimalEast());
    }

    /**
     * Use big decimal method
     * 
     * @param north
     * @param west
     * @param south
     * @param east
     */
    @Deprecated
    public void addGeographicBoundingBox(double north, double west, double south, double east) {

	addGeographicBoundingBox(new BigDecimal(north), new BigDecimal(west), new BigDecimal(south), new BigDecimal(east));
    }

    public void addGeographicBoundingBox(BigDecimal north, BigDecimal west, BigDecimal south, BigDecimal east) {

	addGeographicBoundingBox(null, north, west, south, east);
    }

    public void clearGeographicBoundingBoxes() {

	List<EXExtentPropertyType> extent = getElementType().getExtent();
	if (extent != null) {
	    for (EXExtentPropertyType e : extent) {
		List<EXGeographicExtentPropertyType> ge = e.getEXExtent().getGeographicElement();
		if (ge != null && !ge.isEmpty()) {
		    ge.clear();
		}
	    }
	}
    }

    public void clearVerticalExtents() {

	List<EXExtentPropertyType> extent = getElementType().getExtent();
	if (extent != null) {
	    for (EXExtentPropertyType e : extent) {
		List<EXVerticalExtentPropertyType> ve = e.getEXExtent().getVerticalElement();
		if (ve != null && !ve.isEmpty()) {
		    ve.clear();
		}
	    }
	}
    }

    /**
     * @XPathDirective(target =
     *                        "./*:extent/gmd:EX_Extent/gmd:geographicElement//gmd:EX_GeographicBoundingBox")
     * @return
     */
    public Iterator<GeographicBoundingBox> getGeographicBoundingBoxes() {

	ArrayList<GeographicBoundingBox> out = new ArrayList<GeographicBoundingBox>();

	List<EXExtentPropertyType> extent = getElementType().getExtent();
	for (EXExtentPropertyType exExtentPropertyType : extent) {
	    EXExtentType exExtent = exExtentPropertyType.getEXExtent();
	    if (exExtent != null) {
		List<EXGeographicExtentPropertyType> geographicElement = exExtent.getGeographicElement();
		for (EXGeographicExtentPropertyType exGeographicExtentPropertyType : geographicElement) {

		    JAXBElement<? extends AbstractEXGeographicExtentType> abstractEXGeographicExtent = exGeographicExtentPropertyType
			    .getAbstractEXGeographicExtent();
		    if (abstractEXGeographicExtent != null) {
			AbstractEXGeographicExtentType value = abstractEXGeographicExtent.getValue();
			if (value instanceof EXGeographicBoundingBoxType) {
			    EXGeographicBoundingBoxType t = (EXGeographicBoundingBoxType) value;
			    GeographicBoundingBox geographicBoundingBox = new GeographicBoundingBox(t);
			    out.add(geographicBoundingBox);
			}
		    }
		}
	    }
	}

	return out.iterator();
    }

    public GeographicBoundingBox getGeographicBoundingBox() {

	Iterator<GeographicBoundingBox> geographicBoundingBoxes = getGeographicBoundingBoxes();
	if (geographicBoundingBoxes.hasNext()) {
	    return geographicBoundingBoxes.next();
	}

	return null;
    }

    public Double[] getWS() {

	GeographicBoundingBox geographicBoundingBox = getGeographicBoundingBox();
	if (geographicBoundingBox != null) {

	    Double south = geographicBoundingBox.getSouth();
	    Double west = geographicBoundingBox.getWest();

	    return new Double[] { west, south };
	}

	return null;
    }

    public Double[] getEN() {

	GeographicBoundingBox geographicBoundingBox = getGeographicBoundingBox();
	if (geographicBoundingBox != null) {

	    Double north = geographicBoundingBox.getNorth();
	    Double east = geographicBoundingBox.getEast();

	    return new Double[] { east, north };
	}

	return null;
    }

    /**
     * @XPathDirective(target =
     *                        ".//*:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/
     */
    public Iterator<String> getGeographicDescriptionCodes() {

	ArrayList<String> arrayList = new ArrayList<>();

	try {
	    List<EXExtentPropertyType> extent = getElementType().getExtent();
	    for (EXExtentPropertyType exExtentPropertyType : extent) {
		EXExtentType exExtent = exExtentPropertyType.getEXExtent();
		List<EXGeographicExtentPropertyType> geographicElement = exExtent.getGeographicElement();
		for (EXGeographicExtentPropertyType exGeographicExtentPropertyType : geographicElement) {
		    JAXBElement<? extends AbstractEXGeographicExtentType> abstractEXGeographicExtent = exGeographicExtentPropertyType
			    .getAbstractEXGeographicExtent();
		    AbstractEXGeographicExtentType value = abstractEXGeographicExtent.getValue();
		    if (value instanceof EXGeographicDescriptionType) {
			EXGeographicDescriptionType type = (EXGeographicDescriptionType) value;
			MDIdentifierPropertyType geographicIdentifier = type.getGeographicIdentifier();
			JAXBElement<? extends MDIdentifierType> mdIdentifier = geographicIdentifier.getMDIdentifier();
			MDIdentifierType mdIdentifierType = mdIdentifier.getValue();
			String code = getStringFromCharacterString(mdIdentifierType.getCode());
			arrayList.add(code);
		    }
		}
	    }
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	}

	return arrayList.iterator();
    }

    // ----------------------------
    //
    // Vertical extent
    //
    /**
     * @XPathDirective(target =
     *                        "./gmd:extent/gmd:EX_Extent/gmd:verticalElement//gmd:EX_VerticalExtent")
     * @return
     */
    public Iterator<VerticalExtent> getVerticalExtents() {
	ArrayList<VerticalExtent> out = new ArrayList<VerticalExtent>();

	List<EXExtentPropertyType> extent = getElementType().getExtent();
	for (EXExtentPropertyType exExtentPropertyType : extent) {
	    EXExtentType exExtent = exExtentPropertyType.getEXExtent();
	    if (exExtent != null) {

		List<EXVerticalExtentPropertyType> verticalElement = exExtent.getVerticalElement();
		for (EXVerticalExtentPropertyType exVerticalPropertyType : verticalElement) {

		    EXVerticalExtentType value = exVerticalPropertyType.getEXVerticalExtent();
		    if (value != null) {
			VerticalExtent verticalExtent = new VerticalExtent(value);
			out.add(verticalExtent);
		    }
		}
	    }
	}

	return out.iterator();
    }

    public VerticalExtent getVerticalExtent() {
	Iterator<VerticalExtent> verticalExtents = getVerticalExtents();
	if (verticalExtents.hasNext()) {
	    return verticalExtents.next();
	}

	return null;
    }

    /**
     * @XPathDirective(target = "./gmd:extent/gmd:EX_Extent", parent =
     *                        "gmd:verticalElement", position = Position.LAST)
     * @param extent
     */
    public void addVerticalExtent(VerticalExtent extent) {
	EXVerticalExtentPropertyType exGeographicExtentPropertyType = new EXVerticalExtentPropertyType();

	exGeographicExtentPropertyType.setEXVerticalExtent(extent.getElement().getValue());

	EXExtentType exExtentType = new EXExtentType();
	exExtentType.setVerticalElement(Lists.newArrayList(exGeographicExtentPropertyType));

	EXExtentPropertyType exExtentPropertyType = new EXExtentPropertyType();
	exExtentPropertyType.setEXExtent(exExtentType);

	getElementType().getExtent().add(exExtentPropertyType);
    }

    public void addVerticalExtent(double min, double max) {
	VerticalExtent extent = new VerticalExtent();
	extent.setMinimumValue(min);
	extent.setMaximumValue(max);
	addVerticalExtent(extent);
    }

    public void clearExtents() {
	getElementType().unsetExtent();

    }

    // ----------------------------
    //
    // Bounding polygon
    //
    /**
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent", parent =
     *                        "gmd:geographicElement", position = Position.LAST)
     * @param polygon
     */
    public void addBoundingPolygon(BoundingPolygon polygon) {
	EXExtentPropertyType extentProperty = new EXExtentPropertyType();
	EXExtentType extent = new EXExtentType();
	List<EXGeographicExtentPropertyType> geographicExtents = new ArrayList<>();
	EXGeographicExtentPropertyType geographicExtent = new EXGeographicExtentPropertyType();
	ObjectFactory factory = new ObjectFactory();
	geographicExtent.setAbstractEXGeographicExtent(factory.createEXBoundingPolygon(polygon.getElementType()));
	geographicExtents.add(geographicExtent);
	extent.setGeographicElement(geographicExtents);
	extentProperty.setEXExtent(extent);
	getElementType().getExtent().add(extentProperty);
    }

    /**
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent", parent =
     *                        "gmd:geographicElement", position = Position.LAST)
     * @return
     */
    public Iterator<BoundingPolygon> getBoundingPolygons() {

	return getBoundingPolygonsList().iterator();
    }

    /**
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent", parent =
     *                        "gmd:geographicElement", position = Position.LAST)
     * @return
     */
    public List<BoundingPolygon> getBoundingPolygonsList() {
	List<BoundingPolygon> ret = new ArrayList<>();
	if (getElementType().isSetExtent()) {
	    List<EXExtentPropertyType> extents = getElementType().getExtent();
	    for (EXExtentPropertyType exExtentPropertyType : extents) {
		if (exExtentPropertyType.isSetEXExtent()) {
		    EXExtentType extent = exExtentPropertyType.getEXExtent();
		    if (extent.isSetGeographicElement()) {
			List<EXGeographicExtentPropertyType> geographics = extent.getGeographicElement();
			for (EXGeographicExtentPropertyType geographic : geographics) {
			    if (geographic.isSetAbstractEXGeographicExtent()) {
				JAXBElement<? extends AbstractEXGeographicExtentType> abstractGeo = geographic
					.getAbstractEXGeographicExtent();
				AbstractEXGeographicExtentType abstractExtent = abstractGeo.getValue();
				if (abstractExtent instanceof EXBoundingPolygonType) {
				    EXBoundingPolygonType polygonType = (EXBoundingPolygonType) abstractExtent;
				    ret.add(new BoundingPolygon(polygonType));
				}
			    }
			}
		    }
		}
	    }
	}
	return ret;
    }

    // *****
    // root
    // *****

    /**
     * @XPathDirective(clear = "gmd:spatialRepresentationType", target = ".", after
     *                       = "gmd:abstract", position = Position.FIRST)
     * @param spatialRepresentationTypeCode
     */
    public void setSpatialRepresentationType(String spatialRepresentationTypeCode) {
	if (spatialRepresentationTypeCode == null) {
	    getElementType().unsetSpatialRepresentationType();
	    return;
	}
	List<MDSpatialRepresentationTypeCodePropertyType> spatialProperties = new ArrayList<>();
	MDSpatialRepresentationTypeCodePropertyType spatialProperty = new MDSpatialRepresentationTypeCodePropertyType();
	spatialProperty.setMDSpatialRepresentationTypeCode(createCodeListValueType(MD_SPATIAL_REPRESENTATION_TYPE_CODE_CODELIST,
		spatialRepresentationTypeCode, ISO_19115_CODESPACE, spatialRepresentationTypeCode));
	spatialProperties.add(spatialProperty);
	getElementType().setSpatialRepresentationType(spatialProperties);
    }

    /**
     * @XPathDirective(target =
     *                        "gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue")
     * @return
     */
    public String getSpatialRepresentationTypeCodeListValue() {
	if (getElementType().isSetSpatialRepresentationType()) {
	    MDSpatialRepresentationTypeCodePropertyType spatialProperty = getElementType().getSpatialRepresentationType().get(0);
	    if (spatialProperty.isSetMDSpatialRepresentationTypeCode()) {
		return spatialProperty.getMDSpatialRepresentationTypeCode().getCodeListValue();
	    }
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:supplementalInformation/gco:CharacterString")
     */
    public void setSupplementalInformation(String supplementalInformation) {
	getElementType().setSupplementalInformation(createCharacterStringPropertyType(supplementalInformation));
    }

    /**
     * @XPathDirective(target = "gmd:supplementalInformation/gco:CharacterString")
     */
    public String getSupplementalInformation() {
	return getStringFromCharacterString(getElementType().getSupplementalInformation());
    }

    /**
     * @XPathDirective(target = ".", after = "gmd:citation gmd:abstract gmd:purpose
     *                        gmd:credit gmd_status gmd:pointOfContact
     *                        gmd:resourceMaintenance", position = Position.FIRST)
     */
    public void clearGraphicOverviews() {
	getElementType().unsetGraphicOverview();
    }

    /**
     * @XPathDirective(target = ".", after = "gmd:citation gmd:abstract gmd:purpose
     *                        gmd:credit gmd_status gmd:pointOfContact
     *                        gmd:resourceMaintenance", position = Position.FIRST)
     * @param browseGraphic
     */
    public void addGraphicOverview(BrowseGraphic browseGraphic) {
	MDBrowseGraphicPropertyType browseGraphicProperty = new MDBrowseGraphicPropertyType();
	browseGraphicProperty.setMDBrowseGraphic(browseGraphic.getElementType());
	getElementType().getGraphicOverview().add(browseGraphicProperty);
    }

    /**
     * @XPathDirective(target = "gmd:graphicOverview/gmd:MD_BrowseGraphic")
     * @return
     */
    public Iterator<BrowseGraphic> getGraphicOverviews() {
	List<MDBrowseGraphicPropertyType> overviews = getElementType().getGraphicOverview();
	ArrayList<BrowseGraphic> ret = new ArrayList<BrowseGraphic>();
	for (MDBrowseGraphicPropertyType mdBrowseGraphicPropertyType : overviews) {
	    if (mdBrowseGraphicPropertyType.isSetMDBrowseGraphic()) {
		ret.add(new BrowseGraphic(mdBrowseGraphicPropertyType.getMDBrowseGraphic()));
	    }
	}
	return ret.iterator();

    }

    /**
     * @XPathDirective(target = "gmd:graphicOverview/gmd:MD_BrowseGraphic")
     * @return
     */
    public BrowseGraphic getGraphicOverview() {
	Iterator<BrowseGraphic> iterator = getGraphicOverviews();
	if (iterator.hasNext()) {
	    return iterator.next();
	}
	return null;
    }

    public void setSpatialResolution(MDResolution resolution) {
	clearSpatialResolution();
	addSpatialResolution(resolution);
    }

    public void addSpatialResolution(MDResolution resolution) {
	List<MDResolutionPropertyType> list = getElement().getValue().getSpatialResolution();
	if (list == null) {
	    list = new ArrayList<MDResolutionPropertyType>();
	}
	MDResolutionPropertyType property = new MDResolutionPropertyType();
	property.setMDResolution(resolution.getElementType());
	list.add(property);
	getElement().getValue().setSpatialResolution(list);
    }

    public void addAggregateInformation(String identifier, String associationType) {
	List<MDAggregateInformationPropertyType> aggregateInfo = getElement().getValue().getAggregationInfo();
	MDAggregateInformationPropertyType aggregationProperty = new MDAggregateInformationPropertyType();
	MDAggregateInformationType informationType = new MDAggregateInformationType();

	MDIdentifierPropertyType identifierProperty = new MDIdentifierPropertyType();
	MDIdentifierType mdIdentifierType = new MDIdentifierType();
	mdIdentifierType.setCode(createCharacterStringPropertyType(identifier));
	identifierProperty.setMDIdentifier(ObjectFactories.GMD().createMDIdentifier(mdIdentifierType));
	informationType.setAggregateDataSetIdentifier(identifierProperty);

	DSAssociationTypeCodePropertyType atpt = new DSAssociationTypeCodePropertyType();
	atpt.setDSAssociationTypeCode(MIMetadata.createCodeListValueType(ISOMetadata.DS_ASSOCIATION_TYPE_CODE_CODELIST, associationType,
		ISOMetadata.ISO_19115_CODESPACE, associationType));

	informationType.setAssociationType(atpt);
	aggregationProperty.setMDAggregateInformation(informationType);
	aggregateInfo.add(aggregationProperty);
    }

    public Iterator<MDResolution> getSpatialResolutions() {
	List<MDResolution> ret = new ArrayList<>();
	List<MDResolutionPropertyType> list = getElement().getValue().getSpatialResolution();
	if (list != null) {
	    for (MDResolutionPropertyType mdResolutionPropertyType : list) {
		ret.add(new MDResolution(mdResolutionPropertyType.getMDResolution()));
	    }
	}
	return ret.iterator();
    }

    public void clearSpatialResolution() {
	getElement().getValue().setSpatialResolution(null);
    }

    public MDResolution getSpatialResolution() {
	List<MDResolutionPropertyType> list = getElement().getValue().getSpatialResolution();
	if (list == null || list.isEmpty()) {
	    return null;
	}
	return new MDResolution(list.get(0).getMDResolution());
    }

    @Override
    public JAXBElement<MDDataIdentificationType> getElement() {

	JAXBElement<MDDataIdentificationType> element = ObjectFactories.GMD().createMDDataIdentification((MDDataIdentificationType) type);
	return element;

    }

    public Iterator<Double> getDistanceValues() {
	ArrayList<Double> ret = new ArrayList<Double>();
	List<MDResolution> resolutions = Lists.newArrayList(getSpatialResolutions());
	for (MDResolution resolution : resolutions) {
	    if (resolution.getDistanceValue() != null) {
		ret.add(resolution.getDistanceValue());
	    }
	}
	return ret.iterator();
    }

    public Iterator<Integer> getDenominators() {
	ArrayList<Integer> ret = new ArrayList<Integer>();
	List<MDResolution> resolutions = Lists.newArrayList(getSpatialResolutions());
	for (MDResolution resolution : resolutions) {
	    if (resolution.getEquivalentScale() != null) {
		ret.add(resolution.getEquivalentScale().intValue());
	    }
	}
	return ret.iterator();
    }

    public List<ResponsibleParty> getPointOfContactParty() {
	List<ResponsibleParty> ret = getCitedParty("pointOfContact");
	List<CIResponsiblePartyPropertyType> pocs = getElementType().getPointOfContact();
	for (CIResponsiblePartyPropertyType poc : pocs) {
	    CIResponsiblePartyType ciparty = poc.getCIResponsibleParty();
	    ResponsibleParty party = new ResponsibleParty(ciparty);
	    ret.add(party);
	}
	return ret;
    }

    public List<ResponsibleParty> getOriginatorParty() {
	return getCitedParty(new String[] { "originator" });
    }

    public List<ResponsibleParty> getCitedParty(String... roles) {
	List<ResponsibleParty> ret = new ArrayList<>();
	CICitationPropertyType citation = getElementType().getCitation();
	if (citation != null) {
	    CICitationType cicitation = citation.getCICitation();
	    if (cicitation != null) {
		List<CIResponsiblePartyPropertyType> parties = cicitation.getCitedResponsibleParty();
		if (parties != null) {
		    for (CIResponsiblePartyPropertyType party : parties) {
			CIResponsiblePartyType ciparty = party.getCIResponsibleParty();
			if (ciparty != null) {
			    CIRoleCodePropertyType role = ciparty.getRole();
			    if (role != null) {
				CodeListValueType roleCode = role.getCIRoleCode();
				if (roleCode != null) {
				    String value = roleCode.getCodeListValue();
				    if (value != null) {
					if (roles != null && roles.length > 0) {
					    for (String r : roles) {
						if (value.equals(r)) {
						    ret.add(new ResponsibleParty(ciparty));
						}
					    }
					} else {
					    ret.add(new ResponsibleParty(ciparty));
					}
				    }
				}
			    }
			}
		    }
		}
	    }
	}
	return ret;
    }

}
