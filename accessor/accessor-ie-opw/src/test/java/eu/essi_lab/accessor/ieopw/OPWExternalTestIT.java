package eu.essi_lab.accessor.ieopw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class OPWExternalTestIT {

    private static final String WATER_LEVEL = "01041|S";

    @Test
    public void testSeries() throws GSException {

	OPWClient client = new OPWClient(System.getProperty("opwEndpoint", OPWClient.DEFAULT_ENDPOINT));

	List<OPWEntity> series = client.retrieveSeries(Arrays.asList(OPWParameter.values()));
	assertTrue(series.size() > 500);

	// only stations suitable for republication
	assertTrue(series.stream().allMatch(s -> OPWClient.isRepublishable(s.getStationNumber())));

	OPWEntity waterLevel = series.stream().filter(s -> s.getSeriesId().equals(WATER_LEVEL)).findFirst().get();
	assertEquals("Sandy Mills", waterLevel.getStationName());

	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(waterLevel.getString(OPWClient.END)).get();
	// real time series
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(7));
    }

    @Test
    public void testFirstObservation() throws GSException {

	Date begin = new OPWClient().retrieveFirstObservation(OPWClient.parseSeriesId(WATER_LEVEL)).get();

	assertEquals("1973-10-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(begin));
    }

    @Test
    public void testRecentObservations() throws GSException {

	OPWClient client = new OPWClient();
	OPWEntity waterLevel = OPWClient.parseSeriesId(WATER_LEVEL);

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(2));

	// from the week feed, 15 minutes values
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, begin, end);
	assertTrue(values.size() > 100);
	assertFalse(values.firstKey().before(begin));

	// from the month feed
	begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(20));
	assertTrue(client.retrieveObservations(waterLevel, begin, end).size() > 1500);
    }

    @Test
    public void testArchiveObservations() throws GSException {

	OPWClient client = new OPWClient();
	OPWEntity waterLevel = OPWClient.parseSeriesId(WATER_LEVEL);

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("1990-01-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("1990-01-02T00:00:00Z").get();

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, begin, end);
	assertEquals(97, values.size());
	assertEquals(begin, values.firstKey());
	assertEquals(end, values.lastKey());
    }

    @Test
    public void testArchiveAndFeedObservations() throws GSException {

	OPWClient client = new OPWClient();
	OPWEntity waterLevel = OPWClient.parseSeriesId(WATER_LEVEL);

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(60));

	// the archive is completed with the most recent observations of the month feed
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, begin, end);
	assertTrue(values.size() > 5000);
	assertTrue(end.getTime() - values.lastKey().getTime() < TimeUnit.DAYS.toMillis(1));
    }

    @Test
    public void testConnector() throws GSException {

	OPWConnector connector = new OPWConnector();
	connector.setSourceURL(OPWClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("01041,25017");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new OPWEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(OPWClient.BEGIN))).//
		map(OPWEntity::getSeriesId).//
		collect(Collectors.toList());

	assertTrue(ids.contains("01041|S"));
	assertTrue(ids.contains("01041|Q"));
	assertTrue(ids.stream().allMatch(id -> id.startsWith("01041|") || id.startsWith("25017|")));
    }
}
