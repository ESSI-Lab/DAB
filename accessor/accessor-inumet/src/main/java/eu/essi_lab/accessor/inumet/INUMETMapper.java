package eu.essi_lab.accessor.inumet;

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

import java.util.Date;

import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.iso.datamodel.classes.VerticalCRS;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

public class INUMETMapper extends AbstractResourceMapper {

    public static final String MISSING_VALUE = "-9999";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.INUMET_URI;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {
	return null;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	String ns = CommonNameSpaceContext.INUMET_URI + ":";

	Logger logger = GSLoggerFactory.getLogger(getClass());

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	String originalMetadata = originalMD.getMetadata();

	JSONObject metadata = new JSONObject(originalMetadata);

	JSONObject station = metadata.getJSONObject("station");

	JSONObject variable = metadata.getJSONObject("variable");

	String parameterId = variable.getString("id");
	String parameterIdString = variable.getString("idStr");
	String parameterName = variable.getString("nombre").trim();
	String parameterAbbreviation = variable.getString("abrevVariab");
	String parameterUnits = variable.getString("unidad");
	String parameterDescription = variable.getString("descripVariab");
	String parameterTipoIngreso = variable.getString("tipoIngreso");
	String parameterNDecim = variable.getString("nDecim");
	String parameterNDigitosAlImprimir = variable.getString("nDigitosAlImprimir");
	String parameterEntera = variable.getString("entera");
	String parameterPeriodicidad = variable.getString("periodicidad");
	String parameterTamanioColumnaCodigo = variable.getString("tamanioColumnaCodigo");
	String parameterPeriodoDeRegistro = variable.getString("periodoDeRegistro");
	String parameterTipoDeMedicion = variable.getString("tipoDeMedicion");
	String parameterGrupoVista = variable.getString("grupoVista");

	String stationId = station.getString("id");
	String stationIdString = station.getString("idStr");
	String stationName = station.getString("nombre");
	String stationIdWMO = station.getString("codigoOMM");
	String stationIdPluviometer = station.getString("codigoPluviometrico");
	String stationidOACI = station.getString("idOACI");
	String stationidAutomatica = station.getString("idAutomatica");
	String stationLatitude = station.getString("latitud");
	String stationLongitude = station.getString("longitud");
	String stationAltitude = station.getString("altitud");
	String stationCotaBarom = station.getString("cotaBarom");
	String stationTipoMet = station.getString("tipoMet");
	String stationTipoPluvio = station.getString("tipoPluvio");
	String stationTipoExterna = station.getString("tipoExterna");
	String stationTipoAutomatica = station.getString("tipoAutomatica");
	String stationTipoAeronautica = station.getString("tipoAeronautica");
	String stationTimeZone = station.getString("husoHorario");
	String stationOrganization = station.getString("gerencia");
	String stationState = station.getString("estado");
	String stationusarDatosEnProductos = station.getString("usarDatosEnProductos");
	String stationWigosLocalId = station.getString("wigosLocalId");

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	String platformIdentifier = ns + "station:" + stationId; // e.g. "1115"

	String parameterIdentifier = ns + parameterId;

	coreMetadata.addDistributionFormat("JSON");

	// coreMetadata.getMIMetadata().setLanguage("Portuguese");

	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(platformIdentifier);

	String geographicLocation = "";
	String siteDescription = stationName;

	platform.setDescription(siteDescription);
	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationType);
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationSituation);

	// Keywords keyword = new Keywords();
	// keyword.setTypeCode("place");
	// keyword.addKeyword(stationDistrict);
	// coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	// Keywords keyword2 = new Keywords();
	// keyword2.setTypeCode("place");
	// keyword2.addKeyword(stationState);
	// coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword2);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	Keywords keyword3 = new Keywords();
	keyword3.addKeyword(parameterUnits);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword3);

	Keywords keyword4 = new Keywords();
	keyword4.addKeyword(parameterDescription);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword3);

	try {
	    Double north = Double.parseDouble(stationLatitude);
	    Double east = Double.parseDouble(stationLongitude);
	    if (north > 90 || north < -90) {
		String warn = "Invalid latitude for station: " + platformIdentifier;
		GSLoggerFactory.getLogger(getClass()).warn(warn);
	    }
	    if (east > 180 || east < -180) {
		String warn = "Invalid longitude for station: " + platformIdentifier;
		GSLoggerFactory.getLogger(getClass()).warn(warn);
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addGeographicBoundingBox(geographicLocation, north, east, north, east);
	} catch (Exception e) {
	    logger.error("Unable to parse site latitude/longitude: " + e.getMessage());
	}

	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("4326");
	referenceSystem.setCodeSpace("EPSG");
	coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	try {
	    VerticalExtent verticalExtent = new VerticalExtent();
	    Double vertical = Double.parseDouble(stationAltitude);
	    verticalExtent.setMinimumValue(vertical);
	    verticalExtent.setMaximumValue(vertical);
	    VerticalCRS verticalCRS = new VerticalCRS();
	    verticalExtent.setVerticalCRS(verticalCRS);
	    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	} catch (Exception e) {
	    String warn = "Unable to parse site elevation: " + e.getMessage();
	    logger.warn(warn);
	}

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(parameterIdentifier);
	if (parameterDescription.contains(" acumulada 24 horas de 10 a 10 UTC")) {
	    parameterName = parameterDescription.replace(" acumulada 24 horas de 10 a 10 UTC", "").trim();
	    InterpolationType interpolation = InterpolationType.TOTAL;
	    dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	    dataset.getExtensionHandler().setTimeUnits("day");
	    dataset.getExtensionHandler().setTimeUnitsAbbreviation("day");
	    dataset.getExtensionHandler().setTimeSupport("1");
	}
	coverageDescription.setAttributeTitle(parameterName);

	coverageDescription.setAttributeDescription(parameterDescription);

	dataset.getExtensionHandler().setAttributeMissingValue(MISSING_VALUE);
	String unitName = parameterUnits;
	dataset.getExtensionHandler().setAttributeUnits(unitName);

	String unitAbbreviation = parameterUnits;
	dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unitAbbreviation);

	String attributeDescription = parameterDescription;

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	// Keywords: TODO check if a better map is possible

	DataQuality dataQuality = new DataQuality();
	coreMetadata.getMIMetadata().addDataQuality(dataQuality);

	dataQuality.setLineageStatement("Type of measurement: " + parameterTipoDeMedicion);

	// String beginPosition = getISO8601Date(stationTimeBegin);
	// String endPosition = getISO8601Date(stationTimeEnd);
	//
	// if (beginPosition == null) {
	// Long threeMonths = 1000 * 60 * 60 * 24 * 100l;
	// Date threeMonthsAgo = new Date(System.currentTimeMillis() - threeMonths);
	// beginPosition = ISO8601DateTimeUtils.getISO8601DateTime(threeMonthsAgo);
	// }
	//

	Date now = new Date();
	// long fiveDays = 1000 * 60 * 60 * 24 * 5l;
	// String beginDate = ISO8601DateTimeUtils.getISO8601DateTime(new Date(now.getTime() - fiveDays));
	String endDate = ISO8601DateTimeUtils.getISO8601DateTime(now);
	// coreMetadata.addTemporalExtent(beginDate, endDate);

	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeforeNowBeginPosition(FrameValue.P5D);
	temporalExtent.setEndPosition(endDate);
	coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

	setIndeterminatePosition(dataset);

	MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.INLAND_WATERS;

	coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);

	ResponsibleParty datasetContact = new ResponsibleParty();
	datasetContact.setOrganisationName("Instituto Uruguayo de Meteorología (INUMET)");
	datasetContact.setRoleCode("pointOfContact");
	// if (stationOrganization.toLowerCase().contains("inmet")) {
	// Contact contactInfo = new Contact();
	// Address address = new Address();
	// address.addElectronicMailAddress("diretor.inmet@inmet.gov.br");
	// address.addDeliveryPoint("INMET - Instituto Nacional de Meteorologia | Eixo Monumental Sul Via S1 -
	// Sudoeste");
	// address.setCity("Brasília");
	// address.setAdministrativeArea("DF");
	// address.setPostalCode("70680-900");
	// address.setCountry("Brazil");
	// contactInfo.addPhoneVoice("+(61) 2102-4602");
	// contactInfo.setAddress(address);
	// Online online = new Online();
	// online.setLinkage("http://www.inmet.gov.br/");
	// contactInfo.setOnline(online);
	// datasetContact.setContactInfo(contactInfo);
	// }

	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);

	coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " - " + parameterName);

	coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(parameterDescription);

	coreMetadata.getMIMetadata().getDataIdentification().setAbstract("Acquisition made at station: " + stationName + " Measurement: "
		+ parameterDescription + " Measurement type: " + parameterTipoDeMedicion);

	GridSpatialRepresentation grid = new GridSpatialRepresentation();
	grid.setNumberOfDimensions(1);
	grid.setCellGeometryCode("point");
	Dimension time = new Dimension();
	time.setDimensionNameTypeCode("time");

	// if (series.isTimeScaleRegular()) {
	// Number timeSpacing = series.getTimeScaleTimeSpacing();
	// if (timeSpacing != null && timeSpacing.doubleValue() > Math.pow(10, -16)) {
	// String resolutionUOM = series.getTimeScaleUnitName();
	// time.setResolution(resolutionUOM, timeSpacing.doubleValue());
	// }
	// }
	grid.addAxisDimension(time);
	coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

	INUMETIdentifierMangler mangler = new INUMETIdentifierMangler();

	// site code network + site code: both needed for access
	mangler.setPlatformIdentifier(stationId);

	// variable vocabulary + variable code: both needed for access
	mangler.setParameterIdentifier(parameterId);

	String identifier = mangler.getMangling();

	coreMetadata.addDistributionOnlineResource(identifier, CommonNameSpaceContext.INUMET_URI, NetProtocols.INUMET.getCommonURN(),
		"download");

	String resourceIdentifier = generateCode(dataset, identifier);

	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

	Online downloadOnline = coreMetadata.getOnline();

	String onlineId = downloadOnline.getIdentifier();
	if (onlineId == null) {
	    downloadOnline.setIdentifier();
	}

	downloadOnline.setIdentifier(onlineId);

	dataset.getExtensionHandler().setCountry(Country.URUGUAY.getShortName());

	return dataset;
    }

}
