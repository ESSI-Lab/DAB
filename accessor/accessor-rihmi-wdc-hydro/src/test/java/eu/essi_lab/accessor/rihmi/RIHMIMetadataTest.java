package eu.essi_lab.accessor.rihmi;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class RIHMIMetadataTest {

    @Test
    public void test() throws IOException {
	RIHMIMetadata m = new RIHMIMetadata();
	m.setParameterId("p1");
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	m.marshal(baos);
	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	RIHMIMetadata m2 = m.unmarshal(bais);
	assertEquals(m.getParameterId(), m2.getParameterId());
	baos.close();
	bais.close();
    }

}
