package eu.essi_lab.accessor.inmet;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.FTPDownloader;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class INMETConnectorExternalTestIT {

    private INMETConnector connector;
    private GSSource source;
    private XMLReader tagsoupReader;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() throws Exception {
	this.connector = new INMETConnector();

	this.source = Mockito.mock(GSSource.class);
	this.tagsoupReader = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
	this.tagsoupReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    }

    @Ignore // obsolete accessor
    @Test
    public void testMetadataSupport() throws GSException {
	TestCase.assertTrue(connector.listMetadataFormats().contains(CommonNameSpaceContext.INMET_CSV_URI));
    }

    @Ignore // obsolete accessor
    @Test
    public void testListRecords() throws GSException, IOException {
	Mockito.when(source.getEndpoint()).thenReturn("ftp://ftp.inmet.gov.br/");
	connector.setSourceURL("ftp://ftp.inmet.gov.br/");

	this.connector.setDownloader(new FTPDownloader());

	// List<String> value = new ArrayList<String>();
	// value.add("http://dd.weather.gc.ca/hydrometric/csv/");
	// value.add("http://dd.weather.gc.ca/hydrometric/doc/");
	//
	// Mockito.when(connector.getWebConnector().getHrefs(canadaHTML)).thenReturn(value);

	connector.getINMETFiles();

	String secondId = "PREC_A002_20180927.HIS.CSV";

	// first record
	ListRecordsRequest listRecords = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(listRecords);
	Assert.assertEquals(secondId, response.getResumptionToken());

	int stationsnumber = connector.getINMETfileSize();
	int var = stationsnumber / 10;
	var = var * 10;

	// add max number of records
	// connector.setMaxRecords(50);
	// listRecords.setResumptionToken("51");
	// response = connector.listRecords(listRecords);
	// Assert.assertEquals(null, response.getResumptionToken());
	//
	// connector.setMaxRecords(10);
	// listRecords.setResumptionToken(null);
	// response = connector.listRecords(listRecords);
	// Assert.assertEquals("10", response.getResumptionToken());

    }
    //

    /*
     * Test for retrieving files with null date time.
     * The following files contains all 9999 values
     * PREC_A109_20180927.HIS.CSV
     * PREC_S111_20180927.HIS.CSV
     */
    @Ignore // obsolete accessor
    public void checkFileWithoutTime() throws GSException, IOException {

	Mockito.when(source.getEndpoint()).thenReturn("ftp://ftp.inmet.gov.br/");
	connector.setSourceURL("ftp://ftp.inmet.gov.br/");
	this.connector.setDownloader(new FTPDownloader());

	connector.getINMETFiles();

	List<String> filesNames = connector.getINMETfileNames();

	for (String name : filesNames) {

	    File file = connector.getDownloader().downloadStream("ftp://broker:Pla645!z@ftp.inmet.gov.br/", name);
	    String res = connector.createCSVMetadataRecord(file, name);
	    if (res == null)
		System.out.println(name);

	}

    }

}
