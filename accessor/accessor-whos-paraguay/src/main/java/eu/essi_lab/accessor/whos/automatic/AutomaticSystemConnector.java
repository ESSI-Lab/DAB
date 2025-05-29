package eu.essi_lab.accessor.whos.automatic;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalCRS;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
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
public class AutomaticSystemConnector extends HarvestedQueryConnector<AutomaticSystemConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "AutomaticSystemConnector";
    /**
     * 
     */
    private static final String AUTOMATICSYSTEM_CONNECTOR_SOURCE_NOT_FOUND_ERROR = "AUTOMATICSYSTEM_CONNECTOR_SOURCE_NOT_FOUND_ERROR";

    private List<AutomaticSystemStation> stationList = new ArrayList<AutomaticSystemStation>();

    private int partialNumbers;

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.contains("automaticas.meteorologia.gov.py");
    }

    private static String NS = "automaticas.meteorologia.gov.py";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	AutomaticSystemClient client = new AutomaticSystemClient();
	// client.setToken(ConfigurationWrapper.getCredentialsSetting().getDMHToken().orElse(null));

	Optional<GSSource> source = ConfigurationWrapper.getHarvestedSources().stream().filter(s -> this.supports(s)).findFirst();

	if (!source.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to find connector source", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    AUTOMATICSYSTEM_CONNECTOR_SOURCE_NOT_FOUND_ERROR);
	}

	try {

	    String token = request.getResumptionToken();
	    int start = 0;
	    if (token != null) {

		start = Integer.valueOf(token);
	    }
	    if (stationList.isEmpty())
		populateStations(client);

	    if (start < stationList.size()) {

		AutomaticSystemStation station = stationList.get(start);
		GSLoggerFactory.getLogger(getClass()).info("Adding station " + station.getName());
		List<AutomaticSystemVariable> variables = client.getVariables(station.getId());
		int variableCount = 0;
		for (AutomaticSystemVariable variable : variables) {
		    
		    Dataset dataset = new Dataset();
		    dataset.setSource(source.get());

		    String stationId = station.getId().toString();
		    String parameterId = variable.getId().toString();

		    String platformIdentifier = NS + ":" + stationId;
		    String parameterIdentifier = NS + ":" + parameterId;
		    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

		    MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

		    miMetadata.setCharacterSetCode("utf8");

		    miMetadata.addHierarchyLevelScopeCodeListValue("dataset");

		    MIPlatform platform = new MIPlatform();
		    platform.setMDIdentifierCode(platformIdentifier);
		    platform.setDescription(station.getName());
		    Citation citation = new Citation();
		    citation.setTitle(station.getName());
		    platform.setCitation(citation);

		    miMetadata.addMIPlatform(platform);

		    try {
			Double north = station.getLatitude().doubleValue();
			Double east = station.getLongitude().doubleValue();
			if (north > 90 || north < -90) {
			    String warn = "Invalid latitude for station: " + platformIdentifier;
			    GSLoggerFactory.getLogger(getClass()).warn(warn);
			}
			if (east > 180 || east < -180) {
			    String warn = "Invalid longitude for station: " + platformIdentifier;
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

		    BigDecimal vertical = station.getAltitude();
		    if (vertical != null) {
			try {
			    VerticalExtent verticalExtent = new VerticalExtent();
			    verticalExtent.setMinimumValue(vertical.doubleValue());
			    verticalExtent.setMaximumValue(vertical.doubleValue());
			    VerticalCRS verticalCRS = new VerticalCRS();
			    verticalExtent.setVerticalCRS(verticalCRS);
			    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
			} catch (Exception e) {
			    String warn = "Unable to parse site elevation: " + e.getMessage();
			    logger.warn(warn);
			}
		    }

		    CoverageDescription coverageDescription = new CoverageDescription();

		    coverageDescription.setAttributeIdentifier(parameterIdentifier);
		    String attributeTitle = normalize(variable.getVariableName());
		    if (attributeTitle.endsWith(" mínima")) {
			attributeTitle = attributeTitle.replace(" mínima", "");
			InterpolationType interpolation = InterpolationType.MIN;
			dataset.getExtensionHandler().setTimeInterpolation(interpolation);
		    }
		    if (attributeTitle.endsWith(" maxima")) {
			attributeTitle = attributeTitle.replace(" maxima", "");
			InterpolationType interpolation = InterpolationType.MAX;
			dataset.getExtensionHandler().setTimeInterpolation(interpolation);
		    }
		    coverageDescription.setAttributeTitle(attributeTitle);
		    coverageDescription.setAttributeDescription(variable.getVariableName());

		    // dataset.getExtensionHandler().setTimeUnits(variable.getAggregationPeriodUnits());
		    // dataset.getExtensionHandler().setTimeUnitsAbbreviation(variable.getAggregationPeriodUnits());
		    // dataset.getExtensionHandler().setTimeSupport(variable.getAggregationPeriod().toString());
		    // dataset.getExtensionHandler().setAttributeMissingValue(MISSING_VALUE);
		    String unitName = variable.getUnitOfMeasure();
		    dataset.getExtensionHandler().setAttributeUnits(unitName);

		    String unitAbbreviation = variable.getUnitOfMeasureCode();
		    dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unitAbbreviation);

		    String attributeDescription = variable.getVariableName();

		    coverageDescription.setAttributeDescription(attributeDescription);
		    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

		    TemporalExtent temporalExtent = new TemporalExtent();
		    temporalExtent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(variable.getObservationsStart()));
		    temporalExtent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(variable.getObservationsEnd()));
		    coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

		    AbstractResourceMapper.setIndeterminatePosition(dataset);

		    MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.INLAND_WATERS;

		    coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);

		    ResponsibleParty datasetContact = new ResponsibleParty();
		    datasetContact.setOrganisationName("Dirección de Meteorología e Hidrología (DMH), Paraguay");
		    datasetContact.setRoleCode("publisher");

		    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

		    coreMetadata.getMIMetadata().getDataIdentification()
			    .setCitationTitle(station.getName() + " - " + normalize(variable.getVariableName()));

		    // coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(parameterDescription);

		    coreMetadata.getMIMetadata().getDataIdentification()
			    .setAbstract("Acquisition made at station: " + station.getName() + " Parameter: " + variable.getVariableName());

		    GridSpatialRepresentation grid = new GridSpatialRepresentation();
		    grid.setNumberOfDimensions(1);
		    grid.setCellGeometryCode("point");
		    Dimension time = new Dimension();
		    time.setDimensionNameTypeCode("time");

		    grid.addAxisDimension(time);
		    coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

		    AutomaticSystemIdentifierMangler mangler = new AutomaticSystemIdentifierMangler();

		    // site code network + site code: both needed for access
		    mangler.setPlatformIdentifier(stationId);

		    // variable vocabulary + variable code: both needed for access
		    mangler.setParameterIdentifier(parameterId);

		    String identifier = mangler.getMangling();

		    coreMetadata.addDistributionOnlineResource(identifier, getSourceURL(),
			    CommonNameSpaceContext.AUTOMATIC_SYSTEM_PARAGUAY_URI, "download");

		    String resourceIdentifier = AbstractResourceMapper.generateCode(dataset, identifier);

		    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

		    coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

		    Online downloadOnline = coreMetadata.getOnline();

		    String onlineId = downloadOnline.getIdentifier();
		    if (onlineId == null) {
			downloadOnline.setIdentifier();
		    }

		    downloadOnline.setIdentifier(onlineId);

		    dataset.getExtensionHandler().setCountry(Country.PARAGUAY.getShortName());

		    OriginalMetadata record = new OriginalMetadata();
		    record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
		    record.setMetadata(dataset.asString(true));
		    ret.addRecord(record);
		    variableCount++;

		}
		partialNumbers += variableCount;
		ret.setResumptionToken(String.valueOf(start + 1));

		GSLoggerFactory.getLogger(getClass()).info("Found {} variable for station {} ", variableCount, station.getName());
		if (getSetting().getMaxRecords().isPresent() && start == getSetting().getMaxRecords().get()) {

		    GSLoggerFactory.getLogger(getClass())
			    .info("Max number of stations [" + getSetting().getMaxRecords().get() + "] reached");
		    ret.setResumptionToken(null);

		    return ret;
		}
		// }
		GSLoggerFactory.getLogger(getClass()).info("Stations added");

	    } else {
		ret.setResumptionToken(null);

		GSLoggerFactory.getLogger(getClass()).info("Added all Collection records: " + partialNumbers, stationList.size());
		partialNumbers = 0;
		// return ret;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return ret;

    }

    private void populateStations(AutomaticSystemClient client) throws Exception {
	if (stationList.isEmpty()) {
	    stationList = client.getStations();
	    GSLoggerFactory.getLogger(getClass()).trace("Number of Automatic System stations found: {}", stationList.size());
	}

    }

    private String normalize(String variableName) {
	return variableName.replace("_", " ");
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
    protected AutomaticSystemConnectorSetting initSetting() {

	return new AutomaticSystemConnectorSetting();
    }
}
