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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.jaxb.common.ObjectFactories;
import net.opengis.iso19139.gmd.v_20060504.CIContactPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIResponsiblePartyType;
import net.opengis.iso19139.gmd.v_20060504.CIRoleCodePropertyType;

public class ResponsibleParty extends ISOMetadata<CIResponsiblePartyType> {

    public ResponsibleParty(InputStream stream) throws JAXBException {

	super(stream);
    }

    public ResponsibleParty(CIResponsiblePartyType type) {

	super(type);
    }

    public ResponsibleParty() {

	super(new CIResponsiblePartyType());
    }

    @Override
    public JAXBElement<CIResponsiblePartyType> getElement() {

	JAXBElement<CIResponsiblePartyType> element = ObjectFactories.GMD().createCIResponsibleParty(type);
	return element;
    }

    public String getOrganisationURI() {
	try {
	    return ISOMetadata.getHREFStringFromCharacterString(type.getOrganisationName());
	} catch (NullPointerException ex) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:organisationName/*[1]")
     */
    public String getOrganisationName() {
	try {
	    return ISOMetadata.getStringFromCharacterString(type.getOrganisationName());
	} catch (NullPointerException ex) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:organisationName/gco:CharacterString")
     */
    public void setOrganisationName(String organisationName) {
	type.setOrganisationName(createCharacterStringPropertyType(organisationName));
    }

    /**
     * @XPathDirective(target = "gmd:individualName/*[1]")
     */
    public String getIndividualName() {
	try {
	    return ISOMetadata.getStringFromCharacterString(type.getIndividualName());
	} catch (NullPointerException ex) {
	}
	return null;
    }

    public String getIndividualURI() {
	try {
	    return ISOMetadata.getHREFStringFromCharacterString(type.getIndividualName());
	} catch (NullPointerException ex) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:individualName/gco:CharacterString")
     */
    public void setIndividualName(String individualName) {
	type.setIndividualName(createCharacterStringPropertyType(individualName));
    }

    /**
     * @XPathDirective(target = "gmd:positionName/*[1]")
     */
    public String getPositionName() {
	try {
	    return ISOMetadata.getStringFromCharacterString(type.getPositionName());
	} catch (NullPointerException ex) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = "gmd:positionName/gco:CharacterString")
     */
    public void setPositionName(String positionName) {
	type.setPositionName(createCharacterStringPropertyType(positionName));
    }

    /**
     * @XPathDirective(target = ".", clear = "gmd:role", parent = "gmd:role", after
     *                        = "gmd:contactInfo", position = Position.LAST)
     */
    public void setRoleCode(String roleCode) {
	CIRoleCodePropertyType roleCodeProperty = new CIRoleCodePropertyType();
	roleCodeProperty.setCIRoleCode(createCodeListValueType(CI_ROLE_CODE_CODELIST, roleCode, ISO_19115_CODESPACE, roleCode));
	type.setRole(roleCodeProperty);
    }

    /**
     * @XPathDirective(target = "gmd:role/gmd:CI_RoleCode")
     */
    public String getRoleCode() {
	try {
	    return type.getRole().getCIRoleCode().getCodeListValue();
	} catch (NullPointerException ex) {
	}
	return null;
    }

    /**
     * @XPathDirective(target = ".", clear = "gmd:contactInfo", parent =
     *                        "gmd:contactInfo", after = "gmd:positionName",
     *                        position = Position.LAST)
     */
    public void setContactInfo(Contact contact) {
	if (contact == null) {
	    type.setContactInfo(null);
	    return;
	}
	CIContactPropertyType contactProperty = new CIContactPropertyType();
	contactProperty.setCIContact(contact.getElementType());
	type.setContactInfo(contactProperty);
    }

    /**
     * @XPathDirective(target = "gmd:contactInfo/gmd:CI_Contact")
     */
    public Contact getContact() {
	if (type.isSetContactInfo() && type.getContactInfo().isSetCIContact()) {
	    return new Contact(type.getContactInfo().getCIContact());
	}
	return null;
    }
}
