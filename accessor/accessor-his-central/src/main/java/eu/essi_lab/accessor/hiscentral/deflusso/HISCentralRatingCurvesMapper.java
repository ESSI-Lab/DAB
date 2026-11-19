package eu.essi_lab.accessor.hiscentral.deflusso;

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

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ratings.RatingCurve;
import eu.essi_lab.model.ratings.RatingCurves;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * Maps a harvested rating-curve station into a {@link Dataset} carrying a WaterML 2.0 download link served by
 * {@link eu.essi_lab.downloader.hiscentral.HISCentralRatingCurvesDownloader}.
 *
 * @author boldrini
 */
public class HISCentralRatingCurvesMapper extends FileIdentifierMapper {

    public static final String ORGANIZATION = "HIS-Central";

    public static final String ENDPOINT_KEY = "endpoint";
    public static final String SOURCE_ID_KEY = "source-id";
    public static final String STATION_ID_KEY = "station-id";
    public static final String STATION_NAME_KEY = "station-name";
    public static final String BEGIN_KEY = "begin";
    public static final String END_KEY = "end";
    public static final String LEVEL_SENSOR_KEY = "level-sensor";
    public static final String DISCHARGE_SENSOR_KEY = "discharge-sensor";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.HISCENTRAL_RATING_CURVES_NS_URI;
    }

    /**
     * Builds the lightweight {@link OriginalMetadata} (endpoint + station id/name) consumed by {@link #execMapping}.
     */
    public static OriginalMetadata create(String endpoint, RatingCurves station) {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_RATING_CURVES_NS_URI);

	JSONObject json = new JSONObject();
	json.put(ENDPOINT_KEY, endpoint);
	if (station.getSourceId() != null) {
	    json.put(SOURCE_ID_KEY, station.getSourceId());
	}
	json.put(STATION_ID_KEY, station.getStationIdentifier());
	json.put(STATION_NAME_KEY, station.getName());
	if (station.getLevelSensor() != null) {
	    json.put(LEVEL_SENSOR_KEY, station.getLevelSensor());
	}
	if (station.getDischargeSensor() != null) {
	    json.put(DISCHARGE_SENSOR_KEY, station.getDischargeSensor());
	}

	//
	// overall validity period: earliest start, latest end (omitted if a curve is still in use)
	//
	Instant begin = null;
	Instant end = null;
	boolean open = station.getCurves().isEmpty();

	for (RatingCurve curve : station.getCurves()) {

	    Optional<Instant> curveBegin = curve.getBeginTime()
		    .or(() -> Optional.ofNullable(curve.getBeginDate()).map(d -> d.atStartOfDay().toInstant(ZoneOffset.UTC)));
	    Optional<Instant> curveEnd = curve.getEndTime()
		    .or(() -> Optional.ofNullable(curve.getEndDate()).map(d -> d.atStartOfDay().toInstant(ZoneOffset.UTC)));

	    if (curveBegin.isPresent() && (begin == null || curveBegin.get().isBefore(begin))) {
		begin = curveBegin.get();
	    }
	    if (curveEnd.isEmpty()) {
		open = true;
	    } else if (end == null || curveEnd.get().isAfter(end)) {
		end = curveEnd.get();
	    }
	}

	if (begin != null) {
	    json.put(BEGIN_KEY, begin.toString());
	    if (!open && end != null) {
		json.put(END_KEY, end.toString());
	    }
	}

	originalMetadata.setMetadata(json.toString(4));

	return originalMetadata;
    }

    /**
     * Reads the HIS-Central source id (SharePoint sub-folder name) from harvested original metadata.
     */
    public static String readSourceId(OriginalMetadata metadata) {

	if (metadata == null || metadata.getMetadata() == null) {
	    return null;
	}
	return new JSONObject(metadata.getMetadata()).optString(SOURCE_ID_KEY, null);
    }

    /**
     * Reads the station id from harvested original metadata.
     */
    public static String readStationId(OriginalMetadata metadata) {

	if (metadata == null || metadata.getMetadata() == null) {
	    return null;
	}
	return new JSONObject(metadata.getMetadata()).optString(STATION_ID_KEY, null);
    }

    /**
     * Reads the level sensor (time series) name from harvested original metadata, if any.
     */
    public static String readLevelSensor(OriginalMetadata metadata) {

	if (metadata == null || metadata.getMetadata() == null) {
	    return null;
	}
	return new JSONObject(metadata.getMetadata()).optString(LEVEL_SENSOR_KEY, null);
    }

    /**
     * Reads the discharge sensor (time series) name from harvested original metadata, if any.
     */
    public static String readDischargeSensor(OriginalMetadata metadata) {

	if (metadata == null || metadata.getMetadata() == null) {
	    return null;
	}
	return new JSONObject(metadata.getMetadata()).optString(DISCHARGE_SENSOR_KEY, null);
    }

    /**
     * Reads the station name from harvested original metadata.
     */
    public static String readStationName(OriginalMetadata metadata) {

	if (metadata == null || metadata.getMetadata() == null) {
	    return null;
	}
	return new JSONObject(metadata.getMetadata()).optString(STATION_NAME_KEY, null);
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	try {

	    JSONObject json = new JSONObject(originalMD.getMetadata());
	    String endpoint = json.optString(ENDPOINT_KEY);
	    String sourceId = json.optString(SOURCE_ID_KEY, null);
	    String stationId = json.optString(STATION_ID_KEY);
	    String stationName = json.optString(STATION_NAME_KEY);
	    String levelSensor = json.optString(LEVEL_SENSOR_KEY, "");
	    String dischargeSensor = json.optString(DISCHARGE_SENSOR_KEY, "");
	    boolean hasSensors = !levelSensor.isEmpty() || !dischargeSensor.isEmpty();
	    String sensors = levelSensor + " / " + dischargeSensor;

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    coreMetadata.getMIMetadata().setLanguage("Italian");
	    coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	    coreMetadata.addDistributionFormat("WaterML 2.0");

	    String title = stationName == null || stationName.isEmpty() ? stationId : stationName;
	    coreMetadata.getMIMetadata().getDataIdentification()
		    .setCitationTitle(title + " - Scala di deflusso" + (hasSensors ? " (" + sensors + ")" : ""));
	    coreMetadata.getMIMetadata().getDataIdentification()
		    .setAbstract("Rating curve (scala di deflusso) - Nome stazione: " + title + " - Codice stazione: "
			    + stationId //
			    + (levelSensor.isEmpty() ? "" : " - Sensore di livello: " + levelSensor) //
			    + (dischargeSensor.isEmpty() ? "" : " - Sensore di portata: " + dischargeSensor));

	    ResponsibleParty publisherContact = new ResponsibleParty();
	    publisherContact.setOrganisationName(ORGANIZATION);
	    publisherContact.setRoleCode("publisher");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(title);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Scala di deflusso");
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Rating curve");
	    if (sourceId != null && !sourceId.isEmpty()) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(sourceId);
	    }
	    if (!levelSensor.isEmpty()) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(levelSensor);
	    }
	    if (!dischargeSensor.isEmpty()) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(dischargeSensor);
	    }

	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("EPSG:4326");
	    referenceSystem.setCodeSpace("EPSG");
	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	    dataset.getExtensionHandler().setCountry("ITA");

	    //
	    // validity period of the curves, if known
	    //
	    String begin = json.optString(BEGIN_KEY, null);
	    if (begin != null && !begin.isEmpty()) {
		TemporalExtent temporalExtent = new TemporalExtent();
		temporalExtent.setBeginPosition(begin);
		String end = json.optString(END_KEY, null);
		if (end != null && !end.isEmpty()) {
		    temporalExtent.setEndPosition(end);
		} else {
		    temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		}
		coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);
	    }

	    MIPlatform platform = new MIPlatform();
	    try {
		if (stationId != null && !stationId.isEmpty()) {
		    platform.setMDIdentifierCode(stationId);
		} else {
		    platform.setMDIdentifierCode(StringUtils.hashSHA1messageDigest(title));
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to set platform identifier for {}", title, e);
	    }
	    platform.setDescription(title);
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(title);
	    platform.setCitation(platformCitation);
	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    // one record per station, or per station and sensors pair when the station has several series
	    String resourceIdentifier = generateCode(dataset,
		    hasSensors ? stationId + "\u0000" + levelSensor + "\u0000" + dischargeSensor : stationId);
	    coreMetadata.getMIMetadata().setFileIdentifier(resourceIdentifier);
	    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	    Distribution distribution = coreMetadata.getMIMetadata().getDistribution();

	    Online download = new Online();
	    download.setLinkage(endpoint);
	    download.setName(stationId);
	    download.setFunctionCode("download");
	    download.setProtocol(CommonNameSpaceContext.HISCENTRAL_RATING_CURVES_NS_URI);
	    download.setIdentifier(resourceIdentifier);
	    distribution.addDistributionOnline(download);

	    CoverageDescription coverageDescription = new CoverageDescription();
	    coverageDescription.setAttributeIdentifier("Rating curve");
	    coverageDescription.setAttributeTitle("Rating curve");
	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.AVERAGE);
	    dataset.getExtensionHandler().setAttributeUnits("table");

	    dataset.getPropertyHandler().setIsRatingCurve(true);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error while mapping rating curve metadata", e);
	}

	return dataset;
    }
}
