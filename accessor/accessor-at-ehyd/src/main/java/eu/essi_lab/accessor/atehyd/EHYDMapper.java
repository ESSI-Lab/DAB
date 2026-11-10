package eu.essi_lab.accessor.atehyd;

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
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * @author boldrini
 */
public class EHYDMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String EHYD_SCHEMA = "https://ehyd.gv.at/schema";

    static final String LICENSE = "Creative Commons Attribution 4.0 International (CC BY 4.0) - Open Government Data Austria"
	    + " (https://www.data.gv.at) - Datenquelle: BML, eHYD (https://ehyd.gv.at)";

    static final String ORGANIZATION = "Federal Ministry of Agriculture and Forestry, Climate and Environmental Protection, Regions"
	    + " and Water Management (BML) - Hydrographic Service of Austria";

    static final String ARCHIVE_NOTE = "Checked archive data published in the hydrographic yearbooks of Austria (Hydrographisches"
	    + " Jahrbuch von Österreich), available on eHYD up to the latest published yearbook (not updated in real time)."
	    + " Times are in MEZ (UTC+1).";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	EHYDEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(EHYDEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String hzbnr = entity.getHZBNumber();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), hzbnr));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String stationName = entity.getStationName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(EHYDClient.BEGIN, null);
	String end = object.optString(EHYDClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	String operator = getOperator(entity);
	if (!operator.isEmpty()) {
	    ResponsibleParty originator = new ResponsibleParty();
	    originator.setOrganisationName(operator);
	    originator.setRoleCode("originator");
	    dataId.addPointOfContact(originator);
	}

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(LICENSE);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, keywords);

	coreMetadata.setTitle(getStationLabel(entity) + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " at station " + stationName + " (HZB number "
		+ hzbnr + ")" + getStationDescription(entity) + ", from the eHYD portal of the " + ORGANIZATION + ". " + ARCHIVE_NOTE);

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(EHYDClient.LAT) && object.has(EHYDClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(EHYDClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(EHYDClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.EHYD.getCommonURN(), //
		"download");

	// archive series, ending with the latest published yearbook: no indeterminate end position

	return dataset;
    }

    /**
     * @param entity
     * @return e.g. "Water level, daily mean"
     */
    static String getPropertyName(EHYDEntity entity) {

	return entity.getProduct().map(EHYDProduct::getLabel).orElse(entity.getString(EHYDClient.PRODUCT));
    }

    /**
     * @param entity
     * @return the property name, with the German name of the product and the unit
     */
    static String getPropertyDescription(EHYDEntity entity) {

	StringBuilder builder = new StringBuilder(getPropertyName(entity).toLowerCase());

	String german = entity.getProduct().map(EHYDProduct::getFileName).orElse("");
	String unit = entity.getUnit();

	builder.append(" (").append(german);
	if (!unit.isEmpty()) {
	    builder.append(german.isEmpty() ? "" : ", ").append(unit);
	}
	builder.append(")");

	if (entity.getProduct().filter(p -> p.getProperty().equals("Water level")).isPresent()) {
	    builder.append(", referred to the gauge zero");
	}

	return builder.toString().replace(" ()", "");
    }

    /**
     * @param entity
     * @return e.g. "Mühlen (Navisbach)"
     */
    static String getStationLabel(EHYDEntity entity) {

	String waterbody = entity.getString(EHYDClient.WATERBODY);
	return entity.getStationName() + (waterbody.isEmpty() ? "" : " (" + waterbody + ")");
    }

    /**
     * @param entity
     * @return the description of the station location, prefixed by a comma
     */
    private static String getStationDescription(EHYDEntity entity) {

	StringBuilder builder = new StringBuilder();

	append(builder, "waterbody ", entity.getString(EHYDClient.WATERBODY), "");
	append(builder, "groundwater body ", entity.getString(EHYDClient.GROUNDWATER_BODY), "");
	append(builder, "aquifer ", entity.getString(EHYDClient.AQUIFER), "");
	append(builder, "catchment area ", entity.getString(EHYDClient.CATCHMENT_AREA), " km²");
	append(builder, "gauge zero ", entity.getString(EHYDClient.GAUGE_DATUM), " m above the Adriatic sea");
	append(builder, "", entity.getString(EHYDClient.FEDERAL_STATE), "");

	String operator = getOperator(entity);
	if (!operator.isEmpty()) {
	    builder.append(", operated by ").append(operator);
	}

	return builder.toString();
    }

    /**
     * @param builder
     * @param prefix
     * @param value
     * @param suffix
     */
    private static void append(StringBuilder builder, String prefix, String value, String suffix) {

	if (!value.isEmpty()) {
	    builder.append(", ").append(prefix).append(value).append(suffix);
	}
    }

    /**
     * @param entity
     * @return e.g. "Hydrographischer Dienst (HD-Tirol)"
     */
    static String getOperator(EHYDEntity entity) {

	String operator = entity.getString(EHYDClient.OPERATOR);
	String office = entity.getString(EHYDClient.OFFICE);

	if (operator.isEmpty()) {
	    return office;
	}

	if (office.isEmpty() || operator.contains(office)) {
	    return operator;
	}

	return operator + " (" + office + ")";
    }

    /**
     * @param entity
     * @param keywords
     */
    private void addKeywords(EHYDEntity entity, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	entity.getProduct().ifPresent(p -> {
	    addKeyword(keywords, p.getProperty());
	    addKeyword(keywords, p.getFileName());
	});
	addKeyword(keywords, entity.getString(EHYDClient.WATERBODY));
	addKeyword(keywords, entity.getString(EHYDClient.GROUNDWATER_BODY));
	addKeyword(keywords, entity.getString(EHYDClient.FEDERAL_STATE));
	addKeyword(keywords, entity.getString(EHYDClient.OFFICE));

	addKeyword(keywords, "Austria");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "eHYD");
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
    private void addPlatform(EHYDEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(entity.getStationName());
	platform.setCitation(citation);

	platform.setDescription("Station " + entity.getStationName() + " (HZB number " + entity.getHZBNumber() + ")"
		+ getStationDescription(entity) + " (Austria)");
	platform.setMDIdentifierCode(NetProtocolWrapper.EHYD.getCommonURN() + "/" + entity.getHZBNumber());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(EHYDEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.EHYD.getCommonURN() + ":" + entity.getProduct().map(EHYDProduct::getId).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(EHYDEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();

	handler.setAttributeUnits(getUnitName(unit));
	handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit));
	handler.setCountry("Austria");

	entity.getProduct().ifPresent(product -> {
	    handler.setTimeInterpolation(product.getInterpolation());
	    handler.setTimeResolutionDuration8601(product.getAggregation());
	    handler.setTimeAggregationDuration8601(product.getAggregation());
	});
    }

    /**
     * @param unit the unit used by eHYD
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "cm":
	    return "centimetres";
	case "m³/s":
	    return "cubic metres per second";
	case "°C":
	    return "degrees Celsius";
	case "t":
	    return "tonnes";
	case "m ü.A.":
	    return "metres above the Adriatic sea";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the unit used by eHYD
     * @return
     */
    static String getUnitAbbreviation(String unit) {

	switch (unit) {
	case "m³/s":
	    return "m3/s";
	case "m ü.A.":
	    return "m";
	default:
	    return unit;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private EHYDEntity getEntity(OriginalMetadata originalMD) {

	return new EHYDEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return EHYD_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	EHYDEntity entity = getEntity(resource.getOriginalMetadata());
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
