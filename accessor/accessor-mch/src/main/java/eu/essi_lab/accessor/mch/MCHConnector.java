package eu.essi_lab.accessor.mch;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.accessor.mch.datamodel.MCHAvailability;
import eu.essi_lab.accessor.mch.datamodel.MCHCountry;
import eu.essi_lab.accessor.mch.datamodel.MCHStation;
import eu.essi_lab.accessor.mch.datamodel.MCHVariable;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author boldrini
 */
public class MCHConnector extends HarvestedQueryConnector<MCHConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "MCHConnector";

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	if (url.endsWith("/")) {
	    url = url.substring(0, url.length() - 1);
	}
	Downloader d = new Downloader();
	Optional<String> str = d.downloadOptionalString(url);
	if (str.isPresent()) {
	    return str.get().contains("MCH API");
	}
	return false;
    }

    public enum Resolution {
	DAILY("daily"), DETAILED("detailed");

	private String label;

	Resolution(String label) {
	    this.label = label;
	}

	public String getLabel() {
	    return label;
	}

	public static Resolution decode(String label) {
	    for (Resolution res : Resolution.values()) {
		if (res.name().equals(label) || res.getLabel().equals(label)) {
		    return res;
		}
	    }
	    return null;
	}

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	try {
	    MCHClient client = new MCHClient(getSourceURL());
	    List<MCHStation> stations = client.getStations();
	    MCHCountry country = client.getCountry();
	    for (MCHStation station : stations) {
		GSLoggerFactory.getLogger(getClass()).info("Adding station " + station.getStationName());
		List<MCHAvailability> avails = client.getAvailability(station.getStationName());
		for (MCHAvailability avail : avails) {

		    Dataset dataset = new Dataset();

		    String stationId = station.getStationId();
		    String parameterId = avail.getVariable();

		    MCHVariable variable = client.getVariable(parameterId);

		    Resolution resolution = variable.getTypeDDorDE().equals("DD") ? Resolution.DAILY : Resolution.DETAILED;

		    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

		    MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

		    miMetadata.setCharacterSetCode("utf8");

		    miMetadata.addHierarchyLevelScopeCodeListValue("dataset");

		    MIPlatform platform = new MIPlatform();
		    platform.setMDIdentifierCode(stationId);
		    platform.setDescription(station.getStationName2());
		    Citation citation = new Citation();
		    citation.setTitle(station.getStationName());
		    platform.setCitation(citation);

		    miMetadata.addMIPlatform(platform);

		    try {
			Double north = station.getLatitude().doubleValue();
			Double east = station.getLongitude().doubleValue();
			if (north > 90 || north < -90) {
			    String warn = "Invalid latitude for station: " + stationId;
			    GSLoggerFactory.getLogger(getClass()).warn(warn);
			}
			if (east > 180 || east < -180) {
			    String warn = "Invalid longitude for station: " + stationId;
			    GSLoggerFactory.getLogger(getClass()).warn(warn);
			}
			coreMetadata.getMIMetadata().getDataIdentification().addGeographicBoundingBox(north, east, north, east);
		    } catch (Exception e) {
			GSLoggerFactory.getLogger(getClass()).error("Unable to parse site latitude/longitude: " + e.getMessage());
		    }

		    ReferenceSystem referenceSystem = new ReferenceSystem();
		    referenceSystem.setCode("4326");
		    referenceSystem.setCodeSpace("EPSG");
		    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

		    GSLogger logger = GSLoggerFactory.getLogger(getClass());

		    CoverageDescription coverageDescription = new CoverageDescription();

		    coverageDescription.setAttributeIdentifier(parameterId);
		    coverageDescription.setAttributeTitle(variable.getVariable());
		    coverageDescription.setAttributeDescription(variable.getVariableDescription());

		    // InterpolationType interpolation = InterpolationType.TOTAL;
		    // dataset.getExtensionHandler().setTimeInterpolation(interpolation);
		    // dataset.getExtensionHandler().setTimeUnits(variable.getAggregationPeriodUnits());
		    // dataset.getExtensionHandler().setTimeUnitsAbbreviation(variable.getAggregationPeriodUnits());
		    // dataset.getExtensionHandler().setTimeSupport(variable.getAggregationPeriod().toString());
		    // dataset.getExtensionHandler().setAttributeMissingValue(MISSING_VALUE);
		    String unitName = variable.getUnit();
		    dataset.getExtensionHandler().setAttributeUnits(unitName);

		    String unitAbbreviation = variable.getUnit();
		    dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unitAbbreviation);

		    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

		    TemporalExtent temporalExtent = new TemporalExtent();

		    Date beginDate = avail.getStartDate();
		    Date endDate = avail.getEndDate();

		    temporalExtent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(beginDate));
		    temporalExtent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(endDate));
		    coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

		    AbstractResourceMapper.setIndeterminatePosition(dataset);

		    MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.INLAND_WATERS;

		    coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);

		    // ResponsibleParty datasetContact = new ResponsibleParty();
		    // datasetContact.setOrganisationName("Dirección Nacional de Aguas (DINAGUA), Uruguay");
		    // datasetContact.setRoleCode("publisher");

		    // coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

		    String cumulationType = variable.getCumulationType();
		    String interpolationType = null;
		    switch (cumulationType) {
		    case "CUMU":
			interpolationType = InterpolationType.TOTAL.getLabel();
			break;
		    case "MEAN":
			interpolationType = InterpolationType.AVERAGE.getLabel();
			break;
		    default:
			interpolationType = cumulationType;
			break;
		    }

		    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(station.getStationName() + " - "
			    + variable.getVariable() + " - " + resolution.getLabel() + " " + interpolationType);

		    // coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(parameterDescription);

		    coreMetadata.getMIMetadata().getDataIdentification()
			    .setAbstract("Acquisition made at station: " + station.getStationName() + " Parameter: "
				    + variable.getVariable() + " Aggregation: " + resolution.getLabel() + " " + interpolationType);

		    if (interpolationType != null) {
			dataset.getExtensionHandler().setTimeInterpolation(interpolationType);
		    }

		    if (resolution.equals(Resolution.DAILY)) {

			dataset.getExtensionHandler().setTimeSupport("1");
			dataset.getExtensionHandler().setTimeUnits("day");
			dataset.getExtensionHandler().setTimeUnitsAbbreviation("day");

		    }

		    GridSpatialRepresentation grid = new GridSpatialRepresentation();
		    grid.setNumberOfDimensions(1);
		    grid.setCellGeometryCode("point");
		    Dimension time = new Dimension();
		    time.setDimensionNameTypeCode("time");

		    grid.addAxisDimension(time);
		    coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

		    MCHIdentifierMangler mangler = new MCHIdentifierMangler();

		    mangler.setStationId(station.getStationId());

		    mangler.setVariableName(variable.getVariable());

		    mangler.setResolution(resolution.name());

		    String identifier = mangler.getMangling();

		    coreMetadata.addDistributionOnlineResource(identifier, getSourceURL(), NetProtocolWrapper.MCH.getCommonURN(), "download");

		    coreMetadata.getDataIdentification().setResourceIdentifier(identifier);

		    Online downloadOnline = coreMetadata.getOnline();

		    String onlineId = downloadOnline.getIdentifier();
		    if (onlineId == null) {
			downloadOnline.setIdentifier();
		    }

		    downloadOnline.setIdentifier(onlineId);

		    if (country != null && country.getCode() != null) {
			dataset.getExtensionHandler().setCountry(Country.decode(country.getCode()).getShortName());
		    }

		    OriginalMetadata record = new OriginalMetadata();
		    record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
		    record.setMetadata(dataset.asString(true));
		    ret.addRecord(record);

		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Stations added");

	} catch (Exception e) {
	    e.printStackTrace();
	}

	ret.setResumptionToken(null);
	return ret;

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected MCHConnectorSetting initSetting() {

	return new MCHConnectorSetting();
    }
}
