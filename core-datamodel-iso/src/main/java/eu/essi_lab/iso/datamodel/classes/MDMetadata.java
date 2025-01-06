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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Node;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gco.v_20060504.DatePropertyType;
import net.opengis.iso19139.gmd.v_20060504.AbstractMDContentInformationType;
import net.opengis.iso19139.gmd.v_20060504.AbstractMDIdentificationType;
import net.opengis.iso19139.gmd.v_20060504.AbstractMDSpatialRepresentationType;
import net.opengis.iso19139.gmd.v_20060504.CIResponsiblePartyPropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQDataQualityPropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQDataQualityType;
import net.opengis.iso19139.gmd.v_20060504.MDAggregateInformationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDAggregateInformationType;
import net.opengis.iso19139.gmd.v_20060504.MDCharacterSetCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDContentInformationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDCoverageDescriptionType;
import net.opengis.iso19139.gmd.v_20060504.MDDataIdentificationType;
import net.opengis.iso19139.gmd.v_20060504.MDDistributionPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDDistributionType;
import net.opengis.iso19139.gmd.v_20060504.MDGridSpatialRepresentationType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentificationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierType;
import net.opengis.iso19139.gmd.v_20060504.MDMetadataType;
import net.opengis.iso19139.gmd.v_20060504.MDReferenceSystemPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDScopeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDSpatialRepresentationPropertyType;
import net.opengis.iso19139.srv.v_20060504.SVServiceIdentificationType;

/**
 * MD_Metadata
 * 
 * @author Fabrizio
 */
public class MDMetadata extends ISOMetadata<MDMetadataType> {

    @XmlElement(name = "MD_Metadata", namespace = CommonNameSpaceContext.GMD_NS_URI)
    protected MDMetadataType type;

    public MDMetadata() {

	this(new MDMetadataType());
    }

    public MDMetadata(MDMetadataType type) {

	this.type = type;
    }

    public MDMetadata(InputStream stream) throws JAXBException {

	this.type = fromStream(stream);
    }

    public MDMetadata(String string) throws UnsupportedEncodingException, JAXBException {

	this(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    }

    public MDMetadata(Node node) throws JAXBException {

	this.type = fromNode(node);
    }

    /**
     * @XPathDirective(target = ".", create = "gmd:fileIdentifier/gco:CharacterString", clear = "gmd:fileIdentifier",
     *                        position = Position.FIRST)
     * @param id
     */
    public void setFileIdentifier(String id) {

	getElementType().setFileIdentifier(createCharacterStringPropertyType(id));
    }

    /**
     * @XPathDirective(target = "gmd:fileIdentifier/gco:CharacterString")
     * @return
     */
    @XmlTransient
    public String getFileIdentifier() {
	try {
	    return ISOMetadata.getStringFromCharacterString(getElementType().getFileIdentifier());
	} catch (NullPointerException ex) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = ".", create = "gmd:hierarchyLevelName/gco:CharacterString", clear =
     *                        "gmd:hierachyLevelName", after = "gmd:hierarchyLevel")
     * @param hierarhcyLevelName
     */
    public void setHierarchyLevelName(String hierarhcyLevelName) {

	getElementType().setHierarchyLevelName(Lists.newArrayList(createCharacterStringPropertyType(hierarhcyLevelName)));
    }

    /**
     * @XPathDirective(target = "gmd:hierarchyLevelName/gco:CharacterString")
     * @return
     */
    @XmlTransient
    public String getHierarchyLevelName() {

	try {
	    return ISOMetadata.getStringFromCharacterString(getElementType().getHierarchyLevelName().get(0));
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	}

	return null;

    }

    /**
     * @return
     */
    public Iterator<String> getAggregatedResourcesIdentifiers() {

	ArrayList<String> arrayList = new ArrayList<String>();

	List<MDIdentificationPropertyType> identificationInfo = getElementType().getIdentificationInfo();
	for (MDIdentificationPropertyType mdIdentificationPropertyType : identificationInfo) {

	    JAXBElement<? extends AbstractMDIdentificationType> abstractMDIdentification = mdIdentificationPropertyType
		    .getAbstractMDIdentification();
	    if (abstractMDIdentification != null) {

		List<MDAggregateInformationPropertyType> aggregationInfo = abstractMDIdentification.getValue().getAggregationInfo();
		for (MDAggregateInformationPropertyType mdAggregateInformationPropertyType : aggregationInfo) {

		    try {
			String id = ISOMetadata.getStringFromCharacterString(mdAggregateInformationPropertyType.getMDAggregateInformation()
				.getAggregateDataSetIdentifier().getMDIdentifier().getValue().getCode());

			if (id != null && !id.equals("")) {
			    arrayList.add(id);
			}
		    } catch (Exception ex) {
		    }
		}
	    }
	}

	return arrayList.iterator();
    }

    public void addAggregatedResourceIdentifier(String identifier) {

	List<MDIdentificationPropertyType> identificationInfo = getElementType().getIdentificationInfo();
	MDIdentificationPropertyType mdIdentificationPropertyType = null;
	if (identificationInfo.isEmpty()) {

	    mdIdentificationPropertyType = new MDIdentificationPropertyType();
	    getElementType().getIdentificationInfo().add(mdIdentificationPropertyType);

	} else {
	    mdIdentificationPropertyType = identificationInfo.get(0);
	}

	JAXBElement<? extends AbstractMDIdentificationType> abstractMDIdentification = mdIdentificationPropertyType
		.getAbstractMDIdentification();

	if (abstractMDIdentification == null) {

	    MDDataIdentificationType mdDataIdentificationType = new MDDataIdentificationType();

	    abstractMDIdentification = ObjectFactories.GMD().createAbstractMDIdentification(mdDataIdentificationType);

	    mdIdentificationPropertyType.setAbstractMDIdentification(abstractMDIdentification);
	}

	AbstractMDIdentificationType idType = abstractMDIdentification.getValue();

	List<MDAggregateInformationPropertyType> aggregationInfo = idType.getAggregationInfo();
	MDAggregateInformationPropertyType info = new MDAggregateInformationPropertyType();
	aggregationInfo.add(info);

	MDAggregateInformationType mdAggregateInformation = info.getMDAggregateInformation();
	if (mdAggregateInformation == null) {

	    mdAggregateInformation = new MDAggregateInformationType();
	    info.setMDAggregateInformation(mdAggregateInformation);
	}

	MDIdentifierPropertyType aggregateDataSetIdentifier = mdAggregateInformation.getAggregateDataSetIdentifier();
	if (aggregateDataSetIdentifier == null) {

	    aggregateDataSetIdentifier = new MDIdentifierPropertyType();
	    mdAggregateInformation.setAggregateDataSetIdentifier(aggregateDataSetIdentifier);
	}

	JAXBElement<? extends MDIdentifierType> mdIdentifier = aggregateDataSetIdentifier.getMDIdentifier();
	MDIdentifierType mdIdentifierType = null;

	if (mdIdentifier == null) {
	    mdIdentifierType = new MDIdentifierType();
	    aggregateDataSetIdentifier.setMDIdentifier(ObjectFactories.GMD().createMDIdentifier(mdIdentifierType));
	    mdIdentifierType.setCode(createCharacterStringPropertyType(identifier));
	}

    }

    /**
     * @XPathDirective(target = ".", after = "gmd:parentIdentifier", position = Position.FIRST)
     * @param scopeCode
     */
    public void addHierarchyLevelScopeCodeListValue(String scopeCode) {

	MDScopeCodePropertyType mdScopeCodePropertyType = new MDScopeCodePropertyType();
	mdScopeCodePropertyType.setMDScopeCode(ObjectFactories.GMD()
		.createMDScopeCode(createCodeListValueType(MD_SCOPE_CODE_CODELIST, scopeCode, ISO_19115_CODESPACE, scopeCode)));

	getElementType().getHierarchyLevel().add(mdScopeCodePropertyType);
    }

    /**
     * @XPathDirective(target = "gmd:hierarchyLevel")
     * @return
     */
    @XmlTransient
    public Iterator<String> getHierarchyLevelScopeCodeListValues() {

	ArrayList<String> out = new ArrayList<String>();
	List<MDScopeCodePropertyType> hierarchyLevel = getElementType().getHierarchyLevel();
	for (MDScopeCodePropertyType mdScopeCodePropertyType : hierarchyLevel) {
	    JAXBElement<CodeListValueType> mdScopeCode = mdScopeCodePropertyType.getMDScopeCode();
	    if (mdScopeCode != null && mdScopeCode.getValue() != null) {
		out.add(mdScopeCode.getValue().getCodeListValue());
	    }
	}

	return out.iterator();
    }

    @XmlTransient
    public String getHierarchyLevelScopeCodeListValue() {

	Iterator<String> hierarchyLevels = getHierarchyLevelScopeCodeListValues();
	if (hierarchyLevels.hasNext()) {
	    return hierarchyLevels.next();
	}

	return null;
    }

    /**
     * @XPathDirective(clear = "gmd:hierarchyLevel")
     */
    public void clearHierarchyLevels() {

	getElementType().getHierarchyLevel().clear();
    }

    /**
     * @XPathDirective(target = ".", create = "gmd:language/gco:CharacterString", clear = "gmd:language", after =
     *                        "gmd:fileIdentifier", position = Position.FIRST)
     * @param language
     */
    public void setLanguage(String language) {

	getElementType().setLanguage(createCharacterStringPropertyType(language));
    }

    /**
     * @XPathDirective(target = "gmd:language/gco:CharacterString")
     * @return
     */
    @XmlTransient
    public String getLanguage() {
	    Object value = ISOMetadata.getStringFromCharacterString(getElementType().getLanguage());
	    if (value==null) {
		return null;
	    }
	    if (value instanceof CodeListValueType) {
		CodeListValueType clvt = (CodeListValueType) value;
		return clvt.getValue();
	    } else {
		return value.toString();
	    }

    }

    /**
     * @XPathDirective(target = "gmd:characterSet")
     * @param code
     */
    public void setCharacterSetCode(String code) {

	MDCharacterSetCodePropertyType propertyType = new MDCharacterSetCodePropertyType();
	propertyType.setMDCharacterSetCode(createCodeListValueType(MD_CHARACTER_SET_CODE_CODELIST, code, ISO_19115_CODESPACE, code));

	getElementType().setCharacterSet(propertyType);
    }

    /**
     * @XPathDirective(target = "gmd:characterSet/gmd:MD_CharacterSetCode")
     * @return
     */
    @XmlTransient
    public String getCharacterSetCode() {

	try {
	    return getElementType().getCharacterSet().getMDCharacterSetCode().getCodeListValue();
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = ".", create = "gmd:parentIdentifier/gco:CharacterString", clear =
     *                        "gmd:parentIdentifier", after = "gmd:fileIdentifier gmd:language gmd:characterSet",
     *                        position = Position.FIRST)
     * @param id
     */
    public void setParentIdentifier(String id) {

	getElementType().setParentIdentifier(createCharacterStringPropertyType(id));
    }

    /**
     * @XPathDirective(target = "gmd:parentIdentifier/gco:CharacterString")
     * @return
     */
    @XmlTransient
    public String getParentIdentifier() {
	try {
	    return ISOMetadata.getStringFromCharacterString(getElementType().getParentIdentifier());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:dateStamp/gco:Date")
     * @param date
     */
    public void setDateStampAsDate(String date) {

	DatePropertyType datePropertyType = new DatePropertyType();
	datePropertyType.setDate(date);

	getElementType().setDateStamp(datePropertyType);
    }

    /**
     * @XPathDirective(target = "gmd:dateStamp/gco:DateTime")
     * @param calendar
     */
    public void setDateStampAsDateTime(XMLGregorianCalendar calendar) {

	DatePropertyType datePropertyType = new DatePropertyType();
	datePropertyType.setDateTime(calendar);

	getElementType().setDateStamp(datePropertyType);
    }

    /**
     * @XPathDirective(target = "gmd:dateStamp/gco:Date")
     * @return
     */
    @XmlTransient
    public String getDateStamp() {
	if (getElementType().getDateStamp()==null) {
	    return null;
	}
	return getElementType().getDateStamp().getDate();
    }

    /**
     * @XPathDirective(target = "gmd:dateStamp/gco:DateTime")
     * @return
     */
    public XMLGregorianCalendar getDateTimeStamp() {
	if (getElementType().getDateStamp()==null) {
	    return null;
	}
	return getElementType().getDateStamp().getDateTime();
    }

    /**
     * @XPathDirective(target = "gmd:metadataStandardName/gco:CharacterString")
     * @return
     */
    @XmlTransient
    public String getMetadataStandardName() {

	try {
	    return ISOMetadata.getStringFromCharacterString(getElementType().getMetadataStandardName());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:metadataStandardName/gco:CharacterString")
     * @param name
     */
    public void setMetadataStandardName(String name) {

	if (name == null) {
	    getElementType().setMetadataStandardName(null);
	    return;
	}

	CharacterStringPropertyType metadataStandardName = createCharacterStringPropertyType(name);

	getElementType().setMetadataStandardName(metadataStandardName);

    }

    /**
     * @XPathDirective(target = "gmd:metadataStandardVersion/gco:CharacterString")
     * @param name
     */
    public void setMetadataStandardVersion(String name) {

	if (name == null) {
	    getElementType().setMetadataStandardVersion(null);
	    return;
	}

	CharacterStringPropertyType metadataStandardVersion = createCharacterStringPropertyType(name);
	getElementType().setMetadataStandardVersion(metadataStandardVersion);
    }

    /**
     * @XPathDirective(target = "gmd:metadataStandardVersion/gco:CharacterString")
     * @return
     */
    @XmlTransient
    public String getMetadataStandardVersion() {

	try {
	    return ISOMetadata.getStringFromCharacterString(getElementType().getMetadataStandardVersion());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    // -------------------------------
    //
    // DataIdentification
    //
    /**
     * @XPathDirective(target = ".", parent = "gmd:identificationInfo", before = "gmd:distributionInfo", position =
     *                        Position.LAST)
     * @param identification
     */
    public void addDataIdentification(DataIdentification identification) {

	MDIdentificationPropertyType propertyType = new MDIdentificationPropertyType();
	propertyType.setAbstractMDIdentification(identification.getElement());

	getElementType().getIdentificationInfo().add(propertyType);

    }

    /**
     * @XPathDirective(target = "gmd:identificationInfo/gmd:MD_DataIdentification |
     *                        gmd:identificationInfo/*[@gco:isoType='MD_DataIdentification_Type']")
     * @return
     */
    @XmlTransient
    public Iterator<DataIdentification> getDataIdentifications() {

	ArrayList<DataIdentification> out = new ArrayList<DataIdentification>();
	List<MDIdentificationPropertyType> identificationInfo = getElementType().getIdentificationInfo();
	for (MDIdentificationPropertyType type : identificationInfo) {

	    JAXBElement<? extends AbstractMDIdentificationType> element = type.getAbstractMDIdentification();

	    if (element != null) {
		AbstractMDIdentificationType value = element.getValue();
		if (value instanceof MDDataIdentificationType) {

		    MDDataIdentificationType mdid = (MDDataIdentificationType) value;
		    DataIdentification dataIdentification = new DataIdentification(mdid);
		    out.add(dataIdentification);
		} else if (value instanceof SVServiceIdentificationType) {
		    SVServiceIdentificationType si = (SVServiceIdentificationType) value;
		    // ServiceIdentification service =
		    Iterator<ServiceIdentification> serviceIdentifications = getServiceIdentifications();
		    if (serviceIdentifications.hasNext()) {
			ServiceIdentification serviceId = serviceIdentifications.next();

			DataIdentification dataId = convertServiceIdentificationToDataIdentification(serviceId);

			out.add(dataId);
		    }
		}
	    }
	}

	return out.iterator();
    }

    public DataIdentification getDataIdentification() {

	Iterator<DataIdentification> dataIdentifications = getDataIdentifications();
	if (dataIdentifications.hasNext()) {
	    return dataIdentifications.next();
	}
	Iterator<ServiceIdentification> serviceIdentifications = getServiceIdentifications();
	if (serviceIdentifications.hasNext()) {
	    ServiceIdentification serviceId = serviceIdentifications.next();

	    DataIdentification dataId = convertServiceIdentificationToDataIdentification(serviceId);

	    return dataId;
	}

	return null;
    }

    public DataIdentification convertServiceIdentificationToDataIdentification(ServiceIdentification serviceId) {
	DataIdentification result = new DataIdentification();

	String title = serviceId.getCitationTitle();
	String abstakt = serviceId.getAbstract();
	String creationDate = serviceId.getCitationCreationDate();
	String alternativeTitle = serviceId.getCitationAlternateTitle();
	String revisionDate = serviceId.getCitationRevisionDate();
	String editionDate = serviceId.getCitationEditionDate();
	ResponsibleParty contact = serviceId.getPointOfContact();
	String publicationDate = serviceId.getCitationPublicationDate();
	BrowseGraphic browseGraphic = serviceId.getGraphicOverview();
	VerticalExtent verticalExtent = serviceId.getVerticalExtent();
	Iterator<LegalConstraints> legalContraints = serviceId.getLegalConstraints();
	String resourceID = serviceId.getResourceIdentifier();

	if (title != null && !title.equals(""))
	    result.setCitationTitle(title);

	if (abstakt != null && !abstakt.equals(""))
	    result.setAbstract(abstakt);

	if (contact != null) {
	    result.addPointOfContact(contact);
	    result.addCitationResponsibleParty(contact);
	}

	TemporalExtent extent = serviceId.getTemporalExtent();
	if (extent != null)
	    result.addTemporalExtent(extent);

	if (browseGraphic != null)
	    result.addGraphicOverview(browseGraphic);

	if (serviceId.getGeographicBoundingBox() != null) {

	    GeographicBoundingBox boundingBox = serviceId.getGeographicBoundingBox();
	    if (Objects.nonNull(boundingBox.getEast()) && //
		    Objects.nonNull(boundingBox.getWest()) && //
		    Objects.nonNull(boundingBox.getNorth()) && //
		    Objects.nonNull(boundingBox.getSouth()) //
	    ) {
		result.addGeographicBoundingBox(serviceId.getGeographicBoundingBox());

	    }
	}

	Iterator<Keywords> keywords = serviceId.getKeywords();
	if (keywords != null) {
	    while (keywords.hasNext()) {
		result.addKeywords(keywords.next());
	    }
	}

	if (creationDate != null)
	    result.setCitationCreationDate(creationDate);

	if (editionDate != null)
	    result.setCitationEditionDate(editionDate);

	if (publicationDate != null)
	    result.setCitationPublicationDate(publicationDate);
	
	if (revisionDate != null)
	    result.setCitationRevisionDate(revisionDate);

	if (resourceID != null && !resourceID.equals(""))
	    result.setResourceIdentifier(resourceID);

	if (alternativeTitle != null && !alternativeTitle.equals(""))
	    result.addCitationAlternateTitle(alternativeTitle);

	if (verticalExtent != null)
	    result.addVerticalExtent(verticalExtent);

	if (legalContraints != null) {
	    while (legalContraints.hasNext()) {
		result.addLegalConstraints(legalContraints.next());
	    }
	}

	return result;

    }

    /**
     * @XPathDirective(clear = "gmd:identificationInfo[gmd:MD_DataIdentification]")
     */
    public void clearDataIdentifications() {

	List<MDIdentificationPropertyType> identificationInfo = getElementType().getIdentificationInfo();
	Iterator<MDIdentificationPropertyType> iterator = identificationInfo.iterator();
	while (iterator.hasNext()) {

	    MDIdentificationPropertyType type = iterator.next();
	    JAXBElement<? extends AbstractMDIdentificationType> element = type.getAbstractMDIdentification();
	    AbstractMDIdentificationType value = element.getValue();
	    if (value instanceof MDDataIdentificationType) {
		iterator.remove();
	    }
	}
    }

    // -------------------------------
    //
    // Distribution
    //
    public void setDistribution(Distribution distribution) {

	if (distribution == null) {
	    getElementType().setDistributionInfo(null);
	    return;
	}
	MDDistributionPropertyType propertyType = new MDDistributionPropertyType();
	propertyType.setMDDistribution(distribution.getElementType());
	getElementType().setDistributionInfo(propertyType);
    }

    /**
     * @XPathDirective(target = "gmd:distributionInfo/gmd:MD_Distribution")
     * @return
     */
    @XmlTransient
    public Distribution getDistribution() {

	MDDistributionPropertyType info = getElementType().getDistributionInfo();

	if (info != null) {
	    MDDistributionType mdDistribution = info.getMDDistribution();
	    if (mdDistribution != null) {
		return new Distribution(mdDistribution);
	    }
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:referenceSystemInfo/gmd:MD_ReferenceSystem")
     */
    public Iterator<ReferenceSystem> getReferenceSystemInfos() {
	ArrayList<ReferenceSystem> ret = new ArrayList<>();
	List<MDReferenceSystemPropertyType> referenceSystems = getElementType().getReferenceSystemInfo();
	for (MDReferenceSystemPropertyType referenceSystem : referenceSystems) {
	    if (referenceSystem.isSetMDReferenceSystem()) {
		ret.add(new ReferenceSystem(referenceSystem.getMDReferenceSystem()));
	    }
	}
	return ret.iterator();
    }

    /**
     * @XPathDirective(target = ".", parent = "gmd:referenceSystemInfo", before = "gmd:identificationInfo", position =
     *                        Position.LAST)
     */
    public void addReferenceSystemInfo(ReferenceSystem referenceSystem) {
	MDReferenceSystemPropertyType referenceProperty = new MDReferenceSystemPropertyType();
	referenceProperty.setMDReferenceSystem(referenceSystem.getElementType());
	getElementType().getReferenceSystemInfo().add(referenceProperty);
    }

    /**
     * @XPathDirective(clear = "gmd:referenceSystemInfo")
     */
    public void clearReferenceSystemInfos() {
	getElementType().unsetReferenceSystemInfo();
    }

    public void clearContacts() {
	getElementType().unsetContact();
    }

    /**
     * @param contact party responsible for the metadata information
     */
    /**
     * @XPathDirective(target = ".", clear = "gmd:contact", parent = "gmd:contact", before = "gmd:identificationInfo")
     */
    public void addContact(ResponsibleParty contact) {
	CIResponsiblePartyPropertyType contactProperty = new CIResponsiblePartyPropertyType();
	contactProperty.setCIResponsibleParty(contact.getElementType());
	getElementType().getContact().add(contactProperty);
    }

    /**
     * @return party responsible for the metadata information
     */
    /**
     * @XPathDirective(target = ".//*:contact/*:CI_ResponsibleParty")
     */
    public Iterator<ResponsibleParty> getContacts() {
	List<ResponsibleParty> ret = new ArrayList<>();
	List<CIResponsiblePartyPropertyType> contactProperties = getElementType().getContact();
	for (CIResponsiblePartyPropertyType contactProperty : contactProperties) {
	    ret.add(new ResponsibleParty(contactProperty.getCIResponsibleParty()));
	}
	return ret.iterator();
    }

    /**
     * @XPathDirective(target = "gmd:contentInfo/gmd:MD_CoverageDescription")
     */
    public CoverageDescription getCoverageDescription() {

	List<MDContentInformationPropertyType> contentInfo = getElementType().getContentInfo();
	if (!contentInfo.isEmpty()) {

	    if (contentInfo.get(0).isSetAbstractMDContentInformation()) {

		JAXBElement<? extends AbstractMDContentInformationType> abstractMDContentInformation = contentInfo.get(0)
			.getAbstractMDContentInformation();
		AbstractMDContentInformationType value = abstractMDContentInformation.getValue();
		if (value instanceof MDCoverageDescriptionType) {

		    MDCoverageDescriptionType type = (MDCoverageDescriptionType) value;
		    return new CoverageDescription(type);
		}
	    }
	}

	return null;
    }
    
    /**
     * @XPathDirective(target = "gmd:contentInfo/gmd:MD_CoverageDescription")
     */
    public void clearContentInfos() {

	List<MDContentInformationPropertyType> ci = getElementType().getContentInfo();
	if (ci!=null) {
	    ci.clear();
	}
    }

    /**
     * @XPathDirective(target = "gmd:contentInfo/gmd:MD_CoverageDescription")
     */
    public Iterator<CoverageDescription> getCoverageDescriptions() {

	List<CoverageDescription> descriptions = new ArrayList<>();
	List<MDContentInformationPropertyType> contentInfos = getElementType().getContentInfo();
	for (MDContentInformationPropertyType contentInfo : contentInfos) {

	    if (contentInfo.isSetAbstractMDContentInformation()) {

		JAXBElement<? extends AbstractMDContentInformationType> abstractMDContentInformation = contentInfo
			.getAbstractMDContentInformation();
		AbstractMDContentInformationType value = abstractMDContentInformation.getValue();
		if (value instanceof MDCoverageDescriptionType) {

		    MDCoverageDescriptionType type = (MDCoverageDescriptionType) value;
		    descriptions.add(new CoverageDescription(type));
		}
	    }
	}

	return descriptions.iterator();
    }

    /**
     * @XPathDirective(target = "gmd:contentInfo/gmd:MD_CoverageDescription")
     */
    public void addCoverageDescription(CoverageDescription description) {

	MDContentInformationPropertyType contentInfoType = new MDContentInformationPropertyType();
	MDCoverageDescriptionType value = description.getElementType();
	contentInfoType.setAbstractMDContentInformation(ObjectFactories.GMD().createMDCoverageDescription(value));

	getElementType().getContentInfo().add(contentInfoType);
    }

    // -------------------------------
    //
    // ServiceIdentification
    //
    /**
     * @XPathDirective(target = ".", parent = "gmd:identificationInfo", before = "gmd:distributionInfo", position =
     *                        Position.LAST)
     * @param srvId
     */
    public void addServiceIdentification(ServiceIdentification srvId) {

	// SVServiceIdentificationPropertyType propertyType = new SVServiceIdentificationPropertyType();

	MDIdentificationPropertyType propertyType = new MDIdentificationPropertyType();

	propertyType.setAbstractMDIdentification(srvId.getElement());

	getElementType().getIdentificationInfo().add(propertyType);

    }

    /**
     * @XPathDirective(target = "gmd:identificationInfo/srv:SV_ServiceIdentification")
     */
    public Iterator<ServiceIdentification> getServiceIdentifications() {

	ArrayList<ServiceIdentification> out = new ArrayList<ServiceIdentification>();
	List<MDIdentificationPropertyType> identificationInfo = getElementType().getIdentificationInfo();
	for (MDIdentificationPropertyType type : identificationInfo) {

	    JAXBElement<? extends AbstractMDIdentificationType> element = type.getAbstractMDIdentification();

	    if (element != null) {
		AbstractMDIdentificationType value = element.getValue();
		if (value instanceof SVServiceIdentificationType) {

		    SVServiceIdentificationType mdid = (SVServiceIdentificationType) value;
		    ServiceIdentification serviceIdentification = new ServiceIdentification(mdid);
		    out.add(serviceIdentification);
		}
	    }
	}

	return out.iterator();
    }

    public ServiceIdentification getServiceIdentification() {

	Iterator<ServiceIdentification> serviceIdentifications = getServiceIdentifications();
	if (serviceIdentifications.hasNext()) {
	    return serviceIdentifications.next();
	}

	return null;
    }

    /**
     * @XPathDirective(clear = "gmd:identificationInfo[srv:SV_ServiceIdentification]")
     */
    public void clearServiceIdentifications() {

	List<MDIdentificationPropertyType> identificationInfo = getElementType().getIdentificationInfo();
	Iterator<MDIdentificationPropertyType> iterator = identificationInfo.iterator();
	while (iterator.hasNext()) {

	    MDIdentificationPropertyType type = iterator.next();
	    JAXBElement<? extends AbstractMDIdentificationType> element = type.getAbstractMDIdentification();
	    AbstractMDIdentificationType value = element.getValue();

	    if (value instanceof SVServiceIdentificationType) {
		iterator.remove();
	    }

	}
    }

    /**
     * @XPathDirective(clear = "gmd:distributionInfo")
     */
    void clearDistributionInfo() {
	// TODO
    }

    /**
     * @XPathDirective(target = "gmd:spatialRepresentationInfo/gmd:MD_GridSpatialRepresentation")
     */
    public Iterator<GridSpatialRepresentation> getGridSpatialRepresentations() {
	ArrayList<GridSpatialRepresentation> ret = new ArrayList<>();
	for (MDSpatialRepresentationPropertyType mdsrp : getElementType().getSpatialRepresentationInfo()) {
	    AbstractMDSpatialRepresentationType abstractValue = mdsrp.getAbstractMDSpatialRepresentation().getValue();
	    if (abstractValue instanceof MDGridSpatialRepresentationType) {
		MDGridSpatialRepresentationType mdgrid = (MDGridSpatialRepresentationType) abstractValue;
		ret.add(new GridSpatialRepresentation(mdgrid));
	    }
	}
	return ret.iterator();
    }

    public GridSpatialRepresentation getGridSpatialRepresentation() {
	try {
	    return getGridSpatialRepresentations().next();
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @XPathDirective(target = ".", parent = "gmd:spatialRepresentationInfo", before = "gmd:referenceSystemInfo",
     *                        position = Position.LAST)
     */
    public void addGridSpatialRepresentation(GridSpatialRepresentation grid) {
	MDSpatialRepresentationPropertyType property = new MDSpatialRepresentationPropertyType();
	JAXBElement<? extends AbstractMDSpatialRepresentationType> jaxb = ObjectFactories.GMD()
		.createAbstractMDSpatialRepresentation(grid.getElementType());
	property.setAbstractMDSpatialRepresentation(jaxb);
	getElementType().getSpatialRepresentationInfo().add(property);
    }

    /**
     * @XPathDirective(clear = "gmd:spatialRepresentationInfo[gmd:MD_GridSpatialRepresentation]")
     */
    public void clearGridSpatialRepresentations() {
	getElementType().getSpatialRepresentationInfo().clear();
    }

    public void clearDataQuality() {
	getElementType().getDataQualityInfo().clear();
    }

    public void setDataQuality(DataQuality dq) {
	clearDataQuality();
	addDataQuality(dq);
    }

    public void addDataQuality(DataQuality dq) {
	DQDataQualityPropertyType property = new DQDataQualityPropertyType();
	DQDataQualityType qualityType = dq.getElementType();
	property.setDQDataQuality(qualityType);
	getElementType().getDataQualityInfo().add(property);
    }

    public Iterator<DataQuality> getDataQualities() {
	ArrayList<DataQuality> ret = new ArrayList<>();
	for (DQDataQualityPropertyType dataQuality : getElementType().getDataQualityInfo()) {
	    ret.add(new DataQuality(dataQuality.getDQDataQuality()));
	}
	return ret.iterator();
    }

    DataQuality getDataQuality() {
	return null;
    }

    public MDMetadataType getElementType() {

	return type;
    }

    public JAXBElement<? extends MDMetadataType> getElement() {

	JAXBElement<MDMetadataType> element = ObjectFactories.GMD().createMDMetadata(type);
	return element;
    }

}
