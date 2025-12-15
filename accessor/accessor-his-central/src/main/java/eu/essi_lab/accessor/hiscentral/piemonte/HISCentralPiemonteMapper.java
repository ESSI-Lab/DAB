package eu.essi_lab.accessor.hiscentral.piemonte;

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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.piemonte.HISCentralPiemonteConnector.PIEMONTE_Variable;
import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
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
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author Roberto
 */
public class HISCentralPiemonteMapper extends FileIdentifierMapper {

    public static final String MISSING_VALUE = "-9999";
    
    public static final String ORGANIZATION = "Dipartimento Rischi Naturali e Ambientali";

    private SimpleDateFormat iso8601Format;

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_PIEMONTE_NS_URI;
    }

    public HISCentralPiemonteMapper() {
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
    static OriginalMetadata create(JSONObject datasetInfo, String variableType, String variableName) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_PIEMONTE_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	// jsonObject.put("sensor-info", sensorInfo);
	jsonObject.put("var-type", variableType);
	jsonObject.put("var-name", variableName);

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
    private String retrieveVariableInfo(OriginalMetadata metadata) {
	// JSONObject sensorObj = new JSONObject(metadata.getMetadata()).optJSONObject("sensor-info")
	return new JSONObject(metadata.getMetadata()).optString("var-name");
    }

    /**
     * @param metadata
     * @return
     */
    private String retrieveVarInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optString("var-type");
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	/**
	 * DATASET INFO
	 * "url": "https://utility.arpa.piemonte.it/meteoidro/stazione_meteorologica/PIE-001003-900-1993-07-22/",
	 * "codice_istat_comune": "001003",
	 * "progr_punto_com": 900,
	 * "codice_stazione": "250 ",
	 * "denominazione": "ALA DI STURA ",
	 * "indirizzo_localita": "VIVAIO FORESTALE LA FABBRICA",
	 * "nazione": "ITALIA",
	 * "longitudine_e_wgs84_d": 7.3104067,
	 * "latitudine_n_wgs84_d": 45.312405,
	 * "quota_stazione": 1006,
	 * "esposizione": "NW",
	 * "note": null,
	 * "tipo_staz": "HPT ",
	 * "data_inizio": "1993-07-22",
	 * "data_fine": null,
	 * "sigla_prov": "TO",
	 * "comune": "ALA DI STURA",
	 * "fk_id_punto_misura_meteo":
	 * "https://utility.arpa.piemonte.it/meteoidro/punti_misura_meteo/PIE-001003-900/"
	 **/
	/**
	 * SENSOR INFO
	 * "url": "https://utility.arpa.piemonte.it/meteoidro/sensore_meteo/PIE-001003-900-PLUV-2004-02-25/",
	 * "codice_istat_comune": "001003",
	 * "progr_punto_com": 900,
	 * "id_parametro": "PLUV",
	 * "data_inizio": "1993-07-22",
	 * "data_fine": null,
	 * "quota_da_pc": null,
	 * "altezza_supporto": null,
	 * "note": "Pluviometro riscaldato",
	 * "fk_id_stazione_meteorologica":
	 * "https://utility.arpa.piemonte.it/meteoidro/stazione_meteorologica/PIE-001003-900-1993-07-22/"
	 **/

	try {

	    JSONObject datasetInfo = retrieveDatasetInfo(originalMD);

	    String variableName = retrieveVariableInfo(originalMD);

	    String varType = retrieveVarInfo(originalMD);
	    String puntoMisuraUrl = datasetInfo.optString(varType);

	    String stationName = datasetInfo.getString("denominazione").replaceAll("\\s+$", "");
	    Double lat = datasetInfo.optDouble("latitudine_n_wgs84_d");
	    Double lon = datasetInfo.optDouble("longitudine_e_wgs84_d");
	    Double alt = datasetInfo.optDouble("quota_stazione");
	    String stationType = datasetInfo.optString("tipo_staz").replaceAll("\\s+$", "");
	    String stationCode = datasetInfo.optString("codice_stazione").replaceAll("\\s+$", "");
	    String startTime = datasetInfo.optString("data_inizio");
	    String endTime = datasetInfo.optString("data_fine");
	    String comune = datasetInfo.optString("comune");
	    String siglaProv = datasetInfo.optString("sigla_prov");
	    // meteo and hydro case
	    String parameterName = null;
	    String paramCode = null;
	    String paramId = null;
	    String sensorUrl = null;
	    String stationUrl = datasetInfo.optString("url");
	    String getDataParam = null;

	    String uom = null;

	    String getVariableField = "";
	    JSONArray variables = null;

	    // meteo case
	    PIEMONTE_Variable m = PIEMONTE_Variable.decode(variableName);
	    if (m != null) {
		paramId = m.name();//
		parameterName = m.getLabel();
		paramCode = m.getParam();
		uom = m.getUnits();
		// parameterName = sensorInfo.optString("note");
		// sensorUrl = sensorInfo.optString("url");
		// getDataParam = puntoMisuraUrl.contains("_meteo") ? "dati_giornalieri_meteo" :
		// "dati_giornalieri_idro";
		getDataParam = m.getStationType().getDataParameter();
		getVariableField = m.getStationType().getVariableField();
		if (getVariableField != null) {
		    variables = datasetInfo.optJSONArray(getVariableField);
		    sensorUrl = getSensorUrl(variables, paramId);
		} else {
		    sensorUrl = datasetInfo.optString("url");
		}

	    }

	    //
	    //
	    //

	    // String timeSeriesId = sensorInfo.get("timeSeriesId").toString();
	    //
	    // String tempExtenBegin = sensorInfo.getString("temporalExtent");
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
	    //
	    // String uom = sensorInfo.getJSONObject("observedProperty").getString("uom");
	    // String basePhenomenon = sensorInfo.getJSONObject("observedProperty").getString("basePhenomenon");
	    //
	    // String pointLon =
	    // sensorInfo.getJSONObject("spatialSamplingFeature").getJSONArray("Point").get(0).toString();
	    // String pointLat =
	    // sensorInfo.getJSONObject("spatialSamplingFeature").getJSONArray("Point").get(1).toString();
	    //
	    // String intendedObservationSpacing = sensorInfo.getString("intendedObservationSpacing");
	    //
	    // String aggregationTimePeriod = sensorInfo.get("aggregationTimePeriod").toString();
	    //
	    // //
	    // // intendedObservationSpacing = "P15M"
	    // //
	    // // 15 -> timeResolution
	    // // M -> timeUnits
	    // //
	    // String timeResolution = intendedObservationSpacing.substring(1, intendedObservationSpacing.length() - 1);
	    // String timeUnits = intendedObservationSpacing.substring(intendedObservationSpacing.length() - 1);
	    //
	    // dataset.getExtensionHandler().setTimeUnits(timeUnits);
	    // dataset.getExtensionHandler().setTimeResolution(timeResolution);
	    //
	    // if (aggregationTimePeriod != null && aggregationTimePeriod.equals("0")) {

	    // the datasets are available on a daily aggregated basis
	    dataset.getExtensionHandler().setTimeUnits("d");
	    dataset.getExtensionHandler().setTimeSupport("1");
	    dataset.getExtensionHandler().setTimeResolution("1");
	    // }

	    //
	    //
	    //

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    coreMetadata.getMIMetadata().setLanguage("Italian");
	    coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	    coreMetadata.addDistributionFormat("WaterML 1.1");
	    parameterName = parameterName.isEmpty() ? paramId : parameterName;
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " - " + paramCode);
	    String abstrakt = "";

	    if (stationName != null && !stationName.isEmpty()) {
		// title
		abstrakt = "Nome stazione: " + stationName;
		abstrakt = (stationCode != null && !stationCode.isEmpty()) ? abstrakt + " - Codice stazione: " + stationCode : abstrakt;
		abstrakt = (comune != null && !comune.isEmpty()) ? abstrakt + " - Comune stazione: " + comune : abstrakt;
		abstrakt = (siglaProv != null && !siglaProv.isEmpty()) ? abstrakt + " (" + siglaProv + ")" : abstrakt;
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().setAbstract(abstrakt);

	    // LegalConstraints legalConstraints = new LegalConstraints();
	    // legalConstraints.addUseLimitation(resourceConstraints);
	    // coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(legalConstraints);

	    //
	    // id
	    //
	    // coreMetadata.setIdentifier(timeSeriesId);
	    // coreMetadata.getMIMetadata().setFileIdentifier(timeSeriesId);

	    //
	    // responsible party
	    //
	     ResponsibleParty publisherContact = new ResponsibleParty();	  
	     publisherContact.setOrganisationName(ORGANIZATION);
	     publisherContact.setRoleCode("publisher");	      
	     coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	    //
	    // keywords
	    //
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationName);
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationType);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(parameterName);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("PIEMONTE");

	    // Keywords kwd = new Keywords();
	    // kwd.setTypeCode("platform");
	    // kwd.addKeyword(stationName);
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeywords(kwd);

	    //
	    // bbox
	    //
	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("EPSG:4326");
	    referenceSystem.setCodeSpace("EPSG");
	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	    if (lat != null && lon != null) {
		coreMetadata.addBoundingBox(//
			new BigDecimal(lat), //
			new BigDecimal(lon), //
			new BigDecimal(lat), //
			new BigDecimal(lon));
	    }

	    if (alt != null)
		coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(alt, alt);

	    //
	    // platform
	    //

	    MIPlatform platform = new MIPlatform();
	    try {
		if (stationCode != null && !stationCode.isEmpty()) {
		    platform.setMDIdentifierCode(stationCode);
		} else {
		    String hashIdFromTitle = StringUtils.hashSHA1messageDigest(stationName);
		    platform.setMDIdentifierCode(hashIdFromTitle);
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(this.getClass()).warn("Error reading key {}: ", stationName, e);
		// return null;
	    }

	    dataset.getExtensionHandler().setCountry("ITA");

	    platform.setDescription(stationName + " - " + stationType);

	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(stationName + " - " + stationType);
	    platform.setCitation(platformCitation);

	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    //
	    // temp extent
	    //

	    TemporalExtent temporalExtent = new TemporalExtent();
	    temporalExtent.setBeginPosition(startTime);
	    if (endTime != null && !endTime.contains("null") && !endTime.isEmpty()) {
		temporalExtent.setEndPosition(endTime);
	    } else {
		temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	    }

	    coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

	    setIndeterminatePosition(dataset);

	    Distribution distribution = coreMetadata.getMIMetadata().getDistribution();

	    //
	    // distribution info, information
	    //

	    // sensor URL
	    if (sensorUrl != null && !sensorUrl.isEmpty()) {
		Online sensorOnlineURL = new Online();
		sensorOnlineURL.setLinkage(sensorUrl);
		sensorOnlineURL.setFunctionCode("information");
		sensorOnlineURL.setName("Sensore Meteo-Idro-Pluviometrica - " + parameterName);
		sensorOnlineURL.setDescription("Sensore Rete Meteo-Idro-Pluviometrica - " + parameterName);

		distribution.addDistributionOnline(sensorOnlineURL);
	    }

	    // station url
	    if (stationUrl != null && !stationUrl.isEmpty()) {
		Online stationOnlineURL = new Online();
		stationOnlineURL.setLinkage(stationUrl);
		stationOnlineURL.setFunctionCode("information");
		stationOnlineURL.setName("Stazione Rete Meteo-Idro-Pluviometrica - " + stationName);
		stationOnlineURL.setDescription("Stazione Rete Meteo-Idro-Pluviometrica - " + stationName);

		distribution.addDistributionOnline(stationOnlineURL);
	    }
	    //
	    // distribution info, download
	    //

	    if (puntoMisuraUrl != null && !puntoMisuraUrl.isEmpty()) {

		if (puntoMisuraUrl.endsWith("?format=json")) {

		    puntoMisuraUrl = puntoMisuraUrl.replace("?format=json", "");
		}
		if (puntoMisuraUrl.endsWith("/")) {
		    puntoMisuraUrl = puntoMisuraUrl.substring(0, puntoMisuraUrl.length() - 1);
		}
		String[] splittedUrl = puntoMisuraUrl.split("/");
		String lastPath = splittedUrl[splittedUrl.length - 1];
		puntoMisuraUrl = puntoMisuraUrl.replace(lastPath, "");
		HISCentralPiemonteClient client = new HISCentralPiemonteClient(puntoMisuraUrl);
		lastPath = lastPath + "/?format=json";
		String resp = client.getData(lastPath);
		if (resp != null) {
		    JSONObject listObjects = new JSONObject(resp);
		    String onlineData = listObjects.optString(getDataParam);
		    String beginData = listObjects.optString("data_inizio_dati");
		    String endData = listObjects.optString("data_fine_dati");

		    if (beginData != null && !beginData.isEmpty()) {
			coreMetadata.getDataIdentification().getTemporalExtent().setBeginPosition(beginData);
		    }
		    if (endData != null && !endData.isEmpty()) {
			coreMetadata.getDataIdentification().getTemporalExtent().setEndPosition(endData);
		    }

		    onlineData = onlineData.contains("?format=json") ? onlineData.replace("?format=json", "") : onlineData;
		    Online o = new Online();
		    o.setLinkage(onlineData);
		    o.setFunctionCode("download");
		    o.setName(stationName + "_" + paramCode);
		    o.setProtocol(CommonNameSpaceContext.HISCENTRAL_PIEMONTE_NS_URI);
		    distribution.addDistributionOnline(o);

		}

	    }

	    // if (tempExtenBegin.contains("+")) {
	    //
	    // tempExtenBegin = tempExtenBegin.substring(0, tempExtenBegin.indexOf("+"));
	    // }

	    // TODO: build download links

	    // String linkage = HISCentralPiemonteConnector.SENSOR_URL + "?id=" + timeSeriesId;
	    //
	    // online = new Online();
	    // online.setLinkage(linkage);
	    // online.setFunctionCode("download");
	    // online.setName(stationName + "_" + timeSeriesId);
	    // online.setProtocol(CommonNameSpaceContext.HISCENTRAL_PIEMONTE_NS_URI);
	    //
	    // distribution.addDistributionOnline(online);

	    //
	    // coverage description
	    //

	    CoverageDescription coverageDescription = new CoverageDescription();

	    coverageDescription.setAttributeIdentifier(paramId);
	    coverageDescription.setAttributeTitle(parameterName);

	    InterpolationType interpolation = m.getInterpolation();
	    if (interpolation != null) {
		dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	    }

	    dataset.getExtensionHandler().setAttributeMissingValue(MISSING_VALUE);

	    if (uom != null) {
		dataset.getExtensionHandler().setAttributeUnits(uom);
	    }

	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    // as no description is given this field is calculated
	    HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private String getSensorUrl(JSONArray variables, String paramId) {
	String sensorUrl = null;
	for (int k = 0; k < variables.length(); k++) {
	    String pId = variables.getJSONObject(k).optString("id_parametro");

	    if (pId.equals("PLUV") && PIEMONTE_Variable.PTOT.name().equals(paramId)) {
		sensorUrl = variables.getJSONObject(k).optString("url");
		break;
	    }
	    if (pId.equals("TERMA") && (PIEMONTE_Variable.TMAX.name().equals(paramId) || PIEMONTE_Variable.TMIN.name().equals(paramId)
		    || PIEMONTE_Variable.TMEDIA.name().equals(paramId))) {
		sensorUrl = variables.getJSONObject(k).optString("url");
		break;
	    }
	    if (pId.equals("IGRO") && (PIEMONTE_Variable.UMAX.name().equals(paramId) || PIEMONTE_Variable.UMIN.name().equals(paramId)
		    || PIEMONTE_Variable.UMMEDIA.name().equals(paramId))) {
		sensorUrl = variables.getJSONObject(k).optString("url");
		break;
	    }
	    if ((pId.equals("VELV") || paramId.equals("VELS"))
		    && (PIEMONTE_Variable.VMEDIA.name().equals(paramId) || PIEMONTE_Variable.VRAFFICA.name().equals(paramId))) {
		sensorUrl = variables.getJSONObject(k).optString("url");
		break;
	    }
	    if (pId.equals("RADD") && PIEMONTE_Variable.RADD.name().equals(paramId)) {
		sensorUrl = variables.getJSONObject(k).optString("url");
		break;
	    }
	    if (pId.equals("IDRO") && PIEMONTE_Variable.IDRO.name().equals(paramId)) {
		sensorUrl = variables.getJSONObject(k).optString("url");
		break;
	    }
	    if (pId.equals("PORTATA") && PIEMONTE_Variable.PORTATA.name().equals(paramId)) {
		sensorUrl = variables.getJSONObject(k).optString("url");
		break;
	    }
	}
	// variables.getJSONObject(0).optString("url");
	return sensorUrl;
    }

    public static void main(String[] args) {
	String puntoMisuraUrl = "https://utility.arpa.piemonte.it/meteoidro/punti_misura_meteo/PIE-001003-900/";
	if (puntoMisuraUrl.endsWith("/")) {
	    puntoMisuraUrl = puntoMisuraUrl.substring(0, puntoMisuraUrl.length() - 1);
	}
	String[] splittedUrl = puntoMisuraUrl.split("/");
	puntoMisuraUrl = puntoMisuraUrl.replace(splittedUrl[splittedUrl.length - 1], "");
	System.out.println(puntoMisuraUrl);
    }

}
