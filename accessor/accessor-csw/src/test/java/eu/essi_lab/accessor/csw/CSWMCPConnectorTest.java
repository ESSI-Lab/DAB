package eu.essi_lab.accessor.csw;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.Capabilities;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

public class CSWMCPConnectorTest {

    private CSWMCP2Connector connector2;
    private CSWMCP2Connector connector1;

    @Before
    public void init() throws GSException {
	connector1 = Mockito.spy(new CSWMCP2Connector());
	connector2 = Mockito.spy(new CSWMCP2Connector());

	Mockito.doReturn(new Capabilities()).when(connector1).getCapabilities(Mockito.any(), Mockito.anyBoolean());

	Mockito.doReturn(null).when(connector2).getCapabilities(Mockito.any(), Mockito.anyBoolean());

    }

    @Test
    public void testConnector() throws GSException {
	Assert.assertEquals(CommonNameSpaceContext.GMD_NS_URI, connector2.getRequestedMetadataSchema());
	Assert.assertEquals(CommonNameSpaceContext.MCP_2_NS_URI, connector2.getReturnedMetadataSchema());
	GSSource source = Mockito.mock(GSSource.class);
	Mockito.when(source.getEndpoint()).thenReturn("http://www.aodn.org.au");
	Assert.assertFalse(connector2.supports(source));
	Assert.assertTrue(connector1.supports(source));
	Mockito.when(source.getEndpoint()).thenReturn("http://www.google.com");
	Assert.assertFalse(connector1.supports(source));
    }
}
