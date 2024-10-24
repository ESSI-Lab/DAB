package eu.essi_lab.bufr;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.bufr.datamodel.BUFRCollection;
import eu.essi_lab.bufr.datamodel.BUFRElement;
import eu.essi_lab.bufr.datamodel.BUFRRecord;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.resource.OriginalMetadata;

public class BUFRConnectorTest {

    @Test
    public void testName() throws Exception {
	MockedBUFRConnector c = new MockedBUFRConnector();
	c.setSourceURL("fake/Country");
	ListRecordsRequest listRecords = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = c.listRecords(listRecords);
	Iterator<OriginalMetadata> iterator = response.getRecords();
	int i = 0;
	while (iterator.hasNext()) {
	    GSLoggerFactory.getLogger(getClass()).info("Collection " + i++);
	    OriginalMetadata metadata = (OriginalMetadata) iterator.next();
	    ByteArrayInputStream bais = new ByteArrayInputStream(metadata.getMetadata().getBytes());
	    BUFRCollection collection = BUFRCollection.unmarshal(bais);
	    if (i == 1) {
		collection.marshal(System.out);
	    }
	    bais.close();
	    List<BUFRRecord> records = collection.getRecords();
	    GSLoggerFactory.getLogger(getClass()).info("Records #" + records.size());
	    for (BUFRRecord record : records) {
		List<BUFRElement> variableElements = record.identifyVariables();
		HashSet<String> variableNames = new HashSet<String>();
		for (BUFRElement variableElement : variableElements) {
		    GSLoggerFactory.getLogger(getClass()).info(variableElement.getName());
		    variableNames.add(variableElement.getName());
		}
		assertEquals(1, variableNames.size());
	    }
	}
    }

}
