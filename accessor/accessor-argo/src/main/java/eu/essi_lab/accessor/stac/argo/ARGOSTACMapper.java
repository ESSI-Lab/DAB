package eu.essi_lab.accessor.stac.argo;

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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.geo.BBOXUtils;
import eu.essi_lab.lib.net.nvs.NVSClient;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;

public class ARGOSTACMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private String WMS_URL = "https://geoserver.ifremer.fr/geomesa/wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&STYLES&LAYERS=argo_station&exceptions=application%2Fvnd.ogc.se_inimage&CQL_FILTER=platform_code%3D%27ARGO_PLACEHOLDER%27&CRS=EPSG%3A4326&WIDTH=768&HEIGHT=384&BBOX=-90,-180,90,180";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.ARGO_STAC_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	DatasetCollection dataset = new DatasetCollection();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, DatasetCollection dataset) {

	String originalMetadata = originalMD.getMetadata();

	JSONObject object = new JSONObject(originalMetadata);
	// id
	// bbox
	// type
	// links
	// assets
	// geometry
	// collection
	// properties
	// - p
	// - p
	// - p
	// - p
	// stac_version
	// stac_extensions

	String platformId = getString(object, "id");
	JSONArray bboxArray = getJSONArray(object, "bbox");
	JSONArray linksArray = getJSONArray(object, "links");
	JSONObject assetsObj = object.optJSONObject("assets");
	JSONObject geometryObj = object.optJSONObject("geometry");
	JSONObject propertiesObj = object.optJSONObject("properties");

	String title = getString(propertiesObj, "title");
	String PI_name = getString(propertiesObj, "PI_name");
	String mission = getString(propertiesObj, "mission");
	String sci_doi = getString(propertiesObj, "sci:doi");
	String platformCode = getString(propertiesObj, "platform");
	String parameters = getString(propertiesObj, "parameters");
	String description = getString(propertiesObj, "description");
	String manual_version = getString(propertiesObj, "manual_version");
	String constellation = getString(propertiesObj, "constellation");
	String platform_type = getString(propertiesObj, "platform_type");
	String wmo_inst_type = getString(propertiesObj, "wmo_inst_type");
	String platform_maker = getString(propertiesObj, "platform_maker");
	String start_datetime = getString(propertiesObj, "start_datetime");
	String end_datetime = getString(propertiesObj, "end_datetime");
	String updated_datetime = getString(propertiesObj, "updated");
	String deployment_ship = getString(propertiesObj, "deployment_ship");
	String float_serial_no = getString(propertiesObj, "float_serial_no");
	String firmware_version = getString(propertiesObj, "firmware_version");
	String positioning_system = getString(propertiesObj, "positioning_system");
	String deployment_cruise_id = getString(propertiesObj, "deployment_cruise_id");

	JSONArray columnParameters = getJSONArray(propertiesObj, "table:columns");

	JSONArray instruments = getJSONArray(propertiesObj, "instruments");
	JSONArray providers = getJSONArray(propertiesObj, "providers");

	JSONArray themes = getJSONArray(propertiesObj, "themes");

	// bbox
	BigDecimal north = null;
	BigDecimal south = null;
	BigDecimal west = null;
	BigDecimal east = null;
	if (bboxArray.length() == 4) {
	    west = bboxArray.getBigDecimal(0);
	    south = bboxArray.getBigDecimal(1);
	    east = bboxArray.getBigDecimal(2);
	    north = bboxArray.getBigDecimal(3);
	}

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().setHierarchyLevelName("series");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

	// title
	if (title != null) {
	    title = (description != null && !description.isEmpty()) ? title + " - " + description : title;
	    title = (mission != null && !mission.isEmpty()) ? title + " - " + mission : title;
	    coreMetadata.setTitle(title);
	    coreMetadata.setAbstract(title);
	}

	// keywords -
	// mission -> project
	if (mission != null) {
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("project");
	    keyword.addKeyword(mission);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(maker);
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(model);
	if (deployment_cruise_id != null) {
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("cruise");
	    keyword.addKeyword(deployment_cruise_id);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	// bbox
	if (west != null && east != null && north != null & south != null) {
	    coreMetadata.addBoundingBox(north, west, south, east);
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("Missing bbox for ARGO float: {}", platformCode);
	}

	// temporal extent
	TemporalExtent extent = new TemporalExtent();
	if (start_datetime != null && !start_datetime.isEmpty()) {
	    extent.setBeginPosition(start_datetime);
	    if (end_datetime != null && !end_datetime.isEmpty()) {
		extent.setEndPosition(end_datetime);
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);

	} else {
	    GSLoggerFactory.getLogger(getClass()).error("Missing temporal extent for ARGO float: {}", platformCode);
	}
	if (updated_datetime != null && !updated_datetime.isEmpty()) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(updated_datetime);
	    coreMetadata.getMIMetadata().setDateStampAsDate(updated_datetime);
	}

	// parameters
	// adding extracted information in ISO 19115-2 (where possible) and extended
	// parts
	// PARAMETER IDENTIFIERS
	String platform_family = null;

	MIPlatform platform = null;
	for (int i = 0; i < themes.length(); i++) {
	    JSONObject themeObj = themes.getJSONObject(i);
	    if (themeObj != null && !themeObj.isEmpty()) {
		String conceptScheme = getString(themeObj, "scheme");
		JSONArray concepts = getJSONArray(themeObj, "concepts");
		for (int j = 0; j < concepts.length(); j++) {
		    JSONObject concept = concepts.getJSONObject(j);
		    String conceptId = getString(concept, "id");
		    String conceptURL = getString(concept, "url");
		    String conceptTitle = getString(concept, "title");
		    String conceptDescription = getString(concept, "description");

		    switch (conceptScheme) {
		    // R03 -> parameters
		    case "http://vocab.nerc.ac.uk/collection/R03/current/":
			CoverageDescription coverage = new CoverageDescription();
			coverage.setAttributeIdentifier(conceptURL);
			coverage.setAttributeDescription(conceptDescription);
			coverage.setAttributeTitle(conceptId);
			Keywords key = new Keywords();
			key.addKeyword(conceptId, conceptURL);
			coreMetadata.getMIMetadata().getDataIdentification().addKeywords(key);
			coreMetadata.getMIMetadata().addCoverageDescription(coverage);
			break;
		    // R04 -> organizations
		    case "http://vocab.nerc.ac.uk/collection/R04/current/":
			ResponsibleParty organization = new ResponsibleParty();
			CharacterStringPropertyType value = ISOMetadata.createAnchorPropertyType(conceptURL, conceptId);
			organization.getElementType().setOrganisationName(value);
			organization.setRoleCode("originator");
			// organization.setRoleCode("author");
			coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(organization);
			dataset.getExtensionHandler().addOriginatorOrganisationDescription(conceptDescription);
			dataset.getExtensionHandler().addOriginatorOrganisationIdentifier(conceptURL);
			break;
		    // R25 -> sensors
		    case "http://vocab.nerc.ac.uk/collection/R25/current/":
			MIInstrument myInstrument = new MIInstrument();
			myInstrument.setMDIdentifierTypeIdentifier(conceptId);
			myInstrument.setMDIdentifierTypeCode(conceptURL);
			if (conceptDescription != null) {
			    myInstrument.setDescription(conceptDescription);
			}
			myInstrument.setTitle(conceptId);
			coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
			Keywords keyword = new Keywords();
			keyword.setTypeCode("instrument");
			keyword.addKeyword(conceptId, conceptURL);
			keyword.addKeyword(conceptId);
			coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

			break;
		    // R26 -> other sensors
		    case "http://vocab.nerc.ac.uk/collection/R26/current/":
			// MIInstrument myInstrument26 = new MIInstrument();
			// myInstrument26.setMDIdentifierTypeIdentifier(conceptId);
			// myInstrument26.setMDIdentifierTypeCode(conceptURL);
			// if (conceptDescription != null) {
			// myInstrument26.setDescription(conceptDescription);
			// }
			// myInstrument26.setTitle(conceptId);
			// coreMetadata.getMIMetadata().addMIInstrument(myInstrument26);
			// Keywords keyword26 = new Keywords();
			// keyword26.setTypeCode("instrument");
			// keyword26.addKeyword(conceptId, conceptURL);
			// coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword26);
			break;
		    // R27 -> other sensors
		    case "http://vocab.nerc.ac.uk/collection/R27/current/":
			// MIInstrument myInstrument27 = new MIInstrument();
			// myInstrument27.setMDIdentifierTypeIdentifier(conceptId);
			// myInstrument27.setMDIdentifierTypeCode(conceptURL);
			// if (conceptDescription != null) {
			// myInstrument27.setDescription(conceptDescription);
			// }
			// myInstrument27.setTitle(conceptId);
			// coreMetadata.getMIMetadata().addMIInstrument(myInstrument27);
			// Keywords keyword27 = new Keywords();
			// keyword27.setTypeCode("instrument");
			// keyword27.addKeyword(conceptId, conceptURL);
			// coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword27);
			break;
		    // R022 -> platform family
		    case "http://vocab.nerc.ac.uk/collection/R22/current/":
			// platform_family = conceptId;
			// MIPlatform platform22 = new MIPlatform();
			// platform22.setMDIdentifierCode(platformCode);
			// if (conceptDescription != null) {
			// platform22.setDescription(conceptDescription);
			// }
			// Citation platformCitation22 = new Citation();
			// platformCitation22.setTitle(conceptTitle);
			// platform22.setCitation(platformCitation22);
			// coreMetadata.getMIMetadata().addMIPlatform(platform22);
			// Keywords keyword22 = new Keywords();
			// keyword22.setTypeCode("platform");
			// keyword22.addKeyword(conceptId, conceptURL);
			// coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword22);
			break;
		    // R23 -> platform type
		    case "http://vocab.nerc.ac.uk/collection/R23/current/":
			platform = new MIPlatform();
			platform.setMDIdentifierCode(platformCode);
			if (conceptDescription != null) {
			    platform.setDescription(conceptDescription);
			}
			Citation platformCitation23 = new Citation();
			platformCitation23.setTitle(conceptId);
			platform.setCitation(platformCitation23);
			coreMetadata.getMIMetadata().addMIPlatform(platform);
			Keywords keyword23 = new Keywords();
			keyword23.setTypeCode("platform");
			keyword23.addKeyword(conceptId, conceptURL);
			coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword23);
			break;
		    // R24 -> platform maker
		    case "http://vocab.nerc.ac.uk/collection/R24/current/":

			break;
		    // R08 -> WMO instrument type
		    case "http://vocab.nerc.ac.uk/collection/R08/current/":

			break;
		    // R10 -> transmission frequency
		    case "http://vocab.nerc.ac.uk/collection/R10/current/":

			break;
		    // R10 -> positioning system ARGOS
		    case "http://vocab.nerc.ac.uk/collection/R09/current/":

			break;

		    default:
			break;
		    }

		}
	    }
	}

	if (platform != null) {
	    String platTitle = platform.getCitation().getTitle();
	    if (platform_maker != null) {
		platTitle = platform_maker + " " + platTitle;
	    }
	    if (constellation != null) {
		platTitle = platTitle + " " + constellation;
	    }
	    platform.getCitation().setTitle(platTitle);
	}

	// responsible parties

	if (PI_name != null) {
	    ResponsibleParty principalInvestigator = new ResponsibleParty();
	    principalInvestigator.setIndividualName(PI_name);
	    principalInvestigator.setRoleCode("owner");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(principalInvestigator);
	}

	if (providers != null) {
	    for (int j = 0; j < providers.length(); j++) {

		JSONObject provObj = providers.getJSONObject(j);
		if (provObj != null) {
		    String provName = getString(provObj, "name");
		    if (!provName.equals(PI_name)) {
			String descrProv = getString(provObj, "description");
			if (descrProv == null) {
			    ResponsibleParty respParty = new ResponsibleParty();
			    respParty.setOrganisationName(provName);
			    JSONArray roles = getJSONArray(provObj, "roles");
			    if (!roles.isEmpty()) {
				String role = roles.getString(0);
				if (role.equals("host")) {
				    respParty.setRoleCode("publisher");
				} else if (role.equals("producer")) {
				    respParty.setRoleCode("originator");
				} else {
				    respParty.setRoleCode(role);
				}
			    }
			    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(respParty);
			}
		    }
		}
	    }
	}

	// units of measures
	if (!columnParameters.isEmpty()) {
	    for (int i = 0; i < columnParameters.length(); i++) {
		JSONObject parameterObj = columnParameters.getJSONObject(i);
		String units = getString(parameterObj, "units");
		if (units != null) {
		    dataset.getExtensionHandler().setAttributeUnits(units);
		    dataset.getExtensionHandler().setAttributeUnitsAbbreviation(units);
		}
	    }
	}

	if (platformCode != null && !platformCode.isEmpty()) {
	    // OVERVIEW

	    String overview = WMS_URL.replace("ARGO_PLACEHOLDER", platformCode);

	    BrowseGraphic graphic = new BrowseGraphic();
	    graphic.setFileDescription(coreMetadata.getTitle());
	    graphic.setFileName(overview);
	    graphic.setFileType("image/png");
	    coreMetadata.getMIMetadata().getDataIdentification().addGraphicOverview(graphic);

	    // IDENTIFIER
	    try {
		String identifier = StringUtils.hashSHA1messageDigest(platformCode);
		coreMetadata.setIdentifier(identifier);
		coreMetadata.getMIMetadata().setFileIdentifier(identifier);
		coreMetadata.getDataIdentification().setResourceIdentifier(platformCode);
	    } catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	// ONLINE INFORMATION LINKS
	if (!linksArray.isEmpty()) {
	    for (int i = 0; i < linksArray.length(); i++) {
		JSONObject linkObj = linksArray.getJSONObject(i);
		String relation = getString(linkObj, "rel");
		if (relation != null && !relation.isEmpty()) {
		    Online online = new Online();
		    String href = getString(linkObj, "href");
		    online.setLinkage(href);
		    online.setFunctionCode("information");
		    if (relation.contains("related")) {
			String t = getString(linkObj, "title");
			online.setName(t);
			coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
		    } else if (relation.contains("cite-as")) {
			online.setName("DOI");
			coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
		    }
		}
	    }
	}

	// ONLINE DOWNLOAD LINKS
	if (!assetsObj.isEmpty()) {

	    JSONObject fileObj0 = assetsObj.optJSONObject("Platform_File0");
	    JSONObject fileObj1 = assetsObj.optJSONObject("Platform_File1");
	    JSONObject fileObj2 = assetsObj.optJSONObject("Platform_File2");
	    JSONObject fileObj3 = assetsObj.optJSONObject("Platform_File3");

	    if (fileObj0 != null) {
		String linkage = getString(fileObj0, "href");
		String t0 = getString(fileObj0, "title");
		Online online = new Online();
		online.setLinkage(linkage);
		online.setFunctionCode("download");
		online.setName(t0);
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (fileObj1 != null) {
		String linkage = getString(fileObj1, "href");
		String t0 = getString(fileObj1, "title");
		Online online = new Online();
		online.setLinkage(linkage);
		online.setFunctionCode("download");
		online.setName(t0);
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (fileObj2 != null) {
		String linkage = getString(fileObj2, "href");
		String t0 = getString(fileObj2, "title");
		Online online = new Online();
		online.setLinkage(linkage);
		online.setFunctionCode("download");
		online.setName(t0);
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (fileObj3 != null) {
		String linkage = getString(fileObj3, "href");
		String t0 = getString(fileObj3, "title");
		Online online = new Online();
		online.setLinkage(linkage);
		online.setFunctionCode("download");
		online.setName(t0);
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }

	}

	// organization
	// if (authorName != null) {
	// ResponsibleParty principalInvestigator = new ResponsibleParty();
	// principalInvestigator.setIndividualName(authorName);
	// principalInvestigator.setRoleCode("author");
	// coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(principalInvestigator);
	// }
	//
	// if (owner != null) {
	// ResponsibleParty ownerContact = new ResponsibleParty();
	// ownerContact.setOrganisationName(owner);
	// ownerContact.setRoleCode("owner");
	// coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(ownerContact);
	// }
	//
	// if (dataCenterName != null) {
	// ResponsibleParty publisherContact = new ResponsibleParty();
	// publisherContact.setOrganisationName(dataCenterName);
	// publisherContact.setRoleCode("publisher");
	// coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);
	// }
	//
	// if (istitutionName != null) {
	// ResponsibleParty istitutionContact = new ResponsibleParty();
	// istitutionContact.setOrganisationName(istitutionName);
	// istitutionContact.setRoleCode("publisher");
	// coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(istitutionContact);
	// }

    }

    private Date getDate(String date, String platformCode) {
	Date d = null;
	try {
	    d = ISO8601DateTimeUtils.parseISO8601(date);
	    Date earlyDate = ISO8601DateTimeUtils.parseISO8601("1600-01-01");
	    Date futureDate = ISO8601DateTimeUtils.parseISO8601("2200-01-01");
	    if (d.before(earlyDate) || d.after(futureDate)) {
		GSLoggerFactory.getLogger(getClass()).error("Suspect temporal extent for ARGO float: {}", platformCode);
		return null;
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unparsable temporal extent for ARGO float {}: {}", platformCode, date);
	}
	return d;
    }

    private BigDecimal getBigDecimal(JSONObject result, String key) {
	try {
	    BigDecimal d = result.optBigDecimal(key, null);
	    return d;
	} catch (Exception e) {
	    logger.warn("Error reading key {}: ", key, e);
	    return null;
	}
    }

    private Integer getInteger(JSONObject result, String key) {
	try {
	    Integer i = result.optIntegerObject(key);
	    if (i == null) {
		return null;
	    }
	    return i;
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
