package eu.essi_lab.access.datacache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;
import eu.essi_lab.access.datacache.opensearch.OpenSearchConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.StorageInfo;

public class OpenSearchConnectorInternalTestIT {

    private static final String DATA_ID = "My-data-id";

    private DataCacheConnector connector;

    // docker run -p 9200:9200 -p 9600:9600 -e "discovery.type=single-node" -e "plugins.security.disabled=true"
    // opensearchproject/opensearch:1.3.5

    

    public static final StorageInfo ES_STORAGE = new StorageInfo(System.getProperty("es.host"));


    @Before
    public void before() throws Exception {
	connector = DataCacheConnectorFactory.newDataCacheConnector(DataConnectorType.OPEN_SEARCH_DOCKERHUB_1_3,
		new URL(System.getProperty("es.host")), System.getProperty("es.user"), System.getProperty("es.password"), "my-db");
	// connector = DataConnectorFactory.newDataConnector(DataConnectorType.OPENS_SEARCH_AWS_1_3,
	// new URL(System.getProperty("dataCacheHost")), System.getProperty("dataCacheUser"),
	// System.getProperty("dataCachePassword"),
	// "my-db");

	connector.configure(OpenSearchConnector.FLUSH_INTERVAL_MS, "1000");
	connector.configure(OpenSearchConnector.MAX_BULK_SIZE, "1000");
	connector.configure(OpenSearchConnector.CACHED_DAYS, "2");

	connector.deleteBefore(null, null);

	while ((long) connector.count() != 0) {
	    Thread.sleep(1000);
	}
    }

    @After
    public void after() throws Exception {
	connector.deleteBefore(null, null);

	while ((long) connector.count() != 0) {
	    Thread.sleep(1000);
	}

	connector.close();
    }

    @Test
    public void testStations() throws Exception {

	Thread.sleep(1000);
	// connector.deleteStations(null);
	connector.writeStation(new StationRecord(new BBOX4326(new BigDecimal(0), new BigDecimal(0), new BigDecimal(0), new BigDecimal(0)),
		null, null, "m1", "Station 1", "downloadUrl", "featureInfo", "a", "sourceIdentifier"));
	connector.writeStation(
		new StationRecord(new BBOX4326(new BigDecimal(45), new BigDecimal(45), new BigDecimal(170), new BigDecimal(170)), null,
			null, "m2", "Station 2", "downloadUrl", "featureInfo", "a", "sourceIdentifier"));
	connector.writeStation(
		new StationRecord(new BBOX4326(new BigDecimal(30), new BigDecimal(32), new BigDecimal(20), new BigDecimal(61)), null, null,
			"m3", "Station 3", "downloadUrl", "featureInfo", "a", "sourceIdentifier"));
	Thread.sleep(1000);
	assertEquals(0, connector.getStationsWithProperties(
		new BBOX4326(new BigDecimal(2), new BigDecimal(4), new BigDecimal(0), new BigDecimal(1)), null, null, true).size());

	assertEquals(3,
		connector.getStationsWithProperties(
			new BBOX4326(new BigDecimal(-90), new BigDecimal(90), new BigDecimal(-180), new BigDecimal(180)), null, null, true)
			.size());

	assertEquals(1,
		connector
			.getStationsWithProperties(
				new BBOX4326(new BigDecimal(30), new BigDecimal(31), new BigDecimal(20), new BigDecimal(21)), null, null, true)
			.size());

    }

    @Test
    public void testWritesDeletes() throws Exception {
	assertEquals(0l, (long) connector.count());
	List<DataRecord> records = getFakeRecords(DATA_ID, 10, 1000); // 10 seconds, 1 record per second
	connector.write(records);
	connector.waitForFlush();
	Thread.sleep(1000);
	assertEquals(10l, (long) connector.count());
	connector.deleteBefore(null, null);
	Thread.sleep(1000);
	assertEquals(0l, (long) connector.count());
	connector.write(records);
	connector.waitForFlush();
	Thread.sleep(1000);
	assertEquals(10l, (long) connector.count());
	Date date = connector.getFirstDate(DATA_ID);
	assertEquals("1982-01-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(date));
	Thread.sleep(1000);
	connector.deleteBefore(ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T00:00:05Z").get(), null);
	Thread.sleep(1000);
	date = connector.getFirstDate(DATA_ID);
	assertEquals("1982-01-01T00:00:05Z", ISO8601DateTimeUtils.getISO8601DateTime(date));
	Thread.sleep(1000);
	assertEquals(5l, (long) connector.count());
    }

    @Test
    public void testGetLastRecords() throws Exception {
	assertEquals(0l, (long) connector.count());
	List<DataRecord> records = new ArrayList<>();
	Date base = ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T00:00:00Z").get();
	long ms = 1000l;
	records.add(new DataRecord(new Date(base.getTime() + 1 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.add(new DataRecord(new Date(base.getTime() + 2 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.add(new DataRecord(new Date(base.getTime() + 3 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.add(new DataRecord(new Date(base.getTime() + 4 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.add(new DataRecord(new Date(base.getTime() + 5 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.add(new DataRecord(new Date(base.getTime() + 6 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.add(new DataRecord(new Date(base.getTime() + 1 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "2"));
	records.add(new DataRecord(new Date(base.getTime() + 4 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "2"));

	connector.write(records);
	connector.waitForFlush();
	Thread.sleep(1000);
	assertEquals(records.size(), (long) connector.count());
	Thread.sleep(1000);
	// gets from first dataset
	records = connector.getLastRecords(3, DATA_ID + "1");
	assertEquals(3, records.size());
	assertPresent(records, DATA_ID + "1", new Date(base.getTime() + 4 * ms), new Date(base.getTime() + 5 * ms),
		new Date(base.getTime() + 6 * ms));
	// gets from second dataset
	records = connector.getLastRecords(3, DATA_ID + "2");
	assertEquals(2, records.size());
	assertPresent(records, DATA_ID + "2", new Date(base.getTime() + 1 * ms), new Date(base.getTime() + 4 * ms));
	// gets from both
	records = connector.getLastRecords(3, DATA_ID + "1", DATA_ID + "2");
	assertEquals(5, records.size());
	assertPresent(records, DATA_ID + "1", new Date(base.getTime() + 4 * ms), new Date(base.getTime() + 5 * ms),
		new Date(base.getTime() + 6 * ms));
	assertPresent(records, DATA_ID + "2", new Date(base.getTime() + 1 * ms), new Date(base.getTime() + 4 * ms));

    }

    @Test
    public void testStatistics() throws Exception {
	connector.writeStatistics(new StatisticsRecord(new Date(), "sourceId", "dataIdentifier", 34, null, new Date(), null));
	connector.waitForFlush();
	Thread.sleep(1000);

    }

    @Test
    @Ignore
    public void testCachedDays() throws Exception {
	assertEquals(0l, (long) connector.count());
	List<DataRecord> records = new ArrayList<>();
	Date base = ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T00:00:00Z").get();
	Date now = new Date();
	long ms = 1000l;
	records.add(new DataRecord(new Date(base.getTime() + 1 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.get(records.size() - 1).setActive(false);
	records.add(new DataRecord(new Date(base.getTime() + 2 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.get(records.size() - 1).setActive(false);
	records.add(new DataRecord(new Date(base.getTime() + 3 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.get(records.size() - 1).setActive(false);
	records.add(new DataRecord(new Date(base.getTime() + 4 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.get(records.size() - 1).setActive(false);
	records.add(new DataRecord(new Date(base.getTime() + 5 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.get(records.size() - 1).setActive(false);
	records.add(new DataRecord(new Date(base.getTime() + 6 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "1"));
	records.get(records.size() - 1).setActive(false);

	records.add(new DataRecord(new Date(now.getTime() + 1 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "2"));
	records.get(records.size() - 1).setActive(true);
	records.add(new DataRecord(new Date(now.getTime() + 4 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "2"));
	records.get(records.size() - 1).setActive(true);

	records.add(new DataRecord(new Date(now.getTime() + 1 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "3"));
	records.add(new DataRecord(new Date(now.getTime() + 2 * ms), new BigDecimal("1"), null, null, null, DATA_ID + "3"));

	connector.write(records);
	connector.waitForFlush();
	Thread.sleep(1000);
	assertEquals(records.size(), (long) connector.count());
	Thread.sleep(1000);

	connector.deleteFromActiveStationsBefore(new Date(now.getTime() + 3 * ms), null);
	Thread.sleep(1000);
	assertEquals(7, (long) connector.count());

    }

    private void assertPresent(List<DataRecord> records, String dataId, Date... dates) {
	d: for (Date date : dates) {
	    for (DataRecord record : records) {
		String id = record.getDataIdentifier();
		if (id.equals(dataId)) {
		    Date d = record.getDate();
		    if (d.equals(date)) {
			continue d;
		    }
		}
	    }
	    fail("Date not found " + ISO8601DateTimeUtils.getISO8601DateTime(date) + "for metadata record: " + dataId);
	}

    }

    @Test
    public void testRetrieves() throws Exception {
	List<DataRecord> records = getFakeRecords(10, 24, 3600000l); // 10 time series, 1 day, 1 record for each hour
	connector.write(records);
	connector.waitForFlush();
	Thread.sleep(1000);
	assertEquals(240l, (long) connector.count());

	List<DataRecord> ret = connector.getRecords(null, null);
	assertEquals(240, ret.size());
	checkContent(ret);

	ret = connector.getRecords(null, null, DATA_ID + 0);
	assertEquals(24, ret.size());
	checkContent(ret);

	ret = connector.getRecords(ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T00:00:00Z").get(), //
		ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T11:00:00Z").get());
	assertEquals(120, ret.size());
	checkContent(ret);

	ret = connector.getRecords(ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T00:00:00Z").get(), //
		ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T11:00:00Z").get(), //
		DATA_ID + 0);
	assertEquals(12, ret.size());
	checkContent(ret);
    }

    @Test
    public void testBulkSize() throws Exception {
	long start = System.currentTimeMillis();
	connector.configure(OpenSearchConnector.MAX_BULK_SIZE, "80000");

	long series = 2000;
	int numberOfRecords = 20;
	List<DataRecord> records = getFakeRecords(series, numberOfRecords, 3600 * 1000l);
	// hour
	connector.write(records);
	connector.waitForFlush();
	Thread.sleep(1000);

	long total = series * numberOfRecords;

	long time = System.currentTimeMillis() - start;

	System.out.println("Speed (values/s): " + ((double) total / (double) time) * 1000);

	assertEquals(total, (long) connector.count());

	List<DataRecord> ret = connector.getRecords(null, null);
	assertEquals(total, ret.size());
	checkContent(ret);
	for (DataRecord dataRecord : ret) {
	    System.out.println(dataRecord.getDataIdentifier() + " " + dataRecord.getDate());
	}

	ret = connector.getRecords(null, null, DATA_ID + 0);
	assertEquals(total / series, ret.size());
	checkContent(ret);

	// ret = connector.getRecords(ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T00:00:00Z").get(), //
	// ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T11:00:00Z").get());
	// assertEquals(120, ret.size());
	// checkContent(ret);
	//
	// ret = connector.getRecords(ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T00:00:00Z").get(), //
	// ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T11:00:00Z").get(), //
	// MD_ID + 0);
	// assertEquals(12, ret.size());
	// checkContent(ret);

    }

    private void checkContent(List<DataRecord> ret) {
	for (DataRecord record : ret) {
	    String metadataId = record.getDataIdentifier();
	    assertNotNull(metadataId);
	    Date date = record.getDate();
	    assertNotNull(date);
	    BigDecimal value = record.getValue();
	    assertNotNull(value);
	}

    }

    private List<DataRecord> getFakeRecords(long numberOfTimeseries, int numberOfRecords, long intervalMs) {
	GSLoggerFactory.getLogger(getClass()).info("Preparing fake records");
	List<DataRecord> ret = new ArrayList<DataRecord>();
	for (int i = 0; i < numberOfTimeseries; i++) {
	    String metadataIdentifier = DATA_ID + i;
	    List<DataRecord> records = getFakeRecords(metadataIdentifier, numberOfRecords, intervalMs);
	    ret.addAll(records);
	}
	GSLoggerFactory.getLogger(getClass()).info("Prepared fake records");
	return ret;
    }

    private List<DataRecord> getFakeRecords(String metadataIdentifier, int n, long ms) {
	List<DataRecord> ret = new ArrayList<DataRecord>();
	Date base = ISO8601DateTimeUtils.parseISO8601ToDate("1982-01-01T00:00:00Z").get();
	for (int i = 0; i < n; i++) {
	    SimpleEntry<BigDecimal, BigDecimal> latLon = new SimpleEntry<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO);
	    // DataRecord record = new DataRecord(new Date(base.getTime() + i * ms), new BigDecimal(Math.random()), "m",
	    // "precipitation",
	    // latLon, metadataIdentifier);
	    DataRecord record = new DataRecord(new Date(base.getTime() + i * ms), new BigDecimal(Math.random()), null, null, null,
		    metadataIdentifier);
	    ret.add(record);
	}
	return ret;
    }

    public static void main(String[] args) {
	// System.out.println(ISO8601DateTimeUtils.getISO8601DateTime(new Date(378691200000l)));
	// System.out.println(ISO8601DateTimeUtils.getISO8601DateTime(new Date(378691282800l)));
	OpenSearchConnectorInternalTestIT t = new OpenSearchConnectorInternalTestIT();
	List<DataRecord> records = t.getFakeRecords(1, 1, 0);
	OpenSearchConnector c = new OpenSearchConnector();
	System.out.println(c.getJSONObject(records.get(0)).toString());
    }

}
