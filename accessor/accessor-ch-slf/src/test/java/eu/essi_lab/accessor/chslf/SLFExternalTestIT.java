package eu.essi_lab.accessor.chslf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Test;

public class SLFExternalTestIT {

    @Test
    public void testStationsAndMeasurements() {

	SLFClient client = new SLFClient(System.getProperty("slfEndpoint", SLFClient.DEFAULT_ENDPOINT));

	List<JSONObject> stations = client.retrieveStations(SLFNetwork.IMIS);
	assertTrue(stations.size() > 100);

	List<JSONObject> studyPlots = client.retrieveStations(SLFNetwork.STUDY_PLOT);
	assertTrue(studyPlots.size() > 10);

	Optional<Map<String, Set<String>>> available = client.retrieveAvailableParameters(SLFNetwork.IMIS);
	assertTrue(available.isPresent());
	assertFalse(available.get().isEmpty());

	String stationCode = available.get().keySet().iterator().next();

	List<JSONObject> measurements = client.retrieveMeasurements(SLFNetwork.IMIS, stationCode, 3);
	assertFalse(measurements.isEmpty());

	List<JSONObject> dailySnow = client.retrieveMeasurements(SLFNetwork.IMIS_DAILY_SNOW, "DAV2", 7);
	for (JSONObject measurement : dailySnow) {
	    assertTrue(measurement.getString(SLFClient.MEASUREMENT_STATION_CODE).equals("DAV2"));
	}
    }
}
