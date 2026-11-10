package eu.essi_lab.accessor.lvlvgmc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.lvlvgmc.LVGMCClient.Resource;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;

/**
 * @author boldrini
 */
public class LVGMCClientTest {

    // records of the DataStore resources "Hidroloģiskās stacijas" and "Hidroloģiskie parametri"
    static final String STATION = "{\"_id\":7,\"STATION_ID\":\"HD073801\",\"NAME\":\"Jelgava\",\"BEGIN_DATE\":\"1920-01-01T00:00:00\","
	    + "\"END_DATE\":\"3999-12-31T23:59:00\",\"LATITUDE\":563918,\"LONGITUDE\":234406,\"GAUSS1\":\"483753.17\","
	    + "\"GAUSS2\":\"279014.59\",\"GEOGR1\":23.735,\"GEOGR2\":56.655,\"ELEVATION\":0.03}";

    static final String LEVEL = "{\"_id\":1,\"ABBREVIATION\":\"LIMEN\",\"EN_DESCRIPTION\":\"Water level, real, hourly average\","
	    + "\"LV_DESCRIPTION\":\"Ūdens līmenis, faktiskais, stundas vidējais\",\"SCALE\":1,\"LOWER_LIMIT\":-300,"
	    + "\"UPPER_LIMIT\":2000,\"MEASUREMENT_UNIT\":\"cm\"}";

    static final String SEA_LEVEL = "{\"_id\":3,\"ABBREVIATION\":\"SEHL\",\"EN_DESCRIPTION\":\"Sea level, last hour 15 minutes mean values\","
	    + "\"LV_DESCRIPTION\":\"Jūras līmenis, stundas pēdējo 15 minūšu vidējais\",\"SCALE\":1,\"LOWER_LIMIT\":375,"
	    + "\"UPPER_LIMIT\":735,\"MEASUREMENT_UNIT\":\"cm\"}";

    // records of a datastore_search_sql request (archive with p = 1, operational data with p = 2)
    static final String VALUES = "[{\"DATETIME\":\"2026-10-26T21:00:00\",\"VALUE\":99,\"p\":1},"
	    + "{\"DATETIME\":\"2026-10-26T22:00:00\",\"VALUE\":98,\"p\":1},"//
	    + "{\"DATETIME\":\"2026-10-26T22:00:00\",\"VALUE\":97,\"p\":2},"//
	    + "{\"DATETIME\":\"2026-10-26T23:00:00\",\"VALUE\":null,\"p\":2},"//
	    + "{\"DATETIME\":\"2026-10-27T00:00:00\",\"VALUE\":96.5,\"p\":2}]";

    // resources of the package_show response (only the used fields)
    static final String RESOURCES = "[{\"id\":\"17460efb-ae99-4d1d-8144-1068f184b05f\",\"name\":\"Meteoroloģiskie operatīvie dati\","
	    + "\"datastore_active\":true,\"url\":\"https://data.gov.lv/dati/dataset/40d80be5-0c09-47c4-80f3-fad4bec19f33/resource/"
	    + "17460efb-ae99-4d1d-8144-1068f184b05f/download/meteo_operativie_dati.csv\"},"
	    + "{\"id\":\"11111111-2222-3333-4444-555555555555\",\"name\":\"Hidroloģiskie arhīva dati\",\"datastore_active\":true,"
	    + "\"url\":\"https://data.gov.lv/dati/dataset/40d80be5-0c09-47c4-80f3-fad4bec19f33/resource/"
	    + "11111111-2222-3333-4444-555555555555/download/hidro_arhiva_dati.csv\"},"
	    + "{\"id\":\"66666666-2222-3333-4444-555555555555\",\"name\":\"Hidroloģiskās stacijas (renamed)\",\"datastore_active\":true,"
	    + "\"url\":\"https://data.gov.lv/dati/dataset/x/resource/66666666-2222-3333-4444-555555555555/download/hidro_stacijas.csv\"}]";

    /**
     * @param parameter
     * @return
     */
    static LVGMCEntity createSeries(String parameter) {

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2025-10-27T00:00:00Z").get();
	Date end = new Date();

	return LVGMCClient.createSeries(new JSONObject(STATION), new JSONObject(parameter), begin, end);
    }

    @Test
    public void createSeriesTest() {

	LVGMCEntity series = createSeries(LEVEL);

	assertEquals("HD073801|LIMEN", series.getSeriesId());
	assertEquals("HD073801", series.getStationId());
	assertEquals("Jelgava", series.getStationName());
	assertEquals("LIMEN", series.getAbbreviation());
	assertEquals("Water level", series.getPropertyName());
	assertEquals("Water level, real, hourly average", series.getEnglishDescription());
	assertEquals("cm", series.getUnit());
	assertEquals("centimetres", series.getUnitName());
	assertEquals("PT1H", series.getAggregationPeriod());
	assertTrue(series.isWaterLevel());
	assertEquals(56.655, series.getObject().getDouble(LVGMCClient.LATITUDE), 0);
	assertEquals(23.735, series.getObject().getDouble(LVGMCClient.LONGITUDE), 0);
	assertEquals("1920-01-01T00:00:00Z", series.getString(LVGMCClient.BEGIN_DATE));
	// the station is still in operation
	assertFalse(series.getObject().has(LVGMCClient.END_DATE));
	assertEquals("2025-10-27T00:00:00Z", series.getString(LVGMCClient.BEGIN));

	LVGMCEntity sea = createSeries(SEA_LEVEL);
	assertEquals("Sea level", sea.getPropertyName());
	assertEquals("PT15M", sea.getAggregationPeriod());
	assertFalse(sea.isWaterLevel());

	// unknown parameter
	LVGMCEntity unknown = LVGMCClient.createSeries(new JSONObject(STATION),
		new JSONObject("{\"ABBREVIATION\":\"QDEB\",\"EN_DESCRIPTION\":\"Discharge, hourly average\",\"MEASUREMENT_UNIT\":\"m3/s\"}"),
		new Date(), new Date());
	assertEquals("Discharge", unknown.getPropertyName());
	assertEquals("cubic metres per second", unknown.getUnitName());
    }

    @Test
    public void seriesIdTest() {

	LVGMCEntity series = LVGMCClient.parseSeriesId("HD073548|LIMEN");
	assertEquals("HD073548", series.getStationId());
	assertEquals("LIMEN", series.getAbbreviation());

	assertNull(LVGMCClient.parseSeriesId("HD073548"));
	assertNull(LVGMCClient.parseSeriesId("HD073548|LIMEN' OR '1'='1"));
	assertNull(LVGMCClient.parseSeriesId(null));
    }

    @Test
    public void parseDateTest() {

	Date expected = ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-26T22:00:00Z").get();

	assertEquals(expected, LVGMCClient.parseDate("2026-10-26T22:00:00").get());
	assertEquals(expected, LVGMCClient.parseDate("2026.10.26 22:00:00").get());
	assertEquals(expected, LVGMCClient.parseDate("2026-10-26T22:00:00Z").get());
	assertEquals("2026-10-26T22:00:00", LVGMCClient.formatSQLTime(expected));
	assertFalse(LVGMCClient.parseDate(null).isPresent());
	assertFalse(LVGMCClient.parseDate("null").isPresent());
    }

    @Test
    public void parseObservationsTest() {

	TreeMap<Date, BigDecimal> values = new TreeMap<>();
	LVGMCClient.parseObservations(new JSONArray(VALUES), values);

	assertEquals(3, values.size());
	// the archive value is kept
	assertEquals(0, new BigDecimal("98").compareTo(values.get(LVGMCClient.parseDate("2026-10-26T22:00:00").get())));
	assertEquals(new BigDecimal("96.5"), values.lastEntry().getValue());
    }

    @Test
    public void sqlTest() {

	LVGMCEntity series = LVGMCClient.parseSeriesId("HD073548|LIMEN");
	Date begin = LVGMCClient.parseDate("2026-01-01T00:00:00").get();
	Date end = LVGMCClient.parseDate("2026-02-01T00:00:00").get();

	assertEquals("SELECT \"DATETIME\", \"VALUE\", 1 AS p FROM \"a90de53b-e8b6-4cda-97c4-ecb86fbafc2d\" WHERE \"STATION_ID\" ="
		+ " 'HD073548' AND \"ABBREVIATION\" = 'LIMEN' AND \"DATETIME\" BETWEEN '2026-01-01T00:00:00' AND '2026-02-01T00:00:00'",
		LVGMCClient.selectValues(Resource.ARCHIVE.getDefaultId(), series, begin, end, 1));

	assertTrue(LVGMCClient.selectTimes(Resource.OPERATIONAL.getDefaultId(), null).endsWith("FROM \""
		+ Resource.OPERATIONAL.getDefaultId() + "\""));

	try {
	    LVGMCClient.checkCode("HD073548'");
	    assertTrue(false);
	} catch (IllegalArgumentException e) {
	    // expected
	}

	try {
	    LVGMCClient.checkResourceId("a90de53b\" ; x");
	    assertTrue(false);
	} catch (IllegalArgumentException e) {
	    // expected
	}
    }

    @Test
    public void findResourcesTest() {

	Map<Resource, String> resources = LVGMCClient.findResources(new JSONArray(RESOURCES));

	// found by name
	assertEquals("11111111-2222-3333-4444-555555555555", resources.get(Resource.ARCHIVE));
	// found by file name
	assertEquals("66666666-2222-3333-4444-555555555555", resources.get(Resource.STATIONS));
	// default
	assertEquals(Resource.OPERATIONAL.getDefaultId(), resources.get(Resource.OPERATIONAL));
	assertEquals(Resource.PARAMETERS.getDefaultId(), LVGMCClient.findResources(null).get(Resource.PARAMETERS));
    }

    @Test
    public void endpointTest() {

	String[] parsed = LVGMCClient.parseEndpoint(LVGMCClient.DEFAULT_ENDPOINT);
	assertEquals("https://data.gov.lv/dati/api/3/action/", parsed[0]);
	assertEquals(LVGMCClient.DATASET_ID, parsed[1]);

	parsed = LVGMCClient.parseEndpoint("https://data.gov.lv/dati/lv/dataset/hidrometeorologiskie-noverojumi/");
	assertEquals("https://data.gov.lv/dati/lv/api/3/action/", parsed[0]);
	assertEquals("hidrometeorologiskie-noverojumi", parsed[1]);

	parsed = LVGMCClient.parseEndpoint("https://data.gov.lv/dati/api/3/action/package_show?id=hidrometeorologiskie-noverojumi");
	assertEquals("https://data.gov.lv/dati/api/3/action/", parsed[0]);
	assertEquals("hidrometeorologiskie-noverojumi", parsed[1]);

	parsed = LVGMCClient.parseEndpoint(null);
	assertEquals("https://data.gov.lv/dati/api/3/action/", parsed[0]);
    }

    @Test
    public void supportsTest() {

	GSSource source = new GSSource();
	source.setEndpoint(LVGMCClient.DEFAULT_ENDPOINT);
	assertTrue(new LVGMCConnector().supports(source));

	source.setEndpoint("https://data.gov.lv/dati/dataset/hidrometeorologiskie-noverojumi");
	assertTrue(new LVGMCConnector().supports(source));

	source.setEndpoint("https://data.gov.lv/dati/dataset/klimatiskie-dati");
	assertFalse(new LVGMCConnector().supports(source));
    }

    @Test
    public void settingTest() {

	LVGMCConnectorSetting setting = new LVGMCConnectorSetting();
	assertTrue(setting.getParameters().isEmpty());
	assertTrue(setting.getStations().isEmpty());

	setting.setParameters(" limen, WTEMD,LIMEN ");
	setting.setStations("HD073548, HD073801");

	assertEquals(2, setting.getParameters().size());
	assertEquals("LIMEN", setting.getParameters().get(0));
	assertEquals(2, setting.getStations().size());
	assertEquals("HD073801", setting.getStations().get(1));
    }
}
