package eu.essi_lab.accessor.ukeafloodmonitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
public class EAFloodMonitoringExternalTestIT {

    private static final String WATER_LEVEL = "1029TH-level-stage-i-15_min-mASD";

    @Test
    public void testSeries() throws GSException {

	EAFloodMonitoringClient client = new EAFloodMonitoringClient(
		System.getProperty("eaEndpoint", EAFloodMonitoringClient.DEFAULT_ENDPOINT));

	long start = System.currentTimeMillis();
	List<EAFloodMonitoringEntity> series = client.retrieveSeries(new EAFloodMonitoringConnectorSetting().getParameters(),
		Collections.emptyList());
	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} series in {} ms", series.size(), System.currentTimeMillis() - start);

	assertTrue(series.size() > 3000);
	assertTrue(series.stream().anyMatch(s -> s.getParameter().equals("flow")));
	assertTrue(series.stream().anyMatch(s -> s.getQualifier().equals("Groundwater")));
	assertTrue(series.stream().anyMatch(s -> s.getQualifier().equals("Tidal Level")));
	assertFalse(series.stream().anyMatch(s -> s.getParameter().equals("rainfall")));

	long located = series.stream().filter(s -> s.getObject().has(EAFloodMonitoringClient.LAT)).count();
	GSLoggerFactory.getLogger(getClass()).info("{} series located, {} from grid references", located,
		series.stream().filter(s -> s.getObject().has(EAFloodMonitoringClient.COORDINATES_FROM_GRID_REFERENCE)).count());

	EAFloodMonitoringEntity waterLevel = series.stream().filter(s -> s.getSeriesId().equals(WATER_LEVEL)).findFirst().get();
	assertEquals("Bourton Dickler", waterLevel.getStationLabel());

	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(waterLevel.getString(EAFloodMonitoringClient.END)).get();
	// real time series
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(2));
    }

    @Test
    public void testLatestObservation() throws GSException {

	Date latest = new EAFloodMonitoringClient().retrieveLatestObservation(WATER_LEVEL).get();
	assertTrue(System.currentTimeMillis() - latest.getTime() < TimeUnit.DAYS.toMillis(2));

	assertFalse(new EAFloodMonitoringClient().retrieveLatestObservation("not-existing-measure").isPresent());
    }

    @Test
    public void testWindowObservations() throws GSException {

	EAFloodMonitoringClient client = new EAFloodMonitoringClient();

	Date end = new Date();
	Date begin = EAFloodMonitoringClient.getWindowBegin(end);

	// the whole rolling window, 15 minutes values
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(WATER_LEVEL, begin, end);
	GSLoggerFactory.getLogger(getClass()).info("{} values from {} to {}", values.size(), values.firstKey(), values.lastKey());
	assertTrue(values.size() > 2000);
	assertEquals(begin, values.firstKey());

	// periods older than the rolling window are not available
	Date old = new Date(end.getTime() - TimeUnit.DAYS.toMillis(365));
	assertTrue(client.retrieveObservations(WATER_LEVEL, old, new Date(old.getTime() + TimeUnit.DAYS.toMillis(10))).isEmpty());
    }

    @Test
    public void testSplitObservations() throws GSException {

	// 5 minutes values (about 8000 readings in the rolling window)
	String measure = "E47901-level-tidal_level-i-5_min-mAOD";

	Date end = new Date();
	Date begin = EAFloodMonitoringClient.getWindowBegin(end);

	TreeMap<Date, BigDecimal> values = new EAFloodMonitoringClient().retrieveObservations(measure, begin, end);
	GSLoggerFactory.getLogger(getClass()).info("{} values of {}", values.size(), measure);
	assertTrue(values.size() > 5000);

	// with a lower limit the requests are split, with the same result
	EAFloodMonitoringClient client = new EAFloodMonitoringClient();
	client.setLimit(1000);
	TreeMap<Date, BigDecimal> split = client.retrieveObservations(measure, begin, values.lastKey());
	assertEquals(values.headMap(values.lastKey(), true), split);
    }

    @Test
    public void testConnector() throws GSException {

	EAFloodMonitoringConnector connector = new EAFloodMonitoringConnector();
	connector.setSourceURL(EAFloodMonitoringClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("1029TH,E9030");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new EAFloodMonitoringEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(EAFloodMonitoringClient.BEGIN))).//
		map(EAFloodMonitoringEntity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Harvested {}", ids);

	assertTrue(ids.contains(WATER_LEVEL));
	assertTrue(ids.stream().allMatch(id -> id.startsWith("1029TH-") || id.startsWith("E9030-")));

	GSSource source = new GSSource();
	source.setEndpoint(EAFloodMonitoringClient.DEFAULT_ENDPOINT);

	for (OriginalMetadata record : records) {
	    GSResource resource = new EAFloodMonitoringMapper().map(record, source);
	    GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	}
    }

    @Test
    public void testRainfallSetting() throws GSException {

	EAFloodMonitoringConnector connector = new EAFloodMonitoringConnector();
	connector.setSourceURL(EAFloodMonitoringClient.DEFAULT_ENDPOINT);
	connector.getSetting().setParameters("rainfall");
	connector.getSetting().setMaxRecords(5);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertEquals(5, records.size());
	assertTrue(records.stream().allMatch(r -> new EAFloodMonitoringEntity(r.getMetadata()).getParameter().equals("rainfall")));
	assertTrue(Arrays.asList("total", "instantaneous", "event", "cumulative total")
		.contains(new EAFloodMonitoringEntity(records.get(0).getMetadata()).getValueType()));
    }

    @Test
    public void testDownloader() throws Exception {

	EAFloodMonitoringConnector connector = new EAFloodMonitoringConnector();
	connector.setSourceURL(EAFloodMonitoringClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("1029TH");

	OriginalMetadata record = connector.listRecords(new ListRecordsRequest()).getRecordsAsList().stream()
		.filter(r -> new EAFloodMonitoringEntity(r.getMetadata()).getSeriesId().equals(WATER_LEVEL)).findFirst().get();

	GSSource source = new GSSource();
	source.setEndpoint(EAFloodMonitoringClient.DEFAULT_ENDPOINT);
	GSResource resource = new EAFloodMonitoringMapper().map(record, source);

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	EAFloodMonitoringDownloader downloader = new EAFloodMonitoringDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	long begin = descriptor.getTemporalDimension().getContinueDimension().getLower().longValue();
	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	GSLoggerFactory.getLogger(getClass()).info("Remote extent {} / {}", new Date(begin), new Date(end));
	assertTrue(System.currentTimeMillis() - end < TimeUnit.DAYS.toMillis(2));

	// the whole rolling window
	File file = downloader.download(descriptor);
	assertNotNull(file);
	String wml = Files.readString(file.toPath());
	GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes, {} values", wml.length(), wml.split(":value ").length - 1);
	assertTrue(wml.contains(":value"));

	// the last day
	descriptor.setTemporalDimension(new Date(end - TimeUnit.DAYS.toMillis(1)), new Date(end));
	wml = Files.readString(downloader.download(descriptor).toPath());
	GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes, {} values", wml.length(), wml.split(":value ").length - 1);
	System.out.println(wml.substring(wml.indexOf(":value") - 4, Math.min(wml.length(), wml.indexOf(":value") + 300)));
    }
}
