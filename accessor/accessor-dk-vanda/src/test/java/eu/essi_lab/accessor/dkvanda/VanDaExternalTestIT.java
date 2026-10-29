package eu.essi_lab.accessor.dkvanda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
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
public class VanDaExternalTestIT {

    private static final String STATION = "21006844";
    private static final String WATER_LEVEL = "21006844|1|25|1233";

    @Test
    public void testSeries() throws GSException {

	VanDaClient client = new VanDaClient(System.getProperty("vandaEndpoint", VanDaClient.DEFAULT_ENDPOINT));

	long start = System.currentTimeMillis();
	List<VanDaEntity> series = client.retrieveSeries(new VanDaConnectorSetting().getExcludedExaminationTypes());
	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} series in {} ms", series.size(), System.currentTimeMillis() - start);

	assertTrue(series.size() > 4000);
	assertTrue(series.stream().noneMatch(s -> s.getExaminationTypeCode() == 21 || s.getExaminationTypeCode() == 42));
	assertTrue(series.stream().allMatch(s -> s.getObject().has(VanDaClient.BEGIN) && s.getObject().has(VanDaClient.END)));

	// all the stations are in Denmark
	assertTrue(series.stream().filter(s -> s.getObject().has(VanDaClient.LAT)).allMatch(s -> {
	    double lat = s.getObject().getDouble(VanDaClient.LAT);
	    double lon = s.getObject().getDouble(VanDaClient.LON);
	    return lat > 54 && lat < 58 && lon > 7 && lon < 16;
	}));

	VanDaEntity waterLevel = series.stream().filter(s -> s.getSeriesId().equals(WATER_LEVEL)).findFirst().get();
	assertEquals("Ellerup Bæk, Bæk, Ved Vejbro", waterLevel.getStationName());
	GSLoggerFactory.getLogger(getClass()).info(waterLevel.toString());
    }

    @Test
    public void testExtent() throws GSException {

	SimpleEntry<Date, Date> extent = new VanDaClient().retrieveExtent(VanDaClient.parseSeriesId(WATER_LEVEL)).get();

	assertEquals("2023-11-03T06:30:00Z", ISO8601DateTimeUtils.getISO8601DateTime(extent.getKey()));
	// real time series
	assertTrue(System.currentTimeMillis() - extent.getValue().getTime() < TimeUnit.DAYS.toMillis(7));
    }

    @Test
    public void testObservations() throws GSException {

	VanDaClient client = new VanDaClient();
	VanDaEntity waterLevel = VanDaClient.parseSeriesId(WATER_LEVEL);

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(2));

	// 10 minutes values
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, begin, end);
	assertTrue(values.size() > 100);
	assertFalse(values.firstKey().before(begin));

	// a long period, split in several requests
	begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(100));
	long start = System.currentTimeMillis();
	values = client.retrieveObservations(waterLevel, begin, end);
	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} values of 100 days in {} ms", values.size(),
		System.currentTimeMillis() - start);
	assertTrue(values.size() > 5000);
	assertFalse(values.firstKey().before(begin));
	assertFalse(values.lastKey().after(end));
    }

    @Test
    public void testConnector() throws GSException {

	VanDaConnector connector = new VanDaConnector();
	connector.setSourceURL(VanDaClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations(STATION + ",21006857");
	connector.getSetting().setMaxRecords(20);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new VanDaEntity(r.getMetadata())).//
		map(VanDaEntity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Harvested {} records: {}", ids.size(), ids);

	assertTrue(ids.contains(WATER_LEVEL));
	assertTrue(ids.contains("21006857|1|27|1155"));
	assertTrue(ids.stream().allMatch(id -> id.startsWith(STATION + "|") || id.startsWith("21006857|")));

	GSSource source = new GSSource();
	source.setEndpoint(VanDaClient.DEFAULT_ENDPOINT);
	GSResource resource = new VanDaMapper().map(records.get(0), source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	GSLoggerFactory.getLogger(getClass()).info("Abstract: {}", resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());
    }
}
