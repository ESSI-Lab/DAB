package eu.essi_lab.accessor.rarcticnet;

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
public class RArcticNetMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String R_ARCTICNET_SCHEMA = "https://www.r-arcticnet.sr.unh.edu/v4.0/schema";

    static final String CITATION = "Lammers, R.B., A.I. Shiklomanov, C.J. Vorosmarty, B.M. Fekete and B.J. Peterson (2001),"
	    + " Assessment of contemporary Arctic river runoff based on observational discharge records, J. Geophys. Res., 106(D4),"
	    + " 3321-3334, doi:10.1029/2000JD900444";

    static final String LICENSE = "No licence or terms of use are stated on the R-ArcticNet web site"
	    + " (https://www.r-arcticnet.sr.unh.edu/v4.0/), where the data are openly downloadable for the pan-Arctic scientific"
	    + " community. Please cite R-ArcticNet v4.0 and " + CITATION + ". Original data from Environment Canada (HYDAT),"
	    + " U.S. Geological Survey and State Hydrological Institute (St. Petersburg, Russia).";

    static final String ORGANIZATION = "Water Systems Analysis Group, University of New Hampshire (UNH)";

    static final String PROPERTY_NAME = "Discharge";

    static final String UNIT_NAME = "cubic metres per second";
    static final String UNIT_ABBREVIATION = "m3/s";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	RArcticNetEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(RArcticNetEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String pointId = entity.getPointId();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), pointId));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String name = entity.getName();
	String label = getLabel(entity);
	String begin = object.optString(RArcticNetClient.BEGIN, null);
	String end = object.optString(RArcticNetClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	String agency = getSourceAgency(entity.getString(RArcticNetClient.SOURCE));
	if (!agency.isEmpty()) {
	    ResponsibleParty originator = new ResponsibleParty();
	    originator.setOrganisationName(agency);
	    originator.setRoleCode("originator");
	    dataId.addPointOfContact(originator);
	}

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(LICENSE);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, agency, keywords);

	coreMetadata.setTitle(name + " - " + PROPERTY_NAME + " (" + label + " mean)");

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " at gauge " + getSiteDescription(entity, agency)
		+ " from " + getDatasetDescription(entity) + ". Static historical archive, not updated: values from "
		+ formatDate(begin) + " to " + formatDate(end) + " (" + object.optInt(RArcticNetClient.VALUE_COUNT) + " " + label
		+ " values, missing values are omitted).");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	BigDecimal lat = parseDecimal(entity.getString(RArcticNetClient.LAT));
	BigDecimal lon = parseDecimal(entity.getString(RArcticNetClient.LON));
	if (lat != null && lon != null) {
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, agency, coreMetadata);
	addCoverageDescription(entity, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.R_ARCTICNET.getCommonURN(), //
		"download");

	// the archive is static: the series are not real time, so no indeterminate end position is set

	return dataset;
    }

    /**
     * @param entity
     * @return "monthly" or "daily"
     */
    static String getLabel(RArcticNetEntity entity) {

	return entity.getDataset().map(RArcticNetDataset::getLabel).orElse("");
    }

    /**
     * @param entity
     * @return e.g. "mean monthly river discharge (m3/s)"
     */
    static String getPropertyDescription(RArcticNetEntity entity) {

	return "mean " + getLabel(entity) + " river discharge (" + UNIT_ABBREVIATION + ")";
    }

    /**
     * @param entity
     * @return
     */
    private static String getDatasetDescription(RArcticNetEntity entity) {

	if (entity.getDataset().filter(d -> d == RArcticNetDataset.DAILY).isPresent()) {

	    return "the Russian daily discharge data of the NSF-funded UCLA/UNH project, distributed by R-ArcticNet v4.0 (A Regional,"
		    + " Electronic, Hydrographic Data Network for the Arctic Region) of the " + ORGANIZATION;
	}

	return "R-ArcticNet v4.0 (A Regional, Electronic, Hydrographic Data Network for the Arctic Region), the pan-Arctic river"
		+ " discharge database of the " + ORGANIZATION;
    }

    /**
     * @param entity
     * @param agency
     * @return e.g. "Kolyma At Orotuk (R-ArcticNet PointID 5855, State Hydrological Institute code 1001), hydrological zone Anadyr
     *         Kolyma, drainage area 43600 km2"
     */
    static String getSiteDescription(RArcticNetEntity entity, String agency) {

	StringBuilder builder = new StringBuilder();

	builder.append(entity.getName()).append(" (R-ArcticNet PointID ").append(entity.getPointId());

	String code = entity.getCode();
	if (!code.isEmpty()) {
	    builder.append(", ").append(agency.isEmpty() ? "" : agency + " ").append("code ").append(code);
	}
	builder.append(")");

	String hydrozone = entity.getString(RArcticNetClient.HYDROZONE);
	if (!hydrozone.isEmpty()) {
	    builder.append(", hydrological zone ").append(hydrozone);
	}

	BigDecimal area = parseDecimal(entity.getString(RArcticNetClient.DRAINAGE_AREA));
	if (area != null && area.signum() > 0) {
	    builder.append(", drainage area ").append(area.stripTrailingZeros().toPlainString()).append(" km2");
	}

	return builder.toString();
    }

    /**
     * @param source the source of the data, as given by the site attributes
     * @return the name of the source agency, or an empty string if not known
     */
    static String getSourceAgency(String source) {

	switch (source) {
	case "Hydat":
	    return "Environment Canada (HYDAT)";
	case "USGS":
	case "AK":
	case "HCDN":
	    return "U.S. Geological Survey (USGS)";
	case "SHI":
	    return "State Hydrological Institute (SHI)";
	case "UNESCO":
	    return "UNESCO RivDIS";
	default:
	    return source;
	}
    }

    /**
     * @param source the source of the data, as given by the site attributes
     * @return the country of the gauge, when implied by the source agency (the State Hydrological Institute supplied also data of
     *         Finland, Iceland, Mongolia and Norway), or null
     */
    static String getCountry(String source) {

	switch (source) {
	case "Hydat":
	    return "Canada";
	case "USGS":
	case "AK":
	case "HCDN":
	    return "United States";
	default:
	    return null;
	}
    }

    /**
     * @param entity
     * @param agency
     * @param keywords
     */
    private void addKeywords(RArcticNetEntity entity, String agency, Keywords keywords) {

	addKeyword(keywords, entity.getName());
	addKeyword(keywords, PROPERTY_NAME);
	addKeyword(keywords, "River discharge");
	addKeyword(keywords, getLabel(entity).isEmpty() ? null : "Mean " + getLabel(entity) + " discharge");
	addKeyword(keywords, entity.getString(RArcticNetClient.HYDROZONE));
	addKeyword(keywords, agency);
	addKeyword(keywords, getCountry(entity.getString(RArcticNetClient.SOURCE)));

	addKeyword(keywords, "Arctic");
	addKeyword(keywords, "Pan-Arctic");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "R-ArcticNet");
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
     * @param agency
     * @param coreMetadata
     */
    private void addPlatform(RArcticNetEntity entity, String agency, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(entity.getName());
	platform.setCitation(citation);

	platform.setDescription("Gauge " + getSiteDescription(entity, agency));
	platform.setMDIdentifierCode(NetProtocolWrapper.R_ARCTICNET.getCommonURN() + "/" + entity.getPointId());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param coreMetadata
     */
    private void addCoverageDescription(RArcticNetEntity entity, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(PROPERTY_NAME);
	coverageDescription.setAttributeDescription("Mean " + getLabel(entity) + " river discharge (" + UNIT_ABBREVIATION + ")");
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.R_ARCTICNET.getCommonURN() + ":" + PROPERTY_NAME);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(RArcticNetEntity entity, ExtensionHandler handler) {

	handler.setAttributeUnits(UNIT_NAME);
	handler.setAttributeUnitsAbbreviation(UNIT_ABBREVIATION);

	String country = getCountry(entity.getString(RArcticNetClient.SOURCE));
	if (country != null) {
	    handler.setCountry(country);
	}

	entity.getDataset().ifPresent(d -> {

	    handler.setTimeResolutionDuration8601(d.getDuration());
	    handler.setTimeAggregationDuration8601(d.getDuration());
	});

	// mean values of the month (or day), timestamped at its beginning
	handler.setTimeInterpolation(InterpolationType.AVERAGE_SUCC);
    }

    /**
     * @param date e.g. "1957-01-01T00:00:00Z"
     * @return e.g. "1957-01-01"
     */
    private static String formatDate(String date) {

	return date == null ? "" : date.length() > 10 ? date.substring(0, 10) : date;
    }

    /**
     * @param text
     * @return
     */
    private static BigDecimal parseDecimal(String text) {

	try {
	    return text == null || text.isBlank() ? null : new BigDecimal(text.trim());
	} catch (NumberFormatException e) {
	    return null;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private RArcticNetEntity getEntity(OriginalMetadata originalMD) {

	return new RArcticNetEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return R_ARCTICNET_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	RArcticNetEntity entity = getEntity(resource.getOriginalMetadata());
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
