package eu.essi_lab.accessor.wof;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.resource.OriginalMetadata;

public class CUAHSIHISServerConnectorExternalTestIT {

    @Rule
    public ExpectedException ee = ExpectedException.none();
    protected static final String ENDPOINT2 = "https://hisprod.rtnccad.epa.gov/prod/CUAHSI_1_1.asmx?WSDL";
    protected static final String ENDPOINT3 = "http://hydroportal.cuahsi.org/GlobalRiversObservatory/webapp/cuahsi_1_1.asmx?WSDL";
    protected static final String ENDPOINT1 = "http://icewater.usu.edu/littlebearriverwof/cuahsi_1_1.asmx?WSDL";
    protected static final String ENDPOINT_1_0 = "http://icewater.boisestate.edu/srbdataservices/cuahsi_1_0.asmx?WSDL";
    protected static final String ENDPOINT_BIG_SERVICE = "http://hydroportal.cuahsi.org/nwisgw/cuahsi_1_1.asmx?WSDL";
    @SuppressWarnings("rawtypes")
    private CUAHSIHISServerConnector connector;

    @SuppressWarnings("rawtypes")
    @Before
    public void init() {
	this.connector = new CUAHSIHISServerConnector();
	this.connector.setSourceURL(ENDPOINT1);
    }

    @Test
    public void testSupport() {
	Assert.assertEquals(1, connector.listMetadataFormats().size());
	Assert.assertEquals(CommonNameSpaceContext.WML1_NS_URI, connector.listMetadataFormats().get(0));
	Assert.assertEquals(new ESSILabProvider(), connector.getProvider());
	try {
	    Assert.assertFalse(connector.supportsIncrementalHarvesting());
	} catch (GSException e) {
	    e.printStackTrace();
	}
	GSSource source1 = new GSSource();
	source1.setEndpoint(ENDPOINT1);
	Assert.assertTrue(connector.supports(source1));
	GSSource source2 = new GSSource();
	source2.setEndpoint(ENDPOINT2);
	Assert.assertTrue(connector.supports(source2));
	GSSource source3 = new GSSource();
	source3.setEndpoint(ENDPOINT3);
	Assert.assertTrue(connector.supports(source3));
	
	GSSource source5 = new GSSource();
	source5.setEndpoint(null);
	Assert.assertFalse(connector.supports(source5));
	GSSource source6 = new GSSource();
	source6.setEndpoint("http://www.google.com");
	Assert.assertFalse(connector.supports(source6));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {
	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	String token = response.getResumptionToken();
	int count = 0;
	while (token != null && count < 14) {
	    System.out.println(token);
	    Iterator<OriginalMetadata> iterator = response.getRecords();
	    List<OriginalMetadata> list = Lists.newArrayList(iterator);
	    Assert.assertEquals(1, list.size());
	    count++;
	    token = response.getResumptionToken();
	    request.setResumptionToken(token);
	    response = connector.listRecords(request);
	}
	System.out.println("Retrieved " + count + " original metadata records.");
	Assert.assertTrue(count >= 14);
    }

    @Test
    public void testWrongToken1() throws GSException {
	ee.expect(GSException.class);
	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken("pippo");
	connector.listRecords(request);
    }

    @Test
    public void testWrongToken2() throws GSException {
	ee.expect(GSException.class);
	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken("pippo:pippo");
	connector.listRecords(request);
    }

    @Test
    public void testWrongToken3() throws GSException {
	ee.expect(GSException.class);
	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken("-1:0");
	connector.listRecords(request);
    }

    @Test
    public void testWrongToken4() throws GSException {
	ee.expect(GSException.class);
	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken("2300:0");
	connector.listRecords(request);
    }

    @Test
    public void testWrongToken5() throws GSException {
	ee.expect(GSException.class);
	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken("0:-1");
	connector.listRecords(request);
    }

    @Test
    public void testWrongToken6() throws GSException {
	ee.expect(GSException.class);
	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken("0:2300");
	connector.listRecords(request);
    }
}
