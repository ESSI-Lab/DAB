package eu.essi_lab.accessor.wof.client;

import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.wof.client.datamodel.SiteInfo;
import eu.essi_lab.model.exceptions.GSException;
import junit.framework.TestCase;

public class CUAHSIHISServerClient2ExternalTestIT extends CUAHSIHISServerClientTest {

    @Before
    public void init() {
	this.client = new CUAHSIHISServerClient1_1(CUAHSIEndpoints.ENDPOINT2);
    }

    @Test
    public void testServer2() throws GSException, UnsupportedEncodingException, TransformerException {
	SiteInfo site = testHISServerEndpoint(client, 100);
	TestCase.assertEquals("Trib to East Fork River Main Stemat Round Bottom Rd. ", site.getSiteName());
	TestCase.assertEquals("EPA_Lake_Harsha_Data", site.getSiteCodeNetwork());
	TestCase.assertEquals("1", site.getSiteId());
	TestCase.assertEquals("143", site.getSiteCode());
	TestCase.assertEquals("39.156", site.getLatitude());
	TestCase.assertEquals("-84.287", site.getLongitude());
	TestCase.assertEquals("Clermont", site.getCounty());
	TestCase.assertEquals("OH", site.getState());
	// This service does not have series!
	TestCase.assertEquals(0, site.getSeries().size());
    }

}
