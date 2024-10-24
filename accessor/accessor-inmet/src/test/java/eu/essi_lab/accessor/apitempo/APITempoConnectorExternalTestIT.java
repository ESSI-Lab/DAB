package eu.essi_lab.accessor.apitempo;

import org.junit.Test;

import eu.essi_lab.messages.listrecords.ListRecordsRequest;

public class APITempoConnectorExternalTestIT {
    @Test
    public void test() throws Exception {
	APITempoConnector connector = new APITempoConnector();
	connector.setSourceURL("https://apitempo.inmet.gov.br/plata/");
	APITempoClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
	ListRecordsRequest lrr = new ListRecordsRequest();
	lrr.setResumptionToken("20");
	connector.listRecords(lrr);
    }

}
