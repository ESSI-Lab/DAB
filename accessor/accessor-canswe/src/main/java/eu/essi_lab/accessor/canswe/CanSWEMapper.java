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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
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
     * Separator of the parts of the online resource name: variable, statistic and site id
     */
    static final String NAME_SEPARATOR = ";";

    private static final String PUBLISHER = "Environment and Climate Change Canada (ECCC)";

    private static final String LICENSE = "Open Government Licence - Canada (https://open.canada.ca/en/open-government-licence-canada)";

    private static final String HYDRO_QUEBEC = "Hydro-Quebec";

    private static final String HYDRO_QUEBEC_LICENSE = "Hydro-Quebec data are available under the terms of the Creative Commons Attribution - Non Commercial - Share A Like 4.0 International License (https://creativecommons.org/licenses/by-nc-sa/4.0/)";

    private static final String CITATION = "Vionnet, V., Mortimer, C., Brady, M., Arnal, L., and Brown, R.: Canadian historical Snow Water Equivalent dataset (CanSWE, 1928-2020), Earth Syst. Sci. Data, 13, 4603-4619, https://doi.org/10.5194/essd-13-4603-2021, 2021.";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject record = new JSONObject(originalMD.getMetadata());
	JSONObject global = record.optJSONObject(CanSWEConnector.RECORD_GLOBAL);
	if (global == null) {
	    global = new JSONObject();
	}
	JSONObject site = record.getJSONObject(CanSWEConnector.RECORD_SITE);
	JSONObject variable = record.getJSONObject(CanSWEConnector.RECORD_VARIABLE);
	JSONArray stations = site.getJSONArray(CanSWEConnector.RECORD_SITE_STATIONS);
	CanSWEStatistic statistic = CanSWEStatistic.valueOf(record.getString(CanSWEConnector.RECORD_STATISTIC));

	String siteId = site.getString(CanSWEConnector.RECORD_SITE_ID);
	String variableName = variable.getString(CanSWEConnector.RECORD_VARIABLE_NAME);
	String longName = getVariableLabel(variableName, variable.optString("long_name", variableName));
	String units = normalizeUnits(variable.optString("units", ""));

	Set<String> names = new LinkedHashSet<>();
	Set<String> ids = new LinkedHashSet<>();
	Set<String> providers = new LinkedHashSet<>();
	Set<String> methods = new LinkedHashSet<>();
	List<Double> elevations = new ArrayList<>();

	for (int i = 0; i < stations.length(); i++) {
	    JSONObject station = stations.getJSONObject(i);
	    ids.add(station.getString("station_id"));
	    if (station.has("station_name")) {
		names.add(station.getString("station_name"));
	    }
	    if (station.has("source")) {
		providers.add(station.getString("source"));
	    }
	    if (station.has("type_mes_label")) {
		methods.add(station.getString("type_mes_label"));
	    }
	    if (station.has("elevation")) {
		elevations.add(station.getDouble("elevation"));
	    }
	}

	String siteName = names.isEmpty() ? siteId : String.join(" / ", names);
	String siteLabel = siteName + " (" + String.join(", ", ids) + ")";
	boolean colocated = stations.length() > 1;

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.getMIMetadata().setParentIdentifier(decorateIdentifier(source.getEndpoint(), siteId));

	String title = longName;
	if (statistic != CanSWEStatistic.VALUE) {
	    title += " (" + statistic.getLabel() + " values)";
	}
	title += " at " + siteLabel;
	coreMetadata.setTitle(title);

	StringBuilder abstract_ = new StringBuilder();
	abstract_.append("Timeseries of ").append(longName.toLowerCase());
	if (!units.isEmpty()) {
	    abstract_.append(" (").append(units).append(")");
	}
	abstract_.append(" at ").append(colocated ? "the co-located stations " : "the station ").append(siteLabel);
	if (!providers.isEmpty()) {
	    abstract_.append(", data provided by ").append(String.join(", ", providers));
	}
	abstract_.append(", from the Canadian historical Snow Water Equivalent dataset (CanSWE).");
	if (!methods.isEmpty()) {
	    abstract_.append(" Measurement method: ").append(String.join(", ", methods).toLowerCase()).append(".");
	}
	if (statistic != CanSWEStatistic.VALUE) {
	    abstract_.append(" At this location more than one value is available at the same time (")
		    .append(record.optLong(CanSWEConnector.RECORD_MULTIPLE_VALUES)).append(" times): this time series has the ")
		    .append(statistic.getLabel()).append(" of the values at each time (when a single value is available, it is reported).");
	} else if (colocated) {
	    abstract_.append(" The time series merges the values of the co-located stations.");
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

	for (String provider : providers) {
	    ResponsibleParty originator = new ResponsibleParty();
	    originator.setOrganisationName(provider);
	    originator.setRoleCode("originator");
	    dataId.addPointOfContact(originator);
	}

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(LICENSE);
	if (providers.stream().anyMatch(p -> p.toLowerCase().startsWith("hydro-qu"))) {
	    constraints.addUseLimitation(HYDRO_QUEBEC_LICENSE);
	}
	dataId.addLegalConstraints(constraints);

	dataId.setSupplementalInformation("Please cite: " + CITATION + " Dataset: " + source.getEndpoint());

	Keywords keywords = new Keywords();
	ids.forEach(id -> addKeyword(keywords, id));
	names.forEach(name -> addKeyword(keywords, name));
	providers.forEach(provider -> addKeyword(keywords, provider));
	methods.forEach(method -> addKeyword(keywords, method));
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

	if (site.has(CanSWEConnector.RECORD_SITE_LATITUDE) && site.has(CanSWEConnector.RECORD_SITE_LONGITUDE)) {
	    BigDecimal lat = new BigDecimal(site.getString(CanSWEConnector.RECORD_SITE_LATITUDE));
	    BigDecimal lon = new BigDecimal(site.getString(CanSWEConnector.RECORD_SITE_LONGITUDE));
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	if (!elevations.isEmpty()) {
	    VerticalExtent verticalExtent = new VerticalExtent();
	    verticalExtent.setMinimumValue(elevations.stream().mapToDouble(Double::doubleValue).min().getAsDouble());
	    verticalExtent.setMaximumValue(elevations.stream().mapToDouble(Double::doubleValue).max().getAsDouble());
	    dataId.addVerticalExtent(verticalExtent);
	}

	MIPlatform platform = new MIPlatform();
	Citation citation = new Citation();
	citation.setTitle(siteName);
	platform.setCitation(citation);
	platform.setDescription(siteLabel + (providers.isEmpty() ? "" : ", " + String.join(", ", providers)));
	platform.setMDIdentifierCode(NetProtocolWrapper.CANSWE.getCommonURN() + "/" + siteId);
	coreMetadata.getMIMetadata().addMIPlatform(platform);

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.CANSWE.getCommonURN() + ":" + variableName
		+ (statistic == CanSWEStatistic.VALUE ? "" : ":" + statistic.getLabel()));
	coverageDescription.setAttributeTitle(longName);
	coverageDescription.setAttributeDescription(longName + (units.isEmpty() ? "" : ", " + units));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	ExtensionHandler handler = dataset.getExtensionHandler();
	handler.setAttributeUnits(units);
	handler.setAttributeUnitsAbbreviation(units);
	handler.setCountry(Country.CANADA.getShortName());

	switch (statistic) {
	case MAXIMUM:
	    handler.setTimeInterpolation(InterpolationType.MAX);
	    break;
	case MINIMUM:
	    handler.setTimeInterpolation(InterpolationType.MIN);
	    break;
	default:
	    // snow surveys and automatic sensors observations
	    handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	}

	coreMetadata.addDistributionOnlineResource(//
		getOnlineName(variableName, statistic, siteId), //
		record.getString(CanSWEConnector.RECORD_FILE_URL), //
		NetProtocolWrapper.CANSWE.getCommonURN(), //
		"download");

	return dataset;
    }

    /**
     * @param variable
     * @param statistic
     * @param siteId
     * @return the name of the online resource
     */
    public static String getOnlineName(String variable, CanSWEStatistic statistic, String siteId) {

	return variable + NAME_SEPARATOR + statistic.name() + NAME_SEPARATOR + siteId;
    }

    /**
     * @param name as returned by {@link #getOnlineName(String, CanSWEStatistic, String)}
     * @return variable, statistic and site id
     */
    public static Optional<String[]> parseOnlineName(String name) {

	if (name == null) {
	    return Optional.empty();
	}

	String[] split = name.split(NAME_SEPARATOR, 3);
	if (split.length != 3) {
	    return Optional.empty();
	}

	try {
	    CanSWEStatistic.valueOf(split[1]);
	} catch (IllegalArgumentException e) {
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
		CanSWEStatistic.valueOf(record.getString(CanSWEConnector.RECORD_STATISTIC)), //
		record.getJSONObject(CanSWEConnector.RECORD_SITE).getString(CanSWEConnector.RECORD_SITE_ID));
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
