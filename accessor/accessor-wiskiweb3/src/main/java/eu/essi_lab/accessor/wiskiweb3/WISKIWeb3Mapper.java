package eu.essi_lab.accessor.wiskiweb3;

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
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
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
public class WISKIWeb3Mapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String WISKIWEB3_SCHEMA = "urn:kisters:wiskiweb3:schema";

    /**
     * Used when the organisation is not given by the settings nor by the web application
     */
    static final String DEFAULT_ORGANISATION = "WISKI-Web publisher";

    /**
     * Series of the year file shorter than this are described as a possible rolling window of one year
     */
    private static final long ONE_YEAR_WINDOW = TimeUnit.DAYS.toMillis(370);

    private static final Pattern MINUTES_PATTERN = Pattern.compile("^(\\d+)m\\.");
    private static final Pattern HOURS_PATTERN = Pattern.compile("^(\\d*)h\\.");

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	WISKIWeb3Entity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(WISKIWeb3Entity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationPath = entity.getStationPath();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationPath.substring(0, stationPath.lastIndexOf('/'))));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String stationName = entity.getStationName();
	String propertyName = getPropertyName(entity);
	String organisation = getOrganisation(entity);
	String begin = object.optString(WISKIWeb3Client.BEGIN, null);
	String end = object.optString(WISKIWeb3Client.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(organisation);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	String responsible = entity.getString(WISKIWeb3Client.BODY_RESPONSIBLE);
	if (!responsible.isEmpty() && !responsible.equals(organisation)) {
	    ResponsibleParty operator = new ResponsibleParty();
	    operator.setOrganisationName(responsible);
	    operator.setRoleCode("custodian");
	    dataId.addPointOfContact(operator);
	}

	String license = entity.getString(WISKIWeb3Client.LICENSE);
	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(license.isEmpty() ? "See the terms of use of " + organisation : license);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, propertyName, keywords);

	coreMetadata.setTitle(stationName + " - " + propertyName + " (" + getSeriesDescription(entity) + ")");

	coreMetadata.setAbstract(getAbstract(entity, propertyName, organisation));

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(WISKIWeb3Client.LAT) && object.has(WISKIWeb3Client.LON)) {
	    try {
		BigDecimal lat = new BigDecimal(object.get(WISKIWeb3Client.LAT).toString());
		BigDecimal lon = new BigDecimal(object.get(WISKIWeb3Client.LON).toString());
		coreMetadata.addBoundingBox(lat, lon, lat, lon);
	    } catch (NumberFormatException e) {
		// no position
	    }
	}

	addPlatform(entity, source, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.WISKIWEB3.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @param propertyName
     * @param organisation
     * @return
     */
    private static String getAbstract(WISKIWeb3Entity entity, String propertyName, String organisation) {

	StringBuilder builder = new StringBuilder();

	builder.append("Time series of ").append(getPropertyDescription(entity, propertyName)).//
		append(" (").append(getSeriesDescription(entity)).append(") at station ").//
		append(entity.getStationName()).append(" (").append(entity.getStationNumber()).append(")").//
		append(getWaterbodyDescription(entity)).//
		append(", published by ").append(organisation).append(" through its KISTERS WISKI-Web site.");

	String responsible = entity.getString(WISKIWeb3Client.BODY_RESPONSIBLE);
	if (!responsible.isEmpty() && !responsible.equals(organisation)) {
	    builder.append(" Station operated by ").append(responsible).append(".");
	}

	if (entity.isRecent()) {

	    builder.append(" Only the most recent values are published (rolling window of about one week);"
		    + " older values are available as daily values.");

	} else if (isOneYearWindow(entity)) {

	    builder.append(" About one year of daily values is published (possibly a rolling window of one year).");

	} else {

	    builder.append(" Daily values of the whole period published by the site.");
	}

	builder.append(" Provisional (unverified) data.");

	return builder.toString();
    }

    /**
     * @param entity
     * @return true if the series of the year file covers about one year
     */
    private static boolean isOneYearWindow(WISKIWeb3Entity entity) {

	Optional<Date> begin = ISO8601DateTimeUtils.parseISO8601ToDate(entity.getString(WISKIWeb3Client.BEGIN));
	Optional<Date> end = ISO8601DateTimeUtils.parseISO8601ToDate(entity.getString(WISKIWeb3Client.END));

	return begin.isPresent() && end.isPresent() && end.get().getTime() - begin.get().getTime() < ONE_YEAR_WINDOW;
    }

    /**
     * @param entity
     * @return
     */
    static String getOrganisation(WISKIWeb3Entity entity) {

	String organisation = entity.getString(WISKIWeb3Client.ORGANISATION);
	return organisation.isEmpty() ? DEFAULT_ORGANISATION : organisation;
    }

    /**
     * @param entity
     * @return the English name of the property, e.g. "Water level", or the name of the provider if not known
     */
    static String getPropertyName(WISKIWeb3Entity entity) {

	String longName = entity.getString(WISKIWeb3Client.PARAMETER_LONG_NAME);
	String name = entity.getString(WISKIWeb3Client.PARAMETER);
	String code = entity.getParameterCode();

	return getEnglishName(longName).//
		or(() -> getEnglishName(name)).//
		or(() -> getEnglishName(code)).//
		orElse(!longName.isEmpty() ? longName : !name.isEmpty() ? name : code);
    }

    /**
     * @param name a German parameter name or code, e.g. "Wasserstand", "W"
     * @return
     */
    static Optional<String> getEnglishName(String name) {

	if (name == null || name.isEmpty()) {
	    return Optional.empty();
	}

	String lower = name.toLowerCase();

	if (lower.contains("quellsch") || lower.startsWith("sch")) {
	    return Optional.of("Spring discharge");
	}
	if (lower.contains("wassertemp") || lower.equals("wt") || lower.equals("tw")) {
	    return Optional.of("Water temperature");
	}
	if (lower.contains("lufttemp") || lower.equals("lt") || lower.equals("ta")) {
	    return Optional.of("Air temperature");
	}
	if (lower.contains("grundwasser") || lower.startsWith("gw")) {
	    return Optional.of("Groundwater level");
	}
	if (lower.contains("wasserstand") || lower.equals("w") || lower.equals("s")) {
	    return Optional.of("Water level");
	}
	if (lower.contains("durchfluss") || lower.contains("abfluss") || lower.equals("q")) {
	    return Optional.of("Discharge");
	}
	if (lower.contains("niederschlag") || lower.equals("n") || lower.equals("p")) {
	    return Optional.of("Precipitation");
	}

	return Optional.empty();
    }

    /**
     * @param entity
     * @param propertyName
     * @return the property name with the name of the provider, the unit and the gauge datum of the water level
     */
    static String getPropertyDescription(WISKIWeb3Entity entity, String propertyName) {

	StringBuilder builder = new StringBuilder(propertyName);

	String longName = entity.getString(WISKIWeb3Client.PARAMETER_LONG_NAME);
	if (longName.isEmpty()) {
	    longName = entity.getString(WISKIWeb3Client.PARAMETER);
	}
	if (!longName.isEmpty() && !longName.equals(propertyName)) {
	    builder.append(" - ").append(longName);
	}

	String unit = entity.getUnit();
	if (!unit.isEmpty()) {
	    builder.append(" [").append(unit).append("]");
	}

	String datum = entity.getString(WISKIWeb3Client.GAUGE_DATUM);
	if (propertyName.equals("Water level") && !datum.isEmpty()) {
	    builder.append(" (gauge datum ").append(datum).append(" ").append(entity.getString(WISKIWeb3Client.GAUGE_DATUM_POSTFIX))
		    .append(")");
	}

	return builder.toString().trim().replaceAll("\\s+\\)", ")");
    }

    /**
     * @param entity
     * @return e.g. "15 minutes values" or "daily mean values"
     */
    static String getSeriesDescription(WISKIWeb3Entity entity) {

	String ts = entity.getTsShortName();

	if (ts.startsWith("Day.Mean")) {
	    return "daily mean values";
	}
	if (ts.startsWith("Day.Min")) {
	    return "daily minimum values";
	}
	if (ts.startsWith("Day.Max")) {
	    return "daily maximum values";
	}
	if (ts.startsWith("Day.Total")) {
	    return "daily totals";
	}

	Optional<String> resolution = getResolution(entity);
	if (resolution.isPresent()) {
	    switch (resolution.get()) {
	    case "PT1H":
		return isTotal(ts) ? "hourly totals" : "hourly values";
	    case "P1D":
		return "daily values";
	    default:
		return resolution.get().replace("PT", "").replace("M", " minutes").replace("H", " hours") + " values";
	    }
	}

	return entity.isRecent() ? "recent values" : "daily values";
    }

    /**
     * @param entity
     * @return the time resolution as ISO 8601 duration, from the ts_shortname (e.g. "PT15M" from "15m.Cmd.RelAbs.P")
     */
    static Optional<String> getResolution(WISKIWeb3Entity entity) {

	String ts = entity.getTsShortName();

	Matcher minutes = MINUTES_PATTERN.matcher(ts);
	if (minutes.find()) {
	    return Optional.of("PT" + minutes.group(1) + "M");
	}

	Matcher hours = HOURS_PATTERN.matcher(ts);
	if (hours.find()) {
	    return Optional.of("PT" + (hours.group(1).isEmpty() ? "1" : hours.group(1)) + "H");
	}

	if (ts.startsWith("Day.")) {
	    return Optional.of("P1D");
	}

	return Optional.empty();
    }

    /**
     * @param ts
     * @return
     */
    private static boolean isTotal(String ts) {

	return ts.contains("Total") || ts.contains("Sum");
    }

    /**
     * @param entity
     * @return
     */
    private static String getWaterbodyDescription(WISKIWeb3Entity entity) {

	String waterbody = entity.getString(WISKIWeb3Client.WATERBODY);
	if (waterbody.isEmpty()) {
	    waterbody = entity.getString(WISKIWeb3Client.RIVER);
	}
	String catchment = entity.getString(WISKIWeb3Client.CATCHMENT);

	StringBuilder builder = new StringBuilder();
	if (!waterbody.isEmpty()) {
	    builder.append(", waterbody ").append(waterbody);
	}
	if (!catchment.isEmpty()) {
	    builder.append(", catchment ").append(catchment);
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(WISKIWeb3Entity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getString(WISKIWeb3Client.PARAMETER_LONG_NAME));
	addKeyword(keywords, entity.getString(WISKIWeb3Client.PARAMETER));
	addKeyword(keywords, entity.getString(WISKIWeb3Client.WATERBODY));
	addKeyword(keywords, entity.getString(WISKIWeb3Client.CATCHMENT));
	addKeyword(keywords, entity.getString(WISKIWeb3Client.HYDRO_UNIT));
	addKeyword(keywords, entity.getString(WISKIWeb3Client.COUNTRY));

	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "WISKI");
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
    private void addPlatform(WISKIWeb3Entity entity, GSSource source, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();
	String longName = entity.getString(WISKIWeb3Client.STATION_LONG_NAME);

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	String country = entity.getString(WISKIWeb3Client.COUNTRY);
	platform.setDescription("Station " + (longName.isEmpty() ? stationName : longName) + " (" + entity.getStationNumber() + ")"
		+ getWaterbodyDescription(entity) + (country.isEmpty() ? "" : " (" + country + ")"));
	platform.setMDIdentifierCode(
		NetProtocolWrapper.WISKIWEB3.getCommonURN() + ":" + getHost(source.getEndpoint()) + ":" + entity.getStationNumber());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param endpoint
     * @return
     */
    private static String getHost(String endpoint) {

	try {
	    String host = URI.create(endpoint.trim()).getHost();
	    return host == null ? "" : host;
	} catch (Exception e) {
	    return "";
	}
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(WISKIWeb3Entity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity, propertyName));
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.WISKIWEB3.getCommonURN() + ":" + entity.getParameterCode());
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(WISKIWeb3Entity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();
	if (!unit.isEmpty()) {
	    handler.setAttributeUnits(getUnitName(unit));
	    handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit));
	}

	String country = entity.getString(WISKIWeb3Client.COUNTRY);
	if (!country.isEmpty()) {
	    handler.setCountry(country);
	}

	Optional<String> resolution = getResolution(entity);
	resolution.ifPresent(handler::setTimeResolutionDuration8601);

	String ts = entity.getTsShortName();

	if (ts.startsWith("Day.Mean")) {
	    handler.setTimeInterpolation(InterpolationType.AVERAGE_SUCC);
	    handler.setTimeAggregationDuration8601("P1D");
	} else if (ts.startsWith("Day.Min")) {
	    handler.setTimeInterpolation(InterpolationType.MIN_SUCC);
	    handler.setTimeAggregationDuration8601("P1D");
	} else if (ts.startsWith("Day.Max")) {
	    handler.setTimeInterpolation(InterpolationType.MAX_SUCC);
	    handler.setTimeAggregationDuration8601("P1D");
	} else if (ts.startsWith("Day.Total")) {
	    handler.setTimeInterpolation(InterpolationType.TOTAL_SUCC);
	    handler.setTimeAggregationDuration8601("P1D");
	} else if (isTotal(ts)) {
	    handler.setTimeInterpolation(InterpolationType.TOTAL_PREC);
	    resolution.ifPresent(handler::setTimeAggregationDuration8601);
	} else {
	    handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	}
    }

    /**
     * @param unit the unit symbol of the site
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "cm":
	    return "centimetres";
	case "m":
	    return "metres";
	case "m³/s":
	case "m3/s":
	    return "cubic metres per second";
	case "l/s":
	    return "litres per second";
	case "°C":
	    return "degrees Celsius";
	case "mm":
	    return "millimetres";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the unit symbol of the site
     * @return
     */
    static String getUnitAbbreviation(String unit) {

	return unit.replace("³", "3");
    }

    /**
     * @param originalMD
     * @return
     */
    private WISKIWeb3Entity getEntity(OriginalMetadata originalMD) {

	return new WISKIWeb3Entity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return WISKIWEB3_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	WISKIWeb3Entity entity = getEntity(resource.getOriginalMetadata());
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
