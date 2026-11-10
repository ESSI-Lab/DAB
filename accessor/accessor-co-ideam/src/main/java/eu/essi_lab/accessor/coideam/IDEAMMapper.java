package eu.essi_lab.accessor.coideam;

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
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * @author boldrini
 */
public class IDEAMMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String IDEAM_SCHEMA = "https://www.datos.gov.co/ideam/schema";

    static final String LICENSE = "Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0) -"
	    + " http://creativecommons.org/licenses/by-sa/4.0/legalcode - Source: Instituto de Hidrología, Meteorología y Estudios"
	    + " Ambientales (IDEAM), https://www.datos.gov.co. Raw data from the automatic stations, not validated by IDEAM: they may"
	    + " contain errors and inconsistencies and cannot be used as legal evidence; their use and interpretation are the sole"
	    + " responsibility of the user.";

    static final String ORGANIZATION = "Instituto de Hidrología, Meteorología y Estudios Ambientales (IDEAM)";

    static final String COUNTRY = "Colombia";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	IDEAMEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(IDEAMEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationCode = entity.getStationCode();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationCode));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String stationName = entity.getStationName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(IDEAMClient.BEGIN, null);
	String end = object.optString(IDEAMClient.END, null);

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

	coreMetadata.setTitle(stationName + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " at station " + stationName + " ("
		+ stationCode + ")" + getLocationDescription(entity) + ", from the raw data of the automatic stations of the "
		+ ORGANIZATION + " of Colombia published on datos.gov.co (dataset \""
		+ entity.getDataset().map(IDEAMDataset::getName).orElse("") + "\"). Observation times are in Colombian legal time"
		+ " (UTC-5). Raw data, not validated by IDEAM");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	getLocation(entity).ifPresent(location -> coreMetadata.addBoundingBox(location[0], location[1], location[0], location[1]));

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.IDEAM.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return the location of the station (latitude, longitude) given by the most recent row of the series or, if not valid, by
     *         the station catalog
     */
    static Optional<BigDecimal[]> getLocation(IDEAMEntity entity) {

	Optional<BigDecimal[]> location = getLocation(entity, IDEAMClient.LAT, IDEAMClient.LON);
	if (location.isPresent()) {
	    return location;
	}

	return getLocation(entity, IDEAMClient.CATALOG_LAT, IDEAMClient.CATALOG_LON);
    }

    /**
     * @param entity
     * @param latKey
     * @param lonKey
     * @return
     */
    private static Optional<BigDecimal[]> getLocation(IDEAMEntity entity, String latKey, String lonKey) {

	try {
	    BigDecimal lat = new BigDecimal(entity.getString(latKey));
	    BigDecimal lon = new BigDecimal(entity.getString(lonKey));

	    // some rows have a zero longitude, which is outside of Colombia
	    if (lat.signum() == 0 || lon.signum() == 0 || lat.abs().doubleValue() > 90 || lon.abs().doubleValue() > 180) {
		return Optional.empty();
	    }

	    return Optional.of(new BigDecimal[] { lat, lon });

	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param entity
     * @return e.g. "Water level"
     */
    static String getPropertyName(IDEAMEntity entity) {

	return entity.getDataset().map(IDEAMDataset::getLabel).orElse(entity.getString(IDEAMClient.SENSOR_DESCRIPTION));
    }

    /**
     * @param entity
     * @return the property name with the name of the dataset and the sensor description, e.g. "Water level (Nivel Instantáneo
     *         del Rio, sensor 0230 NIVEL INSTANTANEO)"
     */
    static String getPropertyDescription(IDEAMEntity entity) {

	StringBuilder builder = new StringBuilder(getPropertyName(entity));
	builder.append(" (");
	entity.getDataset().ifPresent(d -> builder.append(d.getName()).append(", "));
	builder.append("sensor ").append(entity.getSensorCode());

	String description = entity.getString(IDEAMClient.SENSOR_DESCRIPTION);
	if (!description.isEmpty()) {
	    builder.append(" ").append(description);
	}
	builder.append(")");

	return builder.toString();
    }

    /**
     * @param entity
     * @return
     */
    private static String getLocationDescription(IDEAMEntity entity) {

	StringBuilder builder = new StringBuilder();

	String stream = entity.getString(IDEAMClient.STREAM);
	if (!stream.isEmpty()) {
	    builder.append(", stream ").append(stream);
	}

	String zone = entity.getString(IDEAMClient.HYDROGRAPHIC_ZONE);
	if (!zone.isEmpty()) {
	    builder.append(", hydrographic zone ").append(zone);
	}

	String municipality = entity.getString(IDEAMClient.MUNICIPALITY);
	String department = entity.getString(IDEAMClient.DEPARTMENT);
	if (!municipality.isEmpty() || !department.isEmpty()) {
	    builder.append(", ").append(municipality);
	    if (!municipality.isEmpty() && !department.isEmpty()) {
		builder.append(" (").append(department).append(")");
	    } else {
		builder.append(department);
	    }
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(IDEAMEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	entity.getDataset().ifPresent(d -> addKeyword(keywords, d.getName()));
	addKeyword(keywords, entity.getString(IDEAMClient.SENSOR_DESCRIPTION));
	addKeyword(keywords, entity.getString(IDEAMClient.STREAM));
	addKeyword(keywords, entity.getString(IDEAMClient.HYDROGRAPHIC_ZONE));
	addKeyword(keywords, entity.getString(IDEAMClient.HYDROGRAPHIC_SUBZONE));
	addKeyword(keywords, entity.getString(IDEAMClient.DEPARTMENT));
	addKeyword(keywords, entity.getString(IDEAMClient.MUNICIPALITY));

	addKeyword(keywords, COUNTRY);
	addKeyword(keywords, entity.getDataset().filter(d -> !d.isWaterLevel()).isPresent() ? "Meteorology" : "Hydrology");
	addKeyword(keywords, "IDEAM");
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
    private void addPlatform(IDEAMEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	StringBuilder description = new StringBuilder("Station " + stationName + " (" + entity.getStationCode() + ")");
	description.append(getLocationDescription(entity));
	String category = entity.getString(IDEAMClient.CATEGORY);
	if (!category.isEmpty()) {
	    description.append(", category ").append(category);
	}
	String operator = entity.getString(IDEAMClient.OPERATOR);
	if (!operator.isEmpty()) {
	    description.append(", operated by ").append(operator);
	}
	description.append(" (").append(COUNTRY).append(")");

	platform.setDescription(description.toString());
	platform.setMDIdentifierCode(NetProtocolWrapper.IDEAM.getCommonURN() + "/" + entity.getStationCode());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(IDEAMEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.IDEAM.getCommonURN() + ":" + entity.getDataset().map(IDEAMDataset::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(IDEAMEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();

	handler.setAttributeUnits(getUnitName(unit));
	handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit));
	handler.setCountry(COUNTRY);

	Optional<IDEAMDataset> dataset = entity.getDataset();
	if (dataset.isEmpty()) {
	    return;
	}

	handler.setTimeResolutionDuration8601(dataset.get().getResolution());

	switch (dataset.get()) {
	case NIVEL_MIN:
	    handler.setTimeInterpolation(InterpolationType.MIN_PREC);
	    break;
	case NIVEL_MAX:
	    handler.setTimeInterpolation(InterpolationType.MAX_PREC);
	    break;
	case PRECIPITACION:
	    handler.setTimeInterpolation(InterpolationType.TOTAL_PREC);
	    break;
	default:
	    handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	}

	dataset.get().getAggregation().ifPresent(handler::setTimeAggregationDuration8601);
    }

    /**
     * @param unit the unit used by IDEAM
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit.toLowerCase()) {
	case "m":
	case "mt":
	    return "metres";
	case "cm":
	    return "centimetres";
	case "mm":
	    return "millimetres";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the unit used by IDEAM
     * @return
     */
    static String getUnitAbbreviation(String unit) {

	switch (unit.toLowerCase()) {
	case "m":
	case "mt":
	    return "m";
	case "cm":
	    return "cm";
	case "mm":
	    return "mm";
	default:
	    return unit;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private IDEAMEntity getEntity(OriginalMetadata originalMD) {

	return new IDEAMEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return IDEAM_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	IDEAMEntity entity = getEntity(resource.getOriginalMetadata());
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
