package eu.essi_lab.accessor.kisters;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class KistersExternalTestIT {

    @Test
    public void test() {
	KISTERSClient client = new KISTERSClient(System.getProperty("kistersEndpoint"));
	List<KISTERSEntity> stations = client.retrieveStations();
	System.out.println(stations.size());
	assertTrue(stations.size()>10);
	List<KISTERSEntity> timeseries = client.retrieveTimeSeries();
	System.out.println(timeseries.size());
	for (KISTERSEntity ts : timeseries) {
	    System.out.println(ts.getObject().toString());
	}
    }

}
