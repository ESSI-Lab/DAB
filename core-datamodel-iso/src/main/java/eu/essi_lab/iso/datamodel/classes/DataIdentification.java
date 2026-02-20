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

import com.google.common.collect.*;
import eu.essi_lab.iso.datamodel.*;
import eu.essi_lab.jaxb.common.*;
import net.opengis.iso19139.gco.v_20060504.*;
import net.opengis.iso19139.gmd.v_20060504.*;
import net.opengis.iso19139.gmd.v_20060504.ObjectFactory;

import javax.xml.bind.*;
import java.io.*;
import java.math.*;
import java.util.*;

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
     * @return
     * @XPathDirective(target = ".//gmd:topicCategory")
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

    @Override
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
     * @param type
     */
    public void addTopicCategory(MDTopicCategoryCodeType type) {

	addTopicCategory(type.name());
    }

    /**
     * Convenience method to add topic category by string code
     *
     * @param topicCategoryString
     */
    @Override
    public void addTopicCategory(String topicCategoryString) {
	if (topicCategoryString == null || topicCategoryString.isEmpty()) {
	    return;
	}
	// Try to find matching enum value
	try {
	    MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.fromValue(topicCategoryString.toLowerCase());
	    MDTopicCategoryCodePropertyType topicProp = new MDTopicCategoryCodePropertyType();
	    topicProp.setMDTopicCategoryCode(topic);
	    getElementType().getTopicCategory().add(topicProp);
	} catch (Exception e) {
	    // If enum conversion fails, try to match by name

	    eu.essi_lab.lib.utils.GSLoggerFactory.getLogger(getClass())
		    .warn("Could not convert topic category string to enum: " + topicCategoryString);

	}
    }

    // --------------------------------------------------------
    //
    // resource languages
    //

    /**
     * @return
     * @XPathDirective(target = ".//gmd:language/gco:CharacterString")
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
     * @param language
     * @XPathDirective(create = "gmd:language/gco:CharacterString", target = ".", after = "gmd:spatialResolution", position =
     * Position.FIRST)
     */
    public void addLanguage(String language) {
	if (language != null) {
	    getElementType().getLanguage().add(createCharacterStringPropertyType(language));
	}
    }

    /**
     * @XPathDirective(create = "gmd:language/gco:CharacterString", target = ".", after = "gmd:spatialResolution", position =
     * Position.FIRST)
     */
    public void clearLanguages() {

	getElementType().unsetLanguage();

    }

    // --------------------------------------------------------
    //
    // Character set
    //

    /**
     * @param code
     * @XPathDirective(target = "gmd:characterSet")
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
     * @return
     * @XPathDirective(target = "gmd:characterSet/gmd:MD_CharacterSetCode")
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
     * @return
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent/gmd:temporalElement//gmd:EX_TemporalExtent")
     */
    @Override
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
     * @param extent
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent", parent = "gmd:temporalElement", before =
     * "*:extent/gmd:EX_Extent/gmd:verticalElement", position = Position.LAST)
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
     * @param description
     * @param north
     * @param west
     * @param south
     * @param east
     * @XPathDirective(target = "./gmd:extent/gmd:EX_Extent/gmd:description/gco:CharacterString")
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
     * @param bbox
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent", parent = "gmd:geographicElement", position = Position.FIRST)
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

    @Override
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
     * @return
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent/gmd:geographicElement//gmd:EX_GeographicBoundingBox")
     */
    @Override
    public Iterator<GeographicBoundingBox> getGeographicBoundingBoxes() {

	return super.getGeographicBoundingBoxes(getElementType().getExtent());
    }

    /**
     * @XPathDirective(target = ".//*:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/
     */
    @Override
    public Iterator<String> getGeographicDescriptionCodes() {

	return super.getGeographicDescriptionCodes(getElementType().getExtent());
    }

    // ----------------------------
    //
    // Vertical extent
    //

    /**
     * @return
     * @XPathDirective(target = "./gmd:extent/gmd:EX_Extent/gmd:verticalElement//gmd:EX_VerticalExtent")
     */
    @Override
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
     * @param extent
     * @XPathDirective(target = "./gmd:extent/gmd:EX_Extent", parent = "gmd:verticalElement", position = Position.LAST)
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

    /**
     * Convenience method to add vertical extent with CRS
     *
     * @param min
     * @param max
     * @param crs
     */
    public void addVerticalExtent(double min, double max, String crs) {
	VerticalExtent extent = new VerticalExtent();
	extent.setMinimumValue(min);
	extent.setMaximumValue(max);
	if (crs != null && !crs.isEmpty()) {
	    VerticalCRS verticalCRS = new VerticalCRS();
	    // Set CRS identifier if needed
	    extent.setVerticalCRS(verticalCRS);
	}
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
     * @param polygon
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent", parent = "gmd:geographicElement", position = Position.LAST)
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
     * @return
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent", parent = "gmd:geographicElement", position = Position.LAST)
     */
    public Iterator<BoundingPolygon> getBoundingPolygons() {

	return getBoundingPolygonsList().iterator();
    }

    /**
     * @return
     * @XPathDirective(target = "./*:extent/gmd:EX_Extent", parent = "gmd:geographicElement", position = Position.LAST)
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
				JAXBElement<? extends AbstractEXGeographicExtentType> abstractGeo = geographic.getAbstractEXGeographicExtent();
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
     * @param spatialRepresentationTypeCode
     * @XPathDirective(clear = "gmd:spatialRepresentationType", target = ".", after = "gmd:abstract", position = Position.FIRST)
     */
    public void setSpatialRepresentationType(String spatialRepresentationTypeCode) {
	if (spatialRepresentationTypeCode == null) {
	    getElementType().unsetSpatialRepresentationType();
	    return;
	}
	List<MDSpatialRepresentationTypeCodePropertyType> spatialProperties = new ArrayList<>();
	MDSpatialRepresentationTypeCodePropertyType spatialProperty = new MDSpatialRepresentationTypeCodePropertyType();
	spatialProperty.setMDSpatialRepresentationTypeCode(
		createCodeListValueType(MD_SPATIAL_REPRESENTATION_TYPE_CODE_CODELIST, spatialRepresentationTypeCode, ISO_19115_CODESPACE,
			spatialRepresentationTypeCode));
	spatialProperties.add(spatialProperty);
	getElementType().setSpatialRepresentationType(spatialProperties);
    }

    /**
     * @return
     * @XPathDirective(target = "gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue")
     */
    @Override
    public String getSpatialRepresentationTypeCodeListValue() {

	List<String> list = getSpatialRepresentationTypeCodeListValueList();

	if (!list.isEmpty()) {

	    return list.get(0);
	}

	return null;
    }

    /**
     * @return
     */
    @Override
    public List<String> getSpatialRepresentationTypeCodeListValueList() {

	ArrayList<String> out = new ArrayList<>();
	try {
	    if (getElementType().isSetSpatialRepresentationType()) {
		List<MDSpatialRepresentationTypeCodePropertyType> spatialProperty = getElementType().getSpatialRepresentationType();
		for (MDSpatialRepresentationTypeCodePropertyType type : spatialProperty) {
		    if (type.isSetMDSpatialRepresentationTypeCode()) {
			String codeListValue = type.getMDSpatialRepresentationTypeCode().getCodeListValue();
			if (codeListValue != null && !codeListValue.isEmpty()) {
			    out.add(codeListValue);
			}
		    }
		}
	    }
	} catch (Exception e) {
	}

	return out;
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
     * @XPathDirective(target = ".", after = "gmd:citation gmd:abstract gmd:purpose gmd:credit gmd_status gmd:pointOfContact
     * gmd:resourceMaintenance", position = Position.FIRST)
     */
    public void clearGraphicOverviews() {
	getElementType().unsetGraphicOverview();
    }

    /**
     * @param browseGraphic
     * @XPathDirective(target = ".", after = "gmd:citation gmd:abstract gmd:purpose gmd:credit gmd_status gmd:pointOfContact
     * gmd:resourceMaintenance", position = Position.FIRST)
     */
    public void addGraphicOverview(BrowseGraphic browseGraphic) {
	MDBrowseGraphicPropertyType browseGraphicProperty = new MDBrowseGraphicPropertyType();
	browseGraphicProperty.setMDBrowseGraphic(browseGraphic.getElementType());
	getElementType().getGraphicOverview().add(browseGraphicProperty);
    }

    /**
     * Convenience method to add a browse graphic from URL and description
     *
     * @param url
     * @param description
     */
    public void addBrowseGraphic(String url, String description) {
	BrowseGraphic graphic = new BrowseGraphic();
	graphic.setFileName(url);
	if (description != null) {
	    graphic.setFileDescription(description);
	}
	addGraphicOverview(graphic);
    }

    public void addBrowseGraphic(String url, String description, String fileType) {
	BrowseGraphic graphic = new BrowseGraphic();
	graphic.setFileName(url);
	if (description != null) {
	    graphic.setFileDescription(description);
	}
	graphic.setFileType(fileType);
	addGraphicOverview(graphic);
    }

    /**
     * @return
     * @XPathDirective(target = "gmd:graphicOverview/gmd:MD_BrowseGraphic")
     */
    @Override
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
     * @return
     * @XPathDirective(target = "gmd:graphicOverview/gmd:MD_BrowseGraphic")
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

    /**
     * Convenience method to set spatial resolution from a distance value
     *
     * @param distance
     */
    public void setSpatialResolution(Double distance) {
	if (distance == null || distance.isNaN()) {
	    clearSpatialResolution();
	    return;
	}
	MDResolution resolution = new MDResolution();
	resolution.setDistance("m", distance);
	setSpatialResolution(resolution);
    }

    /**
     * Convenience method to set equivalent scale
     *
     * @param scale
     */
    public void setEquivalentScale(int scale) {
	MDResolution resolution = getSpatialResolution();
	if (resolution == null) {
	    resolution = new MDResolution();
	}
	resolution.setEquivalentScale(java.math.BigInteger.valueOf(scale));
	setSpatialResolution(resolution);
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
}
