package eu.essi_lab.accessor.fedeo.distributed;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.fedeo.harvested.FEDEOCollectionMapper;
import eu.essi_lab.accessor.fedeo.harvested.FEDEOOriginalMDWrapper;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 */
public class FEDEOCollectionMapperExternalTestIT {

    @Test
    public void test1() throws IOException, GSException {

	OriginalMetadata om = new OriginalMetadata();

	InputStream stream = FEDEOCollectionMapperExternalTestIT.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/fedeo/test/fedeo_19139_entry.xml");

	String md = IOStreamUtils.asUTF8String(stream);

	// Mockito.doReturn(md).when(om).getMetadata();

	om.setMetadata(md);

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	FEDEOOriginalMDWrapper wrapper = new FEDEOOriginalMDWrapper();

	FEDEOCollectionMapper mapper = Mockito.spy(new FEDEOCollectionMapper());

	Mockito.doReturn("https://fedeo.ceos.org/opensearch/request?").when(source).getEndpoint();

	GSResource res = mapper.map(om, source);
	Assert.assertNotNull(res);

	Assert.assertTrue(res.getHarmonizedMetadata().getCoreMetadata().getTitle().contains("ASAR Alternating Polarisation"));

	Assert.assertTrue("The Original Metadata should start with <entry", res.getOriginalMetadata().getMetadata().startsWith("<entry"));

	// Mockito.verify(mapper, Mockito.times(0)).enrichWithSecondLevelUrl(Mockito.any(), Mockito.any(),
	// Mockito.any());

    }

    // check originalMetadata
    @Test
    public void test2() throws IOException, GSException, JAXBException {

	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);

	InputStream stream = FEDEOCollectionMapperExternalTestIT.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/fedeo/test/md_metadata.xml");

	String md = IOStreamUtils.asUTF8String(stream);

	MDMetadata mdMetadata = new MDMetadata(md);

	Assert.assertNotNull(mdMetadata);
    }

    boolean passed = false;

    @Test
    public void testExtendedMetadataField() throws IOException, GSException, SAXException, XPathExpressionException, TransformerException {

	InputStream stream = FEDEOCollectionMapperExternalTestIT.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/fedeo/test/fedeo_iso19139response.xml");

	XMLDocumentReader xdoc = new XMLDocumentReader(stream);

	Node[] entries = xdoc.evaluateNodes("//*:entry");

	List<String> results = new ArrayList<String>();

	for (Node n : entries) {

	    results.add(XMLDocumentReader.asString(n));
	}

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	for (String metadata : results) {

	    OriginalMetadata metadataRecord = new OriginalMetadata();

	    metadataRecord.setMetadata(metadata);
	    metadataRecord.setSchemeURI(FEDEOCollectionMapper.SCHEMA_URI);

	    ret.addRecord(metadataRecord);
	}

	ret.getRecords().forEachRemaining(o -> {

	    try {

		FEDEOCollectionMapper mapper = new FEDEOCollectionMapper();

		GSSource source = new GSSource();
		source.setEndpoint("https://fedeo.ceos.org/opensearch/request?");

		GSResource resource = mapper.map(o, source);

		Optional<String> identifier = resource.getExtensionHandler().getFEDEOSecondLevelInfo();

		System.out.println(identifier);

		// passed if at least one is present
		passed |= identifier.isPresent();

	    } catch (GSException ex) {

		ex.log();
	    }
	});

	Assert.assertTrue(passed);
    }
}