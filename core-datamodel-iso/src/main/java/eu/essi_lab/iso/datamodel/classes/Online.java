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
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gmd.v_20060504.CIOnLineFunctionCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIOnlineResourceType;
import net.opengis.iso19139.gmd.v_20060504.URLPropertyType;
import net.opengis.iso19139.gmx.v_20060504.AnchorType;

/**
 * CI_OnlineResource
 * 
 * @author Fabrizio
 */
public class Online extends ISOMetadata<CIOnlineResourceType> {

    public Online(InputStream stream) throws JAXBException {

	super(stream);
    }

    public Online(CIOnlineResourceType type) {

	super(type);
    }

    public Online() {

	super(new CIOnlineResourceType());
    }

    // --------------------------------------------------------
    //
    // Linkage
    //
    /**
     * @XPathDirective(target = "gmd:name/gco:CharacterString")
     */
    public void setName(String value) {

	type.setName(createCharacterStringPropertyType(value));
    }

    /**
     * @XPathDirective(target = "gmd:name/gco:CharacterString")
     */
    public String getName() {

	try {
	    return ISOMetadata.getStringFromCharacterString(type.getName());

	} catch (NullPointerException ex) {
	}
	return null;
    }

    // --------------------------------------------------------
    //
    // Linkage
    //
    /**
     * @XPathDirective(target = "gmd:linkage/gmd:URL")
     */
    public void setLinkage(String value) {

	URLPropertyType urlPropertyType = new URLPropertyType();
	urlPropertyType.setURL(value);

	type.setLinkage(urlPropertyType);
    }

    /**
     * @XPathDirective(target = "gmd:linkage/gmd:URL")
     */
    public String getLinkage() {

	try {
	    return type.getLinkage().getURL();
	} catch (NullPointerException ex) {
	}

	return null;
    }

    // --------------------------------------------------------
    //
    // Protocol
    //
    /**
     * @XPathDirective(target = "gmd:protocol/gco:CharacterString")
     */
    public void setProtocol(String value) {

	type.setProtocol(createCharacterStringPropertyType(value));
    }

    /**
     * @XPathDirective(target = "gmd:protocol/gco:CharacterString")
     */
    public String getProtocol() {

	try {
	    return ISOMetadata.getStringFromCharacterString(type.getProtocol());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:protocol/gmx:Anchor/@xlink:href,@xlink:title")
     */
    public void setProtocolAnchor(String href, String text) {

	type.setProtocol(createAnchorPropertyType(href, text));
    }

    /**
     * @XPathDirective(target = "gmd:applicationProfile/gco:CharacterString")
     */
    public void setApplicationProfile(String value) {

	type.setApplicationProfile(createCharacterStringPropertyType(value));
    }

    /**
     * @XPathDirective(target = "gmd:applicationProfile/gco:CharacterString")
     */
    public String getApplicationProfile() {

	try {
	    return ISOMetadata.getStringFromCharacterString(type.getApplicationProfile());
	} catch (NullPointerException ex) {
	}

	return null;
    }

    // --------------------------------------------------------
    //
    // Description
    //
    /**
     * @XPathDirective(target = "gmd:description/gco:CharacterString")
     */
    public void setDescription(String value) {

	type.setDescription(createCharacterStringPropertyType(value));
    }

    /**
     * @XPathDirective(target = "gmd:description/gco:CharacterString")
     */
    public String getDescription() {

	try {
	    String value = ISOMetadata.getStringFromCharacterString(type.getDescription());
	    return value.toString();
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:description/gmx:Anchor/@xlink:href")
     */
    public void setDescriptionGmxAnchor(String value) {

	type.setDescription(createAnchorPropertyType(value));
    }
    
    /**
     * @XPathDirective(target = "gmd:description/gmx:Anchor/@xlink:href,@xlink:title")
     */
    public void setDescriptionGmxAnchor(String value, String title) {

	type.setDescription(createAnchorPropertyType(value, title));

    }

    /**
     * @XPathDirective(target = "gmd:description/gmx:Anchor/@*:href")
     */
    public String getDescriptionGmxAnchor() {

	try {
	    JAXBElement<?> jaxbElement = type.getDescription().getCharacterString();
	    @SuppressWarnings("unchecked")
	    JAXBElement<AnchorType> anchor = (JAXBElement<AnchorType>) jaxbElement;
	    return anchor.getValue().getHref();
	} catch (ClassCastException | NullPointerException ex) {
	}

	return null;
    }

    /**
     * @XPathDirective(target = "gmd:protocol/gmx:Anchor/@*:href")
     */
    public String getProtocolGmxAnchor() {

	try {
	    JAXBElement<?> jaxbElement = type.getProtocol().getCharacterString();
	    @SuppressWarnings("unchecked")
	    JAXBElement<AnchorType> anchor = (JAXBElement<AnchorType>) jaxbElement;
	    return anchor.getValue().getHref();
	} catch (ClassCastException | NullPointerException ex) {
	}

	return null;
    }

    // --------------------------------------------------------
    //
    // Function code
    //
    /**
     * @XPathDirective(target = "gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue")
     */
    public void setFunctionCode(String code) {
	CIOnLineFunctionCodePropertyType propertyType = new CIOnLineFunctionCodePropertyType();
	propertyType.setCIOnLineFunctionCode(createCodeListValueType(CI_ON_LINE_FUNCTION_CODE_CODELIST, code, ISO_19115_CODESPACE, code));
	type.setFunction(propertyType);
    }

    /**
     * @XPathDirective(target = "gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue")
     */
    public String getFunctionCode() {

	try {
	    return type.getFunction().getCIOnLineFunctionCode().getCodeListValue();
	} catch (NullPointerException ex) {
	}

	return null;
    }

    public JAXBElement<CIOnlineResourceType> getElement() {

	JAXBElement<CIOnlineResourceType> element = ObjectFactories.GMD().createCIOnlineResource(type);
	return element;
    }

    public String getIdentifier() {

	try {
	    return type.getId();
	} catch (NullPointerException ex) {
	}

	return null;
    }

    /**
     * Sets a random identifier and returns it. The urn:uuid: prefix is needed, as without it is not a valid id (valid
     * identifiers can't begin with a number in the ISO 19115 schema).
     * 
     * @return
     */
    public String setIdentifier() {
	String id = "urn:uuid:" + UUID.randomUUID().toString();
	setIdentifier(id);
	return id;
    }

    public void setIdentifier(String id) {

	type.setId(id);
    }

    @Override
    public String toString() {
	return getLinkage() + " " + getProtocol() + " " + getName();
    }

}
