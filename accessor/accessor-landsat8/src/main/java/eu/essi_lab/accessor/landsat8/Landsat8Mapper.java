/**
 *
 */
package eu.essi_lab.accessor.landsat8;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;

import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.net.protocols.impl.HTTPProtocol;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.XMLGregorianCalendarUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.SatelliteScene;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Fabrizio
 */
public class Landsat8Mapper extends OriginalIdentifierMapper {

    public static final String LANDSAT_8_SCHEME_URI = "landsat8-scheme-uri";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	try {
	    Properties props = new Properties();
	    props.load(new ByteArrayInputStream(resource.getOriginalMetadata().getMetadata().getBytes(StandardCharsets.UTF_8)));

	    String sceneId = props.getProperty("LANDSAT_SCENE_ID");
	    if (Objects.nonNull(sceneId) && !sceneId.isEmpty()) {

		return sceneId.replace("\"", "");
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Properties props = new Properties();
	try {
	    props.load(new ByteArrayInputStream(originalMD.getMetadata().getBytes(StandardCharsets.UTF_8)));
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Can't load original metadata file");
	}

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	// ---------------
	//
	// scene id
	//

	String sceneId = props.getProperty("LANDSAT_PRODUCT_ID");
	if (Objects.isNull(sceneId) || sceneId.isEmpty()) {
	    sceneId = props.getProperty("LANDSAT_SCENE_ID").replace("\"", "");
	} else {
	    sceneId = sceneId.replace("\"", "");
	}

	MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	// -----------------
	//
	// time stamp
	//
	//
	String ingDate = props.getProperty("FILE_DATE").replace("\"", "");
	Date iso8601 = ISO8601DateTimeUtils.parseISO8601(ingDate);
	try {
	    miMetadata.setDateStampAsDateTime(XMLGregorianCalendarUtils.createGregorianCalendar(iso8601));
	} catch (DatatypeConfigurationException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Can't parse date {}", iso8601);
	}

	// ---------------
	//
	// parent
	//
	String parent = props.getProperty("PARENTID").replace("\"", "");
	miMetadata.setParentIdentifier(parent);

	// ---------------
	//
	// thumbnail
	//
	String txtLink = props.getProperty("TXTLINK");
	String thumb = txtLink.replace("_MTL.txt", "_thumb_small.jpg");

	BrowseGraphic browseGraphic = new BrowseGraphic();
	browseGraphic.setFileDescription("Overview of Landsat8 scene " + sceneId);
	browseGraphic.setFileName(thumb);
	miMetadata.getDataIdentification().addGraphicOverview(browseGraphic);

	// ---------------
	//
	// bbox
	//
	Double south = Double.valueOf(props.getProperty("CORNER_LL_LAT_PRODUCT").replace("\"", ""));
	Double west = Double.valueOf(props.getProperty("CORNER_LL_LON_PRODUCT").replace("\"", ""));
	Double north = Double.valueOf(props.getProperty("CORNER_UR_LAT_PRODUCT").replace("\"", ""));
	Double east = Double.valueOf(props.getProperty("CORNER_UR_LON_PRODUCT").replace("\"", ""));

	dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(north, west, south, east);

	// ---------------
	//
	// time
	//
	String date = props.getProperty("DATE_ACQUIRED").replace("\"", "");
	String time = null;

	if (props.getProperty("SCENE_CENTER_TIME") != null) {
	    time = props.getProperty("SCENE_CENTER_TIME").replace("\"", "");
	    time = date + "T" + time.subSequence(0, 8);// 2016-05-13'T'09:19:05
	}
	dataset.getHarmonizedMetadata().getCoreMetadata().addTemporalExtent(time, time);

	// ----------------
	//
	// sensor identifier
	//
	//
	String sensorId = props.getProperty("SENSOR_ID").replace("\"", "");
	sensorId = sensorId.replace("\"", "");

	MIInstrument miInstrument = new MIInstrument();
	miInstrument.setMDIdentifierTypeCode(sensorId);
	miMetadata.addMIInstrument(miInstrument);

	// -------------------
	//
	// sensor title
	//
	String instrumentTitle = createSensorTitle(sensorId);
	miInstrument.setTitle(instrumentTitle);

	// ----------------
	//
	// sensor platform
	//
	//
	String platformId = props.getProperty("SPACECRAFT_ID").replace("\"", "");
	MIPlatform miPlatform = new MIPlatform();
	miPlatform.setMDIdentifierCode(platformId);
	miPlatform.setDescription("Platform " + platformId);
	miMetadata.addMIPlatform(miPlatform);

	// ------------------
	//
	// online
	//
	//
	dataset.getHarmonizedMetadata().getCoreMetadata().addDistributionFormat("GEOTIFF");

	//
	// this is commented according to the implementation of the GI-cat clone production
	// which is fully replicated in the addDirectDownload method (where only one of the
	// 2 types of link is added instead of both)
	//

	// List<String> fileNameBands = props.keySet().//
	// stream().//
	// filter(k -> k.toString().startsWith("FILE_NAME_BAND_")).//
	// map(k -> props.getProperty(k.toString()).replace("\"", "")).//
	// collect(Collectors.toList());
	//
	// for (String fileName : fileNameBands) {
	//
	// String lastPart = fileName.substring(fileName.indexOf('_'), fileName.length());
	// String link = txtLink.replace("_MTL.txt", lastPart);
	//
	// dataset.getHarmonizedMetadata().getCoreMetadata().addDistributionOnlineResource(fileName, link, "HTTP-GET",
	// "download");
	// }

	addDirectDownload(sceneId, miMetadata);

	// ----------------------
	//
	// cloud cover percentage
	//
	//
	Double cloud = Double.valueOf(props.getProperty("CLOUD_COVER").replace("\"", ""));
	miMetadata.addCloudCoverPercentage(cloud);

	// ------------------------
	//
	// SatelliteScene extension
	//
	//
	SatelliteScene satelliteScene = new SatelliteScene();
	satelliteScene.setOrigin("landsat");

	Integer row = Integer.valueOf(props.getProperty("WRS_ROW").replace("\"", ""));
	satelliteScene.setRow(row);

	Integer path = Integer.valueOf(props.getProperty("WRS_PATH").replace("\"", ""));
	satelliteScene.setPath(path);

	String productType = props.getProperty("DATA_TYPE").replace("\"", "");
	satelliteScene.setProductType(productType);

	String queryables = "prodType,";
	queryables += "cloudcp,";
	queryables += "pubDatefrom,";
	queryables += "pubDateuntil,";
	queryables += "row,";
	queryables += "path";

	satelliteScene.setCollectionQueryables(queryables);

	dataset.getExtensionHandler().setSatelliteScene(satelliteScene);

	// -----------------
	//
	// title
	//
	//
	String title = platformId + " " + //
		(row != null ? "- Row " + row + " " : "") + //
		(path != null ? "- Path " + path + " " : "") + //
		(time != null ? "- Day " + time.substring(0, 10) + " " : "") + //
		"(" + sceneId + ")";

	dataset.getHarmonizedMetadata().getCoreMetadata().setTitle(title);

	// -------------------
	//
	// organization
	//
	//
	ResponsibleParty responsibleParty = new ResponsibleParty();
	responsibleParty.setRoleCode("originator");
	responsibleParty.setOrganisationName("United States Geological Survey");

	miMetadata.getDataIdentification().addPointOfContact(responsibleParty);

	return dataset;
    }

    private void addDirectDownload(String sceneId, MIMetadata mi_Metadata) {

	String first = sceneId.substring(3, 6);
	String second = sceneId.substring(6, 9);

	String link = "https://s3-us-west-2.amazonaws.com/landsat-pds/L8/" + first + "/" + second + "/" + sceneId + "/" + sceneId + "_";// 085/106/LC80851062015111LGN00/index.html";

	if (sceneId.contains("_")) {// LC08_L1TP_231090_20170511_20170525_01_T1
	    first = sceneId.substring(10, 13);
	    second = sceneId.substring(13, 16);

	    link = "https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/" + first + "/" + second + "/" + sceneId + "/" + sceneId + "_";
	}

	for (int i = 1; i < 12; i++) {

	    String band = "B" + i + ".TIF";

	    Online onLine = new Online();
	    onLine.setProtocol(new HTTPProtocol().getCommonURN());
	    onLine.setLinkage(link + band);
	    onLine.setName("Band " + i);
	    onLine.setFunctionCode("download");

	    mi_Metadata.getDistribution().addDistributionOnline(onLine);
	}
    }

    /**
     * @param sensorId
     * @return
     */
    private String createSensorTitle(String sensorId) {

	String ret = null;

	if (sensorId != null && sensorId.toLowerCase().contains("oli")) {
	    ret = "Operational Land Imager";
	}

	if (sensorId != null && sensorId.toLowerCase().contains("tirs")) {
	    if (ret == null) {
		ret = "";
	    } else {
		ret += " ";
	    }
	    ret += "Thermal Infrared Sensor";
	}

	return ret;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return LANDSAT_8_SCHEME_URI;
    }
}
