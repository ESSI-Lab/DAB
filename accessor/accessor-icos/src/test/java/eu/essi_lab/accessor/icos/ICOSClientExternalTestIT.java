package eu.essi_lab.accessor.icos;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class ICOSClientExternalTestIT {

	@Test
	public void test() throws IOException {
		ICOSClient client = new ICOSClient("https://meta.icos-cp.eu/sparql");
		List<String> concepts = client.getEquivalentConcepts("http://meta.icos-cp.eu/resources/cpmeta/salinity");
		assertTrue(concepts.size()==1);
		assertTrue(concepts.contains("http://vocab.nerc.ac.uk/collection/P01/current/PSLTZZ01/"));
		concepts = client.getEquivalentConcepts("http://meta.icos-cp.eu/resources/cpmeta/fake");
		assertTrue(concepts.size()==0);
	}

}
