package eu.essi_lab.accessor.czchmi;

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
import java.util.Optional;

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
public class CHMIMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String CHMI_SCHEMA = "https://opendata.chmi.cz/hydrology/schema";

    static final String LICENSE = "Creative Commons Attribution 4.0 International (CC BY 4.0) - Source: Czech Hydrometeorological"
	    + " Institute (CHMI / ČHMÚ), https://opendata.chmi.cz/hydrology/ . According to the CHMI terms of use, the data must not"
	    + " be used to create or distribute warning products or products for crisis management. Operational (now and recent)"
	    + " data are not validated and may be affected by errors (e.g. construction works in the river bed, ice phenomena).";

    static final String ORGANIZATION = "Czech Hydrometeorological Institute (CHMI / ČHMÚ)";

    static final String COUNTRY = "Czechia";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	CHMIEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(CHMIEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationId = entity.getStationId();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationId));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String stationName = entity.getStationName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(CHMIClient.BEGIN, null);
	String end = object.optString(CHMIClient.END, null);

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

	coreMetadata.setTitle(stationName + getRiverDescription(entity, " (", ")") + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " at station " + stationName + " ("
		+ stationId + ")" + getRiverDescription(entity, " on the river ", "") + ", from the hydrology open data of the "
		+ ORGANIZATION + ". " + getDatasetDescription(entity));

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(CHMIClient.LAT) && object.has(CHMIClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(CHMIClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(CHMIClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.CHMI.getCommonURN(), //
		"download");

	if (isOperational(entity)) {
	    setIndeterminatePosition(dataset);
	}

	return dataset;
    }

    /**
     * @param entity
     * @return true if the series is updated in real time
     */
    static boolean isOperational(CHMIEntity entity) {

	return entity.getParameter().filter(p -> !p.getProduct().isHistorical()).isPresent();
    }

    /**
     * @param entity
     * @return e.g. "Daily mean discharge"
     */
    static String getPropertyName(CHMIEntity entity) {

	return entity.getParameter().map(CHMIParameter::getLabel).orElse(entity.getString(CHMIClient.PARAMETER));
    }

    /**
     * @param entity
     * @return the property name, with the CHMI name and the unit, e.g. "Water level (Vodní stav, cm above the gauge zero)"
     */
    static String getPropertyDescription(CHMIEntity entity) {

	Optional<CHMIParameter> parameter = entity.getParameter();
	if (parameter.isEmpty()) {
	    return getPropertyName(entity);
	}

	String unit = getUnitAbbreviation(parameter.get().getUnit());
	if (parameter.get().getUnit().equals("CM")) {
	    unit += " above the gauge zero";
	}

	return parameter.get().getLabel() + " (" + parameter.get().getProviderName() + ", " + unit + ")";
    }

    /**
     * @param entity
     * @return
     */
    private static String getDatasetDescription(CHMIEntity entity) {

	Optional<CHMIParameter> parameter = entity.getParameter();
	if (parameter.isEmpty()) {
	    return "";
	}

	if (parameter.get().getProduct().isHistorical()) {
	    return "Validated data of the historical archive (one file per year, published after the validation of each year).";
	}

	String ret = "Operational (not validated) data: 10 minutes values of the current day (now dataset) and values of the previous"
		+ " days of the current year (recent dataset)";

	Optional<CHMIParameter> counterpart = parameter.get().getHistoricalCounterpart();
	if (counterpart.isPresent()) {
	    ret += ", completed in the past with the validated instantaneous values of the historical archive ("
		    + counterpart.get().getProviderName() + ")";
	}

	return ret + ".";
    }

    /**
     * @param entity
     * @param prefix
     * @param suffix
     * @return
     */
    private static String getRiverDescription(CHMIEntity entity, String prefix, String suffix) {

	String river = entity.getString(CHMIClient.STREAM_NAME);
	return river.isEmpty() ? "" : prefix + river + suffix;
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(CHMIEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	entity.getParameter().ifPresent(p -> addKeyword(keywords, p.getProviderName()));
	addKeyword(keywords, entity.getString(CHMIClient.STREAM_NAME));
	addKeyword(keywords, entity.getString(CHMIClient.OPERATOR));

	addKeyword(keywords, COUNTRY);
	addKeyword(keywords, "Czech Republic");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "CHMI");
	addKeyword(keywords, "ČHMÚ");
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
    private void addPlatform(CHMIEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	StringBuilder description = new StringBuilder("Station " + stationName + " (" + entity.getStationId() + ")");
	description.append(getRiverDescription(entity, " on the river ", ""));
	appendDetail(description, "database number", entity.getString(CHMIClient.DBC), "");
	appendDetail(description, "catchment area", entity.getString(CHMIClient.CATCHMENT_AREA), " km2");
	appendDetail(description, "elevation", entity.getString(CHMIClient.ELEVATION), " m");
	appendDetail(description, "river km", entity.getString(CHMIClient.RIVER_KM), "");
	appendDetail(description, "hydrological order", entity.getString(CHMIClient.BASIN_CODE), "");
	appendDetail(description, "operator", entity.getString(CHMIClient.OPERATOR), "");
	appendDetail(description, "location", entity.getString(CHMIClient.LOCATION_DESCRIPTION), "");
	description.append(" (" + COUNTRY + ")");

	platform.setDescription(description.toString());
	platform.setMDIdentifierCode(NetProtocolWrapper.CHMI.getCommonURN() + "/" + entity.getStationId());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param builder
     * @param name
     * @param value
     * @param unit
     */
    private static void appendDetail(StringBuilder builder, String name, String value, String unit) {

	if (!value.isEmpty()) {
	    builder.append(", ").append(name).append(" ").append(value).append(unit);
	}
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(CHMIEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.CHMI.getCommonURN() + ":" + entity.getParameter().map(CHMIParameter::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(CHMIEntity entity, ExtensionHandler handler) {

	handler.setCountry(COUNTRY);

	Optional<CHMIParameter> parameter = entity.getParameter();
	if (parameter.isEmpty()) {
	    return;
	}

	String unit = parameter.get().getUnit();

	handler.setAttributeUnits(getUnitName(unit));
	handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit));

	if (parameter.get().getResolution() != null) {
	    handler.setTimeResolutionDuration8601(parameter.get().getResolution());
	}

	if (parameter.get().getAggregation() != null) {
	    handler.setTimeAggregationDuration8601(parameter.get().getAggregation());
	}

	handler.setTimeInterpolation(parameter.get().getInterpolation());
    }

    /**
     * @param unit the CHMI unit code
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "CM":
	    return "centimetres";
	case "M3_S":
	    return "cubic metres per second";
	case "0C":
	    return "degrees Celsius";
	case "G_M3":
	    return "grams per cubic metre";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the CHMI unit code
     * @return
     */
    static String getUnitAbbreviation(String unit) {

	switch (unit) {
	case "CM":
	    return "cm";
	case "M3_S":
	    return "m3/s";
	case "0C":
	    return "°C";
	case "G_M3":
	    return "g/m3";
	default:
	    return unit;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private CHMIEntity getEntity(OriginalMetadata originalMD) {

	return new CHMIEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CHMI_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	CHMIEntity entity = getEntity(resource.getOriginalMetadata());
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
