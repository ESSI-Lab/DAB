package eu.essi_lab.accessor.bndmet;

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
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.accessor.bndmet.model.BNDMETParameter;
import eu.essi_lab.accessor.bndmet.model.BNDMETParameter.BNDMET_Parameter_Code;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation.BNDMET_Station_Code;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
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
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

public class BNDMETMapper extends AbstractResourceMapper {

    public static final String MISSING_VALUE = "-9999";
    private static final String BNDMET_MAPPING_ERROR = "BNDMET_MAPPING_ERROR";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.BNDMET_URI;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {
	return null;
    }

    String ns = CommonNameSpaceContext.BNDMET_URI + ":";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Logger logger = GSLoggerFactory.getLogger(getClass());

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	String originalMetadata = originalMD.getMetadata();

	BNDMETStation station;
	BNDMETParameter parameter;
	try {
	    JSONObject jsonObject = new JSONObject(originalMetadata);
	    station = new BNDMETStation(jsonObject);
	    parameter = station.getParameters().get(0);

	} catch (Exception e1) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to parse BNDMET JSON: " + originalMetadata, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BNDMET_MAPPING_ERROR, //
		    e1);
	}

	String parameterCode = parameter.getValue(BNDMET_Parameter_Code.CODIGO);
	String parameterMeasurementType = parameter.getValue(BNDMET_Parameter_Code.TIPO_GERACAO);
	String parameterDescription = parameter.getValue(BNDMET_Parameter_Code.DESCRICAO);
	String parameterPeriod = parameter.getValue(BNDMET_Parameter_Code.PERIODICIDADE);
	String parameterUnits = parameter.getValue(BNDMET_Parameter_Code.UNIDADE);
	String parameterClass = parameter.getValue(BNDMET_Parameter_Code.CLASSE);

	String stationOscarCode = station.getValue(BNDMET_Station_Code.CD_OSCAR);
	String stationName = station.getValue(BNDMET_Station_Code.DC_NOME);
	String stationCapital = station.getValue(BNDMET_Station_Code.FL_CAPITAL);
	String stationTimeEnd = station.getValue(BNDMET_Station_Code.DT_FIM_OPERACAO);
	String stationSituation = station.getValue(BNDMET_Station_Code.CD_SITUACAO);
	String stationType = station.getValue(BNDMET_Station_Code.TP_ESTACAO);
	String stationLatitude = station.getValue(BNDMET_Station_Code.VL_LATITUDE);
	String stationWigosCode = station.getValue(BNDMET_Station_Code.CD_WSI);
	String stationDistrict = station.getValue(BNDMET_Station_Code.CD_DISTRITO);
	String stationAltitude = station.getValue(BNDMET_Station_Code.VL_ALTITUDE);
	String stationState = station.getValue(BNDMET_Station_Code.SG_ESTADO);
	String stationOrganization = station.getValue(BNDMET_Station_Code.SG_ENTIDADE);
	String stationCode = station.getValue(BNDMET_Station_Code.CD_ESTACAO);
	String stationLongitude = station.getValue(BNDMET_Station_Code.VL_LONGITUDE);
	String stationTimeBegin = station.getValue(BNDMET_Station_Code.DT_INICIO_OPERACAO);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	String platformIdentifier = "urn:wmo:wigos:" + stationWigosCode; // e.g. "0-76-0-2300200000000446"

	String parameterIdentifier = ns + parameterCode;

	coreMetadata.addDistributionFormat("JSON");

	coreMetadata.getMIMetadata().setLanguage("Portuguese");

	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(platformIdentifier);

	String geographicLocation = "";
	String siteDescription = stationName + " ; " + //
		stationType + " ; " + //
		stationSituation;

	platform.setDescription(siteDescription);
	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationType);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationSituation);

	Keywords keyword = new Keywords();
	keyword.setTypeCode("place");
	keyword.addKeyword(stationDistrict);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	Keywords keyword2 = new Keywords();
	keyword2.setTypeCode("place");
	keyword2.addKeyword(stationState);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword2);

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
	String parameterName = parameterDescription;
	if (parameterName.contains("(") && !parameterName.contains("NUVENS")) {
	    parameterName = parameterName.substring(0, parameterName.indexOf("(")).trim();
	}
	if (parameterName.contains(",") && !parameterName.contains("VENTO")) {
	    parameterName = parameterName.substring(0, parameterName.indexOf(",")).trim();
	}
	parameterName = removeSuffix(parameterName, new String[] { //

		" HORARIA", //
		" MAX. NA HORA ANT.", //
		" MAX.NA HORA ANT.", //
		" MAXIMA DIARIA", //
		" MAXIMA MENSAL", //
		" MAXIMA", //
		" MEDIA DIARIA", //
		" MEDIA MENSAL", //
		" MIN. NA HORA ANT.", //
		" MÁXIMA MEDIA", //
		" MÁXIMA NA HORA ANT.", //
		" MÁXIMA", //
		" MÉDIA COMPENSADA", //
		" MÉDIA DIÁRIA", //
		" MÉDIA MENSAL", //
		" MÉDIA", //
		" MÍNIMA MEDIA", //
		" MÍNIMA NA HORA ANT.", //
		" MÍNIMA", //
		" TOTAL", //
		", MENSAL", //

	});
	parameterName = parameterName.toLowerCase();

	coverageDescription.setAttributeTitle(parameterName);

	InterpolationType interpolation = InterpolationType.CONTINUOUS;
	if (parameterUnits.equals("codigo")) {
	    interpolation = InterpolationType.CATEGORICAL;
	}
	if (parameterUnits.equals("número")) {
	    interpolation = InterpolationType.CONST;
	}
	if (parameterMeasurementType.equals("Dado Observado")) {
	    interpolation = InterpolationType.CONTINUOUS;
	}
	if (parameterMeasurementType.toLowerCase().contains("media") || //
		parameterDescription.toLowerCase().contains("media") || //
		parameterMeasurementType.toLowerCase().contains("média") || //
		parameterDescription.toLowerCase().contains("média")) {

	    interpolation = InterpolationType.AVERAGE;
	}
	if (parameterMeasurementType.toLowerCase().contains("max") || //
		parameterDescription.toLowerCase().contains("max") || //
		parameterMeasurementType.toLowerCase().contains("máx") || //
		parameterDescription.toLowerCase().contains("máx")) {

	    interpolation = InterpolationType.MAX;
	}
	if (parameterMeasurementType.toLowerCase().contains("min") || //
		parameterDescription.toLowerCase().contains("min") || //
		parameterMeasurementType.toLowerCase().contains("mín") || //
		parameterDescription.toLowerCase().contains("mín")) {
	    interpolation = InterpolationType.MIN;
	}
	if (parameterMeasurementType.toLowerCase().contains("acumulado") || //
		parameterMeasurementType.toLowerCase().contains("total") || //
		parameterDescription.toLowerCase().contains("acumulado") || //
		parameterDescription.toLowerCase().contains("total")) {
	    interpolation = InterpolationType.TOTAL;
	}

	dataset.getExtensionHandler().setTimeInterpolation(interpolation);

	String timeUnits;
	String timeUnitsAbbreviation;
	switch (parameterPeriod.toLowerCase()) {
	case "mensal":
	    timeUnits = "month";
	    timeUnitsAbbreviation = "month";
	    break;
	case "diario":
	    timeUnits = "day";
	    timeUnitsAbbreviation = "day";
	    break;
	case "horário":
	case "horario":
	    timeUnits = "hour";
	    timeUnitsAbbreviation = "h";
	    break;
	default:
	    timeUnits = "unknown";
	    timeUnitsAbbreviation = "unk";
	    break;
	}
	dataset.getExtensionHandler().setTimeUnits(timeUnits);
	dataset.getExtensionHandler().setTimeUnitsAbbreviation(timeUnitsAbbreviation);

	switch (interpolation) {
	case CONTINUOUS:
	    break;
	default:
	    dataset.getExtensionHandler().setTimeSupport("1");
	    break;
	}

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

	dataQuality.setLineageStatement(parameterMeasurementType);

	String beginPosition = getISO8601Date(stationTimeBegin);
	String endPosition = getISO8601Date(stationTimeEnd);

	if (beginPosition == null) {
	    Long threeMonths = 1000 * 60 * 60 * 24 * 100l;
	    Date threeMonthsAgo = new Date(System.currentTimeMillis() - threeMonths);
	    beginPosition = ISO8601DateTimeUtils.getISO8601DateTime(threeMonthsAgo);
	}

	coreMetadata.addTemporalExtent(beginPosition, endPosition);

	setIndeterminatePosition(dataset);

	MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.INLAND_WATERS;

	coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);

	ResponsibleParty datasetContact = new ResponsibleParty();
	datasetContact.setOrganisationName(stationOrganization);
	datasetContact.setRoleCode("publisher");
	if (stationOrganization.toLowerCase().contains("inmet")) {
	    Contact contactInfo = new Contact();
	    Address address = new Address();
	    address.addElectronicMailAddress("diretor.inmet@inmet.gov.br");
	    address.addDeliveryPoint("INMET - Instituto Nacional de Meteorologia | Eixo Monumental Sul Via S1 - Sudoeste");
	    address.setCity("Brasília");
	    address.setAdministrativeArea("DF");
	    address.setPostalCode("70680-900");
	    address.setCountry("Brazil");
	    contactInfo.addPhoneVoice("+(61) 2102-4602");
	    contactInfo.setAddress(address);
	    Online online = new Online();
	    online.setLinkage("http://www.inmet.gov.br/");
	    contactInfo.setOnline(online);
	    datasetContact.setContactInfo(contactInfo);
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(datasetContact);
	}

	coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " - " + parameterDescription);

	coreMetadata.getMIMetadata().getDataIdentification()
		.setCitationAlternateTitle(parameterDescription + " " + parameterMeasurementType);

	coreMetadata.getMIMetadata().getDataIdentification().setAbstract("Acquisition made at station: " + stationName + " Measurement: "
		+ parameterDescription + " Measurement type: " + parameterMeasurementType);

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

	BNDMETIdentifierMangler mangler = new BNDMETIdentifierMangler();

	// site code network + site code: both needed for access
	mangler.setPlatformIdentifier(stationCode);

	// variable vocabulary + variable code: both needed for access
	mangler.setParameterIdentifier(parameterCode);

	String identifier = mangler.getMangling();

	coreMetadata.addDistributionOnlineResource(identifier, BNDMETClient.STANDARD_ENDPOINT, NetProtocols.BNDMET.getCommonURN(),
		"download");

	String resourceIdentifier = generateCode(dataset, identifier);

	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	Online downloadOnline = coreMetadata.getOnline();

	String onlineId = downloadOnline.getIdentifier();
	if (onlineId == null) {
	    downloadOnline.setIdentifier();
	}

	downloadOnline.setIdentifier(onlineId);
	Country country = Country.BRAZIL;
	dataset.getExtensionHandler().setCountry(country.getShortName());
	dataset.getExtensionHandler().setCountryISO3(country.getISO3());
	return dataset;
    }

    private String removeSuffix(String parameterName, String[] removes) {
	String ret = parameterName;
	for (String remove : removes) {
	    if (ret.endsWith(remove)) {
		ret = ret.replace(remove, "");
	    }
	}
	return ret;
    }

    /**
     * @param timeString e.g. "2009-04-21T21:00:00.000-03:00"
     * @return
     */
    public static String getISO8601Date(String timeString) {
	if (timeString == null) {
	    return null;
	}
	Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(timeString);
	if (optionalDate.isPresent()) {
	    Date date = optionalDate.get();
	    return ISO8601DateTimeUtils.getISO8601DateTime(date);
	}
	return null;
    }

}
