/**
 * 
 */
package eu.essi_lab.accessor.healthsites;

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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.AccessType;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Fabrizio
 */
public class HealthSitesMapper extends OriginalIdentifierMapper {

    /**
     * 
     */
    public static final String HEALTH_SITES_SCHEME_URI = "https://healthsites.io/api/v2/healthsites/metadata";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	String metadata = resource.getOriginalMetadata().getMetadata();
	JSONObject object = new JSONObject(metadata);

	if (object.has("uuid")) {
	    return object.getString("uuid");
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	String metadata = originalMD.getMetadata();
	JSONObject object = new JSONObject(metadata);

	Dataset dataset = new Dataset();

	dataset.setSource(source);

	MIMetadata md_Metadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	DataIdentification dataId = md_Metadata.getDataIdentification();

	String id = "";
	// String longName = "HealthSites - ";
	String title = "Title not provided";
	String timeStamp = null;
	Double[] geom;
	String url = null;
	Double lat = null;
	Double lon = null;
	String country = null;
	String state = null;
	String city = null;
	String addr = null;
	String postalCode = null;
	String email = null;
	String tel = null;
	String fax = null;
	JSONArray bbox = null;
	String type = null;

	JSONObject attributes = object.getJSONObject("attributes");

	// TITLE
	if (attributes.has("name")) {

	    title = attributes.getString("name");
	}

	// // ABSTRACT (type)
	// if (attributes.has("type")) {
	// type = attributes.getString("type");
	// longName = longName + type;
	// }

	if (title == null || title.isEmpty()) {
	    title = "None";
	}

	dataId.setCitationTitle(title);
	dataId.setAbstract(title);

	// TIME
	if (attributes.has("changeset_timestamp")) {
	    timeStamp = attributes.getString("changeset_timestamp");
	}

	if (timeStamp != null) {
	    TemporalExtent timeExtent = new TemporalExtent();
	    timeExtent.setId(UUID.randomUUID().toString().substring(0, 6));
	    timeExtent.setBeginPosition(timeStamp);
	    timeExtent.setEndPosition(timeStamp);

	    dataId.addTemporalExtent(timeExtent);
	}

	// ONLINE
	if (attributes.has("source_url")) {
	    url = attributes.getString("source_url");
	}

	if (url != null) {

	    Online httpOnline = new Online();
	    httpOnline.setLinkage(url);
	    httpOnline.setName(title);
	    httpOnline.setFunctionCode("download");
	    httpOnline.setProtocol("HTTP");

	    httpOnline.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());

	    md_Metadata.getDistribution().addDistributionOnline(httpOnline);
	}

	// KEYWORDS
	Keywords md_Keywords = new Keywords();
	dataId.addKeywords(md_Keywords);

	md_Keywords.addKeyword("HEALTHSITES");
	md_Keywords.addKeyword("HEALTH");

	if (type != null) {
	    md_Keywords.addKeyword(type.toUpperCase());
	}

	md_Keywords.addKeyword(title);

	ResponsibleParty contact = new ResponsibleParty();

	if (attributes.has("addr_street")) {
	    addr = attributes.getString("addr_street");
	}

	if (attributes.has("addr_city")) {
	    city = attributes.getString("addr_city");
	}

	// if (attributes.has("provincia")) {
	// state = attributes.getString("provincia");
	// }
	//
	if (attributes.has("addr_postcode")) {
	    postalCode = attributes.getString("addr_postcode");
	}
	//
	// if (attributes.has("email")) {
	// email = attributes.getString("email");
	// }
	//
	// if (attributes.has("tel")) {
	// tel = attributes.getString("tel");
	// }
	//
	// if (attributes.has("fax")) {
	// fax = attributes.getString("fax");
	// }

	contact.setRoleCode("pointOfContact");

	Contact contactInfo = new Contact();
	Address address = new Address();

	if (city != null) {
	    address.setCity(city);
	}
	if (addr != null) {
	    address.addDeliveryPoint(addr);
	}

	if (postalCode != null) {
	    address.setPostalCode(postalCode);
	}
	//
	// if (state != null) {
	// address.setAdministrativeArea(state);
	// }
	//
	// if (email != null) {
	// address.addElectronicMailAddress(email);
	// }
	//
	// if (tel != null) {
	// contactInfo.addPhoneVoice(tel);
	// }
	//
	// if (fax != null) {
	// contactInfo.addPhoneFax(fax);
	// }

	contactInfo.setAddress(address);
	contact.setContactInfo(contactInfo);
	dataId.addPointOfContact(contact);

	//
	// BBOX
	//

	JSONObject centroid = object.getJSONObject("centroid");

	if (centroid.has("coordinates")) {

	    bbox = centroid.getJSONArray("coordinates");
	}

	if (bbox != null) {
	    lon = bbox.getDouble(0);
	    lat = bbox.getDouble(1);
	}

	if (lat != null && lon != null) {
	    dataId.addGeographicBoundingBox(lat, lon, lat, lon);
	}

	return dataset;
    }

    static List<String> splitPageResults(ClonableInputStream stream) throws Exception {

	ArrayList<String> out = Lists.newArrayList();

	JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(stream.clone()));
	for (int i = 0; i < array.length(); i++) {

	    JSONObject object = array.getJSONObject(i);
	    out.add(object.toString());
	}

	return out;
    }

    static boolean hasResults(ClonableInputStream stream) throws Exception {

	JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(stream.clone()));
	return array.length() > 0;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return HEALTH_SITES_SCHEME_URI;
    }

}
