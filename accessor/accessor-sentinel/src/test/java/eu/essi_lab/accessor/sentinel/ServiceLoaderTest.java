package eu.essi_lab.accessor.sentinel;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.accessor.sentinel.access.SentinelAccessAugmenter;
import eu.essi_lab.accessor.sentinel.downloader.SentinelDownloader;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cfga.Configurable;
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

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(SentinelAccessor.class, accessors.get(0).getClass());

	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(SentinelAccessor.class, accessors.get(0).getClass());
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

	Assert.assertEquals(1, StreamUtils.iteratorToStream(loader.iterator()).count());

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.filter(c -> c.getClass().equals(SentinelConnector.class)).//
			findFirst().isPresent());//
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(SentinelMapper.SENTINEL_SCHEME_URI)).//

		findFirst().//
		isPresent());

    }

    @Test
    public void dataDownloaderTest() {

	ServiceLoader<DataDownloader> loader = ServiceLoader.load(DataDownloader.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(d -> d.getClass().equals(SentinelDownloader.class)).//
		findFirst().//
		isPresent());

    }

    @Test
    public void augmentersTest() {

	@SuppressWarnings("rawtypes")
	ServiceLoader<Augmenter> loader = ServiceLoader.load(Augmenter.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(d -> d.getClass().equals(SentinelThumbnailAugmenter.class)).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(d -> d.getClass().equals(SentinelOnlineResourceAugmenter.class)).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(d -> d.getClass().equals(SentinelAccessAugmenter.class)).//
		findFirst().//
		isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(SentinelAccessor.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(SentinelConnector.class.getName())).//
		findFirst().//
		isPresent());

    }
}
