package eu.essi_lab.accessor.nlrijkswaterstaat;

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
public class RijkswaterstaatMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String RIJKSWATERSTAAT_SCHEMA = "https://ddapi20-waterwebservices.rijkswaterstaat.nl/schema";

    static final String LICENSE = "Creative Commons zero (CC0) - https://rijkswaterstaatdata.nl/waterdata/";

    /**
     * Code used by AQUO for "not applicable"
     */
    private static final String NOT_APPLICABLE = "NVT";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	RijkswaterstaatEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(RijkswaterstaatEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String locationCode = entity.getLocationCode();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), locationCode));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String locationName = object.optString(RijkswaterstaatClient.LOCATION_NAME, locationCode);
	String propertyName = getPropertyName(entity);
	String begin = object.optString(RijkswaterstaatClient.BEGIN, null);
	String end = object.optString(RijkswaterstaatClient.END, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName("Rijkswaterstaat");
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	LegalConstraints constraints = new LegalConstraints();
	constraints.addUseLimitation(LICENSE);
	dataId.addLegalConstraints(constraints);

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);
	addKeywords(entity, keywords);

	coreMetadata.setTitle(locationName + " - " + propertyName);

	coreMetadata.setAbstract("Time series of " + propertyName + " at location " + locationName + " (" + locationCode
		+ ") from the Rijkswaterstaat WaterWebservices");

	dataset.getExtensionHandler().setCountry("Netherlands");

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(RijkswaterstaatClient.LAT) && object.has(RijkswaterstaatClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(RijkswaterstaatClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(RijkswaterstaatClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(object, locationCode, locationName, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	addExtensions(entity, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.RIJKSWATERSTAAT.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return the AQUO description of the observed property, e.g. "Waterhoogte in Oppervlaktewater t.o.v. Normaal Amsterdams Peil in
     *         cm"
     */
    static String getPropertyName(RijkswaterstaatEntity entity) {

	String description = entity.getObject().optString(RijkswaterstaatClient.PROPERTY_DESCRIPTION, "");
	if (!description.isEmpty()) {
	    return description;
	}

	StringBuilder builder = new StringBuilder(entity.getDescription("Grootheid"));
	String parameter = entity.getDescription("Parameter");
	if (isApplicable(entity.getCode("Parameter")) && !parameter.isEmpty()) {
	    builder.append(" ").append(parameter);
	}

	String compartment = entity.getDescription("Compartiment");
	if (!compartment.isEmpty()) {
	    builder.append(" in ").append(compartment);
	}

	return builder.toString().trim();
    }

    /**
     * @param entity
     * @return the AQUO codes identifying the observed property, independently from the location
     */
    static String getPropertyIdentifier(RijkswaterstaatEntity entity) {

	String seriesId = entity.getSeriesId();
	return seriesId.substring(seriesId.indexOf('|') + 1);
    }

    /**
     * @param entity
     * @param keywords
     */
    private void addKeywords(RijkswaterstaatEntity entity, Keywords keywords) {

	JSONObject object = entity.getObject();

	addKeyword(keywords, object.optString(RijkswaterstaatClient.LOCATION_NAME, null));

	for (String key : RijkswaterstaatClient.AQUO_KEYS) {
	    if (isApplicable(entity.getCode(key))) {
		addKeyword(keywords, entity.getDescription(key));
	    }
	}

	addKeyword(keywords, "Netherlands");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "Rijkswaterstaat");
    }

    /**
     * @param code
     * @return
     */
    private static boolean isApplicable(String code) {

	return code != null && !code.isEmpty() && !code.equals(NOT_APPLICABLE);
    }

    /**
     * @param keywords
     * @param value
     */
    private void addKeyword(Keywords keywords, String value) {

	if (value != null && !value.isEmpty() && !value.equals(NOT_APPLICABLE)) {
	    keywords.addKeyword(value);
	}
    }

    /**
     * @param object
     * @param locationCode
     * @param locationName
     * @param coreMetadata
     */
    private void addPlatform(JSONObject object, String locationCode, String locationName, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(locationName);
	platform.setCitation(citation);

	String description = object.optString(RijkswaterstaatClient.LOCATION_DESCRIPTION, "");
	platform.setDescription(description.isEmpty() || description.equals(locationName) ? //
		"Location " + locationName + " (Netherlands)" : //
		"Location " + locationName + ", " + description + " (Netherlands)");
	platform.setMDIdentifierCode(NetProtocolWrapper.RIJKSWATERSTAAT.getCommonURN() + "/" + locationCode);

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(RijkswaterstaatEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(propertyName);
	coverageDescription
		.setAttributeIdentifier(NetProtocolWrapper.RIJKSWATERSTAAT.getCommonURN() + ":" + getPropertyIdentifier(entity));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param entity
     * @param handler
     */
    private void addExtensions(RijkswaterstaatEntity entity, ExtensionHandler handler) {

	String unitCode = entity.getCode("Eenheid");
	String unitName = entity.getDescription("Eenheid");

	handler.setAttributeUnits(unitName.isEmpty() ? unitCode : unitName);
	handler.setAttributeUnitsAbbreviation(unitCode);
	handler.setCountry("Netherlands");
    }

    /**
     * @param originalMD
     * @return
     */
    private RijkswaterstaatEntity getEntity(OriginalMetadata originalMD) {

	return new RijkswaterstaatEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return RIJKSWATERSTAAT_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	RijkswaterstaatEntity entity = getEntity(resource.getOriginalMetadata());
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
