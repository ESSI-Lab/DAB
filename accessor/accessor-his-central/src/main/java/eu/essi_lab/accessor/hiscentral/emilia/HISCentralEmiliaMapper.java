package eu.essi_lab.accessor.hiscentral.emilia;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.accessor.hiscentral.emilia.HISCentralEmiliaConnector.EMILIA_TIMERANGE;
import eu.essi_lab.accessor.hiscentral.emilia.HISCentralEmiliaConnector.EMILIA_VARIABLE;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalCRS;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class HISCentralEmiliaMapper extends FileIdentifierMapper {

    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private Downloader downloader = new Downloader();

    private String PLATFORM_PREFIX = "HIS-CENTRAL-EMILIA";

    private SimpleDateFormat iso8601WMLFormat;

    // private String WMS_URL =
    // "https://geoserver.ifremer.fr/geomesa/wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&STYLES&LAYERS=argo_station&exceptions=application%2Fvnd.ogc.se_inimage&CQL_FILTER=platform_code%3D%27ARGO_PLACEHOLDER%27&CRS=EPSG%3A4326&WIDTH=768&HEIGHT=384&BBOX=-90,-180,90,180";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_EMILIA_NS_URI;
    }

    public HISCentralEmiliaMapper() {
	this.iso8601WMLFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	iso8601WMLFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static void setIndeterminatePosition(GSResource gsResource) {
	setIndeterminatePosition(gsResource, TimeUnit.DAYS.toMillis(30));
    }

    /**
     * @param datasetInfo
     * @param sensorInfo
     * @return
     */
    static OriginalMetadata create(JSONObject datasetInfo, String var, String interpolation, // String level,
	    String startDate, String endDate) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_EMILIA_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("sensor-info", var);
	jsonObject.put("interpolation", interpolation);
	// jsonObject.put("level", level);
	jsonObject.put("starDate", startDate);
	jsonObject.put("endDate", endDate);

	originalMetadata.setMetadata(jsonObject.toString(4));

	return originalMetadata;
    }

    /**
     * @param metadata
     * @return
     */
    private JSONObject retrieveDatasetInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("dataset-info");
    }

    /**
     * @param metadata
     * @return
     */
    private String retrieveSensorInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getString("sensor-info");
    }

    /**
     * @param metadata
     * @return
     */
    private String retrieveDate(OriginalMetadata metadata, String date) {

	return new JSONObject(metadata.getMetadata()).getString(date);
    }

    /**
     * @param metadata
     * @return
     */
    private Optional<String> retrieveInterpolationCode(OriginalMetadata metadata) {

	try {
	    return Optional.of(new JSONObject(metadata.getMetadata()).getString("interpolation"));
	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(getClass()).error(ex);

	}

	return Optional.empty();
    }

    /**
     * @param metadata
     * @return
     */
    private String retrieveLevel(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getString("level");
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	String originalMetadata = originalMD.getMetadata();

	JSONObject stationInfo = retrieveDatasetInfo(originalMD);

	String variable = retrieveSensorInfo(originalMD);

	EMILIA_VARIABLE var = EMILIA_VARIABLE.decode(variable);

	Optional<String> interp = retrieveInterpolationCode(originalMD);

	// EMILIA_TIMERANGE eInterp;

	String interpLabel = "";

	if (interp.isPresent()) {

	    interpLabel = " (" + EMILIA_TIMERANGE.decode(interp.get()).getLabel() + ")";

	    // eInterp = EMILIA_TIMERANGE.decode(interp.get());
	}

	// String level = retrieveLevel(originalMD);

	// EMILIA_TIMERANGE eInterpolation = EMILIA_TIMERANGE.decode(interp);

	// EMILIA_LEVEL eLev = EMILIA_LEVEL.decode(level);

	if (var != null) {

	    HISCentralEmiliaStation station = new HISCentralEmiliaStation(stationInfo);

	    BigDecimal alt = station.getAltitude();
	    BigDecimal lat = station.getLatitude();
	    BigDecimal lon = station.getLongitude();

	    String stationName = station.getName();
	    String stationType = station.getType();

	    String varName = var.getLabel();
	    String units = var.getUnits();

	    String startDate = retrieveDate(originalMD, "starDate");
	    String endDate = retrieveDate(originalMD, "endDate");
	    String dataUrl = HISCentralEmiliaConnector.BASE_URL;
	    String unitName = null;
	    int dataLength = 0;

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    // IDENTIFIER
	    String id = null;
	    try {
		id = StringUtils.hashSHA1messageDigest(stationName + " - " + varName);
		coreMetadata.setIdentifier(id);
		coreMetadata.getMIMetadata().setFileIdentifier(id);
		// coreMetadata.getDataIdentification().setResourceIdentifier(stationCode);
	    } catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    ResponsibleParty publisherContact = new ResponsibleParty();
	    publisherContact.setOrganisationName("Settore Idrologico e Geologico - Regione Emilia-Romagna");
	    publisherContact.setRoleCode("publisher");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	    // keywords
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationName);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationType);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(varName);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("EMILIA-ROMAGNA");

	    // bbox
	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("EPSG:4326");
	    referenceSystem.setCodeSpace("EPSG");
	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);
	    if (lon != null && lat != null) {

		coreMetadata.addBoundingBox(lat.doubleValue(), lon.doubleValue(), lat.doubleValue(), lon.doubleValue());
	    }

	    // platform

	    String platformIdentifier = PLATFORM_PREFIX + stationType + ":" + stationName;

	    String parameterIdentifier = PLATFORM_PREFIX + ":" + varName;

	    coreMetadata.addDistributionFormat("WaterML 1.1");

	    coreMetadata.getMIMetadata().setLanguage("English");

	    coreMetadata.getMIMetadata().setCharacterSetCode("utf8");

	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    MIPlatform platform = new MIPlatform();

	    platform.setMDIdentifierCode(platformIdentifier);

	    platform.setDescription(stationName);

	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(stationName);
	    platform.setCitation(platformCitation);

	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("platform");
	    keyword.addKeyword(stationName);// or platformDescription
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	    if (alt != null) {
		try {
		    VerticalExtent verticalExtent = new VerticalExtent();
		    Double vertical = alt.doubleValue();
		    verticalExtent.setMinimumValue(vertical);
		    verticalExtent.setMaximumValue(vertical);
		    VerticalCRS verticalCRS = new VerticalCRS();
		    // verticalCRS.setId(siteInfo.getVerticalDatum());
		    verticalExtent.setVerticalCRS(verticalCRS);
		    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
		} catch (Exception e) {
		    String warn = "Unable to parse site elevation for station: " + stationName + " " + e.getMessage();
		    logger.warn(warn);
		}
	    }
	    //
	    // String posAccuracyM = siteInfo.getProperty(SiteProperty.POS_ACCURACY_M);
	    // if (posAccuracyM.length() > 0) {
	    // MDResolution resolution = new MDResolution();
	    // try {
	    // Double value = Double.parseDouble(posAccuracyM);
	    // resolution.setDistance("m", value);
	    // coreMetadata.getMIMetadata().getDataIdentification().setSpatialResolution(resolution);
	    // } catch (Exception e) {
	    // String warn = "Unable to parse site position accuracy in metres: " + posAccuracyM + " " + e.getMessage();
	    // logger.warn(warn);
	    // }
	    // }
	    //
	    TemporalExtent temporalExtent = new TemporalExtent();
	    if (startDate != null && endDate != null) {
		temporalExtent.setBeginPosition(startDate);
		temporalExtent.setEndPosition(endDate);
		temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

	    }
	    
	    setIndeterminatePosition(dataset);

	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " - " + varName + interpLabel);

	    // coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(title);

	    coreMetadata.getMIMetadata().getDataIdentification().setAbstract(stationName + " - " + varName + interpLabel);

	    GridSpatialRepresentation grid = new GridSpatialRepresentation();
	    grid.setNumberOfDimensions(1);
	    grid.setCellGeometryCode("point");
	    Dimension time = new Dimension();
	    time.setDimensionNameTypeCode("time");
	    Long valueCount = Long.valueOf(dataLength);
	    if (valueCount != null) {
		time.setDimensionSize(new BigInteger("" + valueCount));
		ExtensionHandler extensionHandler = dataset.getExtensionHandler();
		extensionHandler.setDataSize(valueCount);
	    }

	    // if (series.isTimeScaleRegular()) {
	    // Number timeSpacing = series.getTimeScaleTimeSpacing();
	    // if (timeSpacing != null && timeSpacing.doubleValue() > Math.pow(10, -16)) {
	    // String resolutionUOM = series.getTimeScaleUnitName();
	    // time.setResolution(resolutionUOM, timeSpacing.doubleValue());
	    // }
	    // }
	    grid.addAxisDimension(time);
	    coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

	    HISCentralEmiliaIdentifierMangler mangler = new HISCentralEmiliaIdentifierMangler();

	    // site code network + site code: both needed for access
	    mangler.setPlatformIdentifier(stationName + ":" + stationType);

	    // variable vocabulary + variable code: both needed for access
	    mangler.setParameterIdentifier(PLATFORM_PREFIX + ":" + var.name());

	    if (interp.isPresent()) {
		
		mangler.setInterpolationIdentifier(interp.get());
	    }

	    // String qualityCode = series.getQualityControlLevelID();
	    //
	    // // ARPA-ER hacks
	    // if (hisServerEndpoint.contains("hydrolite.ddns.net") && qualityCode != null &&
	    // qualityCode.equals("-9999")) {
	    // qualityCode = "1";
	    // }
	    //
	    // mangler.setQualityIdentifier(qualityCode);

	    mangler.setSourceIdentifier(id);

	    CoverageDescription coverageDescription = new CoverageDescription();

	    coverageDescription.setAttributeIdentifier(PLATFORM_PREFIX + ":" + varName);
	    // variable = (variable.contains(" ")) ? variable.split(" ")[0] : varName;
	    coverageDescription.setAttributeTitle(varName);

	    if (interp.isPresent()) {

		InterpolationType interpolation = null;

		switch (interp.get().toLowerCase()) {
		case "average":
		    interpolation = InterpolationType.AVERAGE;
		    break;
		case "minimum":
		    interpolation = InterpolationType.MIN;
		    break;
		case "maximum":
		    interpolation = InterpolationType.MAX;
		    break;
		case "accumulation":
		    interpolation = InterpolationType.TOTAL;
		    break;
		case "istantaneous":
		    interpolation = InterpolationType.INSTANT_TOTAL;
		    break;
		case "vectorial":
		    interpolation = InterpolationType.STATISTICAL;
		    break;
		case "valid":
		    interpolation = InterpolationType.CONTINUOUS;
		    break;
		default:
		    interpolation = InterpolationType.decode(interp.get());
		    GSLoggerFactory.getLogger(getClass()).error("not known interpolation: {}", interp);
		    break;
		}

		dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	    }

	    //
	    // intendedObservationSpacing = "PT1H"
	    //
	    // 15 -> timeResolution
	    // M -> timeUnits

	    // if (intervalTime != null) {
	    // String timeUnits = intervalTime.substring(2, intervalTime.length() - 1);
	    // String timeResolution = intervalTime.substring(intervalTime.length() - 1);
	    //
	    // dataset.getExtensionHandler().setTimeUnits(timeUnits);
	    // dataset.getExtensionHandler().setTimeResolution(timeResolution);
	    // dataset.getExtensionHandler().setTimeSupport(timeUnits);
	    // }

	    // InterpolationType interpolation = getInterpolationType(series);
	    // if (interpolation != null) {
	    // dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	    // }
	    //
	    // Number timeSupport = getTimeScaleTimeSupport(series);
	    // if (timeSupport != null && !timeSupport.toString().equals("0")) {
	    // dataset.getExtensionHandler().setTimeSupport(timeSupport.toString());
	    // }
	    //
	    // String timeUnits = getTimeScaleUnitName(series);
	    // dataset.getExtensionHandler().setTimeUnits(timeUnits);
	    //
	    // String timeUnitsAbbreviation = getTimeScaleUnitAbbreviation(series);
	    // dataset.getExtensionHandler().setTimeUnitsAbbreviation(timeUnitsAbbreviation);

	    dataset.getExtensionHandler().setTimeUnits("M");
	    dataset.getExtensionHandler().setTimeResolution("15");
	    dataset.getExtensionHandler().setTimeSupport("15");

	    dataset.getExtensionHandler().setCountry("ITA");

	    //
	    String missingValue = "-9999";
	    dataset.getExtensionHandler().setAttributeMissingValue(missingValue);
	    //
	    if (units != null) {
		dataset.getExtensionHandler().setAttributeUnits(units);
	    }
	    //
	    // String unitAbbreviation = getUnitAbbreviation(series);
	    // dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unitAbbreviation);

	    String attributeDescription = varName + " Units: " + units + " No data value: " + missingValue;

	    coverageDescription.setAttributeDescription(attributeDescription);
	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    String identifier = mangler.getMangling();

	    coreMetadata.addDistributionOnlineResource(identifier, dataUrl, CommonNameSpaceContext.HISCENTRAL_EMILIA_NS_URI, "download");

	    coreMetadata.getDataIdentification().setResourceIdentifier(identifier);

	    String resourceIdentifier = generateCode(dataset, stationName + "-" + varName);

	    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	    coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

	}

    }

    private Double getDouble(JSONObject result, String key) {
	try {
	    Double d = result.optDouble(key);
	    if (d == null || d.isNaN()) {
		return null;
	    }
	    return d;
	} catch (Exception e) {
	    logger.warn("Error reading key {}: ", key, e);
	    return null;
	}
    }

    private JSONArray getJSONArray(JSONObject result, String key) {
	try {
	    boolean hasKey = result.has(key);
	    if (!hasKey) {
		return new JSONArray();
	    }
	    JSONArray ret = result.getJSONArray(key);
	    if (ret == null || ret.length() == 0) {
		ret = new JSONArray();
	    }
	    return ret;
	} catch (Exception e) {
	    logger.warn("Error getting json array", e);
	    return new JSONArray();
	}

    }

    private String getString(JSONObject result, String key) {
	try {
	    String ret = result.optString(key, null);
	    if (ret == null || "".equals(ret) || "[]".equals(ret) || "null".equals(ret)) {
		return null;
	    }
	    return ret;
	} catch (Exception e) {
	    logger.warn("Error reading key {}: ", key, e);
	    return null;
	}
    }

    /**
     * Gets ISO 19115 UTC ISO 8601 format from a WML time position
     * 
     * @param timePosition
     * @return
     */
    private String normalizeUTCPosition(String timePosition) {
	try {
	    iso8601WMLFormat.parse(timePosition);
	} catch (ParseException e) {
	    String error = "Time position parsing error" + e.getMessage();
	    logger.error(error);
	}
	if (!timePosition.endsWith("Z")) {
	    timePosition += "Z";
	}
	return timePosition;
    }

    public static void main(String[] args) {
	String endTime = "2020-12-02T06:06:30.000+0000";
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");// 2020-01-02T06:06:30.000+0000
	df.setTimeZone(TimeZone.getTimeZone("UTC"));
	Date d = null;
	String tt = "2004-01-01T00:00:00";
	SimpleDateFormat iso8601WMLFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601WMLFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	try {
	    iso8601WMLFormat.parse(tt);
	} catch (ParseException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	try {
	    d = df.parse(endTime);

	} catch (Exception e) {
	    // TODO: handle exception
	    e.printStackTrace();
	}
	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	cal.setTime(d);
	System.out.println(cal.get(Calendar.YEAR));
	System.out.println(cal.get(Calendar.MONTH));
	System.out.println(cal.get(Calendar.DAY_OF_MONTH));
    }
}
