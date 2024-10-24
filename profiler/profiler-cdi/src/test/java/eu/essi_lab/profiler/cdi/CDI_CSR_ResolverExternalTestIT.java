package eu.essi_lab.profiler.cdi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.net.nvs.NVSClient;

public class CDI_CSR_ResolverExternalTestIT {

    private NVSClient resolver;

    @Before
    public void init() {
	this.resolver = new NVSClient();
    }

    @Test
    public void test() {
	Assert.assertEquals("sea level recorders", resolver.getLabel("http://www.seadatanet.org/urnurl/SDN:L05::111"));
    }
}
