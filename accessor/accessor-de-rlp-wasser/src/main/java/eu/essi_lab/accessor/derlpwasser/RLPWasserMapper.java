package eu.essi_lab.accessor.derlpwasser;

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
public class RLPWasserMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String RLP_WASSER_SCHEMA = "https://hochwasser.rlp.de/api/v1/schema";

    static final String LICENSE = "Copyright Landesamt für Umwelt Rheinland-Pfalz (LfU) - https://hochwasser.rlp.de/impressum -"
	    + " All contributions, figures and data published on the website are protected by copyright. They may be modified,"
	    + " reproduced, passed on to third parties or used for public reproduction only with the consent of the LfU. The LfU must"
	    + " be cited as source, if possible with the date of processing. No guarantee is given on completeness, correctness and"
	    + " timeliness of the data. Real time data are unverified raw data.";

    static final String ORGANIZATION = "Landesamt für Umwelt Rheinland-Pfalz (LfU)";

    /**
     * Resolution of the measurements
     */
    static final String RESOLUTION = "PT15M";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	RLPWasserEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(RLPWasserEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String number = entity.getSiteNumber();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), number));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String siteName = entity.getSiteName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(RLPWasserClient.BEGIN, null);
	String end = object.optString(RLPWasserClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	String operator = entity.getString(RLPWasserClient.OPERATOR);
	if (!operator.isEmpty()) {
	    ResponsibleParty owner = new ResponsibleParty();
	    owner.setOrganisationName(operator);
	    owner.setRoleCode("owner");
	    dataId.addPointOfContact(owner);
	}

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(LICENSE);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, propertyName, keywords);

	coreMetadata.setTitle(siteName + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " at measurement site " + siteName + " ("
		+ number + ")" + getSiteDescription(entity) + " from the flood portal (Hochwassermeldedienst) of the " + ORGANIZATION
		+ ". The portal publishes only the measurements of the last 5 days (15 minutes values, unverified raw data).");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(RLPWasserClient.LAT) && object.has(RLPWasserClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(RLPWasserClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(RLPWasserClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.RLP_WASSER.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return e.g. "Water level (Wasserstand)"
     */
    static String getPropertyName(RLPWasserEntity entity) {

	return entity.getParameter().map(p -> p.getLabel() + " (" + p.getGermanName() + ")").orElse(entity.getString(RLPWasserClient.PARAMETER));
    }

    /**
     * @param entity
     * @return the property name, with the reference of the water level
     */
    static String getPropertyDescription(RLPWasserEntity entity) {

	String name = getPropertyName(entity);

	if (entity.getParameter().filter(p -> p == RLPWasserParameter.W).isPresent()) {

	    if (entity.isSeaSite()) {
		return name + " (lake level, referred to the sea level, m ü. NHN)";
	    }

	    String elevation = entity.getString(RLPWasserClient.ELEVATION);
	    if (!elevation.isEmpty()) {
		return name + " (referred to the gauge datum, " + elevation + " m ü. NHN DHHN2016)";
	    }

	    return name + " (referred to the gauge datum)";
	}

	return name;
    }

    /**
     * @param entity
     * @return
     */
    private static String getSiteDescription(RLPWasserEntity entity) {

	StringBuilder builder = new StringBuilder();

	String river = entity.getString(RLPWasserClient.RIVER);
	if (!river.isEmpty()) {
	    builder.append(", river ").append(river);
	}

	String kilometer = entity.getString(RLPWasserClient.RIVER_KILOMETER);
	if (!kilometer.isEmpty()) {
	    builder.append(" (km ").append(kilometer).append(")");
	}

	String area = entity.getString(RLPWasserClient.CATCHMENT_AREA);
	if (!area.isEmpty()) {
	    builder.append(", catchment area ").append(area).append(" km²");
	}

	String operator = entity.getString(RLPWasserClient.OPERATOR);
	if (!operator.isEmpty()) {
	    builder.append(", operated by ").append(operator);
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(RLPWasserEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getSiteName());
	entity.getParameter().ifPresent(p -> {
	    addKeyword(keywords, p.getLabel());
	    addKeyword(keywords, p.getGermanName());
	});
	addKeyword(keywords, entity.getString(RLPWasserClient.RIVER));
	addKeyword(keywords, entity.getString(RLPWasserClient.OPERATOR));

	addKeyword(keywords, entity.getString(RLPWasserClient.COUNTRY));
	addKeyword(keywords, "Rheinland-Pfalz");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "LfU RLP");
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
    private void addPlatform(RLPWasserEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String siteName = entity.getSiteName();

	Citation citation = new Citation();
	citation.setTitle(siteName);
	platform.setCitation(citation);

	platform.setDescription("Measurement site " + siteName + " (" + entity.getSiteNumber() + ")" + getSiteDescription(entity)
		+ " (" + entity.getString(RLPWasserClient.COUNTRY) + ")");
	platform.setMDIdentifierCode(NetProtocolWrapper.RLP_WASSER.getCommonURN() + "/" + entity.getSiteNumber());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(RLPWasserEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.RLP_WASSER.getCommonURN() + ":" + entity.getParameter().map(RLPWasserParameter::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(RLPWasserEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();

	handler.setAttributeUnits(getUnitName(unit));
	handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit));

	String country = entity.getString(RLPWasserClient.COUNTRY);
	if (!country.isEmpty()) {
	    handler.setCountry(country);
	}

	handler.setTimeResolutionDuration8601(RESOLUTION);
	handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
    }

    /**
     * @param unit the unit symbol used by the portal
     * @return
     */
    static String getUnitName(String unit) {

	switch (unit) {
	case "cm":
	    return "centimetres";
	case "m ü. NHN":
	    return "metres above sea level (NHN)";
	case "m³/s":
	    return "cubic metres per second";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the unit symbol used by the portal
     * @return
     */
    static String getUnitAbbreviation(String unit) {

	switch (unit) {
	case "m ü. NHN":
	    return "m";
	case "m³/s":
	    return "m3/s";
	default:
	    return unit;
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private RLPWasserEntity getEntity(OriginalMetadata originalMD) {

	return new RLPWasserEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return RLP_WASSER_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	RLPWasserEntity entity = getEntity(resource.getOriginalMetadata());
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
