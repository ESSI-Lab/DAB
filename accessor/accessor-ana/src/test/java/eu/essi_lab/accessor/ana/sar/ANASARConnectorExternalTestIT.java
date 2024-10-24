package eu.essi_lab.accessor.ana.sar;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class ANASARConnectorExternalTestIT {

    @Test
    public void test() throws IOException, GSException {
	ANASARConnector connector = new ANASARConnector();
	connector.getSetting().setMaxRecords(1);

	String token = null;
//	while (true) {
	    System.out.println("Token: " + token);
	    ListRecordsRequest request = new ListRecordsRequest();
	    request.setResumptionToken(token);
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    List<OriginalMetadata> list = response.getRecordsAsList();
	    token = response.getResumptionToken();
	    System.out.println(list.size() + " results");
	    assertTrue(!list.isEmpty());
	    for (OriginalMetadata record : list) {
		System.out.println(record.getMetadata());
	    }
	// if (token == null) {
	// return;
	// }
	// }
    }

}
