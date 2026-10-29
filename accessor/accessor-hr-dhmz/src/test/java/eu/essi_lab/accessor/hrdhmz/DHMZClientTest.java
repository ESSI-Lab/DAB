package eu.essi_lab.accessor.hrdhmz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class DHMZClientTest {

    static final String STATION_4 = "{'kod': '4', 'ttip': 'Postaja: <b>BADLJEVINA UZV., 3171</b><br>Vodotok: <b>BIJELA</b><br> <span style=\"color: #102DBA\";><b>SAVA</b></span><br>Početak rada: <b>30. 06. 1948.</b><br>Kraj rada: <b>---</b><br>Kota nule vodokaza: <b>137,140</b> m n/m<br><hr><table border=1px style=\"width:100%;border-collapse:collapse;border: 1px solid #a0a0a0;font-size:100%\"> <tr><td style=\"padding:3px\">Godine mjerenja vodostaja:</td><td style=\"padding:3px\"><b>1949-2025</b></td></tr><tr><td style=\"padding:3px\">Godine mjerenja protoka:</td><td style=\"padding:3px\"><b>1949-2025</b></td></tr><tr><td style=\"padding:3px\">Godine mjerenja temperatura:</td><td style=\"padding:3px\"><b>1976-1979, 1981-2024</b></td></tr><tr><td style=\"padding:3px\">Godine mjerenja koncentracija:</td><td style=\"padding:3px\"><b>1984-2024</b></td></tr><tr><td style=\"padding:3px\">Godine mjerenja pronosa:</td><td style=\"padding:3px\"><b>1984-2024</b></td></tr><tr><td style=\"padding:3px\">Broj vodomjerenja:</td><td style=\"padding:3px\"><b>180</b></td></tr><tr><td style=\"padding:3px\">Broj mjerenja profila:</td><td style=\"padding:3px\"><b>5</b></td></tr></table> <hr><span style=\"color: #B41010\";><b>Automatska dojava</b></span><br><span style=\"color: #102DBA\";><b>Zadnji podatak: </b></span>27. 10. 2026. 20:00&nbsp;<b>18</b>&nbsp;cm', 'sifra': '3171', 'gsirina': '45.5042777778', 'gduzina': '17.1894444444', 'ikona': 'sliv1a.png', 'kodvod': '2', 'kodsliv': '1','boja':'#B18904','autom':'t'}";

    static final String STATION_941 = "{'kod': '941', 'ttip': 'Postaja: <b>MURSKO SREDIŠĆE, 5165</b><br>Vodotok: <b>GORNJI POTOK</b><br> <span style=\"color: #102DBA\";><b>DRAVA I DUNAV</b></span><br>Početak rada: <b>27. 07. 2000.</b><br>Kraj rada: <b>---</b><br>Kota nule vodokaza: <b>160,110</b> m n/m<br><hr><table border=1px style=\"width:100%;border-collapse:collapse;border: 1px solid #a0a0a0;font-size:100%\"> <tr><td style=\"padding:3px\">Godine mjerenja vodostaja:</td><td style=\"padding:3px\"><b>2000-2023</b></td></tr><tr><td style=\"padding:3px\">Broj vodomjerenja:</td><td style=\"padding:3px\"><b>28</b></td></tr><tr><td style=\"padding:3px\">Broj mjerenja profila:</td><td style=\"padding:3px\"><b>6</b></td></tr></table> <hr><span style=\"color: #102DBA\";><b>Limnigrafska postaja</b></span>', 'sifra': '5165', 'gsirina': '46.5178888889', 'gduzina': '16.4251388889', 'ikona': 'sliv3.png', 'kodvod': '361', 'kodsliv': '3','boja':'#00FFFF','autom':'f'}";

    static final String STATION_6 = "{'kod': '6', 'ttip': 'Postaja: <b>BREGANA, 3903</b><br>Vodotok: <b>BREGANA</b><br> <span style=\"color: #102DBA\";><b>SAVA</b></span><br>Početak rada: <b>24. 07. 1952.</b><br>Kraj rada: <b>23. 01. 1969.</b><br>Kota nule vodokaza: <b>150,770</b> m n/m<br><hr>Godine mjerenja vodostaja: <b>1952-1968</b><hr><span style=\"color: #102DBA\";><b>Vodokazna postaja</b></span>', 'sifra': '3903', 'gsirina': 'None', 'gduzina': 'None', 'ikona': 'sliv1.png', 'kodvod': '3', 'kodsliv': '1','boja':'#B18904','autom':'f'}";

    static final String DAILY = "\n{'success': 'true', 'godina': '2020', 'psuho': '50', 'pnema': '48', 'kota': '78080', 'dpodaci':"
	    + " [{'term': '1577836800000', 'point': '255'}, {'term': '1577923200000', 'point': '48'}, {'term': '1578009600000',"
	    + " 'point': '50'}, {'term': '1578096000000', 'point': '178'}]}";

    static final String REAL_TIME = "{'success': 'true', 'godina': '0', 'psuho': '-121', 'pnema': '-123', 'kota': '78080', 'dpodaci':"
	    + " [{'term': '1793124000000', 'point': '-77'}, {'term': '1793127600000', 'point': '-123'}, {'term': '1793131200000',"
	    + " 'point': '-76'}]}";

    static final String YEARS = "{'success': 'true', 'godinepostaje': [{'gd':'2027','gs':'dojava'},{'gd':'2025','gs':'2025'},"
	    + "{'gd':'2024','gs':'2024'},{'gd':'1991','gs':'1991'}]}";

    static final String LATEST = "{'success': 'true', 'postaje': [{'kod': '3', 'ime': 'AKUMULACIJA PAKRA', 'sifra': '3399', 'sliv':"
	    + " 'Porječje Save', 'vod': 'AKUMULACIJA PAKRA', 'zterm': '27. 10. 2026. 20:00', 'zpod': '193&nbsp;cm'}]}";

    static final Date NOW = ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-27T20:30:00Z").get();

    /**
     * @param station
     * @return
     */
    static List<DHMZEntity> createSeries(String station) {

	return DHMZClient.createSeries(DHMZClient.parseResponse(station), Arrays.asList(DHMZProperty.values()), true, NOW);
    }

    @Test
    public void parseStationTest() {

	JSONObject station = DHMZClient.parseStation(DHMZClient.parseResponse(STATION_4));

	assertEquals("4", station.getString(DHMZClient.STATION_KEY));
	assertEquals("3171", station.getString(DHMZClient.STATION_CODE));
	assertEquals("BADLJEVINA UZV.", station.getString(DHMZClient.STATION_NAME));
	assertEquals("BIJELA", station.getString(DHMZClient.RIVER));
	assertEquals("SAVA", station.getString(DHMZClient.BASIN));
	assertEquals("1948-06-30", station.getString(DHMZClient.OPERATION_BEGIN));
	assertFalse(station.has(DHMZClient.OPERATION_END));
	assertEquals("137.140", station.getString(DHMZClient.GAUGE_ZERO));
	assertTrue(station.getBoolean(DHMZClient.AUTOMATIC));
	// CET
	assertEquals("2026-10-27T19:00:00Z", station.getString(DHMZClient.LATEST_TIME));
	assertEquals("18", station.getString(DHMZClient.LATEST_VALUE));
	assertEquals(new BigDecimal("45.5042777778"), station.getBigDecimal(DHMZClient.LAT));

	// no coordinates
	JSONObject historical = DHMZClient.parseStation(DHMZClient.parseResponse(STATION_6));
	assertFalse(historical.has(DHMZClient.LAT));
	assertEquals("1969-01-23", historical.getString(DHMZClient.OPERATION_END));
	assertFalse(historical.getBoolean(DHMZClient.AUTOMATIC));
    }

    @Test
    public void createSeriesTest() {

	List<DHMZEntity> series = createSeries(STATION_4);

	assertEquals(Arrays.asList("4|WATER_LEVEL", "4|DISCHARGE", "4|WATER_TEMPERATURE", "4|SUSPENDED_SEDIMENT_CONCENTRATION",
		"4|SUSPENDED_SEDIMENT_LOAD", "4|WATER_LEVEL|RT"), series.stream().map(DHMZEntity::getSeriesId).toList());

	DHMZEntity temperature = series.get(2);
	assertEquals(DHMZProperty.WATER_TEMPERATURE, temperature.getProperty().get());
	assertEquals("1976-1979, 1981-2024", temperature.getString(DHMZClient.YEARS));
	assertEquals("1976-01-01T00:00:00Z", temperature.getString(DHMZClient.BEGIN));
	assertEquals("2024-12-31T00:00:00Z", temperature.getString(DHMZClient.END));

	// the first year begins with the start of operation
	assertEquals("1949-01-01T00:00:00Z", series.get(0).getString(DHMZClient.BEGIN));

	DHMZEntity realTime = series.get(5);
	assertTrue(realTime.isRealTime());
	assertEquals("2026-10-11T23:00:00Z", realTime.getString(DHMZClient.BEGIN));
	assertEquals("2026-10-27T19:00:00Z", realTime.getString(DHMZClient.END));

	// non automatic station
	List<DHMZEntity> manual = createSeries(STATION_941);
	assertEquals(Arrays.asList("941|WATER_LEVEL"), manual.stream().map(DHMZEntity::getSeriesId).toList());
	assertEquals("2000-07-27T00:00:00Z", manual.get(0).getString(DHMZClient.BEGIN));

	// end of operation in the last year
	List<DHMZEntity> historical = createSeries(STATION_6);
	assertEquals("1952-07-24T00:00:00Z", historical.get(0).getString(DHMZClient.BEGIN));
	assertEquals("1968-12-31T00:00:00Z", historical.get(0).getString(DHMZClient.END));

	// real time series not requested
	assertEquals(5, DHMZClient.createSeries(DHMZClient.parseResponse(STATION_4), Arrays.asList(DHMZProperty.values()), false, NOW)
		.size());

	// latest observation out of the real time window
	Date later = ISO8601DateTimeUtils.parseISO8601ToDate("2026-11-20T00:00:00Z").get();
	assertEquals(5, DHMZClient.createSeries(DHMZClient.parseResponse(STATION_4), Arrays.asList(DHMZProperty.values()), true, later)
		.size());
    }

    @Test
    public void parseYearsTest() {

	assertEquals(new TreeSet<>(Arrays.asList(1923, 1924, 1949, 1950, 2025)), DHMZClient.parseYears("1923-1924, 1949-1950, 2025"));
	assertEquals(new TreeSet<>(Arrays.asList(1976, 1977, 1981)), DHMZClient.parseYears("1976-1977 1981"));
	assertTrue(DHMZClient.parseYears("").isEmpty());

	assertEquals(new TreeSet<>(Arrays.asList(1991, 2024, 2025)), DHMZClient.parseYearList(DHMZClient.parseResponse(YEARS)));
    }

    @Test
    public void parseValuesTest() {

	TreeMap<Date, BigDecimal> daily = DHMZClient.parseDailyValues(DHMZClient.parseResponse(DAILY), 2020);

	// missing ("pnema") and dry ("psuho") values are skipped
	assertEquals(2, daily.size());
	assertEquals("2020-01-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(daily.firstKey()));
	assertEquals(new BigDecimal("255"), daily.firstEntry().getValue());
	assertEquals(new BigDecimal("178"), daily.lastEntry().getValue());

	// the service returned another year
	assertTrue(DHMZClient.parseDailyValues(DHMZClient.parseResponse(DAILY), 1995).isEmpty());

	TreeMap<Date, BigDecimal> realTime = DHMZClient.parseRealTimeValues(DHMZClient.parseResponse(REAL_TIME));
	assertEquals(2, realTime.size());
	// CET to UTC
	assertEquals("2026-10-27T19:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(realTime.lastKey()));
	assertEquals(new BigDecimal("-76"), realTime.lastEntry().getValue());

	Map<String, Date> latest = DHMZClient.parseLatestObservations(DHMZClient.parseResponse(LATEST));
	assertEquals("2026-10-27T19:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(latest.get("3")));
    }

    @Test
    public void seriesIdTest() {

	DHMZEntity series = DHMZClient.parseSeriesId("384|DISCHARGE");
	assertEquals("384", series.getStationKey());
	assertEquals(DHMZProperty.DISCHARGE, series.getProperty().get());
	assertFalse(series.isRealTime());

	DHMZEntity realTime = DHMZClient.parseSeriesId("384|WATER_LEVEL|RT");
	assertTrue(realTime.isRealTime());
	assertEquals("384|WATER_LEVEL|RT", realTime.getSeriesId());

	assertNull(DHMZClient.parseSeriesId("384|DISCHARGE|RT"));
	assertNull(DHMZClient.parseSeriesId("384|UNKNOWN"));
	assertNull(DHMZClient.parseSeriesId("384"));
	assertNull(DHMZClient.parseSeriesId(null));
    }

    @Test
    public void endpointTest() {

	assertEquals(DHMZClient.DEFAULT_ENDPOINT, DHMZClient.normalizeEndpoint(null));
	assertEquals(DHMZClient.DEFAULT_ENDPOINT, DHMZClient.normalizeEndpoint("https://hidro.dhz.hr"));
	assertEquals(DHMZClient.DEFAULT_ENDPOINT, DHMZClient.normalizeEndpoint("https://hidro.dhz.hr/hidroweb/"));
	assertEquals(DHMZClient.DEFAULT_ENDPOINT, DHMZClient.normalizeEndpoint(DHMZClient.DEFAULT_ENDPOINT + "?funkc=markeri"));
    }
}
