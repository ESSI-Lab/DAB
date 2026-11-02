package eu.essi_lab.accessor.ittrentinofloods;

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
public class TrentinoFloodsMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String TRENTINO_FLOODS_SCHEMA = "https://www.floods.it/public/Opendata/schema";

    static final String LICENSE = "Creative Commons Attribution 4.0 International (CC BY 4.0) - Source: Provincia autonoma di Trento"
	    + " - Servizio Prevenzione Rischi e Centrale Unica di Emergenza, Ufficio Dighe (https://dati.trentino.it/dataset/"
	    + "rilevamento-sensori-idrometrici-livello, https://dati.trentino.it/dataset/rilevamento-sensori-idrometrici-portata-calcolata)."
	    + " Data are not validated.";

    static final String ORGANIZATION = "Provincia autonoma di Trento - Servizio Prevenzione Rischi e Centrale Unica di Emergenza,"
	    + " Ufficio Dighe";

    /**
     * Resolution of the observations
     */
    static final String RESOLUTION = "PT1H";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	TrentinoFloodsEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(TrentinoFloodsEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationName = entity.getStationName();

	// the level and discharge sensors of a station share the station name
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), "station:" + stationName));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String propertyName = getPropertyName(entity);
	String begin = object.optString(TrentinoFloodsClient.BEGIN, null);
	String end = object.optString(TrentinoFloodsClient.END, null);

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

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " measured by the hydrometric sensor "
		+ entity.getSensorId() + " (" + entity.getString(TrentinoFloodsClient.TYPE) + ") at station " + stationName
		+ getWaterbodyDescription(entity) + ", from the open data of the " + ORGANIZATION + ". The provider publishes only the"
		+ " hourly values of the last 7 days (rolling window, updated to the previous day); values are not validated.");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	    setIndeterminatePosition(dataset);
	}

	if (object.has(TrentinoFloodsClient.LAT) && object.has(TrentinoFloodsClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(TrentinoFloodsClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(TrentinoFloodsClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.TRENTINO_FLOODS.getCommonURN(), //
		"download");

	return dataset;
    }

    /**
     * @param entity
     * @return e.g. "Water level"
     */
    static String getPropertyName(TrentinoFloodsEntity entity) {

	return entity.getProperty().map(TrentinoFloodsProperty::getLabel).orElse(entity.getString(TrentinoFloodsClient.TYPE));
    }

    /**
     * @param entity
     * @return e.g. "water level (Livello)"
     */
    static String getPropertyDescription(TrentinoFloodsEntity entity) {

	return entity.getProperty().map(p -> {

	    String description = p.getLabel().toLowerCase() + " (" + p.getOriginalName() + ")";
	    if (p == TrentinoFloodsProperty.DISCHARGE) {
		description += " calculated from the water level";
	    }
	    return description;

	}).orElse(entity.getString(TrentinoFloodsClient.TYPE));
    }

    /**
     * @param entity
     * @return
     */
    private static String getWaterbodyDescription(TrentinoFloodsEntity entity) {

	String waterbody = entity.getString(TrentinoFloodsClient.WATERBODY);
	String basin = entity.getString(TrentinoFloodsClient.BASIN);

	StringBuilder builder = new StringBuilder();
	if (!waterbody.isEmpty()) {
	    builder.append(", ").append(waterbody);
	}
	if (!basin.isEmpty()) {
	    builder.append(", basin ").append(basin.replace("\\", "/"));
	}

	return builder.toString();
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(TrentinoFloodsEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	entity.getProperty().ifPresent(p -> addKeyword(keywords, p.getOriginalName()));
	addKeyword(keywords, entity.getString(TrentinoFloodsClient.TYPE));
	addKeyword(keywords, entity.getString(TrentinoFloodsClient.WATERBODY));

	for (String basin : entity.getString(TrentinoFloodsClient.BASIN).split("\\\\")) {
	    addKeyword(keywords, basin.trim());
	}

	addKeyword(keywords, "Trentino");
	addKeyword(keywords, "Italy");
	addKeyword(keywords, "Hydrology");
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
    private void addPlatform(TrentinoFloodsEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	String stationName = entity.getStationName();

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	platform.setDescription("Hydrometric station " + stationName + getWaterbodyDescription(entity) + " (Trentino, Italy)");
	platform.setMDIdentifierCode(NetProtocolWrapper.TRENTINO_FLOODS.getCommonURN() + "/" + stationName.replace(" ", ""));

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(TrentinoFloodsEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(getPropertyDescription(entity));
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.TRENTINO_FLOODS.getCommonURN() + ":"
		+ entity.getProperty().map(TrentinoFloodsProperty::name).orElse(entity.getDataset()));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(TrentinoFloodsEntity entity, ExtensionHandler handler) {

	entity.getProperty().ifPresent(p -> {
	    handler.setAttributeUnits(p.getUnitName());
	    handler.setAttributeUnitsAbbreviation(p.getUnitAbbreviation());
	});

	handler.setCountry("Italy");
	handler.setTimeResolutionDuration8601(RESOLUTION);
	handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
    }

    /**
     * @param originalMD
     * @return
     */
    private TrentinoFloodsEntity getEntity(OriginalMetadata originalMD) {

	return new TrentinoFloodsEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return TRENTINO_FLOODS_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	TrentinoFloodsEntity entity = getEntity(resource.getOriginalMetadata());
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
