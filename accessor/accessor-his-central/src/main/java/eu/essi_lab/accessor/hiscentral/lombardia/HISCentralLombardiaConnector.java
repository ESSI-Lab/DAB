package eu.essi_lab.accessor.hiscentral.lombardia;

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
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_FUNZIONE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_OPERATORE;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaClient.ID_PERIODO;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocols;
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
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.model.resource.data.EPSGCRS;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author boldrini
 */
public class HISCentralLombardiaConnector extends HarvestedQueryConnector<HISCentralLombardiaConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "HISCentralLombardiaConnector";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public HISCentralLombardiaConnector() {

    }

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.contains("lombardia");
    }

    private static String NS = "arpalombardia.it";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	String token = request.getResumptionToken();

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

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
		    "connector source not found");
	}

	try {

	    HISCentralLombardiaClient client = new HISCentralLombardiaClient(new URL(getSourceURL()));

	    List<String> stationsIdentifiers = client.getStationIdentifiers();

	    int stationsCount = stationsIdentifiers.size();
	    int stationIndex;
	    int addedRecords = 0;

	    if (token == null) {
		stationIndex = 0;
	    } else {
		stationIndex = Integer.parseInt(token);
	    }

	    if (stationIndex < (stationsCount - 1)) {
		ret.setResumptionToken("" + (stationIndex + 1));
	    } else {
		ret.setResumptionToken(null);
	    }

	    String stationId = stationsIdentifiers.get(stationIndex);

	    GSLoggerFactory.getLogger(getClass()).info("Handling station [" + stationIndex + "/" + stationsCount + "] STARTED");

	    Stazione station = client.getStazione(stationId);
	    Comune comune = station.getComune();
	    String statoStazione = station.getStato();
	    String tipoStazione = station.getTipoStazione();
	    String indirizzo = station.getIndirizzo();
	    String nomeStazione = station.getNome();
	    BigDecimal quotaStazione = station.getQuota();
	    BigDecimal utm32Est = station.getUtm32TEst();
	    BigDecimal utm32Nord = station.getUtm32TNord();

	    if (utm32Est == null) {
		return ret;
	    }

	    List<Sensore> sensors = client.elencoSensori(stationId);

	    for (Sensore sensor : sensors) {

		String sensorId = sensor.getId();
		String nomeSensore = sensor.getNome();
		String stato = sensor.getStato();
		String tipo = sensor.getTipoSensore();
		String idTipoSensore = sensor.getIdTipoSensore();
		String unita = sensor.getUnitaMisura();
		String frequenza = sensor.getFrequenza();
		BigDecimal quota = sensor.getQuota();

		ID_FUNZIONE funzione = sensor.getFunzione();
		ID_OPERATORE operatore = sensor.getOperatore();
		ID_PERIODO periodo = sensor.getPeriodo();

		Date from = sensor.getFrom();
		Date to = sensor.getTo();
		Double lat = null;
		Double lon = null;
		if (utm32Est != null && utm32Nord != null) {
		    SimpleEntry<Double, Double> coordinates = new SimpleEntry<>(utm32Est.doubleValue(), utm32Nord.doubleValue());
		    CRS sourceCRS = new EPSGCRS(7791);
		    CRS targetCRS = CRS.EPSG_4326();
		    SimpleEntry<Double, Double> latlon = CRSUtils.translatePoint(coordinates, sourceCRS, targetCRS);
		    lat = latlon.getKey();
		    lon = latlon.getValue();
		    if (lat > 90 || lat < -90) {
			String warn = "Invalid latitude for station: " + stationId;
			GSLoggerFactory.getLogger(getClass()).warn(warn);
		    }
		    if (lon > 180 || lon < -180) {
			String warn = "Invalid longitude for station: " + stationId;
			GSLoggerFactory.getLogger(getClass()).warn(warn);
		    }
		}
		Dataset dataset = new Dataset();
		dataset.setSource(source.get());

		String missingValue = "-999.0";
		dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

		String platformIdentifier = NS + ":" + stationId;
		String parameterIdentifier = NS + ":" + idTipoSensore;
		CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

		MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

		miMetadata.setCharacterSetCode("utf8");

		miMetadata.addHierarchyLevelScopeCodeListValue("dataset");

		MIPlatform platform = new MIPlatform();
		platform.setMDIdentifierCode(platformIdentifier);
		platform.setDescription(statoStazione + " " + tipoStazione);
		Citation citation = new Citation();
		citation.setTitle(nomeStazione);
		platform.setCitation(citation);

		miMetadata.addMIPlatform(platform);
		if (lat != null && lon != null) {
		    coreMetadata.getMIMetadata().getDataIdentification().addGeographicBoundingBox(lat, lon, lat, lon);
		}
		if (quotaStazione != null) {
		    double dv = quotaStazione.doubleValue();
		    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(dv, dv);
		}

		ReferenceSystem referenceSystem = new ReferenceSystem();
		referenceSystem.setCode("EPSG:7791");
		referenceSystem.setCodeSpace("EPSG");
		coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

		CoverageDescription coverageDescription = new CoverageDescription();

		coverageDescription.setAttributeIdentifier(parameterIdentifier);

		coverageDescription.setAttributeTitle(tipo);
		coverageDescription.setAttributeDescription(tipo);

		String frequenzaString = sensor.getFrequenza();
		Integer sensorResolutionInMinutes = null;
		if (frequenzaString != null) {
		    sensorResolutionInMinutes = Integer.parseInt(frequenzaString);
		}

		InterpolationType interpolation = null;
		String operatoreId = "null";
		if (operatore != null) {
		    operatoreId = "" + operatore.getId();
		    switch (operatore) {
		    case ID_1_MEDIA:
			interpolation = InterpolationType.AVERAGE;
			break;
		    case ID_2_MINIMO:
			interpolation = InterpolationType.MIN;
			break;
		    case ID_3_MASSIMO:
			interpolation = InterpolationType.MAX;
			break;
		    case ID_4_CUMULATA:
			interpolation = InterpolationType.TOTAL;
			break;
		    case ID_10_MEDIA_MOBILE_8_ORE:
			interpolation = InterpolationType.AVERAGE;
			break;
		    case ID_12_MASSIMO_MEDI_GIORNALIERO:
			interpolation = InterpolationType.MAX_DAILY_AVERAGES;
			break;
		    case ID_13_MINIMO_MEDI_GIORNALIERO:
			interpolation = InterpolationType.MIN_DAILY_AVERAGES;
			break;
		    default:
			break;
		    }
		    dataset.getExtensionHandler().setTimeInterpolation(interpolation);
		}

		String periodoId = "null";
		if (periodo != null) {
		    periodoId = "" + periodo.getId();
		    switch (periodo) {
		    case ID_5_T1M:
			dataset.getExtensionHandler().setTimeUnits("minutes");
			dataset.getExtensionHandler().setTimeUnitsAbbreviation("min");
			dataset.getExtensionHandler().setTimeResolution("1");
			dataset.getExtensionHandler().setTimeSupport("1");
			break;
		    case ID_10_T5M:
			dataset.getExtensionHandler().setTimeUnits("minutes");
			dataset.getExtensionHandler().setTimeUnitsAbbreviation("min");
			dataset.getExtensionHandler().setTimeResolution("5");
			dataset.getExtensionHandler().setTimeSupport("5");
			break;
		    case ID_1_T10M:
			dataset.getExtensionHandler().setTimeUnits("minutes");
			dataset.getExtensionHandler().setTimeUnitsAbbreviation("min");
			dataset.getExtensionHandler().setTimeResolution("10");
			dataset.getExtensionHandler().setTimeSupport("10");
			break;
		    case ID_2_T30M:
			dataset.getExtensionHandler().setTimeUnits("minutes");
			dataset.getExtensionHandler().setTimeUnitsAbbreviation("min");
			dataset.getExtensionHandler().setTimeResolution("30");
			dataset.getExtensionHandler().setTimeSupport("30");
			break;
		    case ID_3_T60M:
			dataset.getExtensionHandler().setTimeUnits("hour");
			dataset.getExtensionHandler().setTimeUnitsAbbreviation("h");
			dataset.getExtensionHandler().setTimeResolution("1");
			dataset.getExtensionHandler().setTimeSupport("1");
			break;
		    case ID_8_T2H:
			dataset.getExtensionHandler().setTimeUnits("hour");
			dataset.getExtensionHandler().setTimeUnitsAbbreviation("h");
			dataset.getExtensionHandler().setTimeResolution("2");
			dataset.getExtensionHandler().setTimeSupport("2");
			break;
		    case ID_6_T3H:
			dataset.getExtensionHandler().setTimeUnits("hour");
			dataset.getExtensionHandler().setTimeUnitsAbbreviation("h");
			dataset.getExtensionHandler().setTimeResolution("3");
			dataset.getExtensionHandler().setTimeSupport("3");
			break;
		    case ID_9_T4H:
			dataset.getExtensionHandler().setTimeUnits("hour");
			dataset.getExtensionHandler().setTimeUnitsAbbreviation("h");
			dataset.getExtensionHandler().setTimeResolution("4");
			dataset.getExtensionHandler().setTimeSupport("4");
			break;
		    case ID_4_T1D:
			dataset.getExtensionHandler().setTimeUnits("day");
			dataset.getExtensionHandler().setTimeUnitsAbbreviation("d");
			dataset.getExtensionHandler().setTimeResolution("1");
			dataset.getExtensionHandler().setTimeSupport("1");
			break;
		    default:
			break;
		    }
		}

		coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

		TemporalExtent temporalExtent = new TemporalExtent();
		temporalExtent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(from));
		temporalExtent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(to));
		coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);
		AbstractResourceMapper.setIndeterminatePosition(dataset);

		dataset.getExtensionHandler().setAttributeUnits(unita);
		dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unita);

		if (tipoStazione != null) {
		    if (tipoStazione.toLowerCase().contains("meteo")) {
			coreMetadata.getMIMetadata().getDataIdentification()
				.addTopicCategory(MDTopicCategoryCodeType.CLIMATOLOGY_METEOROLOGY_ATMOSPHERE);
		    }
		    if (tipoStazione.toLowerCase().contains("idro")) {
			coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(MDTopicCategoryCodeType.INLAND_WATERS);
		    }
		    if (tipoStazione.toLowerCase().contains("nivo")) {
			coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(MDTopicCategoryCodeType.INLAND_WATERS);
		    }
		}

		ResponsibleParty datasetContact = new ResponsibleParty();
		datasetContact.setOrganisationName("ARPA Lombardia");
		datasetContact.setRoleCode("publisher");

		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

		String title = nomeSensore + " - " + operatore + " - " + periodo.getLabel();
		coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(title);

		coreMetadata.getMIMetadata().getDataIdentification()
			.setAbstract(title + "\nThe dataset leverages a sensor frequency of " + sensorResolutionInMinutes
				+ " minutes.\nThe station address is " + station.getIndirizzo() + "\nStation state is: "
				+ station.getStato() + "\nStatoin type: " + tipoStazione + "\nStation municipality: "
				+ station.getComune().getNome());

		GridSpatialRepresentation grid = new GridSpatialRepresentation();
		grid.setNumberOfDimensions(1);
		grid.setCellGeometryCode("point");
		Dimension time = new Dimension();
		time.setDimensionNameTypeCode("time");
		grid.addAxisDimension(time);
		coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

		HISCentralLombardiaIdentifierMangler mangler = new HISCentralLombardiaIdentifierMangler();

		String funzioneId = "null";
		if (funzione != null) {
		    funzioneId = "" + funzione.getId();
		}

		mangler.setSensorIdentifier(sensor.getId());
		mangler.setFunctionIdentifier(funzioneId);
		mangler.setOperatorIdentifier(operatoreId);
		mangler.setPeriodIdentifier(periodoId);

		String identifier = mangler.getMangling();

		coreMetadata.addDistributionOnlineResource(identifier, getSourceURL(), NetProtocols.ARPA_LOMBARDIA.getCommonURN(),
			"download");

		String resourceIdentifier = AbstractResourceMapper.generateCode(dataset, identifier);

		coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

		coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

		dataset.getExtensionHandler().setCountry(Country.ITALY.getShortName());

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

	    }

	    GSLoggerFactory.getLogger(getClass()).info("Handling station [" + stationIndex + "/" + stationsCount + "] ENDED");
	    stationIndex++;

	} catch (

	GSException gsex) {

	    throw gsex;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    "HIS Central Lombardia connector error", //
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
    protected HISCentralLombardiaConnectorSetting initSetting() {

	return new HISCentralLombardiaConnectorSetting();
    }

    public static void main(String[] args) throws Exception {
	// EAST - NORTH
	SimpleEntry<Double, Double> coordinates = new SimpleEntry<Double, Double>(564213.42, 5130098.33);
	CRS sourceCRS = new EPSGCRS(7791);
	CRS targetCRS = CRS.EPSG_4326();
	SimpleEntry<Double, Double> latlon = CRSUtils.translatePoint(coordinates, sourceCRS, targetCRS);
	System.out.println(latlon.getKey());
	System.out.println(latlon.getValue());
    }

}
