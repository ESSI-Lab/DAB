package eu.essi_lab.accessor.hiscentral.sardegna;

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

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.downloader.hiscentral.HISCentralSardegnaDownloader;
import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.model.resource.data.EPSGCRS;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;

/**
 * @author Roberto
 */
public class HISCentralSardegnaMapper extends FileIdentifierMapper {

    private SimpleDateFormat iso8601Format;

    private static final String HISCENTRAL_SARDEGNA_MAPPER_ERROR = "HISCENTRAL_SARDEGNA_MAPPER_ERROR";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_SARDEGNA_NS_URI;
    }

    public HISCentralSardegnaMapper() {
	// 2010-06-01T00:00:00
	// 2023-01-16T09:30:00+01:00
	this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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
    static OriginalMetadata create(JSONObject datasetInfo, HISCentralSardegnaVariable var, JSONObject metadataInfo) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_SARDEGNA_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("variable", var.getJson());
	jsonObject.put("metadata-info", metadataInfo);

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
    private JSONObject retrieveParameterInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("variable");
    }

    /**
     * @param metadata
     * @return
     */
    private JSONObject retrieveMetadataInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("metadata-info");
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	try {

	    JSONObject datasetInfo = retrieveDatasetInfo(originalMD);

	    //
	    // DATASET INFO
	    // "_id": "654cf975343203a2f5aabb7d",
	    // "COD_STAZ": "SS012S071",
	    // "NOME": "AGGIUS RC",
	    // "LOCALITA": "VIA PAOLI 13B",
	    // "DATA_INIZIO": "2019-09-30T22:00:00Z",
	    // "QUOTA": 509,
	    // "WGS84_UTM_32N_X": 505342,
	    // "WGS84_UTM_32N_Y": 4530845,
	    // "TIPO_RETE": "TR - Climatologica",
	    // "TCI_TERMO": "SI",
	    // "P1H_PLUVIO": "SI",
	    // "LIT_IDRO": "NO"

	    JSONObject variableObj = retrieveParameterInfo(originalMD);

	    JSONObject metadataInfo = retrieveMetadataInfo(originalMD);

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    // metadata info

	    // {
	    // "_id": "65dc48a4e0ba0dc2899e4b96",
	    // "Organizations": [
	    // {
	    // "Name": "ARPA Sardegna",
	    // "Email": [
	    // "info@arpa.sardegna.it",
	    // "servizioidrografico@arpa.sardegna.it",
	    // "dipartimento.imc@arpa.sardegna.it"
	    // ],
	    // "Roles": [
	    // "Publisher",
	    // "Distributor",
	    // "PointOfContact",
	    // "Author",
	    // "Owner",
	    // "Custodian"
	    // ]
	    // }
	    // ],
	    // "Coordinate reference system": {
	    // "PROJCS": "WGS 84 / UTM zone 32 N",
	    // "units": "meters",
	    // "EPSG": "32632"
	    // },
	    // "License": "Creative Commons Attribution 4.0",
	    // "Abstract": "I database ARPAS sono alimentati da varie tipologie di reti di stazioni in tempo reale: Rete
	    // Fiduciaria di Protezione Civile, di monitoraggio idro-termo-pluviometrico; Rete Unica Regionale, Rete
	    // Climatologica e Rete EX-SAR di monitoraggio meteorologico e pluvio-termometrico.",
	    // "Data": [
	    // {
	    // "WMO_observed_property": "http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/172",
	    // "WMO_name": "Stream level",
	    // "ARPAS_CODIFICA": "LIT",
	    // "Unita di misura": "m",
	    // "Tipo interpolazione": "Istantanea",
	    // "Risoluzione temporale (ISO8601)": "PT1H",
	    // "url_anagrafica":
	    // "https://eu-central-1.aws.data.mongodb-api.com/app/hiscentral-dqluv/endpoint/getLITStations",
	    // "Hydro-ontology": "https://hydro.geodab.eu/hydro-ontology/concept/11",
	    // "keyword": [
	    // {
	    // "label": "livello idrometrico",
	    // "uri": "http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/172"
	    // },
	    // {
	    // "label": "idrografia",
	    // "uri": "http://inspire.ec.europa.eu/theme/hy"
	    // }
	    // ]
	    // },
	    // {
	    // "WMO_observed_property": "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/224",
	    // "WMO_name": "Air temperature (at specified distance from reference surface)",
	    // "ARPAS_CODIFICA": "TCI",
	    // "Unita di misura": "°C",
	    // "Tipo interpolazione": "Istantanea",
	    // "Risoluzione temporale (ISO8601)": "PT1H",
	    // "url_anagrafica":
	    // "https://eu-central-1.aws.data.mongodb-api.com/app/hiscentral-dqluv/endpoint/getTCIStations",
	    // "Hydro-ontology": "https://hydro.geodab.eu/hydro-ontology/concept/49",
	    // "keyword": [
	    // {
	    // "label": "temperatura dell'aria",
	    // "uri": "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/224"
	    // },
	    // {
	    // "label": "condizioni atmosferiche",
	    // "uri": "http://inspire.ec.europa.eu/theme/ac"
	    // }
	    // ]
	    // },
	    // {
	    // "WMO_observed_property": "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/210",
	    // "WMO_name": "Amount of precipitation",
	    // "ARPAS_CODIFICA": "P1H",
	    // "Unita di misura": "mm",
	    // "Tipo interpolazione": "Cumulato intervallo precedente",
	    // "Risoluzione temporale (ISO8601)": "PT1H",
	    // "Durata aggregazione (ISO8601)": "PT1H",
	    // "url_anagrafica":
	    // "https://eu-central-1.aws.data.mongodb-api.com/app/hiscentral-dqluv/endpoint/getP1HStations",
	    // "Hydro-ontology": "https://hydro.geodab.eu/hydro-ontology/concept/65",
	    // "keyword": [
	    // {
	    // "label": "precipitazione",
	    // "uri": "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/210"
	    // },
	    // {
	    // "label": "condizioni atmosferiche",
	    // "uri": "http://inspire.ec.europa.eu/theme/ac"
	    // }
	    // ]
	    // }
	    // ]
	    // }

	    HISCentralSardegnaVariable var = new HISCentralSardegnaVariable(variableObj);

	    String parameterName = var.getVariableName();
	    String parameterURI = var.getVariableURI();
	    String paramCode = var.getId();
	    String uom = var.getVariableUnits();
	    String aggregationPeriodUnits = var.getAggregationPeriodUnits();
	    String interp = var.getInterpolation();
	    String varPath = null;

	    // getDataLITByStation
	    // getDataP1HByStations
	    // getDataTCIByStations

	    if (var != null) {
		if (paramCode.equals("TCI")) {
		    varPath = "getDataTCI";
		}
		if (paramCode.equals("P1H")) {
		    varPath = "getDataP1H";
		}
		if (paramCode.equals("LIT")) {
		    varPath = "getDataLIT";
		}
	    }

	    String resourceTitle = datasetInfo.optString("NOME");
	    String resourceAbstract = datasetInfo.optString("LOCALITA"); // always null
	    String id = datasetInfo.optString("_id");
	    String stationCode = datasetInfo.optString("COD_STAZ");

	    String stationType = datasetInfo.optString("TIPO_RETE");

	    BigDecimal utm32Est = datasetInfo.getBigDecimal("WGS84_UTM_32N_X");
	    BigDecimal utm32Nord = datasetInfo.getBigDecimal("WGS84_UTM_32N_Y");
	    BigDecimal alt = datasetInfo.optBigDecimal("QUOTA m slm", null);

	    // temporal
	    String tempExtentBegin = datasetInfo.optString("DATA_INIZIO_STAZIONE");
	    String tempExtentEnd = datasetInfo.optString("DATA_FINE");

	    /**
	     * Organizations
	     */
	    JSONArray organizations = metadataInfo.optJSONArray("Organizations");
	    String orgName = null;
	    List<String> emailList = new ArrayList<String>();
	    List<String> roleList = new ArrayList<String>();
	    if (organizations != null) {
		for (int j = 0; j < organizations.length(); j++) {
		    JSONObject org = organizations.optJSONObject(j);
		    if (org != null) {
			orgName = org.optString("Name");
			JSONArray emails = org.optJSONArray("Email");
			if (emails != null) {
			    for (int k = 0; k < emails.length(); k++) {
				String mail = emails.optString(k);
				if (mail != null)
				    emailList.add(mail);
			    }
			}
			JSONArray roles = org.optJSONArray("Roles");
			if (roles != null) {
			    for (int k = 0; k < roles.length(); k++) {
				String role = roles.optString(k);
				if (role != null)
				    roleList.add(role);
			    }
			}
		    }
		}
	    }

	    for (String role : roleList) {
		ResponsibleParty repsContact = new ResponsibleParty();
		Contact contact = new Contact();
		Address address = new Address();
		for (String mail : emailList) {
		    address.addElectronicMailAddress(mail);
		}
		contact.setAddress(address);
		repsContact.setContactInfo(contact);
		repsContact.setRoleCode(role);
		repsContact.setOrganisationName(orgName);
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(repsContact);
	    }

	    /**
	     * Coordinate reference system (CRS)
	     */

	    JSONObject crsObj = metadataInfo.optJSONObject("Coordinate reference system");
	    String epsg = null;
	    String projection = null;
	    String crsUnit = null;
	    if (crsObj != null) {
		projection = crsObj.optString("PROJCS");
		crsUnit = crsObj.optString("units");
		epsg = crsObj.optString("EPSG");
	    }

	    /**
	     * License
	     */
	    String license = metadataInfo.optString("License");

	    // legal constraints
	    //
	    //
	    if (license != null && !license.isEmpty()) {

		LegalConstraints access = new LegalConstraints();
		access.addAccessConstraintsCode("other");
		access.addUseLimitation(license);
		coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(access);

	    }
	    // if (legalLimitations != null && !legalLimitations.isEmpty()) {
	    //
	    // LegalConstraints rc = new LegalConstraints();
	    // rc.addUseConstraintsCode("other");
	    //
	    // String limitationsURL = null;
	    // if (legalLimitations.toLowerCase().contains("uri:")) {
	    // String[] splittedLegal = legalLimitations.toLowerCase().split("uri:");
	    // limitationsURL = splittedLegal[1].trim();
	    // }
	    // if (limitationsURL != null) {
	    //
	    // rc.getElementType().getOtherConstraints().add(ISOMetadata.createAnchorPropertyType(limitationsURL));
	    // } else {
	    // rc.addUseLimitation(limitationsURL);
	    // }
	    //
	    // coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(rc);
	    // }

	    /**
	     * Abstrakt
	     */
	    String abstrakt = metadataInfo.optString("Abstract");
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(resourceTitle + " - " + parameterName);
	    if (abstrakt != null && !abstrakt.isEmpty()) {
		coreMetadata.getMIMetadata().getDataIdentification().setAbstract(abstrakt);
	    } else {
		coreMetadata.getMIMetadata().getDataIdentification()
			.setAbstract(resourceTitle + " - " + resourceAbstract + " - " + parameterName);
	    }

	    /**
	     * Keywords
	     */

	    JSONArray keywordsArray = var.getKeywords();
	    Map<String, String> keywordMap = new HashMap<String, String>();
	    if (keywordsArray != null) {
		for (int k = 0; k < keywordsArray.length(); k++) {
		    JSONObject keyObj = keywordsArray.optJSONObject(k);
		    if (keyObj != null) {
			String keywordLabel = keyObj.optString("label");
			String keywordURI = keyObj.optString("uri");
			keywordMap.put(keywordLabel, keywordURI);
		    }

		}
	    }
	    Keywords key = new Keywords();
	    for (Map.Entry<String, String> entry : keywordMap.entrySet()) {
		key.addKeyword(entry.getKey());
		CharacterStringPropertyType value = ISOMetadata.createAnchorPropertyType(entry.getValue(),
			StringEscapeUtils.escapeXml11(entry.getKey()));
		List<CharacterStringPropertyType> list = new ArrayList<CharacterStringPropertyType>();
		list.add(value);
		key.getElementType().setKeyword(list);
		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(key);

	    }

	    Keywords keys = new Keywords();
	    List<String> keyList = Arrays.asList(stationType, parameterName, paramCode, stationCode, "SARDEGNA");
	    keys.addKeywords(keyList);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keys);
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationType);
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword(parameterName);
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword(paramCode);
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationCode);
	    // // coreMetadata.getMIMetadata().getDataIdentification().addKeyword(measureDescription);
	    //
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword("SARDEGNA");

	    Keywords kwd = new Keywords();
	    kwd.setTypeCode("platform");
	    kwd.addKeyword(resourceTitle);
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(kwd);

	    /**
	     * INTERPOLATION
	     */

	    if (interp.toLowerCase().contains("cumulato")) {
		dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.AVERAGE_PREC);
	    } else {
		dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.INSTANT_TOTAL);
	    }

	    // String uom = sensorInfo.getJSONObject("observedProperty").getString("uom");
	    // String basePhenomenon = sensorInfo.getJSONObject("observedProperty").getString("basePhenomenon");

	    // String intendedObservationSpacing = sensorInfo.getString("intendedObservationSpacing");
	    //
	    // String aggregationTimePeriod = sensorInfo.get("aggregationTimePeriod").toString();

	    //
	    // intendedObservationSpacing = "PT1H"
	    //
	    // 1 -> timeResolution
	    // H -> timeUnits
	    //
	    String timeResolution = aggregationPeriodUnits.substring(2, aggregationPeriodUnits.length() - 1);
	    String timeUnits = aggregationPeriodUnits.substring(aggregationPeriodUnits.length() - 1);

	    dataset.getExtensionHandler().setTimeUnits(timeUnits);
	    dataset.getExtensionHandler().setTimeResolution(timeResolution);
	    dataset.getExtensionHandler().setTimeSupport(timeResolution);

	    // dataset.getExtensionHandler().setTimeUnits("h");
	    // dataset.getExtensionHandler().setTimeSupport("1");
	    // dataset.getExtensionHandler().setTimeResolution("1");

	    coreMetadata.getMIMetadata().setLanguage("Italian");
	    coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	    coreMetadata.addDistributionFormat("WaterML 1.1");

	    //
	    // id
	    //

	    String resourceIdentifier = generateCode(dataset, stationCode + "-" + parameterName);

	    coreMetadata.getMIMetadata().setFileIdentifier(resourceIdentifier);

	    //
	    // topic category
	    //
	    // if (topicCataegory.toLowerCase().contains("meteo")) {
	    // MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.CLIMATOLOGY_METEOROLOGY_ATMOSPHERE;
	    // coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);
	    //
	    // }

	    //

	    //
	    // bbox
	    //
	    Double lat = null;
	    Double lon = null;
	    if (utm32Est != null && utm32Nord != null) {
		SimpleEntry<Double, Double> coordinates = new SimpleEntry<>(utm32Est.doubleValue(), utm32Nord.doubleValue());
		CRS sourceCRS = new EPSGCRS(7791);
		CRS targetCRS = CRS.EPSG_4326();
		SimpleEntry<Double, Double> latlon = CRSUtils.translatePoint(coordinates, sourceCRS, targetCRS);
		lat = latlon.getKey();
		lon = latlon.getValue();
		if (lat > 90 || lat < -90) {
		    String warn = "Invalid latitude for station: " + stationCode;
		    GSLoggerFactory.getLogger(getClass()).warn(warn);
		}
		if (lon > 180 || lon < -180) {
		    String warn = "Invalid longitude for station: " + stationCode;
		    GSLoggerFactory.getLogger(getClass()).warn(warn);
		}
	    }

	    // bbox
	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCodeSpace("EPSG");
	    if (epsg != null) {
		referenceSystem.setCode("EPSG:" + epsg);
	    } else {
		referenceSystem.setCode("EPSG:7791");
	    }
	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);
	    if (lat != null && lon != null) {
		coreMetadata.getMIMetadata().getDataIdentification().addGeographicBoundingBox(lat, lon, lat, lon);
	    }

	    // vertical extent
	    if (alt != null)
		coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(alt.doubleValue(), alt.doubleValue());

	    //
	    // platform
	    //

	    MIPlatform platform = new MIPlatform();

	    platform.setMDIdentifierCode(stationCode);

	    dataset.getExtensionHandler().setCountry("ITA");

	    platform.setDescription(resourceTitle);

	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(resourceTitle);
	    platform.setCitation(platformCitation);

	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    //
	    // temp extent
	    //

	    // data linkage (last 24 hours)

	    String linkage = HISCentralSardegnaConnector.BASE_URL.endsWith("/")
		    ? HISCentralSardegnaConnector.BASE_URL + varPath + "?cod_staz=" + stationCode
		    : HISCentralSardegnaConnector.BASE_URL + "/" + varPath + "?cod_staz=" + stationCode;

	    TemporalExtent temporalExtent = new TemporalExtent();
	    temporalExtent.setBeginPosition(tempExtentBegin);
	    // end time to be reviewed
	    if (tempExtentEnd == null || tempExtentEnd.isEmpty()) {
		String getDataUrl = linkage + "&data_iniziale="
			+ ISO8601DateTimeUtils.getISO8601Date(new Date(System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000L))
			+ "&data_finale=" + ISO8601DateTimeUtils.getISO8601Date();
		String date = getLastDate(getDataUrl);
		if (date != null && !date.isEmpty()) {
		    temporalExtent.setEndPosition(date);
		} else {
		    temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		}
	    } else {
		temporalExtent.setEndPosition(tempExtentEnd);
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

	    Online online = new Online();
	    online.setLinkage(linkage);
	    online.setFunctionCode("download");
	    online.setName(resourceTitle + "_" + stationCode + "_" + parameterName);
	    online.setIdentifier(resourceIdentifier);
	    online.setProtocol(CommonNameSpaceContext.HISCENTRAL_SARDEGNA_NS_URI);

	    distribution.addDistributionOnline(online);

	    //
	    // coverage description
	    //

	    CoverageDescription coverageDescription = new CoverageDescription();

	    coverageDescription.setAttributeIdentifier(paramCode);
	    coverageDescription.setAttributeTitle(parameterName);

	    dataset.getExtensionHandler().setAttributeMissingValue(HISCentralSardegnaDownloader.MISSING_VALUE);

	    if (uom != null) {
		dataset.getExtensionHandler().setAttributeUnits(uom);
	    }

	    // if (uriCode != null && !uriCode.isEmpty()) {
	    // dataset.getExtensionHandler().setObservedPropertyURI(uriCode);
	    // }

	    // as no description is given this field is calculated
	    HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);

	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private String getLastDate(String linkage) throws GSException {
	GSLoggerFactory.getLogger(getClass()).info("Getting data");

	String result = null;
	String token = null;
	String date = null;

	try {

	    if (HISCentralSardegnaConnector.API_KEY == null) {
		HISCentralSardegnaConnector.API_KEY = ConfigurationWrapper.getCredentialsSetting().getSardegnaApiKey().orElse(null);
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Getting " + linkage);

	    int timeout = 120;
	    int responseTimeout = 200;
	    InputStream stream = null;

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, timeout);
	    downloader.setResponseTimeout(TimeUnit.SECONDS, responseTimeout);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    linkage.trim(), //
		    HttpHeaderUtils.build("api-key", HISCentralSardegnaConnector.API_KEY));

	    stream = getStationResponse.body();

	    GSLoggerFactory.getLogger(getClass()).info("Got " + linkage);

	    int responseCode = getStationResponse.statusCode();
	    if (responseCode > 400) {
		// repeat again
		HISCentralSardegnaConnector.API_KEY = ConfigurationWrapper.getCredentialsSetting().getSardegnaApiKey().orElse(null);

		getStationResponse = downloader.downloadResponse(//
			linkage.trim(), //
			HttpHeaderUtils.build("api-key", HISCentralSardegnaConnector.API_KEY));

		stream = getStationResponse.body();

		GSLoggerFactory.getLogger(getClass()).info("Got " + linkage);
	    }

	    if (stream != null) {
		JSONArray obj = new JSONArray(IOStreamUtils.asUTF8String(stream));
		stream.close();
		if (obj != null) {
		    if (obj.length() > 0) {
			JSONObject jsonObj = (JSONObject) obj.get(obj.length() - 1);
			if (jsonObj != null) {
			    date = jsonObj.optString("data_mis");
			    return date;
			}
		    }
		}
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + linkage);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + linkage + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HISCENTRAL_SARDEGNA_MAPPER_ERROR);
	}

	return null;
    }

}
