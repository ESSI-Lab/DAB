package eu.essi_lab.profiler.oaipmh.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformerFactory;
import eu.essi_lab.profiler.oaipmh.OAIPMHProfiler;
 
public class OAIPMHRequestTransformerFactoryTest {

    @Test
    public void test(){
	
	List<DiscoveryRequestTransformer> transformers = DiscoveryRequestTransformerFactory.loadTransformers(//
		new ESSILabProvider(), //
		OAIPMHProfiler.OAI_PMH_PROFILER_TYPE);//
	Assert.assertFalse(transformers.isEmpty());
	
	DiscoveryRequestTransformer webRequestTransformer = transformers.get(0);
	Assert.assertEquals(webRequestTransformer.getProfilerType(), OAIPMHProfiler.OAI_PMH_PROFILER_TYPE);
    }
}
