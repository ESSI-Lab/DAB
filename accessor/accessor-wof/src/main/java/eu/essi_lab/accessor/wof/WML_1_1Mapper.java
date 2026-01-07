package eu.essi_lab.accessor.wof;

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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.wof.client.datamodel.Site;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo.SiteProperty;
import eu.essi_lab.accessor.wof.client.datamodel.SitesResponseDocument;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeries;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.accessor.wof.utils.WOFIdentifierMangler;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MDResolution;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.VerticalCRS;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * Mapper from WaterML 1.1. Supported root elements are sitesResponse and timeSeriesResponse.
 * The mapper expects to find a single series, which will be mapped as a dataset.
 * SiteInfo or sourceInfo metadata is also expected to be present in the correct locations.
 *
 * @author boldrini
 */
public class WML_1_1Mapper extends OriginalIdentifierMapper {

    public static final String CUAHSI_DATA_TYPE_CV = "CUAHSI Data Type CV";
    public static final String CUAHSI_VALUE_TYPE_CV = "CUAHSI Value Type CV";
    public static final String CUAHSI_QUALITY_CONTROL_LEVELS_CV = "CUAHSI Quality Control Levels CV";
    private static final String WML_1_1_MAPPER_ERROR = "WML_1_1_MAPPER_ERROR";

    private boolean nowEndDate = true;

    /**
     * Sets it to true to add the indeterminate time position now in case the end position is within the last week
     * 
     * @param nowEndDate
     */
    public void setNowEndDate(boolean nowEndDate) {
	this.nowEndDate = nowEndDate;
    }

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());
    private SimpleDateFormat iso8601WMLFormat;

    public WML_1_1Mapper() {
	this.iso8601WMLFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601WMLFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(dataset, originalMD);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.WML1_NS_URI;
    }

    private void mapMetadata(Dataset dataset, OriginalMetadata originalMD) throws GSException {
	String originalMetadata = originalMD.getMetadata();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	XMLDocumentReader reader;
	try {
	    reader = new XMLDocumentReader(originalMetadata);
	} catch (SAXException | IOException e1) {
	    ErrorInfo info = new ErrorInfo();
	    info.setCaller(this.getClass());
	    info.setErrorId(WML_1_1_MAPPER_ERROR);
	    info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    info.setSeverity(ErrorInfo.SEVERITY_ERROR);
	    info.setErrorDescription("Unable to parse WML 1.1 (not a valid XML document): " + originalMetadata);

	    logger.error(info.getErrorDescription());

	    throw GSException.createException(info);
	}
	SitesResponseDocument srd = null;
	TimeSeriesResponseDocument tsrd = null;
	try {
	    String localName = reader.evaluateString("local-name(*[1])");
	    switch (localName) {
	    case "sitesResponse":
	    case "site":
		srd = new SitesResponseDocument(reader.getDocument());

		break;
	    case "TimeSeriesResponse":
	    case "timeSeriesResponse":
	    case "timeSeries":
		tsrd = new TimeSeriesResponseDocument(reader.getDocument());
		tsrd.fixTimes();
		break;
	    default:
		ErrorInfo info = new ErrorInfo();
		info.setCaller(this.getClass());
		info.setErrorId(WML_1_1_MAPPER_ERROR);
		info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
		info.setSeverity(ErrorInfo.SEVERITY_ERROR);
		info.setErrorDescription(
			"Unable to parse WML 1.1 (missing sitesResponse or TimeSeriesResponse root elements): " + originalMetadata);
		logger.error(info.getErrorDescription());
		throw GSException.createException(info);
	    }
	} catch (XPathExpressionException e1) {
	    e1.printStackTrace();
	}

	SiteInfo siteInfo = null;
	TimeSeries series = null;
	String hisServerEndpoint = "";

	if (srd != null) {

	    List<Site> sites = srd.getSites();

	    if (sites.isEmpty()) {
		ErrorInfo info = new ErrorInfo();
		info.setCaller(this.getClass());
		info.setErrorId(WML_1_1_MAPPER_ERROR);
		info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
		info.setSeverity(ErrorInfo.SEVERITY_ERROR);
		info.setErrorDescription("Unable to parse WML 1.1 (missing site): " + originalMetadata);
		logger.error(info.getErrorDescription());
		throw GSException.createException(info);
	    }

	    Site site = sites.get(0);

	    hisServerEndpoint = site.getSeriesCatalogWSDL();

	    siteInfo = site.getSitesInfo();

	    checkSiteInfo(siteInfo, originalMetadata);

	    List<TimeSeries> seriess = site.getSeries();

	    checkSeries(seriess, originalMetadata);

	    series = seriess.get(0);

	} else if (tsrd != null) {

	    List<TimeSeries> seriess = tsrd.getTimeSeries();

	    checkSeries(seriess, originalMetadata);

	    series = seriess.get(0);

	    siteInfo = series.getSiteInfo();

	    checkSiteInfo(siteInfo, originalMetadata);

	} else {
	    ErrorInfo info = new ErrorInfo();
	    info.setErrorDescription("This should not happen. Mapper problem with: " + originalMetadata);
	    info.setCaller(this.getClass());
	    info.setErrorId(WML_1_1_MAPPER_ERROR);
	    info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    info.setSeverity(ErrorInfo.SEVERITY_ERROR);
	    logger.error(info.getErrorDescription());
	    throw GSException.createException(info);
	}

	GSSource source = dataset.getSource();
	String platformPrefix = "";
	String attributePrefix = "";
	String onlineGetSite = "";
	if (source != null) {
	    if (source.getEndpoint().contains("alerta.ina.gob.ar")) {
		platformPrefix = "argentina-ina:";
		attributePrefix = "argentina-ina:";
		if (!hisServerEndpoint.isEmpty()) {
		    onlineGetSite = hisServerEndpoint.endsWith("/") ? hisServerEndpoint + "GetSiteInfo?site=" + siteInfo.getSiteCode()
			    : hisServerEndpoint + "/GetSiteInfo?site=" + siteInfo.getSiteCode();
		}
		if (siteInfo.getDataPolicy() != null) {
		    LegalConstraints rc = new LegalConstraints();
		    rc.addUseLimitation(siteInfo.getDataPolicy());
		    coreMetadata.getDataIdentification().addLegalConstraints(rc);
		}
	    } else {
		platformPrefix = "cuahsi:";
		attributePrefix = "cuahsi:";
	    }
	}

	String platformIdentifier = platformPrefix + siteInfo.getSiteCodeNetwork() + ":" + siteInfo.getSiteCode();

	String parameterIdentifier = platformPrefix + series.getVariableVocabulary() + ":" + series.getVariableCode();

	coreMetadata.addDistributionFormat("WaterML 1.1");

	coreMetadata.getMIMetadata().setLanguage("English");

	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(platformIdentifier);

	String geographicLocation = "";
	String siteDescription = siteInfo.getSiteName();
	if (siteInfo.getProperty(SiteProperty.SITE_TYPE).length() > 0) {
	    siteDescription += " ; " + siteInfo.getProperty(SiteProperty.SITE_TYPE);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(siteInfo.getProperty(SiteProperty.SITE_TYPE));
	}
	if (siteInfo.getSiteComments().length() > 0) {
	    siteDescription += " ; " + siteInfo.getSiteComments();
	}
	if (siteInfo.getProperty(SiteProperty.COUNTY).length() > 0) {
	    geographicLocation += siteInfo.getProperty(SiteProperty.COUNTY);
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("place");
	    keyword.addKeyword(siteInfo.getProperty(SiteProperty.COUNTY));
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}
	if (siteInfo.getProperty(SiteProperty.STATE).length() > 0) {
	    geographicLocation += " ; " + siteInfo.getProperty(SiteProperty.STATE);
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("place");
	    keyword.addKeyword(siteInfo.getProperty(SiteProperty.STATE));
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}
	String countryProperty = getCountryProperty(siteInfo);
	if (countryProperty.length() > 0) {
	    dataset.getExtensionHandler().setCountry(countryProperty);
	}

	platform.setDescription(siteDescription);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(siteInfo.getSiteName());
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	if (series.getGeneralCategory().length() > 0) {
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("discipline");
	    keyword.addKeyword(series.getGeneralCategory());
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	if (series.getSampleMedium().length() > 0) {
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("stratum");
	    keyword.addKeyword(series.getSampleMedium());
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	if (getUnitName(series).length() > 0) {
	    Keywords keyword = new Keywords();
	    keyword.addKeyword(getUnitName(series));
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	if (series.getUnitType().length() > 0) {
	    Keywords keyword = new Keywords();
	    keyword.addKeyword(series.getUnitType());
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	try {
	    // Attention, geographic SRS could be NAD83 (or Clarke...) instead of WGS84!
	    // TODO: use libraries to convert between the two geographical crs
	    // based on srs. For the moment, no conversion is made; the error
	    // (only on latitudes) is usually however very low e.g. 48.7438798543649 instead of 48.7438798534299
	    BigDecimal north = new BigDecimal(siteInfo.getLatitude());
	    BigDecimal east = new BigDecimal(siteInfo.getLongitude());
	    if (north.compareTo(new BigDecimal(90)) > 0 || north.compareTo(new BigDecimal(-90)) < 0) {
		String warn = "Invalid latitude for station: " + platformIdentifier;
		GSLoggerFactory.getLogger(getClass()).warn(warn);
	    }
	    if (east.compareTo(new BigDecimal(180)) > 0 || east.compareTo(new BigDecimal(-180)) < 0) {
		String warn = "Invalid longitude for station: " + platformIdentifier;
		GSLoggerFactory.getLogger(getClass()).warn(warn);
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addGeographicBoundingBox(geographicLocation, north, east, north, east);
	} catch (Exception e) {
	    logger.error("Unable to parse site latitude/longitude " + siteInfo.getLatitude() + "/" + siteInfo.getLongitude() + " "
		    + e.getMessage());
	}

	ReferenceSystem referenceSystem = createReferenceSystem(siteInfo.getSRS());
	coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	// additional reference system information
	if (siteInfo.getLocalSiteXYProjectionInformation().length() > 0) {
	    ReferenceSystem referenceSystem2 = createReferenceSystem(siteInfo.getLocalSiteXYProjectionInformation());
	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem2);
	}

	try {
	    VerticalExtent verticalExtent = new VerticalExtent();
	    Double vertical = Double.parseDouble(siteInfo.getElevationMetres());
	    verticalExtent.setMinimumValue(vertical);
	    verticalExtent.setMaximumValue(vertical);
	    VerticalCRS verticalCRS = new VerticalCRS();
	    verticalCRS.setId(siteInfo.getVerticalDatum());
	    verticalExtent.setVerticalCRS(verticalCRS);
	    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	} catch (Exception e) {
	    String warn = "Unable to parse site elevation: " + siteInfo.getElevationMetres() + " " + e.getMessage();
	    logger.warn(warn);
	}

	String posAccuracyM = siteInfo.getProperty(SiteProperty.POS_ACCURACY_M);
	if (posAccuracyM.length() > 0) {
	    MDResolution resolution = new MDResolution();
	    try {
		Double value = Double.parseDouble(posAccuracyM);
		resolution.setDistance("m", value);
		coreMetadata.getMIMetadata().getDataIdentification().setSpatialResolution(resolution);
	    } catch (Exception e) {
		String warn = "Unable to parse site position accuracy in metres: " + posAccuracyM + " " + e.getMessage();
		logger.warn(warn);
	    }
	}

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(attributePrefix + series.getVariableVocabulary() + ":" + series.getVariableCode());
	coverageDescription.setAttributeTitle(series.getVariableName());

	InterpolationType interpolation = getInterpolationType(series);
	if (interpolation != null) {
	    dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	}

	Number timeSupport = getTimeScaleTimeSupport(series);
	if (timeSupport != null && !timeSupport.toString().equals("0")) {
	    dataset.getExtensionHandler().setTimeSupport(timeSupport.toString());
	}

	String timeUnits = getTimeScaleUnitName(series);
	dataset.getExtensionHandler().setTimeUnits(timeUnits);

	String timeUnitsAbbreviation = getTimeScaleUnitAbbreviation(series);
	dataset.getExtensionHandler().setTimeUnitsAbbreviation(timeUnitsAbbreviation);

	String missingValue = series.getNoDataValue();
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	String unitName = getUnitName(series);
	dataset.getExtensionHandler().setAttributeUnits(unitName);

	String unitAbbreviation = getUnitAbbreviation(series);
	dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unitAbbreviation);

	String speciation = "";

	String attributeDescription = series.getVariableName() + " Data type: " + series.getDataType() + " Value type: "
		+ series.getValueType() + " Units: " + getUnitName(series) + " Units type: " + series.getUnitType() + " Unit abbreviation: "
		+ getUnitAbbreviation(series) + " No data value: " + series.getNoDataValue() + speciation + " Speciation: "
		+ series.getSpeciation();

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	// Keywords: TODO check if a better map is possible

	if (series.getQualityControlLevelDefinition().length() > 0) {
	    Keywords keyword = new Keywords();
	    keyword.addKeyword(series.getQualityControlLevelDefinition());
	    keyword.setThesaurusNameCitationTitle(CUAHSI_QUALITY_CONTROL_LEVELS_CV);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}
	if (series.getValueType().length() > 0) {
	    Keywords keyword = new Keywords();
	    keyword.addKeyword(series.getValueType());
	    keyword.setThesaurusNameCitationTitle(CUAHSI_VALUE_TYPE_CV);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	if (series.getDataType().length() > 0) {
	    Keywords keyword = new Keywords();
	    keyword.addKeyword(series.getDataType());
	    keyword.setThesaurusNameCitationTitle(CUAHSI_DATA_TYPE_CV);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	DataQuality dataQuality = new DataQuality();
	coreMetadata.getMIMetadata().addDataQuality(dataQuality);

	dataQuality.setLineageStatement(series.getMethodDescription());

	String beginPosition = series.getBeginTimePositionUTC();
	beginPosition = normalizeUTCPosition(beginPosition);
	String endPosition = series.getEndTimePositionUTC();
	endPosition = normalizeUTCPosition(endPosition);

	try {
	    iso8601WMLFormat.parse(beginPosition);
	    iso8601WMLFormat.parse(endPosition);
	    coreMetadata.addTemporalExtent(beginPosition, endPosition);

	    if (nowEndDate) {
		setIndeterminatePosition(dataset);
	    }

	} catch (ParseException e) {
	    String error = "Unable to parse dates: " + beginPosition + " - " + endPosition;
	    logger.error(error);
	}

	String topicCategory = series.getSourceMetadataTopicCategory().toLowerCase();
	if (topicCategory.contains("climatology")) {
	    topicCategory = "climatologymeteorologyatmosphere";
	}
	MDTopicCategoryCodeType topic = null;
	switch (topicCategory) {
	case "farming":
	    topic = MDTopicCategoryCodeType.FARMING;
	    break;
	case "biota":
	    topic = MDTopicCategoryCodeType.BIOTA;
	    break;
	case "boundaries":
	    topic = MDTopicCategoryCodeType.BOUNDARIES;
	    break;
	case "climatologymeteorologyatmosphere":
	    topic = MDTopicCategoryCodeType.CLIMATOLOGY_METEOROLOGY_ATMOSPHERE;
	    break;
	case "economy":
	    topic = MDTopicCategoryCodeType.ECONOMY;
	    break;
	case "elevation":
	    topic = MDTopicCategoryCodeType.ELEVATION;
	    break;
	case "environment":
	    topic = MDTopicCategoryCodeType.ENVIRONMENT;
	    break;
	case "geoscientificinformation":
	    topic = MDTopicCategoryCodeType.GEOSCIENTIFIC_INFORMATION;
	    break;
	case "health":
	    topic = MDTopicCategoryCodeType.HEALTH;
	    break;
	case "imagerybasemapsrarthcover":
	    topic = MDTopicCategoryCodeType.IMAGERY_BASE_MAPS_EARTH_COVER;
	    break;
	case "intelligencemilitary":
	    topic = MDTopicCategoryCodeType.INTELLIGENCE_MILITARY;
	    break;
	case "inlandwaters":
	    topic = MDTopicCategoryCodeType.INLAND_WATERS;
	    break;
	case "location":
	    topic = MDTopicCategoryCodeType.LOCATION;
	    break;
	case "oceans":
	    topic = MDTopicCategoryCodeType.OCEANS;
	    break;
	case "planningcadastre":
	    topic = MDTopicCategoryCodeType.PLANNING_CADASTRE;
	    break;
	case "society":
	    topic = MDTopicCategoryCodeType.SOCIETY;
	    break;
	case "structure":
	    topic = MDTopicCategoryCodeType.STRUCTURE;
	    break;
	case "transportation":
	    topic = MDTopicCategoryCodeType.TRANSPORTATION;
	    break;
	case "utilitiescommunication":
	    topic = MDTopicCategoryCodeType.UTILITIES_COMMUNICATION;
	    break;
	default:
	    if (topicCategory.length() > 0) {
		String error = "Unable to parse topic category: " + series.getSourceMetadataTopicCategory();
		logger.error(error);
	    }
	    break;
	}
	if (topic != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);
	}

	ResponsibleParty datasetContact = getContactInfo(series);
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

	coreMetadata.getMIMetadata().getDataIdentification()
		.setCitationTitle(siteInfo.getSiteName() + " - " + series.getVariableName() + " - " + series.getDataType());

	coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(series.getSourceMetadataTitle());

	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(series.getSourceMetadataAbstract());

	GridSpatialRepresentation grid = new GridSpatialRepresentation();
	grid.setNumberOfDimensions(1);
	grid.setCellGeometryCode("point");
	Dimension time = new Dimension();
	time.setDimensionNameTypeCode("time");
	Long valueCount = series.getValueCount();
	if (valueCount != null) {
	    time.setDimensionSize(new BigInteger("" + valueCount));
	    ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	    extensionHandler.setDataSize(valueCount);
	}

	if (series.isTimeScaleRegular()) {
	    Number timeSpacing = series.getTimeScaleTimeSpacing();
	    if (timeSpacing != null && timeSpacing.doubleValue() > Math.pow(10, -16)) {
		String resolutionUOM = series.getTimeScaleUnitName();
		time.setResolution(resolutionUOM, timeSpacing.doubleValue());
	    }
	}
	grid.addAxisDimension(time);
	coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

	WOFIdentifierMangler mangler = new WOFIdentifierMangler();

	// site code network + site code: both needed for access
	mangler.setPlatformIdentifier(siteInfo.getSiteCodeNetwork() + ":" + siteInfo.getSiteCode());

	// variable vocabulary + variable code: both needed for access
	mangler.setParameterIdentifier(series.getVariableVocabulary() + ":" + series.getVariableCode());

	String qualityCode = series.getQualityControlLevelID();

	// ARPA-ER hacks
	if (hisServerEndpoint.contains("hydrolite.ddns.net") && qualityCode != null && qualityCode.equals("-9999")) {
	    qualityCode = "1";
	}

	mangler.setQualityIdentifier(qualityCode);

	mangler.setSourceIdentifier(series.getSourceId());

	mangler.setMethodIdentifier(series.getMethodId());

	String identifier = mangler.getMangling();

	if (onlineGetSite.isEmpty()) {
	    coreMetadata.addDistributionOnlineResource(identifier, hisServerEndpoint, NetProtocolWrapper.CUAHSI_WATER_ONE_FLOW_1_1.getCommonURN(),
		    "download");
	} else {
	    coreMetadata.addDistributionOnlineResource(identifier, hisServerEndpoint, NetProtocolWrapper.CUAHSI_WATER_ONE_FLOW_1_1.getCommonURN(),
		    "download");
	    coreMetadata.addDistributionOnlineResource("Site Information", onlineGetSite, NetProtocolWrapper.HTTP.getCommonURN(),
		    "info");
	}

	String resourceIdentifier = generateCode(dataset, identifier);

	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	Online downloadOnline = coreMetadata.getOnline();

	String onlineId = downloadOnline.getIdentifier();
	if (onlineId == null) {
	    downloadOnline.setIdentifier();
	}

	downloadOnline.setIdentifier(onlineId);
    }

    public String getCountryProperty(SiteInfo siteInfo) {
	return siteInfo.getProperty(SiteProperty.COUNTRY);
    }

    public ResponsibleParty getContactInfo(TimeSeries series) {
	ResponsibleParty datasetContact = new ResponsibleParty();
	datasetContact.setOrganisationName(getSourceOrganization(series));
	datasetContact.setIndividualName(series.getSourceContactName());
	datasetContact.setPositionName(series.getSourceContactType());
	datasetContact.setRoleCode("pointOfContact");
	Contact contactInfo = new Contact();
	Address address = new Address();
	address.addElectronicMailAddress(series.getSourceContactEmail());
	address.addDeliveryPoint(series.getSourceContactAddress());

	contactInfo.addPhoneVoice(series.getSourceContactPhone());
	contactInfo.setAddress(address);
	Online online = new Online();
	online.setLinkage(series.getSourceLink());
	contactInfo.setOnline(online);
	datasetContact.setContactInfo(contactInfo);
	return datasetContact;
    }

    public String getSourceOrganization(TimeSeries series) {
	return series.getSourceOrganization();
    }

    public String getUnitName(TimeSeries series) {
	return series.getUnitName();
    }

    public String getUnitAbbreviation(TimeSeries series) {
	return series.getUnitAbbreviation();
    }

    public String getTimeScaleUnitAbbreviation(TimeSeries series) {
	return series.getTimeScaleUnitAbbreviation();
    }

    public String getTimeScaleUnitName(TimeSeries series) {
	return series.getTimeScaleUnitName();
    }

    public Number getTimeScaleTimeSupport(TimeSeries series) {
	return series.getTimeScaleTimeSupport();
    }

    public InterpolationType getInterpolationType(TimeSeries series) {
	String dataType = series.getDataType();
	if (dataType != null) {
	    dataType = dataType.toLowerCase();
	    if (dataType.contains("continuous")) {
		return InterpolationType.CONTINUOUS;
	    } else if (dataType.contains("sporadic")) {
		return InterpolationType.DISCONTINUOUS;
	    } else if (dataType.contains("cumulative")) {
		return InterpolationType.TOTAL;
	    } else if (dataType.contains("incremental")) {
		return InterpolationType.INCREMENTAL;
	    } else if (dataType.contains("average")) {
		return InterpolationType.AVERAGE;
	    } else if (dataType.contains("maximum")) {
		return InterpolationType.MAX;
	    } else if (dataType.contains("minimum")) {
		return InterpolationType.MIN;
	    } else if (dataType.contains("categorical")) {
		return InterpolationType.CATEGORICAL;
	    }
	}
	return null;
    }

    private void checkSeries(List<TimeSeries> seriess, String originalMetadata) throws GSException {
	if (seriess.isEmpty()) {
	    ErrorInfo info = new ErrorInfo();
	    info.setErrorDescription("Unable to parse WML 1.1 (missing series): " + originalMetadata);
	    info.setCaller(this.getClass());
	    info.setErrorId(WML_1_1_MAPPER_ERROR);
	    info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    info.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    logger.error(info.getErrorDescription());
	    throw GSException.createException(info);
	}
    }

    private void checkSiteInfo(SiteInfo siteInfo, String originalMetadata) throws GSException {
	if (siteInfo == null) {
	    ErrorInfo info = new ErrorInfo();
	    info.setErrorDescription("Unable to parse WML 1.1 (missing siteInfo): " + originalMetadata);
	    info.setCaller(this.getClass());
	    info.setErrorId(WML_1_1_MAPPER_ERROR);
	    info.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    info.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    logger.error(info.getErrorDescription());
	    throw GSException.createException(info);
	}
    }

    /**
     * Gets ISO 19115 UTC ISO 8601 format from a WML time position
     * 
     * @param timePosition
     * @return
     */
    private String normalizeUTCPosition(String timePosition) {
	try {
	    iso8601WMLFormat.parse(timePosition);
	} catch (ParseException e) {
	    String error = "Time position parsing error" + e.getMessage();
	    logger.error(error);
	}
	if (!timePosition.endsWith("Z")) {
	    timePosition += "Z";
	}
	return timePosition;
    }

    private ReferenceSystem createReferenceSystem(String srs) {
	ReferenceSystem referenceSystem = new ReferenceSystem();
	long count = srs.chars().filter(ch -> ch == ':').count();
	if (count == 1 && srs.length() >= 3) {
	    referenceSystem.setCodeSpace(srs.split(":")[0]);
	    referenceSystem.setCode(srs.split(":")[1]);
	} else {
	    referenceSystem.setCode(srs);
	}
	return referenceSystem;
    }

}
