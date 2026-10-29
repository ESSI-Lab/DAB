package eu.essi_lab.accessor.czchmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.accessor.czchmi.CHMIClient.Inventory;
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
public class CHMIExternalTestIT {

    /**
     * Vestřev (Labe), operational station with validated data since 2003
     */
    private static final String STATION = "0-203-1-004200";

    @Test
    public void testSeries() throws GSException {

	CHMIClient client = new CHMIClient(System.getProperty("chmiEndpoint", CHMIClient.DEFAULT_ENDPOINT));

	Inventory inventory = client.retrieveInventory(Arrays.asList(CHMIParameter.values()));
	assertTrue(inventory.getStations().size() > 700);

	List<CHMIEntity> series = client.retrieveSeries(inventory, STATION, Arrays.asList(CHMIParameter.values()));
	Map<String, CHMIEntity> map = series.stream().collect(Collectors.toMap(CHMIEntity::getSeriesId, Function.identity()));

	GSLoggerFactory.getLogger(getClass()).info("Series of {}: {}", STATION, map.keySet());

	CHMIEntity waterLevel = map.get(STATION + "|H");
	assertEquals("Vestřev", waterLevel.getStationName());
	// the validated instantaneous water levels start in 2003
	assertEquals("2003-01-01T00:00:00Z", waterLevel.getString(CHMIClient.BEGIN));
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(waterLevel.getString(CHMIClient.END)).get();
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(2));

	assertTrue(map.containsKey(STATION + "|Q"));
	assertTrue(map.containsKey(STATION + "|TH"));
	assertTrue(map.containsKey(STATION + "|QD"));
	assertTrue(map.containsKey(STATION + "|QM"));
	assertTrue(map.containsKey(STATION + "|QNEX"));
	// included in the operational series
	assertFalse(map.containsKey(STATION + "|HC"));
	assertFalse(map.containsKey(STATION + "|TO"));
    }

    @Test
    public void testOperationalObservations() throws GSException {

	CHMIClient client = new CHMIClient();
	CHMIEntity discharge = CHMIClient.parseSeriesId(STATION + "|Q");

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(3));

	// recent and now files, 10 minutes values
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(discharge, begin, end);
	GSLoggerFactory.getLogger(getClass()).info("Discharge values of the last 3 days: {}", values.size());
	assertTrue(values.size() > 300);
	assertFalse(values.firstKey().before(begin));
	assertTrue(end.getTime() - values.lastKey().getTime() < TimeUnit.DAYS.toMillis(1));

	assertTrue(client.retrieveLatestObservation(discharge).isPresent());
    }

    @Test
    public void testOperationalAndValidatedObservations() throws GSException {

	CHMIClient client = new CHMIClient();
	CHMIEntity waterLevel = CHMIClient.parseSeriesId(STATION + "|H");

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2025-12-31T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2026-01-01T23:59:59Z").get();

	// validated 10 minutes values of 2025 and recent values of 2026
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, begin, end);
	assertEquals(288, values.size());
	assertEquals(begin, values.firstKey());

	// discharge is not in the validated archive: the recent files are found with a binary search
	CHMIEntity discharge = CHMIClient.parseSeriesId(STATION + "|Q");
	begin = ISO8601DateTimeUtils.parseISO8601ToDate("2020-01-01T00:00:00Z").get();
	end = ISO8601DateTimeUtils.parseISO8601ToDate("2026-01-02T23:59:59Z").get();
	values = client.retrieveObservations(discharge, begin, end);
	assertEquals(288, values.size());
	assertEquals(ISO8601DateTimeUtils.parseISO8601ToDate("2026-01-01T00:00:00Z").get(), values.firstKey());
    }

    @Test
    public void testHistoricalObservations() throws GSException {

	CHMIClient client = new CHMIClient();
	CHMIEntity discharge = CHMIClient.parseSeriesId(STATION + "|QD");

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2023-12-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2024-01-31T00:00:00Z").get();

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(discharge, begin, end);
	assertEquals(62, values.size());
	assertEquals(begin, values.firstKey());
	assertEquals(end, values.lastKey());
    }

    @Test
    public void testConnector() throws GSException {

	CHMIConnector connector = new CHMIConnector();
	connector.setSourceURL(CHMIClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("004200,0-203-1-001000");
	connector.getSetting().setParameters("H,Q,TH,QD,HH");
	connector.getSetting().setMaxRecords(20);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new CHMIEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(CHMIClient.BEGIN))).//
		map(CHMIEntity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Harvested series: {}", ids);

	assertTrue(ids.contains(STATION + "|H"));
	assertTrue(ids.contains(STATION + "|QD"));
	assertTrue(ids.contains(STATION + "|HH"));
	assertTrue(ids.contains("0-203-1-001000|Q"));
	assertTrue(ids.stream().allMatch(id -> id.startsWith(STATION + "|") || id.startsWith("0-203-1-001000|")));
    }

    @Test
    public void testDownloader() throws GSException, IOException {

	CHMIConnector connector = new CHMIConnector();
	connector.setSourceURL(CHMIClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations(STATION);
	connector.getSetting().setParameters("TH");

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertEquals(1, records.size());

	GSSource source = new GSSource();
	source.setEndpoint(CHMIClient.DEFAULT_ENDPOINT);

	GSResource resource = new CHMIMapper().map(records.get(0), source);
	GSLoggerFactory.getLogger(getClass()).info("Mapped {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	CHMIDownloader downloader = new CHMIDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	// validated values of the end of 2025, recent and now values of 2026
	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	assertTrue(System.currentTimeMillis() - end < TimeUnit.DAYS.toMillis(1));
	descriptor.setTemporalDimension(ISO8601DateTimeUtils.parseISO8601ToDate("2025-12-25T00:00:00Z").get(), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes, {} values", wml.length(), wml.split(":value ").length - 1);
	assertTrue(wml.contains(":value"));
    }
}
