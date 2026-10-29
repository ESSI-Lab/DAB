package eu.essi_lab.accessor.ththaiwater;

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
public class ThaiWaterMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String THAIWATER_SCHEMA = "https://api-v3.thaiwater.net/api/v1/thaiwater30/schema";

    /**
     * No open licence is stated by ThaiWater: the portal footer only reports the copyright of HII
     */
    static final String LICENSE = "Copyright Hydro-Informatics Institute (HII), all rights reserved - no open data licence is stated"
	    + " by ThaiWater (https://www.thaiwater.net). Data are collected from the agencies operating the stations; real time data"
	    + " are provisional.";

    static final String ORGANIZATION = "Hydro-Informatics Institute (HII)";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	ThaiWaterEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(ThaiWaterEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), entity.getStationKey()));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String stationName = entity.getStationName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(ThaiWaterClient.BEGIN, null);
	String end = object.optString(ThaiWaterClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	String agency = entity.getString(ThaiWaterClient.AGENCY);
	// the stations of HII are already described by the point of contact
	if (!agency.isEmpty() && !agency.startsWith("Hydro")) {
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
	addKeywords(entity, propertyName, keywords);

	coreMetadata.setTitle(stationName + " - " + propertyName);

	coreMetadata.setAbstract(getAbstract(entity));

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(ThaiWaterClient.LAT) && object.has(ThaiWaterClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(ThaiWaterClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(ThaiWaterClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.THAIWATER.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return e.g. "Water level"
     */
    static String getPropertyName(ThaiWaterEntity entity) {

	return entity.getParameter().map(ThaiWaterParameter::getLabel).orElse(entity.getString(ThaiWaterClient.PARAMETER));
    }

    /**
     * @param entity
     * @return
     */
    static String getAbstract(ThaiWaterEntity entity) {

	ThaiWaterParameter parameter = entity.getParameter().orElse(ThaiWaterParameter.WATER_LEVEL);
	ThaiWaterStationType type = parameter.getStationType();

	StringBuilder builder = new StringBuilder();

	builder.append(type == ThaiWaterStationType.DAM ? "Daily time series of " : "Time series of ");
	builder.append(parameter.getDescription());
	builder.append(" in ").append(parameter.getUnitName());
	builder.append(type == ThaiWaterStationType.DAM ? " of the reservoir " : " at the station ");
	builder.append(entity.getStationName());
	builder.append(getLocationDescription(entity));

	String agency = entity.getString(ThaiWaterClient.AGENCY);
	if (!agency.isEmpty()) {
	    builder.append(", operated by ").append(agency);
	}

	builder.append(". Published by ThaiWater, the water data portal of the " + ORGANIZATION + " (ThaiWater property \""
		+ parameter.getKey() + "\"). ");

	switch (type) {
	case TELE_WATERLEVEL:
	    builder.append("The observations of the last year are available at full resolution (10 minutes or hourly, depending on the"
		    + " agency); older observations, since " + ThaiWaterClient.FIRST_YEAR
		    + ", are available as daily readings of 06:00 local time (and at full resolution for requested periods up to one"
		    + " month).");
	    break;
	case CANAL:
	    builder.append("Observations every 15 minutes.");
	    break;
	case FLOW:
	    builder.append("Observations every 10 minutes.");
	    break;
	case DAM:
	default:
	    builder.append(parameter.isDailyVolume() ? "Daily volumes." : "Daily values.");
	    break;
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @return e.g. ", Tha Chin Basin, Suphan Buri (Thailand)"
     */
    private static String getLocationDescription(ThaiWaterEntity entity) {

	StringBuilder builder = new StringBuilder();

	String river = entity.getString(ThaiWaterClient.RIVER);
	if (!river.isEmpty()) {
	    builder.append(", river ").append(river);
	}

	String basin = entity.getString(ThaiWaterClient.BASIN);
	if (!basin.isEmpty()) {
	    builder.append(", ").append(basin);
	}

	String province = entity.getString(ThaiWaterClient.PROVINCE);
	if (!province.isEmpty()) {
	    builder.append(", ").append(province).append(" province");
	}

	builder.append(" (Thailand)");

	return builder.toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(ThaiWaterEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, entity.getString(ThaiWaterClient.STATION_NAME_TH));
	addKeyword(keywords, propertyName);
	entity.getParameter().ifPresent(p -> addKeyword(keywords, p.getKey()));
	addKeyword(keywords, entity.getString(ThaiWaterClient.RIVER));
	addKeyword(keywords, entity.getString(ThaiWaterClient.BASIN));
	addKeyword(keywords, entity.getString(ThaiWaterClient.PROVINCE));
	addKeyword(keywords, entity.getString(ThaiWaterClient.AGENCY_SHORT_NAME));

	addKeyword(keywords, "Thailand");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "ThaiWater");
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
    private void addPlatform(ThaiWaterEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	String type = entity.getStationType().map(ThaiWaterStationType::getLabel).orElse("Station");
	platform.setDescription(type + " " + stationName + getLocationDescription(entity));
	platform.setMDIdentifierCode(NetProtocolWrapper.THAIWATER.getCommonURN() + "/" + entity.getStationKey());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(ThaiWaterEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(entity.getParameter().map(ThaiWaterParameter::getDescription).orElse(propertyName));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.THAIWATER.getCommonURN() + ":" + entity.getParameter().map(ThaiWaterParameter::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(ThaiWaterEntity entity, ExtensionHandler handler) {

	handler.setCountry("Thailand");

	if (entity.getParameter().isEmpty()) {
	    return;
	}

	ThaiWaterParameter parameter = entity.getParameter().get();

	handler.setAttributeUnits(parameter.getUnitName());
	handler.setAttributeUnitsAbbreviation(parameter.getUnit());

	switch (parameter.getStationType()) {
	case CANAL:
	    handler.setTimeResolutionDuration8601("PT15M");
	    break;
	case FLOW:
	    handler.setTimeResolutionDuration8601("PT10M");
	    break;
	case DAM:
	    handler.setTimeResolutionDuration8601("P1D");
	    break;
	default:
	    // 10 minutes or hourly, depending on the agency
	    break;
	}

	if (parameter.isDailyVolume()) {
	    handler.setTimeInterpolation(InterpolationType.TOTAL);
	    handler.setTimeAggregationDuration8601("P1D");
	} else {
	    handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private ThaiWaterEntity getEntity(OriginalMetadata originalMD) {

	return new ThaiWaterEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return THAIWATER_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	ThaiWaterEntity entity = getEntity(resource.getOriginalMetadata());
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
