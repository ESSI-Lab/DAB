package eu.essi_lab.accessor.inmet;

import java.util.Iterator;

import org.junit.Ignore;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class INMETUruguayExternalTestIT {

    /**
     * Ignored, it is an obsolete accessor
     * 
     * @throws Exception
     */
    @Ignore
    public void testName() throws Exception {
	INMETConnector connector = new INMETConnector();
	connector.setSourceURL("ftp://ftp.inmet.gov.br/bufr");
	ListRecordsRequest listRecords = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> records = connector.listRecords(listRecords);
	Iterator<OriginalMetadata> iterator = records.getRecords();
	while (iterator.hasNext()) {
	    OriginalMetadata metadata = (OriginalMetadata) iterator.next();

	}

    }

}
