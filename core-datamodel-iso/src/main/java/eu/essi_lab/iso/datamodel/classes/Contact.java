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
import net.opengis.iso19139.gmd.v_20060504.CIAddressPropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIContactType;
import net.opengis.iso19139.gmd.v_20060504.CIOnlineResourcePropertyType;
import net.opengis.iso19139.gmd.v_20060504.CIOnlineResourceType;
import net.opengis.iso19139.gmd.v_20060504.CITelephonePropertyType;
import net.opengis.iso19139.gmd.v_20060504.CITelephoneType;

public class Contact extends ISOMetadata<CIContactType> {

    public Contact(InputStream stream) throws JAXBException {

	super(stream);
    }

    public Contact(CIContactType type) {

	super(type);
    }

    public Contact() {

	super(new CIContactType());
    }

    @Override
    public JAXBElement<CIContactType> getElement() {

	JAXBElement<CIContactType> element = ObjectFactories.GMD().createCIContact(type);
	return element;
    }

    /**
     * @XPathDirective(target = "gmd:phone/gmd:CI_Telephone//gmd:voice/gco:CharacterString")
     * @return
     */
    public Iterator<String> getPhoneVoices() {
	ArrayList<String> ret = new ArrayList<>();
	if (type.isSetPhone() && type.getPhone().isSetCITelephone() && type.getPhone().getCITelephone().isSetVoice()) {
	    CITelephonePropertyType phoneProperty = type.getPhone();
	    CITelephoneType phone = phoneProperty.getCITelephone();
	    List<CharacterStringPropertyType> voices = phone.getVoice();
	    for (CharacterStringPropertyType voice : voices) {
		if (voice != null && voice.isSetCharacterString()) {
		    ret.add(getStringFromCharacterString(voice));
		}
	    }
	}
	return ret.iterator();
    }

    /**
     * @XPathDirective(target = "gmd:phone/gmd:CI_Telephone", create = "gmd:voice/gco:CharacterString", position =
     *                        Position.FIRST)
     * @param phoneVoice
     */
    public void addPhoneVoice(String phoneVoice) {
	if (!type.isSetPhone()) {
	    type.setPhone(new CITelephonePropertyType());
	}
	if (!type.getPhone().isSetCITelephone()) {
	    type.getPhone().setCITelephone(new CITelephoneType());
	}
	if (!type.getPhone().getCITelephone().isSetVoice()) {
	    type.getPhone().getCITelephone().setVoice(new ArrayList<>());
	}
	type.getPhone().getCITelephone().getVoice().add(createCharacterStringPropertyType(phoneVoice));
    }

    /**
     * @XPathDirective(clear = "gmd:phone/gmd:CI_Telephone//gmd:voice")
     */
    public void clearPhoneVoices() {
	if (type.isSetPhone()) {
	    if (type.getPhone().isSetCITelephone()) {
		if (type.getPhone().getCITelephone().isSetVoice()) {
		    type.getPhone().getCITelephone().unsetVoice();
		}
	    }
	}
    }

    /**
     * @XPathDirective(target = "gmd:phone/gmd:CI_Telephone//gmd:facsimile/gco:CharacterString")
     * @return
     */
    public Iterator<String> getPhoneFaxList() {
	ArrayList<String> ret = new ArrayList<>();
	if (type.isSetPhone() && type.getPhone().isSetCITelephone() && type.getPhone().getCITelephone().isSetFacsimile()) {
	    CITelephonePropertyType phoneProperty = type.getPhone();
	    CITelephoneType phone = phoneProperty.getCITelephone();
	    List<CharacterStringPropertyType> faxes = phone.getFacsimile();
	    for (CharacterStringPropertyType fax : faxes) {
		if (fax.isSetCharacterString()) {
		    ret.add(getStringFromCharacterString(fax));
		}
	    }
	}
	return ret.iterator();
    }

    /**
     * @XPathDirective(target = "gmd:phone/gmd:CI_Telephone", create = "gmd:facsimile/gco:CharacterString", after =
     *                        "gmd:phone/gmd:CI_Telephone/gmd:voice[last()]", position = Position.FIRST)
     * @param phoneFax
     */
    public void addPhoneFax(String phoneFax) {
	if (!type.isSetPhone()) {
	    type.setPhone(new CITelephonePropertyType());
	}
	if (!type.getPhone().isSetCITelephone()) {
	    type.getPhone().setCITelephone(new CITelephoneType());
	}
	if (!type.getPhone().getCITelephone().isSetFacsimile()) {
	    type.getPhone().getCITelephone().setFacsimile(new ArrayList<>());
	}
	type.getPhone().getCITelephone().getFacsimile().add(createCharacterStringPropertyType(phoneFax));

    }

    /**
     * @XPathDirective(clear = "gmd:phone/gmd:CI_Telephone//gmd:facsimile")
     */
    public void clearPhoneFaxList() {
	if (type.isSetPhone()) {
	    if (type.getPhone().isSetCITelephone()) {
		if (type.getPhone().getCITelephone().isSetFacsimile()) {
		    type.getPhone().getCITelephone().unsetFacsimile();
		}
	    }
	}
    }

    /**
     * @XPathDirective(target = "gmd:address/gmd:CI_Address")
     * @return
     */
    public Address getAddress() {
	if (type.isSetAddress() && type.getAddress().isSetCIAddress()) {
	    return new Address(type.getAddress().getCIAddress());
	}
	return null;
    }

    /**
     * @XPathDirective(target = ".", clear = "gmd:address", parent = "gmd:address", after = "gmd:phone", position =
     *                        Position.FIRST)
     * @param value
     */
    public void setAddress(Address value) {
	CIAddressPropertyType addressProperty = null;
	if (value != null) {
	    addressProperty = new CIAddressPropertyType();
	    addressProperty.setCIAddress(value.getElementType());
	}
	type.setAddress(addressProperty);
    }

    /**
     * @XPathDirective(target = ".", parent = "gmd:onlineResource", after = "gmd:address", position = Position.LAST)
     * @param online
     */
    public void setOnline(Online online) {
	if (!type.isSetOnlineResource()) {
	    type.setOnlineResource(new CIOnlineResourcePropertyType());
	}
	CIOnlineResourceType onlineType = null;
	if (online != null) {
	    onlineType = online.getElementType();
	}
	type.getOnlineResource().setCIOnlineResource(onlineType);
    }

    /**
     * @XPathDirective(target = "//gmd:CI_OnlineResource")
     * @return
     */
    public Online getOnline() {
	if (type.isSetOnlineResource()) {
	    if (type.getOnlineResource().isSetCIOnlineResource()) {
		return new Online(type.getOnlineResource().getCIOnlineResource());
	    }
	}
	return null;
    }
}
