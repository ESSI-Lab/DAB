package eu.essi_lab.accessor.wms._1_1_1;

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

import eu.essi_lab.accessor.wms.IWMSContact;
import eu.essi_lab.jaxb.wms._1_1_1.ContactAddress;
import eu.essi_lab.jaxb.wms._1_1_1.ContactInformation;
import eu.essi_lab.jaxb.wms._1_1_1.ContactPersonPrimary;

public class WMS_1_1_1Contact implements IWMSContact {
    private ContactInformation contact;
    private ContactPersonPrimary contactPersonPrimary;
    private ContactAddress address;

    public WMS_1_1_1Contact(ContactInformation contact) {
	this.contact = contact;
	if (contact != null) {
	    this.contactPersonPrimary = contact.getContactPersonPrimary();
	    this.address = contact.getContactAddress();

	}
    }

    @Override
    public String getContactIndividualName() {
	if (contactPersonPrimary != null) {
	    return contactPersonPrimary.getContactPerson();
	}
	return null;
    }

    @Override
    public String getContactPositionName() {
	if (contact != null) {
	    return contact.getContactPosition();
	}
	return null;
    }

    @Override
    public String getOrganisationName() {
	if (contactPersonPrimary != null) {
	    return contactPersonPrimary.getContactOrganization();
	}
	return null;
    }

    @Override
    public String getDeliveryPoint() {
	if (address != null) {
	    return address.getAddress();
	}
	return null;
    }

    @Override
    public String getAdministrativeArea() {
	if (address != null) {
	    return address.getStateOrProvince();
	}
	return null;
    }

    @Override
    public String getCity() {
	if (address != null) {
	    return address.getCity();
	}
	return null;
    }

    @Override
    public String getPostCode() {
	if (address != null) {
	    return address.getPostCode();
	}
	return null;
    }

    @Override
    public String getCountry() {
	if (address != null) {
	    return address.getCountry();
	}
	return null;
    }

    @Override
    public String getMailAddress() {
	if (contact!=null){
	    return contact.getContactElectronicMailAddress();
	}
	return null;
    }

    @Override
    public String getPhone() {
	if (contact!=null){
	    return contact.getContactVoiceTelephone();
	}
	return null;
    }

    @Override
    public String getFax() {
	if (contact!=null){
	    return contact.getContactFacsimileTelephone();
	}
	return null;
    }

}
