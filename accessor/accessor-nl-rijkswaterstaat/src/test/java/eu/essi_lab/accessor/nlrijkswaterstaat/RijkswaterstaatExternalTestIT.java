package eu.essi_lab.accessor.nlrijkswaterstaat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class RijkswaterstaatExternalTestIT {

    private static final String WATER_LEVEL = "hoekvanholland|meting|OW|WATHTE|cm|NAP|NVT|NVT|NVT|NVT|";

    @Test
    public void testSeriesAndObservations() throws GSException {

	RijkswaterstaatClient client = new RijkswaterstaatClient(
		System.getProperty("rijkswaterstaatEndpoint", RijkswaterstaatClient.DEFAULT_ENDPOINT));

	List<RijkswaterstaatEntity> series = client.retrieveSeries(Arrays.asList(RijkswaterstaatClient.MEASUREMENT_PROCESS_TYPE));
	assertTrue(series.size() > 10000);

	// not only discharge: many observed properties are published
	long quantities = series.stream().map(s -> s.getCode("Grootheid")).distinct().count();
	assertTrue(quantities > 50);

	RijkswaterstaatEntity waterLevel = series.stream().filter(s -> s.getSeriesId().equals(WATER_LEVEL)).findFirst().get();

	Map<String, SimpleEntry<Date, Date>> latest = client.retrieveLatestObservations(Arrays.asList(waterLevel));
	SimpleEntry<Date, Date> extent = latest.get(WATER_LEVEL);
	assertNotNull(extent);
	// real time series
	assertTrue(System.currentTimeMillis() - extent.getValue().getTime() < TimeUnit.DAYS.toMillis(7));

	Date begin = client.estimateFirstObservation(waterLevel, extent.getKey(), 6);
	assertTrue(begin.before(extent.getKey()));

	Date end = extent.getValue();
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, new Date(end.getTime() - TimeUnit.DAYS.toMillis(2)),
		end);
	assertFalse(values.isEmpty());
	assertEquals(end, values.lastKey());
    }

    @Test
    public void testExactFirstObservation() throws GSException {

	RijkswaterstaatClient client = new RijkswaterstaatClient();
	RijkswaterstaatEntity waterLevel = RijkswaterstaatClient.parseSeriesId(WATER_LEVEL);

	SimpleEntry<Date, Date> extent = client.retrieveLatestObservations(Arrays.asList(waterLevel)).get(WATER_LEVEL);
	Date begin = client.findFirstObservation(waterLevel, extent.getKey());

	assertEquals("1900-01-01T00:10:00Z", ISO8601DateTimeUtils.getISO8601DateTime(begin));
    }

    @Test
    public void testLargeObservationRequestIsSplit() throws GSException {

	RijkswaterstaatClient client = new RijkswaterstaatClient();

	RijkswaterstaatEntity waterLevel = RijkswaterstaatClient.parseSeriesId(WATER_LEVEL);
	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(3 * 366));

	// three years of 10 minutes data exceed the maximum number of observations of a single request
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, begin, end);
	assertTrue(values.size() > 100000);
    }

    @Test
    public void testConnector() throws GSException {

	RijkswaterstaatConnector connector = new RijkswaterstaatConnector();
	connector.setSourceURL(RijkswaterstaatClient.DEFAULT_ENDPOINT);
	connector.getSetting().setLocationCodePrefix("hoekvanholland");
	connector.getSetting().setBeginSearchMaxRequests(2);
	connector.getSetting().setMaxRecords(10);

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(new ListRecordsRequest());

	List<OriginalMetadata> records = response.getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new RijkswaterstaatEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(RijkswaterstaatClient.BEGIN))).//
		map(RijkswaterstaatEntity::getSeriesId).//
		collect(Collectors.toList());

	assertTrue(ids.stream().allMatch(id -> id.startsWith("hoekvanholland")));
    }

    @Test
    public void testConnectorExactBegin() throws GSException {

	RijkswaterstaatConnector connector = new RijkswaterstaatConnector();
	connector.setSourceURL(RijkswaterstaatClient.DEFAULT_ENDPOINT);
	connector.getSetting().setLocationCodePrefix("hoekvanholland");
	connector.getSetting().setExactBegin(true);
	connector.getSetting().setMaxRecords(3);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	RijkswaterstaatClient client = new RijkswaterstaatClient();

	for (OriginalMetadata record : records) {

	    RijkswaterstaatEntity series = new RijkswaterstaatEntity(record.getMetadata());
	    Date begin = ISO8601DateTimeUtils.parseISO8601ToDate(series.getObject().getString(RijkswaterstaatClient.BEGIN)).get();

	    // the harvested begin is an observation, and there are no observations before it
	    assertFalse(client.retrieveObservations(series, begin, begin).isEmpty());
	    assertFalse(client.hasObservations(series, RijkswaterstaatClient.EARLIEST_DATE, new Date(begin.getTime() - 1000)));
	}
    }
}
