package eu.essi_lab.iso.datamodel.classes;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import net.opengis.iso19139.gmd.v_20060504.CIAddressType;

public class Address extends ISOMetadata<CIAddressType> {

    public Address(InputStream stream) throws JAXBException {

	super(stream);
    }

    public Address(CIAddressType type) {

	super(type);
    }

    public Address() {

	super(new CIAddressType());
    }

    @Override
    public JAXBElement<CIAddressType> getElement() {

	JAXBElement<CIAddressType> element = ObjectFactories.GMD().createCIAddress(type);
	return element;
    }
    public void addElectronicMailAddress(String electronicMailAddress) {
	type.getElectronicMailAddress().add(createCharacterStringPropertyType(electronicMailAddress));
    }

    /**
     * gmd:deliveryPoint/gco:CharacterString
     * 
     * @param deliveryPoint
     */
    public void addDeliveryPoint(String deliveryPoint) {
	type.getDeliveryPoint().add(createCharacterStringPropertyType(deliveryPoint));
    }

    /**
     * //gmd:deliveryPoint/gco:CharacterString
     * 
     * @return
     */

    public String getDeliveryPoint() {
	if (type.isSetDeliveryPoint() && !type.getDeliveryPoint().isEmpty() && type.getDeliveryPoint().get(0).isSetCharacterString()) {
	    return getStringFromCharacterString(type.getDeliveryPoint().get(0));
	}
	return null;
    }

    /**
     * @XPathDirective(target = "//gmd:deliveryPoint/gco:CharacterString")
     */
    public void clearDeliveryPoints() {
	type.unsetDeliveryPoint();
    }

    /**
     * @XPathDirective(target = ".", create = "gmd:city/gco:CharacterString", after = "gmd:deliveryPoint", position =
     *                        Position.FIRST)
     * @param city
     */
    public void setCity(String city) {
	type.setCity(createCharacterStringPropertyType(city));
    }

    /**
     * @XPathDirective(target = "//gmd:city/gco:CharacterString")
     * @return
     */
    public String getCity() {
	if (type.isSetCity() && type.getCity().isSetCharacterString()) {
	    return getStringFromCharacterString(type.getCity());
	}
	return null;
    }

    /**
     * @XPathDirective(target = ".", create = "gmd:administrativeArea/gco:CharacterString", after = "gmd:deliveryPoint
     *                        gmd:city", position = Position.FIRST)
     * @param administrativeArea
     */
    public void setAdministrativeArea(String administrativeArea) {
	type.setAdministrativeArea(createCharacterStringPropertyType(administrativeArea));
    }

    /**
     * @XPathDirective(target = "//gmd:administrativeArea/gco:CharacterString")
     * @return
     */
    public String getAdministrativeArea() {
	if (type.isSetAdministrativeArea() && type.getAdministrativeArea().isSetCharacterString()) {
	    return getStringFromCharacterString(type.getAdministrativeArea());
	}
	return null;
    }

    /**
     * @XPathDirective(target = ".", create = "gmd:postalCode/gco:CharacterString", after = "gmd:deliveryPoint gmd:city
     *                        gmd:administrativeArea", position = Position.FIRST)
     * @param postalCode
     */
    public void setPostalCode(String postalCode) {
	type.setPostalCode(createCharacterStringPropertyType(postalCode));
    }

    /**
     * @XPathDirective(target = "//gmd:postalCode/gco:CharacterString")
     * @return
     */
    public String getPostalCode() {
	if (type.isSetPostalCode() && type.getPostalCode().isSetCharacterString()) {
	    return getStringFromCharacterString(type.getPostalCode());
	}
	return null;
    }

    /**
     * @XPathDirective(target = ".", create = "gmd:country/gco:CharacterString", after = "gmd:deliveryPoint gmd:city
     *                        gmd:administrativeArea gmd:postalCode", position = Position.FIRST)
     * @param country
     */
    public void setCountry(String country) {
	type.setCountry(createCharacterStringPropertyType(country));
    }

    /**
     * @XPathDirective(target = "//gmd:country/gco:CharacterString")
     * @return
     */
    public String getCountry() {
	if (type.isSetCountry()) {
	    return getStringFromCharacterString(type.getCountry());
	}
	return null;
    }

    public String getElectronicMailAddress() {
	if (type.isSetElectronicMailAddress() && !type.getElectronicMailAddress().isEmpty()
		&& type.getElectronicMailAddress().get(0) != null && type.getElectronicMailAddress().get(0).isSetCharacterString()) {
	    return getStringFromCharacterString(type.getElectronicMailAddress().get(0));
	}
	return null;
    }

    /**
     * @XPathDirective(clear = "//gmd:electronicMailAddress")
     */
    public void clearElectronicMailAddresses() {
	type.unsetElectronicMailAddress();
    }

    /**
     * @XPathDirective(target = "//gmd:electronicMailAddress/gco:CharacterString")
     * @return
     */
    public Iterator<String> getElectronicMailAddresses() {
	ArrayList<String> ret = new ArrayList<>();
	if (type.isSetElectronicMailAddress()) {
	    List<CharacterStringPropertyType> addresses = type.getElectronicMailAddress();
	    for (CharacterStringPropertyType address : addresses) {
		String str = getStringFromCharacterString(address);
		if (str != null) {
		    ret.add(str);
		}
	    }
	}
	return ret.iterator();
    }

}
