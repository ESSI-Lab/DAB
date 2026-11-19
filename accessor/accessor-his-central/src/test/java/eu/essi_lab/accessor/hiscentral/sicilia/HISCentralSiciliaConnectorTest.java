package eu.essi_lab.accessor.hiscentral.sicilia;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;

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
	source.setEndpoint("https://siciliadrpc-aegis.caedns.it/platform/");

	GSResource resource = new HISCentralSiciliaMapper().map(HISCentralSiciliaMapper.create(station, new JSONObject(RADAR)), source);
	CoreMetadata core = resource.getHarmonizedMetadata().getCoreMetadata();

	Assert.assertEquals("Arancio Diga R2 - Idrometro Radar", core.getTitle());

	GeographicBoundingBox bbox = core.getBoundingBox();
	Assert.assertEquals(37.6325, bbox.getNorth(), 1e-6);
	Assert.assertEquals(13.068333, bbox.getEast(), 1e-6);
	Assert.assertEquals(InterpolationType.CONTINUOUS, resource.getExtensionHandler().getTimeInterpolation().get());
    }

    @Test
    public void virtualTest() {

	Assert.assertTrue(HISCentralSiciliaConnector.isVirtual(new JSONObject("{ \"elementName\": \"Pioggia Raccolta in 1 Ora\", \"isVirtual\": true }")));
	Assert.assertFalse(HISCentralSiciliaConnector.isVirtual(new JSONObject("{ \"elementName\": \"Pioggia Cumulata\", \"isVirtual\": false }")));
	Assert.assertFalse(HISCentralSiciliaConnector.isVirtual(new JSONObject(RADAR)));
    }

    @Test
    public void noStartDateTest() throws Exception {

	// e.g. Linguaglossa Etna Nord [SIAS]: start and end dates are null
	JSONObject station = new JSONObject("{ \"i\": 1035400, \"n\": \"Linguaglossa Etna Nord [SIAS]\", \"x\": 15.07, \"y\": 37.81, \"s\": 0 }");
	JSONObject element = new JSONObject("{ \"elementId\": 79545, \"elementName\": \"Precipitazione totale\", \"stationId\": 1035400,"
		+ " \"quantity\": \"Incrementi di Pioggia\", \"measUnit\": \"mm\", \"startDate\": null, \"endDate\": null, \"dtr\": 300,"
		+ " \"isVirtual\": false }");

	// skipped by the harvesting
	Assert.assertFalse(HISCentralSiciliaConnector.hasStartDate(element));
	Assert.assertFalse(HISCentralSiciliaConnector.hasStartDate(new JSONObject("{ \"startDate\": \"\" }")));
	Assert.assertTrue(HISCentralSiciliaConnector.hasStartDate(new JSONObject(RADAR)));

	GSSource source = new GSSource();
	source.setEndpoint("https://siciliadrpc-aegis.caedns.it/platform/");

	GSResource resource = new HISCentralSiciliaMapper().map(HISCentralSiciliaMapper.create(station, element), source);
	// in any case the mapper keeps only the end position, the begin is unknown
	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	Assert.assertTrue(extent.isEndPositionIndeterminate());
	Assert.assertTrue(extent.getBeginPosition() == null || extent.getBeginPosition().isEmpty());
    }

    @Test
    public void observedPropertyTest() throws Exception {

	String base = "http://his-central-ontology.geodab.eu/hydro-ontology/concept/";

	Assert.assertEquals(base + "33", HISCentralSiciliaMapper.getObservedPropertyURI("Velocità Vento Vettoriale").get());
	Assert.assertEquals(base + "5327", HISCentralSiciliaMapper.getObservedPropertyURI("Velocità Vento Raffica").get());
	Assert.assertEquals(base + "68", HISCentralSiciliaMapper.getObservedPropertyURI("Direzione Vento Raffica").get());
	Assert.assertEquals(base + "5262", HISCentralSiciliaMapper.getObservedPropertyURI("Intensità di Pioggia").get());
	Assert.assertEquals(base + "65", HISCentralSiciliaMapper.getObservedPropertyURI("Incrementi di Pioggia").get());
	Assert.assertEquals(base + "49", HISCentralSiciliaMapper.getObservedPropertyURI("Temperatura Aria Minima").get());
	Assert.assertEquals(base + "2293", HISCentralSiciliaMapper.getObservedPropertyURI("pH dell’Acqua").get());
	Assert.assertFalse(HISCentralSiciliaMapper.getObservedPropertyURI("Resistività dell'Acqua").isPresent());

	// the concept is set by the mapping, the statistic is in the time interpolation
	JSONObject station = new JSONObject("{ \"i\": 776600, \"n\": \"Cammarata Stazione [X]\", \"x\": 13.66, \"y\": 37.64, \"s\": 0 }");
	JSONObject element = new JSONObject("{ \"elementId\": 1, \"elementName\": \"Temperatura Aria Minima\", \"stationId\": 776600,"
		+ " \"quantity\": \"Temperatura Aria Minima\", \"measUnit\": \"°C\", \"startDate\": \"2020-01-01T00:00:00\", \"endDate\": null,"
		+ " \"dtr\": 86400, \"isVirtual\": false }");

	GSSource source = new GSSource();
	source.setEndpoint("https://siciliadrpc-aegis.caedns.it/platform/");

	GSResource resource = new HISCentralSiciliaMapper().map(HISCentralSiciliaMapper.create(station, element), source);
	Assert.assertEquals(base + "49", resource.getExtensionHandler().getObservedPropertyURI().get());
	Assert.assertEquals(InterpolationType.MIN, resource.getExtensionHandler().getTimeInterpolation().get());
    }

    @Test
    public void activeTest() {

	Assert.assertTrue(HISCentralSiciliaConnector.isActive(new JSONObject("{ \"i\": 776600, \"n\": \"Cammarata Stazione [X]\", \"s\": 0 }")));
	Assert.assertFalse(HISCentralSiciliaConnector.isActive(new JSONObject("{ \"i\": 590600, \"n\": \"Lipari\", \"s\": 40 }")));
	Assert.assertTrue(HISCentralSiciliaConnector.isActive(new JSONObject("{ \"i\": 776600, \"n\": \"No state\" }")));

	// the state is kept by the flattening of multi-location stations
	Assert.assertTrue(HISCentralSiciliaConnector.isActive(HISCentralSiciliaConnector.flatten(new JSONObject(ARANCIO))));
	Assert.assertFalse(HISCentralSiciliaConnector.isActive(HISCentralSiciliaConnector.flatten(new JSONObject(ARANCIO.replace("\"s\": 0", "\"s\": 40")))));
    }

    @Test
    public void dataLinkageTest() {

	String endpoint = "https://siciliadrpc-aegis.caedns.it/platform/";

	Assert.assertEquals("https://siciliadrpc-aegis.caedns.it/platform/v3/data/", HISCentralSiciliaClient.getDataLinkagePrefix(endpoint));
	Assert.assertTrue(HISCentralSiciliaClient.isDataLinkage("https://siciliadrpc-aegis.caedns.it/platform/v3/data/54820", endpoint));
	// records harvested before the switch to /v3/data
	Assert.assertTrue(HISCentralSiciliaClient.isDataLinkage("https://siciliadrpc-aegis.caedns.it/platform/v3/data-combo/54820", endpoint));
	Assert.assertFalse(HISCentralSiciliaClient.isDataLinkage("https://other.host/platform/v3/data/54820", endpoint));
    }

    @Test
    public void statisticTest() throws Exception {

	Assert.assertEquals(InterpolationType.MIN, HISCentralSiciliaMapper.getStatistic("Termometro aria Min").get());
	Assert.assertEquals(InterpolationType.MAX, HISCentralSiciliaMapper.getStatistic("Termometro aria Max").get());
	Assert.assertEquals(InterpolationType.AVERAGE, HISCentralSiciliaMapper.getStatistic("Termometro aria media").get());
	Assert.assertEquals(InterpolationType.MIN, HISCentralSiciliaMapper.getStatistic("Temperatura Aria Minima").get());
	Assert.assertEquals(InterpolationType.MAX, HISCentralSiciliaMapper.getStatistic("Velocita' vento massima").get());
	Assert.assertFalse(HISCentralSiciliaMapper.getStatistic("Temperatura Aria").isPresent());
	Assert.assertFalse(HISCentralSiciliaMapper.getStatistic("Intensità di Pioggia").isPresent());
	Assert.assertFalse(HISCentralSiciliaMapper.getStatistic("Idrometro valle diga").isPresent());

	// Lipari: same quantity, different statistics
	JSONObject lipari = new JSONObject("{ \"i\": 590600, \"n\": \"Lipari\", \"x\": 14.931666, \"y\": 38.485555, \"z\": 89 }");
	JSONObject min = new JSONObject("{ \"elementId\": 54820, \"elementName\": \"Termometro aria Min\", \"stationId\": 590600,"
		+ " \"quantity\": \"Temperatura Aria\", \"measUnit\": \"°C\", \"startDate\": \"2016-01-01T00:00:00\", \"endDate\": null,"
		+ " \"dtr\": 0, \"isVirtual\": false }");

	GSSource source = new GSSource();
	source.setEndpoint("https://siciliadrpc-aegis.caedns.it/platform/");

	GSResource resource = new HISCentralSiciliaMapper().map(HISCentralSiciliaMapper.create(lipari, min), source);
	Assert.assertEquals("Lipari - Termometro aria Min", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	Assert.assertEquals(InterpolationType.MIN, resource.getExtensionHandler().getTimeInterpolation().get());
    }
}
