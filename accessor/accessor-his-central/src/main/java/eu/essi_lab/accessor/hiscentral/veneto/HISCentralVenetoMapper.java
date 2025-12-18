package eu.essi_lab.accessor.hiscentral.veneto;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.utils.DescriptionParsingResult;
import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * @author Roberto
 */
public class HISCentralVenetoMapper extends FileIdentifierMapper {

    private SimpleDateFormat iso8601Format;

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_VENETO_NS_URI;
    }

    public HISCentralVenetoMapper() {
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
    static OriginalMetadata create(JSONObject datasetInfo, JSONObject sensorInfo, String valore, int startYear, int endYear) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_VENETO_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("sensor-info", sensorInfo);
	jsonObject.put("value", valore);
	jsonObject.put("startDate", startYear);
	jsonObject.put("endDate", endYear);

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
    private JSONObject retrieveSensorInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("sensor-info");
    }

    private String retrieveValueType(OriginalMetadata metadata) {
	String ret = null;
	JSONObject jsonMetadata = new JSONObject(metadata.getMetadata());
	ret = jsonMetadata.optString("value");
	return ret;
    }

    private String retrieveStartYear(OriginalMetadata metadata) {
	JSONObject jsonMetadata = new JSONObject(metadata.getMetadata());
	int startYear = jsonMetadata.optInt("startDate");
	return String.valueOf(startYear);
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	JSONObject datasetInfo = retrieveDatasetInfo(originalMD);

	JSONObject sensorInfo = retrieveSensorInfo(originalMD);

	// {
	// "endDate": 2023,
	// "dataset-info": {
	// "altitude": 5,
	// "tipo": "TARIA2M",
	// "comune": "MOGLIANO VENETO",
	// "latitudine": 45.5807425,
	// "nome_stazione": "Mogliano Veneto",
	// "codice_stazione": 227,
	// "codseq": 300001606,
	// "provincia": "TREVISO",
	// "point": {
	// "type": "Point",
	// "coordinates": [
	// 12.30779083,
	// 45.5807425
	// ]
	// },
	// "longitudine": 12.30779083
	// },
	// "sensor-info": {
	// "dataora": "2023-07-31T00:00:00",
	// "tipo": "TARIA2M",
	// "valore": {
	// "MINIMO": 19.8,
	// "MEDIO": 24.9,
	// "MASSIMO": 30.0
	// },
	// "aggiornamento": "2023-08-25T18:11:32",
	// "unitnm": "°C",
	// "nome_stazione": "Mogliano Veneto",
	// "nome_sensore": "Temperatura aria a 2m",
	// "codice_stazione": 227
	// },
	// "value": "MEDIO",
	// "startDate": 2010
	// }

	// String resourceLocator = datasetInfo.getString("ResourceLocator");
	//
	// String keyword = datasetInfo.getString("Keyword");
	//
	//
	// String contactInfo = datasetInfo.optJSONObject("ResponsibleParty").getString("contactInfo");
	//
	// String resourceConstraints = datasetInfo.optJSONObject("ResourceConstraints").getString("useLimitation");
	// String resourceAbstract = datasetInfo.optString("ResourceAbstract");

	String stationName = datasetInfo.optString("nome_stazione");
	// String organisationName = datasetInfo.optString("proprietario");
	// String tempExtenBegin = datasetInfo.optString("data_inizio");
	// String tempExtenEnd = datasetInfo.optString("data_fine");

	BigDecimal pointLon = datasetInfo.optBigDecimal("longitudine", null);
	BigDecimal pointLat = datasetInfo.optBigDecimal("latitudine", null);
	Double altitude = datasetInfo.optDouble("altitude");

	String stationCode = datasetInfo.optString("codice_stazione");
	// String abbr = datasetInfo.optString("sigla");
	String timeSeriesId = datasetInfo.optString("codseq");

	// String timeSeriesId = sensorInfo.optString("id");
	String measureName = sensorInfo.optString("nome_sensore");

	String units = sensorInfo.optString("unitnm");

	String measureType = sensorInfo.optString("tipo");

	String endTime = sensorInfo.optString("dataora");

	String interpolation = retrieveValueType(originalMD);

	//
	// String stationName = sensorInfo.getString("name");
	//
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

	// String intendedObservationSpacing = sensorInfo.getString("intendedObservationSpacing");
	//
	// String aggregationTimePeriod = sensorInfo.get("aggregationTimePeriod").toString();

	//
	// intendedObservationSpacing = "P15M"
	//
	// 15 -> timeResolution
	// M -> timeUnits
	//
	// String timeUnits = intendedObservationSpacing.substring(1, intendedObservationSpacing.length() - 1);
	// String timeResolution = intendedObservationSpacing.substring(intendedObservationSpacing.length() - 1);
	//
	// dataset.getExtensionHandler().setTimeUnits(timeUnits);
	// dataset.getExtensionHandler().setTimeResolution(timeResolution);
	//
	// if (aggregationTimePeriod != null && aggregationTimePeriod.equals("0")) {
	// dataset.getExtensionHandler().setTimeSupport(timeUnits);
	// }

	//
	//
	//

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setLanguage("Italian");
	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.addDistributionFormat("WaterML 1.1");

	coreMetadata.getMIMetadata().getDataIdentification()
		.setCitationTitle(stationName + " - " + measureName + " (" + interpolation + ")");
	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(stationName + " - " + measureName + " (" + interpolation + ")");

	//
	// id
	//
	String resourceIdentifier = generateCode(dataset, timeSeriesId);
	coreMetadata.getMIMetadata().setFileIdentifier(resourceIdentifier);

	//
	// responsible party
	//
	ResponsibleParty publisherContact = new ResponsibleParty();

	publisherContact.setOrganisationName("ARPAV");
	publisherContact.setRoleCode("publisher");

	// Contact contact = new Contact();
	//
	// Address address = new Address();
	// address.addElectronicMailAddress(contactInfo);
	// contact.setAddress(address);
	//
	// publisherContact.setContactInfo(contact);

	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	//
	// keywords
	//
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(abbr);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(measureName);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("VENETO");

	Keywords kwd = new Keywords();
	kwd.setTypeCode("platform");
	kwd.addKeyword(stationName);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(kwd);

	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);
	//
	// bbox
	//
	if (pointLat != null && pointLon != null) {
	    coreMetadata.addBoundingBox(//
		    pointLat, //
		    pointLon, //
		    pointLat, //
		    pointLon);
	}

	// vertical extent
	coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(altitude, altitude);

	//
	// platform
	//

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(stationCode);

	dataset.getExtensionHandler().setCountry("ITA");

	platform.setDescription(stationName);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	//
	// temp extent
	//

	TemporalExtent temporalExtent = new TemporalExtent();
	String startYear = retrieveStartYear(originalMD);
	if (startYear != null && !startYear.isEmpty()) {
	    temporalExtent.setBeginPosition(startYear + "-01-01T00:00:00");
	} else {
	    temporalExtent.setBeginPosition("2010-01-01T00:00:00");
	}

	if (endTime != null && !endTime.isEmpty()) {
	    temporalExtent.setEndPosition(endTime);
	} else {
	    temporalExtent.setBeforeNowBeginPosition(FrameValue.P1M);
	}

	coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

	setIndeterminatePosition(dataset);

	Distribution distribution = coreMetadata.getMIMetadata().getDistribution();

	//
	// distribution info, information
	//

	// Online online = new Online();
	// online.setLinkage(resourceLocator);
	// online.setFunctionCode("information");
	// online.setName("Rete Meteo-Idro-Pluviometrica");
	//
	// distribution.addDistributionOnline(online);

	//
	// distribution info, download
	//
	Calendar c = Calendar.getInstance();
	String currentYear = String.valueOf(c.get(Calendar.YEAR));

	String linkage = HISCentralVenetoConnector.BASE_URL + HISCentralVenetoConnector.DATA_URL + "?anno=" + currentYear + "&codseq="
		+ timeSeriesId;

	Online online = new Online();
	online.setLinkage(linkage);
	online.setFunctionCode("download");
	online.setName(stationName + " - " + measureName + " (" + interpolation + ")");
	online.setIdentifier(resourceIdentifier);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_VENETO_NS_URI);

	distribution.addDistributionOnline(online);

	//
	// coverage description
	//

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(measureType);

	DescriptionParsingResult parsedDescription = HISCentralUtils.parseDescription(measureName);
	if (parsedDescription != null) {
	    measureName = parsedDescription.getRest();
	    coverageDescription.setAttributeDescription(measureName);
	} else {
	    HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);
	}
	if (measureName.startsWith("Portata ")) {
	    measureName = "Portata";
	}
	if (measureName.startsWith("Livello idrometrico ")) {
	    measureName = "Livello idrometrico";
	}

	coverageDescription.setAttributeTitle(measureName);

	String missingValue = "-9999";
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);
	if (units != null) {
	    dataset.getExtensionHandler().setAttributeUnits(units);
	}
	// as no description is given this field is calculated

	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

    }

}
