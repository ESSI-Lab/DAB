package eu.essi_lab.accessor.stac.harvested;

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
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Roberto
 */
public class STACCollectionMapper extends OriginalIdentifierMapper {

    /**
     * Collection specification
     * https://github.com/radiantearth/stac-spec/blob/master/collection-spec/collection-spec.md
     * type - REQUIRED. Must be set to Collection to be a valid Collection.
     * stac_version - REQUIRED. The STAC version the Collection implements.
     * stac_extensions - A list of extension identifiers the Collection implements.
     * id - REQUIRED. Identifier for the Collection that is unique across the provider.
     * title - A short descriptive one-line title for the Collection.
     * description - REQUIRED. Detailed multi-line description to fully explain the Collection. CommonMark 0.29 syntax
     * MAY be used for rich text representation.
     * keywords - List of keywords describing the Collection.
     * license - REQUIRED. Collection's license(s), either a SPDX License identifier, various if multiple licenses apply
     * or proprietary for all other cases.
     * providers - A list of providers, which may include all organizations capturing or processing the data or the
     * hosting provider. Providers should be listed in chronological order with the most recent provider being the last
     * element of the list.
     * extent - REQUIRED. Spatial and temporal extents.
     * summaries - STRONGLY RECOMMENDED. A map of property summaries, either a set of values, a range of values or a
     * JSON Schema.
     * links - REQUIRED. A list of references to other documents.
     * assets -Dictionary of asset objects that can be downloaded, each with a unique key.
     */

    public static enum RELATION_TYPE {
	ROOT("root"), // URL to the root STAC entity (Catalog or Collection).
	SELF("self"), // Absolute URL to the location that the Collection file can be found online, if available.
	ITEM("item"), // URL to a STAC Item
	ITEMS("items"), COLLECTIONS("collections"), // URL to a Collection. Absolute URLs should be used whenever
						    // possible. The referenced Collection is STRONGLY RECOMMENDED to
						    // implement the same STAC version as the Item.
	PARENT("parent"), // URL to the parent STAC entity (Catalog or Collection).
	CHILD("child"), // URL to a child STAC entity (Catalog or Collection).
	LICENSE("license"), // The license URL(s) for the Collection SHOULD be specified if the license field is set to
			    // proprietary or various
	DERIVED_FROM("derived_from"); // URL to a STAC Collection that was used as input data in the creation of this
				      // Collection.

	private String id;

	public String getId() {
	    return id;
	}

	RELATION_TYPE(String id) {
	    this.id = id;
	}

    }

    public STACCollectionMapper() {
	// TODO Auto-generated constructor stub
    }

    public static final String STAC_SCHEME_URI = "http://stacspec.org";

    private static final String STAC_VERSION = "stac_version";
    private static final String STAC_EXTENSIONS = "stac_extensions";
    private static final String RESOURCE_ID_KEY = "id";
    private static final String TITLE_KEY = "title";
    private static final String TYPE = "type";
    private static final String LICENSE = "license";
    private static final String ABSTRACT_KEY = "description";
    private static final String EXTENT = "extent";
    private static final String PROPERTIES = "properties";
    private static final String PROVIDERS = "providers";
    private static final String KEYWORDS = "keywords";
    private static final String SUMMARIES = "summaries";
    private static final String ASSETS = "assets";
    private static final String TEMPORAL = "temporal";
    private static final String SPATIAL = "spatial";
    private static final String CONTACT_ROLE = "role";
    private static final String CONTACT_NAME = "name";

    // private static final String CONTACTS_KEY = "contacts";
    // private static final String CONTACT_EMAIL = "email";
    // private static final String PUBLISHED_KEY = "published";
    //
    // private static final String CONTACT_ORG = "organization";
    // private static final String DOWNLOAD_ARCHIVE = "archive";
    private static final String DOWNLOAD_URL = "links";
    // private static final String BBOX_POLYGON = "extent";

    public static final String WMS_CAPABILITIES_URL = "https://ows.digitalearth.africa/wms?request=GetCapabilities&service=WMS&version=1.1.1";

    public static final String WMS_BASE_URL = "https://ows.digitalearth.africa/wms?";

    public static XMLDocumentReader wmsCapabilities;

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
	miMetadata.setHierarchyLevelName("series");
	miMetadata.addHierarchyLevelScopeCodeListValue("series");

	readString(json, RESOURCE_ID_KEY).ifPresent(id -> ret.getExtensionHandler().setSTACSecondLevelInfo(id));

	readString(json, TITLE_KEY).ifPresent(title -> miMetadata.getDataIdentification().setCitationTitle(title));

	readString(json, ABSTRACT_KEY).ifPresent(abs -> miMetadata.getDataIdentification().setAbstract(abs));

	// readString(json, PROVIDERS).ifPresent(date ->
	// miMetadata.getDataIdentification().setCitationPublicationDate(date));

	readString(json, LICENSE).ifPresent(limitation -> {

	    LegalConstraints legalConstraints = new LegalConstraints();
	    legalConstraints.addUseLimitation(limitation);

	    miMetadata.getDataIdentification().addLegalConstraints(legalConstraints);
	});

	addExtent(json, miMetadata);

	addContactInfo(miMetadata, json.optJSONArray(PROVIDERS));

//	addDistribution(json, miMetadata);

	if (wmsCapabilities == null) {

	    wmsCapabilities = getCapabilities();
	}

	enrichMetadata(miMetadata);

	return ret;
    }

    private void enrichMetadata(MIMetadata md) {
	try {
	    if (wmsCapabilities != null) {
		String title = md.getDataIdentification().getCitationTitle();
		Node node = wmsCapabilities.evaluateNode("//*:Layer//*:Layer[@queryable]/*:Name[text()='" + title + "']/..");
		if (node != null) {
		    //add WMS layer
		    Online online = new Online();
		    online.setLinkage(WMS_BASE_URL);
		    online.setName(title);
		    online.setProtocol(NetProtocolWrapper.WMS_1_3_0.getCommonURN());
		    online.setFunctionCode("download");
		    md.getDistribution().addDistributionOnline(online);
		    //enrich metadata
		    XMLNodeReader reader = new XMLNodeReader(node);
		    String newTitle = reader.evaluateString("*:Title");
		    String newAbs = reader.evaluateString("*:Abstract");
		    List<Node> keywords = reader.evaluateOriginalNodesList("*:KeywordList/*:Keyword");
		    if (newTitle != null && !newTitle.isEmpty()) {
			md.getDataIdentification().setCitationTitle(newTitle);
		    }
		    if (newAbs != null && !newAbs.isEmpty()) {
			md.getDataIdentification().setAbstract(newAbs);
		    }
		    for(Node n: keywords) {
			md.getDataIdentification().addKeyword(n.getTextContent());
		    }
		} else {
		    GSLoggerFactory.getLogger(getClass()).info("Original STAC metadata only for record with id: {}",
			    md.getDataIdentification().getCitationTitle());
		}

	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Failed to enrich metadata for record with id: {}",
		    md.getDataIdentification().getCitationTitle());
	}

    }

    private XMLDocumentReader getCapabilities() {
	XMLDocumentReader xdoc;
	Downloader d = new Downloader();
	try {
	    Optional<InputStream> capabilities = d.downloadOptionalStream(WMS_CAPABILITIES_URL);
	    if (capabilities.isPresent()) {
		xdoc = new XMLDocumentReader(capabilities.get());
		return xdoc;
	    }
	} catch (Exception e) {
	    return null;
	}
	return null;

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

    private void addExtent(JSONObject json, MIMetadata md) {
	if (json.has(EXTENT)) {
	    Optional<Object> extent = readObject(json, EXTENT);
	    if (extent.isPresent()) {
		Optional<Object> temporalObject = readObject((JSONObject) extent.get(), TEMPORAL);
		Optional<Object> spatiallObject = readObject((JSONObject) extent.get(), SPATIAL);
		// BBOX: specification WGS 84 longitude/latitude
		if (spatiallObject.isPresent()) {
		    JSONObject bboxObject = (JSONObject) spatiallObject.get();
		    JSONArray bboxArray = bboxObject.optJSONArray("bbox");
		    if (bboxArray != null) {
			if (bboxArray.length() > 0) {
			    JSONArray doublesArray = bboxArray.optJSONArray(0);
			    if (doublesArray != null && doublesArray.length() == 4) {
				Double west = doublesArray.optDouble(0);
				Double east = doublesArray.optDouble(2);
				Double north = doublesArray.optDouble(3);
				Double south = doublesArray.optDouble(1);
				if (!west.isNaN() && !east.isNaN() && !north.isNaN() && !south.isNaN()) {
				    md.getDataIdentification().addGeographicBoundingBox(north, west, south, east);
				}
			    }

			}
		    }
		}
		// TIME: Each inner array consists of exactly two elements, either a timestamp or null.
		// Open date ranges are supported by setting the start and/or the end time to null.
		// Timestamps consist of a date and time in UTC and MUST be formatted according to RFC 3339, section
		// 5.6.
		if (temporalObject.isPresent()) {
		    JSONObject timeObject = (JSONObject) temporalObject.get();
		    JSONArray intervalArray = timeObject.optJSONArray("interval");
		    // Optional<String> interval = readString(timeObject, "interval");
		    if (intervalArray != null) {
			if (intervalArray.length() > 0) {
			    JSONArray timeArray = intervalArray.optJSONArray(0);

			    if (timeArray != null && timeArray.length() == 2) {
				String startDate = timeArray.optString(0);
				String endDate = timeArray.optString(1);

				TemporalExtent tempExtent = new TemporalExtent();
				if (!startDate.isEmpty() && !endDate.isEmpty()) {

				    tempExtent.setBeginPosition(startDate);

				    tempExtent.setEndPosition(endDate);
				}

				md.getDataIdentification().addTemporalExtent(tempExtent);
			    }

			}
		    }
		}
	    }
	}

    }

    /**
     * @param json
     * @param md
     */
    private void addDistribution(JSONObject json, MIMetadata md) {

	if (json.has(DOWNLOAD_URL)) {

	    JSONArray arrayLinks = json.optJSONArray(DOWNLOAD_URL);
	    if (arrayLinks != null) {
		for (int j = 0; j < arrayLinks.length(); j++) {
		    JSONObject objectURL = arrayLinks.getJSONObject(j);
		    String rel = readString(objectURL, "rel").orElse(null);
		    String url = readString(objectURL, "href").orElse(null);
		    RELATION_TYPE relType = RELATION_TYPE.valueOf(rel.toUpperCase());
		    String description = rel;
		    switch (relType) {
		    case CHILD:
			description = "URL to a child STAC entity (Catalog or Collection)";
			break;
		    case DERIVED_FROM:
			description = "URL to a STAC Collection that was used as input data in the creation of this Collection";
			break;
		    case ITEM:
		    case ITEMS:
			description = "URL to a STAC Item";
			break;
		    case LICENSE:
			description = "The license URL(s) for the Collection";
			break;
		    case PARENT:
			description = "URL to the parent STAC entity (Catalog or Collection)";
			break;
		    case ROOT:
			description = "URL to the root STAC entity (Catalog or Collection)";
			break;
		    case SELF:
			description = "Absolute URL to the location that the Collection file can be found online";
			break;
		    default:
			break;

		    }

		    Online online = new Online();

		    online.setLinkage(url);
		    online.setProtocol(NetProtocolWrapper.HTTP.getCommonURN());
		    online.setFunctionCode("information");
		    online.setDescription(description);

		    md.getDistribution().addDistributionOnline(online);

		}
	    }
	}
    }

    /**
     * @param md
     * @param contactsArray
     */
    private void addContactInfo(MIMetadata md, JSONArray contactsArray) {

	if (contactsArray != null) {

	    for (int i = 0; i < contactsArray.length(); i++) {

		ResponsibleParty respParty = new ResponsibleParty();

		JSONObject jsonContact = contactsArray.getJSONObject(i);

		if (jsonContact.has(CONTACT_NAME)) {

		    String name = jsonContact.get(CONTACT_NAME).toString();
		    if (StringUtils.isNotEmptyAndNotNull(name)) {

			respParty.setIndividualName(name);
			respParty.setOrganisationName(name);
			// toAdd = new Boolean(false);
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

		md.getDataIdentification().addPointOfContact(respParty);

	    }
	}
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return STAC_SCHEME_URI;
    }
}
