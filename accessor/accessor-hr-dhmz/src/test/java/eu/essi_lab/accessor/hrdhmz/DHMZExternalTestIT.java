package eu.essi_lab.accessor.hrdhmz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * @author boldrini
 */
public class DHMZExternalTestIT {

    /**
     * Aljmaš, Danube (station code 5001)
     */
    private static final String STATION_KEY = "384";

    @Test
    public void testSeries() throws GSException {

	DHMZClient client = new DHMZClient(System.getProperty("dhmzEndpoint", DHMZClient.DEFAULT_ENDPOINT));

	List<JSONObject> stations = client.retrieveStations();
	GSLoggerFactory.getLogger(getClass()).info("{} stations", stations.size());
	assertTrue(stations.size() > 900);

	Date now = new Date();
	List<DHMZEntity> series = stations.stream().//
		map(s -> DHMZClient.createSeries(s, Arrays.asList(DHMZProperty.values()), true, now)).//
		flatMap(Collection::stream).//
		collect(Collectors.toList());

	Map<String, Long> counts = series.stream().collect(Collectors.groupingBy(
		s -> s.getProperty().get().name() + (s.isRealTime() ? "|RT" : ""), TreeMap::new, Collectors.counting()));
	GSLoggerFactory.getLogger(getClass()).info("{} series: {}", series.size(), counts);

	assertTrue(series.size() > 1500);
	assertTrue(counts.get("WATER_LEVEL|RT") > 150);

	DHMZEntity discharge = series.stream().filter(s -> s.getSeriesId().equals(STATION_KEY + "|DISCHARGE")).findFirst().get();
	assertEquals("ALJMAŠ", discharge.getStationName());
	assertEquals("2001-01-01T00:00:00Z", discharge.getString(DHMZClient.BEGIN));

	DHMZEntity realTime = series.stream().filter(s -> s.getSeriesId().equals(STATION_KEY + "|WATER_LEVEL|RT")).findFirst().get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(realTime.getString(DHMZClient.END)).get();
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(2));
    }

    @Test
    public void testDailyObservations() throws GSException {

	DHMZClient client = new DHMZClient();

	TreeMap<Date, BigDecimal> values = client.retrieveDailyValues(STATION_KEY, DHMZProperty.DISCHARGE, 2020);
	assertEquals(366, values.size());
	assertEquals("2020-01-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));

	// years 1992-1997 are not available, and the first months of 1998 are missing
	DHMZEntity waterLevel = DHMZClient.parseSeriesId(STATION_KEY + "|WATER_LEVEL");
	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("1995-12-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("1999-01-31T00:00:00Z").get();
	values = client.retrieveObservations(waterLevel, begin, end);
	GSLoggerFactory.getLogger(getClass()).info("{} values from {} to {}", values.size(), values.firstKey(), values.lastKey());
	assertEquals(1998, values.firstKey().toInstant().atOffset(java.time.ZoneOffset.UTC).getYear());
	assertEquals("1999-01-31T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(values.lastKey()));
    }

    @Test
    public void testRealTimeObservations() throws GSException {

	TreeMap<Date, BigDecimal> values = new DHMZClient().retrieveRealTimeValues(STATION_KEY);
	GSLoggerFactory.getLogger(getClass()).info("{} real time values from {} to {}", values.size(), values.firstKey(),
		values.lastKey());
	assertTrue(values.size() > 300);
	assertTrue(System.currentTimeMillis() - values.lastKey().getTime() < TimeUnit.DAYS.toMillis(2));
	assertFalse(values.lastKey().after(new Date()));
    }

    @Test
    public void testConnector() throws GSException {

	DHMZConnector connector = new DHMZConnector();
	connector.setSourceURL(DHMZClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("5001,3171");
	connector.getSetting().setMaxRecords(20);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();

	List<String> ids = records.stream().map(r -> new DHMZEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(DHMZClient.BEGIN))).//
		map(DHMZEntity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Records: {}", ids);

	assertTrue(ids.contains("4|SUSPENDED_SEDIMENT_LOAD"));
	assertTrue(ids.contains("384|WATER_TEMPERATURE"));
	assertTrue(ids.contains("384|WATER_LEVEL|RT"));
	assertTrue(ids.stream().allMatch(id -> id.startsWith("4|") || id.startsWith("384|")));
    }

    @Test
    public void testDownloader() throws Exception {

	DHMZConnector connector = new DHMZConnector();
	connector.setSourceURL(DHMZClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("5001");

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();

	GSSource source = new GSSource();
	source.setEndpoint(DHMZClient.DEFAULT_ENDPOINT);

	for (String seriesId : new String[] { STATION_KEY + "|WATER_LEVEL|RT", STATION_KEY + "|DISCHARGE" }) {

	    OriginalMetadata record = records.stream().filter(r -> new DHMZEntity(r.getMetadata()).getSeriesId().equals(seriesId))
		    .findFirst().get();

	    GSResource resource = new DHMZMapper().map(record, source);
	    GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	    Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	    // during harvesting the identifier is assigned by the identifier decorator
	    online.setIdentifier();

	    DHMZDownloader downloader = new DHMZDownloader();
	    downloader.setOnlineResource(resource, online.getIdentifier());
	    assertTrue(downloader.canDownload());
	    assertTrue(downloader.canConnect());

	    DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	    assertNotNull(descriptor.getTemporalDimension());

	    long begin = descriptor.getTemporalDimension().getContinueDimension().getLower().longValue();
	    long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	    GSLoggerFactory.getLogger(getClass()).info("Remote extent {} / {}", new Date(begin), new Date(end));

	    // the last 60 days of the series
	    descriptor.setTemporalDimension(new Date(Math.max(begin, end - TimeUnit.DAYS.toMillis(60))), new Date(end));

	    File file = downloader.download(descriptor);
	    assertNotNull(file);
	    String wml = Files.readString(file.toPath());
	    GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes, {} values", wml.length(), wml.split(":value ").length - 1);
	    assertTrue(wml.contains(":value"));
	}
    }
}
