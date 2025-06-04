package eu.essi_lab.accessor.savahis;

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

import java.math.BigInteger;
import java.time.Duration;
import java.util.Date;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.VerticalCRS;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class SavaHISMapper extends OriginalIdentifierMapper {

    private boolean nowEndDate = true;
    private long nowEndDateMilliseconds = 1000 * 60 * 60 * 24 * 5; // the last 5 days

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.SAVAHIS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);
	 ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	String metadata = originalMD.getMetadata();
	try {
	    // from WML2
	    XMLDocumentReader reader = new XMLDocumentReader(metadata);

	    String elementCount = reader.evaluateString("count(*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:observationMember/*)");

	    if (elementCount.equals("0")) {
		return null;
	    }

	    String beginPosition = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT
		    + "/*:observationMember/*:OM_Observation/*:phenomenonTime/*:TimePeriod/*:beginPosition");
	    Date beginDate = null;
	    if (beginPosition != null && !beginPosition.equals("")) {
		beginDate = ISO8601DateTimeUtils.parseISO8601ToDate(beginPosition).get();
	    }
	    String endPosition = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT
		    + "/*:observationMember/*:OM_Observation/*:phenomenonTime/*:TimePeriod/*:endPosition");
	    Date endDate = null;
	    if (endPosition != null && !endPosition.equals("")) {
		endDate = ISO8601DateTimeUtils.parseISO8601ToDate(endPosition).get();
	    }
	    String stationCode = reader.evaluateString(
		    "*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:observationMember/*:OM_Observation/*:observedProperty/@MonitoringPoint");
	    String property = reader.evaluateString(
		    "*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:observationMember/*:OM_Observation/*:observedProperty/@ObservedProperty");

	    String stationName = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT
		    + "/*:observationMember/*:OM_Observation/*:featureOfInterest/*:MonitoringPoint/*:name");
	    String stationDescription = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT
		    + "/*:observationMember/*:OM_Observation/*:featureOfInterest/*:MonitoringPoint/*:description");
	    String position = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT
		    + "/*:observationMember/*:OM_Observation/*:featureOfInterest/*:MonitoringPoint/*:shape/*:Point/*:pos");
	    position = position.replace("\n", "");
	    String[] split = position.split(" ");
	    String lon = split[0];
	    String lat = split[1];
	    // cm
	    String units = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT
		    + "/*:observationMember/*:OM_Observation/*:result/*:MeasurementTimeseries/*:defaultPointMetadata/*:DefaultTVPMeasurementMetadata/*:uom/@*:title"); // e.g.

	    // from GML
	    String elevation = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:z");
	    String authority = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:authority");
	    String metadataCurator = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:ins_by");
	    String countryCode = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:country");
	    String riverCode = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:river");
	    String basinCode = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:river_basin");
	    String gaugeZero = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:gauge_zero");
	    String riverKilometer = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:river_kilometer");
	    String catchmentArea = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:catchment_area");
	    String bank = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:bank");
	    String classification = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:classification");
	    String timestep = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:timestep");
	    String monitoringType = reader.evaluateString("*:" + SavaHISConnector.SAVA_HIS_ROOT + "/*:featureMember/*/*:monitoring_type");

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    String platformIdentifier = stationCode;

	    SavaHISParameter parameter = SavaHISParameter.decode(property);
	    String parameterIdentifier = property;

	    coreMetadata.getMIMetadata().getDataIdentification().setResourceIdentifier(parameterIdentifier);

	    coreMetadata.addDistributionFormat("WaterML 2.0");

	    coreMetadata.getMIMetadata().setLanguage("English");

	    coreMetadata.getMIMetadata().setCharacterSetCode("utf8");

	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    MIPlatform platform = new MIPlatform();

	    platform.setMDIdentifierCode(platformIdentifier);

	    SavaHISCountry country = SavaHISCountry.decode(countryCode);
	    
	    switch (country) {
	    case BOSNIA_HERZEGOVINA:
		extensionHandler.setCountryISO3(Country.BOSNIA_AND_HERZEGOVINA.getISO3());
		break;

	    case CROATIA:
		extensionHandler.setCountryISO3(Country.CROATIA.getISO3());
		break;

	    case MONTENEGRO:
		extensionHandler.setCountryISO3(Country.MONTENEGRO.getISO3());
		break;

	    case SERBIA:
		extensionHandler.setCountryISO3(Country.SERBIA.getISO3());
		break;

	    case SLOVENIA:
		extensionHandler.setCountryISO3(Country.SLOVENIA.getISO3());
		break;

	    case UNKNOWN:
	    default:
		break;
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(country.getName());

	    SavaHISRiver river = SavaHISRiver.decode(riverCode);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(river.getName());

	    SavaHISRiverBasin basin = SavaHISRiverBasin.decode(basinCode);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(basin.getName());

	    platform.setDescription(stationDescription);

	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(stationName);
	    platform.setCitation(platformCitation);

	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    Keywords keyword = new Keywords();
	    keyword.addKeyword(units);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	    try {
		// Attention, geographic SRS could be NAD83 (or Clarke...) instead of WGS84!
		// TODO: use libraries to convert between the two geographical crs
		// based on srs. For the moment, no conversion is made; the error
		// (only on latitudes) is usually however very low e.g. 48.7438798543649 instead of 48.7438798534299
		Double north = Double.parseDouble(lat);
		Double east = Double.parseDouble(lon);
		coreMetadata.getMIMetadata().getDataIdentification().addGeographicBoundingBox(north, east, north, east);
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass())
			.error("Unable to parse site latitude/longitude " + lat + "/" + lon + " " + e.getMessage());
	    }

	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("EPSG:4326");
	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	    try {
		VerticalExtent verticalExtent = new VerticalExtent();
		Double vertical = Double.parseDouble(elevation);
		verticalExtent.setMinimumValue(vertical);
		verticalExtent.setMaximumValue(vertical);
		VerticalCRS verticalCRS = new VerticalCRS();

		verticalExtent.setVerticalCRS(verticalCRS);
		coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to parse site elevation: " + elevation + " " + e.getMessage());
	    }

	    CoverageDescription coverageDescription = new CoverageDescription();
	    // the name is from the CUAHSI concepts controlled vocabulary
	    coverageDescription.setAttributeIdentifier(parameterIdentifier);
	    coverageDescription.setAttributeTitle(parameter.getName());

	    String speciation = "";

	    String attributeDescription = parameterIdentifier + " Units: " + units;

	    coverageDescription.setAttributeDescription(attributeDescription);
	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    // Keywords: TODO check if a better map is possible

	    ResponsibleParty datasetContact = new ResponsibleParty();
	    datasetContact.setOrganisationName(authority);
	    datasetContact.setRoleCode("originator");
	    Contact contactInfo = new Contact();
	    Address address = new Address();
	    /*
	     * commented because these are site properties, not source organization properties!
	     * address.setCountry(site.getProperty(SiteProperty.STATE));
	     * address.setAdministrativeArea(site.getProperty(SiteProperty.COUNTY));
	     */
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

	    String title = stationName + " - " + parameterIdentifier;

	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(title);

	    coreMetadata.getMIMetadata().getDataIdentification().setAbstract(title);

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

		long extent = endDate.getTime() - beginDate.getTime();
		Duration duration = Duration.ofMillis(extent);

		SavaHISFrequency frequency = SavaHISFrequency.decode(property);

		Long valueCount = null;

		if (frequency != null) {
		    valueCount = duration.getSeconds() / frequency.getDuration().getSeconds();
		    time.setResolution("s", frequency.getDuration().getSeconds());
		}
		if (valueCount != null) {
		    time.setDimensionSize(new BigInteger("" + valueCount));
		   
		    extensionHandler.setDataSize(valueCount);
		}
	    }

	    grid.addAxisDimension(time);
	    coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

	    SavaHISIdentifierMangler mangler = new SavaHISIdentifierMangler();

	    mangler.setMonitoringType(monitoringType);

	    mangler.setPlatformIdentifier(platformIdentifier);

	    mangler.setParameterIdentifier(parameterIdentifier);

	    String identifier = mangler.getMangling(); // Old: platformIdentifier + ";" + parameterIdentifier;

	    coreMetadata.addDistributionOnlineResource(identifier, source.getEndpoint(), NetProtocols.SAVAHIS.getCommonURN(), "download");

	    Online downloadOnline = coreMetadata.getOnline();

	    String onlineId = downloadOnline.getIdentifier();
	    if (onlineId == null) {
		downloadOnline.setIdentifier();
	    }
	    downloadOnline.setIdentifier(onlineId);

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return dataset;
    }

}
