package eu.essi_lab.accessor.hiscentral.sicilia;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;

/**
 * Stations with sub-locations (e.g. the dams): name and coordinates are in the "l" array, some sensors have their own
 * position
 */
public class HISCentralSiciliaConnectorTest {

    private static final String ARANCIO = "{ \"i\": 680900, \"o\": \"\", \"s\": 0, \"l\": ["
	    + "{ \"t\": 0, \"n\": \"Arancio Diga R2\", \"x\": 13.068333, \"y\": 37.6325, \"z\": 185, \"hwl\": true, \"hr\": true },"
	    + "{ \"t\": 1, \"n\": \"Arancio Diga R2 #2\", \"x\": 13.063944, \"y\": 37.625361, \"z\": 145, \"hwl\": true, \"hr\": false } ] }";

    private static final String RADAR = "{ \"elementId\": 50639, \"elementName\": \"Idrometro Radar\", \"stationId\": 680900,"
	    + " \"stationName\": \"Arancio Diga R2\", \"quantity\": \"Livello Idrometrico\", \"measUnit\": \"m\","
	    + " \"startDate\": \"2015-01-01T00:00:00\", \"endDate\": null, \"dtr\": 600 }";

    @Test
    public void flattenTest() {

	JSONObject station = HISCentralSiciliaConnector.flatten(new JSONObject(ARANCIO));

	Assert.assertEquals(680900, station.getInt("i"));
	Assert.assertEquals("Arancio Diga R2", station.getString("n"));
	Assert.assertEquals(13.068333, station.getDouble("x"), 1e-9);
	Assert.assertEquals(37.6325, station.getDouble("y"), 1e-9);
	Assert.assertEquals(185, station.getInt("z"));
	Assert.assertTrue(station.has(HISCentralSiciliaConnector.SUB_LOCATIONS));

	// plain stations are unchanged
	JSONObject plain = new JSONObject("{ \"i\": 776600, \"n\": \"Cammarata Stazione [X]\", \"x\": 13.668888, \"y\": 37.648055 }");
	Assert.assertSame(plain, HISCentralSiciliaConnector.flatten(plain));
    }

    @Test
    public void locateTest() {

	JSONObject station = HISCentralSiciliaConnector.flatten(new JSONObject(ARANCIO));

	// sensor at the station location
	JSONObject radar = new JSONObject("{ \"elementId\": 50639, \"longitude\": 13.068333, \"latitude\": 37.6325,"
		+ " \"altitude\": null, \"_strictElementLongitude\": null, \"_strictElementLatitude\": null }");
	Assert.assertSame(station, HISCentralSiciliaConnector.locate(station, radar));

	// downstream sensor with its own location and altitude
	JSONObject valle = new JSONObject("{ \"elementId\": 57367, \"longitude\": 13.063944, \"latitude\": 37.625361,"
		+ " \"altitude\": 145, \"_strictElementLongitude\": 13.063944, \"_strictElementLatitude\": 37.625361 }");
	JSONObject located = HISCentralSiciliaConnector.locate(station, valle);
	Assert.assertEquals(13.063944, located.getDouble("x"), 1e-9);
	Assert.assertEquals(37.625361, located.getDouble("y"), 1e-9);
	Assert.assertEquals(145, located.getInt("z"));
	Assert.assertEquals("Arancio Diga R2", located.getString("n"));

	// altitude taken from the matching sub-location
	valle.put("altitude", JSONObject.NULL);
	Assert.assertEquals(145, HISCentralSiciliaConnector.locate(station, valle).getInt("z"));
    }

    @Test
    public void mapperTest() throws Exception {

	JSONObject station = HISCentralSiciliaConnector.flatten(new JSONObject(ARANCIO));

	GSSource source = new GSSource();
	source.setEndpoint("https://www.protezionecivilesicilia.it:8443/DatascapeA/");

	GSResource resource = new HISCentralSiciliaMapper().map(HISCentralSiciliaMapper.create(station, new JSONObject(RADAR)), source);
	CoreMetadata core = resource.getHarmonizedMetadata().getCoreMetadata();

	Assert.assertEquals("Arancio Diga R2 - Idrometro Radar", core.getTitle());

	GeographicBoundingBox bbox = core.getBoundingBox();
	Assert.assertEquals(37.6325, bbox.getNorth(), 1e-6);
	Assert.assertEquals(13.068333, bbox.getEast(), 1e-6);
    }
}
