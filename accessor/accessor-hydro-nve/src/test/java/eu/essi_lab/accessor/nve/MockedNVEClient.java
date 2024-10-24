package eu.essi_lab.accessor.nve;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class MockedNVEClient extends NVEClient {

    @Override
    public InputStream downloadStations(String stationId) throws IOException {
	return downloadStations();
    }

    @Override
    public InputStream downloadStations() throws UnsupportedOperationException, IOException {
	return MockedNVEClient.class.getClassLoader().getResourceAsStream("stations.json");
    }

    @Override
    public InputStream downloadObservations(String stationId, String parameter, String resolutionTime, Date begin, Date end)
	    throws IOException {
	return MockedNVEClient.class.getClassLoader().getResourceAsStream("observations.json");
    }

}
