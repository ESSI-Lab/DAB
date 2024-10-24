package eu.essi_lab.accessor.wekeo;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WEKEOClientExternalTestIT {

    private WEKEOClient client;

    @Before
    public void init() {
	this.client = new WEKEOClient();

	client.setUser(System.getProperty("wekeo.username"));
	client.setPassword(System.getProperty("wekeo.password"));

    }

    @Test
    public void testGETToken() throws Exception {

	String token = client.getToken();
	System.out.println(token);
	Assert.assertTrue(token != null);

    }

    @Test
    public void testGETCollectionId() throws Exception {

	String token = client.getToken();
	System.out.println(token);

	List<String> res = client.getCollections(token);
	System.out.println(res.size());
	Assert.assertTrue(res.size() > 0);

    }

    @Test
    public void testGETMetadataCollectionId() throws Exception {

	String token = client.getToken();
	System.out.println(token);

	String id = "EO:ECMWF:DAT:ERA5_HOURLY_VARIABLES_ON_PRESSURE_LEVELS";

	String res = client.getMetadataCollection(id, token);
	System.out.println(res);
	Assert.assertTrue(res != null);

    }

}
