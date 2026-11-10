package eu.essi_lab.accessor.czchmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class CHMIClientTest {

    /**
     * Excerpt of now/metadata/meta1.json
     */
    static final String STATIONS = "{\"zaznamID\":\"5ED3DCC9EC6C14C5E064020820C987EB\",\"datovyZdrojID\":\"hydrologie\","
	    + "\"datovyTokID\":\"Open.Data.Metadata\",\"datumVytvoreni\":\"2026-10-27T14:00:03Z\",\"verzeDat\":\"1.0\",\"data\":{\"type\":"
	    + "\"DataCollection\",\"data\":{\"header\":\"objID,DBC,STATION_NAME,STREAM_NAME,GEOGR1,GEOGR2,SPA_TYP,SPAH_DS,SPAH_UNIT,"
	    + "DRYH,SPA1H,SPA2H,SPA3H,SPA4H,SPAQ_DS,SPAQ_UNIT,DRYQ,SPA1Q,SPA2Q,SPA3Q,SPA4Q,ISFORECAST\",\"values\":[[\"0-203-1-004200\","
	    + "\"004200\",\"Vestřev\",\"Labe\",50.5144063,15.7417222,\"H\",\"vodní stav\",\"CM\",14,110,135,160,335,\"průtok\",\"M3_S\","
	    + "1.24,33,45.2,60,238,1],[\"0-203-1-003000\",\"003000\",\"Prosečné\",\"Malé Labe\",50.552152,15.694443,\"H\",\"vodní stav\","
	    + "\"CM\",45,null,null,null,214,\"průtok\",\"M3_S\",0.288,null,null,null,70.5,0]]}}}";

    /**
     * Excerpt of now/data/0-203-1-004200.json (with a missing value added)
     */
    static final String NOW = "{\"objList\" : [{\"objID\":\"0-203-1-004200\",\"tsList\":[{\"tsConID\":\"H\",\"unit\":\"CM\",\"tsData\":"
	    + "[{\"dt\":\"2026-10-27T00:00:00Z\",\"value\":25},{\"dt\":\"2026-10-27T00:10:00Z\",\"value\":null},"
	    + "{\"dt\":\"2026-10-27T00:20:00Z\",\"value\":26}]},{\"tsConID\":\"H_F\",\"unit\":\"CM\",\"tsData\":[{\"dt\":"
	    + "\"2026-10-27T12:00:00Z\",\"value\":22}]},{\"tsConID\":\"Q\",\"unit\":\"M3_S\",\"tsData\":[{\"dt\":\"2026-10-27T00:00:00Z\","
	    + "\"value\":2.74927}]},{\"tsConID\":\"TH\",\"unit\":\"0C\",\"tsData\":[{\"dt\":\"2026-10-27T00:00:00Z\",\"value\":7.6}]}]}]}";

    /**
     * Excerpt of historical/data/daily/H_0-203-1-000400_DQ_2021.json
     */
    static final String DAILY = "{\"objID\":\"0-203-1-000400\",\"tsList\":[{\"tsConID\":\"HD\",\"unit\":\"CM\",\"tsData\":{\"type\":"
	    + "\"DataCollection\",\"data\":{\"header\":\"DT,VAL\",\"values\":[[\"2021-10-20T00:00:00Z\",14],[\"2021-12-31T00:00:00Z\","
	    + "51]]}}},{\"tsConID\":\"QD\",\"unit\":\"M3_S\",\"tsData\":{\"type\":\"DataCollection\",\"data\":{\"header\":\"DT,VAL\","
	    + "\"values\":[[\"2021-10-21T00:00:00Z\",0.228],[\"2021-10-22T00:00:00Z\",null],[\"2021-12-31T00:00:00Z\",1.06]]}}}]}";

    /**
     * Excerpt of the listing of historical/data/daily/
     */
    static final String LISTING = "<html><body><h1>Index of /hydrology/historical/data/daily/</h1><hr><pre><a href=\"../\">../</a>\n"
	    + "<a href=\"H_0-203-1-000400_DQ_2021.json\">H_0-203-1-000400_DQ_2021.json</a>  01-Jul-2025 12:07  32412\n"
	    + "<a href=\"H_0-203-1-000400_DQ_2025.json\">H_0-203-1-000400_DQ_2025.json</a>  21-May-2026 11:19  32533\n"
	    + "<a href=\"H_0-203-1-001000_DQ_1911.json\">H_0-203-1-001000_DQ_1911.json</a>  01-Jul-2025 12:07  32412\n"
	    + "<a href=\"H_0-203-1-001000_MQ_1900.json\">H_0-203-1-001000_MQ_1900.json</a>  01-Jul-2025 12:07  1009\n"
	    + "</pre><hr></body></html>";

    /**
     * Excerpt of the listing of recent/data/
     */
    static final String RECENT_LISTING = "<a href=\"../\">../</a>\n"
	    + "<a href=\"20260102_0-203-1-001000.json\">20260102_0-203-1-001000.json</a>  03-Jan-2026 00:20  5996\n"
	    + "<a href=\"20260101_0-203-1-001000.json\">20260101_0-203-1-001000.json</a>  02-Jan-2026 00:20  5996\n"
	    + "<a href=\"20260315_0-203-1-002000.json\">20260315_0-203-1-002000.json</a>  16-Mar-2026 00:20  12661\n";

    /**
     * @return
     */
    static JSONObject getStation() {

	return CHMIClient.parseTable(new JSONObject(STATIONS)).get(0);
    }

    @Test
    public void parseTableTest() {

	List<JSONObject> rows = CHMIClient.parseTable(new JSONObject(STATIONS));
	assertEquals(2, rows.size());

	JSONObject station = rows.get(0);
	assertEquals("0-203-1-004200", station.getString(CHMIClient.OBJ_ID));
	assertEquals("004200", station.getString(CHMIClient.DBC));
	assertEquals("Vestřev", station.getString(CHMIClient.STATION_NAME));
	assertEquals("Labe", station.getString(CHMIClient.STREAM_NAME));
	assertEquals(50.5144063, station.getDouble(CHMIClient.LAT), 0);

	// null values are omitted
	assertFalse(rows.get(1).has("SPA1H"));
    }

    @Test
    public void parseOperationalTest() {

	Map<String, TreeMap<Date, BigDecimal>> series = CHMIClient.parseOperational(new JSONObject(NOW));

	TreeMap<Date, BigDecimal> waterLevel = series.get("H");
	// missing values are discarded
	assertEquals(2, waterLevel.size());
	assertEquals(date("2026-10-27T00:00:00Z"), waterLevel.firstKey());
	assertEquals(new BigDecimal("26"), waterLevel.lastEntry().getValue());

	assertEquals(new BigDecimal("2.74927"), series.get("Q").firstEntry().getValue());
	assertEquals(new BigDecimal("7.6"), series.get("TH").firstEntry().getValue());
    }

    @Test
    public void parseHistoricalTest() {

	Map<String, TreeMap<Date, BigDecimal>> series = CHMIClient.parseHistorical(new JSONObject(DAILY));

	assertEquals(2, series.get("HD").size());

	TreeMap<Date, BigDecimal> discharge = series.get("QD");
	assertEquals(2, discharge.size());
	assertEquals(date("2021-10-21T00:00:00Z"), discharge.firstKey());
	assertEquals(new BigDecimal("1.06"), discharge.lastEntry().getValue());
    }

    @Test
    public void parseListingTest() {

	Map<String, int[]> years = CHMIClient.parseListing(LISTING, CHMIProduct.DAILY);

	assertEquals(2, years.size());
	assertEquals(2021, years.get("0-203-1-000400")[0]);
	assertEquals(2025, years.get("0-203-1-000400")[1]);
	assertEquals(1911, years.get("0-203-1-001000")[0]);
	assertEquals(1911, years.get("0-203-1-001000")[1]);

	Map<String, LocalDate> recent = CHMIClient.parseRecentListing(RECENT_LISTING);
	assertEquals(LocalDate.of(2026, 1, 1), recent.get("0-203-1-001000"));
	assertEquals(LocalDate.of(2026, 3, 15), recent.get("0-203-1-002000"));
    }

    @Test
    public void createSeriesTest() {

	CHMIEntity series = CHMIClient.createSeries(getStation(), CHMIParameter.H, CHMIClient.getYearBegin(1925),
		date("2026-10-27T14:30:00Z"));

	assertEquals("0-203-1-004200|H", series.getSeriesId());
	assertEquals("0-203-1-004200", series.getStationId());
	assertEquals("Vestřev", series.getStationName());
	assertEquals(CHMIParameter.H, series.getParameter().get());
	assertEquals("1925-01-01T00:00:00Z", series.getString(CHMIClient.BEGIN));
	assertEquals("2026-10-27T14:30:00Z", series.getString(CHMIClient.END));
    }

    @Test
    public void seriesIdTest() {

	CHMIEntity series = CHMIClient.parseSeriesId("0-203-1-004200|QNEX");
	assertEquals("0-203-1-004200", series.getStationId());
	assertEquals(CHMIParameter.QNEX, series.getParameter().get());
	assertEquals("0-203-1-004200|QNEX", series.getSeriesId());

	assertNull(CHMIClient.parseSeriesId("0-203-1-004200"));
	assertNull(CHMIClient.parseSeriesId("0-203-1-004200|H_F"));
	assertNull(CHMIClient.parseSeriesId(null));
    }

    @Test
    public void pathsTest() {

	assertEquals("recent/data/20260101_0-203-1-001000.json",
		CHMIClient.getRecentPath("0-203-1-001000", LocalDate.of(2026, 1, 1)));
	assertEquals("historical/data/morning_water_level/H_0-203-1-016000_OH7_1971.json",
		CHMIClient.getHistoricalPath(CHMIProduct.MORNING_WATER_LEVEL, "0-203-1-016000", 1971));
	assertEquals("2025-12-31T23:59:59Z", ISO8601DateTimeUtils.getISO8601DateTime(CHMIClient.getYearEnd(2025)));
	assertEquals("https://opendata.chmi.cz/hydrology/", CHMIClient.normalizeEndpoint("https://opendata.chmi.cz/hydrology"));
    }

    @Test
    public void parametersTest() {

	assertEquals(CHMIParameter.HC, CHMIParameter.H.getHistoricalCounterpart().get());
	assertEquals(CHMIParameter.TO, CHMIParameter.TH.getHistoricalCounterpart().get());
	assertFalse(CHMIParameter.Q.getHistoricalCounterpart().isPresent());
	assertEquals(4, CHMIParameter.getParameters(CHMIProduct.DAILY).size());
	assertTrue(CHMIParameter.getParameters(CHMIProduct.OPERATIONAL).contains(CHMIParameter.TH));

	CHMIConnectorSetting setting = new CHMIConnectorSetting();
	assertEquals(CHMIParameter.values().length, setting.getParameters().size());
	setting.setParameters("H, qd,X");
	assertEquals(2, setting.getParameters().size());
	assertEquals(CHMIParameter.QD, setting.getParameters().get(1));
    }

    /**
     * @param text
     * @return
     */
    private static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
