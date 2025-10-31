package eu.essi_lab.accessor.inmet;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.apitempo.APITempoAccessor;
import eu.essi_lab.accessor.apitempo.APITempoConnector;
import eu.essi_lab.accessor.apitempo.APITempoDownloader;
import eu.essi_lab.accessor.bndmet.BNDMETAccessor;
import eu.essi_lab.accessor.bndmet.BNDMETConnector;
import eu.essi_lab.accessor.bndmet.download.BNDMETDownloader;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.bufr.BUFRAccessor;
import eu.essi_lab.bufr.BUFRConnector;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.ommdk.IResourceMapper;

/**
 * @author Fabrizio
 */
public class ServiceLoaderTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void accessorLoaderTest() {

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    accessors.sort((a1, a2) -> a1.getClass().getSimpleName().compareTo(a2.getClass().getSimpleName()));

	    Assert.assertEquals(4, accessors.size());

	    Assert.assertEquals(APITempoAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(BNDMETAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(BUFRAccessor.class, accessors.get(2).getClass());
	    Assert.assertEquals(INMETAccessor.class, accessors.get(3).getClass());

	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getSimpleName().compareTo(a2.getClass().getSimpleName()));

	    Assert.assertEquals(4, accessors.size());

	    Assert.assertEquals(APITempoAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(BNDMETAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(BUFRAccessor.class, accessors.get(2).getClass());
	    Assert.assertEquals(INMETAccessor.class, accessors.get(3).getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.MIXED);

	    Assert.assertEquals(0, accessors.size());
	}
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedQueryConnectorTest() {

	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	Assert.assertEquals(4, StreamUtils.iteratorToStream(loader.iterator()).count());

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.anyMatch(c -> c.getClass().equals(BNDMETConnector.class)));//

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.anyMatch(c -> c.getClass().equals(APITempoConnector.class)));//

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.anyMatch(c -> c.getClass().equals(BUFRConnector.class)));//

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.anyMatch(c -> c.getClass().equals(INMETConnector.class)));//

    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.APITEMPO_URI)));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.BNDMET_URI)));
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.BUFR_URI)));
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.INMET_CSV_URI)));
   }

    @Test
    public void dataDownloaderTest() {

	ServiceLoader<DataDownloader> loader = ServiceLoader.load(DataDownloader.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(d -> d.getClass().equals(BNDMETDownloader.class)));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(d -> d.getClass().equals(APITempoDownloader.class)));

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(APITempoAccessor.class.getName())));
	

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(APITempoConnector.class.getName())));


	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(BNDMETAccessor.class.getName())));

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(BNDMETConnector.class.getName())));
	
	
	
	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(BUFRAccessor.class.getName())));

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(BUFRConnector.class.getName())));
	
	
	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(INMETAccessor.class.getName())));

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(INMETConnector.class.getName())));


    }
}
