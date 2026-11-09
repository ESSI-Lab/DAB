package eu.essi_lab.accessor.canswe;

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
public class CanSWEMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String CANSWE_SCHEMA = "https://zenodo.org/records/10835278/CanSWE/schema";

    /**
     * Separator of the parts of the online resource name: variable and station id
     */
    static final String NAME_SEPARATOR = ";";

    private static final String PUBLISHER = "Environment and Climate Change Canada (ECCC)";

    private static final String LICENSE = "Open Government Licence - Canada (https://open.canada.ca/en/open-government-licence-canada)";

    private static final String HYDRO_QUEBEC_LICENSE = "Hydro-Quebec data are available under the terms of the Creative Commons Attribution - Non Commercial - Share A Like 4.0 International License (https://creativecommons.org/licenses/by-nc-sa/4.0/)";

    private static final String CITATION = "Vionnet, V., Mortimer, C., Brady, M., Arnal, L., and Brown, R.: Canadian historical Snow Water Equivalent dataset (CanSWE, 1928-2020), Earth Syst. Sci. Data, 13, 4603-4619, https://doi.org/10.5194/essd-13-4603-2021, 2021.";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject record = new JSONObject(originalMD.getMetadata());
	JSONObject station = record.getJSONObject(CanSWEConnector.RECORD_STATION);
	JSONObject variable = record.getJSONObject(CanSWEConnector.RECORD_VARIABLE);

	String stationId = station.getString(CanSWEConnector.RECORD_STATION_ID);
	String stationName = station.optString("station_name", stationId);
	String provider = station.optString("source", null);
	String method = station.optString("type_mes_label", null);
	String variableName = variable.getString(CanSWEConnector.RECORD_VARIABLE_NAME);
	String longName = getVariableLabel(variableName, variable.optString("long_name", variableName));
	String units = normalizeUnits(variable.optString("units", ""));
	String stationLabel = stationName + " (" + stationId + ")";

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.getMIMetadata().setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationId));

	coreMetadata.setTitle(longName + " at " + stationLabel);

	StringBuilder abstract_ = new StringBuilder();
	abstract_.append("Timeseries of ").append(longName.toLowerCase());
	if (!units.isEmpty()) {
	    abstract_.append(" (").append(units).append(")");
	}
	abstract_.append(" at the station ").append(stationLabel);
	if (provider != null) {
	    abstract_.append(", data provided by ").append(provider);
	}
	abstract_.append(", from the Canadian historical Snow Water Equivalent dataset (CanSWE).");
	if (method != null) {
	    abstract_.append(" Measurement method: ").append(method.toLowerCase()).append(".");
	}
	String alternativeIds = join(station.optString("station_id_sec", null), station.optString("station_id_ter", null));
	if (!alternativeIds.isEmpty()) {
	    abstract_.append(" Alternative station ids: ").append(alternativeIds).append(".");
	}
	if (variable.has("description")) {
	    abstract_.append("\n").append(variable.getString("description"));
	}
	coreMetadata.setAbstract(abstract_.toString());

	DataIdentification dataId = coreMetadata.getDataIdentification();

	ResponsibleParty publisher = new ResponsibleParty();
	publisher.setOrganisationName(PUBLISHER);
	publisher.setRoleCode("publisher");
	dataId.addPointOfContact(publisher);

	if (provider != null) {
	    ResponsibleParty originator = new ResponsibleParty();
	    originator.setOrganisationName(provider);
	    originator.setRoleCode("originator");
	    dataId.addPointOfContact(originator);
	}

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(LICENSE);
	if (provider != null && provider.toLowerCase().startsWith("hydro-qu")) {
	    constraints.addUseLimitation(HYDRO_QUEBEC_LICENSE);
	}
	dataId.addLegalConstraints(constraints);

	dataId.setSupplementalInformation("Please cite: " + CITATION + " Dataset: " + source.getEndpoint());

	Keywords keywords = new Keywords();
	addKeyword(keywords, stationId);
	addKeyword(keywords, stationName);
	addKeyword(keywords, station.optString("station_name_sec", null));
	addKeyword(keywords, station.optString("station_name_ter", null));
	addKeyword(keywords, station.optString("station_id_sec", null));
	addKeyword(keywords, station.optString("station_id_ter", null));
	addKeyword(keywords, provider);
	addKeyword(keywords, method);
	addKeyword(keywords, longName);
	addKeyword(keywords, variable.optString("standard_name", null));
	addKeyword(keywords, "CanSWE");
	addKeyword(keywords, "Snow");
	addKeyword(keywords, "Snow Water Equivalent");
	addKeyword(keywords, "Snow Depth");
	addKeyword(keywords, "Bulk Snow Density");
	addKeyword(keywords, "Cryosphere");
	addKeyword(keywords, "Canada");
	dataId.addKeywords(keywords);

	if (record.has(CanSWEConnector.RECORD_BEGIN) && record.has(CanSWEConnector.RECORD_END)) {
	    coreMetadata.addTemporalExtent(//
		    ISO8601DateTimeUtils.getISO8601DateTime(new Date(record.getLong(CanSWEConnector.RECORD_BEGIN))), //
		    ISO8601DateTimeUtils.getISO8601DateTime(new Date(record.getLong(CanSWEConnector.RECORD_END))));
	}

	if (station.has(CanSWEConnector.RECORD_LATITUDE) && station.has(CanSWEConnector.RECORD_LONGITUDE)) {
	    BigDecimal lat = new BigDecimal(station.getString(CanSWEConnector.RECORD_LATITUDE));
	    BigDecimal lon = new BigDecimal(station.getString(CanSWEConnector.RECORD_LONGITUDE));
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	if (station.has("elevation")) {
	    VerticalExtent verticalExtent = new VerticalExtent();
	    verticalExtent.setMinimumValue(station.getDouble("elevation"));
	    verticalExtent.setMaximumValue(station.getDouble("elevation"));
	    dataId.addVerticalExtent(verticalExtent);
	}

	MIPlatform platform = new MIPlatform();
	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);
	platform.setDescription(stationLabel + (provider == null ? "" : ", " + provider));
	platform.setMDIdentifierCode(NetProtocolWrapper.CANSWE.getCommonURN() + "/" + stationId);
	coreMetadata.getMIMetadata().addMIPlatform(platform);

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.CANSWE.getCommonURN() + ":" + variableName);
	coverageDescription.setAttributeTitle(longName);
	coverageDescription.setAttributeDescription(longName + (units.isEmpty() ? "" : ", " + units));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	ExtensionHandler handler = dataset.getExtensionHandler();
	handler.setAttributeUnits(units);
	handler.setAttributeUnitsAbbreviation(units);
	handler.setCountry(Country.CANADA.getShortName());
	// snow surveys and automatic sensors observations
	handler.setTimeInterpolation(InterpolationType.CONTINUOUS);

	coreMetadata.addDistributionOnlineResource(//
		getOnlineName(variableName, stationId), //
		record.getString(CanSWEConnector.RECORD_FILE_URL), //
		NetProtocolWrapper.CANSWE.getCommonURN(), //
		"download");

	return dataset;
    }

    /**
     * @param variable
     * @param stationId
     * @return the name of the online resource
     */
    public static String getOnlineName(String variable, String stationId) {

	return variable + NAME_SEPARATOR + stationId;
    }

    /**
     * @param name as returned by {@link #getOnlineName(String, String)}
     * @return variable and station id
     */
    public static Optional<String[]> parseOnlineName(String name) {

	if (name == null) {
	    return Optional.empty();
	}

	String[] split = name.split(NAME_SEPARATOR, 2);
	if (split.length != 2 || split[0].isEmpty() || split[1].isEmpty()) {
	    return Optional.empty();
	}

	return Optional.of(split);
    }

    /**
     * @param variable
     * @param longName
     * @return
     */
    static String getVariableLabel(String variable, String longName) {

	switch (variable) {
	case CanSWEDataset.SNW:
	    return "Snow water equivalent";
	case CanSWEDataset.SND:
	    return "Snow depth";
	case CanSWEDataset.DEN:
	    return "Bulk snow density";
	default:
	    return longName;
	}
    }

    /**
     * @param units the CF units of the file
     * @return
     */
    static String normalizeUnits(String units) {

	return units.replace("**", "").trim();
    }

    private static String join(String... values) {

	StringBuilder ret = new StringBuilder();
	for (String value : values) {
	    if (value != null && !value.isEmpty()) {
		ret.append(ret.length() == 0 ? "" : ", ").append(value);
	    }
	}
	return ret.toString();
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

	return CANSWE_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject record = new JSONObject(resource.getOriginalMetadata().getMetadata());
	String name = getOnlineName(//
		record.getJSONObject(CanSWEConnector.RECORD_VARIABLE).getString(CanSWEConnector.RECORD_VARIABLE_NAME), //
		record.getJSONObject(CanSWEConnector.RECORD_STATION).getString(CanSWEConnector.RECORD_STATION_ID));
	return decorateIdentifier(resource.getSource().getEndpoint(), name);
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
