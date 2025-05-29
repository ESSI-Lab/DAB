package eu.essi_lab.accessor.argo;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
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

public class ARGOMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private String WMS_URL = "https://geoserver.ifremer.fr/geomesa/wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&STYLES&LAYERS=argo_station&exceptions=application%2Fvnd.ogc.se_inimage&CQL_FILTER=platform_code%3D%27ARGO_PLACEHOLDER%27&CRS=EPSG%3A4326&WIDTH=768&HEIGHT=384&BBOX=-90,-180,90,180";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.ARGO_NS_URI;
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

	// logger.debug("STARTED mapping for record number {} .",
	// station.getRecordID());

	JSONObject object = new JSONObject(originalMetadata);

	JSONObject platformObject = object.optJSONObject("platform");

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setHierarchyLevelName("series");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

	String platformName = null;
	String platformCode = null;
	String platformDescription = null;
	String platformType = null;
	if (platformObject != null) {
	    platformName = getString(platformObject, "name");
	    platformCode = getString(platformObject, "code");
	    platformDescription = getString(platformObject, "description");
	    platformType = getString(platformObject, "type");

	    // title
	    if (platformName != null && !platformName.isEmpty()) {
		String title = platformName;
		title = (platformCode != null && !platformCode.isEmpty()) ? title + " - " + platformCode : title;
		title = (platformDescription != null && !platformDescription.isEmpty()) ? title + " - " + platformDescription : title;
		// title = (platformType != null && !platformType.isEmpty()) ? title + " - " +
		// platformType : title;
		coreMetadata.setTitle(title);
		coreMetadata.setAbstract(title);
	    }
	}

	String countryCode = getString(object, "countryCode");

	JSONArray projectsArray = getJSONArray(object, "projects");

	// String projectName = object.getString("projectName");

	String model = getString(object, "model");

	String maker = getString(object, "maker");

	JSONObject deploymentObject = object.optJSONObject("deployment");
	String cruiseName = null;
	String authorName = null;
	Integer deploymentQC = null;
	BigDecimal deploymentLat = null;
	BigDecimal deploymentLon = null;
	if (deploymentObject != null) {
	    cruiseName = getString(deploymentObject, "cruiseName");
	    authorName = getString(deploymentObject, "principalInvestigatorName");
	    deploymentQC = getInteger(deploymentObject, "qc");
	    deploymentLat = getBigDecimal(deploymentObject, "lat");
	    deploymentLon = getBigDecimal(deploymentObject, "lon");
	}

	JSONArray variablesArray = getJSONArray(object, "variables");

	JSONArray calibrationsArray = getJSONArray(object, "calibrations");

	JSONArray sensorArray = getJSONArray(object, "sensors");

	JSONArray cyclesArray = getJSONArray(object, "cycles");

	BigDecimal minLat = null;
	BigDecimal minLon = null;
	BigDecimal maxLat = null;
	BigDecimal maxLon = null;

	if (deploymentQC != null) {
	    switch (deploymentQC) {
	    case 1:
	    case 2:
	    case 5:
	    case 8:
		minLat = deploymentLat;
		minLon = deploymentLon;
		maxLat = deploymentLat;
		maxLon = deploymentLon;
		break;
	    default:
		GSLoggerFactory.getLogger(getClass()).warn("Discarded deployment position, as quality flag is not good: ", deploymentQC);
		break;
	    }
	}

	TreeSet<Date> dates = new TreeSet<>();

	List<SimpleEntry<BigDecimal, BigDecimal>> latLongs = new ArrayList<>();
	for (int i = 0; i < cyclesArray.length(); i++) {
	    JSONObject cycle = cyclesArray.getJSONObject(i);
	    BigDecimal lat = getBigDecimal(cycle, "lat");
	    BigDecimal lon = getBigDecimal(cycle, "lon");
	    Integer positionQc = getInteger(cycle, "positionQc");
	    if (positionQc != null) {
		switch (positionQc) {
		case 1:
		case 2:
		case 5:
		case 8:
		    SimpleEntry<BigDecimal, BigDecimal> latLon = new SimpleEntry<>(lat, lon);
		    latLongs.add(latLon);
		    break;
		default:
		    // as for Thierry e-mail of 20 June 2024, see below for the codes
		    GSLoggerFactory.getLogger(getClass()).warn("Discarded position, as quality flag is not good: ", positionQc);
		    break;
		}
	    }

	    Integer dateQc = getInteger(cycle, "dateQc");
	    if (dateQc != null) {
		switch (dateQc) {
		case 1:
		case 2:
		case 5:
		case 8:
		    Date start = getDate(getString(cycle, "startDate"), platformCode);
		    if (start != null) {
			dates.add(start);
		    }
		    Date end = getDate(getString(cycle, "endDate"), platformCode);
		    if (end != null) {
			dates.add(end);
		    }
		    break;
		default:
		    GSLoggerFactory.getLogger(getClass()).warn("Discarded date, as quality flag is not good: ", dateQc);
		    break;
		}
	    }
	}

	// table 2, pag 73, argo data management user's manual
	// 0 no qc is performed
	// 1 good data
	// 2 probably good data
	// 3 probably bad data that are potentially adjustable
	// 4 bad data
	// 5 value changed
	// 6 not used
	// 7 not used
	// 8 estimated value
	// 9 missing value

	SimpleEntry<SimpleEntry<BigDecimal, BigDecimal>, SimpleEntry<BigDecimal, BigDecimal>> bbox = BBOXUtils.getBigDecimalBBOX(latLongs);
	SimpleEntry<BigDecimal, BigDecimal> lowerCorner = bbox.getKey();
	SimpleEntry<BigDecimal, BigDecimal> upperCorner = bbox.getValue();
	minLat = lowerCorner.getKey();
	minLon = lowerCorner.getValue();
	maxLat = upperCorner.getKey();
	maxLon = upperCorner.getValue();

	String owner = getString(object, "owner");

	JSONObject dataCenterObject = object.optJSONObject("dataCenter");
	String dataCenterName = null;
	if (dataCenterObject != null) {
	    dataCenterName = getString(dataCenterObject, "name");
	}

	JSONObject institutionObject = object.optJSONObject("institution");
	String istitutionName = null;
	if (institutionObject != null) {
	    istitutionName = getString(institutionObject, "name");
	}

	JSONObject latestCycle = object.optJSONObject("latestCycle");
	JSONObject earliestCycle = object.optJSONObject("earliestCycle");

	if (earliestCycle != null) {
	    Integer dateQc = getInteger(earliestCycle, "dateQc");
	    if (dateQc != null) {
		switch (dateQc) {
		case 1:
		case 2:
		case 5:
		case 8:
		    Date start = getDate(getString(earliestCycle, "startDate"), platformCode);
		    if (start != null) {
			dates.add(start);
		    }
		    Date end = getDate(getString(earliestCycle, "endDate"), platformCode);
		    if (end != null) {
			dates.add(end);
		    }
		    break;
		default:
		    GSLoggerFactory.getLogger(getClass()).warn("Discarded date, as quality flag is not good: ", dateQc);
		    break;
		}
	    }

	}
	if (latestCycle != null) {
	    Integer dateQc = getInteger(latestCycle, "dateQc");
	    if (dateQc != null) {
		switch (dateQc) {
		case 1:
		case 2:
		case 5:
		case 8:
		    Date start = getDate(getString(latestCycle, "startDate"), platformCode);
		    if (start != null) {
			dates.add(start);
		    }
		    Date end = getDate(getString(latestCycle, "endDate"), platformCode);
		    if (end != null) {
			dates.add(end);
		    }
		    break;
		default:
		    GSLoggerFactory.getLogger(getClass()).warn("Discarded date, as quality flag is not good: ", dateQc);
		    break;
		}
	    }
	}

	// keywords
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(countryCode);

	for (Object o : projectsArray) {
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("project");
	    keyword.addKeyword(o.toString());
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(maker);
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(model);
	if (cruiseName != null) {
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("cruise");
	    keyword.addKeyword(cruiseName);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword("ARGO");
	// bbox

	if (minLat != null && minLon != null && maxLat != null && maxLon != null) {

	    if (minLat.equals(BigDecimal.ZERO) || minLon.equals(BigDecimal.ZERO) || maxLat.equals(BigDecimal.ZERO)
		    || maxLon.equals(BigDecimal.ZERO)) {
		GSLoggerFactory.getLogger(getClass()).error("Suspect bbox for ARGO float: {}", platformCode);
	    }
	    if (minLat.compareTo(maxLat) == 1) {
		GSLoggerFactory.getLogger(getClass()).error("Wrong bbox for ARGO float: {}", platformCode);
	    }
	    if (maxLat.compareTo(new BigDecimal(90)) == 1 || //
		    minLat.compareTo(new BigDecimal(-90)) == -1 || //
		    maxLon.compareTo(new BigDecimal(180)) == 1 || //
		    minLat.compareTo(new BigDecimal(-180)) == -1 //
	    ) {
		GSLoggerFactory.getLogger(getClass()).error("Wrong bbox for ARGO float: {}", platformCode);
	    }

	    coreMetadata.addBoundingBox(maxLat, minLon, minLat, maxLon);
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("Missing bbox for ARGO float: {}", platformCode);
	}
	// temporal extent
	if (!dates.isEmpty()) {
	    Date startDate = dates.first();
	    Date endDate = dates.last();
	    if (startDate.after(endDate)) {
		GSLoggerFactory.getLogger(getClass()).error("Start date after end date for ARGO platform {}", platformCode);
	    } else {
		String startTime = ISO8601DateTimeUtils.getISO8601DateTime(startDate);
		String endTime = ISO8601DateTimeUtils.getISO8601DateTime(endDate);
		coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(startTime, endTime);
		coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(endTime);
		coreMetadata.getMIMetadata().setDateStampAsDate(endTime);
	    }

	} else {
	    GSLoggerFactory.getLogger(getClass()).error("Missing temporal extent for ARGO float: {}", platformCode);
	}

	// parameters
	// adding extracted information in ISO 19115-2 (where possible) and extended
	// parts
	// PARAMETER IDENTIFIERS

	boolean useVariables = false; // it was decided to use parameter on 4 October 2024

	if (useVariables) {

	    for (Object o : variablesArray) {
		CoverageDescription description = new CoverageDescription();
		// description.setAttributeIdentifier(o.toString());
		description.setAttributeDescription(o.toString());
		description.setAttributeTitle(o.toString());
		coreMetadata.getMIMetadata().addCoverageDescription(description);

	    }

	} else {
	    NVSClient client = new NVSClient();
	    HashMap<String, String> parameters = new HashMap<String, String>();
	    for (int i = 0; i < calibrationsArray.length(); i++) {
		JSONObject calibration = calibrationsArray.getJSONObject(i);
		String r03Code = calibration.getString("parameter");
		String uri = "http://vocab.nerc.ac.uk/collection/R03/current/" + r03Code + "/";
		String label = client.getLabel(uri);
		if (label == null || label.isEmpty()) {
		    label = r03Code;
		    uri = null;
		}
		parameters.put(label, uri);
	    }
	    Set<Entry<String, String>> entries = parameters.entrySet();
	    for (Entry<String, String> entry : entries) {
		String label = entry.getKey();
		String uri = entry.getValue();
		CoverageDescription description = new CoverageDescription();
		if (uri != null) {
		    description.setAttributeIdentifier(uri);
		}
		// description.setAttributeDescription(o.toString());
		description.setAttributeTitle(label);
		description.setAttributeDescription(label);
		coreMetadata.getMIMetadata().addCoverageDescription(description);
	    }

	}

	// instrument
	// // INSTRUMENT IDENTIFIERS
	for (int j = 0; j < sensorArray.length(); j++) {

	    JSONObject jsonObject = sensorArray.getJSONObject(j);
	    String sensorId = getString(jsonObject, "id");
	    String sensorModel = getString(jsonObject, "model");
	    String sensorMaker = getString(jsonObject, "maker");
	    String sensorSerial = getString(jsonObject, "serial");

	    MIInstrument myInstrument = new MIInstrument();
	    myInstrument.setMDIdentifierTypeIdentifier(sensorSerial);
	    myInstrument.setMDIdentifierTypeCode(sensorId);
	    myInstrument.setDescription("Sensor Model: " + sensorModel + ". Maker: " + sensorMaker);
	    myInstrument.setTitle(sensorModel);
	    // myInstrument.getElementType().getCitation().add(e)
	    coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("instrument");
	    keyword.addKeyword(sensorModel);// or sensorModel
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	// platform
	if (platformName != null || platformCode != null || platformDescription != null) {
	    MIPlatform platform = new MIPlatform();
	    platform.setMDIdentifierCode(platformCode);
	    platform.setDescription(platformDescription);
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(platformName);
	    platform.setCitation(platformCitation);
	    coreMetadata.getMIMetadata().addMIPlatform(platform);
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("platform");
	    keyword.addKeyword(platformName);// or platformDescription
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
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

	// organization
	if (authorName != null) {
	    ResponsibleParty principalInvestigator = new ResponsibleParty();
	    principalInvestigator.setIndividualName(authorName);
	    principalInvestigator.setRoleCode("author");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(principalInvestigator);
	}

	if (owner != null) {
	    ResponsibleParty ownerContact = new ResponsibleParty();
	    ownerContact.setOrganisationName(owner);
	    ownerContact.setRoleCode("owner");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(ownerContact);
	}

	if (dataCenterName != null) {
	    ResponsibleParty publisherContact = new ResponsibleParty();
	    publisherContact.setOrganisationName(dataCenterName);
	    publisherContact.setRoleCode("publisher");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);
	}

	if (istitutionName != null) {
	    ResponsibleParty istitutionContact = new ResponsibleParty();
	    istitutionContact.setOrganisationName(istitutionName);
	    istitutionContact.setRoleCode("publisher");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(istitutionContact);
	}

	// if(owner != null || authorName != null || dataCenterName != null ||
	// istitutionName != null ) {
	//
	// ResponsibleParty creatorContact = new ResponsibleParty();
	//// Contact info = new Contact();
	//// Online online = new Online();
	//// online.setLinkage("https://www.ana.gov.br/");
	//// info.setOnline(online);
	//// creatorContact.setContactInfo(info);
	// if(authorName != null)
	// creatorContact.setIndividualName(authorName);
	// if(istitutionName != null) {
	// creatorContact.setOrganisationName(istitutionName);
	// }else if(owner != null){
	// creatorContact.setOrganisationName(owner);
	// } else {
	// creatorContact.setOrganisationName(dataCenterName);
	// }
	// creatorContact.setRoleCode("author");
	// coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
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
