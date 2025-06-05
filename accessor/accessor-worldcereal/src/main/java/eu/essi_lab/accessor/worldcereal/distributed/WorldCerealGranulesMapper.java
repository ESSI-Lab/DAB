package eu.essi_lab.accessor.worldcereal.distributed;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.xml.xpath.XPathExpressionException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.worldcereal.harvested.WorldCerealCache;
import eu.essi_lab.accessor.worldcereal.harvested.WorldCerealCollectionMapper;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.worldcereal.WorldCerealItem;
import eu.essi_lab.model.resource.worldcereal.WorldCerealMap;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

import static eu.essi_lab.iso.datamodel.classes.Identification.CREATION;
import static eu.essi_lab.iso.datamodel.classes.Identification.PUBLICATION;
import static eu.essi_lab.iso.datamodel.classes.Identification.REVISION;

/**
 * @author roncella
 */
public class WorldCerealGranulesMapper extends OriginalIdentifierMapper {

    public static final String WORLDCEREAL_GRANULES_SCHEME_URI = "https://esa-worldcereal.org/";
    private static final String IDENTIFIER_KEY = "id";
    private static final String SAMPLE_ID = "sample_id";
    private static final String LAND_COVER = "LC";
    private static final String CROP_TYPE = "CT";
    private static final String IRRIGATION_STATUS = "irrigation_status";
    private static final String VAL_TIME = "valid_time";
    private static final String SPLIT = "split";
    private static final String AREA = "area";
    private static final String GEOMETRY = "geometry";
    private static final String PROPERTIES = "properties";
    private static final String WORLDCEREAL_VERSION = "worldcereal_version";
    private static final String TYPE = "type";
    private static final String LINKS = "links";
    private static final String ASSETS = "assets";
    private static final String BBOX = "bbox";
    private static final String WORLDCEREAL_EXTENSIONS = "worldcereal_extensions";
    private static final String COLLECTION = "collection";
    private static final String TITLE = "title";
    private static final String PLATFORM = "platform";
    private static final String INSTRUMENTS = "instruments";
    private static final String PRODUCT = "odc:product";
    private static final String PRODUCER = "odc:producer";
    private static final String FILE_FORMAT = "odc:file_format";
    private static final String START_DATETIME = "start_datetime";
    private static final String END_DATETIME = "end_datetime";
    private static final String CREATED = "created";
    private static final String DATETIME = "datetime";
    private static final String EWOC_CODE = "ewoc_code";
    private static final String CONFIDENCE_LAND_COVER = "quality_score_lc";
    private static final String CONFIDENCE_CROP_TYPE = "quality_score_ct";
    private static final String CONFIDENCE_IRRIGATION_TYPE = "irrigation_status";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject json = new JSONObject(resource.getOriginalMetadata().getMetadata());

	Optional<Object> object = WorldCerealCollectionMapper.readObject(json, IDENTIFIER_KEY);
	if (object.isPresent()) {

	    return object.get().toString();
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject json = new JSONObject(originalMD.getMetadata());

	Dataset dataset = new Dataset();

	String id = WorldCerealCollectionMapper.readObject(json, IDENTIFIER_KEY).orElse(UUID.randomUUID().toString()).toString();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	miMetadata.setHierarchyLevelName("dataset");

	miMetadata.setLanguage("eng");

	dataset.setSource(source);

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	ExtensionHandler extHandler = dataset.getExtensionHandler();

	// bbox
	JSONObject geometryObj = json.optJSONObject(GEOMETRY);
	List<Double> coordList = new ArrayList<Double>();
	if (geometryObj != null) {
	    JSONArray coordinates = geometryObj.optJSONArray("coordinates");
	    String type = geometryObj.optString("type");

	    if (coordinates != null) {
		if (type.toLowerCase().contains("point")) {
		    for (int h = 0; h < coordinates.length(); h++) {
			coordList.add(coordinates.getDouble(h));
		    }
		} else if (type.toLowerCase().contains("multi")) {
		    JSONArray coords = coordinates.getJSONArray(0);

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
		    JSONArray coords = coordinates.getJSONArray(0);
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

	    dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(north, west, south, east);
	}
	// JSONArray doublesArray = json.optJSONArray(BBOX);
	// if (doublesArray != null && doublesArray.length() == 4) {
	// Double west = doublesArray.optDouble(0);
	// Double east = doublesArray.optDouble(2);
	// Double north = doublesArray.optDouble(3);
	// Double south = doublesArray.optDouble(1);
	// if (!west.isNaN() && !east.isNaN() && !north.isNaN() && !south.isNaN()) {
	// dataIdentification.addGeographicBoundingBox(north, west, south, east);
	// }
	// }

	// WorldCerealCollectionMapper.readObject(json, BBOX).ifPresent(bbox ->
	//
	// WorldCerealCollectionMapper.readObject(json, LATITUDE_KEY).ifPresent(lat ->
	//
	// dataIdentification.addGeographicBoundingBox(//
	// "Event point", //
	// Double.valueOf(lat.toString()), //
	// Double.valueOf(lon.toString()), //
	// Double.valueOf(lat.toString()), //
	// Double.valueOf(lon.toString()))));

	// properties: title/sampleID, time, keyword, etc
	JSONObject properties = json.optJSONObject(PROPERTIES);
	if (properties != null) {

	    /**
	     * SAMPLE_ID
	     */

	    Optional<String> t = WorldCerealCollectionMapper.readString(properties, SAMPLE_ID);

	    List<WorldCerealItem> cropList = new ArrayList<WorldCerealItem>();
	    List<WorldCerealItem> landCoverList = new ArrayList<WorldCerealItem>();
	    List<WorldCerealItem> irrigationList = new ArrayList<WorldCerealItem>();
	    /**
	     * EWOC_CODE
	     */
	    WorldCerealCache wcCache = WorldCerealCache.getInstance();
	    Optional<String> ewoc_code = WorldCerealCollectionMapper.readString(properties, EWOC_CODE);

	    if (ewoc_code.isPresent()) {
		String code = ewoc_code.get();
		if (wcCache.containsKeyCrop(code)) {
		    WorldCerealItem item = new WorldCerealItem();
		    item.setLabel(wcCache.getCrop(code));
		    item.setCode(code);
		    cropList.add(item);
		} else if (wcCache.containsKeyLc(code)) {
		    WorldCerealItem item = new WorldCerealItem();
		    item.setLabel(wcCache.getLc(code));
		    item.setCode(code);
		    landCoverList.add(item);
		}
	    }

	    /**
	     * IRRIGATION_STATUS
	     */
	    Optional<String> irr_code = WorldCerealCollectionMapper.readString(properties, IRRIGATION_STATUS);
	    if (irr_code.isPresent()) {
		String code = irr_code.get();
		if (wcCache.containsKeyIrr(code)) {
		    WorldCerealItem item = new WorldCerealItem();
		    item.setLabel(wcCache.getIrr(code));
		    item.setCode(code);
		    irrigationList.add(item);
		}
	    }

	    /**
	     * QUALITY SCORES/CONFIDENCE
	     */

	    Double confidenceCropType = properties.optDouble(CONFIDENCE_CROP_TYPE);
	    // Double confidenceIrrType = json.optDouble(CONFIDENCE_IRRIGATION_TYPE);
	    Double confidenceLcType = properties.optDouble(CONFIDENCE_LAND_COVER);

	    WorldCerealMap worldCerealMap = new WorldCerealMap();
	    // worldCerealMap.setWorldCerealQueryables(queryables);
	    worldCerealMap.setCropTypes(cropList);
	    worldCerealMap.setIrrigationTypes(irrigationList);
	    worldCerealMap.setLandCoverTypes(landCoverList);

	    if (confidenceCropType != null && !confidenceCropType.isNaN()) {
		worldCerealMap.setCropTypeConfidence(confidenceCropType);
	    }
	    if (confidenceLcType != null && !confidenceLcType.isNaN()) {
		worldCerealMap.setLcTypeConfidence(confidenceLcType);
	    }

	    extHandler.setWorldCereal(worldCerealMap);

	    /**
	     * TEMPORAL_EXTENT
	     */

	    TemporalExtent tempExtent = new TemporalExtent();

	    Optional<String> valTime = WorldCerealCollectionMapper.readString(properties, VAL_TIME);
	    if (valTime.isPresent()) {
		tempExtent.setBeginPosition(valTime.get());
		tempExtent.setEndPosition(valTime.get());
		dataIdentification.addTemporalExtent(tempExtent);
	    }

	    // WorldCerealCollectionMapper.readString(properties, VAL_TIME)
	    // .ifPresent(startTime -> tempExtent.setBeginPosition(startTime));
	    //
	    // WorldCerealCollectionMapper.readString(properties, END_DATETIME).ifPresent(endTime ->
	    // tempExtent.setEndPosition(endTime));
	    //
	    // dataIdentification.addTemporalExtent(tempExtent);

	    String title = "WorldCereal Item ";

	    if (t.isPresent()) {
		dataIdentification.setCitationTitle(title + t.get());
	    } else {
		dataIdentification.setCitationTitle(title + id);
	    }

	    WorldCerealCollectionMapper.readString(properties, CREATED).ifPresent(up -> dataIdentification.addCitationDate(up, CREATION));

	    WorldCerealCollectionMapper.readString(properties, DATETIME)
		    .ifPresent(dateTime -> dataIdentification.addCitationDate(dateTime, PUBLICATION));

	    Set<String> keywords = new HashSet<>();
	    WorldCerealCollectionMapper.readString(properties, PRODUCT).ifPresent(keywords::add);
	    WorldCerealCollectionMapper.readString(properties, PLATFORM).ifPresent(key -> {
		keywords.add(key);
		MIPlatform platform = new MIPlatform();
		platform.setMDIdentifierCode(key);
		platform.setDescription("Platform " + key);
		Citation platformCitation = new Citation();
		platformCitation.setTitle(key);
		platform.setCitation(platformCitation);
		miMetadata.addMIPlatform(platform);
	    });
	    JSONArray instrumentArray = properties.optJSONArray(INSTRUMENTS);
	    if (instrumentArray != null) {
		for (int k = 0; k < instrumentArray.length(); k++) {
		    String instr = instrumentArray.getString(k);
		    keywords.add(instr);
		    MIInstrument instrument = new MIInstrument();
		    instrument.setMDIdentifierTypeIdentifier(instr);
		    instrument.setMDIdentifierTypeCode(instr);
		    instrument.setDescription("Instrument: " + instr);
		    instrument.setTitle(instr);
		    miMetadata.addMIInstrument(instrument);
		}
	    }
	    keywords.forEach(k -> dataIdentification.addKeyword(k));

	    WorldCerealCollectionMapper.readString(properties, FILE_FORMAT).ifPresent(f -> {

		Format format = new Format();
		format.setName(f);
		miMetadata.getDistribution().addFormat(format);

	    });

	    WorldCerealCollectionMapper.readString(properties, PRODUCER).ifPresent(org -> {

		ResponsibleParty responsibleParty = new ResponsibleParty();
		responsibleParty.setRoleCode("originator");
		Contact contact = new Contact();
		Online online = new Online();
		online.setLinkage(org);
		contact.setOnline(online);
		responsibleParty.setContactInfo(contact);
		// responsibleParty.setOrganisationName(org);

		dataIdentification.addPointOfContact(responsibleParty);
	    });

	    // ONLINE Resources: LINKS
	    // JSONArray arrayLinks = json.optJSONArray(LINKS);
	    // if (arrayLinks != null) {
	    // for (int j = 0; j < arrayLinks.length(); j++) {
	    // JSONObject objectURL = arrayLinks.getJSONObject(j);
	    // String rel = WorldCerealCollectionMapper.readString(objectURL, "rel").orElse(null);
	    // String url = WorldCerealCollectionMapper.readString(objectURL, "href").orElse(null);
	    // boolean relationFound = false;
	    // for (RELATION_TYPE relationId : RELATION_TYPE.values()) {
	    // if (relationId.getId().equalsIgnoreCase(rel)) {
	    // relationFound = true;
	    // break;
	    // }
	    // }
	    // String description = rel;
	    // if (relationFound) {
	    // RELATION_TYPE relType = RELATION_TYPE.valueOf(rel.toUpperCase());
	    //
	    // switch (relType) {
	    // case COLLECTIONS:
	    // description = "URL to a child STAC entity (Catalog or Collection)";
	    // break;
	    // case DERIVED_FROM:
	    // description = "URL to a STAC Collection that was used as input data in the creation of this Collection";
	    // break;
	    // case PARENT:
	    // description = "URL to the parent STAC entity (Catalog or Collection)";
	    // break;
	    // case ROOT:
	    // description = "URL to the root STAC entity (Catalog or Collection)";
	    // break;
	    // case SELF:
	    // description = "Absolute URL to the Item";
	    // break;
	    // default:
	    // break;
	    //
	    // }
	    // }
	    //
	    // Online online = new Online();
	    //
	    // online.setLinkage(url);
	    // online.setProtocol(NetProtocols.HTTP.getCommonURN());
	    // online.setFunctionCode("information");
	    // online.setDescription(description);
	    // WorldCerealCollectionMapper.readString(objectURL, "title").ifPresent(titleLink ->
	    // online.setName(titleLink));
	    // WorldCerealCollectionMapper.readString(objectURL, "type").ifPresent(typeLink ->
	    // online.setFunctionCode(typeLink));
	    //
	    // miMetadata.getDistribution().addDistributionOnline(online);
	    //
	    // }
	    // }

	    // ONLINE Resources: ASSETS
	    // s3://deafrica-input-datasets --> https://deafrica-input-datasets.s3.af-south-1.amazonaws.com/
	    JSONObject assetObj = json.optJSONObject(ASSETS);
	    if (assetObj != null) {
		Iterator<String> keys = assetObj.keys();
		while (keys.hasNext()) {
		    String key = keys.next();
		    JSONObject keyObj = assetObj.optJSONObject(key);

		    // String rel = WorldCerealCollectionMapper.readString(keyObj, "rel").orElse(null);
		    String url = WorldCerealCollectionMapper.readString(keyObj, "href").orElse(null);
		    String type = WorldCerealCollectionMapper.readString(keyObj, "type").orElse(null);

		    String s3ToHTTP = "";
		    if (url.startsWith("s3://")) {
			s3ToHTTP = fromS3ToHTTP(url);
		    }
		    String titleURL = WorldCerealCollectionMapper.readString(keyObj, "title").orElse(null);

		    if (key.equalsIgnoreCase("thumbnail")) {
			if (!s3ToHTTP.isEmpty()) {
			    BrowseGraphic graphic = new BrowseGraphic();
			    graphic.setFileDescription(titleURL);
			    graphic.setFileName(s3ToHTTP);
			    if (s3ToHTTP.contains(".jpeg")) {
				graphic.setFileType("image/jpeg");
			    } else if (s3ToHTTP.contains(".png")) {
				graphic.setFileType("image/png");
			    }
			    dataIdentification.addGraphicOverview(graphic);
			}

		    } else {

			Online assetOnline = new Online();
			assetOnline.setProtocol(NetProtocols.HTTP.getCommonURN());
			if (!s3ToHTTP.isEmpty()) {
			    // assetOnline.setProtocol(NetProtocols.HTTP.getCommonURN());
			    // assetOnline.setDescription("AWS Simple Storage Service (S3)");
			    assetOnline.setLinkage(s3ToHTTP);
			} else {
			    assetOnline.setLinkage(url);
			}

			// assetOnline.setProtocol(NetProtocols.HTTP.getCommonURN());
			assetOnline.setFunctionCode(type);

			if (titleURL != null) {
			    assetOnline.setName(titleURL);
			} else {
			    assetOnline.setName(key);
			}

			miMetadata.getDistribution().addDistributionOnline(assetOnline);
		    }

		}

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

    private String fromS3ToHTTP(String url) {
	String httpResult = "";
	String[] splittedUrl = url.split("s3://");
	String[] secondSplit = splittedUrl[1].split("/", 2);
	String bucketURL = "";
	if (secondSplit.length > 1) {
	    bucketURL = secondSplit[0];
	}
	if (!bucketURL.isEmpty()) {
	    httpResult = "https://" + bucketURL + ".s3.af-south-1.amazonaws.com/" + secondSplit[1];
	} else {
	    // TODO
	    GSLoggerFactory.getLogger(getClass()).error("Failed to create link for record with url: {}", url);
	}
	return httpResult;
    }

    private BrowseGraphic createGraphicOverview(String url) {

	BrowseGraphic graphic = new BrowseGraphic();

	graphic.setFileName(url);
	graphic.setFileType("image/png");

	return graphic;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return WORLDCEREAL_GRANULES_SCHEME_URI.toLowerCase();
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
