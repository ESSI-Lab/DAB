package eu.essi_lab.accessor.emodnet;

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

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.edmo.EDMOClient;
import eu.essi_lab.lib.net.nvs.NVSClient;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gmd.v_20060504.CIRoleCodePropertyType;

public class EMODNETPhysicsMapper extends FileIdentifierMapper {

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.EMODNET_PHYSICS_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	DatasetCollection dataset = new DatasetCollection();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private class Party {
	private String name;
	private String role;
	private String uri; // concept from ontology
	boolean organization = true;
	private String email;
	private String url; // organization homepage

	@Override
	public boolean equals(Object obj) {
	    if (obj instanceof Party) {
		Party party = (Party) obj;
		if (party.getUri() != null && party.getUri().equals(uri)) {
		    if ((party.getRole() == null && role == null) || (party.getRole().equals(role))) {
			return true;
		    }
		}
		if (party.getName() != null && party.getName().equals(name)) {
		    if ((party.getRole() == null && role == null) || (party.getRole().equals(role))) {
			return true;
		    }
		}
	    }
	    return super.equals(obj);
	}

	public String getUrl() {
	    return url;
	}

	public void setUrl(String url) {
	    this.url = url;
	}

	public String getEmail() {
	    return email;
	}

	public void setEmail(String email) {
	    this.email = email;
	}

	public boolean isOrganization() {
	    return organization;
	}

	public void setOrganization(boolean organization) {
	    this.organization = organization;
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getRole() {
	    return role;
	}

	public void setRole(String role) {
	    this.role = role;
	}

	public String getUri() {
	    return uri;
	}

	public void setUri(String uri) {
	    this.uri = uri;
	}

    }

    private void mapMetadata(OriginalMetadata originalMD, DatasetCollection dataset) {

	GSLoggerFactory.getLogger(getClass()).info("EMODNet Physics Mappper STARTED");

	String originalMetadata = originalMD.getMetadata();

	JSONObject json = new JSONObject(originalMetadata);

	JSONArray countries = null;
	JSONArray dataOwners = null;
	JSONArray platforms = null;

	if (json.has("additionalMetadata")) {
	    JSONObject additionalMetadata = json.getJSONObject("additionalMetadata");
	    countries = additionalMetadata.optJSONArray("metadata_countries");
	    dataOwners = additionalMetadata.optJSONArray("metadata_data_owners");
	    platforms = additionalMetadata.optJSONArray("metadata_platforms");
	}

	String linkage = null;
	JSONArray rows = json.getJSONObject("table").getJSONArray("rows");
	String title = null;
	String description = null;
	LinkedHashMap<String, String> instrumentUriToName = new LinkedHashMap<>();
	LinkedHashSet<String> instrumentNamesWithoutUri = new LinkedHashSet<>();
	LinkedHashMap<String, String> parameterUrnToName = new LinkedHashMap<>();
	String west = null, south = null, east = null, north = null, up = null, down = null;
	String verticalUnits = null;

	String beginPosition = null;
	String endPosition = null;
	String project = null;
	String lineage = null;
	// HashSet<String> platformNames = new HashSet<String>();
	String license = null;
	String timeStamp = null;
	List<String> keywords = new ArrayList<>();
	String keywordsVoc = null;

	List<String> institutions = new ArrayList<String>();
	List<String> institutionURIs = new ArrayList<String>();

	List<String> creatorNames = new ArrayList<String>();
	List<String> creatorEmails = new ArrayList<String>();
	List<String> creatorTypes = new ArrayList<String>();
	List<String> creatorURLs = new ArrayList<String>();

	List<String> publisherNames = new ArrayList<String>();
	List<String> publisherEmails = new ArrayList<String>();
	List<String> publisherTypes = new ArrayList<String>();
	List<String> publisherURLs = new ArrayList<String>();

	List<String> piNames = new ArrayList<String>();
	List<String> piEmails = new ArrayList<String>();

	String dataIdentifier = null;

	HashSet<Party> parties = new HashSet<>();

	HashMap<String, HashMap<String, String>> attributesByVariable = new HashMap<String, HashMap<String, String>>();

	List<SimpleEntry<String, String>> platformNameCode = new ArrayList<>();

	if (platforms != null) {
	    for (int i = 0; i < platforms.length(); i++) {
		String code = platforms.getJSONArray(i).optString(0);
		String name = platforms.getJSONArray(i).optString(1);
		platformNameCode.add(new SimpleEntry<String, String>(name, code));
	    }
	}

	List<String> tmpPNames = new ArrayList<>();
	List<String> tmpPCodes = new ArrayList<>();
	List<String> tmpWmoPlatformCodes = new ArrayList<>();
	for (int i = 0; i < rows.length(); i++) {
	    JSONArray row = rows.getJSONArray(i);
	    String type = row.getString(0);
	    String var = row.getString(1);
	    String name = row.getString(2);
	    String dataType = row.getString(3);
	    String value = row.getString(4);
	    // switch (var) {
	    // case "NC_GLOBAL":
	    switch (name) {
	    case "id":
		case "doi":
		dataIdentifier = value;
		break;
	    case "title":
		title = value;
		break;
	    case "platform_name":
		addSeparatedValuesCommaSemicolonOnly(tmpPNames, value);
		break;
	    case "platform_code":
		addSeparatedValuesCommaSemicolonOnly(tmpPCodes, value);
		break;
	    case "wmo_platform_code":
		addSeparatedValuesCommaSemicolonOnly(tmpWmoPlatformCodes, value);
		break;
	    case "license":
		license = value;
		break;
	    case "project":
		project = value;
		break;
	    case "history":
		lineage = value;
		break;
	    case "summary":
		description = value;
		break;
	    case "SDN_url": {
		List<String> sdnUrls = new ArrayList<>();
		addSeparatedValuesCommaSemicolonOnly(sdnUrls, value);
		for (String u : sdnUrls) {
		    putParameterPair(parameterUrnToName, u, null);
		}
		break;
	    }
	    case "geospatial_lat_max":
		north = value;
		break;
	    case "geospatial_lat_min":
		south = value;
		break;
	    case "geospatial_lon_max":
		east = value;
		break;
	    case "geospatial_lon_min":
		west = value;
		break;
	    case "geospatial_vertical_max":
		up = value;
		break;
	    case "geospatial_vertical_min":
		down = value;
		break;
	    case "geospatial_vertical_units":
		verticalUnits = value;
		break;
	    case "time_coverage_start":
		beginPosition = value;
		break;
	    case "time_coverage_end":
		endPosition = value;
		break;
	    case "institution":
		addSeparatedValuesCommaSemicolonOnly(institutions, value);
		break;
	    case "creator_name":
		addSeparatedValuesCommaSemicolonOnly(creatorNames, value);
		break;
	    case "creator_email":
		addSeparatedValuesCommaSemicolonOnly(creatorEmails, value);
		break;
	    case "creator_type":
		addSeparatedValuesCommaSemicolonOnly(creatorTypes, value);
		break;
	    case "creator_url":
		addSeparatedValuesCommaSemicolonOnly(creatorURLs, value);
		break;
	    case "publisher_name":
		addSeparatedValuesCommaSemicolonOnly(publisherNames, value);
		break;
	    case "publisher_email":
		addSeparatedValuesCommaSemicolonOnly(publisherEmails, value);
		break;
	    case "publisher_type":
		addSeparatedValuesCommaSemicolonOnly(publisherTypes, value);
		break;
	    case "publisher_url":
		addSeparatedValuesCommaSemicolonOnly(publisherURLs, value);
		break;
	    case "principal_investigator":
		addSeparatedValuesCommaSemicolonOnly(piNames, value);
		break;
	    case "principal_investigator_email":
		addSeparatedValuesCommaSemicolonOnly(piEmails, value);
		break;
	    case "keywords_vocabulary":
		keywordsVoc = value;
		break;
	    case "institution_edmo_uri":
		addSeparatedValuesCommaSemicolonOnly(institutionURIs, value);
		break;
	    case "institution_ror_uri":
		addSeparatedValuesCommaSemicolonOnly(institutionURIs, value);
		break;
	    case "keywords":
		addSeparatedValues(keywords, value);
		break;
	    case "date_created":
	    case "date_update":
		timeStamp = value;
		break;

	    default:
		break;
	    }
	    // break;

	    // default:
	    // break;
	    // }

	    HashMap<String, String> attributes = attributesByVariable.get(var);
	    if (attributes == null) {
		attributes = new HashMap<>();
		attributesByVariable.put(var, attributes);
	    }
	    attributes.put(name, value);
	}

	if (tmpPCodes.size() == tmpPNames.size()) {
	    for (int i = 0; i < tmpPCodes.size(); i++) {
		String code = tmpPCodes.get(i);
		String name = tmpPNames.get(i);
		platformNameCode.add(new SimpleEntry<String, String>(name, code));
	    }
	} else {
	    for (int i = 0; i < tmpPNames.size(); i++) {
		String name = tmpPNames.get(i);
		platformNameCode.add(new SimpleEntry<String, String>(name, null));
	    }
	}

	List<String> platformMdIdentifiers = new ArrayList<>();
	for (int i = 0; i < platformNameCode.size(); i++) {
	    platformMdIdentifiers.add(resolvePlatformMdIdentifier(i, platformNameCode.get(i).getValue(), tmpWmoPlatformCodes));
	}
	appendPlatformsFromEmsoVariableRows(attributesByVariable, platformNameCode, platformMdIdentifiers);

	if (dataOwners != null) {
	    for (int j = 0; j < dataOwners.length(); j++) {
		JSONArray arr = dataOwners.getJSONArray(j);
		String label = arr.optString(1);

		Integer edmo = arr.optIntegerObject(0, null);
		Party party = new Party();
		if (edmo != null) {
		    EDMOClient client = new EDMOClient();
		    label = client.getLabelFromCode("" + edmo);
		    party.setUri(client.getURN("" + edmo));
		}
		party.setOrganization(true);
		party.setName(label);
		party.setRole("originator");
		parties.add(party);

	    }

	}

	expandInstitutionNamesForUriList(institutions, institutionURIs);

	List<String> empty = new ArrayList<>();
	List<Party> originatorParties = getParties(institutions, institutionURIs, empty, empty, empty, "originator");
	parties.addAll(originatorParties);

	List<Party> publisherParties = getParties(publisherNames, empty, publisherEmails, publisherURLs, publisherTypes, "publisher");
	parties.addAll(publisherParties);

	List<Party> creatorParties = getParties(creatorNames, empty, creatorEmails, creatorURLs, creatorTypes, "originator");
	parties.addAll(creatorParties);

	List<String> persons = new ArrayList<String>();
	for (int j = 0; j < piNames.size(); j++) {
	    persons.add("person");
	}
	List<Party> piParties = getParties(piNames, empty, piEmails, empty, persons, "principalInvestigator");
	parties.addAll(piParties);

	for (String variable : new TreeSet<>(attributesByVariable.keySet())) {
	    HashMap<String, String> attributes = attributesByVariable.get(variable);
	    collectParametersFromAttributes(attributes, parameterUrnToName);
	    collectInstrumentsFromAttributes(attributes, instrumentUriToName, instrumentNamesWithoutUri);
	}

	GSPropertyHandler additionalInfo = originalMD.getAdditionalInfo();
	if (additionalInfo != null) {
	    Boolean isDownloadLink = originalMD.getAdditionalInfo().get("isDownloadLink", Boolean.class);
	    if (isDownloadLink) {
		linkage = originalMD.getAdditionalInfo().get("downloadLink", String.class);
	    }
	}

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setHierarchyLevelName("series");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

	String identifier = json.getString("identifier");
	coreMetadata.setIdentifier(identifier);
	coreMetadata.getMIMetadata().setFileIdentifier(identifier);

	if (countries != null) {
	    for (int i = 0; i < countries.length(); i++) {
		String country = countries.getString(i);
		dataset.getExtensionHandler().setCountry(country);
	    }
	}

	coreMetadata.getDataIdentification().setResourceIdentifier(dataIdentifier);

	if (linkage != null) {
	    Online online = new Online();
	    online.setLinkage(linkage);
	    online.setProtocol("GET DATA");
	    online.setName(identifier);
	    online.setFunctionCode("download");
	    online.setDescription("NetCDF Download");
	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	}

	// title
	if (title != null && !title.isEmpty())

	{
	    coreMetadata.setTitle(title);
	}
	// abstract
	if (description != null && !description.isEmpty()) {
	    coreMetadata.setAbstract(description);
	}
	// keywords and PARAMETER IDENTIFIERS

	if (!parameterUrnToName.isEmpty()) {

	    for (java.util.Map.Entry<String, String> parameterEntry : parameterUrnToName.entrySet()) {

		String urn = parameterEntry.getKey();
		String label = parameterEntry.getValue();

		CoverageDescription descr = new CoverageDescription();
		descr.setAttributeIdentifier(toNvsHttpsUriIfSdnUrn(urn));
		if (label != null && !label.isEmpty()) {
		    descr.setAttributeTitle(label.trim());
		    descr.setAttributeDescription(label.trim());
		} else {
		    NVSClient client = new NVSClient();
		    String resolved = client.getLabel(urn);
		    if (resolved != null) {
			descr.setAttributeTitle(resolved);
			descr.setAttributeDescription(resolved);
		    }
		}
		coreMetadata.getMIMetadata().addCoverageDescription(descr);
	    }

	}
	{
	    Keywords keyword = new Keywords();
	    for (String s : keywords) {
		keyword.addKeyword(s.trim());
	    }
	    if (keywordsVoc != null) {
		keyword.setThesaurusNameCitationTitle(keywordsVoc);
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	if (project != null) {
	    project = project.replace(";", ",");
	    String[] split = project.split(",");
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("project");
	    for (String s : split) {
		keyword.addKeyword(s.trim());
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	Keywords keyword = new Keywords();
	keyword.setTypeCode("instrument");

	for (java.util.Map.Entry<String, String> instrumentEntry : instrumentUriToName.entrySet()) {
	    String sensorURI = instrumentEntry.getKey();
	    if (isUnknownSeaVoXL22InstrumentPlaceholderUri(sensorURI)) {
		continue;
	    }
	    String sensorLabel = instrumentEntry.getValue();
	    if (sensorLabel == null && sensorURI != null && sensorURI.contains("vocab.nerc.ac.uk")) {
		NVSClient client = new NVSClient();
		sensorLabel = client.getLabel(sensorURI);
	    }
	    if (isIgnoredSensorModel(sensorLabel)) {
		sensorLabel = null;
	    }

	    MIInstrument myInstrument = new MIInstrument();
	    if (sensorURI != null) {
		myInstrument.setMDIdentifierTypeCode(sensorURI);
	    }
	    keyword.addKeyword(sensorLabel, sensorURI);
	    myInstrument.setDescription(sensorLabel);
	    myInstrument.setTitle(sensorLabel);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	    coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
	}
	for (String sensorLabel : instrumentNamesWithoutUri) {
	    MIInstrument myInstrument = new MIInstrument();
	    keyword.addKeyword(sensorLabel, null);
	    myInstrument.setDescription(sensorLabel);
	    myInstrument.setTitle(sensorLabel);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	    coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
	}

	// platform

	Keywords pk = new Keywords();
	pk.setTypeCode("platform");

	for (int i = 0; i < platformNameCode.size(); i++) {
	    SimpleEntry<String, String> nameCode = platformNameCode.get(i);
	    String platformName = nameCode.getKey();
	    String platformCode = nameCode.getValue();
	    String platformMdIdentifier = platformMdIdentifiers.get(i);
	    MIPlatform platform = new MIPlatform();
	    if (platformMdIdentifier != null && !platformMdIdentifier.isEmpty()) {
		platform.setMDIdentifierCode(platformMdIdentifier);
	    }
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(platformName);
	    platform.setCitation(platformCitation);
	    coreMetadata.getMIMetadata().addMIPlatform(platform);
	    pk.addKeyword(platformName, platformMdIdentifier != null && !platformMdIdentifier.isEmpty() ? platformMdIdentifier : platformCode);
	}

	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(pk);
	// bbox

	if ((west != null && !west.isEmpty()) && (east != null && !east.isEmpty()) && (north != null && !north.isEmpty())
		&& (south != null && !south.isEmpty())) {
	    BigDecimal w = new BigDecimal(west);
	    BigDecimal e = new BigDecimal(east);
	    BigDecimal n = new BigDecimal(north);
	    BigDecimal s = new BigDecimal(south);

	    dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(n, w, s, e);
	}

	// datestamp
	if (timeStamp != null && !timeStamp.isEmpty()) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(timeStamp);
	    coreMetadata.getMIMetadata().setDateStampAsDate(timeStamp);
	}

	// temporal extent
	TemporalExtent tempExtent = new TemporalExtent();

	if (beginPosition != null && !beginPosition.isEmpty()) {
	    tempExtent.setBeginPosition(beginPosition);
	}
	if (endPosition != null && !endPosition.isEmpty()) {
	    tempExtent.setEndPosition(endPosition);
	}

	coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(tempExtent);

	for (Party party : parties) {
	    ResponsibleParty partyContact = new ResponsibleParty();
	    String name = party.getName();
	    String uri = party.getUri();
	    if (uri != null) {
		uri = uri.trim();
	    }
	    try {
		EDMOClient client = new EDMOClient();
		String semanticName = client.getLabelFromURI(uri);
		if (semanticName != null) {
		    name = semanticName;
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    CharacterStringPropertyType value = ISOMetadata.createAnchorPropertyType(uri, name);
	    if (party.isOrganization()) {
		partyContact.getElementType().setOrganisationName(value);
	    } else {
		partyContact.getElementType().setIndividualName(value);
	    }
	    if (party.getRole().equals("originator")) {
		dataset.getExtensionHandler().addOriginatorOrganisationIdentifier(party.getUri());
	    }
	    CIRoleCodePropertyType role = new CIRoleCodePropertyType();
	    CodeListValueType clvt = new CodeListValueType();
	    clvt.setCodeListValue(party.getRole());
	    clvt.setValue(party.getRole());
	    role.setCIRoleCode(clvt);
	    partyContact.getElementType().setRole(role);

	    Contact contact = new Contact();
	    Address address = new Address();
	    if (party.getEmail() != null) {
		address.addElectronicMailAddress(party.getEmail());
	    }
	    contact.setAddress(address);
	    if (party.getUrl() != null) {
		Online online = new Online();
		online.setLinkage(party.getUrl());
		contact.setOnline(online);
	    }
	    partyContact.setContactInfo(contact);
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(partyContact);

	}

	DataQuality dq = new DataQuality();
	dq.setLineageStatement(lineage);

	coreMetadata.getMIMetadata().addDataQuality(dq);

	LegalConstraints lc = new LegalConstraints();

	lc.addUseConstraintsCode("license");
	lc.addOtherConstraints(license);
	coreMetadata.getDataIdentification().addLegalConstraints(lc);

	GSLoggerFactory.getLogger(getClass()).info("EMODNet Physics Mappper ENDED");

    }

    /**
     * One {@code institution} string with several identifiers ({@code institution_edmo_uri},
     * {@code institution_ror_uri}, …) should yield one party per URI, all with the same organisation name.
     */
    private void expandInstitutionNamesForUriList(List<String> institutions, List<String> institutionURIs) {

	if (institutions.size() == 1 && institutionURIs.size() > 1) {
	    String name = institutions.get(0);
	    while (institutions.size() < institutionURIs.size()) {
		institutions.add(name);
	    }
	}
    }

    private List<Party> getParties(List<String> names, List<String> uris, List<String> emails, List<String> urls, List<String> types,
	    String role) {
	List<Party> ret = new ArrayList<>();
	if (names.size() < uris.size()) {
	    names.clear();
	    for (String uri : uris) {
		EDMOClient client = new EDMOClient();
		String label = client.getLabelFromURI(uri.trim());
		names.add(label);
	    }
	}
	boolean oneToOne = false;
	if (names.size() == uris.size()) {
	    oneToOne = true;
	}
	for (int i = 0; i < names.size(); i++) {
	    String name = names.get(i);
	    String uri = oneToOne ? uris.get(i) : null;
	    Party party = new Party();
	    party.setName(name);
	    party.setUri(uri);
	    party.setRole(role);
	    if (names.size() == types.size()) {
		String pType = types.get(i);
		if (pType != null && pType.equals("person")) {
		    party.setOrganization(false);
		}
	    }
	    if (names.size() == urls.size()) {
		String url = urls.get(i);
		party.setUrl(url);
	    }
	    if (names.size() == emails.size()) {
		String eMail = emails.get(i);
		party.setEmail(eMail);
	    }
	    ret.add(party);
	}

	return ret;
    }

    /**
     * Maps SeaDataNet URNs {@code SDN:<collection>::<notation>} to canonical NVS HTTPS URIs, e.g.
     * {@code SDN:P01::TEMPPR01} → {@code https://vocab.nerc.ac.uk/collection/P01/current/TEMPPR01/}.
     */
    private static String toNvsHttpsUriIfSdnUrn(String identifier) {

	if (identifier == null) {
	    return null;
	}
	String s = identifier.trim();
	if (s.isEmpty()) {
	    return identifier;
	}
	if (!s.toUpperCase(Locale.ROOT).startsWith("SDN:")) {
	    return identifier;
	}
	String body = s.substring(4);
	int sepIdx = body.indexOf("::");
	if (sepIdx <= 0 || sepIdx >= body.length() - 2) {
	    return identifier;
	}
	String collection = body.substring(0, sepIdx).trim();
	String notation = body.substring(sepIdx + 2).trim();
	if (collection.isEmpty() || notation.isEmpty()) {
	    return identifier;
	}
	return "https://vocab.nerc.ac.uk/collection/" + collection + "/current/" + notation + "/";
    }

    private void collectParametersFromAttributes(HashMap<String, String> attributes, LinkedHashMap<String, String> parameterUrnToName) {

	String parURN = attributes.get("sdn_parameter_uri");
	if (parURN == null || parURN.trim().isEmpty()) {
	    parURN = attributes.get("sdn_parameter_urn");
	}
	String parLabel = attributes.get("sdn_parameter_name");
	List<String> urns = new ArrayList<>();
	List<String> labels = new ArrayList<>();
	if (parURN != null) {
	    addSeparatedValuesCommaSemicolonOnly(urns, parURN);
	}
	if (parLabel != null) {
	    addSeparatedValuesCommaSemicolonOnly(labels, parLabel);
	}
	if (urns.isEmpty()) {
	    return;
	}
	if (urns.size() == labels.size()) {
	    for (int i = 0; i < urns.size(); i++) {
		putParameterPair(parameterUrnToName, urns.get(i), labels.get(i));
	    }
	} else if (labels.size() == 1) {
	    for (String urn : urns) {
		putParameterPair(parameterUrnToName, urn, labels.get(0));
	    }
	} else if (urns.size() == 1) {
	    for (String lab : labels) {
		putParameterPair(parameterUrnToName, urns.get(0), lab);
	    }
	} else {
	    for (String urn : urns) {
		putParameterPair(parameterUrnToName, urn, labels.isEmpty() ? null : labels.get(0));
	    }
	}
    }

    private void putParameterPair(LinkedHashMap<String, String> parameterUrnToName, String urnRaw, String labelRaw) {

	if (urnRaw == null) {
	    return;
	}
	String urn = urnRaw.trim();
	if (urn.isEmpty()) {
	    return;
	}
	String label = labelRaw != null ? labelRaw.trim() : null;
	if (!parameterUrnToName.containsKey(urn)) {
	    parameterUrnToName.put(urn, label);
	} else if (label != null && !label.isEmpty()
		&& (parameterUrnToName.get(urn) == null || parameterUrnToName.get(urn).isEmpty())) {
	    parameterUrnToName.put(urn, label);
	}
    }

    /**
     * EMSO ERDDAP: synthetic rows use {@code variable_type=platform} with {@code emso_platform_name} /
     * {@code emso_platform_uri}, or alternatively {@code emso_site_name} / {@code emso_site_uri}.
     */
    private void appendPlatformsFromEmsoVariableRows(HashMap<String, HashMap<String, String>> attributesByVariable,
	    List<SimpleEntry<String, String>> platformNameCode, List<String> platformMdIdentifiers) {

	for (String variable : new TreeSet<>(attributesByVariable.keySet())) {
	    HashMap<String, String> attributes = attributesByVariable.get(variable);
	    if (attributes == null || !"platform".equalsIgnoreCase(attributes.get("variable_type"))) {
		continue;
	    }
	    String platformName = trimToNull(attributes.get("emso_platform_name"));
	    if (platformName == null) {
		platformName = trimToNull(attributes.get("emso_site_name"));
	    }
	    if (platformName == null) {
		continue;
	    }

	    String platformId = trimToNull(attributes.get("platform_id"));

	    String platformUri = trimToNull(attributes.get("emso_platform_uri"));
	    if (platformUri == null) {
		platformUri = trimToNull(attributes.get("emso_site_uri"));
	    }
	    String wmo = trimToNull(attributes.get("wmo_platform_code"));

	    platformNameCode.add(new SimpleEntry<>(platformName, platformId));
	    String mdId = platformUri != null ? platformUri : (wmo != null ? wmo : platformId);
	    platformMdIdentifiers.add(mdId);
	}
    }

    private static String trimToNull(String value) {

	if (value == null) {
	    return null;
	}
	String t = value.trim();
	return t.isEmpty() ? null : t;
    }

    private static String resolvePlatformMdIdentifier(int platformIndex, String platformCode, List<String> wmoCodes) {

	String wmo = wmoAt(platformIndex, wmoCodes);
	if (wmo != null && !wmo.isEmpty()) {
	    return wmo;
	}
	return platformCode;
    }

    private static String wmoAt(int index, List<String> wmoes) {

	if (wmoes == null || wmoes.isEmpty()) {
	    return null;
	}
	if (index < wmoes.size()) {
	    return wmoes.get(index).trim();
	}
	if (wmoes.size() == 1) {
	    return wmoes.get(0).trim();
	}
	return null;
    }

    /**
     * Pairs {@code sensor_reference} (instrument URI) with {@code sensor_model} (instrument name) per variable.
     */
    private void collectInstrumentsFromAttributes(HashMap<String, String> attributes, LinkedHashMap<String, String> instrumentUriToName,
	    LinkedHashSet<String> instrumentNamesWithoutUri) {

	String sURN = attributes.get("sensor_reference");
	String sLabel = attributes.get("sensor_model");

	List<String> urns = new ArrayList<>();
	List<String> labels = new ArrayList<>();
	if (sURN != null && !sURN.isEmpty() && !sURN.equals("NaN")) {
	    addSeparatedValuesCommaSemicolonOnly(urns, sURN);
	}
	if (sLabel != null && !sLabel.isEmpty()) {
	    addSeparatedValuesCommaSemicolonOnly(labels, sLabel);
	}
	if (urns.isEmpty() && labels.isEmpty()) {
	    return;
	}
	if (!urns.isEmpty()) {
	    if (urns.size() == labels.size()) {
		for (int i = 0; i < urns.size(); i++) {
		    putInstrumentPair(instrumentUriToName, urns.get(i), labels.get(i));
		}
	    } else if (labels.size() == 1) {
		for (String urn : urns) {
		    putInstrumentPair(instrumentUriToName, urn, labels.get(0));
		}
	    } else if (urns.size() == 1) {
		for (String lab : labels) {
		    putInstrumentPair(instrumentUriToName, urns.get(0), lab);
		}
	    } else {
		for (String urn : urns) {
		    putInstrumentPair(instrumentUriToName, urn, labels.isEmpty() ? null : labels.get(0));
		}
	    }
	} else {
	    for (String lab : labels) {
		if (!isIgnoredSensorModel(lab)) {
		    instrumentNamesWithoutUri.add(lab);
		}
	    }
	}
    }

    /** NVS SeaVoX L22 placeholder meaning instrument type unknown (prefLabel {@code unknown}), e.g. TOOLZZZ. */
    private static boolean isUnknownSeaVoXL22InstrumentPlaceholderUri(String uri) {

	if (uri == null || uri.trim().isEmpty()) {
	    return false;
	}
	String lower = uri.trim().toLowerCase(Locale.ROOT);
	return lower.contains("/l22/current/toolzzz") || lower.contains("sdn:l22::toolzzz");
    }

    /** CF/ACDD placeholder: do not use {@code sensor_model} literally when it is "unknown". */
    private static boolean isIgnoredSensorModel(String label) {

	if (label == null) {
	    return true;
	}
	return "unknown".equalsIgnoreCase(label.trim());
    }

    private void putInstrumentPair(LinkedHashMap<String, String> instrumentUriToName, String urnRaw, String labelRaw) {

	if (urnRaw == null) {
	    return;
	}
	String urn = urnRaw.trim();
	if (urn.isEmpty() || "NaN".equals(urn)) {
	    return;
	}
	if (isUnknownSeaVoXL22InstrumentPlaceholderUri(urn)) {
	    return;
	}
	String label = labelRaw != null ? labelRaw.trim() : null;
	if (isIgnoredSensorModel(label)) {
	    label = null;
	}
	if (!instrumentUriToName.containsKey(urn)) {
	    instrumentUriToName.put(urn, label);
	} else if (label != null && instrumentUriToName.get(urn) == null) {
	    instrumentUriToName.put(urn, label);
	}
    }

    /**
     * Splits only on {@code ;} or {@code ,}. Never splits on whitespace — used for {@code sensor_model},
     * {@code sdn_parameter_name}, organisation/person attributes ({@code institution}, {@code creator_name}, …),
     * URNs/URLs, and similar phrases that contain spaces.
     */
    private void addSeparatedValuesCommaSemicolonOnly(Collection<String> collection, String value) {

	if (value == null || value.isEmpty()) {
	    return;
	}
	value = value.trim();
	String[] parts;
	if (value.contains(";")) {
	    parts = value.split(";");
	} else if (value.contains(",")) {
	    parts = value.split(",");
	} else {
	    collection.add(value);
	    return;
	}
	for (String v : parts) {
	    if (v != null) {
		v = v.trim();
		if (!v.isEmpty()) {
		    collection.add(v);
		}
	    }
	}
    }

    private void addSeparatedValues(Collection<String> collection, String value) {
	if (value == null || value.isEmpty()) {
	    return;
	}
	value = value.trim();
	String[] values;
	if (value.contains(";")) {
	    values = value.split(";");
	} else if (value.contains(",")) {
	    values = value.split(",");
	} else {
	    values = value.split(" ");
	}
	for (String v : values) {
	    if (v != null) {
		v = v.trim();
		if (!v.isEmpty()) {
		    collection.add(v);
		}
	    }
	}
    }

}
