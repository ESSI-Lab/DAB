package eu.essi_lab.testit.connector.ckan;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.ckan.CKANConnector;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.Source;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class CKANConnectorExternalTestIT {

    private CKANConnector connector;
    private Source source;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() {
	this.connector = new CKANConnector();
	this.source = Mockito.mock(Source.class);
    }

    @Test
    public void testSupport1() {
	Mockito.when(source.getEndpoint()).thenReturn("http://www.google.com");
	TestCase.assertEquals(false, connector.supports(source));
    }

    @Test
    public void testSupport2() {
	Mockito.when(source.getEndpoint()).thenReturn("http://data.jrc.ec.europa.eu");
	TestCase.assertEquals(true, connector.supports(source));
    }

    @Test
    public void testSupport3() {
	Mockito.when(source.getEndpoint()).thenReturn("http://data.jrc.ec.europa.eu/api/3/action/");
	TestCase.assertEquals(true, connector.supports(source));
    }

    @Test
    public void testSupport4() {
	Mockito.when(source.getEndpoint()).thenReturn("http://data.jrc.ec.europa.eu/api/3/action/package_list");
	TestCase.assertEquals(true, connector.supports(source));
    }

    @Test
    public void testSupport5() {
	Mockito.when(source.getEndpoint()).thenReturn("http://data.jrc.ec.europa.eu/api/3/action/package_list?q=");
	TestCase.assertEquals(true, connector.supports(source));
    }

    @Test
    public void testPackageList() throws GSException {
	Mockito.when(source.getEndpoint()).thenReturn("http://data.jrc.ec.europa.eu/api/3/action/package_list?q=");
	connector.setSourceURL(source.getEndpoint());
	Set<String> identifiers = connector.getPackageList();
	Assert.assertTrue(identifiers.contains("jrc-odin-4700058"));
	Assert.assertFalse(identifiers.contains("fake-package-id"));
	Assert.assertTrue(identifiers.size()>2647);

    }

    @Test
    public void testListRecords() throws GSException, IOException {
	Mockito.when(source.getEndpoint()).thenReturn("http://data.jrc.ec.europa.eu");
	connector.setSourceURL("http://data.jrc.ec.europa.eu");

	String firstId = "0026aa70-cc6d-4f6f-8c2f-554a2f9b17f2";
	String secondId = "00a87831-3a64-4a08-a681-3929aeca1876";
	String thirdId = "00acf6ea-e2b5-4f31-988f-9dd4654e398e";
	String lastId = "jrc-trimis-projects";

	// first record
	ListRecordsRequest listRecords = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(listRecords);
	List<OriginalMetadata> metadatas = Lists.newArrayList(response.getRecords());
	Assert.assertEquals(1, metadatas.size());
	String metadata = metadatas.get(0).getMetadata();
	Assert.assertTrue(metadata.contains(firstId));
	Assert.assertEquals(secondId, response.getResumptionToken());
	// second record
	String token = response.getResumptionToken();
	listRecords.setResumptionToken(token);
	response = connector.listRecords(listRecords);
	metadatas = Lists.newArrayList(response.getRecords());
	Assert.assertEquals(1, metadatas.size());
	metadata = metadatas.get(0).getMetadata();
	Assert.assertTrue(metadata.contains(token));
	Assert.assertEquals(thirdId, response.getResumptionToken());
	// last record
	listRecords.setResumptionToken(lastId);
	response = connector.listRecords(listRecords);
	metadatas = Lists.newArrayList(response.getRecords());
	Assert.assertEquals(1, metadatas.size());
	metadata = metadatas.get(0).getMetadata();
	Assert.assertTrue(metadata.contains(lastId));
	Assert.assertNull(response.getResumptionToken());
	// fake record -> GSException
	listRecords.setResumptionToken("fake-record-id");
	expectedException.expect(GSException.class);
	response = connector.listRecords(listRecords);

    }

}
