package eu.essi_lab.lib.net.edmo;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class EDMOClient {
    private String url;

    private static ExpiringCache<String> cache = new ExpiringCache<>();
    private static ExpiringCache<ResponsibleParty> parties = new ExpiringCache<>();
    static {
	cache.setMaxSize(1000);
	cache.setDuration(TimeUnit.MINUTES.toMillis(30));
	parties.setMaxSize(1000);
	parties.setDuration(TimeUnit.MINUTES.toMillis(30));
    }

    public EDMOClient() {
	this("https://edmo.seadatanet.org/sparql/sparql");
    }

    public EDMOClient(String url) {
	this.url = url;
    }

    public String getLabelFromURI(String uri) {
	String code = getCodeFromURI(uri);
	if (code == null) {
	    return null;
	}
	return getLabelFromCode(code);
    }

    private String getCodeFromURI(String uri) {
	if (uri == null) {
	    return null;
	}
	String code = null;
	// http://www.seadatanet.org/urnurl/SDN:EDMO::1348
	if (uri.contains("www.seadatanet.org/urnurl/SDN:EDMO::")) {
	    code = uri.substring(uri.lastIndexOf(":") + 1).trim();
	}
	// https://edmo.seadatanet.org/report/3078
	if (code == null && uri.contains("edmo.seadatanet.org/report")) {
	    code = uri.substring(uri.lastIndexOf("/") + 1).trim();
	}
	return code;
    }

    public String getLabelFromCode(String edmoCode) {
	ResponsibleParty res = getResponsiblePartyFromCode(edmoCode);
	if (res!=null) {
	    return res.getOrganisationName();
	}
	return null;
    }

    public String getURN(String edmoCode) {
	return "https://edmo.seadatanet.org/report/" + edmoCode;
    }

    public ResponsibleParty getResponsiblePartyFromURI(String uri) {
	String code = getCodeFromURI(uri);
	if (code == null) {
	    return null;
	}
	return getResponsiblePartyFromCode(code);
    }

    public ResponsibleParty getResponsiblePartyFromCode(String code) {
	if (code == null) {
	    return null;
	}
	code = code.trim();
	ResponsibleParty ret = parties.get(code);
	if (ret != null) {
	    return ret;
	}
	String url = this.url + "?query=DESCRIBE%20%3Chttps%3A%2F%2Fedmo.seadatanet.org%2Freport%2F" + code
		+ "%3E%0D%0A&accept=application%2Frdf%2Bxml";
	Downloader d = new Downloader();
	Optional<InputStream> stream = d.downloadOptionalStream(url);
	if (stream.isPresent()) {
	    try {
		XMLDocumentReader reader = new XMLDocumentReader(stream.get());
		String name = reader.evaluateString("/*:RDF/*:Description/*:name");
		String country = reader.evaluateString("/*:RDF/*:Description/*:country-name");
		String email = reader.evaluateString("/*:RDF/*:Description/*:email/@*:resource");
		String altName = reader.evaluateString("/*:RDF/*:Description/*:altName");
		String seeAlso = reader.evaluateString("/*:RDF/*:Description/*:seeAlso");
		String locality = reader.evaluateString("/*:RDF/*:Description/*:locality");
		String streetAddress = reader.evaluateString("/*:RDF/*:Description/*:street-address");
		String postalCode = reader.evaluateString("/*:RDF/*:Description/*:postal-code");
		String definition = reader.evaluateString("/*:RDF/*:Description/*:definition");
		String lon = reader.evaluateString("/*:RDF/*:Description/*:long");
		String lat = reader.evaluateString("/*:RDF/*:Description/*:lat");
		String tel = reader.evaluateString("/*:RDF/*:Description/*:tel");
		String fax = reader.evaluateString("/*:RDF/*:Description/*:fax");
		String logo = reader.evaluateString("/*:RDF/*:Description/*:logo");
		ret = new ResponsibleParty();
		ret.setOrganisationName(name);
		Contact contact = new Contact();
		contact.addPhoneVoice(tel);
		contact.addPhoneFax(fax);
		Address address = new Address();
		address.setCity(locality);
		address.setPostalCode(postalCode);
		address.addDeliveryPoint(streetAddress);
		address.setCountry(country);
		address.addElectronicMailAddress(email);

		contact.setAddress(address);
		Online online = new Online();
		online.setLinkage(seeAlso);
		contact.setOnline(online);
		ret.setContactInfo(contact);
		parties.put(code, ret);
		return ret;
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("EDMO code not found: {}", url);
	}
	return null;
    }

}
