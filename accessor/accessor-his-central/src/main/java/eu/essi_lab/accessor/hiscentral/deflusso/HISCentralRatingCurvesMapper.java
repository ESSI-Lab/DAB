package eu.essi_lab.accessor.hiscentral.deflusso;

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
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ratings.RatingCurves;
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

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.HISCENTRAL_RATING_CURVES_NS_URI;
    }

    /**
     * Builds the lightweight {@link OriginalMetadata} (endpoint + station id/name) consumed by {@link #execMapping}.
     */
    static OriginalMetadata create(String endpoint, RatingCurves station) {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_RATING_CURVES_NS_URI);

	JSONObject json = new JSONObject();
	json.put(ENDPOINT_KEY, endpoint);
	if (station.getSourceId() != null) {
	    json.put(SOURCE_ID_KEY, station.getSourceId());
	}
	json.put(STATION_ID_KEY, station.getStationIdentifier());
	json.put(STATION_NAME_KEY, station.getName());

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

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    coreMetadata.getMIMetadata().setLanguage("Italian");
	    coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	    coreMetadata.addDistributionFormat("WaterML 2.0");

	    String title = stationName == null || stationName.isEmpty() ? stationId : stationName;
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(title + " - Scala di deflusso");
	    coreMetadata.getMIMetadata().getDataIdentification()
		    .setAbstract("Rating curve (scala di deflusso) - Nome stazione: " + title + " - Codice stazione: "
			    + stationId);

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

	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("EPSG:4326");
	    referenceSystem.setCodeSpace("EPSG");
	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	    dataset.getExtensionHandler().setCountry("ITA");

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

	    String resourceIdentifier = generateCode(dataset, stationId);
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
	    coverageDescription.setAttributeIdentifier("scala_deflusso");
	    coverageDescription.setAttributeTitle("Scala di deflusso");
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
