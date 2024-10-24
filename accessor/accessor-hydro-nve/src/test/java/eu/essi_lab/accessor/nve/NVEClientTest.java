package eu.essi_lab.accessor.nve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import eu.essi_lab.model.exceptions.GSException;

public class NVEClientTest {

    @Test
    public void testStations() throws GSException {
	MockedNVEClient client = new MockedNVEClient();
	Map<String, NVEStation> stations = client.getStations();
	assertEquals(4673, stations.size());

	NVEStation station = stations.get("1.10.0");
	assertEquals("1.10.0", station.getId());
	assertEquals(59.20959, station.getLatitude(), 0.001);
	assertEquals(11.69155, station.getLongitude(), 0.001);
	List<NVESeries> series = station.getSeries();
	assertEquals(1, series.size());
	NVESeries s = series.get(0);
	assertEquals("1000", s.getParameterId());
	assertEquals("Vannstand", s.getParameterName());
	assertEquals("m", s.getUnit());
	assertEquals("1851-11-16T00:00:00", s.getSeriesFrom());
	assertEquals("1853-12-30T00:00:00", s.getSeriesTo());
	List<NVEResolution> resolutions = s.getResolutions();
	assertEquals(2, resolutions.size());
	NVEResolution res = resolutions.get(0);
	assertEquals("0", res.getResTime());
	assertEquals("Instantaneous", res.getMethod());
	assertEquals("1851-11-16T11:00:00Z", res.getDataFromTime());
	assertEquals("1853-12-30T11:00:00Z", res.getDataToTime());
    }

    @Test
    public void testObservations() throws GSException {
	MockedNVEClient client = new MockedNVEClient();
	// fake data
	NVEObservations observations = client.getObservations("stationId", "parameter", "resolutionTime", null, null);

	assertEquals("413", observations.getObservationCount());

	List<NVEObservation> obs = observations.getObservations();
	NVEObservation ob1 = obs.get(0);
	assertEquals("1851-11-16T11:00:00Z", ob1.getTime());
	assertEquals("2.07", ob1.getValue());
	NVEObservation ob2 = obs.get(1);
	assertEquals("1851-11-17T11:00:00Z", ob2.getTime());
	assertNull(ob2.getValue());
    }

}
