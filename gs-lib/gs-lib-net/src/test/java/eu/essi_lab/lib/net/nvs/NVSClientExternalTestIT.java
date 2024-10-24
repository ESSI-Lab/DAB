package eu.essi_lab.lib.net.nvs;

import static org.junit.Assert.*;

import org.junit.Test;

public class NVSClientExternalTestIT {

	@Test
	public void test() {
		NVSClient client = new NVSClient();
		String label = client.getLabel("http://vocab.nerc.ac.uk/collection/P02/current/TDNT/");
		System.out.println(label);
		assertTrue(label.equals("Dissolved total and organic nitrogen concentrations in the water column"));
		label = client.getLabel("http://vocab.nerc.ac.uk/collection/L05/current/30/");
		System.out.println(label);
		assertTrue(label.equals("discrete water samplers"));
		label = client.getLabel("http://vocab.nerc.ac.uk/collection/R03/current/PRES/");
		System.out.println(label);
		assertTrue(label.equals("Sea water pressure, equals 0 at sea-level"));		
	}


}
