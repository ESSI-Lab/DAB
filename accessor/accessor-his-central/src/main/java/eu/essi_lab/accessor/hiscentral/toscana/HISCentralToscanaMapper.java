package eu.essi_lab.accessor.hiscentral.toscana;

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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalCRS;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
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

public class HISCentralToscanaMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private Downloader downloader = new Downloader();

    private String PLATFORM_PREFIX = "HIS-CENTRAL-TOSCANA";

    private SimpleDateFormat iso8601WMLFormat;

    // private String WMS_URL =
    // "https://geoserver.ifremer.fr/geomesa/wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&STYLES&LAYERS=argo_station&exceptions=application%2Fvnd.ogc.se_inimage&CQL_FILTER=platform_code%3D%27ARGO_PLACEHOLDER%27&CRS=EPSG%3A4326&WIDTH=768&HEIGHT=384&BBOX=-90,-180,90,180";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_TOSCANA_NS_URI;
    }

    public HISCentralToscanaMapper() {
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

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	String originalMetadata = originalMD.getMetadata();

	JSONObject originalObj = new JSONObject(originalMetadata);

	Map<String, Object> mappedJSON = originalObj.toMap();
	// variable name
	String variableCode = null;
	JSONObject object = null;
	for (Map.Entry<String, Object> newEntry : mappedJSON.entrySet()) {
	    variableCode = newEntry.getKey();
	    object = originalObj.getJSONObject(variableCode);
	    break;
	}

	if (object != null) {

	    JSONObject geometryObject = object.optJSONObject("geometry");

	    JSONObject propertiesObject = object.optJSONObject("properties");

	    String coordinates = getString(geometryObject, "coordinates");

	    String stationCode = getString(propertiesObject, "Codice");

	    String stationName = getString(propertiesObject, "Nome");

	    String stationCity = getString(propertiesObject, "Comune");

	    String stationProvince = getString(propertiesObject, "Provincia");

	    String stationAltitude = getString(propertiesObject, "Quota mslm");

	    JSONObject infoObject = propertiesObject.optJSONObject("Consistenza");

	    Map<String, Object> variables = infoObject.toMap();

	    String startDate = null;
	    String endDate = null;
	    String dataUrl = null;
	    String unitName = null;
	    int dataLength = 0;
	    String title = null;

	    for (Map.Entry<String, Object> entry : variables.entrySet()) {
		String variableName = entry.getKey();
		if (variableName.equals(variableCode)) {
		    logger.trace("SIR TOSCANA List Data finding for station code: {} and variable: {}", stationCode, variableName);
		    ArrayList<List<String>> years = null;
		    Map<String, Object> yearAndDate = (Map<String, Object>) entry.getValue();
		    for (Map.Entry<String, Object> info : yearAndDate.entrySet()) {
			// Anni-SorgenteDati
			String key = info.getKey();
			// String values = info.getValue();
			if (key.toLowerCase().contains("anni")) {
			    years = (ArrayList<List<String>>) info.getValue();
			} else if (key.toLowerCase().contains("sorgente")) {
			    dataUrl = (String) info.getValue();
			}

		    }

		    // get data
		    Optional<String> dataResponse = downloader.downloadOptionalString(dataUrl);
		    if (dataResponse.isPresent()) {
			JSONObject dataObject = new JSONObject(dataResponse.get());
			JSONObject propertiesData = dataObject.optJSONObject("properties");
			if (propertiesData != null) {
			    unitName = getString(propertiesData, "UnitaMisura");
			    JSONArray dataArray = propertiesData.optJSONArray("SerieDati");
			    if (dataArray.length() > 0) {
				dataLength = dataArray.length();
				startDate = getString(dataArray.getJSONObject(0), "Data");
				startDate = startDate.contains(" ") ? startDate.replace(" ", "T") : startDate;
				endDate = getString(dataArray.getJSONObject(dataLength - 1), "Data");
				endDate = endDate.contains(" ") ? endDate.replace(" ", "T") : endDate;
			    } else if (years != null) {
				List<String> listYears = years.get(0);
				if (listYears.size() > 0) {
				    startDate = listYears.get(0);
				    endDate = listYears.get(listYears.size() - 1);
				}

			    }
			}

		    }

		}

	    }

	    // bbox
	    Double lat = null;
	    Double lon = null;
	    coordinates = coordinates.replace("[", "").replace("]", "");
	    String[] splittedCoord = coordinates.split(",");
	    if (splittedCoord.length > 1) {
		lon = Double.valueOf(splittedCoord[0]);
		lat = Double.valueOf(splittedCoord[1]);
	    }

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    // IDENTIFIER
	    String id = null;
	    try {
		id = StringUtils.hashSHA1messageDigest(stationName + " - " + variableCode + " - " + stationCode);
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
	    publisherContact.setOrganisationName("Settore Idrologico e Geologico - Regione Toscana");
	    publisherContact.setRoleCode("publisher");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	    // keywords
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationName);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationCode);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationProvince);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationCity);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("TOSCANA");

	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("EPSG:4326");
	    referenceSystem.setCodeSpace("EPSG");
	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	    // bbox    
	    if (lat != null && lon != null) {
		coreMetadata.addBoundingBox(//
			new BigDecimal(lat), //
			new BigDecimal(lon), //
			new BigDecimal(lat), //
			new BigDecimal(lon));
	    }

	    // platform

	    String platformIdentifier = PLATFORM_PREFIX + stationCode + ":" + stationName;

	    String parameterIdentifier = PLATFORM_PREFIX + ":" + variableCode;

	    coreMetadata.addDistributionFormat("WaterML 1.1");

	    coreMetadata.getMIMetadata().setLanguage("English");

	    coreMetadata.getMIMetadata().setCharacterSetCode("utf8");

	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    MIPlatform platform = new MIPlatform();

	    platform.setMDIdentifierCode(stationCode);

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

	    if (startDate != null && endDate != null) {
		String beginPosition = normalizeUTCPosition(startDate);
		String endPosition = normalizeUTCPosition(endDate);

		try {
		    iso8601WMLFormat.parse(beginPosition);
		    iso8601WMLFormat.parse(endPosition);
		    coreMetadata.addTemporalExtent(beginPosition, endPosition);
		    setIndeterminatePosition(dataset);

		} catch (ParseException e) {
		    String error = "Unable to parse dates: " + beginPosition + " - " + endPosition;
		    logger.error(error);
		    TemporalExtent temporalExtent = new TemporalExtent();
		    if (!startDate.isEmpty()) {
			startDate = startDate.replace("Z", "");
			// Create a LocalDate for the first day of the year
			LocalDate date = LocalDate.of(Integer.parseInt(startDate), 1, 1);
			String isoDateTime = date.atStartOfDay().toString() + "Z";
			temporalExtent.setBeginPosition(isoDateTime);

		    }
		    if (!endDate.isEmpty()) {
			endDate = endDate.replace("Z", "");
			LocalDate date = LocalDate.of(Integer.parseInt(startDate), 1, 1);
			String isoDateTime = date.atStartOfDay().toString() + "Z";
			temporalExtent.setEndPosition(isoDateTime);
		    }
		    coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

		}
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " - " + variableCode);

	    if (stationName != null && !stationName.isEmpty()) {
		// title
		title = "Nome stazione: " + stationName;
		title = (stationCode != null && !stationCode.isEmpty()) ? title + " - Codice stazione: " + stationCode : title;
		title = (stationCity != null && !stationCity.isEmpty()) ? title + " - Comune stazione: " + stationCity : title;
		title = (stationProvince != null && !stationProvince.isEmpty()) ? title + " (" + stationProvince + ")" : title;

	    }

	    coreMetadata.getMIMetadata().getDataIdentification().setCitationAlternateTitle(title);

	    coreMetadata.getMIMetadata().getDataIdentification().setAbstract(title);

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

	    HISCentralToscanaIdentifierMangler mangler = new HISCentralToscanaIdentifierMangler();

	    // site code network + site code: both needed for access
	    mangler.setPlatformIdentifier(stationName + ":" + stationCode);

	    // variable vocabulary + variable code: both needed for access
	    mangler.setParameterIdentifier(variableCode);

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

	    coverageDescription.setAttributeIdentifier(variableCode);

	    InterpolationType interpolation = null;

	    if (variableCode.toLowerCase().contains("minima")) {
		interpolation = InterpolationType.MIN;
	    } else if (variableCode.toLowerCase().contains("aggregazione")) {
		interpolation = InterpolationType.TOTAL;
	    } else if (variableCode.toLowerCase().contains("medio")) {
		interpolation = InterpolationType.AVERAGE;
	    } else if (variableCode.toLowerCase().contains("instantaneo")) {
		interpolation = InterpolationType.CONTINUOUS;
	    } else if (variableCode.toLowerCase().contains("massima")) {
		interpolation = InterpolationType.MAX;
	    }
	    if (interpolation != null) {
		dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	    }
	    if (variableCode.toLowerCase().contains("giornalier")) {
		dataset.getExtensionHandler().setTimeResolution("1");
		dataset.getExtensionHandler().setTimeSupport("1");
		dataset.getExtensionHandler().setTimeUnits("d");
	    }
	    if (variableCode.toLowerCase().contains("a 24 ore")) {
		dataset.getExtensionHandler().setTimeResolution("1");
		dataset.getExtensionHandler().setTimeSupport("1");
		dataset.getExtensionHandler().setTimeUnits("d");
	    }
	    String attributeTitle = variableCode;
	    String generalCategory = (variableCode.contains(" ")) ? variableCode.split(" ")[0] : variableCode;
	    switch (generalCategory) {
	    case "TERMOMETRIA":
		if (unitName.equals("C")) {
		    attributeTitle = "Temperatura";
		}
		break;
	    case "PLUVIOMETRIA":
		if (unitName.equals("mm")) {
		    attributeTitle = "Precipitazione";
		}
		break;
	    case "FREATIMETRIA":
		if (unitName.equals("m da p.c.")) {
		    attributeTitle = "Livello";
		}
		break;
	    case "IDROMETRIA":
		if (unitName.equals("mc/s")) {
		    attributeTitle = "Portata";
		}
		if (unitName.equals("m szi")) {
		    attributeTitle = "Livello";
		}
		break;
	    default:
		break;
	    }
	    if (attributeTitle.contains("Velocita Raffica Giornaliera")) {
		attributeTitle = "Velocita Raffica";
	    }
	    if (attributeTitle.contains("Velocita Media Giornaliera")) {
		attributeTitle = "Velocita Vento";
	    }
	    if (attributeTitle.contains("Direzione Media Giornaliera")) {
		attributeTitle = "Direzione Vento";
	    }

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
	    String missingValue = "-9999.0";
	    dataset.getExtensionHandler().setAttributeMissingValue(missingValue);
	    //
	    if (unitName != null) {
		dataset.getExtensionHandler().setAttributeUnits(unitName);
	    }
	    //
	    // String unitAbbreviation = getUnitAbbreviation(series);
	    // dataset.getExtensionHandler().setAttributeUnitsAbbreviation(unitAbbreviation);

	    // as no description is given this field is calculated
	    HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);

	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    String identifier = mangler.getMangling();

	    coreMetadata.addDistributionOnlineResource(identifier, dataUrl, CommonNameSpaceContext.HISCENTRAL_TOSCANA_NS_URI, "download");

	    coreMetadata.getDataIdentification().setResourceIdentifier(identifier);

	    Online downloadOnline = coreMetadata.getOnline();

	    String resourceIdentifier = generateCode(dataset, stationCode + "-" + variableCode);

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
	String input = "2002Z";
	// Extract the year part from the string
	String yearPart = input.substring(0, 4);
	// Create a LocalDate for the first day of the year
	LocalDate date = LocalDate.of(Integer.parseInt(yearPart), 1, 1);
	String isoDateTime = date.atStartOfDay().toString() + "Z";
	System.out.println(isoDateTime);

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
