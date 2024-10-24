package eu.essi_lab.accessor.sos;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.essi_lab.accessor.sos.SOSProperties.SOSProperty;

public class SOSPropertiesTest {
    @Test
    public void testName() throws Exception {
	SOSProperties properties = new SOSProperties();
	String v = "foid";
	properties.setProperty(SOSProperty.FOI_ID, v);
	String str = properties.asString();
	System.out.println(str);
	SOSProperties properties2 = new SOSProperties(str);
	String p = properties2.getProperty(SOSProperty.FOI_ID);
	System.out.println(p);
	assertEquals(v, p);
    }
}
