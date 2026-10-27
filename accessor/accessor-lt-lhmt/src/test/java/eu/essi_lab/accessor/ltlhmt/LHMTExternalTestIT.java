package eu.essi_lab.accessor.ltlhmt;

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

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class LHMTExternalTestIT {

    @Test
    public void testStations() throws GSException {

	List<JSONObject> stations = new LHMTClient().retrieveStations();
	assertTrue(stations.size() > 80);
	assertTrue(stations.stream().anyMatch(s -> s.getString(LHMTClient.CODE).equals("nemajunu-vms")));
    }

    @Test
    public void testSeries() throws GSException {

	LHMTClient client = new LHMTClient();

	JSONObject river = new JSONObject().put(LHMTClient.CODE, "nemajunu-vms");
	List<String> ids = client.retrieveSeries(river, Arrays.asList(LHMTParameter.values())).stream()
		.map(LHMTEntity::getSeriesId).collect(Collectors.toList());
	assertTrue(ids.contains("nemajunu-vms|WATER_LEVEL"));
	assertTrue(ids.contains("nemajunu-vms|WATER_TEMPERATURE"));
	assertTrue(ids.contains("nemajunu-vms|WATER_LEVEL_DAILY"));
	assertTrue(ids.contains("nemajunu-vms|DISCHARGE_DAILY"));

	// a reservoir: no discharge
	JSONObject reservoir = new JSONObject().put(LHMTClient.CODE, "birstono-vms");
	ids = client.retrieveSeries(reservoir, Arrays.asList(LHMTParameter.values())).stream().map(LHMTEntity::getSeriesId)
		.collect(Collectors.toList());
	assertTrue(ids.contains("birstono-vms|WATER_LEVEL_DAILY"));
	assertFalse(ids.contains("birstono-vms|DISCHARGE_DAILY"));

	// a station without water temperature
	JSONObject noTemperature = new JSONObject().put(LHMTClient.CODE, "smalininku-vms");
	ids = client.retrieveSeries(noTemperature, Arrays.asList(LHMTParameter.values())).stream().map(LHMTEntity::getSeriesId)
		.collect(Collectors.toList());
	assertTrue(ids.contains("smalininku-vms|WATER_LEVEL"));
	assertFalse(ids.contains("smalininku-vms|WATER_TEMPERATURE"));
    }

    @Test
    public void testMeasuredObservations() throws GSException {

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(3));

	TreeMap<Date, BigDecimal> values = new LHMTClient()
		.retrieveObservations(LHMTClient.parseSeriesId("nemajunu-vms|WATER_LEVEL"), begin, end);

	assertTrue(values.size() > 48);
	assertFalse(values.firstKey().before(begin));
	assertTrue(end.getTime() - values.lastKey().getTime() < TimeUnit.DAYS.toMillis(1));
    }

    @Test
    public void testHistoricalObservations() throws GSException {

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2019-12-15T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2020-01-15T00:00:00Z").get();

	TreeMap<Date, BigDecimal> values = new LHMTClient()
		.retrieveObservations(LHMTClient.parseSeriesId("nemajunu-vms|DISCHARGE_DAILY"), begin, end);

	assertEquals(32, values.size());
	assertEquals(begin, values.firstKey());
	assertEquals(end, values.lastKey());
    }

    @Test
    public void testConnector() throws GSException {

	LHMTConnector connector = new LHMTConnector();
	connector.setSourceURL(LHMTClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("nemajunu-vms,birstono-vms");

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new LHMTEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(LHMTClient.BEGIN))).//
		map(LHMTEntity::getSeriesId).//
		collect(Collectors.toList());

	assertTrue(ids.contains("nemajunu-vms|WATER_LEVEL"));
	assertTrue(ids.stream().allMatch(id -> id.startsWith("nemajunu-vms|") || id.startsWith("birstono-vms|")));
    }
}
