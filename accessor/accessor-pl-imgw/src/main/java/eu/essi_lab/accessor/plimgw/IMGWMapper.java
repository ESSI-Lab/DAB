package eu.essi_lab.accessor.plimgw;

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
public class IMGWMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String IMGW_SCHEMA = "https://danepubliczne.imgw.pl/hydro/schema";

    static final String LICENSE = "IMGW-PIB terms of use (Regulamin serwisu danepubliczne.imgw.pl) - https://danepubliczne.imgw.pl/ -"
	    + " The data can be used free of charge, provided that the source is acknowledged: \"Źródłem pochodzenia danych jest"
	    + " IMGW-PIB\" (the source of the data is IMGW-PIB). Commercial use may require an agreement with IMGW-PIB. Real time"
	    + " data are not verified.";

    static final String ORGANIZATION = "Institute of Meteorology and Water Management - National Research Institute (IMGW-PIB)";

    /**
     * Resolution of the observations of the archive
     */
    static final String RESOLUTION = "P1D";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	IMGWEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(IMGWEntity entity, GSSource source) {

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

	String stationName = entity.getStationName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(IMGWClient.BEGIN, null);
	String end = object.optString(IMGWClient.END, null);

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

	String river = entity.getString(IMGWClient.RIVER);
	coreMetadata.setTitle(stationName + (river.isEmpty() ? "" : " (" + river + ")") + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " at station " + stationName + " ("
		+ stationCode + ")" + getLocationDescription(entity) + " from the public data service of the " + ORGANIZATION
		+ " of Poland. " + getContentDescription(entity));

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(IMGWClient.LAT) && object.has(IMGWClient.LON) && !object.isNull(IMGWClient.LAT)
		&& !object.isNull(IMGWClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(IMGWClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(IMGWClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.IMGW.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return e.g. "Water level"
     */
    static String getPropertyName(IMGWEntity entity) {

	return entity.getProperty().map(IMGWProperty::getLabel).orElse("");
    }

    /**
     * @param entity
     * @return the property name, with the IMGW name, the unit and the gauge zero of the water level
     */
    static String getPropertyDescription(IMGWEntity entity) {

	if (entity.getProperty().isEmpty()) {
	    return "";
	}

	IMGWProperty property = entity.getProperty().get();

	String ret = property.getLabel() + " (" + property.getPolishName() + ", " + property.getUnit() + ")";

	String gaugeZero = entity.getString(IMGWClient.GAUGE_ZERO);
	if (property == IMGWProperty.H && !gaugeZero.isEmpty()) {
	    ret += " referred to the gauge zero at " + gaugeZero + " m a.s.l.";
	}

	return ret;
    }

    /**
     * @param entity
     * @return
     */
    private static String getLocationDescription(IMGWEntity entity) {

	String river = entity.getString(IMGWClient.RIVER);
	String km = entity.getString(IMGWClient.RIVER_KM);
	String voivodeship = entity.getString(IMGWClient.VOIVODESHIP);

	StringBuilder builder = new StringBuilder();
	if (!river.isEmpty()) {
	    builder.append(", river ").append(river);
	    if (!km.isEmpty()) {
		builder.append(" (km ").append(km).append(")");
	    }
	}
	if (!voivodeship.isEmpty()) {
	    builder.append(", voivodeship ").append(voivodeship);
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @return the description of the sources of the observations
     */
    static String getContentDescription(IMGWEntity entity) {

	String archiveBegin = entity.getString(IMGWClient.ARCHIVE_BEGIN);
	String archiveEnd = entity.getString(IMGWClient.ARCHIVE_END);
	String latest = entity.getString(IMGWClient.LATEST);

	StringBuilder builder = new StringBuilder();

	if (!archiveBegin.isEmpty() && !archiveEnd.isEmpty()) {
	    builder.append("Daily validated values from " + archiveBegin.substring(0, 10) + " to " + archiveEnd.substring(0, 10)
		    + " are taken from the archive of hydrological data, published by hydrological year (November-October) with a"
		    + " delay of about one year.");
	}

	if (!latest.isEmpty()) {
	    if (builder.length() > 0) {
		builder.append(" ");
	    }
	    builder.append("The real time service publishes only the latest (not verified) observation (" + latest
		    + " at harvesting time), hence the observations between the end of the archive and the latest one are not"
		    + " available.");
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(IMGWEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getProperty().map(IMGWProperty::getPolishName).orElse(""));
	addKeyword(keywords, entity.getString(IMGWClient.RIVER));
	addKeyword(keywords, entity.getString(IMGWClient.VOIVODESHIP));

	addKeyword(keywords, "Poland");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "IMGW-PIB");
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
    private void addPlatform(IMGWEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	String foundation = entity.getString(IMGWClient.FOUNDATION_YEAR);

	platform.setDescription("Station " + stationName + " (" + entity.getStationCode() + ")" + getLocationDescription(entity)
		+ " (Poland)" + (foundation.isEmpty() ? "" : ", established in " + foundation));
	platform.setMDIdentifierCode(NetProtocolWrapper.IMGW.getCommonURN() + "/" + entity.getStationCode());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(IMGWEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.IMGW.getCommonURN() + ":" + entity.getProperty().map(IMGWProperty::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(IMGWEntity entity, ExtensionHandler handler) {

	entity.getProperty().ifPresent(property -> {
	    handler.setAttributeUnits(property.getUnitName());
	    handler.setAttributeUnitsAbbreviation(property.getUnit());
	});

	handler.setCountry("Poland");
	handler.setTimeResolutionDuration8601(RESOLUTION);
	handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
    }

    /**
     * @param originalMD
     * @return
     */
    private IMGWEntity getEntity(OriginalMetadata originalMD) {

	return new IMGWEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return IMGW_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	IMGWEntity entity = getEntity(resource.getOriginalMetadata());
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
