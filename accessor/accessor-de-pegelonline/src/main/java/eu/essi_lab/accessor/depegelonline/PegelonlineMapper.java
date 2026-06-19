package eu.essi_lab.accessor.depegelonline;

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
public class PegelonlineMapper extends AbstractResourceMapper {

    /**
     * 
     */
    public static final String PEGELONLINE_SCHEMA = "https://www.pegelonline.wsv.de/webservices/rest-api/schema";

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	PegelonlineEntity entity = getEntity(originalMD);
	return mapTimeseries(entity, source);
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapTimeseries(PegelonlineEntity entity, GSSource source) {

	JSONObject object = entity.getObject();

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String stationUuid = object.optString(PegelonlineClient.STATION_UUID, null);
	if (stationUuid != null) {
	    dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
		    .setParentIdentifier(decorateIdentifier(source.getEndpoint(), stationUuid));
	}

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);

	String stationLabel = object.optString(PegelonlineClient.STATION_LABEL, "");
	String timeseriesLabel = object.optString(PegelonlineClient.TIMESERIES_LABEL, "");
	String timeseriesId = object.optString(PegelonlineClient.TIMESERIES_ID, "");
	String waterName = object.optString(PegelonlineClient.WATER_NAME, "");
	String from = object.optString(PegelonlineClient.FROM, null);
	String to = object.optString(PegelonlineClient.TO, null);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName("Wasserstraßen- und Schifffahrtsverwaltung des Bundes (WSV)");
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);

	addKeywords(object, keywords);

	coreMetadata.setTitle(stationLabel + " - " + timeseriesLabel);

	coreMetadata.setAbstract("Timeseries " + timeseriesLabel + " from station " + stationLabel + ", water body " + waterName
		+ " (PEGELONLINE REST API, last " + PegelonlineClient.DATA_RETENTION_DAYS + " days)");

	dataset.getExtensionHandler().setCountry("Germany");

	if (from != null && to != null) {
	    coreMetadata.addTemporalExtent(from, to);
	}

	if (object.has(PegelonlineClient.STATION_LAT) && object.has(PegelonlineClient.STATION_LONG)) {
	    BigDecimal lat = new BigDecimal(object.get(PegelonlineClient.STATION_LAT).toString());
	    BigDecimal lon = new BigDecimal(object.get(PegelonlineClient.STATION_LONG).toString());
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	addPlatform(object, stationUuid, stationLabel, coreMetadata);
	addCoverageDescription(timeseriesLabel, coreMetadata, timeseriesId);
	addExtensions(object, dataset.getExtensionHandler());

	coreMetadata.addDistributionOnlineResource(//
		timeseriesId, //
		source.getEndpoint(), //
		NetProtocolWrapper.PEGELONLINE.getCommonURN(), //
		"download");

	return dataset;
    }

    /**
     * @param object
     * @param keywords
     */
    private void addKeywords(JSONObject object, Keywords keywords) {

	addKeyword(keywords, object.optString(PegelonlineClient.STATION_LABEL, null));
	addKeyword(keywords, object.optString(PegelonlineClient.WATER_NAME, null));
	addKeyword(keywords, object.optString(PegelonlineClient.TIMESERIES_LABEL, null));
	addKeyword(keywords, object.optString(PegelonlineClient.TIMESERIES_SHORTNAME, null));
	addKeyword(keywords, object.optString(PegelonlineClient.STATION_AGENCY, null));
	addKeyword(keywords, "Germany");
	addKeyword(keywords, "Hydrology");
	addKeyword(keywords, "PEGELONLINE");
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
     * @param stationUuid
     * @param stationLabel
     * @param coreMetadata
     */
    private void addPlatform(JSONObject object, String stationUuid, String stationLabel, CoreMetadata coreMetadata) {

	if (stationUuid == null) {
	    stationUuid = object.optString(PegelonlineClient.STATION_UUID, "");
	}

	MIPlatform platform = new MIPlatform();

	Citation citation = new Citation();
	citation.setTitle(stationLabel);
	platform.setCitation(citation);

	platform.setDescription("Station " + stationLabel + " (Germany)");
	platform.setMDIdentifierCode(NetProtocolWrapper.PEGELONLINE.getCommonURN() + "/" + stationUuid);

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param timeseriesLabel
     * @param coreMetadata
     * @param timeseriesId
     */
    private void addCoverageDescription(String timeseriesLabel, CoreMetadata coreMetadata, String timeseriesId) {

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeTitle(timeseriesLabel);
	coverageDescription.setAttributeDescription(timeseriesLabel);
	coverageDescription.setAttributeIdentifier(NetProtocolWrapper.PEGELONLINE.getCommonURN() + ":" + timeseriesId);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param object
     * @param handler
     */
    private void addExtensions(JSONObject object, ExtensionHandler handler) {

	String unitName = object.optString(PegelonlineClient.UNIT_NAME, "");
	handler.setAttributeUnits(unitName);
	handler.setAttributeUnitsAbbreviation(unitName);
	handler.setCountry("Germany");
	handler.setTimeInterpolation("instantaneous");

	if (object.has(PegelonlineClient.PERIOD)) {
	    int periodSeconds = object.getInt(PegelonlineClient.PERIOD);
	    String duration = periodToIso8601(periodSeconds);
	    handler.setTimeResolutionDuration8601(duration);
	}
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
    private PegelonlineEntity getEntity(OriginalMetadata originalMD) {

	return new PegelonlineEntity(originalMD.getMetadata());
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return PEGELONLINE_SCHEMA;
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	PegelonlineEntity entity = getEntity(resource.getOriginalMetadata());
	String entityId = entity.getObject().optString(PegelonlineClient.TIMESERIES_ID, null);
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
