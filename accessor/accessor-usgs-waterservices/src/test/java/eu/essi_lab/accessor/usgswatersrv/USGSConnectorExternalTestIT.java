package eu.essi_lab.accessor.usgswatersrv;

import org.junit.Test;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class USGSConnectorExternalTestIT {

    @Test
    public void test() throws GSException {
	USGSConnector connector = new USGSConnector();
	connector.setSourceURL("https://waterservices.usgs.gov/nwis/site?");
	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> results = connector.listRecords(request);
	String resumption = results.getResumptionToken();
	int i = 10;
	while (resumption != null) {
	    request.setResumptionToken(resumption);
	    results = connector.listRecords(request);
	    resumption = results.getResumptionToken();
	    if (i--<0) {
		break;
	    }
	}

    }

}
