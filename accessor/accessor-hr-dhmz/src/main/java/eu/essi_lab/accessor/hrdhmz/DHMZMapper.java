package eu.essi_lab.accessor.hrdhmz;

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
public class DHMZMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String DHMZ_SCHEMA = "https://hidro.dhz.hr/hidroweb/schema";

    static final String LICENSE = "Data of the Croatian Meteorological and Hydrological Service (DHMZ) are freely available for reuse;"
	    + " publication, further use or reuse must state the source of the data (Izvor: DHMZ), as required by article 17 of the"
	    + " Croatian Law on meteorological and hydrological activity (NN 66/19, 114/22) - https://meteo.hr/o_nama.php?section=naslovnica&param=uvjeti";

    static final String REAL_TIME_NOTE = " Real time data of the last 15 days are received directly from the automatic stations and are"
	    + " not verified.";

    static final String ORGANIZATION = "Croatian Meteorological and Hydrological Service (DHMZ - Državni hidrometeorološki zavod)";

    static final String COUNTRY = "Croatia";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	DHMZEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(DHMZEntity entity, GSSource source) {

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
	String begin = object.optString(DHMZClient.BEGIN, null);
	String end = object.optString(DHMZClient.END, null);

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

	coreMetadata.setTitle(stationName + " - " + propertyName + (entity.isRealTime() ? " (real time)" : " (daily)"));

	coreMetadata.setAbstract(getAbstract(entity));

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(DHMZClient.LAT) && object.has(DHMZClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(DHMZClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(DHMZClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.DHMZ.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return
     */
    static String getAbstract(DHMZEntity entity) {

	StringBuilder builder = new StringBuilder();

	if (entity.isRealTime()) {
	    builder.append("Real time ").append(getPropertyDescription(entity).toLowerCase());
	} else {
	    builder.append("Daily ").append(getPropertyDescription(entity).toLowerCase());
	}

	builder.append(" at station ").append(entity.getStationName()).append(" (").append(entity.getStationCode()).append(")");
	builder.append(getWaterbodyDescription(entity));
	builder.append(", from the hidroweb service of the ").append(ORGANIZATION).append(".");

	if (entity.isRealTime()) {
	    builder.append(" The service keeps only the observations of the last ").append(DHMZClient.REAL_TIME_DAYS)
		    .append(" days (rolling window).").append(REAL_TIME_NOTE);
	} else {
	    String years = entity.getString(DHMZClient.YEARS);
	    if (!years.isEmpty()) {
		builder.append(" Years of measurement: ").append(years).append(".");
	    }
	    builder.append(" Daily values are published for the complete years of the archive.");
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @return e.g. "Water level"
     */
    static String getPropertyName(DHMZEntity entity) {

	return entity.getProperty().map(DHMZProperty::getLabel).orElse("");
    }

    /**
     * @param entity
     * @return the property name, with the gauge zero of the water level
     */
    static String getPropertyDescription(DHMZEntity entity) {

	String name = getPropertyName(entity);
	String gaugeZero = entity.getString(DHMZClient.GAUGE_ZERO);

	if (entity.getProperty().filter(p -> p == DHMZProperty.WATER_LEVEL).isPresent() && !gaugeZero.isEmpty()) {
	    return name + " (referred to the gauge zero, " + gaugeZero + " m a.s.l.)";
	}

	return name;
    }

    /**
     * @param entity
     * @return
     */
    private static String getWaterbodyDescription(DHMZEntity entity) {

	String river = entity.getString(DHMZClient.RIVER);
	String subBasin = entity.getString(DHMZClient.SUB_BASIN);
	String basin = entity.getString(DHMZClient.BASIN);

	StringBuilder builder = new StringBuilder();
	if (!river.isEmpty()) {
	    builder.append(", river ").append(river);
	}
	if (!subBasin.isEmpty()) {
	    builder.append(", ").append(subBasin);
	}
	if (!basin.isEmpty()) {
	    // the complete station list groups the stations by area (e.g. "SAVA", "DALMACIJA"), other lists by sea basin
	    String name = getBasinName(basin);
	    builder.append(", ").append(name.equals(basin) ? "area " + basin : name);
	}

	return builder.toString();
    }

    /**
     * @param basin e.g. "CRNOMORSKI SLIV"
     * @return e.g. "Black Sea basin"
     */
    static String getBasinName(String basin) {

	switch (basin.toUpperCase()) {
	case "CRNOMORSKI SLIV":
	    return "Black Sea basin";
	case "JADRANSKI SLIV":
	    return "Adriatic Sea basin";
	default:
	    return basin;
	}
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(DHMZEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getProperty().map(DHMZProperty::getLocalName).orElse(null));
	addKeyword(keywords, entity.getString(DHMZClient.RIVER));
	addKeyword(keywords, entity.getString(DHMZClient.SUB_BASIN));
	addKeyword(keywords, entity.getString(DHMZClient.BASIN));
	addKeyword(keywords, getBasinName(entity.getString(DHMZClient.BASIN)));

	addKeyword(keywords, COUNTRY);
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "DHMZ");
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
    private void addPlatform(DHMZEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	StringBuilder description = new StringBuilder();
	description.append("Station ").append(stationName).append(" (").append(entity.getStationCode()).append(")");
	description.append(getWaterbodyDescription(entity)).append(" (").append(COUNTRY).append(")");

	String type = entity.getString(DHMZClient.STATION_TYPE);
	if (!type.isEmpty()) {
	    description.append(", ").append(type);
	}
	if (entity.getObject().optBoolean(DHMZClient.AUTOMATIC, false)) {
	    description.append(", automatic station");
	}

	platform.setDescription(description.toString());
	platform.setMDIdentifierCode(NetProtocolWrapper.DHMZ.getCommonURN() + "/" + entity.getStationCode());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(DHMZEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity) + " - "
		+ entity.getProperty().map(DHMZProperty::getLocalName).orElse(""));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.DHMZ.getCommonURN() + ":" + entity.getProperty().map(DHMZProperty::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(DHMZEntity entity, ExtensionHandler handler) {

	entity.getProperty().ifPresent(property -> {

	    handler.setAttributeUnits(property.getUnitName());
	    handler.setAttributeUnitsAbbreviation(property.getUnit());

	    if (entity.isRealTime()) {
		handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	    } else {
		handler.setTimeInterpolation(property.getInterpolation());
		handler.setTimeResolutionDuration8601("P1D");
		handler.setTimeAggregationDuration8601("P1D");
	    }
	});

	handler.setCountry(COUNTRY);
    }

    /**
     * @param originalMD
     * @return
     */
    private DHMZEntity getEntity(OriginalMetadata originalMD) {

	return new DHMZEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return DHMZ_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	DHMZEntity entity = getEntity(resource.getOriginalMetadata());
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
