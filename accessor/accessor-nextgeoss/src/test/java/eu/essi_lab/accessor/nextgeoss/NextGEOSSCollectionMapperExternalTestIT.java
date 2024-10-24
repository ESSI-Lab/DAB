package eu.essi_lab.accessor.nextgeoss;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.nextgeoss.harvested.NextGEOSSCollectionMapper;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author roncella
 */
public class NextGEOSSCollectionMapperExternalTestIT {

    @Test
    public void test1() throws IOException, GSException {

	OriginalMetadata om = new OriginalMetadata();

	InputStream stream = NextGEOSSCollectionMapperExternalTestIT.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/nextgeoss/test/firstCollection.xml");

	String originalAtom = IOStreamUtils.asUTF8String(stream);

	// Mockito.doReturn(md).when(om).getMetadata();

	om.setMetadata(originalAtom);

	GSSource source = Mockito.mock(GSSource.class);

	String sid = "sourceid";
	Mockito.doReturn(sid).when(source).getUniqueIdentifier();

	NextGEOSSCollectionMapper mapper = Mockito.spy(new NextGEOSSCollectionMapper());
	
	Mockito.doReturn("https://catalogue.nextgeoss.eu/opensearch/collection_search.atom?").when(source).getEndpoint();

	GSResource res = mapper.map(om, source);
	Assert.assertNotNull(res);

	Assert.assertTrue(res.getHarmonizedMetadata().getCoreMetadata().getTitle().contains("CGS S1 GRD L1"));


	// Mockito.verify(mapper, Mockito.times(0)).enrichWithSecondLevelUrl(Mockito.any(), Mockito.any(),
	// Mockito.any());

    }

    // check originalMetadata
//    @Test
//    public void test2() throws IOException, GSException, JAXBException {
//
//	OriginalMetadata om = Mockito.mock(OriginalMetadata.class);
//
//	InputStream stream = NextGEOSSCollectionMapperExternalTestIT.class.getClassLoader()
//		.getResourceAsStream("eu/essi_lab/accessor/fedeo/test/md_metadata.xml");
//
//	String md = IOStreamUtils.asUTF8String(stream);
//
//	MDMetadata mdMetadata = new MDMetadata(md);
//
//	Assert.assertNotNull(mdMetadata);
//
//    }
    
}