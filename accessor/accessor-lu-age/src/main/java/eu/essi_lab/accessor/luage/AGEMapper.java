package eu.essi_lab.accessor.luage;

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
public class AGEMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String AGE_SCHEMA = "https://inondations.public.lu/schema";

    static final String ORGANIZATION = "Water Management Agency of Luxembourg (Administration de la gestion de l'eau, AGE)";

    static final String LICENSE = "Creative Commons CC0 1.0 Universal Public Domain Dedication -"
	    + " https://creativecommons.org/publicdomain/zero/1.0/ - Data source: " + ORGANIZATION
	    + " (inondations.lu, https://inondations.public.lu/). Raw data, not validated, provided as is, without any warranty.";

    /**
     * Resolution of the published values
     */
    static final String RESOLUTION = "PT15M";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	AGEEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(AGEEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationCode = entity.getStationCode();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationCode));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String propertyName = getParameterLabel(entity.getParameter());
	String begin = object.optString(AGEClient.BEGIN, null);
	String end = object.optString(AGEClient.END, null);

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

	coreMetadata.setTitle(getStationDescription(entity) + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + propertyName.toLowerCase() + " at station " + entity.getStationName()
		+ getWaterCourseDescription(entity) + getGaugeZeroDescription(entity) + ", published every 15 minutes by the "
		+ ORGANIZATION + " on the inondations.lu flood portal. Raw data, not validated: only the values of the last 7 days are available.");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(AGEClient.LAT) && object.has(AGEClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(AGEClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(AGEClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.AGE.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param parameter the WISKI parameter type
     * @return
     */
    static String getParameterLabel(String parameter) {

	switch (parameter) {
	case "W":
	    return "Water level";
	case "Q":
	    return "Discharge";
	default:
	    return parameter;
	}
    }

    /**
     * @param unit
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
	default:
	    return unit;
	}
    }

    /**
     * @param entity
     * @return e.g. "Hesperange (Alzette)"
     */
    private static String getStationDescription(AGEEntity entity) {

	String waterCourse = entity.getWaterCourse();

	return entity.getStationName() + (waterCourse.isEmpty() ? "" : " (" + waterCourse + ")");
    }

    /**
     * @param entity
     * @return
     */
    private static String getWaterCourseDescription(AGEEntity entity) {

	String waterCourse = entity.getWaterCourse();

	return waterCourse.isEmpty() ? "" : " on the " + waterCourse;
    }

    /**
     * @param entity
     * @return the gauge zero, if numeric (the reservoir levels are absolute)
     */
    private static String getGaugeZeroDescription(AGEEntity entity) {

	String gaugeZero = entity.getString(AGEClient.GAUGE_ZERO);

	return gaugeZero.matches("^[0-9].*") ? " (gauge zero " + gaugeZero + ")" : "";
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(AGEEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getWaterCourse());

	addKeyword(keywords, "Luxembourg");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "AGE");
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
    private void addPlatform(AGEEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(getStationDescription(entity));
	platform.setCitation(citation);

	StringBuilder description = new StringBuilder("Hydrometric station " + entity.getStationName() + getWaterCourseDescription(entity));

	String catchmentArea = entity.getString(AGEClient.CATCHMENT_AREA);
	if (!catchmentArea.isEmpty()) {
	    description.append(", catchment area ").append(catchmentArea);
	}

	String serviceDate = entity.getString(AGEClient.SERVICE_DATE);
	if (!serviceDate.isEmpty()) {
	    description.append(", in service since ").append(serviceDate);
	}

	String page = entity.getString(AGEClient.PAGE);
	if (!page.isEmpty()) {
	    description.append(" (").append(page).append(")");
	}

	platform.setDescription(description.toString());
	platform.setMDIdentifierCode(NetProtocolWrapper.AGE.getCommonURN() + "/" + entity.getStationCode());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(AGEEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(propertyName);
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.AGE.getCommonURN() + ":" + entity.getParameter());
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(AGEEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();
	if (!unit.isEmpty()) {
	    handler.setAttributeUnits(getUnitName(unit));
	    handler.setAttributeUnitsAbbreviation(unit);
	}

	handler.setCountry("Luxembourg");
	handler.setTimeResolutionDuration8601(RESOLUTION);
	handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
    }

    /**
     * @param originalMD
     * @return
     */
    private AGEEntity getEntity(OriginalMetadata originalMD) {

	return new AGEEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return AGE_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	AGEEntity entity = getEntity(resource.getOriginalMetadata());
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
