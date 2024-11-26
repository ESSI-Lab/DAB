package eu.essi_lab.accessor.kisters;

import static org.junit.Assert.*;

import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

public class KistersExternalTestIT {

    @Test
    public void test() {
	KISTERSClient client = new KISTERSClient(System.getProperty("kistersEndpoint"));
	List<KISTERSEntity> stations = client.retrieveStationsBySiteName("ZA_DWA");
	System.out.println(stations.size());
	assertTrue(stations.size()>10);
	System.out.println("{");
	for (KISTERSEntity station : stations) {
	    JSONObject json = station.getObject();
	    System.out.println(json);
	    System.out.println(",");
	}
	System.out.println("}");
//	List<KISTERSEntity> timeseries = client.retrieveTimeSeries();
//	System.out.println(timeseries.size());
//	for (KISTERSEntity ts : timeseries) {
//	    System.out.println(ts.getObject().toString());
//	}
    }

}
