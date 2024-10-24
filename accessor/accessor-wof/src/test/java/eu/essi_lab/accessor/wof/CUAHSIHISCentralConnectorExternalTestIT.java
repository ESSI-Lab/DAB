package eu.essi_lab.accessor.wof;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.resource.OriginalMetadata;

public class CUAHSIHISCentralConnectorExternalTestIT {

    @Rule
    public ExpectedException ee = ExpectedException.none();
    protected static final String ENDPOINT1 = "http://hiscentral.cuahsi.org/webservices/hiscentral.asmx";
    protected static final String ENDPOINT2 = "http://193.206.192.247/hiscentral/webservices/hiscentral.asmx";
    private static final boolean LONG_TESTS = false;
    private CUAHSIHISCentralConnector connector;

    @Before
    public void init() {
	this.connector = new CUAHSIHISCentralConnector();
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
	source3.setEndpoint(null);
	Assert.assertFalse(connector.supports(source3));
	GSSource source4 = new GSSource();
	source4.setEndpoint("http://www.google.com");
	Assert.assertFalse(connector.supports(source4));
    }

    @Test
//    @Ignore
    public void testAllServices() throws Exception {
	Integer server = 62;
	String max = "";
	main: while (server < 98) {
	    int site = 0;
	    server++;
	    inner: while (true) {
		ListRecordsRequest request = new ListRecordsRequest();
		String resumptionToken = server.toString() + ":" + site;
		switch (resumptionToken) {
		case "3:0": // https://www4.des.state.nh.us/WaterOneFlow/cuahsi_1_1.asmx 403
		case "44:0": // https://hydro1.gesdisc.eosdis.nasa.gov/daac-bin/his/1.0/NLDAS_FORA_002.cgi?
//		case "63:0": // https://hydroportal.cuahsi.org/nwisgw/cuahsi_1_1.asmx?
		case "77:0": // https://hydro1.gesdisc.eosdis.nasa.gov/daac-bin/his/1.0/NLDAS_NOAH_002.cgi?

		    continue main;
		default:
		    break;
		}
		request.setResumptionToken(resumptionToken);
		ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
		List<OriginalMetadata> records = response.getRecordsAsList();
		GSLoggerFactory.getLogger(getClass()).info("\n");
		GSLoggerFactory.getLogger(getClass()).info("\nObtained {} records from:{}\n\n", records.size(), resumptionToken);
		if (records != null && !records.isEmpty()) {
		    if (site > 0) {
			max += "Obtained " + records.size() + " from " + resumptionToken + "\n";
		    }
		    continue main;
		}
		site++;
	    }

	}
	System.out.println(max);
    }

    @Test
    public void test() throws Exception {
	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	printResponse(response);
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
	    printResponse(response);
	}
	System.out.println("Retrieved " + count + " original metadata records.");
	Assert.assertTrue(count >= 14);
    }

    @Test
    public void testUS() throws Exception {

	if (LONG_TESTS) {

	    List<Integer> knownErrors = new ArrayList<Integer>();

	    // knownErrors.add("http://icewater.boisestate.edu/srbdataservices/cuahsi_1_0.asmx?WSDL");
	    // Snake River Basin, Modeled Streamflow
	    // knownErrors.add("http://icewater.boisestate.edu/dcew2dataservices/cuahsi_1_0.asmx?WSDL");
	    // Dry Creek Experimental Watershed, SW Idaho
	    // knownErrors.add("http://icewater.geology.isu.edu/PortneufWatershed/cuahsi_1_0.asmx?WSDL");
	    // Portneuf Watershed Observations, Idaho
	    // knownErrors.add("http://worldwater.byu.edu/interactive/gill_lab/services/index.php/cuahsi_1_1.asmx?WSDL");
	    // Gill Ecosystem and Global Change Ecology Lab-BYU

	    // NWS-WGRFC Hourly Multi-sensor Precipitation Estimates
	    // NWS-WGRFC Daily Multi-sensor Precipitation Estimates Recent Values
	    // NWS-LMRFC Hourly Multi Sensor Precipitation Estimates
	    // USACE Hourly Reservoir Discharges values

	    testCentral(0, 90, knownErrors);
	}

    }

    @Test
    public void testITA() throws Exception {

	if (LONG_TESTS) {
	    this.connector.setSourceURL(ENDPOINT2);
	    List<Integer> knownErrors = new ArrayList<Integer>();
	    // Permanent errors:

	    testCentral(0, 18, knownErrors);

	}
    }

    private void testCentral(int start, int expected, List<Integer> knownErrors) {
	String token = start + ":";
	while (true) {
	    try {
		ListRecordsRequest request = new ListRecordsRequest();
		request.setResumptionToken(token);
		ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
		printResponse(response);
		Iterator<OriginalMetadata> iterator = response.getRecords();
		List<OriginalMetadata> list = Lists.newArrayList(iterator);
		String resumptionToken = response.getResumptionToken();
		if (list.isEmpty()) {
		    if (resumptionToken.startsWith("" + start + ":")) {
			token = resumptionToken;
			System.out.println(start + ": possibly recoverable error");
			continue;
		    } else {
			if (knownErrors.contains(start)) {
			    System.out.println(start + ": Known permanent failure");
			    start++;
			} else {
			    fail("Unexpeced permanent failure with service " + start);
			    // System.out.println(start + ": permanent failure");
			    // start++;
			}
		    }
		} else {
		    System.out.println(start + ": success");
		    start++;
		    token = start + ":";
		}
	    } catch (Exception e) {
		System.out.println(start + ": List finished fine");
		assertTrue(start > expected);
		return;
	    }
	}

    }

    private void printResponse(ListRecordsResponse<OriginalMetadata> response) {
	Iterator<OriginalMetadata> iterator = response.getRecords();
	if (iterator.hasNext()) {
	    OriginalMetadata metadata = iterator.next();
	    String md = metadata.getMetadata();
	    System.out.println(md.substring(0, 100).replace("\n", ""));
	}

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

}
