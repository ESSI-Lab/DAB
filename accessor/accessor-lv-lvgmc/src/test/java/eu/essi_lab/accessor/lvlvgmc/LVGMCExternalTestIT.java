package eu.essi_lab.accessor.lvlvgmc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.lvlvgmc.LVGMCClient.Resource;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * @author boldrini
 */
public class LVGMCExternalTestIT {

    @Test
    public void testResources() throws GSException {

	Map<Resource, String> resources = new LVGMCClient().getResources();
	assertEquals(Resource.values().length, resources.size());
	System.out.println(resources);
    }

    @Test
    public void testStationsAndParameters() throws GSException {

	LVGMCClient client = new LVGMCClient();

	Map<String, JSONObject> stations = client.retrieveStations();
	System.out.println("Stations: " + stations.size());
	assertTrue(stations.size() > 50);
	assertTrue(stations.containsKey("HD073548"));

	Map<String, JSONObject> parameters = client.retrieveParameters();
	System.out.println("Parameters: " + parameters.keySet());
	assertTrue(parameters.containsKey("LIMEN"));
	assertTrue(parameters.containsKey("WTEMD"));
    }

    @Test
    public void testSeries() throws GSException {

	long start = System.currentTimeMillis();
	List<LVGMCEntity> series = new LVGMCClient().retrieveSeries(Arrays.asList(), Arrays.asList());
	System.out.println("Series: " + series.size() + " in " + (System.currentTimeMillis() - start) + " ms");
	System.out.println(series.stream().collect(Collectors.groupingBy(LVGMCEntity::getAbbreviation, Collectors.counting())));

	assertTrue(series.size() > 100);

	LVGMCEntity level = series.stream().filter(s -> s.getSeriesId().equals("HD073548|LIMEN")).findFirst().get();
	System.out.println(level);
	assertEquals("Water level", level.getPropertyName());
	assertTrue(level.getObject().has(LVGMCClient.LATITUDE));

	Date end = LVGMCClient.parseDate(level.getString(LVGMCClient.END)).get();
	Date begin = LVGMCClient.parseDate(level.getString(LVGMCClient.BEGIN)).get();
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(2));
	assertTrue(end.getTime() - begin.getTime() > TimeUnit.DAYS.toMillis(360));

	List<LVGMCEntity> filtered = new LVGMCClient().retrieveSeries(Arrays.asList("HD073548"), Arrays.asList("LIMEN"));
	assertEquals(1, filtered.size());
    }

    @Test
    public void testObservations() throws GSException {

	LVGMCClient client = new LVGMCClient();
	LVGMCEntity series = LVGMCClient.parseSeriesId("HD073548|LIMEN");

	SimpleEntry<Date, Date> extent = client.retrieveExtent(series).get();
	System.out.println("Extent: " + extent);

	// the whole series, in more than one request
	long start = System.currentTimeMillis();
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(series, extent.getKey(), extent.getValue());
	System.out.println("Values: " + values.size() + " in " + (System.currentTimeMillis() - start) + " ms");

	assertTrue(values.size() > 8000);
	assertEquals(extent.getKey(), values.firstKey());
	assertEquals(extent.getValue(), values.lastKey());

	// the last hours, from the operational data
	Date begin = new Date(extent.getValue().getTime() - TimeUnit.HOURS.toMillis(5));
	values = client.retrieveObservations(series, begin, extent.getValue());
	assertEquals(6, values.size());
    }

    @Test
    public void testConnectorAndDownloader() throws Exception {

	LVGMCConnector connector = new LVGMCConnector();
	connector.setSourceURL(LVGMCClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("HD073548,HD073801");

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(new ListRecordsRequest());
	List<OriginalMetadata> records = response.getRecordsAsList();
	List<String> ids = records.stream().map(r -> new LVGMCEntity(r.getMetadata()).getSeriesId()).collect(Collectors.toList());
	System.out.println("Records: " + ids);
	assertTrue(ids.contains("HD073548|LIMEN"));

	OriginalMetadata record = records.stream().filter(r -> r.getMetadata().contains("HD073548|LIMEN")).findFirst().get();

	GSSource source = new GSSource();
	source.setEndpoint(LVGMCClient.DEFAULT_ENDPOINT);
	GSResource resource = new LVGMCMapper().map(record, source);
	System.out.println("Title: " + resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	System.out.println("Abstract: " + resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	online.setIdentifier();

	LVGMCDownloader downloader = new LVGMCDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	assertTrue(System.currentTimeMillis() - end < TimeUnit.DAYS.toMillis(1));

	descriptor.setTemporalDimension(new Date(end - TimeUnit.DAYS.toMillis(30)), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	System.out.println("WML size: " + wml.length());
	assertTrue(wml.contains(":value"));
	System.out.println(wml.substring(wml.indexOf(":value") - 4, Math.min(wml.length(), wml.indexOf(":value") + 300)));

	// a long window, clamped to the available extent
	descriptor.setTemporalDimension(new Date(end - TimeUnit.DAYS.toMillis(3650)), new Date(end));
	file = downloader.download(descriptor);
	assertNotNull(file);
	System.out.println("WML size (10 years requested): " + file.length());
	assertFalse(Files.readString(file.toPath()).isEmpty());
    }

    @Test
    public void testFullHarvestPaging() throws GSException {

	LVGMCConnector connector = new LVGMCConnector();
	connector.setSourceURL(LVGMCClient.DEFAULT_ENDPOINT);
	connector.getSetting().setMaxRecords(60);

	ListRecordsRequest request = new ListRecordsRequest();
	int count = 0;
	do {
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    count += response.getRecordsAsList().size();
	    request.setResumptionToken(response.getResumptionToken());
	} while (request.getResumptionToken() != null);

	System.out.println("Harvested (max 60): " + count);
	assertEquals(60, count);
    }
}
