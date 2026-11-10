package eu.essi_lab.accessor.usnrcsawdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Test;

public class NRCSAWDBExternalTestIT {

    /**
     * Adin Mtn SNOTEL station, California
     */
    private static final String ADIN_MTN = "301:CA:SNTL";

    @Test
    public void testStationsElementsAndData() {

	NRCSAWDBClient client = new NRCSAWDBClient(System.getProperty("awdbEndpoint", NRCSAWDBClient.DEFAULT_ENDPOINT));

	assertTrue(client.retrieveElements().containsKey("WTEQ"));
	assertEquals("SNOTEL", client.retrieveNetworkNames().get("SNTL"));

	List<JSONObject> stations = client.retrieveStations("SNTL");
	assertTrue(stations.size() > 500);

	List<JSONObject> withElements = client.retrieveStationElements(Arrays.asList(ADIN_MTN), Arrays.asList("WTEQ", "SNWD"),
		Arrays.asList("DAILY", "SEMIMONTHLY"));
	assertEquals(1, withElements.size());

	List<JSONObject> selected = NRCSAWDBConnector.selectFinestDurations(withElements.get(0), Arrays.asList("DAILY", "SEMIMONTHLY"));
	assertFalse(selected.isEmpty());

	NRCSAWDBTimeseriesId id = NRCSAWDBTimeseriesId.parse(ADIN_MTN + "/WTEQ:*:1/DAILY").get();

	Optional<JSONObject> station = client.retrieveStationElement(id);
	assertTrue(station.isPresent());
	assertEquals(-8.0, station.get().getDouble(NRCSAWDBClient.STATION_DATA_TIME_ZONE), 0);

	Date begin = NRCSAWDBClient.parseApiDate("2026-03-01 00:00", 0).get();
	Date end = NRCSAWDBClient.parseApiDate("2026-03-10 00:00", 0).get();

	Optional<JSONObject> data = client.retrieveData(id, begin, end, -8);
	assertTrue(data.isPresent());
	assertTrue(data.get().getJSONArray("values").length() >= 9);
    }
}
