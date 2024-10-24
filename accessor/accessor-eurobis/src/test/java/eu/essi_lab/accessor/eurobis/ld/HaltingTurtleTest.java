package eu.essi_lab.accessor.eurobis.ld;

import java.io.InputStream;

import org.junit.Test;

public class HaltingTurtleTest {

	@Test
	public void test() throws Exception {
//		EurOBISLdClient client = new EurOBISLdClient();
		InputStream stream = HaltingTurtleTest.class.getClassLoader().getResourceAsStream("haltingturtle.ttl");
//		InputStream stream = client.getMetadata("https://marineinfo.org/id/dataset/466.ttl"); 396 536 feature of interest& keywords
		DCATDataset dataset = new DCATDataset(stream);
		dataset.print();
	}

}
