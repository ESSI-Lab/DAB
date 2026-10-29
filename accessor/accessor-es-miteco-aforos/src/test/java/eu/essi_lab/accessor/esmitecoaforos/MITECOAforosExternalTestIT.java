package eu.essi_lab.accessor.esmitecoaforos;

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
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.accessor.esmitecoaforos.MITECOAforosClient.Archive;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * Live tests: the first run downloads the archive (about 136 MB) in the local cache
 *
 * @author boldrini
 */
public class MITECOAforosExternalTestIT {

    private static final String WATER_LEVEL = "CANTABRICO|1033|RIVER_LEVEL_DAILY";

    @Test
    public void testArchive() throws Exception {

	MITECOAforosClient client = new MITECOAforosClient();

	Archive archive = client.resolveArchive();
	System.out.println("Archive: " + archive.getUrl() + " edition " + archive.getEdition());
	assertTrue(archive.getUrl().matches(".*/Anuario-\\d{2}-\\d{2}-csv\\.zip"));

	long start = System.currentTimeMillis();
	File file = client.getArchiveFile();
	System.out.println("Cached archive: " + file + " (" + file.length() + " bytes) in " + (System.currentTimeMillis() - start) + " ms");
	assertTrue(file.length() > 100_000_000);

	// the second request uses the cached copy
	start = System.currentTimeMillis();
	assertEquals(file, new MITECOAforosClient().getArchiveFile());
	assertTrue(System.currentTimeMillis() - start < 10000);
    }

    @Test
    public void testAllSeries() throws Exception {

	long start = System.currentTimeMillis();

	List<MITECOAforosEntity> series = new MITECOAforosClient().retrieveSeries(Arrays.asList(MITECOAforosVariable.values()),
		Collections.emptyList(), Collections.emptyList());

	System.out.println("Series: " + series.size() + " in " + (System.currentTimeMillis() - start) + " ms");
	assertTrue(series.size() > 5000);

	Map<String, Long> byVariable = series.stream()
		.collect(Collectors.groupingBy(s -> s.getVariable().get().name(), TreeMap::new, Collectors.counting()));
	System.out.println(byVariable);
	System.out.println("Max memory used (MB): " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1_000_000);

	// all the series are located
	long located = series.stream().filter(s -> s.getObject().has(MITECOAforosClient.LAT)).count();
	System.out.println("Located series: " + located);
	assertTrue(located > series.size() * 0.95);

	MITECOAforosEntity level = series.stream().filter(s -> s.getSeriesId().equals(WATER_LEVEL)).findFirst().get();
	System.out.println(level);
	assertEquals("PUENTE DE SAMA", level.getStationName());
	assertEquals("1912-01-01T00:00:00Z", level.getString(MITECOAforosClient.BEGIN));
    }

    @Test
    public void testConnector() throws Exception {

	MITECOAforosConnector connector = new MITECOAforosConnector();
	connector.setSourceURL(MITECOAforosClient.DEFAULT_ENDPOINT);
	connector.getSetting().setBasins("CANTABRICO");
	connector.getSetting().setMaxRecords(250);

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(new ListRecordsRequest());
	List<OriginalMetadata> records = response.getRecordsAsList();
	assertEquals(MITECOAforosConnector.SERIES_PER_PAGE, records.size());
	assertNotNull(response.getResumptionToken());

	ListRecordsRequest next = new ListRecordsRequest();
	next.setResumptionToken(response.getResumptionToken());
	response = connector.listRecords(next);
	assertEquals(50, response.getRecordsAsList().size());

	List<MITECOAforosEntity> series = records.stream().map(r -> new MITECOAforosEntity(r.getMetadata())).collect(Collectors.toList());
	assertTrue(series.stream().allMatch(s -> s.getBasin().equals("CANTABRICO")));

	GSSource source = new GSSource();
	source.setEndpoint(MITECOAforosClient.DEFAULT_ENDPOINT);

	GSResource resource = new MITECOAforosMapper().map(records.get(0), source);
	System.out.println(resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	System.out.println(resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());
    }

    @Test
    public void testObservations() throws Exception {

	MITECOAforosClient client = new MITECOAforosClient();

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("1912-01-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("1912-01-31T00:00:00Z").get();

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(MITECOAforosClient.parseSeriesId(WATER_LEVEL), begin, end);
	assertEquals(31, values.size());

	// a long Ebro series, read entirely
	long start = System.currentTimeMillis();
	values = client.retrieveObservations(MITECOAforosClient.parseSeriesId("EBRO|9001|RIVER_DISCHARGE_DAILY"), null, null);
	System.out.println("EBRO 9001 discharge: " + values.size() + " values " + values.firstKey() + " - " + values.lastKey() + " in "
		+ (System.currentTimeMillis() - start) + " ms");
	assertFalse(values.isEmpty());
    }

    @Test
    public void testDownloader() throws Exception {

	MITECOAforosEntity series = new MITECOAforosClient().retrieveSeries(Arrays.asList(MITECOAforosVariable.RESERVOIR_STORAGE_DAILY),
		Arrays.asList("CANTABRICO"), Arrays.asList("1077")).get(0);

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setMetadata(series.toString());
	originalMetadata.setSchemeURI(MITECOAforosMapper.MITECO_AFOROS_SCHEMA);

	GSSource source = new GSSource();
	source.setEndpoint(MITECOAforosClient.DEFAULT_ENDPOINT);

	GSResource resource = new MITECOAforosMapper().map(originalMetadata, source);

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	online.setIdentifier();

	MITECOAforosDownloader downloader = new MITECOAforosDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	long upper = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	descriptor.setTemporalDimension(new Date(upper - 364l * 24 * 60 * 60 * 1000), new Date(upper));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	System.out.println("WML size: " + wml.length() + " bytes, end " + series.getString(MITECOAforosClient.END));
	assertTrue(wml.contains(":value"));
	System.out.println(wml.substring(wml.indexOf(":value") - 4, Math.min(wml.length(), wml.indexOf(":value") + 300)));
    }
}
