package eu.essi_lab.accessor.enhydris;

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
import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class EnhydrisMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String ENHYDRIS_SCHEMA = "https://enhydris.readthedocs.io/schema";

    static final String AGGREGATED = "Aggregated";

    /**
     * Pandas like time step, e.g. "10min", "3h", "1D", "7D", "1M"
     */
    private static final Pattern TIME_STEP = Pattern.compile("^(\\d*)\\s*([A-Za-z]+)$");

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	EnhydrisEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(EnhydrisEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Optional<EnhydrisInstance> instance = EnhydrisInstance.decode(source.getEndpoint());
	String publisher = instance.map(EnhydrisInstance::getPublisher).orElse(getHost(source.getEndpoint()));

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), String.valueOf(entity.getStationId())));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String propertyName = getPropertyName(entity);
	String begin = object.optString(EnhydrisClient.BEGIN, null);
	String end = object.optString(EnhydrisClient.END, null);

	String owner = getOwnerDescription(entity);
	if (!owner.isEmpty()) {
	    ResponsibleParty party = new ResponsibleParty();
	    party.setOrganisationName(owner);
	    party.setRoleCode("owner");
	    dataId.addPointOfContact(party);
	}

	ResponsibleParty publisherParty = new ResponsibleParty();
	publisherParty.setOrganisationName(publisher);
	publisherParty.setRoleCode("publisher");
	dataId.addPointOfContact(publisherParty);

	instance.ifPresent(i -> {
	    LegalConstraints constraints = new LegalConstraints();
	    constraints.addUseLimitation(i.getLicense());
	    dataId.addLegalConstraints(constraints);
	});

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, propertyName, instance, keywords);

	coreMetadata.setTitle(entity.getStationName() + " - " + propertyName + " (" + getTimeseriesDescription(entity) + ")");

	StringBuilder description = new StringBuilder("Time series of " + propertyName.toLowerCase());
	String variable = entity.getVariable();
	if (!variable.isEmpty() && !variable.equalsIgnoreCase(propertyName)) {
	    description.append(" (").append(variable.toLowerCase()).append(")");
	}
	description.append(" at station ").append(entity.getStationName());
	if (!owner.isEmpty()) {
	    description.append(", owned by ").append(owner);
	}
	description.append(". ").append(getTimeseriesDescription(entity)).append(" time series, published by ").append(publisher)
		.append(" through the Enhydris API (").append(source.getEndpoint()).append(").");

	coreMetadata.setAbstract(description.toString());

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(EnhydrisClient.LAT) && object.has(EnhydrisClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(EnhydrisClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(EnhydrisClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, source, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, instance, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.ENHYDRIS.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return the name of the time series group (e.g. "Mean stage"), or the variable if not available
     */
    static String getPropertyName(EnhydrisEntity entity) {

	if (!entity.getGroupName().isEmpty()) {
	    return entity.getGroupName();
	}

	if (!entity.getVariable().isEmpty()) {
	    return entity.getVariable();
	}

	return "Variable " + entity.getString(EnhydrisClient.VARIABLE_ID);
    }

    /**
     * @param entity
     * @return e.g. "Initial, 10min" or "Aggregated mean, 1D"
     */
    static String getTimeseriesDescription(EnhydrisEntity entity) {

	StringBuilder builder = new StringBuilder(entity.getTimeseriesType().isEmpty() ? "Time series" : entity.getTimeseriesType());

	if (!entity.getTimeseriesName().isEmpty()) {
	    builder.append(" ").append(entity.getTimeseriesName().toLowerCase());
	}

	if (!entity.getTimeStep().isEmpty()) {
	    builder.append(", ").append(entity.getTimeStep());
	}

	return builder.toString();
    }

    /**
     * @param timeStep e.g. "10min", "3h", "1D"
     * @return the ISO 8601 duration, e.g. "PT10M", "PT3H", "P1D"
     */
    static Optional<String> getResolution(String timeStep) {

	Matcher matcher = TIME_STEP.matcher(timeStep == null ? "" : timeStep.trim());
	if (!matcher.find()) {
	    return Optional.empty();
	}

	String number = matcher.group(1).isEmpty() ? "1" : matcher.group(1);

	switch (matcher.group(2)) {
	case "s":
	case "S":
	    return Optional.of("PT" + number + "S");
	case "min":
	case "T":
	    return Optional.of("PT" + number + "M");
	case "h":
	case "H":
	    return Optional.of("PT" + number + "H");
	case "d":
	case "D":
	    return Optional.of("P" + number + "D");
	case "W":
	    return Optional.of("P" + number + "W");
	case "M":
	case "MS":
	    return Optional.of("P" + number + "M");
	case "Y":
	case "YS":
	case "A":
	case "AS":
	    return Optional.of("P" + number + "Y");
	default:
	    return Optional.empty();
	}
    }

    /**
     * @param entity
     * @return
     */
    static InterpolationType getInterpolation(EnhydrisEntity entity) {

	if (AGGREGATED.equalsIgnoreCase(entity.getTimeseriesType())) {

	    switch (entity.getTimeseriesName().toLowerCase()) {
	    case "sum":
		return InterpolationType.TOTAL_PREC;
	    case "mean":
		return InterpolationType.AVERAGE_PREC;
	    case "max":
		return InterpolationType.MAX_PREC;
	    case "min":
		return InterpolationType.MIN_PREC;
	    default:
		return null;
	    }
	}

	switch (entity.getVariable().toLowerCase()) {
	case "rainfall":
	case "precipitation":
	case "snowfall":
	    return InterpolationType.TOTAL_PREC;
	default:
	    return InterpolationType.CONTINUOUS;
	}
    }

    /**
     * @param entity
     * @return e.g. "Ελληνικό Κέντρο Θαλάσσιων Ερευνών (ΕΛΚΕΘΕ)"
     */
    private static String getOwnerDescription(EnhydrisEntity entity) {

	String owner = entity.getOwner();
	String acronym = entity.getString(EnhydrisClient.OWNER_ACRONYM);

	if (owner.isEmpty()) {
	    return acronym;
	}

	return acronym.isEmpty() || acronym.equalsIgnoreCase(owner) ? owner : owner + " (" + acronym + ")";
    }

    /**
     * @param entity
     * @param propertyName
     * @param instance
     * @param keywords
     */
    private void addKeywords(EnhydrisEntity entity, String propertyName, Optional<EnhydrisInstance> instance, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	if (!entity.getVariable().equalsIgnoreCase(propertyName)) {
	    addKeyword(keywords, entity.getVariable());
	}
	addKeyword(keywords, entity.getOwner());
	addKeyword(keywords, entity.getString(EnhydrisClient.OWNER_ACRONYM));

	instance.ifPresent(i -> addKeyword(keywords, i.getCountry()));
	addKeyword(keywords, "Enhydris");
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
     * @param source
     * @param coreMetadata
     */
    private void addPlatform(EnhydrisEntity entity, GSSource source, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(entity.getStationName());
	platform.setCitation(citation);

	StringBuilder description = new StringBuilder("Station " + entity.getStationName());

	String code = entity.getString(EnhydrisClient.STATION_CODE);
	if (!code.isEmpty()) {
	    description.append(" (").append(code).append(")");
	}

	String owner = getOwnerDescription(entity);
	if (!owner.isEmpty()) {
	    description.append(", owned by ").append(owner);
	}

	String altitude = entity.getString(EnhydrisClient.ALTITUDE);
	if (!altitude.isEmpty()) {
	    description.append(", altitude ").append(altitude).append(" m");
	}

	String remarks = entity.getString(EnhydrisClient.STATION_REMARKS);
	if (!remarks.isEmpty()) {
	    description.append(". ").append(remarks.replaceAll("\\s+", " "));
	}

	platform.setDescription(description.toString());
	platform.setMDIdentifierCode(
		NetProtocolWrapper.ENHYDRIS.getCommonURN() + "/" + getHost(source.getEndpoint()) + "/" + entity.getStationId());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(EnhydrisEntity entity, String propertyName, CoreMetadata coreMetadata) {

	String variable = entity.getVariable().isEmpty() ? propertyName : entity.getVariable();

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(variable);
	coverageDescription.setAttributeDescription(propertyName);
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.ENHYDRIS.getCommonURN() + ":" + entity.getString(EnhydrisClient.VARIABLE_ID));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param instance
     * @param handler
     */
    private void addExtensions(EnhydrisEntity entity, Optional<EnhydrisInstance> instance, ExtensionHandler handler) {

	String unit = entity.getUnit();
	if (!unit.isEmpty() && !unit.equals("-")) {
	    handler.setAttributeUnitsAbbreviation(unit);
	    String unitName = entity.getString(EnhydrisClient.UNIT_NAME);
	    handler.setAttributeUnits(unitName.isEmpty() ? unit : unitName);
	}

	instance.ifPresent(i -> handler.setCountry(i.getCountry()));

	getResolution(entity.getTimeStep()).ifPresent(handler::setTimeResolutionDuration8601);

	InterpolationType interpolation = getInterpolation(entity);
	if (interpolation != null) {
	    handler.setTimeInterpolation(interpolation);
	}
    }

    /**
     * @param endpoint
     * @return
     */
    private static String getHost(String endpoint) {

	try {
	    String host = URI.create(endpoint).getHost();
	    return host == null ? endpoint : host;
	} catch (Exception e) {
	    return endpoint;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private EnhydrisEntity getEntity(OriginalMetadata originalMD) {

	return new EnhydrisEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return ENHYDRIS_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	EnhydrisEntity entity = getEntity(resource.getOriginalMetadata());
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
