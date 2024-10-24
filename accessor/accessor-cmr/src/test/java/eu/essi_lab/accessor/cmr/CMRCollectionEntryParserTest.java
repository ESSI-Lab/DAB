package eu.essi_lab.accessor.cmr;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMRCollectionEntryParser;
import eu.essi_lab.accessor.cmr.harvested.CMRCollectionEntryParser;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class CMRCollectionEntryParserTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test() throws IOException, SAXException, GSException {

	InputStream stream = CMRCollectionEntryParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cmrosdd.xml");

	String baseurl = "https://cmr.earthdata.nasa.gov/testurl";

	CMRCollectionEntryParser parser = new CMRCollectionEntryParser(stream, baseurl);

	String clientid = "clientid";

	String url = parser.getSecondLevelOpenSearchDD(clientid);

	Assert.assertEquals(baseurl + "?datasetId=C1214587974-SCIOPS&dataCenter=SCIOPS&shortName=KUKRI_He&clientId=" + clientid, url);

    }

    @Test
    public void test2() throws IOException, SAXException, GSException {

	InputStream stream = CMRCollectionEntryParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cmrosdd-withversion.xml");

	String baseurl = "https://cmr.earthdata.nasa.gov/testurl";

	CMRCollectionEntryParser parser = new CMRCollectionEntryParser(stream, baseurl);

	String clientid = "clientid";

	String url = parser.getSecondLevelOpenSearchDD(clientid);

	Assert.assertEquals(baseurl + "?datasetId=C1214587974-SCIOPS&dataCenter=SCIOPS&shortName=KUKRI_He&versionId=1&clientId=" + clientid,
		url);

    }

    @Test
    public void testNoShort() throws IOException, SAXException, GSException {

	expectedException.expect(GSException.class);

	InputStream stream = CWICCMRCollectionEntryParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cmrosdd-nosn.xml");

	CWICCMRCollectionEntryParser parser = new CWICCMRCollectionEntryParser(stream);

	String clientid = "clientid";
	String url = parser.getSecondLevelOpenSearchDD(clientid);

    }

    @Test
    public void testNoDId() throws IOException, SAXException, GSException {

	expectedException.expect(GSException.class);

	InputStream stream = CWICCMRCollectionEntryParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cmrosdd-noid.xml");

	CWICCMRCollectionEntryParser parser = new CWICCMRCollectionEntryParser(stream);

	String clientid = "clientid";
	String url = parser.getSecondLevelOpenSearchDD(clientid);

    }

    @Test
    public void testNoDataCenter() throws IOException, SAXException, GSException {

	expectedException.expect(GSException.class);

	InputStream stream = CWICCMRCollectionEntryParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cmrosdd-nodc.xml");

	CWICCMRCollectionEntryParser parser = new CWICCMRCollectionEntryParser(stream);

	String clientid = "clientid";
	String url = parser.getSecondLevelOpenSearchDD(clientid);

    }

}