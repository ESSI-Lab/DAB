package eu.essi_lab.accessor.dkvanda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class VanDaClientTest {

    /**
     * An element of the station list (some examinations removed, a placeholder and an empty examination added)
     */
    static final String STATION = "{\"stationUid\":\"475b1938-089c-4ee5-85b0-bbf1badb5762\",\"stationId\":\"21006844\","
	    + "\"operatorStationId\":\"\",\"oldStationNumber\":\"21000072\",\"locationType\":\"Vandløb\",\"locationTypeSc\":1,"
	    + "\"stationOwnerCvr\":\"DK25798376\",\"stationOwnerName\":\"Miljøstyrelsen\",\"operatorCvr\":\"DK31119103\","
	    + "\"operatorName\":\"DCE, Aarhus Universitet\",\"name\":\"Ellerup Bæk, Bæk, Ved Vejbro\",\"dguNumber\":null,"
	    + "\"description\":\"Opland = 4,13 km2\",\"catchmentAreaKm2\":null,\"loggerId\":\"EL1K-010884\","
	    + "\"location\":{\"x\":547271.3133,\"y\":6231639.0415,\"srid\":\"25832\"},\"measurementPoints\":[{\"number\":1,"
	    + "\"name\":\"Sted 1\",\"measurementPointType\":\"Vandløb\",\"measurementPointTypeSc\":1,\"description\":null,"
	    + "\"location\":{\"x\":547271.3133,\"y\":6231639.0415,\"srid\":\"25832\"},\"intakeNumber\":null,\"intakeDepthM\":null,"
	    + "\"elevationCorrectedIntakeDepthM\":null,\"bottomDepthM\":null,\"examinations\":["
	    + "{\"parameter\":\"Vandføring\",\"parameterSc\":1155,\"examinationType\":\"Vandføring Feltmåling\",\"examinationTypeSc\":24,"
	    + "\"unit\":\"l/s\",\"unitSc\":55,\"firstResult\":\"2023-12-15T11:11:00.00Z\",\"latestResult\":\"2026-09-30T08:08:00.00Z\"},"
	    + "{\"parameter\":\"Vandstand DVR90\",\"parameterSc\":439,\"examinationType\":\"Kotemåling\",\"examinationTypeSc\":21,"
	    + "\"unit\":\"m\",\"unitSc\":63,\"firstResult\":\"1997-09-08T23:01:00.00Z\",\"latestResult\":\"2026-09-25T10:15:00.00Z\"},"
	    + "{\"parameter\":\"Vandstand\",\"parameterSc\":1233,\"examinationType\":\"Vandstand\",\"examinationTypeSc\":25,"
	    + "\"unit\":\"cm\",\"unitSc\":19,\"firstResult\":\"2023-11-03T06:30:00.00Z\",\"latestResult\":\"2026-10-27T14:00:00.00Z\"},"
	    + "{\"parameter\":\"Datalogger spænding\",\"parameterSc\":2593,\"examinationType\":\"Batterispænding\",\"examinationTypeSc\":42,"
	    + "\"unit\":\"Volt \",\"unitSc\":305,\"firstResult\":\"2025-05-13T08:00:00.00Z\",\"latestResult\":\"2026-10-27T14:00:00.00Z\"}]},"
	    + "{\"number\":2,\"name\":\"Sted 2\",\"measurementPointType\":\"Vandløb\",\"measurementPointTypeSc\":1,\"description\":null,"
	    + "\"location\":null,\"examinations\":["
	    + "{\"parameter\":\"Vandstand DVR90\",\"parameterSc\":439,\"examinationType\":\"Kotemåling\",\"examinationTypeSc\":21,"
	    + "\"unit\":\"m\",\"unitSc\":63,\"firstResult\":\"1900-01-01T00:00:00.00Z\",\"latestResult\":\"1900-01-01T00:00:00.00Z\"},"
	    + "{\"parameter\":\"Vandføring\",\"parameterSc\":1155,\"examinationType\":\"Vandføring Feltmåling\",\"examinationTypeSc\":24,"
	    + "\"unit\":\"l/s\",\"unitSc\":55,\"firstResult\":null,\"latestResult\":null}]}]}";

    /**
     * A response of the measurements endpoint (some fields removed, a null result added)
     */
    static final String MEASUREMENTS = "[{\"stationId\":\"21006844\",\"operatorStationId\":\"\","
	    + "\"stationName\":\"Ellerup Bæk, Bæk, Ved Vejbro\",\"results\":["
	    + "{\"measurementPointNumber\":1,\"parameterSc\":1233,\"parameter\":\"Vandstand\",\"examinationTypeSc\":25,"
	    + "\"examinationType\":\"Vandstand\",\"measurementDateTime\":\"2026-10-26T12:00:00.00Z\",\"depthM\":null,\"result\":33,"
	    + "\"resultElevationCorrected\":2881.9,\"unitSc\":19,\"unit\":\"cm\"},"
	    + "{\"measurementPointNumber\":1,\"parameterSc\":1233,\"parameter\":\"Vandstand\",\"examinationTypeSc\":25,"
	    + "\"examinationType\":\"Vandstand\",\"measurementDateTime\":\"2026-10-26T11:50:00.00Z\",\"depthM\":null,\"result\":null,"
	    + "\"resultElevationCorrected\":null,\"unitSc\":19,\"unit\":\"cm\"},"
	    + "{\"measurementPointNumber\":1,\"parameterSc\":1233,\"parameter\":\"Vandstand\",\"examinationTypeSc\":25,"
	    + "\"examinationType\":\"Vandstand\",\"measurementDateTime\":\"2026-10-26T11:40:00.00Z\",\"depthM\":null,\"result\":33.1,"
	    + "\"resultElevationCorrected\":2882,\"unitSc\":19,\"unit\":\"cm\"}]}]";

    /**
     * @return
     */
    static List<VanDaEntity> createSeries() {

	return VanDaClient.createSeries(new JSONObject(STATION), Collections.emptyList());
    }

    /**
     * @return
     */
    static VanDaEntity createWaterLevelSeries() {

	return createSeries().stream().filter(s -> s.getExaminationTypeCode() == 25).findFirst().get();
    }

    @Test
    public void createSeriesTest() {

	List<VanDaEntity> series = createSeries();

	// placeholder dates and examinations without results are discarded
	assertEquals(4, series.size());

	VanDaEntity waterLevel = createWaterLevelSeries();
	assertEquals("21006844|1|25|1233", waterLevel.getSeriesId());
	assertEquals("21006844", waterLevel.getStationId());
	assertEquals("Ellerup Bæk, Bæk, Ved Vejbro", waterLevel.getStationName());
	assertEquals(1, waterLevel.getMeasurementPointNumber());
	assertEquals(1233, waterLevel.getParameterCode());
	assertEquals("cm", waterLevel.getUnit());
	assertEquals("2023-11-03T06:30:00Z", waterLevel.getString(VanDaClient.BEGIN));
	assertEquals("2026-10-27T14:00:00Z", waterLevel.getString(VanDaClient.END));
	assertEquals("Sted 1", waterLevel.getString(VanDaClient.MEASUREMENT_POINT_NAME_KEY));
	assertEquals(2, waterLevel.getObject().getInt(VanDaClient.MEASUREMENT_POINTS_COUNT));

	// ETRS89 / UTM 32N converted to WGS 84 (about 56.24 N, 9.76 E)
	double lat = waterLevel.getObject().getDouble(VanDaClient.LAT);
	double lon = waterLevel.getObject().getDouble(VanDaClient.LON);
	assertEquals(56.237, lat, 0.01);
	assertEquals(9.762, lon, 0.01);

	// non breaking spaces are removed
	assertEquals("Volt", series.stream().filter(s -> s.getExaminationTypeCode() == 42).findFirst().get().getUnit());

	// excluded examination types
	List<VanDaEntity> filtered = VanDaClient.createSeries(new JSONObject(STATION), Arrays.asList(21, 42));
	assertEquals(2, filtered.size());
	assertTrue(filtered.stream().noneMatch(s -> s.getExaminationTypeCode() == 21 || s.getExaminationTypeCode() == 42));
    }

    @Test
    public void seriesIdTest() {

	VanDaEntity series = VanDaClient.parseSeriesId("21006844|2|27|1155");
	assertEquals("21006844", series.getStationId());
	assertEquals(2, series.getMeasurementPointNumber());
	assertEquals(27, series.getExaminationTypeCode());
	assertEquals(1155, series.getParameterCode());
	assertEquals("21006844|2|27|1155", series.getSeriesId());

	assertNull(VanDaClient.parseSeriesId("21006844|2|27"));
	assertNull(VanDaClient.parseSeriesId("21006844|x|27|1155"));
	assertNull(VanDaClient.parseSeriesId("|1|27|1155"));
	assertNull(VanDaClient.parseSeriesId(null));
    }

    @Test
    public void parseMeasurementsTest() {

	TreeMap<Date, BigDecimal> values = VanDaClient.parseMeasurements(new JSONArray(MEASUREMENTS), createWaterLevelSeries());

	// null results are discarded
	assertEquals(2, values.size());
	assertEquals(date("2026-10-26T11:40:00Z"), values.firstKey());
	assertEquals(new BigDecimal("33.1"), values.firstEntry().getValue());
	assertEquals(new BigDecimal("33"), values.lastEntry().getValue());

	// results of other series are discarded
	assertTrue(VanDaClient.parseMeasurements(new JSONArray(MEASUREMENTS), VanDaClient.parseSeriesId("21006844|2|25|1233")).isEmpty());
    }

    @Test
    public void splitPeriodTest() {

	Date begin = date("2025-01-01T00:00:30Z");
	Date end = date("2025-03-15T12:00:10Z");

	List<SimpleEntry<Date, Date>> windows = VanDaClient.splitPeriod(begin, end);

	assertEquals(3, windows.size());
	assertEquals("2025-01-01T00:00Z", VanDaClient.formatDate(windows.get(0).getKey()));
	assertEquals("2025-02-01T00:00Z", VanDaClient.formatDate(windows.get(0).getValue()));
	assertEquals("2025-02-01T00:01Z", VanDaClient.formatDate(windows.get(1).getKey()));
	assertEquals("2025-03-15T12:01Z", VanDaClient.formatDate(windows.get(2).getValue()));

	for (SimpleEntry<Date, Date> window : windows) {
	    assertFalse(window.getValue().getTime() - window.getKey().getTime() > VanDaClient.MAX_REQUEST_WINDOW);
	}

	List<SimpleEntry<Date, Date>> single = VanDaClient.splitPeriod(begin, new Date(begin.getTime() + TimeUnit.HOURS.toMillis(1)));
	assertEquals(1, single.size());
    }

    @Test
    public void parseResultDateTest() {

	assertEquals("2023-12-15T11:11:00Z", VanDaClient.parseResultDate("2023-12-15T11:11:00.00Z").get());
	assertFalse(VanDaClient.parseResultDate("1900-01-01T00:00:00.00Z").isPresent());
	assertFalse(VanDaClient.parseResultDate("1916-12-31T23:00:00.00Z").isPresent());
	assertFalse(VanDaClient.parseResultDate(null).isPresent());
    }

    /**
     * @param text
     * @return
     */
    private static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
