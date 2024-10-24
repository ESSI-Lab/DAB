package eu.essi_lab.accessor.nve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.model.exceptions.GSException;

public class NVEClientExternalTestIT {

    private NVEClient client;

    @Before
    public void init() {
	this.client = new NVEClient();
	client.setAuthorizationKey("w004XwbJzkGpaUbHInrmNA==");
    }

    @Test
    public void testSpecificStation() throws GSException {
	Map<String, NVEStation> stations = client.getStations("1.10.0");

	assertEquals(1, stations.size());
	NVEStation station = stations.get("1.10.0");
	assertEquals("1.10.0", station.getId());

    }

    @Test
    public void testStations() throws GSException {
	Map<String, NVEStation> stations = client.getStations();
	System.out.println(stations.size());
	TreeSet<String> codes = new TreeSet<>();
	for (NVEStation station : stations.values()) {
	    codes.add(station.getId());
	    System.out.print(station.toString() + " ");
	    List<NVESeries> seriesList = station.getSeries();
	    for (NVESeries series : seriesList) {
		System.out.print(series.getParameterName() + ",");
	    }
	    System.out.println();

	}
	System.out.println(stations.size());
	assertTrue(stations.size() > 4000);
	for (String code : codes) {
	    System.out.println(code);
	}
    }

    @Test
    public void testDownload() throws IOException, GSException {
	Optional<Date> od1 = ISO8601DateTimeUtils.parseISO8601ToDate("1853-01-19T11:00:00Z");
	Optional<Date> od2 = ISO8601DateTimeUtils.parseISO8601ToDate("1853-06-19T11:00:00Z");
	NVEObservations observations = client.getObservations("1.14.0", "1000", "0", od1.get(), od2.get());
	assertNotNull(observations);
	assertEquals("1.14.0", observations.getStationId());
	List<NVEObservation> obs = observations.getObservations();
	assertTrue(obs.size() > 20);

    }

    @Test
    public void testConcurrentDownload() throws IOException, GSException, InterruptedException, ExecutionException {

	int threads = 10;
	TaskListExecutor<Boolean> taskList = new TaskListExecutor<>(threads);

	for (int i = 0; i < threads; i++) {
	    taskList.addTask(new Callable<Boolean>() {

		@Override
		public Boolean call() throws Exception {
		    Optional<Date> od1 = ISO8601DateTimeUtils.parseISO8601ToDate("1853-01-19T11:00:00Z");
		    Optional<Date> od2 = ISO8601DateTimeUtils.parseISO8601ToDate("1853-06-19T11:00:00Z");
		    NVEObservations observations = client.getObservations("1.14.0", "1000", "0", od1.get(), od2.get());
		    assertNotNull(observations);
		    assertEquals("1.14.0", observations.getStationId());
		    List<NVEObservation> obs = observations.getObservations();
		    boolean ok = obs.size() > 20;
		    return ok;
		}
	    });
	}

	List<Future<Boolean>> results = taskList.executeAndWait();
	for (Future<Boolean> result : results) {
	    assertTrue(result.get());
	}
    }

}
