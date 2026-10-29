package eu.essi_lab.accessor.ththaiwater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class ThaiWaterClientTest {

    /**
     * Entry of public/waterlevel_load (some fields removed)
     */
    static final String TELE_WATERLEVEL_ENTRY = "{\"id\":1332076181,\"waterlevel_datetime\":\"2026-10-28 01:00\",\"waterlevel_m\":null,"
	    + "\"waterlevel_msl\":\"3.98\",\"flow_rate\":null,\"discharge\":\"395.00\",\"storage_percent\":\"96.88\","
	    + "\"station_type\":\"tele_waterlevel\",\"agency\":{\"id\":12,\"agency_name\":{\"th\":\"\",\"en\":\"Royal Irrigation Department\","
	    + "\"jp\":\"\"},\"agency_shortname\":{\"th\":\"\",\"en\":\"RID\",\"jp\":\"\"}},\"basin\":{\"id\":10,\"basin_code\":10,"
	    + "\"basin_name\":{\"th\":\"\",\"en\":\"Chao Phraya Basin\"}},\"station\":{\"id\":2611,\"tele_station_name\":{\"th\":"
	    + "\"บ้านบางหลวงโดด\"},\"tele_station_lat\":14.41588,"
	    + "\"tele_station_long\":100.440804,\"tele_station_oldcode\":\"C.36\",\"tele_station_type\":\"tele_waterlevel\",\"min_bank\":4.3,"
	    + "\"qmax\":441},\"geocode\":{\"amphoe_name\":{\"th\":\"\",\"en\":\"Bang Ban District\"},\"province_code\":\"14\","
	    + "\"province_name\":{\"th\":\"\",\"en\":\"Phra Nakhon Si Ayutthaya\"}},\"river_name\":\"แม่น้ำน้อย\"}";

    /**
     * Entry of analyst/dam, dam_daily (some fields removed)
     */
    static final String DAM_ENTRY = "{\"id\":2910665,\"dam_date\":\"2026-10-27\",\"dam_storage\":4277.88,\"dam_storage_percent\":75.86,"
	    + "\"dam_inflow\":7.62,\"dam_uses_water\":2926.34,\"dam_level\":0,\"dam_released\":4.19,\"dam_spilled\":0,"
	    + "\"station_type\":\"dam_daily\",\"dam\":{\"id\":25,\"dam_oldcode\":\"31\",\"dam_name\":{\"en\":\"Ratchaprapa\",\"th\":\"\"},"
	    + "\"dam_lat\":9.054554,\"dam_long\":98.670247,\"max_storage\":6144.38},\"agency\":{\"id\":12,\"agency_name\":{\"th\":\"\","
	    + "\"en\":\"Royal Irrigation Department\"},\"agency_shortname\":{\"en\":\"RID\"}},\"basin\":{\"basin_name\":{\"th\":\"\","
	    + "\"en\":\"Peninsula - Upper East Coast Basin\"}},\"geocode\":{\"province_name\":{\"th\":\"\",\"en\":\"Surat Thani\"}}}";

    /**
     * Entry of public/canal_waterlevel
     */
    static final String CANAL_ENTRY = "{\"canal_datetime\":\"2026-10-28 03:05\",\"canal_value\":-0.42,\"canal_out\":0,\"agency\":"
	    + "{\"agency_name\":{\"th\":\"\",\"en\":\"Department of Bangkok\",\"jp\":\"\"}},\"geocode\":{\"province_name\":{\"th\":\"\","
	    + "\"en\":\"Bangkok\"}},\"station\":{\"id\":210,\"canal_name\":{\"th\":\"ค.ลาว\"},\"canal_lat\":13.73722,"
	    + "\"canal_long\":100.62499,\"canal_oldcode\":\"WL.KLA.01\",\"bank\":1.26}}";

    /**
     * Entry of public/flow
     */
    static final String FLOW_ENTRY = "{\"flow_datetime\":\"2026-10-28 03:05\",\"flow_value\":8.18,\"agency\":{\"agency_name\":{\"th\":\"\","
	    + "\"en\":\"Department of Bangkok\"}},\"station\":{\"id\":12,\"flow_name\":{\"th\":\"คลอง\"},"
	    + "\"flow_lat\":13.80012,\"flow_long\":100.51957,\"flow_oldcode\":\"FW.BSU.01\"}}";

    /**
     * @return
     */
    static ThaiWaterEntity createWaterLevelSeries() {

	return ThaiWaterClient.createSeries(new JSONObject(TELE_WATERLEVEL_ENTRY), ThaiWaterStationType.TELE_WATERLEVEL).get(0);
    }

    @Test
    public void createWaterLevelSeriesTest() {

	List<ThaiWaterEntity> series = ThaiWaterClient.createSeries(new JSONObject(TELE_WATERLEVEL_ENTRY),
		ThaiWaterStationType.TELE_WATERLEVEL);

	assertEquals(2, series.size());

	ThaiWaterEntity waterLevel = series.get(0);
	assertEquals("tele_waterlevel|2611|WATER_LEVEL", waterLevel.getSeriesId());
	assertEquals("tele_waterlevel|2611", waterLevel.getStationKey());
	assertEquals("2611", waterLevel.getStationId());
	assertEquals("C.36", waterLevel.getStationCode());
	// Thai name only
	assertEquals("บ้านบางหลวงโดด (C.36)", waterLevel.getStationName());
	assertEquals(ThaiWaterParameter.WATER_LEVEL, waterLevel.getParameter().get());
	assertEquals("Royal Irrigation Department", waterLevel.getString(ThaiWaterClient.AGENCY));
	assertEquals("Chao Phraya Basin", waterLevel.getString(ThaiWaterClient.BASIN));
	assertEquals("Phra Nakhon Si Ayutthaya", waterLevel.getString(ThaiWaterClient.PROVINCE));
	assertEquals("3.98", waterLevel.getString(ThaiWaterClient.LATEST_VALUE));
	// local time UTC+7
	assertEquals("2026-10-27T18:00:00Z", waterLevel.getString(ThaiWaterClient.END));

	assertEquals("tele_waterlevel|2611|DISCHARGE", series.get(1).getSeriesId());

	JSONObject noDischarge = new JSONObject(TELE_WATERLEVEL_ENTRY);
	noDischarge.put("discharge", JSONObject.NULL);
	assertEquals(1, ThaiWaterClient.createSeries(noDischarge, ThaiWaterStationType.TELE_WATERLEVEL).size());

	JSONObject noDate = new JSONObject(TELE_WATERLEVEL_ENTRY);
	noDate.put("waterlevel_datetime", JSONObject.NULL);
	assertTrue(ThaiWaterClient.createSeries(noDate, ThaiWaterStationType.TELE_WATERLEVEL).isEmpty());
    }

    @Test
    public void createOtherSeriesTest() {

	List<ThaiWaterEntity> dams = ThaiWaterClient.createSeries(new JSONObject(DAM_ENTRY), ThaiWaterStationType.DAM);

	// the level is 0 (not measured)
	assertEquals(4, dams.size());
	assertEquals("dam|25|DAM_STORAGE", dams.get(0).getSeriesId());
	assertTrue(dams.stream().noneMatch(s -> s.getParameter().get() == ThaiWaterParameter.DAM_LEVEL));
	assertEquals("Ratchaprapa (31)", dams.get(0).getStationName());
	assertEquals("2026-10-26T17:00:00Z", dams.get(0).getString(ThaiWaterClient.END));
	assertEquals(9.054554, dams.get(0).getObject().getDouble(ThaiWaterClient.LAT), 0);

	List<ThaiWaterEntity> canal = ThaiWaterClient.createSeries(new JSONObject(CANAL_ENTRY), ThaiWaterStationType.CANAL);
	assertEquals(1, canal.size());
	assertEquals("canal|210|CANAL_WATER_LEVEL", canal.get(0).getSeriesId());
	assertEquals("-0.42", canal.get(0).getString(ThaiWaterClient.LATEST_VALUE));

	List<ThaiWaterEntity> flow = ThaiWaterClient.createSeries(new JSONObject(FLOW_ENTRY), ThaiWaterStationType.FLOW);
	assertEquals(1, flow.size());
	assertEquals("flow|12|CANAL_FLOW", flow.get(0).getSeriesId());
	assertEquals("FW.BSU.01", flow.get(0).getStationCode());
    }

    @Test
    public void seriesIdTest() {

	ThaiWaterEntity series = ThaiWaterClient.parseSeriesId("dam|25|DAM_INFLOW");
	assertEquals("25", series.getStationId());
	assertEquals(ThaiWaterStationType.DAM, series.getStationType().get());
	assertEquals(ThaiWaterParameter.DAM_INFLOW, series.getParameter().get());
	assertEquals("dam|25|DAM_INFLOW", series.getSeriesId());

	assertEquals("analyst/dam_yearly_graph?data_type=dam_inflow&dam_id=25&year=2020",
		ThaiWaterClient.getYearPath(series, ThaiWaterParameter.DAM_INFLOW, 2020));

	ThaiWaterEntity waterLevel = ThaiWaterClient.parseSeriesId("tele_waterlevel|737|WATER_LEVEL");
	assertEquals("public/waterlevel_graph_year?station_type=tele_waterlevel&station_id=737&year=2015",
		ThaiWaterClient.getYearPath(waterLevel, ThaiWaterParameter.WATER_LEVEL, 2015));
	assertEquals("public/waterlevel_graph?station_type=tele_waterlevel&station_id=737&start_date=2026-10-20&end_date=2026-10-27",
		ThaiWaterClient.getGraphPath(waterLevel, ThaiWaterParameter.WATER_LEVEL, LocalDate.of(2026, 10, 20),
			LocalDate.of(2026, 10, 27)));

	ThaiWaterEntity canal = ThaiWaterClient.parseSeriesId("canal|210|CANAL_WATER_LEVEL");
	assertEquals("public/waterlevel_graph?station_type=canal&station_id=210&start_date=2026-10-20&end_date=2026-10-27",
		ThaiWaterClient.getGraphPath(canal, ThaiWaterParameter.CANAL_WATER_LEVEL, LocalDate.of(2026, 10, 20),
			LocalDate.of(2026, 10, 27)));

	ThaiWaterEntity flow = ThaiWaterClient.parseSeriesId("flow|12|CANAL_FLOW");
	assertEquals("public/flow_graph?station_id=12&date_start=2026-10-20&date_end=2026-10-27", ThaiWaterClient.getGraphPath(flow,
		ThaiWaterParameter.CANAL_FLOW, LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 27)));

	// parameter not observed by the station type
	assertNull(ThaiWaterClient.parseSeriesId("dam|25|WATER_LEVEL"));
	assertNull(ThaiWaterClient.parseSeriesId("tele_waterlevel|737"));
	assertNull(ThaiWaterClient.parseSeriesId("x|737|WATER_LEVEL"));
	assertNull(ThaiWaterClient.parseSeriesId(null));
    }

    @Test
    public void parseGraphTest() {

	JSONObject graph = new JSONObject("{\"result\":\"OK\",\"data\":{\"graph_data\":[{\"datetime\":\"2026-10-27 21:00\",\"value\":null,"
		+ "\"value_out\":null,\"discharge\":401},{\"datetime\":\"2026-10-27 22:00\",\"value\":4.01,\"value_out\":null,"
		+ "\"discharge\":399},{\"datetime\":\"2026-10-27 23:00\",\"value\":4,\"value_out\":null,\"discharge\":398}],"
		+ "\"min_bank\":4.3,\"warning_level\":null,\"critical_level\":null,\"ground_level\":-5.95,\"qmax\":441}}");

	TreeMap<Date, BigDecimal> waterLevel = ThaiWaterClient.parseGraph(graph, ThaiWaterParameter.WATER_LEVEL);
	assertEquals(2, waterLevel.size());
	assertEquals(date("2026-10-27T15:00:00Z"), waterLevel.firstKey());
	assertEquals(new BigDecimal("4.01"), waterLevel.firstEntry().getValue());

	TreeMap<Date, BigDecimal> discharge = ThaiWaterClient.parseGraph(graph, ThaiWaterParameter.DISCHARGE);
	assertEquals(3, discharge.size());
	assertEquals(new BigDecimal("398"), discharge.lastEntry().getValue());

	JSONObject flow = new JSONObject("{\"result\":\"OK\",\"data\":[{\"flow_datetime\":\"2026-09-28 00:00\",\"flow_value\":21.34},"
		+ "{\"flow_datetime\":\"2026-09-28 00:10\",\"flow_value\":null}]}");
	TreeMap<Date, BigDecimal> flowValues = ThaiWaterClient.parseGraph(flow, ThaiWaterParameter.CANAL_FLOW);
	assertEquals(1, flowValues.size());
	assertEquals(date("2026-09-27T17:00:00Z"), flowValues.firstKey());
    }

    @Test
    public void parseYearTest() {

	JSONObject year = new JSONObject("{\"result\":\"OK\",\"data\":{\"graph_data\":[{\"year\":\"2020\",\"data\":[{\"datetime\":"
		+ "\"2020-02-13 06:00\",\"waterlevel_m\":0.47,\"waterlevel_msl\":0.47,\"discharge\":null},{\"datetime\":\"2020-02-14 06:00\","
		+ "\"waterlevel_m\":0.45,\"waterlevel_msl\":0.45,\"discharge\":12.5}]}],\"min_bank\":4.3,\"qmax\":441}}");

	TreeMap<Date, BigDecimal> waterLevel = ThaiWaterClient.parseYear(year, ThaiWaterParameter.WATER_LEVEL);
	assertEquals(2, waterLevel.size());
	assertEquals(date("2020-02-12T23:00:00Z"), waterLevel.firstKey());

	assertEquals(1, ThaiWaterClient.parseYear(year, ThaiWaterParameter.DISCHARGE).size());

	JSONObject dam = new JSONObject("{\"result\":\"OK\",\"data\":{\"graph_data\":[{\"year\":2026,\"dam_name\":\"\",\"data\":[{\"date\":"
		+ "\"2026-01-01T00:00:00+07:00\",\"value\":4464.08},{\"date\":\"2026-12-31T00:00:00+07:00\",\"value\":null}]}],"
		+ "\"lower_bound\":1351.54,\"upper_bound\":6144.38}}");

	TreeMap<Date, BigDecimal> storage = ThaiWaterClient.parseYear(dam, ThaiWaterParameter.DAM_STORAGE);
	assertEquals(1, storage.size());
	assertEquals(date("2025-12-31T17:00:00Z"), storage.firstKey());
	assertEquals(new BigDecimal("4464.08"), storage.firstEntry().getValue());

	// canal stations have no yearly graph
	assertTrue(ThaiWaterClient.parseYear(new JSONObject("{\"result\":\"OK\",\"data\":null}"), ThaiWaterParameter.WATER_LEVEL)
		.isEmpty());
    }

    @Test
    public void parseDateTest() {

	assertEquals(date("2026-10-27T19:30:00Z"), ThaiWaterClient.parseDate("2026-10-28 02:30").get());
	assertEquals(date("2026-10-27T17:00:00Z"), ThaiWaterClient.parseDate("2026-10-28 00:00:00").get());
	assertEquals(date("2026-10-26T17:00:00Z"), ThaiWaterClient.parseDate("2026-10-27").get());
	assertEquals(date("2025-12-31T17:00:00Z"), ThaiWaterClient.parseDate("2026-01-01T00:00:00+07:00").get());
	assertFalse(ThaiWaterClient.parseDate("").isPresent());
	assertFalse(ThaiWaterClient.parseDate(null).isPresent());

	assertEquals(LocalDate.of(2026, 10, 28), ThaiWaterClient.toLocalDate(date("2026-10-27T17:00:00Z")));
    }

    @Test
    public void endpointTest() {

	assertEquals(ThaiWaterClient.DEFAULT_ENDPOINT,
		ThaiWaterClient.normalizeEndpoint("https://api-v3.thaiwater.net/api/v1/thaiwater30/public"));
	assertEquals(ThaiWaterClient.DEFAULT_ENDPOINT,
		ThaiWaterClient.normalizeEndpoint("https://api-v3.thaiwater.net/api/v1/thaiwater30"));
	assertEquals(ThaiWaterClient.DEFAULT_ENDPOINT, ThaiWaterClient.normalizeEndpoint(null));
    }

    /**
     * @param text
     * @return
     */
    private static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
