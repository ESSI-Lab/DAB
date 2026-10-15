package eu.essi_lab.accessor.datalakes;

import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.ommdk.IResourceMapper;

public class ServiceLoaderTest {

    @Test
    public void harvestedAccessorTest() {
	ServiceLoader<IHarvestedAccessor> loader = ServiceLoader.load(IHarvestedAccessor.class);
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(accessor -> accessor.getClass().equals(DatalakesAccessor.class)));
    }

    @Test
    public void harvestedQueryConnectorTest() {
	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(connector -> connector.getClass().equals(DatalakesConnector.class)));
    }

    @Test
    public void resourceMapperTest() {
	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(mapper -> mapper.getClass().equals(DatalakesMapper.class)));
    }

    @Test
    public void dataDownloaderTest() {
	ServiceLoader<DataDownloader> loader = ServiceLoader.load(DataDownloader.class);
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(downloader -> downloader.getClass().equals(DatalakesDownloader.class)));
    }

    @Test
    public void configurableTest() {
	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(configurable -> configurable.getClass().equals(DatalakesAccessor.class)));
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(configurable -> configurable.getClass().equals(DatalakesConnector.class)));
    }
}
