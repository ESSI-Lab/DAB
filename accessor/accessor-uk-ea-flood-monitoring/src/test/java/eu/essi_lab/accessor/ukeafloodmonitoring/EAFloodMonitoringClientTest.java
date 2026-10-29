package eu.essi_lab.accessor.ukeafloodmonitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class EAFloodMonitoringClientTest {

    static final String STATION = "{\"@id\":\"http://environment.data.gov.uk/flood-monitoring/id/stations/1029TH\",\"RLOIid\":\"7041\","
	    + "\"catchmentName\":\"Cotswolds\",\"dateOpened\":\"1994-01-01\",\"easting\":417990,\"label\":\"Bourton Dickler\","
	    + "\"lat\":51.874767,\"long\":-1.740083,\"northing\":219610,\"notation\":\"1029TH\",\"riverName\":\"River Dikler\","
	    + "\"stageScale\":\"http://environment.data.gov.uk/flood-monitoring/id/stations/1029TH/stageScale\","
	    + "\"stationReference\":\"1029TH\","
	    + "\"status\":\"http://environment.data.gov.uk/flood-monitoring/def/core/statusActive\","
	    + "\"town\":\"Little Rissington\",\"wiskiID\":\"1029TH\"}";

    static final String MEASURE = "{\"@id\":\"http://environment.data.gov.uk/flood-monitoring/id/measures/1029TH-level-stage-i-15_min-mASD\","
	    + "\"datumType\":\"http://environment.data.gov.uk/flood-monitoring/def/core/datumASD\","
	    + "\"label\":\"RIVER DIKLER AT BOURTON ON THE WATER - level-stage-i-15_min-mASD\","
	    + "\"latestReading\":{\"@id\":\"http://environment.data.gov.uk/flood-monitoring/data/readings/1029TH-level-stage-i-15_min-mASD/2026-10-27T14-00-00Z\","
	    + "\"date\":\"2026-10-27\",\"dateTime\":\"2026-10-27T14:00:00Z\","
	    + "\"measure\":\"http://environment.data.gov.uk/flood-monitoring/id/measures/1029TH-level-stage-i-15_min-mASD\","
	    + "\"value\":0.086},\"notation\":\"1029TH-level-stage-i-15_min-mASD\",\"parameter\":\"level\","
	    + "\"parameterName\":\"Water Level\",\"period\":900,\"qualifier\":\"Stage\","
	    + "\"station\":\"http://environment.data.gov.uk/flood-monitoring/id/stations/1029TH\","
	    + "\"stationReference\":\"1029TH\",\"unit\":\"http://qudt.org/1.1/vocab/unit#Meter\",\"unitName\":\"mASD\","
	    + "\"valueType\":\"instantaneous\"}";

    static final String GROUNDWATER_STATION = "{\"@id\":\"http://environment.data.gov.uk/flood-monitoring/id/stations/E9030\",\"gridReference\":\"TQ3910\","
	    + "\"label\":\"HOUNDEAN BOTTOM GWL\",\"notation\":\"E9030\",\"stationReference\":\"E9030\"}";

    static final String READINGS = "{\"items\":[{\"@id\":\"http://environment.data.gov.uk/flood-monitoring/data/readings/1029TH-level-stage-i-15_min-mASD/2026-10-27T14-00-00Z\","
	    + "\"dateTime\":\"2026-10-27T14:00:00Z\","
	    + "\"measure\":\"http://environment.data.gov.uk/flood-monitoring/id/measures/1029TH-level-stage-i-15_min-mASD\","
	    + "\"value\":0.086},"
	    + "{\"@id\":\"http://environment.data.gov.uk/flood-monitoring/data/readings/1029TH-level-stage-i-15_min-mASD/2026-10-27T13-45-00Z\","
	    + "\"dateTime\":\"2026-10-27T13:45:00Z\","
	    + "\"measure\":\"http://environment.data.gov.uk/flood-monitoring/id/measures/1029TH-level-stage-i-15_min-mASD\","
	    + "\"value\":0.093},"
	    + "{\"@id\":\"http://environment.data.gov.uk/flood-monitoring/data/readings/1029TH-level-stage-i-15_min-mASD/2026-10-27T13-30-00Z\","
	    + "\"dateTime\":\"2026-10-27T13:30:00Z\","
	    + "\"measure\":\"http://environment.data.gov.uk/flood-monitoring/id/measures/1029TH-level-stage-i-15_min-mASD\","
	    + "\"value\":0.094},{\"dateTime\":\"2026-10-27T13:15:00Z\",\"value\":[0.1,0.2]},"
	    + "{\"dateTime\":\"2026-10-27T13:00:00Z\"}]}";

    static final Date NOW = date("2026-10-27T15:00:00Z");

    /**
     * @return
     */
    static EAFloodMonitoringEntity createWaterLevelSeries() {

	return EAFloodMonitoringClient.createSeries(new JSONObject(MEASURE), new JSONObject(STATION), NOW).get();
    }

    @Test
    public void createSeriesTest() {

	EAFloodMonitoringEntity series = createWaterLevelSeries();

	assertEquals("1029TH-level-stage-i-15_min-mASD", series.getSeriesId());
	assertEquals("1029TH", series.getStationReference());
	assertEquals("Bourton Dickler", series.getStationLabel());
	assertEquals("level", series.getParameter());
	assertEquals("Stage", series.getQualifier());
	assertEquals("mASD", series.getUnit());
	assertEquals(900, series.getPeriod().get().intValue());
	assertEquals("Active", series.getString(EAFloodMonitoringClient.STATION_STATUS));
	assertEquals("River Dikler", series.getString(EAFloodMonitoringClient.RIVER_NAME));
	assertEquals("51.874767", series.getString(EAFloodMonitoringClient.LAT));

	// rolling window
	assertEquals("2026-09-29T00:00:00Z", series.getString(EAFloodMonitoringClient.BEGIN));
	assertEquals("2026-10-27T14:00:00Z", series.getString(EAFloodMonitoringClient.END));

	// measures without readings, or with readings older than the rolling window, are discarded
	JSONObject noReadings = new JSONObject(MEASURE);
	noReadings.remove(EAFloodMonitoringClient.LATEST_READING);
	assertFalse(EAFloodMonitoringClient.createSeries(noReadings, null, NOW).isPresent());

	JSONObject old = new JSONObject(MEASURE);
	old.getJSONObject(EAFloodMonitoringClient.LATEST_READING).put(EAFloodMonitoringClient.DATE_TIME, "2026-09-01T00:00:00Z");
	assertFalse(EAFloodMonitoringClient.createSeries(old, null, NOW).isPresent());
    }

    @Test
    public void stationArraysTest() {

	// stations made of more sites have array properties
	JSONObject station = new JSONObject(STATION);
	station.put("label", new JSONArray(Arrays.asList("Ilfracombe Lambda", "Ilfracombe Lambda Upstream")));
	station.put("lat", new JSONArray(Arrays.asList(51.19557, 51.196412)));
	station.put("long", new JSONArray(Arrays.asList(-4.119951, -4.120133)));

	EAFloodMonitoringEntity series = EAFloodMonitoringClient.createSeries(new JSONObject(MEASURE), station, NOW).get();
	assertEquals("Ilfracombe Lambda", series.getStationLabel());
	assertEquals("51.19557", series.getString(EAFloodMonitoringClient.LAT));
	assertEquals("-4.119951", series.getString(EAFloodMonitoringClient.LON));
    }

    @Test
    public void gridReferenceStationTest() {

	EAFloodMonitoringEntity series = EAFloodMonitoringClient
		.createSeries(new JSONObject(MEASURE), new JSONObject(GROUNDWATER_STATION), NOW).get();

	assertTrue(series.getObject().getBoolean(EAFloodMonitoringClient.COORDINATES_FROM_GRID_REFERENCE));
	// TQ3910 is the 1 km square south east of Lewes
	double lat = series.getObject().getDouble(EAFloodMonitoringClient.LAT);
	double lon = series.getObject().getDouble(EAFloodMonitoringClient.LON);
	assertEquals(50.877, lat, 0.001);
	assertEquals(-0.0185, lon, 0.001);
    }

    @Test
    public void gridReferenceTest() {

	double[] en = EAFloodMonitoringGridReference.toEastingNorthing("SY 5876090850").get();
	assertEquals(358760.5, en[0], 0.001);
	assertEquals(90850.5, en[1], 0.001);

	en = EAFloodMonitoringGridReference.toEastingNorthing("TQ3910").get();
	assertEquals(539500, en[0], 0.001);
	assertEquals(110500, en[1], 0.001);

	// station 44239 (Kingston Russell): easting 358760, northing 90850, lat 50.715805, long -2.5855
	double[] latLon = EAFloodMonitoringGridReference.toLatLon(358760, 90850);
	assertEquals(50.715805, latLon[0], 0.0001);
	assertEquals(-2.5855, latLon[1], 0.0001);

	// station 1029TH (Bourton Dickler): easting 417990, northing 219610, lat 51.874767, long -1.740083
	latLon = EAFloodMonitoringGridReference.toLatLon(417990, 219610);
	assertEquals(51.874767, latLon[0], 0.0001);
	assertEquals(-1.740083, latLon[1], 0.0001);

	assertFalse(EAFloodMonitoringGridReference.toLatLon("XX1234").isPresent());
	assertFalse(EAFloodMonitoringGridReference.toLatLon("TQ391").isPresent());
	assertFalse(EAFloodMonitoringGridReference.toLatLon(null).isPresent());
    }

    @Test
    public void matchesTest() {

	JSONObject measure = new JSONObject(MEASURE);

	assertTrue(EAFloodMonitoringClient.matches(Arrays.asList("level"), measure));
	assertTrue(EAFloodMonitoringClient.matches(Arrays.asList("flow", "level:stage"), measure));
	assertFalse(EAFloodMonitoringClient.matches(Arrays.asList("level:Groundwater"), measure));
	assertFalse(EAFloodMonitoringClient.matches(Arrays.asList("flow", "temperature:Water"), measure));
    }

    @Test
    public void parseReadingsTest() {

	TreeMap<Date, BigDecimal> values = new TreeMap<>();
	EAFloodMonitoringClient.parseReadings(EAFloodMonitoringClient.getItems(READINGS), date("2026-10-27T13:00:00Z"),
		date("2026-10-27T13:50:00Z"), values);

	// readings out of the period, without values or with more values are discarded
	assertEquals(2, values.size());
	assertEquals(date("2026-10-27T13:30:00Z"), values.firstKey());
	assertEquals(new BigDecimal("0.093"), values.lastEntry().getValue());
    }

    @Test
    public void itemsTest() {

	// single item view
	assertEquals(1, EAFloodMonitoringClient.getItems("{\"items\":" + MEASURE + "}").length());
	assertEquals(5, EAFloodMonitoringClient.getItems(READINGS).length());
	assertEquals(0, EAFloodMonitoringClient.getItems("{\"meta\":{}}").length());
    }

    @Test
    public void windowTest() {

	assertEquals(date("2026-09-29T00:00:00Z"), EAFloodMonitoringClient.getWindowBegin(NOW));
	assertEquals(date("2026-09-29T00:00:00Z"), EAFloodMonitoringClient.getWindowBegin(date("2026-10-27T00:00:00Z")));
    }

    @Test
    public void settingTest() {

	EAFloodMonitoringConnectorSetting setting = new EAFloodMonitoringConnectorSetting();
	assertEquals(Arrays.asList("level", "flow", "temperature:Water"), setting.getParameters());
	assertTrue(setting.getStations().isEmpty());

	setting.setParameters(" level, rainfall ,level");
	setting.setStations("1029TH, E9030");
	List<String> parameters = setting.getParameters();
	assertEquals(Arrays.asList("level", "rainfall"), parameters);
	assertEquals(Arrays.asList("1029TH", "E9030"), setting.getStations());

	// the setting survives serialization
	EAFloodMonitoringConnectorSetting copy = new EAFloodMonitoringConnectorSetting(setting.getObject().toString());
	assertEquals(parameters, copy.getParameters());
    }

    /**
     * @param text
     * @return
     */
    static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
