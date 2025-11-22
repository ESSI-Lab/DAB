package eu.essi_lab.accessor.dinaguaws;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.accessor.dinaguaws.client.DinaguaClient;
import eu.essi_lab.accessor.dinaguaws.client.DinaguaStation;
import eu.essi_lab.accessor.dinaguaws.client.JSONDinaguaClient;
import eu.essi_lab.accessor.dinaguaws.client.Variable;
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
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author boldrini
 */
public class DinaguaStatusConnector extends HarvestedQueryConnector<DinaguaStatusConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "DinaguaStatusConnector";
    /**
     * 
     */
    private static final String DINAGUA_CONNECTOR_SOURCE_NOT_FOUND_ERROR = "DINAGUA_CONNECTOR_SOURCE_NOT_FOUND_ERROR";

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.contains("ambiente.gub.uy");
    }

    private static String NS = "ambiente.gub.uy";

    private static final String CLIENT_ENDPOINT = "https://www.ambiente.gub.uy/dinaguaws";
    private static final String DINAGUA_CONNECTOR_LIST_RECORDS_ERROR = "DINAGUA_CONNECTOR_LIST_RECORDS_ERROR";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	ret.setResumptionToken(null);

	Optional<GSSource> source = ConfigurationWrapper.//
		getHarvestedSources().//
		stream().//
		filter(s -> this.supports(s)).//
		findFirst();

	if (!source.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to find connector source", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    DINAGUA_CONNECTOR_SOURCE_NOT_FOUND_ERROR);
	}

	try {
	    DinaguaClient client = new JSONDinaguaClient(CLIENT_ENDPOINT);

	    Set<DinaguaStation> stations = client.getStatusStations();

	    int stationsCount = stations.size();
	    int stationIndex = 1;
	    int addedRecords = 0;

	    for (DinaguaStation station : stations) {

		GSLoggerFactory.getLogger(getClass()).info("Handling station [" + stationIndex + "/" + stationsCount + "] STARTED");

		for (Variable series : station.getVariables()) {

		    Dataset dataset = new Dataset();
		    dataset.setSource(source.get());

		    String stationId = station.getId();
		    String parameterId = series.getAbbreviation();

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
			BigDecimal north = station.getLatitude();
			BigDecimal east = station.getLongitude();
			if (north.compareTo(new BigDecimal("90")) > 1 || north.compareTo(new BigDecimal("-90")) < 1) {
			    String warn = "Invalid latitude for station: " + platformIdentifier;
			    GSLoggerFactory.getLogger(getClass()).warn(warn);
			}
			if (east.compareTo(new BigDecimal("180")) > 1 || east.compareTo(new BigDecimal("-180")) < 1) {
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

		    CoverageDescription coverageDescription = new CoverageDescription();

		    coverageDescription.setAttributeIdentifier(parameterIdentifier);
		    coverageDescription.setAttributeTitle(series.getLabel());
		    coverageDescription.setAttributeDescription(series.getLabel() + " (" + series.getAbbreviation() + ")");

		    dataset.getExtensionHandler().setTimeResolutionDuration8601("P1M");
		    dataset.getExtensionHandler().setTimeAggregationDuration8601("P1M");

		    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

		    TemporalExtent temporalExtent = new TemporalExtent();

		    Date beginDate = station.getBeginDate();
		    Date endDate = station.getEndDate();

		    temporalExtent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(beginDate));
		    temporalExtent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(endDate));
		    coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

		    AbstractResourceMapper.setIndeterminatePosition(dataset);

		    MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.INLAND_WATERS;

		    coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);

		    ResponsibleParty datasetContact = new ResponsibleParty();
		    datasetContact.setOrganisationName("Dirección Nacional de Aguas (DINAGUA), Uruguay");
		    datasetContact.setRoleCode("publisher");

		    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

		    coreMetadata.getMIMetadata().getDataIdentification()
			    .setCitationTitle(station.getName() + " - status product " + series.getLabel());

		    // coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(parameterDescription);

		    coreMetadata.getMIMetadata().getDataIdentification().setAbstract(
			    "Acquisition made at station: " + station.getName() + " Parameter: status product " + series.getLabel());

		    GridSpatialRepresentation grid = new GridSpatialRepresentation();
		    grid.setNumberOfDimensions(1);
		    grid.setCellGeometryCode("point");
		    Dimension time = new Dimension();
		    time.setDimensionNameTypeCode("time");

		    grid.addAxisDimension(time);
		    coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

		    DinaguaStatusIdentifierMangler mangler = new DinaguaStatusIdentifierMangler();

		    // site code network + site code: both needed for access
		    mangler.setStationIdentifier(stationId);

		    // variable vocabulary + variable code: both needed for access
		    mangler.setTemporalidadIdentifier(series.getAbbreviation());

		    String identifier = mangler.getMangling();

		    coreMetadata.addDistributionOnlineResource(identifier, getSourceURL(), NetProtocolWrapper.DINAGUASTATUSWS.getCommonURN(),
			    "download");

		    String resourceIdentifier = AbstractResourceMapper.generateCode(dataset, identifier);

		    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

		    coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

		    Online downloadOnline = coreMetadata.getOnline();

		    String onlineId = downloadOnline.getIdentifier();
		    if (onlineId == null) {
			downloadOnline.setIdentifier();
		    }

		    downloadOnline.setIdentifier(onlineId);

		    dataset.getExtensionHandler().setCountry(station.getCountry());

		    //
		    //
		    //

		    OriginalMetadata record = new OriginalMetadata();
		    record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
		    record.setMetadata(dataset.asString(true));

		    ret.addRecord(record);

		    addedRecords++;

		    if (getSetting().getMaxRecords().isPresent() && addedRecords == getSetting().getMaxRecords().get()) {

			GSLoggerFactory.getLogger(getClass())
				.info("Max number of records [" + getSetting().getMaxRecords().get() + "] reached");
			return ret;
		    }

		    GSLoggerFactory.getLogger(getClass()).info("Handling station [" + stationIndex + "/" + stationsCount + "] ENDED");
		    stationIndex++;
		}
	    }

	} catch (GSException gsex) {

	    throw gsex;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    DINAGUA_CONNECTOR_LIST_RECORDS_ERROR, //
		    e);
	}

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
    protected DinaguaStatusConnectorSetting initSetting() {

	return new DinaguaStatusConnectorSetting();
    }
}
