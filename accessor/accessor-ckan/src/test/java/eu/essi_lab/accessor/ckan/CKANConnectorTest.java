package eu.essi_lab.accessor.ckan;

import java.io.IOException;
import java.util.Set;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import eu.essi_lab.accessor.ckan.md.CKANConstants;
import eu.essi_lab.lib.net.utils.MockedDownloader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class CKANConnectorTest {

    private CKANConnector connector;
    private GSSource source;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() {
	this.connector = new CKANConnector();
	this.source = Mockito.mock(GSSource.class);
    }

    @Test
    public void testMetadataSupport() throws GSException {
	TestCase.assertTrue(connector.listMetadataFormats().contains(CKANConstants.CKAN));
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
	Mockito.when(source.getEndpoint()).thenReturn("http://drdsi.jrc.ec.europa.eu");
	connector.setSourceURL("http://drdsi.jrc.ec.europa.eu");
	String danubePackage = IOUtils.toString(CKANConnectorTest.class.getClassLoader().getResourceAsStream("danube_package_list.json"));
	this.connector.setDownloader(new MockedDownloader(danubePackage));
	TestCase.assertEquals(true, connector.supports(source));
    }

    @Test
    public void testListRecords() throws GSException, IOException {
	Mockito.when(source.getEndpoint()).thenReturn("http://drdsi.jrc.ec.europa.eu");
	connector.setSourceURL("http://drdsi.jrc.ec.europa.eu");
	String danubePackage = IOUtils.toString(CKANConnectorTest.class.getClassLoader().getResourceAsStream("danube_package_list.json"));
	this.connector.setDownloader(new MockedDownloader(danubePackage));
	Set<String> packageList = connector.getPackageList();
	Assert.assertTrue(packageList.contains("accounts-in-2014-asset-accounts"));
	Assert.assertFalse(packageList.contains("fake-package-id"));
	Assert.assertEquals(10210, packageList.size());

	String firstId = "06-08-2015-administrative-units";
	String secondId = "1-10-000-meretaranyu-topografiai-terkep";
	String thirdId = "1-100-000-meretaranyu-topografiai-terkep";
	String lastId = "zur-zasady-uzemniho-rozvoje-plzeskeho-kraje";

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
