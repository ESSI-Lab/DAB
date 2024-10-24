package eu.essi_lab.plugins;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.DataValidator;
import eu.essi_lab.cdk.IDriverConnector;
import eu.essi_lab.indexes.CustomIndexedElements;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.DynamicView;
import eu.essi_lab.model.pluggable.PluginsLoader;
import eu.essi_lab.ommdk.IResourceMapper;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.rsf.MessageResponseFormatter;
import eu.essi_lab.pdk.rsm.MessageResponseMapper;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.csw.profile.CSWProfile;
import eu.essi_lab.profiler.oaipmh.profile.OAIPMHProfile;

/**
 * The test checks that all declared plugins are found. This is useful to prevent runtime errors.
 * 
 * @author boldrini
 */
public class ServiceLoaderTest {

    public void testExpectedSize(Class plugin, int count) {
	PluginsLoader<?> pluginsLoader = new PluginsLoader<>();
	List<?> plugins = pluginsLoader.loadPlugins(plugin);
	GSLoggerFactory.getLogger(getClass()).info("Found " + plugins.size() + " " + plugin.getSimpleName());
	assertTrue(plugins.size() >= count);

    }

    @Test
    public void testExpectedSizes() {
	testExpectedSize(CSWProfile.class, 3);
	testExpectedSize(CustomIndexedElements.class, 0);
	testExpectedSize(DataDownloader.class, 15);
	testExpectedSize(DynamicView.class, 3);
	testExpectedSize(OAIPMHProfile.class, 4);
	testExpectedSize(Profiler.class, 15);
	testExpectedSize(WebRequestTransformer.class, 0);
	testExpectedSize(DataValidator.class, 10);
	// IDriverConnector is no longer registered as a service
	testExpectedSize(IDriverConnector.class, 0);
	testExpectedSize(IResourceMapper.class, 50);
	testExpectedSize(MessageResponseFormatter.class, 0);
	testExpectedSize(MessageResponseMapper.class, 0);
    }

    @Test
    public void testProfilers() {

	PluginsLoader<Profiler> pluginsLoader = new PluginsLoader<>();
	List<Profiler> profilers = pluginsLoader.loadPlugins(Profiler.class);
	HashSet<String> expectedProfilerNames = new HashSet<String>(Arrays.asList(//
		"SOSProfiler", //
		"HydroCSVProfiler", //
		"OAIPMHProfiler", //
		"GWISProfiler", //
		"GWPSProfiler", //
		"WMSProfiler", //
		"OSProfiler", //
		"CSWProfiler", //
		"PubSubProfiler", //
		"CSWPubSubProfiler", //
		"HISCentralProfiler", //
		"RestProfiler", //
		"HydroServerProfiler", //
		"CSWISOGeoProfiler", //
		"KMAOAIPMHProfiler", //
		"ARPARestProfiler" //
	));

	HashSet<String> profilerNames = new HashSet<String>();
	for (Profiler profiler : profilers) {
	    String name = profiler.getClass().getSimpleName();
	    profilerNames.add(name);
	}

	for (String expectedProfilerName : expectedProfilerNames) {
	    System.out.println("Checking: " + expectedProfilerName);
	    Assert.assertTrue(profilerNames.contains(expectedProfilerName));
	}

    }

}
