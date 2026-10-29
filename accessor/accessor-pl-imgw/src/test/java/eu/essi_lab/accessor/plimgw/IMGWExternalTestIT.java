package eu.essi_lab.accessor.plimgw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
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
public class IMGWExternalTestIT {

    private static final String STATION = "153190040";

    /**
     * Series harvested once (the monthly archive is read at each harvesting)
     */
    private static List<OriginalMetadata> records;

    @BeforeClass
    public static void harvest() throws GSException {

	IMGWConnector connector = new IMGWConnector();
	connector.setSourceURL(IMGWClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations(STATION + ",154190080");
	connector.getSetting().setMaxRecords(10);

	long start = System.currentTimeMillis();
	records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	System.out.println("Harvested " + records.size() + " records in " + (System.currentTimeMillis() - start) + " ms");
    }

    @Test
    public void testStations() throws GSException {

	JSONArray stations = new IMGWClient().retrieveStations();
	assertTrue(stations.length() > 800);
	System.out.println(stations.length() + " stations");
    }

    @Test
    public void testConnector() {

	List<String> ids = records.stream().map(r -> new IMGWEntity(r.getMetadata())).//
		peek(e -> System.out.println(e.getSeriesId() + " " + e.getString(IMGWClient.BEGIN) + " / " + e.getString(IMGWClient.END))).//
		map(IMGWEntity::getSeriesId).//
		collect(Collectors.toList());

	assertTrue(ids.contains(STATION + "|H"));
	assertTrue(ids.contains(STATION + "|Q"));
	assertTrue(ids.contains("154190080|T"));
	assertTrue(ids.stream().allMatch(id -> id.startsWith(STATION + "|") || id.startsWith("154190080|")));

	IMGWEntity discharge = getSeries(STATION + "|Q");
	// first month with monthly characteristics: November 1970 (hydrological year 1971)
	assertEquals("1970-11-01T00:00:00Z", discharge.getString(IMGWClient.BEGIN));
	Date end = discharge.getDate(IMGWClient.END).get();
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(7));
    }

    @Test
    public void testArchiveObservations() throws GSException {

	IMGWClient client = new IMGWClient();
	IMGWEntity waterLevel = getSeries(STATION + "|H");

	// yearly file (hydrological year 2025)
	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2024-12-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2025-01-31T00:00:00Z").get();

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, begin, end);
	assertEquals(62, values.size());
	assertEquals(begin, values.firstKey());
	assertEquals(end, values.lastKey());

	// monthly files, across two hydrological years
	begin = ISO8601DateTimeUtils.parseISO8601ToDate("1990-10-15T00:00:00Z").get();
	end = ISO8601DateTimeUtils.parseISO8601ToDate("1990-11-14T00:00:00Z").get();
	values = client.retrieveObservations(waterLevel, begin, end);
	assertEquals(31, values.size());
    }

    @Test
    public void testLatestObservation() throws GSException {

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(2));

	TreeMap<Date, BigDecimal> values = new IMGWClient().retrieveObservations(getSeries(STATION + "|H"), begin, end);
	assertEquals(1, values.size());
	System.out.println("Latest: " + values);
    }

    @Test
    public void testDownloader() throws Exception {

	GSSource source = new GSSource();
	source.setEndpoint(IMGWClient.DEFAULT_ENDPOINT);

	OriginalMetadata record = records.stream().filter(r -> new IMGWEntity(r.getMetadata()).getSeriesId().equals(STATION + "|Q"))
		.findFirst().get();
	GSResource resource = new IMGWMapper().map(record, source);
	System.out.println(resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	System.out.println(resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	online.setIdentifier();

	IMGWDownloader downloader = new IMGWDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	// the last year: the end of the archive and the latest observation
	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	assertTrue(System.currentTimeMillis() - end < TimeUnit.DAYS.toMillis(7));
	descriptor.setTemporalDimension(new Date(end - 400l * 24 * 60 * 60 * 1000), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	assertTrue(wml.contains(":value"));
	System.out.println("WML size " + wml.length() + " values " + (wml.split(":value ").length - 1));
	System.out.println(wml.substring(wml.indexOf(":value") - 4, Math.min(wml.length(), wml.indexOf(":value") + 300)));
    }

    /**
     * @param seriesId
     * @return
     */
    private static IMGWEntity getSeries(String seriesId) {

	Optional<IMGWEntity> ret = records.stream().map(r -> new IMGWEntity(r.getMetadata()))
		.filter(e -> e.getSeriesId().equals(seriesId)).findFirst();
	assertFalse(ret.isEmpty());
	return ret.get();
    }
}
