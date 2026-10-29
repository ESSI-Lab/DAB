package eu.essi_lab.accessor.ukeafloodmonitoring;

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
import java.util.ArrayList;
import java.util.List;
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
public class EAFloodMonitoringMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String EA_FLOOD_MONITORING_SCHEMA = "https://environment.data.gov.uk/flood-monitoring/schema";

    static final String LICENSE = "Open Government Licence v3.0 - http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/"
	    + " - This uses Environment Agency flood and river level data from the real-time data API (Beta)."
	    + " Real time data are provisional and not quality controlled.";

    static final String ORGANIZATION = "Environment Agency";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	EAFloodMonitoringEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(EAFloodMonitoringEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationReference = entity.getStationReference();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationReference));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String stationLabel = entity.getStationLabel();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(EAFloodMonitoringClient.BEGIN, null);
	String end = object.optString(EAFloodMonitoringClient.END, null);

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

	coreMetadata.setTitle(stationLabel + " - " + propertyName + " (" + getSeriesDetails(entity) + ")");

	StringBuilder description = new StringBuilder();
	description.append("Real time time series of ").append(getPropertyDescription(entity));
	description.append(" (EA parameter ").append(entity.getString(EAFloodMonitoringClient.PARAMETER_NAME));
	if (!entity.getQualifier().isEmpty()) {
	    description.append(", qualifier ").append(entity.getQualifier());
	}
	description.append(", measure ").append(entity.getSeriesId()).append(")");
	description.append(" at station ").append(stationLabel).append(" (").append(stationReference).append(")");
	description.append(getLocationDescription(entity));
	description.append(", from the Real Time flood-monitoring API (Beta) of the ").append(ORGANIZATION).append(" (England).");
	if (!entity.getString(EAFloodMonitoringClient.DATE_OPENED).isEmpty()) {
	    description.append(" The station was opened on ").append(entity.getString(EAFloodMonitoringClient.DATE_OPENED)).append(".");
	}
	description.append(" The API publishes only the readings of the last " + EAFloodMonitoringClient.WINDOW_DAYS
		+ " days (rolling window); the validated archive of the series is published by the Environment Agency Hydrology API.");

	coreMetadata.setAbstract(description.toString());

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(EAFloodMonitoringClient.LAT) && object.has(EAFloodMonitoringClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(EAFloodMonitoringClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(EAFloodMonitoringClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.EA_FLOOD_MONITORING.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return the English name of the observed property, e.g. "Water level", "Tidal level", "Groundwater level", "Discharge"
     */
    static String getPropertyName(EAFloodMonitoringEntity entity) {

	String parameter = entity.getParameter().toLowerCase();
	String qualifier = entity.getQualifier();
	String lowerQualifier = qualifier.toLowerCase();

	switch (parameter) {
	case "level":

	    switch (lowerQualifier) {
	    case "downstream stage":
		return "Water level (downstream stage)";
	    case "tidal level":
		return "Tidal level";
	    case "groundwater":
		return "Groundwater level";
	    case "reservoir level":
		return "Reservoir level";
	    case "sump level":
		return "Sump level";
	    case "":
	    case "none":
	    case "stage":
	    case "water":
	    case "logged":
	    case "height":
		return "Water level";
	    default:
		// e.g. numbered sensors
		return lowerQualifier.matches("\\d+") ? "Water level (sensor " + qualifier + ")" : "Water level (" + qualifier + ")";
	    }

	case "flow":
	    return lowerQualifier.equals("groundwater") ? "Groundwater flow" : "Discharge";

	case "temperature":

	    switch (lowerQualifier) {
	    case "water":
		return "Water temperature";
	    case "dry bulb":
		return "Air temperature";
	    case "wet bulb":
		return "Wet bulb temperature";
	    default:
		return "Temperature";
	    }

	case "rainfall":
	    return "Precipitation";

	case "wind":

	    switch (lowerQualifier) {
	    case "speed":
		return "Wind speed";
	    case "direction":
		return "Wind direction";
	    default:
		return "Wind";
	    }

	default:
	    String name = entity.getString(EAFloodMonitoringClient.PARAMETER_NAME);
	    return name.isEmpty() ? entity.getParameter() : name;
	}
    }

    /**
     * @param entity
     * @return the property name, with the datum of the levels
     */
    static String getPropertyDescription(EAFloodMonitoringEntity entity) {

	String name = getPropertyName(entity);
	String unit = entity.getUnit();

	if (unit.isEmpty()) {
	    return name;
	}

	return name + " in " + getUnitName(unit);
    }

    /**
     * @param entity
     * @return the unit, the period and the value type, e.g. "mASD, 15 min" or "mAOD, 15 min mean"
     */
    static String getSeriesDetails(EAFloodMonitoringEntity entity) {

	List<String> details = new ArrayList<>();

	if (!entity.getUnit().isEmpty()) {
	    details.add(entity.getUnit());
	}

	String valueType = entity.getValueType();
	String period = entity.getPeriod().map(EAFloodMonitoringMapper::getPeriodLabel).orElse("");

	if (valueType.isEmpty() || valueType.equalsIgnoreCase("instantaneous")) {
	    valueType = "";
	}

	String timing = (period + " " + valueType).trim();
	if (!timing.isEmpty()) {
	    details.add(timing);
	}

	return String.join(", ", details);
    }

    /**
     * @param seconds
     * @return e.g. "15 min", "1 hour"
     */
    static String getPeriodLabel(int seconds) {

	if (seconds % 86400 == 0) {
	    return seconds / 86400 + (seconds == 86400 ? " day" : " days");
	}
	if (seconds % 3600 == 0) {
	    return seconds / 3600 + (seconds == 3600 ? " hour" : " hours");
	}
	if (seconds % 60 == 0) {
	    return seconds / 60 + " min";
	}
	return seconds + " s";
    }

    /**
     * @param seconds
     * @return e.g. "PT15M"
     */
    static String getPeriodDuration(int seconds) {

	if (seconds % 86400 == 0) {
	    return "P" + seconds / 86400 + "D";
	}
	if (seconds % 3600 == 0) {
	    return "PT" + seconds / 3600 + "H";
	}
	if (seconds % 60 == 0) {
	    return "PT" + seconds / 60 + "M";
	}
	return "PT" + seconds + "S";
    }

    /**
     * @param entity
     * @return
     */
    private static String getLocationDescription(EAFloodMonitoringEntity entity) {

	StringBuilder builder = new StringBuilder();

	String river = entity.getString(EAFloodMonitoringClient.RIVER_NAME);
	String catchment = entity.getString(EAFloodMonitoringClient.CATCHMENT_NAME);
	String town = entity.getString(EAFloodMonitoringClient.TOWN);

	if (!river.isEmpty()) {
	    builder.append(", river ").append(river);
	}
	if (!catchment.isEmpty()) {
	    builder.append(", catchment ").append(catchment);
	}
	if (!town.isEmpty()) {
	    builder.append(", ").append(town);
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(EAFloodMonitoringEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationLabel());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getString(EAFloodMonitoringClient.PARAMETER_NAME));
	addKeyword(keywords, entity.getQualifier());
	addKeyword(keywords, entity.getString(EAFloodMonitoringClient.RIVER_NAME));
	addKeyword(keywords, entity.getString(EAFloodMonitoringClient.CATCHMENT_NAME));
	addKeyword(keywords, entity.getString(EAFloodMonitoringClient.TOWN));

	addKeyword(keywords, "England");
	addKeyword(keywords, "United Kingdom");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "Flood monitoring");
	addKeyword(keywords, ORGANIZATION);
    }

    /**
     * @param keywords
     * @param value
     */
    private void addKeyword(Keywords keywords, String value) {

	if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("none") && !value.matches("\\d+")) {
	    keywords.addKeyword(value);
	}
    }

    /**
     * @param entity
     * @param coreMetadata
     */
    private void addPlatform(EAFloodMonitoringEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationLabel = entity.getStationLabel();

	Citation citation = new Citation();
	citation.setTitle(stationLabel);
	platform.setCitation(citation);

	platform.setDescription(
		"Station " + stationLabel + " (" + entity.getStationReference() + ")" + getLocationDescription(entity) + " (England)");
	platform.setMDIdentifierCode(NetProtocolWrapper.EA_FLOOD_MONITORING.getCommonURN() + "/" + entity.getStationReference());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(EAFloodMonitoringEntity entity, String propertyName, CoreMetadata coreMetadata) {

	String identifier = entity.getParameter();
	if (!entity.getQualifier().isEmpty()) {
	    identifier += "-" + entity.getQualifier().toLowerCase().replace(" ", "_");
	}

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.EA_FLOOD_MONITORING.getCommonURN() + ":" + identifier);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(EAFloodMonitoringEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();

	if (!unit.isEmpty()) {
	    handler.setAttributeUnits(getUnitName(unit));
	    handler.setAttributeUnitsAbbreviation(unit);
	}
	handler.setCountry("United Kingdom");

	Optional<String> duration = entity.getPeriod().map(EAFloodMonitoringMapper::getPeriodDuration);
	duration.ifPresent(handler::setTimeResolutionDuration8601);

	switch (entity.getValueType().toLowerCase()) {
	case "instantaneous":
	    handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	    break;
	case "mean":
	    handler.setTimeInterpolation(InterpolationType.AVERAGE);
	    duration.ifPresent(handler::setTimeAggregationDuration8601);
	    break;
	case "total":
	    handler.setTimeInterpolation(InterpolationType.TOTAL_PREC);
	    duration.ifPresent(handler::setTimeAggregationDuration8601);
	    break;
	case "event":
	    handler.setTimeInterpolation(InterpolationType.INSTANT_TOTAL);
	    break;
	default:
	    break;
	}
    }

    /**
     * @param unit the unit name used by the API
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "m":
	    return "metres";
	case "mASD":
	    return "metres above stage datum";
	case "mAOD":
	    return "metres above Ordnance Datum (Newlyn)";
	case "mBDAT":
	    return "metres below datum";
	case "mm":
	    return "millimetres";
	case "m3/s":
	    return "cubic metres per second";
	case "Ml/d":
	    return "megalitres per day";
	case "deg C":
	    return "degrees Celsius";
	case "m/s":
	    return "metres per second";
	case "deg":
	    return "degrees";
	default:
	    return unit;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private EAFloodMonitoringEntity getEntity(OriginalMetadata originalMD) {

	return new EAFloodMonitoringEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return EA_FLOOD_MONITORING_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	EAFloodMonitoringEntity entity = getEntity(resource.getOriginalMetadata());
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
