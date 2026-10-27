package eu.essi_lab.accessor.ltlhmt;

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
import java.util.Optional;

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
public class LHMTMapper extends AbstractResourceMapper {

    /**
     *
     */
    public static final String LHMT_SCHEMA = "https://api.meteo.lt/v1/hydro-stations/schema";

    static final String ORGANIZATION = "Lithuanian Hydrometeorological Service under the Ministry of Environment (LHMT)";

    static final String LICENSE = "Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0) -"
	    + " https://creativecommons.org/licenses/by-sa/4.0/ - Data source: " + ORGANIZATION
	    + " (Meteo LT API, https://api.meteo.lt/). The data are provided as is, without any warranty.";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	LHMTEntity entity = getEntity(originalMD);
	return mapSeries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapSeries(LHMTEntity entity, GSSource source) {

	JSONObject object = entity.getObject();
	Optional<LHMTParameter> parameter = entity.getParameter();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationCode = entity.getStationCode();
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		.setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationCode));

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	String propertyName = parameter.map(LHMTParameter::getLabel).orElse("");
	String begin = object.optString(LHMTClient.BEGIN, null);
	String end = object.optString(LHMTClient.END, null);

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

	boolean daily = parameter.map(LHMTParameter::isDaily).orElse(false);

	coreMetadata.setTitle(getStationDescription(entity) + " - " + propertyName + (daily ? " (daily mean)" : ""));

	coreMetadata.setAbstract((daily ? "Daily mean" : "Hourly") + " time series of " + propertyName.toLowerCase() + " at station "
		+ entity.getStationName() + " (" + stationCode + ")" + getWaterBodyDescription(entity) + " from the Meteo LT API of the "
		+ ORGANIZATION + ". "
		+ (daily ? "Historical observations are available from 2000; the data of a year are published from the middle of the following year."
			: "Measured observations are available for the last 30 days."));

	if (begin != null && end != null) {
	    coreMetadata.addTemporalExtent(begin, end);
	}

	if (object.has(LHMTClient.LAT) && object.has(LHMTClient.LON)) {
	    BigDecimal lat = new BigDecimal(object.get(LHMTClient.LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(LHMTClient.LON).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(entity, coreMetadata);
	addCoverageDescription(entity, propertyName, coreMetadata);
	parameter.ifPresent(p -> addExtensions(p, dataset.getExtensionHandler()));

	coreMetadata.addDistributionOnlineResource(//
		entity.getSeriesId(), //
		source.getEndpoint(), //
		NetProtocolWrapper.LHMT.getCommonURN(), //
		"download");

	setIndeterminatePosition(dataset);

	return dataset;
    }

    /**
     * @param entity
     * @return e.g. "Nemajūnų VMS (Nemunas)"
     */
    private static String getStationDescription(LHMTEntity entity) {

	String waterBody = entity.getWaterBody();

	return entity.getStationName() + (waterBody.isEmpty() ? "" : " (" + waterBody + ")");
    }

    /**
     * @param entity
     * @return
     */
    private static String getWaterBodyDescription(LHMTEntity entity) {

	String waterBody = entity.getWaterBody();

	return waterBody.isEmpty() ? "" : ", water body " + waterBody;
    }

    /**
     * @param entity
     * @param propertyName
     * @param keywords
     */
    private void addKeywords(LHMTEntity entity, String propertyName, Keywords keywords) {

	addKeyword(keywords, entity.getStationName());
	addKeyword(keywords, propertyName);
	addKeyword(keywords, entity.getWaterBody());

	addKeyword(keywords, "Lithuania");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "LHMT");
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
    private void addPlatform(LHMTEntity entity, CoreMetadata coreMetadata) {

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(getStationDescription(entity));
	platform.setCitation(citation);

	platform.setDescription(
		"Hydrological station " + entity.getStationName() + " (" + entity.getStationCode() + ")" + getWaterBodyDescription(entity)
			+ " (Lithuania)");
	platform.setMDIdentifierCode(NetProtocolWrapper.LHMT.getCommonURN() + "/" + entity.getStationCode());

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param entity
     * @param propertyName
     * @param coreMetadata
     */
    private void addCoverageDescription(LHMTEntity entity, String propertyName, CoreMetadata coreMetadata) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(propertyName);
	coverageDescription.setAttributeDescription(propertyName);
	coverageDescription.setAttributeIdentifier(
		NetProtocolWrapper.LHMT.getCommonURN() + ":" + entity.getParameter().map(LHMTParameter::name).orElse(""));
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param parameter
     * @param handler
     */
    private void addExtensions(LHMTParameter parameter, ExtensionHandler handler) {

	handler.setAttributeUnits(parameter.getUnitName());
	handler.setAttributeUnitsAbbreviation(parameter.getUnitAbbreviation());
	handler.setCountry("Lithuania");
	handler.setTimeResolutionDuration8601(parameter.getResolution());

	if (parameter.isDaily()) {
	    handler.setTimeInterpolation(InterpolationType.AVERAGE);
	    handler.setTimeAggregationDuration8601("P1D");
	} else {
	    handler.setTimeInterpolation(InterpolationType.CONTINUOUS);
	}
    }

    /**
     * @param originalMD
     * @return
     */
    private LHMTEntity getEntity(OriginalMetadata originalMD) {

	return new LHMTEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return LHMT_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	LHMTEntity entity = getEntity(resource.getOriginalMetadata());
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
