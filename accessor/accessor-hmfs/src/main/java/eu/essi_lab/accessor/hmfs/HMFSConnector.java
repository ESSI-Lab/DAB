package eu.essi_lab.accessor.hmfs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.ConfiguredGmailClient;
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
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author boldrini
 */
public class HMFSConnector extends HarvestedQueryConnector<HMFSConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "HMFSConnector";
    /**
     * 
     */
    private static final String HMFS_CONNECTOR_SOURCE_NOT_FOUND_ERROR = "HMFS_CONNECTOR_SOURCE_NOT_FOUND_ERROR";

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.contains("alerta.ina");
    }

    private final String DISCHARGE_STATION = "1005802104";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	HMFSClient client = new HMFSClient(getSourceURL());

	Optional<GSSource> source = ConfigurationWrapper.getHarvestedSources().stream().filter(s -> this.supports(s)).findFirst();

	if (getSetting().getMaxRecords().isPresent()) {
	    Integer maxRecords = getSetting().getMaxRecords().get();
	    if (maxRecords > 0) {
		HMFSClient.setMaxStations(maxRecords);
	    }
	}

	if (!source.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to find connector source", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    HMFS_CONNECTOR_SOURCE_NOT_FOUND_ERROR);
	}

	try {

	    String token = request.getResumptionToken();
	    if (token == null) {
		token = "0";
	    }
	    Integer i = Integer.parseInt(token);
	    List<String> identifiers = client.getStationIdentifiers();
	    if (i == identifiers.size() - 1) {
		ret.setResumptionToken(null);
	    } else {
		ret.setResumptionToken("" + (i + 1));
	    }
	    String identifier = identifiers.get(i);
	    // identifier = dischargeStation;
	    // ret.setResumptionToken(null);
	    HMFSStation station = client.getStation(identifier);
	    BigDecimal lat = station.getLatitude();
	    BigDecimal lon = station.getLongitude();
	    String stationCode = "" + station.getId();
	    List<HMFSSeries> series = new ArrayList<HMFSSeries>();

	    client.getSeries();
	    series = client.getSeries(stationCode);

	    GSLoggerFactory.getLogger(getClass()).info("Adding station " + stationCode + " " + i + "/" + identifiers.size());

	    // boolean staticInformation = true;

	    String NS = getSourceURL();
	    for (HMFSSeries serie : series) {

		String seriesCode = "" + serie.getId();
		HMFSProcedure procedure = serie.getProcedure();
		String seriesType = serie.getType();
		// "unidades": {
		// "id": 10,
		// "nombre": "metros cúbicos por segundo",
		// "abrev": "m^3/s",
		// "UnitsID": 36,
		// "UnitsType": "Flow"
		// },
		HMFSUnits units = serie.getUnits();
		// "var": {
		// "id": 4,
		// "var": "Q",
		// "nombre": "Caudal",
		// "abrev": "caudal",
		// "type": "num",
		// "datatype": "Continuous",
		// "valuetype": "Derived Value",
		// "GeneralCategory": "Hydrology",
		// "VariableName": "Discharge",
		// "SampleMedium": "Surface Water",
		// "def_unit_id": "10",
		// "timeSupport": {
		// "years": 0,
		// "months": 0,
		// "days": 0,
		// "hours": 0,
		// "minutes": 0,
		// "seconds": 0,
		// "milliseconds": 0
		// },
		// "def_hora_corte": null
		// },
		HMFSVariable variable = serie.getVariable();
		String variableCode = "" + variable.getId();
		// String forecastDate;
		// if (staticInformation) {
		// forecastDate = getStaticLastForecast(stationCode, seriesCode, variableCode);
		// } else {
		// forecastDate = client.getLastForecast(stationCode, seriesCode, variableCode);
		// }

		// client.getSeries
		//
		// List<HMFSSeriesInformation> infos;
		// if (staticInformation) {
		// infos = getStaticSeriesInformation(stationCode, seriesCode, variableCode, forecastDate);
		// } else {
		// infos = client.getSeriesInformation(stationCode, seriesCode, variableCode, forecastDate);
		// }

		List<HMFSSeriesInformation> infos = client.getSeriesInformation(stationCode, seriesCode, variableCode);

		if (infos == null) {
		    infos = new ArrayList<HMFSSeriesInformation>();
		}

		for (HMFSSeriesInformation info : infos) {

		    String beginDate = info.getBeginDate();
		    String endDate = info.getEndDate();
		    // String count = info.getCount();

		    List<String> qualifiers = info.getQualifiers();
		    for (String qualifier : qualifiers) {

			Dataset dataset = new Dataset();
			dataset.setSource(source.get());

			// String parameterId = seriesCode + ";" + variableCode;
			String platformIdentifier = NS + ":" + stationCode;
			// String parameterIdentifier = NS + ":" + parameterId;
			CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

			MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

			miMetadata.setCharacterSetCode("utf8");

			miMetadata.addHierarchyLevelScopeCodeListValue("dataset");

			MIPlatform platform = new MIPlatform();
			platform.setMDIdentifierCode(platformIdentifier);
			platform.setDescription(stationCode);
			Citation citation = new Citation();
			citation.setTitle(stationCode);
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

			CoverageDescription coverageDescription = new CoverageDescription();

			coverageDescription.setAttributeIdentifier(
				AbstractResourceMapper.generateCode(dataset, "" + variable.getId() + "-" + qualifier));
			coverageDescription.setAttributeTitle(variable.getVariableName() + "-" + qualifier);
			coverageDescription.setAttributeDescription(variable.getVariableName() + "-" + qualifier);
			coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

			InterpolationType interpolation = null;

			switch (variable.getDataType().toLowerCase()) {
			case "continuous":
			    interpolation = InterpolationType.CONTINUOUS;
			    break;
			case "average":
			    interpolation = InterpolationType.AVERAGE;
			    break;
			default:
			    interpolation = InterpolationType.decode(variable.getDataType());
			    GSLoggerFactory.getLogger(getClass()).error("not known interpolation: {}", variable.getDataType());
			    break;
			}

			dataset.getExtensionHandler().setTimeInterpolation(interpolation);

			String timeUnits = null;
			String timeSupport = variable.getTimeSupport();
			if (timeSupport == null || timeSupport.isEmpty() || timeSupport.equals("00:00:00")) {
			    timeSupport = null;
			} else {
			    if (timeSupport.contains(" ")) {
				String[] splits = timeSupport.split(" ");
				timeSupport = splits[0];
				timeUnits = splits[1];
				switch (timeUnits) {
				case "mon":
				    timeUnits = "months";
				    break;
				default:
				    break;
				}
			    }
			}

			dataset.getExtensionHandler().setTimeUnits(timeUnits);
			dataset.getExtensionHandler().setTimeUnitsAbbreviation(timeUnits);
			dataset.getExtensionHandler().setTimeSupport(timeSupport);
			// dataset.getExtensionHandler().setAttributeMissingValue(MISSING_VALUE);
			String unitName = units.getName();
			dataset.getExtensionHandler().setAttributeUnits(unitName);

			String unitAbbreviation = units.getAbbreviation();
			dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unitAbbreviation);

			TemporalExtent temporalExtent = new TemporalExtent();
			temporalExtent.setBeginPosition(beginDate);
			temporalExtent.setEndPosition(endDate);
			coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

			AbstractResourceMapper.setIndeterminatePosition(dataset);

			MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.INLAND_WATERS;

			coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);

			ResponsibleParty datasetContact = new ResponsibleParty();
			datasetContact.setOrganisationName("Hydrologic Research Center (HRC)");
			datasetContact.setRoleCode("originator");

			coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

			String title = "HMFS forecast at station " + stationCode + " - " + variable.getVariableName() + " (qualifier "
				+ qualifier + ")";
			coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(title);

			// coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(parameterDescription);

			coreMetadata.getMIMetadata().getDataIdentification().setAbstract(title + " (lat/lon: " + lat + "/" + lon + ")");

			GridSpatialRepresentation grid = new GridSpatialRepresentation();
			grid.setNumberOfDimensions(1);
			grid.setCellGeometryCode("point");
			Dimension time = new Dimension();
			time.setDimensionNameTypeCode("time");

			grid.addAxisDimension(time);
			coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

			HMFSIdentifierMangler mangler = new HMFSIdentifierMangler();

			mangler.setForecastDate(client.getForecastDate());
			mangler.setQualifier(qualifier);
			mangler.setSeries(seriesCode);
			mangler.setStation(stationCode);
			mangler.setType(seriesType);
			mangler.setVariable(variableCode);

			String hmfsIdentifier = mangler.getMangling();

			coreMetadata.addDistributionOnlineResource(hmfsIdentifier, getSourceURL(), NetProtocols.HMFS.getCommonURN(),
				"download");

			String resourceIdentifier = AbstractResourceMapper.generateCode(dataset, hmfsIdentifier);

			coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

			coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

			Online downloadOnline = coreMetadata.getOnline();

			String onlineId = downloadOnline.getIdentifier();
			if (onlineId == null) {
			    downloadOnline.setIdentifier();
			}

			downloadOnline.setIdentifier(onlineId);

			// dataset.getExtensionHandler().setCountry(Country.PARAGUAY.getShortName());

			OriginalMetadata record = new OriginalMetadata();
			record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
			record.setMetadata(dataset.asString(true));
			ret.addRecord(record);
			//GSLoggerFactory.getLogger(getClass()).info("Time-Series {} added");
		    }
		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Stations added");

	} catch (Exception e) {
	    e.printStackTrace();
	    String subject = ConfiguredGmailClient.MAIL_REPORT_SUBJECT + ConfiguredGmailClient.MAIL_HARVESTING_SUBJECT
		    + ConfiguredGmailClient.MAIL_ERROR_SUBJECT;
	    ConfiguredGmailClient.sendEmail(subject, "ERROR DURING HMFS HARVESTING: {}", e.getMessage());
	    throw GSException.createException(getClass(), e.getMessage(), e);
	}

	return ret;

    }

    public List<HMFSSeriesInformation> getStaticSeriesInformation(String stationCode, String seriesCode, String variableCode,
	    String forecastDate) {
	Date date = ISO8601DateTimeUtils.parseISO8601ToDate(forecastDate).get();
	List<HMFSSeriesInformation> ret = new ArrayList<HMFSSeriesInformation>();

	switch (variableCode) {
	case "90":
	    ret.add(new HMFSSeriesInformation("{\"series_id\": " + seriesCode + ",\"series_table\": \"series_areal\",\"estacion_id\": "
		    + stationCode + "," + "\"var_id\": " + variableCode + ",\"qualifier\": \"main\",\"begin_date\": \"" + forecastDate
		    + "\",\n" + "\"end_date\": \"" + add(date, "P91D") + "\"}"));
	    break;
	case "20":
	    for (int q = 1; q < 11; q++) {
		ret.add(new HMFSSeriesInformation("{\"series_id\": " + seriesCode + ",\"series_table\": \"series_areal\",\"estacion_id\": "
			+ stationCode + "," + "\"var_id\": " + variableCode + ",\"qualifier\": \"" + q + "\",\"begin_date\": \""
			+ add(date, "PT6H") + "\",\n" + "\"end_date\": \"" + add(date, "P120D") + "\"}"));
	    }
	    break;
	case "4":
	    if (stationCode.equals(DISCHARGE_STATION)) {
		for (int q = 1; q < 11; q++) {
		    ret.add(new HMFSSeriesInformation("{\"series_id\": " + seriesCode + ",\"series_table\": \"series\",\"estacion_id\": "
			    + stationCode + "," + "\"var_id\": " + variableCode + ",\"qualifier\": \"" + q + "\",\"begin_date\": \""
			    + add(date, "PT1H") + "\",\n" + "\"end_date\": \"" + add(date, "P120D") + "\"}"));
		}
	    }
	    break;
	default:
	    break;
	}

	return ret;
    }

    private String add(Date date, String duration) {
	Date newDate = ISO8601DateTimeUtils.addDuration(date, ISO8601DateTimeUtils.getDuration(duration));
	return ISO8601DateTimeUtils.getISO8601DateTime(newDate);
    }

    private String getStaticLastForecast(String stationCode, String seriesCode, String variableCode) {
	switch (stationCode) {
	case DISCHARGE_STATION:

	    switch (variableCode) {
	    case "4":
	    case "20":
	    case "90":
		return "2023-04-01T00:00:00.000Z";
	    default:
		break;
	    }

	    break;

	default:
	    // ALL THE OTHER STATIONS

	    switch (variableCode) {
	    case "20":
	    case "90":
		return "2023-04-01T00:00:00.000Z";
	    default:
		break;
	    }

	    break;
	}
	return null;

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
    protected HMFSConnectorSetting initSetting() {

	return new HMFSConnectorSetting();
    }

    public static void main(String[] args) {
	HMFSConnector c = new HMFSConnector();
	Date date = ISO8601DateTimeUtils.parseISO8601ToDate("2023-04-01T00:00:00.000Z").get();
	String newDate = c.add(date, "P91D");
	System.out.println(newDate);
    }
}
