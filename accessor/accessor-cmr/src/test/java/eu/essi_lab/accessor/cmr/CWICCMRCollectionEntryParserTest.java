package eu.essi_lab.accessor.cmr;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMRCollectionEntryParser;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class CWICCMRCollectionEntryParserTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test() throws IOException, SAXException, GSException {

	InputStream stream = CWICCMRCollectionEntryParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cwicosdd.xml");

	CWICCMRCollectionEntryParser parser = new CWICCMRCollectionEntryParser(stream);

	String clientid = "clientid";
	String url = parser.getSecondLevelOpenSearchDD(clientid);

	Assert.assertEquals("http://cwic.wgiss.ceos.org/opensearch/datasets/C1481-GHRC/osdd.xml?clientId=" + clientid, url);
    }

    @Test
    public void testNoURL() throws IOException, SAXException, GSException {

	expectedException.expect(GSException.class);

	InputStream stream = CWICCMRCollectionEntryParserTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cwicosddNoUrl.xml");

	CWICCMRCollectionEntryParser parser = new CWICCMRCollectionEntryParser(stream);

	String clientid = "clientid";
	String url = parser.getSecondLevelOpenSearchDD(clientid);

	Assert.assertEquals("http://cmr.wgiss.ceos.org/opensearch/datasets/C1481-GHRC/osdd.xml?clientId=", url);
    }
}