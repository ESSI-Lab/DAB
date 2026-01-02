package eu.essi_lab.accessor.stac.distributed;

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

import static eu.essi_lab.iso.datamodel.classes.Identification.CREATION;
import static eu.essi_lab.iso.datamodel.classes.Identification.PUBLICATION;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.stac.harvested.STACCollectionMapper;
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
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author ilsanto
 */
public class STACGranulesResultMapper extends OriginalIdentifierMapper {

    public static final String STAC_GRANULES_SCHEME_URI = "http://stacspec.org/granules/scheme";
    private static final String IDENTIFIER_KEY = "id";
    private static final String GEOMETRY = "geometry";
    private static final String PROPERTIES = "properties";
    private static final String STAC_VERSION = "stac_version";
    private static final String TYPE = "type";
    private static final String LINKS = "links";
    private static final String ASSETS = "assets";
    private static final String BBOX = "bbox";
    private static final String STAC_EXTENSIONS = "stac_extensions";
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

    /**
     * MAIN FIELDS:
     * #type
     * #id
     * #stac_version
     * #properties
     * #geometry
     * #links
     * #assets
     * #bbox
     * #stac_extensions
     * #collection
     **/

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject json = new JSONObject(resource.getOriginalMetadata().getMetadata());

	Optional<Object> object = STACCollectionMapper.readObject(json, IDENTIFIER_KEY);
	if (object.isPresent()) {

	    return object.get().toString();
	}

	return null;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject json = new JSONObject(originalMD.getMetadata());

	Dataset dataset = new Dataset();

	String id = STACCollectionMapper.readObject(json, IDENTIFIER_KEY).orElse(UUID.randomUUID().toString()).toString();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	miMetadata.setHierarchyLevelName("dataset");

	miMetadata.setLanguage("eng");

	dataset.setSource(source);

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	// bbox
	JSONArray doublesArray = json.optJSONArray(BBOX);
	if (doublesArray != null && doublesArray.length() == 4) {
	    Double west = doublesArray.optDouble(0);
	    Double east = doublesArray.optDouble(2);
	    Double north = doublesArray.optDouble(3);
	    Double south = doublesArray.optDouble(1);
	    if (!west.isNaN() && !east.isNaN() && !north.isNaN() && !south.isNaN()) {
		dataIdentification.addGeographicBoundingBox(north, west, south, east);
	    }
	}

	// STACCollectionMapper.readObject(json, BBOX).ifPresent(bbox ->
	//
	// STACCollectionMapper.readObject(json, LATITUDE_KEY).ifPresent(lat ->
	//
	// dataIdentification.addGeographicBoundingBox(//
	// "Event point", //
	// Double.valueOf(lat.toString()), //
	// Double.valueOf(lon.toString()), //
	// Double.valueOf(lat.toString()), //
	// Double.valueOf(lon.toString()))));

	// properties: title, time, keyword, etc
	JSONObject properties = json.optJSONObject(PROPERTIES);
	if (properties != null) {

	    TemporalExtent tempExtent = new TemporalExtent();

	    STACCollectionMapper.readString(properties, START_DATETIME).ifPresent(startTime -> tempExtent.setBeginPosition(startTime));

	    STACCollectionMapper.readString(properties, END_DATETIME).ifPresent(endTime -> tempExtent.setEndPosition(endTime));

	    dataIdentification.addTemporalExtent(tempExtent);

	    String title = "DEA STAC Item " + id;

	    Optional<String> t = STACCollectionMapper.readString(properties, TITLE);

	    if (t.isPresent()) {
		dataIdentification.setCitationTitle(t.get());
	    } else {
		dataIdentification.setCitationTitle(title);
	    }

	    STACCollectionMapper.readString(properties, CREATED).ifPresent(up -> dataIdentification.addCitationDate(up, CREATION));

	    STACCollectionMapper.readString(properties, DATETIME)
		    .ifPresent(dateTime -> dataIdentification.addCitationDate(dateTime, PUBLICATION));

	    Set<String> keywords = new HashSet<>();
	    STACCollectionMapper.readString(properties, PRODUCT).ifPresent(keywords::add);
	    STACCollectionMapper.readString(properties, PLATFORM).ifPresent(key -> {
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

	    STACCollectionMapper.readString(properties, FILE_FORMAT).ifPresent(f -> {

		Format format = new Format();
		format.setName(f);
		miMetadata.getDistribution().addFormat(format);

	    });

	    // STACCollectionMapper.readString(json, SCIENTIFIC_NAME_KEY).ifPresent(scname ->
	    //
	    // STACCollectionMapper.readString(json, DATASETNAME_KEY).ifPresent(dname ->
	    //
	    // dataIdentification.setCitationTitle(scname + " from " + dname)
	    //
	    // ));
	    //
	    // STACCollectionMapper.readString(json, REVISION_KEY).ifPresent(up ->
	    //
	    // ISO8601DateTimeUtils.parseISO8601ToDate(up.replace(" ", "T"))
	    // .ifPresent(t -> dataIdentification.addCitationDate(ISO8601DateTimeUtils.getISO8601DateTime(t), REVISION))
	    //
	    // );

	    STACCollectionMapper.readString(properties, PRODUCER).ifPresent(org -> {

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
//	    JSONArray arrayLinks = json.optJSONArray(LINKS);
//	    if (arrayLinks != null) {
//		for (int j = 0; j < arrayLinks.length(); j++) {
//		    JSONObject objectURL = arrayLinks.getJSONObject(j);
//		    String rel = STACCollectionMapper.readString(objectURL, "rel").orElse(null);
//		    String url = STACCollectionMapper.readString(objectURL, "href").orElse(null);
//		    boolean relationFound = false;
//		    for (RELATION_TYPE relationId : RELATION_TYPE.values()) {
//			if (relationId.getId().equalsIgnoreCase(rel)) {
//			    relationFound = true;
//			    break;
//			}
//		    }
//		    String description = rel;
//		    if (relationFound) {
//			RELATION_TYPE relType = RELATION_TYPE.valueOf(rel.toUpperCase());
//
//			switch (relType) {
//			case COLLECTIONS:
//			    description = "URL to a child STAC entity (Catalog or Collection)";
//			    break;
//			case DERIVED_FROM:
//			    description = "URL to a STAC Collection that was used as input data in the creation of this Collection";
//			    break;
//			case PARENT:
//			    description = "URL to the parent STAC entity (Catalog or Collection)";
//			    break;
//			case ROOT:
//			    description = "URL to the root STAC entity (Catalog or Collection)";
//			    break;
//			case SELF:
//			    description = "Absolute URL to the Item";
//			    break;
//			default:
//			    break;
//
//			}
//		    }
//
//		    Online online = new Online();
//
//		    online.setLinkage(url);
//		    online.setProtocol(NetProtocols.HTTP.getCommonURN());
//		    online.setFunctionCode("information");
//		    online.setDescription(description);
//		    STACCollectionMapper.readString(objectURL, "title").ifPresent(titleLink -> online.setName(titleLink));
//		    STACCollectionMapper.readString(objectURL, "type").ifPresent(typeLink -> online.setFunctionCode(typeLink));
//
//		    miMetadata.getDistribution().addDistributionOnline(online);
//
//		}
//	    }

	    // ONLINE Resources: ASSETS
	    // s3://deafrica-input-datasets --> https://deafrica-input-datasets.s3.af-south-1.amazonaws.com/
	    JSONObject assetObj = json.optJSONObject(ASSETS);
	    if (assetObj != null) {
		Iterator<String> keys = assetObj.keys();
		while (keys.hasNext()) {
		    String key = keys.next();
		    JSONObject keyObj = assetObj.optJSONObject(key);

		    // String rel = STACCollectionMapper.readString(keyObj, "rel").orElse(null);
		    String url = STACCollectionMapper.readString(keyObj, "href").orElse(null);
		    String type = STACCollectionMapper.readString(keyObj, "type").orElse(null);

		    String s3ToHTTP = "";
		    if (url.startsWith("s3://")) {
			s3ToHTTP = fromS3ToHTTP(url);
		    }
		    String titleURL = STACCollectionMapper.readString(keyObj, "title").orElse(null);

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
			assetOnline.setProtocol(NetProtocolWrapper.HTTP.getCommonURN());
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

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return STAC_GRANULES_SCHEME_URI.toLowerCase();
    }

    public static void main(String[] args) {
	String s3 = "s3://deafrica-landsat/collection02/level-2/standard/tm/1984/199/034/LT05_L2SP_199034_19840319_20200918_02_T1/LT05_L2SP_199034_19840319_20200918_02_T1_thumb_small.jpeg";
	String s3ToHTTP = "";
	if (s3.startsWith("s3://")) {
	    String[] splittedUrl = s3.split("s3://");
	    String[] secondSplit = splittedUrl[1].split("/", 2);
	    String bucketURL = "";
	    if (secondSplit.length > 1) {
		bucketURL = secondSplit[0];
	    }
	    if (!bucketURL.isEmpty()) {
		s3ToHTTP = "https://" + bucketURL + ".s3.af-south-1.amazonaws.com/" + secondSplit[1];
		System.out.println(s3ToHTTP);
	    } else {
		// TODO
		System.out.println("ERROR");
	    }
	}
    }

}
