package eu.essi_lab.accessor.chexistenzbafu;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class CHExistenzBafuExternalTestIT {

    @Test
    public void testLocationsParametersLatestAndReadings() {

	CHExistenzBafuClient client = new CHExistenzBafuClient(System.getProperty("chExistenzBafuEndpoint",
		CHExistenzBafuClient.DEFAULT_ENDPOINT));

	List<CHExistenzBafuEntity> locations = client.retrieveLocations();
	assertTrue(locations.size() > 10);

	CHExistenzBafuEntity location = locations.stream()
		.filter(l -> "2135".equals(CHExistenzBafuClient.getLocationId(l)))
		.findFirst()
		.orElse(locations.get(0));
	String locationId = CHExistenzBafuClient.getLocationId(location);
	assertNotNull(locationId);

	Map<String, CHExistenzBafuEntity> parameters = client.retrieveParameters();
	assertFalse(parameters.isEmpty());
	assertTrue(parameters.containsKey("temperature"));

	List<CHExistenzBafuEntity> latest = client.retrieveLatest(locationId);
	assertFalse(latest.isEmpty());

	String parameter = latest.get(0).getObject().getString(CHExistenzBafuClient.READING_PARAMETER);
	assertNotNull(parameter);

	SimpleEntry<Date, Date> extent = client.retrieveReadingExtent(locationId, parameter);
	assertNotNull(extent);

	Date end = extent.getValue();
	Date begin = new Date(end.getTime() - 7L * 24 * 60 * 60 * 1000);

	List<CHExistenzBafuEntity> readings = client.retrieveReadings(locationId, parameter, begin, end);
	assertFalse(readings.isEmpty());
    }
}
