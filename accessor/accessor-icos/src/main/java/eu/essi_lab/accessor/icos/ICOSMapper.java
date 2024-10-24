package eu.essi_lab.accessor.icos;

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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;

public class ICOSMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.ICOS_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	Boolean isForGEOSS = false;
	GSPropertyHandler info = originalMD.getAdditionalInfo();
	if (info != null) {
	    isForGEOSS = info.get("isForGEOSS", Boolean.class);
	}

	String originalMetadata = originalMD.getMetadata();

	JSONObject object = new JSONObject(originalMetadata);

	String identifier = getString(object, "pid");

	String fileName = getString(object, "fileName");

	String accessUrl = getString(object, "accessUrl");

	JSONObject referencesObj = object.optJSONObject("references");

	JSONObject specificationObject = object.optJSONObject("specification");

	String coverageGeoJsonObject = getString(object, "coverageGeoJson");

	JSONObject licenseObject = referencesObj.optJSONObject("licence");
	String license = null;
	if (licenseObject != null) {
	    license = getString(licenseObject, "name");
	}

	JSONObject coverageGeoObject = object.optJSONObject("coverageGeo");
	String coord = null;
	List<BigDecimal> longitudes = new ArrayList<>();
	List<BigDecimal> latitudes = new ArrayList<>();
	List<BigDecimal> elevations = new ArrayList<>();
	if (coverageGeoObject != null) {
	    try {
		JSONObject geometryObj = coverageGeoObject.optJSONObject("geometry");
		if (geometryObj != null) {
		    JSONArray coordArray = geometryObj.optJSONArray("coordinates");
		    String type = getString(geometryObj, "type");
		    addCoordinates(longitudes, latitudes, elevations, coordArray, type);
		} else {
		    GSLoggerFactory.getLogger(getClass()).error("Missing BBOX for {}", identifier);
		}
	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error("Error for id {}", identifier);
	    }
	}

	JSONObject specificInfoObject = object.optJSONObject("specificInfo");

	// second level string/object

	String refTitle = null;

	List<JSONObject> listIndividuals = new ArrayList<JSONObject>();

	if (referencesObj != null) {
	    refTitle = getString(referencesObj, "title");
	    if (refTitle == null || refTitle.isEmpty()) {
		refTitle = getString(referencesObj, "citationString");
	    }
	    JSONArray authorsArray = getJSONArray(referencesObj, "authors");
	    if (authorsArray.length() > 0) {
		for (int j = 0; j < authorsArray.length(); j++) {
		    JSONObject authorObject = authorsArray.optJSONObject(j);
		    if (authorObject != null) {
			authorObject.put("role", "author");
			listIndividuals.add(authorObject);
		    }
		}
	    }
	}
	List<String> keywords = new ArrayList<>();

	String abstrakt = null;
	String projectKeyword = null;
	String projectURI = null;
	if (specificationObject != null) {

	    JSONObject projectObject = specificationObject.optJSONObject("project");
	    keywords = getStrings(specificationObject, "keywords");
	    if (!keywords.isEmpty()) {
		System.out.println();
	    }
	    if (projectObject != null) {
		keywords.addAll(getStrings(projectObject, "keywords"));
	    }
	    JSONObject selfObject = projectObject.optJSONObject("self");
	    if (selfObject != null) {
		String label = getString(selfObject, "label");
		String comment = getString(selfObject, "comments");
		projectKeyword = label;
		projectURI = getString(selfObject, "uri");
		if (isForGEOSS) {
		    abstrakt = (label != null) ? label : "";
		    abstrakt += (comment != null) ? " " + comment.replace("[\"", "").replace("\"]", "") : "";
		}
	    }

	}
	String startDate = null;
	String endDate = null;
	String platformName = null;
	String platformCode = null;
	String orgURIName = null;
	List<String> respOrgNames = new ArrayList<String>();
	List<String> respOrgURINames = new ArrayList<String>();
	List<String> respOrgRoles = new ArrayList<String>();
	List<String> instrumentTitles = new ArrayList<>();
	List<String> instrumentUris = new ArrayList<>();
	List<String> instrumentComments = new ArrayList<>();
	String stationCoverageGeo = null;
	List<SimpleEntry<String, String>> parameters = new ArrayList<>();

	JSONObject productionObject = specificInfoObject.optJSONObject("productionInfo");
	if (productionObject != null) {
	    JSONObject creatorObject = productionObject.optJSONObject("creator");
	    if (creatorObject != null) {
		JSONObject self = creatorObject.optJSONObject("self");
		if (self != null) {
		    String label = getString(self, "label");
		    respOrgNames.add(label);
		    String uri = getString(self, "uri");
		    respOrgURINames.add(uri);
		    respOrgRoles.add("processor");
		}
	    }
	    JSONObject hostObject = productionObject.optJSONObject("host");
	    if (hostObject != null) {
		JSONObject self = hostObject.optJSONObject("self");
		if (self != null) {
		    String label = getString(self, "label");
		    respOrgNames.add(label);
		    String uri = getString(self, "uri");
		    respOrgURINames.add(uri);
		    respOrgRoles.add("owner");
		}
	    }
	    JSONArray contributors = productionObject.optJSONArray("contributors");
	    if (contributors != null && contributors.length() > 0) {
		for (int i = 0; i < contributors.length(); i++) {
		    JSONObject conObj = contributors.optJSONObject(i);
		    if (conObj != null) {
			conObj.put("role", "contributor");
			listIndividuals.add(conObj);
		    }
		}
	    }
	}

	if (specificInfoObject != null) {
	    JSONObject acquisitionObject = specificInfoObject.optJSONObject("acquisition");

	    if (acquisitionObject != null) {
		JSONObject intervalObject = acquisitionObject.optJSONObject("interval");

		startDate = (intervalObject != null) ? getString(intervalObject, "start") : null;

		endDate = (intervalObject != null) ? getString(intervalObject, "stop") : null;

		JSONObject stationObject = acquisitionObject.optJSONObject("station");

		JSONArray instruments = acquisitionObject.optJSONArray("instrument");

		if (instruments != null) {
		    for (int i = 0; i < instruments.length(); i++) {
			JSONObject instrument = instruments.optJSONObject(i);
			if (instrument != null) {
			    String label = getString(instrument, "label");
			    String uri = getString(instrument, "uri");
			    String comments = getString(instrument, "comments");
			    instrumentComments.add(comments);
			    instrumentTitles.add(label);
			    instrumentUris.add(uri);
			}
		    }
		}

		if (stationObject != null) {
		    JSONObject orgObject = stationObject.optJSONObject("org");
		    platformName = (orgObject != null) ? getString(orgObject, "name") : null;
		    JSONObject selfObject = (orgObject != null) ? orgObject.optJSONObject("self") : null;
		    platformCode = (selfObject != null) ? getString(selfObject, "uri") : null;
		    JSONObject coverageObject = stationObject.optJSONObject("coverage");
		    if (coverageObject != null) {
			JSONObject geoObject = coverageObject.optJSONObject("geo");
			stationCoverageGeo = (geoObject != null) ? getString(geoObject, "coordinates") : null;
		    }

		    JSONObject responsibleOrg = stationObject.optJSONObject("responsibleOrganization");
		    if (responsibleOrg != null) {
			// respOrgName = getString(responsibleOrg, "name");
			JSONObject selfObj = responsibleOrg.optJSONObject("self");
			String label = (selfObj != null) ? getString(selfObj, "label") : null;
			String uri = (selfObj != null) ? getString(selfObj, "uri") : null;
			if (label != null) {
			    respOrgNames.add(label);
			    respOrgURINames.add(uri);
			    respOrgRoles.add("originator");
			}
		    }
		}
	    }

	}

	JSONArray coulmnsArray = getJSONArray(specificInfoObject, "columns");

	if (coulmnsArray.length() > 0) {
	    for (int j = 0; j < coulmnsArray.length(); j++) {
		JSONObject columnObject = coulmnsArray.optJSONObject(j);
		String parameterLabel = getString(columnObject, "label");
		String parameterURI = null;
		JSONObject valueType = columnObject.optJSONObject("valueType");
		if (valueType != null) {
		    JSONObject selfObject = valueType.optJSONObject("self");
		    if (selfObject != null) {
			parameterURI = getString(selfObject, "uri");
			parameterLabel = getString(selfObject, "label");
		    }
		}

		if (parameterLabel == null) {
		    continue;
		} else {
		    String toAdd = (parameterLabel.toLowerCase().contains("latitude") || parameterLabel.toLowerCase().contains("longitude")
			    || parameterLabel.toLowerCase().contains("timestamp") || parameterLabel.toLowerCase().contains("time instant"))
				    ? null
				    : parameterLabel;
		    if (toAdd != null) {
			SimpleEntry<String, String> e = new SimpleEntry(toAdd, parameterURI);
			parameters.add(e);
		    }
		}
	    }
	}

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	// title
	String buildTitle = null;
	if (refTitle != null) {
	    coreMetadata.setTitle(refTitle);
	}

	// abstract
	if (abstrakt != null && !abstrakt.isEmpty()) {
	    coreMetadata.setAbstract(abstrakt);
	}

	// identifier
	if (identifier != null && !identifier.isEmpty()) {

	    String id = identifier;
	    coreMetadata.setIdentifier(id);
	    coreMetadata.getMIMetadata().setFileIdentifier(id);

	}

	coreMetadata.getMIMetadata().getDataIdentification().setResourceIdentifier(identifier);

	// keywords
	if (keywords != null && !keywords.isEmpty()) {
	    for (String k : keywords) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(k);
	    }
	}

	if (license != null) {
	    LegalConstraints lc = new LegalConstraints();
	    lc.addUseConstraintsCode("license");
	    lc.addOtherConstraints(license);
	    coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(lc);
	}

	if (longitudes.isEmpty() || latitudes.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("To investigate: missing bbox for PID: {}", identifier);

	} else {
	    BigDecimal minLon = Collections.min(longitudes);
	    BigDecimal maxLon = Collections.max(longitudes);
	    BigDecimal minLat = Collections.min(latitudes);
	    BigDecimal maxLat = Collections.max(latitudes);
	    dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(maxLat, minLon, minLat, maxLon);
	}
	if (!elevations.isEmpty()) {
	    BigDecimal minEle = Collections.min(elevations);
	    BigDecimal maxEle = Collections.max(elevations);
	    dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().addVerticalExtent(minEle.doubleValue(),
		    maxEle.doubleValue());
	}

	// temporal extent
	TemporalExtent tempExtent = new TemporalExtent();

	if (startDate != null && !startDate.isEmpty() && !startDate.contains("unknown")) {
	    tempExtent.setBeginPosition(startDate);
	}
	if (endDate != null && !endDate.isEmpty() && !endDate.contains("unknown")) {
	    tempExtent.setEndPosition(endDate);
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(endDate);
	    coreMetadata.getMIMetadata().setDateStampAsDate(endDate);

	}

	if (tempExtent.getBeginPosition() != null && tempExtent.getEndPosition() == null) {
	    TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
	    tempExtent.setIndeterminateEndPosition(endTimeInderminate);
	}

	coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(tempExtent);

	ICOSClient client = new ICOSClient();
	// parameters
	for (SimpleEntry<String, String> parameter : parameters) {
	    String s = parameter.getKey();
	    s = s.trim();
	    CoverageDescription description = new CoverageDescription();
	    // description.setAttributeIdentifier(s);
	    description.setAttributeDescription(s);
	    description.setAttributeTitle(s);
	    try {
		String uri = parameter.getValue();
		List<String> uris = client.getEquivalentConcepts(uri);
		if (uris.size() == 1) {
		    uri = uris.get(0);
		} else {
		    GSLoggerFactory.getLogger(getClass()).warn("Normalized URI not found for {}", uri);
		}
		description.setAttributeIdentifier(uri);
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	    coreMetadata.getMIMetadata().addCoverageDescription(description);
	}

	// // INSTRUMENT IDENTIFIERS
	for (int i = 0; i < instrumentTitles.size(); i++) {
	    String label = instrumentTitles.get(i);
	    String uri = instrumentUris.get(i);
	    String comments = instrumentComments.get(i);

	    MIInstrument myInstrument = new MIInstrument();
	    myInstrument.setMDIdentifierTypeIdentifier(label);
	    myInstrument.setMDIdentifierTypeCode(uri);
	    if (comments != null) {
		myInstrument.setDescription(comments);
	    } else {
		myInstrument.setDescription(label + " available at: " + uri);
	    }
	    myInstrument.setTitle(label);
	    coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("instrument");
	    if (uri != null) {
		keyword.addKeyword(label,uri);
	    } else {
		keyword.addKeyword(label);
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	// platform
	if (platformName != null) {
	    MIPlatform platform = new MIPlatform();
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("platform");
	    if (platformCode != null) {
		platform.setMDIdentifierCode(platformCode);
		keyword.addKeyword(platformName, platformCode);
	    } else {
		keyword.addKeyword(platformName);
	    }
	    platform.setDescription(platformName);
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(platformName);
	    platform.setCitation(platformCitation);
	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	if (projectKeyword != null) {
	    Keywords projectKey = new Keywords();
	    projectKey.setTypeCode("project");
	    projectKey.addKeyword(projectKeyword);
	    if (projectURI != null) {
		CharacterStringPropertyType value = ISOMetadata.createAnchorPropertyType(projectURI, projectKeyword);
		List<CharacterStringPropertyType> list = new ArrayList<CharacterStringPropertyType>();
		list.add(value);
		projectKey.getElementType().setKeyword(list);
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(projectKey);
	}

	for (JSONObject authorObject : listIndividuals) {

	    String firstName = getString(authorObject, "firstName");
	    String lastName = getString(authorObject, "lastName");
	    String orcid = getString(authorObject, "orcid");
	    String email = getString(authorObject, "email");
	    String role = getString(authorObject, "role");
	    JSONObject jsonSelf = authorObject.optJSONObject("self");
	    String contactUri = getString(jsonSelf, "uri");

	    ResponsibleParty ownerContact = new ResponsibleParty();
	    ownerContact.setRoleCode(role);

	    ownerContact.setIndividualName(firstName + " " + lastName);
	    Contact contact = new Contact();
	    Address address = new Address();
	    address.addElectronicMailAddress(email);
	    contact.setAddress(address);
	    ownerContact.setContactInfo(contact);

	    if (contactUri != null) {
		CharacterStringPropertyType val = ISOMetadata.createAnchorPropertyType(contactUri, firstName + " " + lastName);
		ownerContact.getElementType().setIndividualName(val);
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(ownerContact);
	}

	if (!respOrgNames.isEmpty()) {
	    for (int i = 0; i < respOrgNames.size(); i++) {
		String label = respOrgNames.get(i);
		String uri = respOrgURINames.get(i);
		String role = respOrgRoles.get(i);
		ResponsibleParty ownerContact = new ResponsibleParty();
		if (role != null && role.equals("originator")) {
		    dataset.getExtensionHandler().addOriginatorOrganisationDescription(label);
		}
		ownerContact.setOrganisationName(label);
		ownerContact.setRoleCode(role);
		if (uri != null) {
		    CharacterStringPropertyType value = ISOMetadata.createAnchorPropertyType(uri, label);
		    ownerContact.getElementType().setOrganisationName(value);
		    dataset.getExtensionHandler().addOriginatorOrganisationIdentifier(label);
		} else {
		    CharacterStringPropertyType value = ISOMetadata.createAnchorPropertyType(null, label);
		    ownerContact.getElementType().setOrganisationName(value);
		}

		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(ownerContact);
	    }
	}

	if (isForGEOSS && accessUrl != null) {
	    Online online = new Online();
	    online.setLinkage(accessUrl);
	    online.setProtocol(NetProtocols.HTTP.getCommonURN());
	    online.setFunctionCode("download");
	    online.setDescription("ICOS Portal Download Page");
	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	}

    }

    private void addCoordinates(List<BigDecimal> longitudes, List<BigDecimal> latitudes, List<BigDecimal> elevations, JSONArray array,
	    String type) {
	if (type != null && type.equals("Polygon")) {
	    array = array.getJSONArray(0);
	}
	if (type != null && type.equals("Point")) {
	    JSONArray tmp = new JSONArray();
	    tmp.put(array);
	    array = tmp;
	}
	for (int i = 0; i < array.length(); i++) {
	    JSONArray coords = array.getJSONArray(i);
	    longitudes.add(coords.getBigDecimal(0));
	    latitudes.add(coords.getBigDecimal(1));
	    if (coords.length() == 3) {
		elevations.add(coords.getBigDecimal(2));
	    }
	}
    }

    private Double getDouble(JSONObject result, String key) {
	try {
	    Double d = result.optDouble(key);
	    if (d == null || d.isNaN()) {
		return null;
	    }
	    return d;
	} catch (Exception e) {
	    logger.warn("Error reading key {}: ", key, e);
	    return null;
	}
    }

    private JSONArray getJSONArray(JSONObject result, String key) {
	try {
	    boolean hasKey = result.has(key);
	    if (!hasKey) {
		return new JSONArray();
	    }
	    JSONArray ret = result.getJSONArray(key);
	    if (ret == null || ret.length() == 0) {
		ret = new JSONArray();
	    }
	    return ret;
	} catch (Exception e) {
	    logger.warn("Error getting json array", e);
	    return new JSONArray();
	}

    }

    private List<String> getStrings(JSONObject result, String key) {
	try {
	    List<String> ret = new ArrayList<>();
	    if (result.has(key)) {
		JSONArray array = result.getJSONArray(key);
		for (int i = 0; i < array.length(); i++) {
		    String s = array.getString(i);
		    ret.add(s);
		}
	    }
	    return ret;
	} catch (Exception e) {
	    logger.warn("Error reading key {}: ", key, e);
	    return null;
	}
    }

    private String getString(JSONObject result, String key) {
	try {
	    String ret = result.optString(key, null);
	    if (ret == null || "".equals(ret) || "[]".equals(ret) || "null".equals(ret)) {
		return null;
	    }
	    return ret;
	} catch (Exception e) {
	    logger.warn("Error reading key {}: ", key, e);
	    return null;
	}
    }

    public static void main(String[] args) {
	String endTime = "2020-12-02T06:06:30.000+0000";
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");// 2020-01-02T06:06:30.000+0000
	df.setTimeZone(TimeZone.getTimeZone("UTC"));
	Date d = null;
	try {
	    d = df.parse(endTime);

	} catch (Exception e) {
	    // TODO: handle exception
	    e.printStackTrace();
	}
	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	cal.setTime(d);
	System.out.println(cal.get(Calendar.YEAR));
	System.out.println(cal.get(Calendar.MONTH));
	System.out.println(cal.get(Calendar.DAY_OF_MONTH));
    }

}
