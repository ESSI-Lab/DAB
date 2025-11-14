package eu.essi_lab.accessor.sensorthings._1_1.mapper;

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

import java.math.BigDecimal;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.VerticalCRS;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.client.request.SensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.response.AddressableEntityResult;
import eu.essi_lab.lib.sensorthings._1_1.model.UnitOfMeasurement;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Datastream;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Location;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.ObservedProperty;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Sensor;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Thing;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.ExtensionHandler;

/**
 * @author Fabrizio
 */
public class HydroServer2Mapper extends SensorThingsMapper {

    /**
     * 
     */
    private static final String SERVER_URN = "urn:hydroserver2:";

    /**
     * 
     */
    public static final String SENSOR_THINGS_1_1_HYDRO_SERVER_2_SCHEMA = "http://www.opengis.net/doc/is/sensorthings/1.1_HydroServer2";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SENSOR_THINGS_1_1_HYDRO_SERVER_2_SCHEMA;
    }

    /**
     * @param streamId
     * @return
     * @throws GSException
     */
    protected Datastream downloadStrem(String streamId) throws GSException {

	SensorThingsRequest sensorThingsRequest = createRequest().//
		add(EntityRef.DATASTREAMS, streamId);

	Optional<AddressableEntityResult<Datastream>> entityResponse;

	try {
	    entityResponse = sensorThingsClient.//
		    execute(sensorThingsRequest).//
		    getAddressableEntityResult(Datastream.class);

	    return entityResponse.get().getEntities().get(0);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).equals(e);

	    throw GSException.createException(getClass(), getDownloadStreamErrorMessage(), e);
	}
    }

    /**
     * @param thingId
     * @return
     * @throws GSException
     */
    protected Thing downloadThing(String thingId) throws GSException {

	SensorThingsRequest sensorThingsRequest = createRequest().//
		add(EntityRef.THINGS, thingId);

	Optional<AddressableEntityResult<Thing>> entityResponse;

	try {
	    entityResponse = sensorThingsClient.//
		    execute(sensorThingsRequest).//
		    getAddressableEntityResult(Thing.class);

	    return entityResponse.get().getEntities().get(0);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).equals(e);

	    throw GSException.createException(getClass(), getDownloadThingErrorMessage(), e);
	}
    }

    /**
     * @param stream
     * @param coreMetadata
     */
    @Override
    protected void setTitle(Datastream stream, CoreMetadata coreMetadata) {

	Optional<String> streamDesc = stream.getDescription();
	//
	// at the moment since title is equal to the id, we replace it with the description
	// deprived of the text after the '-' symbol
	//

	Optional<String> streamId = stream.getIdentifier();
	Optional<String> streamName = stream.getName();

	if (streamId.isPresent() && streamName.isPresent() && streamDesc.isPresent() && streamDesc.get().contains("-")
		&& streamId.get().equals(streamName.get())) {
	    String title = streamDesc.get().substring(0, streamDesc.get().indexOf("-")).trim();
	    coreMetadata.setTitle(title);

	} else {
	    coreMetadata.setTitle(streamName.isPresent() ? streamName.get() : stream.getIdentifier().get());
	}
    }

    /**
     * @param stream
     * @param coreMetadata
     * @param keywords
     */
    @Override
    protected void addInstrument(Datastream stream, CoreMetadata coreMetadata, KeywordsCollector keywords) {

	MIInstrument instrument = null;
	Optional<Sensor> optSensor = stream.getSensor();

	if (optSensor.isPresent()) {

	    instrument = new MIInstrument();
	    coreMetadata.getMIMetadata().addMIInstrument(instrument);

	    Sensor sensor = optSensor.get();

	    Optional<String> sensorName = sensor.getName();
	    instrument.setTitle(sensorName.get().trim());

	    Optional<String> sensorDesc = sensor.getDescription();
	    instrument.setDescription(sensorDesc.get().trim());

	    Object metadata = sensor.getMetadata().get();
	    if (metadata instanceof JSONObject) {
		JSONObject sensorMetadata = (JSONObject) metadata;

		//
		// "methodCode": "dl-smtp",
		// "methodType": "Instrument Deployment",
		// "methodLink": "https://www.decentlab.com/",
		// "sensorModel": {
		// "sensorModelName": "Decentlab DL-SMTP",
		// "sensorModelURL":
		// "https://www.decentlab.com/products/soil-moisture-and-temperature-profile-for-lorawan",
		// "sensorManufacturer": "Decentlab"
		// }

		//
		// Sensor code
		//
		String sensorCode = SERVER_URN;
		sensorCode += sensorName.get().trim();

		String methodCode = sensorMetadata.optString("methodCode");
		if (!methodCode.isEmpty()) {
		    sensorCode = sensorCode + ":" + methodCode.trim();
		    keywords.addKeyword(methodCode, "methodCode");
		}
		instrument.setMDIdentifierTypeCode(sensorCode);

		String methodType = sensorMetadata.optString("methodType");
		if (!methodType.isEmpty()) {
		    instrument.setSensorType(methodType.trim());
		    keywords.addKeyword(methodType, "methodType");
		}

		String methodLink = sensorMetadata.optString("methodLink");
		keywords.addKeyword(methodLink, "mehtodLink");

		if (sensorMetadata.has("sensorModel")) {

		    JSONObject sensorModel = sensorMetadata.getJSONObject("sensorModel");

		    String sensorModelName = sensorModel.optString("sensorModelName");
		    String sensorModelURL = sensorModel.optString("sensorModelURL");
		    String sensorManufacturer = sensorModel.optString("sensorManufacturer");

		    keywords.addKeyword(sensorModelName, "instrument");
		    keywords.addKeyword(sensorModelURL, "sensorModelURL");
		    keywords.addKeyword(sensorManufacturer, "sensorManufacturer");
		}
	    }else if (metadata instanceof String) {
		String strMetadata = (String) metadata;
		
	    }
	}
    }

    /**
     * @param stream
     * @param coreMetadata
     * @param keywords
     */
    @Override
    protected void addCoverageDescription(Datastream stream, CoreMetadata coreMetadata, KeywordsCollector keywords) {

	// normally present
	Optional<ObservedProperty> optObservedProperty = stream.getObservedProperty();

	CoverageDescription coverageDescription = new CoverageDescription();

	if (optObservedProperty.isPresent()) {

	    ObservedProperty observedProperty = optObservedProperty.get();

	    Optional<String> name = observedProperty.getName();
	    if (name.isPresent()) {

		keywords.addKeyword(normalize(name.get()), "observedProperty");
		coverageDescription.setAttributeTitle(normalize(name.get()));
	    }

	    Optional<String> description = observedProperty.getDescription();
	    if (description.isPresent()) {

		keywords.addKeyword(description.get().trim(), "observedPropertyDescription");
		coverageDescription.setAttributeDescription(normalize(description.get()));
	    }

	    Optional<JSONObject> optProperties = observedProperty.getProperties();
	    if (optProperties.isPresent()) {

		JSONObject optObservedPropertyProp = optProperties.get();

		String variableCode = optObservedPropertyProp.optString("variableCode");
		String variableType = optObservedPropertyProp.optString("variableType");

		String coverageId = SERVER_URN;

		if (!variableCode.isEmpty()) {

		    coverageId += variableCode.trim();

		    keywords.addKeyword(variableCode, "variableCode");
		}

		if (!variableType.isEmpty()) {

		    coverageId += ":" + variableType.trim();

		    keywords.addKeyword(variableType, "variableType");
		}

		coverageDescription.setAttributeIdentifier(coverageId);
		coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
	    }
	}
    }

    /**
     * @param thing
     * @param dataId
     */
    @Override
    protected void addResponsibleParty(Thing thing, Datastream stream, DataIdentification dataId) {

	if (thing.getProperties().get().has("contactPeople")) {

	    JSONArray contactPeople = thing.getProperties().get().getJSONArray("contactPeople");

	    contactPeople.forEach(contact -> {

		JSONObject object = (JSONObject) contact;
		String firstName = object.optString("firstName");
		String lastName = object.optString("lastName");
		String email = object.optString("email");
		String organization = object.optString("organizationName");

		if (!firstName.isEmpty() || !lastName.isEmpty() || !organization.isEmpty()) {

		    ResponsibleParty responsibleParty = new ResponsibleParty();
		    if (!firstName.isEmpty() && !lastName.isEmpty()) {
			responsibleParty.setIndividualName(firstName.trim() + " " + lastName.trim());
		    } else {
			responsibleParty.setOrganisationName(organization.trim());
		    }

		    if (!email.isEmpty()) {
			Contact con = new Contact();
			Address address = new Address();
			address.addElectronicMailAddress(email.trim());
			con.setAddress(address);
			responsibleParty.setContactInfo(con);
		    }

		    dataId.addPointOfContact(responsibleParty);
		}
	    });
	}
    }

    /**
     * @param handler
     * @param stream
     */
    @Override
    protected void addExtensions(Datastream stream, ExtensionHandler handler) {

	UnitOfMeasurement unitOfMeasurement = stream.getUnitOfMeasurement();

	//
	// Attribute units
	//
	String unitName = unitOfMeasurement.getName();
	if (!unitName.isEmpty()) {
	    handler.setAttributeUnits(unitName);
	}
	// String unitDef = unitOfMeasurement.getDefinition();

	//
	// Attribute units abbreviation
	//
	String unitSymbol = unitOfMeasurement.getSymbol();
	if (!unitSymbol.isEmpty()) {
	    handler.setAttributeUnitsAbbreviation(unitSymbol);
	}

	JSONObject properties = stream.getProperties().get();

	// String resultType = properties.optString("resultType");
	// String status = properties.optString("status");
	// String sampledMedium = properties.optString("sampledMedium");
	//
	// int valueCount = properties.optInt("valueCount", Integer.MAX_VALUE);

	//
	// Attribute missing value
	//

	BigDecimal noDataValue = properties.optBigDecimal("noDataValue", null);
	if (noDataValue != null) {

	    handler.setAttributeMissingValue(noDataValue.toString());
	}

	// String processingLevelCode = properties.optString("processingLevelCode");

	//
	// Time resolution duration
	//

	String intTimeUnit = null;
	if (properties.has("intendedTimeSpacingUnitOfMeasurement")) {

	    Object intTimeSpacing = properties.get("intendedTimeSpacingUnitOfMeasurement");
	    if (intTimeSpacing instanceof JSONObject) {

		JSONObject intUnit = (JSONObject) intTimeSpacing;
		intTimeUnit = intUnit.optString("name");

		// String name = intUnit.optString("name");
		//
		// String symbol = intUnit.optString("symbol");
		//
		// String definition = intUnit.optString("definition");

	    } else {

		intTimeUnit = properties.optString("intendedTimeSpacingUnitOfMeasurement", null);
	    }
	}

	BigDecimal intendedTimeSpacing = properties.optBigDecimal("intendedTimeSpacing", null);
	if (intendedTimeSpacing != null && intTimeUnit != null && !intTimeUnit.isEmpty()) {

	    handler.setTimeResolutionDuration8601(

		    ISO8601DateTimeUtils.getDuration(intendedTimeSpacing, intTimeUnit).toString());
	}

	//
	// Time interpolation
	//

	String aggregationStatistic = properties.optString("aggregationStatistic");
	if (!aggregationStatistic.isEmpty()) {

	    // should be Continuous or Average
	    handler.setTimeInterpolation(aggregationStatistic);
	}

	//
	// Time aggregation duration
	//

	String aggrTimeUnitName = null;
	if (properties.has("timeAggregationIntervalUnitOfMeasurement")) {

	    Object object = properties.get("timeAggregationIntervalUnitOfMeasurement");
	    if (object instanceof JSONObject) {

		JSONObject timeUnit = (JSONObject) object;
		aggrTimeUnitName = timeUnit.optString("name");

	    } else {

		aggrTimeUnitName = object.toString();
	    }

	    // String timeUnitSymbol = timeUnit.optString("symbol");
	    //
	    // String timeUnitDef = timeUnit.optString("definition");
	}

	BigDecimal timeAggregationInterval = properties.optBigDecimal("timeAggregationInterval", null);
	if (timeAggregationInterval != null && aggrTimeUnitName != null && !aggrTimeUnitName.isEmpty()) {

	    handler.setTimeAggregationDuration8601(

		    ISO8601DateTimeUtils.getDuration(timeAggregationInterval, aggrTimeUnitName).toString());
	}
    }

    /**
     * @param thing
     * @param keywords
     * @param dataId
     */
    @Override
    protected void addVerticalExtent(Thing thing, KeywordsCollector keywords, DataIdentification dataId) {

	Optional<Location> location = thing.getLocations().isEmpty() ? Optional.empty() : Optional.of(thing.getLocations().get(0));

	if (location.isPresent()) {

	    Optional<JSONObject> optLocationProperties = location.get().getProperties();

	    if (optLocationProperties.isPresent()) {

		JSONObject properties = optLocationProperties.get();

		//
		// Vertical extent
		//

		VerticalExtent verticalExtent = new VerticalExtent();

		double elevation = properties.optDouble("elevation_m", Double.MAX_VALUE);

		if (elevation != Double.MAX_VALUE) {

		    verticalExtent.setMinimumValue(elevation);
		    verticalExtent.setMaximumValue(elevation);

		    String elevationDatum = properties.optString("elevationDatum");
		    if (!elevationDatum.isEmpty()) {

			VerticalCRS verticalCRS = new VerticalCRS();
			verticalCRS.setId(elevationDatum);

			verticalExtent.setVerticalCRS(verticalCRS);
		    }

		    dataId.addVerticalExtent(verticalExtent);
		}
	    }
	}
    }

    /**
     * @param coreMetadata
     * @param location
     * @param keywords
     * @param dataId
     * @return
     */
    @Override
    protected void addPlatform(Thing thing, CoreMetadata coreMetadata, DataIdentification dataId, KeywordsCollector keywords,
	    ExtensionHandler handler) {

	Optional<Location> location = thing.getLocations().isEmpty() ? Optional.empty() : Optional.of(thing.getLocations().get(0));

	MIPlatform platform = new MIPlatform();

	String platformName = null;

	if (thing.getName().isPresent()) {
	    platformName = normalize(thing.getName().get());
	} else {

	}

	if (location.isPresent()) {

	    //
	    // Platform Title
	    //

	    if (platformName == null && location.get().getName().isPresent()) {

		platformName = location.get().getName().get();
		platformName = normalize(platformName);
	    }

	    Optional<JSONObject> optLocationProp = location.get().getProperties();

	    //
	    // Platform description and keywords
	    //

	    if (optLocationProp.isPresent()) {

		String state = optLocationProp.get().optString("state");
		String county = optLocationProp.get().optString("county");

		platform.setDescription("State: " + state + ", county: " + county);

		keywords.addKeyword(state, "place");
		keywords.addKeyword(county, "place");

		String countryCode = optLocationProp.get().optString("countryCode");
		if (countryCode != null && !countryCode.isEmpty()) {
		    Country c = Country.decode(countryCode);
		    if (c != null) {
			handler.setCountry(c.getShortName());
			handler.setCountryISO3(c.getISO3());
		    }
		}
	    }
	}

	Citation citation = new Citation();
	citation.setTitle(platformName);
	platform.setCitation(citation);

	//
	// Platform id
	//

	String platformId = SERVER_URN;

	String samplingFeatureType = thing.getProperties().get().optString("samplingFeatureType");
	if (!samplingFeatureType.isEmpty()) {
	    platformId += samplingFeatureType.trim() + ":";
	}

	String samplingFeatureCode = thing.getProperties().get().optString("samplingFeatureCode");
	if (!samplingFeatureCode.isEmpty()) {
	    platformId += samplingFeatureCode.trim() + ":";
	}

	String siteType = thing.getProperties().get().optString("siteType");
	if (!siteType.isEmpty()) {
	    platformId += siteType.trim();
	}

	platformId = platformId.replace(" ", "").replace(",", "").trim();
	platform.setMDIdentifierCode(platformId);

	//
	//
	//

	coreMetadata.getMIMetadata().addMIPlatform(platform);
    }

    /**
     * @param location
     * @param dataId
     * @param keywords
     */
    @Override
    protected void addBoundingBox(Thing thing, DataIdentification dataId, KeywordsCollector keywords) {

	Optional<Location> location = thing.getLocations().isEmpty() ? Optional.empty() : Optional.of(thing.getLocations().get(0));

	if (location.isPresent()) {

	    GeographicBoundingBox boundingBox = null;

	    if (location.get().getLocation().has("geometry")) {

		JSONObject geometry = location.get().getLocation().getJSONObject("geometry");

		if (geometry.has("coordinates")) {

		    boundingBox = createBoundingBox(//
			    location.get().getName(), //
			    geometry.getJSONArray("coordinates"));

		    dataId.addGeographicBoundingBox(boundingBox);
		}
	    }
	}
    }

    /**
     * @param locationName
     * @param coordinates
     * @return
     */
    protected GeographicBoundingBox createBoundingBox(Optional<String> locationName, JSONArray coordinates) {

	GeographicBoundingBox boundingBox = new GeographicBoundingBox();

	if (locationName.isPresent()) {
	    boundingBox.setId(locationName.get());
	}

	boundingBox.setBigDecimalNorth(coordinates.getBigDecimal(0));
	boundingBox.setBigDecimalSouth(coordinates.getBigDecimal(0));

	boundingBox.setBigDecimalWest(coordinates.getBigDecimal(1));
	boundingBox.setBigDecimalEast(coordinates.getBigDecimal(1));

	return boundingBox;
    }

    @Override
    public String getProfileName() {

	return "HydroServer2";
    }

    @Override
    protected String getSupportedProtocol() {

	return NetProtocols.SENSOR_THINGS_1_1_HYDRO_SERVER_2.getCommonURN();
    }
}
