package eu.essi_lab.accessor.cdi;

import java.io.IOException;
import java.util.Set;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.MockedDownloader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class CDIConnectorTest {

    private CDIConnector connector;
    private GSSource source;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() {
	this.connector = new CDIConnector();

	this.source = Mockito.mock(GSSource.class);
    }

    @Test
    public void testMetadataSupport() throws GSException {
	TestCase.assertTrue(connector.listMetadataFormats().contains(CommonNameSpaceContext.SDN_NS_URI));
    }

    @Test
    public void testSupport1() throws GSException {
	Mockito.when(source.getEndpoint()).thenReturn("http://www.google.com");
	connector.setSourceURL("http://www.google.com");
	this.connector.setDownloader(new MockedDownloader("<html>Some html content</html>"));
	TestCase.assertEquals(false, connector.supports(source));
    }

    @Test
    public void testSupport2() throws GSException, IOException {
	Mockito.when(source.getEndpoint()).thenReturn("http://seadatanet.maris2.nl/cdi_aggregation/sdn-cdi-aggr-seadatanet_v3.xml");
	connector.setSourceURL("http://seadatanet.maris2.nl/cdi_aggregation/sdn-cdi-aggr-seadatanet_v3.xml");
	String cdiGroup = IOUtils.toString(CDIConnectorTest.class.getClassLoader().getResourceAsStream("cdiGroup.xml"));
	this.connector.setDownloader(new MockedDownloader(cdiGroup));
	TestCase.assertEquals(true, connector.supports(source));

    }

    @Test
    public void testListRecords() throws GSException, IOException {
	Mockito.when(source.getEndpoint()).thenReturn("http://seadatanet.maris2.nl/cdi_aggregation/sdn-cdi-aggr-seadatanet_v3.xml");
	connector.setSourceURL("http://seadatanet.maris2.nl/cdi_aggregation/sdn-cdi-aggr-seadatanet_v3.xml");
	String cdiGroup = IOUtils.toString(CDIConnectorTest.class.getClassLoader().getResourceAsStream("cdiGroup.xml"));
	this.connector.setDownloader(new MockedDownloader(cdiGroup));
	Set<String> cdiUrls = connector.getCDIUrls();
	Assert.assertTrue(cdiUrls.contains("http://seadatanet.maris2.nl/cdi_aggregation/seadatanet/1022-DS04-3/details.xml"));
	Assert.assertFalse(cdiUrls.contains("http://www.google.com"));
	Assert.assertEquals(553, cdiUrls.size());

	String firstId = "http://seadatanet.maris2.nl/cdi_aggregation/seadatanet/1022-DS02-4/details.xml";
	String secondId = "http://seadatanet.maris2.nl/cdi_aggregation/seadatanet/1022-DS04-3/details.xml";
	String thirdId = "http://seadatanet.maris2.nl/cdi_aggregation/seadatanet/1022-DS04-4/details.xml";
	String lastId = "http://seadatanet.maris2.nl/cdi_aggregation/seadatanet/963-DS07-4/details.xml";

	// first record
	ListRecordsRequest listRecords = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(listRecords);
	Assert.assertEquals(secondId, response.getResumptionToken());
	// second record
	String token = response.getResumptionToken();
	listRecords.setResumptionToken(token);
	response = connector.listRecords(listRecords);
	Assert.assertEquals(thirdId, response.getResumptionToken());
	// last record
	listRecords.setResumptionToken(lastId);
	response = connector.listRecords(listRecords);
	Assert.assertNull(response.getResumptionToken());
	// fake record -> GSException
	listRecords.setResumptionToken("fake-record-id");
	expectedException.expect(GSException.class);
	response = connector.listRecords(listRecords);

    }

}
