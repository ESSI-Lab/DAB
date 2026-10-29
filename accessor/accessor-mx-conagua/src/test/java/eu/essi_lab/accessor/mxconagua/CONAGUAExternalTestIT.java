package eu.essi_lab.accessor.mxconagua;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
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
public class CONAGUAExternalTestIT {

    private static final String STATION = "CNDCA";

    @Test
    public void testStations() throws GSException {

	CONAGUAClient client = new CONAGUAClient(System.getProperty("conaguaEndpoint", CONAGUAClient.DEFAULT_ENDPOINT));

	List<JSONObject> stations = client.retrieveStations();
	assertTrue(stations.size() > 1000);

	JSONObject station = stations.stream().filter(s -> s.getString(CONAGUAClient.STATION_CODE).equals(STATION)).findFirst().get();
	assertEquals("Candelaria, Camp.", station.getString(CONAGUAClient.STATION_NAME));
	assertEquals("Campeche", station.getString(CONAGUAClient.STATE));
    }

    @Test
    public void testObservations() throws GSException {

	CONAGUAClient client = new CONAGUAClient();
	CONAGUAEntity discharge = CONAGUAClient.parseSeriesId(STATION + "|DISCHARGE");

	Date[] extent = client.retrieveExtent(discharge).get();
	assertTrue(extent[0].before(extent[1]));

	Date end = extent[1];
	Date begin = new Date(end.getTime() - 30L * 24 * 3600 * 1000);

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(discharge, begin, end);
	assertFalse(values.isEmpty());
	assertFalse(values.firstKey().before(begin));
	assertEquals(end, values.lastKey());
    }

    @Test
    public void testConnector() throws GSException {

	CONAGUAConnector connector = new CONAGUAConnector();
	connector.setSourceURL(CONAGUAClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations(STATION + ",CANCA");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new CONAGUAEntity(r.getMetadata())).//
		peek(e -> assertTrue(ISO8601DateTimeUtils.parseISO8601ToDate(e.getString(CONAGUAClient.BEGIN)).isPresent())).//
		peek(e -> assertTrue(ISO8601DateTimeUtils.parseISO8601ToDate(e.getString(CONAGUAClient.END)).isPresent())).//
		map(CONAGUAEntity::getSeriesId).//
		collect(Collectors.toList());

	System.out.println(ids);

	assertTrue(ids.stream().allMatch(id -> id.startsWith(STATION + "|") || id.startsWith("CANCA|")));
    }
}
