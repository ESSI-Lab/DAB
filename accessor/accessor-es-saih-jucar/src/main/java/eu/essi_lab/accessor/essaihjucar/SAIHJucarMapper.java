package eu.essi_lab.accessor.essaihjucar;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * @author boldrini
 */
public class SAIHJucarMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String SAIH_JUCAR_SCHEMA = "https://saih.chj.es/schema";

    static final String LICENSE = "Reuse of public sector information under the general conditions of the legal notice of the"
	    + " Confederación Hidrográfica del Júcar (Ley 37/2007 and Real Decreto 1495/2011: cite the source, do not alter the"
	    + " data or their meaning, mention the date of the last update) - https://www.chj.es/es-es/Organismo/Paginas/AvisoLegal.aspx"
	    + " - Source: SAIH Júcar, Confederación Hidrográfica del Júcar (https://saih.chj.es/). Real time data are provisional:"
	    + " they are transmitted every five minutes and loaded without being filtered or validated (https://saih.chj.es/disclaimer).";

    static final String ORGANIZATION = "Confederación Hidrográfica del Júcar (CHJ)";

    static final String SYSTEM = "SAIH (Sistema Automático de Información Hidrológica) Júcar";

    /**
     * Resolution of the observations
     */
    static final String RESOLUTION = "PT5M";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	SAIHJucarEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(SAIHJucarEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationId = entity.getStationId();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), "station:" + stationId));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String stationName = entity.getStationName();
	String propertyName = getPropertyName(entity);
	String variableName = entity.getVariableName();
	String begin = object.optString(SAIHJucarClient.BEGIN, null);
	String end = object.optString(SAIHJucarClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(LICENSE);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, propertyName, keywords);

	coreMetadata.setTitle(stationName + " - " + propertyName + (variableName.isEmpty() ? "" : " (" + variableName + ")"));

	coreMetadata.setAbstract("Time series of " + propertyName.toLowerCase() + getVariableDescription(entity) + " at station "
		+ stationName + getStationDescription(entity) + getReservoirDescription(entity) + ", from the real time " + SYSTEM
		+ " of the " + ORGANIZATION + " (Spain). Five minutes provisional values, not validated. The archive of the web site"
		+ " starts in 2024.");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(SAIHJucarClient.LAT) && object.has(SAIHJucarClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(SAIHJucarClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(SAIHJucarClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getVariableId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.SAIH_JUCAR.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return the English name of the property, e.g. "River discharge"
     */
    static String getPropertyName(SAIHJucarEntity entity) {

	return entity.getType().map(SAIHJucarVariableType::getLabel).orElse(entity.getVariableName());
    }

    /**
     * @param entity
     * @return e.g. " (CAUDAL RÍO SERPIS SOBRE EL AZUD, variable 14099, m³/s)"
     */
    private static String getVariableDescription(SAIHJucarEntity entity) {

	StringBuilder builder = new StringBuilder(" (");
	if (!entity.getVariableName().isEmpty()) {
	    builder.append(entity.getVariableName()).append(", ");
	}
	builder.append("variable ").append(entity.getVariableId());
	if (!entity.getUnit().isEmpty()) {
	    builder.append(", ").append(entity.getUnit());
	}
	builder.append(")");

	String description = entity.getString(SAIHJucarClient.VARIABLE_DESCRIPTION);
	if (!description.isEmpty()) {
	    builder.append(" - ").append(description).append(" -");
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @return e.g. " (9L01, remote station 349) in Villalonga (Valencia), sub-basin Serpis"
     */
    private static String getStationDescription(SAIHJucarEntity entity) {

	StringBuilder builder = new StringBuilder(" (");
	String code = entity.getString(SAIHJucarClient.STATION_CODE);
	if (!code.isEmpty()) {
	    builder.append(code).append(", ");
	}
	builder.append("remote station ").append(entity.getStationId()).append(")");

	String town = entity.getString(SAIHJucarClient.TOWN);
	String province = entity.getString(SAIHJucarClient.PROVINCE);
	if (!town.isEmpty()) {
	    builder.append(" in ").append(town);
	    if (!province.isEmpty()) {
		builder.append(" (").append(province).append(")");
	    }
	}

	String subBasin = entity.getString(SAIHJucarClient.SUB_BASIN);
	if (!subBasin.isEmpty()) {
	    builder.append(", sub-basin ").append(subBasin);
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @return the capacity and the spillway level of a reservoir, if known
     */
    private static String getReservoirDescription(SAIHJucarEntity entity) {

	StringBuilder builder = new StringBuilder();

	String capacity = entity.getString(SAIHJucarClient.RESERVOIR_CAPACITY);
	if (!capacity.isEmpty()) {
	    builder.append(", reservoir capacity at normal maximum level ").append(capacity).append(" hm³");
	}

	String spillway = entity.getString(SAIHJucarClient.RESERVOIR_SPILLWAY_LEVEL);
	if (!spillway.isEmpty()) {
	    builder.append(", spillway level ").append(spillway).append(" m a.s.l.");
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(SAIHJucarEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getVariableName());
	addKeyword(keywords, entity.getString(SAIHJucarClient.SUB_BASIN));
	addKeyword(keywords, entity.getString(SAIHJucarClient.PROVINCE));

	addKeyword(keywords, "Spain");
	addKeyword(keywords, "Júcar");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "SAIH");
    }

    /**
     * @param keywords
     * @param value
     */
    private void addKeyword(Keywords keywords, String value) {

	if (value != null && !value.isEmpty()) {
	    keywords.addKeyword(value);
	}
    }

    /**
     * @param entity
     * @param coreMetadata
     */
    private void addPlatform(SAIHJucarEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	platform.setDescription("Station " + stationName + getStationDescription(entity) + " (Spain)");
	platform.setMDIdentifierCode(NetProtocolWrapper.SAIH_JUCAR.getCommonURN() + "/" + entity.getStationId());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(SAIHJucarEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(
		propertyName + (entity.getVariableName().isEmpty() ? "" : " (" + entity.getVariableName() + ")"));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.SAIH_JUCAR.getCommonURN() + ":" + entity.getType().map(SAIHJucarVariableType::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(SAIHJucarEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();

	handler.setAttributeUnits(getUnitName(unit));
	handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit));
	handler.setCountry("Spain");
	handler.setTimeResolutionDuration8601(RESOLUTION);

	if (entity.getType().filter(t -> t == SAIHJucarVariableType.PRECIPITATION_INTENSITY).isPresent()) {
	    // five minutes intensity
	    handler.setTimeInterpolation(InterpolationType.AVERAGE_PREC);
	    handler.setTimeAggregationDuration8601(RESOLUTION);
	} else {
	    handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	}
    }

    /**
     * @param unit the unit used by the provider
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "m³/s":
	    return "cubic metres per second";
	case "m s.n.m.":
	    return "metres above sea level";
	case "hm³":
	    return "cubic hectometres";
	case "mm/h":
	    return "millimetres per hour";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the unit used by the provider
     * @return
     */
    static String getUnitAbbreviation(String unit) {

	switch (unit) {
	case "m³/s":
	    return "m3/s";
	case "m s.n.m.":
	    return "m";
	case "hm³":
	    return "hm3";
	default:
	    return unit;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private SAIHJucarEntity getEntity(OriginalMetadata originalMD) {

	return new SAIHJucarEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SAIH_JUCAR_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	SAIHJucarEntity entity = getEntity(resource.getOriginalMetadata());
	return decorateIdentifier(resource.getSource().getEndpoint(), entity.getVariableId());
    }

    /**
     * @param endpoint
     * @param entityId
     * @return
     */
    private String decorateIdentifier(String endpoint, String entityId) {

	try {
	    return StringUtils.hashSHA1messageDigest(endpoint + entityId);
	} catch (Exception e) {
	    String id = endpoint + entityId;
	    id = id.replace("https:", "");
	    id = id.replace("http:", "");
	    id = id.replace("//", "");
	    id = id.replace("/", "");
	    return StringUtils.encodeUTF8(id);
	}
    }
}
