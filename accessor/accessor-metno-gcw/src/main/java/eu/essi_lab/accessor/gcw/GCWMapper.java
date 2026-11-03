package eu.essi_lab.accessor.gcw;

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
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
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
 * Maps a time series harvested by {@link GCWConnector}: the descriptive metadata (title, abstract, contacts, license,
 * keywords) come from the ISO record of the GCW catalogue, the station location, time extent, units and aggregation from
 * the OPeNDAP dataset
 *
 * @author boldrini
 */
public class GCWMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String GCW_SCHEMA = "https://gcw.met.no/metsis/timeseries/schema";

    private static final Pattern SECONDS_RESOLUTION = Pattern.compile("^P(\\d+)S$");

    /**
     * Variables whose values are codes of a WMO code table
     */
    private static final Pattern CODE_TABLE = Pattern.compile("(?i).*\\bcode table\\b.*");

    private static final Pattern CELL_METHOD_TIME = Pattern.compile("time\\s*:\\s*(\\w+)");

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject metadata = new JSONObject(originalMD.getMetadata());
	JSONObject record = metadata.getJSONObject(GCWConnector.RECORD_CATALOGUE);
	JSONObject dataset = metadata.optJSONObject(GCWConnector.RECORD_DATASET, new JSONObject());
	JSONObject station = metadata.optJSONObject(GCWConnector.RECORD_STATION, new JSONObject());
	JSONObject series = metadata.getJSONObject(GCWConnector.RECORD_SERIES);

	String variable = series.getString(GCWTimeSeriesDataset.SERIES_VARIABLE);
	Integer levelIndex = series.has(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX) ? series.getInt(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX) : null;
	String seriesName = GCWOPeNDAP.getSeriesName(variable, levelIndex);
	String standardName = series.optString("standard_name", null);
	String name = getName(series);
	String units = normalizeUnits(series.optString("units", ""));
	String level = getLevelLabel(series);
	String datasetTitle = record.optString(GCWClient.TITLE, dataset.optString("title", record.getString(GCWClient.ID)));

	Dataset resource = new Dataset();
	resource.setSource(source);

	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.getMIMetadata().setParentIdentifier(decorateIdentifier(source.getEndpoint(), record.getString(GCWClient.ID)));

	coreMetadata.setTitle(name + (level.isEmpty() ? "" : " " + level) + " - " + datasetTitle);

	String abstract_ = "Time series of " + name + (units.isEmpty() ? "" : " (" + units + ")") + (level.isEmpty() ? "" : " " + level)
		+ " from the dataset \"" + datasetTitle + "\" of the WMO Global Cryosphere Watch (GCW) Data Portal.";
	String description = record.optString(GCWClient.ABSTRACT, dataset.optString("summary", ""));
	if (!description.isEmpty()) {
	    abstract_ += "\n" + description;
	}
	coreMetadata.setAbstract(abstract_);

	DataIdentification dataId = coreMetadata.getDataIdentification();

	JSONArray contacts = record.optJSONArray(GCWClient.CONTACTS, new JSONArray());
	for (int i = 0; i < contacts.length(); i++) {
	    JSONObject contact = contacts.getJSONObject(i);
	    ResponsibleParty party = new ResponsibleParty();
	    if (contact.has(GCWClient.CONTACT_INDIVIDUAL)) {
		party.setIndividualName(contact.getString(GCWClient.CONTACT_INDIVIDUAL));
	    }
	    if (contact.has(GCWClient.CONTACT_ORGANISATION)) {
		party.setOrganisationName(contact.getString(GCWClient.CONTACT_ORGANISATION));
	    }
	    party.setRoleCode(contact.optString(GCWClient.CONTACT_ROLE, "pointOfContact"));
	    if (contact.has(GCWClient.CONTACT_EMAIL)) {
		party.setContactInfo(createContact(contact.getString(GCWClient.CONTACT_EMAIL)));
	    }
	    dataId.addPointOfContact(party);
	}

	if (record.has(GCWClient.DISTRIBUTOR)) {
	    ResponsibleParty distributor = new ResponsibleParty();
	    distributor.setOrganisationName(record.getString(GCWClient.DISTRIBUTOR));
	    distributor.setRoleCode("distributor");
	    dataId.addPointOfContact(distributor);
	}

	JSONArray constraints = record.optJSONArray(GCWClient.CONSTRAINTS, new JSONArray());
	if (!constraints.isEmpty() || dataset.has("license")) {
	    LegalConstraints legalConstraints = new LegalConstraints();
	    for (int i = 0; i < constraints.length(); i++) {
		legalConstraints.addUseLimitation(constraints.getString(i));
	    }
	    if (constraints.isEmpty()) {
		legalConstraints.addUseLimitation(dataset.getString("license"));
	    }
	    dataId.addLegalConstraints(legalConstraints);
	}

	if (dataset.has("references")) {
	    dataId.setSupplementalInformation("Please cite: " + dataset.getString("references"));
	}

	Keywords keywords = new Keywords();
	JSONArray recordKeywords = record.optJSONArray(GCWClient.KEYWORDS, new JSONArray());
	for (int i = 0; i < recordKeywords.length(); i++) {
	    addKeyword(keywords, recordKeywords.getString(i));
	}
	addKeyword(keywords, name);
	addKeyword(keywords, standardName);
	addKeyword(keywords, "Global Cryosphere Watch");
	addKeyword(keywords, "Cryosphere");
	dataId.addKeywords(keywords);

	if (metadata.has(GCWConnector.RECORD_BEGIN) && metadata.has(GCWConnector.RECORD_END)) {
	    coreMetadata.addTemporalExtent(//
		    ISO8601DateTimeUtils.getISO8601DateTime(new Date(metadata.getLong(GCWConnector.RECORD_BEGIN))), //
		    ISO8601DateTimeUtils.getISO8601DateTime(new Date(metadata.getLong(GCWConnector.RECORD_END))));
	} else if (record.has(GCWClient.BEGIN)) {
	    coreMetadata.addTemporalExtent(record.getString(GCWClient.BEGIN), record.optString(GCWClient.END, "now"));
	}

	Optional<Double> latitude = getDouble(station, GCWTimeSeriesDataset.STATION_LATITUDE);
	Optional<Double> longitude = getDouble(station, GCWTimeSeriesDataset.STATION_LONGITUDE);
	if (latitude.isPresent() && longitude.isPresent() && Math.abs(latitude.get()) <= 90 && Math.abs(longitude.get()) <= 360) {
	    BigDecimal lat = BigDecimal.valueOf(latitude.get());
	    BigDecimal lon = BigDecimal.valueOf(longitude.get() > 180 ? longitude.get() - 360 : longitude.get());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	} else if (record.has(GCWClient.NORTH) && record.has(GCWClient.WEST) && record.has(GCWClient.SOUTH) && record.has(GCWClient.EAST)) {
	    coreMetadata.addBoundingBox(//
		    new BigDecimal(record.getString(GCWClient.NORTH)), //
		    new BigDecimal(record.getString(GCWClient.WEST)), //
		    new BigDecimal(record.getString(GCWClient.SOUTH)), //
		    new BigDecimal(record.getString(GCWClient.EAST)));
	}

	getDouble(station, GCWTimeSeriesDataset.STATION_ALTITUDE).ifPresent(altitude -> {
	    VerticalExtent verticalExtent = new VerticalExtent();
	    verticalExtent.setMinimumValue(altitude);
	    verticalExtent.setMaximumValue(altitude);
	    dataId.addVerticalExtent(verticalExtent);
	});

	String platformId = station.optString(GCWTimeSeriesDataset.STATION_ID, record.getString(GCWClient.ID));
	MIPlatform platform = new MIPlatform();
	Citation citation = new Citation();
	citation.setTitle(station.has(GCWTimeSeriesDataset.STATION_ID) ? platformId + " (" + datasetTitle + ")" : datasetTitle);
	platform.setCitation(citation);
	platform.setDescription(datasetTitle);
	platform.setMDIdentifierCode(NetProtocolWrapper.GCW.getCommonURN() + "/" + platformId);
	coreMetadata.getMIMetadata().addMIPlatform(platform);

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.GCW.getCommonURN() + ":" + (standardName != null ? standardName : variable));
	coverageDescription.setAttributeTitle(name);
	coverageDescription.setAttributeDescription(name + (level.isEmpty() ? "" : " " + level) + (units.isEmpty() ? "" : ", " + units));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	ExtensionHandler handler = resource.getExtensionHandler();
	handler.setAttributeUnits(units);
	handler.setAttributeUnitsAbbreviation(units);

	if (CODE_TABLE.matcher(name).matches()) {
	    // e.g. the SYNOP present weather and cloud type codes
	    handler.setTimeInterpolation(InterpolationType.CATEGORICAL);
	} else {
	    getInterpolation(series.optString("cell_methods", null)).ifPresent(handler::setTimeInterpolation);
	}

	String resolution = normalizeResolution(dataset.optString("time_coverage_resolution", null));
	if (resolution != null) {
	    handler.setTimeResolutionDuration8601(resolution);
	}

	coreMetadata.addDistributionOnlineResource(//
		seriesName, //
		record.getString(GCWClient.OPENDAP), //
		NetProtocolWrapper.GCW.getCommonURN(), //
		"download");

	if (record.has(GCWClient.LANDING_PAGE)) {
	    coreMetadata.addDistributionOnlineResource(//
		    "Dataset landing page", //
		    record.getString(GCWClient.LANDING_PAGE), //
		    NetProtocolWrapper.HTTP.getCommonURN(), //
		    "information");
	}

	return resource;
    }

    /**
     * @param series
     * @return the long name, or the standard name, or the variable name
     */
    static String getName(JSONObject series) {

	String longName = series.optString("long_name", "");
	if (!longName.isEmpty()) {
	    return longName;
	}
	String standardName = series.optString("standard_name", "");
	if (!standardName.isEmpty()) {
	    return standardName.replace('_', ' ');
	}
	return series.getString(GCWTimeSeriesDataset.SERIES_VARIABLE);
    }

    /**
     * @param series
     * @return e.g. "at 20 cm depth", or an empty string for series without levels
     */
    static String getLevelLabel(JSONObject series) {

	if (!series.has(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX)) {
	    return "";
	}

	String levelName = series.optString(GCWTimeSeriesDataset.SERIES_LEVEL_NAME, "level");
	if (!series.has(GCWTimeSeriesDataset.SERIES_LEVEL_VALUE)) {
	    return "at " + levelName + " " + series.getInt(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX);
	}

	double value = series.getDouble(GCWTimeSeriesDataset.SERIES_LEVEL_VALUE);
	String formatted = value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
	String units = series.optString(GCWTimeSeriesDataset.SERIES_LEVEL_UNITS, "");

	return "at " + formatted + (units.isEmpty() ? "" : " " + units) + " " + levelName;
    }

    /**
     * @param units the units of the NetCDF variable
     * @return
     */
    static String normalizeUnits(String units) {

	switch (units.trim()) {
	case "degrees_C":
	case "degree_C":
	case "degC":
	case "Celsius":
	case "celsius":
	case "C":
	    return "°C";
	case "-":
	case "1":
	    return "";
	default:
	    return units.trim();
	}
    }

    /**
     * @param resolution the ACDD time_coverage_resolution, e.g. "PT30S", "P0DT1H0M0S" or the non standard "P600S"
     * @return a compact ISO 8601 duration (e.g. "PT10M", "PT1H", "P1D"), or null
     */
    static String normalizeResolution(String resolution) {

	if (resolution == null || resolution.trim().isEmpty()) {
	    return null;
	}

	String value = resolution.trim();
	if (value.matches("^P\\d+[YM]$")) {
	    return value;
	}

	Matcher matcher = SECONDS_RESOLUTION.matcher(value);
	if (matcher.matches()) {
	    value = "PT" + matcher.group(1) + "S";
	}

	try {
	    long seconds = Duration.parse(value).getSeconds();
	    if (seconds <= 0) {
		return null;
	    }
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

	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * @param cellMethods the CF cell_methods attribute, e.g. "time: mean"
     * @return
     */
    static Optional<InterpolationType> getInterpolation(String cellMethods) {

	if (cellMethods == null) {
	    return Optional.empty();
	}

	Matcher matcher = CELL_METHOD_TIME.matcher(cellMethods);
	if (!matcher.find()) {
	    return Optional.empty();
	}

	switch (matcher.group(1).toLowerCase()) {
	case "mean":
	    return Optional.of(InterpolationType.AVERAGE);
	case "sum":
	    return Optional.of(InterpolationType.TOTAL);
	case "maximum":
	    return Optional.of(InterpolationType.MAX);
	case "minimum":
	    return Optional.of(InterpolationType.MIN);
	case "point":
	    return Optional.of(InterpolationType.CONTINUOUS);
	default:
	    return Optional.empty();
	}
    }

    /**
     * @param email
     * @return
     */
    private static Contact createContact(String email) {

	Contact contact = new Contact();
	Address address = new Address();
	address.addElectronicMailAddress(email);
	contact.setAddress(address);
	return contact;
    }

    /**
     * @param object
     * @param key
     * @return
     */
    private static Optional<Double> getDouble(JSONObject object, String key) {

	try {
	    return object.has(key) ? Optional.of(Double.parseDouble(object.get(key).toString())) : Optional.empty();
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
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

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return GCW_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject metadata = new JSONObject(resource.getOriginalMetadata().getMetadata());
	JSONObject series = metadata.getJSONObject(GCWConnector.RECORD_SERIES);
	String seriesName = GCWOPeNDAP.getSeriesName(series.getString(GCWTimeSeriesDataset.SERIES_VARIABLE),
		series.has(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX) ? series.getInt(GCWTimeSeriesDataset.SERIES_LEVEL_INDEX) : null);

	return decorateIdentifier(resource.getSource().getEndpoint(),
		metadata.getJSONObject(GCWConnector.RECORD_CATALOGUE).getString(GCWClient.OPENDAP) + "#" + seriesName);
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
