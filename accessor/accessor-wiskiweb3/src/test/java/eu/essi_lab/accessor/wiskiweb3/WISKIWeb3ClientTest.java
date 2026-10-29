package eu.essi_lab.accessor.wiskiweb3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * Offline tests with samples of the Hesse (HLNUG) and North Rhine-Westphalia (LANUV) sites
 *
 * @author boldrini
 */
public class WISKIWeb3ClientTest {

    /**
     * Entry of internet/layers/10/index.json (Hesse)
     */
    static final String HESSE_LAYER_ENTRY = "{\"ts_id\": 9605010,\"timestamp\": \"2026-10-27T15:30:00.000+01:00\",\"req_timestamp\": null,"
	    + "\"ts_value\": 37,\"station_latitude\": 49.638812,\"station_longitude\": 8.766926,\"class_waterlevel\": null,"
	    + "\"station_id\": \"41248\",\"station_no\": \"23940359\",\"station_name\": \"Fahrenbach\",\"station_longname\": \"Fahrenbach\","
	    + "\"stationparameter_name\": \"W\",\"stationparameter_longname\": \"Wasserstand\",\"ts_shortname\": \"15m.Cmd.RelAbs.P\","
	    + "\"ts_name\": \"15.P\",\"ts_unitsymbol\": \"cm\",\"ts_precision\": \"DECI,0,2\",\"ts_path\": \"0/23940359/W/15m.Cmd.RelAbs.P\","
	    + "\"INTERNET_BEMERKUNG\": \"\",\"URLEXTERN\": \"\",\"Durchflusskennwerte\": \"yes\",\"Meldestufen\": \"yes\","
	    + "\"hydrounit_name\": \"Oberrhein\",\"EXTERN-WATERS-NAME\": \"Weschnitz\",\"BODY_RESPONSIBLE\": \"RPU Darmstadt\","
	    + "\"WTO_OBJECT\": \"Weschnitz\",\"WEB_Alarmklassen_Inaktiv\": \"\",\"Vorhersagepegel\": \"no\",\"WTO_MLST\": \"45,00 km\"}";

    /**
     * Entry of internet/stations/stations.json (Hesse)
     */
    static final String HESSE_STATION = "{\"station_id\":\"41248\",\"station_no\":\"23940359\",\"station_name\":\"Fahrenbach\","
	    + "\"site_no\":\"0\",\"station_latitude\":\"49.638812\",\"station_longitude\":\"8.766926\",\"catchment_name\":\"Weschnitz\","
	    + "\"CATCHMENT_SIZE\":\"44,33 km²\",\"station_gauge_datum\":\"177.523\",\"station_gauge_datum_postfix\":\"ü. NHN\","
	    + "\"BODY_RESPONSIBLE\":\"RPU Darmstadt\",\"WTO_OBJECT\":\"Weschnitz\"}";

    /**
     * Entry of internet/layers/10/index.json (North Rhine-Westphalia), without ts_path
     */
    static final String NRW_LAYER_ENTRY = "{\"ts_id\": 82977010,\"timestamp\": \"2026-06-12T10:30:00.000+01:00\",\"ts_value\": \"-21.60\","
	    + "\"station_latitude\": 50.892323156064,\"station_longitude\": 6.35957794905956,\"ts_name\": \"Wasserstand.Internet.15\","
	    + "\"ts_shortname\": \"15m.Cmd.Internet\",\"station_id\": \"28498\",\"station_no\": \"2825320000100\",\"site_no\": \"100\","
	    + "\"station_name\": \"Kirchberg2\",\"ts_unitsymbol\": \"cm\",\"stationparameter_name\": \"Wasserstand\","
	    + "\"stationparameter_no\": \"S\",\"catchment_name\": \"Rureinzugsgebiet\"}";

    static final String LAYER = "{\"type\":\"resource\",\"id\":\"10\",\"mime_type\":\"application/json\",\"visible\":\"true\","
	    + "\"alias\":\"Wasserstand\",\"label\":\"Wasserstand\",\"href\":\"internet/layers/10/index.json\",\"layer_type\":\"TS_LAYER\"}";

    /**
     * internet/stations/0/24861407/WT/week.json (Hesse, shortened): hourly values and daily means
     */
    static final String WEEK_FILE = "[{\"station_name\":\"Windecken\",\"station_no\":\"24861407\",\"stationparameter_name\":\"WT\","
	    + "\"stationparameter_longname\":\"Wassertemperatur\",\"ts_name\":\"60.P\",\"ts_shortname\":\"h.Cmd.P\",\"ts_unitsymbol\":\"°C\","
	    + "\"rows\": \"4\",\"columns\":\"Timestamp,Value\", \"data\": [[\"2026-10-19T23:00:00.000+01:00\",null],"
	    + "[\"2026-10-20T00:00:00.000+01:00\",10.3],[\"2026-10-20T01:00:00.000+01:00\",10.2],[\"2026-10-20T02:00:00.000+01:00\",null]]},"
	    + "{\"station_name\":\"Windecken\",\"station_no\":\"24861407\",\"stationparameter_name\":\"WT\",\"ts_name\":\"TagMittel\","
	    + "\"ts_shortname\":\"Day.Mean\",\"ts_unitsymbol\":\"°C\",\"rows\": \"1\",\"columns\":\"Timestamp,Value\","
	    + " \"data\": [[\"2026-10-20T00:00:00.000+01:00\",9.8]]}]";

    /**
     * Beginning of internet/stations/0/24861407/WT/year.json (Hesse), as returned by a range request
     */
    static final String YEAR_HEAD = "[{\"station_name\":\"Windecken\",\"station_longname\":\"Windecken\",\"station_no\":\"24861407\","
	    + "\"stationparameter_name\":\"WT\",\"stationparameter_longname\":\"Wassertemperatur\",\"ts_name\":\"TagMin\","
	    + "\"ts_shortname\":\"Day.Min\",\"ts_unitsymbol\":\"°C\",\"ts_precision\":\"Deci,1,0,0\",\"rows\": \"5306\","
	    + "\"columns\":\"Timestamp,Value\", \"data\": [[\"2012-04-17T00:00:00.000+01:00\",null],[\"2012-04-18T00:00:00.000+01:00\",11.4],"
	    + "[\"2012-04-19T00:00:00.000+01:00\",11";

    /**
     * End of the same file
     */
    static final String YEAR_TAIL = "00.000+01:00\",9.6],[\"2026-10-25T00:00:00.000+01:00\",9.8],[\"2026-10-26T00:00:00.000+01:00\",9.7],"
	    + "[\"2026-10-27T00:00:00.000+01:00\",null]]}]";

    /**
     * Excerpt of webpublic/config/app.json (Hesse)
     */
    static final String APP_CONFIG = "{\"apiPath\":\"../data\",\"route\":[{\"path\":\"/\",\"children\":[{\"path\":\"/Wasserstand\","
	    + "\"options\":{\"credits\":{\"text\":\"<span style='font-weight:bold;'>Ungeprüfte Rohdaten</span> © HLNUG<br>Alle Zeitstempel in MEZ\","
	    + "\"href\":\"https://www.hlnug.de/\"}}},{\"path\":\"/download\",\"options\":{\"descr\":\"Nachfolgend bieten wir Ihnen CSV und"
	    + " EXCEL Dateien zum Download an.<br><br>Sofern nicht anders angegeben, stehen alle Inhalte und Dateien in diesem"
	    + " Downloadbereich unter der Lizenz Creative Commons Namensnennung 4.0 International (CC BY 4.0,"
	    + " https://creativecommons.org/licenses/by/4.0/legalcode.de). Bei der Vervielfältigung, Weitergabe oder Abwandlung müssen"
	    + " Sie den Namen des Erstellers wie folgt angeben: „Hessisches Landesamt für Naturschutz, Umwelt und Geologie (HLNUG)“.\"}}]}]}";

    static final String APP_PAGE = "<!DOCTYPE html><html lang=\"de\"><head><meta charset=\"utf-8\" /><title>\n"
	    + "    Hessisches Landesamt für Naturschutz, Umwelt und Geologie: Wasser / Aktuelle Messdaten\n  </title></head></html>";

    /**
     * @param file
     * @return the water level series of Fahrenbach
     */
    static WISKIWeb3Entity createWaterLevelSeries(String file) {

	return WISKIWeb3Client.createSeries(new JSONObject(HESSE_LAYER_ENTRY), new JSONObject(LAYER), new JSONObject(HESSE_STATION),
		file).get();
    }

    @Test
    public void createSeriesTest() {

	WISKIWeb3Entity week = createWaterLevelSeries(WISKIWeb3Client.WEEK_FILE);
	assertEquals("0/23940359/W|week|15m.Cmd.RelAbs.P", week.getSeriesId());
	assertEquals("0/23940359/W", week.getStationPath());
	assertEquals("W", week.getParameterCode());
	assertEquals("internet/stations/0/23940359/W/week.json", WISKIWeb3Client.getFilePath(week));
	// completed from the station list
	assertEquals("177.523", week.getString(WISKIWeb3Client.GAUGE_DATUM));
	assertEquals("Weschnitz", week.getString(WISKIWeb3Client.CATCHMENT));

	WISKIWeb3Entity year = createWaterLevelSeries(WISKIWeb3Client.YEAR_FILE);
	assertEquals("0/23940359/W|year|Day.Mean", year.getSeriesId());
	assertFalse(year.isRecent());

	// station directory from the site, station and parameter numbers
	WISKIWeb3Entity nrw = WISKIWeb3Client
		.createSeries(new JSONObject(NRW_LAYER_ENTRY), new JSONObject(LAYER), null, WISKIWeb3Client.WEEK_FILE).get();
	assertEquals("100/2825320000100/S", nrw.getStationPath());
    }

    @Test
    public void seriesIdTest() {

	WISKIWeb3Entity series = WISKIWeb3Client.parseSeriesId("0/L121/N|week|h.CmdTotal");
	assertEquals("0/L121/N", series.getStationPath());
	assertEquals("L121", series.getStationNumber());
	assertEquals("h.CmdTotal", series.getTsShortName());
	assertEquals("internet/stations/0/L121/N/week.json", WISKIWeb3Client.getFilePath(series));

	assertNull(WISKIWeb3Client.parseSeriesId("0/L121/N|month|h.CmdTotal"));
	assertNull(WISKIWeb3Client.parseSeriesId("../L121/N|week|h.CmdTotal"));
	assertNull(WISKIWeb3Client.parseSeriesId("L121|week"));
    }

    @Test
    public void weekFileTest() {

	// the series of the layer
	WISKIWeb3Client.SeriesInfo info = WISKIWeb3Client.parseSeriesInfo(WEEK_FILE, "h.Cmd.P", WISKIWeb3Client.WEEK_FILE).get();
	assertEquals("h.Cmd.P", info.tsShortName);
	assertEquals("2026-10-19T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(info.firstValid));
	assertEquals("2026-10-20T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(info.lastValid));
	assertEquals("°C", info.header.get(WISKIWeb3Client.UNIT));

	// a series not published in the week file (e.g. moving sums): the first one with rows
	info = WISKIWeb3Client.parseSeriesInfo(WEEK_FILE, "h.Total.06h.MovSum", WISKIWeb3Client.WEEK_FILE).get();
	assertEquals("h.Cmd.P", info.tsShortName);

	TreeMap<Date, BigDecimal> values = WISKIWeb3Client
		.parseValues(WISKIWeb3Client.selectSeries(new JSONArray(WEEK_FILE), "Day.Mean", false).get());
	assertEquals(1, values.size());
	assertEquals(new BigDecimal("9.8"), values.firstEntry().getValue());
    }

    @Test
    public void yearFileTest() {

	WISKIWeb3Client.SeriesInfo info = WISKIWeb3Client.parseHead(YEAR_HEAD, WISKIWeb3Client.DAILY_MEAN).get();
	// the first series is the daily minimum: the mean is published with the same timestamps
	assertEquals(WISKIWeb3Client.DAILY_MEAN, info.tsShortName);
	assertFalse(info.header.containsKey(WISKIWeb3Client.TS_SHORT_NAME));
	assertEquals("°C", info.header.get(WISKIWeb3Client.UNIT));
	// the first row has no value
	assertEquals("2012-04-17T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(info.firstValid));

	assertEquals("2026-10-25T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(WISKIWeb3Client.parseTail(YEAR_TAIL).get()));

	// empty series
	assertTrue(WISKIWeb3Client.parseHead("[{\"ts_shortname\":\"Day.Total.S.P.TSum.0730\",\"rows\": \"0\",\"columns\":"
		+ "\"Timestamp,Value,Aggregation Accuracy %\", \"data\": []}]", WISKIWeb3Client.DAILY_MEAN).isEmpty());

	assertTrue(WISKIWeb3Client.isWholeFile("bytes 0-364/365"));
	assertFalse(WISKIWeb3Client.isWholeFile("bytes 0-32767/1693337"));
    }

    @Test
    public void siteInfoTest() {

	assertEquals("Hessisches Landesamt für Naturschutz, Umwelt und Geologie", WISKIWeb3Client.extractOrganisation(APP_PAGE).get());
	assertEquals("LANUV", WISKIWeb3Client.extractOrganisation("<title>LANUV - Hydrologische Messdaten Online</title>").get());
	assertTrue(WISKIWeb3Client.extractOrganisation("<title>KISTERS WISKI-WEB</title>").isEmpty());

	Optional<String> license = WISKIWeb3Client.extractLicense(APP_CONFIG);
	assertEquals("Creative Commons Attribution 4.0 International (CC BY 4.0) - https://creativecommons.org/licenses/by/4.0/legalcode.de"
		+ " - Attribution: Hessisches Landesamt für Naturschutz, Umwelt und Geologie (HLNUG)"
		+ " - Ungeprüfte Rohdaten © HLNUG Alle Zeitstempel in MEZ", license.get());

	assertEquals("HLNUG", WISKIWeb3Client.extractCreditsOrganisation(APP_CONFIG).get());

	assertEquals("https://www.hlnug.de/static/pegel/wiskiweb3/webpublic/",
		WISKIWeb3Client.getDefaultAppURL(WISKIWeb3Client.DEFAULT_ENDPOINT));
	assertEquals("https://www.hochwasserportal.nrw/webpublic/", WISKIWeb3Client.getDefaultAppURL("https://www.hochwasserportal.nrw/data"));
	assertEquals("https://www.hochwasserportal.nrw/data/",
		WISKIWeb3Client.normalizeEndpoint("https://www.hochwasserportal.nrw/data/internet/"));
    }
}
