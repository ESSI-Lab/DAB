package eu.essi_lab.accessor.hiscentral.test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.sicilia.HISCentralSiciliaClient;
import eu.essi_lab.cfga.gs.setting.CredentialsSetting;

/**
 * Requires the env variables SICILIA_USER, SICILIA_PASSWORD, SICILIA_CLIENT_ID and SICILIA_CLIENT_INSTANCE
 */
public class HISCentralSiciliaClientExternalTestIT {

    private static final String ENDPOINT = "https://www.protezionecivilesicilia.it:8443/DatascapeA/";

    @Before
    public void init() {

	Assume.assumeNotNull(System.getenv("SICILIA_USER"));

	CredentialsSetting credentials = new CredentialsSetting();
	credentials.setSiciliaClientUser(System.getenv("SICILIA_USER"));
	credentials.setSiciliaClientPassword(System.getenv("SICILIA_PASSWORD"));
	credentials.setSiciliaClientId(System.getenv("SICILIA_CLIENT_ID"));
	credentials.setSiciliaClientInstance(System.getenv("SICILIA_CLIENT_INSTANCE"));

	HISCentralSiciliaClient.setCredentials(credentials);
    }

    @Test
    public void test() throws Exception {

	HISCentralSiciliaClient client = new HISCentralSiciliaClient(ENDPOINT);

	JSONArray stations = client.getStations();
	Assert.assertTrue(stations.length() > 100);

	JSONArray elements = client.getElements();
	Assert.assertTrue(elements.length() > 500);

	JSONObject element = elements.getJSONObject(0);
	System.out.println(element.toString(2));
	Assert.assertTrue(element.has("elementId"));
	Assert.assertTrue(element.has("stationId"));
	Assert.assertTrue(element.has("startDate"));

	String temperatureId = null;
	for (int i = 0; i < elements.length() && temperatureId == null; i++) {
	    JSONObject e = elements.getJSONObject(i);
	    if (e.optString("quantity").equals("Temperatura Aria")) {
		temperatureId = e.optString("elementId");
	    }
	}
	Assert.assertNotNull(temperatureId);

	Instant end = Instant.now();
	JSONArray data = client.getData(temperatureId, end.minus(1, ChronoUnit.DAYS), end);
	System.out.println("Values: " + data.length() + " first: " + data.optJSONArray(0));
	Assert.assertTrue(data.length() > 0);
    }
}
