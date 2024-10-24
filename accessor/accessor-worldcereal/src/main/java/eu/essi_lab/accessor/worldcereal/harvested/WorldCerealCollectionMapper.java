package eu.essi_lab.accessor.worldcereal.harvested;

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

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.worldcereal.WorldCerealItem;
import eu.essi_lab.model.resource.worldcereal.WorldCerealMap;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gco.v_20060504.DateTimePropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQAccuracyOfATimeMeasurementType;
import net.opengis.iso19139.gmd.v_20060504.DQDataQualityType;
import net.opengis.iso19139.gmd.v_20060504.DQResultPropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQScopePropertyType;
import net.opengis.iso19139.gmd.v_20060504.DQScopeType;
import net.opengis.iso19139.gmd.v_20060504.MDScopeCodePropertyType;
import net.opengis.iso19139.gmd.v_20060504.ObjectFactory;

public class WorldCerealCollectionMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());
    protected static final String DEFAULT_CLIENT_ID = "gs-service";
    public static final String SCHEMA_URI = "http://essi-lab.eu/worldcereal/collections";

    static final String COLLECTIONS_METADATA_URL = "metadata/items";

    public static final String WORLDCEREAL_SECOND_LEVEL_TEMPLATE = "worldcerealSecondLevel";

    private static final String CANT_READ_GRANULE = "Exception reading original granule";
    private static final String WORLDCEREAL_GRANULES_MAPPER_ORIGINAL_MD_READ_ERROR = "WORLDCEREAL_GRANULES_MAPPER_ORIGINAL_MD_READ_ERROR";
    private static final String RESOURCE_ID_KEY = "id";
    private static final String COLLECTION_ID_KEY = "collectionId";
    private static final String TITLE_KEY = "title";
    private static final String FEATURE_COUNT = "featureCount";
    private static final String TYPE = "type"; // polygon
    private static final String ACCESS_TYPE = "accessType";

    private static final String OBSERVATION_METHOD = "typeOfObservationMethod";
    private static final String CONFIDENCE_LAND_COVER = "confidenceLandCover";
    private static final String CONFIDENCE_CROP_TYPE = "confidenceCropType";
    private static final String CONFIDENCE_IRRIGATION_TYPE = "confidenceIrrigationType";

    private static final String LAND_COVERS = "landCovers";
    private static final String CROP_TYPES = "cropTypes";
    private static final String IRRIGATION_TYPES = "irrTypes";
    private static final String EWOC_CODES = "ewocCodes";
    private static final String EXTENT = "extent";
    private static final String TEMPORAL = "temporal";
    private static final String SPATIAL = "spatial";

    private static final String BBOX = "bbox";
    private static final String CRS = "crs";
    private static final String LAST_MODIFICATION_TIME = "lastModificationTime";
    private static final String LAST_MODIFIER_ID = "lastModifierId";
    private static final String CREATION_TIME = "creationTime";
    private static final String CREATOR_ID = "creatorId";

    private static final String ADDITIONAL_DATA = "additionalData";

    private static final String BACKGROUND_INFO_URL = "https://ewoc-rdm-ui.iiasa.ac.at/details/WorldCereal_crop_legend_ui_v2_20240709.pdf";
    
    private static final String IRRIGATION_INFO_URL = "https://ewoc-rdm-ui.iiasa.ac.at/details/WorldCereal_irrigation_legend_ui_v2_20240709.pdf";
    private static final String CONFIDENCE_SCORE_URL = "https://ewoc-rdm-ui.iiasa.ac.at/details/WorldCereal_ConfidenceScoreCalculations_v1_1.pdf";
    private static final String CALCULATION_DATE_URL = "https://ewoc-rdm-ui.iiasa.ac.at/details/WorldCereal_DerivingValidityTime_v1_1.pdf";
    						 
    						        
    private static final int THRESOLD = 10000;

    // private static final String LICENSE = "license";
    // private static final String ABSTRACT_KEY = "description";
    //
    // private static final String PROPERTIES = "properties";
    // private static final String PROVIDERS = "providers";
    // private static final String KEYWORDS = "keywords";
    // private static final String SUMMARIES = "summaries";
    // private static final String ASSETS = "assets";

    private static final String CONTACT_ROLE = "role";
    private static final String CONTACT_NAME = "name";

    public static int COLLECTIONS_WITH_GRANULES_COUNT = 0;

    protected transient Downloader downloader;

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }




    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset ret = new Dataset();
	ret.setSource(source);

	ExtensionHandler extHandler = ret.getExtensionHandler();

	JSONObject json = new JSONObject(originalMD.getMetadata());

	MIMetadata miMetadata = ret.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	miMetadata.setHierarchyLevelName("dataset");
	miMetadata.addHierarchyLevelScopeCodeListValue("dataset");

	List<JSONObject> collectionMetadataList = null;

	//
	// data quality
	//
	Iterator<DataQuality> dataQualities = miMetadata.getDataQualities();

	DataQuality dataQuality = null;

	if (!dataQualities.hasNext()) {

	    dataQuality = new DataQuality();
	    miMetadata.addDataQuality(dataQuality);

	} else {

	    dataQuality = dataQualities.next();
	}

	Optional<String> collectionOpt = readString(json, COLLECTION_ID_KEY);

	/**
	 * SECOND_LEVEL_INFO
	 */
	String baseAccessURL = null;
	if (collectionOpt.isPresent()) {
	    extHandler.setSTACSecondLevelInfo(collectionOpt.get());
	    collectionMetadataList = getMetadataCollection(collectionOpt.get());
	    baseAccessURL = WorldCerealConnector.BASE_URL + "collections/" + collectionOpt.get() + "/items";
	}

	String name = null;
	String value = null;
	String providerCode = null;
	String datasetDescription = null;
	String providerURL = null;
	String nameContact = null;
	String nameDataset = null;
	String doi = null;
	String license = null;
	String requiredCitation = null;
	String licenseUrl = null;
	String objective = null;
	String infoDesign = null;
	String datasetDescription2 = null;
	String geoParquetUrl = null;
	String geoParquetUrl2 = null;
	String pdfUrl = null;
	List<String> lcTypes = new ArrayList<String>();
	List<String> cTypes = new ArrayList<String>();
	List<String> irrTypes = new ArrayList<String>();

	if (collectionMetadataList != null) {
	    for (JSONObject j : collectionMetadataList) {
		String metadataName = j.optString("name");

		switch (metadataName) {
		case "OriginalDataSet:Provider:Code:":
		    // OriginalDataSet:Provider:Code:
		    providerCode = j.optString("value");
		    break;
		case "OriginalDataSet:Provider:DescriptionCuratedDataSet:":
		    // OriginalDataSet:Provider:DescriptionCuratedDataSet:
		    datasetDescription = j.optString("value");
		    break;
		case "OriginalDataSet:Provider:URL:":
		    // OriginalDataSet:Provider:URL:
		    providerURL = j.optString("value");
		    break;
		case "OriginalDataSet:Provider:Contact:":
		    // OriginalDataSet:Provider:Contact:
		    nameContact = j.optString("value");
		    break;
		case "OriginalDataSet:NameDataSet::":
		    // OriginalDataSet:NameDataSet::
		    nameDataset = j.optString("value");
		    break;
		case "OriginalDataSet:DOI::":
		    // OriginalDataSet:DOI::
		    String toCheck = j.optString("value");
		    if (toCheck != null && toCheck.startsWith("http"))
			doi = j.optString("value");
		    break;
		case "OriginalDataSet:License:TypeOfLicense:":
		    // OriginalDataSet:License:TypeOfLicense:
		    license = j.optString("value");
		    break;
		case "OriginalDataSet:License:ReferenceToLicense:":
		    // OriginalDataSet:License:ReferenceToLicense:
		    licenseUrl = j.optString("value");
		    break;
		case "OriginalDataSet:License:RequiredCitation:":
		    // OriginalDataSet:License:RequiredCitation:
		    requiredCitation = j.optString("value");
		    break;
		case "OriginalDataSet:Objective::":
		    // OriginalDataSet:Objective::
		    objective = j.optString("value");
		    break;
		case "OriginalDataSet:Observation:ObservationMethod:SamplingDesign":
		    break;
		case "OriginalDataSet:Observation:ObservationMethod:InfoOnSamplingDesign":
		    // OriginalDataSet:Observation:ObservationMethod:InfoOnSamplingDesign
		    infoDesign = j.optString("value");
		    break;
		case "OriginalDataSet:Observation:ObservationMethod:Validation":
		    // OriginalDataSet:Observation:ObservationMethod:Validation

		    break;
		case "OriginalDataSet:Observation:ObservationMethod:InfoOnValidation":
		    // OriginalDataSet:Observation:ObservationMethod:InfoOnValidation

		    break;
		case "OriginalDataSet:Observation::ClassificationAccuracy":
		    // OriginalDataSet:Observation::ClassificationAccuracy

		    break;
		case "OriginalDataSet:Observation::SupportingMaterial":
		    // OriginalDataSet:Observation::SupportingMaterial

		    break;
		case "OriginalDataSet:Observation:Geometry:TypeOfGeometry":
		    // OriginalDataSet:Observation:Geometry:TypeOfGeometry

		    break;
		case "OriginalDataSet:Observation::GPSFieldMethod":
		    // OriginalDataSet:Observation::GPSFieldMethod

		    break;
		case "OriginalDataSet:Observation::CoordinateSystem":
		    // OriginalDataSet:Observation::CoordinateSystem

		    break;
		case "OriginalDataSet:DataFormat::":
		    // OriginalDataSet:DataFormat::

		    break;
		case "OriginalDataSet:Language::":
		    // OriginalDataSet:Language::

		    break;
		case "CuratedDataSet::DescriptionCuratedDataSet:":
		    // CuratedDataSet::DescriptionCuratedDataSet:
		    datasetDescription2 = j.optString("value");

		    break;
		case "CuratedDataSet:ObservationCuratedDataset:Geometry:Accuracy":
		    // CuratedDataSet:ObservationCuratedDataset:Geometry:Accuracy

		    break;
		case "CuratedDataSet:ObservationCuratedDataset:Geometry:Continent":
		    // CuratedDataSet:ObservationCuratedDataset:Geometry:Continent

		    break;
		case "CuratedDataSet:ObservationCuratedDataset:Geometry:Country":
		    // CuratedDataSet:ObservationCuratedDataset:Geometry:Country
		    break;

		case "CuratedDataSet:ObservationCuratedDataset:ObservationTime:":
		    // CuratedDataSet:ObservationCuratedDataset:ObservationTime:
		    break;

		case "CuratedDataSet:CurationByWordCereal::":
		    // CuratedDataSet:CurationByWordCereal::

		    break;
		case "codeStats":
		    // codeStats

		    break;

		case "GeoParquetDownloadUrl":
		    geoParquetUrl = j.optString("value");
		    break;

		case "SampleDownloadUrl":
		    geoParquetUrl2 = j.optString("value");
		    break;
		case "CuratedDataSet:ReferenceCuratedDataSet:NameCuratedDataSet:":
		    // CuratedDataSet:ReferenceCuratedDataSet:NameCuratedDataSet:

		    break;

		case "CuratedDataSet::TitleCuratedDataSet:":
		    // CuratedDataSet::TitleCuratedDataSet:

		    break;

		case "CuratedDataSet:ConfidenceIrrigationRainfed::":
		    // CuratedDataSet:ConfidenceIrrigationRainfed::

		    break;
		case "CuratedDataSet:ConfidenceCropType::":
		    // CuratedDataSet:ConfidenceCropType::

		    break;
		case "CuratedDataSet:ConfidenceLandCover::":
		    // CuratedDataSet:ConfidenceLandCover::

		    break;

		case "CuratedDataSet:ObservationCuratedDataset::NoOfObservations":
		    // CuratedDataSet:ObservationCuratedDataset::NoOfObservations

		    break;

		case "OriginalDataSet:Observation:ObservationMethod:TypeOfObservationMethod":
		    // OriginalDataSet:Observation:ObservationMethod:TypeOfObservationMethod

		    break;

		case "CuratedDataSet:ObservationCuratedDataset:Geometry:PointOrPolygonOrRaster":
		    // CuratedDataSet:ObservationCuratedDataset:Geometry:PointOrPolygonOrRaster

		    break;

		case "CuratedDataSet:ObservationCuratedDataset::ListOfIrrigationCodes":
		    // CuratedDataSet:ObservationCuratedDataset::ListOfIrrigationCodes
		    String irrTypesValue = j.optString("value");
		    if (irrTypesValue != null && !irrTypesValue.isEmpty()) {
			String[] splittedIrrtypes = irrTypesValue.split(";");
			irrTypes = Arrays.asList(splittedIrrtypes);
		    }
		    break;

		case "CuratedDataSet:ObservationCuratedDataset::ListOfCropTypes":
		    // CuratedDataSet:ObservationCuratedDataset::ListOfCropTypes
		    String cropTypesValue = j.optString("value");
		    if (cropTypesValue != null && !cropTypesValue.isEmpty()) {
			String[] splittedCropTypes = cropTypesValue.split(";");
			cTypes = Arrays.asList(splittedCropTypes);
		    }
		    break;

		case "CuratedDataSet:ObservationCuratedDataset::ListOfLandCovers":
		    // CuratedDataSet:ObservationCuratedDataset::ListOfLandCovers
		    String lcTypesValue = j.optString("value");
		    if (lcTypesValue != null && !lcTypesValue.isEmpty()) {
			String[] splittedLctypes = lcTypesValue.split(";");
			lcTypes = Arrays.asList(splittedLctypes);
		    }

		    break;

		case "CuratedDataSet:ObservationCuratedDataset::LastDateObservation":
		    // CuratedDataSet:ObservationCuratedDataset::LastDateObservation

		    break;

		case "CuratedDataSet:ObservationCuratedDataset:Geometry:BoundingBoxUR":
		    // CuratedDataSet:ObservationCuratedDataset:Geometry:BoundingBoxUR

		    break;

		case "CuratedDataSet:ObservationCuratedDataset:Geometry:BoundingBoxLL":
		    // CuratedDataSet:ObservationCuratedDataset:Geometry:BoundingBoxLL

		    break;

		case "CuratedDataSet:ObservationCuratedDataset:Summary:FirstDateObservation":
		    // CuratedDataSet:ObservationCuratedDataset:Summary:FirstDateObservation

		    break;
		case "CuratedDataSet:Harmonization:Pdf":
		    pdfUrl = j.optString("value");
		    break;

		default:
		    break;
		}
	    }
	}

	/**
	 * EWOC_CODES
	 */


	WorldCerealCache wcCache = WorldCerealCache.getInstance();
	String queryables = "";
	boolean cropQueryable = false;
	boolean landCoverQueryable = false;
	boolean irrigationQueryable = false;
	JSONArray ewocCodes = json.optJSONArray(EWOC_CODES);
	List<WorldCerealItem> cropList = new ArrayList<WorldCerealItem>();
	List<WorldCerealItem> landCoverList = new ArrayList<WorldCerealItem>();
	List<WorldCerealItem> irrigationList = new ArrayList<WorldCerealItem>();
	Set<String> ewocValues = new HashSet<String>();
	for (int i = 0; i < ewocCodes.length(); i++) {
	    String ewocValue = ewocCodes.optString(i);
	    ewocValues.add(ewocValue);
	    if (wcCache.containsKeyCrop(ewocValue)) {
		cropQueryable = true;
		WorldCerealItem item = new WorldCerealItem();
		item.setLabel(wcCache.getCrop(ewocValue));
		item.setCode(ewocValue);
		cropList.add(item);
	    } else if (wcCache.containsKeyLc(ewocValue)) {
		landCoverQueryable = true;
		WorldCerealItem item = new WorldCerealItem();
		item.setLabel(wcCache.getLc(ewocValue));
		item.setCode(ewocValue);
		landCoverList.add(item);
	    }
	}

	for (String s : lcTypes) {
	    if (!ewocValues.contains(s)) {
		if (wcCache.containsKeyLc(s)) {
		    landCoverQueryable = true;
		    WorldCerealItem item = new WorldCerealItem();
		    item.setLabel(wcCache.getLc(s));
		    item.setCode(s);
		    landCoverList.add(item);
		}
	    }
	}
	for (String s : cTypes) {
	    if (!ewocValues.contains(s)) {
		if (wcCache.containsKeyCrop(s)) {
		    cropQueryable = true;
		    WorldCerealItem item = new WorldCerealItem();
		    item.setLabel(wcCache.getCrop(s));
		    item.setCode(s);
		    cropList.add(item);
		}
	    }
	}

	JSONArray irrigationCodes = json.optJSONArray(IRRIGATION_TYPES);
	Set<String> irrValues = new HashSet<String>();
	for (int j = 0; j < irrigationCodes.length(); j++) {
	    String irrCode = irrigationCodes.optString(j);
	    irrValues.add(irrCode);
	    if (wcCache.containsKeyIrr(irrCode)) {
		irrigationQueryable = true;
		WorldCerealItem item = new WorldCerealItem();
		item.setLabel(wcCache.getIrr(irrCode));
		item.setCode(irrCode);
		irrigationList.add(item);
	    }
	}

	for (String s : irrTypes) {
	    if (!irrValues.contains(s)) {
		if (wcCache.containsKeyIrr(s)) {
		    irrigationQueryable = true;
		    WorldCerealItem item = new WorldCerealItem();
		    item.setLabel(wcCache.getIrr(s));
		    item.setCode(s);
		    irrigationList.add(item);
		}
	    }
	}

	// worldCerealMap.setIrrigationTypes(irrigationItems);
	if (cropQueryable) {
	    queryables = "cropTypes,";
	}
	if (landCoverQueryable) {
	    queryables += "landCoverTypes,";
	}
	if (irrigationQueryable) {
	    queryables += "irrigationTypes,";
	}
	if (!queryables.isEmpty()) {
	    queryables = queryables.substring(0, queryables.length() - 1);
	}

	Double confidenceCropType = json.optDouble(CONFIDENCE_CROP_TYPE);
	Double confidenceIrrType = json.optDouble(CONFIDENCE_IRRIGATION_TYPE);
	Double confidenceLcType = json.optDouble(CONFIDENCE_LAND_COVER);

	WorldCerealMap worldCerealMap = new WorldCerealMap();
	worldCerealMap.setWorldCerealQueryables(queryables);
	worldCerealMap.setCropTypes(cropList);
	worldCerealMap.setIrrigationTypes(irrigationList);
	worldCerealMap.setLandCoverTypes(landCoverList);

	if (confidenceCropType != null && !confidenceCropType.isNaN()) {
	    worldCerealMap.setCropTypeConfidence(confidenceCropType);

	    addReportQuality(dataQuality, "cropConfidence", confidenceCropType);
	    // Keywords kwd = new Keywords();
	    // kwd.setTypeCode("cropConfidence");
	    // kwd.addKeyword(String.valueOf(confidenceCropType));
	    // miMetadata.getDataIdentification().addKeywords(kwd);

	}
	if (confidenceIrrType != null && !confidenceIrrType.isNaN()) {
	    worldCerealMap.setIrrigationTypeConfidence(confidenceIrrType);
	    addReportQuality(dataQuality, "irrigationConfidence", confidenceIrrType);
	    // Keywords kwd = new Keywords();
	    // kwd.setTypeCode("irrigationConfidence");
	    // kwd.addKeyword(String.valueOf(confidenceIrrType));
	    // miMetadata.getDataIdentification().addKeywords(kwd);
	}
	if (confidenceLcType != null && !confidenceLcType.isNaN()) {
	    worldCerealMap.setLcTypeConfidence(confidenceLcType);
	    addReportQuality(dataQuality, "landCoverConfidence", confidenceLcType);
	    // Keywords kwd = new Keywords();
	    // kwd.setTypeCode("landCoverConfidence");
	    // kwd.addKeyword(String.valueOf(confidenceLcType));
	    // miMetadata.getDataIdentification().addKeywords(kwd);
	}

	extHandler.setWorldCereal(worldCerealMap);

	ret.getExtensionHandler().setIsInSitu();

	// readString(json, COLLECTION_ID_KEY).ifPresent(id -> extHandler.setSTACSecondLevelInfo(id));

	/**
	 * TITLE
	 */

	readString(json, TITLE_KEY).ifPresent(title -> miMetadata.getDataIdentification().setCitationTitle(title));

	/**
	 * ABSTRAKT
	 */
	if (nameDataset != null) {
	    miMetadata.getDataIdentification().setAbstract(nameDataset);
	}

	/**
	 * ID
	 */
	Optional<String> optId = readString(json, RESOURCE_ID_KEY);
	String uuid = null;
	if (optId.isPresent()) {
	    uuid = optId.get();
	    miMetadata.setFileIdentifier(uuid);
	}
	// readString(json, RESOURCE_ID_KEY).ifPresent(id -> miMetadata.setFileIdentifier(id));

	/**
	 * LICENSE
	 */
	if (license != null) {
	    LegalConstraints legalConstraints = new LegalConstraints();
	    legalConstraints.addUseLimitation(license);
	    if (licenseUrl != null) {
		legalConstraints.addAccessConstraintsCode(licenseUrl);
	    }
	    if (requiredCitation != null) {
		legalConstraints.addOtherConstraints(requiredCitation);
	    }
	    miMetadata.getDataIdentification().addLegalConstraints(legalConstraints);
	}
	/**
	 * RESPONSIBLE PARTY
	 */
	// to be reviewed
	if (providerCode != null) {
	    // metadata point of contact and mail
	    ResponsibleParty metadataResponsibleParty = new ResponsibleParty();
	    Contact metadatacontactInfo = new Contact();
	    if (nameContact != null && !nameContact.isEmpty()) {

		metadataResponsibleParty.setIndividualName(nameContact);
		// Address metadataAddress = new Address();
		// metadataAddress.addElectronicMailAddress(nameContact);
		// metadatacontactInfo.setAddress(metadataAddress);
	    }
	    // responsibleParty.setIndividualName(contactIndividualName);

	    // TODO: distribution link at the moment points to https://prisma.asi.it/
	    if (providerURL != null) {
		Online metadataOnline = new Online();
		metadataOnline.setLinkage(providerURL);
		metadatacontactInfo.setOnline(metadataOnline);
	    }

	    metadataResponsibleParty.setContactInfo(metadatacontactInfo);
	    metadataResponsibleParty.setRoleCode("originator");
	    metadataResponsibleParty.setOrganisationName(providerCode);
	    miMetadata.getDataIdentification().addPointOfContact(metadataResponsibleParty);
	}

	Optional<Integer> count = readInt(json, FEATURE_COUNT);

	if (count.isPresent()) {
	    COLLECTIONS_WITH_GRANULES_COUNT++;
	    if (count.get() > THRESOLD) {
		return null;
	    }
	}

	// readString(json, PROVIDERS).ifPresent(date ->
	// miMetadata.getDataIdentification().setCitationPublicationDate(date));

	addExtent(json, miMetadata);

	// addContactInfo(miMetadata, json.optJSONArray(PROVIDERS));

	/**
	 * Distribution DOI
	 */
	if (doi != null) {
	    Online online = new Online();

	    online.setLinkage(doi);
	    online.setProtocol(NetProtocols.HTTP.getCommonURN());
	    online.setFunctionCode("information");
	    online.setDescription("DOI");

	    miMetadata.getDistribution().addDistributionOnline(online);
	}

	/**
	 * BASE ACCESS URL
	 */

	if (baseAccessURL != null) {
	    // Online online = new Online();
	    // online.setLinkage(baseAccessURL);
	    // online.setProtocol(WorldCerealCollectionMapper.SCHEMA_URI);
	    // online.setFunctionCode("download");
	    // online.setDescription("Base Access URL");
	    //
	    // miMetadata.getDistribution().addDistributionOnline(online);
	}

	if (geoParquetUrl != null) {
	    Online online = new Online();

	    online.setLinkage(geoParquetUrl);
	    online.setProtocol(NetProtocols.HTTP.getCommonURN());
	    online.setFunctionCode("download");
	    online.setDescription("Download complete dataset (not filtered)");

	    miMetadata.getDistribution().addDistributionOnline(online);
	}

	// if (geoParquetUrl2 != null) {
	// Online online = new Online();
	//
	// online.setLinkage(geoParquetUrl2);
	// online.setProtocol(NetProtocols.HTTP.getCommonURN());
	// online.setFunctionCode("download");
	// online.setDescription("SampleDownloadUrl");
	// if (uuid != null)
	// online.setIdentifier(uuid);
	//
	// miMetadata.getDistribution().addDistributionOnline(online);
	// }

	if (pdfUrl != null) {
	    Online pfdInfo = new Online();
	    pfdInfo.setLinkage(pdfUrl);
	    pfdInfo.setProtocol(NetProtocols.HTTP.getCommonURN());
	    pfdInfo.setFunctionCode("information");
	    pfdInfo.setDescription("Background Information on Curation");
	    miMetadata.getDistribution().addDistributionOnline(pfdInfo);
	}

	Online information = new Online();
	information.setLinkage(BACKGROUND_INFO_URL);
	information.setProtocol(NetProtocols.HTTP.getCommonURN());
	information.setFunctionCode("information");
	information.setDescription("WorldCereal Legend");
	miMetadata.getDistribution().addDistributionOnline(information);
	
	Online irrigationInfoURL = new Online();
	irrigationInfoURL.setLinkage(IRRIGATION_INFO_URL);
	irrigationInfoURL.setProtocol(NetProtocols.HTTP.getCommonURN());
	irrigationInfoURL.setFunctionCode("information");
	irrigationInfoURL.setDescription("Irrigation Legend");
	miMetadata.getDistribution().addDistributionOnline(irrigationInfoURL);

	
	Online confidenceScore = new Online();
	confidenceScore.setLinkage(CONFIDENCE_SCORE_URL);
	confidenceScore.setProtocol(NetProtocols.HTTP.getCommonURN());
	confidenceScore.setFunctionCode("information");
	confidenceScore.setDescription("Confidence Score");
	miMetadata.getDistribution().addDistributionOnline(confidenceScore);

	Online calculationDate = new Online();
	calculationDate.setLinkage(CALCULATION_DATE_URL);
	calculationDate.setProtocol(NetProtocols.HTTP.getCommonURN());
	calculationDate.setFunctionCode("information");
	calculationDate.setDescription("Calculation of date");
	miMetadata.getDistribution().addDistributionOnline(calculationDate);


	// addDistribution(json, miMetadata);

	// enrichMetadata(miMetadata);

	return ret;



    }

    private void addReportQuality(DataQuality dataQuality, String type, Double confidenceType) {

	DQAccuracyOfATimeMeasurementType accuracyMeasurement = new DQAccuracyOfATimeMeasurementType();
	CharacterStringPropertyType measureDescription = new CharacterStringPropertyType();
	measureDescription = ISOMetadata.createCharacterStringPropertyType(String.valueOf(confidenceType));
	accuracyMeasurement.setMeasureDescription(measureDescription);

	List<CharacterStringPropertyType> measureNames = new ArrayList<CharacterStringPropertyType>();
	CharacterStringPropertyType mName = new CharacterStringPropertyType();
	mName = ISOMetadata.createCharacterStringPropertyType(type);
	measureNames.add(mName);
	accuracyMeasurement.setNameOfMeasure(measureNames);

	List<DQResultPropertyType> valueResultsList = new ArrayList<DQResultPropertyType>();
	accuracyMeasurement.setResult(valueResultsList);

	List<DateTimePropertyType> dateTimeList = new ArrayList<DateTimePropertyType>();
	accuracyMeasurement.setDateTime(dateTimeList);

	// Create a JAXBElement for DQAccuracyOfATimeMeasurementType
	ObjectFactory fact = new ObjectFactory();
	JAXBElement<DQAccuracyOfATimeMeasurementType> accuracy = fact.createDQAccuracyOfATimeMeasurement(accuracyMeasurement);
	dataQuality.addReport(accuracy);

    }

    private List<JSONObject> getMetadataCollection(String id) {
	List<JSONObject> res = new ArrayList<JSONObject>();
	String url = WorldCerealConnector.BASE_URL;
	url += WorldCerealConnector.COLLECTIONS_URL + "/" + id + "/" + COLLECTIONS_METADATA_URL;

	Integer code = null;
	int tries = 3;

	while ((code == null || code > 400) && tries > 0) {
	    try {

		Downloader downloader = new Downloader();
		HttpResponse<InputStream> response = downloader.downloadResponse(url);
		code = response.statusCode();

		if (code > 400) {
		    // try again
		    tries--;
		    Thread.sleep(10000);
		} else {

		    JSONArray arr = new JSONArray(IOUtils.toString(response.body(), "UTF-8"));
		    for (int i = 0; i < arr.length(); i++) {
			res.add(arr.getJSONObject(i));
		    }
		}

	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(WorldCerealCollectionMapper.class).info("ERROR getting response from: {}", url);
	    }
	}
	return res;

    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SCHEMA_URI;
    }

    /**
     * @param object
     * @param key
     * @return
     */
    static boolean checkNotNull(JSONObject object, String key) {

	return (object.has(key) && !object.isNull(key));
    }

    /**
     * @param object
     * @param key
     * @return
     */
    public static Optional<String> readString(JSONObject object, String key) {

	if (checkNotNull(object, key)) {

	    return Optional.of(object.get(key).toString());
	}

	return Optional.empty();

    }

    /**
     * @param object
     * @param key
     * @return
     */
    public static Optional<Integer> readInt(JSONObject object, String key) {

	if (checkNotNull(object, key)) {

	    return Optional.of(object.getInt(key));
	}

	return Optional.empty();
    }

    /**
     * @param object
     * @param key
     * @return
     */
    public static Optional<Object> readObject(JSONObject object, String key) {

	if (checkNotNull(object, key))
	    return Optional.of(object.get(key));

	return Optional.empty();

    }

    private BrowseGraphic createGraphicOverview(String url) {

	BrowseGraphic graphic = new BrowseGraphic();

	graphic.setFileName(url);
	graphic.setFileType("image/png");

	return graphic;
    }

    /**
     * @param polygon
     * @param firstIsLat
     * @return
     */
    public static String toBBOX(String polygon, boolean firstIsLat) {

	String[] coords = polygon.split(" ");

	Double[] lats = new Double[coords.length / 2];
	Double[] lons = new Double[coords.length / 2];

	for (int i = 0; i < coords.length; i++) {

	    if (i % 2 == 0) {

		if (firstIsLat) {
		    lats[i / 2] = Double.valueOf(coords[i]);
		} else {
		    lons[i / 2] = Double.valueOf(coords[i]);
		}
	    } else {

		if (!firstIsLat) {
		    lats[i / 2] = Double.valueOf(coords[i]);
		} else {
		    lons[i / 2] = Double.valueOf(coords[i]);
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

    /**
     * @param json
     * @param md
     */

    private void addExtent(JSONObject json, MIMetadata md) {
	if (json.has(EXTENT)) {
	    Optional<Object> extent = readObject(json, EXTENT);
	    if (extent.isPresent()) {
		Optional<Object> temporalObject = readObject((JSONObject) extent.get(), TEMPORAL);
		Optional<Object> spatiallObject = readObject((JSONObject) extent.get(), SPATIAL);
		// BBOX: specification WGS 84 longitude/latitude
		if (spatiallObject.isPresent()) {
		    JSONObject bboxObject = (JSONObject) spatiallObject.get();
		    JSONArray bboxArray = bboxObject.optJSONArray("bbox");
		    if (bboxArray != null) {
			if (bboxArray.length() > 0) {
			    JSONArray doublesArray = bboxArray.optJSONArray(0);
			    if (doublesArray != null && doublesArray.length() == 4) {
				Double west = doublesArray.optDouble(0);
				Double east = doublesArray.optDouble(2);
				Double north = doublesArray.optDouble(3);
				Double south = doublesArray.optDouble(1);
				if (!west.isNaN() && !east.isNaN() && !north.isNaN() && !south.isNaN()) {
				    md.getDataIdentification().addGeographicBoundingBox(north, west, south, east);
				}
			    }

			}
		    }
		}
		// TIME: Each inner array consists of exactly two elements, either a timestamp or null.
		// Open date ranges are supported by setting the start and/or the end time to null.
		// Timestamps consist of a date and time in UTC and MUST be formatted according to RFC 3339, section
		// 5.6.
		if (temporalObject.isPresent()) {
		    JSONObject timeObject = (JSONObject) temporalObject.get();
		    JSONArray intervalArray = timeObject.optJSONArray("interval");
		    // Optional<String> interval = readString(timeObject, "interval");
		    if (intervalArray != null) {
			if (intervalArray.length() > 0) {
			    JSONArray timeArray = intervalArray.optJSONArray(0);

			    if (timeArray != null && timeArray.length() == 2) {
				String startDate = timeArray.optString(0);
				String endDate = timeArray.optString(1);

				TemporalExtent tempExtent = new TemporalExtent();
				if (!startDate.isEmpty() && !endDate.isEmpty()) {

				    String iDate = startDate;
				    String eDate = endDate;
				    if (startDate.contains("T")) {
					iDate = startDate.split("T")[0];
				    }
				    if (endDate.contains("T")) {
					eDate = endDate.split("T")[0];
				    }

				    tempExtent.setBeginPosition(iDate);

				    tempExtent.setEndPosition(eDate);
				}

				md.getDataIdentification().addTemporalExtent(tempExtent);
			    }

			}
		    }
		}
	    }
	}

    }

    public static void main(String[] args) throws Exception {
	DatasetCollection ret = new DatasetCollection();
	MIMetadata miMetadata = ret.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	miMetadata.setHierarchyLevelName("series");
	miMetadata.addHierarchyLevelScopeCodeListValue("series");

	Iterator<DataQuality> dataQualities = miMetadata.getDataQualities();

	DataQuality dataQuality = null;

	if (!dataQualities.hasNext()) {

	    dataQuality = new DataQuality();
	    miMetadata.addDataQuality(dataQuality);

	} else {

	    dataQuality = dataQualities.next();
	}

	//
	// data quality lineage
	//
	String statement = "Tuotteeseen liittyvä palvelutarjonta: hydrologisen tietokannan perusdata,";
	statement += "datan tilastolliset analyysit, vesien käyttöä palvelevat";
	statement += "laskentaohjelmistot(säännöstelylaskenta ja vaikutusten arviointi).";
	statement += "Tuotteen tietojen lähde, ympäristöhallinnon hydrologinen havaintotoiminta,";
	statement += "muiden vesioikeuslupien haltijoiden havaintotoiminta, ostopalvelusopimuksina saatavat";
	statement += "havainnot (lähinnä Ilmatieteen laitos).";

	dataQuality.setLineageStatement(statement);

	//
	// data quality scope
	//
	DQScopePropertyType dqScopePropertyType = new DQScopePropertyType();
	DQScopeType dqScopeType = new DQScopeType();
	MDScopeCodePropertyType mdScopeCodePropertyType = new MDScopeCodePropertyType();
	CodeListValueType valueType = ISOMetadata.createCodeListValueType(ISOMetadata.MX_SCOPE_CODE_CODELIST, "dataset",
		ISOMetadata.ISO_19115_CODESPACE, "dataset");
	mdScopeCodePropertyType.setMDScopeCode(ObjectFactories.GMD().createMDScopeCode(valueType));
	dqScopeType.setLevel(mdScopeCodePropertyType);
	dqScopePropertyType.setDQScope(dqScopeType);

	DQDataQualityType dqElement = dataQuality.getElement().getValue();
	dqElement.setScope(dqScopePropertyType);

	//
	// data quality report
	//

	DQAccuracyOfATimeMeasurementType accuracyMeasurement = new DQAccuracyOfATimeMeasurementType();
	CharacterStringPropertyType measureDescription = new CharacterStringPropertyType();
	measureDescription = ISOMetadata.createCharacterStringPropertyType("Crop");
	accuracyMeasurement.setMeasureDescription(measureDescription);

	List<CharacterStringPropertyType> measureNames = new ArrayList<CharacterStringPropertyType>();
	CharacterStringPropertyType mName = new CharacterStringPropertyType();
	mName = ISOMetadata.createCharacterStringPropertyType("98");
	measureNames.add(mName);
	accuracyMeasurement.setNameOfMeasure(measureNames);

	List<DQResultPropertyType> valueResultsList = new ArrayList<DQResultPropertyType>();
	accuracyMeasurement.setResult(valueResultsList);

	List<DateTimePropertyType> dateTimeList = new ArrayList<DateTimePropertyType>();
	accuracyMeasurement.setDateTime(dateTimeList);

	// Create a JAXBElement for DQAccuracyOfATimeMeasurementType
	ObjectFactory fact = new ObjectFactory();
	JAXBElement<DQAccuracyOfATimeMeasurementType> accuracy = fact.createDQAccuracyOfATimeMeasurement(accuracyMeasurement);
	dataQuality.addReport(accuracy);

	dataQuality.addReport(accuracy);

	System.out.println(miMetadata.asString(true));

    }

}
