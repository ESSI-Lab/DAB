package eu.essi_lab.accessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.TreeSet;

import org.junit.Test;

import eu.essi_lab.accessor.imo.IMOClient;
import eu.essi_lab.accessor.imo.ZRXPBlock;
import eu.essi_lab.accessor.imo.ZRXPDocument;

public class IMOClientExternalTestIT {

    @Test
    public void test() throws Exception {
	IMOClient client = new IMOClient();
	IMOClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));

	List<ZRXPDocument> all = client.downloadAll();

	assertTrue(!all.isEmpty());

	TreeSet<String> identifiers = new TreeSet<>();

	for (ZRXPDocument doc : all) {
	    assertTrue(!doc.getBlocks().isEmpty());
	    for (ZRXPBlock b0 : doc.getBlocks()) {
		identifiers.add(b0.getStationIdentifier());
		String units = b0.getUnit();
		assertEquals("m³/s", units);
		doc.getFile().delete();
	    }

	}

	for (String id : identifiers) {
	    System.out.println(id.substring(1));
	}

    }

}
