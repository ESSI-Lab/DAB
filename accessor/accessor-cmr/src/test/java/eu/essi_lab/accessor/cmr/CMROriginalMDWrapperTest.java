package eu.essi_lab.accessor.cmr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.cmr.harvested.CMROriginalMDWrapper;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class CMROriginalMDWrapperTest {

    private Logger logger = GSLoggerFactory.getLogger(CMROriginalMDWrapper.class);

    @Test
    public void test() throws IOException, SAXException, TransformerException, GSException, JAXBException, XPathExpressionException {

	CommonContext.createMarshaller(true);
	CommonContext.createUnmarshaller();

	InputStream stream = CMROriginalMDWrapperTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cwicOriginal.xml");

	XMLDocumentReader reader = new XMLDocumentReader(stream);

	CMROriginalMDWrapper wrapper = new CMROriginalMDWrapper();

	OriginalMetadata originalMetadata = new OriginalMetadata();

	String metadata = XMLDocumentReader.asString(reader.getDocument());

	originalMetadata.setMetadata(metadata);

	String url = "http://cmr.wgiss.ceos.org/opensearch/granules.atom?datasetId=C1481-GHRC&startIndex={startIndex?}&count={count?}&timeStart={time:start}&timeEnd={time:end}&geoBox={geo:box}&clientId=pippo";

	String url2 = "http://cmr.wgiss.ceos.org/opensearch/granules.atom?datasetId=C1481-GHRC&";

	OriginalMetadata wrappedMetadata = wrapper.wrap(originalMetadata, url, url2);

	logger.debug("Wrapped metadata \n{}", wrappedMetadata.getMetadata());

	Assert.assertEquals(url, wrapper.getUrl(wrappedMetadata));

	Assert.assertEquals(url2, wrapper.getCMRBaseOSDDUrl(wrappedMetadata));

	OriginalMetadata unwrapped = wrapper.getOriginalMetadata(wrappedMetadata);

	compareMetadata(metadata, unwrapped.getMetadata());

    }

    private void compareMetadata(String expected, String found) throws IOException, SAXException, XPathExpressionException {

	XMLDocumentReader expectedReader = new XMLDocumentReader(new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)));

	XMLDocumentReader foundReader = new XMLDocumentReader(new ByteArrayInputStream(found.getBytes(StandardCharsets.UTF_8)));

	String expectedId = expectedReader.evaluateString("//*:fileIdentifier/*:CharacterString");

	String foundId = foundReader.evaluateString("//*:fileIdentifier/*:CharacterString");

	Assert.assertEquals(expectedId, foundId);

	Assert.assertEquals("MI_Metadata", foundReader.evaluateNode("/*").getLocalName());

    }

}