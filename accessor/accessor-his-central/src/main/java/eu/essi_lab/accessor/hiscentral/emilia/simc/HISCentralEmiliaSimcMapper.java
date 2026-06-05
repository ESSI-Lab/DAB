package eu.essi_lab.accessor.hiscentral.emilia.simc;

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

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * Maps ARPAE-SIMC Eve open-data harvest records to ISO19139 / HIS-Central metadata.
 */
public class HISCentralEmiliaSimcMapper extends FileIdentifierMapper {

    public static final String MISSING_VALUE = "-9999";
    public static final String ORGANIZATION = "ARPAE Servizio Idro-Meteo-Clima";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_EMILIA_SIMC_NS_URI;
    }

    public static void setIndeterminatePosition(GSResource gsResource) {
	setIndeterminatePosition(gsResource, TimeUnit.DAYS.toMillis(30));
    }

    /**
     * Builds original metadata for one station–dataset time series.
     */
    static OriginalMetadata create(ArpaeSimcMeteoOpenDataClient.SimcStation station,
	    ArpaeSimcMeteoOpenDataClient.SimcStationSummary summary,
	    ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor descriptor, String datasetResource) {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_EMILIA_SIMC_NS_URI);

	JSONObject json = new JSONObject();
	json.put("station", station.raw());
	json.put("summary", summaryJson(summary));
	if (descriptor != null) {
	    json.put("dataset", descriptor.raw());
	}
	if (datasetResource != null) {
	    json.put("datasetResource", datasetResource);
	}

	originalMetadata.setMetadata(json.toString(4));
	return originalMetadata;
    }

    private static JSONObject summaryJson(ArpaeSimcMeteoOpenDataClient.SimcStationSummary summary) {
	JSONObject o = new JSONObject();
	if (summary.reftimeStart() != null) {
	    o.put("reftime_start", summary.reftimeStart());
	}
	if (summary.reftimeEnd() != null) {
	    o.put("reftime_end", summary.reftimeEnd());
	}
	if (summary.href() != null) {
	    o.put("href", summary.href());
	}
	return o;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	Dataset dataset = new Dataset();
	dataset.setSource(source);
	mapMetadata(originalMD, dataset, source);
	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset, GSSource source) {
	try {
	    JSONObject root = new JSONObject(originalMD.getMetadata());
	    JSONObject stationJson = root.getJSONObject("station");
	    JSONObject summaryJson = root.optJSONObject("summary");
	    JSONObject datasetJson = root.optJSONObject("dataset");
	    String datasetResource = root.optString("datasetResource", null);
	    if (datasetResource != null && datasetResource.isEmpty()) {
		datasetResource = null;
	    }

	    String stationId = stationJson.optString("_id", null);
	    String stationName = stationJson.optString("name", stationId);
	    Double heightM = stationJson.has("height") && !stationJson.isNull("height") ? stationJson.getDouble("height") : null;

	    double[] lonLat = lonLatFromGeometry(stationJson.optJSONObject("geometry"));

	    String bcode = datasetJson != null ? datasetJson.optString("bcode", null) : null;
	    String unit = datasetJson != null ? datasetJson.optString("unit", null) : null;
	    JSONArray timerange = datasetJson != null ? datasetJson.optJSONArray("timerange") : null;

	    String observedProperty = bcode != null ? BCodes.getInstance().getObservedPropertyLabel(bcode) : datasetResource;
	    if (observedProperty == null || observedProperty.isEmpty()) {
		observedProperty = datasetResource != null ? datasetResource : "unknown";
	    }

	    String interpolationLabel = BUFRData.interpolationTypeFromTimerange(timerange);
	    String aggregationPeriod = BUFRData.aggregationPeriodFromTimerange(timerange);
	    InterpolationType interpolation = toInterpolationType(interpolationLabel);

	    String reftimeStart = summaryJson != null ? summaryJson.optString("reftime_start", null) : null;
	    String reftimeEnd = summaryJson != null ? summaryJson.optString("reftime_end", null) : null;

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	    coreMetadata.getMIMetadata().setLanguage("Italian");
	    coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	    coreMetadata.addDistributionFormat("WaterML 1.1");

	    String titleSuffix = aggregationPeriod != null ? " (" + aggregationPeriod + ")" : "";
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " - " + observedProperty + titleSuffix);

	    String abstrakt = "Nome stazione: " + stationName;
	    if (stationId != null && !stationId.isEmpty()) {
		abstrakt += " - Codice: " + stationId;
	    }
	    if (bcode != null) {
		abstrakt += " - B-code: " + bcode;
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().setAbstract(abstrakt);

	    ResponsibleParty publisherContact = new ResponsibleParty();
	    publisherContact.setOrganisationName(ORGANIZATION);
	    publisherContact.setRoleCode("publisher");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationName);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(observedProperty);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("EMILIA-ROMAGNA");
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("SIMC");

	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("EPSG:4326");
	    referenceSystem.setCodeSpace("EPSG");
	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	    if (lonLat != null) {
		coreMetadata.addBoundingBox(lonLat[1], lonLat[0], lonLat[1], lonLat[0]);
	    }
	    if (heightM != null) {
		coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(heightM, heightM);
	    }

	    MIPlatform platform = new MIPlatform();
	    if (stationId != null) {
		platform.setMDIdentifierCode(stationId);
	    }
	    platform.setDescription(stationName);
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(stationName);
	    platform.setCitation(platformCitation);
	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    dataset.getExtensionHandler().setCountry("ITA");

	    TemporalExtent temporalExtent = new TemporalExtent();
	    ISO8601DateTimeUtils.parseISO8601ToDate(reftimeStart).ifPresent(
		    d -> temporalExtent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(d)));
	    if (reftimeEnd != null && !reftimeEnd.isEmpty() && !"null".equalsIgnoreCase(reftimeEnd)) {
		Optional<Date> end = ISO8601DateTimeUtils.parseISO8601ToDate(reftimeEnd);
		if (end.isPresent()) {
		    temporalExtent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(end.get()));
		} else {
		    temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		}
	    } else {
		temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	    }
	    coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);
	    setIndeterminatePosition(dataset);

	    if (aggregationPeriod != null && !aggregationPeriod.isEmpty()) {
		dataset.getExtensionHandler().setTimeResolutionDuration8601(aggregationPeriod);
		dataset.getExtensionHandler().setTimeAggregationDuration8601(aggregationPeriod);
	    }

	    if (interpolation != null) {
		dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	    }
	    dataset.getExtensionHandler().setAttributeMissingValue(MISSING_VALUE);
	    if (unit != null && !unit.isEmpty()) {
		dataset.getExtensionHandler().setAttributeUnits(unit);
	    }

	    CoverageDescription coverageDescription = new CoverageDescription();
	    coverageDescription.setAttributeIdentifier(bcode != null ? bcode : datasetResource);
	    coverageDescription.setAttributeTitle(observedProperty);
	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
	    HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);

	    String baseUrl = source.getEndpoint();
	    if (baseUrl == null || baseUrl.isEmpty()) {
		baseUrl = HISCentralEmiliaSimcConnector.BASE_URL;
	    }
	    if (baseUrl.endsWith("/")) {
		baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
	    }

	    HISCentralEmiliaSimcMangler mangler = new HISCentralEmiliaSimcMangler();
	    mangler.setPlatformIdentifier(stationId);
	    if (datasetResource != null) {
		mangler.setParameterIdentifier(datasetResource);
	    }
	    if (bcode != null) {
		mangler.setBcode(bcode);
	    }

	    String resourceIdentifier = generateCode(dataset, stationId + "-" + (bcode != null ? bcode : datasetResource)
		    + "-" + interpolationLabel + (aggregationPeriod != null ? "-" + aggregationPeriod : ""));
	    coreMetadata.getMIMetadata().setFileIdentifier(resourceIdentifier);
	    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);
	    mangler.setSourceIdentifier(dataset.getSource().getUniqueIdentifier());

	    String identifier = mangler.getMangling();
	    String downloadUrl = datasetResource != null ? baseUrl + "/" + datasetResource : baseUrl;

	    coreMetadata.addDistributionOnlineResource(identifier, downloadUrl, CommonNameSpaceContext.HISCENTRAL_EMILIA_SIMC_NS_URI,
		    "download");
	    coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);
	    dataset.getPropertyHandler().setIsTimeseries(true);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Emilia-SIMC mapping error", e);
	}
    }

    static InterpolationType toInterpolationType(String interpolationLabel) {
	if (interpolationLabel == null) {
	    return null;
	}
	switch (interpolationLabel) {
	case "instantaneous":
	    return InterpolationType.CONTINUOUS;
	case "average_preceding_interval":
	    return InterpolationType.AVERAGE_PREC;
	case "total_preceding_interval":
	    return InterpolationType.TOTAL_PREC;
	case "maximum_preceding_interval":
	    return InterpolationType.MAX_PREC;
	case "minimum_preceding_interval":
	    return InterpolationType.MIN_PREC;
	default:
	    return InterpolationType.decode(interpolationLabel);
	}
    }

    private static double[] lonLatFromGeometry(JSONObject geometry) {
	if (geometry == null) {
	    return null;
	}
	if (!"Point".equalsIgnoreCase(geometry.optString("type", ""))) {
	    return null;
	}
	JSONArray coords = geometry.optJSONArray("coordinates");
	if (coords == null || coords.length() < 2) {
	    return null;
	}
	return new double[] { coords.getDouble(0), coords.getDouble(1) };
    }
}
