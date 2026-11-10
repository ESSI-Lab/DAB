package eu.essi_lab.accessor.atehyd;

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
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.atehyd.EHYDClient.Observations;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author boldrini
 */
public class EHYDClientTest {

    /**
     * An entry of the station list https://ehyd.gv.at/services/Messstellen/json?filter=owf
     */
    static final String STATION_FEATURE = "{\"type\":\"Feature\",\"id\":\"messstellen.fid-3def4fa5_1a17dca0b04_456a\","
	    + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[1275647.24109865,5961020.22882306]},\"geometry_name\":\"the_geom\","
	    + "\"properties\":{\"hzbnr01\":\"201582\",\"symbol\":\"owf_messstelle6\",\"minyear\":1971,\"maxyear\":2021,"
	    + "\"fjson\":\"[{\\\"hzbnr\\\":\\\"201582\\\",\\\"file\\\":\\\"Stammdaten-201582.csv\\\",\\\"filefrom\\\":0,\\\"fileto\\\":0,"
	    + "\\\"filenr\\\":1,\\\"ffound\\\":1,\\\"fsize\\\":4853,\\\"ftime\\\":1715131816,\\\"ftyp\\\":null}, "
	    + "{\\\"hzbnr\\\":\\\"201582\\\",\\\"file\\\":\\\"W-Tagesmittel-201582.csv\\\",\\\"filefrom\\\":1971,\\\"fileto\\\":2021,"
	    + "\\\"filenr\\\":2,\\\"ffound\\\":1,\\\"fsize\\\":522705,\\\"ftime\\\":1715131816,\\\"ftyp\\\":\\\"owf_wasserst_tagesmittel\\\"}, "
	    + "{\\\"hzbnr\\\":\\\"201582\\\",\\\"file\\\":\\\"Q-Tagesmittel-201582.csv\\\",\\\"filefrom\\\":1971,\\\"fileto\\\":2021,"
	    + "\\\"filenr\\\":5,\\\"ffound\\\":1,\\\"fsize\\\":522704,\\\"ftime\\\":1715131816,\\\"ftyp\\\":\\\"owf_abfl_tagesmittel\\\"}, "
	    + "{\\\"hzbnr\\\":\\\"201582\\\",\\\"file\\\":\\\"Q-Monatsmaxima-201582.csv\\\",\\\"filefrom\\\":1971,\\\"fileto\\\":2021,"
	    + "\\\"filenr\\\":7,\\\"ffound\\\":1,\\\"fsize\\\":18179,\\\"ftime\\\":1715131816,\\\"ftyp\\\":\\\"owf_abfl_monatsmaxima\\\"}]\"}}";

    /**
     * The beginning of https://ehyd.gv.at/services/MessstellenExtraData/owf?id=201582&file=2
     */
    static final String WATER_LEVEL_FILE = "Messstelle:                ;Mühlen\r\n" //
	    + "HZB-Nummer:                ;201582\r\n" //
	    + "HD-Nummer:                 ;\r\n" //
	    + "DBMS-Nummer:               ;7001055\r\n" //
	    + "Gewässer:                  ;Navisbach\r\n" //
	    + "Sachgebiet:                ;OWF\r\n" //
	    + "Dienststelle:              ;HD-Tirol\r\n" //
	    + "Messstellenbetreiber:      ;Hydrographischer Dienst\r\n" //
	    + "orogr.Einzugsgebiet [km²]: ;61,5\r\n" //
	    + "Pegelnullpunkt:\r\n" //
	    + "  gültig seit:             ;Höhe [m ü.A.]:\r\n" //
	    + "   01.12.1947              ;01014,52\r\n" //
	    + "   27.11.1980              ;01009,09\r\n" //
	    + "   17.05.2022              ;01009,23\r\n" //
	    + "Bundesmeldenetz(BMN)-Koordinaten:\r\n" //
	    + "  gültig seit:             ;Rechtswert y [m] - Hochwert x [m]:\r\n" //
	    + "   17.05.2022              ;235476,85 - 220280,33\r\n" //
	    + "Exportzeitreihe:           ;(7001055,Wasserstand,I,Mit,T,1,F,Z,0,,,)\r\n" //
	    + "Einheit:                   ;[cm]\r\n" //
	    + "Exportzeitraum:            ;[01.01.1971 00:00,01.01.2022 00:00]\r\n" //
	    + "Der Intervallwert gilt bis zum nächsten Zeitpunkt mit einem Wert oder Lücke\r\n" //
	    + "Werte:\r\n" //
	    + "01.01.1971 00:00:00; Lücke\r\n" //
	    + "02.01.1971 00:00:00;    53\r\n" //
	    + "03.01.1971 00:00:00;    52\r\n" //
	    + "04.01.1971 00:00:00;    51\r\n" //
	    + "05.01.1971 00:00:00; Lücke\r\n";

    /**
     * The end of https://ehyd.gv.at/services/MessstellenExtraData/owf?id=201582&file=2
     */
    static final String WATER_LEVEL_TAIL = "021 00:00:00;    41\r\n" //
	    + "29.12.2021 00:00:00;    42\r\n" //
	    + "30.12.2021 00:00:00;    48\r\n" //
	    + "31.12.2021 00:00:00;    45\r\n" //
	    + "01.01.2022 00:00:00; Lücke\r\n";

    /**
     * The beginning of a groundwater file, with three columns
     */
    static final String GROUNDWATER_FILE = "\r\n" //
	    + "Messstelle:                ;Kirchenviertel, Br  3265\r\n" //
	    + "HZB-Nummer:                ;328781\r\n" //
	    + "PorenGW-Gebiet:            ;378 Murdurchbruchstal\r\n" //
	    + "Grundwasserkörper:         ;Murdurchbruchstal  (Bruck/Mur - Graz) [MUR]\r\n" //
	    + "Dienststelle:              ;HD-Steiermark\r\n" //
	    + "Exportzeitreihe:           ;GWS,I,Mit,M,1,A,Z,0,,,\r\n" //
	    + "Werteformat:               ;2 Nachkommastellen\r\n" //
	    + "Werte:\r\n" //
	    + "01.10.1980 00:00:00   ;   369,72 ;     \r\n" //
	    + "01.11.1980 00:00:00   ;   369,87 ;     \r\n" //
	    + "01.01.2021 00:00:00   ;Lücke         ; \r\n";

    /**
     * From https://ehyd.gv.at/services/Messstellen/info?hzbnr=201582
     */
    static final String INFO = "[{\"caption\":\"Messstelle\",\"data\":[{\"info\":{\"data\":[{\"caption\":\"HZBNR\",\"text\":201582},"
	    + "{\"caption\":\"Name\",\"text\":\"Mühlen\"},{\"tag\":\"dl_mstfiles\",\"data\":{\"fileData\":["
	    + "{\"msttyp\":\"owf\",\"fileName\":\"Stammdaten-201582.csv\",\"fileNr\":1,\"filefrom\":0,\"fileto\":0,\"hzbnr\":\"201582\"},"
	    + "{\"msttyp\":\"owf\",\"fileName\":\"W-Tagesmittel-201582.csv\",\"fileNr\":2,\"filefrom\":1971,\"fileto\":2021,\"hzbnr\":\"201582\"},"
	    + "{\"msttyp\":\"owf\",\"fileName\":\"WT-Monatsmittel-201582.csv\",\"fileNr\":8,\"filefrom\":2007,\"fileto\":2021,\"hzbnr\":\"201582\"},"
	    + "{\"msttyp\":\"owf\",\"fileName\":\"Q-Tagesmittel-201582.csv\",\"fileNr\":5,\"filefrom\":1971,\"fileto\":2021,\"hzbnr\":\"201582\"}"
	    + "]}}]}}]}]";

    /**
     * @return the water level series of the station 201582, with the header of its file
     * @throws GSException
     */
    static EHYDEntity createWaterLevelSeries() throws GSException {

	JSONObject stations = new JSONObject().put("features", new JSONArray().put(new JSONObject(STATION_FEATURE)));
	EHYDEntity series = EHYDClient.parseStations(stations, Arrays.asList(EHYDProduct.OWF_WATER_LEVEL_DAILY_MEAN)).get(0);

	Observations observations = EHYDClient.parse(new BufferedReader(new StringReader(WATER_LEVEL_FILE)), null, null, true);
	observations.header.forEach((k, v) -> series.getObject().put(k, v));

	return series;
    }

    @Test
    public void parseStationsTest() {

	JSONObject stations = new JSONObject().put("features", new JSONArray().put(new JSONObject(STATION_FEATURE)));

	List<EHYDEntity> series = EHYDClient.parseStations(stations, EHYDProduct.getProducts(EHYDProduct.SURFACE_WATER));
	assertEquals(3, series.size());

	EHYDEntity waterLevel = series.get(0);
	assertEquals("201582|owf_wasserst_tagesmittel", waterLevel.getSeriesId());
	assertEquals("201582", waterLevel.getHZBNumber());
	assertEquals(EHYDProduct.OWF_WATER_LEVEL_DAILY_MEAN, waterLevel.getProduct().get());
	assertEquals(2, waterLevel.getFileNumber().intValue());
	assertEquals(1971, waterLevel.getObject().getInt(EHYDClient.FIRST_YEAR));

	// Navis, Tyrol
	assertEquals(47.116, waterLevel.getObject().getDouble(EHYDClient.LAT), 0.01);
	assertEquals(11.46, waterLevel.getObject().getDouble(EHYDClient.LON), 0.01);

	assertEquals(5, series.get(1).getFileNumber().intValue());
	assertEquals(EHYDProduct.OWF_DISCHARGE_MONTHLY_MAX, series.get(2).getProduct().get());

	// only the selected products
	assertEquals(1, EHYDClient.parseStations(stations, Arrays.asList(EHYDProduct.OWF_DISCHARGE_DAILY_MEAN)).size());
	assertTrue(EHYDClient.parseStations(stations, EHYDProduct.getProducts(EHYDProduct.GROUNDWATER)).isEmpty());
    }

    @Test
    public void parseFileTest() throws Exception {

	Observations observations = EHYDClient.parse(new BufferedReader(new StringReader(WATER_LEVEL_FILE)), null, null, true);

	assertEquals("Mühlen", observations.header.get(EHYDClient.STATION_NAME));
	assertEquals("Navisbach", observations.header.get(EHYDClient.WATERBODY));
	assertEquals("HD-Tirol", observations.header.get(EHYDClient.OFFICE));
	assertEquals("Hydrographischer Dienst", observations.header.get(EHYDClient.OPERATOR));
	assertEquals("61,5", observations.header.get(EHYDClient.CATCHMENT_AREA));
	assertEquals("cm", observations.header.get(EHYDClient.UNIT));
	assertEquals("7001055", observations.header.get(EHYDClient.DBMS_NUMBER));
	// the current gauge zero
	assertEquals("1009.23", observations.header.get(EHYDClient.GAUGE_DATUM));

	// gaps are skipped
	assertEquals(3, observations.values.size());
	assertEquals("1971-01-01T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(observations.getFirst().get()));
	assertEquals("1971-01-03T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(observations.getLast().get()));
	assertEquals(new BigDecimal("53"), observations.values.firstEntry().getValue());

	// a period
	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("1971-01-02T12:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("1971-01-03T12:00:00Z").get();
	observations = EHYDClient.parse(new BufferedReader(new StringReader(WATER_LEVEL_FILE)), begin, end, true);
	assertEquals(1, observations.values.size());
	assertEquals(new BigDecimal("52"), observations.values.firstEntry().getValue());
    }

    @Test
    public void parseGroundwaterFileTest() throws Exception {

	Observations observations = EHYDClient.parse(new BufferedReader(new StringReader(GROUNDWATER_FILE)), null, null, true);

	assertEquals("Kirchenviertel, Br  3265", observations.header.get(EHYDClient.STATION_NAME));
	assertEquals("378 Murdurchbruchstal", observations.header.get(EHYDClient.AQUIFER));
	assertNull(observations.header.get(EHYDClient.UNIT));

	assertEquals(2, observations.values.size());
	assertEquals(new BigDecimal("369.72"), observations.values.firstEntry().getValue());
    }

    @Test
    public void parseTailTest() throws Exception {

	Observations observations = EHYDClient.parseTail(WATER_LEVEL_TAIL);

	// the truncated first line is discarded
	assertEquals(3, observations.values.size());
	assertEquals("2021-12-30T23:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(observations.getLast().get()));
    }

    @Test
    public void parseValueTest() {

	assertEquals(new BigDecimal("1.01"), EHYDClient.parseValue("  1,01").get());
	assertEquals(new BigDecimal("0.64"), EHYDClient.parseValue(" 0,640").get());
	assertEquals(new BigDecimal("1014.52"), EHYDClient.parseValue("01014,52").get());
	assertFalse(EHYDClient.parseValue(" Lücke  ").isPresent());
	assertFalse(EHYDClient.parseValue("").isPresent());
    }

    @Test
    public void findFileNumberTest() {

	JSONArray info = new JSONArray(INFO);

	assertEquals(Optional.of(2), EHYDClient.findFileNumber(info, EHYDProduct.OWF_WATER_LEVEL_DAILY_MEAN.getFileName("201582")));
	assertEquals(Optional.of(8),
		EHYDClient.findFileNumber(info, EHYDProduct.OWF_WATER_TEMPERATURE_MONTHLY_MEAN.getFileName("201582")));
	assertFalse(EHYDClient.findFileNumber(info, EHYDProduct.OWF_DISCHARGE_MONTHLY_MAX.getFileName("201582")).isPresent());
    }

    @Test
    public void wholeFileTest() {

	assertTrue(EHYDClient.isWholeFile("bytes 0-4852/4853"));
	assertFalse(EHYDClient.isWholeFile("bytes 0-16383/522705"));
	assertFalse(EHYDClient.isWholeFile("bytes 522405-522704/522705"));
    }

    @Test
    public void seriesIdTest() {

	EHYDEntity series = EHYDClient.parseSeriesId("201582|owf_abfl_tagesmittel");
	assertEquals("201582", series.getHZBNumber());
	assertEquals(EHYDProduct.OWF_DISCHARGE_DAILY_MEAN, series.getProduct().get());

	assertNull(EHYDClient.parseSeriesId("201582|unknown"));
	assertNull(EHYDClient.parseSeriesId("201582"));

	assertEquals("MessstellenExtraData/owf?id=201582&file=5",
		EHYDClient.getFilePath("201582", EHYDProduct.OWF_DISCHARGE_DAILY_MEAN, 5));
    }

    @Test
    public void endpointTest() {

	assertEquals(EHYDClient.DEFAULT_ENDPOINT, EHYDClient.normalizeEndpoint("https://ehyd.gv.at"));
	assertEquals(EHYDClient.DEFAULT_ENDPOINT, EHYDClient.normalizeEndpoint("https://ehyd.gv.at/eHYD/"));
	assertEquals(EHYDClient.DEFAULT_ENDPOINT, EHYDClient.normalizeEndpoint("https://ehyd.gv.at/services"));
    }
}
