package eu.essi_lab.accessor.essaihjucar;

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
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.accessor.essaihjucar.SAIHJucarVariableType.SAIHJucarCategory;
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
public class SAIHJucarExternalTestIT {

    /**
     * Discharge of the Serpis river at Azud Font d'En Carrós
     */
    private static final String DISCHARGE = "14099";

    /**
     * Level of the Ulldecona reservoir
     */
    private static final String RESERVOIR_LEVEL = "2488";

    @Test
    public void testVariables() throws GSException {

	SAIHJucarClient client = new SAIHJucarClient(System.getProperty("saihJucarEndpoint", SAIHJucarClient.DEFAULT_ENDPOINT));

	List<SAIHJucarEntity> variables = client.retrieveVariables(Arrays.asList(SAIHJucarCategory.AFOROS, SAIHJucarCategory.EMBALSES));
	GSLoggerFactory.getLogger(getClass()).info("Variables of gauges and reservoirs: {}", variables.size());
	assertTrue(variables.size() > 150);

	// each variable only once
	assertEquals(variables.size(), variables.stream().map(SAIHJucarEntity::getVariableId).distinct().count());

	SAIHJucarEntity discharge = variables.stream().filter(v -> v.getVariableId().equals(DISCHARGE)).findFirst().get();
	assertEquals("AZUD FONT D'EN CARRÓS", discharge.getStationName());

	SAIHJucarEntity level = variables.stream().filter(v -> v.getVariableId().equals(RESERVOIR_LEVEL)).findFirst().get();
	assertEquals(SAIHJucarVariableType.RESERVOIR_LEVEL, level.getType().get());
	assertTrue(level.getObject().getDouble(SAIHJucarClient.LAT) > 40);
    }

    @Test
    public void testRainVariables() throws GSException {

	List<SAIHJucarEntity> variables = new SAIHJucarClient().retrieveVariables(Arrays.asList(SAIHJucarCategory.LLUVIAS));
	GSLoggerFactory.getLogger(getClass()).info("Rain variables: {}", variables.size());
	assertTrue(variables.size() > 150);
	assertTrue(variables.stream().allMatch(v -> v.getType().get() == SAIHJucarVariableType.PRECIPITATION_INTENSITY));
	assertTrue(variables.stream().anyMatch(v -> v.getVariableId().equals("14481")));
    }

    @Test
    public void testExtent() throws GSException {

	SimpleEntry<Date, Date> extent = new SAIHJucarClient().retrieveExtent(DISCHARGE).get();
	GSLoggerFactory.getLogger(getClass()).info("Extent of {}: {} {}", DISCHARGE, extent.getKey(), extent.getValue());

	assertEquals("2024-12-09T17:30:00Z", ISO8601DateTimeUtils.getISO8601DateTime(extent.getKey()));
	// real time series
	assertTrue(System.currentTimeMillis() - extent.getValue().getTime() < TimeUnit.DAYS.toMillis(2));

	SimpleEntry<Date, Date> levelExtent = new SAIHJucarClient().retrieveExtent(RESERVOIR_LEVEL).get();
	assertEquals("2024-07-25T10:55:00Z", ISO8601DateTimeUtils.getISO8601DateTime(levelExtent.getKey()));
    }

    @Test
    public void testObservations() throws GSException {

	SAIHJucarClient client = new SAIHJucarClient();

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2025-10-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2025-10-02T00:00:00Z").get();

	// five minutes values, both ends included (one value of this day is missing at the source)
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(DISCHARGE, begin, end);
	assertEquals(288, values.size());
	assertEquals(begin, values.firstKey());
	assertEquals(end, values.lastKey());

	// more than one request
	begin = ISO8601DateTimeUtils.parseISO8601ToDate("2025-01-01T00:00:00Z").get();
	end = ISO8601DateTimeUtils.parseISO8601ToDate("2025-03-15T00:00:00Z").get();
	values = client.retrieveObservations(DISCHARGE, begin, end);
	GSLoggerFactory.getLogger(getClass()).info("Values of 73 days: {}", values.size());
	assertTrue(values.size() > 20000);
	assertFalse(values.firstKey().before(begin));
	assertFalse(values.lastKey().after(end));
    }

    @Test
    public void testConnector() throws GSException {

	SAIHJucarConnector connector = new SAIHJucarConnector();
	connector.setSourceURL(SAIHJucarClient.DEFAULT_ENDPOINT);
	connector.getSetting().setCategories("EMBALSES");
	connector.getSetting().setVariables("2488,2489,13862");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertEquals(3, records.size());

	List<String> ids = records.stream().map(r -> new SAIHJucarEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(SAIHJucarClient.BEGIN))).//
		peek(e -> assertTrue(e.getObject().has(SAIHJucarClient.END))).//
		map(SAIHJucarEntity::getVariableId).//
		collect(Collectors.toList());

	assertTrue(ids.containsAll(Arrays.asList("2488", "2489", "13862")));

	GSSource source = new GSSource();
	source.setEndpoint(SAIHJucarClient.DEFAULT_ENDPOINT);
	GSResource resource = new SAIHJucarMapper().map(records.get(0), source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {} {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle(),
		records.get(0).getMetadata());
    }

    @Test
    public void testDownloader() throws Exception {

	SAIHJucarConnector connector = new SAIHJucarConnector();
	connector.setSourceURL(SAIHJucarClient.DEFAULT_ENDPOINT);
	connector.getSetting().setCategories("AFOROS");
	connector.getSetting().setVariables(DISCHARGE);

	OriginalMetadata record = connector.listRecords(new ListRecordsRequest()).getRecordsAsList().get(0);

	GSSource source = new GSSource();
	source.setEndpoint(SAIHJucarClient.DEFAULT_ENDPOINT);
	GSResource resource = new SAIHJucarMapper().map(record, source);

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	SAIHJucarDownloader downloader = new SAIHJucarDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	long begin = descriptor.getTemporalDimension().getContinueDimension().getLower().longValue();
	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	GSLoggerFactory.getLogger(getClass()).info("Remote extent {} / {}", new Date(begin), new Date(end));
	assertTrue(System.currentTimeMillis() - end < TimeUnit.DAYS.toMillis(2));

	// last 40 days, two requests
	descriptor.setTemporalDimension(new Date(end - TimeUnit.DAYS.toMillis(40)), new Date(end));
	File file = downloader.download(descriptor);
	assertNotNull(file);
	GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes", Files.size(file.toPath()));
	assertTrue(Files.size(file.toPath()) > 100000);
    }
}
