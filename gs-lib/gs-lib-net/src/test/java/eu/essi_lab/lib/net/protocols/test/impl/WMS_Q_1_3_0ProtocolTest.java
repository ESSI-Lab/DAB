package eu.essi_lab.lib.net.protocols.test.impl;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.net.protocols.impl.WMS_Q_1_3_0Protocol;

/**
 * @author ilsanto
 */
public class WMS_Q_1_3_0ProtocolTest {

    @Test
    public void test() {

	WMS_Q_1_3_0Protocol protocol = new WMS_Q_1_3_0Protocol();

	Assert.assertEquals("WMS", protocol.getSrvType());

	Assert.assertEquals("urn:ogc:serviceType:WebMapService:1.3.0:HTTP:QualityProfile", protocol.getURNs()[0]);

	Assert.assertEquals("1.3.0", protocol.getSrvVersion());

	Assert.assertEquals("OGC Web Map Service 1.3.0 Protocol Quality Profile", protocol.getDescription());

    }


}