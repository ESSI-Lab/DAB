package eu.essi_lab.accessor.dws;

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.accessor.dws.client.DWSClient;
import eu.essi_lab.accessor.dws.client.DWSStation;
import eu.essi_lab.accessor.dws.client.DWSStationList;
import eu.essi_lab.accessor.dws.client.Variable;
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
import eu.essi_lab.lib.utils.GSLoggerFactory;
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
 * @author roncella
 * @author boldrini
 */
public class DWSConnector extends HarvestedQueryConnector<DWSConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "DWSConnector";

    private List<String> regionList = new ArrayList<String>();
    private int partialNumbers;

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.contains(NS);
    }

    private static String NS = "dws.gov.za";

    private static final String CLIENT_ENDPOINT = "https://www.dws.gov.za/Hydrology/Verified/";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	//
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
		    "DWS_CONNECTOR_SOURCE_NOT_FOUND_ERROR");
	}

	try {

	    // ret.setResumptionToken(null);
	    String token = request.getResumptionToken();
	    int start = 0;
	    if (token != null) {

		start = Integer.valueOf(token);
	    }
	    
	    DWSClient client = new DWSClient(CLIENT_ENDPOINT);

	    if(regionList.isEmpty()) {
		populateRegions(client);
	    }
	    
	    
	    if (start < regionList.size()) {
	    
		
		//for (String region : regionsList) {

		InputStream stationsStream = client.getStations(regionList.get(start));

		DWSStationList list = new DWSStationList(stationsStream);

		HashMap<String, DWSStation> stations = list.getStations();
		int stationsCount = stations.size()*4;
		int stationIndex = 1;
		int addedRecords = 0;
		for (String stationCode : stations.keySet()) {
		    DWSStation station = stations.get(stationCode);
		    GSLoggerFactory.getLogger(getClass()).info("Handling station [" + stationIndex + "/" + stationsCount + "] STARTED");

		    // volume - m^3 - million cubic metres interp=total period=1M
		    // flow rate m^3/s - cubic metres/sec interp=average period=1D
		    // water level, stream - m - meter - interp=continuos
		    // discharge, stream - m^3/s - cubic metres/sec - interp=continous

		    for (Variable series : station.getVariables()) {

			// for (InterpolationType interpolation : client.getInterpolations()) {

			Dataset dataset = new Dataset();
			dataset.setSource(source.get());

			String stationId = station.getStationCode();
			String parameterId = series.getAbbreviation();

			InterpolationType interpolation = series.getInterpolation();

			String platformIdentifier = NS + ":" + stationId;
			String parameterIdentifier = NS + ":" + parameterId + ":" + interpolation.name();
			CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

			MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

			miMetadata.setCharacterSetCode("utf8");

			miMetadata.addHierarchyLevelScopeCodeListValue("dataset");

			MIPlatform platform = new MIPlatform();
			platform.setMDIdentifierCode(platformIdentifier);
			platform.setDescription(station.getStationName());
			Citation citation = new Citation();
			citation.setTitle(station.getStationName());
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

			CoverageDescription coverageDescription = new CoverageDescription();

			coverageDescription.setAttributeIdentifier(parameterIdentifier);
			coverageDescription.setAttributeTitle(series.getLabel());
			coverageDescription.setAttributeDescription(series.getLabel());

			dataset.getExtensionHandler().setTimeInterpolation(interpolation);

			coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

			TemporalExtent temporalExtent = new TemporalExtent();

			Date beginDate = station.getBeginDate();
			Date endDate = station.getEndDate();

			// Date tenDaysBefore = new Date(endDate.getTime() - TimeUnit.DAYS.toMillis(10));

			String description = "";

			// if (!interpolation.equals(InterpolationType.CONTINUOUS)) {
			// dataset.getExtensionHandler().setTimeUnits("days");
			// dataset.getExtensionHandler().setTimeUnitsAbbreviation("d");
			// dataset.getExtensionHandler().setTimeSupport("1");
			// dataset.getExtensionHandler().setTimeResolution("1");
			// }

			switch (interpolation) {
			case CONTINUOUS:

			    description = "continuous";

			    break;
			case TOTAL:
			    dataset.getExtensionHandler().setTimeUnits("months");
			    dataset.getExtensionHandler().setTimeUnitsAbbreviation("M");
			    dataset.getExtensionHandler().setTimeSupport("1");
			    dataset.getExtensionHandler().setTimeResolution("1");
			    description = "monthly " + interpolation.getLabel();
			    break;
			case AVERAGE:
			    dataset.getExtensionHandler().setTimeUnits("days");
			    dataset.getExtensionHandler().setTimeUnitsAbbreviation("d");
			    dataset.getExtensionHandler().setTimeSupport("1");
			    dataset.getExtensionHandler().setTimeResolution("1");
			    description = "daily " + interpolation.getLabel();
			    break;
			default:
			    description = "daily " + interpolation.getLabel();

			    break;
			}
			// DWSData data = client.getData(stationId, series.getAbbreviation(), tenDaysBefore, endDate,
			// interpolation);
			//
			// if (data.getSet().isEmpty()) {
			//
			// GSLoggerFactory.getLogger(getClass())
			// .warn("Station [" + station.getStationName() + "], series [" + series + "], " + "dates ["
			// + ISO8601DateTimeUtils.getISO8601DateTime(tenDaysBefore) + "/"
			// + ISO8601DateTimeUtils.getISO8601DateTime(endDate) + "] has no last 10 days data");
			//
			// continue;
			// }

			String units = series.getUnits();// data.getSet().first().getUnits();
			dataset.getExtensionHandler().setAttributeUnits(units);
			dataset.getExtensionHandler().setAttributeUnitsAbbreviation(units);

			temporalExtent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(beginDate));
			temporalExtent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(endDate));
			coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

			AbstractResourceMapper.setIndeterminatePosition(dataset);

			// MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.INLAND_WATERS;

			MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.INLAND_WATERS;
			coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);

			ResponsibleParty datasetContact = new ResponsibleParty();
			datasetContact.setOrganisationName("Department of Water and Sanitation, Republic of South Africa");
			datasetContact.setRoleCode("publisher");

			coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

			coreMetadata.getMIMetadata().getDataIdentification()
				.setCitationTitle(station.getStationName() + " - " + series.getLabel() + " - " + description);

			// coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(parameterDescription);

			coreMetadata.getMIMetadata().getDataIdentification().setAbstract(
				"Acquisition made at station: " + station.getStationName() + " Parameter: " + series.getLabel());

			GridSpatialRepresentation grid = new GridSpatialRepresentation();
			grid.setNumberOfDimensions(1);
			grid.setCellGeometryCode("point");
			Dimension time = new Dimension();
			time.setDimensionNameTypeCode("time");

			grid.addAxisDimension(time);
			coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

			DWSIdentifierMangler mangler = new DWSIdentifierMangler();

			// site code network + site code: both needed for access
			mangler.setPlatformIdentifier(stationId);

			// variable vocabulary + variable code: both needed for access
			mangler.setSeriesIdentifier(series.getName());

			// mangler.setRegionIdentifier(region);

			String identifier = mangler.getMangling();

			coreMetadata.addDistributionOnlineResource(identifier, getSourceURL(), CommonNameSpaceContext.DWS_URI, "download");

			String resourceIdentifier = AbstractResourceMapper.generateCode(dataset, identifier);

			coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

			coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

			Online downloadOnline = coreMetadata.getOnline();

			String onlineId = downloadOnline.getIdentifier();
			if (onlineId == null) {
			    downloadOnline.setIdentifier();
			}

			downloadOnline.setIdentifier(onlineId);

			dataset.getExtensionHandler().setCountry(Country.SOUTH_AFRICA.getShortName());

			//
			//
			//

			OriginalMetadata record = new OriginalMetadata();
			record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
			record.setMetadata(dataset.asString(true));

			ret.addRecord(record);

			addedRecords++;
			
			partialNumbers = addedRecords;

			if (getSetting().getMaxRecords().isPresent() && partialNumbers == getSetting().getMaxRecords().get()) {

			    GSLoggerFactory.getLogger(getClass())
				    .info("Max number of records [" + getSetting().getMaxRecords().get() + "] reached");
			    ret.setResumptionToken(null);
			    
			    return ret;
			}
			// }

			GSLoggerFactory.getLogger(getClass()).info("Handling station [" + stationIndex + "/" + stationsCount + "] ENDED");
			stationIndex++;
		    }
		}
		ret.setResumptionToken(String.valueOf(start + 1));
		if (stationsStream != null)
		    stationsStream.close();
	    } else {
		ret.setResumptionToken(null);

		GSLoggerFactory.getLogger(getClass()).info("Added all Collection records: " + partialNumbers,
			    regionList.size());
		    partialNumbers = 0;
		    //return ret;
	    }

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    "DWS_CONNECTOR_LIST_RECORDS_ERROR", //
		    e);
	}
	//
	return ret;
	// return null;
    }

    private void populateRegions(DWSClient client) {
	if(regionList.isEmpty()) {
	    regionList = client.getRegions();
	    GSLoggerFactory.getLogger(getClass()).trace("Number of South Africa regions found: {}", regionList.size());
	}
	
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
    protected DWSConnectorSetting initSetting() {

	return new DWSConnectorSetting();
    }
}
