package eu.essi_lab.accessor.coideam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class IDEAMExternalTestIT {

    private static final String STATION = "0021209920";

    private static final String WATER_LEVEL = "NIVEL|" + STATION + "|0230";

    @Test
    public void testSeries() throws GSException {

	IDEAMClient client = new IDEAMClient(System.getProperty("ideamEndpoint", IDEAMClient.DEFAULT_ENDPOINT));

	List<IDEAMEntity> series = client.retrieveSeries(IDEAMDataset.NIVEL, Arrays.asList(STATION));
	assertEquals(1, series.size());

	IDEAMEntity waterLevel = series.get(0);
	assertEquals(WATER_LEVEL, waterLevel.getSeriesId());
	GSLoggerFactory.getLogger(getClass()).info("Series {}: {} - {} ({} values)", waterLevel.getSeriesId(),
		waterLevel.getString(IDEAMClient.BEGIN), waterLevel.getString(IDEAMClient.END), waterLevel.getString(IDEAMClient.COUNT));

	assertTrue(client.completeSeries(waterLevel));
	client.addCatalogEntries(series);
	assertEquals("SANTA ROSITA [21209920]", waterLevel.getStationName());
	assertEquals("BOGOTÁ", waterLevel.getString(IDEAMClient.STREAM));

	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(waterLevel.getString(IDEAMClient.END)).get();
	// real time series, published daily
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(7));
    }

    @Test
    public void testObservations() throws GSException {

	IDEAMClient client = new IDEAMClient();
	IDEAMEntity waterLevel = IDEAMClient.parseSeriesId(WATER_LEVEL);

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(10));

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, begin, end);
	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} values of the last 10 days", values.size());
	assertTrue(values.size() > 100);
	assertFalse(values.firstKey().before(begin));
    }

    @Test
    public void testLongObservations() throws GSException {

	IDEAMClient client = new IDEAMClient();
	IDEAMEntity waterLevel = IDEAMClient.parseSeriesId(WATER_LEVEL);

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2019-01-01T00:00:00Z").get();
	Date end = new Date();

	// several years of hourly values
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, begin, end);
	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} values since 2019: {} - {}", values.size(), values.firstKey(),
		values.lastKey());
	assertTrue(values.size() > 30000);
    }

    @Test
    public void testConnector() throws GSException {

	IDEAMConnector connector = new IDEAMConnector();
	connector.setSourceURL(IDEAMClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations(STATION + ",0022057010");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new IDEAMEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(IDEAMClient.BEGIN))).//
		map(IDEAMEntity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Harvested {}", ids);

	assertTrue(ids.contains(WATER_LEVEL));
	assertTrue(ids.contains("NIVEL_MAX|" + STATION + "|0231"));

	GSSource source = new GSSource();
	source.setEndpoint(IDEAMClient.DEFAULT_ENDPOINT);
	GSResource resource = new IDEAMMapper().map(records.get(0), source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
    }
}
