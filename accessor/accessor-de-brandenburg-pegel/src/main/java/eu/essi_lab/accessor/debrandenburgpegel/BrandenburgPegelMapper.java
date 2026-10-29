package eu.essi_lab.accessor.debrandenburgpegel;

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
public class BrandenburgPegelMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String BRANDENBURG_PEGEL_SCHEMA = "https://pegelportal.brandenburg.de/schema";

    static final String LICENSE = "Datenlizenz Deutschland - Zero - Version 2.0 (dl-de/zero-2-0) - https://www.govdata.de/dl-de/zero-2-0 -"
	    + " Source: Pegelportal Brandenburg (https://pegelportal.brandenburg.de/), Landesamt für Umwelt Brandenburg."
	    + " The values are raw data (Rohdaten), not checked. See also the disclaimer"
	    + " https://lfu.brandenburg.de/lfu/de/service/impressum/haftungsausschluss/";

    static final String ORGANIZATION = "Landesamt für Umwelt Brandenburg (LfU)";

    /**
     * Resolution of the observations
     */
    static final String RESOLUTION = "PT15M";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	BrandenburgPegelEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(BrandenburgPegelEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationNumber = entity.getStationNumber();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationNumber));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String stationName = entity.getStationName();
	String propertyName = getPropertyName(entity);
	String begin = object.optString(BrandenburgPegelClient.BEGIN, null);
	String end = object.optString(BrandenburgPegelClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(ORGANIZATION);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	String operator = entity.getString(BrandenburgPegelClient.OPERATOR);
	if (!operator.isEmpty() && !operator.startsWith("LfU Brandenburg")) {
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

	coreMetadata.setTitle(stationName + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " at gauge " + stationName + " ("
		+ stationNumber + ")" + getWaterbodyDescription(entity) + ", from the Pegelportal of the " + ORGANIZATION
		+ " (Germany). Raw data (Rohdaten) with a 15 minutes resolution; the portal keeps only the observations of the last year"
		+ " (rolling window), the begin of the series moves accordingly.");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(BrandenburgPegelClient.LAT) && object.has(BrandenburgPegelClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(BrandenburgPegelClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(BrandenburgPegelClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getOnlineName(), //
		source.getEndpoint(), //
		NetProtocolWrapper.BRANDENBURG_PEGEL.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return e.g. "Water level"
     */
    static String getPropertyName(BrandenburgPegelEntity entity) {

	return entity.getParameter().map(BrandenburgPegelParameter::getLabel).orElse(entity.getString(BrandenburgPegelClient.PARAMETER));
    }

    /**
     * @param entity
     * @return the property name, with the original name and the unit, e.g. "water level (Wasserstand, cm above the gauge zero)"
     */
    static String getPropertyDescription(BrandenburgPegelEntity entity) {

	String name = getPropertyName(entity).toLowerCase();

	return entity.getParameter().map(p -> {
	    String unit = p == BrandenburgPegelParameter.W ? "cm above the gauge zero (Pegelnullpunkt)" : p.getUnit();
	    return name + " (" + p.getOriginalName() + ", " + unit + ")";
	}).orElse(name);
    }

    /**
     * @param entity
     * @return
     */
    private static String getWaterbodyDescription(BrandenburgPegelEntity entity) {

	String waterbody = entity.getString(BrandenburgPegelClient.WATERBODY);
	String riverKm = entity.getString(BrandenburgPegelClient.RIVER_KM);
	String position = entity.getString(BrandenburgPegelClient.RIVER_POSITION);
	String basin = entity.getString(BrandenburgPegelClient.RIVER_BASIN);
	String operator = entity.getString(BrandenburgPegelClient.OPERATOR);

	StringBuilder builder = new StringBuilder();
	if (!waterbody.isEmpty()) {
	    builder.append(" on ").append(waterbody);
	}
	if (!riverKm.isEmpty()) {
	    builder.append(", ").append(riverKm).append(" km");
	    if (!position.isEmpty()) {
		builder.append(" ").append(position);
	    }
	}
	if (!basin.isEmpty()) {
	    builder.append(", river basin ").append(basin);
	}
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
    private void addKeywords(BrandenburgPegelEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	entity.getParameter().ifPresent(p -> addKeyword(keywords, p.getOriginalName()));
	addKeyword(keywords, entity.getString(BrandenburgPegelClient.WATERBODY));
	addKeyword(keywords, entity.getString(BrandenburgPegelClient.RIVER_BASIN));

	addKeyword(keywords, "Brandenburg");
	addKeyword(keywords, "Germany");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "Pegelportal Brandenburg");
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
    private void addPlatform(BrandenburgPegelEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	platform.setDescription("Gauge " + stationName + " (" + entity.getStationNumber() + ")" + getWaterbodyDescription(entity)
		+ " (Brandenburg, Germany)");
	platform.setMDIdentifierCode(NetProtocolWrapper.BRANDENBURG_PEGEL.getCommonURN() + "/" + entity.getStationNumber());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(BrandenburgPegelEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.BRANDENBURG_PEGEL.getCommonURN() + ":"
		+ entity.getParameter().map(BrandenburgPegelParameter::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(BrandenburgPegelEntity entity, ExtensionHandler handler) {

	entity.getParameter().ifPresent(p -> {
	    handler.setAttributeUnits(p.getUnitName());
	    handler.setAttributeUnitsAbbreviation(p.getUnit());
	});

	handler.setCountry("Germany");
	handler.setTimeResolutionDuration8601(RESOLUTION);
	handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
    }

    /**
     * @param originalMD
     * @return
     */
    private BrandenburgPegelEntity getEntity(OriginalMetadata originalMD) {

	return new BrandenburgPegelEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return BRANDENBURG_PEGEL_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	BrandenburgPegelEntity entity = getEntity(resource.getOriginalMetadata());
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
