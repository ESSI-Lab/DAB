package eu.essi_lab.accessor.nlrijkswaterstaat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class RijkswaterstaatClientTest {

    private static final String LOCATION = "{\"Locatie_MessageID\":1319,\"Code\":\"hoekvanholland\",\"Coordinatenstelsel\":\"ETRS89\","
	    + "\"Lat\":51.976899,\"Lon\":4.119827,\"Naam\":\"Hoek van Holland\",\"Omschrijving\":\"rechter oever kilometer 1030.1\"}";

    private static final String WATER_LEVEL = "{\"AquoMetadata_MessageID\":2140,"
	    + "\"Compartiment\":{\"Code\":\"OW\",\"Omschrijving\":\"Oppervlaktewater\"},"
	    + "\"Grootheid\":{\"Code\":\"WATHTE\",\"Omschrijving\":\"Waterhoogte\"},"
	    + "\"Eenheid\":{\"Code\":\"cm\",\"Omschrijving\":\"centimeter\"},"
	    + "\"Hoedanigheid\":{\"Code\":\"NAP\",\"Omschrijving\":\"t.o.v. Normaal Amsterdams Peil\"},"
	    + "\"Parameter\":{\"Code\":\"NVT\",\"Omschrijving\":\"NVT\"},"
	    + "\"BioTaxon\":{\"Code\":\"NVT\",\"Omschrijving\":\"NVT\"},"
	    + "\"Orgaan\":{\"Code\":\"NVT\",\"Omschrijving\":\"NVT\"},"
	    + "\"Typering\":{\"Code\":\"NVT\",\"Omschrijving\":\"NVT\"},"
	    + "\"Groepering\":{\"Code\":\"\",\"Omschrijving\":\"\"},"
	    + "\"Parameter_Wat_Omschrijving\":\"Waterhoogte in Oppervlaktewater t.o.v. Normaal Amsterdams Peil in cm\","
	    + "\"ProcesType\":\"meting\"}";

    static RijkswaterstaatEntity createWaterLevelSeries() {

	return RijkswaterstaatClient.createSeries(new JSONObject(LOCATION), new JSONObject(WATER_LEVEL));
    }

    @Test
    public void createSeriesTest() {

	RijkswaterstaatEntity series = createWaterLevelSeries();

	assertEquals("hoekvanholland|meting|OW|WATHTE|cm|NAP|NVT|NVT|NVT|NVT|", series.getSeriesId());
	assertEquals("hoekvanholland", series.getLocationCode());
	assertEquals("WATHTE", series.getCode("Grootheid"));
	assertEquals("Waterhoogte", series.getDescription("Grootheid"));
	assertEquals("meting", series.getProcessType());
	assertEquals("Hoek van Holland", series.getObject().getString(RijkswaterstaatClient.LOCATION_NAME));
	assertEquals(51.976899, series.getObject().getDouble(RijkswaterstaatClient.LAT), 0);
    }

    @Test
    public void seriesIdRoundTripTest() {

	RijkswaterstaatEntity series = createWaterLevelSeries();
	RijkswaterstaatEntity parsed = RijkswaterstaatClient.parseSeriesId(series.getSeriesId());

	assertEquals(series.getLocationCode(), parsed.getLocationCode());
	assertEquals(series.getProcessType(), parsed.getProcessType());

	for (String key : RijkswaterstaatClient.AQUO_KEYS) {
	    assertEquals(series.getCode(key), parsed.getCode(key));
	}

	JSONObject request = RijkswaterstaatClient.createAquoMetadataRequest(parsed);
	assertEquals(RijkswaterstaatClient.createAquoMetadataRequest(series).toString(), request.toString());
    }

    @Test
    public void invalidSeriesIdTest() {

	assertNull(RijkswaterstaatClient.parseSeriesId(null));
	assertNull(RijkswaterstaatClient.parseSeriesId("hoekvanholland"));
	assertNull(RijkswaterstaatClient.parseSeriesId("|meting|OW|WATHTE|cm|NAP|NVT|NVT|NVT|NVT|"));
    }

    @Test
    public void aquoMetadataRequestTest() {

	JSONObject request = RijkswaterstaatClient.createAquoMetadataRequest(createWaterLevelSeries());

	assertEquals("WATHTE", request.getJSONObject("Grootheid").getString("Code"));
	assertEquals("meting", request.getString("ProcesType"));
	// empty codes are not sent
	assertFalse(request.has("Groepering"));
    }

    @Test
    public void formatDateTest() {

	Date date = ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-26T13:45:00Z").get();
	assertEquals("2026-10-26T13:45:00.000+00:00", RijkswaterstaatClient.formatDate(date));
    }

    @Test
    public void estimateFirstObservationTest() {

	Date earliest = ISO8601DateTimeUtils.parseISO8601ToDate("1700-01-01T00:00:00Z").get();
	Date first = ISO8601DateTimeUtils.parseISO8601ToDate("1987-03-15T00:00:00Z").get();
	Date known = ISO8601DateTimeUtils.parseISO8601ToDate("2026-10-26T00:00:00Z").get();

	List<Date[]> requests = new ArrayList<>();

	Date estimate = RijkswaterstaatClient.estimateFirstObservation(earliest, known, 6, (begin, end) -> {
	    requests.add(new Date[] { begin, end });
	    return !end.before(first);
	});

	assertEquals(6, requests.size());
	// no observation is excluded and the error is at most (known - earliest) / 2^6
	assertFalse(estimate.after(first));
	long resolution = (known.getTime() - earliest.getTime()) / 64;
	assertTrue(first.getTime() - estimate.getTime() <= resolution);
    }

    @Test
    public void estimateFirstObservationWithoutRequestsTest() {

	Date earliest = ISO8601DateTimeUtils.parseISO8601ToDate("1700-01-01T00:00:00Z").get();
	Date known = ISO8601DateTimeUtils.parseISO8601ToDate("1950-01-01T00:00:00Z").get();

	Date estimate = RijkswaterstaatClient.estimateFirstObservation(earliest, known, 0, (begin, end) -> {
	    throw new IllegalStateException("No requests expected");
	});

	assertEquals(known, estimate);
    }

    @Test
    public void narrowFirstObservationToOneDayTest() {

	Date earliest = ISO8601DateTimeUtils.parseISO8601ToDate("1700-01-01T00:00:00Z").get();
	Date first = ISO8601DateTimeUtils.parseISO8601ToDate("1900-01-01T00:10:00Z").get();
	Date known = ISO8601DateTimeUtils.parseISO8601ToDate("1900-12-31T07:10:00Z").get();

	int[] requests = new int[1];

	SimpleEntry<Date, Date> interval = RijkswaterstaatClient.narrowFirstObservation(earliest, known, Integer.MAX_VALUE,
		RijkswaterstaatClient.EXACT_SEARCH_WINDOW, (begin, end) -> {
		    requests[0]++;
		    return !end.before(first);
		});

	// same sequence as the one observed on the Hoek van Holland water level series
	assertEquals(17, requests[0]);
	assertFalse(interval.getKey().after(first));
	assertFalse(interval.getValue().before(first));
	assertTrue(interval.getValue().getTime() - interval.getKey().getTime() <= RijkswaterstaatClient.EXACT_SEARCH_WINDOW);
    }

    @Test
    public void exactBeginSettingTest() {

	RijkswaterstaatConnectorSetting setting = new RijkswaterstaatConnectorSetting();
	assertFalse(setting.isExactBegin());

	setting.setExactBegin(true);
	assertTrue(setting.isExactBegin());

	assertTrue(new RijkswaterstaatConnectorSetting(setting.getObject()).isExactBegin());
    }

    @Test
    public void pageTest() {

	JSONObject otherLocation = new JSONObject(LOCATION).put("Code", "lobith.bovenrijn.tolkamer");

	List<RijkswaterstaatEntity> series = new ArrayList<>();
	for (int i = 0; i < 3; i++) {
	    series.add(RijkswaterstaatClient.createSeries(new JSONObject(LOCATION), new JSONObject(WATER_LEVEL)));
	}
	series.add(RijkswaterstaatClient.createSeries(otherLocation, new JSONObject(WATER_LEVEL)));

	assertEquals(2, RijkswaterstaatConnector.getPage(series, 0, 2).size());
	assertEquals(3, RijkswaterstaatConnector.getPage(series, 0, 50).size());
	assertEquals(1, RijkswaterstaatConnector.getPage(series, 3, 50).size());
	assertEquals("lobith.bovenrijn.tolkamer", RijkswaterstaatConnector.getPage(series, 3, 50).get(0).getLocationCode());
    }
}
