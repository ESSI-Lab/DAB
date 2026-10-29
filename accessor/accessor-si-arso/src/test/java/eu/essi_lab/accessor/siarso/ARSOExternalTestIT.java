package eu.essi_lab.accessor.siarso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
public class ARSOExternalTestIT {

    private static final String DISCHARGE = "1060|Pretok";

    @Test
    public void testSeries() throws GSException {

	ARSOClient client = new ARSOClient(System.getProperty("arsoEndpoint", ARSOClient.DEFAULT_ENDPOINT));

	List<JSONObject> stations = client.retrieveStations();
	GSLoggerFactory.getLogger(getClass()).info("{} stations", stations.size());
	assertTrue(stations.size() > 150);

	JSONObject station = stations.stream().filter(s -> s.getString(ARSOClient.STATION_ID).equals("1060")).findFirst().get();

	List<ARSOEntity> series = client.retrieveSeries(station, Collections.emptyList());
	List<String> ids = series.stream().map(ARSOEntity::getSeriesId).collect(Collectors.toList());
	GSLoggerFactory.getLogger(getClass()).info("Series: {}", ids);
	assertTrue(ids.contains("1060|Vodostaj"));
	assertTrue(ids.contains(DISCHARGE));

	ARSOEntity discharge = series.stream().filter(s -> s.getSeriesId().equals(DISCHARGE)).findFirst().get();
	assertEquals("Gornja Radgona I (Mura)", discharge.getStationName());

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate(discharge.getString(ARSOClient.BEGIN)).get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(discharge.getString(ARSOClient.END)).get();
	GSLoggerFactory.getLogger(getClass()).info("Extent {} / {}", begin, end);

	// rolling window of 30 days
	assertTrue(System.currentTimeMillis() - begin.getTime() <= TimeUnit.DAYS.toMillis(31));
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(1));

	// sea station
	JSONObject sea = stations.stream().filter(s -> s.getString(ARSOClient.STATION_ID).equals("9400")).findFirst().get();
	List<String> seaIds = client.retrieveSeries(sea, Collections.emptyList()).stream().map(ARSOEntity::getSeriesId)
		.collect(Collectors.toList());
	GSLoggerFactory.getLogger(getClass()).info("Sea series: {}", seaIds);
	assertTrue(seaIds.contains("9400|Temperatura morja"));
    }

    @Test
    public void testObservations() throws GSException {

	ARSOClient client = new ARSOClient();
	ARSOEntity discharge = ARSOClient.parseSeriesId(DISCHARGE);

	Date latest = client.retrieveLatestObservation(discharge).get();
	assertTrue(System.currentTimeMillis() - latest.getTime() < TimeUnit.HOURS.toMillis(6));

	Date end = new Date();

	// last day, 10 minutes values
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(discharge, new Date(end.getTime() - TimeUnit.HOURS.toMillis(12)),
		end);
	GSLoggerFactory.getLogger(getClass()).info("12 hours: {} values", values.size());
	assertTrue(values.size() > 50);

	// last 5 days, 30 minutes values and 10 minutes values
	values = client.retrieveObservations(discharge, new Date(end.getTime() - TimeUnit.DAYS.toMillis(5)), end);
	GSLoggerFactory.getLogger(getClass()).info("5 days: {} values", values.size());
	assertTrue(values.size() > 5 * 40);

	// last 30 days, also hourly values
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(29));
	values = client.retrieveObservations(discharge, begin, end);
	GSLoggerFactory.getLogger(getClass()).info("29 days: {} values, {} / {}", values.size(), values.firstKey(), values.lastKey());
	assertTrue(values.size() > 29 * 20);
	assertFalse(values.firstKey().before(begin));
	assertTrue(values.firstKey().getTime() - begin.getTime() < TimeUnit.HOURS.toMillis(2));
	assertEquals(latest, values.lastKey());

	// older than the rolling window
	assertTrue(client.retrieveObservations(discharge, new Date(end.getTime() - TimeUnit.DAYS.toMillis(100)),
		new Date(end.getTime() - TimeUnit.DAYS.toMillis(60))).isEmpty());
    }

    @Test
    public void testConnectorAndMapper() throws GSException {

	ARSOConnector connector = new ARSOConnector();
	connector.setSourceURL(ARSOClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("1060,9350");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new ARSOEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(ARSOClient.BEGIN))).//
		map(ARSOEntity::getSeriesId).//
		collect(Collectors.toList());
	GSLoggerFactory.getLogger(getClass()).info("Harvested {} records: {}", ids.size(), ids);

	assertTrue(ids.contains(DISCHARGE));
	assertTrue(ids.contains("9350|Gladina morja"));
	assertTrue(ids.stream().allMatch(id -> id.startsWith("1060|") || id.startsWith("9350|")));

	GSSource source = new GSSource();
	source.setEndpoint(ARSOClient.DEFAULT_ENDPOINT);
	GSResource resource = new ARSOMapper().map(records.get(0), source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	GSLoggerFactory.getLogger(getClass()).info("Abstract: {}", resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	ARSODownloader downloader = new ARSODownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	long upper = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	assertTrue(System.currentTimeMillis() - upper < TimeUnit.HOURS.toMillis(6));

	File file = downloader.download(descriptor);
	assertNotNull(file);
	GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes", file.length());
	assertTrue(file.length() > 10000);
    }
}
