package eu.essi_lab.accessor.depegelonline;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class PegelonlineExternalTestIT {

    private static final String BONN_WATER_LEVEL = "593647aa-9fea-43ec-a7d6-6476a76ae868/W";

    @Test
    public void testStationsTimeseriesAndMeasurements() {

	PegelonlineClient client = new PegelonlineClient(System.getProperty("pegelonlineEndpoint",
		PegelonlineClient.DEFAULT_ENDPOINT));

	List<PegelonlineEntity> stations = client.retrieveStations();
	assertTrue(stations.size() > 10);

	PegelonlineEntity station = stations.get(0);
	String stationUuid = PegelonlineClient.getStationUuid(station);
	assertNotNull(stationUuid);
	assertTrue(station.getObject().has("timeseries"));

	SimpleEntry<Date, Date> extent = client.retrieveMeasurementExtent(BONN_WATER_LEVEL);
	assertNotNull(extent);

	List<PegelonlineEntity> measurements = client.retrieveMeasurements(BONN_WATER_LEVEL, extent.getKey(), extent.getValue());
	assertFalse(measurements.isEmpty());
    }
}
