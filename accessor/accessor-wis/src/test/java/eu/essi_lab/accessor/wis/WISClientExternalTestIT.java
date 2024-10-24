package eu.essi_lab.accessor.wis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class WISClientExternalTestIT {

    @Test
    public void test() {
	String endpoint = "https://demo.wis2box.wis.wmo.int/italy/oapi";
	WISClient client = new WISClient(endpoint);
	List<Station> stations = client.getStations();
	System.out.println(stations.size() + " stations");
	assertTrue(stations.size() > 140);
	for (Station station : stations) {
	    System.out.println(station.getWigosId() + " Station " + station.getName() + " ");
	}
	Station station = client.getStation("0-20000-0-16232");
	assertEquals("TERMOLI", station.getName());
	HashSet<ObservedProperty> properties = client.getVariables(station.getWigosId());
	for (ObservedProperty property : properties) {
	    System.out.println(property.getName() + " - " + property.getUnits());
	}
	assertTrue(properties.size() > 10);
	Date now = new Date();
	List<Observation> obs = client.getObservations(station.getWigosId(), "wind_speed",
		new Date(now.getTime() - TimeUnit.DAYS.toMillis(100)), now, 2, true);
	assertTrue(obs.size() == 2);
	print(obs);

	for (ObservedProperty property : properties) {

	    obs = client.getObservations(station.getWigosId(), property.getName(), null, null, 1, true);
	    Date minimum = obs.get(0).getDate();
	    obs = client.getObservations(station.getWigosId(), property.getName(), null, null, 1, false);
	    Date maximum = obs.get(0).getDate();
	    System.out.println(property.getName() + " minimum " + minimum + " maximum " + maximum);
	}

    }

    private void print(List<Observation> obs) {
	for (Observation ob : obs) {
	    System.out.println(ob.getDate() + " " + ob.getValue());
	}

    }

}
