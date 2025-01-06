package eu.essi_lab.accessor.hiscentral.umbria;

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
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.accessor.hiscentral.umbria.HISCentralUmbriaConnector.SORT_ORDER;
import eu.essi_lab.accessor.hiscentral.umbria.HISCentralUmbriaConnector.UMBRIA_Variable;
import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
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
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

public class HISCentralUmbriaMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private Downloader downloader = new Downloader();

    private String PLATFORM_PREFIX = "HIS-CENTRAL-UMBRIA";

    private SimpleDateFormat iso8601WMLFormat;

    /**
     * @param datasetInfo
     * @param sensorInfo
     * @return
     */
    static OriginalMetadata create(JSONObject datasetInfo, JSONObject sensorInfo, String varName, String resourceId, String timeType, String startDate) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_UMBRIA_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("sensor-info", sensorInfo);
	jsonObject.put("resource-identifier", resourceId);
	jsonObject.put("variable", varName);
	jsonObject.put("startDate", startDate);
	jsonObject.put("timeType", timeType);

	originalMetadata.setMetadata(jsonObject.toString(4));

	return originalMetadata;
    }

    // private String WMS_URL =
    // "https://geoserver.ifremer.fr/geomesa/wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&STYLES&LAYERS=argo_station&exceptions=application%2Fvnd.ogc.se_inimage&CQL_FILTER=platform_code%3D%27ARGO_PLACEHOLDER%27&CRS=EPSG%3A4326&WIDTH=768&HEIGHT=384&BBOX=-90,-180,90,180";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_UMBRIA_NS_URI;
    }

    public HISCentralUmbriaMapper() {
	this.iso8601WMLFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601WMLFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	downloader.setConnectionTimeout(TimeUnit.MINUTES, 2);
    }

    public static void setIndeterminatePosition(GSResource gsResource) {
	setIndeterminatePosition(gsResource, TimeUnit.DAYS.toMillis(30));
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    /**
     * @param startTime
     * @return
     */
    private String retrieveStartDate(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optString("startDate");
    }

    /**
     * @param sensor metadata
     * @return
     */
    private JSONObject retrieveSensorInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optJSONObject("sensor-info");
    }

    /**
     * @param 
     * @return station metadata
     */
    private JSONObject retrieveStationInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optJSONObject("dataset-info");
    }

    /**
     * @param 
     * @return UMBRIA_VARIABLE name
     */
    private String retieveVariableInfo(OriginalMetadata metadata) {
	return new JSONObject(metadata.getMetadata()).optString("variable");
    }
    
    /**
     * @param 
     * @return resourceIdentifier
     */
    private String retieveResourceIdentifier(OriginalMetadata metadata) {
	return new JSONObject(metadata.getMetadata()).optString("resource-identifier");
    }

    /**
     * @param time type (e.g. "0-24", "9-9", "NA")
     * @return
     */
    private String retrieveTimeType(OriginalMetadata metadata) {
	return new JSONObject(metadata.getMetadata()).optString("timeType");
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	/**
	 * STATION INFO
	 * {
	 * "_id": 92,
	 * "ID_STAZIONE": "74200",
	 * "NOME_STAZIONE": "Polvese 1",
	 * "LAT": 43.11472222,
	 * "LON": 12.14111111,
	 * "LOCALITA": "PONTILE DI ATTRACCO",
	 * "COMUNE": "CASTIGLIONE DEL LAGO",
	 * "PROVINCIA": "PG"
	 * }
	 */

	JSONObject stationObj = retrieveStationInfo(originalMD);

	/**
	 * SENSOR INFO
	 * {
	 * "_id": 126,
	 * "ID_STAZIONE": "74200",
	 * "ID_SENSORE": "13207",
	 * "STRUMENTO": "Idrometro",
	 * "TIPO_STRUMENTO": "Idrometro",
	 * "UNITA_MISURA": "m",
	 * "STATO_SENSORE": 1
	 * }
	 */

	JSONObject sensorObj = retrieveSensorInfo(originalMD);

	String varId = retieveVariableInfo(originalMD);

	String timeType = retrieveTimeType(originalMD);
	
	String resourceIdentifier = retieveResourceIdentifier(originalMD);

	String tempExtenBegin = retrieveStartDate(originalMD);

	// bbox
	Double lat = null;
	Double lon = null;
	String stationName = null;
	String stationId = null;
	String stationCity = null;
	String stationProvince = null;
	String stationLocation = null;

	String sensorId = null;
	String sensorName = null;
	String sensorType = null;

	String stationAltitude = null;

	String uom = null;

	if (stationObj != null) {
	    lat = getDouble(stationObj, "LAT");
	    lon = getDouble(stationObj, "LON");
	    stationName = getString(stationObj, "NOME_STAZIONE");
	    stationId = getString(stationObj, "ID_STAZIONE");
	    stationCity = getString(stationObj, "COMUNE");
	    stationLocation = getString(stationObj, "LOCALITA");
	    stationProvince = getString(stationObj, "PROVINCIA");
	}

	if (sensorObj != null) {
	    sensorId = getString(sensorObj, "ID_SENSORE");
	    sensorName = getString(sensorObj, "STRUMENTO");
	    sensorType = getString(sensorObj, "TIPO_STRUMENTO");
	    uom = getString(sensorObj, "UNITA_MISURA");

	}

	UMBRIA_Variable variable = UMBRIA_Variable.decode(varId);

	String label = variable.getLabel();
	String param = variable.getParamDescription();
	String paramDescription = variable.getParamDescription();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	// bbox
	if (lon != null && lat != null) {
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	if (timeType != null && !timeType.equals("NA")) {
	    coreMetadata.getMIMetadata().getDataIdentification()
		    .setCitationTitle(stationName + " - " + paramDescription + " (" + timeType + ")");
	} else {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " - " + paramDescription);
	}
	String title = null;
	if (stationName != null && !stationName.isEmpty()) {
	    // title
	    title = "Nome stazione: " + stationName;
	    title = (stationId != null && !stationId.isEmpty()) ? title + " - Codice stazione: " + stationId : title;
	    title = (stationCity != null && !stationCity.isEmpty()) ? title + " - Comune stazione: " + stationCity : title;
	    title = (stationProvince != null && !stationProvince.isEmpty()) ? title + " (" + stationProvince + ")" : title;

	}

	coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(title);

	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(title);

	// IDENTIFIER
	String id = null;
	try {
	    id = StringUtils.hashSHA1messageDigest(stationName + " - " + varId + " - " + stationId);
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
	publisherContact.setOrganisationName("Settore Idrologico e Geologico - Regione Umbria");
	publisherContact.setRoleCode("publisher");
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	// keywords
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationName);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(label);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(paramDescription);
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationId);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationProvince);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationCity);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("UMBRIA");

	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	// platform

	coreMetadata.addDistributionFormat("WaterML 1.1");

	coreMetadata.getMIMetadata().setLanguage("Italian");

	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(stationId);

	dataset.getExtensionHandler().setCountry("ITA");

	platform.setDescription(stationName);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	Keywords keyword = new Keywords();
	keyword.setTypeCode("platform");
	keyword.addKeyword(stationName);// or platformDescription
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	if (stationAltitude != null) {
	    try {
		VerticalExtent verticalExtent = new VerticalExtent();
		Double vertical = Double.parseDouble(stationAltitude);
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

	// temporal extent

	Map<UMBRIA_Variable, List<String>> postResult = HISCentralUmbriaConnector.postData(sensorObj, SORT_ORDER.DESC, resourceIdentifier);
	List<String> listRes = postResult.get(variable);
	String tempExtentEnd = listRes.get(1);
	if (tempExtenBegin != null && tempExtentEnd != null) {

	    coreMetadata.addTemporalExtent(tempExtenBegin, tempExtentEnd);

	}

	// setIndeterminatePosition(dataset);

	HISCentralUmbriaIdentifierMangler mangler = new HISCentralUmbriaIdentifierMangler();

	// site code network + site code: both needed for access
	mangler.setPlatformIdentifier(sensorId + ":" + stationId);

	// variable vocabulary + variable code: both needed for access
	mangler.setParameterIdentifier(varId);

	// String qualityCode = series.getQualityControlLevelID();
	//
	// // ARPA-ER hacks
	// if (hisServerEndpoint.contains("hydrolite.ddns.net") && qualityCode != null &&
	// qualityCode.equals("-9999")) {
	// qualityCode = "1";
	// }
	//
	// mangler.setQualityIdentifier(qualityCode);

	mangler.setResourceIdentifier(resourceIdentifier);
	
	mangler.setSourceIdentifier(id);

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(varId);

	InterpolationType interpolation = variable.getInterpolation();
	if (interpolation != null) {
	    dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	}

	// currently only daily observation are considered
	dataset.getExtensionHandler().setTimeResolution("1");
	dataset.getExtensionHandler().setTimeSupport("1");
	dataset.getExtensionHandler().setTimeUnits("d");

	String attributeTitle = label;
	coverageDescription.setAttributeTitle(attributeTitle);

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
	//
	String missingValue = "-9999";
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);
	//
	if (uom != null) {
	    dataset.getExtensionHandler().setAttributeUnits(uom);
	}
	//
	// String unitAbbreviation = getUnitAbbreviation(series);
	// dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unitAbbreviation);

	// as no description is given this field is calculated
	HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);

	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	String identifier = mangler.getMangling();

	coreMetadata.addDistributionOnlineResource(identifier, HISCentralUmbriaConnector.BASE_SQL_URL,
		CommonNameSpaceContext.HISCENTRAL_UMBRIA_NS_URI, "download");

	coreMetadata.getDataIdentification().setResourceIdentifier(identifier);

	//Online downloadOnline = coreMetadata.getOnline();

	String resourceId = generateCode(dataset, stationId + "-" + paramDescription);

	coreMetadata.getDataIdentification().setResourceIdentifier(resourceId);

	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceId);

	//
	// JSONObject originalObj = new JSONObject(originalMD);
	//
	// Map<String, Object> mappedJSON = originalObj.toMap();
	// // variable name
	// String variableCode = null;
	// JSONObject object = null;
	// for (Map.Entry<String, Object> newEntry : mappedJSON.entrySet()) {
	// variableCode = newEntry.getKey();
	// object = originalObj.getJSONObject(variableCode);
	// break;
	// }
	//
	// if (object != null) {
	//
	// JSONObject geometryObject = object.optJSONObject("geometry");
	//
	// JSONObject propertiesObject = object.optJSONObject("properties");
	//
	// String coordinates = getString(geometryObject, "coordinates");
	//
	// String stationCode = getString(propertiesObject, "Codice");
	//
	// // String stationName = getString(propertiesObject, "Nome");
	// //
	// // String stationCity = getString(propertiesObject, "Comune");
	// //
	// // String stationProvince = getString(propertiesObject, "Provincia");
	//
	// JSONObject infoObject = propertiesObject.optJSONObject("Consistenza");
	//
	// Map<String, Object> variables = infoObject.toMap();
	//
	// String startDate = null;
	// String endDate = null;
	// String dataUrl = null;
	// String unitName = null;
	// int dataLength = 0;
	//
	// for (Map.Entry<String, Object> entry : variables.entrySet()) {
	// String variableName = entry.getKey();
	// if (variableName.equals(variableCode)) {
	// logger.trace("SIR UMBRIA List Data finding for station code: {} and variable: {}", stationCode,
	// variableName);
	// ArrayList<List<String>> years = null;
	// Map<String, Object> yearAndDate = (Map<String, Object>) entry.getValue();
	// for (Map.Entry<String, Object> info : yearAndDate.entrySet()) {
	// // Anni-SorgenteDati
	// String key = info.getKey();
	// // String values = info.getValue();
	// if (key.toLowerCase().contains("anni")) {
	// years = (ArrayList<List<String>>) info.getValue();
	// } else if (key.toLowerCase().contains("sorgente")) {
	// dataUrl = (String) info.getValue();
	// }
	//
	// }
	//
	// // get data
	// Optional<String> dataResponse = downloader.downloadString(dataUrl);
	// if (dataResponse.isPresent()) {
	// JSONObject dataObject = new JSONObject(dataResponse.get());
	// JSONObject propertiesData = dataObject.optJSONObject("properties");
	// if (propertiesData != null) {
	// unitName = getString(propertiesData, "UnitaMisura");
	// JSONArray dataArray = propertiesData.optJSONArray("SerieDati");
	// if (dataArray.length() > 0) {
	// dataLength = dataArray.length();
	// startDate = getString(dataArray.getJSONObject(0), "Data");
	// startDate = startDate.contains(" ") ? startDate.replace(" ", "T") : startDate;
	// endDate = getString(dataArray.getJSONObject(dataLength - 1), "Data");
	// endDate = endDate.contains(" ") ? endDate.replace(" ", "T") : endDate;
	// } else if (years != null) {
	// List<String> listYears = years.get(0);
	// if (listYears.size() > 0) {
	// startDate = listYears.get(0);
	// endDate = listYears.get(listYears.size() - 1);
	// }
	//
	// }
	// }
	//
	// }
	//
	// }
	//
	// }
	//
	// GridSpatialRepresentation grid = new GridSpatialRepresentation();
	// grid.setNumberOfDimensions(1);
	// grid.setCellGeometryCode("point");
	// Dimension time = new Dimension();
	// time.setDimensionNameTypeCode("time");
	// Long valueCount = Long.valueOf(dataLength);
	// if (valueCount != null) {
	// time.setDimensionSize(new BigInteger("" + valueCount));
	// ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	// extensionHandler.setDataSize(valueCount);
	// }
	//
	// // if (series.isTimeScaleRegular()) {
	// // Number timeSpacing = series.getTimeScaleTimeSpacing();
	// // if (timeSpacing != null && timeSpacing.doubleValue() > Math.pow(10, -16)) {
	// // String resolutionUOM = series.getTimeScaleUnitName();
	// // time.setResolution(resolutionUOM, timeSpacing.doubleValue());
	// // }
	// // }
	// grid.addAxisDimension(time);
	// coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

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
