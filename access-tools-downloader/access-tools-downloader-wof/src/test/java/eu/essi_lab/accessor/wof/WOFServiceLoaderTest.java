package eu.essi_lab.accessor.wof;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.downloader.wof.CUAHSIHISServerDownloader;
import eu.essi_lab.model.pluggable.PluginsLoader;

/**
 * @author ilsanto
 */
public class WOFServiceLoaderTest {

   
    @Test
    public void testServiceLoaderDataDownloader() {

	PluginsLoader<DataDownloader> pluginsLoader = new PluginsLoader<>();
	List<DataDownloader> connectors = pluginsLoader.loadPlugins(DataDownloader.class);

	long count = connectors.size();
	boolean found = false;
	for (DataDownloader dataDownloader : connectors) {
	    if (dataDownloader instanceof CUAHSIHISServerDownloader) {
		found = true;

	    }
	}

	System.out.println("Found " + count + " data downloader");

	Assert.assertTrue("Can not find " + CUAHSIHISServerDownloader.class + " via Java Service Loader of class " + DataDownloader.class,
		count > 0 && found);

    }

}