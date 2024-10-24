package eu.essi_lab.accessor.copernicus.dataspace.distributed;

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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.xpath.XPathExpressionException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Node;

import eu.essi_lab.accessor.sentinel.SatelliteLayers;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.impl.HTTPProtocol;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.SatelliteScene;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author roncella
 */
public class CopernicusDataspaceGranulesMapper extends OriginalIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(CopernicusDataspaceGranulesMapper.class);

    private static final String IDENTIFIER = "id";
    private static final String PROPERTIES = "properties";
    private static final String START_TIME = "startDate";
    private static final String END_TIME = "completionDate";
    private static final String GEOMETRY = "geometry";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String PRODUCT_TYPE = "productType";
    private static final String PRODUCT_IDENTIFIER = "productIdentifier";
    private static final String ORGANIZATION_NAME = "organisationName";
    private static final String PROCESSING_LEVEL = "processingLevel";
    private static final String PLATFORM = "platform";
    private static final String INSTRUMENT = "instrument";
    private static final String RESOLUTION = "resolution";
    private static final String SENSOR_MODE = "sensorMode";
    private static final String ORBIT_NUMBER = "orbitNumber";
    private static final String THUMBNAIL = "thumbnail";
    private static final String CLOUD_COVER = "cloudCover";
    private static final String ORBIT_DIRECTION = "orbitDirection";
    private static final String TIMELINESS = "timeliness";
    private static final String RELATIVE_ORBIT_NUMBER = "relativeOrbitNumber";
    private static final String LINKS = "links";
    private static final String SERVICES = "services";
    private static final String DOWNLOAD = "download";
    private static final String PUBLISHED = "published";
    private static final String UPDATED = "updated";
    private static final String SWATH = "swath";
    private static final String COLLECTION = "collection";
    private static final String URL = "url";
    private static final String MIMETYPE = "mimeType";
    private static final String SIZE = "size";
    private static final String POLARISATION = "polarisation";
    private static final String MISSION_ID = "missionTakeId";
    private static final String STATUS = "status";
    private static final String PROCESSING_BASELINE = "processingBaseline";

    private static final String TITLE_XPATH = "//*:title";
    private static final String REVISION_XPATH = "//*:updated";
    private static final String ABSTRACT_XPATH = "//*:summary";
    private static final String THUMBNAIL_XPATH = "//*:link[@rel='icon']/@href";
    private static final String BBOX_XPATH = "//georss:box";
    private static final String BBOX_POLYGON_XPATH = "//georss:polygon";
    private static final String LINKS_XPATH = "//*:link";
    // private static final String DIRECT_DOWNLOAD_XPATH = "//*:link[@rel='enclosure']/@href";
    private static final String TEMPRAL_EXTENT_XPATH = "//*:date";
    private static final String CANT_READ_GRANULE = "Exception reading original atom granule";
    private static final String NEXTGEOSS_MAPPER_ORIGINAL_MD_READ_ERROR = "NEXTGEOSS_MAPPER_ORIGINAL_MD_READ_ERROR";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject resourceObj = new JSONObject(resource.getOriginalMetadata().getMetadata());
	return resourceObj.optString(IDENTIFIER);

    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	GSResource dataset = new Dataset();

	// id
	// Optional<String> optional = read(reader, IDENTIFIER_XPATH);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	miMetadata.setHierarchyLevelName("dataset");

	miMetadata.setLanguage("eng");

	dataset.setSource(source);

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	String id = null;
	JSONObject geometryObj = null;
	JSONObject propertiesObj = null;

	JSONObject jsonObj = new JSONObject(originalMD.getMetadata());

	if (jsonObj != null) {
	    // identifier
	    id = jsonObj.optString(IDENTIFIER);

	    // properties
	    propertiesObj = jsonObj.optJSONObject(PROPERTIES);
	    // geometry/bbox

	    geometryObj = jsonObj.optJSONObject(GEOMETRY);
	    List<Double> coordList = new ArrayList<Double>();
	    if (geometryObj != null) {
		JSONArray coordinates = geometryObj.optJSONArray("coordinates");
		String type = geometryObj.optString("type");

		if (coordinates != null) {
		    JSONArray coords = coordinates.getJSONArray(0);
		    if (type.toLowerCase().contains("multi")) {
			for (int i = 0; i < coords.length(); i++) {
			    JSONArray polygons = coords.getJSONArray(i);
			    for (int k = 0; k < polygons.length(); k++) {
				JSONArray points = polygons.getJSONArray(k);
				for (int h = 0; h < points.length(); h++) {
				    coordList.add(points.getDouble(h));
				}

			    }
			}
		    } else {

			for (int h = 0; h < coords.length(); h++) {
			    JSONArray points = coords.getJSONArray(h);
			    for (int i = 0; i < points.length(); i++) {
				coordList.add(points.getDouble(i));
			    }

			}
		    }
		}
	    }

	    if (!coordList.isEmpty()) {
		String bboxExtent = toBBOX(coordList, false);

		double west = Double.valueOf(bboxExtent.split(" ")[1]);
		double east = Double.valueOf(bboxExtent.split(" ")[3]);
		double north = Double.valueOf(bboxExtent.split(" ")[2]);
		double south = Double.valueOf(bboxExtent.split(" ")[0]);

		coreMetadata.addBoundingBox(north, west, south, east);
	    }

	    if (propertiesObj != null) {
		//
		// time
		//
		String startTime = propertiesObj.optString(START_TIME);
		String endTime = propertiesObj.optString(END_TIME);
		coreMetadata.addTemporalExtent(startTime, endTime);
		//
		// title
		//
		String title = propertiesObj.optString(TITLE);
		if (checkString(title)) {
		    coreMetadata.setTitle(title);
		}
		//
		// abstract
		//
		String abstrakt = propertiesObj.optString(DESCRIPTION);
		if (checkString(abstrakt)) {
		    coreMetadata.setAbstract(abstrakt);
		}

		//
		// organization name
		//
		String orgName = propertiesObj.optString(ORGANIZATION_NAME);
		ResponsibleParty contact = new ResponsibleParty();
		contact.setRoleCode("originator");
		if (checkString(orgName)) {
		    contact.setOrganisationName(orgName);
		} else {
		    contact.setOrganisationName("European Commission");
		}
		miMetadata.addContact(contact);
		miMetadata.getDataIdentification().addCitationResponsibleParty(contact);

		//
		// publication date
		//
		String pubDate = propertiesObj.optString(PUBLISHED);
		if (checkString(pubDate))
		    miMetadata.getDataIdentification().setCitationPublicationDate(pubDate);

		//
		// revision date
		//
		String revDate = propertiesObj.optString(UPDATED);
		if (checkString(revDate))
		    miMetadata.getDataIdentification().setCitationRevisionDate(revDate);

		//
		// cloud cover percentage
		//

		Double ccp = null;
		ccp = propertiesObj.optDouble(CLOUD_COVER);
		if (ccp != null) {
		    miMetadata.addCloudCoverPercentage(ccp);
		}

		//
		// sensor identifier
		//
		String sensorId = propertiesObj.optString(INSTRUMENT);

		MIInstrument miInstrument = new MIInstrument();
		miInstrument.setMDIdentifierTypeCode(sensorId);
		miMetadata.addMIInstrument(miInstrument);

		//
		// sensor title
		//
		// String sensorName =
		// reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='instrumentname']");
		miInstrument.setTitle(sensorId);

		//
		// sensor description
		//
		String sensorDesc = "";

		String sensorOpMode = propertiesObj.optString(SENSOR_MODE);
		String sensorSwath = propertiesObj.optString(SWATH);

		if (checkString(sensorOpMode)) {

		    sensorDesc += "Instrument Operational Mode: " + sensorOpMode;
		}

		if (checkString(sensorSwath)) {

		    sensorDesc += (sensorDesc.equalsIgnoreCase("") ? "" : " -- ") + "Instrument Swath: " + sensorSwath;
		}

		if (checkString(sensorDesc)) {
		    miInstrument.setDescription(sensorDesc);
		}

		//
		// platform desc and id
		//
		String platformId = propertiesObj.optString(PLATFORM);
		String platformName = propertiesObj.optString(COLLECTION);
		MIPlatform miPlatform = new MIPlatform();
		miPlatform.setMDIdentifierCode(platformId);
		miPlatform.setDescription(platformName);
		miMetadata.addMIPlatform(miPlatform);

		//
		// links
		//
		JSONObject service = propertiesObj.optJSONObject(SERVICES);
		if (service != null) {
		    JSONObject downloadObj = service.optJSONObject(DOWNLOAD);
		    if (downloadObj != null) {
			String link = downloadObj.optString(URL);
			String mimeType = downloadObj.optString(MIMETYPE);
			BigDecimal size = downloadObj.optBigDecimal(SIZE, null);
			addLink(miMetadata, link, false, size);

		    }
		}

		JSONArray links = propertiesObj.optJSONArray(LINKS);
		for (int k = 0; k < links.length(); k++) {
		    JSONObject linkObj = links.getJSONObject(k);
		    String linkType = linkObj.optString("type");
		    String linkTitle = linkObj.optString("title");
		    String linkURL = linkObj.optString("href");
		    if (checkString(linkURL)) {
			Online onLine = new Online();
			onLine.setLinkage(linkURL);
			onLine.setProtocol(new HTTPProtocol().getCommonURN());
			if (checkString(linkTitle)) {
			    onLine.setName(linkTitle);
			} else {
			    onLine.setName("JSON link");
			}
			onLine.setFunctionCode("information");
			miMetadata.getDistribution().addDistributionOnline(onLine);

		    }
		}

		// addLink(miMetadata, link, false, size);
		// addLink(miMetadata, alternative, true, size);

		// ------------------------
		//
		// SatelliteScene extension
		//
		//
		SatelliteScene satelliteScene = new SatelliteScene();

		//
		// thumbnail
		//

		String thumbnailURL = propertiesObj.optString(THUMBNAIL);
		if (checkString(thumbnailURL)) {
		    satelliteScene.setThumbnailURL(thumbnailURL);

		    BrowseGraphic browseGraphic = new BrowseGraphic();
		    browseGraphic.setFileName(thumbnailURL);

		    browseGraphic.setFileDescription("Pictorial preview of the dataset");

		    miMetadata.getDataIdentification().addGraphicOverview(browseGraphic);
		}

		// this is used by the AtomGPResultSetMapper
		satelliteScene.setOrigin("sentinel");

		satelliteScene.setPlatid(platformId);

		Integer relOrbit = propertiesObj.optInt(RELATIVE_ORBIT_NUMBER);
		if (relOrbit != null) {
		    satelliteScene.setRelativeOrbit(relOrbit);
		}

		String productType = propertiesObj.optString(PRODUCT_TYPE);
		if (checkString(productType)) {
		    satelliteScene.setProductType(productType);
		}

		if (ccp != null) {
		    satelliteScene.setCloudCoverPercentage(Double.toString(ccp));
		}

		if (checkString(sensorSwath)) {
		    satelliteScene.setSensorSwath(sensorSwath);
		}

		if (checkString(sensorOpMode)) {
		    satelliteScene.setSensorOpMode(sensorOpMode);
		}

		String sensorPolarization = propertiesObj.optString(POLARISATION);
		if (checkString(sensorPolarization)) {
		    // sensorPolarization = sensorPolarization.replace(" ", "").replace(",", "");
		    satelliteScene.setSarPolCh(sensorPolarization);
		}

		// TODO: check
		// String footprint =
		// reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='footprint']");
		// if (checkString(footprint)) {
		// satelliteScene.setFootprint(footprint);
		// }
		// TODO: check
		// String productconsolidation = reader
		// .evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='productconsolidation']");
		// if (checkString(productconsolidation)) {
		// satelliteScene.setProductConsolidation(productconsolidation);
		// }

		String stopRelativeOrbitNumber = propertiesObj.optString(RELATIVE_ORBIT_NUMBER);

		if (checkString(stopRelativeOrbitNumber)) {
		    satelliteScene.setStopRelativeOrbitNumber(stopRelativeOrbitNumber);
		}

		String startOrbitNumber = propertiesObj.optString(ORBIT_NUMBER);
		if (checkString(startOrbitNumber)) {
		    satelliteScene.setStartOrbitNumber(startOrbitNumber);
		}

		String stopOrbitNumber = propertiesObj.optString(ORBIT_NUMBER);
		if (checkString(stopOrbitNumber)) {
		    satelliteScene.setStopOrbitNumber(stopOrbitNumber);
		}

		String orbitdirection = propertiesObj.optString(ORBIT_DIRECTION);
		if (checkString(orbitdirection)) {
		    satelliteScene.setOrbitDirection(orbitdirection);
		}

		// String productclass =
		// reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='productclass']");
		// if (checkString(productclass)) {
		// satelliteScene.setProductClass(productclass);
		// }

		// String acquisitiontype =
		// reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='acquisitiontype']");
		// if (checkString(acquisitiontype)) {
		// satelliteScene.setAcquisitionType(acquisitiontype);
		// }

		// String slicenumber =
		// reader.evaluateString("//*[local-name()='entry']/*[local-name()='int'][@name='slicenumber']");
		// if (checkString(slicenumber)) {
		// satelliteScene.setSliceNumber(slicenumber);
		// }

		String missiondatatakeid = propertiesObj.optString(MISSION_ID);
		if (checkString(missiondatatakeid)) {
		    satelliteScene.setMissionDatatakeid(missiondatatakeid);
		}

		String status = propertiesObj.optString(STATUS);
		if (checkString(status)) {
		    satelliteScene.setStatus(status);
		}

		String processingbaseline = propertiesObj.optString(PROCESSING_BASELINE);
		if (checkString(processingbaseline)) {
		    satelliteScene.setProcessingBaseline(processingbaseline);
		}

		String processinglevel = propertiesObj.optString(PROCESSING_LEVEL);
		if (checkString(processinglevel)) {
		    satelliteScene.setProcessingLevel(processinglevel);
		}

		String dusId = propertiesObj.optString(PRODUCT_IDENTIFIER);
		if (checkString(dusId)) {
		    satelliteScene.setDusId(dusId);
		}

		// TODO: check
		// String s3ProductLevel =
		// reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='productlevel']");
		// if (checkString(s3ProductLevel)) {
		// satelliteScene.setS3ProductLevel(s3ProductLevel);
		// }

		String s3Timeliness = propertiesObj.optString(TIMELINESS);
		if (checkString(s3Timeliness)) {
		    satelliteScene.setS3Timeliness(s3Timeliness);
		}
		// TODO: check
		// String productFormat =
		// reader.evaluateString("//*[local-name()='entry']/*[local-name()='str'][@name='format']");
		// if (checkString(productFormat)) {
		// satelliteScene.setProductFormat(productFormat);
		// }

		if (checkString(sensorId)) {
		    satelliteScene.setS3InstrumentIdx(sensorId);
		}

		//
		// complex links for loading preview
		addComplexLink(miMetadata, dusId, satelliteScene);

		//

		dataset.getExtensionHandler().setSatelliteScene(satelliteScene);

	    }

	}

	return dataset;
    }

    public String toBBOX(List<Double> coords, boolean firstIsLat) {

	Double[] lats = new Double[coords.size() / 2];
	Double[] lons = new Double[coords.size() / 2];

	for (int i = 0; i < coords.size(); i++) {

	    if (i % 2 == 0) {

		if (firstIsLat) {
		    lats[i / 2] = coords.get(i);
		} else {
		    lons[i / 2] = coords.get(i);
		}
	    } else {

		if (!firstIsLat) {
		    lats[i / 2] = coords.get(i);
		} else {
		    lons[i / 2] = coords.get(i);
		}
	    }
	}

	Double minLat = lats[0];
	Double maxLat = lats[0];
	Double minLon = lons[0];
	Double maxLon = lons[0];

	for (int i = 1; i < lats.length; i++) {

	    if (lats[i] < minLat) {
		minLat = lats[i];
	    }
	    if (lats[i] > maxLat) {
		maxLat = lats[i];
	    }

	    if (lons[i] < minLon) {
		minLon = lons[i];
	    }
	    if (lons[i] > maxLon) {
		maxLon = lons[i];
	    }
	}

	return minLat + " " + minLon + " " + maxLat + " " + maxLon;
    }

    private Optional<String> read(XMLDocumentReader reader, String xpath) {

	try {

	    return Optional.ofNullable(reader.evaluateString(xpath));

	} catch (XPathExpressionException e) {
	    logger.warn("Can't evalueate xpath {}", xpath, e);
	}

	return Optional.empty();

    }

    private Optional<Node[]> readDoc(XMLDocumentReader reader, String xpath) {

	try {

	    return Optional.ofNullable(reader.evaluateNodes(xpath));

	} catch (XPathExpressionException e) {
	    logger.warn("Can't evalueate xpath {}", xpath, e);
	}

	return Optional.empty();

    }

    private BrowseGraphic createGraphicOverview(String url) {

	BrowseGraphic graphic = new BrowseGraphic();

	graphic.setFileName(url);
	graphic.setFileType("image/png");

	return graphic;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CopernicusDataspaceGranulesMetadataSchemas.JSON_COPERNICUS_DATASPACE.toString();
    }

    /**
     * @param mi_Metadata
     * @param link
     * @param alternative
     * @param size
     */
    private void addLink(MIMetadata mi_Metadata, String link, boolean alternative, BigDecimal size) {

	Online onLine = new Online();
	onLine.setLinkage(link);
	onLine.setProtocol(new HTTPProtocol().getCommonURN());
	onLine.setName("Product");
	onLine.setFunctionCode("download");
	if (alternative) {
	    onLine.setDescription("Alternative link");
	}

	if (size != null) {
	    mi_Metadata.getDistribution().addDistributionOnline(onLine, size.doubleValue());
	} else {
	    mi_Metadata.getDistribution().addDistributionOnline(onLine);
	}
    }

    /**
     * @param mi_Metadata
     * @param link
     */
    public void addComplexLink(MIMetadata mi_Metadata, String identifier, SatelliteScene scene) {

	// String baseUrl = "http://gs-service-production.geodab.eu/gs-service/services/essi/wms?";
	//String check = identifier.substring(1, 3);
	if (identifier != null && (identifier.contains("Sentinel-2") || identifier.contains("sentinel-2"))) {
	    String link = "http://services.sentinel-hub.com/";

	    for (SatelliteLayers v : SatelliteLayers.values()) {
		Online onLine = new Online();
		onLine.setLinkage(link);
		onLine.setName(v.toString() + "@" + identifier);
		onLine.setProtocol(CommonNameSpaceContext.SENTINEL2_URI);
		onLine.setFunctionCode("download");
		mi_Metadata.getDistribution().addDistributionOnline(onLine);
	    }

	}
    }

    /**
     * @param s
     * @return
     */
    private boolean checkString(String s) {

	return s != null && !s.isEmpty();
    }

    private String extractTemporalEnd(String id) throws ParseException {
	String time = id.substring(id.lastIndexOf("_") + 1);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	Date date = sdf.parse(time);
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'");
	sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
	return sdf2.format(date);
    }

    public static void main(String[] args) {
	String date = "2021-01-09T08:01:24.073000.000Z";
	try {
	    String res = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(date));
	} catch (Exception e) {
	    // TODO: handle exception
	}
	System.out.println(date);
    }
}
