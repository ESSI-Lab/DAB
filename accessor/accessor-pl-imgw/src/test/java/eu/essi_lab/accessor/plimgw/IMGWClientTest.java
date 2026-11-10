package eu.essi_lab.accessor.plimgw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.TreeMap;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.plimgw.IMGWClient.ArchiveIndex;
import eu.essi_lab.accessor.plimgw.IMGWClient.ArchiveRow;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class IMGWClientTest {

    static final String STATION = "{\"id_stacji\":\"153190040\",\"stacja\":\"B\\u0105gart\",\"rzeka\":\"Dzierzgo\\u0144\","
	    + "\"wojewodztwo\":\"pomorskie\",\"lon\":\"19.3692\",\"lat\":\"53.9661\",\"rok_zalozenia_stacji\":\"1959\","
	    + "\"rzedna_zerawodowskazu\":\"-5.077\",\"kilometr_biegu_rzeki\":\"38.61\",\"stan_alarmowy\":\"800\","
	    + "\"stan_ostrzegawczy\":\"790\",\"stan_wody\":\"697\",\"stan_wody_data_pomiaru\":\"2026-10-27 14:20:00\","
	    + "\"temperatura_wody\":null,\"temperatura_wody_data_pomiaru\":null,\"przeplyw\":\"0.98\","
	    + "\"przeplyw_data\":\"2026-10-27 14:10:00\",\"zjawisko_lodowe\":\"3\",\"zjawisko_lodowe_data_pomiaru\":"
	    + "\"2026-01-28 10:50:00\",\"zjawisko_zarastania\":\"101\",\"zjawisko_zarastania_data_pomiaru\":\"2026-08-05 11:00:00\"}";

    // monthly characteristics in the formats of 1971 (quoted, special missing codes), 2023 (semicolons) and 2025 (empty missing
    // values)
    static final String MONTHLY = "\" 153190040\",\"BĄGART\",\"Elbląg (54)\",\"1971\",\"01\",\"1\",9999,2.630,99.9,\"11\"\n" //
	    + "\" 153190040\",\"BĄGART\",\"Elbląg (54)\",\"1981\",\"01\",\"2\",712,3.730,99.9,\"11\"\n" //
	    + "153190040;BĄGART;Elbląg (54);2023;01;1;690;.430;99.9;11\n" //
	    + "\"153190040\",BĄGART,Elbląg (54),\"2025\",\"12\",\"2\",693,,,\"10\"\n";

    static final String DAILY = "\" 149180020\",\"CHAŁUPKI\",\"Odra (1)\",\"1981\",\"01\",\"01\",9999,38.100,99.9,\"11\"\n" //
	    + "\" 153190040\",\"BĄGART\",\"Elbląg (54)\",\"1981\",\"01\",\"01\",712,3.730,99.9,\"11\"\n" //
	    + "\" 153190040\",\"BĄGART\",\"Elbląg (54)\",\"1981\",\"01\",\"02\",9999,3.540,99.9,\"11\"\n" //
	    + "\" 153190040\",\"BĄGART\",\"Elbląg (54)\",\"1981\",\"01\",\"03\",710,3.500,99.9,\"11\"\n" //
	    + "\"153190040\",BĄGART,Elbląg (54),\"1981\",\"03\",\"01\",,3.100,,\"01\"\n";

    static final String LISTING = "<tr><td><a href=\"codz_2022_01.zip\">codz_2022_01.zip</a></td></tr>\n"
	    + "<tr><td><a href=\"codz_2022_02.zip\">codz_2022_02.zip</a></td></tr>\n"
	    + "<tr><td><a href=\"codz_2022_03.zip\">codz_2022_03.zip</a></td></tr>\n"
	    + "<tr><td><a href=\"zjaw_2022.zip\">zjaw_2022.zip</a></td></tr>\n";

    /**
     * @return
     */
    static ArchiveIndex createIndex() {

	ArchiveIndex index = new ArchiveIndex();
	for (String line : MONTHLY.split("\n")) {
	    IMGWClient.parseRow(line).ifPresent(index::add);
	}
	return index;
    }

    /**
     * @return
     */
    static IMGWEntity createWaterLevelSeries() {

	return IMGWClient.createSeries(new JSONObject(STATION), IMGWProperty.H, createIndex()).get();
    }

    @Test
    public void parseRowTest() {

	ArchiveRow old = IMGWClient.parseRow(MONTHLY.split("\n")[0]).get();
	assertEquals("153190040", old.code);
	assertEquals(1971, old.hydrologicalYear);
	assertEquals(11, old.calendarMonth);
	assertEquals(LocalDate.of(1970, 11, 1), old.getMonth());
	assertNull(old.getValue(IMGWProperty.H));
	assertEquals(new BigDecimal("2.630"), old.getValue(IMGWProperty.Q));
	assertNull(old.getValue(IMGWProperty.T));

	ArchiveRow semicolons = IMGWClient.parseRow(MONTHLY.split("\n")[2]).get();
	assertEquals(new BigDecimal("690"), semicolons.getValue(IMGWProperty.H));
	assertEquals(new BigDecimal("0.430"), semicolons.getValue(IMGWProperty.Q));

	ArchiveRow empty = IMGWClient.parseRow(MONTHLY.split("\n")[3]).get();
	assertEquals(LocalDate.of(2025, 10, 1), empty.getMonth());
	assertNull(empty.getValue(IMGWProperty.Q));

	ArchiveRow daily = IMGWClient.parseRow(DAILY.split("\n")[4]).get();
	assertEquals(LocalDate.of(1981, 1, 1), daily.getDate());

	// a name with the separator
	ArchiveRow comma = IMGWClient.parseRow("\"150190340\",KRAKÓW, BIELANY,Wisła (2),\"2025\",\"01\",\"01\",193,59.200,,\"11\"")
		.get();
	assertEquals("150190340", comma.code);
	assertEquals(new BigDecimal("193"), comma.getValue(IMGWProperty.H));

	assertFalse(IMGWClient.parseRow("").isPresent());
	assertFalse(IMGWClient.parseRow("a,b,c").isPresent());
    }

    @Test
    public void createSeriesTest() {

	IMGWEntity series = createWaterLevelSeries();

	assertEquals("153190040|H", series.getSeriesId());
	assertEquals("153190040", series.getStationCode());
	assertEquals("Bągart", series.getStationName());
	assertEquals(IMGWProperty.H, series.getProperty().get());

	// from the first characterized month to the latest observation
	assertEquals("1980-11-01T00:00:00Z", series.getString(IMGWClient.BEGIN));
	assertEquals("2025-10-31T00:00:00Z", series.getString(IMGWClient.ARCHIVE_END));
	assertEquals("2026-10-27T14:20:00Z", series.getString(IMGWClient.END));

	IMGWEntity discharge = IMGWClient.createSeries(new JSONObject(STATION), IMGWProperty.Q, createIndex()).get();
	assertEquals("1970-11-01T00:00:00Z", discharge.getString(IMGWClient.BEGIN));
	assertEquals("2026-10-27T14:10:00Z", discharge.getString(IMGWClient.END));

	// no water temperature, neither in the archive nor in the real time API
	assertFalse(IMGWClient.createSeries(new JSONObject(STATION), IMGWProperty.T, createIndex()).isPresent());

	// only real time
	IMGWEntity realTime = IMGWClient.createSeries(new JSONObject(STATION), IMGWProperty.H, new ArchiveIndex()).get();
	assertEquals("2026-10-27T14:20:00Z", realTime.getString(IMGWClient.BEGIN));
	assertFalse(realTime.getObject().has(IMGWClient.ARCHIVE_BEGIN));
    }

    @Test
    public void latestTest() {

	JSONObject station = new JSONObject(STATION);

	assertEquals(new BigDecimal("0.98"), IMGWClient.getLatestValue(station, IMGWProperty.Q).get());
	assertEquals(date("2026-10-27T14:10:00Z"), IMGWClient.getLatestTime(station, IMGWProperty.Q).get());
	assertFalse(IMGWClient.getLatestTime(station, IMGWProperty.T).isPresent());
    }

    @Test
    public void parseDailyValuesTest() throws Exception {

	TreeMap<Date, BigDecimal> values = new TreeMap<>();
	IMGWClient.parseDailyValues(new BufferedReader(new StringReader(DAILY)), "153190040", IMGWProperty.H,
		date("1980-11-01T00:00:00Z"), date("1981-01-31T00:00:00Z"), values);

	// the missing values are discarded
	assertEquals(2, values.size());
	assertEquals(new BigDecimal("712"), values.get(date("1980-11-01T00:00:00Z")));
	assertEquals(new BigDecimal("710"), values.get(date("1980-11-03T00:00:00Z")));

	values.clear();
	IMGWClient.parseDailyValues(new BufferedReader(new StringReader(DAILY)), "153190040", IMGWProperty.Q,
		date("1980-11-02T00:00:00Z"), date("1981-12-31T00:00:00Z"), values);
	assertEquals(3, values.size());
	assertEquals(new BigDecimal("3.100"), values.lastEntry().getValue());
    }

    @Test
    public void parseListingTest() {

	// the hydrological year 2022 starts in November 2021, the third month is January 2022
	assertEquals(Arrays.asList("codz_2022_02.zip", "codz_2022_03.zip"),
		IMGWClient.parseDailyFiles(LISTING, 2022, LocalDate.of(2021, 12, 15), LocalDate.of(2022, 1, 10)));
	assertTrue(IMGWClient.parseDailyFiles(LISTING, 2022, LocalDate.of(2022, 5, 1), LocalDate.of(2022, 6, 1)).isEmpty());

	String yearly = "<a href=\"codz_2025.zip\">codz_2025.zip</a> <a href=\"zjaw_2025.zip\">zjaw_2025.zip</a>";
	assertEquals(Arrays.asList("codz_2025.zip"),
		IMGWClient.parseDailyFiles(yearly, 2025, LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 1)));

	assertEquals(Arrays.asList(1951, 1952),
		Arrays.asList(IMGWClient.parseYears("<a href=\"1951/\">1951/</a><a href=\"1952/\">1952/</a><a href=\"x.txt\">")
			.toArray()));
    }

    @Test
    public void seriesIdTest() {

	IMGWEntity series = IMGWClient.parseSeriesId("153190040|Q");
	assertEquals("153190040", series.getStationCode());
	assertEquals(IMGWProperty.Q, series.getProperty().get());
	assertEquals("153190040|Q", series.getSeriesId());

	assertNull(IMGWClient.parseSeriesId("153190040"));
	assertNull(IMGWClient.parseSeriesId("153190040|X"));
	assertNull(IMGWClient.parseSeriesId(null));
    }

    @Test
    public void datesTest() {

	assertEquals(2026, IMGWClient.getHydrologicalYear(LocalDate.of(2025, 11, 1)));
	assertEquals(2025, IMGWClient.getHydrologicalYear(LocalDate.of(2025, 10, 31)));
	assertEquals(2024, IMGWClient.getCalendarYear(2025, 12));
	assertEquals(2025, IMGWClient.getCalendarYear(2025, 1));
	assertEquals(date("2026-10-27T14:20:00Z"), IMGWClient.parseAPIDate("2026-10-27 14:20:00").get());
	assertFalse(IMGWClient.parseAPIDate(null).isPresent());
    }

    /**
     * @param text
     * @return
     */
    static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
