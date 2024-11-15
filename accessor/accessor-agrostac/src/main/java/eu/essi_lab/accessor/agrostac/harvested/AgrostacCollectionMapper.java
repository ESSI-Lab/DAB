package eu.essi_lab.accessor.agrostac.harvested;

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
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSPropertyHandler;
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

public class AgrostacCollectionMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());
    protected static final String DEFAULT_CLIENT_ID = "gs-service";
    public static final String SCHEMA_URI = "http://essi-lab.eu/agrostac/collections";

    static final String COLLECTIONS_METADATA_URL = "metadata/items";

    public static final String AGROSTAC_SECOND_LEVEL_TEMPLATE = "agrostacSecondLevel";

    private static final String RESOURCE_ID_KEY = "id";

    // DATASET INFO
    private static final String LICENSE = "license";
    private static final String WIKI_URL = "wiki_url";
    private static final String RELATED_PUBLICATION = "related_publication";
    private static final String COLLECTION_ID_KEY = "datasetid";
    private static final String ORG_NAME = "organization_name";
    private static final String TITLE = "title";
    private static final String SUMMARY = "summary";
    private static final String DATASET_ACCESS = "dataset_access";
    private static final String START_DATE = "min_startdate";
    private static final String END_DATE = "max_enddate";
    private static final String DATASET_CODE = "dataset_code";
    private static final String SOURCE_URL = "source_url";
    private static final String ORG_ADDRESS = "organization_web_address";
    //

    // QUANTITY_COUNT
    private static final String CANOPY_HEIGHT_M_COUNT = "canopy_height_m_count"; // CANOPY_HEIGHT_M
    private static final String LAIG_COUNT = "laig_count"; // LAIG
    private static final String CROP_DEV_BBCH_COUNT = "crop_dev_bbch_count"; // CROP_DEV_BBCH
    private static final String LAIT_COUNT = "lait_count"; // LAIT
    private static final String CUL_NAME_COUNT = "cul_name_count"; // CUL_NAME
    private static final String PLANT_DENSITY_CNT_M2_COUNT = "plant_density_cnt_m2_count"; // PLANT_DENSITY_CNT_M2
    private static final String SO_DWT_KGHA_COUNT = "so_dwt_kgha_count"; // SO_DWT_KGHA
    private static final String TOPS_DWT_KGHA_COUNT = "tops_dwt_kgha_count"; // TOPS_DWT_KGHA
    private static final String SO_FWT_KGHA_COUNT = "so_fwt_kgha_count"; // SO_FWT_KGHA
    private static final String TOPS_FWT_KGHA_COUNT = "tops_fwt_kgha_count"; // TOPS_FWT_KGHA
    private static final String CUL_NOTES_COUNT = "cul_notes_count"; // CUL_NOTES
    private static final String SO_MOISTURE_FWT_FR_COUNT = "so_moisture_fwt_fr_count"; // SO_MOISTURE_FWT_FR

    // OVERVIEW INFO
    private static final String CROPS = "Crops";
    private static final String CROP_CODE = "crop_code";
    private static final String CROP_NAME = "crop_name";

    private static final String CROP_QUANTITIES_PATH = "cropquantities";
    private static final String CROP_QUANTITIES = "Cropquantities";

    private static final String QUANTITY_CODE = "quantitycode";
    private static final String QUANTITY_DESCRIPTION = "quantitydescriptionuk";

    private static final String CROPDATABYAREA_URL = "cropdatabyarea";

    //
    private static final String minLat = "min_latitudedd";
    private static final String minLon = "min_longitudedd";
    private static final String maxLat = "max_latitudedd";
    private static final String maxLon = "max_longitudedd";

    // private static final String LICENSE = "license";
    // private static final String ABSTRACT_KEY = "description";
    //
    // private static final String PROPERTIES = "properties";
    // private static final String PROVIDERS = "providers";
    // private static final String KEYWORDS = "keywords";

    // private static final String ASSETS = "assets";

    public static int COLLECTIONS_WITH_GRANULES_COUNT = 0;

    protected transient Downloader downloader;

    public Downloader getDownloader() {
	return downloader == null ? new Downloader() : downloader;
    }

    public enum CROP_CODES {

	BN("common_beans","1105010018"),WHD("durum_hard_wheat","1101010020"),TRI("triticale","1101050000"),ONI("onions","1103110040"),SBN("soy_soybeans", "1106000020"), CBN("beans", "1105010010"), COT("cotton", "1108000010"), SUN("sunflower", "1106000010"), RYEW(
		"winter_rye", "1101030001"), WHBW("winter_common_soft_wheat", "1101010011"), TRO("unspecified_wheat", "1101010000"), SGG(
			"unspecified_sorghum", "1101070030"), PML("unspecified_millet", "1101070010"), RIC("rice", "1101080000"), GRS(
				"grass_fodder_crops", "1111000000"), RAP("rapeseed_rape", "1106000030"), RAPW("winter_rapeseed_rape",
					"1106000031"), POT("potatoes", "1107000010"), FAB("beans", "1105010010"), RYE("unspecified_rye",
						"1101030000"), OAT("unspecified_oats", "1101040000"), LABL("beans",
							"1105010010"), MAZ("maize", "1101060000"), RAPI("brassicaceae_cruciferae",
								"1103050000"), BAR("unspecified_barley", "1101020000"), PEA("peas",
									"1105010020"), WHB("common_soft_wheat", "1101010010"), BBN("beans",
										"1105010010"), FBT("fodder_beet", "1107000032"), FLAX(
											"flax", "1108020010"), BARW("winter_barley",
												"1101020001"), SBT("sugar_beet",
													"1107000031"), BARS("spring_barley",
														"1101020002");

	private String name;
	private String code;

	CROP_CODES(String name, String code) {
	    this.name = name;
	    this.code = code;
	}

	public String getName() {
	    return name;
	}

	public String getCode() {
	    return code;
	}

	public static CROP_CODES decode(String code) {
	    for (CROP_CODES c : values()) {
		if (code.equals(c.name())) {
		    return c;
		}
	    }
	    return null;
	}

	public static CROP_CODES fromCode(String code) {
	    for (CROP_CODES c : values()) {
		if (code.equals(c.getCode())) {
		    return c;
		}
	    }
	    return null;
	}

    }

    public enum QUANTITY_CODES {

	CANOPY_HEIGHT_M("CANOPY_HEIGHT_M", CANOPY_HEIGHT_M_COUNT), LAIG("LAIG", LAIG_COUNT), CROP_DEV_BBCH("CROP_DEV_BBCH",
		CROP_DEV_BBCH_COUNT), LAIT("LAIT", LAIT_COUNT), CUL_NAME("CUL_NAME", CUL_NAME_COUNT), PLANT_DENSITY_CNT_M2(
			"PLANT_DENSITY_CNT_M2", PLANT_DENSITY_CNT_M2_COUNT), SO_DWT_KGHA("SO_DWT_KGHA", SO_DWT_KGHA_COUNT), TOPS_DWT_KGHA(
				"TOPS_DWT_KGHA", TOPS_DWT_KGHA_COUNT), SO_FWT_KGHA("SO_FWT_KGHA", SO_FWT_KGHA_COUNT), TOPS_FWT_KGHA(
					"TOPS_FWT_KGHA", TOPS_FWT_KGHA_COUNT), CUL_NOTES("CUL_NOTES", CUL_NOTES_COUNT), SO_MOISTURE_FWT_FR(
						"SO_MOISTURE_FWT_FR", SO_MOISTURE_FWT_FR_COUNT), CROP_CODE("CROP_CODE", "CROP_CODE");

	private String name;
	private String code;

	QUANTITY_CODES(String name, String code) {
	    this.name = name;
	    this.code = code;
	}

	public String getName() {
	    return name;
	}

	public String getCode() {
	    return code;
	}

	public static QUANTITY_CODES decode(String code) {
	    for (QUANTITY_CODES c : values()) {
		if (code.equals(c.name())) {
		    return c;
		}
	    }
	    return null;
	}

	public static QUANTITY_CODES fromCode(String code) {
	    for (QUANTITY_CODES c : values()) {
		if (code.equals(c.getCode())) {
		    return c;
		}
	    }
	    return null;
	}

    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	/**
	 * CROP CODE
	 * {
	 * "crop_code": "SBT",
	 * "crop_name": "Sugar beet (Beta vulgaris)",
	 * "dataset_access": "OPEN"
	 * }
	 */

	/**
	 * DATASET METADATA
	 * {
	 * "wiki_url": "http://wiki.agrostac.geodesk.nl/index.php/DWD_PHENO",
	 * "related_publication": "DWD Climate Data Center (CDC): Phenological observations of crops from sowing to
	 * harvest (annual reporters, historical), Version v006, 2019",
	 * "organization_name": "Deutscher Wetterdienst (DWD), Germany",
	 * "min_latitudedd": 47.5544,
	 * "title": "Phenological observations of crops from sowing to harvest in Germany (location is 5 km radius with
	 * for each crop a representative field being monitored)",
	 * "dataset_code": "DWD_PHENO",
	 * "max_latitudedd": 54.9,
	 * "source_url":
	 * "https://opendata.dwd.de/climate_environment/CDC/observations_germany/phenology/annual_reporters/crops/",
	 * "organization_web_address": "https://www.dwd.de",
	 * "license": "Creative Commons Attribution 4.0 International License; CC BY (Attribution), see
	 * https://opendata.dwd.de/climate_environment/CDC/Terms_of_use.pdf",
	 * "min_longitudedd": 6.0,
	 * "max_longitudedd": 15.0333,
	 * "datasetid": 25
	 * }
	 */

	/**
	 * {
	 * "canopy_height_m_count": 0,
	 * "crop_code": "RYEW",
	 * "wiki_url": "http://wiki.agrostac.geodesk.nl/index.php/DWD_PHENO",
	 * "crop_name": "Winter rye (Secale cereale)",
	 * "dataset_access": "OPEN",
	 * "related_publication": "DWD Climate Data Center (CDC): Phenological observations of crops from sowing to
	 * harvest (annual reporters, historical), Version v006, 2019",
	 * "laig_count": 0,
	 * "crop_dev_bbch_count": 296696,
	 * "organization_name": "Deutscher Wetterdienst (DWD), Germany",
	 * "lait_count": 0,
	 * "title": "Phenological observations of crops from sowing to harvest in Germany (location is 5 km radius with
	 * for each crop a representative field being monitored)",
	 * "cul_name_count": 0,
	 * "dataset_code": "DWD_PHENO",
	 * "source_url":
	 * "https://opendata.dwd.de/climate_environment/CDC/observations_germany/phenology/annual_reporters/crops/",
	 * "organization_web_address": "https://www.dwd.de",
	 * "plant_density_cnt_m2_count": 0,
	 * "tops_fwt_kgha_count": 0,
	 * "license": "Creative Commons Attribution 4.0 International License; CC BY (Attribution), see
	 * https://opendata.dwd.de/climate_environment/CDC/Terms_of_use.pdf",
	 * "so_dwt_kgha_count": 0,
	 * "tops_dwt_kgha_count": 0,
	 * "so_fwt_kgha_count": 0,
	 * "datasetid": 25,
	 * "cul_notes_count": 0,
	 * "so_moisture_fwt_fr_count": 0
	 * },
	 */

	Dataset ret = new Dataset();
	ret.setSource(source);

	ExtensionHandler extHandler = ret.getExtensionHandler();

	JSONObject json = new JSONObject(originalMD.getMetadata());

	MIMetadata miMetadata = ret.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	miMetadata.setHierarchyLevelName("dataset");
	miMetadata.addHierarchyLevelScopeCodeListValue("dataset");

	Optional<String> optDataAccess = readString(json, DATASET_ACCESS);
	if (optDataAccess.isPresent()) {
	    if (!optDataAccess.get().toLowerCase().equals("open"))
		return null;
	}

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
	String datasetId = "";
	if (collectionOpt.isPresent()) {
	    datasetId = collectionOpt.get();
	    extHandler.setSTACSecondLevelInfo(datasetId);
	    // collectionMetadataList = getMetadataCollection(collectionOpt.get());
	    baseAccessURL = AgrostacConnector.BASE_URL + "collections/" + datasetId + "/items";
	}

	String nameDataset = null;
	String licenseUrl = null;

	// if (json != null && !json.isEmpty()) {
	// readString(json, TITLE);
	// }

	/**
	 * ACCESS TOKEN
	 */
	GSPropertyHandler info = originalMD.getAdditionalInfo();
	String token = null;
	if (info != null) {
	    token = info.get("accesstoken", String.class);
	}

	String endpoint = AgrostacConnector.BASE_URL;
	String queryables = "";

	boolean cropQueryable = false;
	boolean quantityQueryable = false;
	// JSONArray ewocCodes = json.optJSONArray(EWOC_CODES);
	List<WorldCerealItem> cropList = new ArrayList<WorldCerealItem>();

	List<WorldCerealItem> quantityList = new ArrayList<WorldCerealItem>();

	AgrostacCache agrostacCache = AgrostacCache.getInstance(token);

	JSONObject jsonOverview = agrostacCache.getOverview();

	JSONArray overviewArray = jsonOverview.optJSONArray(CROPS);

	Map<String, Map<String, String>> cropQuantityMap = new HashMap<String, Map<String, String>>();
	Set<String> quantitySet = new HashSet<String>();
	List<String> keywords = new ArrayList<String>();
	for (int k = 0; k < overviewArray.length(); k++) {

	    JSONObject overviewObj = overviewArray.optJSONObject(k);
	    String id = overviewObj.optString(COLLECTION_ID_KEY);

	    if (id.equals(datasetId)) {
		String cropCode = overviewObj.optString(CROP_CODE);
		String cropName = overviewObj.optString(CROP_NAME);
		String cropKey = agrostacCache.getCrop(cropCode);
		if(cropKey != null && !cropKey.isEmpty()) {
		    keywords.add(cropKey);    
		}
		
		CROP_CODES cCode = CROP_CODES.decode(cropCode);
		String newCropCode = null;
		if (cCode != null) {
		    cropName = cCode.getName();
		    newCropCode = cCode.getCode();

		    cropQueryable = true;
		    quantityQueryable = true;
		    WorldCerealItem item = new WorldCerealItem();
		    item.setLabel(cropName);
		    if (newCropCode != null) {
			item.setCode(newCropCode);
		    }
		    cropList.add(item);
		    // calculate quantity for cropCode
		    Map<String, String> quantityMap = getQuantityForCrop(endpoint, token, cropCode);
		    if (!cropQuantityMap.containsKey(cropCode)) {
			cropQuantityMap.put(cropCode, quantityMap);
			for (Map.Entry<String, String> entry : quantityMap.entrySet()) {
			    String quantityCode = entry.getKey();
			    if (!quantitySet.contains(quantityCode)) {
				// check if quantityCount > 0
				QUANTITY_CODES qCode = QUANTITY_CODES.decode(quantityCode);
				if (qCode != null && !qCode.equals(QUANTITY_CODES.CROP_CODE)) {
				    int countQCode = overviewObj.optInt(qCode.getCode());
				    if (countQCode > 0) {
					quantitySet.add(quantityCode);
					String quantityDescription = entry.getValue();
					keywords.add(quantityDescription);
					WorldCerealItem quantityItem = new WorldCerealItem();
					quantityItem.setLabel(quantityDescription);
					quantityItem.setCode(quantityCode);
					quantityList.add(quantityItem);
				    }
				}
			    } else {
				logger.info("QUANTITY ALREADY ADDED!!!");
			    }

			}

		    } else {
			logger.info("CROP ALREADY ADDED!!!");
		    }

		} else {
		    logger.info("NULL!!");
		}
	    }
	}

	// for (Map.Entry<String, Map<String,String>> entry : cropQuantityMap.entrySet()) {
	// String crop = entry.getKey();
	// Map<String, String> mapQ = entry.getValue();
	// for (Map.Entry<String, String> qEntry : mapQ.entrySet()) {
	// String qCode = qEntry.getKey();
	// getArea(endpoint, token, crop, qCode, datasetId);
	// }
	// }

	// worldCerealMap.setIrrigationTypes(irrigationItems);
	if (cropQueryable) {
	    queryables = "cropTypes,";
	}
	if (quantityQueryable) {
	    queryables += "quantityTypes,";
	}

	if (!queryables.isEmpty()) {
	    queryables = queryables.substring(0, queryables.length() - 1);
	}

	Double confidenceCropType = 0.0;
	Double confidenceIrrType = 0.0;
	Double confidenceLcType = 0.0;

	WorldCerealMap worldCerealMap = new WorldCerealMap();
	worldCerealMap.setWorldCerealQueryables(queryables);
	worldCerealMap.setCropTypes(cropList);
	worldCerealMap.setQuantityTypes(quantityList);

	// if (confidenceCropType != null && !confidenceCropType.isNaN()) {
	// worldCerealMap.setCropTypeConfidence(confidenceCropType);
	//
	// addReportQuality(dataQuality, "cropConfidence", confidenceCropType);
	// // Keywords kwd = new Keywords();
	// // kwd.setTypeCode("cropConfidence");
	// // kwd.addKeyword(String.valueOf(confidenceCropType));
	// // miMetadata.getDataIdentification().addKeywords(kwd);
	//
	// }
	// if (confidenceIrrType != null && !confidenceIrrType.isNaN()) {
	// worldCerealMap.setIrrigationTypeConfidence(confidenceIrrType);
	// addReportQuality(dataQuality, "irrigationConfidence", confidenceIrrType);
	// // Keywords kwd = new Keywords();
	// // kwd.setTypeCode("irrigationConfidence");
	// // kwd.addKeyword(String.valueOf(confidenceIrrType));
	// // miMetadata.getDataIdentification().addKeywords(kwd);
	// }
	// if (confidenceLcType != null && !confidenceLcType.isNaN()) {
	// worldCerealMap.setLcTypeConfidence(confidenceLcType);
	// addReportQuality(dataQuality, "landCoverConfidence", confidenceLcType);
	// // Keywords kwd = new Keywords();
	// // kwd.setTypeCode("landCoverConfidence");
	// // kwd.addKeyword(String.valueOf(confidenceLcType));
	// // miMetadata.getDataIdentification().addKeywords(kwd);
	// }

	for (String s : keywords) {
	    Keywords kwd = new Keywords();
	    kwd.addKeyword(s);
	    miMetadata.getDataIdentification().addKeywords(kwd);
	}

	extHandler.setWorldCereal(worldCerealMap);

	ret.getExtensionHandler().setIsInSitu();

	// readString(json, COLLECTION_ID_KEY).ifPresent(id -> extHandler.setSTACSecondLevelInfo(id));

	/**
	 * TITLE
	 */

	readString(json, TITLE).ifPresent(title -> miMetadata.getDataIdentification().setCitationTitle(title));

	/**
	 * ABSTRAKT
	 */

	readString(json, SUMMARY).ifPresent(abstrakt -> miMetadata.getDataIdentification().setAbstract(abstrakt));

	/**
	 * ID
	 */
	Optional<String> optId = readString(json, RESOURCE_ID_KEY);
	String uuid = null;
	if (optId.isPresent()) {
	    uuid = optId.get();
	    miMetadata.setFileIdentifier(uuid);
	} else {
	    Optional<String> datasetCodeOpt = readString(json, DATASET_CODE);
	    if (datasetCodeOpt.isPresent()) {
		String datasetCode = datasetCodeOpt.get();
		String t = miMetadata.getDataIdentification().getCitationTitle();
		String toHash = datasetCode;
		if (t != null) {
		    toHash += t;
		}
		uuid = UUID.nameUUIDFromBytes(toHash.getBytes()).toString();
		miMetadata.setFileIdentifier(uuid);
	    }
	}
	// readString(json, RESOURCE_ID_KEY).ifPresent(id -> miMetadata.setFileIdentifier(id));

	/**
	 * LICENSE
	 */

	Optional<String> optLicense = readString(json, LICENSE);
	Optional<String> relatedPublication = readString(json, RELATED_PUBLICATION);
	if (optLicense.isPresent()) {
	    LegalConstraints legalConstraints = new LegalConstraints();
	    legalConstraints.addUseLimitation(optLicense.get());
	    if (licenseUrl != null) {
		legalConstraints.addAccessConstraintsCode(licenseUrl);
	    }
	    if (relatedPublication.isPresent()) {
		legalConstraints.addOtherConstraints(relatedPublication.get());
	    }
	    miMetadata.getDataIdentification().addLegalConstraints(legalConstraints);
	}
	/**
	 * RESPONSIBLE PARTY
	 */

	Optional<String> orgName = readString(json, ORG_NAME);
	Optional<String> orgWebAddress = readString(json, ORG_ADDRESS);

	// to be reviewed
	if (orgName.isPresent()) {
	    // metadata point of contact and mail
	    ResponsibleParty metadataResponsibleParty = new ResponsibleParty();
	    Contact metadatacontactInfo = new Contact();

	    // Address metadataAddress = new Address();
	    // metadataAddress.addElectronicMailAddress(nameContact);
	    // metadatacontactInfo.setAddress(metadataAddress);

	    // responsibleParty.setIndividualName(contactIndividualName);

	    if (orgWebAddress.isPresent()) {
		Online metadataOnline = new Online();
		metadataOnline.setLinkage(orgWebAddress.get());
		metadatacontactInfo.setOnline(metadataOnline);
	    }

	    metadataResponsibleParty.setContactInfo(metadatacontactInfo);
	    metadataResponsibleParty.setRoleCode("originator");
	    metadataResponsibleParty.setOrganisationName(orgName.get());
	    miMetadata.getDataIdentification().addPointOfContact(metadataResponsibleParty);
	}

	// Optional<Integer> count = readInt(json, FEATURE_COUNT);
	//
	// if (count.isPresent()) {
	// COLLECTIONS_WITH_GRANULES_COUNT++;
	// if (count.get() > THRESOLD) {
	// return null;
	// }
	// }

	// readString(json, PROVIDERS).ifPresent(date ->
	// miMetadata.getDataIdentification().setCitationPublicationDate(date));

	// BBOX
	Double minLatDouble = json.optDouble(minLat);
	Double minLonDouble = json.optDouble(minLon);
	Double maxLatDouble = json.optDouble(maxLat);
	Double maxLonDouble = json.optDouble(maxLon);

	if (minLatDouble != null && !minLatDouble.isNaN() && minLonDouble != null && !minLonDouble.isNaN() && maxLatDouble != null
		&& !maxLatDouble.isNaN() && maxLonDouble != null && !maxLonDouble.isNaN()) {
	    BigDecimal north = BigDecimal.valueOf(maxLatDouble);
	    BigDecimal west = BigDecimal.valueOf(minLonDouble);
	    BigDecimal south = BigDecimal.valueOf(minLatDouble);
	    BigDecimal east = BigDecimal.valueOf(maxLonDouble);
	    miMetadata.getDataIdentification().addGeographicBoundingBox(north, west, south, east);
	}

	// addContactInfo(miMetadata, json.optJSONArray(PROVIDERS));

	/**
	 * TEMPORAL EXTENT
	 */
	Optional<String> optStartDate = readString(json, START_DATE);
	Optional<String> optEndDate = readString(json, END_DATE);

	if (optStartDate.isPresent() && optEndDate.isPresent()) {

	    TemporalExtent tempExtent = new TemporalExtent();
	    tempExtent.setBeginPosition(optStartDate.get());
	    tempExtent.setEndPosition(optEndDate.get());
	    miMetadata.getDataIdentification().addTemporalExtent(tempExtent);

	}

	/**
	 * WIKI URL
	 */

	Optional<String> wikiUrl = readString(json, WIKI_URL);
	if (wikiUrl.isPresent()) {
	    Online online = new Online();

	    online.setLinkage(wikiUrl.get());
	    online.setProtocol(NetProtocols.HTTP.getCommonURN());
	    online.setFunctionCode("information");
	    online.setDescription("Data curation URL");

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

	/**
	 * SOURCE_URL
	 */

	Optional<String> sourceUrl = readString(json, SOURCE_URL);

	if (sourceUrl.isPresent()) {
	    String checkUrl = sourceUrl.get();
	    if (checkUrl.startsWith("www")) {
		checkUrl = "https://" + checkUrl;
	    }
	    if (checkUrl.startsWith("http")) {
		Online online = new Online();
		online.setLinkage(checkUrl);
		online.setProtocol(NetProtocols.HTTP.getCommonURN());
		online.setFunctionCode("information");
		online.setDescription("Source URL");
		miMetadata.getDistribution().addDistributionOnline(online);
	    }
	}

	// if (pdfUrl != null) {
	// Online pfdInfo = new Online();
	// pfdInfo.setLinkage(pdfUrl);
	// pfdInfo.setProtocol(NetProtocols.HTTP.getCommonURN());
	// pfdInfo.setFunctionCode("information");
	// pfdInfo.setDescription("Background Information on Curation");
	// miMetadata.getDistribution().addDistributionOnline(pfdInfo);
	// }
	//
	// Online information = new Online();
	// information.setLinkage(BACKGROUND_INFO_URL);
	// information.setProtocol(NetProtocols.HTTP.getCommonURN());
	// information.setFunctionCode("information");
	// information.setDescription("WorldCereal Legend");
	// miMetadata.getDistribution().addDistributionOnline(information);

	// addDistribution(json, miMetadata);

	// enrichMetadata(miMetadata);

	return ret;

    }

    private Map<String, String> getQuantityForCrop(String endpoint, String token, String cropCode) {
	Map<String, String> ret = new HashMap<String, String>();
	String request = endpoint + CROP_QUANTITIES_PATH + "/" + cropCode + "?accesstoken=" + token;
	Optional<String> resp = getDownloader().downloadOptionalString(request);
	if (resp.isPresent()) {
	    JSONObject cropQuantities = new JSONObject(resp.get());
	    JSONArray arrayCQ = cropQuantities.optJSONArray(CROP_QUANTITIES);
	    for (int k = 0; k < arrayCQ.length(); k++) {
		JSONObject cqObj = arrayCQ.optJSONObject(k);
		String qCode = cqObj.optString(QUANTITY_CODE);
		String qDescription = cqObj.optString(QUANTITY_DESCRIPTION);
		ret.put(qCode, qDescription);
	    }

	}
	return ret;
    }

    private List<Double> getArea(String endpoint, String token, String cCode, String qCode, String datasetId) {
	List<Double> ret = new ArrayList<Double>();
	Double minWest = null;
	Double maxWest = null;
	Double minEast = null;
	Double maxEast = null;
	String request = endpoint + CROPDATABYAREA_URL + "/" + cCode + "?accesstoken=" + token + "cropquantity=" + qCode + "datasetid="
		+ datasetId;
	Optional<String> resp = getDownloader().downloadOptionalString(request);
	if (resp.isPresent()) {
	    JSONObject cropQuantities = new JSONObject(resp.get());
	    JSONArray arrayCQ = cropQuantities.optJSONArray("CropDataByArea");
	    for (int k = 0; k < arrayCQ.length(); k++) {
		JSONObject cqObj = arrayCQ.optJSONObject(k);
		Double lat = cqObj.optDouble("lat");
		Double lon = cqObj.optDouble("lon");

	    }

	}
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
	String url = AgrostacConnector.BASE_URL;
	url += AgrostacConnector.COLLECTIONS_URL + "/" + id + "/" + COLLECTIONS_METADATA_URL;

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
		GSLoggerFactory.getLogger(AgrostacCollectionMapper.class).info("ERROR getting response from: {}", url);
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

}
