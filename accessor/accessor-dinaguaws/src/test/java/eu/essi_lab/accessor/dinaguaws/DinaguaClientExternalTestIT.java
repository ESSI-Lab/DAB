package eu.essi_lab.accessor.dinaguaws;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.dinaguaws.client.DinaguaClient;
import eu.essi_lab.accessor.dinaguaws.client.DinaguaData;
import eu.essi_lab.accessor.dinaguaws.client.DinaguaStation;
import eu.essi_lab.accessor.dinaguaws.client.JSONDinaguaClient;
import eu.essi_lab.accessor.dinaguaws.client.Variable;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.InterpolationType;

/**
 * @author Fabrizio
 */
public class DinaguaClientExternalTestIT {

    @Test
    public void testMissingStations() throws Exception {

	DinaguaClient client = createClient();
	Set<String> stationIdentifiers = client.getStationIdentifiers();
	Set<String> printed = new HashSet<>();
	for (String stationId : stationIdentifiers) {

	    DinaguaStation station = client.getStation(stationId);

	    for (Variable series : station.getVariables()) {
		for (InterpolationType interpolation : client.getInterpolations()) {

		    Date start = new Date(station.getEndDate().getTime() - TimeUnit.DAYS.toMillis(10));
		    DinaguaData data = client.getData(stationId, series.getAbbreviation(), start, station.getEndDate(), interpolation);
		    int size = data.getSet().size();
		    if (size == 0) {
			if (!printed.contains(stationId)) {
			    System.out.println(station.getJson());
			    printed.add(stationId);
			}

			System.out.println(
				stationId + " station, " + series.getAbbreviation() + " " + interpolation + ": " + size + " values");
		    }
		}
	    }
	}

    }

    @Test
    public void getNivelDataTest() throws Exception {

	DinaguaClient client = createClient();

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2015-01-01").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2015-01-20").get();

	DinaguaData nivelContinuos = client.getData("1220", "H", begin, end, InterpolationType.CONTINUOUS);
	DinaguaData nivelMinimum = client.getData("1220", "H", begin, end, InterpolationType.MIN);

	System.out.println("Nivel continuos");

	nivelContinuos.getSet().forEach(d -> {

	    System.out.println(d.getDate()+" "+ d.getValue());
	});
	System.out.println(nivelContinuos.getSet().size() + " values");

	System.out.println("Nivel minimum");

	nivelMinimum.getSet().forEach(d -> {

	    System.out.println(d.getDate()+" "+ d.getValue());
	});
	System.out.println(nivelMinimum.getSet().size() + " values");
    }

    @Test
    public void getStationsTest() throws Exception {

	DinaguaClient client = createClient();

	Set<DinaguaStation> stations = client.getStations();

	for (DinaguaStation station : stations) {

	    String id = station.getId();
	    Assert.assertNotNull(id);
	}
    }

    @Test
    public void getStationTest() throws Exception {

	DinaguaClient client = createClient();

	DinaguaStation station = client.getStation("1590");

	String id = station.getId();
	Assert.assertEquals("1590", id);
    }

    @Test
    public void getTokenTest() throws Exception {

	DinaguaClient client = createClient();

	String token = ((JSONDinaguaClient) client).getToken();

	GSLoggerFactory.getLogger(getClass()).info("Token: " + token);

	Assert.assertNotNull(token);
	Assert.assertFalse(token.toLowerCase().contains("forbidden"));
    }

    /**
     * @return
     */
    private DinaguaClient createClient() {

	String user = System.getProperty("dinaguaUser");
	String password = System.getProperty("dinaguaPassword");

	DinaguaClient client = new JSONDinaguaClient("https://www.ambiente.gub.uy/dinaguaws");

	client.setUser(user);
	client.setPassword(password);

	return client;
    }
}
