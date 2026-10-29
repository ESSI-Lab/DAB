package eu.essi_lab.accessor.esmitecoaforos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.esmitecoaforos.MITECOAforosClient.Archive;
import eu.essi_lab.accessor.esmitecoaforos.MITECOAforosClient.Lookups;
import eu.essi_lab.accessor.esmitecoaforos.MITECOAforosClient.SeriesPeriod;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * Offline tests, with samples of the tables of the CANTABRICO folder of the 2021-22 edition
 *
 * @author boldrini
 */
public class MITECOAforosClientTest {

    static final String ESTAF = "indroea;lugar;comentario;serv;suprest;suprcnc;hoja_id;alti;altimax;naa;naam;naai;num_cuenca;muni_id;"
	    + "xutm;yutm;xutm30;yutm30;long;lat;longwgs84;latwgs84;observ1;observ2;observ3;observ4;observ5;xetrs89;yetrs89;cod_saih\r\n"
	    + "1033;PUENTE DE SAMA                     ;INT 1916                         ;0;557;4866;53;208;;4;4;0;1152;33031;282343;"
	    + "4797727;282343;4797727;-54060;431800;-54105;431756;;;;;;282234;4797522;\r\n";

    static final String AFLIQ = "indroea;fecha;altura;caudal\r\n" //
	    + "1033;01/01/1912;0.3;6.342\r\n" //
	    + "1033;02/01/1912;0.3;\r\n" //
	    + "1033;03/01/1912;;\r\n" //
	    + "1033;30/06/1916;-0.45;\r\n" //
	    + "1034;01/01/1912;0.5;\r\n" //
	    + "1034;02/01/1912;0.5;\r\n";

    // monthly rows are not sorted
    static final String EVAP = "ref_evap;anomes;hora1;hora2;hora3;evapiche;evatanque;temedmax;temedmin;temedsec1;temedhum1;"
	    + "temedsec2;temedhum2;temedsec3;temedhum3;velviento;prectotmes;diasprec;insol\r\n" //
	    + "1001;198406;7;;;2;3.7;18.4;9.1;18.1;17.4;;;;;;78.8;7;\r\n" //
	    + "1001;198307;7;;;1.8;3.4;23.3;19.4;20.8;18.4;;;;;2.5;81;8;\r\n" //
	    + "1001;198308;7;;;1.8;2.8;20.8;15.3;19.1;17.5;;;;;2;124.7;20;\r\n";

    static final String AFLIQE = "ref_ceh;fecha;reserva;salida;entrada;tipo\r\n" //
	    + "1077;01/10/2008;1.577;0.121;;2\r\n" //
	    + "1077;02/10/2008;1.566;0.122;;2\r\n";

    /**
     * @return the station 1033 as described from the sample tables
     */
    static JSONObject describeSampleStation() throws Exception {

	Lookups lookups = new Lookups();
	lookups.rivers.put("1152", "NALON");
	lookups.municipalities.put("33031", new String[] { "LANGREO", "33" });
	lookups.provinces.put("33", "ASTURIAS");

	return MITECOAforosClient.readStations(reader(ESTAF), MITECOAforosStationType.RIVER, lookups).get("1033");
    }

    /**
     * @return the discharge series of station 1033
     */
    static MITECOAforosEntity createDischargeSeries() throws Exception {

	Map<String, SeriesPeriod> periods = new TreeMap<>();
	MITECOAforosClient.scanTable(reader(AFLIQ),
		Arrays.asList(MITECOAforosVariable.RIVER_LEVEL_DAILY, MITECOAforosVariable.RIVER_DISCHARGE_DAILY),
		Collections.emptyList(), periods);

	SeriesPeriod discharge = periods.values().stream()
		.filter(p -> p.stationId.equals("1033") && p.variable == MITECOAforosVariable.RIVER_DISCHARGE_DAILY).findFirst().get();

	return MITECOAforosClient.createSeries("CANTABRICO", discharge, describeSampleStation(), "2021-22");
    }

    @Test
    public void scanTableTest() throws Exception {

	Map<String, SeriesPeriod> periods = new TreeMap<>();
	MITECOAforosClient.scanTable(reader(AFLIQ),
		Arrays.asList(MITECOAforosVariable.RIVER_LEVEL_DAILY, MITECOAforosVariable.RIVER_DISCHARGE_DAILY),
		Collections.emptyList(), periods);

	// 1034 has no discharge values
	assertEquals(3, periods.size());

	SeriesPeriod level = periods.values().iterator().next();
	assertEquals("1033", level.stationId);
	assertEquals(MITECOAforosVariable.RIVER_LEVEL_DAILY, level.variable);
	assertEquals("1912-01-01", level.first);
	assertEquals("1916-06-30", level.last);
	assertEquals(3, level.count);

	periods.clear();
	MITECOAforosClient.scanTable(reader(EVAP), Arrays.asList(MITECOAforosVariable.PRECIPITATION_MONTHLY,
		MITECOAforosVariable.WIND_SPEED_MONTHLY, MITECOAforosVariable.SUNSHINE_DURATION_MONTHLY), Collections.emptyList(),
		periods);

	// no sunshine values
	assertEquals(2, periods.size());
	SeriesPeriod precipitation = periods.values().iterator().next();
	assertEquals("1983-07-01", precipitation.first);
	assertEquals("1984-06-01", precipitation.last);

	// station filter
	periods.clear();
	MITECOAforosClient.scanTable(reader(AFLIQ), Arrays.asList(MITECOAforosVariable.RIVER_LEVEL_DAILY), Arrays.asList("1034"),
		periods);
	assertEquals(1, periods.size());
    }

    @Test
    public void createSeriesTest() throws Exception {

	MITECOAforosEntity series = createDischargeSeries();

	assertEquals("CANTABRICO|1033|RIVER_DISCHARGE_DAILY", series.getSeriesId());
	assertEquals("PUENTE DE SAMA", series.getStationName());
	assertEquals("NALON", series.getString(MITECOAforosClient.RIVER));
	assertEquals("LANGREO", series.getString(MITECOAforosClient.MUNICIPALITY));
	assertEquals("ASTURIAS", series.getString(MITECOAforosClient.PROVINCE));
	assertEquals("Out of service", series.getString(MITECOAforosClient.STATUS));
	assertEquals("557", series.getString(MITECOAforosClient.CATCHMENT_AREA));
	assertEquals("1912-01-01T00:00:00Z", series.getString(MITECOAforosClient.BEGIN));
	assertEquals("1912-01-01T00:00:00Z", series.getString(MITECOAforosClient.END));
	assertEquals(1, series.getObject().getInt(MITECOAforosClient.VALUES));

	// -5° 41' 05", 43° 17' 56"
	assertEquals(new BigDecimal("43.298889"), new BigDecimal(series.getObject().get(MITECOAforosClient.LAT).toString()));
	assertEquals(new BigDecimal("-5.684722"), new BigDecimal(series.getObject().get(MITECOAforosClient.LON).toString()));
    }

    @Test
    public void coordinatesTest() {

	assertEquals(-5.684722, MITECOAforosClient.parsePackedDegrees("-54105").get(), 1e-6);
	assertEquals(0.5, MITECOAforosClient.parsePackedDegrees("3000").get(), 1e-6);
	assertEquals(-0.5, MITECOAforosClient.parsePackedDegrees("-3000").get(), 1e-6);
	// rounding artefact of the source
	assertEquals(43.283333, MITECOAforosClient.parsePackedDegrees("431660").get(), 1e-6);
	assertFalse(MITECOAforosClient.parsePackedDegrees("412281").isPresent());
	assertFalse(MITECOAforosClient.parsePackedDegrees("0").isPresent());
	assertFalse(MITECOAforosClient.parsePackedDegrees("").isPresent());

	// the ETRS89 UTM 30 coordinates of station 1033 agree with its WGS84 coordinates
	double[] latLon = MITECOAforosClient.utmToLatLon(282234, 4797522, 30);
	assertEquals(43.298889, latLon[0], 0.001);
	assertEquals(-5.684722, latLon[1], 0.001);

	// invalid WGS84 coordinates: the UTM ones are used
	Map<String, String> row = new HashMap<>();
	row.put("latwgs84", "412281");
	row.put("longwgs84", "-54105");
	row.put("xetrs89", "282234");
	row.put("yetrs89", "4797522");
	BigDecimal[] coordinates = MITECOAforosClient.parseCoordinates(row).get();
	assertEquals(43.2989, coordinates[0].doubleValue(), 0.001);

	row.put("xetrs89", "");
	assertFalse(MITECOAforosClient.parseCoordinates(row).isPresent());
    }

    @Test
    public void parseObservationsTest() throws Exception {

	TreeMap<Date, BigDecimal> values = MITECOAforosClient.parseObservations(reader(AFLIQ), "1033",
		MITECOAforosVariable.RIVER_LEVEL_DAILY, null, null);
	assertEquals(3, values.size());
	assertEquals(date("1912-01-01T00:00:00Z"), values.firstKey());
	assertEquals(new BigDecimal("-0.45"), values.lastEntry().getValue());

	values = MITECOAforosClient.parseObservations(reader(AFLIQ), "1033", MITECOAforosVariable.RIVER_LEVEL_DAILY,
		date("1912-01-02T00:00:00Z"), date("1913-01-01T00:00:00Z"));
	assertEquals(1, values.size());

	values = MITECOAforosClient.parseObservations(reader(AFLIQ), "1034", MITECOAforosVariable.RIVER_LEVEL_DAILY, null, null);
	assertEquals(2, values.size());

	values = MITECOAforosClient.parseObservations(reader(EVAP), "1001", MITECOAforosVariable.PAN_EVAPORATION_MONTHLY,
		date("1983-08-01T00:00:00Z"), null);
	assertEquals(2, values.size());
	assertEquals(new BigDecimal("2.8"), values.firstEntry().getValue());

	values = MITECOAforosClient.parseObservations(reader(AFLIQE), "1077", MITECOAforosVariable.RESERVOIR_STORAGE_DAILY, null,
		null);
	assertEquals(new BigDecimal("1.566"), values.lastEntry().getValue());
	assertTrue(MITECOAforosClient
		.parseObservations(reader(AFLIQE), "1077", MITECOAforosVariable.RESERVOIR_INFLOW_DAILY, null, null).isEmpty());
    }

    @Test
    public void archiveTest() throws Exception {

	String html = "<a href=\"/content/dam/miteco/es/agua/temas/evaluacion-de-los-recursos-hidricos/sistema-informacion-anuario-aforos/"
		+ "Anuario-20-21-csv.zip\">2020-21</a><a href=\"/content/dam/miteco/es/agua/temas/evaluacion-de-los-recursos-hidricos/"
		+ "sistema-informacion-anuario-aforos/Anuario-21-22-csv.zip\">csv</a><a href=\"/x/Anuario-21-22-mdb.zip\">mdb</a>";

	Archive archive = MITECOAforosClient.findArchive(html, MITECOAforosClient.DEFAULT_ENDPOINT).get();
	assertEquals(MITECOAforosClient.DEFAULT_ARCHIVE_URL, archive.getUrl());
	assertEquals("2021-22", archive.getEdition());

	assertFalse(MITECOAforosClient.findArchive("<html/>", MITECOAforosClient.DEFAULT_ENDPOINT).isPresent());

	assertEquals("Anuario-21-22-csv.zip", MITECOAforosClient.getFileName(MITECOAforosClient.DEFAULT_ARCHIVE_URL));
	assertEquals("other", MITECOAforosClient.parseEdition("https://example.org/other.zip"));

	File file = File.createTempFile("anuario", ".zip");
	file.deleteOnExit();
	try (FileOutputStream output = new FileOutputStream(file)) {
	    output.write(new byte[10]);
	}

	Properties local = new Properties();
	local.setProperty("Last-Modified", "Wed, 09 Sep 2026 13:57:36 GMT");

	Properties remote = new Properties();
	remote.setProperty("Content-Length", "10");
	remote.setProperty("Last-Modified", "Wed, 09 Sep 2026 13:57:36 GMT");
	assertTrue(MITECOAforosClient.isUnchanged(file, local, remote));

	remote.setProperty("Content-Length", "11");
	assertFalse(MITECOAforosClient.isUnchanged(file, local, remote));

	remote.setProperty("Content-Length", "10");
	remote.setProperty("Last-Modified", "Thu, 10 Sep 2026 13:57:36 GMT");
	assertFalse(MITECOAforosClient.isUnchanged(file, local, remote));
    }

    @Test
    public void zipTest() throws Exception {

	File file = File.createTempFile("anuario", ".zip");
	file.deleteOnExit();

	try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file), StandardCharsets.UTF_8)) {
	    zip.putNextEntry(new ZipEntry("MIÑO-SIL/"));
	    zip.putNextEntry(new ZipEntry("MIÑO-SIL/afliq.csv"));
	    zip.write(AFLIQ.getBytes(StandardCharsets.ISO_8859_1));
	    zip.putNextEntry(new ZipEntry("CANTABRICO/estaf.csv"));
	    zip.write(ESTAF.getBytes(StandardCharsets.ISO_8859_1));
	    zip.putNextEntry(new ZipEntry("Descripción_Tablas_Mto.pdf"));
	}

	try (ZipFile zip = new ZipFile(file, StandardCharsets.UTF_8)) {

	    List<String> basins = MITECOAforosClient.listBasins(zip);
	    assertEquals(Arrays.asList("CANTABRICO", "MIÑO-SIL"), basins);

	    try (BufferedReader reader = MITECOAforosClient.openEntry(zip, "MIÑO-SIL", "afliq.csv")) {
		assertEquals(2, MITECOAforosClient
			.parseObservations(reader, "1034", MITECOAforosVariable.RIVER_LEVEL_DAILY, null, null).size());
	    }

	    assertNull(MITECOAforosClient.openEntry(zip, "MIÑO-SIL", "afliqe.csv"));
	}
    }

    @Test
    public void seriesIdTest() {

	MITECOAforosEntity series = MITECOAforosClient.parseSeriesId("GALICIA COSTA|1601|RIVER_LEVEL_DAILY");
	assertEquals("GALICIA COSTA", series.getBasin());
	assertEquals("1601", series.getStationId());
	assertEquals(MITECOAforosVariable.RIVER_LEVEL_DAILY, series.getVariable().get());

	assertNull(MITECOAforosClient.parseSeriesId("EBRO|9001"));
	assertNull(MITECOAforosClient.parseSeriesId("EBRO|9001|X"));
	assertNull(MITECOAforosClient.parseSeriesId(null));
    }

    /**
     * @param text
     * @return
     */
    static BufferedReader reader(String text) {

	return new BufferedReader(new StringReader(text));
    }

    /**
     * @param text
     * @return
     */
    private static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
