package eu.essi_lab.lib.net.edmo;

import static org.junit.Assert.*;

import org.junit.Test;

public class EDMOClientExternalTestIT {

    @Test
    public void test() {
	EDMOClient client = new EDMOClient();
	String label = client.getLabelFromCode("1");
	System.out.println(label);
	assertTrue(label.equals("University of Birmingham, Department of Geological Sciences"));
    }

}
