package eu.essi_lab.accessor.derlpwasser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * @author boldrini
 */
public class RLPWasserExternalTestIT {

    private static final String ALTENAHR = "27180403";

    @Test
    public void testSites() throws GSException {

	RLPWasserClient client = new RLPWasserClient(System.getProperty("rlpEndpoint", RLPWasserClient.DEFAULT_ENDPOINT));

	List<JSONObject> sites = client.retrieveSites();
	GSLoggerFactory.getLogger(getClass()).info("{} sites", sites.size());
	assertTrue(sites.size() > 200);

	// all the sites are in or around Rhineland-Palatinate
	assertTrue(sites.stream().allMatch(s -> s.has(RLPWasserClient.LAT) && s.getDouble(RLPWasserClient.LAT) > 47
		&& s.getDouble(RLPWasserClient.LAT) < 52 && s.getDouble(RLPWasserClient.LON) > 5 && s.getDouble(RLPWasserClient.LON) < 10));

	JSONObject altenahr = sites.stream().filter(s -> s.getString(RLPWasserClient.NUMBER).equals(ALTENAHR)).findFirst().get();
	assertEquals("Altenahr", altenahr.getString(RLPWasserClient.NAME));
	assertEquals("Ahr", altenahr.getString(RLPWasserClient.RIVER));
    }

    @Test
    public void testObservations() throws GSException {

	RLPWasserClient client = new RLPWasserClient();

	RLPWasserEntity discharge = RLPWasserClient.parseSeriesId(ALTENAHR + "|Q");

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(discharge);
	GSLoggerFactory.getLogger(getClass()).info("{} values [{} - {}]", values.size(), values.firstKey(), values.lastKey());

	// 5 days of 15 minutes values
	assertTrue(values.size() > 400);
	assertTrue(System.currentTimeMillis() - values.lastKey().getTime() < TimeUnit.DAYS.toMillis(1));

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(1));

	TreeMap<Date, BigDecimal> day = client.retrieveObservations(discharge, begin, end);
	assertTrue(day.size() > 50 && day.size() <= 97);
	assertFalse(day.firstKey().before(begin));
    }

    @Test
    public void testConnectorAndMapper() throws Exception {

	RLPWasserConnector connector = new RLPWasserConnector();
	connector.setSourceURL(RLPWasserClient.DEFAULT_ENDPOINT);
	connector.getSetting().setSites(ALTENAHR + ",26600128");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new RLPWasserEntity(r.getMetadata())).//
		map(RLPWasserEntity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Series {}", ids);

	assertTrue(ids.contains(ALTENAHR + "|W"));
	assertTrue(ids.contains(ALTENAHR + "|Q"));
	assertTrue(ids.contains("26600128|W"));

	GSSource source = new GSSource();
	source.setEndpoint(RLPWasserClient.DEFAULT_ENDPOINT);

	OriginalMetadata discharge = records.stream()
		.filter(r -> new RLPWasserEntity(r.getMetadata()).getSeriesId().equals(ALTENAHR + "|Q")).findFirst().get();

	GSResource resource = new RLPWasserMapper().map(discharge, source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	RLPWasserDownloader downloader = new RLPWasserDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	long begin = descriptor.getTemporalDimension().getContinueDimension().getLower().longValue();
	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	GSLoggerFactory.getLogger(getClass()).info("Remote extent {} / {}", new Date(begin), new Date(end));
	assertTrue(System.currentTimeMillis() - end < TimeUnit.DAYS.toMillis(1));

	// the whole rolling window
	File file = downloader.download(descriptor);
	assertNotNull(file);
	String wml = Files.readString(file.toPath());
	GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes, {} values", wml.length(), wml.split(":value ").length - 1);
	assertTrue(wml.split(":value ").length > 400);
    }
}
