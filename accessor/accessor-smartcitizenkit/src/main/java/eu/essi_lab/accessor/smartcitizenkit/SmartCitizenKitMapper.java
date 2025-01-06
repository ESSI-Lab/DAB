package eu.essi_lab.accessor.smartcitizenkit;

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

import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author roncella
 */
public class SmartCitizenKitMapper extends OriginalIdentifierMapper {

    private static final String SMART_CITIZEN_KIT = "SMART_CITIZEN_KIT";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public SmartCitizenKitMapper() {
	// do nothing
    }

    public static OriginalMetadata create(JSONObject datasetInfo, JSONObject sensorInfo) {
	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.SMARTCITIZENKIT);

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
     * @param sensor metadata
     * @return
     */
    private JSONObject retrieveSensorInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("sensor-info");
    }

    public enum Resolution {
	HOURLY
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.SMARTCITIZENKIT;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	//
	// DATASET INFO
	// {
	// "id": 15602,
	// "uuid": "a3140944-b642-43e9-8563-2050a25b1680",
	// "name": "Volderslaan_16, eerste_verdieping",
	// "description": "Smart Citizen Kit 2.1 with Urban Sensor Board",
	// "state": "has_published",
	// "system_tags": [
	// "offline",
	// "outdoor"
	// ],
	// "user_tags": [
	// "I-CHANGE",
	// "Research",
	// "First Floor"
	// ],
	// "is_private": false,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "created_at": "2022-08-26T09:49:54Z",
	// "updated_at": "2022-12-03T10:28:39Z",
	// "notify": {
	// "stopped_publishing": false,
	// "low_battery": false
	// },
	// "device_token": "[FILTERED]",
	// "postprocessing": null,
	// "location": {
	// "ip": null,
	// "exposure": "outdoor",
	// "elevation": null,
	// "latitude": 50.923355,
	// "longitude": 5.3232257,
	// "geohash": "u15c9nk601",
	// "city": "Hasselt",
	// "country_code": "BE",
	// "country": "Belgium"
	// },
	// "hardware": {
	// "name": "SCK 2.1",
	// "type": "SCK",
	// "version": "2.1",
	// "slug": "sck:2,1",
	// "last_status_message": "[FILTERED]"
	// },
	// "owner": {
	// "id": 6562,
	// "uuid": "2a6a5648-c7d9-407c-a2f6-a5b7a76f273e",
	// "username": "adnan",
	// "url": null,
	// "avatar": "https://smartcitizen.s3.amazonaws.com/avatars/default.svg",
	// "profile_picture": "",
	// "location": {
	// "city": null,
	// "country": null,
	// "country_code": null
	// },
	// "device_ids": [
	// 4787,
	// 5003,
	// 5004,
	// 5005,
	// 5006,
	// 5017,
	// 5018,
	// 10022,
	// 15595,
	// 15596,
	// 15598,
	// 15599,
	// 15600,
	// 15601,
	// 15602,
	// 15604,
	// 15605,
	// 15606,
	// 15607,
	// 15619,
	// 15846,
	// 15858,
	// 15859,
	// 15860,
	// 15861,
	// 15862,
	// 15871,
	// 16651,
	// 16669,
	// 16677,
	// 16762,
	// 16853,
	// 16854
	// ]
	// },
	// "data": {
	// "sensors": [
	// {
	// "id": 113,
	// "ancestry": "111",
	// "name": "AMS CCS811 - TVOC",
	// "description": "Total Volatile Organic Compounds Digital Indoor Sensor",
	// "unit": "ppb",
	// "created_at": "2019-03-21T16:43:37Z",
	// "updated_at": "2019-03-21T16:43:37Z",
	// "uuid": "0c2a1afc-dc08-4066-aacb-0bde6a3ae6f5",
	// "default_key": "tvoc",
	// "measurement": {
	// "id": 47,
	// "name": "TVOC",
	// "description": "Total volatile organic compounds is a grouping of a wide range of organic chemical compounds
	// to simplify reporting when these are present in ambient air or emissions. Many substances, such as natural
	// gas, could be classified as volatile organic compounds (VOCs).",
	// "unit": null,
	// "uuid": "c6f9a729-1782-4da1-adc9-e88b7143e45c"
	// },
	// "value": 3582.0,
	// "prev_value": 3582.0,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// },
	// {
	// "id": 112,
	// "ancestry": "111",
	// "name": "AMS CCS811 - eCO2",
	// "description": "Equivalent Carbon Dioxide Digital Indoor Sensor",
	// "unit": "ppm",
	// "created_at": "2019-03-21T16:43:37Z",
	// "updated_at": "2019-03-21T16:43:37Z",
	// "uuid": "995343c9-12ac-40c0-b6b9-19699e524f86",
	// "default_key": "eco2",
	// "measurement": {
	// "id": 46,
	// "name": "eCO2",
	// "description": "Equivalent CO2 is the concentration of CO2 that would cause the same level of radiative
	// forcing as a given type and concentration of greenhouse gas. Examples of such greenhouse gases are methane,
	// perfluorocarbons, and nitrous oxide. CO2 is primarily a by-product of human metabolism and is constantly
	// being emitted into the indoor environment by building occupants. CO2 may come from combustion sources as
	// well. Associations of higher indoor carbon dioxide concentrations with impaired work performance and
	// increased health symptoms have been attributed to correlation of indoor CO2 with concentrations of other
	// indoor air pollutants that are also influenced by rates of outdoor-air ventilation.",
	// "unit": null,
	// "uuid": "b6fee847-2bb6-4e1e-8e39-979612e2beb9"
	// },
	// "value": 3341.0,
	// "prev_value": 3341.0,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// },
	// {
	// "id": 14,
	// "ancestry": null,
	// "name": "BH1730FVC - Light",
	// "description": "Digital Ambient Light Sensor",
	// "unit": "lux",
	// "created_at": "2015-02-02T18:24:56Z",
	// "updated_at": "2021-05-07T16:23:50Z",
	// "uuid": "ac4234cf-d2b7-4cfa-8765-9f4477e2de5f",
	// "default_key": "light",
	// "measurement": {
	// "id": 3,
	// "name": "Light",
	// "description": "Lux is a measure of how much light is spread over a given area. A full moon clear night is
	// around 1 lux, inside an office building you usually have 400 lux and a bright day can be more than 20000
	// lux.",
	// "unit": null,
	// "uuid": "50aa0431-86ac-4340-bf51-ad498ee35a3b"
	// },
	// "value": 69.0,
	// "prev_value": 69.0,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// },
	// {
	// "id": 10,
	// "ancestry": null,
	// "name": "Battery SCK",
	// "description": "Custom Circuit",
	// "unit": "%",
	// "created_at": "2015-02-02T18:18:00Z",
	// "updated_at": "2020-12-11T16:12:40Z",
	// "uuid": "c9ff2784-53a7-4a84-b0fc-90ecc7e313f9",
	// "default_key": "bat",
	// "measurement": {
	// "id": 7,
	// "name": "battery",
	// "description": "The SCK remaining battery level in percentage.",
	// "unit": null,
	// "uuid": "c5964926-c2d2-4714-98b5-18f84c6f95c1"
	// },
	// "value": 97.0,
	// "prev_value": 97.0,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// },
	// {
	// "id": 53,
	// "ancestry": "52",
	// "name": "ICS43432 - Noise",
	// "description": "I2S Digital Mems Microphone with custom Audio Processing Algorithm",
	// "unit": "dBA",
	// "created_at": "2018-05-03T10:42:47Z",
	// "updated_at": "2018-05-03T10:42:54Z",
	// "uuid": "f508548e-3fd1-44aa-839b-9bd147168481",
	// "default_key": "noise_dba",
	// "measurement": {
	// "id": 4,
	// "name": "Noise Level",
	// "description": "dB's measure sound pressure difference between the average local pressure and the pressure in
	// the sound wave. A quiet library is below 40dB, your house is around 50dB and a diesel truck in your street
	// 90dB.",
	// "unit": null,
	// "uuid": "2841719f-658e-40df-a14a-74a86adc1410"
	// },
	// "value": 44.83,
	// "prev_value": 44.83,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// },
	// {
	// "id": 58,
	// "ancestry": "57",
	// "name": "NXP MPL3115A2 - Barometric Pressure",
	// "description": "Digital Barometric Pressure Sensor",
	// "unit": "kPa",
	// "created_at": "2018-05-03T10:49:17Z",
	// "updated_at": "2023-07-14T16:52:14Z",
	// "uuid": "cadd2459-6559-4d92-aed1-ba04c557fed8",
	// "default_key": "bar",
	// "measurement": {
	// "id": 25,
	// "name": "Barometric Pressure",
	// "description": "Barometric pressure is the pressure within the atmosphere of Earth. In most circumstances
	// atmospheric pressure is closely approximated by the hydrostatic pressure caused by the weight of air above
	// the measurement point.",
	// "unit": "NULL",
	// "uuid": "2a944e9d-073d-49ed-8dc7-e376495f283d"
	// },
	// "value": 101.66,
	// "prev_value": 101.66,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// },
	// {
	// "id": 89,
	// "ancestry": "86",
	// "name": "Plantower PMS5003 - PM1.0",
	// "description": "Particle Matter PM 1",
	// "unit": "ug/m3",
	// "created_at": "2018-05-22T13:20:34Z",
	// "updated_at": "2023-05-23T11:13:03Z",
	// "uuid": "a4b9efba-241f-446e-9cf2-918f25efd0c5",
	// "default_key": "pm_avg_1",
	// "measurement": {
	// "id": 27,
	// "name": "PM 1",
	// "description": "PM stands for particulate matter: the term for a mixture of solid particles and liquid
	// droplets found in the air. Some particles, such as dust, dirt, soot, or smoke, are large or dark enough to be
	// seen with the naked eye.",
	// "unit": null,
	// "uuid": "9759c2fd-15d8-424b-adcc-d6efcf940f6e"
	// },
	// "value": 5.0,
	// "prev_value": 5.0,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// },
	// {
	// "id": 88,
	// "ancestry": "86",
	// "name": "Plantower PMS5003 - PM10",
	// "description": "Particle Matter PM 10",
	// "unit": "ug/m3",
	// "created_at": "2018-05-22T13:20:34Z",
	// "updated_at": "2023-05-23T11:12:06Z",
	// "uuid": "c2072a22-4d81-4d7c-a38c-af9458b8f309",
	// "default_key": "pm_avg_10",
	// "measurement": {
	// "id": 13,
	// "name": "PM 10",
	// "description": "PM stands for particulate matter: the term for a mixture of solid particles and liquid
	// droplets found in the air. Some particles, such as dust, dirt, soot, or smoke, are large or dark enough to be
	// seen with the naked eye.",
	// "unit": null,
	// "uuid": "30e5b614-ab7e-46bc-b6f7-fa9a30926ce9"
	// },
	// "value": 19.0,
	// "prev_value": 19.0,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// },
	// {
	// "id": 87,
	// "ancestry": "86",
	// "name": "Plantower PMS5003 - PM2.5",
	// "description": "Particle Matter PM 2.5",
	// "unit": "ug/m3",
	// "created_at": "2018-05-22T13:20:34Z",
	// "updated_at": "2023-05-23T11:13:07Z",
	// "uuid": "9ee89ac2-0482-46dd-905f-0b7a1bb12c55",
	// "default_key": "pm_avg_2.5",
	// "measurement": {
	// "id": 14,
	// "name": "PM 2.5",
	// "description": "PM stands for particulate matter: the term for a mixture of solid particles and liquid
	// droplets found in the air. Some particles, such as dust, dirt, soot, or smoke, are large or dark enough to be
	// seen with the naked eye.",
	// "unit": null,
	// "uuid": "c8ecda46-c430-4cbc-9ad4-5ea8a07c5820"
	// },
	// "value": 16.0,
	// "prev_value": 16.0,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// },
	// {
	// "id": 56,
	// "ancestry": "54",
	// "name": "Sensirion SHT31 - Humidity",
	// "description": "Humidity",
	// "unit": "%",
	// "created_at": "2018-05-03T10:47:17Z",
	// "updated_at": "2023-07-14T17:16:44Z",
	// "uuid": "b6543356-0066-4bea-8ad2-687e282f9c20",
	// "default_key": "h",
	// "measurement": {
	// "id": 2,
	// "name": "Relative Humidity",
	// "description": "Relative humidity is a measure of the amount of moisture in the air relative to the total
	// amount of moisture the air can hold. For instance, if the relative humidity was 50%, then the air is only
	// half saturated with moisture.",
	// "unit": null,
	// "uuid": "9cbbd396-5bd3-44be-adc0-7ffba778072d"
	// },
	// "value": 67.99,
	// "prev_value": 67.99,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// },
	// {
	// "id": 55,
	// "ancestry": "54",
	// "name": "Sensirion SHT31 - Temperature",
	// "description": "Temperature",
	// "unit": "ºC",
	// "created_at": "2018-05-03T10:47:15Z",
	// "updated_at": "2023-07-14T17:16:19Z",
	// "uuid": "384e46a2-80dd-481e-a9fc-cfbd512f9f43",
	// "default_key": "t",
	// "measurement": {
	// "id": 1,
	// "name": "Air Temperature",
	// "description": "Air temperature is a measure of how hot or cold the air is. It is the most commonly measured
	// weather parameter. Air temperature is dependent on the amount and strength of the sunlight hitting the earth,
	// and atmospheric conditions, such as cloud cover and humidity, which trap heat.",
	// "unit": null,
	// "uuid": "b3f44b63-0a17-4d84-bbf1-4c17764b7eae"
	// },
	// "value": 4.44,
	// "prev_value": 4.44,
	// "last_reading_at": "2022-12-03T10:13:33Z",
	// "tags": []
	// }
	// ]
	// }
	// }

	/**
	 * SENSOR INFO
	 */
	// {
	// "id": 138,
	// "ancestry": "137",
	// "name": "ADC_49_0",
	// "description": "16-bit Analog to Digital Converter - ADS1X15 addr. 49 ch. 0",
	// "unit": "V",
	// "created_at": "2020-12-11T15:37:12Z",
	// "updated_at": "2020-12-11T15:48:02Z",
	// "uuid": "cf6a2bc0-3338-430f-9cdd-0e50604adb4d",
	// "default_key": "adc_49_0",
	// "measurement": {
	// "id": 11,
	// "name": "Voltage",
	// "description": "Voltage, also known as electric pressure, electric tension, or (electric) potential
	// difference, is the difference in electric potential between two points.",
	// "unit": null,
	// "uuid": "49a89988-2385-4a92-8bde-dc48de240aab"
	// },
	// "value": 0.281891,
	// "prev_value": 0.281891,
	// "last_reading_at": "2023-01-03T14:40:08Z",
	// "tags": [
	// "raw"
	// ]
	// }

	dataset.getPropertyHandler().setIsTimeseries(true);

	JSONObject datasetInfo = retrieveDatasetInfo(originalMD);

	JSONObject sensorInfo = retrieveSensorInfo(originalMD);

	String stationName = datasetInfo.optString("name");
	String stationDescription = datasetInfo.optString("description");

	String stationId = datasetInfo.optString("id");

	String startDate = datasetInfo.optString("created_at");
	String endDate = datasetInfo.optString("last_reading_at");

	JSONObject authorsObj = datasetInfo.optJSONObject("owner");

	JSONObject locationObj = datasetInfo.optJSONObject("location");

	JSONObject platformObj = datasetInfo.optJSONObject("hardware");
	String platformName = null;
	String platformType = null;
	if (platformObj != null) {
	    platformName = platformObj.optString("name");
	    platformType = platformObj.optString("type");
	}

	String variableId = null;
	String variableUUID = null;
	String variableName = null;
	String variableDescription = null;
	String variableUnit = sensorInfo.optString("unit");
	String sensorId = sensorInfo.optString("id");
	JSONObject measurementObj = sensorInfo.optJSONObject("measurement");
	if (measurementObj != null) {
	    variableUUID = measurementObj.optString("uuid");
	    variableId = measurementObj.optString("id");
	    variableName = measurementObj.optString("name");
	    variableDescription = measurementObj.optString("description");
	}

	// SmartCitizenKitVariable variable = SmartCitizenKitVariable.decode(key);
	Double pointLon = null;
	Double pointLat = null;
	Double altitude = null;
	if (locationObj != null) {
	    pointLon = locationObj.optDouble("longitude");
	    pointLat = locationObj.optDouble("latitude");
	    // altitude = locationObj.getDouble("altitude");
	}

	Resolution resolution = Resolution.HOURLY;

	// TEMPORAL EXTENT
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	TemporalExtent extent = new TemporalExtent();

	if (startDate != null && !startDate.isEmpty()) {

	    extent.setBeginPosition(startDate);

	    if (endDate != null && !endDate.isEmpty()) {

		extent.setEndPosition(endDate);
	    } else {
		extent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	    }

	    /**
	     * CODE COMMENTED BELOW COULD BE USEFUL
	     * // if (dateTime.isPresent()) {
	     * // String beginTime = ISO8601DateTimeUtils.getISO8601DateTime(dateTime.get());
	     * // extent.setPosition(beginTime, startIndeterminate, false, true);
	     * // // Estimate of the data size
	     * // // only an estimate seems to be possible, as this odata service doesn't seem to support the
	     * /$count
	     * // // operator
	     * // double expectedValuesPerYears = 12.0; // 1 value every 5 minutes
	     * // double expectedValuesPerDay = expectedValuesPerHours * 24.0;
	     * // long expectedSize = TimeSeriesUtils.estimateSize(dateTime.get(), new Date(),
	     * expectedValuesPerDay);
	     * // GridSpatialRepresentation grid = new GridSpatialRepresentation();
	     * // grid.setNumberOfDimensions(1);
	     * // grid.setCellGeometryCode("point");
	     * // Dimension time = new Dimension();
	     * // time.setDimensionNameTypeCode("time");
	     * // try {
	     * // time.setDimensionSize(new BigInteger("" + expectedSize));
	     * // ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	     * // extensionHandler.setDataSize(expectedSize);
	     * // } catch (Exception e) {
	     * // }
	     * // grid.addAxisDimension(time);
	     * // coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);
	     * // }
	     */

	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	}

	coreMetadata.setTitle("Acquisitions of " + variableName + " through Smart Citizen Kit device: " + stationId);
	coreMetadata.setAbstract(
		stationDescription + " . This dataset contains " + variableDescription + " timeseries from I-CHANGE Smart Citizen Kit");

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(SMART_CITIZEN_KIT);

	coreMetadata.getMIMetadata().getDataIdentification().setCitationPublicationDate(ISO8601DateTimeUtils.getISO8601Date(new Date()));

	// if (!station.getState().equals("")) {
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword("State: " + station.getState());
	// }
	// if (!station.getCountry().equals("")) {
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Country: " + station.getCountry());
	// }
	// if (!station.getIcao().equals("")) {
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword("ICAO: " + station.getIcao());
	// }

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variableName);
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variable.getKey());
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variable.toString().toLowerCase());

	if (resolution.equals(Resolution.HOURLY))
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("hourly");

	ExtensionHandler handler = dataset.getExtensionHandler();
	handler.setTimeUnits("h");
	handler.setTimeResolution("1");
	handler.setAttributeMissingValue("-9999");
	handler.setAttributeUnitsAbbreviation(variableUnit);

	//
	// URL + variable
	//
	// String id = UUID.nameUUIDFromBytes((splittedStrings[0] + splittedStrings[11]).getBytes()).toString();

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	// bounding box (Multipoint)
	// "POLYGON ((8.6391694 44.386031, 8.6391037 44.3860898, 8.6390179 44.3863223, 8.6391489
	// 44.3864548, 8.6393916 44.3866083, 8.6636556 44.3988833, 8.6672147 44.4002499, 8.6676504 44.4003547, 8.6690215
	// 44.4005383, 8.6708526 44.4007344, 8.6736574 44.4008072, 8.6742229 44.4007635, 8.6746885 44.4006371, 8.6750503
	// 44.4005098, 8.675219 44.400353, 8.6750815 44.4002186, 8.6655395 44.3914033, 8.665203 44.3911424, 8.664728
	// 44.3910255, 8.6391694 44.386031))",

	// Double[] bbox = multipointToBbox(coordinates);

	// bounding box (Multipoint)

	// bounding box
	if (pointLon != null && pointLat != null) {

	    coreMetadata.addBoundingBox(pointLat, pointLon, pointLat, pointLon);

	}
	// elevation

	if (altitude != null) {
	    VerticalExtent verticalExtent = new VerticalExtent();
	    verticalExtent.setMinimumValue(altitude);
	    verticalExtent.setMaximumValue(altitude);
	    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	}

	// elevation
	// String minElevation = station.getMinElevation();
	// String maxElevation = station.getMaxElevation();
	// if (minElevation != null && !minElevation.equals("") && maxElevation != null && !maxElevation.equals("")) {
	// VerticalExtent verticalExtent = new VerticalExtent();
	// if (isDouble(minElevation)) {
	// verticalExtent.setMinimumValue(Double.parseDouble(minElevation));
	// }
	// if (isDouble(maxElevation)) {
	// verticalExtent.setMaximumValue(Double.parseDouble(maxElevation));
	// }
	// coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	// }

	// contact point
	if (authorsObj != null) {

	    String authorName = authorsObj.optString("username");

	    if (authorName != null) {
		ResponsibleParty creatorContact = new ResponsibleParty();
		creatorContact.setOrganisationName(authorName);
		creatorContact.setRoleCode("originator");
		// creatorContact.setIndividualName("Anirban Guha");
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
	    }
	}

	// contact point
	// ResponsibleParty creatorContact = new ResponsibleParty();
	//
	// creatorContact.setOrganisationName("Tripura University");
	// creatorContact.setRoleCode("originator");
	// creatorContact.setIndividualName("Anirban Guha");
	//
	// Contact contactcreatorContactInfo = new Contact();
	// Address address = new Address();
	// address.addElectronicMailAddress("anirbanguha@tripurauniv.in");
	// contactcreatorContactInfo.setAddress(address);
	// creatorContact.setContactInfo(contactcreatorContactInfo);
	//

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	String platformIdentifier = "i-change-smart-citizen-kit:" + stationId;

	platform.setMDIdentifierCode(platformIdentifier);

	String siteDescription = stationId;

	platform.setDescription(siteDescription);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationId);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();
	String varId = stationId + ":" + variableUUID;

	coverageDescription.setAttributeIdentifier(varId);
	coverageDescription.setAttributeTitle(variableName);

	String attributeDescription = variableDescription + " Units: " + variableUnit;

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	/**
	 * ONLINE
	 */
	// https://i-change.s3.amazonaws.com/Ams01_TEMP.csv
	// Online online = new Online();
	// online.setProtocol(NetProtocols.HTTP.getCommonURN());
	// String linkage = "https://i-change.s3.amazonaws.com/" + station.getName() + buildingURL;
	// online.setLinkage(linkage);
	// online.setName(variable + "@" + station.getName());
	// online.setFunctionCode("download");
	// online.setDescription(variable + " Station name: " + station.getName());
	//
	// coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

	String resourceIdentifier = generateCode(dataset, variableName + ":" + stationId);

	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	// https://ionbeam-dev.ecmwf.int/api/v1/retrieve?project=public&platform=meteotracker&observation_variable=air_temperature_near_surface&
	// datetime=2022-06-16T18%3A13%3A15%2B00%3A00&filter=select+%2A+from+result+where+source_id+%3D+%2762ab72c11d8e11061d32002a%27%3B&format=csv

	/**
	 * Linkage url to be parametized:
	 * project={public, i-change}
	 * platform = {meteotracker, acronet, smart}
	 * observation_variable = platform.getKey()
	 */
	try {

	    String linkage = SmartCitizenKitConnector.BASE_URL + "devices/" + stationId + "/readings?sensor_id=" + sensorId + "&rollup=1h";// +
	    // station.getName()
	    // +
	    // buildingURL;

	    Online o = new Online();
	    o.setLinkage(linkage);
	    o.setFunctionCode("download");
	    o.setName(stationId + ":" + sensorId + ":" + variableUnit);
	    o.setIdentifier(stationName + ":" + variableDescription);
	    o.setProtocol(CommonNameSpaceContext.SMARTCITIZENKIT);
	    o.setDescription(variableDescription + " Station name: " + stationName);
	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);

	} catch (Exception e) {
	    // TODO: handle exception
	}

	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

    }

    protected boolean isDouble(String str) {
	try {
	    // check if it can be parsed as any double
	    Double.parseDouble(str);
	    return true;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

    public static void main(String[] args) {
	TemporalExtent extent = new TemporalExtent();
	TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
	extent.setIndeterminateEndPosition(endTimeInderminate);
	TimeIndeterminateValueType startIndeterminate = TimeIndeterminateValueType.AFTER;
	extent.setIndeterminateBeginPosition(startIndeterminate);
	Calendar efd = Calendar.getInstance();
	efd.setTime(new Date());
	String value = ISO8601DateTimeUtils.getISO8601Date(efd.getTime());
	extent.setBeginPosition(value);

	Dataset dataset = new Dataset();
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);

	// TemporalExtent timeExt = dataset.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	// String begin = timeExt.getBeginPosition();
	// String end = timeExt.getEndPosition();
	// TimeIndeterminateValueType indBegin = timeExt.getIndeterminateBeginPosition();
	// TimeIndeterminateValueType indEnd = timeExt.getIndeterminateEndPosition();

    }
}
