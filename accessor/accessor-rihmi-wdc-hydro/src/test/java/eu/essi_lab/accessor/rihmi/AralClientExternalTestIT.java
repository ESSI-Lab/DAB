package eu.essi_lab.accessor.rihmi;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class AralClientExternalTestIT {

    
    private RIHMIClient client = null;

    @Before
    public void before() {
	this.client  = new RIHMIClient();
	client.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
    }
    

    @Test
    public void test() throws Exception {
	
	Date start = ISO8601DateTimeUtils.parseISO8601ToDate("2020-08-20T08:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2023-10-17T08:00Z").get();
	InputStream data = client.getWaterML("70801", start, end);
	XMLDocumentReader reader = new XMLDocumentReader(data);
	Number count = reader.evaluateNumber("count(//*:value)");
	System.out.println(count.longValue() + " values retrieved");
	assertTrue(count.longValue() > 20);
    }

    @Test
    public void test2() throws Exception {
	Date start = ISO8601DateTimeUtils.parseISO8601ToDate("2020-08-20T08:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2023-10-17T08:00Z").get();
	InputStream data = client.getWaterML("09803", start, end);
	XMLDocumentReader reader = new XMLDocumentReader(data);
	Number count = reader.evaluateNumber("count(//*:value)");
	System.out.println(count.longValue() + " values retrieved");
	assertTrue(count.longValue() > 10);
    }

    @Test
    public void testStationIdentifiers() throws Exception {
	RIHMIClient client = new RIHMIClient();
	List<String> ids = client.getStationIdentifiers(true);
	System.out.println(ids.size() + " values retrieved");
	assertTrue(ids.size() > 60);
    }

}
