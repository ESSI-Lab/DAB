package eu.essi_lab.accessor.esmitecoaforos;

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
public class MITECOAforosMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String MITECO_AFOROS_SCHEMA = "https://www.miteco.gob.es/anuario-aforos/schema";

    static final String LICENSE = "Esta información se puede usar de modo libre y gratuito siempre que se mencione al Ministerio para"
	    + " la Transición Ecológica y el Reto Demográfico como autor y propietario de la información de la siguiente manera:"
	    + " Fuente: «© Ministerio para la Transición Ecológica y el Reto Demográfico». (Free use provided that the source is"
	    + " acknowledged; see https://www.miteco.gob.es/es/cartografia-y-sig/ide/descargas/agua/anuario-de-aforos.html and"
	    + " https://www.miteco.gob.es/es/atencion-al-ciudadano/aviso-legal.html)";

    static final String ORGANIZATION = "Ministerio para la Transición Ecológica y el Reto Demográfico (MITECO)";

    static final String VALIDATOR = "Centro de Estudios Hidrográficos del CEDEX";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	MITECOAforosEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(MITECOAforosEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), entity.getBasin() + "|" + entity.getStationId()));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	MITECOAforosVariable variable = entity.getVariable().orElse(MITECOAforosVariable.RIVER_DISCHARGE_DAILY);

	String stationName = entity.getStationName();
	String begin = object.optString(MITECOAforosClient.BEGIN, null);
	String end = object.optString(MITECOAforosClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	ResponsibleParty validator = new ResponsibleParty();
	validator.setOrganisationName(VALIDATOR);
	validator.setRoleCode("processor");
	dataId.addPointOfContact(validator);

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(LICENSE);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, variable, keywords);

	coreMetadata.setTitle(stationName + " - " + getPropertyName(variable));

	coreMetadata.setAbstract(getPropertyName(variable) + " (" + variable.getSpanishLabel() + ", " + variable.getUnit()
		+ ") at the " + variable.getStationType().getLabel().toLowerCase() + " " + getStationDescription(entity)
		+ ", from the Anuario de Aforos (yearbook of the official gauging network SAIH-ROEA) published by " + ORGANIZATION
		+ getEditionDescription(entity) + ". Validated historical data: the yearbook is updated once a year and its values end"
		+ " with the last hydrological year (October - September) of the edition.");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(MITECOAforosClient.LAT) && object.has(MITECOAforosClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(MITECOAforosClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(MITECOAforosClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(variable, coreMetadata);
	addExtensions(variable, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.MITECO_AFOROS.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param variable
     * @return e.g. "Discharge (daily mean)"
     */
    static String getPropertyName(MITECOAforosVariable variable) {

	return variable.getLabel() + " (" + (variable.isDaily() ? "daily" : "monthly") + ")";
    }

    /**
     * @param entity
     * @return e.g. "PUENTE DE SAMA (1033), river NALON, municipality LANGREO, province ASTURIAS, basin CANTABRICO"
     */
    static String getStationDescription(MITECOAforosEntity entity) {

	StringBuilder builder = new StringBuilder();
	builder.append(entity.getStationName()).append(" (").append(entity.getStationId()).append(")");

	appendIfNotEmpty(builder, ", river ", entity.getString(MITECOAforosClient.RIVER));
	appendIfNotEmpty(builder, ", municipality ", entity.getString(MITECOAforosClient.MUNICIPALITY));
	appendIfNotEmpty(builder, ", province ", entity.getString(MITECOAforosClient.PROVINCE));
	appendIfNotEmpty(builder, ", basin ", entity.getBasin());

	return builder.toString();
    }

    /**
     * @param entity
     * @return
     */
    private static String getEditionDescription(MITECOAforosEntity entity) {

	String edition = entity.getString(MITECOAforosClient.EDITION);
	return edition.isEmpty() ? "" : " (edition " + edition + ")";
    }

    /**
     * @param builder
     * @param prefix
     * @param value
     */
    private static void appendIfNotEmpty(StringBuilder builder, String prefix, String value) {

	if (value != null && !value.isEmpty()) {
	    builder.append(prefix).append(value);
	}
    }

    /**
     * @param entity
     * @param variable
     * @param keywords
     */
    private void addKeywords(MITECOAforosEntity entity, MITECOAforosVariable variable, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, variable.getLabel());
	addKeyword(keywords, variable.getSpanishLabel());
	addKeyword(keywords, variable.getStationType().getLabel());
	addKeyword(keywords, variable.getStationType().getSpanishLabel());
	addKeyword(keywords, entity.getString(MITECOAforosClient.RIVER));
	addKeyword(keywords, entity.getString(MITECOAforosClient.PROVINCE));
	addKeyword(keywords, entity.getBasin());

	addKeyword(keywords, "Spain");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "Anuario de Aforos");
	addKeyword(keywords, "ROEA");
	addKeyword(keywords, "MITECO");
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
    private void addPlatform(MITECOAforosEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(entity.getStationName());
	platform.setCitation(citation);

	StringBuilder description = new StringBuilder(
		entity.getVariable().map(v -> v.getStationType().getLabel()).orElse("Station") + " " + getStationDescription(entity));
	appendIfNotEmpty(description, ", status: ", entity.getString(MITECOAforosClient.STATUS));
	appendIfNotEmpty(description, ", catchment area (km2): ", entity.getString(MITECOAforosClient.CATCHMENT_AREA));
	appendIfNotEmpty(description, ", altitude (m): ", entity.getString(MITECOAforosClient.ALTITUDE));
	appendIfNotEmpty(description, ", SAIH code: ", entity.getString(MITECOAforosClient.SAIH_CODE));
	appendIfNotEmpty(description, ", associated reservoir: ", entity.getString(MITECOAforosClient.RESERVOIR_ID));
	appendIfNotEmpty(description, ". ", entity.getString(MITECOAforosClient.COMMENT));
	description.append(" (Spain)");

	platform.setDescription(description.toString());
	platform.setMDIdentifierCode(
		NetProtocolWrapper.MITECO_AFOROS.getCommonURN() + "/" + entity.getBasin() + "/" + entity.getStationId());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param variable
     * @param coreMetadata
     */
    private void addCoverageDescription(MITECOAforosVariable variable, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(getPropertyName(variable));
	coverageDescription.setAttributeDescription(variable.getSpanishLabel() + " (" + variable.getUnit() + "), column "
		+ variable.getColumn() + " of table " + variable.getFileName());
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.MITECO_AFOROS.getCommonURN() + ":" + variable.name());
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param variable
     * @param handler
     */
    private void addExtensions(MITECOAforosVariable variable, ExtensionHandler handler) {

	handler.setAttributeUnits(getUnitName(variable.getUnit()));
	handler.setAttributeUnitsAbbreviation(variable.getUnit());
	handler.setCountry("Spain");
	// values timestamped at the beginning of the day (or month)
	handler.setTimeInterpolation(variable.getInterpolation());

	boolean aggregated = variable.getInterpolation() != InterpolationType.CONTINUOUS;

	handler.setTimeResolutionDuration8601(variable.getResolution());
	if (aggregated) {
	    handler.setTimeAggregationDuration8601(variable.getResolution());
	}
    }

    /**
     * @param unit
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "m":
	    return "metres";
	case "m3/s":
	    return "cubic metres per second";
	case "hm3":
	    return "cubic hectometres";
	case "mm":
	    return "millimetres";
	case "mm/d":
	    return "millimetres per day";
	case "°C":
	    return "degrees Celsius";
	case "km/h":
	    return "kilometres per hour";
	case "h":
	    return "hours";
	default:
	    return unit;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private MITECOAforosEntity getEntity(OriginalMetadata originalMD) {

	return new MITECOAforosEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return MITECO_AFOROS_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	MITECOAforosEntity entity = getEntity(resource.getOriginalMetadata());
	return decorateIdentifier(resource.getSource().getEndpoint(), entity.getSeriesId());
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
