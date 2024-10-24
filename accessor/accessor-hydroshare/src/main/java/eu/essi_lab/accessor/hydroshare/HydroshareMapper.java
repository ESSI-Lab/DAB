/**
 *
 */
package eu.essi_lab.accessor.hydroshare;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import eu.essi_lab.lib.utils.GSLoggerFactory;
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
public class HydroshareMapper extends OriginalIdentifierMapper {

    /**
     * 
     */
    public static final String HYDROSHARE_SCHEME_URI = "https://www.hydroshare.org/hsapi/scheme";
    private static final String DOWNLOAD_FUNCTION_CODE = "download";
    private static final String EXCEPTION_GETTING_VALUE = "Exception getting value";
    private static final String EXCEPTION_SETTING_VALUE = "Exception setting value";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject object = new JSONObject(resource.getOriginalMetadata().getMetadata());

	if (object.has("resource_id")) {
	    return object.getString("resource_id");
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	String metadata = originalMD.getMetadata();
	JSONObject object = new JSONObject(metadata);

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	MIMetadata mdMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	DataIdentification dataId = mdMetadata.getDataIdentification();

	String longName = "Hydroshare";
	String shortName = "Title not provided";
	String fromDate = null;
	String toDate = null;

	String resourceUrl = null;
	String resourcemapurl = null;
	String scienceMetadataUrl = null;
	String bagurl = null;
	Double north = null;
	Double south = null;
	Double east = null;
	Double west = null;

	String addr = null;
	String postalCode = null;

	String email = null;
	String tel = null;
	String fax = null;
	JSONArray bbox = null;
	String type = null;
	String contactPoint = null;
	String datestamp = null;

	if (object.has("date_created")) {
	    datestamp = object.getString("date_created");
	    mdMetadata.setDateStampAsDate(datestamp);
	}

	// TITLE
	if (object.has("resource_title")) {
	    shortName = object.getString("resource_title");

	}

	// ABSTRACT (type)
	if (object.has("resource_type")) {
	    type = object.getString("resource_type");
	    longName = longName + " " + type + " : " + shortName;
	}

	if (shortName == null || shortName.isEmpty()) {
	    shortName = "No Name";
	}

	dataId.setCitationTitle(shortName);
	dataId.setAbstract(longName);

	// BBOX & TEMPORAL EXTENT
	if (object.has("coverages")) {
	    bbox = object.getJSONArray("coverages");
	}

	if (bbox != null && bbox.length() > 0) {

	    for (int k = 0; k < bbox.length(); k++) {

		JSONObject var = bbox.getJSONObject(k);

		if (var.has("type")) {

		    JSONObject value = null;
		    String val = null;

		    val = var.getString("type");

		    if (var.has("value")) {
			value = var.getJSONObject("value");
		    }

		    if (val != null) {
			switch (val) {
			case "point":
			    if (value != null) {
				east = value.getDouble("east");
				north = value.getDouble("north");
				south = north;
				west = east;
			    }
			    break;
			case "box":
			    if (value != null) {
				west = value.getDouble("westlimit");
				north = value.getDouble("northlimit");
				east = value.getDouble("eastlimit");
				south = value.getDouble("southlimit");
			    }
			    break;
			case "period":

			    if (value != null) {
				fromDate = value.getString("start");
				toDate = value.getString("end");
			    }
			    break;
			default:
			    GSLoggerFactory.getLogger(getClass()).warn("No action for value {}", val);
			}
		    }
		}
	    }
	}

	if (south != null) {
	    dataId.addGeographicBoundingBox(north, west, south, east);
	}

	if (object.has("date_modified")) {
	    toDate = object.getString("date_modified");
	}

	// TIME
	if (toDate != null && fromDate != null) {
	    TemporalExtent timeExtent = new TemporalExtent();
	    timeExtent.setBeginPosition(fromDate);
	    timeExtent.setEndPosition(toDate);

	    dataId.addTemporalExtent(timeExtent);
	}

	// ONLINE
	Online hydroshareHome = new Online();

	hydroshareHome.setLinkage("https://www.hydroshare.org/search/");
	hydroshareHome.setName("Hydroshare Homepage");
	hydroshareHome.setFunctionCode("information");
	hydroshareHome.setProtocol("HTTP");
	hydroshareHome.setDescription("Hydroshare Homepage");

	hydroshareHome.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());

	mdMetadata.getDistribution().addDistributionOnline(hydroshareHome);

	// ONLINES
	try {
	    if (object.has("bag_url")) {
		bagurl = object.getString("bag_url");
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	try {
	    if (object.has("science_metadata_url")) {
		scienceMetadataUrl = object.getString("science_metadata_url");
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	try {
	    if (object.has("resource_map_url")) {
		resourcemapurl = object.getString("resource_map_url");
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	try {
	    if (object.has("resource_url")) {
		resourceUrl = object.getString("resource_url");
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	if (resourceUrl != null) {

	    Online httpOnline = new Online();

	    httpOnline.setLinkage(resourceUrl);
	    httpOnline.setName("resource_url: " + shortName);
	    httpOnline.setFunctionCode(DOWNLOAD_FUNCTION_CODE);
	    httpOnline.setProtocol("HTTP");
	    httpOnline.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());

	    mdMetadata.getDistribution().addDistributionOnline(httpOnline);
	}

	if (resourcemapurl != null) {

	    Online httpOnline = new Online();

	    httpOnline.setLinkage(resourcemapurl);
	    httpOnline.setName("resource_map_url: " + shortName);
	    httpOnline.setFunctionCode(DOWNLOAD_FUNCTION_CODE);
	    httpOnline.setProtocol("HTTP");
	    httpOnline.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());

	    mdMetadata.getDistribution().addDistributionOnline(httpOnline);
	}

	if (scienceMetadataUrl != null) {

	    Online httpOnline = new Online();

	    httpOnline.setLinkage(scienceMetadataUrl);
	    httpOnline.setName("science_metadata_url: " + shortName);
	    httpOnline.setFunctionCode(DOWNLOAD_FUNCTION_CODE);
	    httpOnline.setProtocol("HTTP");
	    httpOnline.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());

	    mdMetadata.getDistribution().addDistributionOnline(httpOnline);
	}

	if (bagurl != null) {

	    Online httpOnline = new Online();

	    httpOnline.setLinkage(bagurl);
	    httpOnline.setName("bag_url: " + shortName);
	    httpOnline.setFunctionCode(DOWNLOAD_FUNCTION_CODE);
	    httpOnline.setProtocol("HTTP");
	    httpOnline.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());

	    mdMetadata.getDistribution().addDistributionOnline(httpOnline);
	}

	// KEYWORDS
	Keywords mdKeywords = new Keywords();
	dataId.addKeywords(mdKeywords);
	mdKeywords.addKeyword("HYDROSHARE");
	if (type != null) {
	    mdKeywords.addKeyword(type.toUpperCase());
	}
	mdKeywords.addKeyword(shortName);

	ResponsibleParty contact = new ResponsibleParty();

	try {
	    if (object.has("physical-address")) {
		addr = object.getString("physical-address");
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	try {
	    if (object.has("cap")) {
		postalCode = object.getString("cap");
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	try {
	    if (object.has("email")) {
		email = object.getString("email");
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	try {
	    if (object.has("phone")) {
		tel = object.getString("phone");
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	try {
	    if (object.has("fax")) {
		fax = object.getString("fax");
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	try {
	    if (object.has("creator")) {
		contactPoint = object.getString("creator");
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	try {
	    if (contactPoint != null) {
		contact.setIndividualName(contactPoint);
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_GETTING_VALUE, t);
	}

	contact.setRoleCode("pointOfContact");

	Contact contactInfo = new Contact();
	Address address = new Address();

	try {
	    if (addr != null) {
		address.addDeliveryPoint(addr);
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_SETTING_VALUE, t);
	}

	try {
	    if (postalCode != null) {
		address.setPostalCode(postalCode);
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_SETTING_VALUE, t);
	}

	try {
	    if (email != null) {
		address.addElectronicMailAddress(email);
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_SETTING_VALUE, t);
	}

	try {
	    if (tel != null) {
		contactInfo.addPhoneVoice(tel);
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_SETTING_VALUE, t);
	}

	try {
	    if (fax != null) {
		contactInfo.addPhoneFax(fax);
	    }
	} catch (Exception t) {
	    GSLoggerFactory.getLogger(getClass()).warn(EXCEPTION_SETTING_VALUE, t);
	}

	contactInfo.setAddress(address);
	contact.setContactInfo(contactInfo);

	dataId.addPointOfContact(contact);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return HYDROSHARE_SCHEME_URI;
    }

    /**
     * @param stream
     * @return
     * @throws Exception
     */
    static List<String> splitPageResults(ClonableInputStream stream) {

	ArrayList<String> out = Lists.newArrayList();

	JSONObject jResponse = null;

	try {

	    jResponse = new JSONObject(IOStreamUtils.asUTF8String(stream.clone()));

	    JSONArray array = jResponse.getJSONArray("results");

	    for (int i = 0; i < array.length(); i++) {

		JSONObject object = array.getJSONObject(i);
		out.add(object.toString());
	    }

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(HydroshareMapper.class).warn("Can't parse stream", e);

	}

	return out;
    }
}
