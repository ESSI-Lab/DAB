package eu.essi_lab.pdk.rsm.impl.atom;

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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import com.rometools.rome.io.FeedException;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TransferOptions;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.lib.xml.atom.CustomEntry;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.AccessType;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.SatelliteScene;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.worldcereal.WorldCerealItem;
import eu.essi_lab.model.resource.worldcereal.WorldCerealMap;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * This implementation provides a {@link GSResource} mapping suitable for the GEOSS Portal
 *
 * @author Fabrizio
 */
public class AtomGPResultSetMapper extends DiscoveryResultSetMapper<String> {

    /**
     * 
     */
    private static final String GP_ENTRY_AS_STRING_ERROR = "GP_ENTRY_AS_STRING_ERROR";

    private static final String SOS_TAHMO_PROXY_PATH = "sos-tahmo-proxy";

    private static final String SOS_TAHMO_URL = "http://hnapi.hydronet.com/api/service/sos";

    private static final String SOS_TWIGA_URL = "http://hn4s.hydronet.com/api/service/TWIGA/sos";

    private String formatSourceEntry(GSSource gsSource) {

	String out = "<entry>\n";
	out += "   <title>" + gsSource.getLabel().toString().replace("&", "&amp;") + "</title>\n";
	out += "  <id>" + gsSource.getUniqueIdentifier() + "</id>\n";

	out += "  <category label=\"service\" term=\"hlevel\"/> \n";

	//
	// at the moment the service type is not available as source field
	//
	// out += " <category label=\"" + gsSource.getLabel() + "\" term=\"serviceType\"/> \n";
	out += "  <contributor> \n";
	out += "     <orgName>" + gsSource.getLabel().toString().replace("&", "&amp;") + "</orgName> \n";
	out += "     <indName/> \n";
	out += "     <email/> \n";
	out += "     <role/> \n";
	out += "  </contributor> \n";
	out += "  </entry> \n";

	return out;
    }

    @Override
    public String map(DiscoveryMessage message, GSResource resource) throws GSException {

	GSSource source = resource.getSource();

	if (message.isOutputSources()) {
	    String ret = formatSourceEntry(source);
	    return ret;
	}

	GPEntry gpEntry = new GPEntry();

	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	//
	// source
	//
	String sourceId = null;
	if (source != null) {
	    sourceId = source.getUniqueIdentifier();
	    gpEntry.addSourceInfo(source.getUniqueIdentifier(), source.getLabel());
	}

	//
	// identifier
	//
	gpEntry.setId(coreMetadata.getIdentifier());

	//
	// title
	//
	gpEntry.setTitle(encodeEntities(coreMetadata.getTitle()));

	//
	// abstract
	//
	gpEntry.setSimpleSummary(encodeEntities(coreMetadata.getAbstract()));

	//
	// bbox
	//
	GeographicBoundingBox bbox = coreMetadata.getBoundingBox();
	if (bbox != null) {

	    Double south = bbox.getSouth();
	    Double west = bbox.getWest();
	    Double north = bbox.getNorth();
	    Double east = bbox.getEast();

	    gpEntry.setBoundingBox(south + " " + west + " " + north + " " + east);
	}

	//
	// GEOSS Data Core
	//
	try {

	    Stream<LegalConstraints> stream = StreamUtils.iteratorToStream(//
		    coreMetadata.//
			    getMIMetadata().//
			    getDataIdentification().//
			    getLegalConstraints());

	    boolean gdc = stream.//
		    filter(lc -> Objects.nonNull(lc.getUseLimitation())).//
		    anyMatch(lc -> lc.getUseLimitation().equals("geossdatacore"));
	    gdc |= resource.getPropertyHandler().isGDC();

	    if (gdc) {
		gpEntry.setRights("geossdatacore");
	    }

	} catch (Exception ex) {
	}

	//
	// atom logo
	//
	try {
	    String fileName = coreMetadata.getMIMetadata().getDataIdentification().getGraphicOverview().getFileName();
	    if (fileName != null && !fileName.equals("")) {

		gpEntry.setLogo(fileName);
	    }
	} catch (NullPointerException ex) {
	}

	//
	// distribution formats
	//
	Distribution dist = coreMetadata.getMIMetadata().getDistribution();

	if (dist != null) {

	    // filter old gi-axe urls
	    filterResults(dist);

	    String formats = StreamUtils.iteratorToStream(dist.getFormats()).//
		    map(f -> f.getName()).//
		    filter(f -> f != null && !f.equals("")).//
		    collect(Collectors.joining(" ")).//
		    trim();

	    if (!formats.isEmpty()) {

		gpEntry.setContent(encodeEntities(formats));
	    }
	}

	//
	// hierarchy level
	//
	String hlevel = coreMetadata.getMIMetadata().getHierarchyLevelScopeCodeListValue();
	if (Objects.isNull(hlevel) || hlevel.isEmpty()) {
	    hlevel = "dataset";
	}
	if (resource instanceof DatasetCollection) {
	    hlevel = "series";
	}

	gpEntry.addCategory(hlevel, "hlevel");

	//
	// topic category
	//
	try {

	    String topic = coreMetadata.getMIMetadata().getDataIdentification().getTopicCategoryString();
	    if (topic != null && !topic.equals("")) {

		gpEntry.addCategory(topic, "topic");
	    }
	} catch (NullPointerException ex) {
	}

	getDataIdsStream(coreMetadata).flatMap(//
		di -> StreamUtils.iteratorToStream(//
			di.getKeywordsValues()))
		.map(kwd -> encodeEntities(kwd)).//
		forEach(kwd -> gpEntry.addCategory(kwd, "keywords"));

	//
	// time
	//
	TemporalExtent time = coreMetadata.getTemporalExtent();
	if (time != null) {

	    String beginPosition = time.getBeginPosition();
	    String endPosition = time.getEndPosition();

	    gpEntry.setStartTime(beginPosition);
	    gpEntry.setEndTime(endPosition);
	}

	getDataIdsStream(coreMetadata).//
		flatMap(d -> StreamUtils.iteratorToStream(d.getVerticalExtents())).//
		forEach(ve -> gpEntry.addVerticalExent(ve.getMinimumValue(), ve.getMaximumValue()));

	//
	// contributor
	//
	Optional<DataIdentification> first = getDataIdsStream(coreMetadata).findFirst();

	ResponsibleParty originator = first.isPresent() ? first.get().getPointOfContact("originator") : null;
	Iterator<ResponsibleParty> contacts = coreMetadata.getMIMetadata().getContacts();
	if (originator == null && contacts.hasNext()) {
	    while (contacts.hasNext()) {
		ResponsibleParty contact = contacts.next();
		String roleCode = contact.getRoleCode();
		if (roleCode != null && roleCode.equals("originator")) {
		    originator = contact;
		    break;
		}
	    }
	}

	String organisationName = originator != null ? originator.getOrganisationName() : null;
	if (organisationName == null) {
	    organisationName = resource.getSource().getLabel();
	}

	String individualName = originator != null ? originator.getIndividualName() : null;
	String roleCode = originator != null ? originator.getRoleCode() : null;

	gpEntry.addContributor(encodeEntities(organisationName), encodeEntities(individualName), null, roleCode);

	//
	// parent id
	//
	String parentId = coreMetadata.getMIMetadata().getParentIdentifier();

	if (parentId != null && !parentId.equals("")) {

	    gpEntry.setParentId(encodeEntities(parentId));
	}

	//
	// strategy
	//
	BrokeringStrategy strategy = resource.getSource().getBrokeringStrategy();

	gpEntry.setHarvested(strategy == BrokeringStrategy.HARVESTED ? true : false);

	ExtensionHandler handler = resource.getExtensionHandler();
	
	
	
	/**
	 * IN SITU
	 */
	
	
	boolean isInSitu = handler.isInSitu();// resource.getPropertyHandler().isInSitu();
	if(isInSitu) {
	    gpEntry.addSimpleElement("inSitu", "true");
	}
	

	/**
	 * GEO MOUNTAINS
	 */
	if (sourceId != null && sourceId.equals("geomountains")) {
	    // Parent network and / or other comment(s)
	    List<String> parentNetworks = handler.getOriginatorOrganisationDescriptions();
	    if (parentNetworks != null && !parentNetworks.isEmpty()) {
		gpEntry.setGEOMountainsNetwork(parentNetworks.get(0));
	    }
	    // Operating Organisation
	    List<String> opOrg = handler.getOriginatorOrganisationIdentifiers();
	    if (opOrg != null && !opOrg.isEmpty()) {
		gpEntry.setGEOMountainsOrg(opOrg.get(0));
	    }
	    String resourceIdentifier = coreMetadata.getDataIdentification().getResourceIdentifier();
	    if (resourceIdentifier != null && !resourceIdentifier.isEmpty()) {
		gpEntry.setGEOMountainsId(resourceIdentifier);
	    }

	    Optional<String> category = handler.getThemeCategory();
	    if (category.isPresent()) {
		gpEntry.setGEOMountainsCategory(category.get());
	    }

	    Iterator<CoverageDescription> coverageDescr = coreMetadata.getMIMetadata().getCoverageDescriptions();
	    List<String> list = new ArrayList<String>();
	    while (coverageDescr.hasNext()) {
		String s = coverageDescr.next().getAttributeTitle();
		list.add(s);
	    }
	    if (!list.isEmpty()) {
		gpEntry.setGEOMountainsParameters(list);
	    }

	}

	//
	// country
	//
	Optional<String> country = handler.getCountry();
	if (country.isPresent()) {
	    gpEntry.setCountry(country.get());
	}

	//
	// magnitude level
	//
	Optional<String> magnitudeLevel = handler.getMagnitudeLevel();
	if (magnitudeLevel.isPresent()) {

	    gpEntry.setMagnitude(magnitudeLevel.get());
	}

	//
	// license
	//
	Iterator<LegalConstraints> lcIterator = coreMetadata.getMIMetadata().getDataIdentification().getLegalConstraints();
	while (lcIterator.hasNext()) {
	    LegalConstraints lc = lcIterator.next();//
	    if (lc != null) {
		// license type
		String typeOfLicense = lc.getUseLimitation();
		// license reference
		String referenceURL = lc.getAccessConstraintCode();
		gpEntry.addLicense(typeOfLicense, referenceURL);

		// citation
		String citation = lc.getOtherConstraint();
		// add citation response
		gpEntry.addCitation(citation);
		break;
	    }

	}

	//
	// world cereal
	//
	String worldCerealQueryables = "";
	Optional<WorldCerealMap> worldCereal = handler.getWorldCereal();
	if (worldCereal.isPresent()) {
	    WorldCerealMap map = worldCereal.get();
	    List<WorldCerealItem> cropTypesList = map.getCropTypes();
	    List<WorldCerealItem> lcList = map.getLandCoverTypes();
	    List<WorldCerealItem> irrList = map.getIrrigationTypes();
	    List<WorldCerealItem> quantityTypesList = map.getQuantityTypes();
	    worldCerealQueryables = map.getWorldCerealQueryables();
	    if (worldCerealQueryables != null && !worldCerealQueryables.isEmpty()) {
		gpEntry.setWorldCerealQueryables(worldCerealQueryables);
	    }

	    Double cropConfidence = map.getCropTypeConfidence();
	    Double irrConfidence = map.getIrrigationTypeConfidence();
	    Double lcConfidence = map.getLcTypeConfidence();

	    if (cropConfidence != null && !cropConfidence.isNaN()) {
		gpEntry.setWorldCerealConfidence("cropConfidence", String.valueOf(cropConfidence));
	    }

	    if (irrConfidence != null && !irrConfidence.isNaN()) {
		gpEntry.setWorldCerealConfidence("irrigationConfidence", String.valueOf(irrConfidence));
	    }

	    if (lcConfidence != null && !lcConfidence.isNaN()) {
		gpEntry.setWorldCerealConfidence("landCoverConfidence", String.valueOf(lcConfidence));
	    }

	    gpEntry.addWorldCerealType(irrList, "irrigationTypes");
	    gpEntry.addWorldCerealType(lcList, "landCoverTypes");
	    gpEntry.addWorldCerealType(cropTypesList, "cropTypes");
	    gpEntry.addWorldCerealType(quantityTypesList, "quantityTypes");

	}

	Optional<String> optionalCropType = resource.getExtensionHandler().getCropTypes();
	// Optional<List<String>> optionalLandCoverType = resource.getExtensionHandler().getLandCoverTypes();
	// Optional<List<String>> optionalIrrigationType = resource.getExtensionHandler().getIrrigationTypes();
	//
	// satellite
	//
	Optional<SatelliteScene> optionalScene = resource.getExtensionHandler().getSatelliteScene();

	if (optionalScene.isPresent()) {

	    SatelliteScene satelliteScene = optionalScene.get();

	    gpEntry.setCollectionQueryables(satelliteScene.getCollectionQueryables());

	    MIPlatform miPlatform = coreMetadata.getMIMetadata().getMIPlatform();

	    Iterator<MIInstrument> miInstruments = coreMetadata.getMIMetadata().getMIInstruments();
	    MIInstrument instrument = null;
	    String instrumentTitle = null;
	    if (miInstruments.hasNext()) {
		instrument = miInstruments.next();
		instrumentTitle = checkEmptyString(instrument.getTitle());
	    }

	    String platformId = miPlatform != null ? checkEmptyString(miPlatform.getMDIdentifierCode()) : null;
	    String platformDescription = miPlatform != null ? checkEmptyString(miPlatform.getDescription()) : null;
	    String productType = checkEmptyString(satelliteScene.getProductType());

	    List<Double> percentageList = coreMetadata.getMIMetadata().getCloudCoverPercentageList();
	    Double ccp = null;
	    if (!percentageList.isEmpty()) {
		ccp = percentageList.get(0);
	    }

	    if (satelliteScene.getOrigin().equals("landsat")) {

		LandsatTag landsatTag = new LandsatTag();

		landsatTag.setPlatformId(platformId);
		landsatTag.setPlatformDesc(platformDescription);

		landsatTag.setInstrument(instrumentTitle);

		if (ccp != null) {
		    landsatTag.setCloudCoverPercentage(ccp.toString());
		}

		landsatTag.setProductType(productType);

		Integer row = satelliteScene.getRow();
		if (row != null) {
		    landsatTag.setRow(row.toString());
		}

		Integer path = satelliteScene.getPath();
		if (path != null) {
		    landsatTag.setPath(path.toString());
		}

		if (!landsatTag.isEmpty()) {

		    gpEntry.setSatelliteTag(landsatTag);
		}

	    } else {

		SentinelTag sentinelTag = new SentinelTag();

		sentinelTag.setPlatformId(platformId);
		sentinelTag.setPlatformDesc(platformDescription);

		sentinelTag.setInstrument(instrumentTitle);

		if (ccp != null) {
		    sentinelTag.setCloudCoverPercentage(ccp.toString());
		}

		sentinelTag.setProductType(productType);

		String sensorOpMode = checkEmptyString(satelliteScene.getSensorOpMode());
		if (Objects.nonNull(sensorOpMode)) {
		    sentinelTag.setInstrumentOpMode(sensorOpMode);
		}

		Integer relativeOrbit = satelliteScene.getRelativeOrbit();
		if (Objects.nonNull(relativeOrbit)) {
		    sentinelTag.setRelativeOrbit(relativeOrbit.toString());
		}

		String footprint = checkEmptyString(satelliteScene.getFootprint());
		if (Objects.nonNull(footprint)) {
		    sentinelTag.setFootprint(footprint);
		}

		String processingBaseline = checkEmptyString(satelliteScene.getProcessingBaseline());
		if (Objects.nonNull(processingBaseline)) {
		    sentinelTag.setProcessingbaseline(processingBaseline);
		}

		String processingLevel = satelliteScene.getProcessingLevel();
		if (Objects.nonNull(processingLevel)) {
		    sentinelTag.setProcessinglevel(processingLevel);
		}

		String s3InstrumentIdx = checkEmptyString(satelliteScene.getS3InstrumentIdx());
		if (Objects.nonNull(s3InstrumentIdx)) {
		    sentinelTag.setS3InstrumentIdx(s3InstrumentIdx);
		}

		String s3Time = checkEmptyString(satelliteScene.getS3Timeliness());
		if (Objects.nonNull(s3Time)) {
		    sentinelTag.setS3Timeliness(s3Time);
		}

		String s3ProductLevel = checkEmptyString(satelliteScene.getS3ProductLevel());
		if (Objects.nonNull(s3ProductLevel)) {
		    sentinelTag.setS3ProductLevel(s3ProductLevel);
		}

		String startOrbitNumber = checkEmptyString(satelliteScene.getStartOrbitNumber());
		if (Objects.nonNull(startOrbitNumber)) {
		    sentinelTag.setStartOrbitNumber(startOrbitNumber);
		}

		String orbitDirection = checkEmptyString(satelliteScene.getOrbitDirection());
		if (Objects.nonNull(orbitDirection)) {
		    sentinelTag.setOrbitdirection(orbitDirection);
		}

		String sensorPolarization = checkEmptyString(satelliteScene.getSarPolCh());
		if (Objects.nonNull(sensorPolarization)) {
		    sentinelTag.setSensorPolarisation(sensorPolarization);
		}

		String productConsolidation = checkEmptyString(satelliteScene.getProductConsolidation());
		if (Objects.nonNull(productConsolidation)) {
		    sentinelTag.setProductConsolidation(productConsolidation);
		}

		String missionDataTakeId = checkEmptyString(satelliteScene.getMissionDatatakeid());
		if (Objects.nonNull(missionDataTakeId)) {
		    sentinelTag.setMissiondatatakeid(missionDataTakeId);
		}

		String productClass = checkEmptyString(satelliteScene.getProductClass());
		if (Objects.nonNull(productClass)) {
		    sentinelTag.setProductclass(productClass);
		}

		String acquisitionType = checkEmptyString(satelliteScene.getAcquisitionType());
		if (Objects.nonNull(acquisitionType)) {
		    sentinelTag.setAcquisitiontype(acquisitionType);
		}

		String sliceNumber = checkEmptyString(satelliteScene.getSliceNumber());
		if (Objects.nonNull(sliceNumber)) {
		    sentinelTag.setSlicenumber(sliceNumber);
		}

		String stopRelativeOrbitNumber = checkEmptyString(satelliteScene.getStopRelativeOrbitNumber());
		if (Objects.nonNull(stopRelativeOrbitNumber)) {
		    sentinelTag.setStopRelativeOrbitNumber(stopRelativeOrbitNumber);
		}

		String stopOrbitNumber = checkEmptyString(satelliteScene.getStopOrbitNumber());
		if (Objects.nonNull(stopOrbitNumber)) {
		    sentinelTag.setStopOrbitNumber(stopOrbitNumber);
		}

		String status = checkEmptyString(satelliteScene.getStatus());
		if (Objects.nonNull(status)) {
		    sentinelTag.setStatus(status);
		}

		if (!sentinelTag.isEmpty()) {

		    gpEntry.setSatelliteTag(sentinelTag);
		}
	    }
	}

	Optional<String> availableGranules = resource.getExtensionHandler().getAvailableGranules();

	if (availableGranules.isPresent()) {
	    gpEntry.setAvailableGranules(availableGranules.get());
	}

	//
	// distribution
	//
	if (dist != null) {

	    String search = "</gmd:CI_OnlineResource>";
	    Optional<String> viewId = message.getWebRequest().extractViewId();
	    String url = message.getWebRequest().getUriInfo().getBaseUri().toString();

	    if (url.startsWith("http://")) {
		url = url.replace("http://", "https://");
	    }
	    String wpsUrl;
	    if (viewId.isPresent()) {
		wpsUrl = url + "/view/" + viewId.get() + "/gwps";
	    } else {
		wpsUrl = url + "/gwps";
	    }

	    List<DataComplianceReport> reports = new ReportsMetadataHandler(resource).getReports();
	    Map<String, String> distributionMap = new HashMap<String, String>();

	    String singleTransferOptionString2 = "";

	    Iterator<Online> iterator = dist.getDistributionOnlines();
	    List<String> listId = new ArrayList<>();
	    Map<String, String> mapNames = new HashMap<String, String>();
	    while (iterator.hasNext()) {
		Online it = iterator.next();
		listId.add(it.getIdentifier());
		mapNames.put(it.getIdentifier(), it.getName());
	    }

	    String toAdd = "";

	    reps: for (DataComplianceReport report : reports) {

		String onlineId = report.getOnlineId();
		listId.remove(onlineId);

		try {
		    DataComplianceTest lastSucceededTest = report.getLastSucceededTest();
		    switch (lastSucceededTest) {
		    case DOWNLOAD:
			break;
		    case EXECUTION:

			if (report.getFullDataDescriptor().getDataType().equals(DataType.GRID)) {

			    toAdd = "";

			    if (distributionMap.containsKey(onlineId)) {

				break;
			    }
			    String wmsURL;
			    if (viewId.isPresent()) {
				wmsURL = url + "/view/" + viewId.get() + "/wms?";
			    } else {
				wmsURL = url + "/wms?";
			    }

			    String name = mapNames.get(onlineId);

			    if (name.startsWith("VEGETATION_INDEX@") || name.startsWith("NDWI@")) {
				break;
			    }

			    Online onLineAdvance = addWMSInfo(onlineId, wmsURL);

			    String onlineString = onLineAdvance.asString(true);

			    if (onlineString.contains("</gmd:description>")) {
				onlineString = onlineString.replace("</gmd:description>",
					"<gco:CharacterString>" + name + "</gco:CharacterString></gmd:description>");
			    }

			    Online o = dist.getDistributionOnline(onlineId);

			    String baseonline = o.asString(true);

			    toAdd += "<gmd:advancedAccessLink>" + wpsUrl + "/dataset/" + onlineId + "</gmd:advancedAccessLink>";

			    baseonline = baseonline.replace(search, toAdd + search);

			    baseonline = formatOnline(baseonline);

			    singleTransferOptionString2 = singleTransferOptionString2 + baseonline;

			    distributionMap.put(onlineId, onlineString);

			    break;

			} else if (report.getFullDataDescriptor().getDataType().equals(DataType.TIME_SERIES)) {

			    toAdd = "";

			    if (distributionMap.containsKey(onlineId)) {

				break;
			    }

			    String gwisURL = url + "/gwis?request=plot&onlineId=" + onlineId;

			    String name = mapNames.get(onlineId);

			    Online onLineAdvance = addInfo(onlineId, gwisURL, "GWIS", "info");

			    String onlineString = onLineAdvance.asString(true);

			    if (onlineString.contains("</gmd:description>")) {
				onlineString = onlineString.replace("</gmd:description>",
					"<gco:CharacterString>" + name + "</gco:CharacterString></gmd:description>");
			    }

			    Online o = dist.getDistributionOnline(onlineId);

			    String baseonline = o.asString(true);

			    toAdd += "<gmd:advancedAccessLink>" + wpsUrl + "/dataset/" + onlineId + "</gmd:advancedAccessLink>";

			    baseonline = baseonline.replace(search, toAdd + search);

			    baseonline = formatOnline(baseonline);

			    singleTransferOptionString2 = singleTransferOptionString2 + baseonline;

			    distributionMap.put(onlineId, onlineString);

			    break reps;
			} else {

			    toAdd += "<gmd:advancedAccessLink>" + wpsUrl + "/dataset/" + onlineId + "</gmd:advancedAccessLink>";

			    break reps;
			}

		    default:
			break;
		    }

		} catch (UnsupportedEncodingException | JAXBException e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }

	    //
	    // not empty only for GRID data type
	    //
	    if (!distributionMap.isEmpty()) {

		Iterator<TransferOptions> transferOpt = dist.getDistributionTransferOptions();

		Double transferSize = null;

		if (transferOpt.hasNext()) {
		    transferSize = transferOpt.next().getTransferSize();
		}

		TransferOptions to = new TransferOptions();
		if (transferSize != null) {
		    to.setTransferSize(transferSize);
		}

		Format format = dist.getFormat();

		if (!listId.isEmpty()) {
		    for (String id : listId) {
			Online simpleOnline = dist.getDistributionOnline(id);
			to.addOnline(simpleOnline);
		    }
		}

		String singleTransferOptionString = getDistribution(to, format);
		String transferOption = "</gmd:MD_DigitalTransferOptions>";

		singleTransferOptionString = singleTransferOptionString.replace(//
			transferOption, //
			singleTransferOptionString2 + transferOption);

		for (Map.Entry<String, String> map : distributionMap.entrySet()) {
		    String toadd = "" + map.getValue();
		    toadd = formatOnline(toadd);
		    singleTransferOptionString = singleTransferOptionString.replace(//
			    transferOption, //
			    toadd + transferOption);
		}

		try {
		    Element distElement = CustomEntry.createElementFromString(encodeAnd(singleTransferOptionString));
		    gpEntry.addElement(distElement);

		} catch (JDOMException | IOException e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}

	    } else {

		Iterator<Online> distOnlines = dist.getDistributionOnlines();

		while (distOnlines.hasNext()) {

		    Online simpleOnline = distOnlines.next();

		    // china geoss case: endpoint changed
		    String token = "ChinaGEOSS-2018@124.16.184.25";
		    String simpleUrl = simpleOnline.getLinkage();
		    if (simpleUrl != null && simpleUrl.contains(token)) {
			simpleUrl = simpleUrl.replace(token, "ChinaGEOSS@124.16.184.9");
			simpleOnline.setLinkage(simpleUrl);
		    }
		    // Trans-African Hydro-Meteorological Observatory (TAHMO) case: use proxy
		    if (simpleUrl != null && simpleUrl.startsWith(SOS_TAHMO_URL)) {
			try {
			    // check if it is the base endpoint only or getobservation request
			    URL checkURL = new URL(simpleUrl);
			    if (checkURL.getQuery() != null && !checkURL.getQuery().isEmpty()) {
				String replaceUrl = url.endsWith("/") ? url + SOS_TAHMO_PROXY_PATH : url + "/" + SOS_TAHMO_PROXY_PATH;
				simpleUrl = simpleUrl.replace(SOS_TAHMO_URL, replaceUrl);
				simpleOnline.setLinkage(simpleUrl);
			    }

			} catch (MalformedURLException e) {
			    e.printStackTrace();
			}

		    }

		    // TWIGA use case: use proxy (shouuld replace SOS TAHMO)
		    if (simpleUrl != null && simpleUrl.startsWith(SOS_TWIGA_URL)) {
			try {
			    // check if it is the base endpoint only or getobservation request
			    URL checkURL = new URL(simpleUrl);
			    if (checkURL.getQuery() != null && !checkURL.getQuery().isEmpty()) {
				String replaceUrl = url.endsWith("/") ? url + SOS_TAHMO_PROXY_PATH : url + "/" + SOS_TAHMO_PROXY_PATH;
				simpleUrl = simpleUrl.replace(SOS_TWIGA_URL, replaceUrl);
				simpleOnline.setLinkage(simpleUrl);
			    }

			} catch (MalformedURLException e) {
			    e.printStackTrace();
			}

		    }

		    if (simpleOnline.getProtocol() != null) {

			if (simpleOnline.getProtocol().contains("WMS-1.3.0")
				|| simpleOnline.getProtocol().equals(NetProtocols.WMS_1_3_0.getCommonURN())) {

			    simpleOnline.setProtocol(CommonNameSpaceContext.WMS_1_3_0_NS_URI + ":HTTP");
			    simpleOnline.setDescriptionGmxAnchor(AccessType.COMPLEX_ACCESS.getDescriptionAnchor());
			}

			if (simpleOnline.getProtocol().contains("WMS-1.1.0") || simpleOnline.getProtocol().contains("WMS-1.1.1")
				|| simpleOnline.getProtocol().contains("OGC:WMS")
				|| simpleOnline.getProtocol().equals(NetProtocols.WMS_1_1_1.getCommonURN())) {
			    simpleOnline.setProtocol(CommonNameSpaceContext.WMS_1_1_1_NS_URI + ":HTTP");
			    simpleOnline.setDescriptionGmxAnchor(AccessType.COMPLEX_ACCESS.getDescriptionAnchor());
			}
		    }
		}

		boolean isOnlineResourcesLimited = false;
		// GOS4M use-case (UUID-f0becf8d-fc72-4968-8bf3-ed52b53e6a01): limit the number of online resources
		if (source != null) {
		    isOnlineResourcesLimited = source.getUniqueIdentifier().equals("UUID-f0becf8d-fc72-4968-8bf3-ed52b53e6a01") ? true
			    : false;
		}

		String distString = getDistribution(dist, isOnlineResourcesLimited);

		if (!toAdd.isEmpty()) {

		    distString = distString.replace(search, toAdd + search);
		}

		try {

		    // System.out.println(distString);

		    Element distElement = CustomEntry.createElementFromString(encodeAnd(distString));
		    gpEntry.addElement(distElement);

		} catch (JDOMException | IOException e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }
	}

	try {
	    return gpEntry.asString();
	} catch (FeedException e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    GP_ENTRY_AS_STRING_ERROR);
	}
    }

    private void filterResults(Distribution dist) {
	Iterator<TransferOptions> transferOpt = dist.getDistributionTransferOptions();
	// Iterator<Online> onlines = dist.getDistributionOnlines();

	while (transferOpt.hasNext()) {
	    // Online simpleOnline = onlines.next();
	    TransferOptions transfer = transferOpt.next();

	    Iterator<Online> ot = transfer.getOnlines();
	    int i = 0;
	    List<Online> recoveryOnlines = new ArrayList<Online>();
	    while (ot.hasNext()) {
		Online res = ot.next();
		i++;
		if (res.getLinkage() != null
			&& (res.getLinkage().contains("axe.geodab.eu") || res.getLinkage().contains("geodab-gi-axe"))) {
		    continue;
		} else {
		    recoveryOnlines.add(res);
		}
	    }
	    if (!recoveryOnlines.isEmpty() && recoveryOnlines.size() != i) {
		transfer.clearOnlines();
		for (Online o : recoveryOnlines) {
		    transfer.addOnline(o);
		}
	    }

	}
    }

    private String formatOnline(String online) {
	String begin = "<gmd:CI_OnlineResource";
	String end = "</gmd:CI_OnlineResource>";
	online = online.replace(begin, "<gmd:onLine>" + begin);
	online = online.replace(end, end + "</gmd:onLine>");
	return online;
    }

    private Online addWMSInfo(String onlineID, String link) {
	return addInfo(onlineID, link, CommonNameSpaceContext.WMS_1_3_0_NS_URI + ":HTTP", "download");
    }

    private Online addInfo(String onlineID, String link, String protocol, String function) {
	Online wmsonline = new Online();
	wmsonline.setLinkage(link);
	wmsonline.setName(onlineID);
	wmsonline.setProtocol(protocol);
	wmsonline.setDescriptionGmxAnchor(AccessType.COMPLEX_ACCESS.getDescriptionAnchor());
	wmsonline.setFunctionCode(function);
	return wmsonline;
    }

    /**
     * @return
     */
    private Stream<DataIdentification> getDataIdsStream(CoreMetadata coreMetadata) {

	return StreamUtils.iteratorToStream(coreMetadata.getMIMetadata().getDataIdentifications());
    }

    private String getDistribution(Distribution dist, boolean isLimitedOnline) {

	if (dist == null) {
	    return null;
	}

	try {

	    String out = " <gmd:distributionInfo xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" ";
	    out += "xmlns:gmi=\"http://www.isotc211.org/2005/gmi\" ";
	    out += "xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\"  ";
	    out += " xmlns:gml=\"http://www.opengis.net/gml\" ";
	    out += "xmlns:ogc=\"http://www.opengis.net/ogc\"  ";
	    out += "xmlns:ows=\"http://www.opengis.net/ows\" ";
	    out += "xmlns:ns4=\"http://quakeml.org/xmlns/bed/1.2\" ";
	    out += "xmlns:gco=\"http://www.isotc211.org/2005/gco\" ";
	    out += "xmlns:dct=\"http://purl.org/dc/terms/\" ";
	    out += "xmlns:wrs=\"http://www.opengis.net/cat/wrs/1.0\" ";
	    out += "xmlns:dc=\"http://purl.org/dc/elements/1.1/\"  ";
	    out += "xmlns:xlink=\"http://www.w3.org/1999/xlink\"> ";

	    if (isLimitedOnline) {

		List<Online> list = StreamUtils.iteratorToStream(dist.getDistributionOnlines()).collect(Collectors.toList());

		dist.clearDistributionOnlines();

		HashMap<String, List<Online>> protocolToOnlineMap = new HashMap<>();

		final int MAX_ONLINE_PER_PROTOCOL = 5;

		for (Online online : list) {

		    String protocol = online.getProtocol();
		    if (protocol == null || protocol.isEmpty()) {
			protocol = "NO_PROTOCOL";
		    }

		    List<Online> onList = protocolToOnlineMap.get(protocol);

		    if (onList == null) {

			onList = new ArrayList<Online>();
			protocolToOnlineMap.put(protocol, onList);
		    }

		    if (onList.size() < MAX_ONLINE_PER_PROTOCOL) {
			onList.add(online);
		    }
		}

		protocolToOnlineMap.values().forEach(onlineList -> onlineList.forEach(online -> dist.addDistributionOnline(online)));
	    }

	    out += dist.asString(true);

	    out += "</gmd:distributionInfo>";

	    return out;

	} catch (UnsupportedEncodingException | JAXBException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Can't parse distribution info", e);

	}

	return null;
    }

    private String getDistribution(TransferOptions transferOptions, Format format) {

	if (transferOptions == null) {
	    return null;
	}

	try {

	    String out = " <gmd:distributionInfo xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" ";
	    out += "xmlns:gmi=\"http://www.isotc211.org/2005/gmi\" ";
	    out += "xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\"  ";
	    out += " xmlns:gml=\"http://www.opengis.net/gml\" ";
	    out += "xmlns:ogc=\"http://www.opengis.net/ogc\"  ";
	    out += "xmlns:ows=\"http://www.opengis.net/ows\" ";
	    out += "xmlns:ns4=\"http://quakeml.org/xmlns/bed/1.2\" ";
	    out += "xmlns:gco=\"http://www.isotc211.org/2005/gco\" ";
	    out += "xmlns:dct=\"http://purl.org/dc/terms/\" ";
	    out += "xmlns:wrs=\"http://www.opengis.net/cat/wrs/1.0\" ";
	    out += "xmlns:dc=\"http://purl.org/dc/elements/1.1/\"  ";
	    out += "xmlns:xlink=\"http://www.w3.org/1999/xlink\"> ";

	    out += "<gmd:MD_Distribution> ";
	    String formatString = null;
	    if (format != null) {
		formatString = format.asString(true);
	    }
	    if (formatString != null) {
		out += "<gmd:distributionFormat> ";
		out += formatString;
		out += "</gmd:distributionFormat> ";
	    }

	    out += "<gmd:transferOptions> ";

	    if (!transferOptions.getOnlines().hasNext()) {
		out += "<gmd:MD_DigitalTransferOptions></gmd:MD_DigitalTransferOptions> ";
	    } else {
		out += transferOptions.asString(true);
	    }
	    out += "</gmd:transferOptions> ";
	    out += "</gmd:MD_Distribution> ";

	    out += "</gmd:distributionInfo>";

	    return out;

	} catch (UnsupportedEncodingException | JAXBException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Can't parse distribution info", e);

	}

	return null;
    }

    private String checkEmptyString(String value) {

	if (value != null && value.isEmpty()) {

	    return null;
	}

	return value;
    }

    private String encodeAnd(String value) {

	if (value == null) {
	    return null;
	}
	if (value.contains("&amp;")) {
	    value = value.replace("&amp;", "&");
	}
	value = value.replace("&", "&amp;");

	return value;
    }

    private String encodeEntities(String value) {

	if (value == null) {
	    return null;
	}

	value = encodeAnd(value);
	value = value.replace("\"", "&quot;");
	value = value.replace("<", "&lt;");
	value = value.replace(">", "&gt;");

	return value;
    }

    @Override
    public MappingSchema getMappingSchema() {

	return null;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    public static void main(String[] args) throws UnsupportedEncodingException, JAXBException {
	TransferOptions to = new TransferOptions();
	to.setTransferSize(100.0);
	System.out.println(to.asString(true));
    }
}
