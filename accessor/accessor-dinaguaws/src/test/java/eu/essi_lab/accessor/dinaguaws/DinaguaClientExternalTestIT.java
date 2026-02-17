package eu.essi_lab.accessor.dinaguaws;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.dinaguaws.client.DinaguaClient;
import eu.essi_lab.accessor.dinaguaws.client.DinaguaData;
import eu.essi_lab.accessor.dinaguaws.client.DinaguaStation;
import eu.essi_lab.accessor.dinaguaws.client.DinaguaValue;
import eu.essi_lab.accessor.dinaguaws.client.JSONDinaguaClient;
import eu.essi_lab.accessor.dinaguaws.client.Variable;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.InterpolationType;

/**
 * @author Fabrizio
 */
public class DinaguaClientExternalTestIT {

    @Before
    public void init(){
	InputStream trustStream = DinaguaClientExternalTestIT.class.getClassLoader().getResourceAsStream("test-trust.p12");

	if (trustStream != null) {
	    File target = new File(org.apache.commons.io.FileUtils.getTempDirectory(), "test-trust.p12");
	    try {
		org.apache.commons.io.FileUtils.copyInputStreamToFile(trustStream, target);
	    } catch (IOException e) {
		Assert.fail(e.getMessage());
	    }

	    System.setProperty("dab.net.ssl.trustStore", target.getAbsolutePath());
	    System.setProperty("dab.net.ssl.trustStorePassword", "trustpass");
	}
    }

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

	    System.out.println(d.getDate() + " " + d.getValue());
	});
	System.out.println(nivelContinuos.getSet().size() + " values");

	System.out.println("Nivel minimum");

	nivelMinimum.getSet().forEach(d -> {

	    System.out.println(d.getDate() + " " + d.getValue());
	});
	System.out.println(nivelMinimum.getSet().size() + " values");
    }

    @Test
    public void getStationsTest() throws Exception {

	DinaguaClient client = createClient();

	Set<DinaguaStation> stations = client.getStations();

	for (DinaguaStation station : stations) {

	    String id = station.getId();
	    System.out.println(id + " " + station.getCountry());
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
    
    public static SimpleDateFormat YEAR_MONTH_SDF = new SimpleDateFormat("yyyy-MM");


    @Test
    public void getStatusStationsTest() throws Exception {

	DinaguaClient client = createClient();

	Set<DinaguaStation> stations = client.getStatusStations();

	assertTrue(!stations.isEmpty());

	String stationId = "1170";

	DinaguaStation selected = null;

	for (DinaguaStation station : stations) {
	    // System.out.println(station.toString());
	    if (station.getId().equals(stationId)) {
		selected = station;
	    }
	}

	System.out.println(selected.toString());

	List<Variable> variables = selected.getVariables();
	for (Variable variable : variables) {
	    System.out.println(variable.getLabel() + " " + variable.getAbbreviation());
	}

	Date start = selected.getBeginDate();
	Date end = selected.getEndDate();
	System.out.println("start: " + start);
	System.out.println("end: " + end);
	start = ISO8601DateTimeUtils.parseISO8601ToDate("1969-01").get();
	end = ISO8601DateTimeUtils.parseISO8601ToDate("2026-08").get();
	DinaguaData data = client.getStatusData(stationId, "1", start, end);
	TreeSet<DinaguaValue> treeData = data.getSet();
	Iterator<DinaguaValue> it = treeData.iterator();
	while (it.hasNext()) {
	    DinaguaValue d = it.next();
	    System.out.println(YEAR_MONTH_SDF.format(d.getDate()) + ": " + d.getValue());

	}

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
