package eu.essi_lab.accessor.smhi;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class SMHIMetadataTest {

    @Test
    public void test() throws Exception {
	SMHIMetadata metadata = new SMHIMetadata();
	SMHIParameter parameter = new SMHIParameter();
	parameter.setKey("my-key");
	metadata.setParameter(parameter );	
	SMHIStation station = new SMHIStation();
	station.setKey("my-key");
	metadata.setStation(station);
	String out = metadata.marshal();
	System.out.println(out);
	ByteArrayInputStream is = new ByteArrayInputStream(out.getBytes());
	SMHIMetadata metadata2 = SMHIMetadata.unmarshal(is);
	assertEquals(metadata2.getParameter().getKey(), parameter.getKey());
	assertEquals(metadata2.getStation().getKey(), station.getKey());
    }

}
