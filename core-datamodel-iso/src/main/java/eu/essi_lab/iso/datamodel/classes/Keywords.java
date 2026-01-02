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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CICitationType;
import net.opengis.iso19139.gmd.v_20060504.MDKeywordTypeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDKeywordsType;

/**
 * MD_Keywords
 * 
 * @author Fabrizio
 */
public class Keywords extends ISOMetadata<MDKeywordsType> {

    public Keywords(InputStream stream) throws JAXBException {

	super(stream);
    }

    public Keywords(MDKeywordsType type) {

	super(type);
    }

    public Keywords() {

	super(new MDKeywordsType());
    }

    // --------------------------------------------------------
    //
    // Keywords
    //
    /**
     * @XPathDirective(target = ".//gmd:keyword")
     * @return
     */
    public Iterator<String> getKeywords() {

	ArrayList<String> out = new ArrayList<String>();
	List<CharacterStringPropertyType> keywordTypes = type.getKeyword();
	for (CharacterStringPropertyType keywordType : keywordTypes) {
	    String keyword = getStringFromCharacterString(keywordType);
	    if (keyword != null) {
		out.add(keyword);
	    }
	}

	return out.iterator();
    }

    /**
     * @XPathDirective(target = ".", position = Position.FIRST)
     * @param value
     */
    public void addKeyword(String value) {

	List<CharacterStringPropertyType> keyword = type.getKeyword();
	keyword.add(createCharacterStringPropertyType(value));
    }
    
    /**
     * @XPathDirective(target = ".", position = Position.FIRST)
     * @param label
     */
    public void addKeyword(String label,String href) {

	List<CharacterStringPropertyType> keyword = type.getKeyword();
	keyword.add(createAnchorPropertyType(href, label));
    }

    /**
     * @XPathDirective(target = ".", position = Position.LAST)
     * @param keywords
     */
    public void addKeywords(List<String> keywords) {

	for (String kwd : keywords) {
	    addKeyword(kwd);
	}
    }

    public void clearKeywords() {

	type.getKeyword().clear();
    }

    // --------------------------------------------------------
    //
    // Thesaurus
    //

    public void setThesaurusCitation(Citation citation) {
	CICitationPropertyType thesaurusCitation = type.getThesaurusName();

	if (thesaurusCitation == null) {
	    thesaurusCitation = new CICitationPropertyType();
	    type.setThesaurusName(thesaurusCitation);
	}

	thesaurusCitation.setCICitation(citation.getElementType());

    }

    public Citation getThesaurusCitation() {

	CICitationPropertyType thesaurusCitation = type.getThesaurusName();
	CICitationType ciCitationType = null;

	if (thesaurusCitation == null) {
	    thesaurusCitation = new CICitationPropertyType();
	    type.setThesaurusName(thesaurusCitation);
	}

	ciCitationType = thesaurusCitation.getCICitation();

	if (ciCitationType == null) {
	    ciCitationType = new CICitationType();
	    thesaurusCitation.setCICitation(ciCitationType);
	}

	return new Citation(ciCitationType);
    }

    /**
     * @XPathDirective(target = "gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString")
     * @param title
     */
    public void setThesaurusNameCitationTitle(String title) {

	getThesaurusCitation().setTitle(title);
    }

    /**
     * @XPathDirective(target = "gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString")
     * @return
     */
    public String getThesaurusNameCitationTitle() {

	return getThesaurusCitation().getTitle();
    }

    /**
     * @XPathDirective(target = "gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString")
     * @param title
     */
    public void addThesaurusNameCitationAlternateTitle(String title) {

	getThesaurusCitation().addAlternateTitle(title);
    }

    /**
     * @XPathDirective(target = "gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString")
     * @return
     */
    public Iterator<String> getThesaurusNameCitationAlternateTitles() {

	try {
	    CICitationPropertyType thesaurusName = type.getThesaurusName();
	    CICitationType ciCitation = thesaurusName.getCICitation();
	    List<CharacterStringPropertyType> alternateTitles = ciCitation.getAlternateTitle();
	    List<String> strings = new ArrayList<>();
	    for (CharacterStringPropertyType alternateTitle : alternateTitles) {
		strings.add(ISOMetadata.getStringFromCharacterString(alternateTitle));
	    }
	    return strings.iterator();

	} catch (IndexOutOfBoundsException | NullPointerException ex) {
	}

	return null;
    }

    public void setThesaurusDate(String date, String dateType) {

	getThesaurusCitation().addDate(date, dateType);
    }

    // --------------------------------------------------------
    //
    // Type
    //
    //
    /**
     * @XPathDirective(target = ".", parent = "gmd:type", after = "gmd:keyword[last()]", before = "gmd:thesaurusName",
     *                        position = Position.LAST)
     * @param keywordType
     */
    public void setTypeCode(String keywordType) {

	MDKeywordTypeCodePropertyType propertyType = new MDKeywordTypeCodePropertyType();
	propertyType.setMDKeywordTypeCode(
		createCodeListValueType(MD_KEYWORD_TYPE_CODE_CODELIST, keywordType, ISO_19115_CODESPACE, keywordType));

	type.setType(propertyType);
    }

    public String getTypeCode() {

	try {
	    return type.getType().getMDKeywordTypeCode().getCodeListValue();

	} catch (NullPointerException ex) {
	}

	return null;
    }

    @Override
    public JAXBElement<MDKeywordsType> getElement() {

	JAXBElement<MDKeywordsType> element = ObjectFactories.GMD().createMDKeywords(type);
	return element;
    }

}
