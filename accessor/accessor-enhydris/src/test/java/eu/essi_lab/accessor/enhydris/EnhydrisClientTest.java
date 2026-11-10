package eu.essi_lab.accessor.enhydris;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class EnhydrisClientTest {

    static final String STATION = "{\"id\":2022,\"last_update\":\"2026-10-27T12:00:00Z\",\"name\":\"Ευρώτας - Eurotas\",\"code\":\"\","
	    + "\"remarks\":\"HYDRONET station, National Observatory of Athens\",\"geom\":\"SRID=4326;POINT (22.399872 37.130856)\","
	    + "\"display_timezone\":\"Etc/GMT\",\"altitude\":224.0,\"start_date\":null,\"end_date\":null,\"overseer\":\"\",\"owner\":17}";

    static final String GROUP = "{\"id\":1244,\"name\":\"Mean stage\",\"hidden\":false,\"precision\":2,\"remarks\":\"\","
	    + "\"gentity\":2022,\"variable\":14,\"unit_of_measurement\":6}";

    static final String TIMESERIES = "{\"id\":10846,\"type\":\"Initial\",\"time_step\":\"3h\",\"name\":\"\","
	    + "\"publicly_available\":true,\"timeseries_group\":1244}";

    static final String VARIABLE = "{\"id\":14,\"translations\":{\"el\":{\"descr\":\"Στάθμη\"},\"en\":{\"descr\":\"Stage\"}}}";

    static final String UNIT = "{\"id\":6,\"descr\":\"Meters [Μέτρα]\",\"symbol\":\"m\",\"variables\":[]}";

    static final String ORGANIZATION = "{\"id\":17,\"name\":\"Εθνικό Αστεροσκοπείο Αθηνών\",\"acronym\":\"ΕΑΑ\"}";

    static final String DATA = "2026-10-26 00:00,0.84,MISSING2\n" //
	    + "2026-10-26 03:00,,\n" //
	    + "2026-10-26 06:00,0.82,\r\n" //
	    + "2026-10-26 09:00,0.81,MISSING2\n";

    /**
     * @return
     */
    static EnhydrisEntity createSeries() {

	Map<Integer, JSONObject> variables = new HashMap<>();
	variables.put(14, new JSONObject(VARIABLE));

	Map<Integer, JSONObject> units = new HashMap<>();
	units.put(6, new JSONObject(UNIT));

	Map<Integer, JSONObject> organizations = new HashMap<>();
	organizations.put(17, new JSONObject(ORGANIZATION));

	return EnhydrisClient.createSeries(//
		new JSONObject(STATION), //
		new JSONObject(GROUP), //
		new JSONObject(TIMESERIES), //
		variables, //
		units, //
		organizations, //
		ISO8601DateTimeUtils.parseISO8601ToDate("2019-11-12T18:00:00Z").get(), //
		ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-27T12:00:00Z").get());
    }

    @Test
    public void createSeriesTest() {

	EnhydrisEntity series = createSeries();

	assertEquals("2022/1244/10846", series.getSeriesId());
	assertEquals("Ευρώτας - Eurotas", series.getStationName());
	assertEquals("Mean stage", series.getGroupName());
	assertEquals("Stage", series.getVariable());
	assertEquals("m", series.getUnit());
	assertEquals("Meters", series.getString(EnhydrisClient.UNIT_NAME));
	assertEquals("Initial", series.getTimeseriesType());
	assertEquals("3h", series.getTimeStep());
	assertEquals("", series.getTimeseriesName());
	assertEquals("Εθνικό Αστεροσκοπείο Αθηνών", series.getOwner());
	assertEquals("ΕΑΑ", series.getString(EnhydrisClient.OWNER_ACRONYM));
	assertEquals("", series.getString(EnhydrisClient.STATION_CODE));
	assertEquals(new BigDecimal("37.130856"), series.getObject().getBigDecimal(EnhydrisClient.LAT));
	assertEquals(new BigDecimal("22.399872"), series.getObject().getBigDecimal(EnhydrisClient.LON));
	assertEquals("2019-11-12T18:00:00Z", series.getString(EnhydrisClient.BEGIN));
    }

    @Test
    public void parseRecordsTest() {

	TreeMap<Date, BigDecimal> values = EnhydrisClient.parseRecords(DATA);

	assertEquals(3, values.size());
	assertEquals(ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-26T00:00:00Z").get(), values.firstKey());
	assertEquals(new BigDecimal("0.82"), values.get(ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-26T06:00:00Z").get()));

	// HTS header lines are ignored
	assertTrue(EnhydrisClient.parseRecords("Unit=m\nTimezone=+0000\n\n").isEmpty());

	assertEquals(ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-25T16:00:00Z").get(),
		EnhydrisClient.parseTimestamp("2026-10-25 16:00,0.0,").get());
	assertFalse(EnhydrisClient.parseTimestamp("Unit=m").isPresent());
    }

    @Test
    public void parseChartTest() {

	assertEquals(new Date(1575228006000l),
		EnhydrisClient.parseFirstBinEnd("[{\"timestamp\":1574130402,\"min\":0.0},{\"timestamp\":1575228006,\"min\":0.0}]").get());
	assertEquals(new Date(1574130402000l), EnhydrisClient.parseFirstBinEnd("[{\"timestamp\":1574130402,\"min\":0.0}]").get());
	assertFalse(EnhydrisClient.parseFirstBinEnd("[]").isPresent());
    }

    @Test
    public void parsePointTest() {

	assertEquals(new BigDecimal("20.975265"), EnhydrisClient.parsePoint("SRID=4326;POINT (20.975265 39.15104)").get().getKey());
	assertEquals(new BigDecimal("39.15104"), EnhydrisClient.parsePoint("POINT(20.975265 39.15104)").get().getValue());
	assertFalse(EnhydrisClient.parsePoint("SRID=2100;POINT (300000 4300000)").isPresent());
	assertFalse(EnhydrisClient.parsePoint("").isPresent());
    }

    @Test
    public void seriesIdTest() {

	EnhydrisEntity series = EnhydrisClient.parseSeriesId("2022/1244/10846");
	assertEquals(2022, series.getStationId());
	assertEquals(1244, series.getGroupId());
	assertEquals(10846, series.getTimeseriesId());

	assertNull(EnhydrisClient.parseSeriesId("2022/1244"));
	assertNull(EnhydrisClient.parseSeriesId("a/b/c"));
	assertNull(EnhydrisClient.parseSeriesId(null));
    }

    @Test
    public void endpointTest() {

	assertEquals("https://system.openhi.net/api/", EnhydrisClient.normalizeEndpoint("https://system.openhi.net"));
	assertEquals("https://system.openhi.net/api/", EnhydrisClient.normalizeEndpoint("https://system.openhi.net/api"));
	assertEquals("https://system.openhi.net/api/", EnhydrisClient.normalizeEndpoint("https://system.openhi.net/api/"));
	assertEquals(EnhydrisClient.DEFAULT_ENDPOINT, EnhydrisClient.normalizeEndpoint(null));

	assertEquals("2026-10-25T13:30:00Z",
		EnhydrisClient.formatQueryDate(ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-25T13:30:00Z").get()));
    }
}
