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
    public static final String TRENTINO_FLOODS_SCHEMA = "https://www.floods.it/api/v1/monitoring/schema";

    static final String LICENSE = "Creative Commons Attribution 4.0 International (CC BY 4.0) - Source: Provincia autonoma di Trento"
	    + " - Servizio Prevenzione Rischi e Centrale Unica di Emergenza, Ufficio Dighe (https://www.floods.it/public/DatiLive.php)."
	    + " Data are acquired in real time and are not validated; they are released without any warranty.";

    static final String ORGANIZATION = "Provincia autonoma di Trento - Servizio Prevenzione Rischi e Centrale Unica di Emergenza,"
	    + " Ufficio Dighe";

    /**
     * Resolution of the observations
     */
    static final String RESOLUTION = "PT15M";

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

	String site = entity.getSite();

	// the level and discharge series of a site share the site name (the coordinates of the two may differ slightly)
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), "site:" + site));

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

	String waterbody = entity.getString(TrentinoFloodsClient.WATERBODY);

	coreMetadata.setTitle(site + (waterbody.isEmpty() ? "" : " (" + waterbody + ")") + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + getPropertyDescription(entity) + " (series " + entity.getSeriesId() + ") at site "
		+ site + getWaterbodyDescription(entity) + ", from the real time hydrometric monitoring of the " + ORGANIZATION
		+ ". The provider publishes only the values of the last 7 days (rolling window, updated in real time); values are not"
		+ " validated.");

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

	return entity.getProperty().map(TrentinoFloodsProperty::getLabel).orElse(entity.getString(TrentinoFloodsClient.QUANTITY));
    }

    /**
     * @param entity
     * @return e.g. "water level (Livello idrometrico)"
     */
    static String getPropertyDescription(TrentinoFloodsEntity entity) {

	return entity.getProperty().map(p -> {

	    String description = p.getLabel().toLowerCase() + " (" + p.getOriginalName() + ")";
	    if (p == TrentinoFloodsProperty.DISCHARGE) {
		description += " calculated from the water level by means of a rating curve";
	    }
	    return description;

	}).orElse(entity.getString(TrentinoFloodsClient.QUANTITY));
    }

    /**
     * @param entity
     * @return
     */
    private static String getWaterbodyDescription(TrentinoFloodsEntity entity) {

	String waterbody = entity.getString(TrentinoFloodsClient.WATERBODY);

	return waterbody.isEmpty() ? "" : ", " + waterbody;
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(TrentinoFloodsEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getSite());
	addKeyword(keywords, propertyName);
	entity.getProperty().ifPresent(p -> addKeyword(keywords, p.getOriginalName()));
	addKeyword(keywords, entity.getString(TrentinoFloodsClient.QUANTITY));
	addKeyword(keywords, entity.getString(TrentinoFloodsClient.WATERBODY));

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

	String site = entity.getSite();

	Citation citation = new Citation();
	citation.setTitle(site);
	platform.setCitation(citation);

	platform.setDescription("Hydrometric site " + site + getWaterbodyDescription(entity) + " (Trentino, Italy)");
	platform.setMDIdentifierCode(NetProtocolWrapper.TRENTINO_FLOODS.getCommonURN() + "/" + site.replace(" ", ""));

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
		+ entity.getProperty().map(TrentinoFloodsProperty::name).orElse(entity.getString(TrentinoFloodsClient.QUANTITY)));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(TrentinoFloodsEntity entity, ExtensionHandler handler) {

	String uom = entity.getString(TrentinoFloodsClient.UOM);

	entity.getProperty().ifPresentOrElse(p -> {
	    handler.setAttributeUnits(p.getUnitName());
	    handler.setAttributeUnitsAbbreviation(uom.isEmpty() ? p.getUnitAbbreviation() : uom);
	}, () -> {
	    if (!uom.isEmpty()) {
		handler.setAttributeUnits(uom);
		handler.setAttributeUnitsAbbreviation(uom);
	    }
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
