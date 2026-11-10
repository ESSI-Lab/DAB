package eu.essi_lab.gssrv.conf.task.collection;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * Tests the time resolutions of the WCMP2 records
 *
 * @author boldrini
 */
public class WCMP2TimeResolutionTest {

    @Test
    public void storedResolutionsTest() throws Exception {

	DatasetCollection collection = new DatasetCollection();
	collection.getExtensionHandler().addWISTimeResolution("PT1H");
	collection.getExtensionHandler().addWISTimeResolution("P1D");
	collection.getExtensionHandler().addWISTimeResolution("P1M");

	// the resolutions keep their order once the collection is stored and read back
	GSResource restored = GSResource.create(collection.asDocument(true));
	Assert.assertEquals(Arrays.asList("PT1H", "P1D", "P1M"), restored.getExtensionHandler().getWISTimeResolutions());

	// the same holds for the data topics
	collection.getExtensionHandler().addWISDataTopic("topic/a");
	collection.getExtensionHandler().addWISDataTopic("topic/b");
	restored = GSResource.create(collection.asDocument(true));
	Assert.assertEquals(Arrays.asList("topic/a", "topic/b"), restored.getExtensionHandler().getWISDataTopics());
    }

    @Test
    public void storedObservedPropertyTest() throws Exception {

	// a record for the whole source: no constant observed property
	DatasetCollection collection = new DatasetCollection();
	Assert.assertTrue(GSResource.create(collection.asDocument(true)).getExtensionHandler().getWISObservedProperty().isEmpty());

	// a record for each observed property: the exact URI is kept, also with other observed property URIs (e.g. the
	// matching WMO concepts) added to the record
	String uri = "http://hydro.geodab.eu/hydro-ontology/concept/11";
	collection.getExtensionHandler().setObservedPropertyURI("http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/171");
	collection.getExtensionHandler().setWISObservedProperty(uri);
	collection.getExtensionHandler().setWISObservedProperty("http://hydro.geodab.eu/hydro-ontology/concept/12");

	GSResource restored = GSResource.create(collection.asDocument(true));
	Assert.assertEquals(uri, restored.getExtensionHandler().getWISObservedProperty().get());
    }

    @Test
    public void encodingTest() {

	// the finest resolution in time, the second finest in the additional temporal extent
	JSONObject feature = new JSONObject();
	WISUtils.addTimeInterval(feature, "2020-01-01T00:00:00Z", "2026-11-01T00:00:00Z");
	WISUtils.addTimeResolutions(feature, Arrays.asList("PT1H", "P1D", "P1M"));

	System.out.println(feature.toString(3));

	JSONObject time = feature.getJSONObject("time");
	Assert.assertEquals("PT1H", time.getString("resolution"));
	Assert.assertEquals(2, time.getJSONArray("interval").length());

	JSONObject temporal = feature.getJSONObject("additionalExtents").getJSONObject("temporal");
	Assert.assertEquals("P1D", temporal.getString("resolution"));
	Assert.assertEquals("https://www.opengis.net/def/uom/ISO-8601/0/Gregorian", temporal.getString("trs"));
	JSONArray interval = temporal.getJSONArray("interval");
	Assert.assertEquals(1, interval.length());
	Assert.assertEquals("2020-01-01T00:00:00Z", interval.getJSONArray(0).getString(0));
	Assert.assertEquals("2026-11-01T00:00:00Z", interval.getJSONArray(0).getString(1));

	// a single resolution: no additional extents
	feature = new JSONObject();
	WISUtils.addTimeInterval(feature, "2020-01-01T00:00:00Z", "2026-11-01T00:00:00Z");
	WISUtils.addTimeResolutions(feature, List.of("P1D"));
	Assert.assertEquals("P1D", feature.getJSONObject("time").getString("resolution"));
	Assert.assertFalse(feature.has("additionalExtents"));

	// no resolutions: time unchanged
	feature = new JSONObject();
	WISUtils.addTimeInterval(feature, "2020-01-01T00:00:00Z", "2026-11-01T00:00:00Z");
	WISUtils.addTimeResolutions(feature, List.of());
	Assert.assertFalse(feature.getJSONObject("time").has("resolution"));
	Assert.assertFalse(feature.has("additionalExtents"));
    }
}
