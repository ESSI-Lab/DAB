package eu.essi_lab.pdk.rsm.impl.json.jsapi;

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

import com.google.common.collect.*;
import eu.essi_lab.access.compliance.*;
import eu.essi_lab.access.compliance.DataComplianceTester.*;
import eu.essi_lab.access.compliance.wrapper.*;
import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.lib.xml.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.pluggable.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.model.resource.data.*;
import eu.essi_lab.pdk.rsm.*;
import net.opengis.iso19139.gmx.v_20060504.*;
import org.json.*;

import javax.ws.rs.core.*;
import javax.xml.datatype.*;
import java.net.*;
import java.util.*;

/**
 * Result set mapper implementation which maps the {@link GSResource}s according to a JSON schema encoding defined for the <a
 * href="http://api.eurogeoss-broker.eu/docs/index.html">JavaScript API</a>. The {@link #JS_API_MAPPING_SCHEMA} has the following
 * properties:
 * <ul>
 * <li>schema uri: {@link GSNameSpaceContext#GS_DATA_MODEL_SCHEMA_URI}</li>
 * <li>schema name: {@link GSNameSpaceContext#GS_DATA_MODEL_SCHEMA_NAME}</li>
 * <li>schema version: {@link NameSpace#GS_DATA_MODEL_SCHEMA_VERSION}</li>
 * <li>encoding name: {@value #JS_API_DATA_MODEL_ENCODING_NAME}</li>
 * <li>encoding version: {@value #JS_API_DATA_MODEL_ENCODING_NAME_VERSION}</li>
 * <li>encoding media type: {@link MediaType#APPLICATION_JSON}</li>
 * </ul>
 *
 * @author Fabrizio
 */
public class JS_API_ResultSetMapper extends DiscoveryResultSetMapper<String> {

    /**
     * The encoding name of {@link #JS_API_MAPPING_SCHEMA}
     */
    public static final String JS_API_DATA_MODEL_ENCODING_NAME = "js-api-dm-enc";

    /**
     * The encoding version of {@link #JS_API_MAPPING_SCHEMA}
     */
    public static final String JS_API_DATA_MODEL_ENCODING_NAME_VERSION = "1.0";

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema JS_API_MAPPING_SCHEMA = new MappingSchema();

    static {

	JS_API_MAPPING_SCHEMA.setUri(NameSpace.GS_DATA_MODEL_SCHEMA_URI);
	JS_API_MAPPING_SCHEMA.setName(NameSpace.GS_DATA_MODEL_SCHEMA_NAME);
	JS_API_MAPPING_SCHEMA.setVersion(NameSpace.GS_DATA_MODEL_SCHEMA_VERSION);

	JS_API_MAPPING_SCHEMA.setEncoding(JS_API_DATA_MODEL_ENCODING_NAME);
	JS_API_MAPPING_SCHEMA.setEncodingVersion(JS_API_DATA_MODEL_ENCODING_NAME_VERSION);
	JS_API_MAPPING_SCHEMA.setEncodingMediaType(MediaType.APPLICATION_JSON_TYPE);
    }

    private static final String SOS_TAHMO_PROXY_PATH = "sos-tahmo-proxy";

    private static final String SOS_TAHMO_URL = "http://hnapi.hydronet.com/api/service/sos";

    private static final String SOS_TWIGA_URL = "http://hn4s.hydronet.com/api/service/TWIGA/sos";

    @Override
    public String map(DiscoveryMessage message, GSResource resource) {

	GSSource gsSource = resource.getSource();

	if (message.isOutputSources()) {

	    BrokeringStrategy strategy = gsSource.getBrokeringStrategy();
	    String endpoint = gsSource.getEndpoint();
	    String label = gsSource.getLabel();
	    String identifier = gsSource.getUniqueIdentifier();
	    String version = gsSource.getVersion();

	    JSONObject source = new JSONObject();
	    source.put("id", identifier);
	    source.put("type", "composed");
	    source.put("title", label);
	    source.put("harvested", strategy == BrokeringStrategy.HARVESTED);

	    // online
	    JSONObject online = new JSONObject();
	    online.put("url", endpoint);
	    online.put("function", "download");
	    online.put("accessType", "unknown");

	    // onlines array
	    JSONArray onlines = new JSONArray();
	    onlines.put(online);
	    source.put("online", onlines);

	    // service
	    JSONObject service = new JSONObject();
	    service.put("title", label);
	    service.put("type", "unknown");
	    service.put("version", version);
	    service.put("source", true);

	    // operation
	    JSONObject operation = new JSONObject();
	    operation.put("binding", "HTTP_GET");
	    operation.put("online", onlines);

	    // operation array
	    JSONArray operations = new JSONArray();
	    operations.put(operation);
	    source.put("operation", operations);

	    return source.toString();

	}

	JSONObject report = new JSONObject();

	MIMetadata mi_Metadata = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	List<DataIdentification> diList = Lists.newArrayList(mi_Metadata.getDataIdentifications());

	// ---
	// id
	// ---

	String id = mi_Metadata.getFileIdentifier();

	report.put("id", id);

	// ---
	// type
	// ---
	if (resource instanceof DatasetCollection) {

	    report.put("type", "composed");

	} else {

	    report.put("type", "simple");
	}

	// -----------------
	// parent identifier
	// -----------------

	String parent = mi_Metadata.getParentIdentifier();

	if (parent != null) {
	    report.put("parentId", parent);
	}

	// --------------------
	// hierarchy level code
	// --------------------

	mi_Metadata.getHierarchyLevelScopeCodeListValues().forEachRemaining(value -> {

	    report.put("hierarchyLevel", value);
	});

	// --------------------------
	// raster mosaic
	// --------------------------

	resource.getExtensionHandler().getRasterMosaic().ifPresent(mosaic -> {

	    report.put("rasterMosaic", mosaic);
	});

	// ------------------------------
	// distributor and owner org.name
	// ------------------------------

	ArrayList<ResponsibleParty> contacts = Lists.newArrayList(mi_Metadata.getContacts());//

	contacts.forEach(contact -> {

	    handleOrgName(contact, report);
	});

	// -----------
	// native EPSG
	// -----------

	JSONArray epsgArray = new JSONArray();

	mi_Metadata.getReferenceSystemInfos().forEachRemaining(info -> {

	    Optional<AnchorType> codeAnchorType = info.getCodeAnchorType();

	    Optional<String> codeString = info.getCodeString();

	    if(codeAnchorType.isPresent()) {

		String label = codeAnchorType.get().getTitle();
		String url = codeAnchorType.get().getHref();

		JSONObject epsg = new JSONObject();

		epsg.put("label", label);
		epsg.put("url", url);

		epsgArray.put(epsg);

	    }else if(codeString.isPresent()){

		JSONObject epsg = new JSONObject();

		epsg.put("label", codeString.get());

		epsgArray.put(epsg);
	    }
	});

	if(!epsgArray.isEmpty()){

	    report.put("nativeEPSG", epsgArray);
	}

	// --------------------
	// coverageDescription
	// --------------------

	CoverageDescription covDesc = mi_Metadata.getCoverageDescription();

	if (covDesc != null) {
	    String coverageDescription = covDesc.getAttributeDescription();
	    report.put("coverageDescription", coverageDescription);
	}

	if (gsSource != null) {

	    String sourceLabel = gsSource.getLabel();
	    String sourceId = gsSource.getUniqueIdentifier();

	    JSONObject source = new JSONObject();
	    source.put("id", sourceId);
	    source.put("title", sourceLabel);

	    report.put("source", source);
	}

	Distribution distribution = mi_Metadata.getDistribution();

	if (distribution != null) {

	    // ----------------------------
	    // distributor parties org.name
	    // ----------------------------

	    distribution.getDistributorParties().forEach(party -> {

		String name = party.getOrganisationName();
		if (name != null && !name.isEmpty()) {

		    report.put("distributorOrgName", name);
		}
	    });

	    // -------
	    // format
	    // -------

	    List<Format> formats = Lists.newArrayList(distribution.getFormats());

	    if (!formats.isEmpty()) {
		JSONArray array = new JSONArray();
		for (Format format : formats) {
		    if (format.getName() != null && !format.getName().equals("")) {
			array.put(format.getName());
		    }
		}
		report.put("format", array);
	    }

	    // -------
	    // online
	    // -------

	    List<Online> onlineList = Lists.newArrayList(distribution.getDistributionOnlines());

	    onlineList.addAll(Lists.newArrayList(distribution.getDistributorOnlines()));

	    if (!onlineList.isEmpty()) {

		JSONArray online = new JSONArray();

		for (Online on : onlineList) {
		    JSONObject obj = createOnline(on, message);
		    if (obj != null) {
			online.put(obj);
		    }
		}

		ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);
		List<DataComplianceReport> reports = handler.getReports();
		for (DataComplianceReport r : reports) {
		    DataComplianceTest lastSucceededTest = r.getLastSucceededTest();
		    if (lastSucceededTest.equals(DataComplianceTest.EXECUTION)) {
			if (r.getFullDataDescriptor().getDataType().equals(DataType.TIME_SERIES)) {
			    try {
				String onlineId = r.getOnlineId();
				JSONObject jsonOnline = new JSONObject();
				jsonOnline.put("protocol", "GWIS");
				jsonOnline.put("function", "info");
				URL base = new URL(message.getRequestAbsolutePath());
				Optional<String> token = message.getWebRequest().extractTokenId();
				String tokenString = "";
				if (token.isPresent()) {
				    tokenString = "&token=" + token.get();
				}
				URL url = new URL(base, "../gwis?request=plot&onlineId=" + onlineId + tokenString);
				jsonOnline.put("url", url.toExternalForm());
				online.put(jsonOnline);
			    } catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			    break;
			}
		    }

		}

		if (!online.isEmpty()) {

		    report.put("online", online);
		}
	    }

	}

	// -------------------------------------------
	//
	// from the first data identification
	//

	if (!diList.isEmpty()) {

	    DataIdentification firstId = diList.getFirst();

	    // -----
	    // title
	    // -----

	    String title = normalizeText(firstId.getCitationTitle()).orElse("none");

	    report.put("title", title);

	    // ---------------
	    // alternate title
	    // ---------------

	    normalizeText(firstId.getCitationAlternateTitle()).ifPresent(desc -> report.put("alternateTitle", desc));

	    // -----------
	    // description
	    // -----------

	    normalizeText(firstId.getAbstract()).ifPresent(desc -> report.put("description", desc));

	    // --------
	    // updated
	    // --------

	    String revDate = firstId.getCitationRevisionDate();

	    String revDateTime = null;

	    XMLGregorianCalendar dateTime = firstId.getCitationRevisionDateTime();

	    if (dateTime != null) {
		revDateTime = dateTime.toString();
	    }

	    if (revDate != null || revDateTime != null) {
		if (revDate != null) {
		    report.put("update", revDate);
		} else {
		    report.put("update", revDateTime);
		}
	    }

	    // --------
	    // created
	    // --------

	    String crDate = firstId.getCitationCreationDate();

	    String crDateTime = null;
	    dateTime = firstId.getCitationCreationDateTime();

	    if (dateTime != null) {
		crDateTime = dateTime.toString();
	    }

	    String created = crDate != null ? crDate : crDateTime;

	    if (created != null) {

		report.put("created", created);
	    }
	}

	// ---------------------------------------------
	//
	// array of values, from all the identifications
	//
	//
	JSONArray rightsArray = new JSONArray();
	JSONArray authorsArray = new JSONArray();
	JSONArray contributorsArray = new JSONArray();
	JSONArray whereArray = new JSONArray();
	JSONArray verticalExtentArray = new JSONArray();
	JSONArray whenArray = new JSONArray();
	JSONArray keywordArray = new JSONArray();
	JSONArray keywordTypeArray = new JSONArray();
	JSONArray topicArray = new JSONArray();
	JSONArray overviewArray = new JSONArray();

	for (DataIdentification identification : diList) {

	    // --------------------------
	    // spatialRepresentationType
	    // --------------------------

	    String spatialRepresentationType = identification.getSpatialRepresentationTypeCodeListValue();

	    if (spatialRepresentationType != null) {

		report.put("spatialRepresentationType", spatialRepresentationType);
	    }

	    // -------
	    // updated
	    // -------

	    String revDate = identification.getCitationDate(Identification.REVISION);

	    if (revDate != null && !revDate.isEmpty()) {

		report.put("updated", revDate);

	    } else {

		XMLGregorianCalendar revDateTime = identification.getCitationDateTime(Identification.REVISION);

		if (revDateTime != null) {

		    report.put("updated", revDateTime.toString());
		}
	    }

	    // ----------
	    // expiration
	    // ----------

	    String expDate = identification.getCitationDate(Identification.EXPIRATION);

	    if (expDate != null && !expDate.isEmpty()) {

		report.put("expiration", expDate);

	    } else {

		XMLGregorianCalendar expDateTime = identification.getCitationDateTime(Identification.EXPIRATION);

		if (expDateTime != null) {

		    report.put("expiration", expDateTime.toString());
		}
	    }

	    // -------
	    // rights
	    // -------

	    List<String> rights = Lists.newArrayList(identification.getLegalConstraintsAccessCodes());
	    List<String> rights2 = Lists.newArrayList(identification.getLegalConstraintsUseLimitations());

	    rights.addAll(rights2);

	    for (String right : rights) {

		normalizeText(right).ifPresent(rightsArray::put);
	    }

	    //
	    //
	    //

	    Iterator<ResponsibleParty> pointOfContacts = identification.getPointOfContacts();

	    List<ResponsibleParty> parties = identification.getCitedParty();

	    pointOfContacts.forEachRemaining(parties::add);

	    for (ResponsibleParty party : parties) {

		if (party != null) {

		    // ------------------------------
		    // distributor and owner org.name
		    // ------------------------------

		    handleOrgName(party, report);

		    //
		    //
		    //

		    // -----------------------
		    // author and contributor
		    // -----------------------

		    String orgName = party.getOrganisationName();
		    String posName = party.getPositionName();
		    String individualName = party.getIndividualName();

		    Contact contact = party.getContact();
		    JSONObject partyObj = new JSONObject();

		    if (orgName != null) {

			partyObj.put("orgName", orgName);
		    }

		    if (posName != null) {

			partyObj.put("posName", posName);
		    }

		    if (individualName != null) {

			partyObj.put("individualName", individualName);
		    }

		    if (contact != null) {

			Address address = contact.getAddress();

			if (address != null) {

			    String email = address.getElectronicMailAddress();
			    String city = address.getCity();

			    if (email != null) {
				partyObj.put("email", email);
			    }
			    if (city != null) {
				partyObj.put("city", city);
			    }
			}

			Online on = contact.getOnline();
			JSONArray onlineArray = new JSONArray();

			if (on != null) {
			    JSONObject online = createOnline(on, message);
			    onlineArray.put(online);
			}

			if (!onlineArray.isEmpty()) {
			    partyObj.put("online", onlineArray);
			}
		    }

		    String value = party.getRoleCode();

		    if (value != null) {
			switch (value) {
			case "author":
			    authorsArray.put(partyObj);
			    break;
			case "contributor":
			    contributorsArray.put(partyObj);
			    break;
			}
		    }
		}
	    }

	    // --------------
	    // spatial extent
	    // --------------

	    List<GeographicBoundingBox> boundingBoxList = Lists.newArrayList(identification.getGeographicBoundingBoxes());

	    for (GeographicBoundingBox bbox : boundingBoxList) {

		Double south = bbox.getSouth();
		Double west = bbox.getWest();
		Double north = bbox.getNorth();
		Double east = bbox.getEast();

		if (south != null && west != null && north != null && east != null) {

		    JSONObject bboxObj = new JSONObject();

		    bboxObj.put("south", south);
		    bboxObj.put("west", west);
		    bboxObj.put("north", north);
		    bboxObj.put("east", east);

		    whereArray.put(bboxObj);
		}
	    }

	    // ---------------
	    // vertical extent
	    // ---------------

	    List<VerticalExtent> verticalExtentList = Lists.newArrayList(identification.getVerticalExtents());

	    for (VerticalExtent extent : verticalExtentList) {

		Double max = extent.getMaximumValue();
		Double min = extent.getMinimumValue();

		if (max != null && min != null && !Double.isNaN(max) && !Double.isNaN(min)) {
		    JSONObject ext = new JSONObject();

		    ext.put("min", min);
		    ext.put("max", max);

		    verticalExtentArray.put(ext);
		}

	    }

	    // ---------------
	    // temporal extent
	    // ---------------

	    List<TemporalExtent> temporalExtentList = Lists.newArrayList(identification.getTemporalExtents());

	    for (TemporalExtent temp : temporalExtentList) {

		String beginPosition = temp.getBeginPosition();
		String endPosition = temp.getEndPosition();

		addWhen(whenArray, beginPosition, endPosition);

		String timeInstantBegin = temp.getTimeInstantBegin();
		String timeInstantEnd = temp.getTimeInstantEnd();

		addWhen(whenArray, timeInstantBegin, timeInstantEnd);
	    }

	    // --------
	    // keyword
	    // --------

	    List<Keywords> keywordList = Lists.newArrayList(identification.getKeywords());

	    for (Keywords kwd : keywordList) {

		String type = kwd.getTypeCode();

		normalizeText(type).ifPresent(keywordTypeArray::put);

		Iterator<String> keywords = kwd.getKeywords();

		while (keywords.hasNext()) {

		    normalizeText(keywords.next()).ifPresent(keywordArray::put);
		}
	    }

	    // ------
	    // topic
	    // ------

	    List<String> topicCategoryList = Lists.newArrayList(identification.getTopicCategoriesStrings());

	    for (String code : topicCategoryList) {

		normalizeText(code).ifPresent(val -> topicArray.put(val));
	    }

	    // ---------
	    // overview
	    // ---------

	    List<BrowseGraphic> graphicOverviewList = Lists.newArrayList(identification.getGraphicOverviews());

	    for (BrowseGraphic graphic : graphicOverviewList) {

		String fileName = graphic.getFileName();

		if (fileName != null) {

		    overviewArray.put(fileName);
		}
	    }
	}

	// ---------------
	// inserts rights
	if (!rightsArray.isEmpty()) {

	    report.put("rights", rightsArray);
	}

	// ----------------
	// inserts authors
	if (!authorsArray.isEmpty()) {

	    report.put("author", authorsArray);
	}

	// ---------------------
	// inserts contributors
	if (!contributorsArray.isEmpty()) {

	    report.put("contributors", contributorsArray);
	}

	// --------------
	// inserts where
	if (!whereArray.isEmpty()) {

	    report.put("where", whereArray);
	}

	// -----------------------
	// inserts verticalExtent
	if (!verticalExtentArray.isEmpty()) {

	    report.put("verticalExtent", verticalExtentArray);
	}

	// --------------
	// inserts when
	if (!whenArray.isEmpty()) {

	    report.put("when", whenArray);
	}

	// ----------------
	// inserts keyword
	if (!keywordArray.isEmpty()) {

	    report.put("keyword", keywordArray);
	}

	if (!keywordTypeArray.isEmpty()) {

	    report.put("keyword_type", keywordTypeArray);
	}

	// --------------
	// inserts topic
	if (!topicArray.isEmpty()) {

	    report.put("topic", topicArray);
	}

	// -----------------
	// inserts overview
	if (!overviewArray.isEmpty()) {

	    report.put("overview", overviewArray);
	}

	// --------------------
	// attributeDescription
	// --------------------

	JSONArray attributeDescription = new JSONArray();
	JSONArray attributeTitle = new JSONArray();

	List<String> titles = resource.getIndexesMetadata().read(MetadataElement.ATTRIBUTE_TITLE);
	TreeSet<String> sortedAttributeTitles = new TreeSet<>(titles);

	List<String> descriptions = resource.getIndexesMetadata().read(MetadataElement.ATTRIBUTE_DESCRIPTION);
	TreeSet<String> sortedAttributeDescriptions = new TreeSet<>(descriptions);

	for (String sortedDescription : sortedAttributeDescriptions) {

	    attributeDescription.put(sortedDescription);
	}

	if (!attributeDescription.isEmpty()) {

	    report.put("attributeDescription", attributeDescription);
	}

	for (String sortedTitle : sortedAttributeTitles) {

	    attributeTitle.put(sortedTitle);
	}

	if (!attributeTitle.isEmpty()) {

	    report.put("attributeTitle", attributeTitle);
	}

	// ----------------------------------------
	// platformDescription & platformIdentifier
	// ----------------------------------------

	List<MIPlatform> platformList = Lists.newArrayList(mi_Metadata.getMIPlatforms());

	TreeSet<String> sortedPlatformDescriptions = new TreeSet<>();
	TreeSet<String> sortedPlatformTitles = new TreeSet<>();

	if (!platformList.isEmpty()) {

	    for (MIPlatform platform : platformList) {

		String pDesc = platform.getDescription();

		if (pDesc != null) {

		    sortedPlatformDescriptions.add(pDesc);
		}

		Citation citation = platform.getCitation();
		String pTitle = citation != null ? citation.getTitle() : null;

		if (pTitle != null) {
		    sortedPlatformTitles.add(pTitle);
		}
	    }
	}

	JSONArray platformDescription = new JSONArray();

	for (String sortedDescription : sortedPlatformDescriptions) {

	    platformDescription.put(sortedDescription);
	}

	if (!platformDescription.isEmpty()) {

	    report.put("platformDescription", platformDescription);
	}

	JSONArray platformTitle = new JSONArray();

	for (String sortedTitle : sortedPlatformTitles) {

	    platformTitle.put(sortedTitle);
	}

	if (!platformTitle.isEmpty()) {

	    report.put("platformTitle", platformTitle);
	}

	// --------------------------------------------
	// instrumentDescription & instrumentIdentifier
	// --------------------------------------------

	List<MIInstrument> instrList = Lists.newArrayList(mi_Metadata.getMIInstruments());

	if (!instrList.isEmpty()) {

	    JSONArray instrumentIdentifier = new JSONArray();
	    JSONArray instrumentDescription = new JSONArray();
	    JSONArray instrumentTitle = new JSONArray();

	    TreeSet<String> sortedInstrumentDescriptions = new TreeSet<>();
	    TreeSet<String> sortedInstrumentTitles = new TreeSet<>();

	    for (MIInstrument instrument : instrList) {

		String iDesc = instrument.getDescription();

		if (iDesc != null) {
		    sortedInstrumentDescriptions.add(iDesc);
		}

		String iTitle = instrument.getTitle();

		if (iTitle != null) {
		    sortedInstrumentTitles.add(iTitle);
		}
	    }

	    for (String sortedDescription : sortedInstrumentDescriptions) {

		instrumentDescription.put(sortedDescription);
	    }

	    if (!instrumentDescription.isEmpty()) {

		report.put("instrumentDescription", instrumentDescription);
	    }

	    for (String sortedTitle : sortedInstrumentTitles) {

		instrumentTitle.put(sortedTitle);
	    }

	    if (!instrumentTitle.isEmpty()) {

		report.put("instrumentTitle", instrumentTitle);
	    }
	}

	ExtensionHandler handler = resource.getExtensionHandler();

	// -----------------------------------
	// originator organisation description
	// -----------------------------------

	List<String> origOrgDesc = handler.getOriginatorOrganisationDescriptions();

	JSONArray jsonOrigOrgDesc = new JSONArray();

	for (String desc : origOrgDesc) {

	    normalizeText(desc).ifPresent(jsonOrigOrgDesc::put);
	}

	if (!jsonOrigOrgDesc.isEmpty()) {

	    report.put("origOrgDesc", jsonOrigOrgDesc);
	}

	// --------------
	// theme category
	// --------------

	Optional<String> themeCategoryOpt = handler.getThemeCategory();

	themeCategoryOpt.ifPresent(s -> report.put("themeCategory", s));

	return report.toString();
    }

    /**
     * @param responsibleParty
     * @param report
     */
    private void handleOrgName(ResponsibleParty responsibleParty, JSONObject report) {

	String roleCode = responsibleParty.getRoleCode();

	String orgName = responsibleParty.getOrganisationName();

	if (orgName != null && !orgName.isEmpty() && roleCode != null) {

	    if (roleCode.equals("owner")) {

		report.put("ownerOrgName", roleCode);

	    } else if (roleCode.equals("distributor")) {

		report.put("distributorOrgName", roleCode);
	    }
	}
    }

    /**
     * @param text
     * @return
     */
    private Optional<String> normalizeText(String text) {

	if (text == null || text.isEmpty()) {
	    return Optional.empty();
	}

	return Optional.of(text.trim().strip().replaceAll("\\s+", " "));
    }

    /**
     * @param when
     * @param from
     * @param to
     */
    private void addWhen(JSONArray when, String from, String to) {

	if (from != null || to != null) {
	    JSONObject obj = new JSONObject();
	    if (from != null) {
		obj.put("from", normalizeTime(from));
	    }
	    if (to != null) {
		obj.put("to", normalizeTime(to));
	    }
	    when.put(obj);
	}
    }

    /**
     * @param time
     * @return
     */
    private String normalizeTime(String time) {

	return time.contains("T") ? time : time + "T00:00:00";
    }

    /**
     * @param on
     * @param message
     * @return
     */
    private JSONObject createOnline(Online on, DiscoveryMessage message) {

	String url = on.getLinkage();
	String name = on.getName();
	String description = on.getDescription();
	String protocol = on.getProtocol();
	String function = on.getFunctionCode();
	String anchor = on.getDescriptionGmxAnchor();

	JSONObject online = new JSONObject();
	boolean addOnline = false;
	if (url != null) {

	    // filter old gi-axe urls
	    if (url.contains("axe.geodab.eu") || url.contains("geodab-gi-axe")) {
		return null;
	    }
	    // china geoss case: endpoint changed
	    String token = "ChinaGEOSS-2018@124.16.184.25";
	    if (url.contains(token)) {
		url = url.replace(token, "ChinaGEOSS@124.16.184.9");
	    }

	    // Trans-African Hydro-Meteorological Observatory (TAHMO) case: use proxy
	    if (url.startsWith(SOS_TAHMO_URL)) {
		try {
		    // check if it is the base endpoint only or getobservation request
		    URL checkURL = new URL(url);
		    if (checkURL.getQuery() != null && !checkURL.getQuery().isEmpty()) {
			String instanceBaseUrl = message.getWebRequest().getUriInfo().getBaseUri().toString();
			String replaceUrl = instanceBaseUrl.endsWith("/")
				? instanceBaseUrl + SOS_TAHMO_PROXY_PATH
				: instanceBaseUrl + "/" + SOS_TAHMO_PROXY_PATH;
			url = url.replace(SOS_TAHMO_URL, replaceUrl);
		    }

		} catch (MalformedURLException e) {
		    e.printStackTrace();
		}

	    }

	    // TWIGA use case: use proxy (should replace the TAHMO SOS)
	    if (url.startsWith(SOS_TWIGA_URL)) {
		try {
		    // check if it is the base endpoint only or getobservation request
		    URL checkURL = new URL(url);
		    if (checkURL.getQuery() != null && !checkURL.getQuery().isEmpty()) {
			String instanceBaseUrl = message.getWebRequest().getUriInfo().getBaseUri().toString();
			String replaceUrl = instanceBaseUrl.endsWith("/")
				? instanceBaseUrl + SOS_TAHMO_PROXY_PATH
				: instanceBaseUrl + "/" + SOS_TAHMO_PROXY_PATH;
			url = url.replace(SOS_TWIGA_URL, replaceUrl);
		    }

		} catch (MalformedURLException e) {
		    e.printStackTrace();
		}

	    }

	    online.put("url", url);
	    addOnline = true;
	}
	if (name != null) {
	    online.put("name", name);
	    addOnline = true;
	}
	if (description != null) {
	    online.put("description", description);
	    addOnline = true;
	}
	if (protocol != null) {
	    online.put("protocol", protocol);
	    addOnline = true;
	}
	if (function != null) {
	    online.put("function", function);
	    addOnline = true;
	}
	if (anchor != null && anchor.startsWith("http://www.essi-lab.eu/broker/accesstypes/")) {
	    addOnline = true;
	    anchor = anchor.replace("http://www.essi-lab.eu/broker/accesstypes/", "");
	    if (anchor.equals("simple")) {
		anchor = "direct";
	    }
	    online.put("accessType", anchor);
	}

	if (addOnline) {
	    return online;
	}

	return null;
    }

    /**
     * Returns the {@link ESSILabProvider}
     */
    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public MappingSchema getMappingSchema() {

	return JS_API_MAPPING_SCHEMA;
    }

}
