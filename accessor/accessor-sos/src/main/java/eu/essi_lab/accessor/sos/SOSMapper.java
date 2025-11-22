package eu.essi_lab.accessor.sos;

import java.math.BigDecimal;

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

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.accessor.sos.SOSProperties.SOSProperty;
import eu.essi_lab.accessor.sos.grow.SOSGROWConnector;
import eu.essi_lab.accessor.sos.tahmo.SOSTAHMOConnector;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class SOSMapper extends OriginalIdentifierMapper {

    private boolean nowEndDate = true;
    private long nowEndDateMilliseconds = TimeUnit.DAYS.toMillis(20); // the last 20 days

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.SOS_2_0;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	String metadata = originalMD.getMetadata();
	SOSProperties properties = null;

	try {
	    properties = new SOSProperties(metadata);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(getClass(), "SOSMapper_CreteaSOSPropertiesError", e);
	}

	Dataset dataset = new Dataset();

	String beginPosition = properties.getProperty(SOSProperty.TEMP_EXTENT_BEGIN);
	Date beginDate = null;

	if (beginPosition != null && !beginPosition.trim().isEmpty()) {

	    beginDate = ISO8601DateTimeUtils.parseISO8601ToDate(beginPosition).get();

	} else {

	    GSLoggerFactory.getLogger(getClass()).warn("Missing begin position!");

	    return null;
	}

	String endPosition = properties.getProperty(SOSProperty.TEMP_EXTENT_END);
	Date endDate = null;

	if (endPosition != null && !endPosition.trim().isEmpty()) {

	    endDate = ISO8601DateTimeUtils.parseISO8601ToDate(endPosition).get();

	} else {

	    GSLoggerFactory.getLogger(getClass()).warn("Missing end position!");

	    return null;
	}

	String procedureTitle = properties.getProperty(SOSProperty.PROCEDURE_TITLE);
	String procedureHref = properties.getProperty(SOSProperty.PROCEDURE_HREF);

	String parameterIdentifier = properties.getProperty(SOSProperty.OBSERVED_PROPERTY_ID);
	String parameterName = properties.getProperty(SOSProperty.OBSERVED_PROPERTY_NAME);
	if (parameterName == null || parameterName.equals("")) {
	    parameterName = parameterIdentifier;
	}

	String uom = properties.getProperty(SOSProperty.OBSERVED_PROPERTY_UOM_CODE);
	if (uom != null) {
	    dataset.getExtensionHandler().setAttributeUnitsAbbreviation(uom);
	}
	
	String timeInterpolation = properties.getProperty(SOSProperty.PROCEDURE_TIME_INTERPOLATION);
	if (timeInterpolation != null) {
	    dataset.getExtensionHandler().setTimeInterpolation(timeInterpolation);
	}
	
	String timeResolution = properties.getProperty(SOSProperty.PROCEDURE_TIME_RESOLUTION);
	if (timeResolution != null) {
	    dataset.getExtensionHandler().setTimeResolutionDuration8601(timeResolution);
	}
	
	String timeAggregation = properties.getProperty(SOSProperty.PROCEDURE_TIME_AGGREGATION);
	if (timeAggregation != null) {
	    dataset.getExtensionHandler().setTimeAggregationDuration8601(timeAggregation);
	}

	String stationCode = properties.getProperty(SOSProperty.FOI_ID);
	String stationName = properties.getProperty(SOSProperty.FOI_NAME);
	if (stationName == null || stationName.equals("")) {
	    stationName = stationCode;
	}

	String elevation = properties.getProperty(SOSProperty.ELEVATION);
	if (elevation != null) {

	    addVerticalExtent(Double.valueOf(elevation), dataset.getHarmonizedMetadata().getCoreMetadata().getDataIdentification());
	}

	// String stationDescription = reader.evaluateString("*:" +
	// SOSConnector.SAVA_HIS_ROOT
	// +
	// "/*:observationMember/*:OM_Observation/*:featureOfInterest/*:MonitoringPoint/*:description");
	// String position = reader.evaluateString("*:" + SOSConnector.SAVA_HIS_ROOT
	// +
	// "/*:observationMember/*:OM_Observation/*:featureOfInterest/*:MonitoringPoint/*:shape/*:Point/*:pos");
	// position = position.replace("\n", "");
	// String[] split = position.split(" ");
	String lon = properties.getProperty(SOSProperty.LONGITUDE);
	String lat = properties.getProperty(SOSProperty.LATITUDE);
	// cm
	// String units = reader.evaluateString("*:" + SOSConnector.SAVA_HIS_ROOT
	// +
	// "/*:observationMember/*:OM_Observation/*:result/*:MeasurementTimeseries/*:defaultPointMetadata/*:DefaultTVPMeasurementMetadata/*:uom/@*:title");
	// // e.g.

	// from GML
	// String elevation = reader.evaluateString("*:" + SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:z");
	// String authority = reader.evaluateString("*:" + SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:authority");
	// String metadataCurator = reader.evaluateString("*:" +
	// SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:ins_by");
	// String countryCode = reader.evaluateString("*:" + SOSConnector.SAVA_HIS_ROOT
	// +
	// "/*:featureMember/*/*:country");
	// String riverCode = reader.evaluateString("*:" + SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:river");
	// String basinCode = reader.evaluateString("*:" + SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:river_basin");
	// String gaugeZero = reader.evaluateString("*:" + SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:gauge_zero");
	// String riverKilometer = reader.evaluateString("*:" +
	// SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:river_kilometer");
	// String catchmentArea = reader.evaluateString("*:" +
	// SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:catchment_area");
	// String bank = reader.evaluateString("*:" + SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:bank");
	// String classification = reader.evaluateString("*:" +
	// SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:classification");
	// String timestep = reader.evaluateString("*:" + SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:timestep");
	// String monitoringType = reader.evaluateString("*:" +
	// SOSConnector.SAVA_HIS_ROOT +
	// "/*:featureMember/*/*:monitoring_type");

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	String platformIdentifier = stationCode;

	coreMetadata.getMIMetadata().getDataIdentification().setResourceIdentifier(parameterIdentifier);

	coreMetadata.addDistributionFormat("WaterML 2.0");

	coreMetadata.getMIMetadata().setLanguage("English");

	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(platformIdentifier);

	// SavaHISCountry country = SavaHISCountry.decode(countryCode);

	String sensorKeywords = properties.getProperty(SOSProperty.SENSOR_Keywords);
	String keywords = properties.getProperty(SOSProperty.SERVICE_PROVIDER_Keywords);

	if (sensorKeywords != null) {
	    String[] split = sensorKeywords.split(";");
	    for (String s : split) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(s);
	    }
	}

	if (keywords != null) {
	    String[] split = keywords.split(";");
	    for (String s : split) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(s);
	    }
	}

	if (properties.getProperty(SOSProperty.SENSOR_Name) != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(properties.getProperty(SOSProperty.SENSOR_Name));
	}

	if (properties.getProperty(SOSProperty.SENSOR_Description) != null) {

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(properties.getProperty(SOSProperty.SENSOR_Description));
	}

	String sensorOwnerOrganization = properties.getProperty(SOSProperty.SENSOR_ContactOwnerOrganization);
	if (sensorOwnerOrganization != null) {

	    ResponsibleParty datasetContact = new ResponsibleParty();

	    datasetContact.setOrganisationName(sensorOwnerOrganization);
	    datasetContact.setRoleCode("originator");

	    Contact contactInfo = new Contact();
	    datasetContact.setContactInfo(contactInfo);

	    Address address = new Address();

	    contactInfo.setAddress(address);

	    if (properties.getProperty(SOSProperty.SENSOR_ContactOwnerPhone) != null) {
		contactInfo.addPhoneVoice(properties.getProperty(SOSProperty.SENSOR_ContactOwnerPhone));
	    }

	    if (properties.getProperty(SOSProperty.SENSOR_ContactOwnerAddressCity) != null) {
		address.setCity(properties.getProperty(SOSProperty.SENSOR_ContactOwnerAddressCity));
	    }

	    if (properties.getProperty(SOSProperty.SENSOR_ContactOwnerAddressCountry) != null) {
		address.setCountry(properties.getProperty(SOSProperty.SENSOR_ContactOwnerAddressCountry));
	    }

	    if (properties.getProperty(SOSProperty.SENSOR_ContactOwnerAddressDeliveryPoint) != null) {
		address.addDeliveryPoint(properties.getProperty(SOSProperty.SENSOR_ContactOwnerAddressDeliveryPoint));
	    }

	    if (properties.getProperty(SOSProperty.SENSOR_ContactOwnerAddressEmail) != null) {
		address.addElectronicMailAddress(properties.getProperty(SOSProperty.SENSOR_ContactOwnerAddressEmail));
	    }

	    if (properties.getProperty(SOSProperty.SENSOR_ContactOwnerHomepage) != null) {
		Online online = new Online();
		online.setLinkage(properties.getProperty(SOSProperty.SENSOR_ContactOwnerHomepage));
		contactInfo.setOnline(online);
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);
	    coreMetadata.getMIMetadata().addContact(datasetContact);
	}

	String serviceProviderOrganization = properties.getProperty(SOSProperty.SERVICE_PROVIDER_NAME);
	if (serviceProviderOrganization != null) {

	    ResponsibleParty datasetContact = new ResponsibleParty();
	    datasetContact.setOrganisationName(serviceProviderOrganization);

	    String role = "distributor";
	    String serviceProviderRole = properties.getProperty(SOSProperty.SERVICE_PROVIDER_ROLE);

	    if (serviceProviderRole != null) {
		role = serviceProviderRole;
	    }

	    datasetContact.setRoleCode(role);

	    Contact contactInfo = new Contact();
	    datasetContact.setContactInfo(contactInfo);

	    Address address = new Address();
	    contactInfo.setAddress(address);

	    if (properties.getProperty(SOSProperty.SERVICE_PROVIDER_Phone) != null) {
		contactInfo.addPhoneVoice(properties.getProperty(SOSProperty.SERVICE_PROVIDER_Phone));
	    }

	    if (properties.getProperty(SOSProperty.SERVICE_PROVIDER_SITE) != null) {
		Online online = new Online();
		online.setLinkage(properties.getProperty(SOSProperty.SERVICE_PROVIDER_SITE));
		contactInfo.setOnline(online);
	    }

	    if (properties.getProperty(SOSProperty.SERVICE_PROVIDER_AddressCity) != null) {
		address.setCity(properties.getProperty(SOSProperty.SERVICE_PROVIDER_AddressCity));
	    }

	    if (properties.getProperty(SOSProperty.SERVICE_PROVIDER_AddressCountry) != null) {
		address.setCountry(properties.getProperty(SOSProperty.SERVICE_PROVIDER_AddressCountry));
	    }

	    if (properties.getProperty(SOSProperty.SERVICE_PROVIDER_AddressDeliveryPoint) != null) {
		address.addDeliveryPoint(properties.getProperty(SOSProperty.SERVICE_PROVIDER_AddressDeliveryPoint));
	    }

	    if (properties.getProperty(SOSProperty.SERVICE_PROVIDER_AddressEmailAddress) != null) {
		address.addElectronicMailAddress(properties.getProperty(SOSProperty.SERVICE_PROVIDER_AddressEmailAddress));
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);
	    coreMetadata.getMIMetadata().addContact(datasetContact);
	}

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);
	platform.setDescription(stationName);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	// Keywords keyword = new Keywords();
	// keyword.addKeyword(units);
	// coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	try {
	    // Attention, geographic SRS could be NAD83 (or Clarke...) instead of WGS84!
	    // TODO: use libraries to convert between the two geographical crs
	    // based on srs. For the moment, no conversion is made; the error
	    // (only on latitudes) is usually however very low e.g. 48.7438798543649 instead
	    // of 48.7438798534299
	    BigDecimal north = new BigDecimal(lat);
	    BigDecimal east = new BigDecimal(lon);
	    coreMetadata.getMIMetadata().getDataIdentification().addGeographicBoundingBox(north, east, north, east);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass())
		    .error("Unable to parse site latitude/longitude " + lat + "/" + lon + " " + e.getMessage());
	}

	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	//
	//
	//
	
	String country = properties.getProperty(SOSProperty.FOI_COUNTRY);
	if (country==null) {
	    // as a second attempt
	    country  = properties.getProperty(SOSProperty.SENSOR_ContactOwnerAddressCountry);
	}	
	dataset.getExtensionHandler().setCountry(country);

	// try {
	// VerticalExtent verticalExtent = new VerticalExtent();
	// Double vertical = Double.parseDouble(elevation);
	// verticalExtent.setMinimumValue(vertical);
	// verticalExtent.setMaximumValue(vertical);
	// VerticalCRS verticalCRS = new VerticalCRS();
	//
	// verticalExtent.setVerticalCRS(verticalCRS);
	// coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	// } catch (Exception e) {
	// GSLoggerFactory.getLogger(getClass()).warn("Unable to parse site elevation: "
	// + elevation + " " +
	// e.getMessage());
	// }

	CoverageDescription coverageDescription = new CoverageDescription();
	// the name is from the CUAHSI concepts controlled vocabulary
	coverageDescription.setAttributeIdentifier(procedureHref);// obs. property id
	coverageDescription.setAttributeTitle(parameterName);
	coverageDescription.setAttributeDescription(procedureTitle);

	// String speciation = "";

	// String attributeDescription = parameterIdentifier + " Units: " + units;
	//
	// coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	// Keywords: TODO check if a better map is possible

	String title = getTitle(properties);

	coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(title);

	String sensorInfo = "";

	if (properties.getProperty(SOSProperty.SENSOR_Name) != null) {
	    sensorInfo += " Sensor name: " + properties.getProperty(SOSProperty.SENSOR_Name);
	}

	if (properties.getProperty(SOSProperty.SENSOR_Description) != null) {
	    sensorInfo += " Sensor description: " + properties.getProperty(SOSProperty.SENSOR_Description);
	}
	if (properties.getProperty(SOSProperty.SENSOR_ContactManufacturerOrganization) != null) {
	    sensorInfo += " Sensor manufacturer: " + properties.getProperty(SOSProperty.SENSOR_ContactManufacturerOrganization);
	}
	if (properties.getProperty(SOSProperty.SENSOR_ContactOwnerOrganization) != null) {
	    sensorInfo += " Sensor owner: " + properties.getProperty(SOSProperty.SENSOR_ContactOwnerOrganization);
	}
	if (properties.getProperty(SOSProperty.SENSOR_Mobile) != null) {
	    sensorInfo += " Sensor mobile : " + properties.getProperty(SOSProperty.SENSOR_Mobile);
	}

	coreMetadata.setAbstract("Sensor observation data from SOS " + getSOSVersion() + " Observed property: " + parameterName
		+ " Station/platform: " + stationName + sensorInfo);

	String image = properties.getProperty(SOSProperty.SENSOR_DocumentationImage);
	if (image != null) {
	    BrowseGraphic browseGraphic = new BrowseGraphic();
	    browseGraphic.setFileName(image);
	    String description = properties.getProperty(SOSProperty.SENSOR_DocumentationImageDescription);
	    browseGraphic.setFileDescription(description);
	    String format = properties.getProperty(SOSProperty.SENSOR_DocumentationImageFormat);
	    browseGraphic.setFileType(format);
	    coreMetadata.getMIMetadata().getDataIdentification().addGraphicOverview(browseGraphic);
	}

	GridSpatialRepresentation grid = new GridSpatialRepresentation();
	grid.setNumberOfDimensions(1);
	grid.setCellGeometryCode("point");
	Dimension time = new Dimension();
	time.setDimensionNameTypeCode("time");

	if (endDate != null && beginDate != null) {
	    boolean endNow = false;
	    coreMetadata.addTemporalExtent(beginPosition, endPosition);
	    Date now = new Date();

	    long gap = now.getTime() - endDate.getTime();
	    if (this.nowEndDate && gap < nowEndDateMilliseconds) {
		endNow = true;
	    }

	    if (endNow) {
		coreMetadata.getTemporalExtent().setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	    }

	    // long extent = endDate.getTime() - beginDate.getTime();
	    // Duration duration = Duration.ofMillis(extent);

	    // SavaHISFrequency frequency = SavaHISFrequency.decode(property);
	    //
	    // Long valueCount = null;
	    //
	    // if (frequency != null) {
	    // valueCount = duration.getSeconds() / frequency.getDuration().getSeconds();
	    // time.setResolution("s", frequency.getDuration().getSeconds());
	    // }
	    // if (valueCount != null) {
	    // time.setDimensionSize(new BigInteger("" + valueCount));
	    // ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	    // extensionHandler.setDataSize(valueCount);
	    // }
	}

	grid.addAxisDimension(time);
	coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

	SOSIdentifierMangler mangler = getMangler(properties);

	String identifier = mangler.getMangling(); //

	String protocol = getProtocol();

	if (properties.getProperty(SOSProperty.DOWNLOAD_PROTOCOL) != null) {
	    protocol = properties.getProperty(SOSProperty.DOWNLOAD_PROTOCOL);
	}

	coreMetadata.addDistributionOnlineResource(identifier, source.getEndpoint(), protocol, "download");

	Online downloadOnline = coreMetadata.getOnline();

	String onlineId = downloadOnline.getIdentifier();
	if (onlineId == null) {
	    downloadOnline.setIdentifier();
	}
	// add direct link for TAHMO and GROW SOS
	if (source.getEndpoint().contains("hnapi.hydronet.com") || source.getEndpoint().contains("hn4s.hydronet.com")
		|| source.getEndpoint().contains("grow-beta-api.hydronet.com")) {
	    SOSConnector connector = null;
	    connector = source.getEndpoint().contains("hnapi.hydronet.com") || source.getEndpoint().contains("hn4s.hydronet.com")
		    ? new SOSTAHMOConnector()
		    : new SOSGROWConnector();
	    Calendar delay = Calendar.getInstance();
	    delay.setTimeInMillis(endDate.getTime());
	    int unroundedMinutes = delay.get(Calendar.MINUTE);
	    int mod = unroundedMinutes % 5;
	    delay.add(Calendar.MINUTE, unroundedMinutes == 0 ? -5 : -mod);
	    delay.set(Calendar.SECOND, 0);
	    delay.add(Calendar.MILLISECOND, 0);
	    Date delayDate = delay.getTime();
	    // delayDate = delay.getTime();
	    delay.add(Calendar.HOUR, -12);
	    Date beforeDate = new Date(delay.getTimeInMillis());

	    SOSRequestBuilder builder = new SOSRequestBuilder(source.getEndpoint(), "2.0.0");

	    String url = builder.createDataRequest(properties.getProperty(SOSProperty.PROCEDURE_IDENTIFIER),
		    properties.getProperty(SOSProperty.FOI_ID), properties.getProperty(SOSProperty.OBSERVED_PROPERTY_ID), beforeDate,
		    delayDate);

	    coreMetadata.addDistributionOnlineResource(
		    "FOI: " + properties.getProperty(SOSProperty.PROCEDURE_IDENTIFIER) + "Property: "
			    + properties.getProperty(SOSProperty.OBSERVED_PROPERTY_ID),

		    builder.removeCredentialsInRequests(url), NetProtocolWrapper.HTTP.getCommonURN(), "download");
	}

	return dataset;
    }

    /**
     * @return
     */
    protected String getSOSVersion() {

	return "v.2.0.0";
    }

    /**
     * @param thing
     * @param keywords
     * @param dataId
     */
    protected void addVerticalExtent(double value, DataIdentification dataId) {

	VerticalExtent verticalExtent = new VerticalExtent();

	verticalExtent.setMinimumValue(value);
	verticalExtent.setMaximumValue(value);

	dataId.addVerticalExtent(verticalExtent);
    }

    /**
     * @return
     */
    protected String getProtocol() {

	return NetProtocolWrapper.SOS_2_0_0.getCommonURN();
    }

    /**
     * @param properties
     * @return
     */
    protected String getTitle(SOSProperties properties) {

	String stationName = properties.getProperty(SOSProperty.FOI_NAME);
	String procedureTitle = properties.getProperty(SOSProperty.PROCEDURE_TITLE);
	String observedProperty = properties.getProperty(SOSProperty.OBSERVED_PROPERTY_NAME);

	return observedProperty + " at " + stationName + ": " + procedureTitle;
    }

    /**
     * @param properties
     * @return
     */
    protected SOSIdentifierMangler getMangler(SOSProperties properties) {

	SOSIdentifierMangler mangler = new SOSIdentifierMangler();

	mangler.setFeature(properties.getProperty(SOSProperty.FOI_ID));

	mangler.setProcedure(properties.getProperty(SOSProperty.PROCEDURE_IDENTIFIER));

	mangler.setObservedProperty(properties.getProperty(SOSProperty.OBSERVED_PROPERTY_ID));

	return mangler;
    }
}
