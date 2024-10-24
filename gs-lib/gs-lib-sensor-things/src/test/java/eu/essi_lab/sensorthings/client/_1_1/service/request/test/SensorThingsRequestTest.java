package eu.essi_lab.sensorthings.client._1_1.service.request.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.sensorthings._1_1.client.request.AddressingPathSegment;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.client.request.FluentSensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.SensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandItem;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandItem.Operation;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.ExpandOption;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SelectOption;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SystemQueryOptions;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class SensorThingsRequestTest {

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-no-resource-path">Example 10: a SensorThings request
     * with no resource path</a>
     */
    @Test
    public void noResourcePathTest() throws MalformedURLException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		setServiceRootUrl("http://example.org/v1.1/");

	Assert.assertEquals("http://example.org/v1.1/", request.compose());

	Assert.assertFalse(request.isQuoteIdentifiersSet());

	Assert.assertFalse(request.getEntityProperty().isPresent());

	Assert.assertFalse(request.isAddressAssociationLinkSet());

	Assert.assertFalse(request.getSystemQueryOptions().isPresent());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-collection-entities">Usage 2</a>
     */
    @Test
    public void testUsage2_1() throws MalformedURLException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		setServiceRootUrl("http://example.org/v1.1/").//
		add(EntityRef.OBSERVED_PROPERTIES);

	Assert.assertEquals("http://example.org/v1.1/ObservedProperties", request.compose());

	Assert.assertFalse(request.isQuoteIdentifiersSet());

	Assert.assertFalse(request.getEntityProperty().isPresent());

	Assert.assertFalse(request.isAddressAssociationLinkSet());

	Assert.assertEquals(1, request.getAddressableEntityList().size());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-collection-entities">Usage 2</a>
     */
    @Test
    public void testUsage2_2() throws MalformedURLException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVED_PROPERTIES);

	Assert.assertEquals("http://example.org/v1.1/ObservedProperties", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-entity">Usage 3</a>
     */
    @Test
    public void testUsage3_1() throws MalformedURLException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS, "1");

	Assert.assertFalse(request.isQuoteIdentifiersSet());

	Assert.assertEquals("http://example.org/v1.1/Things(1)", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-entity">Usage 3</a>
     */
    @Test
    public void testUsage3_2() throws MalformedURLException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/", true).//
		add(EntityRef.THINGS, "1");

	Assert.assertEquals("http://example.org/v1.1/Things('1')", request.compose());

	Assert.assertTrue(request.isQuoteIdentifiersSet());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-property-of-entity">Usage 4</a>
     */
    @Test
    public void testUsage4_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS, "1").//
		with(AddressingPathSegment.getProperty("resultTime"));

	Assert.assertEquals("resultTime", request.getEntityProperty().get().getName());

	Assert.assertFalse(request.isAddressAssociationLinkSet());

	Assert.assertEquals("http://example.org/v1.1/Observations(1)/resultTime", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-property-of-entity">Usage 4</a>
     */
    @Test
    public void testUsage4_2() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.FEATURES_OF_INTEREST, "2").//
		with(AddressingPathSegment.getProperty("name"));

	Assert.assertEquals("http://example.org/v1.1/FeaturesOfInterest(2)/name", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-value-of-property">Usage 5</a>
     */
    @Test
    public void testUsage5_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS, "1").//
		with(AddressingPathSegment.getProperty("resultTime").//
			setGetValue(true));

	Assert.assertEquals("http://example.org/v1.1/Observations(1)/resultTime/$value", request.compose());

	Assert.assertTrue(request.getEntityProperty().get().isGetValueSet());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-value-of-property">Usage 5</a>
     */
    @Test
    public void testUsage5_2() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.LOCATIONS, "2").//
		with(AddressingPathSegment.getProperty("description", true));

	Assert.assertEquals("http://example.org/v1.1/Locations(2)/description/$value", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-navigation-property">Usage 6</a>
     */
    @Test
    public void testUsage6_1() throws MalformedURLException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		add(EntityRef.OBSERVATIONS);

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)/Observations", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-navigation-property">Usage 6</a>
     */
    @Test
    public void testUsage6_2() throws MalformedURLException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.SENSORS).//
		add(EntityRef.OBSERVATIONS, "1");

	Assert.assertEquals("http://example.org/v1.1/Sensors/Observations(1)", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-associationlink">Usage 7</a>
     */
    @Test
    public void testUsage7_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		add(EntityRef.OBSERVATIONS).//
		with(AddressingPathSegment.ASSOCIATION_LINK);

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)/Observations/$ref", request.compose());

	Assert.assertTrue(request.isAddressAssociationLinkSet());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-address-navigation-property">Usage 7</a>
     */
    @Test
    public void testUsage7_2() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.SENSORS).//
		add(EntityRef.OBSERVATIONS, "1").//
		with(AddressingPathSegment.ASSOCIATION_LINK);

	Assert.assertEquals("http://example.org/v1.1/Sensors/Observations(1)/$ref", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-nested-resource-path">Usage 8</a>
     */
    @Test
    public void testUsage8_1() throws MalformedURLException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		add(EntityRef.OBSERVATIONS, "1");

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)/Observations(1)", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-nested-resource-path">Usage 8</a>
     */
    @Test
    public void testUsage8_2() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		add(EntityRef.OBSERVATIONS, "1").//
		with(AddressingPathSegment.getProperty().setName("resultTime"));

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)/Observations(1)/resultTime", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-nested-resource-path">Usage 8</a>
     */
    @Test
    public void testUsage8_3() throws MalformedURLException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		add(EntityRef.OBSERVATIONS, "1").//
		add(EntityRef.FEATURE_OF_INTEREST);

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)/Observations(1)/FeatureOfInterest", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#usage-nested-resource-path">Usage 8</a>
     */
    @Test
    public void testUsage8_4() throws MalformedURLException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		add(EntityRef.THING);

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)/Thing", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#expand">Example Request 1</a>
     */
    @Test
    public void testExpand_1() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(EntityRef.DATASTREAMS); //

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS).//
		with(//
			SystemQueryOptions.get().//
				expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Things?$expand=Datastreams", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#expand">Example Request 2</a>
     */
    @Test
    public void testExpand_2() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//
		EntityRef.DATASTREAMS, EntityRef.OBSERVED_PROPERTY); //

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS).//
		with(//
			SystemQueryOptions.get().//
				expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Things?$expand=Datastreams/ObservedProperty", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#expand">Example Request 3</a>
     */
    @Test
    public void testExpand_3() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//
		ExpandItem.get(EntityRef.OBSERVATIONS), //
		ExpandItem.get(EntityRef.OBSERVED_PROPERTY) //
	);

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)?$expand=Observations,ObservedProperty", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#expand">Example Request 4</a>
     */
    @Test
    public void testExpand_4() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//
		EntityRef.OBSERVATIONS, //
		Operation.FILTER, "result eq 1"); //

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)?$expand=Observations($filter=result+eq+1)", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#select4">Example Request 1</a>
     */
    @Test
    public void testSelect_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		with(SystemQueryOptions.get().//
			select(new SelectOption("result,resultTime")));

	Assert.assertEquals("http://example.org/v1.1/Observations?$select=result,resultTime", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#select4">Example Request 2</a>
     */
    @Test
    public void testSelect_2() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//
		EntityRef.OBSERVATIONS, EntityRef.FEATURE_OF_INTEREST); //

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		with(SystemQueryOptions.get().//
			select(new SelectOption("id")).//
			expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)?$select=id&$expand=Observations/FeatureOfInterest", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#select4">Example Request 3</a>
     */
    @Test
    public void testSelect_3() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//
		EntityRef.OBSERVATIONS, Operation.SELECT, "result"); //

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)?$expand=Observations($select=result)", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#orderby">Example Request 1</a>
     */
    @Test
    public void orderByTest_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		with(SystemQueryOptions.get().//
			orderBy("result"));

	Assert.assertEquals("http://example.org/v1.1/Observations?$orderby=result", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#orderby">Example Request 2</a>
     */
    @Test
    public void orderByTest_2() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		with(SystemQueryOptions.get().//
			expand(new ExpandOption(EntityRef.DATASTREAM)).//
			orderBy("Datastreams/id desc, phenomenonTime"));

	Assert.assertEquals("http://example.org/v1.1/Observations?$expand=Datastream&$orderby=Datastreams%2Fid+desc%2C+phenomenonTime",
		request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#top">Example Request 1</a>
     */
    @Test
    public void topTest_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			top(5));

	Assert.assertEquals("http://example.org/v1.1/Things?$top=5", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#top">Example Request 2</a>
     */
    @Test
    public void topTest_2() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		with(SystemQueryOptions.get().//
			top(5).//
			orderBy("phenomenonTime desc"));

	Assert.assertEquals("http://example.org/v1.1/Observations?$top=5&$orderby=phenomenonTime+desc", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#skip">Example Request 1</a>
     */
    @Test
    public void skipTest_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			skip(5));

	Assert.assertEquals("http://example.org/v1.1/Things?$skip=5", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#skip">Example Request 2</a>
     */
    @Test
    public void skipTest_2() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		with(SystemQueryOptions.get().//
			skip(2).//
			top(2).//
			orderBy("resultTime"));

	Assert.assertEquals("http://example.org/v1.1/Observations?$top=2&$skip=2&$orderby=resultTime", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#count">Example Request 1</a>
     */
    @Test
    public void countTest() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			count());

	Assert.assertEquals("http://example.org/v1.1/Things?$count=true", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#filter">Example Request 1</a>
     */
    @Test
    public void filterTest_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		with(SystemQueryOptions.get().//
			filter("result lt 10.00"));

	Assert.assertEquals("http://example.org/v1.1/Observations?$filter=result+lt+10.00", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#filter">Example Request 2</a>
     */
    @Test
    public void filterTest_2() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		with(SystemQueryOptions.get().//
			filter("Datastream/id eq 1"));

	Assert.assertEquals("http://example.org/v1.1/Observations?$filter=Datastream%2Fid+eq+1", request.compose());
    }

    /**
     * <a href="https://docs.ogc.org/is/18-088/18-088.html#filter">Example Request 3</a>
     */
    @Test
    public void filterTest_3() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			filter("geo.distance(Locations/location, geography'POINT(-122 43)') gt 1"));

	Assert.assertEquals(
		"http://example.org/v1.1/Things?$filter=geo.distance%28Locations%2Flocation%2C+geography%27POINT%28-122+43%29%27%29+gt+1",
		request.compose());
    }

    @Test
    public void extensionTest_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.of("Party")); //

	Assert.assertEquals("http://example.org/v1.1/Party", request.compose());
    }

    @Test
    public void extensionTest_2() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//
		ExpandItem.get(EntityRef.THING, EntityRef.of("Party")));

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		with(SystemQueryOptions.get()//
			.expand(expandOption)); //

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)?$expand=Thing/Party", request.compose());
    }

    /**
    * 
    */
    @Test
    public void testExpandFilterAndSelect() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//
		ExpandItem.get(EntityRef.OBSERVATIONS, Operation.FILTER, "result eq 1"),
		ExpandItem.get(EntityRef.OBSERVATIONS, Operation.SELECT, "result")); //

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.DATASTREAMS, "1").//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Datastreams(1)?$expand=Observations($filter=result+eq+1),Observations($select=result)",
		request.compose());
    }

    /**
     * 
     */
    @Test
    public void testExpandPropertiesAndExpandHierarchy() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//
		ExpandItem.get(EntityRef.DATASTREAMS), // navigation property
		ExpandItem.get(EntityRef.LOCATIONS), // navigation property
		ExpandItem.get(EntityRef.DATASTREAMS, EntityRef.OBSERVED_PROPERTY)); // navigation
										     // hierarchy

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS, "1").//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Things(1)?$expand=Datastreams,Locations,Datastreams/ObservedProperty",
		request.compose());
    }

    /**
     * 
     */
    @Test
    public void testExpandMultipleExpandHierarchy_1() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//

		ExpandItem.get(EntityRef.DATASTREAMS, EntityRef.OBSERVED_PROPERTY), // navigation
										    // hierarchy
		ExpandItem.get(EntityRef.LOCATIONS, EntityRef.HISTORICAL_LOCATIONS)// navigation
										   // hierarchy
	);

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Things?$expand=Datastreams/ObservedProperty,Locations/HistoricalLocations",
		request.compose());
    }

    /**
     * 
     */
    @Test
    public void testExpandMultipleExpandHierarchy_2() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//

		ExpandItem.get(EntityRef.DATASTREAMS, EntityRef.OBSERVED_PROPERTY), // navigation
										    // hierarchy
		ExpandItem.get(EntityRef.DATASTREAMS, EntityRef.SENSOR)// navigation hierarchy
	);

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Things?$expand=Datastreams/ObservedProperty,Datastreams/Sensor", request.compose());
    }

    /**
     * 
     */
    @Test
    public void testExpandMultipleExpandHierarchyAndSelect() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//

		// navigation hierarchy with select
		ExpandItem.get(EntityRef.DATASTREAMS, EntityRef.OBSERVED_PROPERTY, Operation.SELECT, "id"),
		// navigation hierarchy
		ExpandItem.get(EntityRef.DATASTREAMS, EntityRef.SENSOR));

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	Assert.assertEquals("http://example.org/v1.1/Things?$expand=Datastreams/ObservedProperty($select=id),Datastreams/Sensor",
		request.compose());

    }

    /**
     * 
     */
    @Test
    public void testExpandMultipleHierarchyAndSelectAndFilter() throws MalformedURLException {

	ExpandOption expandOption = new ExpandOption(//

		// navigation hierarchy with select
		ExpandItem.get(EntityRef.DATASTREAMS, EntityRef.OBSERVED_PROPERTY, Operation.SELECT, "id"),
		// navigation hierarchy with filter
		ExpandItem.get(EntityRef.DATASTREAMS, EntityRef.SENSOR, Operation.FILTER,
			"encodingType eq 'http://www.opengis.net/doc/IS/SensorML/2.0'"));

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			expand(expandOption));

	Assert.assertEquals(
		"http://example.org/v1.1/Things?$expand=Datastreams/ObservedProperty($select=id),Datastreams/Sensor($filter=encodingType+eq+%27http%3A%2F%2Fwww.opengis.net%2Fdoc%2FIS%2FSensorML%2F2.0%27)",
		request.compose());

    }

    /**
     * https://docs.ogc.org/is/18-088/18-088.html#_request_4
     */
    @Test
    public void dataArrayTest_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		setDataArrayResultFormat();

	Assert.assertEquals("http://example.org/v1.1/Observations?$resultFormat=dataArray", request.compose());
    }

    /**
     * https://docs.ogc.org/is/18-088/18-088.html#_request_4
     */
    @Test
    public void dataArrayTest_2() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		setDataArrayResultFormat().with(//
			SystemQueryOptions.get().count());

	Assert.assertEquals("http://example.org/v1.1/Observations?$count=true&$resultFormat=dataArray", request.compose());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void extendedAddressableEntityTest() throws IllegalArgumentException, GSException, IOException {

	FluentSensorThingsRequest request = FluentSensorThingsRequest.//
		get("https://citiobs.demo.secure-dimensions.de/staplus/v1.1/").//
		add(EntityRef.of("Parties"));

	Assert.assertEquals("https://citiobs.demo.secure-dimensions.de/staplus/v1.1/Parties", request.compose());
    }

    /**
     * @throws IllegalArgumentException
     * @throws GSException
     * @throws IOException
     */
    @Test
    public void expandExtendedAddressableEntityTest() throws IllegalArgumentException, GSException, IOException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("https://citiobs.demo.secure-dimensions.de/staplus/v1.1/").//
		add(EntityRef.THINGS).//
		with(SystemQueryOptions.get().//
			expand(new ExpandOption(EntityRef.of("Party"))));

	Assert.assertEquals("https://citiobs.demo.secure-dimensions.de/staplus/v1.1/Things?$expand=Party", request.compose());
    }

    /**
     * Addressing a property or a property value not allowed with an entitis collection
     */
    @Test
    public void notAllowedTest_1() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.FEATURES_OF_INTEREST).//
		with(AddressingPathSegment.getProperty("name"));

	try {
	    request.compose();
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals(
		    "Addressing a property or a property value not allowed with an entitis collection, entity identifier is required",
		    ex.getMessage());
	}
    }

    /**
     * At least one entity set must be provided to address an association link
     */
    @Test
    public void notAllowedTest_2() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		with(AddressingPathSegment.ASSOCIATION_LINK);//

	try {
	    request.compose();
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals("At least one entity set must be provided to address an association link", ex.getMessage());
	}
    }

    /**
     * The 'filter' system option is allowed only with an entitis collection
     */
    @Test
    public void notAllowedTest_3() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.FEATURES_OF_INTEREST, "1").//
		with(SystemQueryOptions.get().//
			filter("name eq xxx"));//

	try {
	    request.compose();
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals("The 'filter' system option is allowed only with an entitis collection", ex.getMessage());
	}
    }

    /**
     * "'dataArray' response format supported only for Observation entities collection"
     */
    @Test
    public void notAllowedTest_4() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.FEATURES_OF_INTEREST).//
		setDataArrayResultFormat();

	try {
	    request.compose();
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals("'dataArray' response format supported only for Observation entities collection", ex.getMessage());
	}
    }

    /**
     * "'dataArray' response format supported only for Observation entities collection"
     */
    @Test
    public void notAllowedTest_5() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS, "1").//
		setDataArrayResultFormat();

	try {
	    request.compose();
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals("'dataArray' response format supported only for Observation entities collection", ex.getMessage());
	}
    }

    /**
     * 'expand' system option not allowed with 'dataArray' response format
     */
    @Test
    public void notAllowedTest_6() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		setDataArrayResultFormat().//
		with(SystemQueryOptions.get().//
			expand(new ExpandOption(EntityRef.FEATURE_OF_INTEREST)));

	try {
	    request.compose();
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals("'expand' system option not allowed with 'dataArray' response format", ex.getMessage());
	}
    }

    /**
     * Addressing a property, a property value or an association link not allowed with 'dataArray' response format
     */
    @Test
    public void notAllowedTest_7() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		setDataArrayResultFormat().//
		with(AddressingPathSegment.ASSOCIATION_LINK);

	try {
	    request.compose();
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals(
		    "Addressing a property, a property value or an association link not allowed with 'dataArray' response format",
		    ex.getMessage());
	}
    }

    /**
     * Addressing a property, a property value or an association link not allowed with 'dataArray' response format
     */
    @Test
    public void notAllowedTest_8() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		setDataArrayResultFormat().//
		with(AddressingPathSegment.getProperty("result"));

	try {
	    request.compose();
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals(
		    "Addressing a property, a property value or an association link not allowed with 'dataArray' response format",
		    ex.getMessage());
	}
    }

    /**
     * Addressing a property, a property value or an association link not allowed with 'dataArray' response format
     */
    @Test
    public void notAllowedTest_9() throws MalformedURLException {

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get("http://example.org/v1.1/").//
		add(EntityRef.OBSERVATIONS).//
		setDataArrayResultFormat().//
		with(AddressingPathSegment.//
			getProperty("result").//
			setGetValue(true));

	try {
	    request.compose();
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals(
		    "Addressing a property, a property value or an association link not allowed with 'dataArray' response format",
		    ex.getMessage());
	}
    }
}
