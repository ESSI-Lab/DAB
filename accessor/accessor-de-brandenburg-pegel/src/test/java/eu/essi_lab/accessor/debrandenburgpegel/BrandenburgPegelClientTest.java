package eu.essi_lab.accessor.debrandenburgpegel;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class BrandenburgPegelClientTest {

    /**
     * Excerpt of the start page: a river basin link, a gauge with water level and discharge, a gauge with water level only and
     * without link, a gauge of a neighbouring portal and a link feature
     */
    static final String START_PAGE = "<li><a href='flussgebiet.php?fgid=1&thema=karte' title='Flussgebiet Ucker'"
	    + " class='dropdown-items bb-dropdown-items'>Ucker </a></li>\n" //
	    + "        features: [\n" //
	    + "                      new ol.Feature({\n" //
	    + "              geometry: new ol.geom.Point([145640.13,5751234.92]),\n" //
	    + "              pegel: 'pgurl',\n" //
	    + "              pgurl: 'https://www.hochwasserzentralen.de/',\n" //
	    + "              pgtext: 'Hochwasserzentralen'\n" //
	    + "            }),\n" //
	    + "                          new ol.Feature({\n" //
	    + "                geometry: new ol.geom.Point([423373.12,5907470.59]),\n" //
	    + "                pegel: 'bb',\n" //
	    + "                pkz: '0491200',\n" //
	    + "                uuid: '',\n" //
	    + "                flusskm: '63,30',\n" //
	    + "                flussmuendung: 'oberhalb der M&uuml;ndung',\n" //
	    + "                pegelinbb: '1',\n" //
	    + "                pegelurl: 'https://pegelportal.brandenburg.de/messstelle.php?fgid=1&pkz=0491200&thema=ws_graph',\n" //
	    + "                name: 'Prenzlau, Wehr UP',\n" //
	    + "                gewaesser: 'Ucker',\n" //
	    + "                betreiber: 'LfU Brandenburg',\n" //
	    + "                pgtyp: 'k. A.',\n" //
	    + "                pgparameter: 'W/Q',\n" //
	    + "                label_position: '1',\n" //
	    + "                                  klasse: '1',\n" //
	    + "                  alarmklasse: '0',\n" //
	    + "                  datum: '27.10.2026',\n" //
	    + "                  zeit: '15:15',\n" //
	    + "                                wert: '23',\n" //
	    + "                wstendenz: '2',\n" //
	    + "                wspng: '1',\n" //
	    + "                qpng: '1',\n" //
	    + "                fgid: '1',\n" //
	    + "                qwert: '0,166'\n" //
	    + "              }),\n" //
	    + "                          new ol.Feature({\n" //
	    + "                geometry: new ol.geom.Point([423416.11,5907355.59]),\n" //
	    + "                pegel: 'bb',\n" //
	    + "                pkz: '0491101',\n" //
	    + "                pegelurl: '',\n" //
	    + "                name: 'Prenzlau, Wehr OP',\n" //
	    + "                gewaesser: 'Ucker',\n" //
	    + "                betreiber: 'LfU Brandenburg',\n" //
	    + "                pgparameter: 'W',\n" //
	    + "                  datum: '27.10.2026',\n" //
	    + "                  zeit: '15:30',\n" //
	    + "                fgid: '1',\n" //
	    + "                qwert: '-'\n" //
	    + "              }),\n" //
	    + "                          new ol.Feature({\n" //
	    + "                geometry: new ol.geom.Point([458658.00,5886045.00]),\n" //
	    + "                pegel: 'bb',\n" //
	    + "                pkz: '153140020',\n" //
	    + "                pegelurl: 'https://hydro.imgw.pl/#/station/hydro/153140020?h=73',\n" //
	    + "                name: 'Widuchowa',\n" //
	    + "                gewaesser: 'Oder',\n" //
	    + "                betreiber: 'IMGW-PIB',\n" //
	    + "                pgparameter: 'W',\n" //
	    + "                  datum: '27.10.2026',\n" //
	    + "                  zeit: '20:00',\n" //
	    + "                fgid: '5'\n" //
	    + "              }),\n" //
	    + "        ]\n";

    static final String CSV = "Datum;\"Wasserstand in cm\";\"Stationsnummer: 0491101\";\"Stationsname: Prenzlau, Wehr OP\";"
	    + "\"Gewässer: Ucker\";\"Fehlwerte: -777\"\n" //
	    + "\"24.10.2026 02:15\";62\n" //
	    + "\"24.10.2026 02:30\";-777\n" //
	    + "\"24.10.2026 02:45\";62\n" //
	    + "\"25.10.2026 02:15\";63\n";

    /**
     * Excerpt of a gauge page with the charts of water level (Tab1) and discharge (Tab2)
     */
    static final String STATION_PAGE = "    <!-- Tablesorter: required -->\n" //
	    + "<!-- Tab1 Wasserstand --><li class='accordion-item' id='panel1'>\n" //
	    + "    // data = new Array([1761523200000,22],[1761524100000,22]);\n" //
	    + "    var data = new Array();\n" //
	    + "          data.push([1761523200000,22])\n" //
	    + "          data.push([1761524100000,23])\n" //
	    + "          data.push([1761525000000,null])\n" //
	    + "                  data2 = new Array([1761523200000,6.75],[1793115000000,6.75]);\n" //
	    + "              dataTaW.push({x: 1286323200000, y: 93, primStatus: 'Geprüft'})\n" //
	    + "		<!-- Tab2 Abfluss --><li class='accordion-item' id='panel2'>\n" //
	    + "    var data = new Array();\n" //
	    + "          data.push([1793113200000,0.009])\n" //
	    + "          data.push([1793114100000,0.166])\n" //
	    + "          data.push([1793115000000,null])\n" //
	    + "		<!-- Tab3 Alarmwerte --><li class='accordion-item' id='panel3'>\n" //
	    + "          data.push([1793116000000,9])\n";

    static final Date NOW = ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-27T20:00:00Z").get();

    /**
     * @return
     */
    static List<BrandenburgPegelEntity> createSeries() {

	return BrandenburgPegelClient.parseStartPage(START_PAGE, Arrays.asList(BrandenburgPegelParameter.values()), NOW);
    }

    @Test
    public void startPageTest() {

	List<BrandenburgPegelEntity> series = createSeries();

	// the gauge of the neighbouring portal is discarded, the water level only gauge has a single series
	assertEquals(3, series.size());
	assertEquals("0491101|W", series.get(0).getSeriesId());
	assertEquals("0491200|Q", series.get(1).getSeriesId());
	assertEquals("0491200|W", series.get(2).getSeriesId());

	BrandenburgPegelEntity waterLevel = series.get(2);
	assertEquals("Prenzlau, Wehr UP", waterLevel.getStationName());
	assertEquals("oberhalb der Mündung", waterLevel.getString(BrandenburgPegelClient.RIVER_POSITION));
	assertEquals("Ucker", waterLevel.getString(BrandenburgPegelClient.RIVER_BASIN));
	assertEquals("0491200|W|1", waterLevel.getOnlineName());

	// MEZ = UTC+1
	assertEquals("2026-10-27T14:15:00Z", waterLevel.getString(BrandenburgPegelClient.END));
	assertEquals("2025-10-26T23:00:00Z", waterLevel.getString(BrandenburgPegelClient.BEGIN));

	double lat = waterLevel.getObject().getDouble(BrandenburgPegelClient.LAT);
	double lon = waterLevel.getObject().getDouble(BrandenburgPegelClient.LON);
	assertEquals(53.31, lat, 0.02);
	assertEquals(13.84, lon, 0.02);

	// only the water level
	assertEquals(2, BrandenburgPegelClient.parseStartPage(START_PAGE, Arrays.asList(BrandenburgPegelParameter.W), NOW).size());
    }

    @Test
    public void staleSeriesTest() {

	// latest value before the window of the last year
	Date later = ISO8601DateTimeUtils.parseISO8601ToDate("2027-11-01T00:00:00Z").get();
	assertTrue(BrandenburgPegelClient.parseStartPage(START_PAGE, Arrays.asList(BrandenburgPegelParameter.values()), later)
		.isEmpty());
    }

    @Test
    public void csvTest() {

	TreeMap<Date, BigDecimal> values = BrandenburgPegelClient.parseCsv(CSV);

	// missing value -777 skipped
	assertEquals(3, values.size());
	assertEquals("2026-10-24T01:15:00Z", ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));
	assertEquals(new BigDecimal("62"), values.firstEntry().getValue());
	// no daylight saving time
	assertEquals("2026-10-25T01:15:00Z", ISO8601DateTimeUtils.getISO8601DateTime(values.lastKey()));
    }

    @Test
    public void stationPageTest() throws Exception {

	TreeMap<Date, BigDecimal> waterLevel = BrandenburgPegelClient
		.parseStationPage(new BufferedReader(new StringReader(STATION_PAGE)), BrandenburgPegelParameter.W);

	assertEquals(2, waterLevel.size());
	// epoch of the MEZ wall clock time 27.10.2025 00:00
	assertEquals("2025-10-26T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(waterLevel.firstKey()));
	assertEquals(new BigDecimal("23"), waterLevel.lastEntry().getValue());

	TreeMap<Date, BigDecimal> discharge = BrandenburgPegelClient
		.parseStationPage(new BufferedReader(new StringReader(STATION_PAGE)), BrandenburgPegelParameter.Q);

	assertEquals(2, discharge.size());
	assertEquals("2026-10-27T14:15:00Z", ISO8601DateTimeUtils.getISO8601DateTime(discharge.lastKey()));
	assertEquals(new BigDecimal("0.166"), discharge.lastEntry().getValue());
    }

    @Test
    public void windowsTest() {

	assertEquals("2025-10-26T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(BrandenburgPegelClient.getPageWindowStart(NOW)));
	assertEquals("2026-09-26T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(BrandenburgPegelClient.getCsvWindowStart(NOW)));
	assertEquals("2026-10-19T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(BrandenburgPegelClient.getWeekWindowStart(NOW)));

	// 23:30 UTC is already the next day in MEZ
	Date lateEvening = ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-27T23:30:00Z").get();
	assertEquals("2025-10-27T23:00:00Z",
		ISO8601DateTimeUtils.getISO8601DateTime(BrandenburgPegelClient.getPageWindowStart(lateEvening)));
    }

    @Test
    public void seriesIdTest() {

	BrandenburgPegelEntity series = BrandenburgPegelClient.parseSeriesId("0491200|Q|1");
	assertEquals("0491200|Q", series.getSeriesId());
	assertEquals("0491200", series.getStationNumber());
	assertEquals("1", series.getRiverBasinId());
	assertEquals(BrandenburgPegelParameter.Q, series.getParameter().get());

	assertEquals("0491101|W", BrandenburgPegelClient.parseSeriesId("0491101|W").getOnlineName());

	assertNull(BrandenburgPegelClient.parseSeriesId("0491200|X|1"));
	assertNull(BrandenburgPegelClient.parseSeriesId("0491200"));
	assertNull(BrandenburgPegelClient.parseSeriesId(null));
    }

    @Test
    public void decodeTest() {

	assertEquals("PGE GiEK S.A. Oddział Elektrownia Turów",
		BrandenburgPegelClient.decodeEntities("PGE GiEK S.A. Oddzia&#322; Elektrownia Tur&#243;w"));
	assertNull(BrandenburgPegelClient.parseDecimal("-777"));
	assertNull(BrandenburgPegelClient.parseDecimal("null"));
	assertEquals(new BigDecimal("0.166"), BrandenburgPegelClient.parseDecimal("0,166"));
	assertFalse(BrandenburgPegelClient.parseDate("27.10.2026").isPresent());
	assertEquals("https://pegelportal.brandenburg.de/", BrandenburgPegelClient.normalizeEndpoint("https://pegelportal.brandenburg.de/start.php"));
    }
}
