package eu.essi_lab.accessor.luage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class AGEClientTest {

    static final String HOME_PAGE = "<a href=\"//inondations.public.lu/fr/alzette/alzette/hesperange.html\">Hesperange</a>"
	    + "<a href=\"/fr/sure/our/gemund.html\">Gemünd</a>"
	    + "<a href=\"https://inondations.public.lu/fr/alzette/alzette/hesperange.html\">Hesperange</a>"
	    + "<a href=\"//inondations.public.lu/fr/alzette/alzette.html\">Alzette</a>"
	    + "<a href=\"//inondations.public.lu/fr/support/contact.html\">Contact</a>";

    static final String STATION_PAGE = "<div class=\"chart\" data-to-json=\"{&#34;id&#34;:&#34;Ettelbrück&#34;,"
	    + "&#34;jsonFile&#34;:&#34;/content/dam/inondations/ctie/datas/Ettelbrück-Alzette.json&#34;,"
	    + "&#34;stationName&#34;:&#34;Ettelbrück / Alzette&#34;,&#34;waterCourse&#34;:&#34;Alzette&#34;,"
	    + "&#34;zeroScale&#34;:&#34;194,06 m NN.&#34;,&#34;basinVersion&#34;:&#34;1091,9 km²&#34;,"
	    + "&#34;serviceDate&#34;:&#34;01.09.1996&#34;,"
	    + "&#34;operator&#34;:&#34;Administration de la gestion de l\\u0027eau&#34;}\"></div>"
	    + "<div class=\"vcard\" data-latitude=\"49.84479999\" data-longitude=\"6.09858403\" data-zoom-on-click=\"false\"></div>";

    static final String DATA = "[{\"ts_path\":\"0/42/W_out/15m.Cmd.RelAbs.P\",\"ts_unitsymbol\":\"cm\",\"station_name\":\"Ettelbrück / Alzette\","
	    + "\"parametertype_name\":\"W\",\"rows\": \"4\",\"columns\":\"Timestamp,Value\", \"data\": ["
	    + "[\"2026-10-25T02:45:00.000+02:00\",41.0],[\"2026-10-25T02:00:00.000+01:00\",41.3],"
	    + "[\"2026-10-25T02:15:00.000+01:00\",null],[\"2026-10-25T02:30:00.000+01:00\",41.5]]}]";

    static final String STATION_URL = AGEClient.DEFAULT_ENDPOINT + "fr/alzette/alzette/ettelbruck-alzette.html";

    /**
     * @return
     */
    static AGEEntity createSeries() {

	JSONObject station = AGEClient.parseStation(STATION_PAGE, STATION_URL).get();
	JSONObject series = new JSONArray(DATA).getJSONObject(0);

	TreeMap<Date, BigDecimal> values = AGEClient.parseObservations(series);

	return AGEClient.createSeries(station, series, values.firstKey(), values.lastKey());
    }

    @Test
    public void parseStationPagesTest() {

	Set<String> pages = AGEClient.parseStationPages(HOME_PAGE);

	assertEquals(2, pages.size());
	assertTrue(pages.contains("fr/alzette/alzette/hesperange.html"));
	assertTrue(pages.contains("fr/sure/our/gemund.html"));
    }

    @Test
    public void parseStationTest() {

	JSONObject station = AGEClient.parseStation(STATION_PAGE, STATION_URL).get();

	assertEquals("Ettelbrück-Alzette", station.getString(AGEClient.CODE));
	assertEquals("Ettelbrück / Alzette", station.getString(AGEClient.NAME));
	assertEquals("Alzette", station.getString(AGEClient.WATER_COURSE));
	assertEquals("Administration de la gestion de l'eau", station.getString(AGEClient.OPERATOR));
	assertEquals("194,06 m NN.", station.getString(AGEClient.GAUGE_ZERO));
	assertEquals("1091,9 km²", station.getString(AGEClient.CATCHMENT_AREA));
	assertEquals(STATION_URL, station.getString(AGEClient.PAGE));
	assertEquals(49.84479999, station.getDouble(AGEClient.LAT), 0);
	assertEquals(6.09858403, station.getDouble(AGEClient.LON), 0);

	assertFalse(AGEClient.parseStation("<html></html>", STATION_URL).isPresent());
	assertFalse(AGEClient.parseStation(STATION_PAGE.replace("/content/dam/inondations/ctie/datas/", "/other/"), STATION_URL).isPresent());
    }

    @Test
    public void operatorTest() {

	JSONObject station = AGEClient.parseStation(STATION_PAGE, STATION_URL).get();
	assertTrue(AGEClient.isOperatedByAGE(station));

	station.put(AGEClient.OPERATOR, "Administration de la gestion de l’eau");
	assertTrue(AGEClient.isOperatedByAGE(station));

	station.put(AGEClient.OPERATOR, "Service de la navigation fluviale");
	assertFalse(AGEClient.isOperatedByAGE(station));

	station.put(AGEClient.OPERATOR, "Landesamt für Umwelt Rheinland-Pfalz");
	assertFalse(AGEClient.isOperatedByAGE(station));

	station.remove(AGEClient.OPERATOR);
	assertFalse(AGEClient.isOperatedByAGE(station));
    }

    @Test
    public void stationCodeTest() {

	assertEquals("SN_Remich", AGEClient.getStationCode("/content/dam/inondations/ctie/datas/SN_Remich.json").get());
	assertFalse(AGEClient.getStationCode("/content/dam/inondations/ctie/datas/SN_Remich.csv").isPresent());
	assertFalse(AGEClient.getStationCode("/content/dam/inondations/ctie/datas/sub/SN_Remich.json").isPresent());
	assertFalse(AGEClient.getStationCode("").isPresent());

	assertEquals("content/dam/inondations/ctie/datas/Ettelbr%C3%BCck-Alzette.json", AGEClient.getDataPath("Ettelbrück-Alzette"));
    }

    @Test
    public void parseObservationsTest() {

	TreeMap<Date, BigDecimal> values = AGEClient.parseObservations(new JSONArray(DATA).getJSONObject(0));

	// the null value is skipped, the end of the daylight saving time is handled with the UTC offsets
	assertEquals(3, values.size());
	assertEquals(ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-25T00:45:00Z").get(), values.firstKey());
	assertEquals(new BigDecimal("41.0"), values.firstEntry().getValue());
	assertEquals(ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-25T01:00:00Z").get(), values.higherKey(values.firstKey()));
	assertEquals(ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-25T01:30:00Z").get(), values.lastKey());

	assertTrue(AGEClient.parseObservations(new JSONObject()).isEmpty());
    }

    @Test
    public void createSeriesTest() {

	AGEEntity series = createSeries();

	assertEquals("Ettelbrück-Alzette|W", series.getSeriesId());
	assertEquals("Ettelbrück-Alzette", series.getStationCode());
	assertEquals("Ettelbrück / Alzette", series.getStationName());
	assertEquals("W", series.getParameter());
	assertEquals("cm", series.getUnit());
	assertEquals("0/42/W_out/15m.Cmd.RelAbs.P", series.getString(AGEClient.WISKI_PATH));
	assertEquals("2026-10-25T00:45:00Z", series.getObject().getString(AGEClient.BEGIN));
	assertEquals("2026-10-25T01:30:00Z", series.getObject().getString(AGEClient.END));
    }

    @Test
    public void seriesIdTest() {

	AGEEntity series = AGEClient.parseSeriesId("Gemünd_Our|W");
	assertEquals("Gemünd_Our", series.getStationCode());
	assertEquals("W", series.getParameter());

	assertNull(AGEClient.parseSeriesId("Gemünd_Our"));
	assertNull(AGEClient.parseSeriesId("Gemünd_Our|"));
	assertNull(AGEClient.parseSeriesId(null));
    }

    @Test
    public void settingTest() {

	AGEConnectorSetting setting = new AGEConnectorSetting();
	assertTrue(setting.getStations().isEmpty());

	setting.setStations(" Hesperange, SN_Remich,");

	assertEquals(2, setting.getStations().size());
	assertEquals("SN_Remich", setting.getStations().get(1));
    }
}
