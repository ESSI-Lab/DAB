package eu.essi_lab.accessor.ukhydrology;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class UKHydrologyExternalTestIT {

    @Test
    public void testStationsMeasuresAndReadings() {

	UKHydrologyClient client = new UKHydrologyClient(System.getProperty("ukHydrologyEndpoint",
		UKHydrologyClient.DEFAULT_ENDPOINT));

	List<UKHydrologyEntity> stations = client.retrieveStations();
	assertTrue(stations.size() > 10);

	UKHydrologyEntity station = stations.get(0);
	String stationGuid = UKHydrologyClient.getStationGuid(station);
	assertNotNull(stationGuid);

	List<UKHydrologyEntity> measures = client.retrieveMeasures(stationGuid);
	assertFalse(measures.isEmpty());

	UKHydrologyEntity measure = measures.get(0);
	String measureNotation = UKHydrologyClient.getMeasureNotation(measure);
	assertNotNull(measureNotation);

	SimpleEntry<Date, Date> extent = client.retrieveReadingExtent(measureNotation);
	assertNotNull(extent);

	List<UKHydrologyEntity> readings = client.retrieveReadings(measureNotation, extent.getKey(), extent.getValue());
	assertFalse(readings.isEmpty());
    }
}
