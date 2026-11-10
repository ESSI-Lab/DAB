package eu.essi_lab.accessor.ththaiwater;

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
public class ThaiWaterExternalTestIT {

    /**
     * Tha Chedi Temple (TTC06), HII, 10 minutes values
     */
    private static final String WATER_LEVEL = "tele_waterlevel|737|WATER_LEVEL";

    /**
     * C.36, RID, hourly values with discharge
     */
    private static final String DISCHARGE = "tele_waterlevel|2611|DISCHARGE";

    private static final String DAM_STORAGE = "dam|25|DAM_STORAGE";

    private static final String CANAL_WATER_LEVEL = "canal|210|CANAL_WATER_LEVEL";

    private static final String CANAL_FLOW = "flow|12|CANAL_FLOW";

    @Test
    public void testSeries() throws GSException {

	ThaiWaterClient client = new ThaiWaterClient();

	List<ThaiWaterEntity> series = client.retrieveSeries(Arrays.asList(ThaiWaterStationType.values()));
	GSLoggerFactory.getLogger(getClass()).info("{} series", series.size());
	assertTrue(series.size() > 1000);

	for (String id : new String[] { WATER_LEVEL, DISCHARGE, DAM_STORAGE, CANAL_WATER_LEVEL, CANAL_FLOW }) {
	    ThaiWaterEntity entity = series.stream().filter(s -> s.getSeriesId().equals(id)).findFirst().get();
	    Date end = ISO8601DateTimeUtils.parseISO8601ToDate(entity.getString(ThaiWaterClient.END)).get();
	    // real time series
	    assertTrue(id, System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(7));
	}
    }

    @Test
    public void testFirstObservation() throws GSException {

	ThaiWaterClient client = new ThaiWaterClient();

	Date begin = client.retrieveFirstObservation(withEnd(WATER_LEVEL)).get();
	GSLoggerFactory.getLogger(getClass()).info("{} begins at {}", WATER_LEVEL, begin);
	assertEquals(2012, ThaiWaterClient.toLocalDate(begin).getYear());

	begin = client.retrieveFirstObservation(withEnd(DAM_STORAGE)).get();
	GSLoggerFactory.getLogger(getClass()).info("{} begins at {}", DAM_STORAGE, begin);
	assertTrue(ThaiWaterClient.toLocalDate(begin).getYear() <= 1999);

	begin = client.retrieveFirstObservation(withEnd(CANAL_WATER_LEVEL)).get();
	GSLoggerFactory.getLogger(getClass()).info("{} begins at {}", CANAL_WATER_LEVEL, begin);
	assertTrue(ThaiWaterClient.toLocalDate(begin).getYear() <= 2020);
    }

    @Test
    public void testRecentObservations() throws GSException {

	ThaiWaterClient client = new ThaiWaterClient();

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(2));

	// 10 minutes values
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(ThaiWaterClient.parseSeriesId(WATER_LEVEL), begin, end);
	GSLoggerFactory.getLogger(getClass()).info("{} values of {}", values.size(), WATER_LEVEL);
	assertTrue(values.size() > 100);
	assertFalse(values.firstKey().before(begin));

	// hourly values
	values = client.retrieveObservations(ThaiWaterClient.parseSeriesId(DISCHARGE), begin, end);
	assertTrue(values.size() > 20);

	values = client.retrieveObservations(ThaiWaterClient.parseSeriesId(CANAL_FLOW), begin, end);
	assertTrue(values.size() > 100);

	values = client.retrieveObservations(ThaiWaterClient.parseSeriesId(CANAL_WATER_LEVEL), begin, end);
	assertTrue(values.size() > 100);

	assertTrue(client.retrieveLatestObservation(ThaiWaterClient.parseSeriesId(WATER_LEVEL)).isPresent());
    }

    @Test
    public void testDailyObservations() throws GSException {

	ThaiWaterClient client = new ThaiWaterClient();

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2015-01-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2018-12-31T00:00:00Z").get();

	// daily values, since the period is long and older than one year
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(ThaiWaterClient.parseSeriesId(WATER_LEVEL), begin, end);
	GSLoggerFactory.getLogger(getClass()).info("{} daily values of {}", values.size(), WATER_LEVEL);
	assertTrue(values.size() > 700 && values.size() <= 1461);

	values = client.retrieveObservations(ThaiWaterClient.parseSeriesId(DAM_STORAGE), begin, end);
	assertTrue(values.size() > 1000);

	// a short old period is read at full resolution (10 minutes)
	end = ISO8601DateTimeUtils.parseISO8601ToDate("2015-01-03T00:00:00Z").get();
	values = client.retrieveObservations(ThaiWaterClient.parseSeriesId(WATER_LEVEL), begin, end);
	assertTrue(values.size() > 50);
    }

    @Test
    public void testConnector() throws GSException, IOException {

	ThaiWaterConnector connector = new ThaiWaterConnector();
	connector.setSourceURL(ThaiWaterClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStationTypes("tele_waterlevel,dam");
	connector.getSetting().setStations("2611,dam|25");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new ThaiWaterEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(ThaiWaterClient.BEGIN))).//
		map(ThaiWaterEntity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Harvested {}", ids);

	assertTrue(ids.contains("tele_waterlevel|2611|WATER_LEVEL"));
	assertTrue(ids.contains(DAM_STORAGE));
	assertTrue(ids.stream().allMatch(id -> id.startsWith("tele_waterlevel|2611|") || id.startsWith("dam|25|")));

	OriginalMetadata waterLevel = records.get(ids.indexOf("tele_waterlevel|2611|WATER_LEVEL"));

	GSSource source = new GSSource();
	source.setEndpoint(ThaiWaterClient.DEFAULT_ENDPOINT);
	GSResource resource = new ThaiWaterMapper().map(waterLevel, source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {}, extent: {} / {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle(),
		resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getBeginPosition(),
		resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getEndPosition());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	ThaiWaterDownloader downloader = new ThaiWaterDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	assertTrue(System.currentTimeMillis() - end < TimeUnit.DAYS.toMillis(1));

	descriptor.setTemporalDimension(new Date(end - TimeUnit.DAYS.toMillis(2)), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	GSLoggerFactory.getLogger(getClass()).info("WML size: {} values: {}", wml.length(), wml.split(":value ").length - 1);
	assertTrue(wml.contains(":value"));
    }

    /**
     * @param seriesId
     * @return the series, with the current time as end
     */
    private static ThaiWaterEntity withEnd(String seriesId) {

	ThaiWaterEntity series = ThaiWaterClient.parseSeriesId(seriesId);
	series.getObject().put(ThaiWaterClient.END, ISO8601DateTimeUtils.getISO8601DateTime(new Date()));
	return series;
    }
}
