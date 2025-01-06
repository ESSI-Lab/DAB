package eu.essi_lab.accessor.obis.harvested;

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

import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.geo.BBOXUtils;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Fabrizio
 */
public class OBISResourceMapper extends OriginalIdentifierMapper {

    /**
     *
     */
    public static final String OBIS_SCHEME_URI = "http://api.iobis.org/scheme";

    private static final String RESOURCE_ID_KEY = "id";
    private static final String TITLE_KEY = "title";
    private static final String ABSTRACT_KEY = "abstract";
    private static final String CONTACTS_KEY = "contacts";
    private static final String CONTACT_NAME = "givenname";
    private static final String CONTACT_EMAIL = "email";
    private static final String CONTACT_ROLE = "role";
    private static final String PUBLISHED_KEY = "published";
    private static final String INTELLECTUAL_RIGHTS = "intellectualrights";
    private static final String CONTACT_ORG = "organization";
    private static final String DOWNLOAD_ARCHIVE = "archive";
    private static final String DOWNLOAD_URL = "url";
    private static final String BBOX_POLYGON = "extent";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject json = new JSONObject(resource.getOriginalMetadata().getMetadata());

	return readString(json, RESOURCE_ID_KEY).orElse(null);
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	DatasetCollection ret = new DatasetCollection();
	ret.setSource(source);

	JSONObject json = new JSONObject(originalMD.getMetadata());

	MIMetadata miMetadata = ret.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	readString(json, TITLE_KEY).ifPresent(title -> miMetadata.getDataIdentification().setCitationTitle(title));

	readString(json, ABSTRACT_KEY).ifPresent(abs -> miMetadata.getDataIdentification().setAbstract(abs));

	readString(json, PUBLISHED_KEY).ifPresent(date -> miMetadata.getDataIdentification().setCitationPublicationDate(date));

	readString(json, INTELLECTUAL_RIGHTS).ifPresent(limitation -> {

	    LegalConstraints legalConstraints = new LegalConstraints();
	    legalConstraints.addUseLimitation(limitation);

	    miMetadata.getDataIdentification().addLegalConstraints(legalConstraints);
	});

	addBoundingBox(json, miMetadata);

	addContactInfo(miMetadata, json.getJSONArray(CONTACTS_KEY));

	addDistribution(json, miMetadata);

	return ret;
    }

    /**
     * @param object
     * @param key
     * @return
     */
    static boolean checkNotNull(JSONObject object, String key) {

	return (object.has(key) && !object.isNull(key));
    }

    /**
     * @param object
     * @param key
     * @return
     */
    public static Optional<String> readString(JSONObject object, String key) {

	if (checkNotNull(object, key)) {

	    return Optional.of(object.get(key).toString());
	}

	return Optional.empty();

    }

    /**
     * @param object
     * @param key
     * @return
     */
    public static Optional<Integer> readInt(JSONObject object, String key) {

	if (checkNotNull(object, key)) {

	    return Optional.of(object.getInt(key));
	}

	return Optional.empty();
    }

    /**
     * @param object
     * @param key
     * @return
     */
    public static Optional<Object> readObject(JSONObject object, String key) {

	if (checkNotNull(object, key))
	    return Optional.of(object.get(key));

	return Optional.empty();

    }

    /**
     * @param json
     * @param md
     */

    private void addBoundingBox(JSONObject json, MIMetadata md) {
	if (json.has(BBOX_POLYGON)) {
	    String polygon = json.get(BBOX_POLYGON).toString();
	    if (StringUtils.isNotEmptyAndNotNull(polygon)) {
		String bbox = BBOXUtils.toBBOX(polygon, false);

		double west = Double.valueOf(bbox.split(" ")[1]);
		double east = Double.valueOf(bbox.split(" ")[3]);
		double north = Double.valueOf(bbox.split(" ")[2]);
		double south = Double.valueOf(bbox.split(" ")[0]);
		md.getDataIdentification().addGeographicBoundingBox(north, west, south, east);
	    }
	}

    }

    /**
     * @param json
     * @param md
     */
    private void addDistribution(JSONObject json, MIMetadata md) {

	if (json.has(DOWNLOAD_URL)) {

	    String url = json.get(DOWNLOAD_URL).toString();
	    if (StringUtils.isNotEmptyAndNotNull(url)) {

		Online online = new Online();

		online.setLinkage(url);
		online.setProtocol(NetProtocols.HTTP.getCommonURN());
		online.setFunctionCode("information");
		online.setDescription("GBIF integrated publishing tooolkit");

		if (url.contains("r=")) {
		    String name = url.substring(url.indexOf("r=") + 2, url.length());
		    online.setName(name);
		}

		md.getDistribution().addDistributionOnline(online);
	    }
	}

	if (json.has(DOWNLOAD_ARCHIVE)) {

	    String url = json.get(DOWNLOAD_ARCHIVE).toString();
	    if (StringUtils.isNotEmptyAndNotNull(url)) {

		Online online = new Online();

		online.setLinkage(url);
		online.setProtocol(NetProtocols.HTTP.getCommonURN());
		online.setFunctionCode("download");
		online.setDescription("GBIF archive");

		if (url.contains("r=")) {
		    String name = url.substring(url.indexOf("r=") + 2, url.length());
		    online.setName(name);
		}

		md.getDistribution().addDistributionOnline(online);

		Format format = new Format();
		format.setName("GBIF archive");
		md.getDistribution().addFormat(format);
	    }
	}
    }

    /**
     * @param md
     * @param contactsArray
     */
    private void addContactInfo(MIMetadata md, JSONArray contactsArray) {

	for (int i = 0; i < contactsArray.length(); i++) {

	    Boolean toAdd = null;

	    ResponsibleParty respParty = new ResponsibleParty();

	    JSONObject jsonContact = contactsArray.getJSONObject(i);

	    if (jsonContact.has(CONTACT_NAME)) {

		String name = jsonContact.get(CONTACT_NAME).toString();
		if (StringUtils.isNotEmptyAndNotNull(name)) {

		    respParty.setIndividualName(name);
		    toAdd = new Boolean(false);
		}
	    }

	    if (jsonContact.has(CONTACT_ORG)) {

		String name = jsonContact.get(CONTACT_ORG).toString();
		if (StringUtils.isNotEmptyAndNotNull(name)) {

		    respParty.setOrganisationName(name);
		    if (toAdd != null) {
			toAdd = true;
		    }
		}
	    }

	    Contact contactinfo = new Contact();
	    respParty.setContactInfo(contactinfo);

	    if (jsonContact.has(CONTACT_EMAIL)) {

		String mail = jsonContact.get(CONTACT_EMAIL).toString();

		if (StringUtils.isNotEmptyAndNotNull(mail)) {

		    Address addr = new Address();
		    addr.addElectronicMailAddress(mail);

		    contactinfo.setAddress(addr);
		}
	    }

	    String role = "";

	    if (jsonContact.has(CONTACT_ROLE) && StringUtils.isNotEmptyAndNotNull(jsonContact.get(CONTACT_ROLE).toString())) {

		role = jsonContact.get(CONTACT_ROLE).toString();
	    }

	    String roleCode = "pointOfContact";
	    if (role.equalsIgnoreCase("creator")) {
		roleCode = "originator";
	    }

	    respParty.setRoleCode(roleCode);

	    if (toAdd != null && toAdd) {
		md.getDataIdentification().addPointOfContact(respParty);
	    }
	}
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return OBIS_SCHEME_URI;
    }
}
