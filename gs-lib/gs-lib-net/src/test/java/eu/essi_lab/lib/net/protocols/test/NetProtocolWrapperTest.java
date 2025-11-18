package eu.essi_lab.lib.net.protocols.test;

import org.junit.Test;

import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Fabrizio
 */
public class NetProtocolWrapperTest {

    @Test
    public void test() {

	assertEquals(NetProtocolWrapper.WCS_1_0_0.get(), NetProtocolWrapper.get("urn:ogc:serviceType:WebCoverageService:1.0.0:HTTP").get());

	assertEquals(NetProtocolWrapper.WCS_1_0_0.get(), NetProtocolWrapper.get("OGC:WCS-1.0-http-get").get());

	assertEquals(NetProtocolWrapper.WCS_1_0_0.get(), NetProtocolWrapper.get("OGC:WCS-1.0.0-http-get-coverage").get());

	assertTrue(NetProtocolWrapper.get("OGC:WCS-500.0.0-http-get-coverage").isEmpty());

	boolean bool1 = NetProtocolWrapper.check("urn:ogc:serviceType:WebMapService:1.1.1:HTTP", NetProtocolWrapper.WMS_1_1_1);

	boolean bool2 = NetProtocolWrapper.check("OGC:WMS-1.1.1-http-get", NetProtocolWrapper.WMS_1_1_1);

	boolean bool3 = NetProtocolWrapper.check("OGC:WMS-1.1.1-http-get-map", NetProtocolWrapper.WMS_1_1_1);

	boolean bool4 = NetProtocolWrapper.check("OGC:WMS-1.1.1-http-get-map-xxx", NetProtocolWrapper.WMS_1_1_1);

	assertTrue(bool1);
	assertTrue(bool2);
	assertTrue(bool3);
	assertFalse(bool4);
    }
}
