package eu.essi_lab.lib.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class KVPManglerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void nullTest() {
	exception.expect(RuntimeException.class);
	new KVPMangler(null);
    }

    @Test
    public void emptyTest() {
	exception.expect(RuntimeException.class);
	new KVPMangler("");
    }

    @Test
    public void test() {
	KVPMangler mangler = new KVPMangler(";");
	mangler.setMangling("k1;v1");
	mangler.setParameter("k2", "v2");
	assertEquals("k1;v1;k2;v2", mangler.getMangling());
	assertEquals("v1", mangler.getParameterValue("k1"));
	assertEquals("v2", mangler.getParameterValue("k2"));
	assertNull(mangler.getParameterValue("k3"));
    }

    @Test
    public void test2() {
	KVPMangler mangler = new KVPMangler(";");
	mangler.setMangling("");
	assertEquals("", mangler.getMangling());
	assertNull(mangler.getParameterValue("k2"));
    }

}
