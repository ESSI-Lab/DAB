package eu.essi_lab.accessor.whos;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.accessor.whos.automatic.AutomaticSystemClient;
import eu.essi_lab.accessor.whos.automatic.AutomaticSystemObservation;
import eu.essi_lab.accessor.whos.automatic.AutomaticSystemStation;
import eu.essi_lab.accessor.whos.automatic.AutomaticSystemVariable;

public class DMHClientExternalTestIT {

    @Test
    public void test() throws Exception {
	AutomaticSystemClient client = new AutomaticSystemClient();
	//client.setToken(System.getProperty("dmhToken"));
	//DMHClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
	List<AutomaticSystemStation> stations = client.getStations();
	System.out.println("Found " + stations.size() + " stations");
	assertTrue(stations.size() > 10);
	boolean foundVar = false;
	boolean downloadTest = false;
	boolean simplePrint = true;
	int f = 0;
	int e = 0;
	String fullStations = "";
	String emptyStations = "";
	for (AutomaticSystemStation station : stations) {
	    String name = station.getName();
	    BigDecimal latitude = station.getLatitude();
	    assertNotNull(latitude);
	    BigDecimal longitude = station.getLongitude();
	    assertNotNull(longitude);

	    assertTrue(name.length() > 3);

	    List<AutomaticSystemVariable> vars = client.getVariables(station.getId());
	    if (vars.isEmpty()) {
		emptyStations += (++e + ") " + name + " " + latitude + " " + longitude) + "\n";
	    } else {
		if (simplePrint) {
		    fullStations += name + " " + latitude + " " + longitude + "\n";
		} else {
		    fullStations += "\n" + (++f + ") " + name + " " + latitude + " " + longitude) + "\nVariables: ";
		}
	    }
	    for (AutomaticSystemVariable var : vars) {
		String varName = var.getVariableName();
		assertNotNull(varName);
		if (!simplePrint) {
		fullStations += varName + ", ";
		}
		if (downloadTest) {
		    if (!varName.contains("aire")) {
			continue;
		    }
		    Date end = var.getObservationsEnd();
		    Date start = new Date(end.getTime() - 1000 * 60 * 60 * 24 * 10); // last ten days
		    List<AutomaticSystemObservation> observations = client.getObservations(station.getCode(), varName, start, end);
		    assertTrue(observations.size() > 2);
		    for (AutomaticSystemObservation observation : observations) {
			Date date = observation.getDate();
			BigDecimal value = observation.getValue();
			System.out.println(date + " " + value);
		    }
		    foundVar = true;
		    break;
		}
		if (foundVar) {
		    break;
		}
	    }

	}
	System.out.println(fullStations);
	System.out.println(emptyStations);
	assertTrue(foundVar);
    }
}
