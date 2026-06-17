package eu.essi_lab.accessor.chexistenzbafu;

import java.math.BigDecimal;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
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
public class CHExistenzBafuMapper extends AbstractResourceMapper {

    /**
     * 
     */
    public static final String CH_EXISTENZ_BAFU_SCHEMA = "https://api.existenz.ch/apiv1/hydro/schema";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	CHExistenzBafuEntity entity = getEntity(originalMD);
	return mapMeasure(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapMeasure(CHExistenzBafuEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String locationId = object.optString(CHExistenzBafuClient.LOCATION_ID, "");
	if (!locationId.isEmpty()) {
	    dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .setParentIdentifier(decorateIdentifier(source.getEndpoint(), locationId));
	}

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);

	String stationLabel = object.optString(CHExistenzBafuClient.LOCATION_NAME, "");
	String parameterName = object.optString(CHExistenzBafuClient.PARAMETER_NAME, "");
	String parameterCode = object.optString(CHExistenzBafuClient.PARAMETER, "");
	String waterBodyName = object.optString(CHExistenzBafuClient.WATER_BODY_NAME, "");
	String waterBodyType = object.optString(CHExistenzBafuClient.WATER_BODY_TYPE, "");
	String measureNotation = object.optString(CHExistenzBafuClient.MEASURE_NOTATION, "");
	String from = object.optString(CHExistenzBafuClient.FROM, null);
	String to = object.optString(CHExistenzBafuClient.TO, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName("Swiss Federal Office for the Environment (BAFU)");
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	addKeywords(object, keywords);

	coreMetadata.setTitle(stationLabel + " - " + parameterName);

	coreMetadata.setAbstract("Timeseries " + parameterName + " from station " + stationLabel + ", water body " + waterBodyName
		+ " (" + waterBodyType + ", Swiss BAFU Hydrology API via Existenz.ch)");

	dataset.getExtensionHandler().setCountry("Switzerland");

	if (from != null && to != null) {
	    coreMetadata.addTemporalExtent(from, to);
	}

	if (object.has(CHExistenzBafuClient.STATION_LAT) && object.has(CHExistenzBafuClient.STATION_LON)) {
	    BigDecimal lat = new BigDecimal(object.get(CHExistenzBafuClient.STATION_LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(CHExistenzBafuClient.STATION_LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(locationId, stationLabel, coreMetadata);
	addCoverageDescription(parameterName, coreMetadata, measureNotation);
	addExtensions(object, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		measureNotation, //
		source.getEndpoint(), //
		NetProtocolWrapper.CH_EXISTENZ_BAFU.getCommonURN(), //
		"download");

	return dataset;
    }

    /**
     * @param object
     * @param keywords
     */
    private void addKeywords(JSONObject object, Keywords keywords) {

	addKeyword(keywords, object.optString(CHExistenzBafuClient.LOCATION_NAME, null));
	addKeyword(keywords, object.optString(CHExistenzBafuClient.WATER_BODY_NAME, null));
	addKeyword(keywords, object.optString(CHExistenzBafuClient.WATER_BODY_TYPE, null));
	addKeyword(keywords, object.optString(CHExistenzBafuClient.PARAMETER_NAME, null));
	addKeyword(keywords, object.optString(CHExistenzBafuClient.PARAMETER, null));
	addKeyword(keywords, "Switzerland");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "BAFU");
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
     * @param locationId
     * @param stationLabel
     * @param coreMetadata
     */
    private void addPlatform(String locationId, String stationLabel, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(stationLabel);
	platform.setCitation(citation);

	platform.setDescription("Station " + stationLabel + " (Switzerland)");
	platform.setMDIdentifierCode(NetProtocolWrapper.CH_EXISTENZ_BAFU.getCommonURN() + "/" + locationId);

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param parameterName
     * @param coreMetadata
     * @param measureNotation
     */
    private void addCoverageDescription(String parameterName, CoreMetadata coreMetadata, String measureNotation) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(parameterName);
	coverageDescription.setAttributeDescription(parameterName);
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.CH_EXISTENZ_BAFU.getCommonURN() + ":" + measureNotation);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param object
     * @param handler
     */
    private void addExtensions(JSONObject object, ExtensionHandler handler) {

	String unitName = object.optString(CHExistenzBafuClient.PARAMETER_UNIT, "");
	handler.setAttributeUnits(unitName);
	handler.setAttributeUnitsAbbreviation(unitName);
	handler.setCountry("Switzerland");
	handler.setTimeInterpolation("instantaneous");
	handler.setTimeResolutionDuration8601("PT10M");
    }

    /**
     * @param originalMD
     * @return
     */
    private CHExistenzBafuEntity getEntity(OriginalMetadata originalMD) {

	return new CHExistenzBafuEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CH_EXISTENZ_BAFU_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	CHExistenzBafuEntity entity = getEntity(resource.getOriginalMetadata());
	String entityId = entity.getObject().optString(CHExistenzBafuClient.MEASURE_NOTATION, null);
	return decorateIdentifier(resource.getSource().getEndpoint(), entityId);
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
