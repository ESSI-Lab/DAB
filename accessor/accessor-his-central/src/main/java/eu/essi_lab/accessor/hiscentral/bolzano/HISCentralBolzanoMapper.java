package eu.essi_lab.accessor.hiscentral.bolzano;

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

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author Roberto
 */
public class HISCentralBolzanoMapper extends FileIdentifierMapper {

    private SimpleDateFormat iso8601Format;

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_BOLZANO_NS_URI;
    }

    public HISCentralBolzanoMapper() {
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
    static OriginalMetadata create(JSONObject datasetInfo, JSONObject sensorInfo) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_BOLZANO_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("sensor-info", sensorInfo);

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

	//
	// DATASET INFO
	// {
	// "type" : "Feature",
	// "geometry" : {
	// "type" : "Point",
	// "coordinates" : [ 1350416.7428132016, 5944669.743398376 ]
	// },
	// "properties" : {
	// "SCODE" : "50400WS",
	// "NAME_D" : "Prettau Lengspitze",
	// "NAME_I" : "Predoi Pizzo Lungo",
	// "NAME_L" : "Prettau Lengspitze",
	// "NAME_E" : "Prettau Lengspitze",
	// "ALT" : 3105,
	// "LONG" : 12.131,
	// "LAT" : 47.0159
	// }
	// }
	//

	// String resourceLocator = datasetInfo.getString("ResourceLocator");
	//
	// String keyword = datasetInfo.getString("Keyword");
	//
	//
	// String contactInfo = datasetInfo.optJSONObject("ResponsibleParty").getString("contactInfo");
	//
	// String resourceConstraints = datasetInfo.optJSONObject("ResourceConstraints").getString("useLimitation");
	// String resourceAbstract = datasetInfo.optString("ResourceAbstract");

	JSONObject properties = datasetInfo.optJSONObject("properties");

	String resourceTitle = properties.optString("NAME_I");
	String resourceGermanTitle = properties.optString("NAME_D");
	// String organisationName = datasetInfo.optString("proprietario");
	// String tempExtenBegin = datasetInfo.optString("data_inizio");
	// String tempExtenEnd = datasetInfo.optString("data_fine");

	Double pointLon = properties.optDouble("LONG");
	Double pointLat = properties.optDouble("LAT");
	Double altitude = properties.optDouble("ALT");

	String id = properties.optString("SCODE");

	//
	// MEASURE INFO
	// "SCODE":"19850PG",
	// "TYPE":"Q",
	// "DESC_D":"Durchfluss",
	// "DESC_I":"Portata",
	// "DESC_L":"Ega passeda",
	// "UNIT":"m³/s",
	// "DATE":"2023-08-21T12:20:00CEST",
	// "VALUE":36.4
	//

	String parameterType = sensorInfo.optString("TYPE");
	String measureName = sensorInfo.optString("DESC_I");

	String unit = sensorInfo.optString("UNIT");

	String tempExtenEnd = sensorInfo.optString("DATE");
	//

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setLanguage("Italian");
	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.addDistributionFormat("WaterML 1.1");

	coreMetadata.getMIMetadata().getDataIdentification()
		.setCitationTitle(resourceTitle + " (" + resourceGermanTitle + ")" + " - " + measureName);
	coreMetadata.getMIMetadata().getDataIdentification()
		.setAbstract(resourceTitle + " (" + resourceGermanTitle + ")" + " - " + measureName);

	//
	// id
	//
	coreMetadata.setIdentifier(id);
	coreMetadata.getMIMetadata().setFileIdentifier(id);

	//
	// responsible party
	//
	ResponsibleParty publisherContact = new ResponsibleParty();

	publisherContact.setOrganisationName("Servizio meteorologico provinciale di Bolzano-Alto Adige");
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
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(measureDescription);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("BOLZANO");
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("ALTO ADIGE");
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword("FRIULI VENEZIA GIULIA");

	Keywords kwd = new Keywords();
	kwd.setTypeCode("platform");
	kwd.addKeyword(resourceTitle);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(kwd);

	//
	// bbox
	//

	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	coreMetadata.addBoundingBox(//
		pointLat, //
		pointLon, //
		pointLat, //
		pointLon);

	// vertical extent
	coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(altitude, altitude);

	//
	// platform
	//

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(id);

	dataset.getExtensionHandler().setCountry("ITA");

	platform.setDescription(resourceTitle);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(resourceTitle);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	//
	// temp extent
	//

	// String beginPosition = normalizeUTCPosition(tempExtenBegin);

	TemporalExtent temporalExtent = new TemporalExtent();
	// temporalExtent.setBeginPosition(beginPosition);
	// end time to be reviewed

	// TODO: check with the provider
	temporalExtent.setBeginPosition("2014-08-01T00:00:00");

	if (tempExtenEnd != null && !tempExtenEnd.isEmpty()) {
	    tempExtenEnd = tempExtenEnd.contains("CEST") ? tempExtenEnd.split("CEST")[0] : tempExtenEnd;
	    tempExtenEnd = tempExtenEnd.contains("CET") ? tempExtenEnd.split("CET")[0] : tempExtenEnd;
	    temporalExtent.setEndPosition(tempExtenEnd);
	}
	temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
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

	// if (tempExtenBegin.contains("+")) {
	//
	// tempExtenBegin = tempExtenBegin.substring(0, tempExtenBegin.indexOf("+"));
	// }

	String linkage = HISCentralBolzanoConnector.BASE_URL + "timeseries?station_code=" + id + "&sensor_code=" + parameterType;

	Online online = new Online();
	online.setLinkage(linkage);
	online.setFunctionCode("download");
	online.setName(resourceTitle + "_" + parameterType);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_BOLZANO_NS_URI);

	distribution.addDistributionOnline(online);

	//
	// coverage description
	//

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(parameterType);
	coverageDescription.setAttributeTitle(measureName);

	String missingValue = "-9999";
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	if (unit != null) {
	    dataset.getExtensionHandler().setAttributeUnits(unit);
	}

	// as no description is given this field is calculated
	HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);

	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

    }

    /**
     * @param timePosition
     * @return
     */
    private String normalizeUTCPosition(String timePosition) {

	if (timePosition.endsWith("/")) {
	    timePosition = timePosition.substring(0, timePosition.length() - 1);
	}

	return timePosition;
    }
}
