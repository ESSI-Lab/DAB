package eu.essi_lab.lib.net.protocols.test.impl;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.net.protocols.impl.SOS_2_0_0Protocol;

/**
 * @author ilsanto
 */
public class SOS_2_0_0ProtocolTest {

    @Test
    public void test() {

	SOS_2_0_0Protocol protocol = new SOS_2_0_0Protocol();

	Assert.assertEquals("SOS", protocol.getSrvType());

	Assert.assertEquals("urn:ogc:serviceType:SensorObservationService:2.0.0:HTTP", protocol.getURNs()[0]);

	Assert.assertEquals("2.0.0", protocol.getSrvVersion());

	Assert.assertEquals("OGC Sensor Observation Service 2.0.0 Protocol", protocol.getDescription());

    }
}