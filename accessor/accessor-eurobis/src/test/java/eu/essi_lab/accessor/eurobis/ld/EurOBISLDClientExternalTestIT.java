package eu.essi_lab.accessor.eurobis.ld;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * @author boldrini
 */
public class EurOBISLDClientExternalTestIT {

    @Test
    public void test() throws Exception {
	EurOBISLdClient client = new EurOBISLdClient();
	List<String> urls = client.getDatasetURIs();
	assertTrue(!urls.isEmpty());
	for (String url : urls) {
	    assertTrue(url != null);
	    System.out.println(url);
	    InputStream s = client.getMetadata(0, ".ttl");
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    IOUtils.copy(s, baos);
	    String str = new String(baos.toByteArray());
//	    DCATDataset dataset = new DCATDataset(s);
//	    assertNotNull(dataset.getElement(RDFElement.TITLE));
//	    assertFalse(dataset.getElement(RDFElement.TITLE).isEmpty());
//	    dataset.print();
	    

	}

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
