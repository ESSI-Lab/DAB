package eu.essi_lab.accessor.mxconagua;

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
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * @author boldrini
 */
public class CONAGUAMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String CONAGUA_SCHEMA = "https://sih.conagua.gob.mx/basedatos/Hidros/schema";

    /**
     * No licence is stated by the SIH site for its data files (the only Creative Commons link of the site is the attribution of the
     * OpenTopoMap base map)
     */
    static final String LICENSE = "Data published by the Comisión Nacional del Agua (CONAGUA), Sistema de Información Hidrológica"
	    + " (SIH) - https://sih.conagua.gob.mx/ - No explicit licence is stated by the provider: please cite CONAGUA SIH as the"
	    + " source of the data.";

    static final String ORGANIZATION = "Comisión Nacional del Agua (CONAGUA)";

    /**
     * Resolution of the values of the station files
     */
    static final String RESOLUTION = "P1D";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	CONAGUAEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(CONAGUAEntity entity, GSSource source) {

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
	String begin = object.optString(CONAGUAClient.BEGIN, null);
	String end = object.optString(CONAGUAClient.END, null);

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

	coreMetadata.setTitle(stationName + " - " + propertyName);

	coreMetadata.setAbstract("Daily time series of " + getPropertyDescription(entity) + " at hydrometric station " + stationName
		+ " (" + stationCode + ")" + getLocationDescription(entity) + ", from the historical database of the Sistema de"
		+ " Información Hidrológica (SIH) of the " + ORGANIZATION + " of Mexico. The station files are updated weekly by the"
		+ " provider.");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(CONAGUAClient.LAT) && object.has(CONAGUAClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(CONAGUAClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(CONAGUAClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.CONAGUA.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return e.g. "Discharge (Gasto)"
     */
    static String getPropertyName(CONAGUAEntity entity) {

	return entity.getProperty().map(p -> p.getLabel() + " (" + p.getColumn() + ")").orElse(entity.getString(CONAGUAClient.PROPERTY));
    }

    /**
     * @param entity
     * @return e.g. "discharge (Gasto, m³/s)"
     */
    static String getPropertyDescription(CONAGUAEntity entity) {

	String unit = entity.getUnit();

	return entity.getProperty().//
		map(p -> p.getLabel().toLowerCase() + " (" + p.getColumn() + (unit.isEmpty() ? "" : ", " + unit) + ")").//
		orElse(entity.getString(CONAGUAClient.PROPERTY));
    }

    /**
     * @param entity
     * @return
     */
    private static String getLocationDescription(CONAGUAEntity entity) {

	String municipality = entity.getString(CONAGUAClient.MUNICIPALITY);
	String state = entity.getString(CONAGUAClient.STATE);
	String basin = entity.getString(CONAGUAClient.BASIN);
	String region = entity.getString(CONAGUAClient.HYDROLOGICAL_REGION);

	StringBuilder builder = new StringBuilder();
	if (!municipality.isEmpty()) {
	    builder.append(", municipality ").append(municipality);
	}
	if (!state.isEmpty()) {
	    builder.append(", state ").append(state);
	}
	if (!basin.isEmpty()) {
	    builder.append(", basin ").append(basin);
	}
	if (!region.isEmpty()) {
	    builder.append(", hydrological region ").append(region);
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(CONAGUAEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	entity.getProperty().ifPresent(p -> {
	    addKeyword(keywords, p.getLabel());
	    addKeyword(keywords, p.getColumn());
	});
	addKeyword(keywords, entity.getString(CONAGUAClient.STATE));
	addKeyword(keywords, entity.getString(CONAGUAClient.MUNICIPALITY));
	addKeyword(keywords, entity.getString(CONAGUAClient.BASIN));

	addKeyword(keywords, "Mexico");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "CONAGUA");
	addKeyword(keywords, "SIH");
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
    private void addPlatform(CONAGUAEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	String altitude = entity.getString(CONAGUAClient.ALTITUDE);

	platform.setDescription("Hydrometric station " + stationName + " (" + entity.getStationCode() + ")"
		+ getLocationDescription(entity) + (altitude.isEmpty() ? "" : ", altitude " + altitude + " m") + " (Mexico)");
	platform.setMDIdentifierCode(NetProtocolWrapper.CONAGUA.getCommonURN() + "/" + entity.getStationCode());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(CONAGUAEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.CONAGUA.getCommonURN() + ":" + entity.getProperty().map(CONAGUAProperty::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(CONAGUAEntity entity, ExtensionHandler handler) {

	String unit = entity.getUnit();

	handler.setAttributeUnits(getUnitName(unit, entity));
	handler.setAttributeUnitsAbbreviation(getUnitAbbreviation(unit, entity));
	handler.setCountry("Mexico");
	handler.setTimeResolutionDuration8601(RESOLUTION);
    }

    /**
     * @param unit the unit written by CONAGUA
     * @param entity
     * @return
     */
    static String getUnitName(String unit, CONAGUAEntity entity) {

	switch (getUnitAbbreviation(unit, entity)) {
	case "m":
	    return "metres";
	case "m3/s":
	    return "cubic metres per second";
	default:
	    return unit;
	}
    }

    /**
     * @param unit the unit written by CONAGUA, e.g. "m³/s"
     * @param entity
     * @return e.g. "m3/s"
     */
    static String getUnitAbbreviation(String unit, CONAGUAEntity entity) {

	if (unit == null || unit.isEmpty()) {
	    return entity.getProperty().map(CONAGUAProperty::getUnitAbbreviation).orElse("");
	}

	return unit.replace("³", "3");
    }

    /**
     * @param originalMD
     * @return
     */
    private CONAGUAEntity getEntity(OriginalMetadata originalMD) {

	return new CONAGUAEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CONAGUA_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	CONAGUAEntity entity = getEntity(resource.getOriginalMetadata());
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
