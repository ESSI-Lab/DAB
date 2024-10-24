package eu.essi_lab.accessor.thredds;

import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Ignored
 * 
 * @author Fabrizio
 */
public class THREDDSConnectorExternalTestIt {

    @Test
    @Ignore
    public void test() throws GSException, InterruptedException {
	THREDDSConnector connector = new THREDDSConnector();
	connector.setSourceURL("https://data.nodc.noaa.gov/thredds/catalog/ncei/wod/catalog.xml");
	connector.setSourceURL("https://thredds.ucar.edu/thredds/idd/forecastModels.xml");
	connector.setSourceURL("https://thredds.ucar.edu/thredds/idd/obsData.xml");
	String token = null;
	ListRecordsRequest req = new ListRecordsRequest();
	req.setResumptionToken(token);
	do {
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(req);
	    token = response.getResumptionToken();
	    req.setResumptionToken(token);
	    System.out.println(token);
	    // Thread.sleep(2000);
	} while (token != null);
    }

}
