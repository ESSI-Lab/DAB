package eu.essi_lab.profiler.opensearch.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformerFactory;
import eu.essi_lab.profiler.os.OSProfilerSetting;

public class OSRequestTransformerFactoryTest {

    @Test
    public void test() {

	List<DiscoveryRequestTransformer> transformers = DiscoveryRequestTransformerFactory.loadTransformers(//
		new ESSILabProvider(), //
		OSProfilerSetting.OPEN_SEARCH_PROFILER_TYPE);//
	Assert.assertFalse(transformers.isEmpty());

	DiscoveryRequestTransformer webRequestTransformer = transformers.get(0);
	Assert.assertEquals(webRequestTransformer.getProfilerType(), OSProfilerSetting.OPEN_SEARCH_PROFILER_TYPE);
    }
}
