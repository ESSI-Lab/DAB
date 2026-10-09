package eu.essi_lab.accessor.geuspromice;

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
public class PROMICEMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String PROMICE_SCHEMA = "https://thredds.geus.dk/thredds/aws/l3sites/schema";

    private static final String DEFAULT_INSTITUTION = "Geological Survey of Denmark and Greenland (GEUS)";

    private static final String DEFAULT_LICENSE = "Creative Commons Attribution 4.0 International (CC-BY-4.0) https://creativecommons.org/licenses/by/4.0";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	JSONObject record = new JSONObject(originalMD.getMetadata());
	JSONObject site = record.getJSONObject(PROMICEConnector.RECORD_SITE);
	JSONObject variable = record.getJSONObject(PROMICEConnector.RECORD_VARIABLE);

	String siteId = site.optString("site_id", record.optString(PROMICEConnector.RECORD_DATASET_NAME));
	String project = site.optString("project", "PROMICE");
	String variableName = variable.getString(PROMICESite.VARIABLE_NAME);
	String longName = variable.optString("long_name", variableName);
	String units = normalizeUnits(variable.optString("units", ""));
	String resolution = PROMICENetCDF.normalizeResolution(site.optString("time_coverage_resolution", null));
	String institution = site.optString("institution", DEFAULT_INSTITUTION);
	String siteLabel = project + " site " + siteId;

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().setHierarchyLevelName("dataset");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.getMIMetadata().setParentIdentifier(decorateIdentifier(source.getEndpoint(), siteId));

	coreMetadata.setTitle(longName + " (" + getResolutionLabel(resolution) + ") at " + siteLabel);

	String abstract_ = "Timeseries of " + longName.toLowerCase() + (units.isEmpty() ? "" : " (" + units + ")") + " at the " + siteLabel + " ("
		+ site.optString("location_type", "Greenland") + "), from the Level 3 automatic weather station data of the Programme for Monitoring of the Greenland Ice Sheet (PROMICE) and the Greenland Climate Network (GC-Net), published by "
		+ institution + ".";
	if (site.has("stations")) {
	    abstract_ += " The site record merges the data of the stations: " + site.getString("stations") + ".";
	}
	if (site.has("summary")) {
	    abstract_ += "\n" + site.getString("summary");
	}
	coreMetadata.setAbstract(abstract_);

	DataIdentification dataId = coreMetadata.getDataIdentification();

	ResponsibleParty publisher = new ResponsibleParty();
	publisher.setOrganisationName(institution);
	publisher.setRoleCode("pointOfContact");
	dataId.addPointOfContact(publisher);

	if (site.has("creator_name")) {
	    ResponsibleParty creator = new ResponsibleParty();
	    creator.setIndividualName(site.getString("creator_name"));
	    creator.setOrganisationName(institution);
	    creator.setRoleCode("author");
	    dataId.addPointOfContact(creator);
	}

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(site.optString("license", DEFAULT_LICENSE));
	dataId.addLegalConstraints(constraints);

	if (site.has("references")) {
	    dataId.setSupplementalInformation("Please cite: " + site.getString("references"));
	}

	Keywords keywords = new Keywords();
	addKeyword(keywords, siteId);
	addKeyword(keywords, longName);
	addKeyword(keywords, variable.optString("standard_name", null));
	addKeyword(keywords, project);
	addKeyword(keywords, "PROMICE");
	addKeyword(keywords, "Greenland");
	addKeyword(keywords, "Ice sheet");
	addKeyword(keywords, "Cryosphere");
	dataId.addKeywords(keywords);

	if (variable.has(PROMICESite.VARIABLE_BEGIN) && variable.has(PROMICESite.VARIABLE_END)) {
	    coreMetadata.addTemporalExtent(//
		    ISO8601DateTimeUtils.getISO8601DateTime(new Date(variable.getLong(PROMICESite.VARIABLE_BEGIN))), //
		    ISO8601DateTimeUtils.getISO8601DateTime(new Date(variable.getLong(PROMICESite.VARIABLE_END))));
	}

	// the stations move with the ice: the average position of the site is used
	if (site.has("latitude") && site.has("longitude")) {
	    BigDecimal lat = new BigDecimal(site.getString("latitude"));
	    BigDecimal lon = new BigDecimal(site.getString("longitude"));
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	if (site.has("altitude")) {
	    try {
		double altitude = Math.round(Double.parseDouble(site.getString("altitude")) * 10) / 10.0;
		VerticalExtent verticalExtent = new VerticalExtent();
		verticalExtent.setMinimumValue(altitude);
		verticalExtent.setMaximumValue(altitude);
		dataId.addVerticalExtent(verticalExtent);
	    } catch (NumberFormatException e) {
	    }
	}

	MIPlatform platform = new MIPlatform();
	Citation citation = new Citation();
	citation.setTitle(siteId);
	platform.setCitation(citation);
	platform.setDescription(siteLabel + " (" + site.optString("location_type", "") + ")");
	platform.setMDIdentifierCode(NetProtocolWrapper.PROMICE.getCommonURN() + "/" + siteId);
	coreMetadata.getMIMetadata().addMIPlatform(platform);

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.PROMICE.getCommonURN() + ":" + variableName);
	coverageDescription.setAttributeTitle(longName);
	coverageDescription.setAttributeDescription(longName + (units.isEmpty() ? "" : ", " + units));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	ExtensionHandler handler = dataset.getExtensionHandler();
	handler.setAttributeUnits(units);
	handler.setAttributeUnitsAbbreviation(units);
	handler.setCountry(Country.GREENLAND.getShortName());

	// L3 values are averages (totals for rainfall) over the time step
	handler.setTimeInterpolation(variableName.startsWith("rainfall") ? InterpolationType.TOTAL : InterpolationType.AVERAGE);
	if (resolution != null) {
	    handler.setTimeResolutionDuration8601(resolution);
	    handler.setTimeAggregationDuration8601(resolution);
	}

	coreMetadata.addDistributionOnlineResource(//
		variableName, //
		record.getString(PROMICEConnector.RECORD_FILE_URL), //
		NetProtocolWrapper.PROMICE.getCommonURN(), //
		"download");

	return dataset;
    }

    /**
     * @param units the CF units of the files
     * @return
     */
    static String normalizeUnits(String units) {

	switch (units) {
	case "degrees_C":
	case "C":
	    return "°C";
	case "-":
	    return "";
	default:
	    return units;
	}
    }

    /**
     * @param resolution
     * @return
     */
    static String getResolutionLabel(String resolution) {

	if (resolution == null) {
	    return "time series";
	}

	switch (resolution) {
	case "PT1H":
	    return "hourly";
	case "P1D":
	    return "daily";
	case "P1M":
	    return "monthly";
	default:
	    return resolution;
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

	return PROMICE_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	JSONObject record = new JSONObject(resource.getOriginalMetadata().getMetadata());
	return decorateIdentifier(resource.getSource().getEndpoint(), record.getString(PROMICEConnector.RECORD_FILE_URL) + "#"
		+ record.getJSONObject(PROMICEConnector.RECORD_VARIABLE).getString(PROMICESite.VARIABLE_NAME));
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
