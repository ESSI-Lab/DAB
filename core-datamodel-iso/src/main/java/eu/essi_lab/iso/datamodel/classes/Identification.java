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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gco.v_20060504.DatePropertyType;
import net.opengis.iso19139.gmd.v_20060504.AbstractMDIdentificationType;
import net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationType;
import net.opengis.iso19139.gmd.v_20060504.CIDatePropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIDateType;
import net.opengis.iso19139.gmd.v_20060504.CIDateTypeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIResponsiblePartyPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIResponsiblePartyType;
import net.opengis.iso19139.gmd.v_20060504.MDConstraintsPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDConstraintsType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentifierType;
import net.opengis.iso19139.gmd.v_20060504.MDKeywordTypeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDKeywordsPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDKeywordsType;
import net.opengis.iso19139.gmd.v_20060504.MDLegalConstraintsType;
import net.opengis.iso19139.gmd.v_20060504.MDRestrictionCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDSecurityConstraintsType;
import net.opengis.iso19139.gmd.v_20060504.ObjectFactory;

/**
 * AbstractMD_Identification
 *
 * @author Fabrizio
 */
public class Identification extends ISOMetadata<AbstractMDIdentificationType> {

    public static final String PUBLICATION = "publication";
    public static final String REVISION = "revision";
    public static final String CREATION = "creation";

    public Identification(AbstractMDIdentificationType type) {

	this.type = type;
    }

    public Identification(InputStream stream) throws JAXBException {

	this.type = (AbstractMDIdentificationType) fromStream(stream);
    }

    // --------------------------------------------------------
    //
    // Resource identifier
    //

    /**
     * @return
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString")
     */
    public String getResourceIdentifier() {

	try {
	    return getStringFromCharacterString(getFirstCitation().getIdentifier().get(0).getMDIdentifier().getValue().getCode());

	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	}
	return null;
    }

    /**
     * @param identifier
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString")
     */
    public void setResourceIdentifier(String identifier) {

	setResourceIdentifier(identifier, null);

    }

    /**
     * @param identifier
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString")
     */
    public void setResourceIdentifier(String identifier, String authority) {

	if (identifier == null) {
	    getFirstCitation().unsetIdentifier();
	    return;
	}
	MDIdentifierPropertyType mdIdentifierPropertyType = new MDIdentifierPropertyType();

	MDIdentifierType mdIdentifierType = new MDIdentifierType();
	mdIdentifierType.setCode(createCharacterStringPropertyType(identifier));
	if (authority != null) {
	    CICitationPropertyType cpt = new CICitationPropertyType();
	    CICitationType citation = new CICitationType();
	    citation.setTitle(MIMetadata.createCharacterStringPropertyType(authority));
	    cpt.setCICitation(citation);
	    mdIdentifierType.setAuthority(cpt);
	}
	mdIdentifierPropertyType.setMDIdentifier(ObjectFactories.GMD().createMDIdentifier(mdIdentifierType));

	getFirstCitation().getIdentifier().add(mdIdentifierPropertyType);

    }

    // --------------------------------------------------------
    //
    // Keywords
    //

    /**
     * @return
     * @XPathDirective(target = ".//gmd:descriptiveKeywords/gmd:MD_Keywords")
     */
    public Iterator<Keywords> getKeywords() {

	ArrayList<Keywords> out = new ArrayList<>();
	List<MDKeywordsPropertyType> descriptiveKeywords = type.getDescriptiveKeywords();

	for (MDKeywordsPropertyType kwd : descriptiveKeywords) {

	    MDKeywordsType mdKeywords = kwd.getMDKeywords();
	    if (mdKeywords != null) {
		out.add(new Keywords(mdKeywords));
	    }
	}

	return out.iterator();
    }

    public List<String> getKeywordsOfType(String t) {

	ArrayList<String> out = new ArrayList<>();

	List<MDKeywordsPropertyType> descriptiveKeywords = type.getDescriptiveKeywords();
	for (MDKeywordsPropertyType kwd : descriptiveKeywords) {
	    try {
		MDKeywordsType mdKeywords = kwd.getMDKeywords();
		String myType = null;
		MDKeywordTypeCodePropertyType tt = mdKeywords.getType();
		if (tt != null) {
		    CodeListValueType code = tt.getMDKeywordTypeCode();
		    if (code != null) {
			myType = code.getCodeListValue();
		    }
		}
		if (myType != null && myType.equals(t)) {
		    List<CharacterStringPropertyType> keyword = mdKeywords.getKeyword();
		    for (CharacterStringPropertyType kk : keyword) {
			out.add(getStringFromCharacterString(kk));
		    }
		    break;
		}

	    } catch (NullPointerException | IndexOutOfBoundsException ex) {
		// nothing to do here
	    }
	}

	return out;
    }

    public Iterator<String> getKeywords(String thesaurusTitle) {

	ArrayList<String> out = new ArrayList<>();

	List<MDKeywordsPropertyType> descriptiveKeywords = type.getDescriptiveKeywords();
	for (MDKeywordsPropertyType kwd : descriptiveKeywords) {
	    try {
		MDKeywordsType mdKeywords = kwd.getMDKeywords();
		CICitationPropertyType thesaurusName = mdKeywords.getThesaurusName();
		CICitationType ciCitation = thesaurusName.getCICitation();

		String title = getStringFromCharacterString(ciCitation.getTitle());
		if (title.equals(thesaurusTitle)) {
		    List<CharacterStringPropertyType> keyword = mdKeywords.getKeyword();
		    for (CharacterStringPropertyType t : keyword) {
			out.add(getStringFromCharacterString(t));
		    }
		    break;
		}
	    } catch (NullPointerException | IndexOutOfBoundsException ex) {
		// nothing to do here
	    }
	}

	return out.iterator();
    }

    public Iterator<String> getKeywordsValues() {

	ArrayList<String> out = new ArrayList<>();
	Iterator<Keywords> keywords = getKeywords();

	while (keywords.hasNext()) {
	    Keywords kwd = keywords.next();
	    out.addAll(Lists.newArrayList(kwd.getKeywords()));
	}

	return out.iterator();
    }

    /**
     * @param keyword
     * @XPathDirective(target = ".", parent = "gmd:descriptiveKeywords", after = "gmd:abstract")
     */
    public void addKeywords(Keywords keyword) {

	List<MDKeywordsPropertyType> descriptiveKeywords = type.getDescriptiveKeywords();

	MDKeywordsPropertyType mdKeywordsPropertyType = new MDKeywordsPropertyType();
	descriptiveKeywords.add(mdKeywordsPropertyType);

	mdKeywordsPropertyType.setMDKeywords(keyword.getElementType());
    }

    public void addKeyword(String keyword) {

	GSLoggerFactory.getLogger(getClass()).warn("Deprecated.. use addKeywords or reimplement, as this method may cause problems");
	// as it can add keyword to group of keywords from different thesaurii

	List<MDKeywordsPropertyType> descriptiveKeywords = type.getDescriptiveKeywords();
	MDKeywordsPropertyType mdKeywordsPropertyType = null;

	if (!descriptiveKeywords.isEmpty()) {

	    mdKeywordsPropertyType = descriptiveKeywords.get(0);

	} else {

	    mdKeywordsPropertyType = new MDKeywordsPropertyType();
	    descriptiveKeywords.add(mdKeywordsPropertyType);
	}

	MDKeywordsType mdKeywords = mdKeywordsPropertyType.getMDKeywords();
	if (mdKeywords == null) {
	    mdKeywords = new MDKeywordsType();
	    mdKeywordsPropertyType.setMDKeywords(mdKeywords);
	}

	mdKeywords.getKeyword().add(createCharacterStringPropertyType(keyword));
    }

    public Iterator<String> getKeywordTypes() {

	ArrayList<String> out = new ArrayList<>();
	try {
	    List<MDKeywordsPropertyType> descriptiveKeywords = type.getDescriptiveKeywords();
	    for (MDKeywordsPropertyType kwd : descriptiveKeywords) {

		MDKeywordsType mdKeywords = kwd.getMDKeywords();

		MDKeywordTypeCodePropertyType propertyType = mdKeywords.getType();
		String codeListValue = propertyType.getMDKeywordTypeCode().getCodeListValue();
		out.add(codeListValue);
	    }
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	    // nothing to do here
	}

	return out.iterator();
    }

    /**
     * @XPathDirective(clear = "//gmd:descriptiveKeywords")
     */
    public void clearKeywords() {

	type.getDescriptiveKeywords().clear();
    }

    // --------------------------------------------------------
    //
    // Abstract
    //

    /**
     * @param abs
     * @XPathDirective(target = "gmd:abstract/gco:CharacterString")
     */
    public void setAbstract(String abs) {

	type.setAbstract(createCharacterStringPropertyType(abs));
    }

    /**
     * @return
     * @XPathDirective(target = "gmd:abstract/gco:CharacterString")
     */
    public String getAbstract() {

	try {
	    return ISOMetadata.getStringFromCharacterString(type.getAbstract());

	} catch (NullPointerException ex) {
	    // nothing to do here
	}

	return null;
    }

    // --------------------------------------------------------
    //
    // Citation
    //

    public void addCitationDate(String date, String dateType) {

	addCitationDateOrDateTime(date, null, dateType);
    }

    // commented, because ISO 19115 mandates the date type! (see ISO 19115/2003 pag. 34)
    // public void addCitationDate(String date) {
    //
    // addCitationDate(date, null);
    // }

    /**
     * @XPathDirective(clear = "//gmd:citation/gmd:CI_Citation/gmd:date")
     */
    public void clearCitationDates() {

	getFirstCitation().getDate().clear();
    }

    public Iterator<String> getCitationDates() {
	return getCitationDates(null);
    }

    /**
     * @return
     * @XPathDirective(target = "//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date")
     */
    public Iterator<String> getCitationDates(String type) {

	ArrayList<String> list = new ArrayList<>();
	List<CIDatePropertyType> date = getFirstCitation().getDate();
	for (CIDatePropertyType ciDatePropertyType : date) {
	    if (type == null) {
		list.add(ciDatePropertyType.getCIDate().getDate().getDate());
	    } else {
		try {
		    String myType = ciDatePropertyType.getCIDate().getDateType().getCIDateTypeCode().getCodeListValue();
		    if (type.equals(myType)) {
			list.add(ciDatePropertyType.getCIDate().getDate().getDate());
		    }
		} catch (NullPointerException e) {
		    // nothing to do here
		}
	    }
	}

	return list.iterator();
    }

    public void addCitationDateTime(XMLGregorianCalendar dateTime) {

	addCitationDateTime(dateTime, null);
    }

    /**
     * @param altTitle
     * @XPathDirective(target = "gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString")
     */
    public void addCitationAlternateTitle(String altTitle) {

	getFirstCitation().getAlternateTitle().add(createCharacterStringPropertyType(altTitle));
    }

    /**
     * @return
     * @XPathDirective(target = "gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString")
     */
    public String getCitationAlternateTitle() {

	try {
	    return ISOMetadata.getStringFromCharacterString(getFirstCitation().getAlternateTitle().get(0));
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	    // nothing to do here
	}
	return null;
    }

    /**
     * @return
     * @XPathDirective(target = "gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString")
     */
    public void clearCitationAlternateTitles() {

	try {
	    getFirstCitation().unsetAlternateTitle();
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	    // nothing to do here
	}
    }

    /**
     * @param title
     * @XPathDirective(target = "gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString")
     */
    public void setCitationTitle(String title) {

	getFirstCitation().setTitle(createCharacterStringPropertyType(title));
    }

    /**
     * @return
     * @XPathDirective(target = "gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString")
     */
    public String getCitationTitle() {

	try {
	    return ISOMetadata.getStringFromCharacterString(getFirstCitation().getTitle());
	} catch (NullPointerException ex) {
	    // nothing to do here
	}

	return null;
    }

    /**
     * @param titleString
     * @XPathDirective(target = "gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString")
     */
    public void setCitationAlternateTitle(String titleString) {
	setCitationAlternateTitle(new String[] { titleString });
    }

    /**
     * @param titlesString
     * @XPathDirective(target = "gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString")
     */
    public void setCitationAlternateTitle(String[] titlesString) {
	List<CharacterStringPropertyType> titles = new ArrayList<>();
	for (String titleString : titlesString) {
	    titles.add(createCharacterStringPropertyType(titleString));
	}
	getFirstCitation().setAlternateTitle(titles);
    }

    /**
     * @param date
     * @XPathDirective(target = "gmd:citation/gmd:CI_Citation/gmd:editionDate/gco:Date")
     */
    public void setCitationEditionDate(String date) {

	DatePropertyType datePropertyType = new DatePropertyType();
	datePropertyType.setDate(date);

	getFirstCitation().setEditionDate(datePropertyType);
    }

    /**
     * @return
     * @XPathDirective(target = "gmd:citation/gmd:CI_Citation/gmd:editionDate/gco:Date")
     */
    public String getCitationEditionDate() {

	try {
	    return getFirstCitation().getEditionDate().getDate();
	} catch (NullPointerException ex) {
	    // nothing to do here
	}

	return null;
    }

    /**
     * @return
     * @XPathDirective(target =
     *                        "(.//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:DateTime)[1]")
     */
    public XMLGregorianCalendar getCitationPublicationDateTime() {

	return (XMLGregorianCalendar) getCitationDateTime(PUBLICATION);
    }

    /**
     * @return
     * @XPathDirective(target =
     *                        "(.//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:Date)[1]")
     */
    public String getCitationPublicationDate() {

	Object out = getCitationDate(PUBLICATION);
	if (out != null) {
	    return out.toString();
	}
	return null;
    }

    /**
     * @return
     * @XPathDirective(target =
     *                        "(.//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation'
     *                        or gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='created']/gmd:date/gco:DateTime)[1]")
     */
    public XMLGregorianCalendar getCitationCreationDateTime() {

	return (XMLGregorianCalendar) getCitationDateTime(CREATION);
    }

    /**
     * @return
     * @XPathDirective(target =
     *                        "(.//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation'
     *                        or gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='created']/gmd:date/gco:Date)[1]")
     */
    public String getCitationCreationDate() {

	Object out = getCitationDate(CREATION);
	if (out != null) {
	    return out.toString();
	}
	return null;
    }

    public void setCitationRevisionDate(String date) {

	setCitationDateOrDateTime(date, null, REVISION);
    }

    public void setCitationCreationDate(String date) {

	setCitationDateOrDateTime(date, null, CREATION);
    }

    public void setCitationPublicationDate(String date) {

	setCitationDateOrDateTime(date, null, PUBLICATION);
    }

    public void setCitationRevisionDateTime(XMLGregorianCalendar calendar) {

	setCitationDateOrDateTime(null, calendar, REVISION);
    }

    public void setCitationCreationDateTime(XMLGregorianCalendar calendar) {

	setCitationDateOrDateTime(null, calendar, CREATION);
    }

    public void setCitationPublicationDateTime(XMLGregorianCalendar calendar) {

	setCitationDateOrDateTime(null, calendar, PUBLICATION);
    }

    public List<ResponsibleParty> getCitationResponsibleParties() {
	List<ResponsibleParty> ret = new ArrayList<>();
	for (CIResponsiblePartyPropertyType responsibleParty : type.getCitation().getCICitation().getCitedResponsibleParty()) {
	    CIResponsiblePartyType party = responsibleParty.getCIResponsibleParty();
	    ret.add(new ResponsibleParty(party));
	}
	return ret;
    }

    public void addCitationResponsibleParty(ResponsibleParty party) {
	CIResponsiblePartyPropertyType responsiblePartyProperty = new CIResponsiblePartyPropertyType();
	responsiblePartyProperty.setCIResponsibleParty(party.getElementType());

	CICitationPropertyType citation = type.getCitation();
	if (citation == null) {
	    citation = new CICitationPropertyType();
	    type.setCitation(citation);
	}

	CICitationType ciCitation = citation.getCICitation();
	if (ciCitation == null) {
	    ciCitation = new CICitationType();
	    citation.setCICitation(ciCitation);
	}

	ciCitation.getCitedResponsibleParty().add(responsiblePartyProperty);
    }

    public void clearCitationResponsibleParty() {
	type.getCitation().getCICitation().getCitedResponsibleParty().clear();
    }

    /**
     * @param pointOfContact
     * @XPathDirective(target = ".", parent = "gmd:pointOfContact", after = "gmd:status", position = Position.LAST)
     */
    public void addPointOfContact(ResponsibleParty pointOfContact) {
	CIResponsiblePartyPropertyType responsiblePartyProperty = new CIResponsiblePartyPropertyType();
	responsiblePartyProperty.setCIResponsibleParty(pointOfContact.getElementType());
	type.getPointOfContact().add(responsiblePartyProperty);
    }

    public ResponsibleParty getPointOfContact() {
	if (!type.getPointOfContact().isEmpty() && type.getPointOfContact().get(0).isSetCIResponsibleParty()) {
	    return new ResponsibleParty(type.getPointOfContact().get(0).getCIResponsibleParty());
	}
	return null;
    }

    public Iterator<ResponsibleParty> getPointOfContacts() {

	ArrayList<ResponsibleParty> out = new ArrayList<>();
	List<CIResponsiblePartyPropertyType> pointOfContact = type.getPointOfContact();
	for (CIResponsiblePartyPropertyType ciResponsiblePartyPropertyType : pointOfContact) {

	    if (ciResponsiblePartyPropertyType.isSetCIResponsibleParty()) {
		out.add(new ResponsibleParty(ciResponsiblePartyPropertyType.getCIResponsibleParty()));
	    }
	}

	return out.iterator();
    }

    public ResponsibleParty getPointOfContact(String role) {
	if (!type.getPointOfContact().isEmpty()) {
	    for (CIResponsiblePartyPropertyType poc : type.getPointOfContact()) {
		if (poc.isSetCIResponsibleParty()) {
		    CIResponsiblePartyType party = poc.getCIResponsibleParty();
		    if (party != null && party.getRole() != null && party.getRole().isSetCIRoleCode()
			    && party.getRole().getCIRoleCode().isSetCodeListValue()
			    && party.getRole().getCIRoleCode().getCodeListValue().equals(role)) {
			return new ResponsibleParty(party);

		    }
		}
	    }
	}
	return null;
    }

    public void clearPointOfContacts() {
	type.unsetPointOfContact();
    }

    /**
     * @return
     * @XPathDirective(target =
     *                        "(.//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:DateTime)[1]")
     */
    public XMLGregorianCalendar getCitationRevisionDateTime() {

	return getCitationDateTime(REVISION);
    }

    /**
     * @return
     * @XPathDirective(target =
     *                        "(.//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date)[1]")
     */
    public String getCitationRevisionDate() {

	Object out = getCitationDate(REVISION);
	if (out != null) {
	    return out.toString();
	}
	return null;
    }

    //
    // ***************************************************************************

    // ****************************************
    // LEGAL and SECURITY ACCESS constraints
    // *************************************

    /**
     * @return
     * @XPathDirective(target = ".//gmd:resourceConstraints/gmd:MD_LegalConstraints")
     */
    public Iterator<LegalConstraints> getLegalConstraints() {
	ArrayList<LegalConstraints> constraints = new ArrayList<>();
	Iterator<MDConstraintsPropertyType> iterator = type.getResourceConstraints().iterator();
	while (iterator.hasNext()) {
	    MDConstraintsPropertyType mdConstraintsPropertyType = (MDConstraintsPropertyType) iterator.next();
	    if (mdConstraintsPropertyType.isSetMDConstraints()) {
		MDConstraintsType constraint = mdConstraintsPropertyType.getMDConstraints().getValue();
		if (constraint instanceof MDLegalConstraintsType) {
		    MDLegalConstraintsType legalConstraint = (MDLegalConstraintsType) constraint;
		    constraints.add(new LegalConstraints(legalConstraint));
		}
	    }
	}
	return constraints.iterator();
    }

    /**
     * @param rc
     * @XPathDirective(target = ".", parent = "gmd:resourceConstraints", after = "gmd:abstract")
     */
    public void addLegalConstraints(LegalConstraints rc) {
	MDConstraintsPropertyType constraintProperty = new MDConstraintsPropertyType();
	ObjectFactory factory = new ObjectFactory();
	constraintProperty.setMDConstraints(factory.createMDLegalConstraints(rc.getElementType()));
	type.getResourceConstraints().add(constraintProperty);
    }

    public void addAccessConstraint(String restrictionCode) {

	MDLegalConstraintsType lcType = new MDLegalConstraintsType();
	List<MDRestrictionCodePropertyType> codeList = new ArrayList<>();
	MDRestrictionCodePropertyType codeProperty = new MDRestrictionCodePropertyType();
	CodeListValueType clvt = createCodeListValueType(MD_RESTRICTION_CODE_CODELIST, restrictionCode, ISO_19115_CODESPACE,
		restrictionCode);
	codeProperty.setMDRestrictionCode(clvt);
	codeList.add(codeProperty);
	lcType.setAccessConstraints(codeList);
	LegalConstraints lc = new LegalConstraints(lcType);
	addLegalConstraints(lc);
    }

    /**
     * @XPathDirective(target = ".", parent = "gmd:resourceConstraints", after = "gmd:abstract")
     */
    public void clearResourceConstraints() {
	type.unsetResourceConstraints();
    }

    /**
     * @return
     * @XPathDirective(target = "exists(.//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useConstraints)")
     */
    public boolean hasUseLegalConstraints() {
	Iterator<LegalConstraints> constraints = getLegalConstraints();
	while (constraints.hasNext()) {
	    LegalConstraints legalConstraints = (LegalConstraints) constraints.next();
	    if (legalConstraints.getUseConstraintsCode() != null) {
		return true;
	    }
	}
	return false;
    }

    /**
     * @return
     * @XPathDirective(target = "exists(.//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints)")
     */
    public boolean hasAccessLegalConstraints() {
	Iterator<LegalConstraints> constraints = getLegalConstraints();
	while (constraints.hasNext()) {
	    LegalConstraints legalConstraints = constraints.next();
	    if (legalConstraints.getAccessConstraintCode() != null) {
		return true;
	    }
	}
	return false;
    }

    /**
     * @return
     * @XPathDirective(target = "exists(.//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints)")
     */
    public boolean hasOtherLegalConstraints() {
	Iterator<LegalConstraints> constraints = getLegalConstraints();
	while (constraints.hasNext()) {
	    LegalConstraints legalConstraints = constraints.next();
	    if (legalConstraints.getOtherConstraints().hasNext()) {
		return true;
	    }
	}
	return false;
    }

    /**
     * LEGAL USE LIMITATION CODE
     */
    /**
     * @return
     * @XPathDirective(target =
     *                        ".//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gmd:MD_RestrictionCode/@codeListValue")
     */
    public Iterator<String> getLegalConstraintsUseLimitations() {
	List<String> ret = new ArrayList<>();
	Iterator<LegalConstraints> constraints = getLegalConstraints();
	while (constraints.hasNext()) {
	    LegalConstraints legalConstraints = (LegalConstraints) constraints.next();
	    if (legalConstraints.getUseLimitation() != null) {
		ArrayList<String> list = Lists.newArrayList(legalConstraints.getUseLimitations());
		ret.addAll(list);
	    }
	}
	return ret.iterator();
    }

    /**
     * LEGAL ACCESS CODE
     */
    /**
     * @return
     * @XPathDirective(target =
     *                        ".//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue")
     */
    public Iterator<String> getLegalConstraintsAccessCodes() {
	List<String> ret = new ArrayList<>();
	Iterator<LegalConstraints> constraints = getLegalConstraints();
	while (constraints.hasNext()) {
	    LegalConstraints legalConstraints = (LegalConstraints) constraints.next();
	    if (legalConstraints.getAccessConstraintCode() != null) {
		ArrayList<String> list = Lists.newArrayList(legalConstraints.getAccessConstraintCodes());
		ret.addAll(list);
	    }
	}
	return ret.iterator();
    }

    /**
     * LEGAL USE CODE
     */
    /**
     * @return
     * @XPathDirective(target =
     *                        ".//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue")
     */
    public Iterator<String> getLegalConstraintsUseCodes() {
	List<String> ret = new ArrayList<>();
	Iterator<LegalConstraints> constraints = getLegalConstraints();
	while (constraints.hasNext()) {
	    LegalConstraints legalConstraints = (LegalConstraints) constraints.next();
	    if (legalConstraints.getUseConstraintsCode() != null) {
		ArrayList<String> list = Lists.newArrayList(legalConstraints.getUseConstraintsCodes());
		ret.addAll(list);
	    }
	}
	return ret.iterator();
    }

    /**
     * LEGAL OTHER CODE
     */
    /**
     * @return
     * @XPathDirective(target =
     *                        ".//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gmd:MD_RestrictionCode/@codeListValue")
     */
    public Iterator<String> getLegalConstraintsOthers() {
	List<String> ret = new ArrayList<>();
	Iterator<LegalConstraints> constraints = getLegalConstraints();
	while (constraints.hasNext()) {
	    LegalConstraints legalConstraints = (LegalConstraints) constraints.next();
	    if (legalConstraints.getOtherConstraint() != null) {
		ArrayList<String> list = Lists.newArrayList(legalConstraints.getOtherConstraints());
		ret.addAll(list);
	    }
	}
	return ret.iterator();
    }

    /**
     * @return
     * @XPathDirective(target = "exists(.//gmd:resourceConstraints/gmd:MD_SecurityConstraints)")
     */
    public boolean hasSecurityConstraints() {
	Iterator<MDConstraintsPropertyType> iterator = type.getResourceConstraints().iterator();
	while (iterator.hasNext()) {
	    MDConstraintsPropertyType mdConstraintsPropertyType = (MDConstraintsPropertyType) iterator.next();
	    if (mdConstraintsPropertyType.isSetMDConstraints()) {
		MDConstraintsType constraint = mdConstraintsPropertyType.getMDConstraints().getValue();
		if (constraint instanceof MDSecurityConstraintsType) {
		    return true;
		}
	    }
	}
	return false;
    }

    private void setCitationDateOrDateTime(String date, XMLGregorianCalendar dateTime, String dateType) {

	CICitationType firstCitation = getFirstCitation();

	CIDateTypeCodePropertyType propertyType = new CIDateTypeCodePropertyType();
	CodeListValueType codeListValueType = createCodeListValueType(CI_DATE_TYPE_CODE_CODELIST, dateType, ISO_19115_CODESPACE, dateType);
	propertyType.setCIDateTypeCode(codeListValueType);

	CIDateType ciDateType = new CIDateType();
	ciDateType.setDateType(propertyType);

	DatePropertyType datePropertyType = new DatePropertyType();
	if (dateTime != null) {
	    datePropertyType.setDateTime(dateTime);
	} else {
	    datePropertyType.setDate(date);
	}

	CIDatePropertyType type = new CIDatePropertyType();
	ciDateType.setDate(datePropertyType);
	type.setCIDate(ciDateType);

	CIDatePropertyType datePropertyToRemove = null;
	List<CIDatePropertyType> dates = firstCitation.getDate();
	for (CIDatePropertyType dateProperty : dates) {
	    try {
		if (dateProperty.getCIDate().getDateType().getCIDateTypeCode().getCodeListValue().equals(dateType)) {
		    datePropertyToRemove = dateProperty;
		}
	    } catch (NullPointerException ex) {
		// nothing to do here
	    }
	}
	if (datePropertyToRemove != null) {
	    firstCitation.getDate().remove(datePropertyToRemove);
	}
	firstCitation.getDate().add(type);
    }

    // commented, because ISO 19115 mandates the date type! (see ISO 19115/2003 pag. 34)
    // public void addCitationDate(String date) {
    //
    // addCitationDate(date, null);
    // }

    private void addCitationDateTime(XMLGregorianCalendar dateTime, String dateType) {

	addCitationDateOrDateTime(null, dateTime, dateType);
    }

    private void addCitationDateOrDateTime(String date, XMLGregorianCalendar dateTime, String type) {

	CIDatePropertyType ciDatePropertyType = new CIDatePropertyType();
	CIDateType ciDateType = new CIDateType();

	if (type != null) {
	    CIDateTypeCodePropertyType ciDateTypeCodePropertyType = new CIDateTypeCodePropertyType();
	    ciDateTypeCodePropertyType
		    .setCIDateTypeCode(createCodeListValueType(CI_DATE_TYPE_CODE_CODELIST, type, ISO_19115_CODESPACE, type));
	    ciDateType.setDateType(ciDateTypeCodePropertyType);
	}

	DatePropertyType datePropertyType = new DatePropertyType();
	if (dateTime != null) {
	    datePropertyType.setDateTime(dateTime);
	}
	if (date != null) {
	    datePropertyType.setDate(date);
	}

	ciDateType.setDate(datePropertyType);
	ciDatePropertyType.setCIDate(ciDateType);

	getFirstCitation().getDate().add(ciDatePropertyType);
    }

    private String getCitationDate(String codeListValue) {

	try {
	    List<CIDatePropertyType> date = getFirstCitation().getDate();
	    for (CIDatePropertyType ciDatePropertyType : date) {

		CIDateTypeCodePropertyType dateType = ciDatePropertyType.getCIDate().getDateType();
		String currentCLV = dateType.getCIDateTypeCode().getCodeListValue();

		if (currentCLV.equals(codeListValue)) {
		    DatePropertyType myDate = ciDatePropertyType.getCIDate().getDate();
		    if (myDate != null) {
			if (myDate.getDate() != null) {
			    return myDate.getDate();
			} else if (myDate.getDateTime() != null) {
			    return myDate.getDateTime().toString();
			}
		    }
		}
	    }
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	    // nothing to do here
	}

	return null;
    }

    private XMLGregorianCalendar getCitationDateTime(String codeListValue) {

	try {
	    List<CIDatePropertyType> date = getFirstCitation().getDate();
	    for (CIDatePropertyType ciDatePropertyType : date) {

		CIDateTypeCodePropertyType dateType = ciDatePropertyType.getCIDate().getDateType();
		String currentCLV = dateType.getCIDateTypeCode().getCodeListValue();

		if (currentCLV.equals(codeListValue)) {
		    DatePropertyType myDate = ciDatePropertyType.getCIDate().getDate();
		    if (myDate != null) {
			return myDate.getDateTime();
		    }
		}
	    }
	} catch (NullPointerException | IndexOutOfBoundsException ex) {
	    // nothing to do here
	}

	return null;
    }

    private CICitationType getFirstCitation() {

	CICitationPropertyType citation = type.getCitation();
	if (citation == null) {
	    citation = new CICitationPropertyType();
	    type.setCitation(citation);
	}

	CICitationType ciCitation = citation.getCICitation();
	if (ciCitation == null) {
	    ciCitation = new CICitationType();
	    citation.setCICitation(ciCitation);
	}

	return ciCitation;
    }

    /**
     * @return
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString")
     */
    String getCitationRS_Code() {
	return null;
    }

    /**
     * @return
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString")
     */
    String getCitationRS_CodeSpace() {
	return null;
    }

    /**
     * @param code
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString")
     */
    void setCitationRS_Code(String code) {
    }

    /**
     * @param codeSpace
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString")
     */
    void setCitationRS_CodeSpace(String codeSpace) {
    }

    /**
     * @return
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/*:MD_Identifier/gmd:code/gco:CharacterString")
     */
    String getCitationMD_Code() {
	return null;
    }

    /**
     * @param code
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString")
     */
    void setCitationMD_Code(String code) {
    }

    /**
     * @return
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/*:MD_Identifier/gmd:authority/gco:CharacterString")
     */
    String getCitationMD_Authority() {
	return null;
    }

    /**
     * @param authority
     * @XPathDirective(target =
     *                        "gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:authority/gco:CharacterString")
     */
    void setCitationMD_Authority(String authority) {
    }

    void setCitation(String title, String alternatetTile, String date) {
    }

    //
    // ***************************************************************************

    // ****************************************
    // LEGAL and SECURITY ACCESS constraints
    // *************************************

}
