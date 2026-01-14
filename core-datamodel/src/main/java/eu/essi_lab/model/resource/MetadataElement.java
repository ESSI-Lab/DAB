package eu.essi_lab.model.resource;

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

import java.util.*;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.resource.composed.ComposedElement;

/**
 * Enumeration of queryable {@link HarmonizedMetadata} element names
 *
 * @author Fabrizio
 * @see ResourceProperty
 */
public enum MetadataElement implements Queryable {

    /**
     *
     */
    ORGANIZATION(OrganizationElementWrapper.build()),

    /**
     *
     */
    QUALIFIER(QualifierElementWrapper.build()),

    /**
     *
     */
    KEYWORD_SA(SA_ElementWrapper.of("keyword_SA")), //

    /**
     *
     */
    PARAMETER_SA(SA_ElementWrapper.of("parameter_SA")), //

    /**
     *
     */
    INSTRUMENT_SA(SA_ElementWrapper.of("instrument_SA")), //

    /**
     *
     */
    RESPONSIBLE_ORG_SA(SA_ElementWrapper.of("responsibleOrg_SA")), //

    /**
     *
     */
    CRUISE_SA(SA_ElementWrapper.of("cruise_SA")), //

    /**
     *
     */
    PROJECT_SA(SA_ElementWrapper.of("project_SA")), //

    /**
     *
     */
    ANY_TEXT("anyText", false),

    /**
     *
     */
    SUBJECT("subject"),

    /**
     *
     */
    DISTRIBUTOR_ORG_NAME("distOrgName"),

    /**
     *
     */
    DATE_STAMP("dateStamp", "Date stamp", ContentType.ISO8601_DATE_TIME),

    /**
     * MD_KeywordTypeCode
     */
    KEYWORD_TYPE("keywordType"),

    /**
     * //gmd:keyword/gco:CharacterString
     */
    KEYWORD_BLUE_CLOUD(MetadataElement.KEYWORD_BLUE_CLOUD_EL_NAME),

    /**
     *
     */
    KEYWORD_URI_BLUE_CLOUD(MetadataElement.KEYWORD_URI_BLUE_CLOUD_EL_NAME),

    /**
     *
     */
    KEYWORD(MetadataElement.KEYWORD_EL_NAME, "Keyword"),

    /**
     *
     */
    KEYWORD_URI(MetadataElement.KEYWORD_URI_EL_NAME),

    /**
     * //gmd:topicCategory/gmd:MD_TopicCategoryCode"
     */
    TOPIC_CATEGORY("topicCategory", "Topic category"),

    /**
     *
     */
    HIERARCHY_LEVEL_CODE_LIST_VALUE("hLevelCodeValue"),

    /**
     *
     */
    INSTRUMENT_IDENTIFIER(MetadataElement.INSTRUMENT_IDENTIFIER_EL_NAME),

    /**
     *
     */
    UNIQUE_INSTRUMENT_IDENTIFIER(MetadataElement.UNIQUE_INSTRUMENT_IDENTIFIER_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     *
     */
    INSTRUMENT_DESCRIPTION("instrumentDesc"),

    /**
     *
     */
    INSTRUMENT_TITLE("instrumentTitle"), INSTRUMENT_URI("instrumentURI"),

    CRUISE_NAME(MetadataElement.CRUISE_NAME_EL_NAME),

    CRUISE_URI(MetadataElement.CRUISE_URI_EL_NAME),

    PROJECT_NAME(MetadataElement.PROJECT_NAME_EL_NAME),

    PROJECT_URI(MetadataElement.PROJECT_URI_EL_NAME),

    /**
     *
     */
    PLATFORM_IDENTIFIER(MetadataElement.PLATFORM_IDENTIFIER_EL_NAME),

    /**
     *
     */
    PLATFORM_TITLE(MetadataElement.PLATFORM_TITLE_EL_NAME),

    PLATFORM_URI(MetadataElement.PLATFORM_URI_EL_NAME),

    /**
     *
     */
    UNIQUE_PLATFORM_IDENTIFIER(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     *
     */
    TIME_INTERPOLATION(MetadataElement.TIME_INTERPOLATION_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     *
     */
    TIME_SUPPORT(MetadataElement.TIME_SUPPORT_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     *
     */
    TIME_RESOLUTION(MetadataElement.TIME_RESOLUTION_EL_NAME, false, true, ContentType.TEXTUAL, true),

    TIME_RESOLUTION_DURATION_8601(MetadataElement.TIME_RESOLUTION_DURATION_8601_EL_NAME, false, true, ContentType.TEXTUAL, true),

    TIME_AGGREGATION_DURATION_8601(MetadataElement.TIME_AGGREGATION_DURATION_8601_EL_NAME, false, true, ContentType.TEXTUAL, true),

    WIS_TOPIC_HIERARCHY(MetadataElement.WIS_TOPIC_HIERARCHY_EL_NAME, false, true, ContentType.TEXTUAL, true),
    /**
     *
     */
    TIME_UNITS(MetadataElement.TIME_UNITS_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     *
     */
    TIME_UNITS_ABBREVIATION(MetadataElement.TIME_UNITS_ABBREVIATION_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     *
     */
    DATA_SIZE(MetadataElement.DATA_SIZE_EL_NAME, false, true, ContentType.LONG, true),

    /**
     *
     */
    PLATFORM_DESCRIPTION("platformDesc"),

    /**
     *
     */
    ATTRIBUTE_IDENTIFIER(MetadataElement.ATTRIBUTE_IDENTIFIER_EL_NAME),

    /**
     *
     */
    ATTRIBUTE_TITLE(MetadataElement.ATTRIBUTE_TITLE_EL_NAME),

    /**
     *
     */
    ATTRIBUTE_UNITS(MetadataElement.ATTRIBUTE_UNITS_EL_NAME),

    /**
     *
     */
    ATTRIBUTE_UNITS_URI(MetadataElement.ATTRIBUTE_UNITS_URI_EL_NAME),

    /**
     *
     */
    ATTRIBUTE_UNITS_ABBREVIATION(MetadataElement.ATTRIBUTE_UNITS_ABBREVIATION_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     *
     */
    ATTRIBUTE_MISSING_VALUE(MetadataElement.ATTRIBUTE_MISSING_VALUE_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     *
     */
    UNIQUE_ATTRIBUTE_IDENTIFIER(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     * NEW
     */
    OBSERVED_PROPERTY_URI(MetadataElement.OBSERVED_PROPERTY_URI_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     *
     */
    ATTRIBUTE_DESCRIPTION(MetadataElement.ATTRIBUTE_DESCRIPTION_EL_NAME),

    /**
     *
     */
    COVERAGE_CONTENT_TYPE_CODE("coverageContentTypeCode"),

    /**
     *
     */
    COUPLING_TYPE("couplingType"),

    /**
     *
     */
    DISTANCE_UOM("distanceUOM"),

    /**
     *
     */
    REVISION_DATE("revisionDate", ContentType.ISO8601_DATE_TIME),

    REFERENCE_DATE("referenceDate", ContentType.ISO8601_DATE_TIME),

    /**
     *
     */
    ONLINE_LINKAGE("onlineLinkage", "Online link"),

    /**
     *
     */
    ONLINE_ID("onlineId"),

    /**
     *
     */
    BOUNDING_BOX("bbox", "Spatial extent", ContentType.SPATIAL),

    /**
     *
     */
    SPATIAL_REPRESENTATION_TYPE("spatialRepresentationType"),

    /**
     *
     */
    RASTER_MOSAIC ("rasterMosaic"),

    /**
     *
     */
    METADATA_VERSION("metadataVersion"),

    /**
     *
     */
    METADATA_ORIGINAL_VERSION("metadataOriginalVersion"),

    /**
     *
     */
    AUTHOR("creator"),
    /**
     *
     */
    IDENTIFIER("fileId"),

    /**
     *
     */
    PARENT_IDENTIFIER("parentId"),

    /**
     *
     */
    TITLE("title", "Title"),

    /**
     *
     */
    ABSTRACT("abstract", "Abstract"),

    /**
     *
     */
    AGGREGATED_RESOURCE_IDENTIFIER("aggResId"),

    /**
     *
     */
    DISTRIBUTION_FORMAT(MetadataElement.DISTRIBUTION_FORMAT_EL_NAME, "Distribution format"),

    /**
     *
     */
    RESOURCE_IDENTIFIER("resourceId"),

    /**
     *
     */
    RESOURCE_LANGUAGE("resLanguage"),

    /**
     *
     */
    GEOGRAPHIC_DESCRIPTION_CODE("geoDescCode"),

    /**
     *
     */
    SERVICE_TYPE("srvType"),

    /**
     *
     */
    SERVICE_TYPE_VERSION("srvTypeVersion"),

    /**
     *
     */
    OPERATION("operation"),

    /**
     *
     */
    OPERATES_ON("operatesOn"),

    /**
     *
     */
    OPERATES_ON_IDENTIFIER("operatesOnId"),

    /**
     *
     */
    OPERATES_ON_NAME("operatesOnName"),

    /**
     *
     */
    CRS_AUTHORITY("crsAuthority"),
    /**
     *
     */
    CRS_VERSION("crsVersion"),
    /**
     *
     */
    CRS_ID("crsId"),
    /**
     *
     */
    ALTERNATE_TITLE("alternateTitle"),

    /**
     *
     */
    ORGANISATION_NAME(MetadataElement.ORGANISATION_NAME_EL_NAME),

    /**
     *
     */
    ORGANISATION_ROLE(MetadataElement.ORGANISATION_ROLE_EL_NAME),

    /**
     *
     */
    ORGANISATION_URI(MetadataElement.ORGANISATION_URI_EL_NAME),
    /**
     *
     */
    LANGUAGE("language"),
    /**
     *
     */
    HAS_SECURITY_CONSTRAINTS("hasSecurityConst", ContentType.BOOLEAN),
    /**
     *
     */
    HAS_USE_LEGAL_CONSTRAINTS("hasUseLegalConst", ContentType.BOOLEAN),
    /**
     *
     */
    HAS_ACCESS_LEGAL_CONSTRAINTS("hasAccessLegalConst", ContentType.BOOLEAN),
    /**
     *
     */
    HAS_OTHER_LEGAL_CONSTRAINTS("hasOtherLegalConst", ContentType.BOOLEAN),

    /**
     *
     */
    DENOMINATOR("denominator", ContentType.INTEGER),

    /**
     *
     */
    DISTANCE_VALUE("distValue", ContentType.DOUBLE),

    /**
     * Virtual element, given by the union of the {@link #TEMP_EXTENT_BEGIN} and {@link #TEMP_EXTENT_END}
     */
    TEMP_EXTENT("tmpExtent", ContentType.ISO8601_DATE_TIME),

    /**
     *
     */
    TEMP_EXTENT_BEGIN("tmpExtentBegin", "Temporal extent begin", ContentType.ISO8601_DATE_TIME),

    /**
     *
     */
    TEMP_EXTENT_BEGIN_NOW("tmpExtentBegin_Now", ContentType.BOOLEAN),

    /**
     *
     */
    TEMP_EXTENT_END("tmpExtentEnd", "Temporal extent end", ContentType.ISO8601_DATE_TIME),

    /**
     *
     */
    TEMP_EXTENT_END_NOW("tmpExtentEnd_Now", ContentType.BOOLEAN),

    /**
     *
     */
    TEMP_EXTENT_BEGIN_BEFORE_NOW("tmpExtentBeginBeforeNow", ContentType.TEXTUAL),

    /**
     *
     */
    ONLINE_PROTOCOL(MetadataElement.ONLINE_PROTOCOL_EL_NAME, "Online protocol"),

    /**
     *
     */
    PROCESSING_LEVEL_CODE("procLevCode"),

    /**
     *
     */
    BAND_RESOLUTION("bandResolution", ContentType.DOUBLE),

    /**
     *
     */
    BAND_PEAK_RESPONSE_WL("bandPeakRespWl", ContentType.DOUBLE),
    /**
     *
     */
    ILLUMINATION_ZENITH_ANGLE("illZenithAngle", ContentType.DOUBLE),
    /**
     *
     */
    CLOUD_COVER_PERC("cloudCoverPerc", ContentType.DOUBLE),
    /**
     *
     */
    ILLUMINATION_AZIMUTH_ANGLE("illAziAngle", ContentType.DOUBLE),

    /**
     *
     */
    CREATION_DATE("creationDate", ContentType.ISO8601_DATE_TIME),

    /**
     *
     */
    PUBLICATION_DATE("publicationDate", ContentType.ISO8601_DATE_TIME),
    //

    /**
     *
     */
    ESSI_SPATIAL_RESOLUTION_X("spatialResX", ContentType.DOUBLE),
    /**
     *
     */
    ESSI_SPATIAL_RESOLUTION_Y("spatialResY", ContentType.DOUBLE),
    /**
     *
     */
    ESSI_TEMPORAL_RESOLUTION("timeResDouble", ContentType.DOUBLE),

    /**
     * Extended (QuakeML)
     */
    QML_MAGNITUDE_VALUE("qmlMagValue", ContentType.DOUBLE),

    /**
     * Extended (QuakeML)
     */
    QML_MAGNITUDE_TYPE("qmlMagType"),

    /**
     * Extended (QuakeML)
     */
    QML_DEPTH_VALUE("qmlDepthValue", ContentType.DOUBLE),

    /**
     * Extended
     */
    EOP_POLARIZATION_MODE("eopPolarizationMode"),

    /**
     * Extended
     */
    VARIABLE("variable"),

    /**
     * Extended
     */
    SITE_INFO("siteInfo"),

    /**
     * Extended
     */
    BNHS_INFO(MetadataElement.BNHS_INFO_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     * Extended
     */
    ORIGINATOR_ORGANISATION_IDENTIFIER(MetadataElement.ORIGINATOR_ORGANISATION_IDENTIFIER_EL_NAME, false, true, ContentType.TEXTUAL, true),

    /**
     * Extended
     */
    ORIGINATOR_ORGANISATION_DESCRIPTION("origOrgDesc", false, true, ContentType.TEXTUAL, true),

    /**
     * Extended
     */
    THEME_CATEGORY("themeCategory", false, true, ContentType.TEXTUAL, true),

    // ----------------------------------------------------------
    //
    // IN SITU
    //
    IN_SITU("inSitu", ContentType.BOOLEAN),

    // ----------------------------------------------------------
    //
    // WORLDCEREAL SPECIFIC QUERYABLES
    //
    CROP_TYPES(MetadataElement.CROP_TYPES_EL_NAME, false, true, ContentType.TEXTUAL, true), QUANTITY_TYPES(
	    MetadataElement.QUANTITY_TYPES_EL_NAME, false, true, ContentType.TEXTUAL, true), LAND_COVER_TYPES(
	    MetadataElement.LAND_COVER_TYPES_EL_NAME, false, true, ContentType.TEXTUAL, true), IRRIGATION_TYPES(
	    MetadataElement.IRRIGATION_TYPES_EL_NAME, false, true, ContentType.TEXTUAL, true), CONFIDENCE_CROP_TYPE("cropConfidence",
	    ContentType.DOUBLE), CONFIDENCE_IRR_TYPE("irrigationConfidence", ContentType.DOUBLE), CONFIDENCE_LC_TYPE("landCoverConfidence",
	    ContentType.DOUBLE),

    // ----------------------------------------------------------
    //
    // LANDSAT SPECIFIC QUERYABLES (available in SatelliteScene)
    //
    ROW("row"), //
    PATH("path"), //

    // ----------------------------------------------------------
    //
    // SENTINEL SPECIFIC QUERYABLES (available in SatelliteScene)
    //
    PRODUCT_TYPE(MetadataElement.PRODUCT_TYPE_EL_NAME), //
    SENSOR_OP_MODE(MetadataElement.SENSOR_OP_MODE_EL_NAME), //
    SENSOR_SWATH(MetadataElement.SENSOR_SWATH_EL_NAME), //
    S3_INSTRUMENT_IDX(MetadataElement.S3_INSTRUMENT_IDX_EL_NAME), //
    S3_PRODUCT_LEVEL(MetadataElement.S3_PRODUCT_LEVEL_EL_NAME), //
    S3_TIMELINESS(MetadataElement.S3_TIMELINESS_EL_NAME), //
    RELATIVE_ORBIT("relativeOrbit"), //
    SAR_POL_CH(MetadataElement.SAR_POL_CH_EL_NAME),

    // BNHS QUERYABLES

    HYCOS_IDENTIFIER(MetadataElement.HYCOS_IDENTIFIER_EL_NAME), //
    STATION_IDENTIFIER(MetadataElement.STATION_IDENTIFIER_EL_NAME), //
    COUNTRY(MetadataElement.COUNTRY_EL_NAME, false, true, ContentType.TEXTUAL, true), //
    COUNTRY_ISO3(MetadataElement.COUNTRY_ISO3_EL_NAME, false, true, ContentType.TEXTUAL, true), //
    DATA_DISCLAIMER(MetadataElement.DATA_DISCLAIMER_EL_NAME, ContentType.TEXTUAL), //
    USE_LEGAL_CONSTRAINTS(MetadataElement.USE_LEGAL_CONST_EL_NAME, ContentType.TEXTUAL), //
    USE_LEGAL_CONSTRAINTS_URI(MetadataElement.USE_LEGAL_CONST_URI_EL_NAME, ContentType.TEXTUAL), //
    RIVER(MetadataElement.RIVER_EL_NAME, ContentType.TEXTUAL), //
    RIVER_BASIN(MetadataElement.RIVER_BASIN_EL_NAME, ContentType.TEXTUAL), //
    LAKE_STATION(MetadataElement.LAKE_STATION_EL_NAME), //
    GRDC_ID(MetadataElement.GRDC_ID_EL_NAME), //
    GRDC_ARDB(MetadataElement.GRDC_ARDB_EL_NAME), //
    WMO_REGION(MetadataElement.WMO_REGION_EL_NAME), //
    LATITUDE_OF_DISCHARGE(MetadataElement.LATITUDE_OF_DISCHARGE_EL_NAME), //
    LONGITUDE_OF_DISCHARGE(MetadataElement.LONGITUDE_OF_DISCHARGE_EL_NAME), //
    STATUS(MetadataElement.STATUS_EL_NAME), //
    DRAINAGE_AREA(MetadataElement.DRAINAGE_AREA_EL_NAME), //
    EFFECTIVE_DRAINAGE_AREA(MetadataElement.EFFECTIVE_DRAINAGE_AREA_EL_NAME), //
    DRAINAGE_SHAPEFILE(MetadataElement.DRAINAGE_SHAPEFILE_EL_NAME), //
    LATITUDE(MetadataElement.LATITUDE_EL_NAME), //
    LONGITUDE(MetadataElement.LONGITUDE_EL_NAME), //
    ALTITUDE_DATUM(MetadataElement.ALTITUDE_DATUM_EL_NAME), //
    ALTITUDE(MetadataElement.ALTITUDE_EL_NAME), //
    ELEVATION_MIN(MetadataElement.ELEVATION_MIN_EL_NAME, ContentType.DOUBLE), //
    ELEVATION_MAX(MetadataElement.ELEVATION_MAX_EL_NAME, ContentType.DOUBLE), //
    FLOW_TO_OCEAN(MetadataElement.FLOW_TO_OCEAN_EL_NAME), //
    DOWNSTREAM_HYCOS_STATION(MetadataElement.DOWNSTREAM_HYCOS_STATION_EL_NAME), //
    REGULATION(MetadataElement.REGULATION_EL_NAME), //
    REGULATION_START_DATE(MetadataElement.REGULATION_START_DATE_EL_NAME), //
    REGULATION_END_DATE(MetadataElement.REGULATION_END_DATE_EL_NAME), //
    LAND_USE_CHANGE(MetadataElement.LAND_USE_CHANGE_EL_NAME), //
    SURFACE_COVER(MetadataElement.SURFACE_COVER_EL_NAME), //
    DATA_QUALITY_ICE(MetadataElement.DATA_QUALITY_ICE_EL_NAME), //
    DATA_QUALITY_OPEN(MetadataElement.DATA_QUALITY_OPEN_EL_NAME), //
    DISCHARGE_AVAILABILITY(MetadataElement.DISCHARGE_AVAILABILITY_EL_NAME), //
    WATER_LEVEL_AVAILABILITY(MetadataElement.WATER_LEVEL_AVAILABILITY_EL_NAME), //
    WATER_TEMPERATURE_AVAILABILITY(MetadataElement.WATER_TEMPERATURE_AVAILABILITY_EL_NAME), //
    ICE_ON_OFF_AVAILABILITY(MetadataElement.ICE_ON_OFF_AVAILABILITY_EL_NAME), //
    ICE_THICKNESS_AVAILABILITY(MetadataElement.ICE_THICKNESS_AVAILABILITY_EL_NAME), //
    SNOW_DEPTH_AVAILABILITY(MetadataElement.SNOW_DEPTH_AVAILABILITY_EL_NAME), //
    MEASUREMENT_METHOD_DISCHARGE(MetadataElement.MEASUREMENT_METHOD_DISCHARGE_EL_NAME), //
    MEASUREMENT_METHOD_WATER_TEMPERATURE(MetadataElement.MEASUREMENT_METHOD_WATER_TEMPERATURE_EL_NAME), //
    MEASUREMENT_METHOD_ICE_ON_OFF(MetadataElement.MEASUREMENT_METHOD_ICE_ON_OFF_EL_NAME);

    public static final String KEYWORD_BLUE_CLOUD_EL_NAME = "keywordBlueCloud";
    public static final String KEYWORD_URI_BLUE_CLOUD_EL_NAME = "keywordURIBlueCloud";
    public static final String KEYWORD_EL_NAME = "keyword";
    public static final String KEYWORD_URI_EL_NAME = "keywordURI";
    public static final String DISTRIBUTION_FORMAT_EL_NAME = "format";
    public static final String ONLINE_PROTOCOL_EL_NAME = "protocol";
    public static final String INSTRUMENT_IDENTIFIER_EL_NAME = "instrumentId";
    public static final String INSTRUMENT_TITLE_EL_NAME = "instrumentTitle";

    public static final String PLATFORM_IDENTIFIER_EL_NAME = "platformId";
    public static final String PLATFORM_TITLE_EL_NAME = "platformTitle";
    public static final String PLATFORM_URI_EL_NAME = "platformURI";

    public static final String CRUISE_NAME_EL_NAME = "cruiseName";
    public static final String CRUISE_URI_EL_NAME = "cruiseURI";

    public static final String PROJECT_NAME_EL_NAME = "projectName";
    public static final String PROJECT_URI_EL_NAME = "projectURI";

    public static final String DATA_SIZE_EL_NAME = "dataSize";

    public static final String TIME_INTERPOLATION_EL_NAME = "time_Interpolation"; // from enum InterpolationType

    public static final String TIME_AGGREGATION_DURATION_8601_EL_NAME = "timeAggregationDuration8601";
    public static final String WIS_TOPIC_HIERARCHY_EL_NAME = "wisTopicHierarchy";
    public static final String TIME_RESOLUTION_DURATION_8601_EL_NAME = "timeResolutionDuration8601";

    public static final String TIME_RESOLUTION_EL_NAME = "timeResolution"; // the optional regular distance from a
    // sample to the next one in the time series
    public static final String TIME_SUPPORT_EL_NAME = "timeSupport"; // the time over which the interpolation is
    // calculated (e.g. -3 for total precipitation in
    // the last 3 hours, or -10 in case average over
    // the last 10 minutes)
    public static final String TIME_UNITS_EL_NAME = "timeUnits"; // can be second, hour, etc.
    public static final String TIME_UNITS_ABBREVIATION_EL_NAME = "timeUnitsAbbreviation"; // can be s, h, etc.

    public static final String BNHS_INFO_EL_NAME = "bnhsInfo";

    public static final String UNIQUE_PLATFORM_IDENTIFIER_EL_NAME = "uniquePlatformId";
    public static final String UNIQUE_INSTRUMENT_IDENTIFIER_EL_NAME = "uniqueInstrumentId";
    public static final String UNIQUE_ATTRIBUTE_IDENTIFIER_EL_NAME = "uniqueAttributeId";

    public static final String ORIGINATOR_ORGANISATION_IDENTIFIER_EL_NAME = "origOrgId";
    public static final String ORIGINATOR_ORGANISATION_DESCRIPTION_EL_NAME = "origOrgDesc";

    public static final String THEME_CATEGORY_EL_NAME = "themeCategory";

    public static final String ATTRIBUTE_IDENTIFIER_EL_NAME = "attributeId";
    public static final String ATTRIBUTE_TITLE_EL_NAME = "attributeTitle";
    public static final String ATTRIBUTE_DESCRIPTION_EL_NAME = "attributeDesc";
    public static final String OBSERVED_PROPERTY_URI_EL_NAME = "observedPropertyURI";
    public static final String ATTRIBUTE_UNITS_EL_NAME = "attributeUnits"; // can be meters, etc.
    public static final String ATTRIBUTE_UNITS_URI_EL_NAME = "attributeUnitsURI"; // can be
    // http://codes.wmo.int/common/unit/m3_s-1,
    // etc.
    public static final String ATTRIBUTE_UNITS_ABBREVIATION_EL_NAME = "attributeUnitsAbbreviation"; // can be m, etc.
    public static final String ATTRIBUTE_MISSING_VALUE_EL_NAME = "attributeMissingValue"; // can be -9999

    public static final String ORGANISATION_NAME_EL_NAME = "organisationName";
    public static final String ORGANISATION_URI_EL_NAME = "organisationURI";
    public static final String ORGANISATION_ROLE_EL_NAME = "organisationRole";

    public static final String CROP_TYPES_EL_NAME = "cropTypes";
    public static final String QUANTITY_TYPES_EL_NAME = "quantityTypes";
    public static final String IRRIGATION_TYPES_EL_NAME = "irrigationTypes";
    public static final String LAND_COVER_TYPES_EL_NAME = "landCoverTypes";

    public static final String PRODUCT_TYPE_EL_NAME = "prodType";
    public static final String SENSOR_OP_MODE_EL_NAME = "sensorOpMode";
    public static final String SENSOR_SWATH_EL_NAME = "sensorSwath";
    public static final String SAR_POL_CH_EL_NAME = "sarPolCh";

    public static final String S3_INSTRUMENT_IDX_EL_NAME = "s3InstrumentIdx";
    public static final String S3_PRODUCT_LEVEL_EL_NAME = "s3ProductLevel";
    public static final String S3_TIMELINESS_EL_NAME = "s3Timeliness";

    public static final String HYCOS_IDENTIFIER_EL_NAME = "HYCOSid";
    public static final String STATION_IDENTIFIER_EL_NAME = "Stationid";
    public static final String COUNTRY_EL_NAME = "Country";
    public static final String COUNTRY_ISO3_EL_NAME = "CountryISO3";
    // public static final String INSTITUTE = "Institute";
    // public static final String STATION_NAME="StationName";
    public static final String DATA_DISCLAIMER_EL_NAME = "dataDisclaimer";
    public static final String USE_LEGAL_CONST_EL_NAME = "useLegalConst";
    public static final String USE_LEGAL_CONST_URI_EL_NAME = "useLegalConstURI";
    public static final String RIVER_EL_NAME = "River";
    public static final String RIVER_BASIN_EL_NAME = "RiverBasin";
    public static final String LAKE_STATION_EL_NAME = "LakeStation";
    public static final String GRDC_ID_EL_NAME = "GRDCid";
    public static final String GRDC_ARDB_EL_NAME = "GRDCardb";
    public static final String WMO_REGION_EL_NAME = "WMORegion";
    public static final String LATITUDE_OF_DISCHARGE_EL_NAME = "LatitudeOfDischarge";
    public static final String LONGITUDE_OF_DISCHARGE_EL_NAME = "LongitudeOfDischarge";
    public static final String LATITUDE_EL_NAME = "Latitude";
    public static final String LONGITUDE_EL_NAME = "Longitude";
    public static final String STATUS_EL_NAME = "Status";
    public static final String DRAINAGE_AREA_EL_NAME = "DrainageArea";
    public static final String EFFECTIVE_DRAINAGE_AREA_EL_NAME = "EffectiveDrainageArea";
    public static final String DRAINAGE_SHAPEFILE_EL_NAME = "DrainageShapefile";
    public static final String ALTITUDE_EL_NAME = "Altitude";
    public static final String ELEVATION_MIN_EL_NAME = "ElevationMin";
    public static final String ELEVATION_MAX_EL_NAME = "ElevationMax";
    public static final String ALTITUDE_DATUM_EL_NAME = "AltitudeDatum";
    public static final String FLOW_TO_OCEAN_EL_NAME = "FlowtoOcean";
    public static final String DOWNSTREAM_HYCOS_STATION_EL_NAME = "DownstreamHYCOSstation";
    public static final String REGULATION_EL_NAME = "Regulation";
    public static final String REGULATION_START_DATE_EL_NAME = "RegulationStartDate";
    public static final String REGULATION_END_DATE_EL_NAME = "RegulationEndDate";
    public static final String LAND_USE_CHANGE_EL_NAME = "LandUseChange";
    public static final String SURFACE_COVER_EL_NAME = "SurfaceCover";
    public static final String DATA_QUALITY_ICE_EL_NAME = "DataQualityIce";
    public static final String DATA_QUALITY_OPEN_EL_NAME = "DataQualityOpen";
    public static final String DISCHARGE_AVAILABILITY_EL_NAME = "DischargeAvailability";
    public static final String WATER_LEVEL_AVAILABILITY_EL_NAME = "WaterLevelAvailability";
    public static final String WATER_TEMPERATURE_AVAILABILITY_EL_NAME = "WaterTemperatureAvailability";
    public static final String ICE_ON_OFF_AVAILABILITY_EL_NAME = "IceOnOffAvailability";
    public static final String ICE_THICKNESS_AVAILABILITY_EL_NAME = "IceThicknessAvailability";
    public static final String SNOW_DEPTH_AVAILABILITY_EL_NAME = "SnowDepthAvailability";
    public static final String MEASUREMENT_METHOD_DISCHARGE_EL_NAME = "MeasurementMethodDischarge";
    public static final String MEASUREMENT_METHOD_WATER_TEMPERATURE_EL_NAME = "MeasurementMethodWaterTemperature";
    public static final String MEASUREMENT_METHOD_ICE_ON_OFF_EL_NAME = "MeasurementMethodIceOnOff";
    // public static final String EQUIPMENT ="Equipment";

    private final String name;
    private final boolean volatileElement;
    private final boolean extendedElement;
    private final ContentType type;
    private boolean isEnabled;
    private ComposedElement element;
    private String readableName;

    /**
     * @param name
     */
    MetadataElement(String name) {

	this(name, null, false, false, ContentType.TEXTUAL, true);
    }

    /**
     * @param name
     * @param readableName
     */
    MetadataElement(String name, String readableName) {

	this(name, readableName, false, false, ContentType.TEXTUAL, true);
    }

    /**
     * @param name
     */
    MetadataElement(String name, boolean enabled) {

	this(name, null, false, false, ContentType.TEXTUAL, enabled);
    }

    /**
     * @param name
     * @param readableName
     * @param enabled
     */
    MetadataElement(String name, String readableName, boolean enabled) {

	this(name, readableName, false, false, ContentType.TEXTUAL, enabled);
    }

    /**
     * @param name
     * @param type
     */
    MetadataElement(String name, ContentType type) {

	this(name, null, false, false, type, true);
    }

    /**
     * @param name
     * @param readableName
     * @param type
     */
    MetadataElement(String name, String readableName, ContentType type) {

	this(name, readableName, false, false, type, true);
    }

    /**
     * @param element
     */
    MetadataElement(ComposedElement element) {

	this.name = element.getName();
	this.element = element;
	this.volatileElement = false;
	this.type = ContentType.COMPOSED;
	this.isEnabled = true;
	this.extendedElement = true;
    }

    /**
     * @param name
     * @param volatileElement
     * @param extendedElement
     * @param type
     * @param enabled
     */
    MetadataElement(//
	    String name, //
	    boolean volatileElement, //
	    boolean extendedElement, //
	    ContentType type, //
	    boolean enabled) {

	this(name, null, volatileElement, extendedElement, type, enabled);
    }

    /**
     * @param name
     * @param volatileElement
     * @param type
     */
    MetadataElement(//
	    String name, //
	    String readableName, //
	    boolean volatileElement, //
	    boolean extendedElement, //
	    ContentType type, //
	    boolean enabled) {

	this.name = name;
	this.readableName = readableName;
	this.volatileElement = volatileElement;
	this.type = type;
	this.isEnabled = enabled;
	this.extendedElement = extendedElement;
    }

    public String getName() {

	return name;
    }

    /**
     * @return
     */
    @Override
    public Optional<String> getReadableName() {

	return Optional.ofNullable(readableName);
    }

    @Override
    public ContentType getContentType() {

	return type;
    }

    /**
     * Return <code>true</code> if this element has no correspondent {@link IndexedElement}, <code>false</code> otherwise
     *
     * @return
     */
    public boolean isVolatile() {

	return volatileElement;
    }

    @Override
    public String toString() {

	return getName();
    }

    /**
     * @param name
     * @return
     * @throws NoSuchElementException
     */
    public static MetadataElement fromName(String name) throws IllegalArgumentException {

	return (MetadataElement) Queryable.fromName(name, values());
    }

    /**
     * @param name
     * @return
     */
    public static Optional<MetadataElement> optFromName(String name) {

	try {
	    return Optional.of(fromName(name));

	} catch (IllegalArgumentException ex) {
	}

	return Optional.empty();
    }

    /**
     * @param readableName
     * @return
     * @throws NoSuchElementException
     */
    public static MetadataElement fromReadableName(String readableName) throws IllegalArgumentException {

	return (MetadataElement) Queryable.fromReadableName(readableName, values());
    }

    /**
     * @param readableName
     * @return
     */
    public static Optional<MetadataElement> optFromReadableName(String readableName) {

	try {
	    return Optional.of(fromReadableName(readableName));

	} catch (IllegalArgumentException ex) {
	}

	return Optional.empty();
    }

    /**
     * Returns the ordered list of these {@link MetadataElement}s
     */
    public static List<MetadataElement> listOrderedValues() {

	//
	return Arrays.stream(values()).//
		sorted(Comparator.comparing(MetadataElement::getName)).//
		collect(Collectors.toList());//
    }

    /**
     * @param name
     * @return
     */
    public static boolean hasComposedElement(String name) {

	return listValues().//
		stream().//
		anyMatch(e -> e.hasComposedElement() && e.getName().equals(name));
    }

    /**
     * @param name
     * @return
     */
    public static List<MetadataElement> withComposedElement() {

	return listValues().//
		stream().//
		filter(MetadataElement::hasComposedElement).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public static List<MetadataElement> listValues() {

	return Arrays.asList(values());
    }

    /**
     * @return
     */
    public static List<Queryable> listQueryables() {

	return Arrays.asList(values());
    }

    @Override
    public boolean isEnabled() {

	return this.isEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {

	this.isEnabled = enabled;
    }

    public boolean isExtendedElement() {

	return extendedElement;
    }

    /**
     * @return <code>true</code> if {@link MetadataElement#getContentType()} is {@link ContentType#COMPOSED}
     */
    public boolean hasComposedElement() {

	return element != null;
    }

    /**
     * @return an {@link Optional} with a new instance of {@link ComposedElement} if {@link MetadataElement#hasComposedElement()} is
     * <code>true</code>, otherwise returns {@link Optional#empty()}
     */
    public Optional<ComposedElement> createComposedElement() {

	if (element != null) {

	    try {
		return Optional.ofNullable(ComposedElement.of(element.asDocument(false)));
	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(MetadataElement.class).error(ex);
	    }
	}

	return Optional.empty();
    }
}
