package eu.essi_lab.accessor.eurobis.ld;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Test;

/**
 * @author boldrini
 */
public class SpecificDatasetExternalTestIT {

    @Test
    public void test() throws Exception {
	EurOBISLdClient client = new EurOBISLdClient();
	String url = "https://marineinfo.org/id/dataset/8274.ttl";
	assertTrue(url != null);
	System.out.println(url);
	InputStream s = client.getMetadata(url);
	DCATDataset dataset = new DCATDataset(s);
	dataset.print();
	assertNotNull(dataset.getElement(RDFElement.TITLE));
	assertNotNull(dataset.getElement(RDFElement.IDENTIFIER));
	assertFalse(dataset.getElement(RDFElement.TITLE).isEmpty());
	assertFalse(dataset.getElement(RDFElement.IDENTIFIER).isEmpty());
	System.out.println(dataset.getElements(RDFElement.ISPARTOF));

	// for (int i = 650; i < urls.size(); i++) {
	// url = urls.get(i);
	// System.out.println("doing " + i + " " + url);
	// s = client.getMetadata(0, ".ttl");
	// dataset = new DCATDataset(s);
	// System.out.println("done " + i + " " + url);
	//
	// }

    }

}
