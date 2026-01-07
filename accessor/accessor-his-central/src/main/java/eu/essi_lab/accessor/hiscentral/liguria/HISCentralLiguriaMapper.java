package eu.essi_lab.accessor.hiscentral.liguria;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
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
 * @author Fabrizio
 */
public class HISCentralLiguriaMapper extends FileIdentifierMapper {

    private SimpleDateFormat iso8601Format;

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_LIGURIA_NS_URI;
    }

    public HISCentralLiguriaMapper() {
	this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601Format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static void setIndeterminatePosition(GSResource gsResource) {
	setIndeterminatePosition(gsResource, TimeUnit.DAYS.toMillis(30));
    }

    /**
     * @param datasetInfo
     * @param sensorInfo
     * @return
     */
    static OriginalMetadata create(String variable, String startTime, String linkage, JSONObject sensorInfo, JSONArray descritpionVarObj) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_LIGURIA_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("variable", variable);
	jsonObject.put("startTime", startTime);
	jsonObject.put("linkage", linkage);
	jsonObject.put("sensor-info", sensorInfo);
	jsonObject.put("var-info", descritpionVarObj);

	originalMetadata.setMetadata(jsonObject.toString(4));

	return originalMetadata;
    }

    static String getDate(Date d) {

	Date dateBefore = new Date(d.getTime() - 30 * 24 * 3600 * 1000l); // Subtract n days
	String isotime = ISO8601DateTimeUtils.getISO8601Date(dateBefore);
	String date = isotime.replace("-", "") + "0000";
	return date;
    }

    /**
     * @param metadata
     * @return
     */
    private String retrieveLinkage(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optString("linkage");
    }

    /**
     * @param startTime
     * @return
     */
    private String retrieveStartTime(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optString("startTime");
    }

    /**
     * @param metadata
     * @return
     */
    private JSONObject retrieveSensorInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optJSONObject("sensor-info");
    }

    private String retieveVariableInfo(OriginalMetadata metadata) {
	return new JSONObject(metadata.getMetadata()).optString("variable");
    }

    private JSONArray retrieveVariableDescription(OriginalMetadata metadata) {
	return new JSONObject(metadata.getMetadata()).optJSONArray("var-info");
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	String varId = retieveVariableInfo(originalMD);

	String link = retrieveLinkage(originalMD);

	// JSONObject datasetInfo = retrieveDatasetInfo(originalMD);

	JSONObject sensorInfo = retrieveSensorInfo(originalMD);

	JSONArray varDescription = retrieveVariableDescription(originalMD);

	String varName = null;
	String unit = null;
	String interpolationType = null;
	String intervalTime = null;

	if (varDescription != null && !varDescription.isEmpty()) {
	    // JSONArray dataItems = varDescription.getJSONArray("items");
	    for (Object arr : varDescription) {

		/**
		 * {
		 * "field_name": "TEMPM",
		 * "measure": "Air temperature (near surface)",
		 * "unit": "°C/10",
		 * "type": "AVERAGE",
		 * "obs_interval": "PT1H"
		 * }
		 */
		JSONObject varDescr = (JSONObject) arr;
		String vName = varDescr.optString("FIELD_NAME");
		if (vName.toLowerCase().equals(varId.toLowerCase())) {
		    // this is the matching case
		    varName = varDescr.optString("MEASURE");
		    unit = varDescr.optString("UNIT");
		    interpolationType = varDescr.optString("TYPE");
		    intervalTime = varDescr.optString("OBS_INTERVAL");
		    break;
		}

	    }

	}

	//
	//
	//

	// String resourceLocator = datasetInfo.getString("ResourceLocator");

	// String keyword = datasetInfo.getString("Keyword");

	// String organisationName = datasetInfo.getJSONObject("ResponsibleParty").getString("organisationName");
	// String contactInfo = datasetInfo.getJSONObject("ResponsibleParty").getString("contactInfo");

	// String resourceConstraints = datasetInfo.getJSONObject("ResourceConstraints").getString("useLimitation");
	// String resourceTitle = datasetInfo.getString("ResourceTitle");
	// String resourceAbstract = datasetInfo.getString("ResourceAbstract");

	//
	//
	//

	// String timeSeriesId = sensorInfo.get("timeSeriesId").toString();

	String tempExtenBegin = retrieveStartTime(originalMD);

	String stationName = sensorInfo.optString("NAME");

	String stationCode = sensorInfo.optString("CODE");

	double elevation = sensorInfo.optDouble("ELEV");

	String province = sensorInfo.optString("PROV");

	String country = sensorInfo.optString("MUNI");

	String region = sensorInfo.optString("REGION");

	String nation = sensorInfo.optString("NATION");

	// proprietario stazione
	String owner = sensorInfo.optString("OWNER");
	// manutentore stazione
	String admin = sensorInfo.optString("ADMIN");

	// String statisticalFunction = "";
	// if (sensorInfo.getJSONObject("observedProperty").has("statisticalFunction")) {
	//
	// statisticalFunction = sensorInfo.getJSONObject("observedProperty").getString("statisticalFunction");
	//
	// if (statisticalFunction.equals("sum")) {
	// dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.TOTAL);
	// } else {
	// dataset.getExtensionHandler().setTimeInterpolation(statisticalFunction);
	// }
	// }

	// String uom = sensorInfo.getJSONObject("observedProperty").getString("uom");
	// String basePhenomenon = sensorInfo.getJSONObject("observedProperty").getString("basePhenomenon");

	InterpolationType interpolation = null;

	switch (interpolationType.toLowerCase()) {
	case "average":
	    interpolation = InterpolationType.AVERAGE;
	    break;
	case "minimum":
	    interpolation = InterpolationType.MIN;
	    break;
	case "maximum":
	    interpolation = InterpolationType.MAX;
	    break;
	case "continuous":
	    interpolation = InterpolationType.CONTINUOUS;
	    break;
	default:
	    interpolation = InterpolationType.decode(interpolationType);
	    GSLoggerFactory.getLogger(getClass()).error("not known interpolation: {}", interpolationType);
	    break;
	}

	dataset.getExtensionHandler().setTimeInterpolation(interpolation);

	//
	// intendedObservationSpacing = "PT1H"
	//
	// 15 -> timeResolution
	// M -> timeUnits

	if (intervalTime != null) {
	    String timeResolution = intervalTime.substring(2, intervalTime.length() - 1);
	    String timeUnits = intervalTime.substring(intervalTime.length() - 1);

	    dataset.getExtensionHandler().setTimeUnits(timeUnits);
	    dataset.getExtensionHandler().setTimeResolution(timeResolution);
	    dataset.getExtensionHandler().setTimeSupport(timeResolution);
	}

	// Double pointLon = sensorInfo.optDouble("LON");// sensorInfo.optString("lon");
	// Double pointLat = sensorInfo.optDouble("LAT");
	//

	BigDecimal SCALE = new BigDecimal("100000");

	BigDecimal pointLat = new BigDecimal(sensorInfo.get("LAT").toString()).divide(SCALE, 5, RoundingMode.UNNECESSARY);
	BigDecimal pointLon = new BigDecimal(sensorInfo.get("LON").toString()).divide(SCALE, 5, RoundingMode.UNNECESSARY);

	double div = 100000;

	// String intendedObservationSpacing = sensorInfo.getString("intendedObservationSpacing");

	// String aggregationTimePeriod = sensorInfo.get("aggregationTimePeriod").toString();

	//
	// intendedObservationSpacing = "P15M"
	//
	// 15 -> timeResolution
	// M -> timeUnits
	//
	// String timeResolution = intendedObservationSpacing.substring(1, intendedObservationSpacing.length() - 1);
	// String timeUnits = intendedObservationSpacing.substring(intendedObservationSpacing.length() - 1);

	// dataset.getExtensionHandler().setTimeUnits(timeUnits);
	// dataset.getExtensionHandler().setTimeResolution(timeResolution);

	// if (aggregationTimePeriod != null && aggregationTimePeriod.equals("0")) {
	// dataset.getExtensionHandler().setTimeSupport(timeResolution);
	// }

	//
	//
	//

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setLanguage("Italian");
	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.addDistributionFormat("WaterML 1.1");

	String abstrakt = "";

	if (varName.contains("(")) {
	    String splittedVar = varName.split("\\(")[0];
	    varName = splittedVar.substring(0, splittedVar.length() - 1);
	}

	coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " (" + stationCode + ") - " + varName);

	if (stationName != null && !stationName.isEmpty()) {
	    // title
	    abstrakt = "Nome stazione: " + stationName;
	    abstrakt = (stationCode != null && !stationCode.isEmpty()) ? abstrakt + " - Codice stazione: " + stationCode : abstrakt;
	    abstrakt = (country != null && !country.isEmpty()) ? abstrakt + " - Comune stazione: " + country : abstrakt;
	    abstrakt = (province != null && !province.isEmpty()) ? abstrakt + " (" + province + ")" : abstrakt;

	}

	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(abstrakt);

	// LegalConstraints legalConstraints = new LegalConstraints();
	// legalConstraints.addUseLimitation(resourceConstraints);
	// coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(legalConstraints);

	//
	// id
	//
	String resourceIdentifier = generateCode(dataset, stationCode + "-" + varName);

	coreMetadata.getMIMetadata().setFileIdentifier(resourceIdentifier);

	//
	// responsible party
	//
	ResponsibleParty publisherContact = new ResponsibleParty();

	publisherContact.setOrganisationName(owner);
	publisherContact.setRoleCode("owner");

	Contact contact = new Contact();

	// Address address = new Address();
	// address.addElectronicMailAddress(contactInfo);
	// contact.setAddress(address);

	publisherContact.setContactInfo(contact);

	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	ResponsibleParty adminContact = new ResponsibleParty();

	adminContact.setOrganisationName(admin);
	adminContact.setRoleCode("admin");

	Contact adminC = new Contact();

	// Address address = new Address();
	// address.addElectronicMailAddress(contactInfo);
	// contact.setAddress(address);

	adminContact.setContactInfo(adminC);

	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(adminContact);
	//
	// ResponsibleParty publisherContact = new ResponsibleParty();
	//
	// publisherContact.setOrganisationName("ARPAL");
	// publisherContact.setRoleCode("publisher");
	//
	// Contact contact = new Contact();
	//
	// // Address address = new Address();
	// // address.addElectronicMailAddress(contactInfo);
	// // contact.setAddress(address);
	//
	// publisherContact.setContactInfo(contact);
	//
	// coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	//
	// keywords
	//
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(country);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(province);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(varId);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(varName);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("LIGURIA");

	Keywords kwd = new Keywords();
	kwd.setTypeCode("platform");
	kwd.addKeyword(stationName);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(kwd);

	//
	// bbox
	//
	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	if (pointLat != null && pointLon != null) {
	    coreMetadata.addBoundingBox(//
		    pointLat, //
		    pointLon, //
		    pointLat, //
		    pointLon);
	}

	coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(elevation, elevation);

	//
	// platform
	//

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(stationCode);

	dataset.getExtensionHandler().setCountry("ITA");

	platform.setDescription(stationName + " (" + stationCode + ")");

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	//
	// temp extent
	//

	TemporalExtent temporalExtent = new TemporalExtent();
	if (tempExtenBegin != null && !tempExtenBegin.isEmpty()) {
	    String beginPosition = transformDate(tempExtenBegin);
	    temporalExtent.setBeginPosition(beginPosition);

	}

	temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);

	coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

	setIndeterminatePosition(dataset);

	Distribution distribution = coreMetadata.getMIMetadata().getDistribution();

	//
	// distribution info, information
	//

	Online online = new Online();
	// online.setLinkage(link);
	// online.setFunctionCode("information");
	// online.setName("Rete Meteo-Idro-Pluviometrica");
	//
	// distribution.addDistributionOnline(online);

	//
	// distribution info, download
	//

	// if (tempExtenBegin.contains("+")) {
	//
	// tempExtenBegin = tempExtenBegin.substring(0, tempExtenBegin.indexOf("+"));
	// }
	// Date d = new Date();
	String linkage = HISCentralLiguriaConnector.BASE_URL + HISCentralLiguriaConnector.DATI_URL;

	online = new Online();
	online.setLinkage(linkage);
	online.setFunctionCode("download");
	online.setName(stationName + "_" + stationCode + "_" + varId);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_LIGURIA_NS_URI);

	distribution.addDistributionOnline(online);

	//
	// coverage description
	//

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(varId);
	if (varName.equals("Mean river discharge")) {
	    varName = "river discharge";
	}
	coverageDescription.setAttributeTitle(varName);

	String missingValue = "-9999";
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	if (unit != null) {

	    String[] splittedUnit = unit.split("/");
	    if (splittedUnit != null) {

		String measureUnits;
		if (splittedUnit.length > 2) {
		    StringBuilder builder = new StringBuilder();
		    for (int i = 0; i < splittedUnit.length - 1; i++) {
			builder.append(splittedUnit[i]);
			builder.append("/");
		    }
		    measureUnits = builder.deleteCharAt(builder.length() - 1).toString();
		} else {
		    measureUnits = splittedUnit[0];
		}
		dataset.getExtensionHandler().setAttributeUnits(measureUnits);
	    }
	}

	// if (uom != null) {
	// dataset.getExtensionHandler().setAttributeUnits(uom);
	// }

	// String units = uom != null ? " Units: " + uom : "";
	//

	// as no description is given this field is calculated
	HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);

	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }

    /**
     * @param timePosition
     * @return
     */
    private String transformDate(String timePosition) {

	String res = null;
	Optional<Date> date;

	date = ISO8601DateTimeUtils.parseNotStandard2ToDate(timePosition);

	if (date.isPresent()) {
	    res = ISO8601DateTimeUtils.getISO8601DateTime(date.get());
	}
	return res;
    }

}
