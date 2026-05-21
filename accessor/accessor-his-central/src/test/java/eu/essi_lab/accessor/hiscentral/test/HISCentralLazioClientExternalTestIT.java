package eu.essi_lab.accessor.hiscentral.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeNotNull;

import org.json.JSONArray;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.lazio.HISCentralLazioClient;

/**
 * External integration test for {@link HISCentralLazioClient}: each {@link HISCentralLazioClient#getStations(String)}
 * acquires a token, calls the API, then revokes the token.
 * <p>
 * Run with system properties (example):
 *
 * <pre>
 * mvn -pl accessor/accessor-his-central test -Dtest=HISCentralLazioClientExternalTestIT \
 *   -DgiProxyEndpoint=https://your-gi-proxy/base \
 *   -DHIS-CENTRAL-LAZIO-ENDPOINT=https://rlaziodatacenter.caedns.it/datascape \
 *   -DHIS-CENTRAL-LAZIO-USERNAME=... \
 *   -DHIS-CENTRAL-LAZIO-PASSWORD=... \
 *   -DHIS-CENTRAL-LAZIO-CLIENT-ID=... \
 *   -DHIS-CENTRAL-LAZIO-CLIENT-INSTANCE=...
 * </pre>
 * <p>
 * {@code giProxyEndpoint} is optional; when set it matches production use for token, revoke, and GET via the GI proxy.
 */
public class HISCentralLazioClientExternalTestIT {

    private static final String PROP_ENDPOINT = "PROP_ENDPOINT";

    private static final String PROP_USER = "PROP_USER";

    private static final String PROP_PASSWORD = "PROP_PASSWORD";

    private static final String PROP_CLIENT_ID = "PROP_CLIENT_ID";

    private static final String PROP_CLIENT_INSTANCE = "PROP_CLIENT_INSTANCE";

    private static final int REQUEST_LOOPS = 5;

    private static void requireProps() {

	assumeNotNull("Set -D" + PROP_ENDPOINT, System.getProperty(PROP_ENDPOINT));
	Assume.assumeFalse("Set -D" + PROP_ENDPOINT, System.getProperty(PROP_ENDPOINT).trim().isEmpty());

	assumeNotNull("Set -D" + PROP_USER, System.getProperty(PROP_USER));
	Assume.assumeFalse("Set -D" + PROP_USER, System.getProperty(PROP_USER).trim().isEmpty());

	assumeNotNull("Set -D" + PROP_PASSWORD, System.getProperty(PROP_PASSWORD));
	Assume.assumeFalse("Set -D" + PROP_PASSWORD, System.getProperty(PROP_PASSWORD).trim().isEmpty());

	assumeNotNull("Set -D" + PROP_CLIENT_ID, System.getProperty(PROP_CLIENT_ID));
	Assume.assumeFalse("Set -D" + PROP_CLIENT_ID, System.getProperty(PROP_CLIENT_ID).trim().isEmpty());

	assumeNotNull("Set -D" + PROP_CLIENT_INSTANCE, System.getProperty(PROP_CLIENT_INSTANCE));
	assumeFalse("Set -D" + PROP_CLIENT_INSTANCE, System.getProperty(PROP_CLIENT_INSTANCE).trim().isEmpty());
    }

    @After
    public void resetClientStatics() {

	HISCentralLazioClient.setGiProxyEndpoint(null);
	HISCentralLazioClient.clearLazioClientCredentials();
    }

    @Test
    public void repeatedTokenAcquireUseRevokeViaGetStations() throws Exception {

	requireProps();

	HISCentralLazioClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
	HISCentralLazioClient.setLazioClientCredentials(//
		System.getProperty(PROP_USER).trim(), //
		System.getProperty(PROP_PASSWORD).trim(), //
		System.getProperty(PROP_CLIENT_ID).trim(), //
		System.getProperty(PROP_CLIENT_INSTANCE).trim());

	String endpoint = System.getProperty(PROP_ENDPOINT).trim();
	HISCentralLazioClient client = new HISCentralLazioClient(endpoint);

	String getStationPath = "stations?" //
		+ "category=All&field=StationName&field=StationId&field=Locality&field=City&field=Prov&field=Longitude&field=Latitude&field=Altitude";

	for (int i = 0; i < REQUEST_LOOPS; i++) {

	    String res = client.getStations(getStationPath);

	    assertNotNull("Response null on iteration " + i, res);
	    assertFalse("Empty response on iteration " + i, res.trim().isEmpty());

	    JSONArray stationsArray = new JSONArray(res);
	    assertNotNull(stationsArray);
	    System.out.println("Iteration " + (i + 1) + "/" + REQUEST_LOOPS + ": stations count = " + stationsArray.length());
	}
    }
}
