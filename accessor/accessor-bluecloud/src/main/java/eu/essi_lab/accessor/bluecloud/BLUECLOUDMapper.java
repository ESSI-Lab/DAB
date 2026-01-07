package eu.essi_lab.accessor.bluecloud;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

public class BLUECLOUDMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    // private String WMS_URL =
    // "https://geoserver.ifremer.fr/geomesa/wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&STYLES&LAYERS=argo_station&exceptions=application%2Fvnd.ogc.se_inimage&CQL_FILTER=platform_code%3D%27ARGO_PLACEHOLDER%27&CRS=EPSG%3A4326&WIDTH=768&HEIGHT=384&BBOX=-90,-180,90,180";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.BLUECLOUD_API;
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

	// logger.debug("STARTED mapping for record number {} .", station.getRecordID());

	JSONObject object = new JSONObject(originalMetadata);

	String source = getString(object, "Source");

	String lastUpdate = getString(object, "Last_Update");

	String title = getString(object, "Title");
	String abs = getString(object, "Abstract");

	String id = getString(object, "Identifier");

	JSONArray keywordsArray = getJSONArray(object, "Keywords");

	JSONArray parametersArray = getJSONArray(object, "Parameters");

	JSONArray instrumentsArray = getJSONArray(object, "Instruments");

	JSONArray platformsArray = getJSONArray(object, "Platforms");

	JSONArray organizationArray = getJSONArray(object, "Organisations");

	Double minLat = getDouble(object, "Bounding_Box_SouthLatitude");
	Double minLon = getDouble(object, "Bounding_Box_WestLongitude");
	Double maxLat = getDouble(object, "Bounding_Box_NorthLatitude");
	Double maxLon = getDouble(object, "Bounding_Box_EastLongitude");

	String startTime = getString(object, "Temporal_Extent_Begin");
	String endTime = getString(object, "Temporal_Extent_End");

	String onlineResource = getString(object, "OnlineResourceUrl");

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setHierarchyLevelName("series");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

	if (id != null && !id.isEmpty()) {
	    coreMetadata.getMIMetadata().getDataIdentification().setResourceIdentifier(id);
	    coreMetadata.setIdentifier(id);
	    coreMetadata.getMIMetadata().setFileIdentifier(generateCode(dataset, id));
	    dataset.setOriginalId(id);
	}

	// title
	if (title != null && !title.isEmpty()) {
	    coreMetadata.setTitle(title);
	}

	if (abs != null && !abs.isEmpty()) {
	    coreMetadata.setAbstract(abs);
	}

	// keywords
	if (keywordsArray != null && keywordsArray.length() > 0) {
	    for (int i = 0; i < keywordsArray.length(); i++) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(keywordsArray.get(i).toString());
	    }
	}

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(source);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Blue-Cloud");
	// bbox

	if (minLat != null && minLon != null && maxLat != null && maxLon != null) {
	    coreMetadata.addBoundingBox(maxLat, minLon, minLat, maxLon);
	}
	// temporal extent
	if (startTime != null && endTime != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(startTime, endTime);
	}

	if (lastUpdate != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(lastUpdate);
	    coreMetadata.getMIMetadata().setDateStampAsDate(lastUpdate);
	}

	// parameters
	// adding extracted information in ISO 19115-2 (where possible) and extended parts
	// PARAMETER IDENTIFIERS

	for (Object o : parametersArray) {
	    CoverageDescription description = new CoverageDescription();
	    //description.setAttributeIdentifier(o.toString());
	    description.setAttributeDescription(o.toString());
	    description.setAttributeTitle(o.toString());
	    coreMetadata.getMIMetadata().addCoverageDescription(description);
	}

	// instrument
	// // INSTRUMENT IDENTIFIERS
	for (int j = 0; j < instrumentsArray.length(); j++) {

	    MIInstrument myInstrument = new MIInstrument();
	    String instrumentName = instrumentsArray.get(j).toString();
	    myInstrument.setMDIdentifierTypeIdentifier(instrumentName);
	    myInstrument.setMDIdentifierTypeCode(instrumentName);
	    myInstrument.setDescription("Sensor Model: " + instrumentName);
	    myInstrument.setTitle(instrumentName);
	    // myInstrument.getElementType().getCitation().add(e)
	    coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("instrument");
	    keyword.addKeyword(instrumentName);// or sensorModel
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	// platform
	for (int j = 0; j < platformsArray.length(); j++) {
	    String platformName = platformsArray.get(j).toString();
	    MIPlatform platform = new MIPlatform();
	    platform.setMDIdentifierCode(platformName);
	    platform.setDescription(platformName);
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(platformName);
	    platform.setCitation(platformCitation);
	    coreMetadata.getMIMetadata().addMIPlatform(platform);
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("platform");
	    keyword.addKeyword(platformName);// or platformDescription
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	// organization
	for (Object o : organizationArray) {
	    ResponsibleParty publisherContact = new ResponsibleParty();
	    publisherContact.setOrganisationName(o.toString());
	    publisherContact.setRoleCode("publisher");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);
	}

	if (onlineResource != null && !onlineResource.isEmpty()) {
	    Online online = new Online();
	    online.setLinkage(onlineResource);
	    online.setProtocol("WWW:DOWNLOAD-1.0-http--download");
	    online.setFunctionCode("download");
	    online.setDescription("Direct Download");
	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
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
