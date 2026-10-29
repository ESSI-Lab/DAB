package eu.essi_lab.accessor.siarso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.siarso.ARSOClient.Table;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class ARSOClientTest {

    static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><arsopodatki verzija=\"1.5\"><vir>Agencija RS za okolje</vir>"
	    + "<predlagan_zajem>5 minut &#x010D;ez polno uro ali pol ure</predlagan_zajem><predlagan_zajem_perioda>30 min</predlagan_zajem_perioda>"
	    + "<datum_priprave>2026-10-27 15:00</datum_priprave>"
	    + "<postaja  sifra=\"1060\"  wgs84_dolzina=\"15.9953\"  wgs84_sirina=\"46.68124\"  kota_0=\"202.35\" ><reka>Mura</reka>"
	    + "<merilno_mesto>Gornja Radgona I</merilno_mesto><ime_kratko>Mura - Gor. Radgona I</ime_kratko><datum>2026-10-27 15:00</datum>"
	    + "<datum_cet>2026-10-27 15:00</datum_cet><vodostaj>53</vodostaj><pretok>42.2</pretok><prvi_vv_pretok>600.0</prvi_vv_pretok>"
	    + "<drugi_vv_pretok>905.0</drugi_vv_pretok><tretji_vv_pretok>1180.0</tretji_vv_pretok><pretok_znacilni>mali pretok</pretok_znacilni>"
	    + "<temp_vode/></postaja>"
	    + "<postaja  sifra=\"9400\"  wgs84_dolzina=\"13.54542\"  wgs84_sirina=\"45.55084\" ><reka>Jadransko morje</reka>"
	    + "<merilno_mesto>OB Piran (NIB)</merilno_mesto><ime_kratko>Jadransko morje - OB Piran</ime_kratko><datum>2026-10-27 15:00</datum>"
	    + "<datum_cet>2026-10-27 15:00</datum_cet><znacilna_visina_valov/><smer_valovanja/><temp_vode>20.1</temp_vode></postaja>"
	    + "</arsopodatki>";

    static final String TABLE = "<h1>Postaja Gornja Radgona I - Mura</h1><table class=\"podatki\">\r\n<thead><tr>\r\n<th>Datum</th>\r\n"
	    + "<th>Vodostaj [cm]</th>\r\n<th>Pretok [m&#x00B3;/s]</th>\r\n<th>Temperatura vode [°C]</th>\r\n</tr></thead>\r\n<tbody>\r\n"
	    + "<tr>\r\n<td>27.10.2026 00:00</td>\r\n<td>56.0</td>\r\n<td>46.1</td>\r\n<td>-</td>\r\n</tr>\r\n"
	    + "<tr>\r\n<td>26.10.2026 23:00</td>\r\n<td>-</td>\r\n<td>47.6</td>\r\n<td>-</td>\r\n</tr>\r\n"
	    + "<tr>\r\n<td>26.10.2026 22:00</td>\r\n<td>57.0</td>\r\n<td>47.6</td>\r\n<td>-</td>\r\n</tr>\r\n"
	    + "</tbody></table><div id=\"noga\"><table><tr><td>other</td></tr></table></div>";

    /**
     * @return
     * @throws Exception
     */
    static JSONObject createStation() throws Exception {

	return ARSOClient.parseStations(XML).get(0);
    }

    /**
     * @return
     * @throws Exception
     */
    static List<ARSOEntity> createSeries() throws Exception {

	return ARSOClient.createSeries(createStation(), ARSOClient.parseTable(TABLE).get(), Collections.emptyList());
    }

    @Test
    public void parseStationsTest() throws Exception {

	List<JSONObject> stations = ARSOClient.parseStations(XML);
	assertEquals(2, stations.size());

	JSONObject station = stations.get(0);
	assertEquals("1060", station.getString(ARSOClient.STATION_ID));
	assertEquals("46.68124", station.getString(ARSOClient.LAT));
	assertEquals("202.35", station.getString(ARSOClient.GAUGE_ZERO));
	assertEquals("Gornja Radgona I", station.getString(ARSOClient.LOCATION));
	assertEquals("905.0", station.getString("drugi_vv_pretok"));
	// instantaneous values are not kept
	assertFalse(station.has("pretok"));
	assertFalse(station.has("pretok_znacilni"));

	JSONObject latest = station.getJSONObject(ARSOClient.LATEST);
	// 15:00 CET
	assertEquals("2026-10-27T14:00:00Z", latest.getString(ARSOClient.DISCHARGE_TAG));
	// missing values
	assertFalse(latest.has(ARSOClient.WATER_TEMPERATURE_TAG));

	assertFalse(stations.get(1).has(ARSOClient.GAUGE_ZERO));
	assertEquals("Jadransko morje", stations.get(1).getString(ARSOClient.RIVER));
    }

    @Test
    public void parseTableTest() {

	Table table = ARSOClient.parseTable(TABLE).get();

	assertEquals(Arrays.asList("Vodostaj", "Pretok", "Temperatura vode"), table.columns);
	assertEquals(Arrays.asList("cm", "m³/s", "°C"), table.units);
	assertEquals(3, table.rows.size());
	assertEquals(date("2026-10-26T21:00:00Z"), table.rows.firstKey());

	TreeMap<Date, BigDecimal> levels = table.getValues(0);
	// missing values are discarded
	assertEquals(2, levels.size());
	assertEquals(new BigDecimal("56.0"), levels.lastEntry().getValue());
	assertTrue(table.getValues(2).isEmpty());

	assertTrue(table.covers(date("2026-10-26T21:30:00Z")));
	assertFalse(table.covers(date("2026-10-26T20:00:00Z")));

	assertFalse(ARSOClient.parseTable("<html><body>Arhiv je za&#x010D;asno nedosegljiv</body></html>").isPresent());
    }

    @Test
    public void createSeriesTest() throws Exception {

	List<ARSOEntity> series = createSeries();

	// the water temperature has no valid values
	assertEquals(2, series.size());

	ARSOEntity level = series.get(0);
	assertEquals("1060|Vodostaj", level.getSeriesId());
	assertEquals(ARSOParameter.WATER_LEVEL, level.getParameter().get());
	assertEquals("cm", level.getUnit());
	assertEquals("Gornja Radgona I (Mura)", level.getStationName());
	assertEquals("2026-10-26T21:00:00Z", level.getString(ARSOClient.BEGIN));
	// updated with the latest value of the XML
	assertEquals("2026-10-27T14:00:00Z", level.getString(ARSOClient.END));
	assertFalse(level.getObject().has(ARSOClient.LATEST));

	ARSOEntity discharge = series.get(1);
	assertEquals("1060|Pretok", discharge.getSeriesId());
	assertEquals("m³/s", discharge.getUnit());

	List<ARSOEntity> filtered = ARSOClient.createSeries(createStation(), ARSOClient.parseTable(TABLE).get(),
		Arrays.asList(ARSOParameter.DISCHARGE));
	assertEquals(1, filtered.size());
	assertEquals("1060|Pretok", filtered.get(0).getSeriesId());
    }

    @Test
    public void seriesIdTest() {

	ARSOEntity series = ARSOClient.parseSeriesId("9400|Hitrost morskega toka 2m pod glad.");
	assertEquals("9400", series.getStationId());
	assertEquals(ARSOParameter.SEA_CURRENT_2M, series.getParameter().get());
	assertEquals("9400|Hitrost morskega toka 2m pod glad.", series.getSeriesId());

	assertNull(ARSOClient.parseSeriesId("9400"));
	assertNull(ARSOClient.parseSeriesId("9400|"));
	assertNull(ARSOClient.parseSeriesId(null));

	assertEquals("vode/podatki/amp/H9400_t_7.html", ARSOClient.getTablePath("9400", 7));
    }

    @Test
    public void utilsTest() {

	assertEquals("2026-10-27T14:30:00Z", ISO8601DateTimeUtils.getISO8601DateTime(ARSOClient.parseTableDate("27.10.2026 15:30").get()));
	// CET also in summer
	assertEquals("2026-07-01T11:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(ARSOClient.parseXMLDate("2026-07-01 12:00").get()));
	assertFalse(ARSOClient.parseTableDate("-").isPresent());

	assertNull(ARSOClient.parseDecimal("-"));
	assertEquals(new BigDecimal("-2.09"), ARSOClient.parseDecimal("-2.09"));

	assertEquals("Kučnica", ARSOClient.unescape("Ku&#x010D;nica"));
	assertEquals("m³/s", ARSOClient.unescape("m&#179;/s"));

	assertEquals("https://www.arso.gov.si/", ARSOClient.normalizeEndpoint("https://www.arso.gov.si"));
	assertEquals("http://www.arso.gov.si/", ARSOClient.normalizeEndpoint("http://www.arso.gov.si/xml/vode/hidro_podatki_zadnji.xml"));
	assertEquals(ARSOClient.DEFAULT_ENDPOINT, ARSOClient.normalizeEndpoint(null));
    }

    /**
     * @param text
     * @return
     */
    static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
