package eu.essi_lab.sensorthings.client._1_1.service.request.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.sensorthings._1_1.client.SensorThingsClient;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.client.request.FluentSensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.SensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandItem;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandItem.Operation;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandOption;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SelectOption;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SystemQueryOptions;
import eu.essi_lab.lib.sensorthings._1_1.client.response.AddressableEntityResult;
import eu.essi_lab.lib.sensorthings._1_1.client.response.DataArrayFormatResult;
import eu.essi_lab.lib.sensorthings._1_1.client.response.DataArrayResultItem;
import eu.essi_lab.lib.sensorthings._1_1.client.response.SensorThingsResponse;
import eu.essi_lab.lib.sensorthings._1_1.client.response.capabilities.ServiceRootResult;
import eu.essi_lab.lib.sensorthings._1_1.client.response.capabilities.ServiceRootResultSetting;
import eu.essi_lab.lib.sensorthings._1_1.client.response.capabilities.ServiceRootResultValue;
import eu.essi_lab.lib.sensorthings._1_1.model.ObservedArea;
import eu.essi_lab.lib.sensorthings._1_1.model.UnitOfMeasurement;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Datastream;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Entity;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.HistoricalLocation;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Location;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Observation;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.ObservedProperty;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Sensor;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Thing;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class SensorThingsClientExternalTestIT {

    @Test(expected = GSException.class)
    public void retryTest() throws MalformedURLException, GSException {

	new SensorThingsClient(new URL("https://test")).//
		withRetryPolicy(3, 2).//
		execute(FluentSensorThingsRequest.get()).//
		getServiceRootResult().//
		get();

    }

    /**
     * @throws GSException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void getServerInfoTest() throws GSException, IllegalArgumentException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://playground.hydroserver.org/api/sensorthings/v1.1/"));

	FluentSensorThingsRequest request = FluentSensorThingsRequest.get(true);

	ServiceRootResult serviceRootResult = client.//
		execute(request).//
		getServiceRootResult().//
		get();

	ServiceRootResultValue value = serviceRootResult.getValue();

	List<SimpleEntry<String, String>> features = value.getServerFeatures().//
		stream().//
		sorted((f1, f2) -> f1.getKey().compareTo(f2.getKey())).//
		collect(Collectors.toList());

	Assert.assertEquals("Datastreams", features.get(0).getKey());
	Assert.assertEquals("FeaturesOfInterest", features.get(1).getKey());
	Assert.assertEquals("HistoricalLocations", features.get(2).getKey());
	Assert.assertEquals("Locations", features.get(3).getKey());
	Assert.assertEquals("Observations", features.get(4).getKey());
	Assert.assertEquals("ObservedProperties", features.get(5).getKey());
	Assert.assertEquals("Sensors", features.get(6).getKey());
	Assert.assertEquals("Things", features.get(7).getKey());

	ServiceRootResultSetting serviceRootResultSettings = serviceRootResult.getServerSettings();

	List<String> conformance = serviceRootResultSettings.getConformance();

	Assert.assertFalse(conformance.isEmpty());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void addressableEntityResponseTest_Things_Collections() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://playground.hydroserver.org/api/sensorthings/v1.1/"));

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get(true).//
		add(EntityRef.THINGS);

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertFalse(nextLink.isPresent());

	List<Thing> things = addressableEntityResponse.getEntities();

	Thing thing = things.get(0);

	String name = thing.getName().get();
	Assert.assertEquals("San Pedro Tabasco", name);

	String description = thing.getDescription().get();
	Assert.assertEquals("947939", description);

	String identifier = thing.getIdentifier().get();
	Assert.assertEquals("4dbd8b69-be1c-4368-923a-eb72b657b9ad", identifier);

	String selfLink = thing.getSelfLink().get();
	Assert.assertEquals("https://playground.hydroserver.org/api/sensorthings/v1.1/Things('4dbd8b69-be1c-4368-923a-eb72b657b9ad')", selfLink);

	Optional<JSONObject> properties = thing.getProperties();
	Assert.assertTrue(properties.isPresent());

	//
	//
	//

	Assert.assertFalse(thing.isInline(EntityRef.FEATURE_OF_INTEREST).isPresent());
	Assert.assertFalse(thing.isInline(EntityRef.DATASTREAMS).get());
	Assert.assertFalse(thing.isInline(EntityRef.HISTORICAL_LOCATIONS).get());
	Assert.assertFalse(thing.isInline(EntityRef.LOCATIONS).get());

	List<Datastream> datastreams = thing.getDatastreams();
	Assert.assertFalse(datastreams.isEmpty());

	List<HistoricalLocation> historicalLocations = thing.getHistoricalLocations();
	Assert.assertTrue(historicalLocations.isEmpty());

	//
	//
	//

	List<Location> locations = thing.getLocations();
	Assert.assertFalse(locations.isEmpty());

	Location location = locations.get(0);

	List<Thing> locThings = location.getThings();
	Assert.assertFalse(locThings.isEmpty());

	JSONObject locationLoc = location.getLocation();
	Assert.assertNotNull(locationLoc);

	String locDesc = location.getDescription().get();
	Assert.assertEquals("location", locDesc);

	String encodingType = location.getEncodingType().get();
	Assert.assertEquals("application/geo+json", encodingType);

	JSONObject entity = location.getObject();
	Assert.assertNotNull(entity);

	List<HistoricalLocation> locHist = location.getHistoricalLocations();
	Assert.assertTrue(locHist.isEmpty());

	String locId = location.getIdentifier().get();
	Assert.assertEquals("dfbbf648-88e3-45fc-85e9-5204cfad4fcc", locId);

	String locName = location.getName().get();
	Assert.assertEquals("Location for San Pedro Tabasco", locName);

	Optional<JSONObject> locProp = location.getProperties();
	Assert.assertTrue(locProp.isPresent());

	String locSelfLink = location.getSelfLink().get();
	Assert.assertEquals("https://playground.hydroserver.org/api/sensorthings/v1.1/Locations('dfbbf648-88e3-45fc-85e9-5204cfad4fcc')",
		locSelfLink);

    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void addressableEntityResponseTest_Things_Single() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://playground.hydroserver.org/api/sensorthings/v1.1/"));

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get(true).//
		add(EntityRef.THINGS, "0637f970-4b79-485a-98f1-de4573411cf0");

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertFalse(nextLink.isPresent());

	List<Thing> entities = addressableEntityResponse.getEntities();
	Assert.assertEquals(1, entities.size());

	Thing thing = entities.get(0);

	String name = thing.getName().get();
	Assert.assertEquals("Rapart Reservoir", name);

	String description = thing.getDescription().get();
	Assert.assertEquals("A beta testing site ", description);

	String identifier = thing.getIdentifier().get();
	Assert.assertEquals("0637f970-4b79-485a-98f1-de4573411cf0", identifier);

	String selfLink = thing.getSelfLink().get();
	Assert.assertEquals("https://playground.hydroserver.org/api/sensorthings/v1.1/Things('0637f970-4b79-485a-98f1-de4573411cf0')", selfLink);

	Optional<JSONObject> properties = thing.getProperties();
	Assert.assertTrue(properties.isPresent());

	//
	//
	//

	Assert.assertFalse(thing.isInline(EntityRef.DATASTREAMS).get());
	Assert.assertFalse(thing.isInline(EntityRef.HISTORICAL_LOCATIONS).get());
	Assert.assertFalse(thing.isInline(EntityRef.LOCATIONS).get());

	List<Datastream> datastreams = thing.getDatastreams();
	Assert.assertTrue(datastreams.isEmpty());

	List<HistoricalLocation> historicalLocations = thing.getHistoricalLocations();
	Assert.assertTrue(historicalLocations.isEmpty());

	List<Location> locations = thing.getLocations();
	Assert.assertFalse(locations.isEmpty());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void addressableEntityResponseTest_Datastreams_Collections() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://playground.hydroserver.org/api/sensorthings/v1.1/"));

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get(true).//
		add(EntityRef.DATASTREAMS);

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Datastream> addressableEntityResponse = response.getAddressableEntityResult(Datastream.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertFalse(nextLink.isPresent());

	List<Datastream> entities = addressableEntityResponse.getEntities();

	Datastream stream = entities.get(0);

	String name = stream.getName().get();
	Assert.assertEquals("0203d754-a5e0-47de-9a3f-c182a1c92c9d", name);

	String description = stream.getDescription().get();
	Assert.assertEquals("Soil moisture at depth 2 at Catchment at Dell Pond  - 0", description);

	String identifier = stream.getIdentifier().get();
	Assert.assertEquals("0203d754-a5e0-47de-9a3f-c182a1c92c9d", identifier);

	String selfLink = stream.getSelfLink().get();
	Assert.assertEquals("https://playground.hydroserver.org/api/sensorthings/v1.1/Datastreams('0203d754-a5e0-47de-9a3f-c182a1c92c9d')",
		selfLink);

	Optional<JSONObject> properties = stream.getProperties();
	Assert.assertTrue(properties.isPresent());

	String observationType = stream.getObservationType();
	Assert.assertFalse(observationType.isEmpty());

	Optional<String> phenTime = stream.getPhenomenonTime();
	Assert.assertTrue(phenTime.isPresent());

	UnitOfMeasurement unitOfMeasurement = stream.getUnitOfMeasurement();
	String definition = unitOfMeasurement.getDefinition();
	Assert.assertEquals(
		"SFU is related to volumetric soil water content and the relationship can be determined by calibration procedures",
		definition);

	String unitName = unitOfMeasurement.getName();
	Assert.assertEquals("Scaled frequency unit", unitName);

	String symbol = unitOfMeasurement.getSymbol();
	Assert.assertEquals("SFU", symbol);

	Optional<ObservedArea> observedArea = stream.getObservedArea();
	Assert.assertFalse(observedArea.isPresent());

	Optional<String> resultTime = stream.getResultTime();
	Assert.assertFalse(resultTime.isPresent());

	List<Observation> obs = stream.getObservations();
	Assert.assertFalse(obs.isEmpty());

	Optional<Thing> thing = stream.getThing();
	Assert.assertTrue(thing.isPresent());

	Optional<ObservedProperty> obsProp = stream.getObservedProperty();
	Assert.assertTrue(obsProp.isPresent());

	Optional<Sensor> sensor = stream.getSensor();
	Assert.assertTrue(sensor.isPresent());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void addressableEntityResponseTest_Observations_Collections() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://playground.hydroserver.org/api/sensorthings/v1.1/"));

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get(true).//
		add(EntityRef.OBSERVATIONS);

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Observation> addressableEntityResponse = response.getAddressableEntityResult(Observation.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertEquals(nextLink.get(), "https://playground.hydroserver.org/api/sensorthings/v1.1/Observations?$skip=100&$top=100");

	List<Observation> entities = addressableEntityResponse.getEntities();

	Observation observation = entities.get(0);

	String identifier = observation.getIdentifier().get();
	Assert.assertEquals("79e33dbc-f659-4142-b60b-abac8b52175e", identifier);

	Assert.assertFalse(observation.getName().isPresent());
	Assert.assertFalse(observation.getDescription().isPresent());

	String selfLink = observation.getSelfLink().get();
	Assert.assertEquals("https://playground.hydroserver.org/api/sensorthings/v1.1/Observations('79e33dbc-f659-4142-b60b-abac8b52175e')",
		selfLink);

	Optional<JSONObject> properties = observation.getProperties();
	Assert.assertFalse(properties.isPresent());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void expandSinglePropertyTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			expand(new ExpandOption(EntityRef.DATASTREAMS)));

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertEquals(nextLink.get(),
		"https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/Things?$skip=100&$expand=Datastreams&$orderby=%40iot.id+asc&$skipFilter=%28%40iot.id+gt+100%29");

	List<Thing> entities = addressableEntityResponse.getEntities();

	Thing thing = entities.get(0);

	String identifier = thing.getIdentifier().get();
	Assert.assertEquals("1", identifier);

	String name = thing.getName().get();
	Assert.assertEquals("[Barbotteau] à Petit-Bourg - Barbotteau", name);

	String description = thing.getDescription().get();
	Assert.assertEquals("Implantation station 2m avant prise d'eau Conseil Général", description);

	String selfLink = thing.getSelfLink().get();
	Assert.assertEquals("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/Things(1)", selfLink);

	Optional<JSONObject> properties = thing.getProperties();
	Assert.assertTrue(properties.isPresent());

	//
	//
	//

	Assert.assertTrue(thing.isInline(EntityRef.DATASTREAMS).get());

	List<Datastream> datastreams = thing.getDatastreams();
	Assert.assertFalse(datastreams.isEmpty());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void expandMultiplePropertiesTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	ExpandOption expandOption = new ExpandOption(//
		ExpandItem.get(EntityRef.DATASTREAMS), //
		ExpandItem.get(EntityRef.LOCATIONS), //
		ExpandItem.get(EntityRef.HISTORICAL_LOCATIONS)//
	);

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertEquals(nextLink.get(),
		"https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/Things?$skip=100&$expand=Datastreams,Locations,HistoricalLocations&$orderby=%40iot.id+asc&$skipFilter=%28%40iot.id+gt+100%29");

	List<Thing> entities = addressableEntityResponse.getEntities();

	Thing thing = entities.get(0);

	String identifier = thing.getIdentifier().get();
	Assert.assertEquals("1", identifier);

	String name = thing.getName().get();
	Assert.assertEquals("[Barbotteau] à Petit-Bourg - Barbotteau", name);

	String description = thing.getDescription().get();
	Assert.assertEquals("Implantation station 2m avant prise d'eau Conseil Général", description);

	String selfLink = thing.getSelfLink().get();
	Assert.assertEquals("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/Things(1)", selfLink);

	Optional<JSONObject> properties = thing.getProperties();
	Assert.assertTrue(properties.isPresent());

	//
	//
	//

	Assert.assertTrue(thing.isInline(EntityRef.DATASTREAMS).get());
	Assert.assertTrue(thing.isInline(EntityRef.LOCATIONS).get());
	Assert.assertTrue(thing.isInline(EntityRef.HISTORICAL_LOCATIONS).get());

	List<Datastream> datastreams = thing.getDatastreams();
	Assert.assertFalse(datastreams.isEmpty());

	List<Location> locations = thing.getLocations();
	Assert.assertFalse(locations.isEmpty());

	List<HistoricalLocation> historicalLocations = thing.getHistoricalLocations();
	Assert.assertFalse(historicalLocations.isEmpty());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void testExpandFilterAndSelect_1() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	ExpandOption expandOption = new ExpandOption(//
		ExpandItem.get(EntityRef.OBSERVATIONS, Operation.FILTER, "result eq 1"),
		ExpandItem.get(EntityRef.OBSERVATIONS, Operation.SELECT, "result")); //

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get(false).//
		add(EntityRef.DATASTREAMS, "35").//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Datastream> addressableEntityResponse = response.getAddressableEntityResult(Datastream.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertFalse(nextLink.isPresent());

	List<Datastream> streams = addressableEntityResponse.getEntities();

	Datastream stream = streams.get(0);

	String identifier = stream.getIdentifier().get();
	Assert.assertEquals("35", identifier);

	String name = stream.getName().get();
	Assert.assertEquals(
		"Hydrometry depth at La rivière de l'Anse Céron au Prêcheur with method Hydrometry depth measurement by electronic probe",
		name);

	String description = stream.getDescription().get();
	Assert.assertEquals(
		"Hydrometry depth at La rivière de l'Anse Céron au Prêcheur with method Hydrometry depth measurement by electronic probe",
		description);

	String selfLink = stream.getSelfLink().get();
	Assert.assertEquals("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/Datastreams(35)", selfLink);

	Optional<JSONObject> properties = stream.getProperties();
	Assert.assertTrue(properties.isPresent());

	//
	//
	//

	Assert.assertTrue(stream.isInline(EntityRef.OBSERVATIONS).get());
	Assert.assertFalse(stream.isInline(EntityRef.OBSERVED_PROPERTY).get());
	Assert.assertFalse(stream.isInline(EntityRef.SENSOR).get());

	List<Observation> obs = stream.getObservations();
	Assert.assertTrue(obs.isEmpty());

	Optional<ObservedProperty> obsProp = stream.getObservedProperty();
	Assert.assertTrue(obsProp.isPresent());

	Optional<Sensor> sensor = stream.getSensor();
	Assert.assertTrue(sensor.isPresent());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void testExpandFilterAndSelect_2() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	ExpandOption expandOption = new ExpandOption(//
		ExpandItem.get(EntityRef.OBSERVATIONS, Operation.FILTER, "result eq 145.0"),
		ExpandItem.get(EntityRef.SENSOR, Operation.SELECT, "name")); //

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get(false).//
		add(EntityRef.DATASTREAMS, "35").//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Datastream> addressableEntityResponse = response.getAddressableEntityResult(Datastream.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertFalse(nextLink.isPresent());

	List<Datastream> streams = addressableEntityResponse.getEntities();

	Datastream stream = streams.get(0);

	String identifier = stream.getIdentifier().get();
	Assert.assertEquals("35", identifier);

	String name = stream.getName().get();
	Assert.assertEquals(
		"Hydrometry depth at La rivière de l'Anse Céron au Prêcheur with method Hydrometry depth measurement by electronic probe",
		name);

	String description = stream.getDescription().get();
	Assert.assertEquals(
		"Hydrometry depth at La rivière de l'Anse Céron au Prêcheur with method Hydrometry depth measurement by electronic probe",
		description);

	String selfLink = stream.getSelfLink().get();
	Assert.assertEquals("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/Datastreams(35)", selfLink);

	Optional<JSONObject> properties = stream.getProperties();
	Assert.assertTrue(properties.isPresent());

	//
	//
	//

	Assert.assertTrue(stream.isInline(EntityRef.OBSERVATIONS).get());
	Assert.assertTrue(stream.isInline(EntityRef.SENSOR).get());

	Assert.assertFalse(stream.isInline(EntityRef.OBSERVED_PROPERTY).get());

	List<Observation> obs = stream.getObservations();
	Assert.assertFalse(obs.isEmpty());

	Optional<ObservedProperty> obsProp = stream.getObservedProperty();
	Assert.assertTrue(obsProp.isPresent());

	Optional<Sensor> sensor = stream.getSensor();
	Assert.assertTrue(sensor.isPresent());

	Assert.assertTrue(sensor.get().getName().isPresent()); // selected
	Assert.assertFalse(sensor.get().getDescription().isPresent()); // not selected
    }

    @Test
    public void testExpandPropertiesAndExpandHierarchy() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	ExpandOption expandOption = new ExpandOption(//
		ExpandItem.get(EntityRef.DATASTREAMS), // navigation property
		ExpandItem.get(EntityRef.LOCATIONS), // navigation property
		ExpandItem.get(EntityRef.DATASTREAMS, EntityRef.OBSERVED_PROPERTY)); // navigation
										     // hierarchy

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get(false).//
		add(EntityRef.THINGS, "1").//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertFalse(nextLink.isPresent());

	List<Thing> things = addressableEntityResponse.getEntities();
	Assert.assertEquals(1, things.size());

	Thing thing = things.get(0);

	String identifier = thing.getIdentifier().get();
	Assert.assertEquals("1", identifier);

	String name = thing.getName().get();
	Assert.assertEquals("[Barbotteau] à Petit-Bourg - Barbotteau", name);

	String description = thing.getDescription().get();
	Assert.assertEquals("Implantation station 2m avant prise d'eau Conseil Général", description);

	String selfLink = thing.getSelfLink().get();
	Assert.assertEquals("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/Things(1)", selfLink);

	Optional<JSONObject> properties = thing.getProperties();
	Assert.assertTrue(properties.isPresent());

	//
	//
	//

	Assert.assertTrue(thing.isInline(EntityRef.DATASTREAMS).get());
	Assert.assertTrue(thing.isInline(EntityRef.LOCATIONS).get());

	Assert.assertFalse(thing.isInline(EntityRef.HISTORICAL_LOCATIONS).get());

	//
	//
	//

	List<Datastream> datastreams = thing.getDatastreams();
	Assert.assertEquals(2, datastreams.size());

	Datastream datastream = datastreams.get(0);
	Assert.assertTrue(datastream.isInline(EntityRef.OBSERVED_PROPERTY).get());

	Optional<ObservedProperty> observedProperty = datastream.getObservedProperty();
	Assert.assertTrue(observedProperty.isPresent());

	List<Location> locations = thing.getLocations();
	Assert.assertEquals(1, locations.size());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void topTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	//
	//
	//

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS);

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertTrue(nextLink.isPresent());

	Assert.assertTrue(addressableEntityResponse.getEntities().size() > 1);

	//
	//
	//

	request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			top(1));

	response = client.execute(request);

	addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	nextLink = addressableEntityResponse.getNextLink();
	Assert.assertTrue(nextLink.isPresent());

	Assert.assertEquals(1, addressableEntityResponse.getEntities().size());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void skipTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	//
	//
	//

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS);

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertTrue(nextLink.isPresent());

	Assert.assertEquals("1", addressableEntityResponse.getEntities().get(0).getIdentifier().get());

	//
	//
	//

	request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			skip(5));

	response = client.execute(request);

	addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	nextLink = addressableEntityResponse.getNextLink();
	Assert.assertTrue(nextLink.isPresent());

	Assert.assertEquals("6", addressableEntityResponse.getEntities().get(0).getIdentifier().get());
    }

    @Test
    public void countTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	//
	//
	//

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS);

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	//
	//
	//

	request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			count());

	response = client.execute(request);

	addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	count = addressableEntityResponse.getCount();
	Assert.assertTrue(count.isPresent());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void orderByTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	//
	//
	//

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS);

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = addressableEntityResponse.getNextLink();
	Assert.assertTrue(nextLink.isPresent());

	Assert.assertEquals("1", addressableEntityResponse.getEntities().get(0).getIdentifier().get());

	//
	//
	//

	request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			orderBy("name"));

	response = client.execute(request);

	addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	count = addressableEntityResponse.getCount();
	Assert.assertFalse(count.isPresent());

	nextLink = addressableEntityResponse.getNextLink();
	Assert.assertTrue(nextLink.isPresent());

	Assert.assertEquals("2453", addressableEntityResponse.getEntities().get(0).getIdentifier().get());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void filterTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	//
	//
	//

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			count());

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	Optional<Integer> count = addressableEntityResponse.getCount();
	Assert.assertTrue(count.get() > 1);

	//
	//
	//

	request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			count().//
			filter("id eq 1"));

	response = client.execute(request);

	addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	count = addressableEntityResponse.getCount();
	Assert.assertEquals(Integer.valueOf("1"), count.get());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void dataArrayTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	//
	//
	//

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.OBSERVATIONS).//
		setDataArrayResultFormat();

	SensorThingsResponse response = client.execute(request);

	DataArrayFormatResult dataArrayResponse = response.getDataArrayFormatResult().get();

	Optional<Integer> count = dataArrayResponse.getCount();
	Assert.assertFalse(count.isPresent());

	Optional<String> nextLink = dataArrayResponse.getNextLink();
	Assert.assertTrue(nextLink.isPresent());

	//
	//
	//

	List<DataArrayResultItem> resultItems = dataArrayResponse.getResultItems();

	DataArrayResultItem item = resultItems.get(0);

	JSONArray components = item.getComponents();
	Assert.assertNotNull(components);

	JSONArray dataArray = item.getDataArray();
	Assert.assertNotNull(dataArray);
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void dataArrayAndCountAndTopTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	//
	//
	//

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.OBSERVATIONS).//
		setDataArrayResultFormat().//
		with(SystemQueryOptions.get().//
			count().//
			top(1));

	SensorThingsResponse response = client.execute(request);

	DataArrayFormatResult dataArrayResponse = response.getDataArrayFormatResult().get();

	Optional<Integer> count = dataArrayResponse.getCount();
	Assert.assertTrue(count.get() > 1);

	Optional<String> nextLink = dataArrayResponse.getNextLink();
	Assert.assertTrue(nextLink.isPresent());

	List<DataArrayResultItem> resultItems = dataArrayResponse.getResultItems();
	Assert.assertEquals(1, resultItems.size());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void dataArrayAndSelectTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1/"));

	//
	//
	//

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.OBSERVATIONS).//
		setDataArrayResultFormat().//
		with(SystemQueryOptions.get().//
			select(new SelectOption("id")));

	SensorThingsResponse response = client.execute(request);

	DataArrayFormatResult dataArrayResponse = response.getDataArrayFormatResult().get();

	List<DataArrayResultItem> resultItems = dataArrayResponse.getResultItems();

	DataArrayResultItem item = resultItems.get(0);

	JSONArray components = item.getComponents();
	Assert.assertEquals(1, components.length());
	Assert.assertEquals("id", components.get(0).toString());

	JSONArray dataArray = item.getDataArray();
	JSONArray jsonArray = dataArray.getJSONArray(0);
	Assert.assertEquals(1, jsonArray.length());
	Assert.assertEquals("1", jsonArray.get(0).toString());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void extendedAddressableEntityResponseTest_Parties_Collections() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://citiobs.demo.secure-dimensions.de/staplus/v1.1/"));

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.of("Parties"));

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Entity> addressableEntityResponse = response.getAddressableEntityResult(Entity.class).get();

	List<Entity> parties = addressableEntityResponse.getEntities();

	Entity party = parties.get(0);

	Optional<String> name = party.getName();
	Assert.assertFalse(name.isPresent());

	Optional<String> description = party.getDescription();
	Assert.assertFalse(description.isPresent());

	String identifier = party.getIdentifier().get();
	Assert.assertEquals("1f42fae7-9382-3d9b-8d21-1994577f0aa1", identifier);

	String selfLink = party.getSelfLink().get();
	Assert.assertEquals("https://citiobs.demo.secure-dimensions.de/staplus/v1.1/Parties('1f42fae7-9382-3d9b-8d21-1994577f0aa1')",
		selfLink);

	Optional<JSONObject> properties = party.getProperties();
	Assert.assertFalse(properties.isPresent());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void expandExtendedAddressableEntityResponseTest_Things_Parties() throws IllegalArgumentException, GSException, IOException {

	SensorThingsClient client = new SensorThingsClient(new URL("https://citiobs.demo.secure-dimensions.de/staplus/v1.1/"));

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			expand(new ExpandOption(EntityRef.of("Party"))));

	SensorThingsResponse response = client.execute(request);

	AddressableEntityResult<Thing> addressableEntityResponse = response.getAddressableEntityResult(Thing.class).get();

	List<Thing> things = addressableEntityResponse.getEntities();

	Thing thing = things.get(0);
	Assert.assertTrue(thing.isInline(EntityRef.of("Party")).get());

	List<Entity> extensions = thing.getExtensions(EntityRef.of("Party"));

	Entity party = extensions.get(0);

	Optional<String> name = party.getName();
	Assert.assertFalse(name.isPresent());

	Optional<String> description = party.getDescription();
	Assert.assertFalse(description.isPresent());

	Optional<String> identifier = party.getIdentifier();
	Assert.assertTrue(identifier.isPresent());

	Optional<String> selfLink = party.getSelfLink();
	Assert.assertTrue(selfLink.isPresent());

	String authId = party.getObject().optString("authId");
	Assert.assertFalse(authId.isEmpty());

	String displayName = party.getObject().optString("displayName");
	Assert.assertFalse(displayName.isEmpty());
	Optional<JSONObject> properties = party.getProperties();
	Assert.assertFalse(properties.isPresent());
    }
}
