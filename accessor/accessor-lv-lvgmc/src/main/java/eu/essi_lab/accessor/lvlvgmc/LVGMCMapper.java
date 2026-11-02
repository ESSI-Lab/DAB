package eu.essi_lab.accessor.lvlvgmc;

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
public class LVGMCMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String LVGMC_SCHEMA = "https://data.gov.lv/lvgmc/hydro/schema";

    static final String ORGANIZATION = "Latvian Environment, Geology and Meteorology Centre (LVGMC)";

    static final String LICENSE = "Creative Commons CC0 1.0 Universal (CC0-1.0) - https://creativecommons.org/publicdomain/zero/1.0/"
	    + " - Data source: " + ORGANIZATION + ", Latvian Open Data Portal, dataset \"Hidrometeoroloģiskie novērojumi\" ("
	    + LVGMCClient.DEFAULT_ENDPOINT + ").";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	LVGMCEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(LVGMCEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationId = entity.getStationId();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationId));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String propertyName = entity.getPropertyName();
	String begin = object.optString(LVGMCClient.BEGIN, null);
	String end = object.optString(LVGMCClient.END, null);

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

	coreMetadata.setTitle(entity.getStationName() + " - " + propertyName);

	coreMetadata.setAbstract(createAbstract(entity, propertyName));

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(LVGMCClient.LATITUDE) && object.has(LVGMCClient.LONGITUDE)) {
	    BigDecimal lat = new BigDecimal(object.get(LVGMCClient.LATITUDE).toString());
	    BigDecimal lon = new BigDecimal(object.get(LVGMCClient.LONGITUDE).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.LVGMC.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @param propertyName
     * @return
     */
    static String createAbstract(LVGMCEntity entity, String propertyName) {

	StringBuilder builder = new StringBuilder();

	builder.append("Hourly time series of ").append(propertyName.toLowerCase()).append(" (LVGMC parameter ")
		.append(entity.getAbbreviation()).append(getDescriptions(entity));

	if (!entity.getUnit().isEmpty()) {
	    builder.append(", unit ").append(entity.getUnit());
	}

	builder.append(") at the hydrological station ").append(entity.getStationName()).append(" (").append(entity.getStationId())
		.append("), Latvia, published by the ").append(ORGANIZATION)
		.append(" on the Latvian Open Data Portal (data.gov.lv, dataset \"Hidrometeoroloģiskie novērojumi\"). ");

	builder.append("Only the last 365 days are available (rolling archive updated daily, complemented by the operational data"
		+ " of the last 48 hours updated hourly): older observations are not available from this source. ");

	builder.append("Times are UTC and refer to the end of the averaging period (e.g. the value at 14:00 refers to 13:00-13:59)."
		+ " The most recent values passed only a basic quality control and can change until the 20th day of the following month.");

	if (entity.isWaterLevel()) {
	    builder.append(" Water levels are given above the station zero: to obtain levels in the LAS-2000,5 height system the"
		    + " station zero height must be added");
	    String elevation = entity.getString(LVGMCClient.ELEVATION);
	    if (!elevation.isEmpty()) {
		builder.append(" (station elevation published by LVGMC: ").append(elevation).append(" m above sea level)");
	    }
	    builder.append(".");
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @return e.g. ": Water level, real, hourly average / Ūdens līmenis, faktiskais, stundas vidējais"
     */
    private static String getDescriptions(LVGMCEntity entity) {

	String en = entity.getEnglishDescription();
	String lv = entity.getLatvianDescription();

	if (en.isEmpty() && lv.isEmpty()) {
	    return "";
	}

	return ": " + (en.isEmpty() ? lv : lv.isEmpty() ? en : en + " / " + lv);
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(LVGMCEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getAbbreviation());
	addKeyword(keywords, entity.getEnglishDescription());
	addKeyword(keywords, entity.getLatvianDescription());

	addKeyword(keywords, "Latvia");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "LVGMC");
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
    private void addPlatform(LVGMCEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(entity.getStationName());
	platform.setCitation(citation);

	String since = entity.getString(LVGMCClient.BEGIN_DATE);
	String until = entity.getString(LVGMCClient.END_DATE);

	platform.setDescription("Hydrological station " + entity.getStationName() + " (" + entity.getStationId() + ") (Latvia)"
		+ (since.length() >= 10 ? ", in operation since " + since.substring(0, 10) : "")
		+ (until.length() >= 10 ? " until " + until.substring(0, 10) : ""));
	platform.setMDIdentifierCode(NetProtocolWrapper.LVGMC.getCommonURN() + "/" + entity.getStationId());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(LVGMCEntity entity, String propertyName, CoreMetadata coreMetadata) {

	String description = entity.getEnglishDescription();

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(description.isEmpty() ? propertyName : description);
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.LVGMC.getCommonURN() + ":" + entity.getAbbreviation());
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(LVGMCEntity entity, ExtensionHandler handler) {

	if (!entity.getUnit().isEmpty()) {
	    handler.setAttributeUnits(entity.getUnitName());
	    handler.setAttributeUnitsAbbreviation(entity.getUnit());
	}

	handler.setCountry("Latvia");
	handler.setTimeResolutionDuration8601("PT1H");
	handler.setTimeInterpolation(InterpolationType.AVERAGE);
	handler.setTimeAggregationDuration8601(entity.getAggregationPeriod());
    }

    /**
     * @param originalMD
     * @return
     */
    private LVGMCEntity getEntity(OriginalMetadata originalMD) {

	return new LVGMCEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return LVGMC_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	LVGMCEntity entity = getEntity(resource.getOriginalMetadata());
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
