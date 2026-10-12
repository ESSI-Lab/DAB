package eu.essi_lab.accessor.usnrcsawdb;

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
import java.util.Date;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Citation;
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
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * @author boldrini
 */
public class NRCSAWDBMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String AWDB_SCHEMA = "https://wcc.sc.egov.usda.gov/awdbRestApi/schema";

    private static final String NRCS_ORGANIZATION = "USDA Natural Resources Conservation Service (NRCS), National Water and Climate Center";

    private static final String NRCS_LICENSE = "Public domain (U.S. Government work). Please cite: USDA Natural Resources Conservation Service, National Water and Climate Center";

    private static final double FEET_TO_METERS = 0.3048;

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject object = new JSONObject(originalMD.getMetadata());
	JSONObject stationElement = object.getJSONObject(NRCSAWDBClient.RECORD_STATION_ELEMENT);
	JSONObject element = object.optJSONObject(NRCSAWDBClient.RECORD_ELEMENT);
	if (element == null) {
	    element = new JSONObject();
	}

	String triplet = object.getString(NRCSAWDBClient.STATION_TRIPLET);
	String timeseriesId = object.getString(NRCSAWDBClient.RECORD_TIMESERIES_ID);
	String stationName = object.optString(NRCSAWDBClient.STATION_NAME, triplet);
	String stateCode = object.optString(NRCSAWDBClient.STATION_STATE_CODE, "");
	String networkName = object.optString(NRCSAWDBClient.RECORD_NETWORK_NAME, object.optString(NRCSAWDBClient.STATION_NETWORK_CODE));
	double timeZone = object.optDouble(NRCSAWDBClient.STATION_DATA_TIME_ZONE, 0);

	String elementCode = stationElement.getString(NRCSAWDBClient.ELEMENT_CODE);
	String duration = stationElement.optString(NRCSAWDBClient.ELEMENT_DURATION);
	int ordinal = stationElement.optInt(NRCSAWDBClient.ELEMENT_ORDINAL, 1);
	Object heightDepth = stationElement.opt(NRCSAWDBClient.ELEMENT_HEIGHT_DEPTH);

	String units = NRCSAWDBUnits.getTargetUnit(//
		stationElement.optString(NRCSAWDBClient.ELEMENT_STORED_UNIT, null), //
		element.optString(NRCSAWDBClient.REFERENCE_METRIC_UNIT, null));

	String variableName = capitalize(element.optString(NRCSAWDBClient.REFERENCE_NAME, elementCode));
	String variableTitle = variableName + getHeightDepthLabel(heightDepth) + (ordinal > 1 ? " (sensor " + ordinal + ")" : "");
	String stationLabel = networkName + " station " + stationName + ", " + stateCode + " (" + triplet + ")";

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.getMIMetadata().setParentIdentifier(decorateIdentifier(source.getEndpoint(), triplet));

	coreMetadata.setTitle(variableTitle + " (" + duration.toLowerCase() + ") at " + stationLabel);

	String description = element.optString(NRCSAWDBClient.REFERENCE_DESCRIPTION, variableName);
	coreMetadata.setAbstract("Timeseries of " + duration.toLowerCase() + " " + variableTitle.toLowerCase() + " (" + description
		+ ", units: " + units + ") measured at " + stationLabel + ", published by the " + NRCS_ORGANIZATION
		+ " Air and Water Database (AWDB).");

	DataIdentification dataId = coreMetadata.getDataIdentification();

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(NRCS_ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	String operator = object.optString(NRCSAWDBClient.STATION_OPERATOR, null);
	if (operator != null && !operator.isEmpty() && !operator.equals("NRCS")) {
	    ResponsibleParty operatorParty = new ResponsibleParty();
	    operatorParty.setOrganisationName(operator);
	    operatorParty.setRoleCode("originator");
	    dataId.addPointOfContact(operatorParty);
	}

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(NRCS_LICENSE);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	addKeyword(keywords, stationName);
	addKeyword(keywords, variableName);
	addKeyword(keywords, networkName);
	addKeyword(keywords, object.optString(NRCSAWDBClient.STATION_COUNTY_NAME, null));
	addKeyword(keywords, stateCode);
	addKeyword(keywords, "NRCS");
	addKeyword(keywords, "Snow");
	addKeyword(keywords, "Cryosphere");
	dataId.addKeywords(keywords);

	addTemporalExtent(coreMetadata, stationElement, timeZone);

	if (object.has(NRCSAWDBClient.STATION_LATITUDE) && object.has(NRCSAWDBClient.STATION_LONGITUDE)) {
	    BigDecimal lat = new BigDecimal(object.get(NRCSAWDBClient.STATION_LATITUDE).toString());
	    BigDecimal lon = new BigDecimal(object.get(NRCSAWDBClient.STATION_LONGITUDE).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	if (object.has(NRCSAWDBClient.STATION_ELEVATION) && !object.isNull(NRCSAWDBClient.STATION_ELEVATION)) {
	    // AWDB elevations are in feet
	    double elevation = Math.round(object.getDouble(NRCSAWDBClient.STATION_ELEVATION) * FEET_TO_METERS * 10) / 10.0;
	    VerticalExtent verticalExtent = new VerticalExtent();
	    verticalExtent.setMinimumValue(elevation);
	    verticalExtent.setMaximumValue(elevation);
	    dataId.addVerticalExtent(verticalExtent);
	}

	MIPlatform platform = new MIPlatform();
	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);
	platform.setDescription(stationLabel);
	platform.setMDIdentifierCode(NetProtocolWrapper.NRCS_AWDB.getCommonURN() + "/" + triplet);
	coreMetadata.getMIMetadata().addMIPlatform(platform);

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.NRCS_AWDB.getCommonURN() + ":" + elementCode);
	coverageDescription.setAttributeTitle(variableTitle);
	coverageDescription.setAttributeDescription(description + ", " + units);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	ExtensionHandler handler = dataset.getExtensionHandler();
	handler.setAttributeUnits(units);
	handler.setAttributeUnitsAbbreviation(units);
	// AWDB networks also include Canadian stations
	Country country = Country.decode(object.optString(NRCSAWDBClient.RECORD_COUNTRY_CODE, "US"));
	if (country != null) {
	    handler.setCountry(country.getShortName());
	}

	InterpolationType interpolation = getInterpolation(element.optString(NRCSAWDBClient.REFERENCE_FUNCTION_CODE));
	handler.setTimeInterpolation(interpolation);

	String isoDuration = getISODuration(duration);
	if (isoDuration != null) {
	    handler.setTimeResolutionDuration8601(isoDuration);
	    if (interpolation != InterpolationType.CONTINUOUS) {
		handler.setTimeAggregationDuration8601(isoDuration);
	    }
	}

	coreMetadata.addDistributionOnlineResource(//
		timeseriesId, //
		source.getEndpoint(), //
		NetProtocolWrapper.NRCS_AWDB.getCommonURN(), //
		"download");

	return dataset;
    }

    /**
     * @param coreMetadata
     * @param stationElement
     * @param timeZone
     */
    private void addTemporalExtent(CoreMetadata coreMetadata, JSONObject stationElement, double timeZone) {

	Optional<Date> begin = NRCSAWDBClient.parseApiDate(stationElement.optString(NRCSAWDBClient.ELEMENT_BEGIN_DATE), timeZone);
	Optional<Date> end = NRCSAWDBClient.parseApiDate(stationElement.optString(NRCSAWDBClient.ELEMENT_END_DATE), timeZone);

	if (begin.isEmpty()) {
	    return;
	}

	Date now = new Date();
	// active station elements have end date 2100-01-01
	Date endDate = end.isEmpty() || end.get().after(now) ? now : end.get();

	coreMetadata.addTemporalExtent(//
		ISO8601DateTimeUtils.getISO8601DateTime(begin.get()), //
		ISO8601DateTimeUtils.getISO8601DateTime(endDate));
    }

    /**
     * @param functionCode the AWDB function code of the element
     * @return
     */
    static InterpolationType getInterpolation(String functionCode) {

	switch (functionCode == null ? "" : functionCode) {
	case "V":
	    return InterpolationType.AVERAGE;
	case "X":
	    return InterpolationType.MAX;
	case "N":
	    return InterpolationType.MIN;
	case "D":
	case "S":
	    return InterpolationType.TOTAL;
	case "Z":
	    return InterpolationType.STATISTICAL;
	case "C":
	default:
	    return InterpolationType.CONTINUOUS;
	}
    }

    /**
     * @param duration
     * @return
     */
    static String getISODuration(String duration) {

	switch (duration == null ? "" : duration) {
	case "HOURLY":
	    return "PT1H";
	case "DAILY":
	    return "P1D";
	case "SEMIMONTHLY":
	    return "P15D";
	case "MONTHLY":
	    return "P1M";
	case "WATER_YEAR":
	case "CALENDAR_YEAR":
	    return "P1Y";
	default:
	    return null;
	}
    }

    /**
     * @param heightDepth in inches, negative for depths
     * @return
     */
    private String getHeightDepthLabel(Object heightDepth) {

	if (heightDepth == null || heightDepth == JSONObject.NULL) {
	    return "";
	}

	try {
	    double value = Double.parseDouble(heightDepth.toString());
	    long cm = Math.round(Math.abs(value) * 2.54);
	    return value < 0 ? " at " + cm + " cm depth" : " at " + cm + " cm height";
	} catch (NumberFormatException e) {
	    return "";
	}
    }

    /**
     * @param name e.g. "SNOW WATER EQUIVALENT"
     * @return e.g. "Snow water equivalent"
     */
    static String capitalize(String name) {

	if (name == null || name.isEmpty()) {
	    return name;
	}

	String lower = name.trim().toLowerCase();
	return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
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

	return AWDB_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject object = new JSONObject(resource.getOriginalMetadata().getMetadata());
	return decorateIdentifier(resource.getSource().getEndpoint(), object.optString(NRCSAWDBClient.RECORD_TIMESERIES_ID, null));
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
