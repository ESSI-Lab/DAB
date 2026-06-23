package eu.essi_lab.accessor.ukhydrology;

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
public class UKHydrologyMapper extends AbstractResourceMapper {

    /**
     * 
     */
    public static final String UK_HYDROLOGY_SCHEMA = "https://environment.data.gov.uk/hydrology/schema";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	UKHydrologyEntity entity = getEntity(originalMD);
	return mapMeasure(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapMeasure(UKHydrologyEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationGuid = UKHydrologyClient.extractLabel(object, UKHydrologyClient.STATION_GUID);
	if (stationGuid == null) {
	    stationGuid = UKHydrologyClient.getStationGuid(entity);
	}
	if (stationGuid != null) {
	    dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationGuid));
	}

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);

	String stationLabel = object.optString(UKHydrologyClient.STATION_LABEL, "");
	String measureLabel = object.optString(UKHydrologyClient.MEASURE_LABEL, "");
	String measureNotation = object.optString(UKHydrologyClient.MEASURE_NOTATION, "");
	String riverName = object.optString(UKHydrologyClient.RIVER_NAME, "");
	String valueStatistic = UKHydrologyClient.extractLabel(object, UKHydrologyClient.VALUE_STATISTIC);
	if (valueStatistic == null) {
	    valueStatistic = object.optString(UKHydrologyClient.VALUE_TYPE, "");
	}
	String observedProperty = UKHydrologyClient.extractLabel(object, UKHydrologyClient.OBSERVED_PROPERTY);
	if (observedProperty == null) {
	    observedProperty = object.optString(UKHydrologyClient.PARAMETER_NAME, "");
	}
	String from = object.optString(UKHydrologyClient.FROM, null);
	String to = object.optString(UKHydrologyClient.TO, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName("Environment Agency");
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	addKeywords(object, keywords);

	coreMetadata.setTitle(stationLabel + " - " + measureLabel);

	coreMetadata.setAbstract("Timeseries " + measureLabel + " with data from station " + stationLabel + ", river " + riverName
		+ " (UK Environment Agency Hydrology API)");

	dataset.getExtensionHandler().setCountry("United Kingdom");

	if (from != null && to != null) {
	    coreMetadata.addTemporalExtent(from, to);
	}

	if (object.has(UKHydrologyClient.STATION_LAT) && object.has(UKHydrologyClient.STATION_LONG)) {
	    BigDecimal lat = new BigDecimal(object.get(UKHydrologyClient.STATION_LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(UKHydrologyClient.STATION_LONG).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(object, stationGuid, stationLabel, coreMetadata);
	addCoverageDescription(observedProperty, coreMetadata, measureNotation);
	addExtensions(object, dataset.getExtensionHandler(), valueStatistic);

	coreMetadata.addDistributionOnlineResource(//
		measureNotation, //
		source.getEndpoint(), //
		NetProtocolWrapper.UK_HYDROLOGY.getCommonURN(), //
		"download");

	return dataset;
    }

    /**
     * @param object
     * @param keywords
     */
    private void addKeywords(JSONObject object, Keywords keywords) {

	addKeyword(keywords, object.optString(UKHydrologyClient.STATION_LABEL, null));
	addKeyword(keywords, object.optString(UKHydrologyClient.RIVER_NAME, null));
	addKeyword(keywords, object.optString(UKHydrologyClient.PARAMETER_NAME, null));
	addKeyword(keywords, object.optString(UKHydrologyClient.PARAMETER, null));
	addKeyword(keywords, UKHydrologyClient.extractLabel(object, UKHydrologyClient.VALUE_STATISTIC));
	addKeyword(keywords, object.optString(UKHydrologyClient.PERIOD_NAME, null));
	addKeyword(keywords, UKHydrologyClient.extractLabel(object, UKHydrologyClient.OBSERVED_PROPERTY));
	addKeyword(keywords, "United Kingdom");
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
     * @param object
     * @param stationGuid
     * @param stationLabel
     * @param coreMetadata
     */
    private void addPlatform(JSONObject object, String stationGuid, String stationLabel, CoreMetadata coreMetadata) {

	if (stationGuid == null) {
	    stationGuid = object.optString(UKHydrologyClient.STATION_GUID, "");
	}

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(stationLabel);
	platform.setCitation(citation);

	platform.setDescription("Station " + stationLabel + " (United Kingdom)");
	platform.setMDIdentifierCode(NetProtocolWrapper.UK_HYDROLOGY.getCommonURN() + "/" + stationGuid);

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param observedProperty
     * @param coreMetadata
     * @param measureNotation
     */
    private void addCoverageDescription(String observedProperty, CoreMetadata coreMetadata, String measureNotation) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(observedProperty);
	coverageDescription.setAttributeDescription(observedProperty);
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.UK_HYDROLOGY.getCommonURN() + ":" + measureNotation);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param object
     * @param handler
     * @param valueStatistic
     */
    private void addExtensions(JSONObject object, ExtensionHandler handler, String valueStatistic) {

	String unitName = object.optString(UKHydrologyClient.UNIT_NAME, "");
	handler.setAttributeUnits(unitName);
	handler.setAttributeUnitsAbbreviation(unitName);
	handler.setCountry("United Kingdom");

	if (valueStatistic != null && !valueStatistic.isEmpty()) {
	    handler.setTimeInterpolation(valueStatistic);
	}

	if (object.has(UKHydrologyClient.PERIOD)) {
	    int periodSeconds = object.getInt(UKHydrologyClient.PERIOD);
	    String duration = periodToIso8601(periodSeconds);
	    handler.setTimeResolutionDuration8601(duration);
	    if (!isInstantaneous(valueStatistic)) {
		handler.setTimeAggregationDuration8601(duration);
	    }
	}
    }

    /**
     * @param valueStatistic
     * @return
     */
    private boolean isInstantaneous(String valueStatistic) {

	return valueStatistic != null && valueStatistic.equalsIgnoreCase("instantaneous");
    }

    /**
     * @param periodSeconds
     * @return
     */
    private String periodToIso8601(int periodSeconds) {

	if (periodSeconds % 86400 == 0) {
	    int days = periodSeconds / 86400;
	    return "P" + days + "D";
	}
	if (periodSeconds % 3600 == 0) {
	    int hours = periodSeconds / 3600;
	    return "PT" + hours + "H";
	}
	if (periodSeconds % 60 == 0) {
	    int minutes = periodSeconds / 60;
	    return "PT" + minutes + "M";
	}
	return "PT" + periodSeconds + "S";
    }

    /**
     * @param originalMD
     * @return
     */
    private UKHydrologyEntity getEntity(OriginalMetadata originalMD) {

	return new UKHydrologyEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return UK_HYDROLOGY_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	UKHydrologyEntity entity = getEntity(resource.getOriginalMetadata());
	String entityId = entity.getObject().optString(UKHydrologyClient.MEASURE_NOTATION, null);
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
