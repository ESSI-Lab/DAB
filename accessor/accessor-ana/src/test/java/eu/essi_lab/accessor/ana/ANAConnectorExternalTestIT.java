package eu.essi_lab.accessor.ana;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.MockedDownloader;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class ANAConnectorExternalTestIT {

    private ANAConnector connector;
    private GSSource source;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() throws Exception {
	this.connector = new ANAConnector();

	this.source = Mockito.mock(GSSource.class);
    }

    @Test
    public void testMetadataSupport() throws GSException {
	TestCase.assertTrue(connector.listMetadataFormats().contains(CommonNameSpaceContext.ANA_URI));
    }

    @Test
    public void testSupport1() throws GSException {
	Mockito.when(source.getEndpoint()).thenReturn("http://www.google.com");
	connector.setSourceURL("http://www.google.com");
	this.connector.setDownloader(new MockedDownloader("<html>Some html content</html>"));
	// List<String> value = new ArrayList<String>();
	// value.add("https://mail.google.com");
	// Mockito.when(connector.getWebConnector()).thenReturn(Mockito.mock(WebConnector.class));
	// Mockito.when(connector.getWebConnector().getHrefs("http://www.google.com")).thenReturn(value);
	TestCase.assertEquals(false, connector.supports(source));
    }

    @Test
    public void testSupport2() throws GSException, IOException, Exception {
	Mockito.when(source.getEndpoint()).thenReturn("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");
	connector.setSourceURL("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");
	TestCase.assertEquals(true, connector.supports(source));

    }

    /*
     * commented because requires too much time ~~ 30 minutes
     */
    @Test
    public void testListRecords() throws GSException, IOException {
	Mockito.when(source.getEndpoint()).thenReturn("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");
	connector.setSourceURL("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");

	String secondId = "2";

	// first record
	ListRecordsRequest listRecords = new ListRecordsRequest();
	connector.getSetting().setMaxRecords(5);
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(listRecords);
	Assert.assertEquals(secondId, response.getResumptionToken());

	listRecords.setResumptionToken("9");
	response = connector.listRecords(listRecords);
	Assert.assertNull(response.getResumptionToken());

	// connector.setMaxRecords(10);
	// listRecords.setResumptionToken(null);
	// response = connector.listRecords(listRecords);
	// Assert.assertEquals("10", response.getResumptionToken());
	//
//	10200000
    }

    @Test
    public void testFirsSiteOnly() throws GSException, IOException {
	Mockito.when(source.getEndpoint()).thenReturn("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");
	connector.setSourceURL("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");

	// first record
	ListRecordsRequest listRecords = new ListRecordsRequest();
	connector.setFirstSiteOnly(true);
	connector.getSetting().setMaxRecords(5);
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(listRecords);
	Iterator<OriginalMetadata> iterator = response.getRecords();
	int i = 0;
	while (iterator.hasNext()) {
	    i++;
	    iterator.next();
	}
	System.out.println(i);
	Assert.assertEquals(null, response.getResumptionToken());

    }
    
    @Test
    public void testHydroInventario() throws GSException {
	connector.setSourceURL("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");
	HydroInventario hydroInventario = connector.getHydroInventario();

    }
    
    @Test
    public void testfindDates() {
	connector.setSourceURL("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");
	String code = "10200000";
	ANAVariable var = ANAVariable.CHUVA;
	Date endDate = connector.getEndDate(code, var);
	Date startDate = connector.getStartDate(code, var, ISO8601DateTimeUtils.parseISO8601ToDate("1900-01-01").get(), endDate);
	assertNotNull(startDate);
	assertNotNull(endDate);
	assertTrue(endDate.after(startDate));
    }
    
    @Test
    public void testfindDates2() {
	connector.setSourceURL("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");
	String code = "10075000";
	ANAVariable var = ANAVariable.CHUVA;
	Date endDate = connector.getEndDate(code, var);
	Date startDate = connector.getStartDate(code, var, ISO8601DateTimeUtils.parseISO8601ToDate("1900-01-01").get(), endDate);
	assertNotNull(startDate);
	assertNotNull(endDate);
	assertTrue(endDate.after(startDate));
    }
    
    

}
