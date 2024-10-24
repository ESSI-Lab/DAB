package eu.essi_lab.accessor.usgswatersrv;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import eu.essi_lab.accessor.usgswatersrv.codes.USGSAgency;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSAltitudeDatum;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSCoordinateAccuracy;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSCoordinateDatum;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSCounty;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSDataType;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSHydrologicalUnit;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSParameterNumeric;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSSiteType;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSState;
import eu.essi_lab.accessor.usgswatersrv.codes.USGSStatistics;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.wms._1_3_0.Keyword;
import eu.essi_lab.lib.net.protocols.NetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author boldrini
 * @author Fabrizio
 */
public class USGSMapper extends OriginalIdentifierMapper {

    private static final String separator = ";";
    private static final String USGS_MAPPER_MISSING_IDENTIFIER_ERROR = "USGS_MAPPER_MISSING_IDENTIFIER_ERROR";
    private static final String USGS_MAPPER_MISSING_LATITUDE_ERROR = "USGS_MAPPER_MISSING_LATITUDE_ERROR";
    private static final String USGS_MAPPER_MISSING_LONGITUDE_ERROR = "USGS_MAPPER_MISSING_LONGITUDE_ERROR";
    private static final String USGS_MAPPER_UNEXPECTED_ERROR = "USGS_MAPPER_UNEXPECTED_ERROR";
    private static final String USGS_URN = "urn:gov:usgs:nwis:";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	try {
	    String seriesMetadata = resource.getOriginalMetadata().getMetadata();

	    String[] split = seriesMetadata.split("\n");

	    if (split.length < 6) {
		return null;
	    }

	    seriesMetadata = split[0] + "\n" + split[1] + "\n" + split[2] + "\n";

	    USGSMetadata seriesValues = new USGSMetadata(seriesMetadata);

	    return seriesValues.get("ts_id");

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	String seriesMetadata = originalMD.getMetadata();

	try {

	    String[] split = seriesMetadata.split("\n");
	    String siteMetadata = null;
	    if (split.length < 6) {
		GSLoggerFactory.getLogger(USGSMapper.class).error("Missing metadata");
		return null;
	    }
	    seriesMetadata = split[0] + "\n" + split[1] + "\n" + split[2] + "\n";
	    siteMetadata = split[3] + "\n" + split[4] + "\n" + split[5] + "\n";

	    /**
	     * SITE PROPERTIES
	     */
	    USGSMetadata siteValues = new USGSMetadata(siteMetadata);

	    String agencyCode = siteValues.get("agency_cd"); // Agency: USGS
	    String siteNumber = siteValues.get("site_no"); // Site identification number: 02339225
	    String stationName = siteValues.get("station_nm"); // Site name: WEHADKEE CREEK BELOW ROCK MILLS, ALA.
	    String siteTypeCode = siteValues.get("site_tp_cd"); // Site type: ST
	    String dmsLatitude = siteValues.get("lat_va");
	    String dmsLongitude = siteValues.get("long_va");
	    String decimalLatitude = siteValues.get("dec_lat_va"); // Decimal latitude: 33.1223435
	    String decimalLongitude = siteValues.get("dec_long_va"); // Decimal longitude: -85.2491113
	    String coordinateMethodCode = siteValues.get("coord_meth_cd"); //
	    String coordinateAccuracyCode = siteValues.get("coord_acy_cd"); // Latitude-longitude accuracy: U
	    String coordinateDatumCode = siteValues.get("coord_datum_cd"); //
	    String decimalCoordinateDatumCode = siteValues.get("dec_coord_datum_cd"); // Decimal Latitude-longitude
	    // datum:
	    String districtCode = siteValues.get("district_cd"); //
	    String stateCode = siteValues.get("state_cd"); //
	    String countyCode = siteValues.get("county_cd"); //
	    String countryCode = siteValues.get("country_cd"); //
	    String landNetLocationDescription = siteValues.get("land_net_ds"); //
	    String locationMapName = siteValues.get("map_nm"); //
	    String locationMapScale = siteValues.get("map_scale_fc");
	    String altitude = siteValues.get("alt_va"); // Altitude of Gage/land surface: 647.21
	    String altitudeMethodCode = siteValues.get("alt_meth_cd"); //
	    String altitudeAccuracy = siteValues.get("alt_acy_va"); // Altitude accuracy: .01
	    String altitudeDatumCode = siteValues.get("alt_datum_cd"); // Altitude datum: NGVD29
	    String hydrologicUnit = siteValues.get("huc_cd"); // Hydrologic unit code: 03130002
	    String basinCode = siteValues.get("basin_cd"); // Drainage basin code
	    String topographicSettingCode = siteValues.get("topo_cd"); // Topographic setting code
	    String instrumentsCode = siteValues.get("instruments_cd"); // Flags for instruments at site
	    String constructionDate = siteValues.get("construction_dt"); // Date of first construction
	    String inventoryDate = siteValues.get("inventory_dt");// Date site established or inventoried
	    String drainageArea = siteValues.get("drain_area_va");// Drainage area
	    String contributingDrainageArea = siteValues.get("contrib_drain_area_va");// Contributing drainage area
	    String timeZoneCode = siteValues.get("tz_cd");// Time Zone abbreviation
	    String localTimeSavings = siteValues.get("local_time_fg");// Site honors Daylight Savings Time
	    String reliabilityCode = siteValues.get("reliability_cd");// Data reliability code
	    String gwFileCode = siteValues.get("gw_file_cd");// Data-other GW files
	    String nationalAquiferCode = siteValues.get("nat_aqfr_cd");// National aquifer code
	    String localAquiferCode = siteValues.get("aqfr_cd");// Local aquifer code
	    String localAquiferTypeCode = siteValues.get("aqfr_type_cd");// Local aquifer type code
	    String wellDepth = siteValues.get("well_depth_va");// Well depth
	    String holeDepth = siteValues.get("hole_depth_va");// Hole depth
	    String depthSourceCode = siteValues.get("depth_src_cd");// Source of depth data
	    String projectNumber = siteValues.get("project_no");// Project number

	    // SITE PROPERTIES CODES

	    HashMap<String, String> countyProperties = USGSCounty.getInstance().getProperties(stateCode + countyCode);
	    String countyName = countyProperties.get("county_nm");
	    String stateName = null;

	    if (countryCode.equals("US")) {
		stateName = USGSState.getInstance().getProperties(stateCode).get("STATE_NAME");
	    }

	    HashMap<String, String> agencyProperties = USGSAgency.getInstance().getProperties(agencyCode);
	    String agencyName = agencyProperties.get("party_nm"); // U.S. Geological Survey
	    HashMap<String, String> siteTypeProperties = USGSSiteType.getInstance().getProperties(siteTypeCode);
	    String coordinateAccuracyProperties = USGSCoordinateAccuracy.getInstance().getProperties(coordinateAccuracyCode)
		    .get("gw_ref_ds"); //
	    // NAD83
	    String coordinateDatumDescription = USGSCoordinateDatum.getInstance().getProperties(decimalCoordinateDatumCode)
		    .get("gw_ref_cd");//
	    // North American Datum of 1983

	    String siteTypeName = siteTypeProperties.get("site_tp_ln");
	    String siteTypeDescription = siteTypeProperties.get("site_tp_ds");
	    // Accurate to + or - .1 sec (Differentially-Corrected GPS).

	    String altitudeDatumDescription = USGSAltitudeDatum.getInstance().getProperties(altitudeDatumCode).get("Description");

	    String hydrologicUnitRegion = USGSHydrologicalUnit.getInstance().getRegion(hydrologicUnit); //
	    // New England Region
	    String hydrologicUnitSubregion = USGSHydrologicalUnit.getInstance().getSubregion(hydrologicUnit); //
	    // St. John
	    String hydrologicUnitAccountingUnit = USGSHydrologicalUnit.getInstance().getAccountingUnit(hydrologicUnit); //
	    // St. John
	    String hydrologicUnitCatalogingUnit = USGSHydrologicalUnit.getInstance().getCatalogingUnit(hydrologicUnit); //
	    // Upper St. John

	    /**
	     * SERIES VALUES
	     */
	    USGSMetadata seriesValues = new USGSMetadata(seriesMetadata);

	    String dataTypeCode = seriesValues.get("data_type_cd"); // Data type: dv
	    String parameterCode = seriesValues.get("parm_cd"); // Parameter code: 00060
	    String statisticalCode = seriesValues.get("stat_cd"); // Statistical code: 00003
	    String timeSeriesId = seriesValues.get("ts_id"); // Internal timeseries ID: 1972
	    String additionalMeasurement = seriesValues.get("loc_web_ds"); // Additional measurement description:
	    String mediumGroup = seriesValues.get("medium_grp_cd"); // Medium group code: wat
	    String parameterGroup = seriesValues.get("parm_grp_cd"); // Parameter group code:
	    String srsId = seriesValues.get("srs_id"); // SRS ID: 1645423
	    String access = seriesValues.get("access_cd"); // Access code: 0
	    String begin = seriesValues.getBeginDate(); // Begin date: 1978-10-01
	    String end = seriesValues.getEndDate(); // End date: 1990-01-31
	    String countNumber = seriesValues.get("count_nu"); // Record count: 4141

	    String dataTypeDescription = USGSDataType.getInstance().getProperties(dataTypeCode).get("description"); //

	    // Daily values (once daily measurements or summarized information for a particular day, such as daily
	    // maximum, minimum and mean)

	    HashMap<String, String> parameterProperties = USGSParameterNumeric.getInstance().getProperties(parameterCode);
	    String parameterUnit = null;
	    String parameterUnitAbbreviation = null;
	    String parameterName = null;
	    String timeBasis = null;
	    String statisticalBasis = null;
	    String parameterDescription = null;
	    if (parameterProperties != null) {
		parameterName = parameterProperties.get("parm_nm"); //
		// Location in cross section, distance from right bank looking upstream, feet
		parameterGroup = parameterProperties.get("group"); // Information

		parameterUnitAbbreviation = parameterProperties.get("parm_unit");// deg C
		parameterUnit = parameterUnitAbbreviation;
		if (parameterName.contains(",")) {
		    int last = parameterName.lastIndexOf(",");
		    parameterUnit = parameterName.substring(last + 1).trim();
		    parameterName = parameterName.substring(0, last).trim();
		}

		// statistical basis and time basis may not be correct
		statisticalBasis = parameterProperties.get("result_statistical_basis");// Mean
		timeBasis = parameterProperties.get("result_time_basis");// 1 Day
		parameterDescription = parameterProperties.get("SRSName");// Stream flow, mean. daily
		String parameterResultParticleSizeBasis = parameterProperties.get("result_particle_size_Basis"); // < 2
														 // mm
		String parameterResultSampleFraction = parameterProperties.get("result_sample_fraction"); // Dissolved
		String parameterResultTemperatureBasis = parameterProperties.get("result_temperature_basis"); // 60 deg
													      // C
		String parameterResultWeightBasis = parameterProperties.get("result_weight_basis"); // Dry
	    } else {
		GSLoggerFactory.getLogger(USGSMapper.class).error("Unrecognized parameter: " + parameterCode);
		return null;
	    }

	    if (dataTypeCode.equals("iv") || dataTypeCode.equals("uv") || dataTypeCode.equals("rt")) {
		// overwrite
		statisticalCode = "00011"; // for these data types we can infer that it is instantaneous
		timeBasis = null;
	    }

	    String statisticalName = null;
	    if (statisticalCode != null && !statisticalCode.isEmpty()) {
		statisticalName = USGSStatistics.getInstance().getProperties(statisticalCode).get("stat_NM"); // MAXIMUM,
													      // MEAN,
													      // etc.
	    }

	    if (dataTypeCode.equals("dv")) { // daily values
		timeBasis = "1 Day";
	    }

	    Dataset dataset = new Dataset();
	    dataset.setSource(source);

	    if (countryCode != null && !countryCode.isEmpty()) {
		Country country = Country.decode(countryCode);
		if (country != null) {
		    dataset.getExtensionHandler().setCountry(country.getShortName());
		}

	    }

	    MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	    ResponsibleParty contact = new ResponsibleParty();
	    contact.setOrganisationName(agencyName);

	    contact.setRoleCode("originator");
	    miMetadata.addContact(contact);
	    miMetadata.getDataIdentification().addPointOfContact(contact);

	    MIPlatform platform = new MIPlatform();
	    platform.setMDIdentifierCode(USGS_URN + siteNumber);
	    platform.setDescription(stationName);
	    Citation citation = new Citation();
	    citation.setTitle(stationName);
	    platform.setCitation(citation);

	    miMetadata.addMIPlatform(platform);

	    addKeyword(miMetadata, parameterUnit);

	    HashSet<String> hucs = new HashSet<>();
	    hucs.add(hydrologicUnitRegion);
	    hucs.add(hydrologicUnitSubregion);
	    hucs.add(hydrologicUnitAccountingUnit);
	    hucs.add(hydrologicUnitCatalogingUnit);

	    for (String huc : hucs) {
		addKeyword(miMetadata, huc);
	    }

	    GridSpatialRepresentation grid = new GridSpatialRepresentation();
	    grid.setNumberOfDimensions(1);
	    grid.setCellGeometryCode("point");
	    Dimension time = new Dimension();
	    time.setDimensionNameTypeCode("time");
	    try {
		Long valueCount = Long.parseLong(countNumber);
		time.setDimensionSize(new BigInteger("" + valueCount));
		ExtensionHandler extensionHandler = dataset.getExtensionHandler();
		extensionHandler.setDataSize(valueCount);
	    } catch (Exception e) {
	    }
	    grid.addAxisDimension(time);
	    miMetadata.addGridSpatialRepresentation(grid);

	    String stat = statisticalName == null || statisticalName.isEmpty() ? "" : " (" + statisticalName + ")";
	    String title = parameterName + stat.toLowerCase() + " at the station: " + stationName;

	    CoverageDescription coverageDescription = new CoverageDescription();

	    coverageDescription.setAttributeTitle(parameterName);
	    coverageDescription.setAttributeDescription(parameterName + stat);
	    coverageDescription.setAttributeIdentifier(USGS_URN + "variable:" + parameterCode + ":" + statisticalCode);

	    String interpolation = null;

	    if (statisticalName != null && !statisticalName.equals("")) {
		switch (statisticalName) {
		case "MEAN":
		    interpolation = InterpolationType.AVERAGE.name();
		    break;
		case "MINIMUM":
		    interpolation = InterpolationType.MIN.name();
		    break;
		case "MAXIMUM":
		    interpolation = InterpolationType.MAX.name();
		    break;
		case "SUM":
		    interpolation = InterpolationType.TOTAL.name();
		    break;
		case "INSTANTANEOUS":
		    interpolation = InterpolationType.CONTINUOUS.name();
		    break;
		default:
		    interpolation = statisticalCode;
		    break;
		}
		dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	    }

	    if (timeBasis != null && !timeBasis.equals("")) {
		Integer quantity = null;
		String timeUnits = "";
		if (timeBasis.contains(" ")) {
		    String[] sp = timeBasis.split(" ");
		    quantity = Integer.parseInt(sp[0]);
		    timeUnits = sp[1];
		} else {
		    switch (timeBasis) {
		    case "Daily":
			quantity = 1;
			timeUnits = "Day";
			break;
		    case "Ultimate":
		    default:
			break;
		    }
		}
		if (quantity != null) {
		    dataset.getExtensionHandler().setTimeSupport("" + quantity);
		    dataset.getExtensionHandler().setTimeUnits(timeUnits);
		}
	    }

	    dataset.getExtensionHandler().setAttributeUnits(parameterUnit);
	    dataset.getExtensionHandler().setAttributeUnitsAbbreviation(parameterUnitAbbreviation);

	    miMetadata.addCoverageDescription(coverageDescription);

	    miMetadata.getDataIdentification().setCitationTitle(title);

	    if (decimalLatitude == null || decimalLatitude.isEmpty()) {

		GSLoggerFactory.getLogger(USGSMapper.class).error("Missing latitude of station: " + stationName);

		return null;
	    }

	    if (coordinateDatumCode == null || coordinateDatumCode.isEmpty()) {

		GSLoggerFactory.getLogger(USGSMapper.class).error("Missing longitude of station: " + stationName);

		return null;

	    }

	    double lat = Double.parseDouble(decimalLatitude);
	    double lon = Double.parseDouble(decimalLongitude);

	    miMetadata.getDataIdentification().addGeographicBoundingBox(lat, lon, lat, lon);

	    if (altitude != null && !altitude.isEmpty()) {

		double d = Double.parseDouble(altitude);
		miMetadata.getDataIdentification().addVerticalExtent(d, d);
		// VerticalExtent verticalExtent = miMetadata.getDataIdentification().getVerticalExtent();
	    }

	    TimeZone timeZone = TimeZone.getTimeZone("GMT");

	    timeZone = TimeZone.getTimeZone(timeZoneCode);

	    if (begin != null && end != null) {

		Date beginDate = getDate(begin, timeZone);
		Date endDate = getDate(end, timeZone);

		if (beginDate != null && endDate != null) {
		    begin = ISO8601DateTimeUtils.getISO8601DateTime(beginDate);
		    end = ISO8601DateTimeUtils.getISO8601DateTime(endDate);

		    miMetadata.getDataIdentification().addTemporalExtent(begin, end);

		    Date currentDate = new Date();
		    long milliseconds = 1000l * 60l * 60l * 96l;

		    // the time series is real time, as it ends in the last 4 days
		    if (Math.abs(currentDate.getTime() - endDate.getTime()) < (milliseconds)) {

			miMetadata.getDataIdentification().getTemporalExtent().setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		    }
		}
	    }

	    ReferenceSystem ref = new ReferenceSystem();
	    ref.setCode(decimalCoordinateDatumCode);

	    miMetadata.addReferenceSystemInfo(ref);

	    NetProtocol protocol = null;
	    String linkage = null;

	    USGSIdentifierMangler mangler = new USGSIdentifierMangler();

	    if (dataTypeCode != null && (dataTypeCode.equals("iv") || dataTypeCode.equals("uv") || dataTypeCode.equals("rt"))) {

		protocol = NetProtocols.USGS_IV;
		linkage = USGSClient.DEFAULT_IV_URL;
		mangler.setTimeSeries(timeSeriesId);
		mangler.setParameterIdentifier(parameterCode);
	    }

	    String sc = statisticalCode == null || statisticalCode.isEmpty() ? "" : statisticalCode;

	    if (dataTypeCode != null && dataTypeCode.equals("dv")) {

		protocol = NetProtocols.USGS_DV;
		linkage = USGSClient.DEFAULT_DV_URL;
		mangler.setParameterIdentifier(parameterCode);
		mangler.setStatisticalCode(statisticalCode);
	    }

	    if (protocol != null && linkage != null) {

		Online online = new Online();

		online.setProtocol(protocol.getCommonURN());
		online.setLinkage(linkage);

		mangler.setSite(siteNumber);

		online.setName(mangler.getMangling());
		online.setDescription("USGS:" + parameterCode + sc + "@" + "USGS:" + siteNumber);
		online.setFunctionCode("download");

		miMetadata.getDistribution().addDistributionOnline(online);
	    }

	    addKeyword(miMetadata, stationName);
	    addKeyword(miMetadata, parameterName);
	    addKeyword(miMetadata, parameterCode + sc);
	    if (stateName != null) {
		addKeyword(miMetadata, stateName);
	    }
	    addKeyword(miMetadata, countyName);

	    return dataset;

	} catch (Exception e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error("Metadata with error: {}", seriesMetadata);

	    throw GSException.createException(//
		    getClass(), //
		    "Unexpected exception: " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    USGS_MAPPER_UNEXPECTED_ERROR);

	}
    }

    private Date getDate(String str, TimeZone timeZone) {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	sdf.setTimeZone(timeZone);
	try {
	    return sdf.parse(str);
	} catch (ParseException e) {
	}
	sdf = new SimpleDateFormat("yyyy");
	sdf.setTimeZone(timeZone);
	try {
	    return sdf.parse(str);
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return USGSConnector.USGS_SITE_METADATA;
    }

    /**
     * @param online
     * @return
     */
    private String createIdFromOnline(Online online) {

	String linkage = online.getLinkage();
	String name = online.getName();

	if (StringUtils.countMatches(name, "@") == 1) {

	    String variableCode = name.split("@")[0];
	    String siteCode = name.split("@")[1];
	    String variableVocabulary = null;
	    String siteNetwork = null;

	    return createDatasetId(variableVocabulary, variableCode, siteNetwork, siteCode, linkage);
	}

	return name;
    }

    /**
     * @param variableVocabulary
     * @param variableCode
     * @param siteNetwork
     * @param siteCode
     * @param url
     * @return
     */
    private String createDatasetId(String variableVocabulary, String variableCode, String siteNetwork, String siteCode, String url) {

	try {
	    if (url != null) {
		String hash = "" + URLEncoder.encode(url, "UTF-8").hashCode();
		return variableCode + "@" + siteCode + "@" + hash;
	    } else {
		return variableCode + "@" + siteCode;
	    }
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}

	return null;
    }

    /**
     * @param mdMetadata
     * @param thesaurus
     * @param value
     */
    private void addKeyword(MIMetadata mdMetadata, String value) {

	Keywords keywords = new Keywords();

	Keyword key = new Keyword();
	key.setValue(value);
	keywords.addKeywords(Arrays.asList(value));

	mdMetadata.getDataIdentification().addKeywords(keywords);
    }

}
