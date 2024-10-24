package eu.essi_lab.lib.net.protocols.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import eu.essi_lab.lib.net.protocols.NetProtocols;

public class NetProtocolsTest {

    @Test
    public void test() {
	assertEquals(NetProtocols.WCS_1_0_0, NetProtocols.decodeFromIdentifier("urn:ogc:serviceType:WebCoverageService:1.0.0:HTTP"));
	assertEquals(NetProtocols.WCS_1_0_0, NetProtocols.decodeFromIdentifier("OGC:WCS-1.0-http-get"));
	assertEquals(NetProtocols.WCS_1_0_0, NetProtocols.decodeFromIdentifier("OGC:WCS-1.0.0-http-get-coverage"));
	assertNull( NetProtocols.decodeFromIdentifier("OGC:WCS-500.0.0-http-get-coverage"));
    }

}
