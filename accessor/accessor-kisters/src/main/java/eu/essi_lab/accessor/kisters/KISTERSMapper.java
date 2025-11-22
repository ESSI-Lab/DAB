package eu.essi_lab.accessor.kisters;

import java.math.BigDecimal;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.AbstractResourceMapper;

/**
 * @author Fabrizio
 */
public class KISTERSMapper extends AbstractResourceMapper {

    /**
     * 
     */
    public static final String KISTERS_SCHEMA = "https://portal.grdc.bafg.de/KiWIS/KiWIS/schema";

    @SuppressWarnings("incomplete-switch")
    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	KISTERSEntity entity = getEntity(originalMD);

	GSResource resource = null;

	switch (entity.getType()) {
	case STATION:
	    resource = mapStation(entity, source);
	    break;
	case TIME_SERIES:

	    resource = mapTimeSeries(entity, source);
	    break;
	}

	return resource;
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private DatasetCollection mapStation(KISTERSEntity entity, GSSource source) {

	DatasetCollection collection = new DatasetCollection();

	collection.setSource(source);
	collection.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("series");
	collection.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

	//
	//
	//

	CoreMetadata coreMetadata = collection.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);

	//
	//
	//

	String stationId = entity.getObject().getString(KISTERSClient.STATION_ID);
	String stationName = entity.getObject().getString(KISTERSClient.STATION_NAME);

	double lat = Double.valueOf(entity.getObject().getString(KISTERSClient.STATION_LAT));
	double lon = Double.valueOf(entity.getObject().getString(KISTERSClient.STATION_LON));

	String from = entity.getObject().getString(KISTERSClient.TS_FROM);
	String to = entity.getObject().getString(KISTERSClient.TS_TO);

	String siteId = entity.getObject().getString(KISTERSClient.SITE_ID);
	String siteName = entity.getObject().getString(KISTERSClient.SITE_NAME);
	String siteLongName = entity.getObject().getString(KISTERSClient.SITE_LONG_NAME);

	String paramTypeId = entity.getObject().getString(KISTERSClient.PARAM_TYPE_ID);
	String paramTypeName = entity.getObject().getString(KISTERSClient.PARAM_TYPE_NAME);
	String paramTypeLongName = entity.getObject().getString(KISTERSClient.PARAM_TYPE_LONG_NAME);

	String riverName = entity.getObject().getString(KISTERSClient.RIVER_NAME);
	String objectType = entity.getObject().getString(KISTERSClient.OBJECT_TYPE);

	String country = entity.getObject().getString(KISTERSClient.GRDC_COUNTRY);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(siteLongName);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);
	//
	// Keywords
	//

	addKeywords(entity, keywords);

	//
	// Title
	//

	coreMetadata.setTitle(stationName);

	//
	// Abstract
	//

	coreMetadata.setAbstract("Station " + stationName + " measuring " + paramTypeLongName + " of the river " + riverName
		+ " managed by " + siteLongName + " (" + country + ")");

	//
	// Country
	//

	collection.getExtensionHandler().setCountry(country);

	//
	// Temp extent
	//

	coreMetadata.addTemporalExtent(from, to);

	//
	// Spatial extent
	//

	coreMetadata.addBoundingBox(lat, lon, lat, lon);

	//
	// Coverage description
	//

	addCoverageDescription(entity, coreMetadata);

	//
	// Extensions
	//

	addExtensions(entity, collection.getExtensionHandler());

	//
	// Platform
	//

	addPlatform(entity, coreMetadata);

	return collection;
    }

    /**
     * @param entity
     * @param source
     * @return
     */
    private Dataset mapTimeSeries(KISTERSEntity entity, GSSource source) {

	Dataset dataset = new Dataset();

	dataset.setSource(source);
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setHierarchyLevelName("dataset");
	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String parentId = entity.getObject().getString(KISTERSClient.STATION_ID);
	parentId = decorateIdentifier(source.getEndpoint(), parentId);

	dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier(parentId);

	//
	//
	//

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	DataIdentification dataId = coreMetadata.getDataIdentification();

	Keywords keywords = new Keywords();
	dataId.addKeywords(keywords);

	//
	//
	//

	String tsId = entity.getObject().getString(KISTERSClient.TS_ID);

	String stationName = entity.getObject().getString(KISTERSClient.STATION_NAME);

	String lat = entity.getObject().getString(KISTERSClient.STATION_LAT);
	String lon = entity.getObject().getString(KISTERSClient.STATION_LON);

	String from = entity.getObject().getString(KISTERSClient.TS_FROM);
	String to = entity.getObject().getString(KISTERSClient.TS_TO);

	String siteId = entity.getObject().getString(KISTERSClient.SITE_ID);
	String siteName = entity.getObject().getString(KISTERSClient.SITE_NAME);
	String siteLongName = entity.getObject().getString(KISTERSClient.SITE_LONG_NAME);

	String paramTypeId = entity.getObject().getString(KISTERSClient.PARAM_TYPE_ID);
	String paramTypeName = entity.getObject().getString(KISTERSClient.PARAM_TYPE_NAME);
	String paramTypeLongName = entity.getObject().getString(KISTERSClient.PARAM_TYPE_LONG_NAME);

	String riverName = entity.getObject().getString(KISTERSClient.RIVER_NAME);
	String objectType = entity.getObject().getString(KISTERSClient.OBJECT_TYPE);

	String country = entity.getObject().getString(KISTERSClient.GRDC_COUNTRY);

	String tsName = entity.getObject().getString(KISTERSClient.TS_NAME);

	String unitSymbol = entity.getObject().getString(KISTERSClient.TS_UNIT_SYMBOL);
	String unitName = entity.getObject().getString(KISTERSClient.TS_UNIT_NAME);
	String spacing = entity.getObject().getString(KISTERSClient.TS_SPACING);

	ResponsibleParty party = new ResponsibleParty();
	party.setOrganisationName(siteLongName);
	party.setRoleCode("pointOfContact");
	dataId.addPointOfContact(party);
	//
	// Keywords
	//

	addKeywords(entity, keywords);

	//
	// Title
	//

	coreMetadata.setTitle(stationName + " - " + tsName);

	//
	// Abstract
	//

	coreMetadata.setAbstract("Timeseries " + tsName + " with data from station " + stationName + ", river " + riverName + " located in "
		+ siteLongName + " (" + country + ")");

	//
	// Country
	//

	dataset.getExtensionHandler().setCountry(country);

	//
	// Temp extent
	//

	coreMetadata.addTemporalExtent(from, to);

	//
	// Spatial extent
	//

	BigDecimal blat = new BigDecimal(lat);
	BigDecimal blon = new BigDecimal(lon);
	coreMetadata.addBoundingBox(blat, blon, blat, blon);

	//
	// Platform
	//

	addPlatform(entity, coreMetadata);

	//
	// Coverage description
	//

	addCoverageDescription(entity, coreMetadata);

	//
	// Extensions
	//

	addExtensions(entity, dataset.getExtensionHandler());

	//
	// Distribution info
	//

	coreMetadata.addDistributionOnlineResource(//
		tsId, //
		source.getEndpoint(), //
		NetProtocolWrapper.KISTERS.getCommonURN(), //
		"download");

	return dataset;
    }

    /**
     * @param entity
     * @param keywords
     */
    private void addKeywords(KISTERSEntity entity, Keywords keywords) {

	String stationName = entity.getObject().getString(KISTERSClient.STATION_NAME);

	String siteName = entity.getObject().getString(KISTERSClient.SITE_NAME);
	String siteLongName = entity.getObject().getString(KISTERSClient.SITE_LONG_NAME);

	String paramTypeName = entity.getObject().getString(KISTERSClient.PARAM_TYPE_NAME);
	String paramTypeLongName = entity.getObject().getString(KISTERSClient.PARAM_TYPE_LONG_NAME);

	String riverName = entity.getObject().getString(KISTERSClient.RIVER_NAME);
	String objectType = entity.getObject().getString(KISTERSClient.OBJECT_TYPE);

	String country = entity.getObject().getString(KISTERSClient.GRDC_COUNTRY);

	keywords.addKeyword(stationName);
	keywords.addKeyword(siteName);
	keywords.addKeyword(siteLongName);
	keywords.addKeyword(paramTypeLongName);
	keywords.addKeyword(riverName);
	keywords.addKeyword(objectType);
	keywords.addKeyword(paramTypeName);
	keywords.addKeyword(paramTypeLongName);
	keywords.addKeyword(country);
    }

    /**
     * @param entity
     * @param coreMetadata
     */
    private void addPlatform(KISTERSEntity entity, CoreMetadata coreMetadata) {

	String stationId = entity.getObject().getString(KISTERSClient.STATION_ID);
	String stationName = entity.getObject().getString(KISTERSClient.STATION_NAME);

	String siteLongName = entity.getObject().getString(KISTERSClient.SITE_LONG_NAME);

	String country = entity.getObject().getString(KISTERSClient.GRDC_COUNTRY);

	MIPlatform platform = new MIPlatform();

	//
	// Platform Title
	//

	Citation citation = new Citation();
	citation.setTitle(stationName);
	platform.setCitation(citation);

	//
	// Platform description and keywords
	//

	platform.setDescription("Station " + stationName + " located in " + siteLongName + " (" + country + ")");

	//
	// Platform id
	//

	platform.setMDIdentifierCode(NetProtocolWrapper.KISTERS.getCommonURN() + "/" + stationId);

	//
	//
	//

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param stream
     * @param coreMetadata
     * @param keywords
     */

    private void addCoverageDescription(KISTERSEntity entity, CoreMetadata coreMetadata) {

	String paramTypeId = entity.getObject().getString(KISTERSClient.PARAM_TYPE_ID);
	String paramTypeName = entity.getObject().getString(KISTERSClient.PARAM_TYPE_NAME);
	String paramTypeLongName = entity.getObject().getString(KISTERSClient.PARAM_TYPE_LONG_NAME);

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeTitle(paramTypeLongName);

	coverageDescription.setAttributeDescription(paramTypeLongName);

	String tsId = entity.getObject().getString(KISTERSClient.TS_ID);

	String coverageId = NetProtocolWrapper.KISTERS.getCommonURN();
	coverageId += ":" + tsId;

	coverageDescription.setAttributeIdentifier(coverageId);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param stream
     * @param handler
     */
    private void addExtensions(KISTERSEntity entity, ExtensionHandler handler) {

	String unitSymbol = entity.getObject().getString(KISTERSClient.TS_UNIT_SYMBOL);
	String unitName = entity.getObject().getString(KISTERSClient.TS_UNIT_NAME);
	String spacing = entity.getObject().getString(KISTERSClient.TS_SPACING);

	String paramTypeId = entity.getObject().getString(KISTERSClient.PARAM_TYPE_ID);
	String paramTypeName = entity.getObject().getString(KISTERSClient.PARAM_TYPE_NAME);
	String paramTypeLongName = entity.getObject().getString(KISTERSClient.PARAM_TYPE_LONG_NAME);

	String country = entity.getObject().getString(KISTERSClient.GRDC_COUNTRY);

	//
	// Attribute units
	//
	handler.setAttributeUnits(unitName);

	//
	// Attribute units abbreviation
	//
	handler.setAttributeUnitsAbbreviation(unitSymbol);

	//
	// Time resolution duration
	//

	handler.setTimeResolutionDuration8601(spacing);

	if (country != null && country.contains(" - ")) {
	    country = country.substring(country.indexOf(" - ") + 3);
	}
	handler.setCountry(country);
	//
	// Time interpolation
	//
	String tsName = entity.getObject().getString(KISTERSClient.TS_NAME);
	handler.setTimeInterpolation(tsName);

	//
	// Time aggregation duration
	//

	// handler.setTimeAggregationDuration8601(spacing);

    }

    /**
     * @param originalMD
     * @return
     */
    private KISTERSEntity getEntity(OriginalMetadata originalMD) {

	String metadata = originalMD.getMetadata();

	return new KISTERSEntity(metadata);
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return KISTERS_SCHEMA;
    }

    /**
     * @param endpoint
     * @param entityId
     * @return
     */
    private String decorateIdentifier(String endpoint, String entityId) {

	String id = null;

	try {

	    id = StringUtils.hashSHA1messageDigest(endpoint + entityId);
	} catch (Exception e) {

	    id = id.replace("https:", "");
	    id = id.replace("http:", "");
	    id = id.replace("//", "");
	    id = id.replace("/", "");
	    id = id.replace("'", "");
	    id = id.replace(".", "");
	    id = id.replace("(", "");
	    id = id.replace(")", "");
	    id = StringUtils.encodeUTF8(id);
	}

	return id;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	KISTERSEntity entity = getEntity(resource.getOriginalMetadata());

	String entityId = null;

	switch (entity.getType()) {
	case STATION:
	    entityId = entity.getObject().getString(KISTERSClient.STATION_ID);
	    break;

	case TIME_SERIES:
	    entityId = entity.getObject().getString(KISTERSClient.TS_ID);
	    break;
	}

	return decorateIdentifier(resource.getSource().getEndpoint(), entityId);
    }
}
