package eu.essi_lab.accessor.ltlhmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
public class LHMTClientTest {

    static final String STATION = "{\"code\":\"nemajunu-vms\",\"name\":\"Nemajūnų VMS\",\"waterBody\":\"Nemunas\","
	    + "\"coordinates\":{\"latitude\":54.554227,\"longitude\":24.072006}}";

    static final String MEASURED = "[{\"observationTimeUtc\":\"2026-10-26 11:00:00\",\"waterLevel\":25.6,\"waterTemperature\":9.7},"
	    + "{\"observationTimeUtc\":\"2026-10-26 12:00:00\",\"waterLevel\":25.9,\"waterTemperature\":null},"
	    + "{\"observationTimeUtc\":\"2026-10-26 13:00:00\",\"waterLevel\":null,\"waterTemperature\":9.8}]";

    static final String HISTORICAL = "[{\"observationDateUtc\":\"2024-12-01\",\"waterLevel\":501,\"waterDischarge\":null},"
	    + "{\"observationDateUtc\":\"2024-12-02\",\"waterLevel\":495,\"waterDischarge\":null}]";

    /**
     * @param parameter
     * @return
     */
    static LHMTEntity createSeries(LHMTParameter parameter) {

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2000-01-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2024-12-31T00:00:00Z").get();

	return LHMTClient.createSeries(new JSONObject(STATION), parameter, begin, end);
    }

    @Test
    public void createSeriesTest() {

	LHMTEntity series = createSeries(LHMTParameter.DISCHARGE_DAILY);

	assertEquals("nemajunu-vms|DISCHARGE_DAILY", series.getSeriesId());
	assertEquals("nemajunu-vms", series.getStationCode());
	assertEquals("Nemajūnų VMS", series.getStationName());
	assertEquals("Nemunas", series.getWaterBody());
	assertEquals(LHMTParameter.DISCHARGE_DAILY, series.getParameter().get());
	assertEquals(54.554227, series.getObject().getDouble(LHMTClient.LAT), 0);
	assertEquals("2000-01-01T00:00:00Z", series.getObject().getString(LHMTClient.BEGIN));
	assertEquals("2024-12-31T00:00:00Z", series.getObject().getString(LHMTClient.END));
    }

    @Test
    public void seriesIdTest() {

	LHMTEntity series = LHMTClient.parseSeriesId("nemajunu-vms|WATER_LEVEL");
	assertEquals("nemajunu-vms", series.getStationCode());
	assertEquals(LHMTParameter.WATER_LEVEL, series.getParameter().get());

	assertNull(LHMTClient.parseSeriesId("nemajunu-vms|UNKNOWN"));
	assertNull(LHMTClient.parseSeriesId("nemajunu-vms"));
	assertNull(LHMTClient.parseSeriesId(null));
    }

    @Test
    public void parseDateTest() {

	assertEquals(ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-26T11:00:00Z").get(),
		LHMTClient.parseDate("2026-10-26 11:00:00").get());
	assertEquals(ISO8601DateTimeUtils.parseISO8601ToDate("2024-12-31T00:00:00Z").get(), LHMTClient.parseDate("2024-12-31").get());
	assertFalse(LHMTClient.parseDate(null).isPresent());
	assertFalse(LHMTClient.parseDate("null").isPresent());
    }

    @Test
    public void parseMeasuredTest() {

	JSONArray observations = new JSONArray(MEASURED);

	TreeMap<Date, BigDecimal> level = LHMTClient.parseObservations(observations, LHMTParameter.WATER_LEVEL);
	assertEquals(2, level.size());
	assertEquals(new BigDecimal("25.6"), level.firstEntry().getValue());
	assertEquals(LHMTClient.parseDate("2026-10-26 12:00:00").get(), level.lastKey());

	TreeMap<Date, BigDecimal> temperature = LHMTClient.parseObservations(observations, LHMTParameter.WATER_TEMPERATURE);
	assertEquals(2, temperature.size());
	assertEquals(new BigDecimal("9.8"), temperature.lastEntry().getValue());
    }

    @Test
    public void parseHistoricalTest() {

	JSONArray observations = new JSONArray(HISTORICAL);

	TreeMap<Date, BigDecimal> level = LHMTClient.parseObservations(observations, LHMTParameter.WATER_LEVEL_DAILY);
	assertEquals(2, level.size());
	assertEquals(LHMTClient.parseDate("2024-12-01").get(), level.firstKey());
	assertEquals(0, new BigDecimal("495").compareTo(level.lastEntry().getValue()));

	assertTrue(LHMTClient.parseObservations(observations, LHMTParameter.DISCHARGE_DAILY).isEmpty());
	assertTrue(LHMTClient.parseObservations(null, LHMTParameter.DISCHARGE_DAILY).isEmpty());
    }

    @Test
    public void hasSeriesTest() {

	JSONArray observations = new JSONArray(MEASURED);
	JSONArray iceCover = new JSONArray("[{\"observationTimeUtc\":\"2026-01-15 11:00:00\",\"waterLevel\":120,\"waterTemperature\":null}]");
	List<String> withoutTemperature = Arrays.asList("smalininku-vms");

	assertTrue(LHMTClient.hasSeries("nemajunu-vms", LHMTParameter.WATER_TEMPERATURE, observations, withoutTemperature));
	assertTrue(LHMTClient.hasSeries("smalininku-vms", LHMTParameter.WATER_TEMPERATURE, observations, withoutTemperature));

	// the water temperature series is kept with ice cover, unless the station does not measure it
	assertTrue(LHMTClient.hasSeries("nemajunu-vms", LHMTParameter.WATER_TEMPERATURE, iceCover, withoutTemperature));
	assertFalse(LHMTClient.hasSeries("smalininku-vms", LHMTParameter.WATER_TEMPERATURE, iceCover, withoutTemperature));
	assertTrue(LHMTClient.hasSeries("smalininku-vms", LHMTParameter.WATER_LEVEL, iceCover, withoutTemperature));

	// other parameters require recent values
	JSONArray historical = new JSONArray(HISTORICAL);
	assertTrue(LHMTClient.hasSeries("birstono-vms", LHMTParameter.WATER_LEVEL_DAILY, historical, withoutTemperature));
	assertFalse(LHMTClient.hasSeries("birstono-vms", LHMTParameter.DISCHARGE_DAILY, historical, withoutTemperature));
    }

    @Test
    public void settingTest() {

	LHMTConnectorSetting setting = new LHMTConnectorSetting();
	assertEquals(LHMTParameter.values().length, setting.getParameters().size());
	assertTrue(setting.getStations().isEmpty());
	assertEquals(LHMTClient.DEFAULT_STATIONS_WITHOUT_TEMPERATURE, setting.getStationsWithoutTemperature());

	setting.setParameters(" water_level, DISCHARGE_DAILY,unknown,WATER_LEVEL ");
	setting.setStations("nemajunu-vms, birstono-vms");
	setting.setStationsWithoutTemperature("");
	assertTrue(setting.getStationsWithoutTemperature().isEmpty());

	assertEquals(2, setting.getParameters().size());
	assertEquals(LHMTParameter.WATER_LEVEL, setting.getParameters().get(0));
	assertEquals(2, setting.getStations().size());
	assertEquals("birstono-vms", setting.getStations().get(1));
    }
}
