package eu.essi_lab.accessor.savahis.test;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.savahis.SavaHISAccessor;
import eu.essi_lab.accessor.savahis.SavaHISConnector;
import eu.essi_lab.accessor.savahis.downloader.SavaHISDownloader;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.ommdk.IResourceMapper;

import static eu.essi_lab.lib.utils.StreamUtils.*;

/**
 * @author Fabrizio
 */
public class ServiceLoaderTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void accessorLoaderTest() {

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(SavaHISAccessor.class, accessors.get(0).getClass());

	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(SavaHISAccessor.class, accessors.get(0).getClass());
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

	Assert.assertEquals(1, iteratorToStream(loader.iterator()).count());

	Assert.assertTrue(//
		iteratorToStream(//
				loader.iterator())
			.anyMatch(c -> c.getClass().equals(SavaHISConnector.class)));//
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.SAVAHIS_URI)));

    }
    
    @Test
    public void downloaderTest() {

	ServiceLoader<DataDownloader> loader = ServiceLoader.load(DataDownloader.class);

	Assert.assertTrue(iteratorToStream(loader.iterator()).//
		anyMatch(d -> d.getClass().equals(SavaHISDownloader.class)));

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(SavaHISAccessor.class.getName())));

	Assert.assertTrue(
		iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(SavaHISConnector.class.getName())));

    }
}
