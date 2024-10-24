package eu.essi_lab.accessor.wof;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.ommdk.IResourceMapper;

/**
 * @author ilsanto
 */
public class WOFServiceLoaderTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void testServiceLoaderHarvestedConnector() {

	PluginsLoader<IHarvestedQueryConnector> pluginsLoader = new PluginsLoader<>();
	List<IHarvestedQueryConnector> connectors = pluginsLoader.loadPlugins(IHarvestedQueryConnector.class);

	long count = connectors.stream().//
		filter(connector -> CUAHSIHISServerConnector.class.isAssignableFrom(connector.getClass())).//
		count();
	boolean found = count > 0;

	System.out.println("Found " + count + " query connectors");

	Assert.assertTrue(
		"Can not find " + CUAHSIHISServerConnector.class + " via Java Service Loader of class " + IHarvestedQueryConnector.class,
		found);

    }

    @Test
    public void testServiceLoaderResourceMapper() {

	PluginsLoader<IResourceMapper> pluginsLoader = new PluginsLoader<>();
	List<IResourceMapper> connectors = pluginsLoader.loadPlugins(IResourceMapper.class);

	long count = connectors.stream().//
		filter(connector -> WML_1_1Mapper.class.isAssignableFrom(connector.getClass())).//
		count();
	boolean found = count > 0;

	System.out.println("Found " + count + " query connectors");

	Assert.assertTrue("Can not find " + WML_1_1Mapper.class + " via Java Service Loader of class " + IResourceMapper.class, found);
    }
}